
Simulation Scenarios Reference
==============================

The simulator comes with quite a number of classes and helper methods for controlling how simulation is run. In this section we will simply list them, to give the reader an idea of what is available.

Simulation Events
-----------------

As already mentioned before, the only events interpreted by the simulator are the following.

1. @javadoc:[SetupEvent](se.sics.kompics.simulator.events.SetupEvent) -- alter global simulation settings
2. @javadoc:[StartNodeEvent](se.sics.kompics.simulator.events.system.StartNodeEvent) -- start a node
3. @javadoc:[KillNodeEvent](se.sics.kompics.simulator.events.system.KillNodeEvent) -- kill a node
4. @javadoc:[TerminateExperiment](se.sics.kompics.simulator.events.TerminateExperiment) -- terminate the experiment
5. @javadoc:[ChangeNetworkModelEvent](se.sics.kompics.simulator.events.system.ChangeNetworkModelEvent) -- change the network model

Creating an operation that produces any other kind of event, will result in a runtime exception.

Distributions
-------------
The main class in this package is @javadoc:[Distribution](se.sics.kompics.simulator.adaptor.distributions.Distribution). There are a number of distributions defined in the simulator module:
	
1. @javadoc:[ConstantDistribution](se.sics.kompics.simulator.adaptor.distributions.ConstantDistribution) -- pass a fixed custom parameter to all events
2. @javadoc:[IntegerUniformDistribution](se.sics.kompics.simulator.adaptor.distributions.IntegerUniformDistribution) -- generate uniformly distributed integers
3. @javadoc:[DoubleUniformDistribution](se.sics.kompics.simulator.adaptor.distributions.DoubleUniformDistribution) -- generate uniformly distributed doubles
4. @javadoc:[LongUniformDistribution](se.sics.kompics.simulator.adaptor.distributions.LongUniformDistribution) -- generate uniformly distributed longs
5. @javadoc:[BigIntegerUniformDistribution](se.sics.kompics.simulator.adaptor.distributions.BigIntegerUniformDistribution) -- generate uniformly distributed big integers
6. @javadoc:[IntegerNormalDistribution](se.sics.kompics.simulator.adaptor.distributions.IntegerNormalDistribution) -- generate normally distributed integers
7. @javadoc:[DoubleNormalDistribution](se.sics.kompics.simulator.adaptor.distributions.DoubleNormalDistribution) -- generate normally distributed doubles
8. @javadoc:[LongNormalDistribution](se.sics.kompics.simulator.adaptor.distributions.LongNormalDistribution) -- generate normally distributed longs
9. @javadoc:[BigIntegerNormalDistribution](se.sics.kompics.simulator.adaptor.distributions.BigIntegerNormalDistribution) -- generate normally distributed big integers
10. @javadoc:[IntegerExponentialDistribution](se.sics.kompics.simulator.adaptor.distributions.IntegerExponentialDistribution) -- generate exponentially distributed integers
11. @javadoc:[DoubleExponentialDistribution](se.sics.kompics.simulator.adaptor.distributions.DoubleExponentialDistribution) -- generate exponentially distributed doubles
12. @javadoc:[LongExponentialDistribution](se.sics.kompics.simulator.adaptor.distributions.LongExponentialDistribution) -- generate exponentially distributed longs
13. @javadoc:[BigIntegerExponentialDistribution](se.sics.kompics.simulator.adaptor.distributions.BigIntegerExponentialDistribution) -- generate exponentially distributed big integers
14. @javadoc:[BasicIntSequentialDistribution](se.sics.kompics.simulator.adaptor.distributions.extra.BasicIntSequentialDistribution) -- generate a predefined sequence `N, N+1, N+2, ...` for parameter `N`
15. @javadoc:[GenIntSequentialDistribution](se.sics.kompics.simulator.adaptor.distributions.extra.GenIntSequentialDistribution) -- genrate a predefined sequence as given in the constructor. Make sure you do not try to raise more events than the size of the provided sequence, as once all the numbers in the sequence have been drawn, `null` values will be returned.

@java[Many of these distributions have convenience methods within a @javadoc:[SimulationScenario](se.sics.kompics.simulator.SimulationScenario) subclass.]@scala[All these distributions have convenient helpers in @scaladoc:[Distributions](se.sics.kompics.sl.simulator.Distributions$).]

In case these distributions to not fit your needs, you can define your own by extending the @javadoc:[Distribution](se.sics.kompics.simulator.adaptor.distributions.Distribution) class

Operations
----------
Depending on the amount of customisation you need for the simulation events, there are 6 types of operations allowing between 0 and 5 generated parameters. Each parameter is generated using some distribution you must provide. You can choose among the distributions presented above or create your own parameter distribution, which fits your needs better.

1. @javadoc:[Operation](se.sics.kompics.simulator.adaptor.Operation) @scala[-- created via @scaladoc:[Op{(_: Unit) => SomeEvent}](se.sics.kompics.sl.simulator.Op$)]
2. @javadoc:[Operation1](se.sics.kompics.simulator.adaptor.Operation1) @scala[-- created via @scaladoc:[Op{(param: DistributionOutputType) => SomeEvent}](se.sics.kompics.sl.simulator.Op$)]
3. @javadoc:[Operation2](se.sics.kompics.simulator.adaptor.Operation2) @scala[-- created via @scaladoc:[Op{(param1: Distribution1OutputType, param2: Distribution2OutputType) => SomeEvent}](se.sics.kompics.sl.simulator.Op$)]
4. @javadoc:[Operation3](se.sics.kompics.simulator.adaptor.Operation3) @scala[-- and so on...]
5. @javadoc:[Operation4](se.sics.kompics.simulator.adaptor.Operation4)
6. @javadoc:[Operation5](se.sics.kompics.simulator.adaptor.Operation5)

StochasticProcesses
-------------------

@@@ div { .group-java }

Event generation related methods:

1. @javadoc:[eventInterarrivalTime](se.sics.kompics.simulator.SimulationScenario.StochasticProcess#eventInterarrivalTime(se.sics.kompics.simulator.adaptor.distributions.Distribution))
2. @javadoc:[raise](se.sics.kompics.simulator.SimulationScenario.StochasticProcess#raise(int,se.sics.kompics.simulator.adaptor.Operation))

Process order related methods: @javadoc:[start](se.sics.kompics.simulator.SimulationScenario.StochasticProcess#start()), @javadoc:[terminate](se.sics.kompics.simulator.SimulationScenario#terminateAt(long)).

@@@

@@@ div { .group-scala }

The DSL methods for creating stochastic processes from operations and then combining them into simulation scenarios can be found in @github[StochasticProcess.scala](/simulator/src/main/scala/se/sics/kompics/sl/simulator/StochasticProcess.scala).

@@@

