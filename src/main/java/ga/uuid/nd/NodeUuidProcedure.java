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

package ga.uuid.nd;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.neo4j.graphdb.Node;
import org.neo4j.procedure.Mode;
import org.neo4j.procedure.Name;
import org.neo4j.procedure.Procedure;

import ga.uuid.UuidProcedure;
import ga.uuid.result.NodeListResult;
import ga.uuid.result.NodeResult;

public class NodeUuidProcedure extends UuidProcedure {

	@Procedure(mode = Mode.READ)
    public Stream<NodeResult> findNode(@Name("moduleId") String moduleId, @Name("uuid") String uuid) {
        return Stream.of(new NodeResult(findNodeByUuid(moduleId, uuid)));
    }

    @Procedure(mode = Mode.READ)
    public Stream<NodeListResult> findNodes(@Name("moduleId") String moduleId, @Name("uuids") List<String> uuids) {
        List<Node> nodes = uuids.stream().map(uuid -> findNodeByUuid(moduleId, uuid)).collect(Collectors.toList());

        return Stream.of(new NodeListResult(nodes));
    }
}
