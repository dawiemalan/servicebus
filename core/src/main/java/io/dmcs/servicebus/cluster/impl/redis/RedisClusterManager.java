package io.dmcs.servicebus.cluster.impl.redis;

import io.dmcs.servicebus.PlatformSupport;
import io.dmcs.servicebus.cluster.impl.AbstractClusterManager;
import io.dmcs.servicebus.cluster.impl.redis.locks.RedisLock;
import io.dmcs.servicebus.cluster.leadership.LeaderGroup;
import io.dmcs.servicebus.cluster.leadership.LeaderSelector;
import io.dmcs.servicebus.cluster.locks.DistributedLock;
import io.dmcs.servicebus.config.ServiceBusProperties;
import lombok.Getter;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RedissonClient;

@Slf4j
@Getter
public class RedisClusterManager extends AbstractClusterManager {

    protected RedissonClient redisson;
    protected RedisServiceRegistry serviceRegistry;

    public RedisClusterManager(ServiceBusProperties config,
                               PlatformSupport platformSupport,
                               RedissonClient redisson) {

        super(config, platformSupport);
        this.redisson = redisson;
        this.serviceRegistry = new RedisServiceRegistry(this);
    }

    @SneakyThrows
    @Override
    public void start() {

        if (this.redisson == null)
            this.redisson = platformSupport.locateBean(RedissonClient.class).orElseThrow(() ->
                    new ExceptionInInitializerError("RedissonClient bean not found"));

        serviceRegistry.start();
        notifyClusterStateChanged(true);
        log.debug("Redis cluster manager started.");
    }

    @Override
    public DistributedLock getLock(String lockName) {
        return RedisLock.of(this, normalizePath(lockName));
    }

    @Override
    public LeaderGroup getLeaderGroup(String name) {

        String groupName = normalizePath(name);
        LeaderGroup leaderGroup = leaderGroups.get(groupName);
        if (leaderGroup != null)
            return leaderGroup;

        leaderGroup = new LeaderSelector(this, groupName);
        leaderGroups.put(groupName, leaderGroup);

        return leaderGroup;
    }

    @Override
    public void stop() {
        super.stop();
//		CloseableUtils.closeQuietly(this.serviceRegistry);
//		CloseableUtils.closeQuietly(this.curator);
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
