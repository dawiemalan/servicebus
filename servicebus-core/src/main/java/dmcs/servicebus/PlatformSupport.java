package dmcs.servicebus;

import com.fasterxml.jackson.databind.ObjectMapper;
import dmcs.servicebus.address.EndpointAddress;
import dmcs.servicebus.events.EsbEvent;
import jakarta.annotation.Nonnull;

import java.util.Optional;
import java.util.Set;

/**
 * An application-side event router, used to publish incoming events to the rest of the application. Mostly
 * used for incoming events, but also used for cluster events such as connection states.
 */
public interface PlatformSupport {

    /**
     * Handles an incoming event from the service bus
     */
    void onEventReceived(EsbEvent event);

    ObjectMapper getObjectMapper();

    String getServiceName();

    String getServiceInstanceId();

    /**
     * REST endpoint address of this service
     *
     * @return
     */
    Optional<EndpointAddress> getRestEndpoint();

    String getRegion();

    String getEnvironment();

    Set<String> getProfiles();

    void registerBean(Object bean);

    <T> Optional<T> locateBean(@Nonnull Class<T> beanType);

    <T> Optional<T> locateBean(@Nonnull Class<T> beanType, String qualifier);
}
