package com.graphaware.module.uuid.generator;

import java.util.HashMap;
import java.util.Map;

import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.QueryExecutionException;
import org.neo4j.graphdb.Result;

import com.graphaware.common.uuid.UuidGenerator;
import com.graphaware.module.uuid.GraphDatabaseServiceAware;
import org.neo4j.kernel.DeadlockDetectedException;

/**
 * A non-trivial UuidGenerator implementation that makes use of the provided GraphDatabaseService and 
 * Cypher to generate a numerically increasing sequence.
 */
public class SequenceIdGenerator implements UuidGenerator, GraphDatabaseServiceAware {

	private static final int MAX_RETRIES = 100;

	public static final String cypher = "MERGE (sequenceMetadata:SequenceMetadata {id: {id} }) SET sequenceMetadata.sequence = coalesce(sequenceMetadata.sequence, {initialSequennceValue}) + {sequenceIncrementAmount} return sequenceMetadata.sequence";
	
	protected GraphDatabaseService database;

	private String generateUuid(int currentRetry) {
		Map<String, Object> parameters = new HashMap<String, Object>();
		parameters.put("initialSequennceValue", 0);
		parameters.put("sequenceIncrementAmount", 1);
		parameters.put("id", 0);

		try {
			Result result = database.execute(cypher, parameters);

			long sequence = (long) result.next().values().iterator().next();
			return String.valueOf(sequence);
		} catch (Exception e) {
			if (e instanceof DeadlockDetectedException && currentRetry < MAX_RETRIES) {
				return generateUuid(++currentRetry);
			}

			throw new RuntimeException(e);
		}
	}
	
	@Override
	public String generateUuid() {
		return generateUuid(0);
	}

	@Override
	public void setGraphDatabaseService(GraphDatabaseService database) {
		this.database = database;
	}
	
}
