package ga.guid;

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

import ga.guid.RelationshipGuidProcedure;

public class RelationshipGuidProcedureTest extends ProcedureIntegrationTest {

    @Override
    protected Class<RelationshipGuidProcedure> procedureClass() {
        return RelationshipGuidProcedure.class;
    }

    @Test
    public void testGetRelationshipByGuid() {
        Long relId = createRelAndGetId();
        String guid = getGuidForRelId(relId);
        int i = 0;
        try (Transaction tx = getDatabase().beginTx()) {
            Result result = getDatabase().execute("CALL ga.guid.findRelationship('" + guid + "') YIELD relationship RETURN id(relationship) as id");
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
    public void testGetMultipleRelationshipsByGuid() {
        Long relId1 = createRelAndGetId();
        Long relId2 = createRelAndGetId();
        String guid1 = getGuidForRelId(relId1);
        String guid2 = getGuidForRelId(relId2);
        Map<String, Object> params = new HashMap<>();
        List<String> guids = new ArrayList<>();
        guids.add(guid1);
        guids.add(guid2);
        params.put("rels", guids);
        int i = 0;
        try (Transaction tx = getDatabase().beginTx()) {
            Result result = getDatabase().execute("CALL ga.guid.findRelationships({rels}) YIELD relationships RETURN relationships", params);
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

    private String getGuidForRelId(Long id) {
        String guid = null;
        try (Transaction tx = getDatabase().beginTx()) {
            Relationship relationship = getDatabase().getRelationshipById(id);
            guid = relationship.getProperty("guid").toString();

            tx.success();
        }

        return guid;
    }
}
