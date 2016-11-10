package ga.guid.result;

import org.neo4j.graphdb.Node;

import java.util.List;

public class NodeListResult {

    public final List<Node> nodes;

    public NodeListResult(List<Node> nodes) {
        this.nodes = nodes;
    }

}
