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

package ga.guid;

import com.graphaware.module.guid.GuidModule;
import com.graphaware.module.guid.read.DefaultGuidReader;
import com.graphaware.module.guid.read.GuidReader;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;
import org.neo4j.procedure.Context;

import static com.graphaware.runtime.RuntimeRegistry.getStartedRuntime;

public abstract class GuidProcedure {

    @Context
    public GraphDatabaseService database;

    protected GuidReader reader(String moduleId) {
        //note: this can't be cached, needs new instance every time
        return new DefaultGuidReader(getStartedRuntime(database).getModule(moduleId, GuidModule.class).getConfiguration(), database);
    }

    protected Node findNodeByGuid(String moduleId, String guid) {
        return database.getNodeById(reader(moduleId).getNodeIdByGuid(guid));
    }

    protected Relationship findRelationshipByGuid(String moduleId, String guid) {
        return database.getRelationshipById(reader(moduleId).getRelationshipIdByGuid(guid));
    }
}
