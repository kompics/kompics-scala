.. _timers:

Timers
======
In distributed systems it is quite common to have certain events happen after some time has passed, or in a regular schedule. And while globally synchronised clocks are rarely available, local clock drift is typically small enough that algorithms can rely on local *time differences* (or *intervals*).

There are number of timer facilities for Java ranging from the simple tree based :java:ref:`java.util.Timer` to the significantly more advanced :java:ref:`io.netty.util.HashedWheelTimer`.

However, Kompics provides its own abstraction for scheduled or periodic events. It is very important that your code only relies on this abstraction as described in this section of the tutorial, if you want to use the Kompics Simulation framework described in section :ref:`simulation`. The reasons for this restriction will be described in the simulation part of the tutorial.

This section introduces the default Kompics timer facilities and the concept of request-response-type events.

.. _reqrespevents:

Request-Response Events
-----------------------
In the previous section we described how Kompics events are broadcasted along all connected channels of their respective ports. We also showed a *PingPong*  application that was already sending messages back and forth. So in a sense we were using the ``Ping`` like a request for a ``Pong``-response. However, imagine we add a second ``Ponger`` component which we connect in the same way as the first one. Now every ``Ping`` would get two ``Pong``, one from each ``Ponger`` component, and our counters would be totally confused. And the same is true the other way around. Imagine we add a second ``Pinger`` component. Now there would be two ``Ping``\s going out in parallel, being answered by two ``Pong``\s which get broadcasted to each ``Pinger`` component, which then answers each of those ``Pong``\s with another ``Ping`` resulting in four ``Ping``\s arriving at the ``Ponger`` the next time, and so on. While the first type of behaviour might often in fact be what we want, clearly, the second type is rarely going to be the desired.

.. note::

	If the first type of behaviour is not desired then :ref:`channelselectors` might provide a solution.

In order to get around the second type of behaviour (two ``Pinger`` -- one ``Ponger``) where it is necessary, Kompics supports two types of request-response patterns: 

	* A *normal* (or old-style) pattern that records the whole path the request takes, and every response to that request simply backtracks along the channels and components recorded. Events using this pattern have to extend :java:ref:`se.sics.kompics.Request` and :java:ref:`se.sics.kompics.Response` respectively.
	* A *direct* (newer) pattern, that only remembers the origin port of the request and triggers the response directly on that port. Events using this pattern have to extend :java:ref:`se.sics.kompics.Direct.Request` and implement :java:ref:`se.sics.kompics.Direct.Response` respectively.

It is generally preferred to use the newer *direct* pattern when possible, because it is more efficient and simpler to understand. However, the disadvantage is, that a component will receive responses even after all its channels have been disconnected, which can lead to confusion.

.. warning::

	With the old-style pattern, responses will travel along the recorded path back to the orginal port. However, if that port is chained, i.e. there are channels connected on the outgoing side, the response will travel further along those channels like a normal event.
	The *direct* pattern does not exhibit this behaviour.

In order to show examples for both types of request-response patterns, we will convert the *PingPong* example to the *direct* pattern now, and we will show the old-style pattern in the next section where the *Kompics Timer* is introduced.

First of all, ``Ping`` now extends :java:ref:`se.sics.kompics.Direct.Request`.

.. literalinclude:: pingpong/src/main/java/se/sics/test/Ping.java

And, of course, ``Pong`` has to implement :java:ref:`se.sics.kompics.Direct.Response` in that case.

.. literalinclude:: pingpong/src/main/java/se/sics/test/Pong.java

Additionally, we have to change the ``pingHandler`` in the ``Ponger`` to use the ``answer`` method instead of ``trigger`` which tells Kompics to use the origin port provided in the request instead of a local port.

.. code-block:: java

	Handler<Ping> pingHandler = new Handler<Ping>(){
		public void handle(Ping event) {
			counter++;
			LOG.info("Got Ping #{}!", counter);
			answer(event, new Pong());
		}
	};

To show that it actually does what we want it to, we add a second ``Pinger`` in the ``Parent`` and connect it like the first one.

.. code-block:: java
	
	public class Parent extends ComponentDefinition {
		Component pinger = create(Pinger.class, Init.NONE);
		Component ponger = create(Ponger.class, Init.NONE);
		Component pinger2 = create(Pinger.class, Init.NONE);

		{
			connect(pinger.getNegative(PingPongPort.class), ponger.getPositive(PingPongPort.class));
			connect(pinger2.getNegative(PingPongPort.class), ponger.getPositive(PingPongPort.class));
		}
	}

Now if we compile and run this we can see that the single ``Ponger`` is counting twice as many ``Ping`` instances as each ``Pinger`` counts ``Pong`` instance::

	mvn clean compile
	mvn exec:java -Dexec.mainClass="se.sics.test.Main"



Kompics Timer
-------------
This section describes the Kompics ``Timer`` port and the default implementation ``JavaTimer``.

In order to use the timer port in Kompics, an additional dependency in the :file:`pom.xml` is required:

.. code-block:: xml

    <dependency>
        <groupId>se.sics.kompics.basic</groupId>
        <artifactId>kompics-port-timer</artifactId>
        <version>${kompics.version}</version>
        <scope>compile</scope>
    </dependency>

The :java:ref:`se.sics.kompics.timer.Timer` port itself allows four types of requests and only a single indication, :java:ref:`se.sics.kompics.timer.Timeout`, which extends the old-style :java:ref:`se.sics.kompics.Response` and must be extended further by any component using Kompics' timer facilities.

The requests come in two pairs, a schedule and a cancel for one-shot timers (:java:ref:`se.sics.kompics.timer.ScheduleTimeout` and :java:ref:`se.sics.kompics.timer.CancelTimeout`) and one for periodic timers (:java:ref:`se.sics.kompics.timer.SchedulePeriodicTimeout` and :java:ref:`se.sics.kompics.timer.CancelPeriodicTimeout`). In both cases the *schedule* event extends the old-style request and requires a prepared timeout response instance to be passed along to the Timer component, using the ``setTimeoutEvent`` method. Additionally, each ``Timeout`` instance generates a random :java:ref:`java.util.UUID` upon creation, which the scheduling component should store somewhere, since it is needed to cancel the timeout later. This is especially important for the periodic timeouts, which should always be cancelled when they are no longer required to reduce resource usage of the system.

.. warning::

	It should be pointed out that the semantics, provided by Kompics' ``Timer`` facilities, are only that timeouts are never handled before their delay/period has passed. Due to propagation and scheduling delays there is no fixed bound on how soon after a timeout was triggered, it is handled. Especially, timeout events can be caught in long port queues on the receiver side, as there is no notion of priority for events in Kompics. 

The default component providing the Kompics ``Timer`` service is ``JavaTimer`` which is accessed by adding the following dependency to the :file:`pom.xml`:

.. code-block:: xml

    <dependency>
        <groupId>se.sics.kompics.basic</groupId>
        <artifactId>kompics-component-java-timer</artifactId>
        <version>${kompics.version}</version>
        <scope>compile</scope>
    </dependency>

The ``JavaTimer`` is based on Java's default :java:ref:`java.util.Timer` and should scale to all but the most extreme needs, with a single instance per Kompics component tree. Since the ``Timer`` port uses the old-style request-response pattern, it is recommended that it not be chained through multiple layers of the system, but instead directly connected to all components that require it. Otherwise strange multi-timer-handling-behaviour can occur as was pointed out in the previous section. The direct connections also incur slightly less latency compared to chained ones, which is important for the timely handling of timeouts.


To update our example, we now want to only trigger ``Ping``\s periodically and not in response to ``Pong``\s anymore. 

First we extend our dual-``Pinger`` setup from before with an instance of ``JavaTimer`` and connect that appropriately.

.. literalinclude:: pingpong/src/main/java/se/sics/test/Parent.java

Then we add ``Timer`` as a required port to the ``Pinger`` and change the handlers' behaviour to schedule a ``PingTimeout`` on start and then send a ``Ping`` whenever the ``PingTimeout`` is received. We'll schedule the timout periodically every second with no initial delay. We also override the ``tearDown`` method to cancel our periodic timeout when we are being stopped. It is simply good form to clean up after oneself. 

.. literalinclude:: pingpong/src/main/java/se/sics/test/Pinger.java

We again compile and run this and see a significantly lower number of ``Ping`` and ``Pong`` counts (unless your system is **really** slow ;)::

	mvn clean compile
	mvn exec:java -Dexec.mainClass="se.sics.test.Main"


As before, the complete example project can be downloaded :download:`here <pingpong.zip>`.