/*
 * Copyright (c) 2013-2016 GraphAware
 *
 * This file is part of the GraphAware Framework.
 *
 * GraphAware Framework is free software: you can redistribute it and/or modify it under the terms of
 * the GNU General Public License as published by the Free Software Foundation, either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details. You should have received a copy of
 * the GNU General Public License along with this program.  If not, see
 * <http://www.gnu.org/licenses/>.
 */
package ga.uuid;

import static org.junit.Assert.assertEquals;

import java.util.HashMap;
import java.util.Map;

import org.junit.Test;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;
import org.neo4j.graphdb.Result;
import org.neo4j.graphdb.Transaction;
import org.neo4j.kernel.impl.proc.Procedures;

import com.graphaware.common.policy.BaseNodeInclusionPolicy;
import com.graphaware.common.policy.BaseRelationshipInclusionPolicy;
import com.graphaware.module.uuid.UuidConfiguration;
import com.graphaware.module.uuid.UuidModule;
import com.graphaware.runtime.GraphAwareRuntime;
import com.graphaware.runtime.GraphAwareRuntimeFactory;
import com.graphaware.runtime.module.RuntimeModule;
import com.graphaware.test.integration.cluster.HighAvailabilityClusterDatabasesIntegrationTest;

/**
 * Test for {@link NodeUuidProcedure} and {@link RelationshipUuidProcedure}
 */
public class UuidProcedureTestHighAvailability extends HighAvailabilityClusterDatabasesIntegrationTest {

	private void registerRuntimeWithModule(GraphDatabaseService database, RuntimeModule module) {
		GraphAwareRuntime runtime = GraphAwareRuntimeFactory.createRuntime(database);
		runtime.registerModule(module);
		runtime.start();
	}

	protected void registerModule(GraphDatabaseService database) {
		UuidConfiguration uuidConfiguration = UuidConfiguration.defaultConfiguration().withUuidProperty("uuid").withUuidIndex("uuidIndex")
				.with(new BaseNodeInclusionPolicy() {
					@Override
					public boolean include(Node node) {
						return true;
					}
				}).with(new BaseRelationshipInclusionPolicy() {

					@Override
					public boolean include(Relationship relationship) {
						return true;
					}

					@Override
					public boolean include(Relationship relationship, Node node) {
						return true;
					}
				});

		UuidModule module = new UuidModule(UuidModule.DEFAULT_MODULE_ID, uuidConfiguration, database);
		registerRuntimeWithModule(database, module);
	}

	@Override
	protected boolean shouldRegisterModules() {
		return true;
	}
	
	@Override
	protected boolean shouldRegisterProcedures() {
		return true;
	}
	
	@Override
	protected void registerProcedures(Procedures procedures) throws Exception {
		super.registerProcedures(procedures);
		procedures.registerProcedure(NodeUuidProcedure.class);
		procedures.registerProcedure(RelationshipUuidProcedure.class);
	}

    @Test
    public void testGetRelationshipByUuid_MASTER() {
        GraphDatabaseService db = getMasterDatabase();
		createPerson(db,"AlessandroMaster1");
        createPerson(db,"AlessandroMaster2");
        Long idRel = createRelation(db, "AlessandroMaster1", "AlessandroMaster2");
        String relUuid = getUuidForRelation(db, idRel);
        
        try (Transaction tx = db.beginTx()) {
        	Result result = db.execute("CALL ga.uuid.findRelationship('" + relUuid + "') YIELD relationship RETURN relationship as n");
            while (result.hasNext()) {
                Map<String, Object> row = result.next();
                Relationship rel = (Relationship) row.get("n");
                assertEquals(idRel, rel.getId(), 0L);
            }

            tx.success();
        }
    }
    
    @Test
    public void testGetRelationshipByUuid_SLAVE() {
        GraphDatabaseService db = getMasterDatabase();
		createPerson(db,"AlessandroSlave1");
        createPerson(db,"AlessandroSlave2");
        Long idRel = createRelation(db, "AlessandroSlave1", "AlessandroSlave2");
        String relUuid = getUuidForRelation(db, idRel);
        
        try (Transaction tx = db.beginTx()) {
        	Result result = db.execute("CALL ga.uuid.findRelationship('" + relUuid + "') YIELD relationship RETURN relationship as n");
            while (result.hasNext()) {
                Map<String, Object> row = result.next();
                Relationship rel = (Relationship) row.get("n");
                assertEquals(idRel, rel.getId(), 0L);
            }

            tx.success();
        }
    }
	
    @Test
    public void testGetNodeByUuid_MASTER() {
        Long aleId = createPerson(getMasterDatabase(),"AlessandroMaster");
        String aleUuid = getUuidForNode(getMasterDatabase(),aleId);
        try (Transaction tx = getMasterDatabase().beginTx()) {
            Result result = getMasterDatabase().execute("CALL ga.uuid.findNode('" + aleUuid + "') YIELD node RETURN node as n");
            while (result.hasNext()) {
                Map<String, Object> row = result.next();
                Node node = (Node) row.get("n");
                assertEquals(aleId, node.getId(), 0L);
            }

            tx.success();
        }
    }

    @Test
    public void testGetNodeByUuid_SLAVE() {
        Long aleId = createPerson(getOneSlaveDatabase(),"AlessandroSlave");
        String aleUuid = getUuidForNode(getOneSlaveDatabase(),aleId);
        try (Transaction tx = getOneSlaveDatabase().beginTx()) {
            Result result = getOneSlaveDatabase().execute("CALL ga.uuid.findNode('" + aleUuid + "') YIELD node RETURN node as n");
            while (result.hasNext()) {
                Map<String, Object> row = result.next();
                Node node = (Node) row.get("n");
                assertEquals(aleId, node.getId(), 0L);
            }

            tx.success();
        }
    }
    
    private String getUuidForNode(GraphDatabaseService db, Long id) {
        String uuid = null;
        try (Transaction tx = db.beginTx()) {
            uuid = db.getNodeById(id).getProperty("uuid").toString();
            tx.success();
        }

        return uuid;
    }

    private Long createPerson(GraphDatabaseService db, String name) {
        Long id = null;
        Map<String, Object> props = new HashMap<>();
        props.put("name", name);
        try (Transaction tx = db.beginTx()) {
            Result result = db.execute("CREATE (n:Person {name: {name} }) RETURN id(n) as id", props);
            while (result.hasNext()) {
                Map<String, Object> row = result.next();
                id = (long) row.get("id");
            }

            tx.success();
        }

        return id;
    }
    
    private Long createRelation(GraphDatabaseService db, String nameFrom, String nameTo) {
        Long id = null;
        Map<String, Object> props = new HashMap<>();
        props.put("nameFrom", nameFrom);
        props.put("nameTo", nameTo);
        
        try (Transaction tx = db.beginTx()) {
            Result result = db.execute("MATCH (a:Person {name: {nameFrom} }),(b:Person {name: {nameTo} }) \n CREATE (a)-[r:LIKES]->(b) RETURN id(r) as id", props);
            while (result.hasNext()) {
                Map<String, Object> row = result.next();
                id = (long) row.get("id");
            }

            tx.success();
        }

        return id;
	}
    

    private String getUuidForRelation(GraphDatabaseService db, Long id) {
        String uuid = null;
        try (Transaction tx = db.beginTx()) {
            uuid = db.getRelationshipById(id).getProperty("uuid").toString();
            tx.success();
        }

        return uuid;
    }
}
