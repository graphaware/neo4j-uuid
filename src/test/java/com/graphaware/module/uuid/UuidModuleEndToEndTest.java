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

package com.graphaware.module.uuid;

import com.graphaware.test.integration.NeoServerIntegrationTest;
import org.junit.Test;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.apache.http.HttpStatus.SC_NOT_FOUND;
import static org.apache.http.HttpStatus.SC_OK;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class UuidModuleEndToEndTest extends NeoServerIntegrationTest {

    public static final Pattern UUID_PATTERN = Pattern.compile("\\\"uuid\\\":\\\"([a-zA-Z0-9-]*)\\\"");

    @Override
    protected String neo4jConfigFile() {
        return "neo4j-uuid-all.properties";
    }

    @Test
    public void testWorkflow() {
        //Create & Assign
        httpClient.executeCypher(baseUrl(), "CREATE (p:Person {name:'Luanne'})");

        String response = httpClient.executeCypher(baseUrl(), "MATCH (p:Person) RETURN p");

        Matcher matcher = UUID_PATTERN.matcher(response);
        assertTrue(matcher.find());
        String uuid = matcher.group(1);

        //Retrieve
        assertEquals("0", httpClient.get(baseUrl() + "/graphaware/uuid/UIDM/node/" + uuid, SC_OK));

        //(can't) Update
        response = httpClient.executeCypher(baseUrl(), "MATCH (p:Person {name:'Luanne'}) SET p.uuid=new");

        System.out.println(response);

        response = httpClient.executeCypher(baseUrl(), "MATCH (p:Person) RETURN p");

        matcher = UUID_PATTERN.matcher(response);
        assertTrue(matcher.find());
        assertEquals(uuid, matcher.group(1));

        //Delete
        httpClient.executeCypher(baseUrl(), "MATCH (p:Person {name:'Luanne'}) DELETE p");
        httpClient.get(baseUrl() + "/graphaware/uuid/node/" + uuid, SC_NOT_FOUND);
    }

    @Test
    public void testWorkflowWithManuallyAssignedId() {
        //Create & Assign
        httpClient.executeCypher(baseUrl(), "CREATE (p:Person {name:'Luanne', uuid:'123'})");

        String response = httpClient.executeCypher(baseUrl(), "MATCH (p:Person) RETURN p");

        Matcher matcher = UUID_PATTERN.matcher(response);
        assertTrue(matcher.find());
        String uuid = matcher.group(1);

        //Retrieve
        assertEquals("0", httpClient.get(baseUrl() + "/graphaware/uuid/UIDM/node/" + uuid, SC_OK));

        //(can't) Update
        response = httpClient.executeCypher(baseUrl(), "MATCH (p:Person {name:'Luanne'}) SET p.uuid=new");

        System.out.println(response);

        response = httpClient.executeCypher(baseUrl(), "MATCH (p:Person) RETURN p");

        matcher = UUID_PATTERN.matcher(response);
        assertTrue(matcher.find());
        assertEquals(uuid, matcher.group(1));

        //Delete
        httpClient.executeCypher(baseUrl(), "MATCH (p:Person {name:'Luanne'}) DELETE p");
        httpClient.get(baseUrl() + "/graphaware/uuid/node/" + uuid, SC_NOT_FOUND);
    }

    @Test
    public void testIssue6() {
        String response = httpClient.executeCypher(baseUrl(), "CREATE (:Person {name:'Luanne', uuid:'123'}), (:Person {name:'Michal', uuid:'123'})");
        assertTrue(response.contains("Neo.DatabaseError.Transaction.CouldNotCommit"));

        assertEquals("{\"results\":[{\"columns\":[\"p\"],\"data\":[]}],\"errors\":[]}", httpClient.executeCypher(baseUrl(), "MATCH (p:Person) RETURN p"));
    }

    @Test
    public void shouldReturn404WhenUuidNotExists() {
        httpClient.get(baseUrl() + "/graphaware/uuid/node/not-exists", SC_NOT_FOUND);
    }

    @Test
    public void shouldReturn404WhenModuleNotExists() {
        httpClient.get(baseUrl() + "/graphaware/uuid/non-existing-module/node/not-exists", SC_NOT_FOUND);
    }
}
