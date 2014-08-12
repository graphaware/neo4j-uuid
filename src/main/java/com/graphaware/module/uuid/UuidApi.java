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

import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

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

    @RequestMapping(value = "/node/{uuid}", method = RequestMethod.GET)
    @ResponseBody
    public Node getNodeIdByUuid(@PathVariable(value = "uuid") String uuid) {
        return null; //TODO till indexing is fixed or we use labels or a global scan
    }

}
