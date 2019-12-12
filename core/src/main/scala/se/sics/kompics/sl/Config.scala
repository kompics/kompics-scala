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

/**
  * A convenience DSL wrapper around [[se.sics.kompics.config.Config]]
  */
class Config(val original: se.sics.kompics.config.Config) {
  import Config._

  private val _conv = PrimitiveConverters; // force the object to be loaded and do it's registration

  /**
    * Read the value at the key and convert it into the requested type
    *
    * @tparam T target type for the value
    * @param key the key for which the value is to be fetched
    *
    * @return `Some[T]` if there is a value and it can be cast to `T`, `None` otherwise
    */
  def readValue[T: TypeTag](key: String): Option[T] = {
    val valueType = typeOf[T];
    val valueClass = asJavaClass[T](valueType);
    original.readValue(key, valueClass).asScala;
  }

  /**
    * Read the value at the key and convert it into the requested type
    *
    * @tparam T target type for the value
    * @param key the key for which the value is to be fetched
    *
    * @return `T` if there is a value and it can be cast to `T`
    * @throws java.lang.ClassCastException if the conversion to `T` fails
    */
  @throws(classOf[ClassCastException])
  def getValue[T: TypeTag](key: String): T = {
    val valueType = typeOf[T];
    val valueClass = asJavaClass[T](valueType);
    original.getValue(key, valueClass);
  }
}

object Config {

  /**
    * Implicit conversion from [[se.sics.kompics.config.Config]] to [[Config]]
    */
  implicit def jconf2SConf(c: se.sics.kompics.config.Config): Config = new Config(c);
}
