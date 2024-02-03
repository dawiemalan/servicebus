package io.dmcs.servicebus.cluster.impl.zookeeper.locks;

import org.apache.curator.framework.recipes.locks.InterProcessSemaphoreV2;

public class ZkSemaphore {

    protected final InterProcessSemaphoreV2 mutex;

    protected ZkSemaphore(InterProcessSemaphoreV2 mutex) {
        this.mutex = mutex;
    }

//	@Override
//	public void acquire() throws Exception {
//		mutex.acquire();
//	}
//
//	@Override
//	public Lease acquire(long time, TimeUnit unit) throws Exception {
//		return ZkLease.of(mutex.acquire(time, unit));
//	}
//
//	@Override
//	public void release() throws Exception {
//		mutex.release();
//	}
//
//	@Override
//	public boolean isAcquiredInThisProcess() {
//		return mutex.isAcquiredInThisProcess();
//	}
//
//	public static ZkSemaphore of(CuratorFramework curator, String name) {
//		return new ZkSemaphore(new InterProcessMutex(curator, name));
//	}
}
