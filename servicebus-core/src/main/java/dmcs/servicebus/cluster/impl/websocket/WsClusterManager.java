package dmcs.servicebus.cluster.impl.websocket;

import dmcs.servicebus.PlatformSupport;
import dmcs.servicebus.cluster.impl.AbstractClusterManager;
import dmcs.servicebus.cluster.leadership.LeaderGroup;
import dmcs.servicebus.cluster.locks.DistributedLock;
import dmcs.servicebus.config.ServiceBusProperties;
import dmcs.servicebus.exceptions.InvalidAddressException;
import io.micronaut.context.BeanContext;
import io.micronaut.http.uri.UriBuilder;
import io.micronaut.websocket.WebSocketClient;
import lombok.Getter;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;

import java.time.Duration;
import java.util.List;

@Slf4j
public class WsClusterManager extends AbstractClusterManager {

    @Getter
    private WsClusterClient clusterClient;

    public WsClusterManager(ServiceBusProperties config,
                            PlatformSupport platformSupport) {
        super(config, platformSupport);
    }

    @Override
    @SneakyThrows
    public void start() {

        //clusterClient = createWebsocketClient(((MnPlatformSupport) platformSupport).getApplicationContext());
        notifyClusterStateChanged(true);
    }

    @Override
    @SneakyThrows
    public void stop() {

        if (clusterClient != null) {
            clusterClient.close();
            clusterClient = null;
        }
    }

    @Override
    public DistributedLock getLock(String lockName) {
        return null;
    }

    @Override
    public LeaderGroup getLeaderGroup(String name) {
        return null;
    }

    private WsClusterClient createWebsocketClient(BeanContext beanContext) throws InvalidAddressException {

        List<String> urls = config.getServerUrls();
        if (urls.isEmpty())
            throw new InvalidAddressException("No server urls configured");

        WebSocketClient webSocketClient = beanContext.getBean(WebSocketClient.class);

        return Flux.from(webSocketClient.connect(
                WsClusterClient.class,
                UriBuilder.of(urls.stream().findFirst().orElseThrow(() -> new InvalidAddressException("No server urls configured"))).build()
        )).blockFirst(Duration.ofSeconds(10));
    }
}
