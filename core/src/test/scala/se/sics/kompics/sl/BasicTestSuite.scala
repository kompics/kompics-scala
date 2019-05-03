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

import org.scalatest._

import se.sics.kompics.KompicsEvent
import se.sics.kompics.Kompics

class BasicTestSuite extends KompicsUnitSuite {

  import KompicsUnitSuite._
  //import ScalaComponent._

  test("Component should be instantiated") {
    val (cd, init) = setup({ cd =>

    }, { event =>

    });
    Kompics.createAndStart(cd, init);
    Kompics.shutdown();
  }

  test("Component should receive Start event") {

    val ew = new EventWaiter;

    val (cd, init) = setup({ cd =>
      cd.child(classOf[BasicTestComponent]);
    }, ew);
    // init
    ew {
      Kompics.createAndStart(cd, init);
    }
    ew { event =>
      event shouldBe a[se.sics.kompics.Start]
    }
    ew.await();
    Kompics.shutdown();
  }

  test("Component should get torn down") {

    val ew = new EventWaiter;

    val (cd, init) = setup({ cd =>
      cd.child(classOf[BasicTestComponent]);
    }, ew);
    // init
    ew {
      Kompics.createAndStart(cd, init);
    }
    ew { event =>
      event shouldBe a[se.sics.kompics.Start]
    }
    ew { event =>
      event shouldBe a[TearDown]
    }
    ew.await();
    Kompics.shutdown();
  }

  test("Components should send and receive events") {

    val ew = new EventWaiter;

    val (cd, init) = setup({ cd =>
      val req = cd.child(classOf[BasicTestRequirer]);
      val prov = cd.child(classOf[BasicTestProvider]);
      //req -- TestPort ++ prov;
      `!connect`(TestPort)(prov -> req);
    }, ew);
    ew {
      Kompics.createAndStart(cd, init);
    }
    ew { event =>
      event should equal(TestMessage("lala"))
    }
    ew { event =>
      event should equal(TestAck)
    }
    ew.await();
    Kompics.shutdown();
  }

  test("Components should handle events in FIFO order") {
    val n = 50;
    val ew = new EventWaiter;

    val (cd, init) = setup({ cd =>
      val sender = cd.child(classOf[FifoSender], Some(Init[FifoSender](n)));
      val receiver = cd.child(classOf[FifoReceiver]);
      //sender -- FifoPort ++ receiver;
      `!connect`(FifoPort)(receiver -> sender);
    }, ew);
    ew {
      Kompics.createAndStart(cd, init);
    }
    for (i <- 1 to n) {
      ew { event =>
        event should equal(FifoMessage(i))
      }
    }
    ew.await();
    Kompics.shutdown();
  }
}

case class TearDown() extends KompicsEvent

class BasicTestComponent extends ComponentDefinition with EventTester {

  ctrl uponEvent {
    case msg: se.sics.kompics.Start => handle {
      check(msg);
      trigger(TearDown(), onSelf);
    }
  }

  loopbck uponEvent {
    case TearDown() => handle { suicide() }
  }

  override def tearDown() {
    check(TearDown())
  }

}

case class TestMessage(test: String) extends KompicsEvent
case object TestAck extends KompicsEvent

object TestPort extends Port {
  request[TestMessage]
  indication(TestAck)
}

class BasicTestRequirer extends ComponentDefinition with EventTester {

  val test = requires(TestPort);

  ctrl uponEvent {
    case _: se.sics.kompics.Start => handle { trigger (TestMessage("lala") -> test); }
  }

  test uponEvent {
    case msg @ TestAck => handle {
      check(msg)
    }
  }
}

class BasicTestProvider extends ComponentDefinition with EventTester {

  val test = provides(TestPort);

  test uponEvent {
    case msg @ TestMessage(t) => handle {
      check(msg);
      trigger (TestAck -> test);
    }

  }

}
case class FifoMessage(i: Int) extends KompicsEvent

object FifoPort extends Port {
  request[FifoMessage];
}

class FifoSender(init: Init[FifoSender]) extends ComponentDefinition with EventTester {
  val fifo = requires(FifoPort);

  val n = init match {
    case Init(n: Int) => n;
  }

  ctrl uponEvent {
    case _: se.sics.kompics.Start => handle {
      for (i <- 1 to n) {
        println(s"Sending msg $i");
        trigger (FifoMessage(i) -> fifo);
      }
    }
  }
}

class FifoReceiver extends ComponentDefinition with EventTester {
  val fifo = provides(FifoPort);

  fifo uponEvent {
    case m @ FifoMessage(i) => handle {
      println(s"Received msg $i");
      check(m);
    }
  }
}
