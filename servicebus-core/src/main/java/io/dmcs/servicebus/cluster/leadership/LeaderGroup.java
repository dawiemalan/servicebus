package io.dmcs.servicebus.cluster.leadership;

import com.google.common.base.Preconditions;
import io.dmcs.common.listeners.StandardListenerManager;
import io.dmcs.servicebus.cluster.ClusterManager;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.io.Closeable;

@Slf4j
public abstract class LeaderGroup implements Closeable {

    @Getter
    private final String name;
    protected final ClusterManager clusterManager;
    protected final StandardListenerManager<LeaderElectionListener> listeners = StandardListenerManager.standard();

    public abstract boolean isLeader();

    protected LeaderGroup(ClusterManager clusterManager, String name) {

        Preconditions.checkNotNull(clusterManager, "clusterManager cannot be null");
        Preconditions.checkNotNull(name, "name cannot be null");

        this.clusterManager = clusterManager;
        this.name = name;
    }

    public void addElectionListener(LeaderElectionListener electionListener) {
        listeners.addListener(electionListener);
    }

    public void removeElectionListener(LeaderElectionListener electionListener) {
        listeners.removeListener(electionListener);
    }

    protected void notifyLeaderChanged() {
        listeners.forEach(listener -> listener.onLeadershipChanged(this));
    }
}
