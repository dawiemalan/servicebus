package io.dmcs.servicebus.micronaut.cluster.impl.websocket;

import io.micronaut.core.annotation.NonNull;
import io.micronaut.websocket.CloseReason;
import io.micronaut.websocket.annotation.ClientWebSocket;
import io.micronaut.websocket.annotation.OnClose;
import io.micronaut.websocket.annotation.OnError;
import io.micronaut.websocket.annotation.OnMessage;
import io.micronaut.websocket.annotation.OnOpen;
import jakarta.validation.constraints.NotBlank;
import lombok.extern.slf4j.Slf4j;

@ClientWebSocket
@Slf4j
public abstract class WsClusterClientImpl implements WsClusterClient {

    @OnMessage
    public void onMessage(String message) {
        log.debug(message);
    }

    @OnOpen
    public void onOpen() {
        log.debug("Session started");
    }

    @OnClose
    public void onClose(CloseReason closeReason) {
        log.debug("Session closed, reason: {}", closeReason.getReason());
    }

    @OnError
    public void onError(Throwable error) {
        log.error(error.getMessage(), error);
    }

	public abstract void send(@NonNull @NotBlank String message);

}
