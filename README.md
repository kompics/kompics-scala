# Kompics Scala DSL

A Scala DSL for the Kompics message-passing component model for building distributed systems

See http://kompics.sics.se/ for more information.

### Current Version
0.9.2-SNAPSHOT for Scala 2.11 and Kompics 0.9.2-SNAPSHOT

#### SBT Dependency
To add Kompics Scala to your project use:
```scala
resolvers += "Kompics Releases" at "http://kompics.sics.se/maven/repository/"
resolvers += "Kompics Snapshots" at "http://kompics.sics.se/maven/snapshotrepository/"

libraryDependencies += "se.sics.kompics" %% "kompics-scala" % "0.9.2-SNAPSHOT"
```
