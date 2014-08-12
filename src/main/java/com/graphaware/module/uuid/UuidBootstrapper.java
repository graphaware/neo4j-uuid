/*
 * Copyright (c) 2014 GraphAware
 *
 * This file is part of GraphAware.
 *
 * GraphAware is free software: you can redistribute it and/or modify it under the terms of
 * the GNU General Public License as published by the Free Software Foundation, either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 *  without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details. You should have received a copy of
 * the GNU General Public License along with this program.  If not, see
 * <http://www.gnu.org/licenses/>.
 */

package com.graphaware.module.uuid;

import com.graphaware.runtime.module.RuntimeModule;
import com.graphaware.runtime.module.RuntimeModuleBootstrapper;
import org.neo4j.graphdb.GraphDatabaseService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

/**
 * Bootstraps the {@link UuidModule} in server mode.
 */
public class UuidBootstrapper implements RuntimeModuleBootstrapper {

    private static final Logger LOG = LoggerFactory.getLogger(UuidBootstrapper.class);

    //keys to use when configuring using neo4j.properties
    private static final String UUID_PROPERTY = "uuidProperty";
    private static final String LABELS = "labels";

    /**
     * @{inheritDoc}
     */
    @Override
    public RuntimeModule bootstrapModule(String moduleId, Map<String, String> config, GraphDatabaseService database) {
        UuidConfiguration configuration = UuidConfiguration.defaultConfiguration();

        if (config.get(UUID_PROPERTY) != null && config.get(UUID_PROPERTY).length() > 0) {
            configuration = configuration.withUuidProperty(config.get(UUID_PROPERTY));
            LOG.info("uuidProperty set to {}", configuration.getUuidProperty());
        }
        if (config.get(LABELS) != null && config.get(LABELS).length() > 0) {
            List<String> labels = new ArrayList<>();
            StringTokenizer st = new StringTokenizer(config.get(LABELS), ",");
            while (st.hasMoreTokens()) {
                labels.add(st.nextToken());
            }
            configuration = configuration.withLabels(labels);
        }
        return new UuidModule(moduleId, configuration);
    }
}
