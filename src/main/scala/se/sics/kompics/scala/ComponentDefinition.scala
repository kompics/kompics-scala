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

import se.sics.kompics.PortType
import se.sics.kompics.Positive
import se.sics.kompics.Negative
import se.sics.kompics.PortCore
import se.sics.kompics.ControlPort
import se.sics.kompics.Fault
import se.sics.kompics.ConfigurationException
import se.sics.kompics.Handler
import se.sics.kompics.ComponentCore
import se.sics.kompics.Component
import se.sics.kompics.LoopbackPort

/**
 * The <code>ComponentDefinition</code> class.
 * 
 * @author Lars Kroll {@literal <lkroll@kth.se>}
 * @version $Id: ComponentDefinition.scala 4036 2011-07-19 15:50:01Z lars $
 */
abstract class ComponentDefinition extends se.sics.kompics.ComponentDefinition(classOf[ScalaComponent]) {
	
//	private val localCore: ScalaComponent = getComponentCore match {
//		case sc: ScalaComponent => sc
//		case _ => throw new ConfigurationException("Invalid core type");
//	};
	
	def ++[P <: PortType](port: P): NegativePort[_ <: P] = {
		this ++ port.getClass();
	}
	
	def ++[P <: PortType](portType: Class[P]): NegativePort[_ <: P] = {
		val oldport = provides(portType);
		oldport match {
			case sc: ScalaPort[P] => return sc
			case _ => throw new ClassCastException
		}
	}
	
	def --[P <: PortType](port: P): PositivePort[_ <: P] = {
		this -- (port.getClass());
	}
	
	def --[P <: PortType](portType: Class[P]): PositivePort[_ <: P] = {
		val oldport = requires(portType);
		oldport match {
			case sc: ScalaPort[P] => return sc;
			case _ => throw new ClassCastException
		}
	}
	
	def ctrl: NegativePort[ControlPort] = {
		control match {
			case sc: ScalaPort[ControlPort] => return sc;
			case pc: PortCore[ControlPort] => return new NegativeWrapper[ControlPort](pc);
			case _ => throw new ClassCastException;
		}
	}
	
	def loopbck: NegativePort[LoopbackPort] = {
		loopback match {
			case sc: ScalaPort[LoopbackPort] => return sc;
			case pc: PortCore[LoopbackPort] => return new NegativeWrapper[LoopbackPort](pc);
			case _ => throw new ClassCastException;
		}
	}
}