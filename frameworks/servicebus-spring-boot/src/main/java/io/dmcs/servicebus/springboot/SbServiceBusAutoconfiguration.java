package io.dmcs.servicebus.springboot;

import io.dmcs.servicebus.PlatformSupport;
import io.dmcs.servicebus.ServiceBus;
import io.dmcs.servicebus.cluster.ClusterManager;
import io.dmcs.servicebus.cluster.impl.zookeeper.ZkClusterManager;
import io.dmcs.servicebus.cluster.impl.zookeeper.ZkServiceManager;
import io.dmcs.servicebus.config.ServiceBusProperties;
import io.dmcs.servicebus.services.ServiceManager;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

@Configuration
@EnableConfigurationProperties(SbServiceBusProperties.class)
@ComponentScan(basePackageClasses = {SbServiceBusProperties.class})
@SuppressWarnings({"SpringFacetCodeInspection"})
@ConditionalOnProperty(prefix = "servicebus", name = "enabled", havingValue = "true", matchIfMissing = true)
public class SbServiceBusAutoconfiguration {

    @Bean
    public PlatformSupport platformSupport(ConfigurableApplicationContext context, Environment environment,
                                           ApplicationEventPublisher publisher, ServiceBusProperties serviceBusProperties) {
        return new SbPlatformSupport(context, environment, publisher, serviceBusProperties);
    }

    @Bean(destroyMethod = "close")
    public ClusterManager clusterManager(ServiceBusProperties config, PlatformSupport platformSupport) {

//        if (config.getClusterType() == ClusterType.WEBSOCKET)
//            return new WsClusterManager(config, platformSupport);

        return new ZkClusterManager(config, platformSupport);
    }

    @Bean
    ServiceManager serviceManager(ServiceBusProperties config, ClusterManager clusterManager,
                                  PlatformSupport platformSupport) {

//        if (config.getClusterType() == ClusterType.WEBSOCKET)
//            return new WsServiceManager(clusterManager, platformSupport);

        return new ZkServiceManager(clusterManager, platformSupport);
    }

    @SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
    @Bean(destroyMethod = "stop", initMethod = "start")
    public ServiceBus serviceBus(ServiceBusProperties config, PlatformSupport platformSupport,
                                 ClusterManager clusterManager, ServiceManager serviceManager) {
        return new SbServiceBus(config, platformSupport, clusterManager, serviceManager);
    }
}
