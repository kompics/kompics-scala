name := "Kompics-Scala"

organization := "se.sics.kompics"

version := "0.9.1-SNAPSHOT"

scalaVersion := "2.11.8"

scalacOptions ++= Seq("-deprecation","-feature")

resolvers += Resolver.mavenLocal

//resolvers := Resolver.mavenLocal +: resolvers.value

libraryDependencies += "se.sics.kompics" % "kompics-core" % "0.9.2-SNAPSHOT"
libraryDependencies += "org.scala-lang" % "scala-reflect" % scalaVersion.value
libraryDependencies += "org.scalactic" %% "scalactic" % "2.2.6"
libraryDependencies += "org.scalatest" %% "scalatest" % "2.2.6" % "test"
libraryDependencies += "ch.qos.logback" % "logback-classic" % "0.9.28" % "test"

parallelExecution in Test := false

//mainClass in assembly := Some("se.kth.climate.fast.dtr.DTR")

//assemblyOption in assembly := (assemblyOption in assembly).value.copy(includeScala = false)