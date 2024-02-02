package dmcs.servicebus.config;

import java.util.Collections;
import java.util.List;

public interface ServiceBusProperties {

    String PREFIX = "servicebus";

    ClusterType getClusterType();

    String getServiceName();

    String getRegion();

    String getEnvironment();

    boolean isEnabled();

    /**
     * Prepend this to all object paths
     */
    String getPathPrefix();

    default List<String> getServerUrls() {
        return Collections.emptyList();
    }
}
