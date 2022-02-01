@@@ index
* [Messages, Addresses, and Headers](messages.md)
* [Serialisation](serialisation.md)
* [Cleanup](cleanup.md)
@@@

Basic Networking
================
As opposed to many other languages and frameworks commonly used for writing distributed applications (e.g., @link:[Erlang](http://www.erlang.se) and @link:[Akka](http://akka.io/)) in Kompics networking is not handled transparently. Instead, Kompics requires explicit addressing of *messages*, that is events that (could) go over the network. There are two reasons for this design: First of all, events in Kompics are normally not messages in that they are not addressed, but follow channels instead. So the logical extension of this pattern to a distributed setting would be to create cross-network channels manually. However, this would be very unintuitive for a programmer as it would be way too static a setup. And further, and this is the second reason, it would be misleading to assume that local events and network messages have the same semantics. Network messages have to deal with unavailable nodes, connection loss, partitions, and so on. Instead of trying to force a one-size-fits-all solution onto the programmer, as systems like Akka and the venerable @link:[Java RMI](http://docs.oracle.com/javase/7/docs/technotes/guides/rmi/) do, Kompics exposes these challenges and allows system designers to find solutions that are appropriate to their particular applications.

The following sections will describe the Kompics @javadoc:[Network](se.sics.kompics.network.Network) port, and its default implementation @javadoc:[NettyNetwork](se.sics.kompics.network.netty.NettyNetwork). The latter also features a serialisation framework, which will also be described in this part of the tutorial. As our example program we will stick to the *PingPong* code from the previous tutorials and extend it with networking capabilities.

### Dependencies

The examples in this tutorial require two new dependencies:

@@dependency[sbt,Maven,Gradle] {
  group="se.sics.kompics.basic"
  artifact="kompics-port-network"
  version=$kompics.version$
  group2="se.sics.kompics.basic"
  artifact2="kompics-component-netty-network"
  version2=$kompics.version$
}

### Contents

@@toc { depth=2 }

