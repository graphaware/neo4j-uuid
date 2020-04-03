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
package com.graphaware.module.uuid.api;

import static com.graphaware.module.uuid.UuidModule.DEFAULT_MODULE_ID;
import static com.graphaware.runtime.RuntimeRegistry.getStartedRuntime;

import com.graphaware.module.uuid.UuidConfiguration;
import com.graphaware.module.uuid.UuidModule;
import com.graphaware.module.uuid.read.DefaultUuidReader;
import com.graphaware.module.uuid.read.TransactionalUuidReader;
import com.graphaware.module.uuid.read.UuidReader;
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
 * REST API for {@link UuidModule}.
 */
@Controller
@RequestMapping("/uuid")
public class UuidApi {

    private final GraphDatabaseService database;

    @Autowired
    public UuidApi(GraphDatabaseService database) {
        this.database = database;
    }

    /**
     * Get the node id of the node which has the given uuid.
     *
     * @param uuid the uuid.
     * @return node id of the node which has the given uuid.
     * @throws org.neo4j.graphdb.NotFoundException if none exist.
     */
    @RequestMapping(value = "/node/{uuid}", method = RequestMethod.GET)
    @ResponseBody
    public Long getNodeIdByUuid(@PathVariable(value = "uuid") String uuid) {
        return getNodeIdByModuleAndUuid(DEFAULT_MODULE_ID, uuid);
    }

    /**
     * Get the node id of the node which has the given uuid.
     *
     * @param moduleId module id (used in the unlikely event that there are multiple modules, or if the module has a non-default ID).
     * @param uuid     the uuid.
     * @return node id of the node which has the given uuid.
     * @throws org.neo4j.graphdb.NotFoundException if none exist.
     */
    @RequestMapping(value = "/{moduleId}/node/{uuid}", method = RequestMethod.GET)
    @ResponseBody
    public Long getNodeIdByModuleAndUuid(@PathVariable(value = "moduleId") String moduleId, @PathVariable(value = "uuid") String uuid) {
        UuidConfiguration configuration = getStartedRuntime(database).getModule(moduleId, UuidModule.class).getConfiguration();
        return getReader(configuration).getNodeIdByUuid(uuid);
    }

    /**
     * Get the relationship id of the relationship which has the given uuid.
     *
     * @param uuid the uuid.
     * @return relationship id of the relationship which has the given uuid.
     * @throws org.neo4j.graphdb.NotFoundException if none exist.
     */
    @RequestMapping(value = "/relationship/{uuid}", method = RequestMethod.GET)
    @ResponseBody
    public Long getRelationshipIdByUuid(@PathVariable(value = "uuid") String uuid) {
        return getRelationshipIdByModuleAndUuid(DEFAULT_MODULE_ID, uuid);
    }

    /**
     * Get the relationship id of the relationship which has the given uuid.
     *
     * @param moduleId module id (used in the unlikely event that there are multiple modules, or if the module has a non-default ID).
     * @param uuid     the uuid.
     * @return relationship id of the relationship which has the given uuid.
     * @throws org.neo4j.graphdb.NotFoundException if none exist.
     */
    @RequestMapping(value = "/{moduleId}/relationship/{uuid}", method = RequestMethod.GET)
    @ResponseBody
    public Long getRelationshipIdByModuleAndUuid(@PathVariable(value = "moduleId") String moduleId, @PathVariable(value = "uuid") String uuid) {
        UuidConfiguration configuration = getStartedRuntime(database).getModule(moduleId, UuidModule.class).getConfiguration();
        return getReader(configuration).getRelationshipIdByUuid(uuid);
    }

    private UuidReader getReader(UuidConfiguration configuration) {
        return new TransactionalUuidReader(database, new DefaultUuidReader(configuration, database));
    }

    @ExceptionHandler(NotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public void handleNotFound() {

    }
}
