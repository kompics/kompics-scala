
Kompics Basics
==============
This section of the tutorial will properly introduce the basic concepts in Kompics, namely *components*, *ports*, *handlers*, and *channels*.

The example application we will use is a simple *Ping Pong* application, with two components that send messages back and forth between them.

Components
----------
A *component* in Kompics is a stateful object that can be scheduled and can access its own state without need for synchronisation. Typically, components encapsulate some kind of functionality, by providing certain services and possibly requiring others. Components can also have *child* components *created* within. These *parent*-*child* relations form a supervision hierarchy tree.

In programming language terms, a component is a class that extends @java[@javadoc[ComponentDefinition](se.sics.kompics.ComponentDefinition)]@scala[@scaladoc[ComponentDefinition](se.sics.kompics.sl.ComponentDefinition)]. If a component needs any additional parameters upon creation, a constructor that takes an implementation of @javadoc[Init](se.sics.kompics.Init) can be used and an instance passed on creation. Otherwise simply pass @java[@javadoc[NONE](se.sics.kompics.Init#NONE)]@scala[@scaladoc[None](se.sics.kompics.sl.Init$#NONE)].

For this example we need three components, two to do the work, and a parent that handles setup:

#### Pinger

Java
:	@@snip[Pinger.java](/docs/src/main/java/jexamples/basics/pingpong/Pinger.java) { #header_only }

Scala
:	@@snip[Pinger.scala](/docs/src/main/scala/sexamples/basics/pingpong/Pinger.scala) { #header_only }

#### Ponger

Java
:	@@snip[Ponger.java](/docs/src/main/java/jexamples/basics/pingpong/Ponger.java) { #header_only }

Scala
:	@@snip[Ponger.scala](/docs/src/main/scala/sexamples/basics/pingpong/Ponger.scala) { #header_only }

#### Parent

Java
:	@@snip[Parent.java](/docs/src/main/java/jexamples/basics/pingpong/Parent.java) { #create_only }

Scala
:	@@snip[Parent.scala](/docs/src/main/scala/sexamples/basics/pingpong/Parent.scala) { #create_only }

Ports and Events
-----------------
A *port type* in Kompics is a bit like a service interface. It defines what kind of *events* (you may think of *messages*, although in Kompics we typically reserve that term for events that are addressable via the network) may pass through the port and in which direction. Events may be declared in a port type in either *indication* ( *positive* ) or *request* ( *negative* ) direction. In a similar fashion a component either *requires* or *provides* a *port* (think of a port type instance).

@@@ note { title='Analogies'}
The closest analogy to the Kompics terminology in this respect might be electric charge carriers and electrodes in some kind of conductive medium. Think of the events as charge carriers (indications carry positive charge, and requests carry negative charge). Every port has both an anode and a cathode side. If a component requires port *A* then inside the component you have access to *A*'s positive (cathode) side where indications (positive charge carriers) come out of, and outside the component you have access to *A* negative (anode) side where requests (negative charge carriers) come out of. Conversely if a component provides *A* then inside the component the negative (anode) side spits out requests (negative charge carriers) and the outside is positive (cathode) and indications (positive charge carriers) come out this way. In both cases the charge that is not going out is the one that is going in.

An alternative analogy, that is a bit more limited but usually easier to keep in mind, is that of service providers and consumers. Consider a port *A* to be a service contract. A component that provides service *A* handles events that are specified as requests in *A* and sends out events that are specified as indications in *A*. Conversely a component that requires service *A* sends out events that are specified as requests in *A* and handles events that are specified as indications in *A* (thus are in a sense replies to its own requests).

@@@

In programming language terms an event is a class that is a subtype of @javadoc[KompicsEvent](se.sics.kompics.KompicsEvent), which is only a marker interface with no required methods). A port type is a singleton that extends @java[@javadoc[PortType](se.sics.kompics.PortType)]@scala[@scaladoc[Port](se.sics.kompics.sl.Port)] and registers its types with their direction during loading. Port instances fall in the two categeories:

1. Those that implement @javadoc[Positive](se.sics.kompics.Positive) over the port type, which are the result of a call to @java[@javadoc[requires](se.sics.kompics.ComponentDefinition#requires)]@scala[@scaladoc[requires](se.sics.kompics.sl.ComponentDefinition#requires(P))], and 
2. those that implement @javadoc[Negative](se.sics.kompics.Negative) of the port type, which are the result of a call to @java[@javadoc[provides](se.sics.kompics.ComponentDefinition#provides)]@scala[@scaladoc[provides](se.sics.kompics.sl.ComponentDefinition#provides)].

Internally ports are binary linked with both a positive and a negative side.

For this example we need two events and one port type:

#### Ping

Java
:	@@snip[Ping.java](/docs/src/main/java/jexamples/basics/pingpong/Ping.java) { }

Scala
:	@@snip[object Ping](/docs/src/main/scala/sexamples/basics/pingpong/PingPongPort.scala) { #ping }

#### Pong

Java
:	@@snip[Pong.java](/docs/src/main/java/jexamples/basics/pingpong/Pong.java) { }

Scala
:	@@snip[object Pong](/docs/src/main/scala/sexamples/basics/pingpong/PingPongPort.scala) { #pong }

#### PingPongPort

Java
:	@@snip[PingPongPort.java](/docs/src/main/java/jexamples/basics/pingpong/PingPongPort.java) { }

Scala
:	@@snip[object PingPongPort](/docs/src/main/scala/sexamples/basics/pingpong/PingPongPort.scala) { #port }


@@@ note
It is highly recommended to only write completely immutable events. Since Kompics will deliver the same event instance to all subscribed handlers in all connected components, writing mutable events can lead to some nasty and difficult to find bugs.

@java[For encapsulating collections in a safe manner, the reader is referred Google's excellent @link:[Guava](https://github.com/google/guava/wiki) library (which is already a dependency of Kompics core anyway) and its immutable collection types.]

@@@

We also want to add the ports to the two components such that ``Pinger`` sends ``Ping``\s and ``Ponger`` sends ``Pong``\s, which is hopefully somewhat intuitive:

#### Pinger with Port

Java
:	@@snip[Pinger.java](/docs/src/main/java/jexamples/basics/pingpong/Pinger.java) { #header_and_port }

Scala
:	@@snip[Pinger.scala](/docs/src/main/scala/sexamples/basics/pingpong/Pinger.scala) { #header_and_port }


#### Ponger with Port

Java
:	@@snip[Ponger.java](/docs/src/main/java/jexamples/basics/pingpong/Ponger.java) { #header_and_port }

Scala
:	@@snip[Ponger.scala](/docs/src/main/scala/sexamples/basics/pingpong/Ponger.scala) { #header_and_port }

Event Handlers
--------------
In order for components to actually get scheduled and process events, a *handler* for a specific event type must be *subscribed* to a port that spits out that kind of event. If an event arrives at a component's port and no handler is subscribed for its type, then the event is simply silently dropped.

@java[In Java terms the most common way of working with handlers is to assign an anonymous class that extends @javadoc[Handler](se.sics.kompics.Handler) of the desired event type to either a class field or a block variable and then call @javadoc[subscribe](se.sics.kompics.ComponentDefinition#subscribe) with that variable and the target port.]@scala[In Scala terms, a handler is a partial function `f: Any => Unit`, which is *subscribed* to a port instance `p` by calling `p uponEvent f`. Within `f`, the event types that are supposed to be handled can be pattern-matched for and then the appropriate code for each event invoked.]

In our example we want the `Pinger` to send the first `Ping` when it received the `Start` event (which we saw in the @ref:[helloworld](../introduction/helloworld.md) example already), and then reply to every `Pong` event it receives with a new `Ping`. The `Ponger` simply waits for a `Ping` and replies with a `Pong`.
We also log something so we can see it working on the console.

#### Pinger (final)

Java
:	@@snip[Pinger.java](/docs/src/main/java/jexamples/basics/pingpong/Pinger.java) { filterLabels=true }

Scala
:	@@snip[Pinger.scala](/docs/src/main/scala/sexamples/basics/pingpong/Pinger.scala) { filterLabels=true }


#### Ponger (final)

Java
:	@@snip[Ponger.java](/docs/src/main/java/jexamples/basics/pingpong/Ponger.java) { filterLabels=true }

Scala
:	@@snip[Ponger.scala](/docs/src/main/scala/sexamples/basics/pingpong/Ponger.scala) { filterLabels=true }

Channels
--------
Since normal events in Kompics are not addressed, we need to tell the system somehow where a triggered event is supposed to go. The method for this is by connecting *channels*. A single channel connects exactly two ports of opposite polarity (or direction, if you prefer). You can connect both ports that are inside and outside of a component. The normal way is to connect the outside of a required port of a component *A* to the outside of a required port of another component *B*. In this setup *B* provides the port's service to *A*, so to speak. The alternative setup is to connect the inside of required port of *A* to the outside of a required port of *B* (remember that insides and outsides are of opposite types, so this actually works). This setup is called *chaining* and it has both *A* and *B* share the service of whatever component connects to the outside of *A*'s port. Alternatively, *A* (or its parent) could pass on the outside of the port that *A* is connected to directly to *B*. This has the same effect, but is a bit more efficient (fewer method invocations while travelling along channel chains). However, it might also break abstraction in some cases. The decision of which method is appropriate under certain conditions is left up to the programmer.

In @java[Java]@scala[Scala] channels are created with the @java[@javadoc:[connect](se.sics.kompics.ComponentDefinition#connect)]@scala[@scaladoc:[connect](se.sics.kompics.sl.ComponentDefinition#connect)] method. @scala[The directionality of the arrow (`->`) is **from** the component which **provides** the port, **to** the component which **requires** the port.]

#### Parent (final)

Java
:	@@snip[Parent.java](/docs/src/main/java/jexamples/basics/pingpong/Parent.java) { filterLabels=true }

Scala
:	@@snip[Parent.scala](/docs/src/main/scala/sexamples/basics/pingpong/Parent.scala) { filterLabels=true }

Kompics Runtime
---------------------
The runtime itself is responsible for starting and stopping the scheduler and initialising the component hierarchy. The entry point to start Kompics is @java[@javadoc:[Kompics.createAndStart](se.sics.kompics.Kompics#createAndStart(java.lang.Class))]@scala[@scaladoc:[Kompics.createAndStart](se.sics.kompics.sl.Kompics$#createAndStart)] which comes in several variants for tuning and parameters. The most basic one simply takes the top-level component's class instance.

For our example we want to start Kompics with the `Parent` top-level component and since it would ping-pong forever on its own, we also want to stop it again after waiting for some time, say ten seconds:

#### Main

Java
:	@@snip[Main.java](/docs/src/main/java/jexamples/basics/pingpong/Main.java) { filterLabels=true }

Scala
:	@@snip[Main.scala](/docs/src/main/scala/sexamples/basics/pingpong/Main.scala) { filterLabels=true }


Now finally compile with:
```bash
sbt compile
```
To run the project from within sbt, execute:

@@@ div { .group-java }
```bash
runMain jexamples.basics.pingpong.Main
```
@@@

@@@ div { .group-scala }
```bash
runMain sexamples.basics.pingpong.Main
```
@@@


Component State
---------------
So far the Kompics components we used haven't really used any state. To show a simple example we are going to introduce a `counter` variable in both `Pinger` and `Pinger` and print the sequence number of the current `Ping` or `Pong` to the console. To show that this works correctly even in multi-threaded execution we'll also add a second thread to the Kompics runtime.

#### Wainting Main 

Java
:	@@snip[Main.java](/docs/src/main/java/jexamples/basics/pingpongstate/Main.java) { filterLabels=true }

Scala
:	@@snip[Main.scala](/docs/src/main/scala/sexamples/basics/pingpongstate/Main.scala) { filterLabels=true }

#### Pinger with State

Java
:	@@snip[Pinger.java](/docs/src/main/java/jexamples/basics/pingpongstate/Pinger.java) { filterLabels=true }

Scala
:	@@snip[Pinger.scala](/docs/src/main/scala/sexamples/basics/pingpongstate/Pinger.scala) { filterLabels=true }

#### Ponger with State

Java
:	@@snip[Ponger.java](/docs/src/main/java/jexamples/basics/pingpongstate/Ponger.java) { filterLabels=true }

Scala
:	@@snip[Ponger.scala](/docs/src/main/scala/sexamples/basics/pingpongstate/Ponger.scala) { filterLabels=true }

Compile and run from within sbt in the same way as before:

@@@ div { .group-java }
```bash
runMain jexamples.basics.pingpongstate.Main
```
@@@

@@@ div { .group-scala }
```bash
runMain sexamples.basics.pingpongstate.Main
```
@@@
