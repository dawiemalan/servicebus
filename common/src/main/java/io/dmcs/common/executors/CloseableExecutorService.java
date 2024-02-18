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

package io.dmcs.common.executors;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Preconditions;
import com.google.common.collect.Maps;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

import java.io.Closeable;
import java.util.Collections;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;
import java.util.concurrent.RunnableFuture;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Decoration on an ExecutorService that tracks created futures and provides
 * a method to close futures created via this class
 */
@SuppressWarnings("unused")
@Slf4j
public class CloseableExecutorService implements Closeable {

    private final Set<Future<?>> futures = Collections.newSetFromMap(Maps.<Future<?>, Boolean>newConcurrentMap());
    private final ExecutorService executorService;
    private final boolean shutdownOnClose;
    protected final AtomicBoolean isOpen = new AtomicBoolean(true);

    protected class InternalScheduledFutureTask implements Future<Void> {
        private final ScheduledFuture<?> scheduledFuture;

        public InternalScheduledFutureTask(ScheduledFuture<?> scheduledFuture) {
            this.scheduledFuture = scheduledFuture;
            futures.add(scheduledFuture);
        }

        @Override
        public boolean cancel(boolean mayInterruptIfRunning) {
            futures.remove(scheduledFuture);
            return scheduledFuture.cancel(mayInterruptIfRunning);
        }

        @Override
        public boolean isCancelled() {
            return scheduledFuture.isCancelled();
        }

        @Override
        public boolean isDone() {
            return scheduledFuture.isDone();
        }

        @Override
        public Void get() throws InterruptedException, ExecutionException {
            return null;
        }

        @Override
        public Void get(long timeout, @NonNull TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
            return null;
        }
    }

    protected class InternalFutureTask<T> extends FutureTask<T> {
        private final RunnableFuture<T> task;

        InternalFutureTask(RunnableFuture<T> task) {
            super(task, null);
            this.task = task;
            futures.add(task);
        }

        @Override
        protected void done() {
            futures.remove(task);
        }
    }

    /**
     * @param executorService the service to decorate
     */
    public CloseableExecutorService(ExecutorService executorService) {
        this(executorService, false);
    }

    /**
     * @param executorService the service to decorate
     * @param shutdownOnClose if true, shutdown the executor service when this is closed
     */
    public CloseableExecutorService(ExecutorService executorService, boolean shutdownOnClose) {
        this.executorService = Preconditions.checkNotNull(executorService, "executorService cannot be null");
        this.shutdownOnClose = shutdownOnClose;
    }

    /**
     * Returns <tt>true</tt> if this executor has been shut down.
     *
     * @return <tt>true</tt> if this executor has been shut down
     */
    public boolean isShutdown() {
        return !isOpen.get();
    }

    @VisibleForTesting
    int size() {
        return futures.size();
    }

    /**
     * Closes any tasks currently in progress
     */
    @Override
    public void close() {
        isOpen.set(false);
        Iterator<Future<?>> iterator = futures.iterator();
        while (iterator.hasNext()) {
            Future<?> future = iterator.next();
            iterator.remove();
            if (!future.isDone() && !future.isCancelled() && !future.cancel(true)) {
                log.warn("Could not cancel " + future);
            }
        }
        if (shutdownOnClose) {
            executorService.shutdownNow();
        }
    }

    /**
     * Submits a value-returning task for execution and returns a Future
     * representing the pending results of the task.  Upon completion,
     * this task may be taken or polled.
     *
     * @param task the task to submit
     * @return a future to watch the task
     */
    public <V> Future<V> submit(Callable<V> task) {
        Preconditions.checkState(isOpen.get(), "CloseableExecutorService is closed");

        InternalFutureTask<V> futureTask = new InternalFutureTask<>(new FutureTask<>(task));
        executorService.execute(futureTask);
        return futureTask;
    }

    /**
     * Submits a Runnable task for execution and returns a Future
     * representing that task.  Upon completion, this task may be
     * taken or polled.
     *
     * @param task the task to submit
     * @return a future to watch the task
     */
    public Future<?> submit(Runnable task) {
        Preconditions.checkState(isOpen.get(), "CloseableExecutorService is closed");

        InternalFutureTask<Void> futureTask = new InternalFutureTask<Void>(new FutureTask<>(task, null));
        executorService.execute(futureTask);
        return futureTask;
    }
}
