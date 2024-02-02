package dmcs.servicebus.cluster.impl.zookeeper;

import dmcs.common.utils.Startable;
import dmcs.servicebus.PlatformSupport;
import dmcs.servicebus.cluster.ClusterManager;
import dmcs.servicebus.cluster.ClusterStateListener;
import dmcs.servicebus.cluster.leadership.LeaderGroup;
import dmcs.servicebus.services.AbstractServiceManager;
import dmcs.servicebus.services.ServiceManager;
import dmcs.servicebus.services.ServiceRegistration;
import io.micronaut.context.annotation.Context;
import io.micronaut.context.annotation.Requires;
import jakarta.annotation.PreDestroy;
import lombok.Getter;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.utils.CloseableUtils;

import java.util.Collection;
import java.util.Comparator;
import java.util.Set;

@SuppressWarnings("MnInjectionPoints")
@Context
@Requires(bean = ZkClusterManager.class, missingBeans = {ServiceManager.class})
@Slf4j
public class ZkServiceManager extends AbstractServiceManager implements ClusterStateListener {

    private static final String CLUSTER_PREFIX = "/cluster";
    private static final String SERVICE_LEADER_GROUPS = CLUSTER_PREFIX + "/services/leaders/";
    @Getter
    private CuratorFramework curator;
    @Getter
    private ZkClusterManager zkClusterManager;

    private final ServiceInstance instance;

    public ZkServiceManager(ClusterManager clusterManager, PlatformSupport platformSupport) {

        super(clusterManager, platformSupport);
        this.zkClusterManager = (ZkClusterManager) clusterManager;
        this.instance = ServiceInstance.builder().serviceRegistration(serviceRegistration).build();
    }

    @Override
    public Set<Startable> getDependencies() {
        return Set.of(zkClusterManager);
    }

    @Override
    public void start() {

        this.curator = zkClusterManager.getCurator();
        assert this.curator != null;
        zkClusterManager.addClusterStateListener(this);

        this.leaderGroup = zkClusterManager
                .getLeaderGroup(SERVICE_LEADER_GROUPS + serviceRegistration.getServiceGroup());
        this.leaderGroup.addElectionListener(this);

        // register ourselves
        registerSelf();
    }

    @Override
    @SneakyThrows
    public void onLeadershipChanged(LeaderGroup leaderGroup) {

        super.onLeadershipChanged(leaderGroup);
        zkClusterManager.serviceRegistry.set(instance).get();
    }

    @PreDestroy
    @Override
    public synchronized void stop() {

        if (zkClusterManager == null)
            return;

        CloseableUtils.closeQuietly(leaderGroup);
        zkClusterManager.serviceRegistry.remove(instance);
        zkClusterManager = null;
    }

    @SneakyThrows
    @Override
    protected void registerSelf() {

        zkClusterManager.serviceRegistry.set(ServiceInstance.builder().serviceRegistration(serviceRegistration).build()).get();
        log.debug("Service registered: {}", serviceRegistration);
    }

    @Override
    public void clusterStateChanged(ClusterManager clusterManager, boolean connected) {

        if (connected)
            registerSelf();
    }

    @Override
    @SneakyThrows
    public Collection<ServiceRegistration> listServices() {
        return zkClusterManager.getServiceRegistry().list().get().stream()
                .map(ServiceInstance::getServiceRegistration)
                .sorted(Comparator.comparing(ServiceRegistration::getName))
                .toList();
    }
}
