name := "Kompics-Scala"

organization := "se.sics.kompics"


version := "1.2.0-SNAPSHOT"

val kompicsVersion = "1.1.0";

scalaVersion := "2.13.1"

crossScalaVersions := Seq("2.11.12", "2.12.10", "2.13.1")

scalacOptions ++= Seq("-deprecation","-feature")


resolvers += Resolver.mavenLocal
resolvers += Resolver.jcenterRepo
resolvers += Resolver.bintrayRepo("kompics", "Maven")

libraryDependencies += "se.sics.kompics" % "kompics-core" % kompicsVersion //version.value
libraryDependencies += "com.typesafe.scala-logging" %% "scala-logging" % "3.9.+"
libraryDependencies += "org.scala-lang" % "scala-reflect" % scalaVersion.value
libraryDependencies += "org.scala-lang.modules" %% "scala-java8-compat" % "0.9.+"
libraryDependencies += "org.scalactic" %% "scalactic" % "3.0.8" % "test"
libraryDependencies += "org.scalatest" %% "scalatest" % "3.0.8" % "test"
libraryDependencies += "ch.qos.logback" % "logback-classic" % "1.2.+" % "test"

parallelExecution in Test := false

bintrayOrganization := Some("kompics")
bintrayRepository := "Maven"
licenses += ("GPL-2.0", url("http://www.opensource.org/licenses/gpl-2.0.php"))
