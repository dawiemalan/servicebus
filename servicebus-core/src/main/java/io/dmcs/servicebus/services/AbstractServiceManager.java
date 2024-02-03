package io.dmcs.servicebus.services;

import io.dmcs.servicebus.PlatformSupport;
import io.dmcs.servicebus.address.ServiceLookupQuery;
import io.dmcs.servicebus.cluster.ClusterManager;
import io.dmcs.servicebus.cluster.leadership.LeaderElectionListener;
import io.dmcs.servicebus.cluster.leadership.LeaderGroup;
import lombok.Getter;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import java.util.Collection;
import java.util.Comparator;

@Slf4j
public abstract class AbstractServiceManager implements ServiceManager, LeaderElectionListener {

    protected ClusterManager clusterManager;
    protected PlatformSupport platformSupport;
    protected LeaderGroup leaderGroup;

    @Getter
    protected final ServiceRegistration serviceRegistration;

    protected abstract void registerSelf();

    protected AbstractServiceManager(ClusterManager clusterManager, PlatformSupport platformSupport) {

        this.clusterManager = clusterManager;
        this.platformSupport = platformSupport;

        serviceRegistration = ServiceRegistration.builder()
                .name(platformSupport.getServiceName())
                .profiles(platformSupport.getProfiles())
				.region(platformSupport.getRegion())
				.environment(platformSupport.getEnvironment())
                .instanceId(platformSupport.getServiceInstanceId())
                .endpoint(platformSupport.getRestEndpoint().orElse(null))
                .build();
    }

    @Override
    @SneakyThrows
    public void onLeadershipChanged(LeaderGroup leaderGroup) {

        this.getServiceRegistration().setLeader(leaderGroup.isLeader());
        if (leaderGroup.isLeader())
            log.info("{} is now leader of group {} containing {} instances",
                    this.getServiceRegistration().toString(),
                    this.getServiceRegistration().getServiceGroup(),
                    listServices(this.getServiceRegistration().getServiceGroup()).size());
    }

    public Collection<ServiceRegistration> locateServices(ServiceLookupQuery query) {

        return listServices().stream().filter(service -> service.matches(query))
                .sorted(Comparator.comparing(ServiceRegistration::getName))
                .toList();
    }

    @Override
    @SneakyThrows
    public Collection<ServiceRegistration> listServices(String serviceGroup) {
        return listServices().stream()
                .filter(it -> it.isInGroup(serviceGroup))
                .toList();
    }

}
