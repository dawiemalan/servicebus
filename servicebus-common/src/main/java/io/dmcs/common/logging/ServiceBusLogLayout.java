package io.dmcs.common.logging;

import ch.qos.logback.classic.PatternLayout;
import org.apache.commons.lang3.StringUtils;

class ServiceBusLogLayout extends PatternLayout {

    private static final String[] SKIP_CLASSES = {
            "full",
            "java.base",
            "java.lang.reflect",
            "sun.reflect",
            "org.codehaus.groovy",
            "groovy.lang",
            "org.springframework.transaction",
            "org.springframework.cglib",
            "org.springframework.aop",
            "org.springframework.cache.interceptor",
            "org.springframework.boot.web",
            "org.springframework.boot.actuate",
            "io.micrometer.core.instrument",
            "io.netty",
            "io.reactivex",
            "io.micronaut.http.server.netty",
            "io.micronaut.http.server.context",
            "io.micronaut.web.router",
            "io.micronaut.http.netty",
            "io.micronaut.context",
            "io.micronaut.configuration.metrics.binder",
            "io.micronaut.aop.chain",
            "io.micronaut.test",
            "io.reactivex",
            "net.sf.cglib",
            "ByCGLIB",
            "net.engio.mbassy",
            "org.apache.catalina",
            "org.apache.tomcat",
            "java.util.concurrent",
            "feign",
            "jdk.internal",
            "org.spockframework.runtime",
            "org.junit.platform",
            "jakarta.servlet",
            "org.aspectj.lang"
    };

    @Override
    public void start() {

        if (StringUtils.contains(getPattern(), "%rEx")) {

            StringBuilder sb = new StringBuilder(getPattern());
            sb.append("%rEx{").append(StringUtils.join(SKIP_CLASSES, ',')).append("}");
            setPattern(sb.toString());
        }

        super.start();
    }
}
