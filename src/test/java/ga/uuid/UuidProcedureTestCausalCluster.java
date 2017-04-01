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

import com.graphaware.common.policy.inclusion.BaseNodeInclusionPolicy;
import com.graphaware.common.policy.inclusion.BaseRelationshipInclusionPolicy;
import org.junit.Before;
import org.junit.Test;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;
import org.neo4j.graphdb.Result;
import org.neo4j.graphdb.Transaction;
import org.neo4j.kernel.impl.proc.Procedures;

import com.graphaware.module.uuid.UuidConfiguration;
import com.graphaware.module.uuid.UuidModule;
import com.graphaware.runtime.GraphAwareRuntime;
import com.graphaware.runtime.GraphAwareRuntimeFactory;
import com.graphaware.runtime.module.RuntimeModule;
import com.graphaware.test.data.DatabasePopulator;
import com.graphaware.test.integration.cluster.CausalClusterDatabasesintegrationTest;

/**
 * Test for {@link NodeUuidProcedure} and {@link RelationshipUuidProcedure}
 */
public class UuidProcedureTestCausalCluster extends CausalClusterDatabasesintegrationTest {

	private static boolean initialized = false;
	private static String aleUuid;
	private static Long aleId;
	private static Long idRel;
	private static String relUuid;

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

	@Override
	protected DatabasePopulator databasePopulator() {
		return new DatabasePopulator() {
			
			@Override
			public void populate(GraphDatabaseService database) {
				aleId = createPerson(database,"Alessandro");
				aleUuid = getUuidForNode(database,aleId);

				createPerson(database,"Raffaello");
				idRel = createRelation(database, "Alessandro","Raffaello");
				relUuid = getUuidForRelation(database, idRel);
			}
		};
	}
	
	@Before
	@Override
	public void setUp() throws Exception {
		super.setUp();
		if(!initialized){
	    	GraphDatabaseService replDb = getOneReplicaDatabase();
	    	
	    	//waiting for replica synchronization
	        try (Transaction tx = replDb.beginTx()) {
	        	do{
	        		Thread.currentThread().sleep(1000L);
	        	} while (! (replDb.index().existsForNodes(UuidConfiguration.defaultConfiguration().getUuidIndex())
	        			&& replDb.index().existsForRelationships(UuidConfiguration.defaultConfiguration().getUuidRelationshipIndex())));
	        	
	            tx.failure();
	        }
	        
	        initialized = true;
		}
	}
	
    

	@Test
    public void testGetNodeByUuid_LEADER() {
    	GraphDatabaseService database = getLeaderDatabase();
		testCallNode(database);
    }

    @Test
    public void testGetNodeByUuid_FOLLOWER() {
    	GraphDatabaseService database = getOneFollowerDatabase();
		testCallNode(database);
    }
    
    @Test
    public void testGetNodeByUuid_REPLICA() throws InterruptedException {
    	GraphDatabaseService replDb = getOneReplicaDatabase();
		testCallNode(replDb);
    }
    
	@Test
    public void testGetRelationByUuid_LEADER() {
    	GraphDatabaseService database = getLeaderDatabase();
		testCallRel(database);
    }

    @Test
    public void testGetRelationByUuid_FOLLOWER() {
    	GraphDatabaseService database = getOneFollowerDatabase();
		testCallRel(database);
    }
    
    @Test
    public void testGetRelationByUuid_REPLICA() throws InterruptedException {
    	GraphDatabaseService replDb = getOneReplicaDatabase();
		testCallRel(replDb);
    }

	private void testCallRel(GraphDatabaseService database) {
        try (Transaction tx = database.beginTx()) {
            Result result = database.execute("CALL ga.uuid.findRelationship('" + relUuid + "') YIELD relationship RETURN relationship as n");
            while (result.hasNext()) {
                Map<String, Object> row = result.next();
                Relationship rel = (Relationship) row.get("n");
                assertEquals(idRel, rel.getId(), 0L);
            }

            tx.success();
        }
	}
    
	private void testCallNode(GraphDatabaseService database) {
        try (Transaction tx = database.beginTx()) {
            Result result = database.execute("CALL ga.uuid.findNode('" + aleUuid + "') YIELD node RETURN node as n");
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

    private String getUuidForRelation(GraphDatabaseService db, Long id) {
        String uuid = null;
        try (Transaction tx = db.beginTx()) {
            uuid = db.getRelationshipById(id).getProperty("uuid").toString();
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
}
