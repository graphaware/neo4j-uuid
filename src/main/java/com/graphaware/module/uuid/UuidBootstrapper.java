/*
 * Copyright (c) 2013-2019 GraphAware
 *
 * This file is part of the GraphAware Framework.
 *
 * GraphAware Framework is free software: you can redistribute it and/or modify it under the terms of
 * the GNU General Public License as published by the Free Software Foundation, either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details. You should have received a copy of
 * the GNU General Public License along with this program.  If not, see
 * <http://www.gnu.org/licenses/>.
 */

package com.graphaware.module.uuid;

import java.util.Map;

import com.graphaware.common.log.LoggerFactory;
import com.graphaware.runtime.module.BaseRuntimeModuleBootstrapper;
import com.graphaware.runtime.module.RuntimeModule;
import com.graphaware.runtime.module.RuntimeModuleBootstrapper;

import org.apache.commons.lang3.StringUtils;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.logging.Log;

/**
 * Bootstraps the {@link UuidModule} in server mode.
 */
public class UuidBootstrapper extends BaseRuntimeModuleBootstrapper<UuidConfiguration> implements RuntimeModuleBootstrapper {

    private static final Log LOG = LoggerFactory.getLogger(UuidBootstrapper.class);

    private static final String UUID_PROPERTY = "uuidProperty";
    private static final String UUID_INDEX = "uuidIndex";
    private static final String UUID_RELATIONSHIP_INDEX = "uuidRelationshipIndex";
    private static final String STRIP_HYPHENS = "stripHyphens";
    private static final String UUID_GENERATOR_CLASS = "uuidGeneratorClass";
    private static final String IMMUTABLE = "immutable";

    /**
     * {@inheritDoc}
     */
    @Override
    protected UuidConfiguration defaultConfiguration() {
        return UuidConfiguration.defaultConfiguration();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected RuntimeModule doBootstrapModule(String moduleId, Map<String, String> config, GraphDatabaseService database, UuidConfiguration configuration) {
    	
    	String uuidProperty = config.get(UUID_PROPERTY);
        if (StringUtils.isNotBlank(uuidProperty)) {
            configuration = configuration.withUuidProperty(uuidProperty);
            LOG.info("uuidProperty set to %s", configuration.getUuidProperty());
        }

        String uuidIndex = config.get(UUID_INDEX);
        if (StringUtils.isNotBlank(uuidIndex)) {
            configuration = configuration.withUuidIndex(uuidIndex);
            LOG.info("uuidIndex set to %s", configuration.getUuidIndex());
        }

        String uuidRelationshipIndex = config.get(UUID_RELATIONSHIP_INDEX);
        if (StringUtils.isNotBlank(uuidRelationshipIndex)) {
            configuration = configuration.withUuidRelationshipIndex(uuidRelationshipIndex);
            LOG.info("uuidRelationshipIndex set to %s", configuration.getUuidRelationshipIndex());
        }

        String stripHypensString = config.get(STRIP_HYPHENS);        
        if (StringUtils.isNotBlank(stripHypensString)) {
            boolean stripHyphens = Boolean.valueOf(stripHypensString);
            configuration = configuration.withStripHyphensProperty(stripHyphens);
            LOG.info("stripHyphens set to %s", configuration.shouldStripHyphens());
        }
        
        String uuidGeneratorClassString = config.get(UUID_GENERATOR_CLASS);
        if (StringUtils.isNotBlank(uuidGeneratorClassString)) {
            configuration = configuration.withUuidGenerator(uuidGeneratorClassString);
            LOG.info("uuidGenerator set to %s", configuration.getUuidGenerator());
        }

        String immutableString = config.get(IMMUTABLE);
        if (StringUtils.isNotBlank(immutableString)) {
            boolean immutable = Boolean.valueOf(immutableString);
            configuration = configuration.withImmutability(immutable);
            LOG.info("Setting immutability to %s", immutableString);
            logImmutabilityWarning();
        }
        

        return new UuidModule(moduleId, configuration, database);
    }

    private void logImmutabilityWarning() {
        LOG.warn("Immutability has been disabled by the configuration. Such setting might have a negative impact on the consistency of your data.");
    }
}
