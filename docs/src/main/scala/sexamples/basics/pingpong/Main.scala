package sexamples.basics.pingpong

import se.sics.kompics.sl._

object Main {
  def main(args: Array[String]): Unit = {
    Kompics.createAndStart(classOf[Parent]);
    try {
      Thread.sleep(10000);
      Kompics.shutdown();
    } catch {
      case ex: InterruptedException => Console.err.println(ex)
    }
  }
}
