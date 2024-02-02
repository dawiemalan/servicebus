package dmcs.servicebus.cluster.impl;

import dmcs.common.listeners.StandardListenerManager;
import dmcs.servicebus.PlatformSupport;
import dmcs.servicebus.cluster.ClusterManager;
import dmcs.servicebus.cluster.ClusterStateListener;
import dmcs.servicebus.cluster.events.ClusterStateEvent;
import dmcs.servicebus.cluster.leadership.LeaderGroup;
import dmcs.servicebus.config.ServiceBusProperties;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@EqualsAndHashCode(of = {"id"}, callSuper = false)
@Slf4j
public abstract class AbstractClusterManager implements ClusterManager {

    @Getter
    protected final String id = UUID.randomUUID().toString();
    @Getter
    protected final ServiceBusProperties config;
    protected final Map<String, LeaderGroup> leaderGroups = new ConcurrentHashMap<>();
    @Getter
    protected final PlatformSupport platformSupport;
    @Getter
    private boolean connected = false;
    private final StandardListenerManager<ClusterStateListener> clusterStateListeners = StandardListenerManager.standard();

    protected AbstractClusterManager(ServiceBusProperties config, PlatformSupport platformSupport) {
        this.config = config;
        this.platformSupport = platformSupport;
    }

    public String normalizePath(String path) {

        Validate.isTrue(!StringUtils.isEmpty(path), "A path name is required");

        path = StringUtils.replaceChars(path, '@', '.');
        StringBuilder sb = new StringBuilder();
        if (!path.startsWith("/"))
            sb.append('/');

        if (!StringUtils.isEmpty(config.getPathPrefix()))
            sb.append(config.getPathPrefix()).append("/");

        sb.append(StringUtils.replaceChars(path, '.', '/'));

        return sb.toString();
    }

    @Override
    public Collection<LeaderGroup> getLeaderGroups() {
        return Collections.unmodifiableCollection(leaderGroups.values());
    }

    public void stop() {
        stopLeaderGroups();
    }

    protected void stopLeaderGroups() {

        leaderGroups.values().forEach(leaderGroup -> {
            try {
                leaderGroup.close();
            } catch (Exception ignored) {
                // ignore
            }
        });

        leaderGroups.clear();
    }

    public void addClusterStateListener(ClusterStateListener listener) {
        clusterStateListeners.addListener(listener);
    }

    public void removeClusterStateListener(ClusterStateListener listener) {
        clusterStateListeners.removeListener(listener);
    }

    public void notifyClusterStateChanged(boolean connected) {

        this.connected = connected;

        platformSupport.onEventReceived(new ClusterStateEvent(this, connected));
        clusterStateListeners.forEach(listener ->
                listener.clusterStateChanged(this, connected));
    }
}
