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

package ga.uuid;

import com.graphaware.module.uuid.UuidModule;
import org.neo4j.graphdb.Node;
import org.neo4j.procedure.Name;
import org.neo4j.procedure.UserFunction;

import java.util.List;
import java.util.stream.Collectors;

public class NodeUuidFunctions extends UuidFunctions {

    @UserFunction
    public Node findNode(@Name("uuid") String uuid) {
        return findNodeByUuid(UuidModule.DEFAULT_MODULE_ID, uuid);
    }

    @UserFunction
    public List<Node> findNodes(@Name("uuids") List<String> uuids) {
        return uuids.stream().map(uuid -> findNodeByUuid(UuidModule.DEFAULT_MODULE_ID, uuid)).collect(Collectors.toList());
    }
}
