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

import com.graphaware.runtime.GraphAwareRuntime;
import com.graphaware.runtime.GraphAwareRuntimeFactory;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.neo4j.graphdb.*;
import org.neo4j.test.TestGraphDatabaseFactory;
import org.neo4j.tooling.GlobalGraphOperations;

import static org.junit.Assert.assertTrue;


public class UuidModuleEmbeddedProgrammaticTest {

    private GraphDatabaseService database;
    private UuidApi uuidApi;
    private Label testLabel=DynamicLabel.label("test");

    @Before
    public void setUp() {
        database = new TestGraphDatabaseFactory().newImpermanentDatabase();
        GraphAwareRuntime runtime = GraphAwareRuntimeFactory.createRuntime(database);
        UuidModule module = new UuidModule("UUIDM", database);
        runtime.registerModule(module);
        runtime.start();
        uuidApi = new UuidApi(database);
    }

    @After
    public void tearDown() {
        database.shutdown();
    }


    public void moduleShouldInitializeCorrectly() {
        //TODO ask MB how to test this
    }

    @Test
    public void newNodesShouldBeAssignedUuid() {
        try (Transaction tx = database.beginTx()) {
            Node node = database.createNode();
            node.addLabel(testLabel);
            node.setProperty("name", "aNode");
            tx.success();
        }

        //Retrieve the node and check that it has a uuid property
        try (Transaction tx = database.beginTx()) {
            for (Node node : GlobalGraphOperations.at(database).getAllNodesWithLabel(testLabel)) {
                assertTrue(node.hasProperty(Properties.UUID));
            }
            tx.success();
        }

    }

    @Test
    public void shouldNotBeAbleToChangeTheUuid() {
        Node node;
        boolean exceptionThrown=false;

        try (Transaction tx = database.beginTx()) {
            node = database.createNode();
            node.addLabel(testLabel);
            node.setProperty("name", "aNode");
            tx.success();
        }

        try (Transaction tx = database.beginTx()) {
            for (Node n : GlobalGraphOperations.at(database).getAllNodesWithLabel(testLabel)) {
               n.setProperty(Properties.UUID,"aNewUuid");
            }
            tx.success();
        }
        catch (TransactionFailureException tfe) {
            exceptionThrown=true;
        }
        assertTrue("Expected an IllegalStateException",exceptionThrown);


    }

    @Test
    public void shouldNotBeAbleToDeleteTheUuid() {
        Node node;
        boolean exceptionThrown=false;

        try (Transaction tx = database.beginTx()) {
            node = database.createNode();
            node.addLabel(testLabel);
            node.setProperty("name", "aNode");
            tx.success();
        }

        try (Transaction tx = database.beginTx()) {
            for (Node n : GlobalGraphOperations.at(database).getAllNodesWithLabel(testLabel)) {
                n.removeProperty(Properties.UUID);
            }
            tx.success();
        }
        catch (TransactionFailureException ise) {
            exceptionThrown=true;
        }
        assertTrue("Expected an IllegalStateException",exceptionThrown);


    }


}
