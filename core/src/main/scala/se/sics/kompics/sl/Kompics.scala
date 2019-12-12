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

import se.sics.kompics.{Kompics => JKompics, Scheduler, FaultHandler, Init => JInit}
import se.sics.kompics.config.{Config => JConfig}

/**
  * Convenient object to forward static members of [[se.sics.kompics.Kompics]].
  *
  * @author Lars Kroll {@literal <lkroll@kth.se>}
  */
object Kompics {

  val SHUTDOWN_TIMEOUT = JKompics.SHUTDOWN_TIMEOUT;
  val logger = JKompics.logger;
  val maxNumOfExecutedEvents = JKompics.maxNumOfExecutedEvents;

  /**
    * @see [[se.sics.kompics.Kompics.setScheduler*]]
    */
  def scheduler_=(sched: Scheduler): Unit = JKompics.setScheduler(sched);

  /**
    * @see [[se.sics.kompics.Kompics.getScheduler*]]
    */
  def scheduler: Scheduler = JKompics.getScheduler;

  /**
    * @see [[se.sics.kompics.Kompics.setFaultHandler*]]
    */
  def faultHandler_=(fh: FaultHandler): Unit = JKompics.setFaultHandler(fh);

  /**
    * @see [[se.sics.kompics.Kompics.resetFaultHandler*]]
    */
  def resetFaultHandler(): Unit = JKompics.resetFaultHandler();

  /**
    * @see [[se.sics.kompics.Kompics.getFaultHandler*]]
    */
  def faultHandler: FaultHandler = JKompics.getFaultHandler;

  /**
    * @see [[se.sics.kompics.Kompics.setConfig*]]
    */
  def config_=(conf: JConfig): Unit = JKompics.setConfig(conf);

  /**
    * @see [[se.sics.kompics.Kompics.resetConfig*]]
    */
  def resetConfig(): Unit = JKompics.resetConfig();

  /**
    * @see [[se.sics.kompics.Kompics.getConfig*]]
    */
  def config: JConfig = JKompics.getConfig;

  /**
    * @see [[se.sics.kompics.Kompics.isOn*]]
    */
  def isOn: Boolean = JKompics.isOn();

  def createAndStart[C <: ComponentDefinition](main: Class[C]): Unit = JKompics.createAndStart[C](main);

  def createAndStart[C <: ComponentDefinition](main: Class[C], init: JInit[C]): Unit =
    JKompics.createAndStart[C](main, init);

  def createAndStart[C <: ComponentDefinition](main: Class[C], workers: Int): Unit =
    JKompics.createAndStart[C](main, workers);

  def createAndStart[C <: ComponentDefinition](main: Class[C], init: JInit[C], workers: Int): Unit =
    JKompics.createAndStart[C](main, init, workers);

  def createAndStart[C <: ComponentDefinition](main: Class[C], workers: Int, maxEventExecuteNumber: Int): Unit =
    JKompics.createAndStart[C](main, workers, maxEventExecuteNumber);

  def createAndStart[C <: ComponentDefinition](main: Class[C],
                                               init: JInit[C],
                                               workers: Int,
                                               maxEventExecuteNumber: Int): Unit =
    JKompics.createAndStart[C](main, init, workers, maxEventExecuteNumber);

  /**
    * @see [[se.sics.kompics.Kompics.asyncShutdown*]]
    */
  def asyncShutdown(): Unit = JKompics.asyncShutdown();

  /**
    * @see [[se.sics.kompics.Kompics.shutdown*]]
    */
  def shutdown(): Unit = JKompics.shutdown();

  /**
    * @see [[se.sics.kompics.Kompics.forceShutdown*]]
    */
  def forceShutdown(): Unit = JKompics.forceShutdown();

  /**
    * @see [[se.sics.kompics.Kompics.waitForTermination*]]
    */
  @throws(classOf[InterruptedException])
  def waitForTermination(): Unit = JKompics.waitForTermination();
}
