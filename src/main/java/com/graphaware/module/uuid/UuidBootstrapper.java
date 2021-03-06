/*
 * Copyright (c) 2013-2020 GraphAware
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

import com.graphaware.common.log.LoggerFactory;
import com.graphaware.runtime.module.BaseModuleBootstrapper;
import com.graphaware.runtime.module.Module;
import com.graphaware.runtime.module.ModuleBootstrapper;
import org.apache.commons.configuration2.Configuration;
import org.apache.commons.lang3.StringUtils;
import org.neo4j.logging.Log;

/**
 * Bootstraps the {@link UuidModule} in server mode.
 */
public class UuidBootstrapper extends BaseModuleBootstrapper<UuidConfiguration> implements ModuleBootstrapper {

    private static final Log LOG = LoggerFactory.getLogger(UuidBootstrapper.class);

    private static final String UUID_PROPERTY = "uuidProperty";
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
    protected Module<?> doBootstrapModule(String moduleId, Configuration config, UuidConfiguration configuration) {
        String uuidProperty = config.getString(UUID_PROPERTY);
        if (StringUtils.isNotBlank(uuidProperty)) {
            configuration = configuration.withUuidProperty(uuidProperty);
            LOG.info("uuidProperty set to %s", configuration.getUuidProperty());
        }

        String stripHypensString = config.getString(STRIP_HYPHENS);
        if (StringUtils.isNotBlank(stripHypensString)) {
            boolean stripHyphens = Boolean.valueOf(stripHypensString);
            configuration = configuration.withStripHyphensProperty(stripHyphens);
            LOG.info("stripHyphens set to %s", configuration.shouldStripHyphens());
        }

        String uuidGeneratorClassString = config.getString(UUID_GENERATOR_CLASS);
        if (StringUtils.isNotBlank(uuidGeneratorClassString)) {
            configuration = configuration.withUuidGenerator(uuidGeneratorClassString);
            LOG.info("uuidGenerator set to %s", configuration.getUuidGenerator());
        }

        String immutableString = config.getString(IMMUTABLE);
        if (StringUtils.isNotBlank(immutableString)) {
            boolean immutable = Boolean.valueOf(immutableString);
            configuration = configuration.withImmutability(immutable);
            LOG.info("Setting immutability to %s", immutableString);
            logImmutabilityWarning();
        }

        return new UuidModule(moduleId, configuration);
    }

    private void logImmutabilityWarning() {
        LOG.warn("Immutability has been disabled by the configuration. Such setting might have a negative impact on the consistency of your data.");
    }
}
