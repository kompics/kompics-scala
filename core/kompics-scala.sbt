enablePlugins(SiteScaladocPlugin)
enablePlugins(ParadoxPlugin, ParadoxSitePlugin)
enablePlugins(GhpagesPlugin)

name := "Kompics-Scala"

organization := "se.sics.kompics"

version := "2.0.0"

scalaVersion := "2.13.1"

crossScalaVersions := Seq("2.11.12", "2.12.10", "2.13.1")

scalacOptions ++= Seq("-deprecation", "-feature") // doesn't with in 2.11 and 2.12 of coursse "-P:silencer:checkUnused")

val kompicsV = "1.2.0";
val silencerV = "1.4.4";

resolvers += Resolver.mavenLocal
resolvers += Resolver.jcenterRepo
resolvers += Resolver.bintrayRepo("kompics", "Maven")

libraryDependencies += "se.sics.kompics" % "kompics-core" % kompicsV
libraryDependencies += "com.typesafe.scala-logging" %% "scala-logging" % "3.9.+"
libraryDependencies += "org.scala-lang" % "scala-reflect" % scalaVersion.value
libraryDependencies += "org.scala-lang.modules" %% "scala-java8-compat" % "0.9.+"
libraryDependencies += "org.scalactic" %% "scalactic" % "3.1.0" % "test"
libraryDependencies += "org.scalatest" %% "scalatest" % "3.1.0" % "test"
libraryDependencies += "ch.qos.logback" % "logback-classic" % "1.2.+" % "test"
libraryDependencies ++= Seq(
  compilerPlugin("com.github.ghik" % "silencer-plugin" % silencerV cross CrossVersion.full),
  "com.github.ghik" % "silencer-lib" % silencerV % Provided cross CrossVersion.full
)

parallelExecution in Test := false

bintrayOrganization := Some("kompics")
bintrayRepository := "Maven"
licenses += ("GPL-2.0", url("http://www.opensource.org/licenses/gpl-2.0.php"))

git.remoteRepo := "git@github.com:kompics/kompics-scala.git"
ghpagesNoJekyll := true

SiteScaladoc / siteSubdirName := "api"
paradoxProperties += ("scaladoc.base_url" -> "api")
paradoxTheme := Some(builtinParadoxTheme("generic"))

autoAPIMappings := true

apiURL := Some(url("https://kompics.github.io/kompics-scala/api/"))
