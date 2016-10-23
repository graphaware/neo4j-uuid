package com.graphaware.module.uuid.generator;

import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Result;

import com.graphaware.common.uuid.UuidGenerator;
import com.graphaware.module.GraphDatabaseServiceAware;

/**
 * A complex implementation of UuidGenerator that makes use of the provided GraphDatabaseService to 
 * generate a numerically increasing sequence through Cypher.
 */
public class SequenceIdGenerator implements UuidGenerator, GraphDatabaseServiceAware {

	public static final String cypher = "MERGE (sequenceMetadata:SequenceMetadata) set sequenceMetadata.sequence = coalesce(sequenceMetadata.sequence, 0) + 1 return sequenceMetadata.sequence";
	
	protected GraphDatabaseService database;
	
	@Override
	public String generateUuid() {
		
		Result result = database.execute(cypher);
		
		long sequence = (long) result.next().values().iterator().next();
		return String.valueOf(sequence);
		
	}

	@Override
	public void setGraphDatabaseService(GraphDatabaseService database) {
		this.database = database;
	}
	
}
