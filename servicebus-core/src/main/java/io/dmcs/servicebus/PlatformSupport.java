package io.dmcs.servicebus;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.dmcs.servicebus.address.EndpointAddress;
import io.dmcs.servicebus.config.ServiceBusProperties;
import io.dmcs.servicebus.events.EsbEvent;
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

	default String getServiceName() {
		return getServiceBusProperties().getServiceName();
	}

    String getServiceInstanceId();

	ServiceBusProperties getServiceBusProperties();

    /**
     * REST endpoint address of this service
     *
     * @return
     */
    Optional<EndpointAddress> getRestEndpoint();

	default String getRegion() {
		return getServiceBusProperties().getRegion();
	}

	default String getEnvironment() {
		return getServiceBusProperties().getEnvironment();
	}

    Set<String> getProfiles();

    void registerBean(Object bean);

    <T> Optional<T> locateBean(@Nonnull Class<T> beanType);

    <T> Optional<T> locateBean(@Nonnull Class<T> beanType, String qualifier);
}
