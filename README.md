# Kompics Scala DSL
[![Build Status](https://travis-ci.org/kompics/kompics-scala.svg?branch=master)](https://travis-ci.org/kompics/kompics-scala)
[![Download](https://api.bintray.com/packages/kompics/Maven/kompics-scala/images/download.svg)](https://bintray.com/kompics/Maven/kompics-scala/_latestVersion)

A Scala DSL for the [Kompics](http://kompics.sics.se/) message-passing component model for building distributed systems

See the [documentation](http://kompics.sics.se/current/scala/index.html) for more information.

### Current Version
`1.0.2` for Scala 2.11 and 2.12

Note that simulations require jdk8 exactly at this point. See [kompics/kompics-simulator#14](https://github.com/kompics/kompics-simulator/issues/14) for tracking this issue.

#### SBT Dependency
To add Kompics Scala to your project use:
```scala
resolvers += Resolver.jcenterRepo
resolvers += Resolver.bintrayRepo("kompics", "Maven")

libraryDependencies += "se.sics.kompics" %% "kompics-scala" % "1.0.+"
```

For the simulation scala DSL also add:
[ ![Download](https://api.bintray.com/packages/kompics/Maven/kompics-scala-simulator/images/download.svg) ](https://bintray.com/kompics/Maven/kompics-scala-simulator/_latestVersion)
```scala
libraryDependencies += "se.sics.kompics" %% "kompics-scala-simulator" % "1.0.+" // probably % "test" as well
```
