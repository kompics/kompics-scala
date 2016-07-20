package se.sics.kompics.sl.simulator

import scala.reflect.runtime.universe._
import collection.JavaConverters._
import se.sics.kompics.ComponentDefinition
import se.sics.kompics.network.{Address, Header, Msg, Network, Transport}
import se.sics.kompics.simulator.events.system.{ChangeNetworkModelEvent, _}
import se.sics.kompics.simulator.util.GlobalView
import se.sics.kompics.simulator.network.identifier.IdentifierExtractor
import se.sics.kompics.simulator.network.identifier.impl.SocketIdExtractor
import se.sics.kompics.simulator.network.NetworkModel
import java.util.HashMap

import collection.mutable._
import scala.collection.mutable;

object StartNode {
  
    def apply[C <: ComponentDefinition: TypeTag](addresser: => Address, init: => se.sics.kompics.Init[C]): StartNodeEvent = {
      apply(addresser, init, mutable.Map[String, Any](), s"StartEvent[${se.sics.kompics.sl.asJavaClass[C](typeOf[C]).getName}] <${addresser.toString()}>");
    }

    def apply[C <: ComponentDefinition: TypeTag, A <: Any](addresser: => Address, init: => se.sics.kompics.Init[C], conf: Map[String, A]): StartNodeEvent = {
      apply(addresser, init, conf, s"StartEvent[${se.sics.kompics.sl.asJavaClass[C](typeOf[C]).getName}] <${addresser.toString()}>");
    }

    def apply[C <: ComponentDefinition: TypeTag](addresser: => Address, init: => se.sics.kompics.Init[C], stringer: => String): StartNodeEvent = {
      apply(addresser, init, mutable.Map[String, Any](), stringer);
    }

    def apply[C <: ComponentDefinition: TypeTag, A <: Any](addresser: => Address, init: => se.sics.kompics.Init[C], conf: Map[String, A],  stringer: => String): StartNodeEvent = {
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
          
            override def initConfigUpdate(): java.util.Map[String, Object] = {
                return conf.mapValues(x => x.asInstanceOf[AnyRef]).asJava;    
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

object KillNode {
    def apply(addresser: => Address): KillNodeEvent = {
        new KillNodeEvent() {

            val selfAddr = addresser;

            override def getNodeAddress(): Address = {
                return selfAddr;
            }

        }
    }
}

object ChangeNetwork {
    def apply(netModel : => NetworkModel): ChangeNetworkModelEvent ={
        new ChangeNetworkModelEvent(netModel)
    }
}

object Setup {
    class SetupBuilder {
        private var systemContext: () => Unit = () => {};
        private var globalView: GlobalView => Unit = (_: GlobalView) => {};
        private var idExtractor: () => IdentifierExtractor = () => { new SocketIdExtractor() };

        def withContext(systemContext: => Unit): SetupBuilder = {
            this.systemContext = (systemContext _);
            this
        }

        def withGlobalView(globalView: GlobalView => Unit): SetupBuilder = {
            this.globalView = globalView;
            this
        }

        def withIdExtractor(idExtractor: => IdentifierExtractor): SetupBuilder = {
            this.idExtractor = (idExtractor _);
            this
        }

        def build(): SetupEvent = {
            new SetupEvent() {

                private val systemContext: () => Unit = SetupBuilder.this.systemContext;
                private val globalView: GlobalView => Unit = SetupBuilder.this.globalView;
                private val idExtractor: () => IdentifierExtractor = SetupBuilder.this.idExtractor;

                override def setupSystemContext() {
                    systemContext()
                }

                override def setupGlobalView(gv: GlobalView) {
                    globalView(gv)
                }

                override def getIdentifierExtractor(): IdentifierExtractor = {
                    idExtractor()
                }
            }
        }
    }

    def apply(systemContext: => Unit): SetupBuilder = {
        val builder = new SetupBuilder();
        builder.withContext(systemContext);
    }
    
    def apply(globalView: GlobalView => Unit): SetupBuilder = {
        val builder = new SetupBuilder();
        builder.withGlobalView(globalView);
    }
    
//    def apply(idExtractor: => IdentifierExtractor): SetupBuilder = {
//        val builder = new SetupBuilder();
//        builder.withIdExtractor(idExtractor())
//    }
    
    def apply(systemContext: => Unit, globalView: GlobalView => Unit, idExtractor: => IdentifierExtractor): SetupEvent = {
        new SetupEvent() {
            override def setupSystemContext() {
                systemContext
            }

            override def setupGlobalView(gv: GlobalView) {
                globalView(gv)
            }

            override def getIdentifierExtractor(): IdentifierExtractor = {
                idExtractor
            }
        }
    }
}