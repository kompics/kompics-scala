@@@ index
* [Basic Networking](basic/index.md)
* [Virtual](virtual/index.md)
@@@

Networking
==========
Since a distributed systems framework is not very useful if it can not communicate with other machines, Kompics provides built-in networking support, of course. There are two kinds of deployment scenarios Kompics' networking middleware is targeted at:

1. Local network or data centre deployments, where every node can directly communicate with every other node in the system, and
2. open internet deployments where some nodes can directly connect to certain nodes, but others are behind NATs.

Additionally, for both these scenarios Kompics supports the creation of *virtual networks* within a single JVM instance, which is often required by protocols that rely on probabilistic load distribtion, such as distributed hash tables (DHTs), for example.

We will begin by describing the fundamentals of networking in Kompics, before describing how to setup up virtual nodes within a Kompics system.

A section on NATed communication is still future work at this point. If you need to know how to do this with Kompics, shoot us an email or a Github issue, and we'll be happy to discuss some ideas.

@@toc { depth=2 }
