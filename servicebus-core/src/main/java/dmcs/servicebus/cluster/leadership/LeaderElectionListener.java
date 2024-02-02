package dmcs.servicebus.cluster.leadership;

public interface LeaderElectionListener {

    void onLeadershipChanged(LeaderGroup leaderGroup);
}
