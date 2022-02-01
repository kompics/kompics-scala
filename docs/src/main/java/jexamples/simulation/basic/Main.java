package jexamples.simulation.basic;

import se.sics.kompics.simulator.SimulationScenario;
import se.sics.kompics.simulator.run.LauncherComp;

public class Main {
  public static void main(String[] args) {
    long seed = 123;
    SimulationScenario.setSeed(seed);
    SimulationScenario simpleBootScenario = BasicSimulation.scenario();
    simpleBootScenario.simulate(LauncherComp.class);
  }
}
