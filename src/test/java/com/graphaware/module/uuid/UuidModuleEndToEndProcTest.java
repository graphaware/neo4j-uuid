/*
 * Copyright (c) 2013-2017 GraphAware
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

import com.graphaware.test.integration.GraphAwareIntegrationTest;
import org.apache.commons.lang.StringUtils;
import org.junit.Test;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class UuidModuleEndToEndProcTest extends GraphAwareIntegrationTest {

    public static final Pattern UUID_PATTERN = Pattern.compile("\\\"uuid\\\":\\\"([a-zA-Z0-9-]*)\\\"");

    @Override
    protected String configFile() {
        return "neo4j-uuid-all.conf";
    }

    @Test
    public void testWorkflow() {
        //Create & Assign
        httpClient.executeCypher(baseNeoUrl(), "CREATE (p:Person {name:'Luanne'}), (c:Company {name:'GraphAware'}), (p)-[:WORKS_AT]->(c)");

        String personUuid = getUuid(httpClient.executeCypher(baseNeoUrl(), "MATCH (p:Person) RETURN p"));
        String companyUuid = getUuid(httpClient.executeCypher(baseNeoUrl(), "MATCH (c:Company) RETURN c"));
        String relUuid = getUuid(httpClient.executeCypher(baseNeoUrl(), "MATCH (p:Person)-[r]-() RETURN r"));

        //Retrieve
        assertEquals("{\"results\":[{\"columns\":[\"id(n)\"],\"data\":[{\"row\":[0],\"meta\":[null]}]}],\"errors\":[]}", findNodeByUuid(personUuid));
        assertEquals("{\"results\":[{\"columns\":[\"id(n)\"],\"data\":[{\"row\":[0],\"meta\":[null]}]}],\"errors\":[]}", findNodeByUuid("UIDM", personUuid));
        assertEquals("{\"results\":[{\"columns\":[\"ids\"],\"data\":[{\"row\":[[0,1]],\"meta\":[null,null]}]}],\"errors\":[]}", findNodesByUuids(personUuid, companyUuid));
        assertEquals("{\"results\":[{\"columns\":[\"ids\"],\"data\":[{\"row\":[[0,1]],\"meta\":[null,null]}]}],\"errors\":[]}", findNodesByUuidsWithModule("UIDM", personUuid, companyUuid));
        assertEquals("{\"results\":[{\"columns\":[\"id(n)\"],\"data\":[{\"row\":[0],\"meta\":[null]}]}],\"errors\":[]}", findRelByUuid(relUuid));
        assertEquals("{\"results\":[{\"columns\":[\"id(n)\"],\"data\":[{\"row\":[0],\"meta\":[null]}]}],\"errors\":[]}", findRelByUuid("UIDM", relUuid));
        assertEquals("{\"results\":[{\"columns\":[\"ids\"],\"data\":[{\"row\":[[0]],\"meta\":[null]}]}],\"errors\":[]}", findRelsByUuids(relUuid));
        assertEquals("{\"results\":[{\"columns\":[\"ids\"],\"data\":[{\"row\":[[0]],\"meta\":[null]}]}],\"errors\":[]}", findRelsByUuidsWithModule("UIDM", relUuid));

        //(can't) Update
        httpClient.executeCypher(baseNeoUrl(), "MATCH (p:Person {name:'Luanne'}) SET p.uuid=new");

        String response = httpClient.executeCypher(baseNeoUrl(), "MATCH (p:Person) RETURN p");

        assertEquals(personUuid, getUuid(response));

        //Delete
        httpClient.executeCypher(baseNeoUrl(), "MATCH (p:Person {name:'Luanne'}) DETACH DELETE p");
        assertEquals("{\"results\":[{\"columns\":[\"id(n)\"],\"data\":[]}],\"errors\":[{\"code\":\"Neo.ClientError.Procedure.ProcedureCallFailed\",\"message\":\"Failed to invoke procedure `ga.uuid.findNode`: Caused by: org.neo4j.graphdb.NotFoundException: Node with UUID " + personUuid + " does not exist\"}]}", findNodeByUuid(personUuid));
        assertEquals("{\"results\":[],\"errors\":[{\"code\":\"Neo.ClientError.Procedure.ProcedureCallFailed\",\"message\":\"Failed to invoke procedure `ga.uuid.findNodes`: Caused by: org.neo4j.graphdb.NotFoundException: Node with UUID " + personUuid + " does not exist\"}]}", findNodesByUuids(personUuid, "something"));
        assertEquals("{\"results\":[{\"columns\":[\"id(n)\"],\"data\":[]}],\"errors\":[{\"code\":\"Neo.ClientError.Procedure.ProcedureCallFailed\",\"message\":\"Failed to invoke procedure `ga.uuid.nd.findNode`: Caused by: org.neo4j.graphdb.NotFoundException: Node with UUID " + personUuid + " does not exist\"}]}", findNodeByUuid("UIDM", personUuid));
        assertEquals("{\"results\":[],\"errors\":[{\"code\":\"Neo.ClientError.Procedure.ProcedureCallFailed\",\"message\":\"Failed to invoke procedure `ga.uuid.nd.findNodes`: Caused by: org.neo4j.graphdb.NotFoundException: Node with UUID " + personUuid + " does not exist\"}]}", findNodesByUuidsWithModule("UIDM", personUuid, "something"));
    }

    @Test
    public void testWorkflowWithManuallyAssignedId() {
        //Create & Assign
        httpClient.executeCypher(baseNeoUrl(), "CREATE (p:Person {name:'Luanne', uuid:'123'}), (c:Company {name:'GraphAware', uuid:'456'}), (p)-[:WORKS_AT {uuid:'789'}]->(c)");

        //Retrieve
        assertEquals("{\"results\":[{\"columns\":[\"id(n)\"],\"data\":[{\"row\":[0],\"meta\":[null]}]}],\"errors\":[]}", findNodeByUuid("123"));
        assertEquals("{\"results\":[{\"columns\":[\"id(n)\"],\"data\":[{\"row\":[0],\"meta\":[null]}]}],\"errors\":[]}", findNodeByUuid("UIDM", "123"));

        //(can't) Update
        httpClient.executeCypher(baseNeoUrl(), "MATCH (p:Person {name:'Luanne'}) SET p.uuid=new");

        assertEquals("123", getUuid(httpClient.executeCypher(baseNeoUrl(), "MATCH (p:Person) RETURN p")));

        //Delete
        httpClient.executeCypher(baseNeoUrl(), "MATCH (p:Person {name:'Luanne'}) DETACH DELETE p");
        assertEquals("{\"results\":[{\"columns\":[\"id(n)\"],\"data\":[]}],\"errors\":[{\"code\":\"Neo.ClientError.Procedure.ProcedureCallFailed\",\"message\":\"Failed to invoke procedure `ga.uuid.findNode`: Caused by: org.neo4j.graphdb.NotFoundException: Node with UUID 123 does not exist\"}]}", findNodeByUuid("123"));
        assertEquals("{\"results\":[{\"columns\":[\"id(n)\"],\"data\":[]}],\"errors\":[{\"code\":\"Neo.ClientError.Procedure.ProcedureCallFailed\",\"message\":\"Failed to invoke procedure `ga.uuid.nd.findNode`: Caused by: org.neo4j.graphdb.NotFoundException: Node with UUID 123 does not exist\"}]}", findNodeByUuid("UIDM", "123"));
    }

    @Test
    public void shouldReturnErrorsWhenUuidNotExists() {
        assertEquals("{\"results\":[{\"columns\":[\"id(n)\"],\"data\":[]}],\"errors\":[{\"code\":\"Neo.ClientError.Procedure.ProcedureCallFailed\",\"message\":\"Failed to invoke procedure `ga.uuid.findNode`: Caused by: org.neo4j.graphdb.NotFoundException: Node with UUID not-exists does not exist\"}]}", findNodeByUuid("not-exists"));
        assertEquals("{\"results\":[{\"columns\":[\"id(n)\"],\"data\":[]}],\"errors\":[{\"code\":\"Neo.ClientError.Procedure.ProcedureCallFailed\",\"message\":\"Failed to invoke procedure `ga.uuid.nd.findNode`: Caused by: org.neo4j.graphdb.NotFoundException: Node with UUID not-exists does not exist\"}]}", findNodeByUuid("UIDM", "not-exists"));
    }

    @Test
    public void shouldReturnErrorsWhenModuleNotExists() {
        assertEquals("{\"results\":[{\"columns\":[\"id(n)\"],\"data\":[]}],\"errors\":[{\"code\":\"Neo.ClientError.Procedure.ProcedureCallFailed\",\"message\":\"Failed to invoke procedure `ga.uuid.nd.findNode`: Caused by: org.neo4j.graphdb.NotFoundException: No module of type com.graphaware.module.uuid.UuidModule with ID not-exists has been registered\"}]}", findNodeByUuid("not-exists", "irrelevant"));
    }

    private String getUuid(String response) {
        Matcher matcher = UUID_PATTERN.matcher(response);
        assertTrue(matcher.find());
        return matcher.group(1);
    }

    private String findNodeByUuid(String uuid) {
        return httpClient.executeCypher(baseNeoUrl(), "CALL ga.uuid.findNode('" + uuid + "') YIELD node as n return id(n)");
    }

    private String findNodeByUuid(String moduleId, String uuid) {
        return httpClient.executeCypher(baseNeoUrl(), "CALL ga.uuid.nd.findNode('" + moduleId + "','" + uuid + "') YIELD node as n return id(n)");
    }

    private String findRelByUuid(String uuid) {
        return httpClient.executeCypher(baseNeoUrl(), "CALL ga.uuid.findRelationship('" + uuid + "') YIELD relationship as n return id(n)");
    }

    private String findRelByUuid(String moduleId, String uuid) {
        return httpClient.executeCypher(baseNeoUrl(), "CALL ga.uuid.nd.findRelationship('" + moduleId + "','" + uuid + "') YIELD relationship as n return id(n)");
    }

    //the following queries could be simpler, but there is a bug in Neo todo report
    private String findNodesByUuids(String... uuid) {
        return httpClient.executeCypher(baseNeoUrl(), "CALL ga.uuid.findNodes(['" + StringUtils.join(uuid, "','") + "']) YIELD nodes UNWIND nodes as node RETURN collect(id(node)) as ids");
    }

    private String findNodesByUuidsWithModule(String moduleId, String... uuid) {
        return httpClient.executeCypher(baseNeoUrl(), "CALL ga.uuid.nd.findNodes('" + moduleId + "',['" + StringUtils.join(uuid, "','") + "']) YIELD nodes UNWIND nodes as node RETURN collect(id(node)) as ids");
    }

    private String findRelsByUuids(String... uuid) {
        return httpClient.executeCypher(baseNeoUrl(), "CALL ga.uuid.findRelationships(['" + StringUtils.join(uuid, "','") + "']) YIELD relationships UNWIND relationships as r RETURN collect(id(r)) as ids");
    }

    private String findRelsByUuidsWithModule(String moduleId, String... uuid) {
        return httpClient.executeCypher(baseNeoUrl(), "CALL ga.uuid.nd.findRelationships('" + moduleId + "',['" + StringUtils.join(uuid, "','") + "']) YIELD relationships UNWIND relationships as r RETURN collect(id(r)) as ids");
    }
}
