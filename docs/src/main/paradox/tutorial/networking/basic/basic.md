.. _basicnetworking:

Basic Networking
================
As opposed to many other languages and frameworks commonly used for writing distributed applications (e.g., `Erlang <http://www.erlang.se>`_ and `Akka <http://akka.io/>`_) in Kompics networking is not handled transparently. Instead Kompics requires explicit addressing of *messages*, that is events that (could) go over the network. There are two reasons for this design: First of all, events in Kompics are normally not messages in that they are not addressed, but follow channels instead. So the logical extension of this pattern to a distributed setting would be to create cross-network channels manually. However, this would be very unintuitive for a programmer as it would be way too static a setup. And further, and this is the second reason, it would be misleading to assume that local events and network messages have the same semantics. Network messages have to deal with unavailable nodes, connection loss, partitions, and so on. Instead of trying to force a one-fits-all solution onto the programmer, as systems like Akka and the venerable `Java RMI <http://docs.oracle.com/javase/7/docs/technotes/guides/rmi/>`_ do, Kompics exposes these challenges and allows system designers to find application appropriate solutions.

The following sections will describe the Kompics ``Network`` port, and its default implementation ``NettyNetwork``. The latter also features a serialisation framework, which will be described as well in this part of the tutorial. As our example program we will stick to the *PingPong* code from the previous tutorials and extend it with networking capabilities.

The examples in this tutorial require two new dependencies in the :file:`pom.xml` file:

.. code-block:: xml

    <dependency>
        <groupId>se.sics.kompics.basic</groupId>
        <artifactId>kompics-port-network</artifactId>
        <version>${kompics.version}</version>
        <scope>compile</scope>
    </dependency>
    <dependency>
        <groupId>se.sics.kompics.basic</groupId>
        <artifactId>kompics-component-netty-network</artifactId>
        <version>${kompics.version}</version>
        <scope>compile</scope>
    </dependency>


Messages, Addresses, and Headers
--------------------------------
The :java:ref:`se.sics.kompics.network.Network` only allows three kinds of events: :java:ref:`se.sics.kompics.network.Msg` may pass in both directions (indication and request), while :java:ref:`se.sics.kompics.network.MessageNotify.Req` is a request and :java:ref:`se.sics.kompics.network.MessageNotify.Resp` is an indication. The latter two can be used to ask the ``Network`` service for feedback about whether or not a specific message was sent, how long it took, and how big the serialised version was. This is convenient for systems that need to keep statistics about their messages, or where resources should be freed as soon as a message crosses the wire.
All messages that go over the network have to implement the ``Msg`` interface, which merely stipulates that a message has to have some kind of header (:java:ref:`se.sics.kompics.network.Header`). The deprecated methods still need to be implemented for backwards compatibility, but should simply forward fields from the ``Header``.
The header itself requires three fields:
	
	#. A **source** address, which on the sender side is typically the *self* address of the node, and on the receiver side refers to the sender.
	#. A **destination** address, which tells the network on the sender side, where the message should go, and is typically the *self* address on the receiver side.
	#. The **transport** protocol to be used for the message. There is no requirement for all ``Network`` implementations to implement all possible protocols. ``NettyNetwork`` implements ``TCP``, ``UDP``, and ``UDT`` only.

The :java:ref:`se.sics.kompics.network.Address` interface has four required methods:
	
	#. IP and 
	#. port are pretty self explanatory.
	#. ``asSocket`` should get a combined represenation of IP and port. It is strongly recommended to keep the internal representation of the address as ``InetSocketAddress``, since this method will be called a lot more often than the first two, and creating a new object for it on the fly is rather expensive.
	#. ``sameHostAs`` is used for avoiding serialisation overhead when the message would end up in the same JVM anyway. This can be used for local addressing of components and is typically important for *virtual nodes* (see section :ref:`virtualnetwork`) where components on the same JVM will often communicate via the network port.

.. note::

	None of the interface presented above make any requirements as to the (im-)mutability of their fields. It is typically recommended to make all of them immutable. However, certain setups will require things like mutable headers, for example routing might be implemented in such a way.

Since almost all application will need custom fields in addition to the fields in the ``Msg``, ``Header``, or ``Address`` interfaces, almost everyone will want to write their own implementations. However, if that should not be the case, a simple default implementation can be found in :java:ref:`se.sics.kompics.network.netty.DirectMessage`, :java:ref:`se.sics.kompics.network.netty.DirectHeader`, and :java:ref:`se.sics.kompics.network.netty.NettyAddress`. For the purpose of this tutorial however, we will write our own, which we will prefix with **T** (for *Tutorial*) to avoid naming conflicts, since Java lacks support for import aliases.

.. literalinclude:: pingpong/src/main/java/se/sics/test/TAddress.java

.. literalinclude:: pingpong/src/main/java/se/sics/test/THeader.java

.. literalinclude:: pingpong/src/main/java/se/sics/test/TMessage.java


For our *PingPong* example we shall have both ``Ping`` and ``Pong`` extend ``TMessage`` instead of the direct request-response. We also hard-code ``TCP`` as protocol for both for now.

.. literalinclude:: pingpong/src/main/java/se/sics/test/Ping.java

.. literalinclude:: pingpong/src/main/java/se/sics/test/Pong.java

Now we need to change the ``Pinger`` and the ``Ponger`` to take a *self* address as init-parameter, require the ``Network`` port and feed the new constructor arguments to the ``Ping`` and ``Pong`` classes. Since we are using the network port now for the ping-pong, we can remove the requirement for the ``PingPongPort``. In this first example we'll make our lives somewhat simple and use the *self* address as source and destination for both classes. This corresponds to the local reflection example mentioned above.

.. literalinclude:: pingpong/src/main/java/se/sics/test/Pinger.java

.. literalinclude:: pingpong/src/main/java/se/sics/test/Ponger.java

Finally, we have to create the ``NettyNetwork`` in the ``Parent`` class, and connect it appropriately. As a preparation for later, we are going to take the port for *self* address as a commandline argument in the ``Main`` class, and hardcode ``127.0.0.1`` as IP address.

.. literalinclude:: pingpong/src/main/java/se/sics/test/Parent.java

.. literalinclude:: pingpong/src/main/java/se/sics/test/Main.java

At this point we can compile and run the code with a slight variation to the usual commands, since we need the port as an argument now (pick a different port if ``34567`` is in use on your system)::

	mvn clean compile
	mvn exec:java -Dexec.mainClass="se.sics.test.Main" -Dexec.args="34567"

The code until here can be found :download:`here <pingpong.zip>`.

We can see from the output that our setup technically works, but we are back to the problem of getting four ``Pong``\s on our two ``Ping``\s. The reason is, of course, that we are cheating. We are running all components in the same JVM, but we are not using proper :ref:`virtualnetwork` support, yet. We also do not actually want to use virtual nodes here, but instead each ``Pinger`` and ``Ponger`` should be on a different host (or at least in a different JVM).

Before we can fix this, though, we have to fix another problem that we don't even see yet: None of our messages are currently serialisable.

Serialisation
-------------
With ``NettyNetwork`` comes a serialisation framework that builds on Netty's :java:ref:`io.netty.buffer.ByteBuf`. The Kompics part is two-fold, and consists of the :java:ref:`se.sics.kompics.network.netty.serialization.Serializer` interface, and a ``Serializer`` registration, mapping, and lookup service in :java:ref:`se.sics.kompics.network.netty.serialization.Serializers`.

For every ``Serializer`` you write and register you will be required to pick a unique ``int`` identifier. You are required to keep track of the identifiers you use yourself. Kompics reserves 0-16 for internal use. By default only a single ``byte`` worth of identifier space is used, to minimise the overhead of the framework. If you are assigning larger identifiers than 255, make sure to use the ``resize`` method in ``Serializers`` before registering the respective ``Serializer``\s.
For larger projects it can become quite difficult to keep track of which identifiers are used and which are free. It is recommended to either assign them all in a single place (e.g., static fields of a class, or a configuration file), or assign subranges to submodules of the project and then do the same single-location-assignment per module.

In the ``toBinary`` and ``fromBinary`` methods in the ``Serializer`` interface you are free to do as you like, as long as at the end of the ``toBinary`` the whole object is in the ``ByteBuf`` and at the end of the ``fromBinary`` the same number of bytes that you put into the ``ByteBuf`` are removed from it again.

The ``Serializers`` class has two important functions. First it registers ``Serializer``\s into the system so they can be looked up when messages come in that have been serialised with them (as defined by their identifier). And second it maps types to specific ``Serializer`` instances that are already registered. The most convenient way to do that is by registering a short name to the ``Serializer`` instance, and then register the class mappings to that name. However, the identifier works as well.

The lookup of ``Serializer``\s for types is hierarchical, that is, when there is no exact mapping found for the type, the system will traverse its supertype (and interface) hierarchy to find a matching supertype ``Serializer`` and use that one instead. For example, if the object to be serialised implements Java's ``Serializable`` interface and there is no more specific ``Serializer`` registered, the object will be serialised with Java object serialisation (which is registered by default).

The other default ``Serializer``\s (and their identifiers) are:

	0. :java:ref:`se.sics.kompics.network.netty.serialization.SpecialSerializers.NullSerializer` for ``null``
	1. :java:ref:`se.sics.kompics.network.netty.serialization.SpecialSerializers.ByteSerializer` for ``byte[]``
	2. :java:ref:`se.sics.kompics.network.netty.serialization.SpecialSerializers.AddressSerializer` for ``NettyAddress``
	3. :java:ref:`se.sics.kompics.network.netty.serialization.JavaSerializer` for anything that implements ``Serializable``
	4. :java:ref:`se.sics.kompics.network.netty.serialization.ProtobufSerializer` for `Protocol Buffers <https://developers.google.com/protocol-buffers/?hl=en>`_ (not registered by default)
	5. :java:ref:`se.sics.kompics.network.netty.NettySerializer` for ``NettyNetwork`` internal messages
	6. :java:ref:`se.sics.kompics.network.netty.serialization.SpecialSerializers.UUIDSerializer` for ``UUID``
	7. :java:ref:`se.sics.kompics.network.netty.serialization.AvroSerializer` for `Avro <https://avro.apache.org/>`_ (not registered by default)
	8. to 16. reserved for future additions

.. note::

	For people familiar with common Java serialisation frameworks it might be obvious that `Kryo <https://github.com/EsotericSoftware/kryo>`_ is missing from the above list. This is on purpose. Kompics' previous messaging middleware used to rely on Kryo for serialisation, but Kryo's low level object layout hacks lead to a host of problems with deployments that included different types of JVMs (e.g., Oracle JRE and Open JRE, or different versions). For this reason we have purposefully moved away from Kryo serialisation. You are welcome to implement your own Kryo-based ``Serializer``, just know that in some setups you might encounter a lot of very difficult to pinpoint bugs.


In our *PingPong* example, the classes that need to be serialised are: ``TAddress``, ``THeader``, ``Ping``, and ``Pong``. Note that there is no necessesity to serialise ``TMessage`` separately from ``Ping`` and ``Pong`` at this point, as it has no content of its own. Of course, we could simply have all of those classes implement ``Serializable`` and be done with it. Since this is a tutorial (and Java's object serialisation is terribly inefficient) we are instead going to write our own custom serialisers. As preparation for future sections and to show off the system, we will split the serialisation logic into two classes: ``NetSerializer`` will deal with ``TAddress`` and ``THeader`` instances, while ``PingPongSerializer`` will deal with ``Ping`` and ``Pong`` messages. Since ``THeader`` is part of the messages, we will of course call the ``NetSerializer`` from the ``PingPongSerializer``, but we will act as if we don't know what kind of ``Header`` we are dealing with and let the serialisation framework figure that out. Similarly, ``TAddress``\es are part of a ``THeader`` so we will invoke the ``NetSerializer`` from itself. Note, that the difference between two approaches is, whether or not the identifier will be prepended. If we invoke a ``Serializer`` manually from within another ``Serializer`` it is assumed that it will be known at serialisation and deserialisation time what the object is and the identifier can be omitted. Otherwise the system will figure it out itself using the identifier.

In order to keep our identifier spaces separate we'll pick 100 for the ``NetSerializer`` and 200 for the ``PingPongSerializer``. And since we only have two instances to deal with, we'll just define the identifiers within the classes in the lazy way. 
We also added some extra constructors to the messages, to make it easier to use ``THeader`` instances from another ``Serializer`` during deserialisation.

.. literalinclude:: pingpong-distributed/src/main/java/se/sics/test/NetSerializer.java

.. literalinclude:: pingpong-distributed/src/main/java/se/sics/test/PingPongSerializer.java

.. note::

	The serialisation above code is not particularly space efficient, since that was not the purpose. To get an idea how to use bit-fields to get a significant space reduction on these kind of frequently used serialisation procedures, take a look at the source code of the ``SpecialSerializers`` class over on `Github <https://github.com/kompics/kompics/blob/master/basic/component-netty-network/src/main/java/se/sics/kompics/network/netty/serialization/SpecialSerializers.java>`_.

All that is left is now is to register the new ``Serializer``\s and map the right types to them. We add the following to the ``Main`` class:

.. code-block:: java

	static {
		// register
		Serializers.register(new NetSerializer(), "netS");
		Serializers.register(new PingPongSerializer(), "ppS");
		// map
		Serializers.register(TAddress.class, "netS");
		Serializers.register(THeader.class, "netS");
		Serializers.register(Ping.class, "ppS");
		Serializers.register(Pong.class, "ppS");
	}

.. _distributedpingpong:

Distributed PingPong
--------------------
Now we are prepared for a true distributed deployment of our *PingPong* example. There are a number of changes we need to make to the way we set up the component hierarchy. First of all, we want to deploy ``Pinger`` and ``Ponger`` separately, and they also need different parameters. The ``Ponger`` is purely reactive and it only needs to know its own *self* address. The ``Pinger`` on the other hand needs to know both its own address and a ``Ponger``'s. We are going to make use of that distinction in the ``Main`` class to decide which one to start. If we see two commandline arguments (1 IP and 1 port), we are going to start a ``Ponger``. If, however, we see four commandline arguments (2 IPs and 2 ports), we are going to start a ``Pinger``. Since our application classes now need different setup, we are also going to split the ``Parent`` into a ``PingerParent`` and a ``PongerParent``. Note that it is always a good idea to have a class without any business logic that sets up the ``Network`` and ``Timer`` connections, as you will see in the section on :ref:`simulation`.

.. literalinclude:: pingpong-distributed/src/main/java/se/sics/test/PingerParent.java

.. literalinclude:: pingpong-distributed/src/main/java/se/sics/test/PongerParent.java

.. literalinclude:: pingpong-distributed/src/main/java/se/sics/test/Main.java

Now we are finally ready to try this. Use the usual command to compile the code::

	mvn clean compile

Now the following commands should be run in different terminals, preferably side by side so you can clearly see how things are happening. For all the examples feel free to change the IPs and ports as you like, but be sure they match up correctly.

Start the ``Ponger`` first with::

	mvn exec:java -Dexec.mainClass="se.sics.test.Main" -Dexec.args="127.0.0.1 34567"

Then start a ``Pinger`` with::

	mvn exec:java -Dexec.mainClass="se.sics.test.Main" -Dexec.args="127.0.0.1 45678 127.0.0.1 34567"

You can start the same ``Pinger`` multiple times or start as many ``Pinger`` JVM in parallel as you like. Make sure they all have their own port, though!
You will see that there is no ``Ping`` amplification anymore as we saw in the last example. All messages are now correctly addressed and replied to. If you can't clearly see what's going on among all the logging output try to reduce the logging level to ``INFO`` in the :file:`log4j.properties`. Finally, use :kbd:`Control-c` to shutdown the ``Ponger`` as it will run forever otherwise.

As always the code until here can be downloaded :download:`here <pingpong-distributed.zip>`.

.. _netcleanup:

Cleanup: Config files, ClassMatchers, and Assembly
--------------------------------------------------
While our example from the previous section works, there are still a number of things that are not optimal with it. We'll use this section to make the whole code a bit nicer, and also change the way we deploy, since we can't really rely on Maven being present on all target machines.

Configuration Files
^^^^^^^^^^^^^^^^^^^
First of all, you might have noticed that we have a lot of redundancy in the passing around of parameters between the different component ``Init`` objects. Furthermore, our reading from the commandline is not the safest. Sure, there are good libraries for commandline options, but this is not really what we want. What we want is to define a bunch of values once and then be able to access them from anywhere within the code. The right solution for this problem is using a configuration file, where we write IPs and ports and such things, and a configuration library that knows how to give us access to the values in our code. Kompics has its own configuration library, which by default uses `Typesafe Config <https://github.com/typesafehub/config>`_ as a backend. 

If you prefer a different configuration library, you may of course wrap it in an implementation of :java:ref:`se.sics.kompics.config.BaselineConfig` and pass it into :java:ref:`se.sics.kompics.config.Config.Factory` and then replace the default config with your custom one in ``Kompics.setConfig`` before starting the runtime. 

For the tutorial we are going to stick to Typesafe Config as a baseline. We are thus going to add a :file:`src/main/resources/reference.conf` file, where we describe default values for all our config options. This is not strictly speaking necessary, but it is generally a good idea to have one place your users can look at where all possible config values are outlined. While we are at it, we also make the timeout period configurable.

.. literalinclude:: pingpong-cleaner/src/main/resources/reference.conf
	:language: JSON
	:caption: reference.conf

Now that we have a configuration file, we can simply throw away all the ``Init`` classes we created before, and pull out the desired values from the config in the ``Pinger`` and ``Ponger`` constructors.

.. code-block:: java
    :caption: Pinger.java

    public Pinger() {
        try {
            InetAddress selfIp = InetAddress.getByName(config().getValue("pingpong.self.host", String.class));
            int selfPort = config().getValue("pingpong.self.port", Integer.class);
            this.self = new TAddress(selfIp, selfPort);
            InetAddress pongerIp = InetAddress.getByName(config().getValue("pingpong.pinger.pongeraddr.host", String.class));
            int pongerPort = config().getValue("pingpong.pinger.pongeraddr.port", Integer.class);
            this.ponger = new TAddress(pongerIp, pongerPort);
        } catch (UnknownHostException ex) {
            throw new RuntimeException(ex);
        }
    }

    Handler<Start> startHandler = new Handler<Start>() {
        @Override
        public void handle(Start event) {
            long period = config().getValue("pingpong.pinger.timeout", Long.class);
            SchedulePeriodicTimeout spt = new SchedulePeriodicTimeout(0, period);
            PingTimeout timeout = new PingTimeout(spt);
            spt.setTimeoutEvent(timeout);
            trigger(spt, timer);
            timerId = timeout.getTimeoutId();
        }
    };

.. code-block:: java
    :caption: Ponger.java

    public Ponger() {
        try {
            InetAddress selfIp = InetAddress.getByName(config().getValue("pingpong.self.host", String.class));
            int selfPort = config().getValue("pingpong.self.port", Integer.class);
            this.self = new TAddress(selfIp, selfPort);
        } catch (UnknownHostException ex) {
            throw new RuntimeException(ex);
        }
    }

However, since the ``NettyNetwork`` needs to know the *self* address as well, we are going to have to duplicate some of this work in the ``PingerParent`` and ``PongerParent``. Alternatively we could continue to pass in the *self* address via the ``Pinger`` and ``Ponger`` ``Init`` classes and only construct it once in the respective parent class.

There is another solution, though, that gives a lot more concise code. Kompics' configuration system supports so called *conversions*, which are used to convert compatible types from the config values to the requested values. For example, it would be unnecessary to throw an exception when the user is asking for an instance of ``Long`` but the value is returned as ``Integer`` by Typesafe Config. Instead the config library will look through a number of :java:ref:`se.sics.kompics.config.Converter` instances that are registered at :java:ref:`se.sics.kompics.config.Conversions` (this is very similar to how the serialisation framework is used) and try to find one that can convert an ``Integer`` object to a ``Long`` object. Thus we can use this system to write ``Converter`` that takes the object at ``"pingpong.self"`` for example and converts it to a ``TAddress``. It turns out that the way we wrote the :file:`reference.conf` Typesafe Config will give us a ``Map`` with the subvalues as keys. In this case we can pull out the values, *convert* them to ``String`` and ``Integer`` instances respectively and then construct the ``TAddress`` as before. As an example, we are also going to support an alternative way to write a ``TAddress``, which is a single ``String`` in the following format: ``"127.0.0.1:34567"``.

.. literalinclude:: pingpong-cleaner/src/main/java/se/sics/test/TAddressConverter.java
	:language: java
	:caption: TAddressConverter.java

Additionally we also need to register the new ``Converter`` in the static initialisation block of the ``Main`` class and then we can get a ``TAddress`` by simply calling ``config().getValue("pingpong.self", TAddress.class);``, for example.

.. code-block:: java
    :caption: Main.java

    static {
        // register
        Serializers.register(new NetSerializer(), "netS");
        Serializers.register(new PingPongSerializer(), "ppS");
        // map
        Serializers.register(TAddress.class, "netS");
        Serializers.register(THeader.class, "netS");
        Serializers.register(Ping.class, "ppS");
        Serializers.register(Pong.class, "ppS");
        // conversions
        Conversions.register(new TAddressConverter());
    }

.. note::

	We are still doing the same work as before, technically even more since we have to look through all the ``Converter`` instances now. It simply looks a lot nicer like this, as there is less unnecessary code duplication. At this time none of the converted values are cached anywhere, thus it is recommended to always write values from the configuration, that are often used, into local fields, instead of pulling them out of the config on demand every time they are needed.


.. todo::
	
	Write another section *somewhere* describing the Config system in more detail including updates.



.. _message_matching_handlers:

Message Matching Handlers
^^^^^^^^^^^^^^^^^^^^^^^^^
Another thing that feels awkward with our code is how we write network messages. Our ``TMessage`` class does almost nothing except define what kind of header we expect, and all actual network messages like ``Ping`` and ``Pong`` have to implement all these annoying constructors that ``TMessage`` requires instead of focusing on their business logic (which is trivially simple to non-existent^^). We would much rather have the ``TMessage`` act as a kind of container for *data* and then ``Ping`` and ``Pong`` would simply be payloads. But then how would the handlers of ``Pinger`` and ``Ponger`` know which messages are for them, i.e. are ``Ping`` and ``Pong`` respectively, and which are for other classes. They would have to match on ``TMessage`` and handle all network messages. That would be way too expensive in a large system. Under no circumstance do we want to schedule components unnecessarily. The solution to our problem can be found in :java:ref:`se.sics.kompics.ClassMatchedHandler` which provides a very simple form of *pattern matching* for Kompics. Instead of matching on a single event type, it matches on two event types: The *context* type, which we will define as ``TMessage``, and the *content* type which will be ``Ping`` and ``Pong`` respectively.

We shall rewrite ``TMessage`` to carry any kind of ``KompicsEvent`` as a payload, and to act as :java:ref:`se.sics.kompics.PatternExtractor` for the payload. We'll also move its serialisation logic into the ``NetSerializer`` leaving the ``PingPongSerializer`` rather trivial as a result.

.. literalinclude:: pingpong-cleaner/src/main/java/se/sics/test/TMessage.java

.. literalinclude:: pingpong-cleaner/src/main/java/se/sics/test/Ping.java

.. literalinclude:: pingpong-cleaner/src/main/java/se/sics/test/Pong.java

.. literalinclude:: pingpong-cleaner/src/main/java/se/sics/test/NetSerializer.java

.. literalinclude:: pingpong-cleaner/src/main/java/se/sics/test/PingPongSerializer.java

.. literalinclude:: pingpong-cleaner/src/main/java/se/sics/test/Pinger.java

.. literalinclude:: pingpong-cleaner/src/main/java/se/sics/test/Ponger.java

And of course remember to register ``TMessage`` to the ``"netS"`` serialiser in the static initilisation block of ``Main``.

.. note::

	The ``ClassMatchedHandler`` is in fact only a specialisation of the more general :java:ref:`se.sics.kompics.MatchedHandler` which can use any kind of pattern to select values, and not just ``Class`` instances. The advantage of the ``ClassMatchedHandler`` is that the pattern to match against can be automatically extracted from the signature of the ``handle`` method using Java's reflection API. For more general ``MatchedHandler`` usages the pattern would have to be supplied manually by overriding the ``pattern`` method.

Assembly
^^^^^^^^
Lastly we finally need to move away from Maven to run our code. We need to able to deploy, configure, and run complete artifacts with all dependencies included. To achieve that goal we are going to need four things: 

	#. A Maven *assembly* plugin, 
	#. a :file:`dist` folder where we collect all deployment artifacts, 
	#. an :file:`application.conf`` in the :file:`dist` folder that is used to override configuration values from the :file:`reference.conf` we need to customise for a specific deployment, and
	#. two bash scripts :file:`pinger.sh` and :file:`ponger.sh` that hide away the ugly JVM configuration parameters from the users.

For the assembly plugin we have two options. Either we use the default `Maven Assembly Plugin <http://maven.apache.org/plugins/maven-assembly-plugin/>`_, which is a bit simpler, or we use `Maven Shade Plugin <https://maven.apache.org/plugins/maven-shade-plugin/>`_, which is a bit more powerful. For the tutorial we are going to use the Shade plugin, as it is usually the better long-term choice.

Our new :file:`pom.xml` then looks as follows: 

.. literalinclude:: pingpong-cleaner/pom.xml
	:language: xml

After we create the new :file:`dist` folder and move the new shaded fat jar from the :file:`target` folder into it, we create the two scripts and the :file:`application.conf` such that the content is as follows:

.. code-block:: console

	$ ls -ohn
	total 9984
	-rw-r--r--  1 501   165B Dec 26 18:43 application.conf
	-rw-r--r--  1 501   4.9M Dec 26 18:28 ping-pong-1.0-SNAPSHOT-fat.jar
	-rwxr-xr-x  1 501    93B Dec 26 18:35 pinger.sh
	-rwxr-xr-x  1 501    93B Dec 26 18:33 ponger.sh


Note the *executable* flag set on the bash scripts. Now write the following into the newly created files.

.. literalinclude:: pingpong-cleaner/dist/application.conf
	:caption: application.conf
	:language: yaml

.. literalinclude:: pingpong-cleaner/dist/ponger.sh
	:caption: ponger.sh
	:language: bash

.. literalinclude:: pingpong-cleaner/dist/pinger.sh
	:caption: pinger.sh
	:language: bash

And now simply pack up the :file:`dist` folder and distribute it to two machines that are connected via the network, unpack and fill in the necessary fields in :file:`application.conf` on both machines.

Finally, start first the ponger with:

.. code-block:: console

	./ponger.sh

And then the pinger:

.. code-block:: console

	./pinger.sh

As always the final code can be downloaded :download:`here <pingpong-cleaner.zip>`.

.. note::

	Of course the bash files only work on \*nix machines. If you need this to run on Windows, you'll either have to write ``.bat`` files or use one of the application packaging tools that generate ``.exe`` files from ``.jar`` files and you'll have to fix all the paths.

