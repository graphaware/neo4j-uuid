package com.graphaware.module.uuid;

import com.graphaware.common.policy.inclusion.all.IncludeAllRelationshipProperties;
import com.graphaware.runtime.policy.all.IncludeAllBusinessNodes;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class UuidConfigurationTest {

    @Test
    public void testConfigurationEqualityOfDefaultConfiguration() {
        UuidConfiguration config1 = UuidConfiguration.defaultConfiguration();
        UuidConfiguration config2 = UuidConfiguration.defaultConfiguration();
        assertEquals(config1,config2);
    }

    @Test
    public void testConfigurationEquality() {
        UuidConfiguration config1 =UuidConfiguration.defaultConfiguration()
                                    .withUuidProperty("id")
                .withUuidGenerator("generator")
                .withImmutability(false)
                .withStripHyphensProperty(true)
                .with(IncludeAllRelationshipProperties.getInstance())
                .with(IncludeAllBusinessNodes.getInstance());

        UuidConfiguration config2 =UuidConfiguration.defaultConfiguration()
                .withUuidProperty("id")
                .withUuidGenerator("generator")
                .withImmutability(false)
                .withStripHyphensProperty(true)
                .with(IncludeAllRelationshipProperties.getInstance())
                .with(IncludeAllBusinessNodes.getInstance());

        assertEquals(config1,config2);
    }

    @Test
    public void testConfigurationEqualityWhenConfigIsExtended() {
        UuidConfiguration config1 =UuidConfiguration.defaultConfiguration()
                .withUuidProperty("id")
                .withUuidGenerator("generator")
                .withImmutability(false)
                .withStripHyphensProperty(true)
                .with(IncludeAllBusinessNodes.getInstance());

        UuidConfiguration config2 =UuidConfiguration.defaultConfiguration()
                .withUuidProperty("id")
                .withUuidGenerator("generator")
                .withImmutability(false)
                .withStripHyphensProperty(true)
                .with(IncludeAllRelationshipProperties.getInstance())
                .with(IncludeAllBusinessNodes.getInstance());

        assertNotEquals(config1,config2);
    }
}
