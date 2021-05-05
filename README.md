GraphAware Neo4j UUID - RETIRED
=====================

## GraphAware Neo4j UUID Has Been Retired
As of May 2021, this [repository has been retired](https://graphaware.com/framework/2021/05/06/from-graphaware-framework-to-graphaware-hume.html).

GraphAware UUID is a simple library that transparently assigns a UUID to newly created nodes and relationships in the graph and makes sure nobody
can (accidentally or intentionally) change or delete them.

## Community vs Enterprise

This open-source (GPLv3) version of the library is compatible with Neo4j Community Edition only. 
It *will not work* with Neo4j Enterprise Edition, which is a proprietary and commercial software product of Neo4j, Inc.

GraphAware offers a *paid* Enterprise version of the GraphAware Framework to licensed users of Neo4j Enterprise Edition.
Please [get in touch](mailto:info@graphaware.com) to receive access.

Getting the Software
--------------------

You will need the <a href="https://github.com/graphaware/neo4j-framework" target="_blank">GraphAware Neo4j Framework</a> and GraphAware Neo4j UUID .jar files (both of which you can <a href="http://graphaware.com/downloads/" target="_blank">download here</a>) dropped
into the `plugins` directory of your Neo4j installation. After adding a few lines of config (read on) and restarting Neo4j, the module will do its magic.

#### Releases

Releases are synced to <a href="http://search.maven.org/#search%7Cga%7C1%7Ca%3A%22uuid%22" target="_blank">Maven Central repository</a>. When using Maven for dependency management, include the following dependency in your pom.xml and change version number to match the required version.

```xml
<dependencies>
    ...
    <dependency>
        <groupId>com.graphaware.neo4j</groupId>
        <artifactId>uuid</artifactId>
        <version>A.B.C.D.E</version>
    </dependency>
    ...
</dependencies>
```

#### Snapshots

To use the latest development version, just clone this repository, run `mvn clean install` and change the version in the
dependency above to A.B.C.D.E-SNAPSHOT.

#### Note on Versioning Scheme

The version number has two parts. The first four numbers indicate compatibility with Neo4j GraphAware Framework.
 The last number is the version of the UUID library. For example, version 4.0.8.58.20 is version 20 of the UUID library
 compatible with GraphAware Neo4j Framework 4.0.8.58.

Setup and Configuration
--------------------

Create or edit `conf/graphaware.conf` to register the UUID module:

```properties
#UIDM becomes the module ID:
com.graphaware.module.neo4j.UIDM.1=com.graphaware.module.uuid.UuidBootstrapper

#optional, default is uuid:
com.graphaware.module.neo4j.UIDM.uuidProperty=uuid

#optional, default is false:
com.graphaware.module.neo4j.UIDM.stripHyphens=false

#optional, default is all nodes:
com.graphaware.module.neo4j.UIDM.node=hasLabel('Label1') || hasLabel('Label2')

#optional, default is no relationships:
com.graphaware.module.neo4j.UIDM.relationship=isType('Type1')
```

Note that "UIDM" becomes the module ID. 

`com.graphaware.module.neo4j.UIDM.uuidProperty` is the property name that will be used to store the assigned UUID on nodes and relationships. The default is "uuid".

`com.graphaware.module.neo4j.UIDM.stripHyphens` is the property name that controls hyphen existence. If its true created UUID will be free from hyphens. 

`com.graphaware.module.neo4j.UIDM.node` specifies either a fully qualified class name of [`NodeInclusionPolicy`](http://graphaware.com/site/framework/latest/apidocs/com/graphaware/common/policy/NodeInclusionPolicy.html) implementation,
or a Spring Expression Language expression determining, which nodes to assign a UUID to. The default is to assign the
UUID property to every node which isn't internal to the framework.

`com.graphaware.module.neo4j.UIDM.relationship` specifies either a fully qualified class name of [`RelationshipInclusionPolicy`](http://graphaware.com/site/framework/latest/apidocs/com/graphaware/common/policy/RelationshipInclusionPolicy.html) implementation,
or a Spring Expression Language expression determining, which relationships to assign a UUID to. The default is **not** to assign the
UUID property to any relationship. If you want to assign UUID to all relationship, please use `com.graphaware.module.neo4j.UIDM.relationship=com.graphaware.runtime.policy.all.IncludeAllBusinessRelationships`

Using GraphAware UUID
---------------------

Apart from the configuration described above, **you must create unique constraints** (or at least indices) in the database.

The module will assign a UUID to nodes and relationships configured, and will prevent modifications to the UUID or deletion of the UUID property from these nodes/relationships by not allowing the transaction to commit.

Note: If you create a node and return it immediately, its contents will not reflect changes performed by transaction event handlers such as this one -- thus the UUID will not be available. A separate call must be made to get the UUID, for example:

```cypher
CREATE (n:User {name: "Alice"}) RETURN id(n)
---
// Later, using saved ID from above:
MATCH (n) WHERE id(n) = 123 RETURN n.uuid
```

### Specifying the Generator Through Configuration

By default, the `com.graphaware.common.uuid.EaioUuidGenerator` is used to generate the underlying UUID. Any generator implementation can be used, be it 
out of the box or your own custom code, by modifying the `conf/graphaware.conf`. The following example configures the UUID module to make use of the `JavaUtilUUIDGenerator`:

```properties
com.graphaware.module.neo4j.UUID.1=com.graphaware.module.uuid.UuidBootstrapper
com.graphaware.module.UUID.neo4j.uuidGeneratorClass=com.graphaware.module.uuid.generator.JavaUtilUUIDGenerator
```

Please see the `com.graphaware.common.uuid.UuidGenerator` interface and the `com.graphaware.module.uuid.generator` package for more information 
and examples of how to implement your own generator. 

### Immutability

This module ensures that all assigned UUIDs on nodes and relationships are immutable, meaning they cannot be deleted nor changed.
In some scenarios, developers might want to disable the immutability with the following configuration setting :

```properties
com.graphaware.module.neo4j.UUID.immutable=false
```

We only allow this setting for development purposes or developers having an specific need and we **fully discourage** the use of this setting.

License
-------

Copyright (c) 2020 GraphAware

GraphAware is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License
as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied
warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
You should have received a copy of the GNU General Public License along with this program.
If not, see <http://www.gnu.org/licenses/>.
