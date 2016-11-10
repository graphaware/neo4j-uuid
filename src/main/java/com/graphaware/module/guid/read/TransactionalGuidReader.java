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

import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Transaction;

public class TransactionalGuidReader implements GuidReader {

    private final GraphDatabaseService database;
    private final GuidReader delegate;

    public TransactionalGuidReader(GraphDatabaseService database, GuidReader delegate) {
        this.database = database;
        this.delegate = delegate;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public long getNodeIdByGuid(String guid) {
        long result;

        try (Transaction tx = database.beginTx()) {
            result = delegate.getNodeIdByGuid(guid);
            tx.success();
        }

        return result;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public long getRelationshipIdByGuid(String guid) {
        long result;

        try (Transaction tx = database.beginTx()) {
            result = delegate.getRelationshipIdByGuid(guid);
            tx.success();
        }

        return result;
    }
}
