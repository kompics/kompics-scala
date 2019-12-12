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

import scala.reflect.runtime.universe._

/**
  * The base class for a port type.
  *
  * To declare a new port type, extend this class in an object and then
  * define the signature for the port, for example:
  * {{{
  * object TestPort extends Port {
  *   request[TestEventClass];
  *   request(TestEventObject);
  *   indication[TestEventClass2];
  *   indication(TestEventObject2);
  * }
  * }}}
  *
  * @author Lars Kroll {@literal <lkroll@kth.se>}
  */
abstract class Port extends se.sics.kompics.PortType {
  se.sics.kompics.PortType.preloadInstance(this);

  /**
    * Declate the given event type as a request on this port.
    *
    * @tparam E the event type to be declared
    */
  def request[E <: KompicsEvent: TypeTag]: Unit = {
    val te = typeOf[E];
    super.request(asJavaClass(te))
  }

  /**
    * Declate the given event object as a request on this port.
    *
    * @param event the event object to be declared
    */
  def request(event: KompicsEvent): Unit = {
    val eventType = event.getClass;
    super.request(eventType);
  }

  /**
    * Declate the given event type as an indication on this port.
    *
    * @tparam E the event type to be declared
    */
  def indication[E <: KompicsEvent: TypeTag]: Unit = {
    val te = typeOf[E];
    super.indication(asJavaClass(te))
  }

  /**
    * Declate the given event object as an indication on this port.
    *
    * @param event the event object to be declared
    */
  def indication(event: KompicsEvent): Unit = {
    val eventType = event.getClass;
    super.indication(eventType);
  }

}
