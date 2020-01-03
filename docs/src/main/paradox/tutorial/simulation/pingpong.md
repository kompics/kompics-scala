PingPong Simulation
-------------------
Starting from the :ref:`distributedpingpong`, in this section we will see how we can run the pinger/ponger configuration as a simulation. As stated in the previous section, do not forget to include the simulator dependency in your pom file.

First change we want to do in order to be able to use the deploy code in simulation (with minimal changes), is use a ``Parent`` and ``Host`` division. The ``Parent`` will typically connect all subcomponents of the system between themselves and to a ``Network`` and ``Timer`` port. 

In deployment, the ``Host`` will create the ``Timer`` and ``Network`` instances and connect them to the ``Parent``, while in simulation, the Simulator will provide the ``Timer`` and ``Network`` and will connect them to the ``Parent``.

.. literalinclude:: sim-pingpong-distributed/src/main/java/se/sics/test/system/PingerParent.java
.. literalinclude:: sim-pingpong-distributed/src/main/java/se/sics/test/system/PingerHost.java
.. literalinclude:: sim-pingpong-distributed/src/main/java/se/sics/test/system/PongerParent.java
.. literalinclude:: sim-pingpong-distributed/src/main/java/se/sics/test/system/PongerHost.java

In the simulation scenario we define two types of ``StartNodeEvent`` events for `Ponger` and `Pinger`. The methods required to be overridden are very simple as they are getter methods for the ``Parent`` ``ComponentDefinition``, ``Init`` and the node ``Address``.
The scenario is set to start 5 ponger nodes and 5 pinger nodes. The sequential distributions will give IPs ending in: :math:`x=\{1,2,3,4,5\}` to the pongers and IPs ending in: :math:`y=\{6,7,8,9,10\}` to pingers and the <x,y> relation is: {<1,6>, <2, 7>, <3,8>, <4,9>, <5,10>} (or :math:`y = x+5`).

.. literalinclude:: sim-pingpong-distributed/src/main/java/se/sics/test/sim/ScenarioGen.java
.. literalinclude:: sim-pingpong-distributed/src/main/java/se/sics/test/sim/ScenarioLauncher.java

.. note::

	In some cases, you might start the simulation and the only output you get are some warnings from log4j, including:
	
	.. code-block:: none
	
		log4j:WARN No appenders could be found for logger (CodeInstrumentation).
		log4j:WARN Please initialize the log4j system properly.

	In the case you should check your `log4j.properties` files as it might be missing or miss-configured.


Remember to set the following in your `log4j.properties` files, so that the logger output is manageable and related to your logs only.

.. code-block:: none

	log4j.logger.Kompics=WARN
	log4j.logger.se.sics.kompics.simulator.core.impl.P2pSimulator=WARN
	log4j.logger.se.sics.kompics.simulator.core.impl.SimulatorMngrComp=WARN
	log4j.logger.CodeInstrumentation=WARN

When debugging, if the source of error is not clear, you might want to turn the loggers above to TRACE. The order should be:

	#. SimulatorMngrComp - to see if the stochastic process generated events order is as expected.
	#. P2pSimulator - to see if network messages and timeouts occur as expected
	#. Kompics - mostly `Component` control events
	#. CodeInstrumentation - only if you declared instrumentation exceptions(advanced).

The code until here can be downloaded :download:`here <sim-pingpong-distributed.zip>`.

Configuration
-------------
We will now change the code to :ref:`netcleanup` version and try to run it in simulation. The configuration now contains two types of parameters: 

	#. node-specific parameters (like addresses)
	#. system-parameters (like timeout) that do not change with each node.
	
In simulation we will have one :file:`reference.conf` config file with system-parameters, and we will tell the simulator to add the node-specific parameters.

Thus the configuration file now contains only the timeout.

.. literalinclude:: sim-pingpong-cleaner/src/main/resources/reference.conf

We now want to tell the simulator to add the *self* and *ponger* addresses to the config of each individual node. For this we will need to override the :java:ref:`se.sics.kompics.simulator.events.system.StartNodeEvent.initConfigUpdate()` method. The default implementation of this method returns an empty Map which corresponds to no change to the config. The returned Map is of type <config-key, config-value>. We can add the addresses to the config in two ways. We can add the components of the address:

.. code-block:: java

	@Override
	public Map<String, Object> initConfigUpdate() {
		HashMap<String, Object> config = new HashMap<>();
		config.put("pingpong.self.host", selfAdr.getIp().getHostName());
		config.put("pingpong.self.port", selfAdr.getPort());
		config.put("pingpong.pinger.pongeraddr.host", pongerAdr.getIp().getHostName());
		config.put("pingpong.pinger.pongeraddr.port", pongerAdr.getPort);
		return config;
	}

Or we can add the ``TAddress`` object into the config directly:

.. code-block:: java

	@Override
	public Map<String, Object> initConfigUpdate() {
		HashMap<String, Object> config = new HashMap<>();
		config.put("pingpong.self", selfAdr);
		config.put("pingpong.pinger.pongeraddr", pongerAdr);
		return config;
	}

In this simulation scenario we have overridden the config method.

.. literalinclude:: sim-pingpong-cleaner/src/main/java/se/sics/test/sim/ScenarioGen.java

The ``Host`` and ``Parent`` are also changed to use the config.

.. literalinclude:: sim-pingpong-cleaner/src/main/java/se/sics/test/system/PingerParent.java
.. literalinclude:: sim-pingpong-cleaner/src/main/java/se/sics/test/system/PingerHost.java
.. literalinclude:: sim-pingpong-cleaner/src/main/java/se/sics/test/system/PongerParent.java
.. literalinclude:: sim-pingpong-cleaner/src/main/java/se/sics/test/system/PongerHost.java

The code until here can be downloaded :download:`here <sim-pingpong-distributed.zip>`.


Global View
-----------
Sometimes you might want to observe some state and do something special (like stop the simulation) in case the state matches a special case that you have considered. With this in mind the simulator offers a :java:ref:`se.sics.kompics.simulator.util.GlobalView`. The ``GlobalView`` allows you to do three things:

	#. check which nodes are dead or alive 
	#. set/get <key,values> shared globally
	#. tell the simulator to terminate this simulation

You can access the ``GlobalView`` from your config, using the key "simulation.globalview":

.. code-block:: java

	GlobalView gv = config().getValue("simulation.globalview", GlobalView.class);

The simulation scenarios before terminated at a specific time:

.. code-block:: java 

	terminateAfterTerminationOf(10000, pinger);

This time we want to check for certain conditions and terminate when one such condition occurs. In this section we want to terminate the current simulation when at least 100 pongs have been received by the pingers or when at least 3 nodes have died. 

We write an observer that will check periodically on the global state and verify if any conditions have been met.

.. literalinclude:: sim-pingpong-global/src/main/java/se/sics/test/sim/SimulationObserver.java

We also add a small bit of code to the ``Pinger`` to record their pongs.

.. code-block:: java

	ClassMatchedHandler<Pong, TMessage> pongHandler = new ClassMatchedHandler<Pong, TMessage>() {

		@Override
		public void handle(Pong content, TMessage context) {
			counter++;
			LOG.info("{} Got Pong #{}! from:{}", new Object[]{self, counter, context.header.src});
			ponged();
		}
	};

	private void ponged() {
		GlobalView gv = config().getValue("simulation.globalview", GlobalView.class);
		gv.setValue("simulation.pongs", gv.getValue("simulation.pongs", Integer.class) + 1);
	}

Since we are using a custom <key, value> from the ``GlobalView``, we might want to initialize this value before. We can do this within the :java:ref:`se.sics.kompics.simulator.events.system.SetupEvent`, by overriding the ``setupGlobalView`` method: 

.. code-block:: java

	static Operation setupOp = new Operation<SetupEvent>() {
		@Override
		public SetupEvent generate() {
			return new SetupEvent() {
				@Override
				public void setupGlobalView(GlobalView gv) {
					gv.setValue("simulation.pongs", 0);
				}
			};
		}
	};

We also want to be able to kill nodes, specifically pongers, so we write a :java:ref:`se.sics.kompics.simulator.events.system.KillNodeEvent`, which requires overriding only the ``getNodeAddress`` method to identify the node we want to kill:

.. code-block:: java

	static Operation1 killPongerOp = new Operation1<KillNodeEvent, Integer>() {
		@Override
		public KillNodeEvent generate(final Integer self) {
			return new KillNodeEvent() {
				TAddress selfAdr;

				{
					try {
						selfAdr = new TAddress(InetAddress.getByName("192.193.0." + self), 10000);
					} catch (UnknownHostException ex) {
						throw new RuntimeException(ex);
					}
				}

				@Override
				public Address getNodeAddress() {
					return selfAdr;
				}

				@Override
				public String toString() {
					return "KillPonger<" + selfAdr.toString() + ">";
				}
			};
		}
	};

We modify now the old scenario ``simplePing``, that starts 5 pongers and 5 pingers and we expect the ``SimulationObserver`` to terminate it early, when at least 100 pongs have been received. In case our observer's termination conditions are not met due to bugs, the simulation might not stop and run forever, so we still want to keep the scenario termination time (a very high one) as a safety net. In this case we added a 10.000s termination time, but the ``SimulationObserver`` should terminate the simulation within a couple of tens of seconds of simulated time.

The second scenario ``killPongers`` will start killing the pongers, which the observer should notice and then stop the simulation. In this case both conditions -- number of pings and number of dead nodes -- can be met, but for the given code (seed, timing conditions) the number of dead nodes will be met first.

.. literalinclude:: sim-pingpong-global/src/main/java/se/sics/test/sim/ScenarioGen.java
.. literalinclude:: sim-pingpong-global/src/main/java/se/sics/test/sim/SimplePingLauncher.java
.. literalinclude:: sim-pingpong-global/src/main/java/se/sics/test/sim/KillPongersLauncher.java

The code until here can be downloaded :download:`here <sim-pingpong-global.zip>`.
