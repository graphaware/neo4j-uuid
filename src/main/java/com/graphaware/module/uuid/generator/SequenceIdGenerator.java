package com.graphaware.module.uuid.generator;

import java.util.HashMap;
import java.util.Map;

import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Result;

import com.graphaware.common.uuid.UuidGenerator;
import com.graphaware.module.uuid.GraphDatabaseServiceAware;

/**
 * A non-trivial UuidGenerator implementation that makes use of the provided GraphDatabaseService and 
 * Cypher to generate a numerically increasing sequence.
 */
public class SequenceIdGenerator implements UuidGenerator, GraphDatabaseServiceAware {

	public static final String cypher = "MERGE (sequenceMetadata:SequenceMetadata) set sequenceMetadata.sequence = coalesce(sequenceMetadata.sequence, {initialSequennceValue}) + {sequenceIncrementAmount} return sequenceMetadata.sequence";
	
	protected GraphDatabaseService database;
	
	@Override
	public String generateUuid() {
		
		Map<String, Object> parameters = new HashMap<String, Object>();
		parameters.put("initialSequennceValue", 0);
		parameters.put("sequenceIncrementAmount", 1);
		
		Result result = database.execute(cypher, parameters);
		
		long sequence = (long) result.next().values().iterator().next();
		return String.valueOf(sequence);
		
	}

	@Override
	public void setGraphDatabaseService(GraphDatabaseService database) {
		this.database = database;
	}
	
}
