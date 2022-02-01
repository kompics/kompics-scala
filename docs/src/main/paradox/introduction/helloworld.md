Hello World in Kompics
======================

This section describes the simplest possible Kompics program, the well-known *Hello World*.

A proper introduction can be found in the @ref:[Tutorial](../tutorial/index.md).

Setup
-----
Set up a new sbt or maven project and follow the instructions in the @ref[Getting Started](gettingstarted.md#including-kompics) section to include the necessary dependencies. For this simple example the `kompics-core` or `kompics-scala` modules will be sufficient.


Hello World Component
---------------------
This very simple component contains but a single handler, which is executed when a ``Start`` event is triggered on the ``control`` port. ``Start`` events are part of Kompic's component lifecycle and always need to be triggered on a component exactly once to get it to handle events. A component that is never started is considered paused and will queue up events on its ports, but never execute them.

Java
:   @@snip [HelloComponent.java](/docs/src/main/java/jexamples/helloworld/HelloComponent.java)

Scala
:   @@snip [HelloComponent.scala](/docs/src/main/scala/sexamples/helloworld/HelloComponent.scala)

@@@ warning
Do not forget to subscribe Kompics handlers to ports in the Java version! It is the most common mistake, and it is very easy to overlook when debugging. In the Scala version, this happens automatically with `uponEvent`, but if you use `handler` instead to create a handler without automatic subscription, it may still become an issue later.
@@@

Main
----
The `main` method is very simple and only starts the Kompics framework by calling @javadoc[Kompics.createAndStart](se.sics.kompics.Kompics#createAndStart(java.lang.Class)) with the class of the root component. It also automatically sends a @javadoc[Start](se.sics.kompics.Start) event to the component once the constructor has finished.

Java
:   @@snip [Main.java](/docs/src/main/java/jexamples/helloworld/Main.java)

Scala
:   @@snip [Main.scala](/docs/src/main/scala/sexamples/helloworld/Main.scala)

Compiling and Running
---------------------
To compile the source code use:
```bash
sbt compile
```
To run the project from within sbt, execute:

@@@ div { .group-java }
```bash
runMain jexamples.helloworld.Main
```
@@@

@@@ div { .group-scala }
```bash
runMain sexamples.helloworld.Main
```
@@@

This is all. Say hello to Kompics ;)

Logging
-------
Kompics has built-in logging support, which adds context information to the logging output, such as what state the component is in and what its unique id is. You can access the provided @link:[SLF4J](https://www.slf4j.org/) logger via the `logger` field, or its @link:[Scala Logging](https://github.com/lightbend/scala-logging) wrapper via the `log` field.

In order to make use of these features, a mapped diagnostic context (MDC) capable logging backend is required, such as @link:[Logback Classic](https://logback.qos.ch/manual/mdc.html), for example.

Logback requires a configuration file to work. Adding a simple `src/main/resources/logback.xml` file to the project will configure the logging system, in this case such that all MDC information is printed:

@@snip [logback.xml](/docs/src/main/resources/logback.xml)

It is also possible to add custom context information to a particular component, by using either @javadoc[loggingContextPut](se.sics.kompics.ComponentDefinition#loggingContextPut(java.lang.String,java.lang.String)) for potentially transient values, or @javadoc[loggingContextPutAlways](se.sics.kompics.ComponentDefinition#loggingCtxPutAlways(java.lang.String,java.lang.String)) for context values that definitely span the life-time of the component.
