package sexamples.basics.pingpongstate

import se.sics.kompics.sl._

// #create_only
class Parent extends ComponentDefinition {
  val pinger = create[Pinger];
  val ponger = create[Ponger];
// #create_only

  connect(PingPongPort)(ponger -> pinger);

// #create_only
}
// #create_only
