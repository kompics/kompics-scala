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

import org.scalatest._
import se.sics.kompics.KompicsEvent
import se.sics.kompics.Kompics
import se.sics.kompics.{ Fault, FaultHandler }
import se.sics.kompics.Fault.ResolveAction

class FaultTestSuite extends KompicsUnitSuite {
    import KompicsUnitSuite._
    import ScalaComponent._

    test("Faults should be escalated to the root") {

        val ew = new EventWaiter;

        Kompics.setFaultHandler(new FaultHandler {
            override def handle(fault: Fault): ResolveAction = {
                ew(fault)
                return ResolveAction.DESTROY
            }
        });

        ew {
            Kompics.createAndStart(classOf[ParentFaulter], se.sics.kompics.Init.NONE);
        }
        ew { event =>
            event shouldBe a[Fault]
            event match {
                case fault: Fault => fault.getCause shouldBe a[TestError]
            }
        }
    }

    test("Faults should escalated by default") {
        val ew = new EventWaiter;
        
        ew {
            Kompics.createAndStart(classOf[Parent], Init[Parent](ew));
        }
        ew { event =>
            event shouldBe a [se.sics.kompics.Start]           
        }
        ew { event =>
            event shouldBe a[Fault]
            event match {
                case fault: Fault => fault.getCause shouldBe a[TestError]
            }
        }
        Kompics.shutdown();
    }
}

class ParentFaulter extends ComponentDefinition {
    ctrl uponEvent {
        throw new TestError();
    }
}

class TestError extends RuntimeException {

}

class Parent(init: Init[Parent]) extends ComponentDefinition with EventTester {

    import KompicsUnitSuite._

    val checker = init match {
        case Init(checker: EventChecker) => checker
    }
    registerHandler(checker);

    val child = create(classOf[Child], Init[Child](checker))
    
    override def handleFault(fault: Fault): ResolveAction = {
        check(fault);
        return ResolveAction.DESTROY;
    }
}

class Child(init: Init[Child]) extends ComponentDefinition with EventTester {
    import KompicsUnitSuite._

    val checker = init match {
        case Init(checker: EventChecker) => checker
    }
    registerHandler(checker);

    val child = create(classOf[GrandChild], Init[GrandChild](checker))
    
    override def handleFault(fault: Fault) = ResolveAction.ESCALATE;
}

class GrandChild(init: Init[GrandChild]) extends ComponentDefinition with EventTester {
    import KompicsUnitSuite._

    val checker = init match {
        case Init(checker: EventChecker) => checker
    }
    registerHandler(checker);

    ctrl uponEvent {
        case msg: se.sics.kompics.Start => { () =>
            check(msg);
            throw new TestError();
        }
    }
}