package dmcs.servicebus.services;

import com.hrakaroo.glob.GlobPattern;
import dmcs.servicebus.address.EndpointAddress;
import dmcs.servicebus.address.ServiceAddress;
import dmcs.servicebus.address.ServiceLookupQuery;
import dmcs.servicebus.exceptions.DuplicateEndpointException;
import lombok.*;
import org.apache.commons.lang3.StringUtils;

import java.time.ZonedDateTime;
import java.util.*;
import java.util.stream.Collectors;

@AllArgsConstructor
@Getter
@EqualsAndHashCode(of = {"instanceId"})
public class ServiceRegistration {

    @Getter
    private String name;
    @Getter
    private String serviceGroup;
    @Getter
    private String instanceId;
    private String address;
    private ZonedDateTime registrationTime;
    private String version;
    @Setter
    private boolean leader;
    @Singular
    private final Set<String> endpoints;
    private Set<String> profiles;
    @Getter
    private String hostAddress;

    /**
     * For Jackson, do not use in code
     */
    public ServiceRegistration() {
        this.profiles = Collections.emptySet();
        this.endpoints = Collections.synchronizedSet(new HashSet<>());
        this.registrationTime = ZonedDateTime.now();
    }

    @SuppressWarnings("unused")
    @Builder
    public ServiceRegistration(String name, String stackId, String environment, String instanceId, // NOSONAR
                               ZonedDateTime registrationTime, String version, Set<String> profiles,
                               EndpointAddress endpoint) {

        this();

        var addr = ServiceAddress.builder()
                .name(name)
                .stackId(stackId)
                .environment(environment)
                .instanceId(instanceId)
                .build();

        this.address = addr.toString();
        this.name = addr.getName();
        this.serviceGroup = addr.getServiceGroup();
        this.instanceId = addr.getInstanceId();

        this.profiles = profiles;
        this.registrationTime = registrationTime;
        this.version = version;

        if (endpoint != null) {
            this.endpoints.add(endpoint.toString());
            this.hostAddress = String.format("%s:%d", endpoint.getHost(), endpoint.getPort());
        }
    }

    public void addEndpoint(EndpointAddress endpoint) throws DuplicateEndpointException {

        if (endpoints.contains(endpoint.toString()))
            throw new DuplicateEndpointException(endpoint.toString());

        endpoints.add(endpoint.toString());
    }

    /**
     * Finds first endpoint that matches a protocol
     */
    public Optional<String> getEndpoint(String protocol) {
        return endpoints.stream().filter(endpointAddress -> endpointAddress.startsWith(protocol))
                .findFirst();
    }

    /**
     * Finds all endpoints that matches a protocol
     */
    public List<String> getEndpoints(String protocol) {
        return endpoints.stream().filter(endpointAddress -> endpointAddress.startsWith(protocol))
                .collect(Collectors.toList());
    }

    /**
     * Returns true if this service matches the given pattern.
     * <p>
     * The wildcard matcher uses the characters '?' and '*' to represent a
     * single or multiple wildcard characters.
     * This is the same as often found on Dos/Unix command lines.
     * </p>
     * <p>
     * For example:
     *
     * </p>
     */
    public boolean matches(String pattern) {

        if (StringUtils.isEmpty(pattern))
            return false;

        pattern = pattern.trim();
        if ("*".equals(pattern))
            return true;

        if (!pattern.contains("@") && !pattern.contains("*"))
            pattern = pattern + "@*";

        // exact match
        if (StringUtils.equals(pattern, address))
            return true;

        if (!pattern.contains(":"))
            pattern = pattern + ":*";

        return GlobPattern.compile(pattern.trim()).matches(this.address);
    }

    public boolean matches(ServiceLookupQuery query) {

        // exact match on instance id
        if (StringUtils.isEmpty(query.getName()) && !StringUtils.isEmpty(query.getInstanceId()))
            return matches("*:" + query.getInstanceId());

        if (!this.matches(query.getName()))
            return false;

        if (query.getInstanceId() != null)
            return StringUtils.equals(instanceId, query.getInstanceId());

        return true;
    }

    public boolean isInGroup(String serviceGroup) {
        return StringUtils.equalsIgnoreCase(this.serviceGroup, serviceGroup);
    }

    @Override
    public String toString() {
        return address;
    }
}
