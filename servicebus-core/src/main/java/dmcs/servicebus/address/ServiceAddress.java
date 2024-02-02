package dmcs.servicebus.address;

import dmcs.servicebus.exceptions.InvalidAddressException;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.SneakyThrows;
import lombok.extern.jackson.Jacksonized;
import org.apache.commons.lang3.StringUtils;

@Builder
@AllArgsConstructor
@Jacksonized
public class ServiceAddress {

    /**
     * Service name
     */
    @Getter
    private String name;
    /**
     * Stack id
     */
    @Getter
    private String stackId;
    /**
     * Environment
     */
    @Getter
    private String environment;
    /**
     * Unique instance id per service instance
     */
    @Getter
    private String instanceId;

    /**
     * Cached string and hashcode values
     */
    private String stringValue;
    private String serviceGroup;
    private int hash;

    /**
     * For Jackson, do not use in code
     */
    public ServiceAddress() {
    }

    @Builder
    @SneakyThrows
    public ServiceAddress(String name, String stackId, String environment, String instanceId) {

        this.name = name;
        this.stackId = stackId;
        this.environment = environment;
        this.instanceId = instanceId;

        validate(this);
        makeStringValue();
    }

    private void makeStringValue() {

        StringBuilder sb = new StringBuilder(name);
        if (!StringUtils.isEmpty(stackId)) {
            sb.append("@").append(stackId);
            if (!StringUtils.isEmpty(environment))
                sb.append(".").append(environment);
        }

        if (!StringUtils.isEmpty(instanceId))
            sb.append(":").append(instanceId);

        stringValue = sb.toString();
        hash = stringValue.hashCode();
        if (stackId == null && environment == null)
            serviceGroup = name;
        else if (stackId != null && environment == null)
            serviceGroup = String.format("%s@%s", name, stackId);
        else
            serviceGroup = String.format("%s@%s.%s", name, stackId, environment);
    }

    public static ServiceAddress of(String input) throws InvalidAddressException {

        ServiceAddress serviceAddress = AddressCache.getServiceAddress(input);
        if (serviceAddress != null)
            return serviceAddress;

        if (!StringUtils.contains(input, "@"))
            throw new InvalidAddressException(input);

        String[] tokens = StringUtils.split(input, "@");
        if (tokens.length != 2)
            throw new InvalidAddressException(input);

        serviceAddress = new ServiceAddress();
        serviceAddress.name = tokens[0];

        tokens = StringUtils.split(tokens[1], "/.");
        if (tokens.length != 2)
            throw new InvalidAddressException(input);

        serviceAddress.stackId = tokens[0];
        if (!StringUtils.contains(tokens[1], ":"))
            serviceAddress.environment = tokens[1];
        else {
            tokens = StringUtils.split(tokens[1], ":");
            serviceAddress.environment = tokens[0];
            serviceAddress.instanceId = tokens[1];
        }

        // reset cached string value
        serviceAddress.makeStringValue();

        // cache this value
        AddressCache.putIfAbsent(serviceAddress);

        return serviceAddress;
    }

    public synchronized String getServiceGroup() {

        if (hash == 0)
            makeStringValue();
        return this.serviceGroup;
    }

    @Override
    public int hashCode() {
        if (hash == 0)
            makeStringValue();
        return hash;
    }

    @Override
    public boolean equals(Object obj) {

        if (hash == 0)
            makeStringValue();

        if (obj instanceof ServiceAddress) // NOSONAR
            return ((ServiceAddress) obj).hash == hash;

        if (obj instanceof String)
            return hash == obj.hashCode();

        return false;
    }

    @Override
    public String toString() {

        if (hash == 0)
            makeStringValue();
        return stringValue;
    }

    private static void validate(ServiceAddress address) throws InvalidAddressException {

        if (address.name == null)
            throw new InvalidAddressException("Name is required");
    }
}
