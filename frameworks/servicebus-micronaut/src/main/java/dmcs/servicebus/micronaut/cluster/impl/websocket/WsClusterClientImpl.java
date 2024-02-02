package dmcs.servicebus.micronaut.cluster.impl.websocket;

import dmcs.servicebus.cluster.impl.websocket.WsClusterClient;
import io.micronaut.core.annotation.NonNull;
import io.micronaut.websocket.CloseReason;
import io.micronaut.websocket.annotation.*;
import jakarta.validation.constraints.NotBlank;
import lombok.extern.slf4j.Slf4j;

@ClientWebSocket
@Slf4j
public abstract class WsClusterClientImpl implements WsClusterClient {

//	private final WsClusterManager clusterManager;
//
//	public WsClusterClient(WsClusterManager clusterManager) {
//		this.clusterManager = clusterManager;
//	}

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

    abstract void send(@NonNull @NotBlank String message);

}
