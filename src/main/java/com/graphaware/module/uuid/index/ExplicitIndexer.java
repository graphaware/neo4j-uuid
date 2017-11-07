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

package com.graphaware.module.uuid.index;

import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;
import org.neo4j.kernel.api.ExplicitIndexHits;
import org.neo4j.kernel.api.ReadOperations;
import org.neo4j.kernel.api.exceptions.explicitindex.ExplicitIndexNotFoundKernelException;
import org.neo4j.kernel.impl.core.ThreadToStatementContextBridge;
import org.neo4j.kernel.internal.GraphDatabaseAPI;
import org.neo4j.logging.Log;

import com.graphaware.common.log.LoggerFactory;
import com.graphaware.module.uuid.UuidConfiguration;

/**
 * Explicit Index implementation for indexing and finding nodes and relationships assigned a UUID.
 */
public class ExplicitIndexer implements UuidIndexer {

    private static final Log LOG = LoggerFactory.getLogger(ExplicitIndexer.class);

    private final GraphDatabaseService database;
    private final UuidConfiguration configuration;
    private ThreadToStatementContextBridge statementContext;

    public ExplicitIndexer(GraphDatabaseService database, UuidConfiguration configuration) {
        this.database = database;
        this.configuration = configuration;
        statementContext = ((GraphDatabaseAPI) database).getDependencyResolver().resolveDependency(ThreadToStatementContextBridge.class);
    }

    /**
     * @inheritDoc
     */
    @Override
    public void indexNode(Node node) {
        database.index().forNodes(configuration.getUuidIndex()).add(node, configuration.getUuidProperty(), node.getProperty(configuration.getUuidProperty()));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Node getNodeByUuid(String uuid) {
        ReadOperations readOperations = statementContext.get().readOperations();
        try (ExplicitIndexHits get = readOperations.nodeExplicitIndexGet(configuration.getUuidIndex(), configuration.getUuidProperty(), uuid)) {
            if (get.hasNext()) {
                long idNode = get.next();
                return database.getNodeById(idNode);
            }
        } catch (ExplicitIndexNotFoundKernelException e) {
            LOG.warn("Explicit node index " + configuration.getUuidIndex() + " does not yet exist.", e);
        }
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void deleteNodeFromIndex(Node node) {
        database.index().forNodes(configuration.getUuidIndex()).remove(node, configuration.getUuidProperty());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void indexRelationship(Relationship relationship) {
        database.index().forRelationships(configuration.getUuidRelationshipIndex()).add(relationship, configuration.getUuidProperty(), relationship.getProperty(configuration.getUuidProperty()));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void deleteRelationshipFromIndex(Relationship relationship) {
        database.index().forRelationships(configuration.getUuidRelationshipIndex()).remove(relationship, configuration.getUuidProperty());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Relationship getRelationshipByUuid(String uuid) {
        ReadOperations readOperations = statementContext.get().readOperations();
        try (ExplicitIndexHits get = readOperations.relationshipExplicitIndexGet(configuration.getUuidRelationshipIndex(), configuration.getUuidProperty(), uuid, -1L, -1L)) {
            if (get.hasNext()) {
                long idRel = get.next();
                return database.getRelationshipById(idRel);
            }
        } catch (ExplicitIndexNotFoundKernelException e) {
            LOG.warn("Explicit relationship index " + configuration.getUuidRelationshipIndex() + " does not yet exist.", e);
        }
        return null;
    }

}
