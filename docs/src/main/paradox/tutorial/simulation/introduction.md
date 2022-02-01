
Introduction to Simulation
--------------------------

In order to use the simulator, we must first write a simulation scenario. A scenario is a parallel and/or sequential composition of *stochastic processes*, which is a finite random sequence of events. We invoke a stochastic process with a specified distribution of inter-arrival times. In other words, stochastic processes define series of events that occur in simulation.

### Stochastic Processes and Operations

The following snippet of code creates a single stochastic process that will generate 1000 events of type `SimpleEvent` with an interarrival time of 2000ms (or 2s) of *simulation time*.

Java
:   @@snip [BasicSimulation.java](/docs/src/main/java/jexamples/simulation/basic/BasicSimulation.java) { #gen1 }

Scala
:   @@snip [BasicSimulation.scala](/docs/src/main/scala/sexamples/simulation/basic/BasicSimulation.scala) { #gen1 }


However, in the example above, all events are exactly the same; there is no variation to the 1000 events generated. We can generate events that differ slightly, by having the event take an integer parameter and generating this parameter randomly.

Java
:   @@snip [BasicSimulation.java](/docs/src/main/java/jexamples/simulation/basic/BasicSimulation.java) { #gen2 }

Scala
:   @@snip [BasicSimulation.scala](/docs/src/main/scala/sexamples/simulation/basic/BasicSimulation.scala) { #gen2 }

The new events generated have a parameter that takes uniform long random values between 1000 and 2000. Both the interarrival time and the event parameters are defined as instances of @javadoc:[Distribution](se.sics.kompics.simulator.adaptor.distributions.Distribution), and the @java[@javadoc:[constant](se.sics.kompics.simulator.SimulationScenario#constant(long))]@scala[@scaladoc:[constant](se.sics.kompics.sl.simulator.Distributions$#constant(Long))] and @java[@javadoc:[uniform](se.sics.kompics.simulator.SimulationScenario#uniform(long,long))]@scala[@scaladoc:[uniform](se.sics.kompics.sl.simulator.Distributions$#uniform(Long,Long))] are just helper methods to generate these distributions. 

There are several types of operations (@javadoc:[Operation](se.sics.kompics.simulator.adaptor.Operation), @javadoc:[Operation1](se.sics.kompics.simulator.adaptor.Operation1), ..., @javadoc:[Operation5](se.sics.kompics.simulator.adaptor.Operation5)) that can be interpreted by the simulator, and they differ in the number of parameters they take to customise the generated events. All parameters are given as distributions. There are a number of distributions provided by the simulator, or you can write your own distribution by extending @javadoc:[Distribution](se.sics.kompics.simulator.adaptor.distributions.Distribution). 

In order to start the stochastic processes and to define the iterative/parallel behaviour, @java[the @javadoc:[StochasticProcess](se.sics.kompics.simulator.SimulationScenario.StochasticProcess) has a number of start/terminate methods]@scala[a @scaladoc[builder DSL](se.sics.kompics.sl.simulator.StochasticProcessBuilder) is provided].

If we want to start the two stochastic processes above, a possible example would be:

Java
:   @@snip [BasicSimulation.java](/docs/src/main/java/jexamples/simulation/basic/BasicSimulation.java) { #scenario }

Scala
:   @@snip [BasicSimulation.scala](/docs/src/main/scala/sexamples/simulation/basic/BasicSimulation.scala) { #scenario }


The above example would start the first stochastic process at the beginning of the simulation. After all 1000 if its events are generated, the simulation waits another 1000ms of simulation time and then starts the second stochastic process. After generating all 1000 of its defined events and waiting another 2000ms of simulation time, the simulation is terminated.

### Simluation Scenario

The stochastic processes created and their order will be defined within a @javadoc:[SimulationScenario](se.sics.kompics.simulator.SimulationScenario), such as the on the in the following @java[class]@scala[object].

#### Basic Scenario

Java
:   @@snip [BasicSimulation.java](/docs/src/main/java/jexamples/simulation/basic/BasicSimulation.java) { filterLabels=true }

Scala
:   @@snip [BasicSimulation.scala](/docs/src/main/scala/sexamples/simulation/basic/BasicSimulation.scala) { filterLabels=true }


### Running a Simulation Scenario

In order to run the simulation scenario, the following `Main` code is required:

Java
:   @@snip [Main.java](/docs/src/main/java/jexamples/simulation/basic/Main.java) { }

Scala
:   @@snip [Main.scala](/docs/src/main/scala/sexamples/simulation/basic/Main.scala) { }

In the above code, we set the simulation scenario *seed* (more about this seed in later sections), we construct the simulation scenario and we use the @javadoc:[LauncherComp](se.sics.kompics.simulator.run.LauncherComp) to execute it.

We can attempt to run this using:

@@@ div { .group-java }
```bash
runMain jexamples.simulation.basic.Main
```
@@@

@@@ div { .group-scala }
```bash
runMain sexamples.simulation.basic.Main
```
@@@

However, the execution will fail with an exception indicating that we are triggering an invalid event. In fact, the only events that can be generated by stochastic processes and interpreted by the simulator are:

1. @javadoc:[SetupEvent](se.sics.kompics.simulator.events.SetupEvent)
2. @javadoc:[StartNodeEvent](se.sics.kompics.simulator.events.system.StartNodeEvent)
3. @javadoc:[KillNodeEvent](se.sics.kompics.simulator.events.system.KillNodeEvent)
4. @javadoc:[TerminateExperiment](se.sics.kompics.simulator.events.TerminateExperiment)
5. @javadoc:[ChangeNetworkModelEvent](se.sics.kompics.simulator.events.system.ChangeNetworkModelEvent)

You can read more about operations, distributions and stochastic processes in the section on @ref:[Scenarios](scenarios.md).
