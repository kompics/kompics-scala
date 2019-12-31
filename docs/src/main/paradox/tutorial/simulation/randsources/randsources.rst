.. _randsources:

Sources of Randomness
=====================

In order to circumvent nondeterministic execution, when running in simulation mode, the Java bytecode of the system is instrumented using the `Javaassist <http://jboss-javassist.github.io/javassist/>`_ toolkit to intercept a number of method calls and replace them with code that exhibits similar, but deterministic behaviour. 

The advantage of using bytecode instrumentation for whole-system simulation is that in order to execute a system in simulation there is no need to change any of its source code. The implication of this fact is that we can simulate not only the code of the system under development, but also any third-party libraries that it might use. The only limitation of this approach is when a third-party library invokes native code. Allowing the execution to “escape” the managed environment of the |jvm| into native code means we loose the guarantee of deterministic execution.

As stated before, the sources of randomness that we consider in simulation include:

	#. system time
	#. random generators
	#. thread creation/execution
	#. network e.g. latency, packet loss
	#. input/output sources

System time
-----------
The two sources of system time that we considered are the java System calls: :java:ref:`java.lang.System.currentTimeMillis()` and :java:ref:`java.lang.System.nanoTime()`. 

These two methods will be instrumented to point to the virtual simulation time. Since the simulation time granularity is ms, the ``nanoTime()`` is a simple ``currentTimeMillis()``:math:`\cdot 10^6`

Random Generators
-----------------

The random generators we considered are: :java:ref:`java.util.Random`, :java:ref:`java.security.SecureRandom` and :java:ref:`java.util.UUID`. 

Currently, ``SecureRandom`` type generators are not allowed in simulations. 

When using ``new Random()`` (no argument constructor), the call is instrumented to ``new Random(0)``.

When using ``UUID.randomUUID()`` the call is instrumented as two calls to the ``Random nextLong()`` method on the simulation global random generator. 

Thread creation
---------------
Thread interleaving creates nondeterminism, which we want to avoid during simulation and thus, we instrument calls to :java:ref:`java.lang.Thread.start()` :java:ref:`java.lang.Thread.sleep()`. Attempts to create threads are intercepted and the simulation halts with an error informing the user that deterministic execution cannot be guaranteed. Kompics protocol components are typically reactive and don’t spawn threads of their own, so they lend themselves well to simulation.

In some cases, especially third party libraries, threads are created, but they might not interact with component logic. If this is the case, we can add that class to the exception list (see section :ref:`instrexcps`)and thus it will not get instrumented.

Network
-------
Randomness induced by network parameters, like latency and network loss are modeled using the SimulationScenario and NetworkModel abstraction.

Input/Output sources
--------------------
Currently there is no warning/error for using |io| Sources, but it should be kept in mind that these might introduce uncertainty into the simulation.

.. _instrexcps:

Instrumentation Exceptions
--------------------------
You can add classes to the instrumentation exception list by adding the following lines to your configuration file:

.. code-block:: none

	instrumentation.exceptions = [
		"a.b.c.MyClass",
		"d.e.f.MyOtherClass"
	]