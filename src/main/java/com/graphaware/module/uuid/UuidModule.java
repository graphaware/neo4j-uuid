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
package com.graphaware.module.uuid;

import com.graphaware.common.util.Change;
import com.graphaware.common.uuid.UuidGenerator;
import com.graphaware.module.uuid.index.ExplicitIndexer;
import com.graphaware.module.uuid.index.UuidIndexer;
import com.graphaware.runtime.module.BaseModule;
import com.graphaware.runtime.module.DeliberateTransactionRollbackException;
import com.graphaware.tx.event.improved.api.ImprovedTransactionData;
import org.neo4j.graphdb.Entity;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;
import org.springframework.util.ClassUtils;

import java.util.Collection;

/**
 * {@link com.graphaware.runtime.module.Module} that assigns UUID's to nodes in the graph.
 */
public class UuidModule extends BaseModule<Void> {

    public static final String DEFAULT_MODULE_ID = "UIDM";

    private final UuidGenerator uuidGenerator;
    private final UuidConfiguration uuidConfiguration;
    private final UuidIndexer uuidIndexer;

    /**
     * Construct a new UUID module.
     *
     * @param moduleId ID of the module.
     */
    public UuidModule(String moduleId, UuidConfiguration configuration, GraphDatabaseService database) {
        super(moduleId);
        this.uuidConfiguration = configuration;
        this.uuidGenerator = instantiateUuidGenerator(configuration, database);
        this.uuidIndexer = new ExplicitIndexer(database, configuration);
    }

    protected UuidGenerator instantiateUuidGenerator(UuidConfiguration uuidConfiguration, GraphDatabaseService database) {

        String uuidGeneratorClassString = uuidConfiguration.getUuidGenerator();

        try {

            // Instantiate the configured/supplied class
            @SuppressWarnings("unchecked")
            Class<UuidGenerator> uuidGeneratorClass = (Class<UuidGenerator>) ClassUtils.forName(uuidGeneratorClassString, getClass().getClassLoader());
            UuidGenerator uuidGenerator = uuidGeneratorClass.newInstance();

            // Provide the GraphDatabaseService to those that wish to make use of it
            if (uuidGenerator instanceof GraphDatabaseServiceAware) {
                ((GraphDatabaseServiceAware) uuidGenerator).setGraphDatabaseService(database);
            }

            return uuidGenerator;

        } catch (Exception e) {
            throw new RuntimeException("Unable to instantiate UuidGenerator of type '" + uuidGeneratorClassString + "'", e);
        }

    }

    public UuidGenerator getUuidGenerator() {
        return uuidGenerator;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public UuidConfiguration getConfiguration() {
        return uuidConfiguration;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Void beforeCommit(ImprovedTransactionData transactionData) throws DeliberateTransactionRollbackException {
        processEntities(transactionData.getAllCreatedNodes(), transactionData.getAllDeletedNodes(), transactionData.getAllChangedNodes());
        processEntities(transactionData.getAllCreatedRelationships(), transactionData.getAllDeletedRelationships(), transactionData.getAllChangedRelationships());

        return null;
    }

    private <E extends Entity> void processEntities(Collection<E> created, Collection<E> deleted, Collection<Change<E>> updated) {

        for (E entity : created) {
            assignUuid(entity);
        }

        for (E entity : deleted) {
            removeUuid(entity);
        }

        for (Change<E> change : updated) {
            if (uuidHasBeenRemoved(change)) {
                if (isImmutable()) {
                    throw new DeliberateTransactionRollbackException("You are not allowed to remove the " + uuidConfiguration.getUuidProperty() + " property");
                } else {
                    removeUuid(change.getCurrent());
                    continue;
                }
            }

            if (uuidHasChanged(change)) {
                if (isImmutable()) {
                    throw new DeliberateTransactionRollbackException("You are not allowed to modify the " + uuidConfiguration.getUuidProperty() + " property");
                } else {
                    removeUuid(change.getCurrent());
                    assignUuid(change.getCurrent());
                    continue;
                }
            }

            if (!hadUuid(change) && hasNoUuid(change)) {
                assignUuid(change.getCurrent());
            }
        }
    }

    private void removeUuid(Entity entity) {
        if (entity instanceof Node) {
            uuidIndexer.deleteNodeFromIndex((Node) entity);
        } else {
            uuidIndexer.deleteRelationshipFromIndex((Relationship) entity);
        }
    }

    private void assignUuid(Entity entity) {
        String uuidProperty = uuidConfiguration.getUuidProperty();

        if (!entity.hasProperty(uuidProperty)) {
            assignNewUuid(entity, uuidProperty);
        } else {
            handleExistingUuid(entity, uuidProperty);
        }

        uuidIndexer.index(entity);
    }

    private void assignNewUuid(Entity entity, String uuidProperty) {
        String uuid = uuidGenerator.generateUuid();

        if (uuidConfiguration.shouldStripHyphens()) {
            uuid = uuid.replaceAll("-", "");
        }

        entity.setProperty(uuidProperty, uuid);
    }

    private void handleExistingUuid(Entity entity, String uuidProperty) {
        Entity existingEntity;

        if (entity instanceof Node) {
            existingEntity = uuidIndexer.getNodeByUuid(entity.getProperty(uuidProperty).toString());
        } else {
            existingEntity = uuidIndexer.getRelationshipByUuid(entity.getProperty(uuidProperty).toString());
        }

        if (existingEntity != null && (existingEntity.getId() != entity.getId())) {
            throw new DeliberateTransactionRollbackException("Another " + existingEntity.getClass().getName() + " with UUID " + entity.getProperty(uuidProperty).toString() + " already exists (#" + existingEntity.getId() + ")!");
        }
    }

    private boolean isImmutable() {
        return uuidConfiguration.getImmutable();
    }

    private boolean uuidHasBeenRemoved(Change<? extends Entity> changed) {
        return hadUuid(changed) && hasNoUuid(changed);
    }

    private boolean uuidHasChanged(Change<? extends Entity> change) {
        return hadUuid(change) && (!change.getPrevious().getProperty(uuidConfiguration.getUuidProperty()).equals(change.getCurrent().getProperty(uuidConfiguration.getUuidProperty())));
    }

    private boolean hasUuid(Entity entity) {
        return entity.hasProperty(uuidConfiguration.getUuidProperty());
    }

    private boolean hasNoUuid(Change<? extends Entity> change) {
        return !hasUuid(change.getCurrent());
    }

    private boolean hadUuid(Change<? extends Entity> change) {
        return hasUuid(change.getPrevious());
    }
}