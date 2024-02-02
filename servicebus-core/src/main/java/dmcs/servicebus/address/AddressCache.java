package dmcs.servicebus.address;

import org.apache.commons.collections4.map.LRUMap;

import java.util.Collections;
import java.util.Map;

public class AddressCache {

    private static final Map<String, EndpointAddress> endpointsCache = Collections.synchronizedMap(new LRUMap<>(1000));
    private static final Map<String, ServiceAddress> servicesCache = Collections.synchronizedMap(new LRUMap<>(1000));

    private AddressCache() {
    }

    static EndpointAddress getEndpointAddress(String address) {
        return endpointsCache.get(address);
    }

    static void putIfAbsent(EndpointAddress address) {

        String key = address.toString();
        endpointsCache.putIfAbsent(key, address);
    }

    static ServiceAddress getServiceAddress(String address) {
        return servicesCache.get(address);
    }

    static void putIfAbsent(ServiceAddress address) {

        String key = address.toString();
        servicesCache.putIfAbsent(key, address);
    }
}
