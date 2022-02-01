
PingPong Simulation
-------------------

Starting from the @ref[Distributed PingPong](../networking/basic/cleanup.md) example, in this section we will see how we can run the pinger/ponger configuration as a simulation.

### Parent vs. Host

The first change we want to do in order to be able to use the deploy code in simulation (with minimal changes), is use a `Parent` and `Host` division. 
The `Parent` will connect all subcomponents of the system between themselves and to a @javadoc:[Network](se.sics.kompics.network.Network) and @javadoc:[Timer](se.sics.kompics.timer.Timer) port. 

In deployment, the `Host` will create the @javadoc:[Timer](se.sics.kompics.timer.Timer) and @javadoc:[Network](se.sics.kompics.network.Network) instances and connect them to the `Parent`, while in simulation, the *simulator* will provide the @javadoc:[Timer](se.sics.kompics.timer.Timer) and @javadoc:[Network](se.sics.kompics.network.Network), instead, and will connect them to the `Parent`.

#### PingerParent

Java
:   @@snip [PingerParent.java](/docs/src/main/java/jexamples/simulation/pingpong/PingerParent.java) {  }

Scala
:   @@snip [Parent.scala](/docs/src/main/scala/sexamples/simulation/pingpong/Parent.scala) { #pinger-parent }

#### PingerHost

Java
:   @@snip [PingerHost.java](/docs/src/main/java/jexamples/simulation/pingpong/PingerHost.java) {  }

Scala
:   @@snip [Parent.scala](/docs/src/main/scala/sexamples/simulation/pingpong/Parent.scala) { #pinger-host }

#### PongerParent

Java
:   @@snip [PongerParent.java](/docs/src/main/java/jexamples/simulation/pingpong/PongerParent.java) {  }

Scala
:   @@snip [Parent.scala](/docs/src/main/scala/sexamples/simulation/pingpong/Parent.scala) { #ponger-parent }

#### PongerHost

Java
:   @@snip [PongerHost.java](/docs/src/main/java/jexamples/simulation/pingpong/PongerHost.java) {  }

Scala
:   @@snip [Parent.scala](/docs/src/main/scala/sexamples/simulation/pingpong/Parent.scala) { #ponger-host }


### Simulation Scenario

In the simulation scenario we define two types of @java[@javadoc:[StartNodeEvent](se.sics.kompics.simulator.events.system.StartNodeEvent)]@scala[@scaladoc:[StartNode](se.sics.kompics.sl.simulator.StartNode$)] events for `PongerParent` and `PingerParent`. @java[The methods that are required to be overridden are very simple, as they are getter methods for the class of the respective parent, an @javadoc:[Init](se.sics.kompics.Init) event for it, and the node's `TAddress`]@scala[Similar to direct component creation, we must specify the type of the component to be created, its @javadoc:[Init](se.sics.kompics.Init) event, and also an address for it]. We are going to pass addresses in the config as a simple map, which the simulator will convert into a config update instance, when creating the components.

The scenario is set to start 5 ponger nodes and 5 pinger nodes. The sequential distributions will give IPs ending in x={1,2,3,4,5} to the pongers and IPs ending in y={6,7,8,9,10} to pingers and the <x,y> relation is: {<1,6>, <2, 7>, <3,8>, <4,9>, <5,10>} (or simply y = x+5).

#### SimulationGen

Java
:   @@snip [ScenarioGen.java](/docs/src/main/java/jexamples/simulation/pingpong/ScenarioGen.java) {  }

Scala
:   @@snip [ScenarioGen.scala](/docs/src/main/scala/sexamples/simulation/pingpong/ScenarioGen.scala) { }

#### Main

In order to start our simulation, we are going to add another branch to our `Main` @java[class]@scala[object], allowing us to specify `simulation` as a command line parameter, as well.

Java
:   @@snip [Main.java](/docs/src/main/java/jexamples/simulation/pingpong/Main.java) { #simulation }

Scala
:   @@snip [Main.scala](/docs/src/main/scala/sexamples/simulation/pingpong/Main.scala) { #simulation }

### Execution

Now we can simply run out simulation from within sbt as before with:

@@@ div { .group-java }
```bash
runMain jexamples.simulation.pingpong.Main simulation
```
@@@

@@@ div { .group-scala }
```bash
runMain sexamples.simulation.pingpong.Main simulation
```
@@@
