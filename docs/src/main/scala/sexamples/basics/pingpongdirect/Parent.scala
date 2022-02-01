package sexamples.basics.pingpongdirect

import se.sics.kompics.sl._

class Parent extends ComponentDefinition {
  val pinger = create[Pinger];
  val pinger2 = create[Pinger];
  val ponger = create[Ponger];

  connect(PingPongPort)(ponger -> pinger);
  connect(PingPongPort)(ponger -> pinger2);
}
