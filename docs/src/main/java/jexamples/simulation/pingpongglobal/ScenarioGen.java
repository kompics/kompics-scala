package jexamples.simulation.pingpongglobal;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Map;
import se.sics.kompics.Init;
import se.sics.kompics.network.Address;
import se.sics.kompics.simulator.SimulationScenario;
import se.sics.kompics.simulator.adaptor.Operation;
import se.sics.kompics.simulator.adaptor.Operation1;
import se.sics.kompics.simulator.adaptor.Operation2;
import se.sics.kompics.simulator.adaptor.distributions.extra.BasicIntSequentialDistribution;
import se.sics.kompics.simulator.events.system.SetupEvent;
import se.sics.kompics.simulator.events.system.KillNodeEvent;
import se.sics.kompics.simulator.events.system.StartNodeEvent;
import se.sics.kompics.simulator.util.GlobalView;

public class ScenarioGen {

  public static final int PORT = 12345;
  public static final String IP_PREFIX = "192.193.0.";

  // #setup
  static Operation<SetupEvent> setupOp =
      new Operation<SetupEvent>() {
        @Override
        public SetupEvent generate() {
          return new SetupEvent() {
            @Override
            public void setupGlobalView(GlobalView gv) {
              gv.setValue("simulation.pongs", 0);
            }
          };
        }
      };
  // #setup

  static Operation<StartNodeEvent> startObserverOp =
      new Operation<StartNodeEvent>() {
        @Override
        public StartNodeEvent generate() {
          return new StartNodeEvent() {
            TAddress selfAdr;

            {
              try {
                selfAdr = new TAddress(InetAddress.getByName("0.0.0.0"), 0);
              } catch (UnknownHostException ex) {
                throw new RuntimeException(ex);
              }
            }

            @Override
            public Map<String, Object> initConfigUpdate() {
              HashMap<String, Object> config = new HashMap<>();
              config.put("pingpong.simulation.checktimeout", 2000);
              return config;
            }

            @Override
            public Address getNodeAddress() {
              return selfAdr;
            }

            @Override
            public Class<SimulationObserver> getComponentDefinition() {
              return SimulationObserver.class;
            }

            @Override
            public Init getComponentInit() {
              return new SimulationObserver.Init(100, 2);
            }
          };
        }
      };

  static Operation1<StartNodeEvent, Integer> startPongerOp =
      new Operation1<StartNodeEvent, Integer>() {

        @Override
        public StartNodeEvent generate(final Integer self) {
          return new StartNodeEvent() {
            TAddress selfAdr;

            {
              try {
                selfAdr = new TAddress(InetAddress.getByName(IP_PREFIX + self), PORT);
              } catch (UnknownHostException ex) {
                throw new RuntimeException(ex);
              }
            }

            @Override
            public Map<String, Object> initConfigUpdate() {
              HashMap<String, Object> config = new HashMap<>();
              config.put("pingpong.ponger.addr", selfAdr);
              return config;
            }

            @Override
            public Address getNodeAddress() {
              return selfAdr;
            }

            @Override
            public Class<PongerParent> getComponentDefinition() {
              return PongerParent.class;
            }

            @Override
            public Init getComponentInit() {
              return Init.NONE;
            }

            @Override
            public String toString() {
              return "StartPonger<" + selfAdr.toString() + ">";
            }
          };
        }
      };

  // #kill
  static Operation1<KillNodeEvent, Integer> killPongerOp =
      new Operation1<KillNodeEvent, Integer>() {
        @Override
        public KillNodeEvent generate(final Integer self) {
          return new KillNodeEvent() {
            TAddress selfAdr;

            {
              try {
                selfAdr = new TAddress(InetAddress.getByName(IP_PREFIX + self), PORT);
              } catch (UnknownHostException ex) {
                throw new RuntimeException(ex);
              }
            }

            @Override
            public Address getNodeAddress() {
              return selfAdr;
            }

            @Override
            public String toString() {
              return "KillPonger<" + selfAdr.toString() + ">";
            }
          };
        }
      };
  // #kill

  static Operation2<StartNodeEvent, Integer, Integer> startPingerOp =
      new Operation2<StartNodeEvent, Integer, Integer>() {

        @Override
        public StartNodeEvent generate(final Integer self, final Integer ponger) {
          return new StartNodeEvent() {
            TAddress selfAdr;
            TAddress pongerAdr;

            {
              try {
                selfAdr = new TAddress(InetAddress.getByName(IP_PREFIX + self), PORT);
                pongerAdr = new TAddress(InetAddress.getByName(IP_PREFIX + ponger), PORT);
              } catch (UnknownHostException ex) {
                throw new RuntimeException(ex);
              }
            }

            @Override
            public Map<String, Object> initConfigUpdate() {
              HashMap<String, Object> config = new HashMap<>();
              config.put("pingpong.pinger.addr", selfAdr);
              config.put("pingpong.pinger.pongeraddr", pongerAdr);
              return config;
            }

            @Override
            public Address getNodeAddress() {
              return selfAdr;
            }

            @Override
            public Class<PingerParent> getComponentDefinition() {
              return PingerParent.class;
            }

            @Override
            public Init getComponentInit() {
              return Init.NONE;
            }

            @Override
            public String toString() {
              return "StartPinger<" + selfAdr.toString() + ">";
            }
          };
        }
      };

  // #simple-ping
  public static SimulationScenario simplePing() {
    SimulationScenario scen =
        new SimulationScenario() {
          {
            SimulationScenario.StochasticProcess setup =
                new SimulationScenario.StochasticProcess() {
                  {
                    raise(1, setupOp);
                  }
                };

            SimulationScenario.StochasticProcess observer =
                new SimulationScenario.StochasticProcess() {
                  {
                    raise(1, startObserverOp);
                  }
                };

            SimulationScenario.StochasticProcess ponger =
                new SimulationScenario.StochasticProcess() {
                  {
                    eventInterarrivalTime(constant(1000));
                    raise(5, startPongerOp, new BasicIntSequentialDistribution(1));
                  }
                };

            SimulationScenario.StochasticProcess pinger =
                new SimulationScenario.StochasticProcess() {
                  {
                    eventInterarrivalTime(constant(1000));
                    raise(
                        5,
                        startPingerOp,
                        new BasicIntSequentialDistribution(6),
                        new BasicIntSequentialDistribution(1));
                  }
                };

            setup.start();
            observer.startAfterTerminationOf(0, setup);
            ponger.startAfterTerminationOf(1000, observer);
            pinger.startAfterTerminationOf(1000, ponger);
            terminateAfterTerminationOf(1000 * 10000, pinger);
          }
        };

    return scen;
  }
  // #simple-ping

  // #kill-pongers
  public static SimulationScenario killPongers() {
    SimulationScenario scen =
        new SimulationScenario() {
          {
            SimulationScenario.StochasticProcess setup =
                new SimulationScenario.StochasticProcess() {
                  {
                    raise(1, setupOp);
                  }
                };

            SimulationScenario.StochasticProcess observer =
                new SimulationScenario.StochasticProcess() {
                  {
                    raise(1, startObserverOp);
                  }
                };

            SimulationScenario.StochasticProcess ponger =
                new SimulationScenario.StochasticProcess() {
                  {
                    eventInterarrivalTime(constant(1000));
                    raise(5, startPongerOp, new BasicIntSequentialDistribution(1));
                  }
                };

            SimulationScenario.StochasticProcess pinger =
                new SimulationScenario.StochasticProcess() {
                  {
                    eventInterarrivalTime(constant(1000));
                    raise(
                        5,
                        startPingerOp,
                        new BasicIntSequentialDistribution(6),
                        new BasicIntSequentialDistribution(1));
                  }
                };

            SimulationScenario.StochasticProcess killPonger =
                new SimulationScenario.StochasticProcess() {
                  {
                    eventInterarrivalTime(constant(0));
                    raise(5, killPongerOp, new BasicIntSequentialDistribution(1));
                  }
                };

            setup.start();
            observer.startAfterTerminationOf(0, setup);
            ponger.startAfterTerminationOf(1000, observer);
            pinger.startAfterTerminationOf(1000, ponger);
            killPonger.startAfterTerminationOf(1000, pinger);
            terminateAfterTerminationOf(1000 * 10000, pinger);
          }
        };

    return scen;
  }
  // #kill-pongers
}
