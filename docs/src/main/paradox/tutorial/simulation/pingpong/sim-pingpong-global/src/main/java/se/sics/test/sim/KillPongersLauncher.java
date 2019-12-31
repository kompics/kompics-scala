package se.sics.test.sim;

import se.sics.kompics.simulator.SimulationScenario;
import se.sics.kompics.simulator.run.LauncherComp;

public class KillPongersLauncher {

    public static void main(String[] args) {
        long seed = 123;
        SimulationScenario.setSeed(seed);
        SimulationScenario killPongersScenario = ScenarioGen.killPongers();
        killPongersScenario.simulate(LauncherComp.class);
    }
}
