package sexamples.basics.pingpongtimer

import se.sics.kompics.sl._
import se.sics.kompics.timer.Timer
import se.sics.kompics.timer.java.JavaTimer

class Parent extends ComponentDefinition {
  val pinger = create[Pinger];
  val pinger2 = create[Pinger];
  val ponger = create[Ponger];

  val timer = create[JavaTimer];

  connect(PingPongPort)(ponger -> pinger);
  connect(PingPongPort)(ponger -> pinger2);
  connect[Timer](timer -> pinger);
  connect[Timer](timer -> pinger2);

}
