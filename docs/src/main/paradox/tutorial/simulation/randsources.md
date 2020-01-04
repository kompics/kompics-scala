
Sources of Randomness
=====================

In order to circumvent nondeterministic execution when running in simulation mode, the Java bytecode of the system is instrumented using the @link:[Javaassist](http://jboss-javassist.github.io/javassist/) toolkit to intercept a number of method calls and replace them with code that exhibits similar, but deterministic behaviour. 

The advantage of using bytecode instrumentation for whole-system simulation is that in order to execute a system in simulation there is no need to change any of its source code. The implication of this fact is that we can simulate not only the code of the system under development, but also any third-party libraries that it might use. The only limitation of this approach is when a third-party library invokes native code. Allowing the execution to "escape" the managed environment of the JVM into native code means that we loose the guarantee of deterministic execution.

As stated before, the sources of randomness that we consider in simulation include:

1. system time
2. random generators
3. thread creation/execution
4. network, e.g. latency, packet loss
5. input/output sources

System time
-----------
The two sources of system time that we consider are the Java system calls: @javadoc:[System.currentTimeMillis()](java.lang.System#currentTimeMillis()) and @javadoc:[System.nanoTime()](java.lang.System#nanoTime()). 

These two methods will be instrumented to point to simulation time, instead. Since the granulatiry of simulation time is only milliseconds, the @javadoc:[System.nanoTime()](java.lang.System#nanoTime()) is a simple multiple of @javadoc:[System.currentTimeMillis()](java.lang.System#currentTimeMillis()) in simulation.

Random Generators
-----------------

The random generators we consider are @javadoc:[Random](java.util.Random), @javadoc:[SecureRandom](java.security.SecureRandom), and @javadoc:[UUID](java.util.UUID) generation. 

Currently, @javadoc:[SecureRandom](java.security.SecureRandom) type generators are simply not allowed in simulations. 

When using `new Random()` (the no argument constructor), the call is instrumented to `new Random(0)`, in order to be deterministic.

When using `UUID.randomUUID()` the call is instrumented as two calls to the @javadoc:[Random.nextLong()](java.util.Random#nextLong()) method on the simulation's global random generator (which is controlled by the simulation seed). 

Thread Creation
---------------

Thread interleaving creates nondeterminism, which we want to avoid during simulation and thus, we instrument calls to @javadoc:[Thread.start()](java.lang.Thread#start()) and @javadoc:[Thread.sleep()](java.lang.Thread#sleep(long)). All attempts to create threads are intercepted and the simulation halts with an error informing the user that deterministic execution cannot be guaranteed. Kompics protocol components are typically reactive and do not need to spawn threads of their own, so they lend themselves well to simulation.

In some cases, especially third party libraries, threads are created, but they might not interact with component logic. If this is the case, we can add that class to the exception list (see section @ref:[Instrumentation Exceptions](#instrumentation-exceptions)) to prevent it from getting instrumented.

Network
-------
Randomness induced by network parameters, such as latency and network loss, are modeled using the @javadoc:[SimulationScenario](se.sics.kompics.simulator.SimulationScenario) and @javadoc:[NetworkModel](se.sics.kompics.simulator.network.NetworkModel) abstraction.

Input/Output sources
--------------------
Currently, there is no warning/error for using I/O Sources, but it should be kept in mind that these might introduce uncertainty into the simulation.

Instrumentation Exceptions
--------------------------
You can add classes to the instrumentation exception list by adding the following lines to your configuration file:

```hocon
instrumentation.exceptions = [
	"a.b.c.MyClass",
	"d.e.f.MyOtherClass"
]
```
