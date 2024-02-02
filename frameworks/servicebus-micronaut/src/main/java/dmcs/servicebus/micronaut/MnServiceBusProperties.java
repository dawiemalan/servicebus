package dmcs.servicebus.micronaut;

import dmcs.servicebus.config.ClusterType;
import dmcs.servicebus.config.ServiceBusProperties;
import io.micronaut.context.annotation.ConfigurationProperties;
import io.micronaut.context.annotation.Requires;
import io.micronaut.core.annotation.Introspected;
import io.micronaut.core.bind.annotation.Bindable;
import io.micronaut.runtime.context.scope.Refreshable;

import java.util.List;

@Introspected
@ConfigurationProperties(ServiceBusProperties.PREFIX)
@Requires(property = "servicebus.enabled", value = "true")
@Refreshable({"servicebus"})
public interface MnServiceBusProperties extends ServiceBusProperties {

    @Override
    @Bindable(defaultValue = "zookeeper")
    ClusterType getClusterType();

    @Override
    @Bindable(defaultValue = "false")
    boolean isEnabled();

    /**
     * Prepend this to all object paths
     */
    @Override
    @Bindable(defaultValue = "")
    String getPathPrefix();

    @Override
    List<String> getServerUrls();
}
