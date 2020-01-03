Channel Selectors
-----------------
When a @javadoc:[Channel](se.sics.kompics.Channel) is connected, it can optionally be given an instance of @javadoc:[ChannelSelector](se.sics.kompics.ChannelSelector) @scala[(at least using the Java API for this)], which is both a way to extract some value from an event (i.e. a specific field to use as a *selection key*) and also define a specific value for which this event should travel along this @javadoc:[Channel](se.sics.kompics.Channel). The internal implementation of this is fairly efficient, but requires the selection value for a @javadoc:[Channel](se.sics.kompics.Channel) to be immutable. Note that, despite its previous name, a @javadoc:[ChannelSelector](se.sics.kompics.ChannelSelector) can not be abused as a filter. It is better to think of it as a way to create a routing table (which is in fact pretty much what happens internally).

### Support for Node Ids

We will update our *PingPong* example such that we start a configurable number of `Pinger`s per host. We will stick to a single `Ponger` for now, since otherwise we would have to figure out how to load-balance across them. We shall add a @java[`byte[]`]@scala[`Array[Byte]`] *id* field to `TAddress`, that we will use as a selection key. However, when creating those ids we shall be a bit lazy and simply use ascending integers in their @java[`byte[]`]@scala[`Array[Byte]`] representation. You will see later why we picked @java[`byte[]`]@scala[`Array[Byte]`] and not @java[`int`]@scala[`Int`] directly. Of course, we will also have to update the `NetSerializer`, which we will not show again here, since it has become pretty big by now and is not terribly relevant to the point.

#### Reference.conf

@@snip[reference.conf](/docs/src/main/resources/reference.conf) { filterLabels=true type=hocon }

#### TAddress

Java
:   @@snip[TAddress.java](/docs/src/main/java/jexamples/virtualnetworking/pingpongselectors/TAddress.java) {  }

Scala
:   @@snip[class TAddress](/docs/src/main/scala/sexamples/virtualnetworking/pingpongselectors/Messages.scala) { #address }

#### IdChannelSelector

Java
:   @@snip[IdChannelSelector.java](/docs/src/main/java/jexamples/virtualnetworking/pingpongselectors/IdChannelSelector.java) {  }

Scala
:   @@snip[IdChannelSelector.scala](/docs/src/main/scala/sexamples/virtualnetworking/pingpongselectors/IdChannelSelector.scala) { }

### Custom Child Configuration

Additionally, we need to tell the `Pinger` components now what their id is, since we can not pull that out of the configuration file, as that is global to the whole Kompics runtime. We can, however, pass a modified version of the @javadoc:[Config](se.sics.kompics.config.Config) object to child components. So we are going to pull the `TAddress` from the `application.conf` in the `PingerParent` and use it as a base address that we pass to @javadoc:[NettyNetwork](se.sics.kompics.network.netty.NettyNetwork). And then for each `Pinger` we create, we are going to write a virtual address with the id into the child @javadoc:[Config](se.sics.kompics.config.Config). This way we do not have to change any code in the `Pinger` itself.

#### PingerParent

Java
:   @@snip[PingerParent.java](/docs/src/main/java/jexamples/virtualnetworking/pingpongselectors/PingerParent.java) {  }

Scala
:   @@snip[class PingerParent](/docs/src/main/scala/sexamples/virtualnetworking/pingpongselectors/Parent.scala) { #pinger-parent }

### Execution

That is it. Either run it again from within sbt, or assemble and copy the fat `.jar` to the `dist` folder, and then distribute that folder to where you want to run from and run the `Ponger` first, then the `Pinger`. You will see one `Pong` for every `Ping`, as expected.
