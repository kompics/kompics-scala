Global View
-----------
Sometimes you might want to observe some state and do something special (e.g., stop the simulation) in case the state matches a special condition that you have considered. For this purpose, the simulator offers a @javadoc:[GlobalView](se.sics.kompics.simulator.util.GlobalView), which allows you to do three things:

1. Check which nodes are dead or alive,
2. set/get key-value-pairs shared globally, and
3. tell the simulator to terminate this simulation.

You can access the @javadoc:[GlobalView](se.sics.kompics.simulator.util.GlobalView) from your config, using the key `"simulation.globalview"`, as in:

@@@ div { .group-java }
```bash
GlobalView gv = config().getValue("simulation.globalview", GlobalView.class);
```
@@@

@@@ div { .group-scala }
```bash
val gv = cfg.getValue[GlobalView]("simulation.globalview");
```
@@@

Obviously, this key is only available while actually running in simulation, so you must take care not access it wrongly when you are sharing code between simulation and deployment.

### Terminate on Condition

So far, all simulation scenarios have terminated at a specific time using @java[`terminateAfterTerminationOf(10000, pinger)`]@scala[`.andThen(10.seconds).afterTermination(Terminate)`]. This time we want to check for certain conditions and terminate when one such condition occurs. In this section, we will to terminate the current simulation when at least 100 pongs have been received by the pingers or when at least 3 nodes have died. 

#### SimulationObserver

To do so, we write an observer that will periodically inspect the global state and check whether any conditions have been met.

Java
:   @@snip [SimulationObserver.java](/docs/src/main/java/jexamples/simulation/pingpongglobal/SimulationObserver.java) {  }

Scala
:   @@snip [SimulationObserver.scala](/docs/src/main/scala/sexamples/simulation/pingpongglobal/SimulationObserver.scala) { }

#### Pinger

We also add a small bit of code to the `Pinger` to record their pongs. We must be careful here, to only access the @javadoc:[GlobalView](se.sics.kompics.simulator.util.GlobalView) when we are actually in simulation mode, because otherwise our program will fail in deployment.

Java
:   @@snip [Pinger.java](/docs/src/main/java/jexamples/simulation/pingpongglobal/Pinger.java) { #ponged }

Scala
:   @@snip [Pinger.scala](/docs/src/main/scala/sexamples/simulation/pingpongglobal/Pinger.scala) { #ponged }

#### ScenarioGen Setup

Since we are using a custom key-value-pair from the @javadoc:[GlobalView](se.sics.kompics.simulator.util.GlobalView), we need to initialize this value before we read it the first time. We can do this from @java[@javadoc:[SetupEvent](se.sics.kompics.simulator.events.system.SetupEvent), by overriding the `setupGlobalView` method]@scala[@scaladoc:[Setup](se.sics.kompics.sl.simulator.Setup$), by initiuating with a function that takes a @javadoc:[GlobalView](se.sics.kompics.simulator.util.GlobalView) instance and modifies it]: 

Java
:   @@snip [ScenarioGen.java](/docs/src/main/java/jexamples/simulation/pingpongglobal/ScenarioGen.java) { #setup }

Scala
:   @@snip [ScenarioGen.scala](/docs/src/main/scala/sexamples/simulation/pingpongglobal/ScenarioGen.scala) { #setup }

#### ScenarioGen Killing Nodes

We also want to be able to kill nodes, specifically `Ponger`s, so we also add an operation for a @java[@javadoc:[KillNodeEvent](se.sics.kompics.simulator.events.system.KillNodeEvent), which requires overriding only the `getNodeAddress` method to identify the node we want to kill]@scala[@scaladoc:[KillNode](se.sics.kompics.sl.simulator.KillNode$), which requires us to pass in the address of the node we want to kill]:

Java
:   @@snip [ScenarioGen.java](/docs/src/main/java/jexamples/simulation/pingpongglobal/ScenarioGen.java) { #kill }

Scala
:   @@snip [ScenarioGen.scala](/docs/src/main/scala/sexamples/simulation/pingpongglobal/ScenarioGen.scala) { #kill }

#### ScenarioGen Simple Ping Scenario

We modify now the old scenario `simplePing`, that starts 5 pongers and 5 pingers and we expect the `SimulationObserver` to terminate it early, when at least 100 pongs have been received. In the case where our observer's termination conditions are not met, for example due to some bug, the simulation might not stop but run forever. To avoid this, we still want to keep the scenario termination time (a very high one) as a safety net. In this case, we added a 10.000s termination time, but the `SimulationObserver` *should* terminate the simulation within a couple of tens of seconds of simulated time.

Java
:   @@snip [ScenarioGen.java](/docs/src/main/java/jexamples/simulation/pingpongglobal/ScenarioGen.java) { #simple-ping }

Scala
:   @@snip [ScenarioGen.scala](/docs/src/main/scala/sexamples/simulation/pingpongglobal/ScenarioGen.scala) { #simple-ping }


#### ScenarioGen Kill Pongers Scenario

The second scenario, `killPongers`, will start killing the pongers, which the observer should notice and then stop the simulation. In this case both conditions -- number of pings and number of dead nodes -- can be met, but for the given code (seed, timing conditions) the number of dead nodes will be met first.

Java
:   @@snip [ScenarioGen.java](/docs/src/main/java/jexamples/simulation/pingpongglobal/ScenarioGen.java) { #kill-pongers }

Scala
:   @@snip [ScenarioGen.scala](/docs/src/main/scala/sexamples/simulation/pingpongglobal/ScenarioGen.scala) { #kill-pongers }

#### Main

We extend our `Main` @java[class]@scala[object] yet again, to differentiate the two simulation we want to run:

Java
:   @@snip [Main.java](/docs/src/main/java/jexamples/simulation/pingpongglobal/Main.java) { #simulation }

Scala
:   @@snip [Main.scala](/docs/src/main/scala/sexamples/simulation/pingpongglobal/Main.scala) { #simulation }

### Execution

Now we can simply run out simulation from within sbt as before with:

#### Simple Ping Scenario

@@@ div { .group-java }
```bash
runMain jexamples.simulation.pingpongglobal.Main simulation simple
```
@@@

@@@ div { .group-scala }
```bash
runMain sexamples.simulation.pingpongglobal.Main simulation simple
```
@@@

#### Kill Pongers Scenario

@@@ div { .group-java }
```bash
runMain jexamples.simulation.pingpongglobal.Main simulation kill
```
@@@

@@@ div { .group-scala }
```bash
runMain sexamples.simulation.pingpongglobal.Main simulation kill
```
@@@

