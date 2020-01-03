package jexamples.simulation.basic;

import se.sics.kompics.simulator.adaptor.Operation;
import se.sics.kompics.simulator.adaptor.Operation1;
import se.sics.kompics.simulator.SimulationScenario;

public class BasicSimulation {

  // #gen1
  static Operation<SimpleEvent> simpleEventGen1 =
      new Operation<SimpleEvent>() {

        @Override
        public SimpleEvent generate() {
          return new SimpleEvent();
        }
      };
  // #gen1

  // #gen2
  static Operation1<SimpleEvent, Long> simpleEventGen2 =
      new Operation1<SimpleEvent, Long>() {

        @Override
        public SimpleEvent generate(Long param) {
          return new SimpleEvent(param);
        }
      };
  // #gen2

  public static SimulationScenario scenario() {
    SimulationScenario scen =
        new SimulationScenario() {
          {
            // #gen1
            StochasticProcess p1 =
                new StochasticProcess() {
                  {
                    eventInterarrivalTime(constant(2000));
                    raise(1000, simpleEventGen1);
                  }
                };
            // #gen1

            // #gen2
            StochasticProcess p2 =
                new StochasticProcess() {
                  {
                    eventInterarrivalTime(constant(2000));
                    raise(1000, simpleEventGen2, uniform(1000L, 2000L));
                  }
                };
            // #gen2

            // #scenario
            p1.start();
            p2.startAfterTerminationOf(1000, p1);
            terminateAfterTerminationOf(1000, p2);
            // #scenario
          }
        };
    return scen;
  }
}
