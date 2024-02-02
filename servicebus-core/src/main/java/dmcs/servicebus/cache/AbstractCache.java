package dmcs.servicebus.cache;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public abstract class AbstractCache<T> implements DistributedCache<T> {

    protected List<CacheListener<T>> listeners = Collections.synchronizedList(new ArrayList<>());

    public void addListener(CacheListener<T> listener) {
        listeners.add(listener);
    }

    public void removeListener(CacheListener<T> listener) {
        listeners.remove(listener);
    }

    protected void notifyItemAdded(T item) {
        listeners.forEach(listener -> listener.itemAdded(item));
    }

    protected void notifyItemDeleted(T item) {
        listeners.forEach(listener -> listener.itemDeleted(item));
    }
}
