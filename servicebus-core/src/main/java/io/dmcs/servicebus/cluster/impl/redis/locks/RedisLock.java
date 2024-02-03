package io.dmcs.servicebus.cluster.impl.redis.locks;

import io.dmcs.servicebus.cluster.impl.redis.RedisClusterManager;
import io.dmcs.servicebus.cluster.locks.DistributedLock;
import lombok.Getter;
import lombok.Setter;
import lombok.SneakyThrows;
import org.redisson.api.LockOptions;
import org.redisson.api.RLock;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

public class RedisLock implements DistributedLock {

    @Getter
    protected final RedisClusterManager clusterManager;
    @Getter
    protected final String name;
    protected final RLock rLock;

    /**
     * Default lock lease time (5 minutes)
     */
    @Getter
    @Setter
    private Duration lockLeaseTime = Duration.ofSeconds(5);

    protected RedisLock(RedisClusterManager clusterManager, String name) {

        this.clusterManager = clusterManager;
        this.name = clusterManager.normalizePath(name);
        this.rLock = clusterManager.getRedisson().getSpinLock(this.name,
                new LockOptions.ConstantBackOff().delay(1000));
    }

    @Override
    @SneakyThrows
    public boolean acquire() {

        rLock.lock(); //lockLeaseTime.toMillis(), TimeUnit.MILLISECONDS);
        return rLock.isHeldByCurrentThread();
    }

    @Override
    @SneakyThrows
    public boolean acquire(long time, TimeUnit unit) {
        return rLock.tryLock(time, unit);
    }

    @Override
    @SneakyThrows
    public void release() {
        if (rLock.isHeldByCurrentThread())
            rLock.unlock();
    }

    @Override
    public boolean isAcquiredInThisProcess() {
        return rLock.isHeldByCurrentThread();
    }

    public static RedisLock of(RedisClusterManager clusterManager, String name) {
        return new RedisLock(clusterManager, name);
    }
}
