package io.dmcs.servicebus.springboot;

import io.dmcs.servicebus.config.ClusterType;
import io.dmcs.servicebus.config.ServiceBusProperties;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

//@Component
@ConfigurationProperties(ServiceBusProperties.PREFIX)
@Getter
@Setter
public class SbServiceBusProperties implements ServiceBusProperties {

    private ClusterType clusterType = ClusterType.ZOOKEEPER;

    private boolean enabled;

	private String serviceName;

	private String environment;

	private String region;

    /**
     * Prepend this to all object paths
     */
    private String pathPrefix;

    private List<String> serverUrls;
}
