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
import org.junit.Test;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.apache.http.HttpStatus.SC_NOT_FOUND;
import static org.apache.http.HttpStatus.SC_OK;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class GuidModuleEndToEndApiTest extends GraphAwareIntegrationTest {

    public static final Pattern GUID_PATTERN = Pattern.compile("\\\"guid\\\":\\\"([a-zA-Z0-9-]*)\\\"");

    @Override
    protected String configFile() {
        return "neo4j-guid-all.conf";
    }

    @Test
    public void testWorkflow() {
        //Create & Assign
        httpClient.executeCypher(baseNeoUrl(), "CREATE (p:Person {name:'Luanne'})");

        String response = httpClient.executeCypher(baseNeoUrl(), "MATCH (p:Person) RETURN p");

        Matcher matcher = GUID_PATTERN.matcher(response);
        assertTrue(matcher.find());
        String guid = matcher.group(1);

        //Retrieve
        assertEquals("0", httpClient.get(baseUrl() + "/guid/UIDM/node/" + guid, SC_OK));

        //(can't) Update
        response = httpClient.executeCypher(baseNeoUrl(), "MATCH (p:Person {name:'Luanne'}) SET p.guid=new");

        System.out.println(response);

        response = httpClient.executeCypher(baseNeoUrl(), "MATCH (p:Person) RETURN p");

        matcher = GUID_PATTERN.matcher(response);
        assertTrue(matcher.find());
        assertEquals(guid, matcher.group(1));

        //Delete
        httpClient.executeCypher(baseNeoUrl(), "MATCH (p:Person {name:'Luanne'}) DELETE p");
        httpClient.get(baseUrl() + "/guid/node/" + guid, SC_NOT_FOUND);
    }

    @Test
    public void testWorkflowWithManuallyAssignedId() {
        //Create & Assign
        httpClient.executeCypher(baseNeoUrl(), "CREATE (p:Person {name:'Luanne', guid:'123'})");

        String response = httpClient.executeCypher(baseNeoUrl(), "MATCH (p:Person) RETURN p");

        Matcher matcher = GUID_PATTERN.matcher(response);
        assertTrue(matcher.find());
        String guid = matcher.group(1);

        //Retrieve
        assertEquals("0", httpClient.get(baseUrl() + "/guid/UIDM/node/" + guid, SC_OK));

        //(can't) Update
        response = httpClient.executeCypher(baseNeoUrl(), "MATCH (p:Person {name:'Luanne'}) SET p.guid=new");

        System.out.println(response);

        response = httpClient.executeCypher(baseNeoUrl(), "MATCH (p:Person) RETURN p");

        matcher = GUID_PATTERN.matcher(response);
        assertTrue(matcher.find());
        assertEquals(guid, matcher.group(1));

        //Delete
        httpClient.executeCypher(baseNeoUrl(), "MATCH (p:Person {name:'Luanne'}) DELETE p");
        httpClient.get(baseUrl() + "/guid/node/" + guid, SC_NOT_FOUND);
    }

    @Test
    public void shouldNotBeAbleToCreateTwoNodesWithTheSameGUID() {
        String response = httpClient.executeCypher(baseNeoUrl(), "CREATE (:Person {name:'Luanne', guid:'123'}), (:Person {name:'Michal', guid:'123'})");

        assertTrue(response.contains("Neo.ClientError.Transaction.TransactionHookFailed"));

        assertEquals("{\"results\":[{\"columns\":[\"p\"],\"data\":[]}],\"errors\":[]}", httpClient.executeCypher(baseNeoUrl(), "MATCH (p:Person) RETURN p"));
    }

    @Test
    public void shouldNotBeAbleToCreateTwoRelsWithTheSameGUID() {
        String response = httpClient.executeCypher(baseNeoUrl(), "CREATE (p1:Person {name:'Luanne'}), (p2:Person {name:'Michal'}), (p1)-[:FRIEND_OF {guid:'123'}]->(p2), (p1)-[:COLLEAGUE_OF {guid:'123'}]->(p2)");

        assertTrue(response.contains("Neo.ClientError.Transaction.TransactionHookFailed"));

        assertEquals("{\"results\":[{\"columns\":[\"p\"],\"data\":[]}],\"errors\":[]}", httpClient.executeCypher(baseNeoUrl(), "MATCH (p:Person) RETURN p"));
    }

    @Test
    public void shouldReturn404WhenGuidNotExists() {
        httpClient.get(baseUrl() + "/guid/node/not-exists", SC_NOT_FOUND);
    }

    @Test
    public void shouldReturn404WhenModuleNotExists() {
        httpClient.get(baseUrl() + "/guid/non-existing-module/node/not-exists", SC_NOT_FOUND);
    }
}
