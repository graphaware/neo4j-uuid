/*
 * Copyright (c) 2013-2020 GraphAware
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

package com.graphaware.module.uuid;

import com.graphaware.runtime.bootstrap.RuntimeExtensionFactory;
import com.graphaware.test.integration.GraphAwareNeo4jBuilder;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Result;
import org.neo4j.graphdb.Transaction;
import org.neo4j.harness.Neo4j;
import org.neo4j.harness.Neo4jBuilders;

import java.util.ArrayList;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class UuidModuleMultipleModulesTest {

    private Neo4j neo4j;
    private GraphDatabaseService database;

    @BeforeEach
    public void setUp() {
        neo4j = GraphAwareNeo4jBuilder.builder(Neo4jBuilders.newInProcessBuilder())
                .withDisabledServer()
                .withExtensionFactories(new ArrayList<>(Collections.singleton(new RuntimeExtensionFactory())))
                .withGAConfig("com.graphaware.runtime.enabled", "*")
                .withGAConfig("com.graphaware.module.neo4j.UID1.1", "com.graphaware.module.uuid.UuidBootstrapper")
                .withGAConfig("com.graphaware.module.neo4j.UID1.uuidProperty", "customerId")
                .withGAConfig("com.graphaware.module.neo4j.UID1.node", "hasLabel('Customer')")
                .withGAConfig("com.graphaware.module.neo4j.UID2.2", "com.graphaware.module.uuid.UuidBootstrapper")
                .withGAConfig("com.graphaware.module.neo4j.UID2.uuidProperty", "userId")
                .withGAConfig("com.graphaware.module.neo4j.UID2.node", "hasLabel('User')")
                .build();

        database = neo4j.defaultDatabaseService();
    }

    @AfterEach
    public void tearDown() {
        neo4j.close();

        GraphAwareNeo4jBuilder.cleanup();
    }

    @Test
    public void testProcedures() {
        //Create & Assign
        String cid, uid;
        try (Transaction tx = database.beginTx()) {
            cid = singleValue(tx.execute("CREATE (c:Customer {name:'c1'}) RETURN id(c)"));
            uid = singleValue(tx.execute("CREATE (u:User {name:'u1'}) RETURN id(u)"));

            tx.commit();
        }

        database.executeTransactionally("CREATE (:SomethingElse {name:'s1'})");

        String uuid;
        try (Transaction tx = database.beginTx()) {
            uuid = singleValue(tx.execute("MATCH (c:Customer) RETURN c.customerId"));

            tx.commit();
        }

        //Retrieve
        assertEquals(cid, findNodeByUuid("Customer", "customerId", uuid));

        try (Transaction tx = database.beginTx()) {
            uuid = singleValue(tx.execute("MATCH (u:User) RETURN u.userId"));

            tx.commit();
        }

        //Retrieve
        assertEquals(uid, findNodeByUuid("User", "userId", uuid));
    }

    private String findNodeByUuid(String label, String property, String uuid) {
        try (Transaction tx = database.beginTx()) {
            return singleValue(tx.execute("MATCH (n:" + label + " {" + property + ":'" + uuid + "'}) RETURN id(n) as id"));
        }
    }

    private String singleValue(Result result) {
        return result.next().values().iterator().next().toString();
    }
}
