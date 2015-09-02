/*
 * Copyright (c) 2015 GraphAware
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

import static org.apache.http.HttpStatus.*;
import static org.junit.Assert.*;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.graphaware.test.integration.NeoServerIntegrationTest;
import org.junit.Test;

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
    public void shouldReturn404WhenUuidNotExists() {
        httpClient.get(baseUrl() + "/graphaware/uuid/node/not-exists", SC_NOT_FOUND);
    }

    @Test
    public void shouldReturn404WhenModuleNotExists() {
        httpClient.get(baseUrl() + "/graphaware/uuid/non-existing-module/node/not-exists", SC_NOT_FOUND);
    }
}
