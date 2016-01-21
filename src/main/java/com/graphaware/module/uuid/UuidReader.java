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


import com.graphaware.module.uuid.index.LegacyIndexer;
import com.graphaware.module.uuid.index.UuidIndexer;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.NotFoundException;
import org.neo4j.graphdb.Relationship;
import org.neo4j.graphdb.Transaction;

public class UuidReader {

    private final UuidIndexer indexer;
    private final GraphDatabaseService database;

    public UuidReader(UuidConfiguration configuration, GraphDatabaseService database) {
        this.database = database;
        this.indexer = new LegacyIndexer(database, configuration);
    }

    /**
     * Get a node by its UUID.
     *
     * @param uuid uuid.
     * @return Node object.
     * @throws org.neo4j.graphdb.NotFoundException in case no node exists with such UUID.
     */
    public Node getNodeByUuid(String uuid) {
        Node node;

        try (Transaction tx = database.beginTx()) {
            node = indexer.getNodeByUuid(uuid);
            tx.success();
        }

        if (node == null) {
            throw new NotFoundException("Node with UUID " + uuid + " does not exist");
        }

        return node;
    }

    /**
     * Get a node ID by its UUID.
     *
     * @param uuid uuid.
     * @return Node ID.
     * @throws org.neo4j.graphdb.NotFoundException in case no node exists with such UUID.
     */
    public long getNodeIdByUuid(String uuid) {
        Node node = getNodeByUuid(uuid);
        long nodeId;

        try (Transaction tx = database.beginTx()) {
            nodeId = node.getId();
            tx.success();
        }

        return nodeId;
    }

    /**
     * Get a relationship by its UUID.
     *
     * @param uuid uuid.
     * @return Relationship object.
     * @throws org.neo4j.graphdb.NotFoundException in case no relationship exists with such UUID.
     */
    public Relationship getRelationshipByUuid(String uuid) {
        Relationship relationship;

        try (Transaction tx = database.beginTx()) {
            relationship = indexer.getRelationshipByUuid(uuid);
            tx.success();
        }

        if (relationship == null) {
            throw new NotFoundException("Relationship with UUID " + uuid + " does not exist");
        }

        return relationship;
    }

    /**
     * Get a relationship ID by its UUID.
     *
     * @param uuid uuid.
     * @return Relationship ID.
     * @throws org.neo4j.graphdb.NotFoundException in case no node exists with such UUID.
     */
    public long getRelationshipIdByUuid(String uuid) {
        Relationship relationship = getRelationshipByUuid(uuid);
        long relId;

        try (Transaction tx = database.beginTx()) {
            relId = relationship.getId();
            tx.success();
        }

        return relId;
    }
}
