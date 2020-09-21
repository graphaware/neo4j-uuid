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
import org.neo4j.graphdb.Transaction;
import org.neo4j.harness.Neo4j;
import org.neo4j.harness.Neo4jBuilders;

import java.util.ArrayList;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class UuidModuleEndToEndMinimalTest {

    private Neo4j neo4j;
    private GraphDatabaseService database;

    @BeforeEach
    public void setUp() {
        neo4j = GraphAwareNeo4jBuilder.builder(Neo4jBuilders.newInProcessBuilder())
                .withDisabledServer()
                .withExtensionFactories(new ArrayList<>(Collections.singleton(new RuntimeExtensionFactory())))
                .withGAConfig("com.graphaware.runtime.enabled", "true")
                .withGAConfig("com.graphaware.module.UIDM.1", "com.graphaware.module.uuid.UuidBootstrapper")
                .build();

        database = neo4j.defaultDatabaseService();
    }

    @AfterEach
    public void tearDown() {
        neo4j.close();
    }

    @Test
    public void testMinimalConfig() {
        database.executeTransactionally("CREATE (p:Person {name:'Luanne'})-[:WORKS_FOR]->(c:Company {name:'GraphAware'})");

        try (Transaction tx = database.beginTx()) {
            tx.getAllNodes().forEach(node -> assertTrue(node.hasProperty("uuid")));

            tx.getAllRelationships().forEach(rel -> assertFalse(rel.hasProperty("uuid")));

            tx.commit();
        }
    }
}
