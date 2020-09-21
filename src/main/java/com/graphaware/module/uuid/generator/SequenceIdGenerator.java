package com.graphaware.module.uuid.generator;

import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Label;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.ResourceIterator;
import org.neo4j.graphdb.Transaction;
import org.neo4j.graphdb.TransactionFailureException;

import com.graphaware.common.uuid.UuidGenerator;
import com.graphaware.module.uuid.GraphDatabaseServiceAware;

/**
 * An UuidGenerator implementation that makes use of the provided GraphDatabaseService to generate a numerical id sequence.
 */
public class SequenceIdGenerator implements UuidGenerator, GraphDatabaseServiceAware {

	protected GraphDatabaseService database;
	
	protected long initialSequenceValue = 0;
	protected String label = "SequenceMetadata";	
	protected String sequencePropertyKey = "sequence";
	
	@Override
	public String generateUuid() {

		String result = null;		
		Label sequenceMetadataLabel = Label.label(label);

		try (Transaction tx = database.beginTx()) {

			Node sequenceMetadataNode = null;
			ResourceIterator<Node> sequenceMetadataNodes = tx.findNodes(sequenceMetadataLabel);
			
			int count = 0;
			while (sequenceMetadataNodes.hasNext()) {
				sequenceMetadataNode = sequenceMetadataNodes.next();
				count++;
			}
			
			if (count == 0) {				
				sequenceMetadataNode = tx.createNode(sequenceMetadataLabel);
				sequenceMetadataNode.setProperty(sequencePropertyKey, initialSequenceValue);
			} else if (count > 1) {
				throw new TransactionFailureException("More than one SequenceMetadata node was found, this undoubtedly is a critical issue with the state of the database"); 
			}

			// Acquiring the write lock is critical to ensure thread safety
			tx.acquireWriteLock(sequenceMetadataNode);
			
			Number sequenceNumber = (Number) sequenceMetadataNode.getProperty(sequencePropertyKey);
			long currentSequence = sequenceNumber.longValue();			
			long nextSequence = updateSequence(currentSequence);
			sequenceMetadataNode.setProperty(sequencePropertyKey, nextSequence);

            result = String.valueOf(nextSequence);
            
            tx.commit();
            
        }
		
		return result;
		
	}
	
	/**
	 * Increment the value of the sequence. 
	 * This is performed in a dedicated method to make it easy to override, should the incrementing algorithm need to be customized.  
	 * 
	 * @param currentSequence The current sequence value in the database.
	 * @return The updated sequence value to be insert into the database.
	 */
	protected long updateSequence(long currentSequence) {
		return currentSequence + 1;
	}

	@Override
	public void setGraphDatabaseService(GraphDatabaseService database) {
		this.database = database;
	}

	public String getLabel() {
		return label;
	}

	public void setLabel(String label) {
		this.label = label;
	}

	public long getInitialSequenceValue() {
		return initialSequenceValue;
	}

	public void setInitialSequenceValue(long initialSequenceValue) {
		this.initialSequenceValue = initialSequenceValue;
	}

	public String getSequencePropertyKey() {
		return sequencePropertyKey;
	}

	public void setSequencePropertyKey(String sequencePropertyKey) {
		this.sequencePropertyKey = sequencePropertyKey;
	}
	
}