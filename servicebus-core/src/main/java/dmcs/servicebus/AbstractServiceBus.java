package dmcs.servicebus;

import com.fasterxml.jackson.databind.ObjectMapper;
import dmcs.common.utils.StartUtils;
import dmcs.servicebus.address.ServiceLookupQuery;
import dmcs.servicebus.cluster.ClusterManager;
import dmcs.servicebus.cluster.leadership.LeaderGroup;
import dmcs.servicebus.cluster.locks.DistributedLock;
import dmcs.servicebus.config.ServiceBusProperties;
import dmcs.servicebus.events.EsbEvent;
import dmcs.servicebus.messaging.EsbMessage;
import dmcs.servicebus.messaging.MessageRouter;
import dmcs.servicebus.services.ServiceManager;
import dmcs.servicebus.services.ServiceRegistration;
import lombok.Getter;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Base class for service bus implementations
 */
@SuppressWarnings("unused")
@Slf4j
public abstract class AbstractServiceBus implements ServiceBus {

    @Getter
    protected final PlatformSupport platformSupport;
    @Getter
    protected final ObjectMapper objectMapper;
    @Getter
    protected final ServiceBusProperties config;
    @Getter
    protected final ClusterManager clusterManager;
    @Getter
    protected final ServiceManager serviceManager;
    @Getter
    protected MessageRouter messageRouter;

    @SuppressWarnings("rawtypes")
    private final ConcurrentHashMap<String, Class> classCache = new ConcurrentHashMap<>();

    protected AbstractServiceBus(ServiceBusProperties config, PlatformSupport platformSupport, ClusterManager clusterManager, ServiceManager serviceManager) {

        this.config = config;
        this.platformSupport = platformSupport;
        this.objectMapper = platformSupport.getObjectMapper();
        this.clusterManager = clusterManager;
        this.serviceManager = serviceManager;
    }

    @Override
    public synchronized void start() {
        StartUtils.deepStart(clusterManager, serviceManager);
    }

    @Override
    public synchronized void stop() {
        log.info("Stopping service bus...");
        serviceManager.stop();
        clusterManager.stop();
    }

    /**
     * Sends an outgoing message on the service bus
     */
    @Override
    public final void publish(EsbEvent event) {
        publish(toMessage(event));
    }

    @Override
    public ServiceRegistration getServiceRegistration() {
        return serviceManager.getServiceRegistration();
    }

    @Override
    public DistributedLock getLock(String lockName) {
        return clusterManager.getLock(lockName);
    }

    @Override
    public LeaderGroup getLeaderGroup(String name) {
        return clusterManager.getLeaderGroup(name);
    }

    @Override
    public Collection<LeaderGroup> getLeaderGroups() {
        return clusterManager.getLeaderGroups();
    }

    @Override
    public void publish(EsbMessage message) {
        //messageRouter.publish(message);
    }

    @SneakyThrows
    public EsbMessage toMessage(EsbEvent event) {

        return EsbMessage.builder()
                .messageId(UUID.randomUUID().toString())
                .payloadType(event.getClass().getCanonicalName())
                .payload(objectMapper.writeValueAsBytes(event))
                .build();
    }

    @SneakyThrows
    public EsbEvent toEvent(EsbMessage esbMessage) {

        return eventFromJson(
                new String(esbMessage.getPayload(), StandardCharsets.UTF_8),
                esbMessage.getPayloadType()
        );
    }

    @SneakyThrows
    public EsbEvent eventFromJson(String className, String json) {
        //noinspection unchecked
        return (EsbEvent) objectMapper.readValue(json, getClassFromCache(className));
    }

    @SneakyThrows
    public EsbEvent eventFromJson(String className, byte[] json) {
        //noinspection unchecked
        return (EsbEvent) objectMapper.readValue(json, getClassFromCache(className));
    }

    @SneakyThrows
    public EsbMessage messageFromJson(String json) {
        return objectMapper.readValue(json, EsbMessage.class);
    }

    @SneakyThrows
    public EsbMessage messageFromJson(byte[] json) {
        return objectMapper.readValue(json, EsbMessage.class);
    }

    @SneakyThrows
    public String toJsonString(Object object) {
        return objectMapper.writeValueAsString(object);
    }

    @SneakyThrows
    public byte[] toJson(Object object) {
        return objectMapper.writeValueAsBytes(object);
    }

    @SuppressWarnings("rawtypes")
    public Class getClassFromCache(String className) throws ClassNotFoundException {

        Class clazz = classCache.get(className);
        if (clazz != null)
            return clazz;

        clazz = Class.forName(className);
        classCache.put(className, clazz);

        return clazz;
    }

    @Override
    public Collection<ServiceRegistration> listServices() {
        return serviceManager.listServices();
    }

    @Override
    public Collection<ServiceRegistration> locateServices(ServiceLookupQuery query) {
        return serviceManager.locateServices(query);
    }
}
