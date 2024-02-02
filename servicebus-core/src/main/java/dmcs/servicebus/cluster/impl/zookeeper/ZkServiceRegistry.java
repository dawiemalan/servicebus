package dmcs.servicebus.cluster.impl.zookeeper;

import dmcs.servicebus.PlatformSupport;
import dmcs.servicebus.services.events.ServiceEvent;
import lombok.extern.slf4j.Slf4j;
import org.apache.curator.x.async.AsyncCuratorFramework;
import org.apache.curator.x.async.modeled.JacksonModelSerializer;
import org.apache.curator.x.async.modeled.ModelSpec;
import org.apache.curator.x.async.modeled.ModeledFramework;
import org.apache.curator.x.async.modeled.ZPath;
import org.apache.curator.x.async.modeled.cached.CachedModeledFramework;
import org.apache.curator.x.async.modeled.cached.ModeledCacheListener;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.data.Stat;

import java.io.Closeable;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@SuppressWarnings("UnusedReturnValue")
@Slf4j
public class ZkServiceRegistry implements Closeable {

    protected static final ZPath path = ZPath.parse("/cluster/services/registry");
    protected ModelSpec<ServiceInstance> modelSpec;
    protected AsyncCuratorFramework async;
    private CachedModeledFramework<ServiceInstance> cache;
    private final PlatformSupport platformSupport;

    public ZkServiceRegistry(AsyncCuratorFramework async, PlatformSupport platformSupport) {

        this.async = async;
        this.platformSupport = platformSupport;

        JacksonModelSerializer<ServiceInstance> serializer = JacksonModelSerializer.build(ServiceInstance.class);
        modelSpec = ModelSpec.builder(path, serializer)
                .withCreateMode(CreateMode.EPHEMERAL)
                .build();
    }

    public void start() {

        cache = ModeledFramework.wrap(async, modelSpec).cached();
        cache.start();

        cache.listenable().addListener((type, pathVar, stat, model) -> notify(type, model));
    }

    public CompletableFuture<String> set(ServiceInstance serviceInstance) {
        return cache.child(serviceInstance.nodeName()).set(serviceInstance).toCompletableFuture();
    }

    public CompletableFuture<Stat> update(ServiceInstance serviceInstance) {
        return cache.child(serviceInstance.nodeName()).update(serviceInstance).toCompletableFuture();
    }

    public CompletableFuture<Void> remove(ServiceInstance serviceInstance) {
        return cache.child(serviceInstance.nodeName()).delete().toCompletableFuture();
    }

    public CompletableFuture<List<ServiceInstance>> list() {
        return cache.list().toCompletableFuture();
    }

    public CompletableFuture<ServiceInstance> get(String id) {
        return cache.child(id).readThrough().toCompletableFuture();
    }

    private void notify(ModeledCacheListener.Type type, ServiceInstance model) {

        ServiceEvent event = null;

        switch (type) {
            case Type.NODE_ADDED:
                event = new ServiceEvent(model.getServiceRegistration(), "JOINED");
                break;
            case Type.NODE_REMOVED:
                event = new ServiceEvent(model.getServiceRegistration(), "LEFT");
                break;
            case Type.NODE_UPDATED:
                event = new ServiceEvent(model.getServiceRegistration(), "UPDATED");
                break;
        }

        if (event != null && platformSupport != null)
            platformSupport.onEventReceived(event);
    }

    @Override
    public void close() {

        if (cache != null)
            cache.close();

        cache = null;
    }
}
