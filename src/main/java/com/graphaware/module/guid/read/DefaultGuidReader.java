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

package com.graphaware.module.guid.read;


import com.graphaware.module.guid.GuidConfiguration;
import com.graphaware.module.guid.index.LegacyIndexer;
import com.graphaware.module.guid.index.GuidIndexer;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.NotFoundException;
import org.neo4j.graphdb.Relationship;

public class DefaultGuidReader implements GuidReader {

    private final GuidIndexer indexer;

    public DefaultGuidReader(GuidConfiguration configuration, GraphDatabaseService database) {
        this.indexer = new LegacyIndexer(database, configuration);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public long getNodeIdByGuid(Object guid) {
        Node node = indexer.getNodeByGuid(guid);

        if (node == null) {
            throw new NotFoundException("Node with GUID " + guid + " does not exist");
        }

        return node.getId();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public long getRelationshipIdByGuid(Object guid) {
        Relationship relationship = indexer.getRelationshipByGuid(guid);

        if (relationship == null) {
            throw new NotFoundException("Relationship with GUID " + guid + " does not exist");
        }

        return relationship.getId();
    }
}
