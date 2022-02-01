.. _answering_requests:


Answering Requests
=======================

So far in our examples, we have used some form of a request-response pattern where we verify that the ``Pinger`` sends a ``Ping`` request with some ``ìd`` and the ``Ponger`` responds with a ``Pong`` of the same ``id``.
Since we know the value of ``id`` that each ping-pong exchange should agree on, we could easily specify matchings for them - e.g using the ``new Ping(8)`` or an equivalent ``matchesPing8`` predicate).
However, in some cases, the ``id`` might be some randomly generated value, in which case we can not accurately specify matchings for the request and response events (we could match by class but that doesn't tell us much).
Kompics offers some constructs for supporting a specialized instance of such scenarios - where request events go out of the CUT and response events come into it.
Here, instead of a dependency component, you provide a response for outgoing requests.

For the examples in this section we change our ``Pinger`` component definition to the following. It triggers three ``Ping`` events with ``id`` s 1-3 on start.

.. literalinclude:: answering-requests/src/main/java/se/sics/test/Pinger.java


Answering Requests Immediately
----------------------------------------

The first construct is the ``answerRequest`` statement that matches an outgoing event of a specified class by calling a provided :java:ref:`com.google.common.base.Function\<RQ, RS\>` instance (called the mapper) with an observed event as argument.
The mapper contains your logic for verifying that the event is indeed the expected request event and returns a response event in that case otherwise ``null`` is returned.
The framework immediately triggers the response event on a specified port, presumably as an incoming event to the CUT.

The following example shows a test case matching the three triggered events of our new component under test ``Pinger``.
Here we treat ``Ping`` 1 and 3 as requests, triggering the response provided by our ``pingToPongMapper`` back into the response port ``pingerPort`` (the last argument to ``answerRequest``).
The ``Ping`` 2 event on the other hand is simply matched normally and in this case, thrown away since our port is not connected to any other ports.

.. literalinclude:: answering-requests/src/main/java/se/sics/test/Main.java

The entire test case can also be downloaded :download:`here <answering-requests.zip>`.


Answering Requests After a While
------------------------------------------

By default, ``answerRequest`` triggers the generated response event right away on the response port but this might not always be desired.
In some cases we might want to wait a while before triggering the responses.
You can force the events to be triggered only after all requests have been received by using the ``answerRequests`` construct of the form ``answerRequests() - end()``.
Here, ``-`` is a sequence of ``answerRequest`` statements that will be answered *in that order* only after all requests have been received.
The following example shows such a usage.
Calling ``answerRequests()`` enters ``ANSWER_REQUEST`` mode while the matching ``end()`` restores the previous mode.

.. code-block:: java

  public static void main(String[] args) { // answerRequestsExample
    // ... setup code as in answerRequestExample ...
    // setup done
    tc.body()
          // treat all three pings as requests
          .answerRequests()
              .answerRequest(Ping.class, pingerPort, pingToPongMapper, pingerPort) // ping 1
              .answerRequest(Ping.class, pingerPort, pingToPongMapper, pingerPort) // ping 2
              .answerRequest(Ping.class, pingerPort, pingToPongMapper, pingerPort) // ping 3
              // After receiving ping 3, trigger pongs 1,2,3 in that order back on the response port 'pingerPort'
          .end()
    ;    
    assertTrue(tc.check());
    assertEquals(3, Pinger.pongsReceived);
  }


Answering Requests In any Order
-------------------------------------------

To match outgoing requests in any order, you can group the ``answerRequest`` statements within an ``unordered`` statement.
This statement also has a variant ``unordered(boolean)`` where supplying ``true`` as argument triggers each response event immediately after the request is matched and ``false`` triggers them in the order that they were received, after all requests have been matched.

Answering Requests In the Future
-----------------------------------------

Using a mapper function, ``answerRequest`` gives limited options as to when to trigger the response events (immediately or when all requests have been received).
If you need better control over which and when response events to requests are triggered, you can provide :java:ref:`se.sics.kompics.testing.Future` instance instead of a mapper.

The ``Future`` abstract class declares a ``set`` and ``get`` method.
The ``set`` method when called with an event as argument returns ``true`` if the event was matched and in which case, a subsequent call to ``get`` should provide a response event.
However, ``get`` is only called if that same ``Future`` instance is used later on in a ``trigger`` statement - the framework triggers the provided response event on the specified port.
Consequently, whether or not a response is triggered and in what order is entirely up to you.

In the following example, we match our requests in any order and choose to only trigger one response back to our CUT.


.. code-block:: java

  class PingPongFuture extends Future<Ping, Pong> {
          private Pong pong;
          private int id;
          PingPongFuture(int id) { this.id = id; }

          public boolean set(Ping ping) {
                  if (ping.id != id) {
                          return false;
                  }
                  // success
                  pong = new Pong(ping.id);
                  return true;
          }
          public Pong get() {
                  // after a successful call to set(), get must succeed.
                  return pong;
          }
  }

  PingPongFuture future1 = new PingPongFuture(1);
  PingPongFuture future2 = new PingPongFuture(2);
  PingPongFuture future3 = new PingPongFuture(3);

  public static void main(String[] args) { // futureExample
    // ... setup code as in answerRequestExample ...
    // setup done
    tc.body()
          // treat all three pings as requests and match them in any order
          .unordered()
              .answerRequest(Ping.class, pingerPort, future3) // ping 3
              .answerRequest(Ping.class, pingerPort, future2) // ping 2
              .answerRequest(Ping.class, pingerPort, future1) // ping 1
          .end()
          // set() for all futures must have succeeded
          // trigger only pong 2 back as response

          .trigger(future2, pingerPort)
    ;    
    assertTrue(tc.check());
    assertEquals(1, Pinger.pongsReceived);
  }


