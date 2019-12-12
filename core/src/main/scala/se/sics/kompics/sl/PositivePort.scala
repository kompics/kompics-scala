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
  Channel,
  ChannelCore,
  ChannelSelector,
  Component,
  ComponentCore,
  ConfigurationException,
  Negative,
  PortCore,
  PortType,
  Positive
}
import se.sics.kompics.{Handler => JHandler}

/**
  * Scala trait for a Positive port
  *
  * @author Lars Kroll {@literal <lkroll@kth.se>}
  */
trait PositivePort[P <: PortType] extends Positive[P] with AnyPort {

  /**
    * Create a bidirectional channel to the `component`.
    */
  def --(component: Component): Channel[P];

  /**
    * Create a bidirectional channel to each `Component` in `components`.
    */
  def --(components: Component*): Seq[Channel[P]];

  /**
    * Get the negative pair/dual.
    */
  def dualNegative: NegativePort[P];
}

/**
  * A wrapper for java ports to implement [[PositivePort]]
  *
  * @author Lars Kroll <lkroll@kth.se>
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

  override def doSubscribe(handler: se.sics.kompics.MatchedHandler[_, _, _]): Unit = {
    original.doSubscribe(handler);
  }

  override def removeChannel(channel: ChannelCore[P]): Unit = {
    original.removeChannel(channel);
  }

  override def uponEvent(handler: Handler): Handler = {
    throw new ConfigurationException("Can't use closure based handlers on non ScalaPort");
  }

  override def --(component: Component): Channel[P] = {
    val negativePort: Negative[_ <: P] = component.getNegative(original.getPortType().getClass());
    negativePort match {
      case neg: PortCore[P] => {
        val channel = Channel.TWO_WAY.connect(original, neg);
        return channel;
      }
      case _ => throw new ClassCastException(s"Can't convert ${negativePort.getClass} to PortCore!");
    }
  }

  override def --(components: Component*): Seq[Channel[P]] = {
    components.map(--);
  }

  override def dualNegative: NegativePort[P] = {
    NegativePort.port2negative(this.getPair())
  }
}

/**
  * Companion object providing utilities to create a [[PositivePort]] from a [[se.sics.kompics.PortCore]]
  *
  * @author Lars Kroll <lkroll@kth.se>
  */
object PositivePort {

  /**
    * Create a [[PositivePort]] from a [[se.sics.kompics.PortCore]] using a [[PositiveWrapper]]
    */
  implicit def port2positive[P <: PortType](x: PortCore[P]): PositivePort[P] = new PositiveWrapper[P](x);
}
