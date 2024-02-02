package dmcs.servicebus.cluster.impl.zookeeper;

import dmcs.servicebus.services.ServiceRegistration;
import lombok.Builder;
import lombok.Getter;
import lombok.extern.jackson.Jacksonized;
import org.apache.curator.x.async.modeled.NodeName;

@Builder
@Jacksonized
public class ServiceInstance implements NodeName {

    @Getter
    private ServiceRegistration serviceRegistration;

    @Override
    public String nodeName() {
        return serviceRegistration.getInstanceId();
    }
}
