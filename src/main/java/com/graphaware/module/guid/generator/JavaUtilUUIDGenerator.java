package com.graphaware.module.guid.generator;

import java.util.UUID;

/**
 * A simple GuidGenerator implementation that makes use of java.util.UUID to generate UUID's.
 */
public class JavaUtilUUIDGenerator implements GuidGenerator {

	@Override
	public String generateGuid() {
		UUID uuid = UUID.randomUUID();
		return uuid.toString();
	}
	
}
