package io.dmcs.servicebus.cluster;

import com.fasterxml.jackson.annotation.JsonRootName;
import io.dmcs.servicebus.config.ServiceBusProperties;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.apache.commons.lang3.StringUtils;

import java.util.Set;

@JsonRootName("member")
@Getter
@Setter
@ToString
public class ClusterMember {

    private String name;
    private String region;
    private String environment;
    private Set<String> profiles;

    public ClusterMember() {
        // for jackson
    }

    public static ClusterMember of(ServiceBusProperties busProperties, String... profiles) {

        ClusterMember member = new ClusterMember();
        member.name = StringUtils.toRootLowerCase(busProperties.getServiceName());
        member.profiles = Set.of(profiles);
        member.region = StringUtils.toRootLowerCase(busProperties.getRegion());
        member.environment = StringUtils.toRootLowerCase(busProperties.getEnvironment());

        return member;
    }
}
