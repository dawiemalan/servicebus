package dmcs.servicebus.address;

import lombok.Builder;
import lombok.Getter;
import lombok.extern.jackson.Jacksonized;

@Builder
@Jacksonized
@Getter
public class ServiceLookupQuery {

    private String name;
    private String instanceId;
    private Boolean leader;
}
