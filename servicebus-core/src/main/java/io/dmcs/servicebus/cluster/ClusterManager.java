package io.dmcs.servicebus.cluster;

import io.dmcs.common.utils.Startable;
import io.dmcs.servicebus.cluster.leadership.LeaderGroup;
import io.dmcs.servicebus.cluster.locks.DistributedLock;

import java.util.Collection;

public interface ClusterManager extends Startable {

    boolean isConnected();

    DistributedLock getLock(String lockName);

    LeaderGroup getLeaderGroup(String name);

    Collection<LeaderGroup> getLeaderGroups();

    void addClusterStateListener(ClusterStateListener listener);

    void removeClusterStateListener(ClusterStateListener listener);

}
