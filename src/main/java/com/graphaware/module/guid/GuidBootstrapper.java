/*
 * Copyright (c) 2013-2016 GraphAware
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

package com.graphaware.module.guid;

import java.util.Map;

import com.graphaware.common.log.LoggerFactory;
import com.graphaware.runtime.module.BaseRuntimeModuleBootstrapper;
import com.graphaware.runtime.module.RuntimeModule;
import com.graphaware.runtime.module.RuntimeModuleBootstrapper;

import org.apache.commons.lang3.StringUtils;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.logging.Log;

/**
 * Bootstraps the {@link GuidModule} in server mode.
 */
public class GuidBootstrapper extends BaseRuntimeModuleBootstrapper<GuidConfiguration> implements RuntimeModuleBootstrapper {

    private static final Log LOG = LoggerFactory.getLogger(GuidBootstrapper.class);

    private static final String GUID_PROPERTY = "guidProperty";
    private static final String GUID_INDEX = "guidIndex";
    private static final String GUID_RELATIONSHIP_INDEX = "guidRelationshipIndex";
    private static final String STRIP_HYPHENS = "stripHyphens";
    private static final String GUID_GENERATOR_CLASS = "GuidGeneratorClass";

    /**
     * {@inheritDoc}
     */
    @Override
    protected GuidConfiguration defaultConfiguration() {
        return GuidConfiguration.defaultConfiguration();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected RuntimeModule doBootstrapModule(String moduleId, Map<String, String> config, GraphDatabaseService database, GuidConfiguration configuration) {
    	
    	String guidProperty = config.get(GUID_PROPERTY);
        if (StringUtils.isNotBlank(guidProperty)) {
            configuration = configuration.withGuidProperty(guidProperty);
            LOG.info("guidProperty set to %s", configuration.getGuidProperty());
        }

        String guidIndex = config.get(GUID_INDEX);
        if (StringUtils.isNotBlank(guidIndex)) {
            configuration = configuration.withGuidIndex(guidIndex);
            LOG.info("guidIndex set to %s", configuration.getGuidIndex());
        }

        String guidRelationshipIndex = config.get(GUID_RELATIONSHIP_INDEX);
        if (StringUtils.isNotBlank(guidRelationshipIndex)) {
            configuration = configuration.withGuidRelationshipIndex(guidRelationshipIndex);
            LOG.info("guidRelationshipIndex set to %s", configuration.getGuidRelationshipIndex());
        }

        String stripHypensString = config.get(STRIP_HYPHENS);        
        if (StringUtils.isNotBlank(stripHypensString)) {
            boolean stripHyphens = Boolean.valueOf(stripHypensString);
            configuration = configuration.withStripHyphensProperty(stripHyphens);
            LOG.info("stripHyphens set to %s", configuration.shouldStripHyphens());
        }
        
        String GuidGeneratorClassString = config.get(GUID_GENERATOR_CLASS);
        if (StringUtils.isNotBlank(GuidGeneratorClassString)) {
            configuration = configuration.withGuidGenerator(GuidGeneratorClassString);
            LOG.info("GuidGenerator set to %s", configuration.getGuidGenerator());
        }
        

        return new GuidModule(moduleId, configuration, database);
    }
}
