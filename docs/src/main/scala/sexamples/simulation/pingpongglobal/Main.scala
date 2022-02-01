package sexamples.simulation.pingpongglobal

import se.sics.kompics.sl._
import se.sics.kompics.config.Conversions;
import se.sics.kompics.network.netty.serialization.Serializers
import se.sics.kompics.simulator.SimulationScenario
import se.sics.kompics.simulator.run.LauncherComp
import java.net.{InetAddress, UnknownHostException}

object Main {
  // #registration
  // register
  Serializers.register(NetSerializer, "netS");
  Serializers.register(PingPongSerializer, "ppS");
  // map
  Serializers.register(classOf[TAddress], "netS");
  Serializers.register(classOf[THeader], "netS");
  Serializers.register(classOf[TMessage], "netS");
  Serializers.register(Ping.getClass, "ppS");
  Serializers.register(Pong.getClass, "ppS");
  // conversions
  Conversions.register(TAddressConverter);
  // #registration

  def main(args: Array[String]): Unit = {
    if (args.length >= 1) {
      if (args(0).equalsIgnoreCase("ponger")) {
        Kompics.createAndStart(classOf[PongerHost], 2);
        println(s"Starting Ponger");
        Kompics.waitForTermination();
        // will never actually terminate...act like a server and keep running until externally exited
      } else if (args(0).equalsIgnoreCase("pinger")) {
        Kompics.createAndStart(classOf[PingerHost], 2);
        Thread.sleep(10000);
        Kompics.shutdown();
        System.exit(0);
        // #simulation
      } else if (args(0).equalsIgnoreCase("simulation")) {
        val seed = 123;
        SimulationScenario.setSeed(seed);
        if (args.length > 1) {
          val scenario = if (args(1).equalsIgnoreCase("simple")) {
            ScenarioGen.simplePingScenario
          } else if (args(1).equalsIgnoreCase("kill")) {
            ScenarioGen.killPongersScenario
          } else {
            throw new RuntimeException(s"Invalid argument: ${args(1)}");
          };
          System.out.println("Starting a Simulation");
          scenario.simulate(classOf[LauncherComp]);
        } else {
          System.err.println("Invalid number of parameters");
          System.exit(1);
        }
      } else {
        // #simulation
        Console.err.println(s"Invalid argument: ${args(0)}");
        System.exit(1);
      }
    } else {
      Console.err.println("Invalid number of parameters");
      System.exit(1);
    }
  }
}
