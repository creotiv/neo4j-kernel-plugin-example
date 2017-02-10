# Example of Neo4j Kernel Plugin.
I spent some amout of time to understand how kernel extension works, how to test them right, and how to work with existing bugs.
In this example extension i tried to cover all problems that i got and how i compose all things together at the end.
Hope this will help some of you and save time and hair on your head)

## How to test
First you will need to install Maven, then just call `mvn test` from base dir.

## How to build & install extension
Call `mvn install -DskipTests` this will create target dir in which you will find neo4j-kernel-plugin-example-1.0.0-SNAPSHOT.jar
This package you should place to plugins/example-extension/ of your server.

References:

* https://neo4j.com/docs/java-reference/current/ - good reference about neo4j development
* https://neo4j.com/docs/java-reference/current/javadocs/org/neo4j/graphdb/factory/GraphDatabaseSettings.html - settings types
* https://neo4j.com/docs/java-reference/current/javadocs/org/neo4j/graphdb/GraphDatabaseService.html - main class to work with local db
* http://neo4j.com/docs/java-reference/current/javadocs/org/neo4j/graphdb/event/TransactionData.html - class to work with update events
* http://neo4j.com/docs/java-reference/current/javadocs/org/neo4j/graphdb/event/TransactionEventHandler.html - interface that add commit hooks

