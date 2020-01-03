package sexamples.virtualnetworking.pingpongselectors

import java.nio.ByteBuffer
import se.sics.kompics.ChannelSelector

class IdChannelSelector(id: Array[Byte])
    extends ChannelSelector[TMessage, ByteBuffer](classOf[TMessage], ByteBuffer.wrap(id), true) {
  override def getValue(event: TMessage): ByteBuffer = {
    event.header.dst.id match {
      case Some(eventId) => ByteBuffer.wrap(eventId)
      case None          => null
    }
  }
}
