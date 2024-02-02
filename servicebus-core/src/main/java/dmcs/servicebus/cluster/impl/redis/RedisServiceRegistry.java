package dmcs.servicebus.cluster.impl.redis;

import dmcs.servicebus.PlatformSupport;
import dmcs.servicebus.services.ServiceRegistration;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.LocalCachedMapOptions;
import org.redisson.api.RLocalCachedMap;
import org.redisson.api.RedissonClient;
import org.redisson.client.codec.Codec;

import java.io.Closeable;
import java.util.List;

@Slf4j
public class RedisServiceRegistry implements Closeable {

    protected static final String SERVICES_PATH = "/cluster/services/registry";

    private final RedissonClient redisson;
    private RLocalCachedMap<String, ServiceRegistration> servicesCache;
    private final PlatformSupport platformSupport;

    public RedisServiceRegistry(RedisClusterManager clusterManager) {

        this.redisson = clusterManager.getRedisson();
        this.platformSupport = clusterManager.getPlatformSupport();
    }

    public void start() {

        var options = LocalCachedMapOptions.<String, ServiceRegistration>defaults()
                .storeCacheMiss(false)
                .storeMode(LocalCachedMapOptions.StoreMode.LOCALCACHE_REDIS)
                .cacheProvider(LocalCachedMapOptions.CacheProvider.REDISSON)
                .evictionPolicy(LocalCachedMapOptions.EvictionPolicy.LRU)
                .reconnectionStrategy(LocalCachedMapOptions.ReconnectionStrategy.LOAD)
                .syncStrategy(LocalCachedMapOptions.SyncStrategy.UPDATE)
                .maxIdle(2000);
        //.timeToLive(30000);

        Codec codec = platformSupport.locateBean(Codec.class).orElse(redisson.getConfig().getCodec());
        log.info("Using codec: {}", codec.getClass().getCanonicalName());

        servicesCache = redisson.getLocalCachedMap(SERVICES_PATH, codec, options);

//		servicesCache.addListener((EntryCreatedListener<String, ServiceRegistration>) event -> {
//			log.debug("Service joined: {}", event.getValue().toString());
//		});
//		servicesCache.addListener((EntryUpdatedListener<String, ServiceRegistration>) event -> {
//			log.debug("Service updated: {}", event.getValue().toString());
//		});
//		servicesCache.addListener((EntryRemovedListener<String, ServiceRegistration>) event -> {
//			log.debug("Service left: {}", event.getValue().toString());
//		});
//		servicesCache.addListener((EntryExpiredListener<String, ServiceRegistration>) event -> {
//			log.warn("Service expired: {}", event.getValue().toString());
//		});
    }

    public ServiceRegistration set(ServiceRegistration service) {
        return servicesCache.put(service.getAddress(), service);
    }

    public ServiceRegistration remove(ServiceRegistration service) {
        return servicesCache.remove(service.getAddress());
    }

    public List<ServiceRegistration> list() {
        return servicesCache.values().stream().toList();
    }

    public ServiceRegistration get(String instanceId) {
        return servicesCache.get(instanceId);
    }

    //	private void notify(ModeledCacheListener.Type type, ServiceInstance model) {
//
//		ServiceEvent event = null;
//
//		switch (type) {
//			case NODE_ADDED:
//				event = new ServiceEvent(model.getServiceRegistration(), "JOINED");
//				break;
//			case NODE_REMOVED:
//				event = new ServiceEvent(model.getServiceRegistration(), "LEFT");
//				break;
//			case NODE_UPDATED:
//				event = new ServiceEvent(model.getServiceRegistration(), "UPDATED");
//				break;
//		}
//
//		if (event != null && platformSupport != null)
//			platformSupport.onEventReceived(event);
//	}

    @Override
    public void close() {

        if (servicesCache != null)
            servicesCache.destroy();

        servicesCache = null;
    }
}
