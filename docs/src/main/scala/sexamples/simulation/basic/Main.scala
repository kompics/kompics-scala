package sexamples.simulation.basic

import se.sics.kompics.simulator.SimulationScenario
import se.sics.kompics.simulator.run.LauncherComp

object Main {
  def main(args: Array[String]): Unit = {
    val seed = 123;
    SimulationScenario.setSeed(seed);
    val simpleBootScenario = BasicSimulation.scenario;
    simpleBootScenario.simulate(classOf[LauncherComp]);
  }
}
