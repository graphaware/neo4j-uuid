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

import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.PropertyContainer;
import org.neo4j.graphdb.Relationship;

/**
 * Indexer for nodes and relationships assigned a GUID.
 */
public interface GuidIndexer {

    /**
     * Index a property container based on the GUID property.
     *
     * @param propertyContainer the property container to index.
     */
    default void index(PropertyContainer propertyContainer) {
        if (propertyContainer instanceof Node) {
            indexNode((Node) propertyContainer);
        } else {
            indexRelationship((Relationship) propertyContainer);
        }
    }

    /**
     * Index a node based on the GUID property
     *
     * @param node the node to index
     */
    void indexNode(Node node);

    /**
     * Remove a node from the index based on the GUID property
     *
     * @param node the node
     */
    void deleteNodeFromIndex(Node node);

    /**
     * Find a node given its GUID
     *
     * @param guid the guid
     * @return the Node with the given GUID or null
     */
    Node getNodeByGuid(String guid);

    /**
     * Index a relationship based on the GUID property
     *
     * @param relationship the relationship to index
     */
    void indexRelationship(Relationship relationship);

    /**
     * Remove a relationship from the index based on the GUID property
     *
     * @param relationship the relationship
     */
    void deleteRelationshipFromIndex(Relationship relationship);

    /**
     * Find a node given its GUID
     *
     * @param guid the guid
     * @return the Relationship with the given GUID or null
     */
    Relationship getRelationshipByGuid(String guid);

}
