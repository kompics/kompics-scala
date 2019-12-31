
Timers
======
In distributed systems it is quite common to have certain events happen after some time has passed, or in a regular schedule. And while globally synchronised clocks are rarely available, local clock drift is typically small enough that algorithms can rely on local *time differences* (or *intervals*).

There are number of timer facilities for Java and Scala ranging from the simple tree based @javadoc:[Timer](java.util.Timer) to the significantly more advanced @javadoc:[HashedWheelTimer](io.netty.util.HashedWheelTimer).

However, Kompics provides its own abstraction for scheduled or periodic events. It is very important that your code only relies on this abstraction as described in this section of the tutorial, if you want to use the Kompics Simulation framework described in section @ref[simulation](simulation/index.md). The reasons for this restriction will be described in detail later in the simulation part of the tutorial, but very shortly it has to do with being able to transparently replace *real time* with *simulation time*.

This section introduces the default Kompics timer facilities and the concept of request-response-type events.

Request-Response Events
-----------------------
In the previous section we described how Kompics events are broadcasted along all connected channels of their respective ports. We also showed a *PingPong*  application that was already sending messages back and forth. So in a sense we were using the `Ping` like a request for a `Pong`-response. However, imagine we add a second ``Ponger`` component which we connect in the same way as the first one. Now every `Ping` would get two `Pong`s, one from each `Ponger` component, and our counters would be totally confused. And the same is true the other way around. Imagine we add a second `Pinger` component. Now there would be two `Ping`s going out in parallel, being answered by two `Pong`s which get broadcasted to each `Pinger` component, which then answers each of those `Pong`s with another `Ping` resulting in four `Ping`s arriving at the `Ponger` the next time, and so on. While the first type of behaviour might often in fact be what we want, clearly, the second type is rarely going to be the desired.

@@@ note
If the first type of behaviour is not desired then @ref:[Channel Selectors](networking/virtual/virtual.md#channel-selectors) might provide a solution.
@@@

In order to get around the second type of behaviour (two `Pinger` -- one `Ponger`) where it is necessary, Kompics supports two types of request-response patterns: 

* A *normal* (or old-style) pattern that records the whole path the request takes, and every response to that request simply backtracks along the channels and components recorded. Events using this pattern have to extend @javadoc:[Request](se.sics.kompics.Request) and @javadoc:[Response](se.sics.kompics.Response) respectively.
* A *direct* (newer) pattern, that only remembers the origin port of the request and triggers the response directly on that port. Events using this pattern have to extend @javadoc[Direct.Request](se.sics.kompics.Direct.Request) and implement @javadoc:[Direct.Response](se.sics.kompics.Direct.Response) respectively.

It is generally preferred to use the newer *direct* pattern when possible, because it is more efficient and simpler to understand. However, the disadvantage is that a component will receive responses even after all its channels have been disconnected, which can lead to confusion.

@@@ warning

With the old-style pattern, responses will travel along the recorded path back to the orginal port. However, if that port is chained, i.e. there are channels connected on the outgoing side, the response will travel further along those channels like a normal event.

The *direct* pattern does not exhibit this behaviour.

@@@

In order to show examples for both types of request-response patterns, we will convert the *PingPong* example to the *direct* pattern now, and we will show the old-style pattern in the next section where the *Kompics Timer* is introduced, since it still relies on this pattern.

First of all, `Ping` now extends @javadoc[Direct.Request](se.sics.kompics.Direct.Request), parametrised by `Pong`, indicating that we expect answer of type `Pong`.

Java
:	@@snip[Ping.java](/docs/src/main/java/jexamples/basics/pingpongdirect/Ping.java) { }

Scala
:	@@snip[class Ping](/docs/src/main/scala/sexamples/basics/pingpongdirect/PingPongPort.scala) { #ping }


And, of course, `Pong` has to implement @javadoc:[Direct.Response](se.sics.kompics.Direct.Response) in that case.

Java
:	@@snip[Pong.java](/docs/src/main/java/jexamples/basics/pingpongdirect/Pong.java) { }

Scala
:	@@snip[object Pong](/docs/src/main/scala/sexamples/basics/pingpongdirect/PingPongPort.scala) { #pong }

Additionally, we have to change our handler for `Ping` events in the `Ponger` to use the @javadoc:[answer](se.sics.kompics.ComponentDefinition#answer) method instead of `trigger`, which causes Kompics to use the origin port provided in the request instead of a local port.

Java
:	@@snip[Ponger.java](/docs/src/main/java/jexamples/basics/pingpongdirect/Ponger.java) { #ping-handler }

Scala
:	@@snip[Ponger.scala](/docs/src/main/scala/sexamples/basics/pingpongdirect/Ponger.scala) { #ping-handler }

To show that it actually behaves as expected, we add a second `Pinger` in the `Parent` and connect it like the first one.

Java
:	@@snip[Parent.java](/docs/src/main/java/jexamples/basics/pingpongdirect/Parent.java) {  }

Scala
:	@@snip[Parent.scala](/docs/src/main/scala/sexamples/basics/pingpongdirect/Parent.scala) { }


Now if we compile and run this we can see that the single `Ponger` is counting twice as many `Ping` instances as each `Pinger` counts `Pong` instances:

@@@ div { .group-java }
```bash
runMain jexamples.basics.pingpongdirect.Main
```
@@@

@@@ div { .group-scala }
```bash
runMain sexamples.basics.pingpongdirect.Main
```
@@@



Kompics Timer
-------------
This section describes the Kompics `Timer` port and the default implementation `JavaTimer`.

In order to use the timer port in Kompics, an additional dependency in our build file is required:

@@dependency[sbt,Maven,Gradle] {
  group="se.sics.kompics.basic"
  artifact="kompics-port-timer"
  version=$kompics.version$
}

The @javadoc:[Timer](se.sics.kompics.timer.Timer) port itself allows four types of requests and only a single indication: @javadoc[Timeout](se.sics.kompics.timer.Timeout), which extends the old-style @javadoc:[Response](se.sics.kompics.Response) and must be extended further by any component using Kompics' timer facilities.

The requests come in two pairs, a *schedule* and a *cancel* for one-shot timers (@javadoc:[ScheduleTimeout](se.sics.kompics.timer.ScheduleTimeout) and @javadoc:[CancelTimeout](se.sics.kompics.timer.CancelTimeout)) and anotherr pair for periodic timers (@javadoc:[SchedulePeriodicTimeout](se.sics.kompics.timer.SchedulePeriodicTimeout) and @javadoc:[CancelPeriodicTimeout](se.sics.kompics.timer.CancelPeriodicTimeout)). In both cases the *schedule* event extends the old-style request and requires a prepared timeout response instance to be passed along to the `Timer` component, using the @javadoc:[setTimeoutEvent](se.sics.kompics.timer.ScheduleTimeout#setTimeoutEvent(se.sics.kompics.timer.Timeout)) method. Additionally, each `Timeout` instance generates a random @javadoc:[UUID](java.util.UUID) upon creation, which the scheduling component should store somewhere, since it is needed to cancel the timeout later. This is especially important for the periodic timeouts, which should **always** be cancelled when they are no longer required, in order to reduce resource usage of the system.

@@@ warning

It should be pointed out that the semantics, provided by Kompics' `Timer` facilities, are only that timeouts are never handled before their delay/period has passed. Due to propagation and scheduling delays there is no fixed bound on how soon after a timeout was triggered, it is handled. In particular, timeout events can be caught in long port queues on the receiver side, as there is no notion of priority for events in Kompics. 

@@@

The default component providing the Kompics `Timer` service is @javadoc:[JavaTimer](se.sics.kompics.timer.java.JavaTimer) which can be accessed after adding the following dependency to the build file:

@@dependency[sbt,Maven,Gradle] {
  group="se.sics.kompics.basic"
  artifact="kompics-component-java-timer"
  version=$kompics.version$
}

The `JavaTimer` is based on Java's default @javadoc:[Timer](java.util.Timer) and should scale to all but the most extreme needs, with a single instance per Kompics component tree. Since the `Timer` port uses the old-style request-response pattern, it is recommended that it not be chained through multiple layers of the system, but instead directly connected to all components that require it. Otherwise strange "multi timer handling"-behaviour can occur, as was pointed out in the previous section. The direct connections also incur slightly less latency compared to chained ones, which is important for the timely handling of timeouts.


To update our example, we now want to only trigger `Ping`s periodically, say one per second, and not in response to `Pong`s anymore. 

First we extend our dual-`Pinger` setup from before with an instance of `JavaTimer` and connect everything appropriately.

Java
:	@@snip[Parent.java](/docs/src/main/java/jexamples/basics/pingpongtimer/Parent.java) {  }

Scala
:	@@snip[Parent.scala](/docs/src/main/scala/sexamples/basics/pingpongtimer/Parent.scala) { }

Then we add `Timer` as a required port to the `Pinger` and change the handlers' behaviour to schedule a `PingTimeout` on start and then send a `Ping` whenever the `PingTimeout` is received. We'll schedule the timout periodically every second with no initial delay. We also override the @javadoc:[tearDown](se.sics.kompics.ComponentDefinition#tearDown) method to cancel our periodic timeout when we are being stopped. It simply is good form to clean up after oneself.

Java
:	@@snip[Pinger.java](/docs/src/main/java/jexamples/basics/pingpongtimer/Pinger.java) {  }

Scala
:	@@snip[Pinger.scala](/docs/src/main/scala/sexamples/basics/pingpongtimer/Pinger.scala) { }

If again compile and run this, we should see a significantly lower rate of `Ping` and `Pong` event production:

@@@ div { .group-java }
```bash
runMain jexamples.basics.pingpongtimer.Main
```
@@@

@@@ div { .group-scala }
```bash
runMain sexamples.basics.pingpongtimer.Main
```
@@@

