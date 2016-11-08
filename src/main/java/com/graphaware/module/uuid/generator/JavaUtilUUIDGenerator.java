package com.graphaware.module.uuid.generator;

import java.util.UUID;

import com.graphaware.common.uuid.UuidGenerator;

/**
 * A simple UuidGenerator implementation that makes use of java.util.UUID to generate UUID's.
 */
public class JavaUtilUUIDGenerator implements UuidGenerator {

	@Override
	public String generateUuid() {
		UUID uuid = UUID.randomUUID();
		return uuid.toString();
	}
	
}
