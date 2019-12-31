.. _simulation:

Simulation
==========
We now show how the same implementation of a distributed system, which is designated for production deployment, is also executable in simulation mode for stepped debugging, protocol correctness testing, or for repeatable studies of the dynamic behaviour of large-scale peer-to-peer systems.

In order to allow the debugging of code, we require deterministic execution during simulation. We achieve this by executing all events and messages in a deterministic fashion and also removing other sources of randomness. 

Possible sources of randomness include:

	#. system time
	#. random generators
	#. thread creation/execution
	#. network e.g. latency, packet loss
	#. input/output sources

A simulation experiment is specified as a ``SimulationScenario`` class that describes the occurence of events such as starting/stopping a node, changing the parameters of the network, or terminating the simulation. Such a ``SimulationScenario`` is then executed by the ``P2pSimulator`` in a single-threaded, deterministic fashion.

.. toctree::
   :maxdepth: 3

   pingpong/pingpong
   randsources/randsources
   scenarios/scenarios
