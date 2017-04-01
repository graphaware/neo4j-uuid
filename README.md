GraphAware Neo4j UUID
=====================

[![Build Status](https://travis-ci.org/graphaware/neo4j-uuid.png)](https://travis-ci.org/graphaware/neo4j-uuid) | <a href="http://graphaware.com/downloads/" target="_blank">Downloads</a> | <a href="http://graphaware.com/site/uuid/latest/apidocs/" target="_blank">Javadoc</a> | Latest Release: 3.1.3.45.14

GraphAware UUID is a simple library that transparently assigns a UUID to newly created nodes and relationships in the graph and makes sure nobody
can (accidentally or intentionally) change or delete them.

Getting the Software
--------------------

### Server Mode

When using Neo4j in the <a href="http://docs.neo4j.org/chunked/stable/server-installation.html" target="_blank">standalone server</a> mode,
you will need the <a href="https://github.com/graphaware/neo4j-framework" target="_blank">GraphAware Neo4j Framework</a> and GraphAware Neo4j UUID .jar files (both of which you can <a href="http://graphaware.com/downloads/" target="_blank">download here</a>) dropped
into the `plugins` directory of your Neo4j installation. After changing a few lines of config (read on) and restarting Neo4j, the module will do its magic.

### Embedded Mode / Java Development

Java developers that use Neo4j in <a href="http://docs.neo4j.org/chunked/stable/tutorials-java-embedded.html" target="_blank">embedded mode</a>
and those developing Neo4j <a href="http://docs.neo4j.org/chunked/stable/server-plugins.html" target="_blank">server plugins</a>,
<a href="http://docs.neo4j.org/chunked/stable/server-unmanaged-extensions.html" target="_blank">unmanaged extensions</a>,
GraphAware Runtime Modules, or Spring MVC Controllers can include use the UUID as a dependency for their Java project.

#### Releases

Releases are synced to <a href="http://search.maven.org/#search%7Cga%7C1%7Ca%3A%22uuid%22" target="_blank">Maven Central repository</a>. When using Maven for dependency management, include the following dependency in your pom.xml.

    <dependencies>
        ...
        <dependency>
            <groupId>com.graphaware.neo4j</groupId>
            <artifactId>uuid</artifactId>
            <version>3.1.3.45.14</version>
        </dependency>
        ...
    </dependencies>

#### Snapshots

To use the latest development version, just clone this repository, run `mvn clean install` and change the version in the
dependency above to 3.1.3.45.15-SNAPSHOT.

#### Note on Versioning Scheme

The version number has two parts. The first four numbers indicate compatibility with Neo4j GraphAware Framework.
 The last number is the version of the UUID library. For example, version 2.1.3.11.1 is version 1 of the UUID library
 compatible with GraphAware Neo4j Framework 2.1.3.11.


Setup and Configuration
--------------------

### Server Mode

Edit `conf/neo4j.conf` to register the UUID module:

```
com.graphaware.runtime.enabled=true

#UIDM becomes the module ID:
com.graphaware.module.UIDM.1=com.graphaware.module.uuid.UuidBootstrapper

#optional, default is uuid:
com.graphaware.module.UIDM.uuidProperty=uuid

#optional, default is false:
com.graphaware.module.UIDM.stripHyphens=false

#optional, default is all nodes:
com.graphaware.module.UIDM.node=hasLabel('Label1') || hasLabel('Label2')

#optional, default is no relationships:
com.graphaware.module.UIDM.relationship=isType('Type1')

#optional, default is uuidIndex
com.graphaware.module.UIDM.uuidIndex=uuidIndex

#optional, default is uuidRelIndex
com.graphaware.module.UIDM.uuidRelationshipIndex=uuidRelIndex
```

Note that "UIDM" becomes the module ID. 

`com.graphaware.module.UIDM.uuidProperty` is the property name that will be used to store the assigned UUID on nodes and relationships. The default is "uuid".

`com.graphaware.module.UIDM.stripHyphens` is the property name that controls hyphen existence. If its true created UUID will be free from hyphens. 

`com.graphaware.module.UIDM.node` specifies either a fully qualified class name of [`NodeInclusionPolicy`](http://graphaware.com/site/framework/latest/apidocs/com/graphaware/common/policy/NodeInclusionPolicy.html) implementation,
or a Spring Expression Language expression determining, which nodes to assign a UUID to. The default is to assign the
UUID property to every node which isn't internal to the framework.

`com.graphaware.module.UIDM.relationship` specifies either a fully qualified class name of [`RelationshipInclusionPolicy`](http://graphaware.com/site/framework/latest/apidocs/com/graphaware/common/policy/RelationshipInclusionPolicy.html) implementation,
or a Spring Expression Language expression determining, which relationships to assign a UUID to. The default is **not** to assign the
UUID property to any relationship. If you want to assign UUID to all relationship, please use `com.graphaware.module.UIDM.relationship=com.graphaware.runtime.policy.all.IncludeAllBusinessRelationships`

`com.graphaware.module.UIDM.uuidIndex` is the index name that will be used to index nodes based on their UUID. The default is "uuidIndex".

`com.graphaware.module.UIDM.uuidRelationshipIndex` is the index name that will be used to index relationships based on their UUID. The default is "uuidRelIndex".

### Embedded Mode / Java Development

To use the UUID module programmatically, register the module like this

```java
 GraphAwareRuntime runtime = GraphAwareRuntimeFactory.createRuntime(database);  //where database is an instance of GraphDatabaseService
 UuidModule module = new UuidModule("UUIDM", UuidConfiguration.defaultConfiguration());
 runtime.registerModule(module);
 runtime.start();
```

Alternatively:
```java
 GraphDatabaseService database = new GraphDatabaseFactory().newEmbeddedDatabaseBuilder(pathToDb)
    .loadPropertiesFromFile(this.getClass().getClassLoader().getResource("neo4j.properties").getPath())
    .newGraphDatabase();
 
 //make sure neo4j.properties contain the lines mentioned in previous section
```

Using GraphAware UUID
---------------------

Apart from the configuration described above, the GraphAware UUID module requires nothing else to function. It will assign a UUID to nodes and relationships configured,
and will prevent modifications to the UUID or deletion of the UUID property from these nodes/relationships by not allowing the transaction to commit.
You can also retrieve a node/relationship by UUID.

### Cypher

Once deployed, you can use the following Cypher syntax:

* `CALL ga.uuid.findNode('<your UUID>') YIELD node AS n ...` (then do something with `n`, e.g. `CALL ga.uuid.findNode('<your UUID>') YIELD node AS n RETURN id(n)`
* `CALL ga.uuid.findRelationship('<your UUID>') YIELD relationship AS r ...`
* `CALL ga.uuid.findNodes(['<UUID1>,<UUID2>,...']) YIELD nodes UNWIND nodes as node ...`
* `CALL ga.uuid.findRelationships(['<UUID1>,<UUID2>,...']) YIELD relationships UNWIND relationships as rel ...`

In case you did not use `UIDM` (the default) as the module ID in your configuration, or if you registered multiple UUID modules,
you will have to use slightly different syntax that allows you to pass in the module ID. 'nd' stands for "non-default":

* `CALL ga.uuid.nd.findNode('<module ID>','<your UUID>') YIELD node AS n ...` (then do something with `n`, e.g. `CALL ga.uuid.findNode('<your UUID>') YIELD node AS n RETURN id(n)`
* `CALL ga.uuid.nd.findRelationship('<module ID>','<your UUID>') YIELD relationship AS r ...`
* `CALL ga.uuid.nd.findNodes('<module ID>',['<UUID1>,<UUID2>,...']) YIELD nodes UNWIND nodes as node ...`
* `CALL ga.uuid.nd.findRelationships('<module ID>',['<UUID1>,<UUID2>,...']) YIELD relationships UNWIND relationships as rel ...`

### REST API

In Server Mode, a node/relationship can be retrieved by its UUID via the REST API.

You can issue GET requests to `http://your-server-address:7474/graphaware/uuid/{moduleId}/node/{uuid}` to get the node ID for a given uuid.

GET requests to `http://your-server-address:7474/graphaware/uuid/{moduleId}/relationship/{uuid}` will get the relationship ID for a given uuid.

{moduleId} is the module ID the UUID Module was registered with. You can omit this part of the URL, in which case "UIDM" is assumed as the default value.
If no node exists with the given UUID, a 404 status code will be returned.

### Java API

To use the Java API to find a node by its UUID, please instantiate `UuidReader` and use the method `getNodeIdByUuid` or `getRelationshipByUuid`

```
 UuidConfiguration configuration = getStartedRuntime(database).getModule(moduleId, UuidModule.class).getConfiguration();
 UuidReader reader = UuidReader(configuration, database);
 Node node = getNodeIdByUuid(uuid);
 Relationship rel = getRelationshipByUuid(uuid);
```

Please refer to Javadoc for more detail.

### Specifying the Generator Through Configuration

By default, the `com.graphaware.common.uuid.EaioUuidGenerator` is used to generate the underlying UUID. Any generator implementation can be used, be it 
out of the box or your own custom code, by modifying the `conf/neo4j.conf`. The following example configures the UUID module to make use of the `JavaUtilUUIDGenerator`:

```
com.graphaware.runtime.enabled=true

com.graphaware.module.UUID.1=com.graphaware.module.uuid.UuidBootstrapper
com.graphaware.module.UUID.relationship=com.graphaware.runtime.policy.all.IncludeAllBusinessRelationships
com.graphaware.module.UUID.uuidGeneratorClass=com.graphaware.module.uuid.generator.JavaUtilUUIDGenerator
```

The following example configures the UUID module to make use of the `SequenceIdGenerator`:

```
com.graphaware.runtime.enabled=true

com.graphaware.module.UUID.1=com.graphaware.module.uuid.UuidBootstrapper
com.graphaware.module.UUID.relationship=com.graphaware.runtime.policy.all.IncludeAllBusinessRelationships
com.graphaware.module.UUID.uuidProperty=sequence
com.graphaware.module.UUID.uuidGeneratorClass=com.graphaware.module.uuid.generator.SequenceIdGenerator
```

Please see the `com.graphaware.common.uuid.UuidGenerator` interface and the `com.graphaware.module.uuid.generator` package for more information 
and examples of how to implement your own generator. 


License
-------

Copyright (c) 2016 GraphAware

GraphAware is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License
as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied
warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
You should have received a copy of the GNU General Public License along with this program.
If not, see <http://www.gnu.org/licenses/>.
