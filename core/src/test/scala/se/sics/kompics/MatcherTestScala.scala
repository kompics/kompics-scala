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
package se.sics.kompics

import org.scalatest._

class MatcherTestScala extends sl.KompicsUnitSuite {
  import sl.KompicsUnitSuite._

  test("ClassMatchers should work in Scala") {

    val ew = new EventWaiter;

    val (cd, init) = setup({ cd =>
      val req = cd.child(classOf[DataParent]);
      val prov = cd.child(classOf[DataChild]);
      //req -- TestPort ++ prov;
      sl.`!connect`[DataPort](prov -> req);
    }, ew);
    ew {
      Kompics.createAndStart(cd, init);
    }
    ew { event =>
      event should equal(Start.event);
    }
    ew { event =>
      event shouldBe a[DataContainer];
      val dc = event.asInstanceOf[DataContainer];
      dc.data shouldBe a[CData];
    }
    ew.await();
    Kompics.shutdown();
  }
}

trait Data;

class CData extends Data {}

class FData extends Data {}

class DataContainer(val data: Data) extends PatternExtractor[Class[Object], Data] {

  override def extractPattern(): Class[Object] = data.getClass.asInstanceOf[Class[Object]];

  override def extractValue(): Data = data;

}

class DataPort extends PortType {

  indication(classOf[DataContainer]);

}

class DataParent extends ComponentDefinition with sl.EventTester {

  val dp = requires(classOf[DataPort]);

  val child = create(classOf[DataChild], Init.NONE);
  connect(dp.getPair(), child.getPositive(classOf[DataPort]), Channel.TWO_WAY);

  val dataHandler = new ClassMatchedHandler[CData, DataContainer]() {

    override def handle(content: CData, context: DataContainer) {
      if ((content != null) && (context != null)) {
        check(context);
      } else {
        throw new RuntimeException(s"Expected CData not ${content} and DataContainer not ${context}");
      }
    }
  };

  val falseDataHandler = new ClassMatchedHandler[FData, DataContainer]() {

    override def handle(content: FData, context: DataContainer) {
      throw new RuntimeException("Only CData handlers should be triggered, not FData!");
    }
  };

  subscribe(falseDataHandler, dp);
  subscribe(dataHandler, dp);
}

class DataChild extends ComponentDefinition with sl.EventTester {

  val dp = provides(classOf[DataPort]);

  val startHandler = new Handler[Start]() {

    override def handle(event: Start) {
      check(event);
      trigger(new DataContainer(new CData()), dp);
    }
  };
  subscribe(startHandler, control);
}
