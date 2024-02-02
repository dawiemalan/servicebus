package dmcs.servicebus.cluster.impl.zookeeper.nodes;

import com.google.common.graph.Graph;
import com.google.common.graph.GraphBuilder;
import com.google.common.graph.MutableGraph;
import lombok.SneakyThrows;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.utils.ZKPaths;

import javax.annotation.Nullable;

@SuppressWarnings("UnstableApiUsage")
public class ZkNodeTree {

    private CuratorFramework curator;
    private Graph<ZkNode> nodeGraph;

    @SneakyThrows
    public ZkNodeTree(CuratorFramework curator) {

        this.curator = curator;
        rebuildTree();
    }

    @SneakyThrows
    private void rebuildTree() {

        MutableGraph<ZkNode> graph = GraphBuilder.undirected().build();
        ZkNode rootNode = makeNode(null, "/");
        assert rootNode != null;
        graph.addNode(rootNode);
        buildChildNodes(rootNode, graph);

        nodeGraph = graph;
    }

    @SneakyThrows
    private void buildChildNodes(ZkNode parent, MutableGraph<ZkNode> graph) {

        curator.getChildren().forPath(ZKPaths.makePath(parent.getPath(), parent.getName())).forEach(s -> {
            var child = makeNode(parent, s);
            if (child != null) {
                graph.putEdge(parent, child);

                // recursive
                buildChildNodes(child, graph);
            }
        });
    }

    @SneakyThrows
    private ZkNode makeNode(@Nullable ZkNode parent, String name) {

        String nodePath = ZKPaths.makePath(parent.getPath(), parent.getName());
        var stat = curator.checkExists().forPath(ZKPaths.makePath(nodePath, name));
        if (stat == null)
            return null;

        return ZkNode.builder()
                .parent(parent)
                .name(name)
                .stat(stat)
                .build();
    }

    private ZkNode updateNode(ZkNode node) throws Exception {

        var stat = curator.checkExists().forPath(ZKPaths.makePath(node.getPath(), node.getName()));
        if (stat == null)
            return null;

        node.setStat(stat);
        return node;
    }
}
