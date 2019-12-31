package se.sics.test;

import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.sics.kompics.ComponentDefinition;
import se.sics.kompics.Handler;
import se.sics.kompics.Positive;
import se.sics.kompics.Start;
import se.sics.kompics.network.Network;
import se.sics.kompics.timer.CancelPeriodicTimeout;
import se.sics.kompics.timer.SchedulePeriodicTimeout;
import se.sics.kompics.timer.Timeout;
import se.sics.kompics.timer.Timer;

public class Pinger extends ComponentDefinition {

    private static final Logger LOG = LoggerFactory.getLogger(Pinger.class);

    Positive<Network> net = requires(Network.class);
    Positive<Timer> timer = requires(Timer.class);

    private long counter = 0;
    private UUID timerId;
    private final TAddress self;
    private final TAddress ponger;

    public Pinger(Init init) {
        this.self = init.self;
        this.ponger = init.ponger;
    }

    Handler<Start> startHandler = new Handler<Start>() {
        @Override
        public void handle(Start event) {
            SchedulePeriodicTimeout spt = new SchedulePeriodicTimeout(0, 1000);
            PingTimeout timeout = new PingTimeout(spt);
            spt.setTimeoutEvent(timeout);
            trigger(spt, timer);
            timerId = timeout.getTimeoutId();
        }
    };
    Handler<Pong> pongHandler = new Handler<Pong>() {
        @Override
        public void handle(Pong event) {
            counter++;
            LOG.info("{} Got Pong #{}!", new Object[]{self, counter, event.header.src});
        }
    };
    Handler<PingTimeout> timeoutHandler = new Handler<PingTimeout>() {
        @Override
        public void handle(PingTimeout event) {
            LOG.info("{} Sending Ping #{}! to:{}", new Object[]{self, counter+1, ponger});
            trigger(new Ping(self, ponger), net);
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

    public static class PingTimeout extends Timeout {

        public PingTimeout(SchedulePeriodicTimeout spt) {
            super(spt);
        }
    }

    public static class Init extends se.sics.kompics.Init<Pinger> {

        public final TAddress self;
        public final TAddress ponger;

        public Init(TAddress self, TAddress ponger) {
            this.self = self;
            this.ponger = ponger;
        }
    }
}
