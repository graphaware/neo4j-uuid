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

import com.graphaware.module.uuid.UuidModule;
import ga.uuid.result.RelationshipListResult;
import ga.uuid.result.RelationshipResult;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Relationship;
import org.neo4j.procedure.Context;
import org.neo4j.procedure.Name;
import org.neo4j.procedure.PerformsWrites;
import org.neo4j.procedure.Procedure;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

public class RelationshipUuidProcedure extends UuidProcedure {

    @Context
    public GraphDatabaseService database;

    @Override
    protected GraphDatabaseService getDatabase() {
        return database;
    }

    @Procedure
    @PerformsWrites
    public Stream<RelationshipResult> findRelationship(@Name("uuid") String uuid) {
        return Stream.of(new RelationshipResult(findRelationshipByUuid(UuidModule.DEFAULT_MODULE_ID, uuid)));
    }

    @Procedure
    @PerformsWrites
    public Stream<RelationshipListResult> findRelationships(@Name("uuids") List<String> uuids) {
        List<Relationship> relationships = new ArrayList<>();
        for (String uuid : uuids) {
            relationships.add(findRelationshipByUuid(UuidModule.DEFAULT_MODULE_ID, uuid));
        }

        return Stream.of(new RelationshipListResult(relationships));
    }
}
