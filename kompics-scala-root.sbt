val kompicsV = "1.2.1";
val silencerV = "1.4.4";

val commonSettings = Seq(
  sonatypeCredentialHost := "s01.oss.sonatype.org",
  sonatypeRepository := "https://s01.oss.sonatype.org/service/local",
  publishTo := sonatypePublishToBundle.value,
  resolvers += Resolver.sonatypeRepo("releases"),
  credentials += Credentials(Path.userHome / ".sbt" / ".credentials"),
  organization := "se.sics.kompics",
  version := "2.0.0",
  scalaVersion := "2.13.1",
  crossScalaVersions := Seq("2.11.12", "2.12.10", "2.13.1"),
  scalacOptions ++= Seq("-deprecation", "-feature"), // doesn't with in 2.11 and 2.12 of course "-P:silencer:checkUnused")
  libraryDependencies ++= Seq(
    "se.sics.kompics" % "kompics-core" % kompicsV,
    "com.typesafe.scala-logging" %% "scala-logging" % "3.9.+",
    "org.scala-lang" % "scala-reflect" % scalaVersion.value,
    "org.scalactic" %% "scalactic" % "3.1.0" % "test",
    "org.scalatest" %% "scalatest" % "3.1.0" % "test",
    "ch.qos.logback" % "logback-classic" % "1.2.+" % "test",
    compilerPlugin("com.github.ghik" % "silencer-plugin" % silencerV cross CrossVersion.full),
    "com.github.ghik" % "silencer-lib" % silencerV % Provided cross CrossVersion.full
  ),
  parallelExecution in Test := false,
  licenses += ("GPL-2.0", url("http://www.opensource.org/licenses/gpl-2.0.php")),
  homepage := Some(url("https://kompics.sics.se/kompics-scala")),
  developers := List(Developer(
    id = "lkroll",
    name = "Lars Kroll",
    email = "bathtor@googlemail.com",
    url = url("https://github.com/Bathtor")),
    Developer(
    id = "meldrum",
    name = "Max Meldrum",
    email = "mmeldrum@kth.se",
    url = url("https://github.com/Max-Meldrum"))
  ),
  autoAPIMappings := true,
  apiURL := Some(url("https://kompics.github.io/kompics-scala/api/"))
);

resolvers += Resolver.sonatypeRepo("releases")


lazy val root = (project in file("."))
  .settings(
    commonSettings,
    publish / skip := true,
    name := "Kompics-Scala-Root"
  )
  .aggregate(core, simulator);

lazy val docs = (project in file("docs"))
  .enablePlugins(ScalaUnidocPlugin)
  .enablePlugins(ParadoxPlugin, ParadoxSitePlugin)
  .enablePlugins(GhpagesPlugin)
  .settings(
    commonSettings,
    name := "Kompics-Scala-Docs",
    git.remoteRepo := "git@github.com:kompics/kompics-scala.git",
    ghpagesNoJekyll := true,
    siteSubdirName in ScalaUnidoc := "api",
    addMappingsToSiteDir(mappings in (ScalaUnidoc, packageDoc), siteSubdirName in ScalaUnidoc),
    paradoxProperties += ("scaladoc.base_url" -> "api"),
    paradoxTheme := Some(builtinParadoxTheme("generic"))
  )
  .dependsOn(core, simulator);

lazy val core = (project in file("core"))
  .settings(
    commonSettings,
    name := "Kompics-Scala",
    libraryDependencies += "org.scala-lang.modules" %% "scala-java8-compat" % "0.9.+"
  );

lazy val simulator = (project in file("simulator"))
  .settings(
    commonSettings,
    name := "Kompics-Scala-Simulator",
    libraryDependencies += "se.sics.kompics.simulator" % "core" % kompicsV
  )
  .dependsOn(core);
