package sexamples.basics.pingpongstate

import se.sics.kompics.sl._

object Main {
  def main(args: Array[String]): Unit = {
    Kompics.createAndStart(classOf[Parent], 2);
    Kompics.waitForTermination();
  }
}
