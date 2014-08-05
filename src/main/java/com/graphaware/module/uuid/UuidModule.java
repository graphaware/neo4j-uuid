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

import com.graphaware.runtime.module.BaseTxDrivenModule;
import com.graphaware.runtime.module.DeliberateTransactionRollbackException;
import com.graphaware.tx.event.improved.api.Change;
import com.graphaware.tx.event.improved.api.ImprovedTransactionData;
import com.graphaware.tx.executor.batch.IterableInputBatchTransactionExecutor;
import com.graphaware.tx.executor.batch.UnitOfWork;
import com.graphaware.tx.executor.single.TransactionCallback;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.tooling.GlobalGraphOperations;

/**
 * {@link com.graphaware.runtime.module.TxDrivenModule} that assigns UUID's to nodes in the graph.
 */
public class UuidModule extends BaseTxDrivenModule<Void> {

    private UuidGenerator uuidGenerator;

    private final static int BATCH_SIZE = 1000;

    public UuidModule(String moduleId, GraphDatabaseService database) {
        super(moduleId);
        this.uuidGenerator = new EaioUuidGenerator();
    }


    @Override
    public void initialize(GraphDatabaseService database) {


        new IterableInputBatchTransactionExecutor<>(
                database,
                BATCH_SIZE,
                new TransactionCallback<Iterable<Node>>() {
                    @Override
                    public Iterable<Node> doInTransaction(GraphDatabaseService database) throws Exception {
                        return GlobalGraphOperations.at(database).getAllNodes();
                    }
                },
                new UnitOfWork<Node>() {
                    @Override
                    public void execute(GraphDatabaseService database, Node node, int batchNumber, int stepNumber) {
                        if (!node.hasProperty(Properties.UUID)) {
                            assignUuid(node);
                        }
                    }
                }
        ).execute();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Void beforeCommit(ImprovedTransactionData transactionData) throws DeliberateTransactionRollbackException {

        //Set the UUID on all created nodes
        for (Node node : transactionData.getAllCreatedNodes()) {
            assignUuid(node);
        }

        //Check if the UUID has been modified or removed from the node and throw an error
        for (Change<Node> change : transactionData.getAllChangedNodes()) {

            if(!change.getCurrent().hasProperty(Properties.UUID)) {
                throw new DeliberateTransactionRollbackException("You are not allowed to remove the " + Properties.UUID + " property");
            }

            if(!change.getPrevious().getProperty(Properties.UUID).equals(change.getCurrent().getProperty(Properties.UUID))) {
                throw new DeliberateTransactionRollbackException("You are not allowed to modify the " + Properties.UUID + " property");
            }

        }

        return null;
    }

    private void assignUuid(Node node) {
        String uuid = uuidGenerator.generateUuid();
        node.setProperty(Properties.UUID, uuid);
    }


}
