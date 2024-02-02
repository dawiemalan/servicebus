package dmcs.servicebus.springboot;

import com.fasterxml.jackson.databind.ObjectMapper;
import dmcs.servicebus.PlatformSupport;
import dmcs.servicebus.address.EndpointAddress;
import dmcs.servicebus.events.EsbEvent;
import lombok.Getter;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.boot.autoconfigure.web.ServerProperties;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.env.Environment;
import org.springframework.util.DigestUtils;

import java.nio.charset.StandardCharsets;
import java.util.*;

@Slf4j
public class SbPlatformSupport implements PlatformSupport {

    @Getter
    private final ObjectMapper objectMapper;

    private final ApplicationEventPublisher publisher;
    @Getter
    private final ConfigurableApplicationContext applicationContext;
    @Getter
    private String serviceInstanceId;
    private EndpointAddress restEndpoint;
    @Getter
    private final Environment environment;
    @Getter
    private final Set<String> profiles;
    private final ServerProperties serverProperties;

    public SbPlatformSupport(ConfigurableApplicationContext context, Environment environment, ApplicationEventPublisher publisher) {

        this.publisher = publisher;
        this.objectMapper = context.getBean(ObjectMapper.class);
        this.applicationContext = context;
        this.environment = environment;
        this.profiles = new HashSet<>(Arrays.asList(environment.getActiveProfiles()));
        this.serviceInstanceId = UUID.randomUUID().toString();
        this.serverProperties = applicationContext.getBean(ServerProperties.class);

        makeRestEndpoint();
    }

    @Override
    public void onEventReceived(EsbEvent event) {
        publisher.publishEvent(event);
    }

    @Override
    public String getServiceName() {
        return environment.getProperty("info.app.name");
    }

    @Override
    public String getStackId() {
        return environment.getProperty("info.region");
    }

    @Override
    public String getStackEnvironment() {
        return environment.getProperty("info.env");
    }

    private void makeRestEndpoint() {

        // use MD5 hash of "hostname:port" as instance id
        this.serviceInstanceId = DigestUtils.md5DigestAsHex(String.format("%s:%d",
                EndpointAddress.LOCAL_HOSTNAME, serverProperties.getPort()).getBytes(StandardCharsets.UTF_8)).toLowerCase(Locale.ROOT);

        setRestEndpoint(
                EndpointAddress.builder()
                        .protocol("http")
                        .host(EndpointAddress.LOCAL_HOSTNAME)
                        .port(serverProperties.getPort())
                        .suffix(serverProperties.getServlet().getContextPath())
                        .build()
        );
    }

    public Optional<EndpointAddress> getRestEndpoint() {
        return Optional.of(restEndpoint);
    }

    private void setRestEndpoint(EndpointAddress endpointAddress) {

        this.restEndpoint = endpointAddress;
        log.info("Service REST endpoint: {}", endpointAddress);
    }

    @Override
    public void registerBean(Object bean) {
        applicationContext.getBeanFactory().registerSingleton(bean.getClass().getSimpleName(), bean);
    }

    @Override
    public @NonNull <T> Optional<T> locateBean(@NonNull Class<T> beanType) {
        try {
            return Optional.of(applicationContext.getBean(beanType));
        } catch (BeansException ignored) {
            return Optional.empty();
        }
    }

    @Override
    public @NonNull <T> Optional<T> locateBean(@NonNull Class<T> beanType, String qualifier) {
        try {
            return Optional.of(applicationContext.getBean(qualifier, beanType));
        } catch (BeansException ignored) {
            return Optional.empty();
        }
    }
}
