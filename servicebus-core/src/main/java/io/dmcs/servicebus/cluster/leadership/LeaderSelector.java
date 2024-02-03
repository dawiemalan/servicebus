/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package io.dmcs.servicebus.cluster.leadership;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Preconditions;
import io.dmcs.common.utils.CloseableExecutorService;
import io.dmcs.common.utils.ThreadUtils;
import io.dmcs.servicebus.cluster.ClusterManager;
import io.dmcs.servicebus.cluster.ClusterStateListener;
import io.dmcs.servicebus.cluster.locks.DistributedLock;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.io.Closeable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

/**
 * <p>
 * Abstraction to select a "leader" amongst multiple contenders in a group of JMVs connected
 * to a cluster. If a group of N thread/processes contends for leadership, one will
 * be assigned leader until it releases leadership at which time another one from the group will
 * be chosen.
 * </p>
 * <p>
 * Note that this class uses an underlying {@link DistributedLock} and as a result leader
 * election is "fair" - each user will become leader in the order originally requested.
 * </p>
 */
@Slf4j
public class LeaderSelector extends LeaderGroup implements Closeable, ClusterStateListener {

    private final CloseableExecutorService executorService;
    private final DistributedLock mutex;
    private final AtomicReference<State> state = new AtomicReference<>(State.LATENT);
    private final AtomicBoolean autoRequeue = new AtomicBoolean(true);
    private final AtomicReference<Future<?>> ourTask = new AtomicReference<>(null);

    @Getter
    private volatile boolean leader;
    @Getter
    private volatile String id = "";

    @VisibleForTesting
    volatile CountDownLatch debugLeadershipLatch = null; // NOSONAR
    volatile CountDownLatch leadershipWaitLatch = null; // NOSONAR

    private enum State {
        LATENT,
        STARTED,
        CLOSED
    }

    // guarded by synchronization
    private boolean isQueued = false;

    private static final ThreadFactory defaultThreadFactory = ThreadUtils.newThreadFactory("LeaderSelector");

    /**
     * @param clusterManager the clusterManager
     * @param name           the path for this leadership group
     */
    public LeaderSelector(ClusterManager clusterManager, String name) {
        this(clusterManager, name, new CloseableExecutorService(Executors.newSingleThreadExecutor(defaultThreadFactory), true));
    }

    /**
     * @param clusterManager  the clusterManager
     * @param groupName       the path for this leadership group
     * @param executorService thread pool to use
     */
    public LeaderSelector(ClusterManager clusterManager, String groupName, CloseableExecutorService executorService) {

        super(clusterManager, groupName);

        leader = false;
        this.executorService = executorService;
        mutex = clusterManager.getLock(groupName);
        start();
    }

    /**
     * Sets the ID to store for this leader. <br>
     * IMPORTANT: must be called prior to {@link #start()} to have effect.
     *
     * @param id ID
     */
    public void setId(String id) {

        Preconditions.checkNotNull(id, "id cannot be null");
        this.id = id;
    }

    /**
     * Attempt leadership. This attempt is done in the background - i.e. this method returns
     * immediately.
     */
    public void start() {

        if (state.get() == State.STARTED)
            return;

        clusterManager.addClusterStateListener(this);
        state.set(State.STARTED);
        requeue();
    }

    /**
     * Re-queue an attempt for leadership. If this instance is already queued, nothing
     * happens and false is returned. If the instance was not queued, it is re-queued and true
     * is returned
     *
     * @return true if re-queue is successful
     */
    public boolean requeue() {

        if (state.get() == State.CLOSED)
            return false;

        return internalRequeue();
    }

    private synchronized boolean internalRequeue() {

        if (!isQueued && (state.get() == State.STARTED)) {

            isQueued = true;
            Future<Void> task = executorService.submit(() -> {
                try {
                    doWorkLoop();
                } finally {
                    clearIsQueued();
                    if (autoRequeue.get()) {
                        internalRequeue();
                    }
                }
                return null;
            });
            ourTask.set(task);

            return true;
        }
        return false;
    }

    /**
     * Shutdown this selector and remove from the leadership group
     */
    public synchronized void close() {

        if (state.get() == State.CLOSED)
            return;

        state.set(State.CLOSED);
        clusterManager.removeClusterStateListener(this);
        executorService.close();

        mutex.release();
        ourTask.set(null);
    }

    /**
     * Attempt to cancel and interrupt the current leadership if this instance has leadership
     */
    public synchronized void interruptLeadership() {

        Future<?> task = ourTask.get();
        if (task != null)
            task.cancel(true);
    }

    @VisibleForTesting
    volatile AtomicInteger failedMutexReleaseCount = null; // NOSONAR

    @VisibleForTesting
    void doWork() throws Exception {

        leader = false;

        try {

            log.trace("[{}] await mutex", this.mutex.getName());
            mutex.acquire();
            log.trace("[{}] mutex acquired", this.mutex.getName());
            leadershipWaitLatch = new CountDownLatch(1);
            leader = true;
            awaitLatch();

        } catch (InterruptedException e) {

            Thread.currentThread().interrupt();
            throw e;
        } finally {

            if (leader) {
                leader = false;
                boolean wasInterrupted = Thread.interrupted();  // clear any interrupted status so that mutex.release() works immediately
                try {
                    mutex.release();
                } catch (Exception e) {

                    if (failedMutexReleaseCount != null)
                        failedMutexReleaseCount.incrementAndGet();

                    ThreadUtils.checkInterrupted(e);
                    log.error("[{}] leader threw an exception", getName(), e);

                } finally {
                    if (wasInterrupted) {
                        Thread.currentThread().interrupt();
                    }
                }
            }
        }
    }

    /**
     * Parks the leadership thread on latch
     */
    private void awaitLatch() throws InterruptedException {

        try {

            log.trace("[{}] leadership changed", this.mutex.getName());
            listeners.forEach(listener -> listener.onLeadershipChanged(this));

            if (debugLeadershipLatch != null)
                debugLeadershipLatch.countDown();

            if (leadershipWaitLatch != null)
                leadershipWaitLatch.await();

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw e;
        } catch (Throwable e) { // NOSONAR
            ThreadUtils.checkInterrupted(e);
        } finally {
            clearIsQueued();
        }
    }

    private void doWorkLoop() throws Exception {

        Exception exception = null;
        try {
            doWork();
        } catch (InterruptedException ignore) {
            Thread.currentThread().interrupt();
        } catch (Exception e) {
            exception = e;
        }

        if ((exception != null) && !autoRequeue.get())   // autoRequeue should ignore connection loss or session expired and just keep trying
            throw exception;
    }

    private synchronized void clearIsQueued() {
        isQueued = false;
    }

    @Override
    public void clusterStateChanged(ClusterManager clusterManager, boolean connected) {
        if (!connected)
            interruptLeadership();
    }
}
