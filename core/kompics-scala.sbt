name := "Kompics-Scala"

organization := "se.sics.kompics"


version := "1.0.1"

scalaVersion := "2.12.8"

crossScalaVersions := Seq("2.11.12", "2.12.8")

scalacOptions ++= Seq("-deprecation","-feature")


resolvers += Resolver.mavenLocal
resolvers += Resolver.jcenterRepo
resolvers += Resolver.bintrayRepo("kompics", "Maven")

libraryDependencies += "se.sics.kompics" % "kompics-core" % "1.0.+"
libraryDependencies += "com.typesafe.scala-logging" %% "scala-logging" % "3.9.+"
libraryDependencies += "org.scala-lang" % "scala-reflect" % scalaVersion.value
libraryDependencies += "org.scalactic" %% "scalactic" % "3.0.+"
libraryDependencies += "org.scalatest" %% "scalatest" % "3.0.+" % "test"
libraryDependencies += "ch.qos.logback" % "logback-classic" % "1.2.+" % "test"

parallelExecution in Test := false

bintrayOrganization := Some("kompics")
bintrayRepository := "Maven"
licenses += ("GPL-2.0", url("http://www.opensource.org/licenses/gpl-2.0.php"))
