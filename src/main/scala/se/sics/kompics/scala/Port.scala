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
package se.sics.kompics.scala

import se.sics.kompics.KompicsEvent
import scala.reflect.runtime.universe._

/**
  * The <code>PortType</code> class.
  *
  * @author Lars Kroll {@literal <lkroll@kth.se>}
  * @version $Id: $
  */
abstract class Port extends se.sics.kompics.PortType {
    se.sics.kompics.PortType.preloadInstance(this);

    //    def request[E <: KompicsEvent](event: E) {
    //        super.request(event.getClass)
    //    }

    def request[E <: KompicsEvent: TypeTag] {
        val te = typeOf[E];
        super.request(asJavaClass(te))
    }
    
    def indication[E <: KompicsEvent: TypeTag] {
        val te = typeOf[E];
        super.indication(asJavaClass(te))
    }
    
    private def asJavaClass(te: Type): Class[_ <: KompicsEvent] = {
        val mirror = runtimeMirror(getClass.getClassLoader)
        return mirror.runtimeClass(te.typeSymbol.asClass).asInstanceOf[Class[_ <: KompicsEvent]]
        
    }
}