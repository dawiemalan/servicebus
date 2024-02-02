package dmcs.servicebus.cluster;

public interface ClusterStateListener {

    void clusterStateChanged(ClusterManager clusterManager, boolean connected);
}
