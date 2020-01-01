Messages, Addresses, and Headers
--------------------------------
The @javadoc:[Network](se.sics.kompics.network.Network) port only allows three kinds of events: A @javadoc:[Msg](se.sics.kompics.network.Msg) may pass in both directions (indication and request), while @javadoc:[MessageNotify.Req](se.sics.kompics.network.MessageNotify.Req) is a request and  @javadoc:[MessageNotify.Resp](se.sics.kompics.network.MessageNotify.Resp) is an indication. The latter two can be used to ask the `Network` service for feedback about whether or not a specific message was sent, how long it took, and how big the serialised version was. This is convenient for systems that need to keep statistics about their messages, or where resources should be freed as soon as a message crosses the wire.
All messages that go over the network have to implement the @javadoc:[Msg](se.sics.kompics.network.Msg) interface, which merely stipulates that a message has to have some kind of @javadoc:[Header](se.sics.kompics.network.Header). The deprecated methods still need to be implemented for backwards compatibility, but should simply forward fields from the header.

### Header

The header itself requires three fields:
	
1. A **source** address, which on the sender side is typically the *self* address of the node, and on the receiver side refers to the sender.
2. A **destination** address, which tells the network on the sender side, where the message should go, and is typically the *self* address on the receiver side.
3. The **transport** protocol to be used for the message. There is no requirement for all ``Network`` implementations to implement all possible protocols. @javadoc:[NettyNetwork](se.sics.kompics.network.netty.NettyNetwork) implements `TCP`, `UDP`, and `UDT` only.

### Address

The @javadoc:[Address](se.sics.kompics.network.Address) interface has four required methods:
	
1. @javadoc:[IP](se.sics.kompics.network.Address#getIp()) and 
2. @javadoc:[port](se.sics.kompics.network.Address#getPort()) are pretty self explanatory.
3. @javadoc:[asSocket](se.sics.kompics.network.Address#asSocket()) should get a combined represenation of IP and port. It is strongly recommended to keep the internal representation of the address as @javadoc:[InetSocketAddress](java.net.InetSocketAddress), since this method will be called a lot more often than the first two, and creating a new object for it on the fly is rather expensive.
4. Finally, the @javadoc:[sameHostAs](se.sics.kompics.network.Address#sameHostAs(se.sics.kompics.network.Address)) method is used for avoiding serialisation overhead when the message would end up in the same JVM anyway. This can be used for local addressing of components and is typically important for @ref:[virtual nodes](../virtual/index.md), where components on the same JVM will often communicate via the network port.

@@@ note

None of the interface presented above make any requirements as to the (im-)mutability of their fields. It is generally recommended to make all of them immutable whenever possible. However, certain setups may require mutable headers, for example. A routing component might be implemented in such a way.

@@@

### Implementations

Since almost all application will need custom fields in addition to the fields in the @javadoc:[Msg](se.sics.kompics.network.Msg), @javadoc:[Header](se.sics.kompics.network.Header), or @javadoc:[Address](se.sics.kompics.network.Address) interfaces, almost everyone will want to write their own implementations. However, if that should not be the case, a simple default implementation can be found in @javadoc:[netty.DirectMessage](se.sics.kompics.network.netty.DirectMessage), @javadoc[netty.DirectHeader](se.sics.kompics.network.netty.DirectHeader), and @javadoc:[NettyAddress](se.sics.kompics.network.netty.NettyAddress). For the purpose of this tutorial, however, we will write our own, which we will prefix with the letter **T** (for *Tutorial*) to easily differentiate it from the interfaces.

#### TAddress

Java
:	@@snip[TAddress.java](/docs/src/main/java/jexamples/networking/pingpong/TAddress.java) {  }

Scala
:	@@snip[class TAddress](/docs/src/main/scala/sexamples/networking/pingpong/Messages.scala) { #address }

#### THeader

Java
:	@@snip[THeader.java](/docs/src/main/java/jexamples/networking/pingpong/THeader.java) {  }

Scala
:	@@snip[class THeader](/docs/src/main/scala/sexamples/networking/pingpong/Messages.scala) { #header }

#### TMessage

Java
:	@@snip[TMessage.java](/docs/src/main/java/jexamples/networking/pingpong/TMessage.java) {  }

Scala
:	@@snip[class TMessage](/docs/src/main/scala/sexamples/networking/pingpong/Messages.scala) { #message }


For our *PingPong* example we shall have both `Ping` and `Pong` extend `TMessage` instead of using the direct request-response. We also hard-code `TCP` as protocol for both for now.

#### Ping

Java
:	@@snip[Ping.java](/docs/src/main/java/jexamples/networking/pingpong/Ping.java) {  }

Scala
:	@@snip[class Ping](/docs/src/main/scala/sexamples/networking/pingpong/Messages.scala) { #ping }

#### Pong

Java
:	@@snip[Pong.java](/docs/src/main/java/jexamples/networking/pingpong/Pong.java) {  }

Scala
:	@@snip[class Pong](/docs/src/main/scala/sexamples/networking/pingpong/Messages.scala) { #pong }


Now we need to change the `Pinger` and the `Ponger` to take a *self* address as init-parameter, require the @javadoc:[Network](se.sics.kompics.network.Network) port and feed the new constructor arguments to the `Ping` and `Pong` classes. Since we are using the network port now for the ping-pong exchange, we can remove the requirement for the `PingPongPort`. In this first example we shall make our lives somewhat simple and use the *self* address as source and destination for both classes. This corresponds to the local reflection example mentioned above.

#### Pinger

Java
:	@@snip[Pinger.java](/docs/src/main/java/jexamples/networking/pingpong/Pinger.java) {  }

Scala
:	@@snip[Pinger.scala](/docs/src/main/scala/sexamples/networking/pingpong/Pinger.scala) { }

#### Ponger

Java
:	@@snip[Ponger.java](/docs/src/main/java/jexamples/networking/pingpong/Ponger.java) {  }

Scala
:	@@snip[Ponger.scala](/docs/src/main/scala/sexamples/networking/pingpong/Ponger.scala) { }


#### Parent
Finally, we have to create the `NettyNetwork` component in the `Parent` class, and connect it appropriately. 

Java
:	@@snip[Parent.java](/docs/src/main/java/jexamples/networking/pingpong/Parent.java) {  }

Scala
:	@@snip[Parent.scala](/docs/src/main/scala/sexamples/networking/pingpong/Parent.scala) { }

#### Main
As a preparation for later, we are going to take the port for *self* address as a commandline argument in the `Main` class, and hardcode `127.0.0.1` as IP address.

Java
:	@@snip[Main.java](/docs/src/main/java/jexamples/networking/pingpong/Main.java) {  }

Scala
:	@@snip[Main.scala](/docs/src/main/scala/sexamples/networking/pingpong/Main.scala) { }


### Execution

At this point we can compile and run the code with a slight variation to the usual commands, since we need the port as an argument now (pick a different port if `34567` is in use on your system):

@@@ div { .group-java }
```bash
runMain jexamples.networking.pingpong.Main 34567
```
@@@

@@@ div { .group-scala }
```bash
runMain sexamples.networking.pingpong.Main 34567
```
@@@

### Problems

We can see from the output that our setup technically works, but we are back to the problem of getting four `Pong`s on our two `Ping`s. The reason is, of course, that we are cheating. We are running all components in the same JVM, but we are not using proper @ref:[virtual network](../virtual/index.md) support, yet. We also do not actually want to use virtual nodes here, but instead each `Pinger` and ``Ponger`` should be on a different host (or at least in a different JVM).

Before we can fix this, though, we have to fix another problem that we do not even see, yet: None of our messages are currently serialisable.
