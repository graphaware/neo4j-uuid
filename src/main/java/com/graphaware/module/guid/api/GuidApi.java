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
package com.graphaware.module.guid.api;

import static com.graphaware.module.guid.GuidModule.DEFAULT_MODULE_ID;
import static com.graphaware.runtime.RuntimeRegistry.getStartedRuntime;

import com.graphaware.module.guid.GuidConfiguration;
import com.graphaware.module.guid.GuidModule;
import com.graphaware.module.guid.read.DefaultGuidReader;
import com.graphaware.module.guid.read.TransactionalGuidReader;
import com.graphaware.module.guid.read.GuidReader;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.NotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * REST API for {@link GuidModule}.
 */
@Controller
@RequestMapping("/guid")
public class GuidApi {

    private final GraphDatabaseService database;

    @Autowired
    public GuidApi(GraphDatabaseService database) {
        this.database = database;
    }

    /**
     * Get the node id of the node which has the given guid.
     *
     * @param guid the guid.
     * @return node id of the node which has the given guid.
     * @throws org.neo4j.graphdb.NotFoundException if none exist.
     */
    @RequestMapping(value = "/node/{guid}", method = RequestMethod.GET)
    @ResponseBody
    public Long getNodeIdByGuid(@PathVariable(value = "guid") String guid) {
        return getNodeIdByModuleAndGuid(DEFAULT_MODULE_ID, guid);
    }

    /**
     * Get the node id of the node which has the given guid.
     *
     * @param moduleId module id (used in the unlikely event that there are multiple modules, or if the module has a non-default ID).
     * @param guid     the guid.
     * @return node id of the node which has the given guid.
     * @throws org.neo4j.graphdb.NotFoundException if none exist.
     */
    @RequestMapping(value = "/{moduleId}/node/{guid}", method = RequestMethod.GET)
    @ResponseBody
    public Long getNodeIdByModuleAndGuid(@PathVariable(value = "moduleId") String moduleId, @PathVariable(value = "guid") String guid) {
        GuidConfiguration configuration = getStartedRuntime(database).getModule(moduleId, GuidModule.class).getConfiguration();
        return getReader(configuration).getNodeIdByGuid(guid);
    }

    /**
     * Get the relationship id of the relationship which has the given guid.
     *
     * @param guid the guid.
     * @return relationship id of the relationship which has the given guid.
     * @throws org.neo4j.graphdb.NotFoundException if none exist.
     */
    @RequestMapping(value = "/relationship/{guid}", method = RequestMethod.GET)
    @ResponseBody
    public Long getRelationshipIdByGuid(@PathVariable(value = "guid") String guid) {
        return getRelationshipIdByModuleAndGuid(DEFAULT_MODULE_ID, guid);
    }

    /**
     * Get the relationship id of the relationship which has the given guid.
     *
     * @param moduleId module id (used in the unlikely event that there are multiple modules, or if the module has a non-default ID).
     * @param guid     the guid.
     * @return relationship id of the relationship which has the given guid.
     * @throws org.neo4j.graphdb.NotFoundException if none exist.
     */
    @RequestMapping(value = "/{moduleId}/relationship/{guid}", method = RequestMethod.GET)
    @ResponseBody
    public Long getRelationshipIdByModuleAndGuid(@PathVariable(value = "moduleId") String moduleId, @PathVariable(value = "guid") String guid) {
        GuidConfiguration configuration = getStartedRuntime(database).getModule(moduleId, GuidModule.class).getConfiguration();
        return getReader(configuration).getRelationshipIdByGuid(guid);
    }

    private GuidReader getReader(GuidConfiguration configuration) {
        return new TransactionalGuidReader(database, new DefaultGuidReader(configuration, database));
    }

    @ExceptionHandler(NotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public void handleNotFound() {

    }
}
