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
import ga.uuid.NodeUuidFunctions;
import ga.uuid.RelationshipUuidFunctions;
import org.apache.commons.lang.StringUtils;
import org.junit.jupiter.api.Test;
import org.neo4j.graphdb.Entity;
import org.neo4j.graphdb.QueryExecutionException;
import org.neo4j.graphdb.Result;
import org.neo4j.graphdb.TransactionFailureException;
import org.neo4j.harness.TestServerBuilder;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class UuidModuleEndToEndProcTest extends DatabaseIntegrationTest {

    @Override
    protected String configFile() {
        return "neo4j-uuid-all.conf";
    }

    @Override
    protected TestServerBuilder registerProceduresAndFunctions(TestServerBuilder testServerBuilder) throws Exception {
        return super.registerProceduresAndFunctions(testServerBuilder)
                .withFunction(NodeUuidFunctions.class)
                .withFunction(RelationshipUuidFunctions.class)
                .withFunction(ga.uuid.nd.NodeUuidFunctions.class)
                .withFunction(ga.uuid.nd.RelationshipUuidFunctions.class);
    }

    @Test
    public void testWorkflow() {
        //Create & Assign
        getDatabase().execute("CREATE (p:Person {name:'Luanne'}), (c:Company {name:'GraphAware'}), (p)-[:WORKS_AT]->(c)");

        String personUuid = singleValue(getDatabase().execute("MATCH (p:Person) RETURN p.uuid"));
        String companyUuid = singleValue(getDatabase().execute("MATCH (c:Company) RETURN c.uuid"));
        String relUuid = singleValue(getDatabase().execute("MATCH (p:Person)-[r]-() RETURN r.uuid"));

        //Retrieve
        assertEquals("0", findNodeByUuid(personUuid));
        assertEquals("0", findNodeByUuid("UIDM", personUuid));
        assertEquals("[0, 1]", findNodesByUuids(personUuid, companyUuid));
        assertEquals("[0, 1]", findNodesByUuidsWithModule("UIDM", personUuid, companyUuid));
        assertEquals("0", findRelByUuid(relUuid));
        assertEquals("0", findRelByUuid("UIDM", relUuid));
        assertEquals("[0]", findRelsByUuids(relUuid));
        assertEquals("[0]", findRelsByUuidsWithModule("UIDM", relUuid));

        //(can't) Update
        assertThrows(TransactionFailureException.class, () -> {
            getDatabase().execute("MATCH (p:Person {name:'Luanne'}) SET p.uuid='new'");
        });

        assertEquals(personUuid, singleValue(getDatabase().execute("MATCH (p:Person) RETURN p.uuid")));

        //Delete
        getDatabase().execute("MATCH (p:Person {name:'Luanne'}) DETACH DELETE p");
        assertEquals("null", findNodeByUuid(personUuid));
        assertEquals("[]", findNodesByUuids(personUuid, "something"));
        assertEquals("null", findNodeByUuid("UIDM", personUuid));
        assertEquals("[]", findNodesByUuidsWithModule("UIDM", personUuid, "something"));
    }

    @Test
    public void testWorkflowWithManuallyAssignedId() {
        //Create & Assign
        getDatabase().execute("CREATE (p:Person {name:'Luanne', uuid:'123'}), (c:Company {name:'GraphAware', uuid:'456'}), (p)-[:WORKS_AT {uuid:'789'}]->(c)");

        //Retrieve
        assertEquals("0", findNodeByUuid("123"));
        assertEquals("0", findNodeByUuid("UIDM", "123"));

        //(can't) Update
        assertThrows(TransactionFailureException.class, () -> {
            getDatabase().execute("MATCH (p:Person {name:'Luanne'}) SET p.uuid='new'");
        });

        assertEquals("123", singleValue(getDatabase().execute("MATCH (p:Person) RETURN p.uuid")));

        //Delete
        getDatabase().execute("MATCH (p:Person {name:'Luanne'}) DETACH DELETE p");
        assertEquals("null", findNodeByUuid("123"));
        assertEquals("null", findNodeByUuid("UIDM", "123"));
    }

    @Test
    public void shouldReturnNullWhenUuidNotExists() {
        assertEquals("null", findNodeByUuid("not-exists"));
        assertEquals("null", findNodeByUuid("UIDM", "not-exists"));
    }

    @Test
    public void shouldReturnErrorsWhenModuleNotExists() {
        assertThrows(QueryExecutionException.class, () -> {
            findNodeByUuid("not-exists", "irrelevant");
        });
    }

    private String findNodeByUuid(String uuid) {
        return singleValue(getDatabase().execute("RETURN id(ga.uuid.findNode('" + uuid + "')) as id"));
    }

    private String findNodeByUuid(String moduleId, String uuid) {
        return singleValue(getDatabase().execute("RETURN id(ga.uuid.nd.findNode('" + moduleId + "','" + uuid + "')) as id"));
    }

    private String findRelByUuid(String uuid) {
        return singleValue(getDatabase().execute("RETURN id(ga.uuid.findRelationship('" + uuid + "')) as id"));
    }

    private String findRelByUuid(String moduleId, String uuid) {
        return singleValue(getDatabase().execute("RETURN id(ga.uuid.nd.findRelationship('" + moduleId + "','" + uuid + "')) as id"));
    }

    //the following queries could be simpler, but there is a bug in Neo todo report
    private String findNodesByUuids(String... uuid) {
        return singleValue(getDatabase().execute("UNWIND ga.uuid.findNodes(['" + StringUtils.join(uuid, "','") + "']) as node RETURN collect(id(node)) as ids"));
    }

    private String findNodesByUuidsWithModule(String moduleId, String... uuid) {
        return singleValue(getDatabase().execute("UNWIND ga.uuid.nd.findNodes('" + moduleId + "',['" + StringUtils.join(uuid, "','") + "']) as node RETURN collect(id(node)) as ids"));
    }

    private String findRelsByUuids(String... uuid) {
        return singleValue(getDatabase().execute("UNWIND ga.uuid.findRelationships(['" + StringUtils.join(uuid, "','") + "']) as relationship RETURN collect(id(relationship)) as ids"));
    }

    private String findRelsByUuidsWithModule(String moduleId, String... uuid) {
        return singleValue(getDatabase().execute("UNWIND ga.uuid.nd.findRelationships('" + moduleId + "',['" + StringUtils.join(uuid, "','") + "']) as relationship RETURN collect(id(relationship)) as ids"));
    }

    private String singleValue(Result result) {
        Object next = result.next().values().iterator().next();
        if (next == null) {
            return "null";
        }
        return next.toString();
    }
}
