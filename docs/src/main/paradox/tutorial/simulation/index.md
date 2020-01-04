@@@ index
* [Introduction to Simulation](introduction.md)
* [Ping Pong](pingpong.md)
* [Global View](globalview.md)
* [Sources of Randomness](randsources.md)
* [Scenarios](scenarios.md)
@@@

Simulation
==========
We will now show how the same implementation of a distributed system, which is designed for production deployment, is also executable in simulation mode for stepped debugging, protocol correctness testing, or for repeatable studies of the dynamic behaviour of large-scale peer-to-peer systems.

In order to allow the debugging of code, we require deterministic execution during simulation. We achieve this by executing all events and messages in a deterministic fashion and, additionally, removing other sources of nondeterminism. 

Possible sources of nondeterminism include:

1. system time
2. random generators
3. thread creation/execution
4. network, e.g. latency, packet loss
5. input/output sources

At a high level, a simulation experiment is specified as a @javadoc:[SimulationScenario](se.sics.kompics.simulator.SimulationScenario) class, that describes the occurence of events such as starting/stopping a node, changing the parameters of the network, or terminating the simulation. Such a @javadoc:[SimulationScenario](se.sics.kompics.simulator.SimulationScenario) is then executed by the @javadoc:[P2pSimulator](se.sics.kompics.simulator.core.impl.P2pSimulator) in a single-threaded, deterministic fashion.

### Dependencies

The examples in this tutorial require a new dependency, which is different depending whether you are using Java or Scala.

#### Kompics Simulator
The `kompics-simulator` module contains the core definitions and the runtime for the Kompics Simulator framework. It is the required dependency in Kompics Java.

@@dependency[sbt,Maven,Gradle] {
  group="se.sics.kompics.simulator"
  artifact="kompics-simulator"
  version=$kompics.version$
}

#### Kompics Scala Simulator
The `kompics-scala-simulator` module provides the Scala DSL for Kompics Simulator. Like with Kompics Core, projects including it as a dependency should elide the `kompics-simulator` dependency above, as it is automatically pulled in as a transitive dependency in the correct version.

@@dependency[sbt,Maven,Gradle] {
  group="se.sics.kompics.sl"
  artifact="kompics-scala-simulator_2.13"
  version=$project.version$
}

### Contents

@@toc { depth=2 }
