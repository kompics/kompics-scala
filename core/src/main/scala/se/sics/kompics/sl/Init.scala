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

/**
  * A convenient case class for matchable init events
  *
  * To assign values of the init instance to fields in a component use, for example:
  * {{{
  * val (field1: String, field2: Int) = init;
  * }}}
  *
  * @tparam T the type of the component definition the init event is supposed to be used in
  * @param params all init arguments for the component
  *
  * @author Lars Kroll {@literal <lkroll@kth.se>}
  */
case class Init[T <: ComponentDefinition](params: Any*) extends se.sics.kompics.Init[T]

/**
  * Provides access to variants of empty init events.
  */
object Init {

  /**
    * Reference to [[se.sics.kompics.Init.NONE]].
    *
    * Use in `create` calls.
    */
  val NONE = se.sics.kompics.Init.NONE;

  /**
    * Cast reference to [[se.sics.kompics.Init.NONE]].
    *
    * Use in abstractions that expect an instance of [[se.sics.kompics.Init]].
    *
    * @tparam T the type of the component definition the init event is supposed to be used in
    */
  def none[T <: se.sics.kompics.ComponentDefinition]: se.sics.kompics.Init[T] =
    se.sics.kompics.Init.NONE.asInstanceOf[se.sics.kompics.Init[T]];
}
