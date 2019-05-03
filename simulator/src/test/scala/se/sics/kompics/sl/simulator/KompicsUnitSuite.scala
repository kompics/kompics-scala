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
package se.sics.kompics.sl

import org.scalatest._

import concurrent.Waiters
import se.sics.kompics.KompicsEvent
import se.sics.kompics.Component
import se.sics.kompics.config.ConfigUpdate

trait EventTester {
  @volatile protected var check: KompicsEvent => Unit = { (msg) =>
  }

  def registerHandler(f: KompicsEvent => Unit) {
    check = f
  }
}

object KompicsUnitSuite {
  //    trait Conf extends Function1[SetupDefinition, Unit]
  //    trait EventChecker extends Function1[KompicsEvent, Unit]
  type Conf = SetupDefinition => Unit;
  type EventChecker = KompicsEvent => Unit;

  class SetupDefinition(init: Init[SetupDefinition])
    extends ComponentDefinition {

    private val (conf, checker) = init match {
      case Init(conf: Conf @unchecked, checker: EventChecker @unchecked) =>
        (conf, checker)
    };

    def child[T <: se.sics.kompics.ComponentDefinition](
      definition: Class[T]): Component = child(definition, None, None);
    def child[T <: se.sics.kompics.ComponentDefinition](
      definition: Class[T],
      initEvent:  Option[se.sics.kompics.Init[T]]): Component =
      child(definition, initEvent, None);
    def child[T <: se.sics.kompics.ComponentDefinition](
      definition: Class[T],
      initEvent:  Option[se.sics.kompics.Init[T]],
      update:     Option[ConfigUpdate]): Component = {
      val c = (initEvent, update) match {
        case (Some(ie), Some(u)) => create(definition, ie, u)
        case (Some(ie), None)    => create(definition, ie)
        case (None, Some(u))     => create(definition, se.sics.kompics.Init.NONE, u)
        case (None, None)        => create(definition, se.sics.kompics.Init.NONE)
      };
      val cd = c.getComponent;
      cd match {
        case et: EventTester => et.registerHandler(checker)
      }
      return c
    }

    conf(this);
  }

}

abstract class KompicsUnitSuite
  extends FunSuite
  with Matchers
  with Waiters {

  import KompicsUnitSuite._

  import org.scalatest.exceptions.NotAllowedException
  import org.scalatest.exceptions.TestFailedException
  import org.scalatest.concurrent.PatienceConfiguration._
  import time.{ Nanoseconds, Seconds, Span }

  override implicit def patienceConfig =
    PatienceConfig(scaled(Span(5, Seconds)))

  def setup(configure: Conf, checker: EventChecker): Tuple2[Class[SetupDefinition], Init[SetupDefinition]] =
    (classOf[SetupDefinition], Init(configure, checker))

  class EventWaiter extends EventChecker {

    private final val creatingThread = Thread.currentThread

    val checkers: java.util.Deque[EventChecker] =
      new java.util.LinkedList[EventChecker]();
    val eventQueue =
      new java.util.concurrent.LinkedBlockingDeque[KompicsEvent]();

    private var init: () => Unit = () => {};

    def apply(init: => Unit) {
      this.init = init _;
    }

    def apply(fun: EventChecker) {
      if (Thread.currentThread != creatingThread) {
        throw new NotAllowedException(
          "EventCheckers must be created on the test thread not within components!",
          2)
      }
      checkers.add(fun)
    }

    class NOF(n: Int) {
      def of(fun: EventChecker) {
        for (_ <- 1 to n) {
          apply(fun)
        }
      }
    }

    def apply(times: Int): NOF = {
      new NOF(times);
    }

    def apply(event: KompicsEvent) {
      println(s"Putting $event into the event queue");
      eventQueue.put(event);
    }

    private def awaitImpl(timeout: Span) {
      if (Thread.currentThread != creatingThread) {
        throw new NotAllowedException("Await must be called on test thread!", 2)
      }
      val startTime: Long = System.nanoTime
      val endTime: Long = startTime + timeout.totalNanos
      def timeLeft: Boolean = endTime > System.nanoTime

      init();

      while (timeLeft && !checkers.isEmpty()) {
        val timeLeft: Span = {
          val diff = endTime - System.nanoTime
          if (diff > 0) Span(diff, Nanoseconds) else Span.Zero
        }
        val e = eventQueue.poll(
          timeLeft.totalNanos,
          java.util.concurrent.TimeUnit.NANOSECONDS);
        println(s"Pulled a $e out of the eventQueue");
        if (e != null) {
          val check = checkers.poll();
          println(s"Checking $e.");
          check(e);
        }
      }
      if (!timeLeft && !checkers.isEmpty()) {
        throw new TestFailedException(
          s"Ran out of time waiting for ${checkers.size()} more events",
          2)
      }
    }

    def await(timeout: Timeout) {
      awaitImpl(timeout.value)
    }

    def await()(implicit config: PatienceConfig) {
      awaitImpl(config.timeout)
    }
  }
}
