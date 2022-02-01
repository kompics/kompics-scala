Cleanup
-------
While our example from the previous section works, there are still a number of things that are not optimal with it. We will use this section to make the whole code a bit nicer, and also look into a way to deploy the final code, since we can not really rely on sbt being present on all target machines.

### Configuration Files

First of all, you might have noticed that we have a lot of redundancy in the passing around of parameters between the different component `Init` instances. Furthermore, our reading from the commandline is not the safest. Sure, there are good libraries for commandline options, but this is not really what we want. What we want is to define a bunch of values once and then be able to access them from anywhere within the code. The right solution for this problem is using a *configuration file*, where we write IPs and ports and such things, and a *configuration library* that knows how to give us access to the values in our code. Kompics comes with its own configuration library, which by default uses [Typesafe Config](https://github.com/lightbend/config) as a backend. 

If you prefer a different configuration library, you may add support for it, by wrapping it in an implementation of @javadoc:[BaselineConfig](se.sics.kompics.config.BaselineConfig), passing it into the @javadoc:[Config.Factory](se.sics.kompics.config.Config.Factory), and finally replacing the default config with your custom one using @javadoc:[Kompics.setConfig](se.sics.kompics.Kompics#setConfig) *before* starting the runtime. 

For the tutorial we are going to stick to *Typesafe Config* as a baseline. We are thus going to add a `src/main/resources/reference.conf` file, where we describe default values for all our config options. This is not strictly speaking necessary, but it is generally a good idea to have one place your users can look at where all possible config values are outlined. While we are at it, we also make the timeout period configurable.

@@snip[reference.conf](/docs/src/main/resources/reference.conf) { #no-num type=hocon }

Now that we have a configuration file, we can simply throw away all the `Init` @java[classes]@scala[instances] we created before, and pull out the desired values from the config in the `Pinger` and `Ponger` constructors. Something along the lines of:

@@@ div { .group-java }
```java
try {
  InetAddress selfIp = InetAddress.getByName(config().getValue("pingpong.pinger.addr.host", String.class));
  int selfPort = config().getValue("pingpong.pinger.addr.port", Integer.class);
  this.self = new TAddress(selfIp, selfPort);
  InetAddress pongerIp = InetAddress.getByName(config().getValue("pingpong.pinger.pongeraddr.host", String.class));
  int pongerPort = config().getValue("pingpong.pinger.pongeraddr.port", Integer.class);
  this.ponger = new TAddress(pongerIp, pongerPort);
  this.timeoutPeriod = config().getValue("pingpong.pinger.timeout", Long.class);
} catch (UnknownHostException ex) {
  throw new RuntimeException(ex);
}
```
@@@

@@@ div { .group-scala }
```scala
val selfIp = InetAddress.getByName(cfg.getValue[String]("pingpong.pinger.addr.host"));
val selfPort = cfg.getValue[Int]("pingpong.pinger.addr.port");
val self = TAddress(selfIp, selfPort);
val pongerIp = InetAddress.getByName(cfg.getValue[String]("pingpong.pinger.pongeraddr.host"));
val pongerPort = cfg.getValue[Int]("pingpong.pinger.pongeraddr.port");
val ponger = TAddress(selfIp, selfPort);
val timeoutPeriod = cfg.getValue[Long]("pingpong.pinger.timeout");
```
@@@

However, we require addresses, and in particular the *self* address in a number of places: `Pinger`, `Ponger`, and `NettyNetwork`. While certainly possible, it may not be the greatest idea to just copy&paste the construction code for it to every place where we need it.

There is another solution, though, that gives a lot more concise code. Kompics' configuration system supports so called *conversions*, which are used to convert compatible types from the config values to the requested values. For example, it would be unnecessary to throw an exception when the user is asking for an instance of `Long` but the value is returned as @java[`Integer`]@scala[`Int`] by *Typesafe Config*. Instead the config library will look through a number of @javadoc:[Converter](se.sics.kompics.config.Converter) instances that are registered at @javadoc:[Conversions](se.sics.kompics.config.Conversions) (this is very similar to how the serialisation framework is used) and try to find one that can convert an @java[`Integer`]@scala[`Int`] instance to a `Long` instance. Thus we can use this system to write @javadoc:[Converter](se.sics.kompics.config.Converter) that takes the configuration object at `"pingpong.self"` for example and converts it to a `TAddress`. It turns out that, because of the way we wrote the `reference.conf` entries, *Typesafe Config* will give us a `Map` with the subvalues as keys. In this case, we can pull out the values, *convert* them to `String` and @java[`Integer`]@scala[`Int`] instances respectively, and then construct the `TAddress` as before. Just to exemplify the method, we are *additionally* going to support an alternative way to write a `TAddress` in the config file: A single `String` in the format `"127.0.0.1:34567"`.

#### TAddressConverter

Java
:   @@snip[TAddressConverter.java](/docs/src/main/java/jexamples/networking/pingpongcleaned/TAddressConverter.java) {  }

Scala
:   @@snip[TAddressConverter.scala](/docs/src/main/scala/sexamples/networking/pingpongcleaned/TAddressConverter.scala) { }


Additionally we also need to register the new @javadoc:[Converter](se.sics.kompics.config.Converter) in the the `Main` @java[class]@scala[object] and then we can get an instance of `TAddress` from the config by simply calling @java[`config().getValue("pingpong.pinger.addr", TAddress.class);`]@scala[`cfg.getValue[TAddress]("pingpong.pinger.addr")`], for example.

Java
:   @@snip[Main.java](/docs/src/main/java/jexamples/networking/pingpongcleaned/Main.java) { #registration }

Scala
:   @@snip[Main.scala](/docs/src/main/scala/sexamples/networking/pingpongcleaned/Main.scala) { #registration }


@@@ note

We are still doing the same work as before to get our `TAddress` instances. Technically, even more than before, since we have to look through all the registered @javadoc:[Converter](se.sics.kompics.config.Converter) instances now. It simply looks a lot nicer like this, as there is less unnecessary code duplication. At this time, none of the converted values are cached anywhere. Thus, it is generally recommended to always write values from the configuration that are used often into local fields, instead of pulling them out of the config on demand every time they are needed.

It is possible to "cache" converted config entries, by reading them out of the config in the converted object, and then writing them back into it, overriting the original, unconverted entry that was in their place. Config updates are somewhat involved, however, so we will not be treating them here.

@@@

<!-- TODO
	Write another section *somewhere* describing the Config system in more detail including updates.
//-->


### Pattern Matching Messages

Another thing that feels awkward with our code is how we write network messages: Our `TMessage` class does almost nothing, except define what kind of header we expect, and all actual network messages like `Ping` and `Pong` have to implement all these annoying constructors that `TMessage` requires, instead of focusing on their business logic (which is trivially simple). We would much rather have the `TMessage` act as a kind of container for *data* and then `Ping` and `Pong` would simply be payloads. But then, how would the handlers of `Pinger` and `Ponger` know which `TMessage` instance are for them, i.e. contain `Ping` and `Pong` instances respectively, and which are for other classes? @java[They would have to match on `TMessage` and handle all network messages. That would be way too expensive in a large system. Under no circumstance do we want to schedule components unnecessarily. The solution to our problem can be found in @javadoc:[ClassMatchedHandler](se.sics.kompics.ClassMatchedHandler), which provides a very simple form of *pattern matching* for Kompics Java. Instead of matching on a single event type, it matches on two event types: The *context* type, which we will define as `TMessage`, and the *content* type, which will be `Ping` and `Pong` respectively.]@scala[We must use pattern matching to already select only the appropriate instance of `TMessage` in the handler.]

We shall rewrite `TMessage` to carry any kind of @javadoc:[KompicsEvent](se.sics.kompics.KompicsEvent) as a @java[payload, and to act as @javadoc:[PatternExtractor](se.sics.kompics.PatternExtractor) for the payload]@scala[payload]. We will also move its serialisation logic into the `NetSerializer` leaving the `PingPongSerializer` rather trivial as a result.

#### TMessage

Java
:   @@snip[TMessage.java](/docs/src/main/java/jexamples/networking/pingpongcleaned/TMessage.java) { }

Scala
:   @@snip[class TMessage](/docs/src/main/scala/sexamples/networking/pingpongcleaned/Messages.scala) { #message }

#### Ping

Java
:   @@snip[Ping.java](/docs/src/main/java/jexamples/networking/pingpongcleaned/Ping.java) { }

Scala
:   @@snip[object Ping](/docs/src/main/scala/sexamples/networking/pingpongcleaned/Messages.scala) { #ping }

#### Pong

Java
:   @@snip[Pong.java](/docs/src/main/java/jexamples/networking/pingpongcleaned/Pong.java) { }

Scala
:   @@snip[object Pong](/docs/src/main/scala/sexamples/networking/pingpongcleaned/Messages.scala) { #pong }

#### NetSerializer

Java
:   @@snip[NetSerializer.java](/docs/src/main/java/jexamples/networking/pingpongcleaned/NetSerializer.java) { }

Scala
:   @@snip[NetSerializer.scala](/docs/src/main/scala/sexamples/networking/pingpongcleaned/NetSerializer.scala) { }

#### PingPongSerializer

Java
:   @@snip[PingPongSerializer.java](/docs/src/main/java/jexamples/networking/pingpongcleaned/PingPongSerializer.java) { }

Scala
:   @@snip[PingPongSerializer.scala](/docs/src/main/scala/sexamples/networking/pingpongcleaned/PingPongSerializer.scala) { }

#### Pinger

Java
:   @@snip[Pinger.java](/docs/src/main/java/jexamples/networking/pingpongcleaned/Pinger.java) { }

Scala
:   @@snip[Pinger.scala](/docs/src/main/scala/sexamples/networking/pingpongcleaned/Pinger.scala) { }

#### Ponger

Java
:   @@snip[Ponger.java](/docs/src/main/java/jexamples/networking/pingpongcleaned/Ponger.java) { }

Scala
:   @@snip[Ponger.scala](/docs/src/main/scala/sexamples/networking/pingpongcleaned/Ponger.scala) { }

And, of course, we must remember to register `TMessage` to the `"netS"` serialiser in the `Main` @java[class]@scala[object].

@@@ note { .group-java }

The @javadoc:[ClassMatchedHandler](se.sics.kompics.ClassMatchedHandler) is in fact only a specialisation of the more general @javadoc:[MatchedHandler](se.sics.kompics.MatchedHandler), which can use any kind of pattern to select values, and not just @javadoc:[Class](java.lang.Class) instances. The advantage of the @javadoc:[ClassMatchedHandler](se.sics.kompics.ClassMatchedHandler) is that the pattern to match against can be automatically extracted from the signature of the @javadoc:[handle](se.sics.kompics.MatchedHandler#handle(V,E)) method using Java's reflection API. For more general @javadoc:[MatchedHandler](se.sics.kompics.MatchedHandler) usages, the pattern would have to be supplied manually by overriding the @javadoc:[pattern](se.sics.kompics.MatchedHandler#pattern()) method.

@@@

### Assembly

Finally, we need to move away from using sbt to run our code. We need to able to deploy, configure, and run complete artifacts with all dependencies included. To achieve that goal we are going to need four things: 


1. The @link:[sbt assembly plugin](https://github.com/sbt/sbt-assembly), 
2. a folder named `dist`, where we collect all deployment artifacts, 
3. an `application.conf` file in the `dist` folder, that is used to override configuration values from the `reference.conf` file, which we need to customise for a specific deployment, and
4. two bash scripts `pinger.sh` and `ponger.sh`, that hide away the ugly JVM configuration parameters from the users (or simply save us time typing them).


For the assembly plugin to select the correct main file, if we have more than one, add the following to the build settings:

@@@ div { .group-java }
```scala
mainClass in assembly := Some("jexamples.networking.pingpongcleaned.Main")
```
@@@

@@@ div { .group-scala }
```scala
mainClass in assembly := Some("sexamples.networking.pingpongcleaned.Main")
```
@@@

Then simply call `assembly` in sbt.

After we create the new `dist` folder and move the new fat jar from the `target` folder into it, we create the two scripts and the `application.conf` file such that the content looks similar to the following:

```console
$ ls -ohn
total 9984
-rw-r--r--  1 501   165B Dec 26 18:43 application.conf
-rw-r--r--  1 501   4.9M Dec 26 18:28 ping-pong-1.0-SNAPSHOT-fat.jar
-rwxr-xr-x  1 501    93B Dec 26 18:35 pinger.sh
-rwxr-xr-x  1 501    93B Dec 26 18:33 ponger.sh
```

Note the *executable* flag set on the bash scripts. Now write the following into the newly created files.

#### application.conf

```hocon
pingpong.ponger.addr = "" // insert ponger self address
pingpong.pinger.addr = "" // insert pinger self address
pingpong.pinger.pongeraddr = "" // insert ponger target address
```

#### pinger.sh

```bash
#!/bin/bash

java -Dconfig.file=./application.conf -jar ping-pong-1.0-SNAPSHOT-fat.jar pinger
```

#### ponger.sh

```bash
#!/bin/bash

java -Dconfig.file=./application.conf -jar ping-pong-1.0-SNAPSHOT-fat.jar ponger
```

And now you can simply pack up the `dist` folder and distribute it to two machines that are connected via the network, unpack, and fill in the necessary fields in `application.conf` on both machines.

Finally, start first the ponger with:

```bash
./ponger.sh
```

And then the pinger:

```bash
./pinger.sh
```

@@@ note

Of course, the bash files only work on *nix machines. If you need this to run on Windows, you'll either have to write `.bat` files, or use one of the application packaging tools that generate `.exe` files from `.jar` files, and you'll have to fix all the paths.

@@@
