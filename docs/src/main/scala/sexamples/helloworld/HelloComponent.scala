package sexamples.helloworld;

import se.sics.kompics.sl._

class HelloComponent extends ComponentDefinition {
  ctrl uponEvent {
    case _: Start => {
      System.out.println("Hello World!");
      Kompics.asyncShutdown();
    }
  }
}
