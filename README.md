# Kompics Scala DSL

A Scala DSL for the [Kompics](http://kompics.sics.se/) message-passing component model for building distributed systems

See the [documentation](http://kompics.sics.se/current/scala/index.html) for more information.

### Current Version
`1.0.0` for Scala 2.11 and 2.12

#### SBT Dependency
To add Kompics Scala to your project use:
```scala
resolvers += "Kompics Releases" at "http://kompics.sics.se/maven/repository/"
resolvers += "Kompics Snapshots" at "http://kompics.sics.se/maven/snapshotrepository/"

libraryDependencies += "se.sics.kompics" %% "kompics-scala" % "1.0.+"
```

For the simulation scala DSL also add:

```scala
libraryDependencies += "se.sics.kompics" %% "kompics-scala-simulator" % "1.0.+" // probably % "test" as well
```