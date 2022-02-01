@@@ index
* [Channel Selectors](channelselectors.md)
* [Virtual Network Channel](virtualnetworkchannel.md)
@@@

Virtual Networks
================

Quite a few distributed systems require some notion of *virtual nodes* (or vnodes, for short), that is multiple instances of the same node functionality within one host machine. In a peer-to-peer system you might join different overlays, in a DHT you might need to artificially increase the number of nodes to meet the statistical requirements for reasonable load-balancing, and so on. The are many ways of realising such vnodes. The easiest might be to simply start one JVM for each node. This requires no change in your code whatsoever, just maybe a new startup script that keeps track of the PIDs of all the started nodes and how many to start. However, JVM instances are not particularly cheap. They eat up significant resources. This approach might scale to a few tens of nodes, but certainly not to hundreds or thousands of vnodes.

An alternative approach is to start all (or at least some) of the vnodes in the same JVM instance. With Kompics, this is also pretty easy: Instead of creating a new startup script, we create a new parent component that manages the different instances of the old parent component. Apart from that, we leave the whole component tree as it was before. Of course, we have to assign different ports to all the different @javadoc:[NettyNetwork](se.sics.kompics.network.netty.NettyNetwork) instances that are being started, so our config files might grow a bit bigger unless we find a programmatic way of assigning them, but nothing that can not be overcome. This approach might scale to a few hundreds of vnodes, but after that we are probably going to have problems finding free ports for Netty to bind on, since it actually uses twice as many ports as we ask it to (one for `TCP` and `UDP` and another one for `UDT`) and most systems limit a single JVM's (or any process') port usage quite drastically. Netty also creates some threads when it starts, so we might end up driving the JVM's (or operating system's) thread scheduler insane.

Finally, we can exploit the fact that most of our vnode's implementations would not actually use the full capacity of their respectivee @javadoc:[Network](se.sics.kompics.network.Network) and @javadoc:[Timer](se.sics.kompics.timer.Timer) implementations, and instead share them among all vnodes. In fact, we might try to share as much common functionality as we can manage to extract from the vnodes. For this to work, however, we need a way to send messages only along a specific channel, or we will end up with the same problem our very first version of networked *PingPong* had, where the `Pong`s reached all `Pinger`s and not just the one that sent the `Ping` that caused it. Part of the solution are the @ref:[Request-Response events](../../timers.md#request-response-events), that were introduced in the section about timers. But as already mentioned there, the request-response pattern is not sufficient. What if we simply want to send an event to one of the vnodes without having an outstanding request? What if a network message for that vnode comes in? The solution to this problem are @ref:[Channel Selectors](channelselectors.md).

### Contents

@@toc { depth=2 }
