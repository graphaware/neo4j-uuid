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

package com.graphaware.module.guid;

import com.graphaware.test.integration.GraphAwareIntegrationTest;
import org.apache.commons.lang.StringUtils;
import org.junit.Test;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class GuidModuleEndToEndProcTest extends GraphAwareIntegrationTest {

    public static final Pattern GUID_PATTERN = Pattern.compile("\\\"guid\\\":\\\"([a-zA-Z0-9-]*)\\\"");

    @Override
    protected String configFile() {
        return "neo4j-guid-all.conf";
    }

    @Test
    public void testWorkflow() {
        //Create & Assign
        httpClient.executeCypher(baseNeoUrl(), "CREATE (p:Person {name:'Luanne'}), (c:Company {name:'GraphAware'}), (p)-[:WORKS_AT]->(c)");

        String personGuid = getGuid(httpClient.executeCypher(baseNeoUrl(), "MATCH (p:Person) RETURN p"));
        String companyGuid = getGuid(httpClient.executeCypher(baseNeoUrl(), "MATCH (c:Company) RETURN c"));
        String relGuid = getGuid(httpClient.executeCypher(baseNeoUrl(), "MATCH (p:Person)-[r]-() RETURN r"));

        //Retrieve
        assertEquals("{\"results\":[{\"columns\":[\"id(n)\"],\"data\":[{\"row\":[0],\"meta\":[null]}]}],\"errors\":[]}", findNodeByGuid(personGuid));
        assertEquals("{\"results\":[{\"columns\":[\"id(n)\"],\"data\":[{\"row\":[0],\"meta\":[null]}]}],\"errors\":[]}", findNodeByGuid("UIDM", personGuid));
        assertEquals("{\"results\":[{\"columns\":[\"ids\"],\"data\":[{\"row\":[[0,1]],\"meta\":[null,null]}]}],\"errors\":[]}", findNodesByGuids(personGuid, companyGuid));
        assertEquals("{\"results\":[{\"columns\":[\"ids\"],\"data\":[{\"row\":[[0,1]],\"meta\":[null,null]}]}],\"errors\":[]}", findNodesByGuidsWithModule("UIDM", personGuid, companyGuid));
        assertEquals("{\"results\":[{\"columns\":[\"id(n)\"],\"data\":[{\"row\":[0],\"meta\":[null]}]}],\"errors\":[]}", findRelByGuid(relGuid));
        assertEquals("{\"results\":[{\"columns\":[\"id(n)\"],\"data\":[{\"row\":[0],\"meta\":[null]}]}],\"errors\":[]}", findRelByGuid("UIDM", relGuid));
        assertEquals("{\"results\":[{\"columns\":[\"ids\"],\"data\":[{\"row\":[[0]],\"meta\":[null]}]}],\"errors\":[]}", findRelsByGuids(relGuid));
        assertEquals("{\"results\":[{\"columns\":[\"ids\"],\"data\":[{\"row\":[[0]],\"meta\":[null]}]}],\"errors\":[]}", findRelsByGuidsWithModule("UIDM", relGuid));

        //(can't) Update
        httpClient.executeCypher(baseNeoUrl(), "MATCH (p:Person {name:'Luanne'}) SET p.guid=new");

        String response = httpClient.executeCypher(baseNeoUrl(), "MATCH (p:Person) RETURN p");

        assertEquals(personGuid, getGuid(response));

        //Delete
        httpClient.executeCypher(baseNeoUrl(), "MATCH (p:Person {name:'Luanne'}) DETACH DELETE p");
        assertEquals("{\"results\":[{\"columns\":[\"id(n)\"],\"data\":[]}],\"errors\":[{\"code\":\"Neo.ClientError.Procedure.ProcedureCallFailed\",\"message\":\"Failed to invoke procedure `ga.guid.findNode`: Caused by: org.neo4j.graphdb.NotFoundException: Node with GUID " + personGuid + " does not exist\"}]}", findNodeByGuid(personGuid));
        assertEquals("{\"results\":[],\"errors\":[{\"code\":\"Neo.ClientError.Procedure.ProcedureCallFailed\",\"message\":\"Failed to invoke procedure `ga.guid.findNodes`: Caused by: org.neo4j.graphdb.NotFoundException: Node with GUID " + personGuid + " does not exist\"}]}", findNodesByGuids(personGuid, "something"));
        assertEquals("{\"results\":[{\"columns\":[\"id(n)\"],\"data\":[]}],\"errors\":[{\"code\":\"Neo.ClientError.Procedure.ProcedureCallFailed\",\"message\":\"Failed to invoke procedure `ga.guid.nd.findNode`: Caused by: org.neo4j.graphdb.NotFoundException: Node with GUID " + personGuid + " does not exist\"}]}", findNodeByGuid("UIDM", personGuid));
        assertEquals("{\"results\":[],\"errors\":[{\"code\":\"Neo.ClientError.Procedure.ProcedureCallFailed\",\"message\":\"Failed to invoke procedure `ga.guid.nd.findNodes`: Caused by: org.neo4j.graphdb.NotFoundException: Node with GUID " + personGuid + " does not exist\"}]}", findNodesByGuidsWithModule("UIDM", personGuid, "something"));
    }

    @Test
    public void testWorkflowWithManuallyAssignedId() {
        //Create & Assign
        httpClient.executeCypher(baseNeoUrl(), "CREATE (p:Person {name:'Luanne', guid:'123'}), (c:Company {name:'GraphAware', guid:'456'}), (p)-[:WORKS_AT {guid:'789'}]->(c)");

        //Retrieve
        assertEquals("{\"results\":[{\"columns\":[\"id(n)\"],\"data\":[{\"row\":[0],\"meta\":[null]}]}],\"errors\":[]}", findNodeByGuid("123"));
        assertEquals("{\"results\":[{\"columns\":[\"id(n)\"],\"data\":[{\"row\":[0],\"meta\":[null]}]}],\"errors\":[]}", findNodeByGuid("UIDM", "123"));

        //(can't) Update
        httpClient.executeCypher(baseNeoUrl(), "MATCH (p:Person {name:'Luanne'}) SET p.guid=new");

        assertEquals("123", getGuid(httpClient.executeCypher(baseNeoUrl(), "MATCH (p:Person) RETURN p")));

        //Delete
        httpClient.executeCypher(baseNeoUrl(), "MATCH (p:Person {name:'Luanne'}) DETACH DELETE p");
        assertEquals("{\"results\":[{\"columns\":[\"id(n)\"],\"data\":[]}],\"errors\":[{\"code\":\"Neo.ClientError.Procedure.ProcedureCallFailed\",\"message\":\"Failed to invoke procedure `ga.guid.findNode`: Caused by: org.neo4j.graphdb.NotFoundException: Node with GUID 123 does not exist\"}]}", findNodeByGuid("123"));
        assertEquals("{\"results\":[{\"columns\":[\"id(n)\"],\"data\":[]}],\"errors\":[{\"code\":\"Neo.ClientError.Procedure.ProcedureCallFailed\",\"message\":\"Failed to invoke procedure `ga.guid.nd.findNode`: Caused by: org.neo4j.graphdb.NotFoundException: Node with GUID 123 does not exist\"}]}", findNodeByGuid("UIDM", "123"));
    }

    @Test
    public void shouldReturnErrorsWhenGuidNotExists() {
        assertEquals("{\"results\":[{\"columns\":[\"id(n)\"],\"data\":[]}],\"errors\":[{\"code\":\"Neo.ClientError.Procedure.ProcedureCallFailed\",\"message\":\"Failed to invoke procedure `ga.guid.findNode`: Caused by: org.neo4j.graphdb.NotFoundException: Node with GUID not-exists does not exist\"}]}", findNodeByGuid("not-exists"));
        assertEquals("{\"results\":[{\"columns\":[\"id(n)\"],\"data\":[]}],\"errors\":[{\"code\":\"Neo.ClientError.Procedure.ProcedureCallFailed\",\"message\":\"Failed to invoke procedure `ga.guid.nd.findNode`: Caused by: org.neo4j.graphdb.NotFoundException: Node with GUID not-exists does not exist\"}]}", findNodeByGuid("UIDM", "not-exists"));
    }

    @Test
    public void shouldReturnErrorsWhenModuleNotExists() {
        assertEquals("{\"results\":[{\"columns\":[\"id(n)\"],\"data\":[]}],\"errors\":[{\"code\":\"Neo.ClientError.Procedure.ProcedureCallFailed\",\"message\":\"Failed to invoke procedure `ga.guid.nd.findNode`: Caused by: org.neo4j.graphdb.NotFoundException: No module of type com.graphaware.module.guid.GuidModule with ID not-exists has been registered\"}]}", findNodeByGuid("not-exists", "irrelevant"));
    }

    private String getGuid(String response) {
        Matcher matcher = GUID_PATTERN.matcher(response);
        assertTrue(matcher.find());
        return matcher.group(1);
    }

    private String findNodeByGuid(String guid) {
        return httpClient.executeCypher(baseNeoUrl(), "CALL ga.guid.findNode('" + guid + "') YIELD node as n return id(n)");
    }

    private String findNodeByGuid(String moduleId, String guid) {
        return httpClient.executeCypher(baseNeoUrl(), "CALL ga.guid.nd.findNode('" + moduleId + "','" + guid + "') YIELD node as n return id(n)");
    }

    private String findRelByGuid(String guid) {
        return httpClient.executeCypher(baseNeoUrl(), "CALL ga.guid.findRelationship('" + guid + "') YIELD relationship as n return id(n)");
    }

    private String findRelByGuid(String moduleId, String guid) {
        return httpClient.executeCypher(baseNeoUrl(), "CALL ga.guid.nd.findRelationship('" + moduleId + "','" + guid + "') YIELD relationship as n return id(n)");
    }

    //the following queries could be simpler, but there is a bug in Neo todo report
    private String findNodesByGuids(String... guid) {
        return httpClient.executeCypher(baseNeoUrl(), "CALL ga.guid.findNodes(['" + StringUtils.join(guid, "','") + "']) YIELD nodes UNWIND nodes as node RETURN collect(id(node)) as ids");
    }

    private String findNodesByGuidsWithModule(String moduleId, String... guid) {
        return httpClient.executeCypher(baseNeoUrl(), "CALL ga.guid.nd.findNodes('" + moduleId + "',['" + StringUtils.join(guid, "','") + "']) YIELD nodes UNWIND nodes as node RETURN collect(id(node)) as ids");
    }

    private String findRelsByGuids(String... guid) {
        return httpClient.executeCypher(baseNeoUrl(), "CALL ga.guid.findRelationships(['" + StringUtils.join(guid, "','") + "']) YIELD relationships UNWIND relationships as r RETURN collect(id(r)) as ids");
    }

    private String findRelsByGuidsWithModule(String moduleId, String... guid) {
        return httpClient.executeCypher(baseNeoUrl(), "CALL ga.guid.nd.findRelationships('" + moduleId + "',['" + StringUtils.join(guid, "','") + "']) YIELD relationships UNWIND relationships as r RETURN collect(id(r)) as ids");
    }
}
