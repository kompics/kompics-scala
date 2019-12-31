What is Kompics?
================

Semantics
---------
Kompics is a programming model for distributed systems that implements protocols as event-driven components connected by channels. Kompics provides a form of type system for events, where every component declares its required and provided ports, which in turn define which event-types may travel along their channels and in which direction. The channels themselves provide first-in-first-out (FIFO) order, exactly-once (per receiver) delivery and events are queued up at the receiving ports until the component is scheduled to execute them.

Scheduling
----------- 
A component is guaranteed to be only scheduled on one thread at a time and thus has exclusive access to its internal state without the need for further synchronisation. Different components, however, are scheduled in parallel in order to exploit the parallelism expressed in a message-passing program. A scheduled component is executed one handler at a time up to a configurable maximum, after which the component will placed at the end of the queue of components waiting to be scheduled. This behaviour provides a tradeoff between efficiency – re-using component state once it is loaded into cache – and fairness, i.e. avoiding starvation of components with fewer events.

Event-handling
--------------
Importantly, as opposed to Actor systems like @link:[Akka](http://akka.io/) or @link:[Erlang](http://www.erlang.se/), events in Kompics are not addressed to components in any way, but are instead broadcasted across all connected channels. In this way the same event can be received by many components. The components themselves decide which events to handle and which to ignore by subscribing event handlers on their declared ports. Matching of events to handlers is usually based on the event's type-hierarchy, although there are some Kompics extensions that provide pattern matching as well.
