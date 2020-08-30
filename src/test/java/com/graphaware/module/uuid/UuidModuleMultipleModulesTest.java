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
import org.junit.jupiter.api.Test;
import org.neo4j.graphdb.Result;
import org.neo4j.harness.TestServerBuilder;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class UuidModuleMultipleModulesTest extends DatabaseIntegrationTest {

    @Override
    protected String configFile() {
        return "neo4j-uuid-multiple.conf";
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
    public void testProcedures() {
        //Create & Assign
        String cid = singleValue(getDatabase().execute("CREATE (c:Customer {name:'c1'}) RETURN id(c)"));
        String uid = singleValue(getDatabase().execute("CREATE (u:User {name:'u1'}) RETURN id(u)"));

        getDatabase().execute("CREATE (:SomethingElse {name:'s1'})");

        String uuid = singleValue(getDatabase().execute("MATCH (c:Customer) RETURN c.customerId"));

        //Retrieve
        assertEquals(cid, findNodeByUuid("UID1", uuid));

        uuid = singleValue(getDatabase().execute("MATCH (u:User) RETURN u.userId"));

        //Retrieve
        assertEquals(uid, findNodeByUuid("UID2", uuid));
    }

    private String findNodeByUuid(String moduleId, String uuid) {
        return singleValue(getDatabase().execute("RETURN id(ga.uuid.nd.findNode('" + moduleId + "','" + uuid + "')) as id"));
    }

    private String singleValue(Result result) {
        return result.next().values().iterator().next().toString();
    }
}
