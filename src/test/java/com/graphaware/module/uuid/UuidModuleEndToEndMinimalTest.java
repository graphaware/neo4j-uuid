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

import com.graphaware.test.integration.DatabaseIntegrationTest;
import org.junit.jupiter.api.Test;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;
import org.neo4j.graphdb.Transaction;
import org.neo4j.helpers.collection.Iterables;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class UuidModuleEndToEndMinimalTest extends DatabaseIntegrationTest {

    @Override
    protected String configFile() {
        return "neo4j-uuid-minimal.conf";
    }

    @Test
    public void testMinimalConfig() {
        getDatabase().execute("CREATE (p:Person {name:'Luanne'})-[:WORKS_FOR]->(c:Company {name:'GraphAware'})");

        try (Transaction tx = getDatabase().beginTx()) {
            for (Node node : Iterables.asResourceIterable(getDatabase().getAllNodes())) {
                assertTrue(node.hasProperty("uuid"));
            }

            for (Relationship rel : Iterables.asResourceIterable(getDatabase().getAllRelationships())) {
                assertFalse(rel.hasProperty("uuid"));
            }

            tx.success();
        }
    }
}
