package sexamples.basics.pingpongstate

import se.sics.kompics.sl._

// #ping
object Ping extends KompicsEvent;
// #ping
// #pong
object Pong extends KompicsEvent;
// #pong

// #port
object PingPongPort extends Port {
  request(Ping);
  indication(Pong);
}
// #port
