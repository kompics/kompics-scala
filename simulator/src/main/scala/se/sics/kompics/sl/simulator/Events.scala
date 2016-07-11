package se.sics.kompics.sl.simulator

import scala.reflect.runtime.universe._

import se.sics.kompics.ComponentDefinition
import se.sics.kompics.network.{ Network, Address, Header, Msg, Transport };
import se.sics.kompics.simulator.events.system._
import se.sics.kompics.simulator.util.GlobalView;
import se.sics.kompics.simulator.network.identifier.IdentifierExtractor;
import se.sics.kompics.simulator.network.identifier.impl.SocketIdExtractor;

object StartNode {
    def apply[C <: ComponentDefinition: TypeTag](addresser: => Address, init: => se.sics.kompics.Init[C]): StartNodeEvent = {
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

            override def getComponentInit(): se.sics.kompics.Init[C] = {
                return init;
            }

            override def toString(): String = {
                return s"StartEvent[${javaType.getName}] <${selfAddr.toString()}>"
            }

        }
    }

    def apply[C <: ComponentDefinition: TypeTag](addresser: => Address, init: => se.sics.kompics.Init[C], stringer: => String): StartNodeEvent = {
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