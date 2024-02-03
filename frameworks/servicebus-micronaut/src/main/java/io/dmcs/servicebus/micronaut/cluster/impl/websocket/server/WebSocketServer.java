package io.dmcs.servicebus.micronaut.cluster.impl.websocket.server;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.dmcs.servicebus.connections.AbstractConnectionManager;
import io.dmcs.servicebus.connections.events.ConnectionEvent;
import io.dmcs.servicebus.events.EsbEvent;
import io.dmcs.servicebus.messaging.EsbSerDesJson;
import io.micronaut.context.event.ApplicationEventPublisher;
import io.micronaut.core.annotation.NonNull;
import io.micronaut.websocket.WebSocketBroadcaster;
import lombok.extern.slf4j.Slf4j;

@SuppressWarnings("unused")
@Slf4j
public class WebSocketServer<C extends WebSocketServerConnection<C, M>, M> extends AbstractConnectionManager<C, M> {

    protected final WebSocketBroadcaster broadcaster;
    protected final EsbSerDesJson esbSerDes;
    protected final ApplicationEventPublisher<EsbEvent> eventPublisher;

    public WebSocketServer(@NonNull WebSocketBroadcaster broadcaster, @NonNull ObjectMapper objectMapper, @NonNull ApplicationEventPublisher<EsbEvent> eventPublisher) {
        super(objectMapper);
        this.broadcaster = broadcaster;
        this.esbSerDes = new EsbSerDesJson(objectMapper);
        this.eventPublisher = eventPublisher;
    }

    @SuppressWarnings("unchecked")
    @Override
    public void publishEvent(ConnectionEvent<C, M> event) {
        eventPublisher.publishEventAsync(event);
    }

    //	/**
//	 * Sends a JSON message to all sessions
//	 *
//	 * @param message
//	 */
//	public void broadcastAsync(Object message) {
//		broadcastAsync(message, MediaType.APPLICATION_JSON_TYPE);
//	}
//
//	/**
//	 * Sends a message to all sessions
//	 *
//	 * @param message
//	 * @param mediaType
//	 */
//	public void broadcastAsync(Object message, MediaType mediaType) {
//		broadcaster.broadcastAsync(message, mediaType, session -> sessions.containsKey(session.getId()));
//	}
//
//	public Stream<WebSocketSession> getSessions() {
//		return sessions.values().stream();
//	}

}
