package io.dmcs.servicebus.cluster.events;

import io.dmcs.servicebus.cluster.ClusterManager;
import io.dmcs.servicebus.events.EsbEvent;
import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
public class ClusterStateEvent extends EsbEvent {

    private final boolean connected;
    private final ClusterManager clusterManager;

    public ClusterStateEvent(ClusterManager clusterManager, boolean isConnected) {
        super();
        this.clusterManager = clusterManager;
        this.connected = isConnected;
    }
}
