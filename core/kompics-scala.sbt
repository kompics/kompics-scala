name := "Kompics-Scala"

organization := "se.sics.kompics"


version := "1.0.0-SNAPSHOT"

scalaVersion := "2.12.2"

crossScalaVersions := Seq("2.11.11", "2.12.2")

scalacOptions ++= Seq("-deprecation","-feature")


resolvers += Resolver.mavenLocal
resolvers += "Kompics Releases" at "http://kompics.sics.se/maven/repository/"
resolvers += "Kompics Snapshots" at "http://kompics.sics.se/maven/snapshotrepository/"

libraryDependencies += "se.sics.kompics" % "kompics-core" % version.value
libraryDependencies += "org.scala-lang" % "scala-reflect" % scalaVersion.value
libraryDependencies += "org.scalactic" %% "scalactic" % "3.0.3"
libraryDependencies += "org.scalatest" %% "scalatest" % "3.0.3" % "test"
libraryDependencies += "ch.qos.logback" % "logback-classic" % "0.9.28" % "test"

parallelExecution in Test := false

publishMavenStyle := true
//credentials += Credentials(Path.userHome / ".ivy2" / ".credentials")
publishTo <<= version { (v: String) =>
	val kompics = "kompics.i.sics.se"
	val keyFile = Path.userHome / ".ssh" / "id_rsa"
	if (v.trim.endsWith("SNAPSHOT"))
		Some(Resolver.sftp("SICS Snapshot Repository", kompics, "/home/maven/snapshotrepository") as("root", keyFile))
	else
		Some(Resolver.sftp("SICS Release Repository", kompics, "/home/maven/repository") as("root", keyFile))
}
