package se.sics.test.sim;

import se.sics.kompics.simulator.SimulationScenario;
import se.sics.kompics.simulator.run.LauncherComp;

public class SimplePingLauncher {
    public static void main(String[] args) {
        long seed = 123;
        SimulationScenario.setSeed(seed);
        SimulationScenario simpleBootScenario = ScenarioGen.simplePing();
        simpleBootScenario.simulate(LauncherComp.class);
    }
}
