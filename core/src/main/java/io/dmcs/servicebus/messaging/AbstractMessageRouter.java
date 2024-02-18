package io.dmcs.servicebus.messaging;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.dmcs.servicebus.PlatformSupport;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class AbstractMessageRouter implements MessageRouter {

    private Map<String, MessageIntegration> integrations = new ConcurrentHashMap<>();
    protected PlatformSupport platformSupport;
    protected ObjectMapper objectMapper;

    public AbstractMessageRouter(PlatformSupport platformSupport, ObjectMapper objectMapper) {
        this.platformSupport = platformSupport;
        this.objectMapper = objectMapper;
    }

    @Override
    public void start() {

        // load all configured integrations

        // and initialize
        integrations.values().forEach(MessageIntegration::start);
    }

    @Override
    public void stop() {

        // stop all configured integrations
        integrations.values().forEach(MessageIntegration::stop);
    }

    @Override
    public void sendMessage(EsbMessage message) {

    }

    @Override
    public void onMessage(EsbMessage message) {

    }
}
