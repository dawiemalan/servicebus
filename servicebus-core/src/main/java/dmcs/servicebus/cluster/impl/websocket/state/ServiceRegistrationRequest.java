package dmcs.servicebus.cluster.impl.websocket.state;

import dmcs.servicebus.events.EsbEvent;
import dmcs.servicebus.services.ServiceRegistration;
import lombok.Builder;
import lombok.Getter;
import lombok.SneakyThrows;
import lombok.extern.jackson.Jacksonized;

@Getter
@Builder
@Jacksonized
public class ServiceRegistrationRequest extends EsbEvent {

    private ServiceRegistration registration;

    public ServiceRegistrationRequest(ServiceRegistration serviceRegistration) {
        super();
    }

    @SneakyThrows
    public static ServiceRegistrationRequest of(ServiceRegistration registration) {
        return builder().registration(registration)
//				.recipient(ServiceAddress.of("servicebus@global.*"))
//				.sender(registration.getAddress())
//				.persistent(false)
//				.className(ServiceRegistrationRequest.class.getCanonicalName())
                .build();
    }
}
