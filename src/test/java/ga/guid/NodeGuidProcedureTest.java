package ga.guid;

import org.junit.Test;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Result;
import org.neo4j.graphdb.Transaction;

import ga.guid.NodeGuidProcedure;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.*;

public class NodeGuidProcedureTest extends ProcedureIntegrationTest {

    @Override
    protected Class<NodeGuidProcedure> procedureClass() {
        return NodeGuidProcedure.class;
    }

    @Test
    public void testGetNodeByGuid() {
        Long aleId = createPerson("Alessandro");
        String aleGuid = getGuidForNode(aleId);
        try (Transaction tx = getDatabase().beginTx()) {
            Result result = getDatabase().execute("CALL ga.guid.findNode('" + aleGuid + "') YIELD node RETURN node as n");
            while (result.hasNext()) {
                Map<String, Object> row = result.next();
                Node node = (Node) row.get("n");
                assertEquals(aleId, node.getId(), 0L);
            }

            tx.success();
        }
    }

    @Test
    public void testGetNodesWithMultipleGuids() {
        Long aleId = createPerson("Alessandro");
        Long luanneId = createPerson("Luanne");
        String aleGuid = getGuidForNode(aleId);
        String luanneGuid = getGuidForNode(luanneId);
        Map<String, Object> params = new HashMap<>();
        List<String> ids = new ArrayList<>();
        ids.add(aleGuid);
        ids.add(luanneGuid);
        params.put("guids", ids);

        try (Transaction tx = getDatabase().beginTx()) {
            Result result = getDatabase().execute("CALL ga.guid.findNodes({guids}) YIELD nodes RETURN nodes", params);
            while (result.hasNext()) {
                Map<String, Object> row = result.next();
                @SuppressWarnings("unchecked")
				List<Node> nodes = (List<Node>) row.get("nodes");
                assertEquals(2, nodes.size());
                assertEquals(aleId, nodes.get(0).getId(), 0L);
                assertEquals(luanneId, nodes.get(1).getId(), 0L);
            }
            tx.success();
        }
    }

    private String getGuidForNode(Long id) {
        String guid = null;
        try (Transaction tx = getDatabase().beginTx()) {
            guid = getDatabase().getNodeById(id).getProperty("guid").toString();
            tx.success();
        }

        return guid;
    }

    private Long createPerson(String name) {
        Long id = null;
        Map<String, Object> props = new HashMap<>();
        props.put("name", name);
        try (Transaction tx = getDatabase().beginTx()) {
            Result result = getDatabase().execute("CREATE (n:Person {name: {name} }) RETURN id(n) as id", props);
            while (result.hasNext()) {
                Map<String, Object> row = result.next();
                id = (long) row.get("id");
            }

            tx.success();
        }

        return id;
    }
}
