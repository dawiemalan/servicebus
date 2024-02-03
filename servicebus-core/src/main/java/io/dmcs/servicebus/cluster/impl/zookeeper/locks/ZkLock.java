package io.dmcs.servicebus.cluster.impl.zookeeper.locks;

import io.dmcs.servicebus.cluster.impl.zookeeper.ZkClusterManager;
import io.dmcs.servicebus.cluster.locks.DistributedLock;
import lombok.Getter;
import lombok.SneakyThrows;
import org.apache.curator.framework.recipes.locks.InterProcessMutex;

import java.util.concurrent.TimeUnit;

public class ZkLock implements DistributedLock {

    @Getter
    protected final ZkClusterManager clusterManager;
    @Getter
    protected final String name;
    protected final InterProcessMutex mutex;

    protected ZkLock(ZkClusterManager clusterManager, String name) {

        this.clusterManager = clusterManager;
        this.name = clusterManager.normalizePath(name);
        this.mutex = new InterProcessMutex(clusterManager.getCurator(), this.name);
    }

    @Override
    @SneakyThrows
    public boolean acquire() {
        mutex.acquire();
        return true;
    }

    @Override
    @SneakyThrows
    public boolean acquire(long time, TimeUnit unit) {
        return mutex.acquire(time, unit);
    }

    @Override
    @SneakyThrows
    public void release() {
        mutex.release();
    }

    @Override
    public boolean isAcquiredInThisProcess() {
        return mutex.isAcquiredInThisProcess();
    }

    public static ZkLock of(ZkClusterManager clusterManager, String name) {
        return new ZkLock(clusterManager, name);
    }
}
