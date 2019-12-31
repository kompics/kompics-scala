package sexamples.helloworld;

import se.sics.kompics.sl._

object Main {
  def main(args: Array[String]): Unit = {
    Kompics.createAndStart(classOf[HelloComponent]);
    Kompics.waitForTermination();
  }
}
