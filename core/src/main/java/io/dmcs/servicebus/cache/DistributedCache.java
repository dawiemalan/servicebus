package io.dmcs.servicebus.cache;

import java.util.List;

public interface DistributedCache<T> {

    void put(String key, T data);

    List<T> listAll();

    T get(String key);

    void delete(String key);

    void addListener(CacheListener<T> listener);

    void removeListener(CacheListener<T> listener);

    void close();
}
