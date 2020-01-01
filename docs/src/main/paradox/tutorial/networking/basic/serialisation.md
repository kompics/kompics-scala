Serialisation
-------------
With @javadoc:[NettyNetwork](se.sics.kompics.network.netty.NettyNetwork) comes a serialisation framework that builds on Netty's @javadoc:[ByteBuf](io.netty.buffer.ByteBuf) facilities. The Kompics part is two-fold: It consists of the @javadoc[Serializer](se.sics.kompics.network.netty.serialization.Serializer) interface, and a registration, mapping, and lookup service for such serialisers in @javadoc[Serializers](se.sics.kompics.network.netty.serialization.Serializers).

### Serialiser Identifiers
For every @javadoc[Serializer](se.sics.kompics.network.netty.serialization.Serializer) you write and register you will be required to pick a unique @java[`int`]@scala[`Int`] identifier. You are required to keep track of the identifiers you use yourself. Kompics reserves the range 0-16 for internal use. By default only a single @java[`byte`]@scala[`Byte`] worth of identifier space is used, in order to minimise the overhead of the framework. If you are assigning larger identifiers than 255 (or their signed equivalents), make sure to use the @javadoc[resize](se.sics.kompics.network.netty.serialization.Serializers#resize(se.sics.kompics.network.netty.serialization.Serializers.IdBytes)) method *before* registering the respective serialisers.

@@@ note

For larger projects it can become quite difficult to keep track of which identifiers are used and which are free. It is recommended to either assign them all in a single place (e.g., @java[static fields of a class]@scala[fields of an object], or a configuration file), or assign subranges to submodules of the project and then do the same single-location-assignment per module.

@@@

### Serialiser Methods

When implementing the @javadoc[toBinary](se.sics.kompics.network.netty.serialization.Serializer#toBinary(java.lang.Object,io.netty.buffer.ByteBuf)) and @javadoc[fromBinary](se.sics.kompics.network.netty.serialization.Serializer#fromBinary(io.netty.buffer.ByteBuf,java.util.Optional)) methods you are free to do as you like, as long as at the end of @javadoc[toBinary](se.sics.kompics.network.netty.serialization.Serializer#toBinary(java.lang.Object,io.netty.buffer.ByteBuf)) the whole object is in the @javadoc:[ByteBuf](io.netty.buffer.ByteBuf) and at the end of @javadoc[fromBinary](se.sics.kompics.network.netty.serialization.Serializer#fromBinary(io.netty.buffer.ByteBuf,java.util.Optional)) the same number of bytes that you put into the @javadoc:[ByteBuf](io.netty.buffer.ByteBuf) are removed from it again.

### Serializers

The @javadoc[Serializers](se.sics.kompics.network.netty.serialization.Serializers) singleton class has two important functions. First, it registers serialisers into the system, so that they can be looked up when messages come in that have been serialised with them (as defined by their identifier). And second, it maps *types* to specific @javadoc[Serializer](se.sics.kompics.network.netty.serialization.Serializer) instances that are already registered. The most convenient way to do that is by registering a short name to the serialiser instance via @javadoc[register(Serializer, String)](se.sics.kompics.network.netty.serialization.Serializers#register(se.sics.kompics.network.netty.serialization.Serializer,java.lang.String)), and then register the class mappings to that name using @javadoc[register(Class, String)](se.sics.kompics.network.netty.serialization.Serializers#register(java.lang.Class,java.lang.String)). Alternatively, the identifier can be used as well with @javadoc[register(Class, int)](se.sics.kompics.network.netty.serialization.Serializers#register(java.lang.Class,int)) or both can be achieved at once for a single type-serialiser-mapping using @javadoc[register(Class, Serializer)](se.sics.kompics.network.netty.serialization.Serializers#register(java.lang.Class,se.sics.kompics.network.netty.serialization.Serializer)).

The lookup of serialisers for types is hierarchical, that is, when there is no exact mapping found for the type, the system will traverse its type hierarchy upwards to find a matching supertype @javadoc[Serializer](se.sics.kompics.network.netty.serialization.Serializer) and attempt to use that one instead. For example, if the object to be serialised implements Java's @javadoc:[Serializable](java.io.Serializable) interface and there is no more specific @javadoc[Serializer](se.sics.kompics.network.netty.serialization.Serializer) registered, the object will be serialised with Java object serialisation (which is registered by default).

### Default Serialisers

The other default serialisers (and their identifiers) are:

- With id=0: @javadoc:[NullSerializer](se.sics.kompics.network.netty.serialization.SpecialSerializers.NullSerializer) for `null`
- With id=1: @javadoc:[ByteSerializer](se.sics.kompics.network.netty.serialization.SpecialSerializers.ByteSerializer) for @java[`byte[]`]@scala[`Array[Byte]`]
- With id=2: @javadoc:[AddressSerializer](se.sics.kompics.network.netty.serialization.SpecialSerializers.AddressSerializer) for @javadoc:[NettyAddress](se.sics.kompics.network.netty.NettyAddress)
- With id=3: @javadoc:[JavaSerializer](se.sics.kompics.network.netty.serialization.JavaSerializer) for anything that implements @javadoc:[Serializable](java.io.Serializable)
- With id=4: @javadoc:[ProtobufSerializer](se.sics.kompics.network.netty.serialization.ProtobufSerializer) for @link:[Protocol Buffers](https://developers.google.com/protocol-buffers/?hl=en) (not registered by default)
- With id=5: @javadoc:[NettySerializer](se.sics.kompics.network.netty.NettySerializer) for @javadoc:[NettyNetwork](se.sics.kompics.network.netty.NettyNetwork) internal messages
- With id=6: @javadoc:[UUIDSerializer](se.sics.kompics.network.netty.serialization.SpecialSerializers.UUIDSerializer) for @javadoc:[UUID](java.util.UUID)
- With id=7: @javadoc:[AvroSerializer](se.sics.kompics.network.netty.serialization.AvroSerializer) for @link:[Avro](https://avro.apache.org/) (not registered by default)
- Ids 8 to 16 are reserved for future additions

@@@ note

For people familiar with common Java serialisation frameworks it might be obvious that @link:[Kryo](https://github.com/EsotericSoftware/kryo) is missing from the above list. This is on purpose. Kompics' previous messaging middleware used to rely on Kryo for serialisation, but Kryo's low level object layout hacks lead to a host of problems with deployments that included different types of JVMs (e.g., Oracle JRE and Open JRE, or different versions). For this reason we have purposefully moved away from Kryo serialisation. You are welcome to implement your own Kryo-based @javadoc[Serializer](se.sics.kompics.network.netty.serialization.Serializer), just know that in some setups you might encounter a lot of very difficult to pinpoint bugs.

@@@

### PingPong Serialisers

In our *PingPong* example, the classes that need to be serialised are: `TAddress`, `THeader`, `Ping`, and `Pong`. Note that there is no necessesity to serialise ``TMessage`` separately from ``Ping`` and ``Pong`` at this point, as it has no content of its own and cannot be instantied. Of course, we could simply have all of those classes implement @javadoc:[Serializable](java.io.Serializable) @scala[(as the case classes already do)] and be done with it. Since this is a tutorial (and Java's object serialisation is terribly inefficient), we are instead going to write our own custom serialisers. As preparation for future sections and to show off the system, we will split the serialisation logic into two classes: `NetSerializer` will deal with `TAddress` and `THeader` instances, while `PingPongSerializer` will deal with `Ping` and `Pong` messages. Since `THeader` is part of the messages, we will of course call the `NetSerializer` from the `PingPongSerializer`, but we will act as if we do not know what kind of @javadoc:[Header](se.sics.kompics.network.Header) we are dealing with and let the serialisation framework figure that out. Similarly, `TAddress`es are part of a `THeader`, so we will invoke the `NetSerializer` from itself. Note, that the difference between two approaches is whether or not the serialiser's identifier will be prepended. If we invoke a serialiser directly from within another serialiser, it is assumed that it will be known at serialisation and deserialisation time what the object is and the identifier can be omitted. If we instead use the @javadoc[Serializers](se.sics.kompics.network.netty.serialization.Serializers) facilities, the system will keep track of that itself using the identifier.

In order to keep our identifier spaces separate we'll pick id=100 for the `NetSerializer` and id=200 for the `PingPongSerializer`. And since we only have two instances to deal with, we'll just define the identifiers within the classes in the lazy way. 

@java[We also added some extra constructors to the messages, to make it easier to pass `THeader` instances from another serialiser during deserialisation.]

#### NetSerializer

Java
:	@@snip[NetSerializer.java](/docs/src/main/java/jexamples/networking/pingpongdistributed/NetSerializer.java) {  }

Scala
:	@@snip[NetSerializer.scala](/docs/src/main/scala/sexamples/networking/pingpongdistributed/NetSerializer.scala) { }

#### PingPongSerializer

Java
:	@@snip[PingPongSerializer.java](/docs/src/main/java/jexamples/networking/pingpongdistributed/PingPongSerializer.java) {  }

Scala
:	@@snip[PingPongSerializer.scala](/docs/src/main/scala/sexamples/networking/pingpongdistributed/PingPongSerializer.scala) { }

@@@ note

The serialisation above code is not particularly space efficient, since that was not the purpose. To get an idea how to use bit-fields to get a significant space reduction on these kind of frequently used serialisation procedures, take a look at the source code of the [SpecialSerializers class](https://github.com/kompics/kompics/blob/master/basic/component-netty-network/src/main/java/se/sics/kompics/network/netty/serialization/SpecialSerializers.java).

@@@

All that is left is now is to register the new serialisers and map the right types to them. We add the following to the `Main` @java[class]@scala[object]:

Java
:	@@snip[Main.java](/docs/src/main/java/jexamples/networking/pingpongdistributed/Main.java) { #serializers }

Scala
:	@@snip[Main.scala](/docs/src/main/scala/sexamples/networking/pingpongdistributed/Main.scala) { #serializers }

Distributed PingPong
--------------------
Now we are prepared for a true distributed deployment of our *PingPong* example. There are a number of changes we need to make to the way we set up the component hierarchy. First of all, we want to deploy `Pinger` and `Ponger` separately, and they also need different parameters. The `Ponger` is purely reactive and it only needs to know its own *self* address. The `Pinger`, on the other hand, needs to know both its own address and a `Ponger`'s. We are going to make use of that distinction in the `Main` @java[class]@scala[object], in order to decide which one to start. If we see *two* commandline arguments (1 IP and 1 port), we are going to start a `Ponger`. If, however, we see *four* commandline arguments (2 IPs and 2 ports), we are going to start a `Pinger`. Since our application components now need different setup, we are also going to split the `Parent` into a `PingerParent` and a `PongerParent`. Note, that it is always a good idea to have a class without any business logic that sets up the `Network` and `Timer` connections, as you will see in the section on @ref:[simulation](../../simulation/index.md).

#### PingerParent

Java
:	@@snip[PingerParent.java](/docs/src/main/java/jexamples/networking/pingpongdistributed/PingerParent.java) {  }

Scala
:	@@snip[Parent.scala](/docs/src/main/scala/sexamples/networking/pingpongdistributed/Parent.scala) { #pinger-parent }

#### PongerParent

Java
:	@@snip[PongerParent.java](/docs/src/main/java/jexamples/networking/pingpongdistributed/PongerParent.java) {  }

Scala
:	@@snip[Parent.scala](/docs/src/main/scala/sexamples/networking/pingpongdistributed/Parent.scala) { #ponger-parent }

#### Main

Java
:	@@snip[Main.java](/docs/src/main/java/jexamples/networking/pingpongdistributed/Main.java) { filterLabels=true }

Scala
:	@@snip[Main.scala](/docs/src/main/scala/sexamples/networking/pingpongdistributed/Main.scala) { filterLabels=true }

### Execution

Now we are finally ready to try this. The following commands should be run in different terminals (different sbt instances), preferably side by side so you can clearly see how things are happening. For all the examples, feel free to change the IPs and ports as you like, but be sure they match up correctly.

Start the `Ponger` first with:

@@@ div { .group-java }
```bash
runMain jexamples.networking.pingpongdistributed.Main 127.0.0.1 34567
```
@@@

@@@ div { .group-scala }
```bash
runMain sexamples.networking.pingpongdistributed.Main 127.0.0.1 34567
```
@@@


Then start a `Pinger` with:

@@@ div { .group-java }
```bash
runMain jexamples.networking.pingpongdistributed.Main 127.0.0.1 34568 127.0.0.1 34567
```
@@@

@@@ div { .group-scala }
```bash
runMain sexamples.networking.pingpongdistributed.Main 127.0.0.1 34568 127.0.0.1 34567
```
@@@

You can start the same `Pinger` multiple times or start as many `Pinger` JVMs in parallel as you like. Make sure they all have their own port, though!

You will see that there is no `Ping` amplification anymore as we saw in the last example. All messages are now correctly addressed and replied to. If you struggle to clearly see what is going on among all the logging output, try to reduce the logging level to `info` in the @github[logback.xml](/docs/src/main/resources/logback.xml) file. Finally, use `Control-c` to shutdown the `Ponger`, as it will run forever otherwise.

