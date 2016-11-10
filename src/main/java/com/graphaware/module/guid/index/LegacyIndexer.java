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

package com.graphaware.module.guid.index;

import com.graphaware.module.guid.GuidConfiguration;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;

/**
 * Legacy Index implementation for indexing and finding nodes and relationships assigned a GUID.
 */
public class LegacyIndexer implements GuidIndexer {

    private final GraphDatabaseService database;
    private final GuidConfiguration configuration;

    public LegacyIndexer(GraphDatabaseService database, GuidConfiguration configuration) {
        this.database = database;
        this.configuration = configuration;
    }

    /**
     * @inheritDoc
     */
    @Override
    public void indexNode(Node node) {
        database.index().forNodes(configuration.getGuidIndex()).add(node, configuration.getGuidProperty(), node.getProperty(configuration.getGuidProperty()));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Node getNodeByGuid(String guid) {
        return database.index().forNodes(configuration.getGuidIndex()).get(configuration.getGuidProperty(), guid).getSingle();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void deleteNodeFromIndex(Node node) {
        database.index().forNodes(configuration.getGuidIndex()).remove(node, configuration.getGuidProperty());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void indexRelationship(Relationship relationship) {
        database.index().forRelationships(configuration.getGuidRelationshipIndex()).add(relationship, configuration.getGuidProperty(), relationship.getProperty(configuration.getGuidProperty()));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void deleteRelationshipFromIndex(Relationship relationship) {
        database.index().forRelationships(configuration.getGuidRelationshipIndex()).remove(relationship, configuration.getGuidProperty());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Relationship getRelationshipByGuid(String guid) {
        return database.index().forRelationships(configuration.getGuidRelationshipIndex()).get(configuration.getGuidProperty(), guid).getSingle();
    }

}
