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

package ga.uuid;

import com.graphaware.module.uuid.UuidConfiguration;
import com.graphaware.module.uuid.UuidModule;
import com.graphaware.module.uuid.UuidReader;
import com.graphaware.runtime.GraphAwareRuntime;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.procedure.Context;
import org.neo4j.procedure.Name;
import org.neo4j.procedure.PerformsWrites;
import org.neo4j.procedure.Procedure;

import java.util.Collections;
import java.util.stream.Stream;

import static com.graphaware.runtime.RuntimeRegistry.getStartedRuntime;

public class UuidProcedures {

    @Context
    public GraphDatabaseService database;

    @Procedure
    public Stream<NodeResult> findNode(@Name("uuid") String uuid) {
        GraphAwareRuntime runtime = getStartedRuntime(database);
        UuidModule module = runtime.getModule(UuidModule.DEFAULT_MODULE_ID, UuidModule.class);
        UuidConfiguration configuration = module.getConfiguration();
        Node nodeById = database.getNodeById(new UuidReader(configuration, database).getNodeIdByUuid(uuid));
        
        return Stream.of(new NodeResult(nodeById));
    }
}
