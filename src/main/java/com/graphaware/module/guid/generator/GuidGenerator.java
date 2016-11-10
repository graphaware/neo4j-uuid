package com.graphaware.module.guid.generator;

public interface GuidGenerator {

	/**
     * Generate a GUID. 
     * Note that the GUID returned is of type Object. This allows values, such as 
     * Strings (ie: a UUID) or a numerical value (such as a sequence), to be 
     * stored to the database as the correct type.
     *
     * @return the GUID.
     */
    Object generateGuid();
    
}