.. _scenarios:

Simulation Scenarios Reference
==============================

Simulation Events
-----------------

	#. :java:ref:`se.sics.kompics.simulator.events.system.SetupEvent`
	#. :java:ref:`se.sics.kompics.simulator.events.system.StartNodeEvent`
	#. :java:ref:`se.sics.kompics.simulator.events.system.KillNodeEvent`
	#. :java:ref:`se.sics.kompics.simulator.events.system.ChangeNetworkModelEvent`

Distributions
-------------
	The main class in this package is :java:ref:`se.sics.kompics.simulator.adaptor.distributions.Distribution`. There are a number of distributions defined in the simulator module:
	
	#. :java:ref:`se.sics.kompics.simulator.adaptor.distributions.ConstantDistribution`
	#. :java:ref:`se.sics.kompics.simulator.adaptor.distributions.IntegerUniformDistribution`
	#. :java:ref:`se.sics.kompics.simulator.adaptor.distributions.DoubleUniformDistribution`
	#. :java:ref:`se.sics.kompics.simulator.adaptor.distributions.LongUniformDistribution`
	#. :java:ref:`se.sics.kompics.simulator.adaptor.distributions.BigIntegerUniformDistribution`	
	#. :java:ref:`se.sics.kompics.simulator.adaptor.distributions.IntegerNormalDistribution`
	#. :java:ref:`se.sics.kompics.simulator.adaptor.distributions.DoubleNormalDistribution`
	#. :java:ref:`se.sics.kompics.simulator.adaptor.distributions.LongNormalDistribution`
	#. :java:ref:`se.sics.kompics.simulator.adaptor.distributions.BigIntegerNormalDistribution`
	#. :java:ref:`se.sics.kompics.simulator.adaptor.distributions.IntegerExponentialDistribution`
	#. :java:ref:`se.sics.kompics.simulator.adaptor.distributions.DoubleExponentialDistribution`
	#. :java:ref:`se.sics.kompics.simulator.adaptor.distributions.LongExponentialDistribution`
	#. :java:ref:`se.sics.kompics.simulator.adaptor.distributions.BigIntegerExponentialDistribution`
	#. :java:ref:`se.sics.kompics.simulator.adaptor.distributions.extra.BasicIntSequentialDistribution`
	#. :java:ref:`se.sics.kompics.simulator.adaptor.distributions.extra.GenIntSequentialDistribution`

The ``ConstantDistribution``, as the name suggests is used when we want to pass a custom parameter to the event, but this custom parameter is fixed for all events.

Distributions between 2 and 13 will generate uniform/normal/exponential distribution random numbers.

The ``SequentialDistributions`` are similar to the ``ConstantDistribution`` as they do not generate random numbers, but a predefined sequence. The ``BasicIntSequentialDistribution`` generates integers from the starting point provided in the constructor. For the ``GenIntSequentialDistribution``, the whole sequence is provided in the constructor. Make sure you do not try to raise more events than the size of this sequence, as once all the numbers in the sequence have been drawn, *null* values will be returned.

In case these distributions to not fit your needs, you can define your own by extending the ``Distribution`` class

Operations
----------
Depending on the amount of customization you need for the simulation events, there are 6 types of operations allowing from 0 to 5 generated parameters. The parameters are generated using a distribution. You can choose among the distributions presented above or create your own parameter distribution that fits your needs better.

	#. :java:ref:`se.sics.kompics.simulator.adaptor.Operation`
	#. :java:ref:`se.sics.kompics.simulator.adaptor.Operation1`
	#. :java:ref:`se.sics.kompics.simulator.adaptor.Operation2`
	#. :java:ref:`se.sics.kompics.simulator.adaptor.Operation3`
	#. :java:ref:`se.sics.kompics.simulator.adaptor.Operation4`
	#. :java:ref:`se.sics.kompics.simulator.adaptor.Operation5`

StochasticProcesses
-------------------
Event generation related methods:

	#. :java:ref:`se.sics.kompics.simulator.SimulationScenario.StochasticProcess.eventInterArrivalTime`
	#. :java:ref:`se.sics.kompics.simulator.SimulationScenario.StochasticProcess.raise`

Process order related methods ``start..``, ``terminate``
