Getting Started
===============

Prerequisites
-------------
Kompics requires that you have a JDK on your machine.
Since Kompics `1.1.0` at least Java 8 is required, and any newer release should work fine.

Including Kompics
-----------------
The best way to include Kompics is to use a build tool, such as @link:[SBT](https://www.scala-sbt.org/) or @link:[Maven](https://maven.apache.org/), and add the required Kompics modules as a dependency.

### Modules

Kompics is very modular and consists of several sub-projects, containing different features, ports, and components.

#### Kompics Core
The `kompics-core` module contains Kompics' Java runtime and the basic definitions. It is the minimum requiremed dependency in Kompics Java.

@@dependency[sbt,Maven,Gradle] {
  group="se.sics.kompics"
  artifact="kompics-core"
  version=$kompics.version$
}

#### Kompics Scala
The `kompics-scala` module provides the core Scala DSL for Kompics. Projects including it as a dependency should elide the `kompics-core` dependency above, as it is automatically pulled in as a transitive dependency in the correct version.

@@dependency[sbt,Maven,Gradle] {
  group="se.sics.kompics"
  artifact="kompics-scala_2.13"
  version=$project.version$
}

#### Timer
The `kompics-port-timer` module provides the definition of a port that provides scheduled timeouts. An implementation of that port can be found in the `kompics-component-java-timer` module.

@@dependency[sbt,Maven,Gradle] {
  group="se.sics.kompics.basic" artifact="kompics-port-timer" version=$kompics.version$
  group2="se.sics.kompics.basic" artifact2="kompics-component-java-timer" version2=$kompics.version$
}

#### Network
The `kompics-port-network` module provides the definition of a port that provides networking capabilities, that is sending of addressed messages over network protocols. An implementation of that port using the @link:[Netty library](https://netty.io/) can be found in the `kompics-component-netty-network` module. It provides networking via TCP, UDP, and UDT.

@@dependency[sbt,Maven,Gradle] {
  group="se.sics.kompics.basic" artifact="kompics-port-network" version=$kompics.version$
  group2="se.sics.kompics.basic" artifact2="kompics-component-netty-network" version2=$kompics.version$
}

Additionally, the kompics-port-virtual-network adds a few definitions to the network port to allow for the usage of *virtual nodes* within the same Kompics instance.

@@dependency[sbt,Maven,Gradle] {
  group="se.sics.kompics.basic"
  artifact="kompics-port-virtual-network"
  version=$kompics.version$
}

#### Simulator
Kompics comes with a DSL to describe experiment/test scenarios and a scheduler and base components to run them. These can be found in the Kompics Simulator module.

@@dependency[sbt,Maven,Gradle] {
  group="se.sics.kompics.simulator"
  artifact="core"
  version=$kompics.version$
}

As setting up simulation scenarios can be quite verbose using the Java API, there is also a Scala DSL for the simulator, that makes creation of simulations a bit more streamlined.

@@dependency[sbt,Maven,Gradle] {
  group="se.sics.kompics"
  artifact="kompics-scala-simulator"
  version=$project.version$
}

@@@ note
Implementations always depend on the modules defining the port types they implement. Thus it is sufficient to add the implementation as a dependency. The separation is meant to allow different implementations for the same port type.
@@@


IDE Support
-----------
Kompics (Java) can be used with any IDE that supports Java and maven, such as @link:[IntelliJ](https://www.jetbrains.com), @link:[Eclipse](https://www.eclipse.org/), or @link:[Netbeans](https://netbeans.org/), for example.

Kompics Scala can be used with any IDE that supports Scala, such as @link:[IntelliJ](https://www.jetbrains.com/help/idea/discover-intellij-idea-for-scala.html), @link:[Scala IDE](http://scala-ide.org/), or @link:[VSCode](https://code.visualstudio.com/) via @link:[Metals](https://scalameta.org/metals/).

Build from Sources
------------------
The sources for Kompics Scala are hosted on [Github](https://github.com/kompics).

To clone the sources you need @link:[Git](http://git-scm.com) installed on your machine.

### Kompics Java from Source
To clone Kompics Java, for example, execute the following in a shell:
```bash
git clone git@github.com:kompics/kompics.git
```

If you have already cloned the repository previously then you can update the code with git pull:
```bash
git pull origin master
```

Change into the `kompics` root directory and execute the following command to build and install a local Kompics version:
```bash
mvn clean install
```

@@@ note
Some of the tests take a very long time to execute and open a lot of file-descriptors. Depending on the rights on your machine, they might in fact fail for you. Either increase the number of concurrent open file-handles allowed for your user, or simply skip the tests with `-DskipTests`.
@@@

### Kompics Scala from Source
To clone Kompics Scala, for example, execute the following in a shell:
```bash
git clone git@github.com:kompics/kompics-scala.git
```

If you have already cloned the repository previously then you can update the code with git pull:
```bash
git pull origin master
```

Change into the `kompics-scala` root directory and execute the following command to build and install a local Kompics Scala version:
```bash
sbt publishLocal
```
