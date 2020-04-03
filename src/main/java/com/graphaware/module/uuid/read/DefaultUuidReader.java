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

package com.graphaware.module.uuid.read;


import com.graphaware.module.uuid.UuidConfiguration;
import com.graphaware.module.uuid.index.ExplicitIndexer;
import com.graphaware.module.uuid.index.UuidIndexer;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.NotFoundException;
import org.neo4j.graphdb.Relationship;

public class DefaultUuidReader implements UuidReader {

    private final UuidIndexer indexer;

    public DefaultUuidReader(UuidConfiguration configuration, GraphDatabaseService database) {
        this.indexer = new ExplicitIndexer(database, configuration);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public long getNodeIdByUuid(String uuid) {
        Node node = indexer.getNodeByUuid(uuid);

        if (node == null) {
            throw new NotFoundException("Node with UUID " + uuid + " does not exist");
        }

        return node.getId();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public long getRelationshipIdByUuid(String uuid) {
        Relationship relationship = indexer.getRelationshipByUuid(uuid);

        if (relationship == null) {
            throw new NotFoundException("Relationship with UUID " + uuid + " does not exist");
        }

        return relationship.getId();
    }
}
