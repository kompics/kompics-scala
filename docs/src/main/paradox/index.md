
@@@ index
* [Introduction](introduction/index.md)
* [Tutorial](tutorial/index.md)
* [Reference](reference.md)
@@@

# Kompics

In this document we will describe how to use the **Kompics** component framework. Wherever applicable, we will present all examples both in Scala and in Java. For features that are exclusively available in Kompics Java, the reader is redirected to the [old documentation page](https://kompics.sics.se).

## Kompics Scala Quickstart

If you just want to get set up quickly with Kompics Scala, instead of reading throught the whole documentation, follow the following steps. These assume that are using [sbt](https://www.scala-sbt.org/) as your build tool. Otherwise, substitute as appropriate.

1. Start a new sbt project
2. Add the following dependency to your `build.sbt` file:

	@@dependency[sbt,Maven,Gradle] {
	  group="se.sics.kompics"
	  artifact="kompics-scala_2.13"
	  version=$project.version$
	}

3. Write a component:

	```scala
	package mypackage
	import se.sics.kompics.sl._
	class MyComponent(init: Init[MyComponent]) extends ComponentDefinition {
	  val Init(/* init paramters */) = init;
	  /* add ports */
	  /* add internal state */
	  ctrl uponEvent {
	    case _: Start => {
	       /* Do something when component is started */
	    }
	  }
	  /* add all your custom handlers */
	}
	```

4. Start the component and wait for it to complete, if it ever does:

	```scala
	package mypackage
	import se.sics.kompics.sl._
	object Main {
	  def main(args: Array[String]): Unit = {
	    Kompics.createAndStart(classOf[MyComponent], Init[MyComponent](/* init paramters */), NUM_THREADS);
	    Kompics.waitForTermination();
	  }
	}
	```

5. Run from within sbt with `runMain mypackage.Main`

