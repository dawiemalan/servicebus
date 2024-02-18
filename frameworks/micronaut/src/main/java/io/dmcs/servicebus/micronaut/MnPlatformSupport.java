package io.dmcs.servicebus.micronaut;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.dmcs.servicebus.PlatformSupport;
import io.dmcs.servicebus.address.EndpointAddress;
import io.dmcs.servicebus.config.ServiceBusProperties;
import io.dmcs.servicebus.events.EsbEvent;
import io.micronaut.context.ApplicationContext;
import io.micronaut.context.env.Environment;
import io.micronaut.context.event.ApplicationEventPublisher;
import io.micronaut.context.exceptions.NoSuchBeanException;
import io.micronaut.inject.qualifiers.Qualifiers;
import io.micronaut.runtime.server.EmbeddedServer;
import jakarta.annotation.Nonnull;
import lombok.Getter;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.digest.DigestUtils;

import java.nio.charset.StandardCharsets;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
public class MnPlatformSupport implements PlatformSupport {

    @Getter
    private final ObjectMapper objectMapper;
    @Getter
    private final ApplicationContext applicationContext;
    @Getter
    private String serviceInstanceId;
    private EndpointAddress restEndpoint;

    private final Environment environment;

	@Getter
    private final ServiceBusProperties serviceBusProperties;

    private final Map<Class<? extends EsbEvent>, ApplicationEventPublisher<EsbEvent>> publisherCache = new ConcurrentHashMap<>();

    public MnPlatformSupport(@Nonnull ApplicationContext context, @Nonnull ServiceBusProperties serviceBusProperties, @Nonnull Environment environment) {

        this.applicationContext = context;
        this.serviceBusProperties = serviceBusProperties;
        this.environment = environment;
        objectMapper = locateBean(ObjectMapper.class).orElseThrow();

        makeRestEndpoint();
    }

    @Override
    public void onEventReceived(EsbEvent event) {
        resolvePublisher(event).publishEventAsync(event);
    }

    @SuppressWarnings({"ReassignedVariable"})
    private synchronized ApplicationEventPublisher<EsbEvent> resolvePublisher(EsbEvent event) {

        ApplicationEventPublisher<EsbEvent> publisher = publisherCache.get(event.getClass()); // NOSONAR
        if (publisher != null)
            return publisher;

        //noinspection unchecked
        publisher = (ApplicationEventPublisher<EsbEvent>) applicationContext.getEventPublisher(event.getClass());
        publisherCache.put(event.getClass(), publisher);

        return publisher;
    }

    @Override
    public Set<String> getProfiles() {
        return environment.getActiveNames();
    }

    @SneakyThrows
    private void makeRestEndpoint() {

        Optional<EmbeddedServer> embeddedServer = locateBean(EmbeddedServer.class);

        embeddedServer.ifPresent(server -> {

            // use MD5 hash of "hostname:port" as instance id
            this.serviceInstanceId = DigestUtils.md2Hex(String.format("%s:%d",
                    EndpointAddress.LOCAL_HOSTNAME, server.getPort()).getBytes(StandardCharsets.UTF_8)).toLowerCase(Locale.ROOT);

            setRestEndpoint(
                    EndpointAddress.builder()
                            .protocol(server.getScheme())
                            .host(EndpointAddress.LOCAL_HOSTNAME)
                            .port(server.getPort())
                            .suffix(environment.get("micronaut.server.context-path", String.class).orElse(null))
                            .build()
            );
        });
    }

    private void setRestEndpoint(EndpointAddress endpointAddress) {
        this.restEndpoint = endpointAddress;
        log.info("Service REST endpoint: {}", endpointAddress);
    }

    public Optional<EndpointAddress> getRestEndpoint() {
        return Optional.of(restEndpoint);
    }

    @Override
    public void registerBean(Object bean) {
        applicationContext.registerSingleton(bean);
    }

    @Override
    public <T> Optional<T> locateBean(@Nonnull Class<T> beanType) {

        try {
            return Optional.of(applicationContext.getBean(beanType));
        } catch (NoSuchBeanException e) {
            return Optional.empty();
        }
    }

    @Override
    public <T> Optional<T> locateBean(@Nonnull Class<T> beanType, String qualifier) {

        try {
            return Optional.of(applicationContext.getBean(beanType, Qualifiers.byName(qualifier)));
        } catch (NoSuchBeanException e) {
            return Optional.empty();
        }
    }
}
