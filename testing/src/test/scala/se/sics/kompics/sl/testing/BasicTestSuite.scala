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

import org.scalatest._

import se.sics.kompics.sl._

import se.sics.kompics.KompicsEvent
import se.sics.kompics.Kompics

class BasicTestSuite extends FunSuite with Matchers {

  test("Trivial Test Case should be created") {
    val ctx = TestContext.newInstance { ctx: TestContext[TestComponent] =>

    };
    ctx.check() should be (true);
  }

}

class TestComponent() extends ComponentDefinition {

}
