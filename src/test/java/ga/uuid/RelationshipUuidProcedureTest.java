package ga.uuid;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Test;
import org.neo4j.graphdb.Relationship;
import org.neo4j.graphdb.Result;
import org.neo4j.graphdb.Transaction;

public class RelationshipUuidProcedureTest extends ProcedureIntegrationTest {

    @Override
    protected Class<RelationshipUuidProcedure> procedureClass() {
        return RelationshipUuidProcedure.class;
    }

    @Test
    public void testGetRelationshipByUuid() {
        Long relId = createRelAndGetId();
        String uuid = getUuidForRelId(relId);
        int i = 0;
        try (Transaction tx = getDatabase().beginTx()) {
            Result result = getDatabase().execute("CALL ga.uuid.findRelationship('" + uuid + "') YIELD relationship RETURN id(relationship) as id");
            while (result.hasNext()) {
                Map<String, Object> row = result.next();
                assertEquals(relId, (long) row.get("id"), 0L);
                ++i;
            }

            tx.success();
        }
        assertTrue(i > 0);
    }

    @Test
    public void testGetMultipleRelationshipsByUuid() {
        Long relId1 = createRelAndGetId();
        Long relId2 = createRelAndGetId();
        String uuid1 = getUuidForRelId(relId1);
        String uuid2 = getUuidForRelId(relId2);
        Map<String, Object> params = new HashMap<>();
        List<String> uuids = new ArrayList<>();
        uuids.add(uuid1);
        uuids.add(uuid2);
        params.put("rels", uuids);
        int i = 0;
        try (Transaction tx = getDatabase().beginTx()) {
            Result result = getDatabase().execute("CALL ga.uuid.findRelationships({rels}) YIELD relationships RETURN relationships", params);
            while (result.hasNext()) {
                ++i;
                Map<String, Object> row = result.next();
                @SuppressWarnings("unchecked")
				List<Relationship> rels = (List<Relationship>) row.get("relationships");
                assertEquals(2, rels.size());
                assertEquals(relId1, rels.get(0).getId(), 0L);
                assertEquals(relId2, rels.get(1).getId(), 0L);
            }

            tx.success();
        }

        assertTrue(i > 0);
    }

    private Long createRelAndGetId() {
        Long id = null;
        try (Transaction tx = getDatabase().beginTx()) {
            Result result = getDatabase().execute("CREATE (n)-[r:RELATES]->(b) RETURN id(r) as relId");
            while (result.hasNext()) {
                Map<String, Object> row = result.next();
                id = (long) row.get("relId");
            }
            tx.success();
        }

        return id;
    }

    private String getUuidForRelId(Long id) {
        String uuid = null;
        try (Transaction tx = getDatabase().beginTx()) {
            Relationship relationship = getDatabase().getRelationshipById(id);
            uuid = relationship.getProperty("uuid").toString();

            tx.success();
        }

        return uuid;
    }
}
