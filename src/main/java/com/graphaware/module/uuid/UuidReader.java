/*
 * Copyright (c) 2014 GraphAware
 *
 * This file is part of GraphAware.
 *
 * GraphAware is free software: you can redistribute it and/or modify it under the terms of
 * the GNU General Public License as published by the Free Software Foundation, either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 *  without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details. You should have received a copy of
 * the GNU General Public License along with this program.  If not, see
 * <http://www.gnu.org/licenses/>.
 */

package com.graphaware.module.uuid;


import com.graphaware.module.uuid.index.LegacyIndexer;
import com.graphaware.module.uuid.index.UuidIndexer;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Transaction;

public class UuidReader {

    private final UuidIndexer indexer;
    private final GraphDatabaseService database;

    public UuidReader(UuidConfiguration configuration, GraphDatabaseService database) {
        this.database = database;
        this.indexer = new LegacyIndexer(database,configuration);
    }

    public Node getNodeByUuid(String uuid) {
        Node node;
        try(Transaction tx = database.beginTx()) {
            node = indexer.getNodeByUuid(uuid);
            tx.success();
        }
        return node;
    }

    public Long getNodeIdByUuid(String uuid) {
        Node node = getNodeByUuid(uuid);
        Long nodeId=null;
        try(Transaction tx = database.beginTx()) {
            if (node != null) {
                nodeId =  node.getId();
            }
            tx.success();
        }
        return nodeId;
    }
}
