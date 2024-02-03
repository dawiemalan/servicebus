package io.dmcs.servicebus.cluster.impl.zookeeper;

import io.dmcs.servicebus.PlatformSupport;
import io.dmcs.servicebus.cluster.impl.AbstractClusterManager;
import io.dmcs.servicebus.cluster.impl.zookeeper.leadership.ZkLeaderGroup;
import io.dmcs.servicebus.cluster.impl.zookeeper.locks.ZkLock;
import io.dmcs.servicebus.cluster.leadership.LeaderGroup;
import io.dmcs.servicebus.cluster.locks.DistributedLock;
import io.dmcs.servicebus.config.ServiceBusProperties;
import lombok.Getter;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.state.ConnectionState;
import org.apache.curator.framework.state.ConnectionStateListener;
import org.apache.curator.framework.state.ConnectionStateListenerManagerFactory;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.curator.utils.CloseableUtils;
import org.apache.curator.x.async.AsyncCuratorFramework;

import java.util.concurrent.TimeUnit;

@Slf4j
public class ZkClusterManager extends AbstractClusterManager implements ConnectionStateListener {

    @Getter
    protected CuratorFramework curator;

    @Getter
    protected ZkServiceRegistry serviceRegistry;

    public ZkClusterManager(ServiceBusProperties config, PlatformSupport platformSupport) {

        super(config, platformSupport);
    }

    @SneakyThrows
    @Override
    public void start() {

        this.curator = CuratorFrameworkFactory.builder()
                .connectString(StringUtils.join(config.getServerUrls(), ","))
                .retryPolicy(new ExponentialBackoffRetry(3000, 29, 10000))
                .connectionStateListenerManagerFactory(
                        ConnectionStateListenerManagerFactory
                                .circuitBreaking(new ExponentialBackoffRetry(3000, 29, 10000))
                )
                .build();

        this.curator.getConnectionStateListenable().addListener(this);
        this.curator.start();
        if (!curator.blockUntilConnected(5, TimeUnit.SECONDS))
            log.error("Failed to connect to Zookeeper cluster [{}], will keep trying", curator.getZookeeperClient().getCurrentConnectionString());

        platformSupport.registerBean(curator);

        serviceRegistry = new ZkServiceRegistry(AsyncCuratorFramework.wrap(curator), platformSupport);
        serviceRegistry.start();
    }

    @Override
    public DistributedLock getLock(String lockName) {
        return ZkLock.of(this, lockName);
    }

    @Override
    public LeaderGroup getLeaderGroup(String name) {

        String groupName = normalizePath(name);
        ZkLeaderGroup leaderGroup = (ZkLeaderGroup) leaderGroups.get(groupName);
        if (leaderGroup != null)
            return leaderGroup;

        leaderGroup = new ZkLeaderGroup(this, groupName);
        leaderGroups.put(groupName, leaderGroup);

        return leaderGroup;
    }

    @Override
    public void stop() {
        super.stop();
        CloseableUtils.closeQuietly(this.serviceRegistry);
        CloseableUtils.closeQuietly(this.curator);
    }

    @Override
    public void stateChanged(CuratorFramework client, ConnectionState newState) {

        if (!newState.isConnected())
            log.error("Disconnected from Zookeeper cluster [{}]", curator.getZookeeperClient().getCurrentConnectionString());
        else
            log.info("Connected to Zookeeper cluster: [{}]", curator.getZookeeperClient().getCurrentConnectionString());

        notifyClusterStateChanged(newState.isConnected());
    }

    @Override
    public boolean equals(Object o) {
        return super.equals(o);
    }

    @Override
    public int hashCode() {
        return super.hashCode();
    }
}
