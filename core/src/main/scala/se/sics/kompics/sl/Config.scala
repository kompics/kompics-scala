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
import scala.reflect.runtime.universe._
import scala.compat.java8.OptionConverters._

class Config(val original: se.sics.kompics.config.Config) {
  import Config._

  val _conv = PrimitiveConverters; // force the object to be loaded and do it's registration

  def readValue[T: TypeTag](key: String): Option[T] = {
    val valueType = typeOf[T];
    val valueClass = asJavaClass[T](valueType);
    original.readValue(key, valueClass).asScala;
  }

  @throws(classOf[ClassCastException])
  def getValue[T: TypeTag](key: String): T = {
    val valueType = typeOf[T];
    val valueClass = asJavaClass[T](valueType);
    original.getValue(key, valueClass);
  }
}

object Config {

  implicit def jconf2SConf(c: se.sics.kompics.config.Config): Config = new Config(c);
}
