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

import com.graphaware.common.uuid.EaioUuidGenerator;
import com.graphaware.common.uuid.UuidGenerator;
import com.graphaware.module.uuid.generator.JavaUtilUUIDGenerator;
import com.graphaware.runtime.GraphAwareRuntime;
import com.graphaware.runtime.bootstrap.RuntimeExtensionFactory;
import com.graphaware.runtime.policy.all.IncludeAllBusinessNodes;
import com.graphaware.runtime.policy.all.IncludeAllBusinessRelationships;
import com.graphaware.test.integration.GraphAwareNeo4jBuilder;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.neo4j.dbms.api.DatabaseManagementService;
import org.neo4j.graphdb.*;
import org.neo4j.harness.Neo4j;
import org.neo4j.harness.Neo4jBuilders;
import org.neo4j.monitoring.DatabaseEventListeners;
import org.neo4j.test.ReflectionUtil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

public class UuidModuleDeclarativeIntegrationTest {

    private final Label personLabel = Label.label("Person");
    private final Label ignoredLabel = Label.label("Ignored");
    private final RelationshipType knowsType = RelationshipType.withName("KNOWS");
    private final RelationshipType ignoredType = RelationshipType.withName("IGNORES");
    private final String UUID = "uuid";
    private final String SEQUENCE = "sequence";

    private Neo4j neo4j;
    private GraphDatabaseService database;
    private DatabaseManagementService databaseManagementService;

    @AfterEach
    public void tearDown() {
        neo4j.close();

        GraphAwareNeo4jBuilder.cleanup();
    }

    @Test
    public void testUuidAssigned() {
        setUpNeo4j(defaultBuilder());

        //When
        try (Transaction tx = database.beginTx()) {
            Node node = tx.createNode();
            node.addLabel(personLabel);
            node.setProperty("name", "aNode");
            tx.commit();
        }

        //Then
        //Retrieve the node and check that it has a uuid property
        try (Transaction tx = database.beginTx()) {
            tx.findNodes(personLabel).forEachRemaining(new Consumer<Node>() {
                @Override
                public void accept(Node node) {
                    assertTrue(node.hasProperty(UUID));
                }
            });

            tx.commit();
        }
    }

    @Test
    public void testUuidAssignedWithoutHyphens() {
        setUpNeo4j(emptyBuilder()
                .withGAConfig("com.graphaware.module.neo4j.UIDM.1", "com.graphaware.module.uuid.UuidBootstrapper")
                .withGAConfig("com.graphaware.module.neo4j.UIDM.uuidProperty", "uuid")
                .withGAConfig("com.graphaware.module.neo4j.UIDM.relationship", "com.graphaware.runtime.policy.all.IncludeAllBusinessRelationships")
                .withGAConfig("com.graphaware.module.neo4j.UIDM.stripHyphens", "true")
        );

        //When
        try (Transaction tx = database.beginTx()) {
            Node node = tx.createNode();
            node.addLabel(personLabel);
            node.setProperty("name", "aNode");
            tx.commit();
        }

        //Then
        //Retrieve the node and check that it has a uuid property
        try (Transaction tx = database.beginTx()) {
            tx.findNodes(personLabel).forEachRemaining(new Consumer<Node>() {
                @Override
                public void accept(Node node) {
                    assertTrue(node.hasProperty(UUID));
                    assertFalse(node.getProperty(UUID).toString().contains("-"));
                }
            });

            tx.commit();
        }
    }

    @Test
    public void testUuidCanBeChangedWhenImmutableIsFalse() {
        setUpNeo4j(emptyBuilder()
                .withGAConfig("com.graphaware.module.neo4j.UIDM.1", "com.graphaware.module.uuid.UuidBootstrapper")
                .withGAConfig("com.graphaware.module.neo4j.UIDM.uuidProperty", "uuid")
                .withGAConfig("com.graphaware.module.neo4j.UIDM.relationship", "com.graphaware.runtime.policy.all.IncludeAllBusinessRelationships")
                .withGAConfig("com.graphaware.module.neo4j.UIDM.stripHyphens", "true")
                .withGAConfig("com.graphaware.module.neo4j.UIDM.immutable", "false")
        );

        //When
        try (Transaction tx = database.beginTx()) {
            Node node = tx.createNode();
            node.addLabel(personLabel);
            node.setProperty("name", "aNode");
            tx.commit();
        }

        //Then
        //Retrieve the node and check that it has a uuid property

        Long nodeId = null;
        String oldUuid = null;
        String newUuid = null;

        try (Transaction tx = database.beginTx()) {
            for (Node node : tx.findNodes(personLabel).stream().collect(Collectors.toList())) {
                assertTrue(node.hasProperty(UUID));

                nodeId = node.getId();
                oldUuid = (String) node.getProperty(UUID);
            }
            tx.commit();
        }

        //Then
        //Change the node uuid property
        try (Transaction tx = database.beginTx()) {
            for (Node node : tx.findNodes(personLabel).stream().collect(Collectors.toList())) {
                newUuid = "123-" + nodeId;
                node.setProperty(UUID, newUuid);
            }
            tx.commit();
        }

        //Then
        // Check nodes have uuid changed
        try (Transaction tx = database.beginTx()) {
            for (Node node : tx.findNodes(personLabel).stream().collect(Collectors.toList())) {
                assertEquals(newUuid, node.getProperty(UUID).toString());
            }
            tx.commit();
        }
    }

    @Test
    public void testUuidCanBeRemovedWhenImmutableIsFalse() {
        setUpNeo4j(emptyBuilder()
                .withGAConfig("com.graphaware.module.neo4j.UIDM.1", "com.graphaware.module.uuid.UuidBootstrapper")
                .withGAConfig("com.graphaware.module.neo4j.UIDM.uuidProperty", "uuid")
                .withGAConfig("com.graphaware.module.neo4j.UIDM.relationship", "com.graphaware.runtime.policy.all.IncludeAllBusinessRelationships")
                .withGAConfig("com.graphaware.module.neo4j.UIDM.stripHyphens", "true")
                .withGAConfig("com.graphaware.module.neo4j.UIDM.immutable", "false")
        );

        //When
        try (Transaction tx = database.beginTx()) {
            Node node = tx.createNode();
            node.addLabel(personLabel);
            node.setProperty("name", "aNode");
            tx.commit();
        }

        //Then
        //Retrieve the node and check that it has a uuid property
        try (Transaction tx = database.beginTx()) {
            for (Node node : tx.findNodes(personLabel).stream().collect(Collectors.toList())) {
                assertTrue(node.hasProperty(UUID));
            }
            tx.commit();
        }

        //Then
        //Remove the node uuid property
        try (Transaction tx = database.beginTx()) {
            for (Node node : tx.findNodes(personLabel).stream().collect(Collectors.toList())) {
                node.removeProperty(UUID);
            }
            tx.commit();
        }

        //Then
        // Check nodes have uuid removed
        try (Transaction tx = database.beginTx()) {
            for (Node node : tx.findNodes(personLabel).stream().collect(Collectors.toList())) {
                assertFalse(node.hasProperty(UUID));
            }
            tx.commit();
        }
    }

    @Test
    public void testUuidsAreAssignedToNodesWithNewIncludedLabel() {
        setUpNeo4j(defaultBuilder());

        // Create a node with a label that is not in the inclusion policies
        try (Transaction tx = database.beginTx()) {
            tx.execute("CREATE (n:ns7Person) SET n.name = 'John'");
            tx.commit();
        }
        checkNoNodesWithLabelHaveUuid("ns7Person");

        // Modify this node and add a label set in Inclusion Policies
        try (Transaction tx = database.beginTx()) {
            tx.execute("MATCH (n:ns7Person) SET n:Person");
            tx.commit();
        }
        checkAllNodesWithLabelHaveUuid("Person");

    }

    @Test
    public void testDefaultGenerator() throws Exception {
        setUpNeo4j(defaultBuilder());

        // Verify the generator instantiated is as expected (the default)
        assertEquals(EaioUuidGenerator.class, getGenerator().getClass());
    }

    @Test
    public void testUuidGeneratorJavaUtilUUID() throws Exception {
        setUpNeo4j(emptyBuilder()
                .withGAConfig("com.graphaware.module.neo4j.UIDM.1", "com.graphaware.module.uuid.UuidBootstrapper")
                .withGAConfig("com.graphaware.module.neo4j.UIDM.uuidProperty", "uuid")
                .withGAConfig("com.graphaware.module.neo4j.UIDM.relationship", "com.graphaware.runtime.policy.all.IncludeAllBusinessRelationships")
                .withGAConfig("com.graphaware.module.neo4j.UIDM.stripHyphens", "true")
                .withGAConfig("com.graphaware.module.neo4j.UIDM.uuidGeneratorClass", "com.graphaware.module.uuid.generator.JavaUtilUUIDGenerator")
        );

        // Verify the generator instantiated is as expected
        assertEquals(JavaUtilUUIDGenerator.class, getGenerator().getClass());


        //When
        try (Transaction tx = database.beginTx()) {
            Node node = tx.createNode();
            node.addLabel(personLabel);
            node.setProperty("name", "aNode");
            tx.commit();
        }

        //Then
        //Retrieve the node and check that it has a uuid property
        try (Transaction tx = database.beginTx()) {
            for (Node node : tx.findNodes(personLabel).stream().collect(Collectors.toList())) {
                assertTrue(node.hasProperty(UUID));
                assertFalse(node.getProperty(UUID).toString().contains("-"));
            }
            tx.commit();
        }
    }

    private UuidGenerator getGenerator() throws Exception {
        return ReflectionUtil.getPrivateField(getRuntime().getModule(UuidModule.class), "uuidGenerator", UuidGenerator.class);
    }

    private GraphAwareRuntime getRuntime() throws Exception {
        DatabaseEventListeners listeners = ReflectionUtil.getPrivateField(databaseManagementService, "databaseEventListeners", DatabaseEventListeners.class);
        return (GraphAwareRuntime) ReflectionUtil.getPrivateField(listeners, "databaseEventListeners", List.class).get(0);
    }

    @Test
    public void testUuidGeneratorInvalidGenerator() {
        setUpNeo4j(emptyBuilder()
                .withGAConfig("com.graphaware.module.neo4j.UIDM.1", "com.graphaware.module.uuid.UuidBootstrapper")
                .withGAConfig("com.graphaware.module.neo4j.UIDM.uuidProperty", SEQUENCE)
                .withGAConfig("com.graphaware.module.neo4j.UIDM.relationship", "com.graphaware.runtime.policy.all.IncludeAllBusinessRelationships")
                .withGAConfig("com.graphaware.module.neo4j.UIDM.stripHyphens", "true")
                .withGAConfig("com.graphaware.module.neo4j.UIDM.uuidGeneratorClass", "com.graphaware.module.uuid.generator.NonExistentGenerator")
        );

        assertThrows(TransactionFailureException.class, () -> {
            database.executeTransactionally("CREATE (p:Person)");
        });
    }

    @Test
    public void testUuidNotAssigned() {
        setUpNeo4j(defaultBuilder());

        //When
        try (Transaction tx = database.beginTx()) {
            Node node = tx.createNode();
            node.addLabel(ignoredLabel);
            node.setProperty("name", "aNode");
            tx.commit();
        }

        //Then
        try (Transaction tx = database.beginTx()) {
            tx.findNodes(ignoredLabel).forEachRemaining(new Consumer<Node>() {
                @Override
                public void accept(Node node) {
                    assertFalse(node.hasProperty(UUID));
                }
            });
            tx.commit();
        }
    }

    @Test
    public void testUuidAssignedToRelationship() throws InterruptedException {
        setUpNeo4j(defaultBuilder());

        //When
        try (Transaction tx = database.beginTx()) {
            Node node = tx.createNode();
            node.addLabel(personLabel);
            node.setProperty("name", "aNode");

            Node another = tx.createNode();
            node.addLabel(personLabel);
            node.setProperty("name", "anotherNode");

            node.createRelationshipTo(another, knowsType);
            tx.commit();
        }

        //Then
        //Retrieve the nodes and relationships and check that it has a uuid property
        try (Transaction tx = database.beginTx()) {
            tx.findNodes(personLabel).forEachRemaining(new Consumer<Node>() {
                @Override
                public void accept(Node node) {
                    assertTrue(node.hasProperty(UUID));
                }
            });
            tx.getAllRelationships().forEach(new Consumer<Relationship>() {
                @Override
                public void accept(Relationship rel) {
                    assertTrue(rel.hasProperty(UUID));
                }
            });

            tx.commit();
        }
    }

    @Test
    public void testUuidNotAssignedToRelationship() {
        setUpNeo4j(defaultBuilder());

        //When
        try (Transaction tx = database.beginTx()) {
            Node node = tx.createNode();
            node.addLabel(ignoredLabel);
            node.setProperty("name", "aNode");


            Node another = tx.createNode();
            another.addLabel(personLabel);
            another.setProperty("name", "anotherNode");

            node.createRelationshipTo(another, ignoredType);
            tx.commit();
        }

        //Then
        try (Transaction tx = database.beginTx()) {
            tx.findNodes(ignoredLabel).forEachRemaining(new Consumer<Node>() {
                @Override
                public void accept(Node node) {
                    assertFalse(node.hasProperty(UUID));
                }
            });

            tx.getAllRelationships().forEach(new Consumer<Relationship>() {
                @Override
                public void accept(Relationship rel) {
                    if (rel.isType(ignoredType)) {
                        assertFalse(rel.hasProperty(UUID));
                    }
                }
            });

            tx.commit();
        }
    }

    @Test
    public void testCorrectChangeOfUuid() {
        setUpNeo4j(emptyBuilder()
                .withGAConfig("com.graphaware.module.neo4j.UIDM.1", "com.graphaware.module.uuid.UuidBootstrapper")
                .withGAConfig("com.graphaware.module.neo4j.UIDM.uuidProperty", "uuid")
                .withGAConfig("com.graphaware.module.neo4j.UIDM.relationship", "com.graphaware.runtime.policy.all.IncludeAllBusinessRelationships")
                .withGAConfig("com.graphaware.module.neo4j.UIDM.stripHyphens", "true")
                .withGAConfig("com.graphaware.module.neo4j.UIDM.immutable", "false")
        );

        long id;
        try (Transaction tx = database.beginTx()) {
            Node node = tx.createNode(Label.label("Person"));
            node.setProperty("name", "Bob");
            id = node.getId();
            tx.commit();
        }

        String uuid;
        try (Transaction tx = database.beginTx()) {
            uuid = tx.getNodeById(id).getProperty("uuid").toString();
            assertFalse(uuid.isEmpty());
        }

        try (Transaction tx = database.beginTx()) {
            tx.getNodeById(id).setProperty("uuid", "new-uuid");
            tx.commit();
        }

        try (Transaction tx = database.beginTx()) {
            assertEquals(id, tx.findNode(Label.label("Person"), "uuid", "new-uuid").getId());
        }
    }


    @Test
    public void longCypherCreateShouldResultInAllNodesAndRelsWithUuid() {
        setUpNeo4j(emptyBuilder()
                .withGAConfig("com.graphaware.module.neo4j.UIDM.1", "com.graphaware.module.uuid.UuidBootstrapper")
                .withGAConfig("com.graphaware.module.neo4j.UIDM.uuidProperty", "uuid")
                .withGAConfig("com.graphaware.module.neo4j.UIDM.uuidIndex", "nodeIndex")
                .withGAConfig("com.graphaware.module.neo4j.UIDM.uuidRelationshipIndex", "relIndex")
                .withGAConfig("com.graphaware.module.neo4j.UIDM.relationship", "com.graphaware.runtime.policy.all.IncludeAllBusinessRelationships")
        );

        String cypher =
                "CREATE (TheMatrix:Movie {title:'The Matrix', released:1999, tagline:'Welcome to the Real World'})\n" +
                        "CREATE (Keanu:Person {name:'Keanu Reeves', born:1964})\n" +
                        "CREATE (Carrie:Person {name:'Carrie-Anne Moss', born:1967})\n" +
                        "CREATE (Laurence:Person {name:'Laurence Fishburne', born:1961})\n" +
                        "CREATE (Hugo:Person {name:'Hugo Weaving', born:1960})\n" +
                        "CREATE (AndyW:Person {name:'Andy Wachowski', born:1967})\n" +
                        "CREATE (LanaW:Person {name:'Lana Wachowski', born:1965})\n" +
                        "CREATE (JoelS:Person {name:'Joel Silver', born:1952})\n" +
                        "CREATE\n" +
                        "  (Keanu)-[:ACTED_IN {roles:['Neo']}]->(TheMatrix),\n" +
                        "  (Carrie)-[:ACTED_IN {roles:['Trinity']}]->(TheMatrix),\n" +
                        "  (Laurence)-[:ACTED_IN {roles:['Morpheus']}]->(TheMatrix),\n" +
                        "  (Hugo)-[:ACTED_IN {roles:['Agent Smith']}]->(TheMatrix),\n" +
                        "  (AndyW)-[:DIRECTED]->(TheMatrix),\n" +
                        "  (LanaW)-[:DIRECTED]->(TheMatrix),\n" +
                        "  (JoelS)-[:PRODUCED]->(TheMatrix)\n" +
                        "  \n" +
                        "CREATE (Emil:Person {name:\"Emil Eifrem\", born:1978})\n" +
                        "CREATE (Emil)-[:ACTED_IN {roles:[\"Emil\"]}]->(TheMatrix)\n" +
                        "\n" +
                        "CREATE (TheMatrixReloaded:Movie {title:'The Matrix Reloaded', released:2003, tagline:'Free your mind'})\n" +
                        "CREATE\n" +
                        "  (Keanu)-[:ACTED_IN {roles:['Neo']}]->(TheMatrixReloaded),\n" +
                        "  (Carrie)-[:ACTED_IN {roles:['Trinity']}]->(TheMatrixReloaded),\n" +
                        "  (Laurence)-[:ACTED_IN {roles:['Morpheus']}]->(TheMatrixReloaded),\n" +
                        "  (Hugo)-[:ACTED_IN {roles:['Agent Smith']}]->(TheMatrixReloaded),\n" +
                        "  (AndyW)-[:DIRECTED]->(TheMatrixReloaded),\n" +
                        "  (LanaW)-[:DIRECTED]->(TheMatrixReloaded),\n" +
                        "  (JoelS)-[:PRODUCED]->(TheMatrixReloaded)\n" +
                        "  \n" +
                        "CREATE (TheMatrixRevolutions:Movie {title:'The Matrix Revolutions', released:2003, tagline:'Everything that has a beginning has an end'})\n" +
                        "CREATE\n" +
                        "  (Keanu)-[:ACTED_IN {roles:['Neo']}]->(TheMatrixRevolutions),\n" +
                        "  (Carrie)-[:ACTED_IN {roles:['Trinity']}]->(TheMatrixRevolutions),\n" +
                        "  (Laurence)-[:ACTED_IN {roles:['Morpheus']}]->(TheMatrixRevolutions),\n" +
                        "  (Hugo)-[:ACTED_IN {roles:['Agent Smith']}]->(TheMatrixRevolutions),\n" +
                        "  (AndyW)-[:DIRECTED]->(TheMatrixRevolutions),\n" +
                        "  (LanaW)-[:DIRECTED]->(TheMatrixRevolutions),\n" +
                        "  (JoelS)-[:PRODUCED]->(TheMatrixRevolutions)\n" +
                        "  \n" +
                        "CREATE (TheDevilsAdvocate:Movie {title:\"The Devil's Advocate\", released:1997, tagline:'Evil has its winning ways'})\n" +
                        "CREATE (Charlize:Person {name:'Charlize Theron', born:1975})\n" +
                        "CREATE (Al:Person {name:'Al Pacino', born:1940})\n" +
                        "CREATE (Taylor:Person {name:'Taylor Hackford', born:1944})\n" +
                        "CREATE\n" +
                        "  (Keanu)-[:ACTED_IN {roles:['Kevin Lomax']}]->(TheDevilsAdvocate),\n" +
                        "  (Charlize)-[:ACTED_IN {roles:['Mary Ann Lomax']}]->(TheDevilsAdvocate),\n" +
                        "  (Al)-[:ACTED_IN {roles:['John Milton']}]->(TheDevilsAdvocate),\n" +
                        "  (Taylor)-[:DIRECTED]->(TheDevilsAdvocate)\n" +
                        "  \n" +
                        "CREATE (AFewGoodMen:Movie {title:\"A Few Good Men\", released:1992, tagline:\"In the heart of the nation's capital, in a courthouse of the U.S. government, one man will stop at nothing to keep his honor, and one will stop at nothing to find the truth.\"})\n" +
                        "CREATE (TomC:Person {name:'Tom Cruise', born:1962})\n" +
                        "CREATE (JackN:Person {name:'Jack Nicholson', born:1937})\n" +
                        "CREATE (DemiM:Person {name:'Demi Moore', born:1962})\n" +
                        "CREATE (KevinB:Person {name:'Kevin Bacon', born:1958})\n" +
                        "CREATE (KieferS:Person {name:'Kiefer Sutherland', born:1966})\n" +
                        "CREATE (NoahW:Person {name:'Noah Wyle', born:1971})\n" +
                        "CREATE (CubaG:Person {name:'Cuba Gooding Jr.', born:1968})\n" +
                        "CREATE (KevinP:Person {name:'Kevin Pollak', born:1957})\n" +
                        "CREATE (JTW:Person {name:'J.T. Walsh', born:1943})\n" +
                        "CREATE (JamesM:Person {name:'James Marshall', born:1967})\n" +
                        "CREATE (ChristopherG:Person {name:'Christopher Guest', born:1948})\n" +
                        "CREATE (RobR:Person {name:'Rob Reiner', born:1947})\n" +
                        "CREATE (AaronS:Person {name:'Aaron Sorkin', born:1961})\n" +
                        "CREATE\n" +
                        "  (TomC)-[:ACTED_IN {roles:['Lt. Daniel Kaffee']}]->(AFewGoodMen),\n" +
                        "  (JackN)-[:ACTED_IN {roles:['Col. Nathan R. Jessup']}]->(AFewGoodMen),\n" +
                        "  (DemiM)-[:ACTED_IN {roles:['Lt. Cdr. JoAnne Galloway']}]->(AFewGoodMen),\n" +
                        "  (KevinB)-[:ACTED_IN {roles:['Capt. Jack Ross']}]->(AFewGoodMen),\n" +
                        "  (KieferS)-[:ACTED_IN {roles:['Lt. Jonathan Kendrick']}]->(AFewGoodMen),\n" +
                        "  (NoahW)-[:ACTED_IN {roles:['Cpl. Jeffrey Barnes']}]->(AFewGoodMen),\n" +
                        "  (CubaG)-[:ACTED_IN {roles:['Cpl. Carl Hammaker']}]->(AFewGoodMen),\n" +
                        "  (KevinP)-[:ACTED_IN {roles:['Lt. Sam Weinberg']}]->(AFewGoodMen),\n" +
                        "  (JTW)-[:ACTED_IN {roles:['Lt. Col. Matthew Andrew Markinson']}]->(AFewGoodMen),\n" +
                        "  (JamesM)-[:ACTED_IN {roles:['Pfc. Louden Downey']}]->(AFewGoodMen),\n" +
                        "  (ChristopherG)-[:ACTED_IN {roles:['Dr. Stone']}]->(AFewGoodMen),\n" +
                        "  (AaronS)-[:ACTED_IN {roles:['Man in Bar']}]->(AFewGoodMen),\n" +
                        "  (RobR)-[:DIRECTED]->(AFewGoodMen),\n" +
                        "  (AaronS)-[:WROTE]->(AFewGoodMen)\n" +
                        "  \n" +
                        "CREATE (TopGun:Movie {title:\"Top Gun\", released:1986, tagline:'I feel the need, the need for speed.'})\n" +
                        "CREATE (KellyM:Person {name:'Kelly McGillis', born:1957})\n" +
                        "CREATE (ValK:Person {name:'Val Kilmer', born:1959})\n" +
                        "CREATE (AnthonyE:Person {name:'Anthony Edwards', born:1962})\n" +
                        "CREATE (TomS:Person {name:'Tom Skerritt', born:1933})\n" +
                        "CREATE (MegR:Person {name:'Meg Ryan', born:1961})\n" +
                        "CREATE (TonyS:Person {name:'Tony Scott', born:1944})\n" +
                        "CREATE (JimC:Person {name:'Jim Cash', born:1941})\n" +
                        "CREATE\n" +
                        "  (TomC)-[:ACTED_IN {roles:['Maverick']}]->(TopGun),\n" +
                        "  (KellyM)-[:ACTED_IN {roles:['Charlie']}]->(TopGun),\n" +
                        "  (ValK)-[:ACTED_IN {roles:['Iceman']}]->(TopGun),\n" +
                        "  (AnthonyE)-[:ACTED_IN {roles:['Goose']}]->(TopGun),\n" +
                        "  (TomS)-[:ACTED_IN {roles:['Viper']}]->(TopGun),\n" +
                        "  (MegR)-[:ACTED_IN {roles:['Carole']}]->(TopGun),\n" +
                        "  (TonyS)-[:DIRECTED]->(TopGun),\n" +
                        "  (JimC)-[:WROTE]->(TopGun)\n" +
                        "  \n" +
                        "CREATE (JerryMaguire:Movie {title:'Jerry Maguire', released:2000, tagline:'The rest of his life begins now.'})\n" +
                        "CREATE (ReneeZ:Person {name:'Renee Zellweger', born:1969})\n" +
                        "CREATE (KellyP:Person {name:'Kelly Preston', born:1962})\n" +
                        "CREATE (JerryO:Person {name:\"Jerry O'Connell\", born:1974})\n" +
                        "CREATE (JayM:Person {name:'Jay Mohr', born:1970})\n" +
                        "CREATE (BonnieH:Person {name:'Bonnie Hunt', born:1961})\n" +
                        "CREATE (ReginaK:Person {name:'Regina King', born:1971})\n" +
                        "CREATE (JonathanL:Person {name:'Jonathan Lipnicki', born:1990})\n" +
                        "CREATE (CameronC:Person {name:'Cameron Crowe', born:1957})\n" +
                        "CREATE\n" +
                        "  (TomC)-[:ACTED_IN {roles:['Jerry Maguire']}]->(JerryMaguire),\n" +
                        "  (CubaG)-[:ACTED_IN {roles:['Rod Tidwell']}]->(JerryMaguire),\n" +
                        "  (ReneeZ)-[:ACTED_IN {roles:['Dorothy Boyd']}]->(JerryMaguire),\n" +
                        "  (KellyP)-[:ACTED_IN {roles:['Avery Bishop']}]->(JerryMaguire),\n" +
                        "  (JerryO)-[:ACTED_IN {roles:['Frank Cushman']}]->(JerryMaguire),\n" +
                        "  (JayM)-[:ACTED_IN {roles:['Bob Sugar']}]->(JerryMaguire),\n" +
                        "  (BonnieH)-[:ACTED_IN {roles:['Laurel Boyd']}]->(JerryMaguire),\n" +
                        "  (ReginaK)-[:ACTED_IN {roles:['Marcee Tidwell']}]->(JerryMaguire),\n" +
                        "  (JonathanL)-[:ACTED_IN {roles:['Ray Boyd']}]->(JerryMaguire),\n" +
                        "  (CameronC)-[:DIRECTED]->(JerryMaguire),\n" +
                        "  (CameronC)-[:PRODUCED]->(JerryMaguire),\n" +
                        "  (CameronC)-[:WROTE]->(JerryMaguire)\n" +
                        "  \n" +
                        "CREATE (StandByMe:Movie {title:\"Stand By Me\", released:1995, tagline:\"For some, it's the last real taste of innocence, and the first real taste of life. But for everyone, it's the time that memories are made of.\"})\n" +
                        "CREATE (RiverP:Person {name:'River Phoenix', born:1970})\n" +
                        "CREATE (CoreyF:Person {name:'Corey Feldman', born:1971})\n" +
                        "CREATE (WilW:Person {name:'Wil Wheaton', born:1972})\n" +
                        "CREATE (JohnC:Person {name:'John Cusack', born:1966})\n" +
                        "CREATE (MarshallB:Person {name:'Marshall Bell', born:1942})\n" +
                        "CREATE\n" +
                        "  (WilW)-[:ACTED_IN {roles:['Gordie Lachance']}]->(StandByMe),\n" +
                        "  (RiverP)-[:ACTED_IN {roles:['Chris Chambers']}]->(StandByMe),\n" +
                        "  (JerryO)-[:ACTED_IN {roles:['Vern Tessio']}]->(StandByMe),\n" +
                        "  (CoreyF)-[:ACTED_IN {roles:['Teddy Duchamp']}]->(StandByMe),\n" +
                        "  (JohnC)-[:ACTED_IN {roles:['Denny Lachance']}]->(StandByMe),\n" +
                        "  (KieferS)-[:ACTED_IN {roles:['Ace Merrill']}]->(StandByMe),\n" +
                        "  (MarshallB)-[:ACTED_IN {roles:['Mr. Lachance']}]->(StandByMe),\n" +
                        "  (RobR)-[:DIRECTED]->(StandByMe)\n" +
                        "  \n" +
                        "CREATE (AsGoodAsItGets:Movie {title:'As Good as It Gets', released:1997, tagline:'A comedy from the heart that goes for the throat.'})\n" +
                        "CREATE (HelenH:Person {name:'Helen Hunt', born:1963})\n" +
                        "CREATE (GregK:Person {name:'Greg Kinnear', born:1963})\n" +
                        "CREATE (JamesB:Person {name:'James L. Brooks', born:1940})\n" +
                        "CREATE\n" +
                        "  (JackN)-[:ACTED_IN {roles:['Melvin Udall']}]->(AsGoodAsItGets),\n" +
                        "  (HelenH)-[:ACTED_IN {roles:['Carol Connelly']}]->(AsGoodAsItGets),\n" +
                        "  (GregK)-[:ACTED_IN {roles:['Simon Bishop']}]->(AsGoodAsItGets),\n" +
                        "  (CubaG)-[:ACTED_IN {roles:['Frank Sachs']}]->(AsGoodAsItGets),\n" +
                        "  (JamesB)-[:DIRECTED]->(AsGoodAsItGets)\n" +
                        "  \n" +
                        "CREATE (WhatDreamsMayCome:Movie {title:'What Dreams May Come', released:1998, tagline:'After life there is more. The end is just the beginning.'})\n" +
                        "CREATE (AnnabellaS:Person {name:'Annabella Sciorra', born:1960})\n" +
                        "CREATE (MaxS:Person {name:'Max von Sydow', born:1929})\n" +
                        "CREATE (WernerH:Person {name:'Werner Herzog', born:1942})\n" +
                        "CREATE (Robin:Person {name:'Robin Williams', born:1951})\n" +
                        "CREATE (VincentW:Person {name:'Vincent Ward', born:1956})\n" +
                        "CREATE\n" +
                        "  (Robin)-[:ACTED_IN {roles:['Chris Nielsen']}]->(WhatDreamsMayCome),\n" +
                        "  (CubaG)-[:ACTED_IN {roles:['Albert Lewis']}]->(WhatDreamsMayCome),\n" +
                        "  (AnnabellaS)-[:ACTED_IN {roles:['Simon Bishop']}]->(WhatDreamsMayCome),\n" +
                        "  (MaxS)-[:ACTED_IN {roles:['The Tracker']}]->(WhatDreamsMayCome),\n" +
                        "  (WernerH)-[:ACTED_IN {roles:['The Face']}]->(WhatDreamsMayCome),\n" +
                        "  (VincentW)-[:DIRECTED]->(WhatDreamsMayCome)\n" +
                        "  \n" +
                        "CREATE (SnowFallingonCedars:Movie {title:'Snow Falling on Cedars', released:1999, tagline:'First loves last. Forever.'})\n" +
                        "CREATE (EthanH:Person {name:'Ethan Hawke', born:1970})\n" +
                        "CREATE (RickY:Person {name:'Rick Yune', born:1971})\n" +
                        "CREATE (JamesC:Person {name:'James Cromwell', born:1940})\n" +
                        "CREATE (ScottH:Person {name:'Scott Hicks', born:1953})\n" +
                        "CREATE\n" +
                        "  (EthanH)-[:ACTED_IN {roles:['Ishmael Chambers']}]->(SnowFallingonCedars),\n" +
                        "  (RickY)-[:ACTED_IN {roles:['Kazuo Miyamoto']}]->(SnowFallingonCedars),\n" +
                        "  (MaxS)-[:ACTED_IN {roles:['Nels Gudmundsson']}]->(SnowFallingonCedars),\n" +
                        "  (JamesC)-[:ACTED_IN {roles:['Judge Fielding']}]->(SnowFallingonCedars),\n" +
                        "  (ScottH)-[:DIRECTED]->(SnowFallingonCedars)\n" +
                        "  \n" +
                        "CREATE (YouveGotMail:Movie {title:\"You've Got Mail\", released:1998, tagline:'At odds in life... in love on-line.'})\n" +
                        "CREATE (ParkerP:Person {name:'Parker Posey', born:1968})\n" +
                        "CREATE (DaveC:Person {name:'Dave Chappelle', born:1973})\n" +
                        "CREATE (SteveZ:Person {name:'Steve Zahn', born:1967})\n" +
                        "CREATE (TomH:Person {name:'Tom Hanks', born:1956})\n" +
                        "CREATE (NoraE:Person {name:'Nora Ephron', born:1941})\n" +
                        "CREATE\n" +
                        "  (TomH)-[:ACTED_IN {roles:['Joe Fox']}]->(YouveGotMail),\n" +
                        "  (MegR)-[:ACTED_IN {roles:['Kathleen Kelly']}]->(YouveGotMail),\n" +
                        "  (GregK)-[:ACTED_IN {roles:['Frank Navasky']}]->(YouveGotMail),\n" +
                        "  (ParkerP)-[:ACTED_IN {roles:['Patricia Eden']}]->(YouveGotMail),\n" +
                        "  (DaveC)-[:ACTED_IN {roles:['Kevin Jackson']}]->(YouveGotMail),\n" +
                        "  (SteveZ)-[:ACTED_IN {roles:['George Pappas']}]->(YouveGotMail),\n" +
                        "  (NoraE)-[:DIRECTED]->(YouveGotMail)\n" +
                        "  \n" +
                        "CREATE (SleeplessInSeattle:Movie {title:'Sleepless in Seattle', released:1993, tagline:'What if someone you never met, someone you never saw, someone you never knew was the only someone for you?'})\n" +
                        "CREATE (RitaW:Person {name:'Rita Wilson', born:1956})\n" +
                        "CREATE (BillPull:Person {name:'Bill Pullman', born:1953})\n" +
                        "CREATE (VictorG:Person {name:'Victor Garber', born:1949})\n" +
                        "CREATE (RosieO:Person {name:\"Rosie O'Donnell\", born:1962})\n" +
                        "CREATE\n" +
                        "  (TomH)-[:ACTED_IN {roles:['Sam Baldwin']}]->(SleeplessInSeattle),\n" +
                        "  (MegR)-[:ACTED_IN {roles:['Annie Reed']}]->(SleeplessInSeattle),\n" +
                        "  (RitaW)-[:ACTED_IN {roles:['Suzy']}]->(SleeplessInSeattle),\n" +
                        "  (BillPull)-[:ACTED_IN {roles:['Walter']}]->(SleeplessInSeattle),\n" +
                        "  (VictorG)-[:ACTED_IN {roles:['Greg']}]->(SleeplessInSeattle),\n" +
                        "  (RosieO)-[:ACTED_IN {roles:['Becky']}]->(SleeplessInSeattle),\n" +
                        "  (NoraE)-[:DIRECTED]->(SleeplessInSeattle)\n" +
                        "  \n" +
                        "CREATE (JoeVersustheVolcano:Movie {title:'Joe Versus the Volcano', released:1990, tagline:'A story of love, lava and burning desire.'})\n" +
                        "CREATE (JohnS:Person {name:'John Patrick Stanley', born:1950})\n" +
                        "CREATE (Nathan:Person {name:'Nathan Lane', born:1956})\n" +
                        "CREATE\n" +
                        "  (TomH)-[:ACTED_IN {roles:['Joe Banks']}]->(JoeVersustheVolcano),\n" +
                        "  (MegR)-[:ACTED_IN {roles:['DeDe', 'Angelica Graynamore', 'Patricia Graynamore']}]->(JoeVersustheVolcano),\n" +
                        "  (Nathan)-[:ACTED_IN {roles:['Baw']}]->(JoeVersustheVolcano),\n" +
                        "  (JohnS)-[:DIRECTED]->(JoeVersustheVolcano)\n" +
                        "  \n" +
                        "CREATE (WhenHarryMetSally:Movie {title:'When Harry Met Sally', released:1998, tagline:'At odds in life... in love on-line.'})\n" +
                        "CREATE (BillyC:Person {name:'Billy Crystal', born:1948})\n" +
                        "CREATE (CarrieF:Person {name:'Carrie Fisher', born:1956})\n" +
                        "CREATE (BrunoK:Person {name:'Bruno Kirby', born:1949})\n" +
                        "CREATE\n" +
                        "  (BillyC)-[:ACTED_IN {roles:['Harry Burns']}]->(WhenHarryMetSally),\n" +
                        "  (MegR)-[:ACTED_IN {roles:['Sally Albright']}]->(WhenHarryMetSally),\n" +
                        "  (CarrieF)-[:ACTED_IN {roles:['Marie']}]->(WhenHarryMetSally),\n" +
                        "  (BrunoK)-[:ACTED_IN {roles:['Jess']}]->(WhenHarryMetSally),\n" +
                        "  (RobR)-[:DIRECTED]->(WhenHarryMetSally),\n" +
                        "  (RobR)-[:PRODUCED]->(WhenHarryMetSally),\n" +
                        "  (NoraE)-[:PRODUCED]->(WhenHarryMetSally),\n" +
                        "  (NoraE)-[:WROTE]->(WhenHarryMetSally)\n" +
                        "  \n" +
                        "CREATE (ThatThingYouDo:Movie {title:'That Thing You Do', released:1996, tagline:'In every life there comes a time when that thing you dream becomes that thing you do'})\n" +
                        "CREATE (LivT:Person {name:'Liv Tyler', born:1977})\n" +
                        "CREATE\n" +
                        "  (TomH)-[:ACTED_IN {roles:['Mr. White']}]->(ThatThingYouDo),\n" +
                        "  (LivT)-[:ACTED_IN {roles:['Faye Dolan']}]->(ThatThingYouDo),\n" +
                        "  (Charlize)-[:ACTED_IN {roles:['Tina']}]->(ThatThingYouDo),\n" +
                        "  (TomH)-[:DIRECTED]->(ThatThingYouDo)\n" +
                        "  \n" +
                        "CREATE (TheReplacements:Movie {title:'The Replacements', released:2000, tagline:'Pain heals, Chicks dig scars... Glory lasts forever'})\n" +
                        "CREATE (Brooke:Person {name:'Brooke Langton', born:1970})\n" +
                        "CREATE (Gene:Person {name:'Gene Hackman', born:1930})\n" +
                        "CREATE (Orlando:Person {name:'Orlando Jones', born:1968})\n" +
                        "CREATE (Howard:Person {name:'Howard Deutch', born:1950})\n" +
                        "CREATE\n" +
                        "  (Keanu)-[:ACTED_IN {roles:['Shane Falco']}]->(TheReplacements),\n" +
                        "  (Brooke)-[:ACTED_IN {roles:['Annabelle Farrell']}]->(TheReplacements),\n" +
                        "  (Gene)-[:ACTED_IN {roles:['Jimmy McGinty']}]->(TheReplacements),\n" +
                        "  (Orlando)-[:ACTED_IN {roles:['Clifford Franklin']}]->(TheReplacements),\n" +
                        "  (Howard)-[:DIRECTED]->(TheReplacements)\n" +
                        "  \n" +
                        "CREATE (RescueDawn:Movie {title:'RescueDawn', released:2006, tagline:\"Based on the extraordinary true story of one man's fight for freedom\"})\n" +
                        "CREATE (ChristianB:Person {name:'Christian Bale', born:1974})\n" +
                        "CREATE (ZachG:Person {name:'Zach Grenier', born:1954})\n" +
                        "CREATE\n" +
                        "  (MarshallB)-[:ACTED_IN {roles:['Admiral']}]->(RescueDawn),\n" +
                        "  (ChristianB)-[:ACTED_IN {roles:['Dieter Dengler']}]->(RescueDawn),\n" +
                        "  (ZachG)-[:ACTED_IN {roles:['Squad Leader']}]->(RescueDawn),\n" +
                        "  (SteveZ)-[:ACTED_IN {roles:['Duane']}]->(RescueDawn),\n" +
                        "  (WernerH)-[:DIRECTED]->(RescueDawn)\n" +
                        "  \n" +
                        "CREATE (TheBirdcage:Movie {title:'The Birdcage', released:1996, tagline:'Come as you are'})\n" +
                        "CREATE (MikeN:Person {name:'Mike Nichols', born:1931})\n" +
                        "CREATE\n" +
                        "  (Robin)-[:ACTED_IN {roles:['Armand Goldman']}]->(TheBirdcage),\n" +
                        "  (Nathan)-[:ACTED_IN {roles:['Albert Goldman']}]->(TheBirdcage),\n" +
                        "  (Gene)-[:ACTED_IN {roles:['Sen. Kevin Keeley']}]->(TheBirdcage),\n" +
                        "  (MikeN)-[:DIRECTED]->(TheBirdcage)\n" +
                        "  \n" +
                        "CREATE (Unforgiven:Movie {title:'Unforgiven', released:1992, tagline:\"It's a hell of a thing, killing a man\"})\n" +
                        "CREATE (RichardH:Person {name:'Richard Harris', born:1930})\n" +
                        "CREATE (ClintE:Person {name:'Clint Eastwood', born:1930})\n" +
                        "CREATE\n" +
                        "  (RichardH)-[:ACTED_IN {roles:['English Bob']}]->(Unforgiven),\n" +
                        "  (ClintE)-[:ACTED_IN {roles:['Bill Munny']}]->(Unforgiven),\n" +
                        "  (Gene)-[:ACTED_IN {roles:['Little Bill Daggett']}]->(Unforgiven),\n" +
                        "  (ClintE)-[:DIRECTED]->(Unforgiven)\n" +
                        "  \n" +
                        "CREATE (JohnnyMnemonic:Movie {title:'Johnny Mnemonic', released:1995, tagline:'The hottest data on earth. In the coolest head in town'})\n" +
                        "CREATE (Takeshi:Person {name:'Takeshi Kitano', born:1947})\n" +
                        "CREATE (Dina:Person {name:'Dina Meyer', born:1968})\n" +
                        "CREATE (IceT:Person {name:'Ice-T', born:1958})\n" +
                        "CREATE (RobertL:Person {name:'Robert Longo', born:1953})\n" +
                        "CREATE\n" +
                        "  (Keanu)-[:ACTED_IN {roles:['Johnny Mnemonic']}]->(JohnnyMnemonic),\n" +
                        "  (Takeshi)-[:ACTED_IN {roles:['Takahashi']}]->(JohnnyMnemonic),\n" +
                        "  (Dina)-[:ACTED_IN {roles:['Jane']}]->(JohnnyMnemonic),\n" +
                        "  (IceT)-[:ACTED_IN {roles:['J-Bone']}]->(JohnnyMnemonic),\n" +
                        "  (RobertL)-[:DIRECTED]->(JohnnyMnemonic)\n" +
                        "  \n" +
                        "CREATE (CloudAtlas:Movie {title:'Cloud Atlas', released:2012, tagline:'Everything is connected'})\n" +
                        "CREATE (HalleB:Person {name:'Halle Berry', born:1966})\n" +
                        "CREATE (JimB:Person {name:'Jim Broadbent', born:1949})\n" +
                        "CREATE (TomT:Person {name:'Tom Tykwer', born:1965})\n" +
                        "CREATE\n" +
                        "  (TomH)-[:ACTED_IN {roles:['Zachry', 'Dr. Henry Goose', 'Isaac Sachs', 'Dermot Hoggins']}]->(CloudAtlas),\n" +
                        "  (Hugo)-[:ACTED_IN {roles:['Bill Smoke', 'Haskell Moore', 'Tadeusz Kesselring', 'Nurse Noakes', 'Boardman Mephi', 'Old Georgie']}]->(CloudAtlas),\n" +
                        "  (HalleB)-[:ACTED_IN {roles:['Luisa Rey', 'Jocasta Ayrs', 'Ovid', 'Meronym']}]->(CloudAtlas),\n" +
                        "  (JimB)-[:ACTED_IN {roles:['Vyvyan Ayrs', 'Captain Molyneux', 'Timothy Cavendish']}]->(CloudAtlas),\n" +
                        "  (TomT)-[:DIRECTED]->(CloudAtlas),\n" +
                        "  (AndyW)-[:DIRECTED]->(CloudAtlas),\n" +
                        "  (LanaW)-[:DIRECTED]->(CloudAtlas)\n" +
                        "  \n" +
                        "CREATE (TheDaVinciCode:Movie {title:'The Da Vinci Code', released:2006, tagline:'Break The Codes'})\n" +
                        "CREATE (IanM:Person {name:'Ian McKellen', born:1939})\n" +
                        "CREATE (AudreyT:Person {name:'Audrey Tautou', born:1976})\n" +
                        "CREATE (PaulB:Person {name:'Paul Bettany', born:1971})\n" +
                        "CREATE (RonH:Person {name:'Ron Howard', born:1954})\n" +
                        "CREATE\n" +
                        "  (TomH)-[:ACTED_IN {roles:['Dr. Robert Langdon']}]->(TheDaVinciCode),\n" +
                        "  (IanM)-[:ACTED_IN {roles:['Sir Leight Teabing']}]->(TheDaVinciCode),\n" +
                        "  (AudreyT)-[:ACTED_IN {roles:['Sophie Neveu']}]->(TheDaVinciCode),\n" +
                        "  (PaulB)-[:ACTED_IN {roles:['Silas']}]->(TheDaVinciCode),\n" +
                        "  (RonH)-[:DIRECTED]->(TheDaVinciCode)\n" +
                        "  \n" +
                        "CREATE (VforVendetta:Movie {title:'V for Vendetta', released:2006, tagline:'Freedom! Forever!'})\n" +
                        "CREATE (NatalieP:Person {name:'Natalie Portman', born:1981})\n" +
                        "CREATE (StephenR:Person {name:'Stephen Rea', born:1946})\n" +
                        "CREATE (JohnH:Person {name:'John Hurt', born:1940})\n" +
                        "CREATE (BenM:Person {name: 'Ben Miles', born:1967})\n" +
                        "CREATE\n" +
                        "  (Hugo)-[:ACTED_IN {roles:['V']}]->(VforVendetta),\n" +
                        "  (NatalieP)-[:ACTED_IN {roles:['Evey Hammond']}]->(VforVendetta),\n" +
                        "  (StephenR)-[:ACTED_IN {roles:['Eric Finch']}]->(VforVendetta),\n" +
                        "  (JohnH)-[:ACTED_IN {roles:['High Chancellor Adam Sutler']}]->(VforVendetta),\n" +
                        "  (BenM)-[:ACTED_IN {roles:['Dascomb']}]->(VforVendetta),\n" +
                        "  (JamesM)-[:DIRECTED]->(VforVendetta),\n" +
                        "  (AndyW)-[:PRODUCED]->(VforVendetta),\n" +
                        "  (LanaW)-[:PRODUCED]->(VforVendetta),\n" +
                        "  (JoelS)-[:PRODUCED]->(VforVendetta),\n" +
                        "  (AndyW)-[:WROTE]->(VforVendetta),\n" +
                        "  (LanaW)-[:WROTE]->(VforVendetta)\n" +
                        "  \n" +
                        "CREATE (SpeedRacer:Movie {title:'Speed Racer', released:2008, tagline:'Speed has no limits'})\n" +
                        "CREATE (EmileH:Person {name:'Emile Hirsch', born:1985})\n" +
                        "CREATE (JohnG:Person {name:'John Goodman', born:1960})\n" +
                        "CREATE (SusanS:Person {name:'Susan Sarandon', born:1946})\n" +
                        "CREATE (MatthewF:Person {name:'Matthew Fox', born:1966})\n" +
                        "CREATE (ChristinaR:Person {name:'Christina Ricci', born:1980})\n" +
                        "CREATE (Rain:Person {name:'Rain', born:1982})\n" +
                        "CREATE\n" +
                        "  (EmileH)-[:ACTED_IN {roles:['Speed Racer']}]->(SpeedRacer),\n" +
                        "  (JohnG)-[:ACTED_IN {roles:['Pops']}]->(SpeedRacer),\n" +
                        "  (SusanS)-[:ACTED_IN {roles:['Mom']}]->(SpeedRacer),\n" +
                        "  (MatthewF)-[:ACTED_IN {roles:['Racer X']}]->(SpeedRacer),\n" +
                        "  (ChristinaR)-[:ACTED_IN {roles:['Trixie']}]->(SpeedRacer),\n" +
                        "  (Rain)-[:ACTED_IN {roles:['Taejo Togokahn']}]->(SpeedRacer),\n" +
                        "  (BenM)-[:ACTED_IN {roles:['Cass Jones']}]->(SpeedRacer),\n" +
                        "  (AndyW)-[:DIRECTED]->(SpeedRacer),\n" +
                        "  (LanaW)-[:DIRECTED]->(SpeedRacer),\n" +
                        "  (AndyW)-[:WROTE]->(SpeedRacer),\n" +
                        "  (LanaW)-[:WROTE]->(SpeedRacer),\n" +
                        "  (JoelS)-[:PRODUCED]->(SpeedRacer)\n" +
                        "  \n" +
                        "CREATE (NinjaAssassin:Movie {title:'Ninja Assassin', released:2009, tagline:'Prepare to enter a secret world of assassins'})\n" +
                        "CREATE (NaomieH:Person {name:'Naomie Harris'})\n" +
                        "CREATE\n" +
                        "  (Rain)-[:ACTED_IN {roles:['Raizo']}]->(NinjaAssassin),\n" +
                        "  (NaomieH)-[:ACTED_IN {roles:['Mika Coretti']}]->(NinjaAssassin),\n" +
                        "  (RickY)-[:ACTED_IN {roles:['Takeshi']}]->(NinjaAssassin),\n" +
                        "  (BenM)-[:ACTED_IN {roles:['Ryan Maslow']}]->(NinjaAssassin),\n" +
                        "  (JamesM)-[:DIRECTED]->(NinjaAssassin),\n" +
                        "  (AndyW)-[:PRODUCED]->(NinjaAssassin),\n" +
                        "  (LanaW)-[:PRODUCED]->(NinjaAssassin),\n" +
                        "  (JoelS)-[:PRODUCED]->(NinjaAssassin)\n" +
                        "  \n" +
                        "CREATE (TheGreenMile:Movie {title:'The Green Mile', released:1999, tagline:\"Walk a mile you'll never forget.\"})\n" +
                        "CREATE (MichaelD:Person {name:'Michael Clarke Duncan', born:1957})\n" +
                        "CREATE (DavidM:Person {name:'David Morse', born:1953})\n" +
                        "CREATE (SamR:Person {name:'Sam Rockwell', born:1968})\n" +
                        "CREATE (GaryS:Person {name:'Gary Sinise', born:1955})\n" +
                        "CREATE (PatriciaC:Person {name:'Patricia Clarkson', born:1959})\n" +
                        "CREATE (FrankD:Person {name:'Frank Darabont', born:1959})\n" +
                        "CREATE\n" +
                        "  (TomH)-[:ACTED_IN {roles:['Paul Edgecomb']}]->(TheGreenMile),\n" +
                        "  (MichaelD)-[:ACTED_IN {roles:['John Coffey']}]->(TheGreenMile),\n" +
                        "  (DavidM)-[:ACTED_IN {roles:['Brutus \"Brutal\" Howell']}]->(TheGreenMile),\n" +
                        "  (BonnieH)-[:ACTED_IN {roles:['Jan Edgecomb']}]->(TheGreenMile),\n" +
                        "  (JamesC)-[:ACTED_IN {roles:['Warden Hal Moores']}]->(TheGreenMile),\n" +
                        "  (SamR)-[:ACTED_IN {roles:['\"Wild Bill\" Wharton']}]->(TheGreenMile),\n" +
                        "  (GaryS)-[:ACTED_IN {roles:['Burt Hammersmith']}]->(TheGreenMile),\n" +
                        "  (PatriciaC)-[:ACTED_IN {roles:['Melinda Moores']}]->(TheGreenMile),\n" +
                        "  (FrankD)-[:DIRECTED]->(TheGreenMile)\n" +
                        "  \n" +
                        "CREATE (FrostNixon:Movie {title:'Frost/Nixon', released:2008, tagline:'400 million people were waiting for the truth.'})\n" +
                        "CREATE (FrankL:Person {name:'Frank Langella', born:1938})\n" +
                        "CREATE (MichaelS:Person {name:'Michael Sheen', born:1969})\n" +
                        "CREATE (OliverP:Person {name:'Oliver Platt', born:1960})\n" +
                        "CREATE\n" +
                        "  (FrankL)-[:ACTED_IN {roles:['Richard Nixon']}]->(FrostNixon),\n" +
                        "  (MichaelS)-[:ACTED_IN {roles:['David Frost']}]->(FrostNixon),\n" +
                        "  (KevinB)-[:ACTED_IN {roles:['Jack Brennan']}]->(FrostNixon),\n" +
                        "  (OliverP)-[:ACTED_IN {roles:['Bob Zelnick']}]->(FrostNixon),\n" +
                        "  (SamR)-[:ACTED_IN {roles:['James Reston, Jr.']}]->(FrostNixon),\n" +
                        "  (RonH)-[:DIRECTED]->(FrostNixon)\n" +
                        "  \n" +
                        "CREATE (Hoffa:Movie {title:'Hoffa', released:1992, tagline:\"He didn't want law. He wanted justice.\"})\n" +
                        "CREATE (DannyD:Person {name:'Danny DeVito', born:1944})\n" +
                        "CREATE (JohnR:Person {name:'John C. Reilly', born:1965})\n" +
                        "CREATE\n" +
                        "  (JackN)-[:ACTED_IN {roles:['Hoffa']}]->(Hoffa),\n" +
                        "  (DannyD)-[:ACTED_IN {roles:['Robert \"Bobby\" Ciaro']}]->(Hoffa),\n" +
                        "  (JTW)-[:ACTED_IN {roles:['Frank Fitzsimmons']}]->(Hoffa),\n" +
                        "  (JohnR)-[:ACTED_IN {roles:['Peter \"Pete\" Connelly']}]->(Hoffa),\n" +
                        "  (DannyD)-[:DIRECTED]->(Hoffa)\n" +
                        "  \n" +
                        "CREATE (Apollo13:Movie {title:'Apollo 13', released:1995, tagline:'Houston, we have a problem.'})\n" +
                        "CREATE (EdH:Person {name:'Ed Harris', born:1950})\n" +
                        "CREATE (BillPax:Person {name:'Bill Paxton', born:1955})\n" +
                        "CREATE\n" +
                        "  (TomH)-[:ACTED_IN {roles:['Jim Lovell']}]->(Apollo13),\n" +
                        "  (KevinB)-[:ACTED_IN {roles:['Jack Swigert']}]->(Apollo13),\n" +
                        "  (EdH)-[:ACTED_IN {roles:['Gene Kranz']}]->(Apollo13),\n" +
                        "  (BillPax)-[:ACTED_IN {roles:['Fred Haise']}]->(Apollo13),\n" +
                        "  (GaryS)-[:ACTED_IN {roles:['Ken Mattingly']}]->(Apollo13),\n" +
                        "  (RonH)-[:DIRECTED]->(Apollo13)\n" +
                        "  \n" +
                        "CREATE (Twister:Movie {title:'Twister', released:1996, tagline:\"Don't Breathe. Don't Look Back.\"})\n" +
                        "CREATE (PhilipH:Person {name:'Philip Seymour Hoffman', born:1967})\n" +
                        "CREATE (JanB:Person {name:'Jan de Bont', born:1943})\n" +
                        "CREATE\n" +
                        "  (BillPax)-[:ACTED_IN {roles:['Bill Harding']}]->(Twister),\n" +
                        "  (HelenH)-[:ACTED_IN {roles:['Dr. Jo Harding']}]->(Twister),\n" +
                        "  (ZachG)-[:ACTED_IN {roles:['Eddie']}]->(Twister),\n" +
                        "  (PhilipH)-[:ACTED_IN {roles:['Dustin \"Dusty\" Davis']}]->(Twister),\n" +
                        "  (JanB)-[:DIRECTED]->(Twister)\n" +
                        "  \n" +
                        "CREATE (CastAway:Movie {title:'Cast Away', released:2000, tagline:'At the edge of the world, his journey begins.'})\n" +
                        "CREATE (RobertZ:Person {name:'Robert Zemeckis', born:1951})\n" +
                        "CREATE\n" +
                        "  (TomH)-[:ACTED_IN {roles:['Chuck Noland']}]->(CastAway),\n" +
                        "  (HelenH)-[:ACTED_IN {roles:['Kelly Frears']}]->(CastAway),\n" +
                        "  (RobertZ)-[:DIRECTED]->(CastAway)\n" +
                        "  \n" +
                        "CREATE (OneFlewOvertheCuckoosNest:Movie {title:\"One Flew Over the Cuckoo's Nest\", released:1975, tagline:\"If he's crazy, what does that make you?\"})\n" +
                        "CREATE (MilosF:Person {name:'Milos Forman', born:1932})\n" +
                        "CREATE\n" +
                        "  (JackN)-[:ACTED_IN {roles:['Randle McMurphy']}]->(OneFlewOvertheCuckoosNest),\n" +
                        "  (DannyD)-[:ACTED_IN {roles:['Martini']}]->(OneFlewOvertheCuckoosNest),\n" +
                        "  (MilosF)-[:DIRECTED]->(OneFlewOvertheCuckoosNest)\n" +
                        "  \n" +
                        "CREATE (SomethingsGottaGive:Movie {title:\"Something's Gotta Give\", released:1975})\n" +
                        "CREATE (DianeK:Person {name:'Diane Keaton', born:1946})\n" +
                        "CREATE (NancyM:Person {name:'Nancy Meyers', born:1949})\n" +
                        "CREATE\n" +
                        "  (JackN)-[:ACTED_IN {roles:['Harry Sanborn']}]->(SomethingsGottaGive),\n" +
                        "  (DianeK)-[:ACTED_IN {roles:['Erica Barry']}]->(SomethingsGottaGive),\n" +
                        "  (Keanu)-[:ACTED_IN {roles:['Julian Mercer']}]->(SomethingsGottaGive),\n" +
                        "  (NancyM)-[:DIRECTED]->(SomethingsGottaGive),\n" +
                        "  (NancyM)-[:PRODUCED]->(SomethingsGottaGive),\n" +
                        "  (NancyM)-[:WROTE]->(SomethingsGottaGive)\n" +
                        "  \n" +
                        "CREATE (BicentennialMan:Movie {title:'Bicentennial Man', released:1999, tagline:\"One robot's 200 year journey to become an ordinary man.\"})\n" +
                        "CREATE (ChrisC:Person {name:'Chris Columbus', born:1958})\n" +
                        "CREATE\n" +
                        "  (Robin)-[:ACTED_IN {roles:['Andrew Marin']}]->(BicentennialMan),\n" +
                        "  (OliverP)-[:ACTED_IN {roles:['Rupert Burns']}]->(BicentennialMan),\n" +
                        "  (ChrisC)-[:DIRECTED]->(BicentennialMan)\n" +
                        "  \n" +
                        "CREATE (CharlieWilsonsWar:Movie {title:\"Charlie Wilson's War\", released:2007, tagline:\"A stiff drink. A little mascara. A lot of nerve. Who said they couldn't bring down the Soviet empire.\"})\n" +
                        "CREATE (JuliaR:Person {name:'Julia Roberts', born:1967})\n" +
                        "CREATE\n" +
                        "  (TomH)-[:ACTED_IN {roles:['Rep. Charlie Wilson']}]->(CharlieWilsonsWar),\n" +
                        "  (JuliaR)-[:ACTED_IN {roles:['Joanne Herring']}]->(CharlieWilsonsWar),\n" +
                        "  (PhilipH)-[:ACTED_IN {roles:['Gust Avrakotos']}]->(CharlieWilsonsWar),\n" +
                        "  (MikeN)-[:DIRECTED]->(CharlieWilsonsWar)\n" +
                        "  \n" +
                        "CREATE (ThePolarExpress:Movie {title:'The Polar Express', released:2004, tagline:'This Holiday Season… Believe'})\n" +
                        "CREATE\n" +
                        "  (TomH)-[:ACTED_IN {roles:['Hero Boy', 'Father', 'Conductor', 'Hobo', 'Scrooge', 'Santa Claus']}]->(ThePolarExpress),\n" +
                        "  (RobertZ)-[:DIRECTED]->(ThePolarExpress)\n" +
                        "  \n" +
                        "CREATE (ALeagueofTheirOwn:Movie {title:'A League of Their Own', released:1992, tagline:'Once in a lifetime you get a chance to do something different.'})\n" +
                        "CREATE (Madonna:Person {name:'Madonna', born:1954})\n" +
                        "CREATE (GeenaD:Person {name:'Geena Davis', born:1956})\n" +
                        "CREATE (LoriP:Person {name:'Lori Petty', born:1963})\n" +
                        "CREATE (PennyM:Person {name:'Penny Marshall', born:1943})\n" +
                        "CREATE\n" +
                        "  (TomH)-[:ACTED_IN {roles:['Jimmy Dugan']}]->(ALeagueofTheirOwn),\n" +
                        "  (GeenaD)-[:ACTED_IN {roles:['Dottie Hinson']}]->(ALeagueofTheirOwn),\n" +
                        "  (LoriP)-[:ACTED_IN {roles:['Kit Keller']}]->(ALeagueofTheirOwn),\n" +
                        "  (RosieO)-[:ACTED_IN {roles:['Doris Murphy']}]->(ALeagueofTheirOwn),\n" +
                        "  (Madonna)-[:ACTED_IN {roles:['\"All the Way\" Mae Mordabito']}]->(ALeagueofTheirOwn),\n" +
                        "  (BillPax)-[:ACTED_IN {roles:['Bob Hinson']}]->(ALeagueofTheirOwn),\n" +
                        "  (PennyM)-[:DIRECTED]->(ALeagueofTheirOwn)\n" +
                        "  \n" +
                        "CREATE (PaulBlythe:Person {name:'Paul Blythe'})\n" +
                        "CREATE (AngelaScope:Person {name:'Angela Scope'})\n" +
                        "CREATE (JessicaThompson:Person {name:'Jessica Thompson'})\n" +
                        "CREATE (JamesThompson:Person {name:'James Thompson'})\n" +
                        "\n" +
                        "CREATE\n" +
                        "  (JamesThompson)-[:FOLLOWS]->(JessicaThompson),\n" +
                        "  (AngelaScope)-[:FOLLOWS]->(JessicaThompson),\n" +
                        "  (PaulBlythe)-[:FOLLOWS]->(AngelaScope)\n" +
                        "  \n" +
                        "CREATE\n" +
                        "  (JessicaThompson)-[:REVIEWED {summary:'An amazing journey', rating:95}]->(CloudAtlas),\n" +
                        "  (JessicaThompson)-[:REVIEWED {summary:'Silly, but fun', rating:65}]->(TheReplacements),\n" +
                        "  (JamesThompson)-[:REVIEWED {summary:'The coolest football movie ever', rating:100}]->(TheReplacements),\n" +
                        "  (AngelaScope)-[:REVIEWED {summary:'Pretty funny at times', rating:62}]->(TheReplacements),\n" +
                        "  (JessicaThompson)-[:REVIEWED {summary:'Dark, but compelling', rating:85}]->(Unforgiven),\n" +
                        "  (JessicaThompson)-[:REVIEWED {summary:\"Slapstick redeemed only by the Robin Williams and Gene Hackman's stellar performances\", rating:45}]->(TheBirdcage),\n" +
                        "  (JessicaThompson)-[:REVIEWED {summary:'A solid romp', rating:68}]->(TheDaVinciCode),\n" +
                        "  (JamesThompson)-[:REVIEWED {summary:'Fun, but a little far fetched', rating:65}]->(TheDaVinciCode)\n" +
                        "  \n" +
                        "RETURN TheMatrix\n" +
                        "\n" +
                        ";";

        try (Transaction tx = database.beginTx()) {
            tx.execute(cypher);
            tx.commit();
        }

        try (Transaction tx = database.beginTx()) {
            tx.getAllNodes().forEach(new Consumer<Node>() {
                @Override
                public void accept(Node node) {
                    if (IncludeAllBusinessNodes.getInstance().include(node)) {
                        assertTrue(node.hasProperty("uuid"));
                    }
                }
            });

            tx.getAllRelationships().forEach(new Consumer<Relationship>() {
                @Override
                public void accept(Relationship r) {
                    if (IncludeAllBusinessRelationships.getInstance().include(r)) {
                        assertTrue(r.hasProperty("uuid"));
                    }
                }
            });

            tx.commit();
        }
    }

    private void checkNoNodesWithLabelHaveUuid(String label) {
        try (Transaction tx = database.beginTx()) {
            Label label1 = Label.label(label);
            tx.findNodes(label1).forEachRemaining(node -> {
                assertFalse(node.hasProperty("uuid"));
            });
            tx.commit();
        }
    }

    private void checkAllNodesWithLabelHaveUuid(String label) {
        try (Transaction tx = database.beginTx()) {
            Label label1 = Label.label(label);
            tx.findNodes(label1).forEachRemaining(node -> {
                assertTrue(node.hasProperty("uuid"));
            });
            tx.commit();
        }
    }

    private void setUpNeo4j(GraphAwareNeo4jBuilder builder) {
        neo4j = builder.build();
        database = neo4j.defaultDatabaseService();
        databaseManagementService = neo4j.databaseManagementService();
    }

    private GraphAwareNeo4jBuilder defaultBuilder() {
        return emptyBuilder()
                .withGAConfig("com.graphaware.runtime.enabled", "true")
                .withGAConfig("com.graphaware.module.neo4j.UIDM.1", "com.graphaware.module.uuid.UuidBootstrapper")
                .withGAConfig("com.graphaware.module.neo4j.UIDM.uuidProperty", "uuid")
                .withGAConfig("com.graphaware.module.neo4j.UIDM.node", "hasLabel('Person') || hasLabel('Company')")
                .withGAConfig("com.graphaware.module.neo4j.UIDM.relationship", "isType('KNOWS')");
    }

    private GraphAwareNeo4jBuilder emptyBuilder() {
        return GraphAwareNeo4jBuilder.builder(Neo4jBuilders.newInProcessBuilder())
                .withDisabledServer()
                .withExtensionFactories(new ArrayList<>(Collections.singleton(new RuntimeExtensionFactory())))
                .withGAConfig("com.graphaware.runtime.enabled", "true");
    }
}
