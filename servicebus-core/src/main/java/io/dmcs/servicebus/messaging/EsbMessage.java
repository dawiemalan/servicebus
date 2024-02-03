package io.dmcs.servicebus.messaging;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.SneakyThrows;
import lombok.experimental.SuperBuilder;
import lombok.extern.jackson.Jacksonized;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;

import java.io.Serializable;
import java.util.AbstractMap;
import java.util.Collection;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

@NoArgsConstructor
@AllArgsConstructor
@Jacksonized
@SuperBuilder
@EqualsAndHashCode(of = {"messageId"})
public class EsbMessage implements Serializable {

    static final long serialVersionUID = 1L;

    /**
     * Message recipient address
     */
    @Getter
    private String to;

    /**
     * Message sender address
     */
    @Getter
    private String from;
    @Getter
    @Builder.Default
    private String messageId = UUID.randomUUID().toString();
    @Getter
    private String payloadType;
    @Getter
    private MessageOptions options;

    private final Map<String, Map.Entry<String, Serializable>> headers = new HashMap<>(32);

    @Getter
    private byte[] payload;

    @SneakyThrows
    public static EsbMessage of(Object message, ObjectMapper objectMapper) {

        if (message instanceof EsbMessage m)
            return m;

        // encode to json
        byte[] bytes = objectMapper.writeValueAsBytes(message);
        return EsbMessage.builder()
                .payloadType(message.getClass().getName())
                .payload(bytes)
                .build();
    }

    public Collection<Map.Entry<String, Serializable>> getHeaders() {
        return headers.values();
    }

    public void clearHeaders() {
        headers.clear();
    }

    public void clearHeadersExcept(Collection<String> normalizedHeaderNames) {
        if (normalizedHeaderNames.isEmpty())
            headers.clear();
        else
            headers.keySet().retainAll(normalizedHeaderNames);
    }

    @SuppressWarnings("unchecked")
    public <T> T getHeader(String headerName) {
        Map.Entry<String, Serializable> entry = headers.get(normalizeHeader(headerName));
        return entry != null ? (T) entry.getValue() : null;
    }

    public void putHeader(String headerName, Serializable value) {
        headers.put(normalizeHeader(headerName), new AbstractMap.SimpleImmutableEntry<>(headerName, value));
    }

    public void addHeader(String headerName, Serializable value) {
        String key = normalizeHeader(headerName);
        Map.Entry<String, Serializable> entry = headers.get(key);
        if (entry != null) {
            headers.put(key, new AbstractMap.SimpleImmutableEntry<>(entry.getKey(), entry.getValue() + ", " + value));
        } else {
            headers.put(key, new AbstractMap.SimpleImmutableEntry<>(headerName, value));
        }
    }

    @SuppressWarnings("unchecked")
    public <T> T removeHeader(String headerName) {
        Map.Entry<String, ?> entry = headers.remove(normalizeHeader(headerName));
        return entry != null ? (T) entry.getValue() : null;
    }

    private static String normalizeHeader(String headerName) {
        return headerName.toLowerCase(Locale.ROOT).replace('_', '-');
    }

    @Override
    public String toString() {

        ReflectionToStringBuilder rtsb = new ReflectionToStringBuilder(this);
        rtsb.setExcludeNullValues(true);
        rtsb.setExcludeFieldNames("payload");

        return StringUtils.removeStart(rtsb.toString(), EsbMessage.class.getPackageName() + ".");
    }
}
