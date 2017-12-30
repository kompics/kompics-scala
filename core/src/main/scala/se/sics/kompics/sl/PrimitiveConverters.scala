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

import se.sics.kompics.config.{ Conversions, Converter }
import scala.reflect.runtime.universe._

abstract class SConv[T: TypeTag] extends Converter[T] {
  override def `type`: Class[T] = {
    val tType = typeOf[T];
    asJavaClass(tType);
  }
}

object PrimitiveConverters {

  Conversions.register(BooleanConv);
  // FLOATING POINT
  Conversions.register(FloatConv);
  Conversions.register(DoubleConv);
  // INTEGERS
  Conversions.register(CharConv);
  Conversions.register(ByteConv);
  Conversions.register(ShortConv);
  Conversions.register(IntConv);
  Conversions.register(LongConv);

  def register() {
    //println("Registering Scala Converters");
  }

  object BooleanConv extends SConv[Boolean] {
    override def convert(o: Any): Boolean = {
      o match {
        case n: Number            => ((n.longValue() == 0) || (n.doubleValue() == 0.0));
        case s: String            => booleanWords(s)
        case b: java.lang.Boolean => b.booleanValue
        case _                    => null.asInstanceOf[Boolean]
      }
    }

    private def booleanWords(s: String): Boolean = {
      s.toLowerCase() match {
        case "true" =>
          return true;
        case "yes" =>
          return true;
        case "t" =>
          return true;
        case "y" =>
          return true;
        case "1" =>
          return true;
        case "false" =>
          return false;
        case "no" =>
          return false;
        case "f" =>
          return false;
        case "n" =>
          return false;
        case "0" =>
          return false;
        case _ =>
          return null.asInstanceOf[Boolean];
      }
    }
  }
  // FLOATING POINT
  object FloatConv extends SConv[Float] {
    override def convert(o: Any): Float = {
      o match {
        case n: Number => n.floatValue
        case s: String => s.toFloat
        case _         => null.asInstanceOf[Float]
      }
    }
  }

  object DoubleConv extends SConv[Double] {
    override def convert(o: Any): Double = {
      o match {
        case n: Number => n.doubleValue
        case s: String => s.toDouble
        case _         => null.asInstanceOf[Double]
      }
    }
  }

  // INTEGERS

  object CharConv extends SConv[Char] {
    override def convert(o: Any): Char = {
      o match {
        case n: Number => n.shortValue.toChar
        case s: String => if (s.length == 1) {
          s.charAt(0)
        } else {
          s.toShort.toChar
        }
        case _ => null.asInstanceOf[Char]
      }
    }
  }

  object ByteConv extends SConv[Byte] {
    override def convert(o: Any): Byte = {
      o match {
        case n: Number => n.byteValue
        case s: String => s.toByte
        case _         => null.asInstanceOf[Byte]
      }
    }
  }

  object ShortConv extends SConv[Short] {
    override def convert(o: Any): Short = {
      o match {
        case n: Number => n.shortValue
        case s: String => s.toShort
        case _         => null.asInstanceOf[Short]
      }
    }
  }

  object IntConv extends SConv[Int] {
    override def convert(o: Any): Int = {
      o match {
        case n: Number => n.intValue
        case s: String => s.toInt
        case _         => null.asInstanceOf[Int]
      }
    }
  }

  object LongConv extends SConv[Long] {
    override def convert(o: Any): Long = {
      o match {
        case n: Number => n.longValue
        case s: String => s.toLong
        case _         => null.asInstanceOf[Long]
      }
    }
  }
}
