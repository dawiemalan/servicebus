package io.dmcs.servicebus.micronaut.config;

import io.micronaut.context.annotation.ConfigurationProperties;
import io.micronaut.core.annotation.Introspected;
import io.micronaut.core.annotation.NonNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@ConfigurationProperties(ServiceBusConfigProperties.PREFIX)
@ToString
@Introspected
@Getter
@Setter
@NoArgsConstructor
public class ServiceBusConfigProperties {

    static final String PREFIX = "servicebus";

    private App app = new App();
    private Stack stack = new Stack();
    private String locale;
    private Jms jms = new Jms();
    private Auditing auditing = new Auditing();
    private Api api = new Api();

    @ConfigurationProperties("app")
    @Getter
    @ToString
    @NoArgsConstructor
    public static class App {
        @NonNull
        String name;
        String version;
        String platform = "Micronaut";
    }

    /**
     * Convenience function for getStack().getId()
     */
    public String getStackId() {

        if (stack == null)
            return null;

        return stack.id;
    }

    /**
     * Convenience function for getStack().getEnvironment()
     */
    public String getEnvironment() {

        if (stack == null)
            return null;

        return stack.environment;
    }

    public String getAppName() {

        if (app == null)
            return null;

        return app.getName();
    }

    @ConfigurationProperties("stack")
    @ToString
    @Getter
    @NoArgsConstructor
    public static class Stack {
        @NonNull
        String id;
        @NonNull
        String environment;
    }

    @ConfigurationProperties("jms")
    @ToString
    @Getter
    @NoArgsConstructor
    public static class Jms {
        String queuePrefix;
    }

    @ConfigurationProperties("auditing")
    @ToString
    @Getter
    @NoArgsConstructor
    public static class Auditing {
        boolean enabled = false;
    }

    @ConfigurationProperties("api")
    @ToString
    @Getter
    @NoArgsConstructor
    public static class Api {

        /**
         * Enable logging of HTTP client requests and responses
         */
        boolean clientTrace;

        /**
         * Enable logging of controller requests and responses
         */
        boolean serverTrace;
    }
}
