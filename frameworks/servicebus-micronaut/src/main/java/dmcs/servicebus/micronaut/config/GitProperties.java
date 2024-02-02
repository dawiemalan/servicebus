package dmcs.servicebus.micronaut.config;

import io.micronaut.context.env.EmptyPropertySource;
import io.micronaut.context.env.PropertySource;
import io.micronaut.core.annotation.Nullable;
import io.micronaut.management.endpoint.info.source.GitInfoSource;
import jakarta.inject.Singleton;
import org.apache.commons.lang3.StringUtils;
import reactor.core.publisher.Mono;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

@Singleton
public class GitProperties {

    private PropertySource propertySource;

    public GitProperties(@Nullable GitInfoSource gitInfoSource) {

        if (gitInfoSource == null)
            return;

        this.propertySource = Mono.from(gitInfoSource.getSource())
                .defaultIfEmpty(new EmptyPropertySource()).block();

    }

    public String getBranch() {
        return StringUtils.defaultString((String) propertySource.get("git.branch"));
    }

    public String getBuildVersion() {
        return StringUtils.defaultString((String) propertySource.get("git.build.version"));
    }

    public Optional<ZonedDateTime> getBuildDate() {
        String timeString = StringUtils.defaultString((String) propertySource.get("git.commit.time"));
        if (StringUtils.isEmpty(timeString))
            return Optional.empty();

        return parseDate(timeString);
    }

    private Optional<ZonedDateTime> parseDate(String value) {
        if (StringUtils.isEmpty(value))
            return Optional.empty();
        return Optional.of(DateTimeFormatter.ISO_ZONED_DATE_TIME
                .parse(StringUtils.removeEnd(value, "00"), ZonedDateTime::from));
    }

}
//2023-01-31T07:28:25+0200
