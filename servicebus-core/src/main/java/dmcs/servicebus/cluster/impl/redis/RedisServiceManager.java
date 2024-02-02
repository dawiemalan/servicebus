package dmcs.servicebus.cluster.impl.redis;

import dmcs.common.utils.Startable;
import dmcs.servicebus.PlatformSupport;
import dmcs.servicebus.cluster.ClusterManager;
import dmcs.servicebus.cluster.ClusterStateListener;
import dmcs.servicebus.cluster.leadership.LeaderGroup;
import dmcs.servicebus.services.AbstractServiceManager;
import dmcs.servicebus.services.ServiceRegistration;
import lombok.Getter;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.curator.utils.CloseableUtils;
import org.redisson.api.RedissonClient;

import java.util.Collection;
import java.util.Comparator;
import java.util.Set;

@Slf4j
public class RedisServiceManager extends AbstractServiceManager implements ClusterStateListener {

    private static final String CLUSTER_PREFIX = "/cluster";
    private static final String SERVICE_LEADER_GROUPS = CLUSTER_PREFIX + "/services/leaders/";
    @Getter
    private RedisClusterManager redisClusterManager;

    @Getter
    private RedissonClient redisson;

    public RedisServiceManager(ClusterManager clusterManager, PlatformSupport platformSupport) {

        super(clusterManager, platformSupport);
        this.redisClusterManager = (RedisClusterManager) clusterManager;
    }

    @Override
    public Set<Startable> getDependencies() {
        return Set.of(redisClusterManager);
    }

    @Override
    public void start() {

        this.redisson = redisClusterManager.getRedisson();
        assert this.redisson != null;
        redisClusterManager.addClusterStateListener(this);

        String groupName = serviceRegistration.getServiceGroup();
        this.leaderGroup = redisClusterManager
                .getLeaderGroup(SERVICE_LEADER_GROUPS + groupName);

        this.leaderGroup.addElectionListener(this);

        // register ourselves
        registerSelf();
    }

    @Override
    @SneakyThrows
    public void onLeadershipChanged(LeaderGroup leaderGroup) {

        super.onLeadershipChanged(leaderGroup);
        //redisClusterManager.serviceRegistry.set(instance).get();
    }

    @Override
    public synchronized void stop() {

        if (redisClusterManager == null)
            return;

        CloseableUtils.closeQuietly(leaderGroup);
        redisClusterManager.serviceRegistry.remove(serviceRegistration);
        redisClusterManager = null;
    }

    @SneakyThrows
    @Override
    protected void registerSelf() {

        redisClusterManager.serviceRegistry.set(serviceRegistration);
        log.info("Registered self as service: {}", serviceRegistration);
    }

    @Override
    public void clusterStateChanged(ClusterManager clusterManager, boolean connected) {

        if (connected)
            registerSelf();
    }

    @Override
    @SneakyThrows
    public Collection<ServiceRegistration> listServices() {
        return redisClusterManager.getServiceRegistry().list().stream()
                .sorted(Comparator.comparing(ServiceRegistration::getName))
                .toList();
    }
}
