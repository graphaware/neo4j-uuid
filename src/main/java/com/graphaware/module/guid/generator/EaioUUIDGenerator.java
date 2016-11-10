package com.graphaware.module.guid.generator;

import com.eaio.uuid.UUID;

/**
 * UUID Generator using the UUID library from http://johannburkard.de/software/uuid/
 */
public class EaioUUIDGenerator implements GuidGenerator {

    @Override
    public String generateGuid() {
        UUID uuid = new UUID();
        return uuid.toString();
    }
    
}	
