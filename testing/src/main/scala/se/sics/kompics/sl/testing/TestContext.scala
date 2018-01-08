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
package se.sics.kompics.sl.testing

import scala.reflect.runtime.universe._;
import se.sics.kompics.sl;
import se.sics.kompics.testing.{ TestContext => JTestContext };
import se.sics.kompics.{ ComponentDefinition => JCD, Init => JInit };

object TestContext {
  def newInstance[C <: JCD: TypeTag](setup: TestContext[C] => Unit): JTestContext[C] = {
    val jc = sl.asJavaClass[C](typeOf[C]);
    val jtc = JTestContext.newInstance(jc);
    val tctx = new TestContext(jtc);
    setup(tctx);
    jtc
  }
  def newInstance[C <: JCD: TypeTag](init: JInit.None)(setup: TestContext[C] => Unit): JTestContext[C] = newInstance[C](setup);
  def newInstance[C <: JCD: TypeTag](init: JInit[C])(setup: TestContext[C] => Unit): JTestContext[C] = {
    val jc = sl.asJavaClass[C](typeOf[C]);
    val jtc = JTestContext.newInstance(jc, init);
    val tctx = new TestContext(jtc);
    setup(tctx);
    jtc
  }
}

class TestContext[C <: JCD] private (val ctx: JTestContext[C]) {

}
