package io.dmcs.servicebus.cluster.impl.zookeeper.nodes;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;
import org.apache.zookeeper.data.Stat;

@Getter
@Builder
@EqualsAndHashCode(of = {"path", "name"}, cacheStrategy = EqualsAndHashCode.CacheStrategy.LAZY)
public class ZkNode {

    private ZkNode parent;
    private String name;
    @Setter
    private Stat stat;

    @Getter(lazy = true)
    private final String path = makePath();

    private String makePath() {

        if (parent == null) {
            if (!StringUtils.isEmpty(name))
                return name;
            else
                return "/";
        }

        return String.format("%s/%s", parent.getPath(), name);
    }
}
