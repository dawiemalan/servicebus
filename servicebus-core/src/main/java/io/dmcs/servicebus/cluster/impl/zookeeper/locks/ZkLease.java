package io.dmcs.servicebus.cluster.impl.zookeeper.locks;

import io.dmcs.servicebus.cluster.locks.Lease;

import java.io.IOException;

public class ZkLease implements Lease {

    protected org.apache.curator.framework.recipes.locks.Lease curatorLease;

    private ZkLease(org.apache.curator.framework.recipes.locks.Lease curatorLease) {
        this.curatorLease = curatorLease;
    }

    @Override
    public void close() throws IOException {

        if (curatorLease != null)
            curatorLease.close();

        curatorLease = null;
    }

    @Override
    public byte[] getData() throws Exception {
        return curatorLease.getData();
    }

    @Override
    public String getName() {
        return curatorLease.getNodeName();
    }

    public static ZkLease of(org.apache.curator.framework.recipes.locks.Lease curatorLease) {
        return new ZkLease(curatorLease);
    }
}
