package dmcs.servicebus.cache;

public interface CacheListener<T> {

    void itemAdded(T item);

    void itemDeleted(T item);
}
