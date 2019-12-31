package sexamples.basics.pingpongdirect

import se.sics.kompics.sl._
import se.sics.kompics.Direct

// #ping
case class Ping() extends Direct.Request[Pong.type];
// #ping
// #pong
object Pong extends Direct.Response;
// #pong

// #port
object PingPongPort extends Port {
  request[Ping];
  indication(Pong);
}
// #port
