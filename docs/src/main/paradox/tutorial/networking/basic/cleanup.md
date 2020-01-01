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
