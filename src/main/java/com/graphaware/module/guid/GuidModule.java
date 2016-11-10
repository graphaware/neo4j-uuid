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
package com.graphaware.module.guid;

import static com.graphaware.common.util.PropertyContainerUtils.id;

import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.PropertyContainer;
import org.neo4j.graphdb.Relationship;
import org.springframework.util.ClassUtils;

import com.graphaware.common.util.Change;
import com.graphaware.module.guid.generator.GuidGenerator;
import com.graphaware.module.guid.index.LegacyIndexer;
import com.graphaware.module.guid.index.GuidIndexer;
import com.graphaware.runtime.module.BaseTxDrivenModule;
import com.graphaware.runtime.module.DeliberateTransactionRollbackException;
import com.graphaware.tx.event.improved.api.ImprovedTransactionData;
import com.graphaware.tx.executor.batch.IterableInputBatchTransactionExecutor;
import com.graphaware.tx.executor.input.AllNodes;
import com.graphaware.tx.executor.input.AllRelationships;

/**
 * {@link com.graphaware.runtime.module.TxDrivenModule} that assigns GUID's to nodes in the graph.
 */
public class GuidModule extends BaseTxDrivenModule<Void> {

    public static final String DEFAULT_MODULE_ID = "UIDM";
    private static final int BATCH_SIZE = 1000;

    private final GuidGenerator GuidGenerator;
    private final GuidConfiguration guidConfiguration;
    private final GuidIndexer guidIndexer;

    /**
     * Construct a new GUID module.
     *
     * @param moduleId ID of the module.
     */
    public GuidModule(String moduleId, GuidConfiguration configuration, GraphDatabaseService database) {
        super(moduleId);        
        this.guidConfiguration = configuration;
        this.GuidGenerator = instantiateGuidGenerator(configuration, database);
        this.guidIndexer = new LegacyIndexer(database, configuration);
    }

    protected GuidGenerator instantiateGuidGenerator(GuidConfiguration guidConfiguration, GraphDatabaseService database) {
    
    	String GuidGeneratorClassString = guidConfiguration.getGuidGenerator();
    	
    	try {
    		
    		// Instantiate the configured/supplied class
    		@SuppressWarnings("unchecked")
			Class<GuidGenerator> GuidGeneratorClass = (Class<GuidGenerator>) ClassUtils.forName(GuidGeneratorClassString,  getClass().getClassLoader());
    		GuidGenerator GuidGenerator = GuidGeneratorClass.newInstance();
    		
    		// Provide the GraphDatabaseService to those that wish to make use of it
    		if (GuidGenerator instanceof GraphDatabaseServiceAware) {
    			((GraphDatabaseServiceAware) GuidGenerator).setGraphDatabaseService(database);
    		}
    		
    		return GuidGenerator;
    		
    	} catch (Exception e) {
    		throw new RuntimeException("Unable to instantiate GuidGenerator of type '" + GuidGeneratorClassString + "'", e);
    	}
    	
    }

    public GuidGenerator getGuidGenerator() {
		return GuidGenerator;
	}

	/**
     * {@inheritDoc}
     */
    @Override
    public GuidConfiguration getConfiguration() {
        return guidConfiguration;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void initialize(GraphDatabaseService database) {
        new IterableInputBatchTransactionExecutor<>(
                database,
                BATCH_SIZE,
                new AllNodes(database, BATCH_SIZE),
                (db, node, batchNumber, stepNumber) -> {
                    if (getConfiguration().getInclusionPolicies().getNodeInclusionPolicy().include(node)) {
                        assignGuid(node);
                    }
                }
        ).execute();

        new IterableInputBatchTransactionExecutor<>(
                database,
                BATCH_SIZE,
                new AllRelationships(database, BATCH_SIZE),
                (db, rel, batchNumber, stepNumber) -> {
                    if (getConfiguration().getInclusionPolicies().getRelationshipInclusionPolicy().include(rel)) {
                        assignGuid(rel);
                    }
                }
        ).execute();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Void beforeCommit(ImprovedTransactionData transactionData) throws DeliberateTransactionRollbackException {

        //Set the GUID on all created nodes
        for (Node node : transactionData.getAllCreatedNodes()) {
            assignGuid(node);
        }

        for (Node node : transactionData.getAllDeletedNodes()) {
            guidIndexer.deleteNodeFromIndex(node);
        }

        //Check if the GUID has been modified or removed from the node and throw an error
        for (Change<Node> change : transactionData.getAllChangedNodes()) {
            if (!change.getCurrent().hasProperty(guidConfiguration.getGuidProperty())) {
                throw new DeliberateTransactionRollbackException("You are not allowed to remove the " + guidConfiguration.getGuidProperty() + " property");
            }

            if (!change.getPrevious().getProperty(guidConfiguration.getGuidProperty()).equals(change.getCurrent().getProperty(guidConfiguration.getGuidProperty()))) {
                throw new DeliberateTransactionRollbackException("You are not allowed to modify the " + guidConfiguration.getGuidProperty() + " property");
            }
        }

        //Set the GUID on all created relationships
        for (Relationship relationship : transactionData.getAllCreatedRelationships()) {
            assignGuid(relationship);
        }

        for (Relationship rel : transactionData.getAllDeletedRelationships()) {
            guidIndexer.deleteRelationshipFromIndex(rel);
        }

        //Check if the GUID has been modified or removed from the relationship and throw an error
        for (Change<Relationship> change : transactionData.getAllChangedRelationships()) {
            if (!change.getCurrent().hasProperty(guidConfiguration.getGuidProperty())) {
                throw new DeliberateTransactionRollbackException("You are not allowed to remove the " + guidConfiguration.getGuidProperty() + " property");
            }

            if (!change.getPrevious().getProperty(guidConfiguration.getGuidProperty()).equals(change.getCurrent().getProperty(guidConfiguration.getGuidProperty()))) {
                throw new DeliberateTransactionRollbackException("You are not allowed to modify the " + guidConfiguration.getGuidProperty() + " property");
            }
        }

        return null;
    }

    protected void assignGuid(PropertyContainer propertyContainer) {
        String guidProperty = guidConfiguration.getGuidProperty();

        if (!propertyContainer.hasProperty(guidProperty)) {
            assignNewGuid(propertyContainer, guidProperty);
        } else {
            handleExistingGuid(propertyContainer, guidProperty);
        }

        guidIndexer.index(propertyContainer);
    }

    private void assignNewGuid(PropertyContainer propertyContainer, String guidProperty) {
    	
        Object guid = GuidGenerator.generateGuid();

        if (guid instanceof String && guidConfiguration.shouldStripHyphens()) {
            guid = ((String) guid).replaceAll("-", "");
        }

        propertyContainer.setProperty(guidProperty, guid);
    }

    private void handleExistingGuid(PropertyContainer propertyContainer, String guidProperty) {
        PropertyContainer existingPc;

        if (propertyContainer instanceof Node) {
            existingPc = guidIndexer.getNodeByGuid(propertyContainer.getProperty(guidProperty).toString());
        } else {
            existingPc = guidIndexer.getRelationshipByGuid(propertyContainer.getProperty(guidProperty).toString());
        }

        if (existingPc != null && (id(existingPc) != id(propertyContainer))) {
            throw new DeliberateTransactionRollbackException("Another " + existingPc.getClass().getName() + " with GUID " + propertyContainer.getProperty(guidProperty).toString() + " already exists (#" + id(existingPc) + ")!");
        }
    }

}