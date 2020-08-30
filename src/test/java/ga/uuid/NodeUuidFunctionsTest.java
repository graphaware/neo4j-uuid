package ga.uuid;

import com.graphaware.test.integration.DatabaseIntegrationTest;
import org.junit.jupiter.api.Test;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Result;
import org.neo4j.graphdb.Transaction;
import org.neo4j.harness.TestServerBuilder;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class NodeUuidFunctionsTest extends DatabaseIntegrationTest {

    @Override
    protected String configFile() {
        return "neo4j-uuid-all.conf";
    }

    @Override
    protected TestServerBuilder registerProceduresAndFunctions(TestServerBuilder testServerBuilder) throws Exception {
        return super.registerProceduresAndFunctions(testServerBuilder)
                .withFunction(NodeUuidFunctions.class)
                .withFunction(ga.uuid.nd.NodeUuidFunctions.class);
    }

    @Test
    public void testGetNodeByUuid() {
        Long aleId = createPerson("Alessandro");
        String aleUuid = getUuidForNode(aleId);
        try (Transaction tx = getDatabase().beginTx()) {
            Result result = getDatabase().execute("RETURN ga.uuid.findNode('" + aleUuid + "') as n");
            while (result.hasNext()) {
                Map<String, Object> row = result.next();
                Node node = (Node) row.get("n");
                assertEquals(aleId, node.getId(), 0L);
            }

            tx.success();
        }
    }

    @Test
    public void testGetNodesWithMultipleUuids() {
        Long aleId = createPerson("Alessandro");
        Long luanneId = createPerson("Luanne");
        String aleUuid = getUuidForNode(aleId);
        String luanneUuid = getUuidForNode(luanneId);
        Map<String, Object> params = new HashMap<>();
        List<String> ids = new ArrayList<>();
        ids.add(aleUuid);
        ids.add(luanneUuid);
        params.put("uuids", ids);

        try (Transaction tx = getDatabase().beginTx()) {
            Result result = getDatabase().execute("RETURN ga.uuid.findNodes({uuids}) as nodes", params);
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

    private String getUuidForNode(Long id) {
        String uuid;
        try (Transaction tx = getDatabase().beginTx()) {
            uuid = getDatabase().getNodeById(id).getProperty("uuid").toString();
            tx.success();
        }

        return uuid;
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
