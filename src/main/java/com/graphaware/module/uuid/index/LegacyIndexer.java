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

package com.graphaware.module.uuid.index;

import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;
import org.neo4j.kernel.api.LegacyIndexHits;
import org.neo4j.kernel.api.ReadOperations;
import org.neo4j.kernel.api.exceptions.legacyindex.LegacyIndexNotFoundKernelException;
import org.neo4j.kernel.impl.core.ThreadToStatementContextBridge;
import org.neo4j.kernel.internal.GraphDatabaseAPI;
import org.neo4j.logging.Log;

import com.graphaware.common.log.LoggerFactory;
import com.graphaware.module.uuid.UuidConfiguration;

/**
 * Legacy Index implementation for indexing and finding nodes and relationships assigned a UUID.
 */
public class LegacyIndexer implements UuidIndexer {

	private static final Log LOG = LoggerFactory.getLogger(LegacyIndexer.class);

	/**
	 * Used by relationshipLegacyIndex to skip node-to-node searching (and use only key:value)
	 */
	private static final long NO_NODE = -1;
	
    private final GraphDatabaseService database;
    private final UuidConfiguration configuration;
	private ThreadToStatementContextBridge statementContext;

    public LegacyIndexer(GraphDatabaseService database, UuidConfiguration configuration) {
        this.database = database;
        this.configuration = configuration;
        GraphDatabaseAPI db = (GraphDatabaseAPI) database;
        statementContext = db.getDependencyResolver().resolveDependency(ThreadToStatementContextBridge.class);
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
    	// database.index().forNodes is a writing operation, so in a READ_REPLICA and FOLLOWER instances the call fails
    	ReadOperations readOperations = statementContext.get().readOperations();
    	try (LegacyIndexHits get = readOperations.nodeLegacyIndexGet(configuration.getUuidIndex(), configuration.getUuidProperty(), uuid);){
    		if(get.hasNext()){
    			long idNode = get.next();
    			return database.getNodeById(idNode);    			
    		}
		} catch (LegacyIndexNotFoundKernelException e) {
			LOG.error("getNodeByUuid("+uuid+"): "+e.getMessage(), e);
			return null;
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
		// database.index().forNodes is a writing operation, so in a
		// READ_REPLICA and FOLLOWER instances the call fails
		ReadOperations readOperations = statementContext.get().readOperations();

		try (LegacyIndexHits get = readOperations.relationshipLegacyIndexGet(configuration.getUuidRelationshipIndex(),
				configuration.getUuidProperty(), uuid, NO_NODE, NO_NODE);) {
			
			if (get.hasNext()) {
				long idRel = get.next();
				return database.getRelationshipById(idRel);
			}
			
		} catch (LegacyIndexNotFoundKernelException e) {
			LOG.error("getNodeByUuid(" + uuid + "): " + e.getMessage(), e);
			return null;
		}
		return null;
	}

}
