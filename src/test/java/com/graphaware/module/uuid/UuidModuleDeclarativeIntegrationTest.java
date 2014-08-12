/*
 * Copyright (c) 2014 GraphAware
 *
 * This file is part of GraphAware.
 *
 * GraphAware is free software: you can redistribute it and/or modify it under the terms of
 * the GNU General Public License as published by the Free Software Foundation, either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 *  without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details. You should have received a copy of
 * the GNU General Public License along with this program.  If not, see
 * <http://www.gnu.org/licenses/>.
 */
package com.graphaware.module.uuid;

import com.graphaware.test.integration.DatabaseIntegrationTest;
import org.junit.Test;
import org.neo4j.graphdb.*;
import org.neo4j.test.TestGraphDatabaseFactory;
import org.neo4j.tooling.GlobalGraphOperations;

import static org.junit.Assert.assertTrue;

public class UuidModuleDeclarativeIntegrationTest extends DatabaseIntegrationTest {

    private final Label personLabel = DynamicLabel.label("Person");
    private final String UUID = "uuid";

    @Override
    protected GraphDatabaseService createDatabase() {
        return new TestGraphDatabaseFactory().newImpermanentDatabaseBuilder()
                .loadPropertiesFromFile(this.getClass().getClassLoader().getResource("neo4j-uuid.properties").getPath())
                .newGraphDatabase();
    }

    @Test
    public void testUuidAssigned() {

        //When
        try (Transaction tx = getDatabase().beginTx()) {
            Node node = getDatabase().createNode();
            node.addLabel(personLabel);
            node.setProperty("name", "aNode");
            tx.success();
        }

        //Then
        //Retrieve the node and check that it has a uuid property
        try (Transaction tx = getDatabase().beginTx()) {
            for (Node node : GlobalGraphOperations.at(getDatabase()).getAllNodesWithLabel(personLabel)) {
                assertTrue(node.hasProperty(UUID));
            }
            tx.success();
        }

    }
}
