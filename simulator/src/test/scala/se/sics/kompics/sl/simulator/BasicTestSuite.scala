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
package se.sics.kompics.sl.simulator

import org.scalatest._

import se.sics.kompics.sl._

import se.sics.kompics.KompicsEvent
import se.sics.kompics.Kompics
import se.sics.kompics.simulator.{ SimulationScenario => JSimulationScenario }
import se.sics.kompics.simulator.run.LauncherComp
import se.sics.kompics.simulator.adaptor._
import se.sics.kompics.simulator.events.system._
import se.sics.kompics.simulator.adaptor.distributions.extra._
import se.sics.kompics.simulator.instrumentation.{ InstrumentationHelper, CodeInterceptor }
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException
import se.sics.kompics.network.{ Network, Address, Header, Msg, Transport };
import se.sics.kompics.timer.{ Timer, SchedulePeriodicTimeout, Timeout, CancelPeriodicTimeout }
import se.sics.kompics.Start
import scala.concurrent.duration._

import com.typesafe.scalalogging.StrictLogging
import java.util.UUID

class BasicTestSuite extends FunSuite with Matchers {

    //import KompicsUnitSuite._
    //import ScalaComponent._

    test("Simulation shouldn't fail") {
        val seed = 1234l;
        JSimulationScenario.setSeed(seed);
        SimpleSimulation.scenario.simulate(classOf[LauncherComp]);

        //customSimulate(classOf[LauncherComp], SimpleSimulation)

        //        InstrumentationHelper.store(SimpleSimulation);
        //        LauncherComp.main(Array.empty[String]);
    }

}

case object SimpleSimulation {
    
    import Distributions._
    // needed for the distributions, but needs to be initialised after setting the seed
    implicit val random = JSimulationScenario.getRandom();
    
    private def intToAddress(i: Int): Address = {
        try {
            new TAddress(new InetSocketAddress(InetAddress.getByName("192.193.0." + i), 10000));
        } catch {
            case ex: UnknownHostException => throw new RuntimeException(ex);
        }
    }
    
    val startPongerOp = Op { (self: Integer) =>
        val selfAddr = intToAddress(self)
        StartNode(selfAddr, new Init[PongerParent](selfAddr));
    }
    val startPingerOp = Op { (self: Integer, ponger: Integer) =>
        val selfAddr = intToAddress(self);
        val pongerAddr = intToAddress(ponger);
        StartNode(selfAddr, new Init[PingerParent](selfAddr, pongerAddr));
    }
    val scenario = raise(5, startPongerOp, 1.toN).arrival(constant(1000.millis)) andThen
    1000.millis afterTermination
    raise(5, startPingerOp, 6.toN, 1.toN).arrival(constant(1000.millis)) andThen
    10000.millis afterTermination
    Terminate;
}

case object Ping extends KompicsEvent
case object Pong extends KompicsEvent

class PingerParent(init: Init[PingerParent]) extends ComponentDefinition {

    private val (self, ponger) = init match {
        case Init(selfAddr: TAddress, pongerAddr: TAddress) => (selfAddr, pongerAddr)
    }

    val net = requires[Network];
    val timer = requires[Timer];

    val pinger = create(classOf[Pinger], new Init[Pinger](self, ponger));

    connect(net -> pinger);
    connect(timer -> pinger);
}

class PongerParent(init: Init[PongerParent]) extends ComponentDefinition {
    val net = requires[Network];
    val timer = requires[Timer];

    private val self = init match {
        case Init(selfAddr: TAddress) => selfAddr
    }

    val ponger = create(classOf[Ponger], new Init[Ponger](self));

    connect(net -> ponger);
}

case class PingTimeout(spt: SchedulePeriodicTimeout) extends Timeout(spt)

class Pinger(init: Init[Pinger]) extends ComponentDefinition with StrictLogging {
    val net = requires[Network];
    val timer = requires[Timer];

    private val (self, ponger) = init match {
        case Init(selfAddr: TAddress, pongerAddr: TAddress) => (selfAddr, pongerAddr)
    }
    private var counter: Long = 0;
    private var timerId: Option[UUID] = None;

    ctrl uponEvent {
        case _: Start => handle {
            val period = cfg.getValue[Long]("pingpong.pinger.timeout");
            val spt = new SchedulePeriodicTimeout(0, period);
            val timeout = PingTimeout(spt);
            spt.setTimeoutEvent(timeout);
            trigger(spt -> timer);
            timerId = Some(timeout.getTimeoutId());
        }
    }

    net uponEvent {
        case context @ TMessage(_, Pong) => handle {
            counter += 1;
            logger.info(s"Got Pong #$counter!");
        }
    }

    timer uponEvent {
        case PingTimeout(_) => handle {
            trigger(TMessage(THeader(self, ponger, Transport.TCP), Ping) -> net);
        }
    }

    override def tearDown(): Unit = {
        timerId match {
            case Some(id) =>
                trigger(new CancelPeriodicTimeout(id) -> timer);
            case None => // nothing
        }
    }
}

class Ponger(init: Init[Ponger]) extends ComponentDefinition with StrictLogging {

    val net = requires[Network];

    private var counter: Long = 0;
    private val self = init match {
        case Init(selfAddr: TAddress) => selfAddr
    }

    net uponEvent {
        case context @ TMessage(_, Ping) => handle {
            counter += 1;
            logger.info(s"Got Ping #$counter!");
            trigger(TMessage(THeader(self, context.getSource, Transport.TCP), Pong) -> net)
        }
    }
}

final case class TAddress(isa: InetSocketAddress) extends Address {
    override def asSocket(): InetSocketAddress = isa;
    override def getIp(): InetAddress = isa.getAddress;
    override def getPort(): Int = isa.getPort;
    override def sameHostAs(other: Address): Boolean = {
        this.isa.equals(other.asSocket());
    }
}

final case class THeader(src: TAddress, dst: TAddress, proto: Transport) extends Header[TAddress] {
    override def getDestination(): TAddress = dst;
    override def getProtocol(): Transport = proto;
    override def getSource(): TAddress = src;
}

final case class TMessage[C <: KompicsEvent](header: THeader, payload: C) extends Msg[TAddress, THeader] {
    override def getDestination(): TAddress = header.dst;
    override def getHeader(): THeader = header;
    override def getProtocol(): Transport = header.proto;
    override def getSource(): TAddress = header.src;
}



