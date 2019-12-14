package se.sics.kompics.sl.simulator

import scala.reflect.runtime.universe._
import com.github.ghik.silencer.silent
import scala.collection.JavaConverters._
//import scala.jdk.CollectionConverters._
import se.sics.kompics.ComponentDefinition
import se.sics.kompics.network.{Address, Header, Msg, Network, Transport}
import se.sics.kompics.simulator.events.system.{ChangeNetworkModelEvent, _}
import se.sics.kompics.simulator.util.GlobalView
import se.sics.kompics.simulator.network.identifier.IdentifierExtractor
import se.sics.kompics.simulator.network.identifier.impl.SocketIdExtractor
import se.sics.kompics.simulator.network.NetworkModel
import java.util.HashMap
//import collection.mutable;

/**
  * Utilities to start a node in a simulation
  *
  * All of them are simply conveniences to create an instance of [[se.sics.kompics.simulator.events.system.StartNodeEvent]].
  */
object StartNode {

  /**
    * Create a node with address and init event
    *
    * @tparam C the component definition for the node
    * @param addresser a function that provides an address for the node
    * @param init a function that provides an init event for the node
    *
    * @return a simulator event that starts a node
    *
    * @see [[se.sics.kompics.simulator.events.system.StartNodeEvent]]
    */
  def apply[C <: ComponentDefinition: TypeTag](addresser: => Address,
                                               init: => se.sics.kompics.Init[C]): StartNodeEvent = {
    apply(addresser,
          init,
          Map[String, Any](),
          s"StartEvent[${se.sics.kompics.sl.asJavaClass[C](typeOf[C]).getName}] <${addresser.toString()}>");
  }

  /**
    * Create a node with address and init event, as well as a custom config
    *
    * @tparam C the component definition for the node
    * @tparam A the value type of the config updates
    * @param addresser a function that provides an address for the node
    * @param init a function that provides an init event for the node
    * @param conf config update values with config keys
    *
    * @return a simulator event that starts a node
    *
    * @see [[se.sics.kompics.simulator.events.system.StartNodeEvent]]
    */
  def apply[C <: ComponentDefinition: TypeTag, A <: Any](addresser: => Address,
                                                         init: => se.sics.kompics.Init[C],
                                                         conf: Map[String, A]): StartNodeEvent = {
    apply(addresser,
          init,
          conf,
          s"StartEvent[${se.sics.kompics.sl.asJavaClass[C](typeOf[C]).getName}] <${addresser.toString()}>");
  }

  /**
    * Create a node with address and init event, and a custom `toString` implementation
    *
    * @tparam C the component definition for the node
    * @param addresser a function that provides an address for the node
    * @param init a function that provides an init event for the node
    * @param stringer a custom implementation for `toString` for the event (for debugging)
    *
    * @return a simulator event that starts a node
    *
    * @see [[se.sics.kompics.simulator.events.system.StartNodeEvent]]
    */
  def apply[C <: ComponentDefinition: TypeTag](addresser: => Address,
                                               init: => se.sics.kompics.Init[C],
                                               stringer: => String): StartNodeEvent = {
    apply(addresser, init, Map[String, Any](), stringer);
  }

  /**
    * Create a node with address and init event, as well as a custom config and a custom `toString` implementation
    *
    * @tparam C the component definition for the node
    * @tparam A the value type of the config updates
    * @param addresser a function that provides an address for the node
    * @param init a function that provides an init event for the node
    * @param conf config update values with config keys
    * @param stringer a custom implementation for `toString` for the event (for debugging)
    *
    * @return a simulator event that starts a node
    *
    * @see [[se.sics.kompics.simulator.events.system.StartNodeEvent]]
    */
  def apply[C <: ComponentDefinition: TypeTag, A <: Any](addresser: => Address,
                                                         init: => se.sics.kompics.Init[C],
                                                         conf: Map[String, A],
                                                         stringer: => String): StartNodeEvent = {
    new StartNodeEvent() {

      val selfAddr = addresser;
      val componentType = typeOf[C];
      val javaType = se.sics.kompics.sl.asJavaClass[C](componentType);

      override def getNodeAddress(): Address = {
        return selfAddr;
      }

      override def getComponentDefinition(): Class[C] = {
        return javaType;
      }

      @silent("deprecated")
      override def initConfigUpdate(): java.util.Map[String, Object] = {
        return conf.map { case (k, v) => (k, v.asInstanceOf[AnyRef]) }.asJava;
      }

      override def getComponentInit(): se.sics.kompics.Init[C] = {
        return init;
      }

      override def toString(): String = {
        return stringer
      }

    }
  }
}

/**
  * Construct instances of [[se.sics.kompics.simulator.events.system.KillNodeEvent]]
  */
object KillNode {

  /**
    * Kill a node
    *
    * @param addresser a function giving the address of the node to kill
    *
    * @return a simulator event that kills a node
    *
    * @see [[se.sics.kompics.simulator.events.system.KillNodeEvent]]
    */
  def apply(addresser: => Address): KillNodeEvent = {
    new KillNodeEvent() {

      val selfAddr = addresser;

      override def getNodeAddress(): Address = {
        return selfAddr;
      }

    }
  }
}

/**
  * Construct instances of [[se.sics.kompics.simulator.events.system.ChangeNetworkModelEvent]]
  */
object ChangeNetwork {

  /**
    * Change the current network model
    *
    * Note that changing the network model during the simulation is possible,
    * but has very poorly defined semantics for in-flight messages.
    * For consistent semantics, only change the network model when you are sure that no messages
    * are currently in transit (such as, at the beginning of the simulation).
    *
    * @param netModel the new network model to change to
    *
    * @return a simulator event that changes the network model
    *
    * @see [[se.sics.kompics.simulator.events.system.ChangeNetworkModelEvent]] and for concrete network models [[se.sics.kompics.simulator.network.NetworkModel]]
    */
  def apply(netModel: => NetworkModel): ChangeNetworkModelEvent = {
    new ChangeNetworkModelEvent(netModel)
  }
}

/**
  * Construct setup events
  *
  * @see [[se.sics.kompics.simulator.events.system.SetupEvent]]
  */
object Setup {

  /**
    * A builder-style DSL for constructing setup events
    */
  class SetupBuilder {
    private var systemContext: () => Unit = () => {};
    private var globalView: GlobalView => Unit = (_: GlobalView) => {};
    private var idExtractor: () => IdentifierExtractor = () => { new SocketIdExtractor() };

    /**
      * Replace the current system context
      *
      * @param systemContext a closure that updates the system context
      *
      * @return the builder itself
      */
    def withContext(systemContext: => Unit): SetupBuilder = {
      this.systemContext = (systemContext _);
      this
    }

    /**
      * Replace the current global view handler
      *
      * @param globalView a function that handles the [[se.sics.kompics.simulator.util.GlobalView GlobalView]]
      *
      * @return the builder itself
      */
    def withGlobalView(globalView: GlobalView => Unit): SetupBuilder = {
      this.globalView = globalView;
      this
    }

    /**
      * Replace the current id extractor
      *
      * @param idExtractor the new id extractor
      *
      * @return the builder itself
      *
      * @see [[se.sics.kompics.simulator.network.identifier.IdentifierExtractor]]
      */
    def withIdExtractor(idExtractor: => IdentifierExtractor): SetupBuilder = {
      this.idExtractor = (idExtractor _);
      this
    }

    /**
      * Finalise the builder and construct the setup event
      *
      * @return a simulator event for simulation setup
      *
      * @see [[se.sics.kompics.simulator.events.system.SetupEvent]]
      */
    def build(): SetupEvent = {
      new SetupEvent() {

        private val systemContext: () => Unit = SetupBuilder.this.systemContext;
        private val globalView: GlobalView => Unit = SetupBuilder.this.globalView;
        private val idExtractor: () => IdentifierExtractor = SetupBuilder.this.idExtractor;

        override def setupSystemContext(): Unit = {
          systemContext()
        }

        override def setupGlobalView(gv: GlobalView): Unit = {
          globalView(gv)
        }

        override def getIdentifierExtractor(): IdentifierExtractor = {
          idExtractor()
        }
      }
    }
  }

  /**
    * Quick builder for system context
    *
    * Has the same behaviour as [[SetupBuilder.withContext]].
    *
    * @param systemContext a closure that updates the system context
    *
    * @return a new builder with the system context preset
    */
  def apply(systemContext: => Unit): SetupBuilder = {
    val builder = new SetupBuilder();
    builder.withContext(systemContext);
  }

  /**
    * Quick builder for global view handlers
    *
    * Has the same behaviour as [[SetupBuilder.withGlobalView]].
    *
    * @param globalView a function that handles the [[se.sics.kompics.simulator.util.GlobalView GlobalView]]
    *
    * @return a new builder with the global view handler preset
    */
  def apply(globalView: GlobalView => Unit): SetupBuilder = {
    val builder = new SetupBuilder();
    builder.withGlobalView(globalView);
  }

  // @Lars Why did I comment this out?!?
  //    def apply(idExtractor: => IdentifierExtractor): SetupBuilder = {
  //        val builder = new SetupBuilder();
  //        builder.withIdExtractor(idExtractor())
  //    }

  /**
    * Quick builder for setup events
    *
    * Bypasses the [[SetupBuilder]].
    *
    * @param systemContext a closure that updates the system context
    * @param globalView a function that handles the [[se.sics.kompics.simulator.util.GlobalView GlobalView]]
    * @param idExtractor the new id extractor
    *
    * @return a simulator event for simulation setup
    *
    * @see [[se.sics.kompics.simulator.events.system.SetupEvent]]
    */
  def apply(systemContext: => Unit, globalView: GlobalView => Unit, idExtractor: => IdentifierExtractor): SetupEvent = {
    new SetupEvent() {
      override def setupSystemContext(): Unit = {
        systemContext
      }

      override def setupGlobalView(gv: GlobalView): Unit = {
        globalView(gv)
      }

      override def getIdentifierExtractor(): IdentifierExtractor = {
        idExtractor
      }
    }
  }
}
