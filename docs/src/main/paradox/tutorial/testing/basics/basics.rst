.. _testing-basics:


Testing Basics
=======================

A test case when testing in Kompics is a method describing the expected behavior of the component under test (CUT) for a given scenario.
This behavior is specified in terms of the expected sequence of events that go in and out of the component.
You can think of a *behavior* simply as a sequence of events while a test case describes a set of behaviors, any of which the CUT's runtime execution must match in order for a test case to be a successful.
The framework executes an instance of the CUT, observing the events that actually occur at runtime and matching them against the next expected event according to the test case.
If a match is unsuccessful then the test case fails immediately.
Kompics allows you to test your component either in a realistic environment, complete isolation and anywhere in between.
A test case may also inject events into the environment.

The ``TestContext`` class implements the DSL for writing test cases in this manner.

In this part of the tutorial we add an ``id`` field to identify our ``Ping`` and ``Pong`` events using :java:ref:`java.util.Comparator` instances. We also create two components ``Pinger`` and ``Ponger``.

.. literalinclude:: pingpong-testing/src/main/java/se/sics/test/Ping.java
.. literalinclude:: pingpong-testing/src/main/java/se/sics/test/Pong.java
.. literalinclude:: pingpong-testing/src/main/java/se/sics/test/PingPongPort.java
.. literalinclude:: pingpong-testing/src/main/java/se/sics/test/Pinger.java
.. literalinclude:: pingpong-testing/src/main/java/se/sics/test/Ponger.java




The behavior of ``Pinger`` is straightforward - it triggers a single ``Ping`` (with ``id`` 8) event when started.
On the other hand, ``Ponger`` listens for incoming ``Ping`` s and responds with a ``Pong`` of the same ``id`` unless the ``id`` happens to be 0 in which case it responds with two ``Pongs`` of ``id`` s 1, 2 in that order.


.. _basicsexample:

The following example shows a test case for this behavior of ``Ponger``.
The entire test case can also be downloaded :download:`here <pingpong-testing.zip>`.


.. literalinclude:: pingpong-testing/src/main/java/se/sics/test/Main.java

The ``newInstance`` method creates a new ``TestContext`` specifically for our ``Ponger`` component - an instance of which is retrieved by calling ``getComponentUnderTest``.
The framework begins observing and matching events only when the ``check`` method is called. As such, this method should only be called finally when the test case has been specified.

Method calls in the body of the test case form statements that instruct the framework to perform specified actions.

The ``expect`` method causes the framework to observe a single event and match it against the specified arguments - failing the test case if no event is observed or the match is not successful.
The body of our test case says that first, we expect a ``Ping`` event, with an ``id`` 8, going into the ``Ponger``'s port (as sent by our connected ``Pinger`` component).
This should then be followed by a ``Pong`` response with the same ``id`` coming out of it.


The ``trigger`` method causes the framework to trigger an event on the specified port. In our example we next trigger an event on the ``Ponger``'s port. In a sense, we mock the behavior of ``Pinger`` by providing stimuli (a ``Ping`` with ``id`` 0) to our CUT just as if it were sent by ``Pinger``.

Next, using a :ref:`conditional statement <conditionals>`, our test case says that the framework should expect *either* a ``Pong`` of ``id`` 1 followed by another with ``id`` 2 *or* a ``Pong`` 3 followed by another with ``id`` 4.
Depending on what events actually show up at runtime, the framework chooses which branch of the statement to match.
So if event ``Pong`` 1 occurs then it is matched successfully and the rest of the ``either`` branch is executed (the framework tries to observe and match an equivalent event to ``Pong`` 2).
Here, we know that this will be the case and that the test case should pass.
Have ``Ponger`` send different ``id`` s in response to a ``Ping`` 0 or change the expected events and see that the test case fails instead.


If we assign each event a symbol, like in our example the letters a-f as shown using comments, then the set of behaviors that our test case describes is {abcd, abef} so that the test cases passes if either of these sequences are observed and no other events occur.
An equivalent notation to describe this set is the `regular expression <https://en.wikipedia.org/wiki/Regular_expression#Basic_concepts>`_ ``ab(cd|ef)``, where the ``either-or`` conditional statement is used as the boolean `or` operator ``|``.
The DSL also explicitly :ref:`supports constructs <blocks>` for the quantification operators of regular expressions ``*`` and ``{n}`` for matching any number of occurrences of a sequence.
This might be helpful when thinking of concise ways to write test cases.


.. _setup:

Setup
------------
As shown in the :ref:`previous example <basicsexample>`, we might need to set up our test environment before we begin matching events.
Any setup activites must be carried out before calling ``body`` for the first time - signalling that we would now like to start describing the expected behaviors of our component.
We refer to this part of the test case (before the ``body`` is called) as the *setup header* of the test case.

Within the setup header, the following methods can be called:

``create``
  Create instances of other components that may communicate with the CUT.
``connect``
  Connect any two ports.
``setComparator``
  Register a :java:ref:`java.util.Comparator` instance for an event class to be used when :ref:`matching events <matching-events>` of that type.
``setDefaultAction``
  By default unmatched events cause the test case to fail. This method :ref:`overrides this behavior <defaultActions>` for desired events by registering a ``Function``.
``setTimeout``
 To ensure test case termination and correctness, the framework needs to know how long to wait for an event to occur before making a decision.
 This method overrides the default timeout.


Modes
-------------
Using method calls to write our test cases necessarily mean that the order in which the methods are called matter.
But more importantly, some combinations of methods calls are simply not legal.
The DSL uses the concept of modes to enforce the legality of test cases by assigning each method a set of valid modes in which they can be used.
Additionally, calling certain method calls change the current mode of the test case and calling a method in an illegal mode always throws an exception.
The possible modes can be found here :java:ref:`se.sics.kompics.testing.MODE`.
Please refer to the javadoc :java:ref:`se.sics.kompics.testing.TestContext` for a list of all methods and their valid modes.



.. _conditionals:

Conditionals
--------------------

A conditional is a compound statement of the form ``either() - or() - end()`` where ``-`` represents the ``either`` and ``or`` branches of the conditional and may contain other statements that would normally appear in the body of a test case, including other conditionals.
When ``either()`` is called, it changes the current mode ``X`` of the test case to ``CONDITIONAL`` and when the matching ``end()`` is called, the current mode is restored to ``X``.

In a typical conditional statement like the if-then(-else) constructs of most programming languages, the necessary condition for executing the ``if`` or ``else`` branch is always clear - If the boolean condition is true then the ``if`` branch is executed otherwise the ``else`` branch is  executed.
In our ``either-or`` construct the condition for matching the branches is inferred from the actual events that are observed at runtime.
If an observed event ``e`` is matched by the first statement in the ``either`` branch then the framework continues to execute statements in that branch and the same goes for the ``or`` branch.
If the first statements in both branches match ``e`` the framework proceeds to execute the next statements of both branches and so on until there is a divergence (a branch's statement fails to match an observed event).

.. _blocks:

Blocks
-------------

The DSL uses the concepts of blocks as a way to split the statements in a test case into partitions that make up a block.
By doing so we may specify instructions to the framework like executing the statements of a block a particular number of times, whitelisting or blacklisting events that can occur while the framework is executing statements within a block etc.

The following code snippet shows an example of a block.

.. _examplerepeatwithoutblock:

.. code-block:: java

  tc.repeat(5)
       // block header
    .body()
       // block body
      .expect(new Ping(1), pingerPort, IN) // a
      .expect(new Ping(2), pingerPort, IN) // b
    .end();

A block is of the form ``repeat() A body() B end()`` where ``A`` and ``B`` are the *header* and *body* respectively of the block.
The statements in a block body is executed sequentially by the framework while those at the header are used to filter out events that may occur while the framework is executing the statements in the body.
Once ``repeat`` is called, the test case enters ``HEADER`` mode from some previous mode ``X`` and statements allowed in the block header can be called (these statements are optional as a block header may be empty). Calling ``body()`` moves the test case into ``BODY`` mode and finally calling the matching ``end()`` restores the current mode back to ``X``.

In our example, the ``repeat(5)`` instructs the framework to execute the statements in the block body exactly 5 times in succession.
As a single execution of the block body matches the sequence ``ab``, the entire block matches the sequence ``ababababab`` - that is 5 occurrences of ``ab`` similar to the regular expression ``(ab){5}``.

.. _blockheader:

Block Headers
^^^^^^^^^^^^^^^^^

Within a block, some events may be *disallowed* by the test case - the occurrence of such events at any point in the block's execution is undesirable so that the test case should fail if observed.
In some other cases, some events may occur but they are not required for a successful test case - that is, they are *allowed* if they do occur.
Finally, some events may be *dropped* if they do occur - if incoming then they are not delivered to the CUT and if outgoing they are not forwarded to any other components in the environment.
For these three cases Kompics offers the respective statements ``disallow``, ``allow`` and ``drop`` that can be called within the header of a block.

The following code snippet shows the :ref:`previous example <examplerepeatwithoutblock>` with a single header statement.

.. code-block:: java

  tc.repeat(5)
       // block header
       .allow(new Ping(0), pingerPort, IN) // c
    .body()
       // block body
      .expect(new Ping(1), pingerPort, IN) // a
      .expect(new Ping(2), pingerPort, IN) // b
    .end(); // end of block

The ``allow`` in this case whitelists the ``Ping`` 0 event within this block (the event may be observed while executing a statement within the block body).
In terms of set of behaviors being described, since the block body already describes the sequence ``ab``, this means that any number of ``c`` events can occur at any positions of this expected sequence.
As such, the regular expression ``(c*ac*bc*){5}`` describes the same set of sequences.

For a concrete example, we utilize the setup code from :ref:`this example <basicsExample>`


.. _example2:

.. code-block:: java

  public static void main(String[] args) { // blocksIntroDemo
    // ... setup code as in basicsExample ...
    // setup done
    tc.body()
        .repeat(2)
            .allow(new Ping(8), pongerPort, Direction.IN)
            .allow(new Pong(8), pongerPort, Direction.OUT)
        .body()
            // this is executed twice
            .trigger(new Ping(0), pongerPort)
            .expect(new Pong(1), pongerPort, Direction.OUT)
            .expect(new Pong(2), pongerPort, Direction.OUT)
        .end()

        // the same scenario but this time do not forward Pong 1 events
        .repeat(3)
            .drop(new Pong(1), pongerPort, Direction.OUT)
        .body()
            // this is executed thrice
            .trigger(new Ping(0), pongerPort)
            .expect(new Pong(2), pongerPort, Direction.OUT)
        .end()
    ;    
    assertTrue(tc.check());
    assertEquals(6, Ponger.pingsReceived);
    assertEquals(8, Pinger.pongsReceived);
  }  

The test case consists of two explicit blocks.
In the first block ``repeat(2)`` we *allow* the ``Ping`` 8 event sent by ``Pinger`` on start and it's response while we execute the same scenario of triggering ``Ping`` 0 and expecting two ``Pong`` s in response.
Within this block ``Pinger`` receives a total of 5 ``Pong`` events while ``Ponger`` receives 3 ``Ping`` events.
In the second block we replay the same scenario three times while *dropping* outgoing ``Pong`` 1 events.
Consequently, ``Pinger`` receives only the three ``Pong`` 2 events that are actually forwarded, bringing its total to 8.

Note that in this example we assumed that the ``Ping`` 8 event would be sent at some point within the first block.
Our assumption is not entirely correct since we haven't taken into consideration any possible delays.
For example, execution of the start handler of ``Pinger`` could be delayed so that the event actually arrives at ``Ponger`` later on when the framework executes statements in the ``repeat(3)`` block.
This would cause our test case to fail since we have not explicitly *allowed* the ``Ping`` event (or its response) there.
For the purpose of this illustration we erred on the side of naiveté, but to be sure one would have to include the same ``allow`` statements in the second block as well.

Nesting Blocks
^^^^^^^^^^^^^^^^^^^

A block ``N`` may be nested within the body of another block ``B``.
Header statements of block ``B`` are in scope while the framework executes statements within ``B`` as well as those of it's nested blocks such as ``N``.
In the case that some event is specified in the header of a block, the same event may be also be specified in the header of nested blocks, in which case the original specification of that event (e.g ``allow``) is shadowed while the nested block is in scope (its statements are being executed).

The following example utilizes nested blocks and shadows headers.

.. _shadowing:

.. code-block:: java

  public static void main(String[] args) { // nestedBlockExample
    // ... setup code as in basicsExample ...
    // setup done
    tc.body()
        // first, handle event from pinger
        .expect(new Ping(8), pongerPort, Direction.IN)
        .expect(new Pong(8), pongerPort, Direction.OUT)

        .repeat(4).body()
            .trigger(new Ping(0), pongerPort)
        .end()

        // after triggering four Ping 0s, ponger should respond with four Pong 1,2 sequences
        .repeat(2)
            // drop Pong 1 events
            .drop(new Pong(1), pongerPort, Direction.OUT)
        .body()
            .repeat(1) // nested block
                // here, handle Pong 1 events (previous drop is shadowed!)
                .allow(new Pong(1), pongerPort, Direction.OUT)
            .body()
                .expect(new Pong(2), pongerPort, Direction.OUT) // preceeding Pong 1 is handled
            .end()
            // drop(...) is back in scope
            .expect(new Pong(2), pongerPort, Direction.OUT) // preceeding Pong 1 is dropped
        .end();
    assertTrue(tc.check());
    assertEquals(7, Pinger.pongsReceived);
  }

.. note::

  In addition to shadowing headers within nested blocks, you may also specify multiple header statements within the same block that end up matching the same event at runtime. The framework's uses a last-statement-wins policy for such cases - choosing the matching statement that was declared last in that block's specification.

Finally, each statement must belong to some block.
The ``TestContext`` instance is created with an implicit block ``repeat(1)`` within which all statements and explicitly created blocks are nested and the entire test case is run.
The calls to ``repeat`` and ``end`` for this block are handled by the framework itself. However the ``body()`` must be called to denote its body.
This is the reason for calling ``body()`` before any sequence can be described - the :ref:`setup header <setup>` is in fact the header of this implicit block and as such can be treated as any normal header.


.. _kleeneBlocks:

Kleene Blocks
^^^^^^^^^^^^^^^^^^^

Omitting an integer argument when creating a block (e.g calling ``repeat()``) instead executes the statements of the block zero or more times.
The next statement of the block is executed as long as the previous one was successful (e.g a successful match using ``expect`` or a ``trigger`` that always succeeds).

As an example, the following code snippet matches any number of outgoing ``Pong`` 0 events followed by a ``Pong`` 1 event.
This construct provides a variant of the Kleene star operator ``*``. In this case, the described set is ``a*b``.

.. code-block:: java

  tc.repeat().body()
        .expect(new Pong(0), pongerPort, OUT)   // a
    .end()
   .expect(new Pong(1), pongerPort, OUT)        // b

Block Entry Functions
^^^^^^^^^^^^^^^^^^^^^^^^^^^^

An instance of the :java:ref:`se.sics.kompics.testing.EntryFunction` interface may be provided as an argument when creating a block.
This interface declares a single ``void run()`` method that is called on entering the block and might be useful if some desired code needs to be run upon executing statements within that block.

The following code snippet shows a minimal usage of an ``EntryFunction`` instance to increment a counter.
Although the body is empty, the framework calls ``run`` for each iteration.

.. _blockInit:

.. code-block:: java

  EntryFunction increment = new EntryFunction() {
          public void run() {
              myCounter.increment();
          }
  };
  tc.repeat(5, increment)
    .body()
    .end();
  //...
  assert myCounter.count == 5;

.. warning::

  Be careful when using entry functions. 
  If the first statement of a block's body can be successfully executed, its entry function is called whether or not the rest of the statements of the block is executed successfully.
  This might not be the intended behavior but it might be best is to avoid writing such test cases.


Ambiguous Testcases
------------------------

In order to correctly verify the behavior of a component, the provided test case should be unambiguous.
Unfortunately, the addition of some constructs like :ref:`Kleene blocks <kleeneBlocks>` and :ref:`Conditionals <conditionals>`, while powerful, make it possible to write test cases whose intention can not be accurately inferred by the framework. 

Consider a scenario where a ``trigger`` is the first statement of a :ref:`Kleene block <kleeneBlocks>`.
The following code snippet shows a minimal example.

.. code-block:: java

  tc.repeat().body
          .trigger(new Ping(0), pongerPort)
    .end();

It is not possible to infer how many times the ``trigger`` statement should be executed - since such a statement always succeeds, the framework would not know when to exit the block.

The second scenario uses the ``trigger`` statement as the first statement in both branches of a :ref:`conditional <conditionals>` as shown in the following example.
Again, it is unclear which of the ``Ping`` events to trigger since the choice of what branch to execute depends on successfully matching events but none of the branches here expect to match an event.

.. code-block:: java

  tc.either()
          .trigger(new Ping(0), pongerPort)
          //...
    .or()      
          .trigger(new Ping(1), pongerPort)
          //...
    .end();


Test cases containing statements of these forms are deemed ambiguous and the behavior of the framework is left unspecified. As such they should be avoided entirely.


Specifying Nondeterministic Testcases
------------------------------------------

The DSL offers two explicit constructs for expressing cases where we are not exactly sure of the ordering of events at certain points even though we know that the events will occur.

.. _unordered:

Unordered
^^^^^^^^^^^^^^^^^

The first is the ``unordered`` statement of the form ``unordered() - end()`` where ``-`` represents a sequence of ``expect`` statements which are executed by the framework in any order such that they are successful.
That is, the events described by the statements are matched in the order that they actually occur at runtime as opposed to the normal sequence of ``expect`` statements whose events must be matched in sequential order.
Calling ``unordered()`` switches the current mode to ``UNORDERED`` while calling the matching ``end()`` restores the previous mode.
The following example verifies the initial ping-pong exchange initiated by ``Pinger`` in its start handler, then using ``unordered``, we verify the ``Ping`` 0 behavior of our CUT - the ``expect`` statements are matched in the order that the events occur.
Comment out the call to ``unordered()`` and its matching ``end()`` and verify that the test case now fails since we would then be expecting ``d`` to happen before ``c``.


.. _unorderedExample:

.. code-block:: java

  public static void main(String[] args) { // unorderedExample
    // ... setup code as in basicsExample ...
    // setup done
    tc.body()
        .expect(new Ping(8), pongerPort, Direction.IN)          // a
        .expect(new Pong(8), pongerPort, Direction.OUT)         // b
        .trigger(new Ping(0), pongerPort)
        .unordered()
            // the specified order of statements does not matter here
            // since 'c' will actually happen before 'd', it will be executed first
            .expect(new Pong(2), pongerPort, Direction.OUT)     // d
            .expect(new Pong(1), pongerPort, Direction.OUT)     // c
        .end();
    assertTrue(tc.check());
  }

Block Expect
^^^^^^^^^^^^^^^^^^^

This range of nondeterministic scenarios that can be expressed with ``unordered`` are limited since the ordering of events are still specified relative to others within the block.
The second construct is the :ref:`header statement <blockheader>` ``blockExpect``, specifying that an event must occur at any point within the block.
Since we are able to group any statements into a single block and use ``blockExpect`` statements to interleave our nondeterministic events with those matched by the block body, we can express an even wider range of scenarios.
The following shows a variant of the :ref:`previous example <unorderedExample>` using this construct.

.. code-block:: java

  public static void main() { // blockExpectExample
    // ... setup code as in basicsExample ...
    // setup done
    tc.body()
        .repeat(1)
            .blockExpect(new Ping(8), pongerPort, Direction.IN)          // a
            .blockExpect(new Pong(8), pongerPort, Direction.OUT)         // b
        .body()    
            // 'a' and 'b' can interleave the execution of any of these three statements
            .trigger(new Ping(0), pongerPort)
            .expect(new Pong(1), pongerPort, Direction.OUT)     // c
            .expect(new Pong(2), pongerPort, Direction.OUT)     // d
        .end();
    assertTrue(tc.check());
  }

Expecting Exceptions
^^^^^^^^^^^^^^^^^^^^^^^^^^

If you want to test that the component throws an exception while handling an event, the ``expectFault`` statements are available.
Place this statement right after the event whose handling causes the exception otherwise the framework treats the thrown exception unexpectedly and fails the test case.
The following code snippet matches an exception of class ``IllegalStateException`` on handling the triggered ``Ping`` event.
An instance of :java:ref:`com.google.common.base.Predicate\<Throwable\>` may also be provided to match the actual expected event if desired.

.. code-block:: java

  tc.trigger(new Ping(-1), pongerPort) // handling this event throws an exception
    .expectFault(IllegalStateException.class)
  //... continue execution normally

.. _defaultActions:

Specifying Default Actions
^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^

Events that are not explicitly matched by any (header) statements in scope cause the test case to fail.
However, you can override this behavior for particular events using the ``setDefaultAction`` statement.
It specifies a :java:ref:`com.google.common.base.Function\<? extends KompicsEvent, Action\>` instance which will be called with an unmatched event of the specified class (or subclass) and returns an :java:ref:`se.sics.kompics.testing.Action`, telling the framework whether to ``DROP`` or ``HANDLE`` the event or ``FAIL`` the test case.
This is shown in the following example where we only explicitly match the outgoin ``Pong`` 8 in response to the ``Pinger``.
However, since we provide a default action to handle the ``Ping 8`` event, the test case succeeds.
Change the implementation of handlePing8 (e.g so that the event is dropped) and verify that the test case does in fact fail.

.. code-block:: java

  Function<Ping, Action> handlePing8 = new Function<Ping, Action> () {
          public Action apply(Ping ping) {
              if (ping.id == 8) {
                  return Action.HANDLE;
              } else {
                  return Action.FAIL;
              }
          }
  };

  public static void main(String[] args) { // defaultActionExample
    // ... setup code as in basicsExample ...
    tc.setDefaultAction(Ping.class, handlePing8);
    // setup done
    tc.body()
            .expect(new Pong(8), pongerPort, Direction.OUT);
    assertTrue(tc.check());
  }


.. _matching-events:

Matching Events
------------------

Statements such as ``expect``, ``allow``, ``disallow`` etc require the framework to determine whether or not an observed event matches what was specified by the statement. Besides the port and direction, they require an event description.
This could be an equivalent instance of the desired event, the event's class or an instance of :java:ref:`com.google.common.base.Predicate\<? extends KompicsEvent\>` that returns true only when called with the expected event.

As an example, in the statement ``expect(new Ping(8), pongerPort, IN)`` used in the :ref:`basics example <basicsExample>`, our desired event was a ``Ping`` with ``id`` 8.
If the framework observes a ``Ping`` event ``p`` when it executes this statement, it checks if a :java:ref:`java.util.Comparator` instance was registered for ``Ping`` events or any of its subclasses using the ``setComparator`` method (the closest match in the class heirarchy is selected) and if found, uses the comparator to determine equivalence.
Otherwise it defaults to the ``equals(Object)`` method.
In our :ref:`example <basicsExample>` we did register ``Ping.comparator`` to extract and compare the ``id`` fields although the same logic could have been implemented by overriding the ``equals`` method of the ``Ping`` class.

If we were not interested in the ``id`` and simply wanted to match an event of type ``Ping`` then the statement ``expect(Ping.class, pongerPort, IN)`` suffices.

Finally, the following code snippet shows how one may match the same ``id`` using a predicate instance.

.. code-block:: java

  Predicate<Ping> matchesPing8 = new Predicate<Ping>() {
            public boolean apply(Ping ping) {
                    return ping.id == 8;
            }
  }
  tc.expect(Ping.class, matchesPing8, pongerPort, IN);


