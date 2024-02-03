//file:noinspection unused
package io.dmcs.servicebus.micronaut.config;

import io.micronaut.context.ApplicationContext;
import io.micronaut.context.annotation.Context;
import io.micronaut.context.env.Environment;
import io.micronaut.core.naming.conventions.StringConvention;
import jakarta.inject.Inject;
import lombok.Getter;
import org.apache.commons.lang3.StringUtils;

import java.time.ZonedDateTime;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

@Context
public class ServiceBusConfig {

    //@VisibleForTesting
    public static ServiceBusConfig INSTANCE;

    private Environment environment;

    private static Map<String, Object> propertiesMap;
    //@VisibleForTesting
    public ProfileMode profileMode;

    @Getter
    private Set<String> profiles;

    @Inject
    private ServiceBusConfigProperties serviceBusConfigProperties;

    @Inject
    private Optional<GitProperties> gitProperties;

    public ServiceBusConfig(ApplicationContext applicationContext) {

        INSTANCE = this;
        INSTANCE.environment = applicationContext.getEnvironment();
        propertiesMap = INSTANCE.environment.getProperties("servicebus", StringConvention.RAW);
        if (INSTANCE.environment.getActiveNames().stream().anyMatch(s -> s.contains("_test")))
            INSTANCE.profileMode = ProfileMode.TEST;
        else if (INSTANCE.environment.getActiveNames().stream().anyMatch(s -> s.contains("test")))
            INSTANCE.profileMode = ProfileMode.UNIT_TESTS;
        else if (INSTANCE.environment.getActiveNames().stream().anyMatch(s -> s.contains("prod")))
            INSTANCE.profileMode = ProfileMode.PRODUCTION;
        else if (INSTANCE.environment.getActiveNames().stream().anyMatch(s -> s.contains("dev")))
            INSTANCE.profileMode = ProfileMode.DEVELOPMENT;
        else
            INSTANCE.profileMode = ProfileMode.UNIT_TESTS;

        INSTANCE.profiles = INSTANCE.environment.getActiveNames();
    }

    /**
     * Returns true if the system is running in production
     */
    public static boolean isProduction() {
        return INSTANCE.profileMode == ProfileMode.PRODUCTION;
    }

    public static boolean isInUnitTest() {
        return INSTANCE.profileMode == ProfileMode.UNIT_TESTS;
    }

    public static boolean isDevOrUnitTest() {
        return isInUnitTest() || isDevelop();
    }

    public static boolean isDevelop() {
        return INSTANCE.profileMode == ProfileMode.DEVELOPMENT;
    }

    /**
     * Returns true if the system is running in production
     */
    @Deprecated
    public static boolean isProd() {
        return isProduction();
    }

    @Deprecated
    public static boolean isTesting() {
        return isInUnitTest();
    }

    public static boolean isTestingOrDev() {
        return isDevOrUnitTest();
    }

    public static String getAppName() {
        return INSTANCE.serviceBusConfigProperties.getApp().getName();
    }

    @Deprecated
    public static String getAppTitle() {
        return getAppName();
    }

    public static boolean isAuditingEnabled() {
        return INSTANCE.serviceBusConfigProperties.getAuditing().isEnabled();
    }

    public static String getEnvironment() {
        return INSTANCE.serviceBusConfigProperties.getStack().getEnvironment();
    }

    public static String getStackId() {
        return INSTANCE.serviceBusConfigProperties.getStack().getId();
    }

    public static String getConfigProperty(String property) {
        return getConfigProperty(property, null);
    }

    public static String getConfigProperty(String property, String defaultValue) {
        return StringUtils.defaultString(
                (String) propertiesMap.get(property),
                defaultValue
        );
    }

    public static String getServerUrl() {
        return String.format("http://localhost:%s%s",
                getConfigProperty("micronaut.server.port", "80"),
                getConfigProperty("micronaut.server.context-path", "/")
        );
    }

    public static boolean isProfileActive(String name) {
        return INSTANCE.profiles.stream().anyMatch(s -> s.equalsIgnoreCase(name));
    }

    public static String getAppVersion() {

        var app = INSTANCE.serviceBusConfigProperties.getApp();
        if (app != null && !StringUtils.isEmpty(app.getVersion()))
            return app.getVersion();

        if (INSTANCE.gitProperties.isPresent())
            return INSTANCE.gitProperties.get().getBuildVersion();

        return "N/A";
    }

    public static Optional<ZonedDateTime> getBuildDate() {

        if (INSTANCE.gitProperties.isPresent())
            return INSTANCE.gitProperties.get().getBuildDate();
        return Optional.empty();
    }

    public enum ProfileMode {
        PRODUCTION,
        TEST,
        UNIT_TESTS,
        DEVELOPMENT;
    }
}
