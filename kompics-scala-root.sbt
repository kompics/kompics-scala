val kompicsV = "1.2.1";
val silencerV = "1.4.4";

val commonSettings = Seq(
  resolvers += Resolver.mavenLocal,
  resolvers += Resolver.jcenterRepo,
  resolvers += Resolver.bintrayRepo("kompics", "Maven"),
  organization := "se.sics.kompics",
  version := "2.0.0",
  scalaVersion := "2.13.1",
  crossScalaVersions := Seq("2.11.12", "2.12.10", "2.13.1"),
  scalacOptions ++= Seq("-deprecation", "-feature"), // doesn't with in 2.11 and 2.12 of course "-P:silencer:checkUnused")
  scalacOptions in (Compile,doc) ++= Seq("-groups", "-implicits"),
  javacOptions ++= Seq("-Xlint:deprecation", "-Xlint:unchecked"),
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
  bintrayOrganization := Some("kompics"),
  bintrayRepository := "Maven",
  licenses += ("GPL-2.0", url("http://www.opensource.org/licenses/gpl-2.0.php")),
  scmInfo := Some(
    ScmInfo(
      url("https://github.com/kompics/kompics-scala"),
      "scm:git:git@github.com:kompics/kompics-scala.git"
    )
  ),
  autoAPIMappings := true,
  apiURL := Some(url("https://kompics.github.io/kompics-scala/api/")),
  fork := true
);

lazy val root = (project in file("."))
  .settings(
    commonSettings,
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
    paradoxProperties ++= Map(
      //"github.base_url" -> "https://github.com/kompics/kompics-scala/tree/master",
      //"github.root.base_dir" -> ".",
      "javadoc.link_style" -> "direct",
      "scaladoc.base_url" -> "api",
      "javadoc.java.base_url" -> "https://docs.oracle.com/en/java/javase/11/docs/api",
      "javadoc.io.netty.base_url" -> "https://netty.io/4.1/api",
      "javadoc.io.netty.link_style" -> "frames",
      "javadoc.se.sics.kompics.base_url" -> s"https://javadoc.io/static/se.sics.kompics/kompics-core/${kompicsV}",
      "javadoc.se.sics.kompics.timer.base_url" -> s"https://javadoc.io/doc/se.sics.kompics.basic/kompics-port-timer/${kompicsV}",
      "javadoc.se.sics.kompics.timer.java.base_url" -> s"https://javadoc.io/doc/se.sics.kompics.basic/kompics-component-java-timer/${kompicsV}",
      "javadoc.se.sics.kompics.network.base_url" -> s"https://javadoc.io/doc/se.sics.kompics.basic/kompics-port-network/${kompicsV}",
      "javadoc.se.sics.kompics.network.netty.base_url" -> s"https://javadoc.io/doc/se.sics.kompics.basic/kompics-component-netty-network/${kompicsV}",
      "javadoc.se.sics.kompics.network.virtual.base_url" -> s"https://javadoc.io/doc/se.sics.kompics.basic/kompics-port-virtual-network/${kompicsV}",
      "javadoc.se.sics.kompics.simulator.base_url" -> s"https://javadoc.io/doc/se.sics.kompics.simulator/core/${kompicsV}"
    ),
    paradoxTheme := Some(builtinParadoxTheme("generic")),
    paradoxProperties += ("project.description" -> "Tutorial and documentation for the Kompics component framework."),
    paradoxProperties ++= Map(
      "kompics.version" -> kompicsV
    ),
    paradoxGroups := Map("Language" -> Seq("Scala", "Java")),
    libraryDependencies ++= Seq(
      "se.sics.kompics.basic" % "kompics-port-timer"  % kompicsV,
      "se.sics.kompics.basic" % "kompics-component-java-timer"  % kompicsV,
      "se.sics.kompics.basic" % "kompics-port-network"  % kompicsV,
      "se.sics.kompics.basic" % "kompics-port-virtual-network"  % kompicsV,
      "se.sics.kompics.basic" % "kompics-component-netty-network"  % kompicsV,
      "ch.qos.logback" % "logback-classic" % "1.2.+"
    )
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
