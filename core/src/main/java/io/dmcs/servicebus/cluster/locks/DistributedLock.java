package io.dmcs.servicebus.cluster.locks;

import io.dmcs.servicebus.cluster.ClusterManager;

import java.util.concurrent.TimeUnit;

public interface DistributedLock {

    /**
     * The {@link ClusterManager} used to create this lock
     *
     * @return
     */
    ClusterManager getClusterManager();

    /**
     * Returns the lock name (or path)
     *
     * @return
     */
    String getName();

    /**
     * Acquire the mutex - blocking until it's available. Each call to acquire must be balanced by a call
     * to {@link #release()}
     */
    boolean acquire();

    /**
     * Acquire the mutex - blocks until it's available or the given time expires. Each call to acquire that returns true must be balanced by a call
     * to {@link #release()}
     *
     * @param time time to wait
     * @param unit time unit
     * @return true if the mutex was acquired, false if not
     * @throws Exception ZK errors, connection interruptions
     */
    boolean acquire(long time, TimeUnit unit);

    /**
     * Perform one release of the mutex.
     */
    void release();

    /**
     * Returns true if the mutex is acquired by a thread in this JVM
     */
    boolean isAcquiredInThisProcess();

}
