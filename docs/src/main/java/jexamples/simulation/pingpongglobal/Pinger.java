package jexamples.simulation.pingpongglobal;

import se.sics.kompics.Kompics;
import se.sics.kompics.ClassMatchedHandler;
import se.sics.kompics.ComponentDefinition;
import se.sics.kompics.Positive;
import se.sics.kompics.Handler;
import se.sics.kompics.Start;
import se.sics.kompics.timer.*;
import se.sics.kompics.network.Network;
import se.sics.kompics.network.Transport;
import se.sics.kompics.simulator.util.GlobalView;

import java.util.UUID;
import java.util.Optional;

public class Pinger extends ComponentDefinition {

  Positive<Network> net = requires(Network.class);
  Positive<Timer> timer = requires(Timer.class);

  private long counter = 0;
  private UUID timerId;
  private final TAddress self;
  private final TAddress ponger;
  private final long timeoutPeriod;

  public Pinger() {
    this.self = config().getValue("pingpong.pinger.addr", TAddress.class);
    this.ponger = config().getValue("pingpong.pinger.pongeraddr", TAddress.class);
    this.timeoutPeriod = config().getValue("pingpong.pinger.timeout", Long.class);
  }

  Handler<Start> startHandler =
      new Handler<Start>() {
        public void handle(Start event) {
          SchedulePeriodicTimeout spt = new SchedulePeriodicTimeout(0, timeoutPeriod);
          PingTimeout timeout = new PingTimeout(spt);
          spt.setTimeoutEvent(timeout);
          trigger(spt, timer);
          timerId = timeout.getTimeoutId();
        }
      };
  // #ponged
  ClassMatchedHandler<Pong, TMessage> pongHandler =
      new ClassMatchedHandler<Pong, TMessage>() {

        @Override
        public void handle(Pong content, TMessage context) {
          counter++;
          logger.info("Got Pong #{}!", counter);
          ponged();
        }
      };
  // #ponged
  Handler<PingTimeout> timeoutHandler =
      new Handler<PingTimeout>() {
        public void handle(PingTimeout event) {
          trigger(new TMessage(self, ponger, Transport.TCP, Ping.EVENT), net);
        }
      };

  {
    subscribe(startHandler, control);
    subscribe(pongHandler, net);
    subscribe(timeoutHandler, timer);
  }

  @Override
  public void tearDown() {
    trigger(new CancelPeriodicTimeout(timerId), timer);
  }
  // #ponged
  private void ponged() {
    Optional<GlobalView> gvo = config().readValue("simulation.globalview", GlobalView.class);
    if (gvo.isPresent()) { // only in simulation
      GlobalView gv = gvo.get();
      gv.setValue("simulation.pongs", gv.getValue("simulation.pongs", Integer.class) + 1);
    }
  }
  // #ponged

  public static class PingTimeout extends Timeout {
    public PingTimeout(SchedulePeriodicTimeout spt) {
      super(spt);
    }
  }
}
