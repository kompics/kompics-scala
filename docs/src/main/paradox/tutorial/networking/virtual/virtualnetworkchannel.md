Virtual Network Channel
-----------------------

There is another way we can realise the channel routing that we implemented with the channel selector in the previous section. We could implement a custom type of @javadoc:[ChannelCore](se.sics.kompics.ChannelCore) implementation, that takes care of the routing internally. Functionally, this is relatively equivalent, but saves us quite a few object creations (just think of all those channels and selector instances if we have 1000 vnodes or more). However, @javadoc:[ChannelCore](se.sics.kompics.ChannelCore) code is not quite trivial, as there can be concurrency issues, the discussion of which would burst the bounds of this tutorial. Luckily, there already *is* an implementation of exactly the type of @java[`byte[]`]@scala[`Array[Byte]`] id switching network channel, that we want:  The @javadoc:[VirtualNetworkChannel](se.sics.kompics.network.virtual.VirtualNetworkChannel).

### Dependencies

In order to be able to use this special kind of channel, we need to replace our `kompics-port-network` dependency, with the following one:

@@dependency[sbt,Maven,Gradle] {
  group="se.sics.kompics.basic"
  artifact="kompics-port-virtual-network"
  version=$kompics.version$
}

### Using a VirtualNetworkChannel

Before we can actually use the new kind of channel, we must change `TAddress` to implement @javadoc:[virtual.Address](se.sics.kompics.network.virtual.Address) and `THeader` to implement @javadoc:[virtual.Header](se.sics.kompics.network.virtual.Header), instead. 

Additionally, we must modify our setup code in the `PingerParent`, such that it uses @javadoc:[VirtualNetworkChannel](se.sics.kompics.network.virtual.VirtualNetworkChannel) instead of the `IdChannelSelector`, which we can delete now.

#### PingerParent

Java
:   @@snip[PingerParent.java](/docs/src/main/java/jexamples/virtualnetworking/pingpongvirtual/PingerParent.java) {  }

Scala
:   @@snip[class PingerParent](/docs/src/main/scala/sexamples/virtualnetworking/pingpongvirtual/Parent.scala) { #pinger-parent }

### Execution

That is all. Run and execute the example the same way as before.
