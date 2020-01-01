Virtual Networks
================
Quite a few distributed systems require some notion of |vnode|\s, that is multiple instances of the same node functionality within one host machine. In a |p2p| system you might join different overlays, in |dht| you might need to artificially increase the number of nodes to meet the statistical requirements for a reasonable load-balancing, and so on. The are many ways of realising such |vnode|\s. The easiest might be to simply start one |jvm| for each node. This requires no change in your code whatsoever, just maybe a new startup script that keeps track of the |pid|\s of all the started nodes and how many to start. However, |jvm| instances aren't cheap. They eat up significant resources. This approach might scale to a few tens of nodes, but certainly not to hundreds or thousands of |vnode|\s.

An alternative approach is to start all (or at least some) of the |vnode|\s in the same |jvm| instance. With Kompics this is also pretty easy: Instead of creating a new startup script, we create a new parent component that manages the different instances of the old parent component. Apart from that, we leave the whole component tree as it was before. Of course, we have to assign different ports to all the different ``NettyNetwork`` instances that are being started, so our config files might grow a bit bigger unless we find a programmatic way of assigning them, but nothing that can't be overcome. This approach might scale to a few hundreds of |vnode|\s, but after that we are probably going to have problems finding free ports for Netty to bind on, since it actually uses twice as many ports as we ask it to (one for ``TCP`` and ``UDP`` and another one for ``UDT``) and most systems limit a single |jvm|\'s (or any process') port usage quite drastically. Netty also creates some threads when it starts, so we might end up driving the |jvm|\'s thread scheduler insane.

Finally, we can exploit the fact that most of our |vnode|\'s implementations wouldn't actually use the full capacity of their ``Network`` and ``Timer`` implementations, and instead share them among all |vnode|\s. In fact we might try to share as much common functionality as we can manage to extract from the |vnode|\s. For this to work, however, we need a way to send messages only along a specific channel, or we will end up with the same problem our very first version of networked *PingPong* had where the ``Pong``\s reached all ``Pinger``\s and not just the one that sent the ``Ping`` that caused it. Part of the solution are the :ref:`reqrespevents` that were introduced in the section about timers. But as already mentioned there, the request-response pattern is not enough. What if we simply want to send an event to one of the |vnode|\s without having an outstanding request? What if a network message for that |vnode| comes in? The solution to this problem are *Channel Selectors*.

Channel Selectors
-----------------
When a ``Channel`` is connected it can optionally be given an instance of :java:ref:`se.sics.kompics.ChannelSelector` which is both a way to extract some value from an event (i.e. a specific field to use as a *selection key*) and also define a specific value for which this event should travel along this ``Channel``. The internal implementation of this is fairly efficient, but requires the selection value for a ``Channel`` to be immutable. Note that despite its previous name, a ``ChannelSelector`` can not be abused as a filter. It's better to think of it like a way to create a routing table (which is in fact pretty much what happens internally). If you want to compare it to the handler pattern matching described earlier (see :ref:`netcleanup`) you can think of a ``ChannelSelector`` to be both the ``PatternExtractor`` and the ``MatchedHandler``.

We will update our *PingPong* example such that we start a configurable number of ``Pinger``\s per host. We stick to a single ``Ponger`` for now, since otherwise we'd have to figure out how to load-balance across them. We shall add a ``byte[]`` id field to ``TAddress`` that we will use as a selection key. However, when creating those ids we shall be a bit lazy and simply use ascending integers in their ``byte[]`` representation. You will see later why we picked ``byte[]`` and not ``int`` directly. Of course, we'll also have to update the ``NetSerializer``, which we won't show again here, since it's pretty big by now and not quite relevant to the point.

.. literalinclude:: pingpong-virtual/src/main/resources/reference.conf
	:language: JSON
	:caption: reference.conf

.. literalinclude:: pingpong-virtual/src/main/java/se/sics/test/TAddress.java
	:language: java
	:caption: TAddress.java

.. literalinclude:: pingpong-virtual/src/main/java/se/sics/test/IdChannelSelector.java
	:language: java
	:caption: IdChannelSelector.java

Additionally, we need to tell the ``Pinger`` components now what their id is, since we can't pull that out of the configuration file, as that is global to the whole Kompics runtime. We can, however, pass a modified version of the ``Config`` object to child components. So we are going to pull the ``TAddress`` from the :file:`application.conf` in the ``PingerParent`` and use it as a base address that we pass to ``NettyNetwork``. And then for each ``Pinger`` we create, we are going to write a virtual address with the id into the child ``Config``. This way we don't have to change any code in the ``Pinger`` itself.

.. literalinclude:: pingpong-virtual/src/main/java/se/sics/test/PingerParent.java
	:language: java
	:caption: PingerParent.java

That's it. Compile and package (``mvn clean package``), copy the fat ``.jar`` to the :file:`dist` folder and then distribute that folder to where you want to run from and run the ``Ponger`` first, then the ``Pinger``. You'll see one ``Pong`` for every ``Ping`` as expected.

The full example code can be downloaded :download:`here <pingpong-virtual.zip>`.


Virtual Network Channel
-----------------------
There is another way we can realise the channel routing that we implemented with the channel selector in the previous section. We can implement a custom type of :java:ref:`se.sics.kompics.ChannelCore` implementation, that takes care of the routing internally. This is functionally relatively equivalent, but saves us quite a few object creations (just think of all those channels and selector instances if we have 1000 |vnode|\s or more). However, ``Channel`` code is not quite trivial as there can be concurrency issues, the discussion of which would burst the bounds of this tutorial. Luckily, there already is an implementation of exactly the type of ``byte[]`` id switching network channel, that we want. All we have to do is replace the ``kompics-port-network`` dependency in our :file:`pom.xml` with ``kompics-port-virtual-network``. Then change ``TAddress`` to implement :java:ref:`se.sics.kompics.network.virtual.Address` and ``THeader`` to implement :java:ref:`se.sics.kompics.network.virtual.Header` instead and modify our setup code in the ``PingerParent`` a bit to use :java:ref:`se.sics.kompics.network.virtual.VirtualNetworkChannel` instead of the ``IdChannelSelector`` which we can delete now.

.. literalinclude:: pingpong-virtualnet/src/main/java/se/sics/test/PingerParent.java
	:language: java
	:caption: new PingerParent.java

Again, the full example code can be downloaded :download:`here <pingpong-virtualnet.zip>`.
