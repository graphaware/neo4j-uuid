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

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;


public class UuidModuleEmbeddedProgrammaticTest {

    private GraphDatabaseService database;
    private final Label testLabel = DynamicLabel.label("test");
    private final Label personLabel = DynamicLabel.label("Person");
    private UuidConfiguration uuidConfiguration;

    @Before
    public void setUp() {
        database = new TestGraphDatabaseFactory().newImpermanentDatabase();
    }

    @After
    public void tearDown() {
        database.shutdown();
    }


    public void moduleShouldInitializeCorrectly() {
        //TODO ask MB how to test this
    }

    @Test
    public void newNodesWithLabelShouldBeAssignedUuid() {
        //Given
        registerModuleWithNoLabels();

        //When
        try (Transaction tx = database.beginTx()) {
            Node node = database.createNode();
            node.addLabel(testLabel);
            node.setProperty("name", "aNode");
            tx.success();
        }

        //Then
        //Retrieve the node and check that it has a uuid property
        try (Transaction tx = database.beginTx()) {
            for (Node node : GlobalGraphOperations.at(database).getAllNodesWithLabel(testLabel)) {
                assertTrue(node.hasProperty(uuidConfiguration.getUuidProperty()));
            }
            tx.success();
        }
    }

    @Test
    public void newNodesWithoutLabelShouldBeAssignedUuid() {
        //Given
        registerModuleWithNoLabels();

        //When
        try (Transaction tx = database.beginTx()) {
            Node node = database.createNode();
            node.setProperty("name", "aNode");
            tx.success();
        }

        //Then
        //Retrieve the node and check that it has a uuid property
        try (Transaction tx = database.beginTx()) {
            for (Node node : GlobalGraphOperations.at(database).getAllNodes()) {
                assertTrue(node.hasProperty(uuidConfiguration.getUuidProperty()));
            }
            tx.success();
        }
    }


    @Test(expected = TransactionFailureException.class)
    public void shouldNotBeAbleToChangeTheUuidOfLabeledNode() {
        Node node;

        //Given
        registerModuleWithNoLabels();

        try (Transaction tx = database.beginTx()) {
            node = database.createNode();
            node.addLabel(testLabel);
            node.setProperty("name", "aNode");
            tx.success();
        }

        //When
        try (Transaction tx = database.beginTx()) {
            for (Node n : GlobalGraphOperations.at(database).getAllNodesWithLabel(testLabel)) {
                n.setProperty(uuidConfiguration.getUuidProperty(), "aNewUuid");
            }
            tx.success();
        }

        //Then
        //Exception should be thrown
    }

    @Test(expected = TransactionFailureException.class)
    public void shouldNotBeAbleToChangeTheUuidOfUnlabeledNode() {
        Node node;

        //Given
        registerModuleWithNoLabels();

        try (Transaction tx = database.beginTx()) {
            node = database.createNode();
            node.setProperty("name", "aNode");
            tx.success();
        }

        //When
        try (Transaction tx = database.beginTx()) {
            for (Node n : GlobalGraphOperations.at(database).getAllNodes()) {
                n.setProperty(uuidConfiguration.getUuidProperty(), "aNewUuid");
            }
            tx.success();
        }

        //Then
        //Exception should be thrown
    }

    @Test(expected = TransactionFailureException.class)
    public void shouldNotBeAbleToDeleteTheUuidOfLabeledNode() {
        Node node;

        //Given
        registerModuleWithNoLabels();

        try (Transaction tx = database.beginTx()) {
            node = database.createNode();
            node.addLabel(testLabel);
            node.setProperty("name", "aNode");
            tx.success();
        }

        //When
        try (Transaction tx = database.beginTx()) {
            for (Node n : GlobalGraphOperations.at(database).getAllNodesWithLabel(testLabel)) {
                n.removeProperty(uuidConfiguration.getUuidProperty());
            }
            tx.success();
        }

        //Then
        //Exception should be thrown
    }

    @Test(expected = TransactionFailureException.class)
    public void shouldNotBeAbleToDeleteTheUuidOfUnlabeledNode() {
        Node node;

        //Given
        registerModuleWithNoLabels();

        try (Transaction tx = database.beginTx()) {
            node = database.createNode();
            node.setProperty("name", "aNode");
            tx.success();
        }

        //When
        try (Transaction tx = database.beginTx()) {
            for (Node n : GlobalGraphOperations.at(database).getAllNodes()) {
                n.removeProperty(uuidConfiguration.getUuidProperty());
            }
            tx.success();
        }
        //Then
        //Exception should be thrown
    }


    @Test
    public void uuidShouldBeAssignedToNodeWithLabelSpecifiedInConfig() {
        //Given
        registerModuleWithLabels();

        //When
        try (Transaction tx = database.beginTx()) {
            Node node = database.createNode();
            node.addLabel(personLabel);
            node.setProperty("name", "aNode");
            tx.success();
        }

        //Then
        //Retrieve the node and check that it has a uuid property
        try (Transaction tx = database.beginTx()) {
            for (Node node : GlobalGraphOperations.at(database).getAllNodesWithLabel(personLabel)) {
                assertTrue(node.hasProperty(uuidConfiguration.getUuidProperty()));
            }
            tx.success();
        }
    }

    @Test
    public void uuidShouldNotBeAssignedToNodeWithLabelNotSpecifiedInConfig() {
        //Given
        registerModuleWithLabels();

        //When
        try (Transaction tx = database.beginTx()) {
            Node node = database.createNode();
            node.addLabel(testLabel);
            node.setProperty("name", "aNode");
            tx.success();
        }

        //Then
        //Retrieve the node and check that it has no uuid property
        try (Transaction tx = database.beginTx()) {
            for (Node node : GlobalGraphOperations.at(database).getAllNodesWithLabel(testLabel)) {
                assertFalse(node.hasProperty(uuidConfiguration.getUuidProperty()));
            }
            tx.success();
        }
    }

    @Test
    public void uuidShouldNotBeAssignedToNodeWithNoLabel() {
        //Given
        registerModuleWithLabels();

        //When
        try (Transaction tx = database.beginTx()) {
            Node node = database.createNode();
            node.setProperty("name", "aNode");
            tx.success();
        }

        //Then
        //Retrieve the node and check that it has no uuid property
        try (Transaction tx = database.beginTx()) {
            for (Node node : GlobalGraphOperations.at(database).getAllNodes()) {
                if (!node.getLabels().iterator().hasNext()) {  //Exclude GA nodes
                    assertFalse(node.hasProperty(uuidConfiguration.getUuidProperty()));
                }
            }
            tx.success();
        }
    }


    @Test
    public void shouldBeAbleToChangeTheUuidOfLabeledNodeNotConfigured() {
        Node node;

        //Given
        registerModuleWithLabels();

        try (Transaction tx = database.beginTx()) {
            node = database.createNode();
            node.addLabel(testLabel);
            node.setProperty("name", "aNode");
            tx.success();
        }

        //When
        try (Transaction tx = database.beginTx()) {
            for (Node n : GlobalGraphOperations.at(database).getAllNodesWithLabel(testLabel)) {
                n.setProperty(uuidConfiguration.getUuidProperty(), "aNewUuid");
            }
            tx.success();
        }

        //Then
        try (Transaction tx = database.beginTx()) {
            for (Node n : GlobalGraphOperations.at(database).getAllNodesWithLabel(testLabel)) {
                assertEquals("aNewUuid", n.getProperty(uuidConfiguration.getUuidProperty()));
            }
            tx.success();
        }
    }

    @Test(expected = TransactionFailureException.class)
    public void shouldNotBeAbleToChangeTheUuidOfLabeledNodeConfigured() {
        Node node;

        //Given
        registerModuleWithNoLabels();

        try (Transaction tx = database.beginTx()) {
            node = database.createNode();
            node.addLabel(personLabel);
            node.setProperty("name", "aNode");
            tx.success();
        }

        //When
        try (Transaction tx = database.beginTx()) {
            for (Node n : GlobalGraphOperations.at(database).getAllNodesWithLabel(personLabel)) {
                n.setProperty(uuidConfiguration.getUuidProperty(), "aNewUuid");
            }
            tx.success();
        }

        //Then
        //Exception should be thrown
    }

    @Test
    public void shouldBeAbleToChangeTheUuidOfUnlabeledNodeWithLabelConfiguration() {
        Node node;

        //Given
        registerModuleWithLabels();

        try (Transaction tx = database.beginTx()) {
            node = database.createNode();
            node.setProperty("name", "aNode");
            tx.success();
        }

        //When
        try (Transaction tx = database.beginTx()) {
            for (Node n : GlobalGraphOperations.at(database).getAllNodes()) {
                if (!node.getLabels().iterator().hasNext()) {  //Exclude GA nodes
                    n.setProperty(uuidConfiguration.getUuidProperty(), "aNewUuid");
                }
            }
            tx.success();
        }

        //Then
        try (Transaction tx = database.beginTx()) {
            for (Node n : GlobalGraphOperations.at(database).getAllNodes()) {
                if (!node.getLabels().iterator().hasNext()) {  //Exclude GA nodes
                    assertEquals("aNewUuid", n.getProperty(uuidConfiguration.getUuidProperty()));
                }
            }
            tx.success();
        }
    }

    @Test
    public void shouldBeAbleToDeleteTheUuidOfLabeledNodeNotConfigured() {
        Node node;

        //Given
        registerModuleWithLabels();

        try (Transaction tx = database.beginTx()) {
            node = database.createNode();
            node.addLabel(testLabel);
            node.setProperty("name", "aNode");
            tx.success();
        }

        //When
        try (Transaction tx = database.beginTx()) {
            for (Node n : GlobalGraphOperations.at(database).getAllNodesWithLabel(testLabel)) {
                n.removeProperty(uuidConfiguration.getUuidProperty());
            }
            tx.success();
        }

        //Then
        try (Transaction tx = database.beginTx()) {
            for (Node n : GlobalGraphOperations.at(database).getAllNodesWithLabel(testLabel)) {
                assertFalse(n.hasProperty(uuidConfiguration.getUuidProperty()));
            }
            tx.success();
        }
    }

    @Test(expected = TransactionFailureException.class)
    public void shouldNotBeAbleToDeleteTheUuidOfLabeledNodeConfigured() {
        Node node;

        //Given
        registerModuleWithNoLabels();

        try (Transaction tx = database.beginTx()) {
            node = database.createNode();
            node.addLabel(personLabel);
            node.setProperty("name", "aNode");
            tx.success();
        }

        //When
        try (Transaction tx = database.beginTx()) {
            for (Node n : GlobalGraphOperations.at(database).getAllNodesWithLabel(personLabel)) {
                n.removeProperty(uuidConfiguration.getUuidProperty());
            }
            tx.success();
        }
        //Then
        //Exception should be thrown
    }

    @Test
    public void shouldBeAbleToDeleteTheUuidOfUnlabeledNodeWithConfiguration() {
        Node node;

        //Given
        registerModuleWithLabels();

        try (Transaction tx = database.beginTx()) {
            node = database.createNode();
            node.setProperty("name", "aNode");
            tx.success();
        }

        //When
        try (Transaction tx = database.beginTx()) {
            for (Node n : GlobalGraphOperations.at(database).getAllNodes()) {
                if (!node.getLabels().iterator().hasNext()) {  //Exclude GA nodes
                    n.removeProperty(uuidConfiguration.getUuidProperty());
                }
            }
            tx.success();
        }
        //Then
        try (Transaction tx = database.beginTx()) {
            for (Node n : GlobalGraphOperations.at(database).getAllNodes()) {
                if (!node.getLabels().iterator().hasNext()) {  //Exclude GA nodes
                    assertFalse(n.hasProperty(uuidConfiguration.getUuidProperty()));
                }
            }
            tx.success();
        }
    }

    private void registerModuleWithNoLabels() {
        uuidConfiguration = UuidConfiguration.defaultConfiguration()
                .withUuidProperty("uuid");
        GraphAwareRuntime runtime = GraphAwareRuntimeFactory.createRuntime(database);
        UuidModule module = new UuidModule("UUIDM", uuidConfiguration);
        runtime.registerModule(module);
        runtime.start();
    }

    private void registerModuleWithLabels() {
        List<String> labels = new ArrayList<>(2);
        labels.add("Person");
        labels.add("Company");

        uuidConfiguration = UuidConfiguration.defaultConfiguration()
                .withUuidProperty("uuid")
                .withLabels(labels);

        GraphAwareRuntime runtime = GraphAwareRuntimeFactory.createRuntime(database);
        UuidModule module = new UuidModule("UUIDM", uuidConfiguration);
        runtime.registerModule(module);
        runtime.start();
    }


}
