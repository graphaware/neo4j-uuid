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

package com.graphaware.module.guid.read;

public interface GuidReader {

    /**
     * Get a node ID by its GUID.
     *
     * @param guid guid.
     * @return Node ID.
     * @throws org.neo4j.graphdb.NotFoundException in case no node exists with such GUID.
     */
    long getNodeIdByGuid(String guid);

    /**
     * Get a relationship ID by its GUID.
     *
     * @param guid guid.
     * @return Relationship ID.
     * @throws org.neo4j.graphdb.NotFoundException in case no node exists with such GUID.
     */
    long getRelationshipIdByGuid(String guid);
}
