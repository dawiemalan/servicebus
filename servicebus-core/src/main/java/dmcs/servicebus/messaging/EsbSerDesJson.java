package dmcs.servicebus.messaging;

import com.fasterxml.jackson.databind.ObjectMapper;
import dmcs.servicebus.events.EsbEvent;
import lombok.SneakyThrows;

import java.nio.charset.StandardCharsets;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@SuppressWarnings("unchecked")
public class EsbSerDesJson {

    protected ObjectMapper objectMapper;

    @SuppressWarnings("rawtypes")
    protected ConcurrentHashMap<String, Class> classCache = new ConcurrentHashMap<>();

    public EsbSerDesJson(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
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
    public EsbEvent fromMessage(EsbMessage esbMessage) {

        return eventFromJson(
                new String(esbMessage.getPayload(), StandardCharsets.UTF_8),
                esbMessage.getPayloadType()
        );
    }

    @SneakyThrows
    public EsbEvent eventFromJson(String className, String json) {
        return (EsbEvent) objectMapper.readValue(json, getClassFromCache(className));
    }

    @SneakyThrows
    public EsbEvent eventFromJson(String className, byte[] json) {
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
    protected Class getClassFromCache(String className) throws ClassNotFoundException {

        Class clazz = classCache.get(className);
        if (clazz != null)
            return clazz;

        clazz = Class.forName(className);
        classCache.put(className, clazz);

        return clazz;
    }
}
