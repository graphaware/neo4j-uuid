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

import static org.apache.http.HttpStatus.SC_OK;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class UuidModuleMultipleModulesTest extends NeoServerIntegrationTest {

    @Override
    protected String neo4jConfigFile() {
        return "neo4j-uuid-multiple.properties";
    }

    @Test
    public void testWorkflow() {
        //Create & Assign
        httpClient.executeCypher(baseUrl(), "CREATE (:Customer {name:'c1'})");
        httpClient.executeCypher(baseUrl(), "CREATE (:User {name:'u1'})");
        httpClient.executeCypher(baseUrl(), "CREATE (:SomethingElse {name:'s1'})");

        String response = httpClient.executeCypher(baseUrl(), "MATCH (c:Customer) RETURN c");

        Matcher matcher = Pattern.compile("\\\"customerId\\\":\\\"([a-zA-Z0-9-]*)\\\"").matcher(response);
        assertTrue(matcher.find());
        String uuid = matcher.group(1);

        //Retrieve
        assertEquals("0", httpClient.get(baseUrl() + "/graphaware/uuid/UID1/node/" + uuid, SC_OK));

        response = httpClient.executeCypher(baseUrl(), "MATCH (u:User) RETURN u");

        matcher = Pattern.compile("\\\"userId\\\":\\\"([a-zA-Z0-9-]*)\\\"").matcher(response);
        assertTrue(matcher.find());
        uuid = matcher.group(1);

        //Retrieve
        assertEquals("1", httpClient.get(baseUrl() + "/graphaware/uuid/UID2/node/" + uuid, SC_OK));
    }
}
