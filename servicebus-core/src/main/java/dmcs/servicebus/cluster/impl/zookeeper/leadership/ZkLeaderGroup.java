package dmcs.servicebus.cluster.impl.zookeeper.leadership;

import dmcs.servicebus.cluster.impl.zookeeper.ZkClusterManager;
import dmcs.servicebus.cluster.leadership.LeaderGroup;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ThreadUtils;
import org.apache.commons.lang3.concurrent.EventCountCircuitBreaker;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.recipes.leader.LeaderSelector;
import org.apache.curator.framework.recipes.leader.LeaderSelectorListener;
import org.apache.curator.framework.state.ConnectionState;

import java.io.IOException;
import java.time.Duration;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

@Slf4j
public class ZkLeaderGroup extends LeaderGroup implements LeaderSelectorListener {

    private final LeaderSelector leaderSelector;
    private Thread leaderThread;
    private final EventCountCircuitBreaker circuitBreaker;
    protected final CountDownLatch leaderLatch = new CountDownLatch(1);

    public ZkLeaderGroup(ZkClusterManager clusterManager, String name) {

        super(clusterManager, name);

        circuitBreaker = new EventCountCircuitBreaker(1, 500, TimeUnit.MILLISECONDS);
        leaderSelector = new LeaderSelector(clusterManager.getCurator(), getName(), this);
        leaderSelector.autoRequeue();
        leaderSelector.start();
    }

    @Override
    @SneakyThrows
    public boolean isLeader() {
        return leaderSelector.hasLeadership();
    }

    @Override
    public void close() throws IOException {
        leaderSelector.close();
    }

    @Override
    public void takeLeadership(CuratorFramework client) throws Exception {

        try {
            leaderThread = Thread.currentThread();

            // protected the cluster from runaway leader executions
            if (!circuitBreaker.checkState()) {

                log.error("[{}] leader group execution returning too fast!", getName());

                // retry in 30 seconds
                ThreadUtils.sleep(Duration.ofSeconds(30));

                return;
            }

            notifyLeaderChanged();

            leaderLatch.await();

        } catch (InterruptedException ignored) { // NOSONAR
            // do nothing
        } catch (Exception e) { // NOSONAR
            log.error(e.getMessage(), e);
        } finally {
            leaderThread = null;
        }

        circuitBreaker.incrementAndCheckState();
    }

    @Override
    public void stateChanged(CuratorFramework client, ConnectionState newState) {

        if (!isLeader() && leaderThread != null) {

            leaderLatch.countDown();
            awaitThreadTerminated(leaderThread);
            leaderThread = null;
            log.debug("leader term");
        }

        listeners.forEach(leaderElectionListener -> leaderElectionListener.onLeadershipChanged(this));
    }

    @SneakyThrows
    private void awaitThreadTerminated(Thread thread) {
        ThreadUtils.join(leaderThread, Duration.ofSeconds(5));
    }
}
