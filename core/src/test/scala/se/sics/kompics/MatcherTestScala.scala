package se.sics.kompics

import org.scalatest._

import se.sics.kompics.KompicsEvent
import se.sics.kompics.Kompics

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
  connect(dp.getPair(), child.getPositive(classOf[DataPort]));

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
