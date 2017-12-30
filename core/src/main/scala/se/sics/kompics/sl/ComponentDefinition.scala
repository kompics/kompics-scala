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

import se.sics.kompics.{ PortType, Positive, Negative, PortCore, ControlPort, Fault, ConfigurationException }
import se.sics.kompics.{ ComponentCore, Channel, LoopbackPort, KompicsEvent, Component }
import se.sics.kompics.{ Handler => JHandler, ComponentDefinition => JCD, Init => JInit }
import se.sics.kompics.config.ConfigUpdate
import org.slf4j.{ Logger => JLogger, MDC };
import com.typesafe.scalalogging.Logger

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

  protected def ++[P <: PortType](port: P): NegativePort[_ <: P] = {
    this ++ port.getClass();
  }

  protected def ++[P <: PortType](portType: Class[P]): NegativePort[_ <: P] = {
    val oldport = provides(portType);
    oldport match {
      case sc: ScalaPort[P @unchecked] => return sc
      case _                           => throw new ClassCastException(s"Can't cast ${oldport.getClass} to ScalaPort!")
    }
  }

  protected def --[P <: PortType](port: P): PositivePort[_ <: P] = {
    this -- (port.getClass());
  }

  protected def --[P <: PortType](portType: Class[P]): PositivePort[_ <: P] = {
    val oldport = requires(portType);
    oldport match {
      case sc: ScalaPort[P @unchecked] => return sc;
      case _                           => throw new ClassCastException
    }
  }

  protected def requires[P <: PortType](port: P): PositivePort[P] = {
    val javaPortType = port.getClass;
    val oldport = requires(javaPortType);
    oldport match {
      case sc: ScalaPort[P @unchecked] => return sc
      case _                           => throw new ClassCastException(s"Can't cast ${oldport.getClass} to ScalaPort!")
    }
  }

  protected def requires[P <: PortType: TypeTag]: PositivePort[P] = {
    val javaPortType = asJavaClass[P](typeOf[P]);
    val oldport = requires(javaPortType);
    oldport match {
      case sc: ScalaPort[P @unchecked] => return sc
      case _                           => throw new ClassCastException(s"Can't cast ${oldport.getClass} to ScalaPort!")
    }
  }

  protected def provides[P <: PortType](port: P): NegativePort[P] = {
    val javaPortType = port.getClass;
    val oldport = provides(javaPortType);
    oldport match {
      case sc: ScalaPort[P @unchecked] => return sc
      case _                           => throw new ClassCastException(s"Can't cast ${oldport.getClass} to ScalaPort!")
    }
  }

  protected def provides[P <: PortType: TypeTag]: NegativePort[P] = {
    val javaPortType = asJavaClass[P](typeOf[P]);
    val oldport = provides(javaPortType);
    oldport match {
      case sc: ScalaPort[P @unchecked] => return sc
      case _                           => throw new ClassCastException(s"Can't cast ${oldport.getClass} to ScalaPort!")
    }
  }

  protected def create[C <: JCD: TypeTag]: Component = {
    val javaCType = asJavaClass[C](typeOf[C]);
    super.create(javaCType, Init.NONE)
  }

  protected def create[C <: JCD: TypeTag](init: JInit[C]): Component = {
    val javaCType = asJavaClass[C](typeOf[C]);
    super.create(javaCType, init)
  }

  protected def create[C <: JCD: TypeTag](init: JInit[C], update: ConfigUpdate): Component = {
    val javaCType = asJavaClass[C](typeOf[C]);
    super.create(javaCType, init, update)
  }

  protected def connect[P <: PortType](portType: P)(t: Tuple2[Component, Component]): Channel[P] = `!connect`[P](portType)(t)

  protected def connect[P <: PortType: TypeTag](t: Tuple2[Component, Component]): Channel[P] = `!connect`[P](t)

  protected def connect[P <: PortType](t: PortAndComponent[P]): Channel[P] = `!connect`(t)

  protected def connect[P <: PortType](t: ComponentAndPort[P]): Channel[P] = `!connect`(t)

  protected def connect[P <: PortType](t: PortAndPort[P]): Channel[P] = `!connect`(t)

  protected def trigger[P <: PortType](t: Tuple2[KompicsEvent, se.sics.kompics.Port[P]]) = `!trigger`(t)(this)

  protected def subscribe(t: Tuple2[Handler, AnyPort]): Unit = {
    t match {
      case (h, p) => p uponEvent h
    }
  }

  protected def unsubscribe(h: Handler, p: AnyPort): Unit = {
    p match {
      case sp: ScalaPort[_] => sp.doUnsubscribe(h);
      case _                => throw new ConfigurationException("Could not unsubscribe handler from non-ScalaPort!");
    }
  }

  protected def ctrl: NegativePort[ControlPort] = {
    control match {
      case sc: ScalaPort[ControlPort] => return sc;
      case pc: PortCore[ControlPort]  => return new NegativeWrapper[ControlPort](pc);
      case _                          => throw new ClassCastException;
    }
  }

  private var loggerMemo: Option[Logger] = None;
  protected[sl] def log: Logger = loggerMemo match {
    case Some(l) => l
    case None    => loggerMemo = Some(Logger(this.logger)); loggerMemo.get
  }
  override protected[sl] def setMDC(): Unit = super.setMDC();
  protected def logCtxPut(p: Tuple2[String, String]): Unit = super.loggingCtxPut(p._1, p._2);
  protected def logCtxPutAlways(p: Tuple2[String, String]): Unit = super.loggingCtxPutAlways(p._1, p._2);
  protected def logCtxRemove(key: String): Unit = super.loggingCtxRemove(key);
  protected def logCtxGet(key: String): Unit = super.loggingCtxGet(key);
  protected def logCtxReset(): Unit = super.loggingCtxReset();

  protected def loopbck: NegativePort[LoopbackPort] = {
    loopback match {
      case sc: ScalaPort[LoopbackPort] => return sc;
      case pc: PortCore[LoopbackPort]  => return new NegativeWrapper[LoopbackPort](pc);
      case _                           => throw new ClassCastException;
    }
  }

  private var configMemo: Option[Config] = None;
  protected def cfg: Config = configMemo match {
    case Some(c) => c
    case None    => configMemo = Some(Config.jconf2SConf(config)); configMemo.get
  }
}
