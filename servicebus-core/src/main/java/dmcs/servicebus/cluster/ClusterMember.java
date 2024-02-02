package dmcs.servicebus.cluster;

import com.fasterxml.jackson.annotation.JsonRootName;
import dmcs.servicebus.config.ServiceBusProperties;
import io.micronaut.context.env.Environment;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.Set;

@JsonRootName("member")
@Getter
@Setter
@ToString
public class ClusterMember {

    private String appName;
    private String stackId;
    private String environment;
    private Set<String> profiles;

    public ClusterMember() {
        // for jackson
    }

    public static ClusterMember of(ServiceBusProperties busProperties, Environment environment) {

        ClusterMember member = new ClusterMember();
        member.appName = busProperties.getApp().getName();
        member.profiles = environment.getActiveNames();

        if (busProperties.getStack() != null) {

            member.stackId = busProperties.getStack().getId().toLowerCase();
            if (busProperties.getStack().getEnvironment() != null)
                member.environment = busProperties.getStack().getEnvironment().toLowerCase();
        }

        return member;
    }
}
