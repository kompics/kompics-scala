# Kompics Scala DSL
[![Build Status](https://travis-ci.org/kompics/kompics-scala.svg?branch=master)](https://travis-ci.org/kompics/kompics-scala)
[![Maven Central](https://img.shields.io/maven-central/v/se.sics.kompics/kompics-scala_2.13)](https://search.maven.org/artifact/se.sics.kompics/kompics-scala_2.13)


A Scala DSL for the [Kompics](http://kompics.sics.se/) message-passing component model for building distributed systems

See the [documentation](http://kompics.sics.se/current/scala/index.html) or [API docs](https://kompics.github.io/kompics-scala/api/se/sics/kompics/sl/index.html) for more information.

### Current Version
`2.0.0` for Scala 2.11, 2.12, and 2.13

#### SBT Dependency
To add Kompics Scala to your project use:
```scala

libraryDependencies += "se.sics.kompics" %% "kompics-scala" % "2.0.+"
```

For the simulation scala DSL also add:
```scala
libraryDependencies += "se.sics.kompics" %% "kompics-scala-simulator" % "2.0.x" // probably % "test" as well
```
