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

import static org.apache.http.HttpStatus.SC_OK;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class GuidModuleMultipleModulesTest extends GraphAwareIntegrationTest {

    @Override
    protected String configFile() {
        return "neo4j-guid-multiple.conf";
    }

    @Test
    public void testWorkflow() {
        //Create & Assign
        httpClient.executeCypher(baseNeoUrl(), "CREATE (:Customer {name:'c1'})");
        httpClient.executeCypher(baseNeoUrl(), "CREATE (:User {name:'u1'})");
        httpClient.executeCypher(baseNeoUrl(), "CREATE (:SomethingElse {name:'s1'})");

        String response = httpClient.executeCypher(baseNeoUrl(), "MATCH (c:Customer) RETURN c");

        Matcher matcher = Pattern.compile("\\\"customerId\\\":\\\"([a-zA-Z0-9-]*)\\\"").matcher(response);
        assertTrue(matcher.find());
        String guid = matcher.group(1);

        //Retrieve
        assertEquals("0", httpClient.get(baseUrl() + "/guid/UID1/node/" + guid, SC_OK));

        response = httpClient.executeCypher(baseNeoUrl(), "MATCH (u:User) RETURN u");

        matcher = Pattern.compile("\\\"userId\\\":\\\"([a-zA-Z0-9-]*)\\\"").matcher(response);
        assertTrue(matcher.find());
        guid = matcher.group(1);

        //Retrieve
        assertEquals("1", httpClient.get(baseUrl() + "/guid/UID2/node/" + guid, SC_OK));
    }

    @Test
    public void testProcedures() {
        //Create & Assign
        httpClient.executeCypher(baseNeoUrl(), "CREATE (:Customer {name:'c1'})");
        httpClient.executeCypher(baseNeoUrl(), "CREATE (:User {name:'u1'})");
        httpClient.executeCypher(baseNeoUrl(), "CREATE (:SomethingElse {name:'s1'})");

        String response = httpClient.executeCypher(baseNeoUrl(), "MATCH (c:Customer) RETURN c");

        Matcher matcher = Pattern.compile("\\\"customerId\\\":\\\"([a-zA-Z0-9-]*)\\\"").matcher(response);
        assertTrue(matcher.find());
        String guid = matcher.group(1);

        //Retrieve
        assertEquals("{\"results\":[{\"columns\":[\"id(n)\"],\"data\":[{\"row\":[0],\"meta\":[null]}]}],\"errors\":[]}", findNodeByGuid("UID1", guid));

        response = httpClient.executeCypher(baseNeoUrl(), "MATCH (u:User) RETURN u");

        matcher = Pattern.compile("\\\"userId\\\":\\\"([a-zA-Z0-9-]*)\\\"").matcher(response);
        assertTrue(matcher.find());
        guid = matcher.group(1);

        //Retrieve
        assertEquals("{\"results\":[{\"columns\":[\"id(n)\"],\"data\":[{\"row\":[1],\"meta\":[null]}]}],\"errors\":[]}", findNodeByGuid("UID2", guid));
    }
    
    private String findNodeByGuid(String moduleId, String guid) {
        return httpClient.executeCypher(baseNeoUrl(), "CALL ga.guid.nd.findNode('" + moduleId + "','" + guid + "') YIELD node as n return id(n)");
    }

}
