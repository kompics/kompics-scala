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
package se.sics.kompics

import scala.reflect.runtime.universe._

package object sl {
    
    type MatchedHandler = () => Unit;
    type Handler = KompicsEvent => MatchedHandler;

    def handle(handler: => Unit): MatchedHandler = {
        handler _;
    }

    def handler(matcher: Handler): Handler = {
        matcher
    }

    def `!connect`[P <: PortType](t: Tuple2[PositivePort[P], NegativePort[P]]): Channel[P] = {
        t match {
            case (pos: PortCore[P], neg: PortCore[P]) =>
                Channel.TWO_WAY.connect(pos, neg);
            case _ => throw new ClassCastException(s"Can't convert (${t._1.getClass}, ${t._2.getClass}) to (PortCore, PortCore)!");
        }
    }

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

    def `!trigger`[P <: PortType](t: Tuple2[KompicsEvent, se.sics.kompics.Port[P]])(implicit cd: se.sics.kompics.ComponentDefinition): Unit = {
        t match {
            case (e, p) =>
                cd.proxy.trigger(e, p);
        }
    }

    def asJavaClass[T](te: Type): Class[T] = {
        val mirror = runtimeMirror(getClass.getClassLoader)
        return mirror.runtimeClass(te.typeSymbol.asClass).asInstanceOf[Class[T]]
    }
}