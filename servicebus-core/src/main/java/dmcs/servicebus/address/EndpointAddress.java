package dmcs.servicebus.address;

import dmcs.servicebus.exceptions.InvalidAddressException;
import lombok.Builder;
import lombok.Getter;
import org.apache.commons.lang3.StringUtils;

import java.net.InetAddress;
import java.net.UnknownHostException;

public class EndpointAddress {

    public static final String LOCAL_HOSTNAME;

    @Getter
    private String protocol;
    @Getter
    private String host;
    @Getter
    private int port;
    @Getter
    private String suffix;

    /**
     * Cached string and hashcode values
     */
    private String stringValue;
    private int hash;

    static {
        try {
            LOCAL_HOSTNAME = InetAddress.getLocalHost().getCanonicalHostName();
        } catch (UnknownHostException e) {
            throw new RuntimeException(e); // NOSONAR
        }
    }

    /**
     * For Jackson, do not use in code
     */
    public EndpointAddress() {
    }

    @Builder
    private EndpointAddress(String protocol, String host, int port, String suffix) {
        this.protocol = protocol;
        this.host = host;
        this.port = port;
        this.suffix = suffix;
    }

    public static EndpointAddress of(String address) throws InvalidAddressException {

        if (StringUtils.isEmpty(address))
            throw new NullPointerException("Address string is required");

        // try to resolve from in-memory cache
        EndpointAddress endpoint = AddressCache.getEndpointAddress(address);
        if (endpoint != null)
            return endpoint;

        EndpointAddressBuilder builder = builder();
        parse(address, builder);
        endpoint = builder.build();

        endpoint.makeStringValue();

        AddressCache.putIfAbsent(endpoint);

        return endpoint;
    }

    private void makeStringValue() {

        StringBuilder sb = new StringBuilder(protocol).append("://").append(host);
        if (port > 0)
            sb.append(":").append(port);

        if (!StringUtils.isEmpty(suffix))
            sb.append("/").append(suffix);

        stringValue = sb.toString();
        hash = stringValue.hashCode();
    }

    private static void parse(String input, EndpointAddressBuilder builder) throws InvalidAddressException {

        if (!StringUtils.contains(input, "//"))
            throw new InvalidAddressException(input);

        String[] tokens = StringUtils.split(input, "/");
        if (tokens.length < 2)
            throw new InvalidAddressException(input);

        builder.protocol = StringUtils.remove(tokens[0], ":");
        parseHostAndPort(tokens[1], builder);

        if (tokens.length > 2)
            builder.suffix(StringUtils.join(tokens, '/', 2, tokens.length));
    }

    private static void parseHostAndPort(String input, EndpointAddressBuilder builder) {

        int bracketIndex = input.lastIndexOf(']');
        int colonIndex = input.lastIndexOf(':');
        if (colonIndex == -1 || colonIndex < bracketIndex) {
            builder.host(input);
        } else {
            builder.host(input.substring(0, colonIndex));
            builder.port(Integer.parseInt(input.substring(colonIndex + 1)));
        }
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

        if (obj == null)
            return false;

        if (obj instanceof EndpointAddress ea)
            return ea.hash == hash;

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
}
