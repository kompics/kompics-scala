package sexamples.basics.pingpongdirect

import se.sics.kompics.sl._

object Main {
  def main(args: Array[String]): Unit = {
    Kompics.createAndStart(classOf[Parent], 3);
    Kompics.waitForTermination();
  }
}
