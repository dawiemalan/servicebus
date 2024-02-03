package io.dmcs.servicebus.cluster.impl.zookeeper.cache;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.dmcs.servicebus.cache.AbstractCache;
import io.dmcs.servicebus.cluster.impl.zookeeper.ZkClusterManager;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.recipes.cache.ChildData;
import org.apache.curator.framework.recipes.cache.CuratorCache;
import org.apache.curator.framework.recipes.cache.CuratorCacheListener;
import org.apache.curator.utils.CloseableUtils;
import org.apache.curator.utils.ZKPaths;

import java.io.IOException;
import java.util.List;

@Slf4j
public class ZkCache<T> extends AbstractCache<T> implements CuratorCacheListener {

    private final Class<T> entityClass;
    private final String nodePath;
    private final CuratorFramework curator;
    private final ObjectMapper objectMapper;
    private CuratorCache curatorCache;

    private boolean initialized;

    public ZkCache(Class<T> entityClass, ZkClusterManager clusterManager, String nodePath, boolean clearOnClose) {

        this.entityClass = entityClass;
        this.nodePath = ZKPaths.getNodeFromPath(nodePath);
        this.curator = clusterManager.getCurator();
        this.objectMapper = clusterManager.getPlatformSupport().getObjectMapper();

        var builder = CuratorCache.builder(curator, nodePath);
        if (!clearOnClose)
            builder.withOptions(CuratorCache.Options.DO_NOT_CLEAR_ON_CLOSE);

        curatorCache = builder.build();
        curatorCache.listenable().addListener(this);
        curatorCache.start();
    }

    @Override
    @SneakyThrows
    public void put(String key, T data) {

        String entryPath = ZKPaths.makePath(nodePath, key);
        byte[] bytes = objectMapper.writeValueAsBytes(data);
        curator.setData().forPath(entryPath, bytes);
    }

    @Override
    public List<T> listAll() {

        return curatorCache.stream().map(childData -> {
            try {
                return objectMapper.readValue(childData.getData(), entityClass);
            } catch (IOException e) {
                log.error(e.getMessage(), e);
                return null;
            }
        }).toList();
    }

    @Override
    public T get(String key) {

        try {
            var bytes = curator.getData().forPath(ZKPaths.makePath(nodePath, key));
            if (bytes == null)
                return null;

            return objectMapper.readValue(bytes, entityClass);

        } catch (Exception e) {
            return null;
        }
    }

    @Override
    @SneakyThrows
    public void delete(String key) {
        curator.delete().forPath(ZKPaths.makePath(nodePath, key));
    }

    public void close() {
        CloseableUtils.closeQuietly(curatorCache);
        curatorCache = null;
    }

    @Override
    public void event(Type type, ChildData oldData, ChildData data) {

        // skip initial cache loading events
        if (!this.initialized)
            return;

        ChildData childData = data;
        if (childData == null)
            childData = oldData;

        if (childData == null)
            return;

        // ignore node creation and update
        if (StringUtils.equals(this.nodePath, childData.getPath()))
            return;

        T item;
        try {
            item = objectMapper.readValue(childData.getData(), entityClass);
        } catch (IOException e) {
            log.error(e.getMessage(), e);
            return;
        }

        switch (type) {
            case NODE_CREATED:
                notifyItemAdded(item);
                break;
            case NODE_DELETED:
                notifyItemDeleted(item);
                break;
            case NODE_CHANGED:
                // do nothing
                break;
        }
    }

    @Override
    public void initialized() {
        this.initialized = true;
    }
}
