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
package se.sics.kompics

import scala.reflect.runtime.universe._
import scala.language.implicitConversions
import scala.compat.java8.OptionConverters._

/**
  * This package contains the Scala DSL for Kompics.
  *
  * It is recommended to import this as `import se.sics.kompics.sl._`, since it
  * contains a number of implicit conversions and convenience methods that are
  * good to have in scope.
  *
  * @author Lars Kroll {@literal <lkroll@kth.se>}
  */
package object sl {

  /**
    * The type used in the match body of a `uponEvent` block.
    */
  type MatchedHandler = () => Unit;

  /**
    * The type returned by an `uponEvent` block.
    */
  type Handler = KompicsEvent => MatchedHandler;

  /*
   * Forward a bunch of types for convenience.
   */
  //type Kompics = se.sics.kompics.Kompics;
  type KompicsEvent = se.sics.kompics.KompicsEvent;
  type Start = se.sics.kompics.Start;
  val Start = se.sics.kompics.Start.event;
  type Started = se.sics.kompics.Started;
  type Stop = se.sics.kompics.Stop;
  val Stop = se.sics.kompics.Stop.event;
  type Stopped = se.sics.kompics.Stopped;
  type Kill = se.sics.kompics.Kill;
  val Kill = se.sics.kompics.Kill.event;
  type Killed = se.sics.kompics.Killed;

  def handle(handler: => Unit): MatchedHandler = {
    handler _;
  }

  def handler(matcher: Handler): Handler = {
    matcher
  }

  case class PortAndPort[P <: PortType](pos: PositivePort[P], neg: NegativePort[P])
  case class PortAndComponent[P <: PortType](pos: PositivePort[P], negC: Component)
  case class ComponentAndPort[P <: PortType](posC: Component, neg: NegativePort[P])

  implicit def tuple2pnp[P <: PortType](t: Tuple2[PositivePort[P], NegativePort[P]]) = PortAndPort(t._1, t._2);
  implicit def tuple2pnc[P <: PortType](t: Tuple2[PositivePort[P], Component]) = PortAndComponent(t._1, t._2);
  implicit def tuple2cnp[P <: PortType](t: Tuple2[Component, NegativePort[P]]) = ComponentAndPort(t._1, t._2);

  implicit def option2optional[T](o: Option[T]): java.util.Optional[T] = o.asJava;

  /**
    * Connect a positive port `p` to a negative port `n`, both of type `P`.
    *
    * Use as ``!connect`[P](p -> n)`.
    */
  def `!connect`[P <: PortType](t: PortAndPort[P]): Channel[P] = {
    t match {
      case PortAndPort(pos: PortCore[P], neg: PortCore[P]) =>
        Channel.TWO_WAY.connect(pos, neg);
      case _ =>
        throw new ClassCastException(s"Can't convert (${t.pos.getClass}, ${t.neg.getClass}) to (PortCore, PortCore)!");
    }
  }

  /**
    * Connect a component `c` to a positive port `p` of type `P`.
    *
    * Use as ``!connect`[P](p -> c)`
    */
  def `!connect`[P <: PortType](t: PortAndComponent[P]): Channel[P] = {
    t match {
      case PortAndComponent(pos: PortCore[P], negC) =>
        val javaPortType = pos.getPortType.getClass;
        val neg = negC.required(javaPortType);
        Channel.TWO_WAY.connect(pos, neg.asInstanceOf[PortCore[P]]);
      case _ =>
        throw new ClassCastException(
          s"Can't convert (${t.pos.getClass}, ${t.negC.getClass}) to (PortCore, Component)!"
        );
    }
  }

  /**
    * Connect a component `c` to a negative port `p` of type `P`.
    *
    * Use as ``!connect`[P](c -> p)`
    */
  def `!connect`[P <: PortType](t: ComponentAndPort[P]): Channel[P] = {
    t match {
      case ComponentAndPort(posC, neg: PortCore[P]) =>
        val javaPortType = neg.getPortType.getClass;
        val pos = posC.provided(javaPortType);
        Channel.TWO_WAY.connect(pos.asInstanceOf[PortCore[P]], neg);
      case _ =>
        throw new ClassCastException(
          s"Can't convert (${t.posC.getClass}, ${t.neg.getClass}) to (Component, PortCore)!"
        );
    }
  }

  /**
    * Connect two components on a Java port type `P`.
    *
    * Use as ``!connect`[P](c1 -> c2)`.
    */
  def `!connect`[P <: PortType: TypeTag](t: Tuple2[Component, Component]): Channel[P] = {
    val portType = typeOf[P];
    val javaPortType = asJavaClass[P](portType);
    t match {
      case (posC, negC) =>
        val neg = negC.required(javaPortType);
        if (neg == null) {
          throw new ConfigurationException(s"No such port: Negative[${javaPortType}] at $negC");
        }
        val pos = posC.provided(javaPortType);
        if (neg == null) {
          throw new ConfigurationException(s"No such port: Positive[${javaPortType}] at $posC");
        }
        Channel.TWO_WAY.connect(pos.asInstanceOf[PortCore[P]], neg.asInstanceOf[PortCore[P]]);
    }
  }

  /**
    * Connect two components on a Scala port object `P`.
    *
    * Use as ``!connect`(P)(c1 -> c2)`.
    */
  def `!connect`[P <: PortType](portType: P)(t: Tuple2[Component, Component]): Channel[P] = {
    val javaPortType = portType.getClass;
    t match {
      case (posC, negC) =>
        val neg = negC.required(javaPortType);
        if (neg == null) {
          throw new ConfigurationException(s"No such port: Negative[${javaPortType}] at $negC");
        }
        val pos = posC.provided(javaPortType);
        if (neg == null) {
          throw new ConfigurationException(s"No such port: Positive[${javaPortType}] at $posC");
        }
        Channel.TWO_WAY.connect(pos.asInstanceOf[PortCore[P]], neg.asInstanceOf[PortCore[P]]);
    }
  }

  /**
    * Trigger an event on a port via a component proxy.
    */
  def `!trigger`[P <: PortType](
      t: Tuple2[KompicsEvent, se.sics.kompics.Port[P]]
  )(implicit cd: se.sics.kompics.ComponentDefinition): Unit = {
    t match {
      case (e, p) =>
        cd.proxy.trigger(e, p);
    }
  }

  def asJavaClass[T](te: Type): Class[T] = {
    //val mirror = runtimeMirror(getClass.getClassLoader)
    val mirror = runtimeMirror(Thread.currentThread().getContextClassLoader())
    return mirror.runtimeClass(te.typeSymbol.asClass).asInstanceOf[Class[T]]
  }
}
