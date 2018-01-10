name := "Kompics-Scala-Simulator"

organization := "se.sics.kompics"

version := "1.0.0"

scalaVersion := "2.12.4"

crossScalaVersions := Seq("2.11.11", "2.12.4")

scalacOptions ++= Seq("-deprecation","-feature")



resolvers += "Kompics Releases" at "http://kompics.sics.se/maven/repository/"
resolvers += "Kompics Snapshots" at "http://kompics.sics.se/maven/snapshotrepository/"
//resolvers += Resolver.mavenLocal

libraryDependencies += "se.sics.kompics" %% "kompics-scala" % "1.0.+"
libraryDependencies += "se.sics.kompics.simulator" % "core" % "1.0.+"
libraryDependencies += "org.scala-lang" % "scala-reflect" % scalaVersion.value
libraryDependencies += "org.scalactic" %% "scalactic" % "3.0.3"
libraryDependencies += "org.scalatest" %% "scalatest" % "3.0.3" % "test"
libraryDependencies += "com.typesafe.scala-logging" %% "scala-logging" % "3.7.1"
libraryDependencies += "ch.qos.logback" % "logback-classic" % "1.2.3" % "test"

parallelExecution in Test := false

publishMavenStyle := true
//credentials += Credentials(Path.userHome / ".ivy2" / ".credentials")
publishTo := {
  val kompics = "kompics.i.sics.se"
  val keyFile = Path.userHome / ".ssh" / "id_rsa"
  if (version.value.trim.endsWith("SNAPSHOT"))
    Some(Resolver.sftp("SICS Snapshot Repository", kompics, "/home/maven/snapshotrepository") as("root", keyFile))
  else
    Some(Resolver.sftp("SICS Release Repository", kompics, "/home/maven/repository") as("root", keyFile))
}
