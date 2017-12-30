/*
 * This file is part of the Kompics component model runtime.
 *
 * Copyright (C) 2009 Swedish Institute of Computer Science (SICS)
 * Copyright (C) 2009 Royal Institute of Technology (KTH)
 *
 * Kompics is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */
package se.sics.kompics.sl

import scala.language.implicitConversions
import scala.language.existentials
import se.sics.kompics.{
  KompicsEvent,
  Component,
  Channel,
  PortType,
  Positive,
  PortCore,
  ComponentCore,
  ConfigurationException,
  ChannelSelector,
  Negative,
  ChannelCore
}
import se.sics.kompics.{ Handler => JHandler }

/**
 * The <code>PositivePort</code> trait.
 *
 * @author Lars Kroll {@literal <lkroll@kth.se>}
 * @version $Id: $
 */
trait PositivePort[P <: PortType] extends Positive[P] with AnyPort {

  def --(component: Component): Channel[P];

  def --(components: Component*): Seq[Channel[P]];
}

/**
 * The <code>PositiveWrapper</code> class.
 *
 * @author Lars Kroll <lkr@lars-kroll.com>
 * @version $Id: $
 */
class PositiveWrapper[P <: PortType](original: PortCore[P]) extends PositivePort[P] {

  override def getPortType(): P = {
    return original.getPortType();
  }

  override def getOwner(): ComponentCore = {
    return original.getOwner();
  }

  override def getPair(): PortCore[P] = {
    return original.getPair();
  }

  override def setPair(port: PortCore[P]): Unit = {
    original.setPair(port);
  }

  override def doSubscribe[E <: KompicsEvent](handler: JHandler[E]): Unit = {
    original.doSubscribe(handler);
  }

  override def doTrigger(event: KompicsEvent, wid: Int, channel: ChannelCore[_]): Unit = {
    original.doTrigger(event, wid, channel);
  }

  override def doTrigger(event: KompicsEvent, wid: Int, component: ComponentCore): Unit = {
    original.doTrigger(event, wid, component);
  }

  override def addChannel(channel: ChannelCore[P]): Unit = {
    original.addChannel(channel);
  }

  override def addChannel(channel: ChannelCore[P], selector: ChannelSelector[_, _]): Unit = {
    original.addChannel(channel, selector);
  }

  override def enqueue(event: KompicsEvent): Unit = {
    original.enqueue(event);
  }

  override def doSubscribe(handler: se.sics.kompics.MatchedHandler[PT, V, E] forSome { type PT; type V; type E <: se.sics.kompics.PatternExtractor[PT, _ >: V] }) {
    original.doSubscribe(handler);
  }

  override def removeChannel(channel: ChannelCore[P]) {
    original.removeChannel(channel);
  }

  override def uponEvent(handler: Handler): Handler = {
    throw new ConfigurationException("Can't use closure based handlers on non ScalaPort");
  }

  def --(component: Component): Channel[P] = {
    val negativePort: Negative[_ <: P] = component.getNegative(original.getPortType().getClass());
    negativePort match {
      case neg: PortCore[P] => {
        val channel = Channel.TWO_WAY.connect(original, neg);
        return channel;
      }
      case _ => throw new ClassCastException(s"Can't convert ${negativePort.getClass} to PortCore!");
    }
  }

  def --(components: Component*): Seq[Channel[P]] = {
    components.map(--);
  }
}

/**
 * The <code>PositivePort</code> object.
 *
 * @author Lars Kroll <lkr@lars-kroll.com>
 * @version $Id: $
 */
object PositivePort {
  implicit def port2positive[P <: PortType](x: PortCore[P]): PositivePort[P] = new PositiveWrapper[P](x);
}
