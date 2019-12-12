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
    * The type returned by an `uponEvent` block.
    */
  type Handler = PartialFunction[KompicsEvent, Unit];

  /*
   * Forward a bunch of types for convenience.
   */
  //type Kompics = se.sics.kompics.Kompics;
  /**
    * The supertype for any event.
    *
    * @see [[se.sics.kompics.KompicsEvent]]
    */
  type KompicsEvent = se.sics.kompics.KompicsEvent;

  /**
    * @see [[se.sics.kompics.Start]]
    */
  type Start = se.sics.kompics.Start;

  /**
    * @see [[se.sics.kompics.Start.EVENT]]
    */
  val Start = se.sics.kompics.Start.EVENT;

  /**
    * @see [[se.sics.kompics.Started]]
    */
  type Started = se.sics.kompics.Started;

  /**
    * @see [[se.sics.kompics.Stop]]
    */
  type Stop = se.sics.kompics.Stop;

  /**
    * @see [[se.sics.kompics.Stop.EVENT]]
    */
  val Stop = se.sics.kompics.Stop.EVENT;

  /**
    * @see [[se.sics.kompics.Stopped]]
    */
  type Stopped = se.sics.kompics.Stopped;

  /**
    * @see [[se.sics.kompics.Kill]]
    */
  type Kill = se.sics.kompics.Kill;

  /**
    * @see [[se.sics.kompics.Kill.EVENT]]
    */
  val Kill = se.sics.kompics.Kill.EVENT;

  /**
    * @see [[se.sics.kompics.Killed]]
    */
  type Killed = se.sics.kompics.Killed;

  /**
    * Does nothing in Kompics Scala 2.x.
    *
    * Simply remove it wherever encountered.
    *
    * Only provided for source compatibility with legacy Kompics Scala 1.x code.
    */
  @deprecated("This is a no-op now and can safely be removed.", "Kompics Scala 2.0.0")
  def handle(closure: => Unit): Unit = closure;

  /**
    * Conveniently create a [[Handler]] instance from a partial function of the right type
    *
    * Does nothing really, and is only meant to enhance readability when handlers aren't subscribed immediately with [[AnyPort.uponEvent* uponEvent]].
    *
    * @param matcher the partial function that forms the handler's body
    *
    * @return really just the partial function again
    */
  def handler(matcher: Handler): Handler = {
    matcher
  }

  /**
    * A named tuple for two ports in opposite directions
    *
    * @tparam P the port type shared by both ports
    */
  case class PortAndPort[P <: PortType](pos: PositivePort[P], neg: NegativePort[P])

  /**
    * A named tuple for a port and a component
    *
    * @tparam P the port type shared by the port and the component
    */
  case class PortAndComponent[P <: PortType](pos: PositivePort[P], negC: Component)

  /**
    * A named tuple for a port and a component
    *
    * @tparam P the port type shared by the port and the component
    */
  case class ComponentAndPort[P <: PortType](posC: Component, neg: NegativePort[P])

  /**
    * Automatically create a [[PortAndPort]] from two port instances of opposite directions
    *
    * @tparam P the shared port type for both port instances
    *
    * @return the new [[PortAndPort]] instance
    */
  implicit def tuple2pnp[P <: PortType](t: Tuple2[PositivePort[P], NegativePort[P]]) = PortAndPort(t._1, t._2);

  /**
    * Automatically create a [[PortAndComponent]] from port instance and a component
    *
    * @tparam P the shared port type for both port and component
    *
    * @return the new [[PortAndComponent]] instance
    */
  implicit def tuple2pnc[P <: PortType](t: Tuple2[PositivePort[P], Component]) = PortAndComponent(t._1, t._2);

  /**
    * Automatically create a [[ComponentAndPort]] from port instance and a component
    *
    * @tparam P the shared port type for both port and component
    *
    * @return the new [[ComponentAndPort]] instance
    */
  implicit def tuple2cnp[P <: PortType](t: Tuple2[Component, NegativePort[P]]) = ComponentAndPort(t._1, t._2);

  implicit def option2optional[T](o: Option[T]): java.util.Optional[T] = o.asJava;

  /**
    * Connect a positive port `p` to a negative port `n`, both of type `P`.
    *
    * @tparam P the type of the port to connect on
    * @param t  two port instances of type `P` in opposite directions as a [[PortAndPort]] object
    *
    * @return the newly created channel
    *
    * @example {{{
    * `!connect`[P](p -> n)
    * }}}
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
    * @tparam P the type of the port to connect on
    * @param t a tuple of a port instance of type `P` and a component
    *
    * @return the newly created channel
    *
    * @example {{{
    * `!connect`[P](p -> c)
    * }}}
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
    * @tparam P the type of the port to connect on
    * @param t a tuple of a component and a port instance of type `P`
    *
    * @return the newly created channel
    *
    * @example {{{
    * `!connect`[P](c -> p)
    * }}}
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
    * @tparam P the type of the port to connect on
    * @param t a tuple of two components which povide `P` in opposite directions
    *
    * @return the newly created channel
    *
    * @example {{{
    * `!connect`[P](c1 -> c2)
    * }}}
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
    * @tparam P the type of the port to connect on
    * @param portType the instance object of the port type
    * @param t a tuple of two components which povide `P` in opposite directions
    *
    * @return the newly created channel
    *
    * @example {{{
    * `!connect`(P)(c1 -> c2)
    * }}}
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
    *
    * Must be used with a [[se.sics.kompics.ComponentDefinition ComponentDefinition]] implicitly in context.
    *
    * @tparam P the port type of the port to trigger on
    * @param t a tuple of a [[se.sics.kompics.KompicsEvent KompicsEvent]] and a port
    *
    * @example {{{
    * `!trigger`(myEvent -> p)
    * }}}
    */
  def `!trigger`[P <: PortType](
      t: Tuple2[KompicsEvent, se.sics.kompics.Port[P]]
  )(implicit cd: se.sics.kompics.ComponentDefinition): Unit = {
    t match {
      case (e, p) =>
        cd.proxy.trigger(e, p);
    }
  }

  /**
    * Give the java class for a type
    *
    * @tparam T the target type
    * @param te the type instance
    *
    * @return the java class for `T`
    */
  def asJavaClass[T](te: Type): Class[T] = {
    //val mirror = runtimeMirror(getClass.getClassLoader)
    val mirror = runtimeMirror(Thread.currentThread().getContextClassLoader())
    return mirror.runtimeClass(te.typeSymbol.asClass).asInstanceOf[Class[T]]
  }
}
