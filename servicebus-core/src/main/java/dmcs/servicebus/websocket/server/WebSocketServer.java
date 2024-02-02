package dmcs.servicebus.websocket.server;

import com.fasterxml.jackson.databind.ObjectMapper;
import dmcs.servicebus.connections.AbstractConnectionManager;
import dmcs.servicebus.connections.events.ConnectionEvent;
import dmcs.servicebus.messaging.EsbSerDesJson;
import io.micronaut.context.event.ApplicationEventPublisher;
import io.micronaut.core.annotation.NonNull;
import io.micronaut.websocket.WebSocketBroadcaster;
import lombok.extern.slf4j.Slf4j;

@SuppressWarnings("unused")
@Slf4j
public class WebSocketServer<C extends WebSocketServerConnection<C, M>, M> extends AbstractConnectionManager<C, M> {

    protected final WebSocketBroadcaster broadcaster;
    protected final EsbSerDesJson esbSerDes;

    public WebSocketServer(@NonNull WebSocketBroadcaster broadcaster, @NonNull ObjectMapper objectMapper) {
        super(objectMapper);
        this.broadcaster = broadcaster;
        this.esbSerDes = new EsbSerDesJson(objectMapper);
    }

    @SuppressWarnings("unchecked")
    @Override
    public void publishEvent(ConnectionEvent<C, M> event) {
        BeanUtils.getBean(ApplicationEventPublisher.class).publishEventAsync(event);
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
