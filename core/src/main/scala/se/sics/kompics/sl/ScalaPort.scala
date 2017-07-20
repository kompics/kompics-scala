/**
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

import scala.collection.JavaConverters._
import scala.language.existentials
import java.util.concurrent.locks.ReentrantReadWriteLock
import se.sics.kompics.ComponentCore
import se.sics.kompics.PortType
import se.sics.kompics.PortCore
import se.sics.kompics.KompicsEvent
import se.sics.kompics.SpinlockQueue
import se.sics.kompics.ChannelCore
import se.sics.kompics.ChannelSelectorSet
import se.sics.kompics.ConfigurationException
import se.sics.kompics.{ Handler => JHandler }
import se.sics.kompics.Request
import se.sics.kompics.Response
import se.sics.kompics.Direct
import se.sics.kompics.Fault
import se.sics.kompics.ChannelSelector
import se.sics.kompics.Component
import se.sics.kompics.Channel
import se.sics.kompics.Positive
import se.sics.kompics.Negative
import se.sics.kompics.RequestPathElement
import se.sics.kompics.Kompics
//import se.sics.kompics.Port
//import scala.collection.mutable.HashMap
//import scala.collection.mutable.ListBuffer
//import scala.collection.mutable.HashSet
//import scala.collection.mutable.SetBuilder
import scala.reflect.Manifest
import java.lang.reflect.Method

/**
 * The <code>ScalaPort</code> class.
 *
 * @author Lars Kroll {@literal <lkroll@kth.se>}
 * @version $Id: $
 */
class ScalaPort[P <: PortType](positive: Boolean, pType: P, parent: ComponentCore, private val rwLock: ReentrantReadWriteLock)
    extends PortCore[P] with NegativePort[P] with PositivePort[P] {

  private var pair: ScalaPort[P] = null;
  private var subs = Array.empty[Handler];
  //private val preparedHandlers = scala.collection.mutable.HashMap.empty[KompicsEvent, Seq[MatchedHandler]];
  private val preparedHandlers = new java.util.concurrent.ConcurrentLinkedQueue[Tuple2[KompicsEvent, Seq[MatchedHandler]]]();
  private val normalChannels = scala.collection.mutable.ListBuffer.empty[ChannelCore[P]];
  private val selectorChannels = new ChannelSelectorSet();
  //private val eventQueue: SpinlockQueue[KompicsEvent] = new SpinlockQueue[KompicsEvent]();

  private def setup() {
    isPositive = positive;
    portType = pType;
    owner = parent;
  };
  setup();

  override def getPair(): PortCore[P] = {
    return pair;
  }

  override def setPair(port: PortCore[P]): Unit = {
    port match {
      case sp: ScalaPort[P] => pair = sp;
      case _                => throw new ConfigurationException("Can only pair up this port with another ScalaPort instance");
    }
  }

  override def doSubscribe[E <: KompicsEvent](handler: JHandler[E]) {
    var eventType = handler.getEventType();
    if (eventType == null) {
      eventType = reflectHandlerEventType(handler);
      handler.setEventType(eventType);
    }
    val closureHandler: Handler = { e =>
      if (e.getClass().isAssignableFrom(eventType)) () => { handler.asInstanceOf[JHandler[KompicsEvent]].handle(e) } else {
        throw new MatchError(e.getClass() + " didn't match " + eventType);
      }
    };
    doSubscribe(closureHandler);
  }

  override def doSubscribe(handler: se.sics.kompics.MatchedHandler[PT, V, E] forSome { type PT; type V; type E <: KompicsEvent with se.sics.kompics.PatternExtractor[PT, _ >: V] }) {
    throw new ConfigurationException("Can not use se.sics.kompics.MatchedHandler in ScalaPort! Use Scala's pattern matching instead.");
  }

  private def reflectHandlerEventType[E <: KompicsEvent](handler: JHandler[E]): Class[E] = {
    try {
      val declared = handler.getClass().getDeclaredMethods();
      val relevant = scala.collection.mutable.TreeSet.empty[Class[_ <: KompicsEvent]](new Ordering[Class[_ <: KompicsEvent]] {
        override def compare(e1: Class[_ <: KompicsEvent], e2: Class[_ <: KompicsEvent]): Int = {
          if (e1.isAssignableFrom(e2)) {
            return 1;
          } else if (e2.isAssignableFrom(e1)) {
            return -1;
          }
          return 0;
        }
      });
      for (m <- declared) {
        if (m.getName().equals("handle")) {
          relevant += m.getParameterTypes()(0).asInstanceOf[Class[_ <: KompicsEvent]];
        }
      }
      val eventType = relevant.firstKey.asInstanceOf[Class[E]];
      return eventType;
    } catch {
      case e: Throwable =>
        throw new RuntimeException(s"Cannot reflect handler event type for "
          + "handler $handler. Please specify it "
          + "as an argument to the handler constructor.", e);
    }
    throw new RuntimeException(
      s"Cannot reflect handler event type for handler $handler. Please specify it "
        + "as an argument to the handler constructor.");
  }

  private def doManifestSubscribe[E <: KompicsEvent: Manifest](handler: JHandler[E]): Unit = {
    val closureHandler: Handler = { case e: E => handle { handler.handle(e) } };
    doSubscribe(closureHandler);
  }

  protected[kompics] def doSubscribe(handler: Handler): Unit = {
    rwLock.writeLock().lock();
    try {
      // Don't care about update performance...only iteration matters
      val newSubs = new Array[Handler](subs.length + 1);
      System.arraycopy(subs, 0, newSubs, 0, subs.length);
      newSubs(subs.length) = handler;
      subs = newSubs;
    } finally {
      rwLock.writeLock().unlock();
    }
  }

  protected[kompics] def doUnsubscribe(handler: Handler): Unit = {
    rwLock.writeLock().lock();
    try {
      // Don't care about update performance...only iteration matters
      if (subs.length > 1) {
        val newSubs = new Array[Handler](subs.length - 1);
        //System.arraycopy(subs, 0, newSubs, 0, subs.length);
        var found = false;
        var (i, j) = (0, 0);
        while (i < subs.length) {
          if (subs(i) eq handler) {
            found = true;
          } else {
            newSubs(j) = subs(i);
            j += 1;
          }
          i += 1;
        }
        if (found) {
          subs = newSubs;
        } else {
          throw new RuntimeException(s"Handler ${handler} is not subscribed to this port ${this}");
        }
      } else if (subs.length == 1) {
        if (subs(0) eq handler) {
          subs = Array.empty[Handler];
        } else {
          throw new RuntimeException(s"Handler ${handler} is not subscribed to this port ${this}");
        }
      } else {
        throw new RuntimeException("Handler ${handler} could not be unsubscribed as no handler is currently subscribed to this port ${this}");
      }
    } finally {
      rwLock.writeLock().unlock();
    }
  }

  def uponEvent(handler: Handler): Handler = { doSubscribe(handler); return handler; }

  private def getMatchingHandlers(event: KompicsEvent): Seq[MatchedHandler] = {
    // This has supposedly the fastest construction for unkown size collections: http://www.lihaoyi.com/post/BenchmarkingScalaCollections.html#construction-performance
    var matching = List.empty[MatchedHandler];
    val l = subs.length;
    var i = 0;
    while (i < l) {
      val handler = subs(i);
      try {
        matching = handler(event) :: matching;
      } catch {
        case e: MatchError => //ignore (not all handlers usually match) //println("MatchError: "+e.getMessage());
      }
      i += 1;
    }
    return matching;
  }

  //  protected[kompics] def pickFirstEvent(): KompicsEvent = {
  //    return eventQueue.poll();
  //  }

  //    protected[kompics] def pollPreparedHandlers(event: KompicsEvent): Seq[MatchedHandler] = {
  //        rwLock.writeLock().lock();
  //        try {
  //            if (preparedHandlers contains event) {
  //                val ph = preparedHandlers(event);
  //                preparedHandlers -= event;
  //                return ph;
  //            } else {
  //                return getMatchingHandlers(event);
  //            }
  //        } finally {
  //            rwLock.writeLock().unlock();
  //        }
  //    }

  protected[kompics] def pollPreparedHandlers(): (KompicsEvent, Seq[MatchedHandler]) = preparedHandlers.poll();

  override def doTrigger(event: KompicsEvent, wid: Int, channel: ChannelCore[_]): Unit = {
    event match {
      case r: Request => r.pushPathElement(channel);
      case _          => // ignore
    }
    pair.deliver(event, wid);
  }

  override def doTrigger(event: KompicsEvent, wid: Int, component: ComponentCore): Unit = {
    //		println(this.getClass()+": "+event+" triggert from "+component);
    event match {
      case r: Request => r.pushPathElement(component);
      case _          => // ignore
    }
    pair.deliver(event, wid);
  }

  private def deliver(event: KompicsEvent, wid: Int): Unit = {

    val eventType = event.getClass();
    var delivered = false;

    rwLock.readLock().lock();
    try {
      event match {
        case response: Response =>
          val pe = response.getTopPathElement();
          if (pe != null) {
            if (pe.isChannel()) {
              val caller = pe.getChannel();
              if (caller != null) {
                // caller can be null since it is a WeakReference
                delivered = deliverToCallerChannel(event, wid, caller);
              }
            } else {
              val component = pe.getComponent();
              if (component == owner) {
                delivered = deliverToSubscribers(event, wid);
              } else {
                throw new RuntimeException(
                  s"Response path invalid: expected to arrive to component ${component.getComponent()}"
                    + " but instead arrived at ${owner.getComponent()}");
              }
            }
          } else {
            // response event has arrived to request origin and was
            // triggered further. We treat it as a regular event
            delivered = deliverToSubscribers(event, wid);
            delivered |= deliverToChannels(event, wid);
          }
        case dresp: Direct.Response =>
          delivered = deliverToSubscribers(event, wid);
        case _ =>
          // event is not a response event
          delivered = deliverToSubscribers(event, wid);
          delivered |= deliverToChannels(event, wid);

      }
    } finally {
      rwLock.readLock().unlock();
    }

    if (!delivered) {
      if (portType.hasEvent(isPositive, eventType)) {
        // nothing
      } else {
        // error, event type doesn't flow on this port in this direction
        throw new RuntimeException(s"${eventType.getCanonicalName()} events cannot be triggered on ${portDirection(!isPositive)} ${portType.getClass().getCanonicalName()}");
      }
    }
  }

  private def deliverToChannels(event: KompicsEvent, wid: Int): Boolean = {
    //		print(event+" trying to deliver to channels...");
    var delivered = false;
    normalChannels.foreach { channel =>
      if (isPositive) {
        channel.forwardToNegative(event, wid);
      } else {
        channel.forwardToPositive(event, wid)
      }
      delivered = true;
    }
    val channels = selectorChannels.get(event).asScala;
    channels.foreach { channel =>
      if (isPositive) {
        channel.forwardToNegative(event, wid);
      } else {
        channel.forwardToPositive(event, wid)
      }
      delivered = true;
    }
    //		if (delivered) println("succeeded") else println("failed");
    return delivered;
  }

  private def deliverToCallerChannel(event: KompicsEvent, wid: Int, caller: ChannelCore[_]): Boolean = {
    if (isPositive) {
      caller.forwardToNegative(event, wid);
    } else {
      caller.forwardToPositive(event, wid);
    }
    return true;
  }

  private def deliverToSubscribers(event: KompicsEvent, wid: Int): Boolean = {
    if (!subs.isEmpty) {
      val handlers = getMatchingHandlers(event);
      if (!handlers.isEmpty) {
        preparedHandlers.offer((event -> handlers));
        owner.eventReceived(this, event, wid);
        return true;
      }
    }
    return false;
  }

  private def portDirection(): String = {
    portDirection(isPositive);
  }

  private def portDirection(pos: Boolean): String = {
    if (pos) {
      return "positive";
    } else {
      return "negative";
    }
  }

  override def addChannel(channel: ChannelCore[P]): Unit = {
    rwLock.writeLock().lock();
    try {
      normalChannels += channel;
    } finally {
      rwLock.writeLock().unlock();
    }
  }

  override def addChannel(channel: ChannelCore[P], selector: ChannelSelector[_, _]): Unit = {
    rwLock.writeLock().lock();
    try {
      selectorChannels.addChannelSelector(channel, selector);
    } finally {
      rwLock.writeLock().unlock();
    }
  }

  override def removeChannel(channel: ChannelCore[P]) {
    rwLock.writeLock().lock();
    try {
      selectorChannels.removeChannel(channel);
      normalChannels -= channel;
    } finally {
      rwLock.writeLock().unlock();
    }
  }

  override def cleanChannels() {
    rwLock.writeLock().lock();
    try {
      selectorChannels.clear();
      normalChannels.clear();
    } finally {
      rwLock.writeLock().unlock();
    }
  }

  override def cleanEvents() {
    preparedHandlers.clear();
  }

  override def findChannelsTo(port: PortCore[P]): java.util.List[Channel[P]] = {
    val channels = List.newBuilder[Channel[P]];
    normalChannels.foreach { channel =>
      if (this.isPositive) {
        if (channel.hasNegativePort(port)) {
          channels += channel;
        }
      } else {
        if (channel.hasPositivePort(port)) {
          channels += channel;
        }
      }
    }
    val sChannels = selectorChannels.iterator().asScala;
    sChannels.foreach {
      case channel: ChannelCore[P] =>
        if (this.isPositive) {
          if (channel.hasNegativePort(port)) {
            channels += channel;
          }
        } else {
          if (channel.hasPositivePort(port)) {
            channels += channel;
          }
        }
    }
    return channels.result().asJava;
  }

  override def enqueue(event: KompicsEvent): Unit = {
    //Kompics.logger.trace("Queuing up event {}", event);
    //eventQueue.offer(event);
  }

  def ++(component: Component): Channel[P] = {
    val positivePort = component.getPositive(this.getPortType().getClass());
    positivePort match {
      case pos: PortCore[P] => {
        val channel = Channel.TWO_WAY.connect(pos, this);
        return channel;
      }
      case _ => throw new ClassCastException(s"Can't convert ${positivePort.getClass} to PortCore!");
    }
  }

  def ++(components: Component*): Seq[Channel[P]] = {
    components.map(++);
  }

  def --(component: Component): Channel[P] = {
    val negativePort = component.getNegative(this.getPortType().getClass());
    negativePort match {
      case neg: PortCore[P] => {
        val channel = Channel.TWO_WAY.connect(this, neg);
        return channel;
      }
      case _ => throw new ClassCastException(s"Can't convert ${negativePort.getClass} to PortCore!");
    }
  }

  def --(components: Component*): Seq[Channel[P]] = {
    components.map(--);
  }
}

object ScalaPort {

  def apply[P <: PortType](positive: Boolean, pType: P, parent: ComponentCore): ScalaPort[P] = {
    return new ScalaPort(positive, pType, parent, new ReentrantReadWriteLock());
  }

  def fromPort[P <: PortType](other: ScalaPort[P]): ScalaPort[P] = {
    val port = new ScalaPort(other.isPositive, other.portType, other.owner, other.rwLock);
    return port;
  }
}
