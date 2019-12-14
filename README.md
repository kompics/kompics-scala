# Kompics Scala DSL
[![Build Status](https://travis-ci.org/kompics/kompics-scala.svg?branch=master)](https://travis-ci.org/kompics/kompics-scala)
[![Download](https://api.bintray.com/packages/kompics/Maven/kompics-scala/images/download.svg)](https://bintray.com/kompics/Maven/kompics-scala/_latestVersion)

A Scala DSL for the [Kompics](http://kompics.sics.se/) message-passing component model for building distributed systems

See the [documentation](http://kompics.sics.se/current/scala/index.html) for more information.

### Current Version
`2.0.0` for Scala 2.11, 2.12, and 2.13

#### SBT Dependency
To add Kompics Scala to your project use:
```scala
resolvers += Resolver.jcenterRepo
resolvers += Resolver.bintrayRepo("kompics", "Maven")

libraryDependencies += "se.sics.kompics" %% "kompics-scala" % "2.0.+"
```

For the simulation scala DSL also add:
[ ![Download](https://api.bintray.com/packages/kompics/Maven/kompics-scala-simulator/images/download.svg) ](https://bintray.com/kompics/Maven/kompics-scala-simulator/_latestVersion)
```scala
libraryDependencies += "se.sics.kompics" %% "kompics-scala-simulator" % "2.0.x" // probably % "test" as well
```
