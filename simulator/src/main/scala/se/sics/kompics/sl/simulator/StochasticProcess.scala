package se.sics.kompics.sl.simulator

import se.sics.kompics.KompicsEvent
import se.sics.kompics.simulator.adaptor._
import se.sics.kompics.simulator.adaptor.distributions._
import scala.concurrent.duration.Duration

/**
  * A sequence of operations with a distribution of time between them
  *
  * @param operations the operations to perform
  * @param eventInterarrivalTime the distribution of time between operations
  */
class StochasticProcess(val operations: Seq[OperationGenerator], // cannot be a case class because of Terminate
                        val eventInterarrivalTime: Distribution[java.lang.Long]);

/**
  * A builder class for [[StochasticProcess]] instances
  */
class StochasticProcessBuilder {
  private val ops = scala.collection.mutable.ListBuffer.empty[OperationGenerator];

  /**
    * Append an operation to the process
    *
    * @tparam E the event type produced by the operation
    * @param count the number time the operation should occur
    * @param op the operation to append
    *
    * @return this instance
    */
  def and[E <: KompicsEvent](count: Int, op: Operation[E]): StochasticProcessBuilder = {
    require(count > 0);
    val generator = new OperationGenerator(new ConcreteOperation(op), count);
    ops += generator;
    this
  }

  /**
    * Append an operation to the process
    *
    * @tparam E the event type produced by the operation
    * @tparam N the type of the first argument of the operation
    * @param count the number time the operation should occur
    * @param op the operation to append
    * @param d the distribution of the first argument of the operation
    *
    * @return this instance
    */
  def and[E <: KompicsEvent, N <: Number](count: Int,
                                          op: Operation1[E, N],
                                          d: Distribution[N]): StochasticProcessBuilder = {
    require(count > 0);
    val generator = new OperationGenerator(new ConcreteOperation(op, d), count);
    ops += generator;
    this
  }

  /**
    * Append an operation to the process
    *
    * @tparam E the event type produced by the operation
    * @tparam N the type of the first argument of the operation
    * @tparam N2 the type of the second argument of the operation
    * @param count the number time the operation should occur
    * @param op the operation to append
    * @param d the distribution of the first argument of the operation
    * @param d2 the distribution of the second argument of the operation
    *
    * @return this instance
    */
  def and[E <: KompicsEvent, N <: Number, N2 <: Number](count: Int,
                                                        op: Operation2[E, N, N2],
                                                        d: Distribution[N],
                                                        d2: Distribution[N2]): StochasticProcessBuilder = {
    require(count > 0);
    val generator = new OperationGenerator(new ConcreteOperation(op, d, d2), count);
    ops += generator;
    this
  }

  /**
    * Append an operation to the process
    *
    * @tparam E the event type produced by the operation
    * @tparam N the type of the first argument of the operation
    * @tparam N2 the type of the second argument of the operation
    * @tparam N3 the type of the third argument of the operation
    * @param count the number time the operation should occur
    * @param op the operation to append
    * @param d the distribution of the first argument of the operation
    * @param d2 the distribution of the second argument of the operation
    * @param d3 the distribution of the third argument of the operation
    *
    * @return this instance
    */
  def and[E <: KompicsEvent, N <: Number, N2 <: Number, N3 <: Number](
      count: Int,
      op: Operation3[E, N, N2, N3],
      d: Distribution[N],
      d2: Distribution[N2],
      d3: Distribution[N3]
  ): StochasticProcessBuilder = {
    require(count > 0);
    val generator = new OperationGenerator(new ConcreteOperation(op, d, d2, d3), count);
    ops += generator;
    this
  }

  /**
    * Append an operation to the process
    *
    * @tparam E the event type produced by the operation
    * @tparam N the type of the first argument of the operation
    * @tparam N2 the type of the second argument of the operation
    * @tparam N3 the type of the third argument of the operation
    * @tparam N4 the type of the fourth argument of the operation
    * @param count the number time the operation should occur
    * @param op the operation to append
    * @param d the distribution of the first argument of the operation
    * @param d2 the distribution of the second argument of the operation
    * @param d3 the distribution of the third argument of the operation
    * @param d4 the distribution of the fourth argument of the operation
    *
    * @return this instance
    */
  def and[E <: KompicsEvent, N <: Number, N2 <: Number, N3 <: Number, N4 <: Number](
      count: Int,
      op: Operation4[E, N, N2, N3, N4],
      d: Distribution[N],
      d2: Distribution[N2],
      d3: Distribution[N3],
      d4: Distribution[N4]
  ): StochasticProcessBuilder = {
    require(count > 0);
    val generator = new OperationGenerator(new ConcreteOperation(op, d, d2, d3, d4), count);
    ops += generator;
    this
  }

  /**
    * Append an operation to the process
    *
    * @tparam E the event type produced by the operation
    * @tparam N the type of the first argument of the operation
    * @tparam N2 the type of the second argument of the operation
    * @tparam N3 the type of the third argument of the operation
    * @tparam N4 the type of the fourth argument of the operation
    * @tparam N5 the type of the fifth argument of the operation
    * @param count the number time the operation should occur
    * @param op the operation to append
    * @param d the distribution of the first argument of the operation
    * @param d2 the distribution of the second argument of the operation
    * @param d3 the distribution of the third argument of the operation
    * @param d4 the distribution of the fourth argument of the operation
    * @param d5 the distribution of the fifths argument of the operation
    *
    * @return this instance
    */
  def and[E <: KompicsEvent, N <: Number, N2 <: Number, N3 <: Number, N4 <: Number, N5 <: Number](
      count: Int,
      op: Operation5[E, N, N2, N3, N4, N5],
      d: Distribution[N],
      d2: Distribution[N2],
      d3: Distribution[N3],
      d4: Distribution[N4],
      d5: Distribution[N5]
  ): StochasticProcessBuilder = {
    require(count > 0);
    val generator = new OperationGenerator(new ConcreteOperation(op, d, d2, d3, d4, d5), count);
    ops += generator;
    this
  }

  /**
    * Complete the stochastic process by giving it an event interarrival time
    *
    * @param interarrivalTime the distribution of time between events
    *
    * @return the final stochastic process
    */
  def arrival(interarrivalTime: Distribution[java.lang.Long]): StochasticProcess =
    new StochasticProcess(ops.toSeq, interarrivalTime);
}

/**
  * A special process that marks the end of the simulation
  */
case object Terminate extends StochasticProcess(null, null)

/**
  * A simulation is formed by a chain of stochastic processes
  *
  * It should end with a [[Terminate]] process.
  *
  * @see [[package se.sics.kompics.sl.simulator]] for an example
  */
class StochasticProcessChain extends Serializable {

  /**
    * Linking objects in the process chain DSL
    */
  sealed trait Connector;

  /**
    * The beginning of a chain
    */
  case object Start extends Connector;

  /**
    * A link that activates some time after a process has terminated
    */
  case class AfterTermination(time: Long) extends Connector {
    def millisAfter(next: StochasticProcess): StochasticProcessChain = {
      chain += (this -> next);
      StochasticProcessChain.this
    }
  }

  /**
    * A link that activates some time after the start of a process
    */
  case class AfterStart(time: Long) extends Connector;

  /**
    * A link that activates at a particular time in the simulation
    */
  case class AtTime(time: Long) extends Connector;

  /**
    * A link that activates multiple processes in parallel
    */
  case object InParallel extends Connector;

  /**
    * A DSL object that indicates that a time connector is expected next
    *
    * @param time the time passed to the time connector
    */
  case class TimeLink(time: Duration) {

    /**
      * Use an [[AfterTermination]] connector to start the next process
      *
      * @param doNext the next process to start
      *
      * @return the process chain itself
      */
    def afterTermination(doNext: StochasticProcess): StochasticProcessChain = {
      chain += (AfterTermination(time.toMillis) -> doNext);
      StochasticProcessChain.this
    }

    /**
      * Use an [[AfterStart]] connector to start the next process
      *
      * @param doNext the next process to start
      *
      * @return the process chain itself
      */
    def afterStart(doNext: StochasticProcess): StochasticProcessChain = {
      chain += (AfterStart(time.toMillis) -> doNext);
      StochasticProcessChain.this
    }
  }

  /**
    * A DSL object that indicates that a concrete time connector is expected next
    *
    * @param time the time passed to the time connector
    */
  case class TimeLink2(time: Duration) {

    /**
      * Use an [[AtTime]] connector to start the next process
      *
      * @param doNext the next process to start
      *
      * @return the process chain itself
      */
    def start(doNext: StochasticProcess): StochasticProcessChain = {
      chain += (AtTime(time.toMillis) -> doNext);
      StochasticProcessChain.this
    }
  }

  @transient private val chain = scala.collection.mutable.ListBuffer.empty[Tuple2[Connector, StochasticProcess]];

  /**
    * The first process in the chain
    *
    * @param sp the process that comes first in the chain
    * @return the process chain itself
    */
  def startWith(sp: StochasticProcess): StochasticProcessChain = {
    chain += (Start -> sp);
    this
  }

  /**
    * Link the next process by time
    *
    * @param time the time value to use for the link
    *
    * @return a DSL object to indicate how interpret the time
    */
  def andThen(time: Duration): TimeLink = {
    TimeLink(time)
  }

  /**
    * Link the next process at a time time
    *
    * @param time the time value to use for the link
    *
    * @return a DSL object to indicate how interpret the time
    */
  def andThenAt(time: Duration): TimeLink2 = {
    TimeLink2(time)
  }

  /**
    * Execute multiple processes in parallel
    *
    * @param parallelProcs the processes to eexecute
    *
    * @return the process chain itself
    */
  def inParallel(parallelProcs: StochasticProcess*): StochasticProcessChain = {
    parallelProcs.foreach(proc => chain += (InParallel -> proc));
    StochasticProcessChain.this
  }

  /**
    * Produce an actual [[se.sics.kompics.simulator.SimulationScenario SimulationScenario]] from the process chain
    *
    * Converts the Scala DSL process chain into a Kompics Java [[se.sics.kompics.simulator.SimulationScenario SimulationScenario]]
    * for execution.
    *
    * Scenarios can be executed by calling [[https://javadoc.io/static/se.sics.kompics.simulator/core/1.2.0/se/sics/kompics/simulator/SimulationScenario.html#simulate(java.lang.Class) simulate]] on them.
    *
    * @return the new scenario
    */
  def get(): se.sics.kompics.simulator.SimulationScenario = {
    new se.sics.kompics.simulator.SimulationScenario() {
      var lastSP: Option[StochasticProcess] = None;
      for ((connector, process) <- chain) {
        (connector, process) match {
          case (Start, Terminate) => {
            this.terminateAt(0);
          }
          case (Start, sp) => {
            val jsp = toJava(sp);
            jsp.start;
            lastSP = Some(jsp);
          }
          case (AfterTermination(time), Terminate) => {
            lastSP match {
              case Some(lsp) => this.terminateAfterTerminationOf(time, lsp);
              case None =>
                sys.error("Can't terminate SimulationScenario after termination of nothing! Start another one first!");
            }
          }
          case (AfterTermination(time), sp) => {
            val jsp = toJava(sp);
            lastSP match {
              case Some(lsp) => jsp.startAfterTerminationOf(time, lsp); lastSP = Some(jsp);
              case None =>
                sys.error("Can't start StochasticProcess after termination of nothing! Start another one first!")
            }
          }
          case (AfterStart(time), Terminate) => {
            lastSP match {
              case Some(lsp) =>
                this.terminateAfterTerminationOf(time, lsp);
                System.err.println(
                  "WARN: There is no primitive to terminate after the start of a previous StochasticProcess. Will terminate after termination instead."
                );
              case None =>
                sys.error("Can't terminate SimulationScenario after start of nothing! Start another one first!");
            }
          }
          case (AfterStart(time), sp) => {
            val jsp = toJava(sp);
            lastSP match {
              case Some(lsp) => jsp.startAfterStartOf(time, lsp); lastSP = Some(jsp);
              case None      => sys.error("Can't start StochasticProcess after start of nothing! Start another one first!")
            }
          }
          case (InParallel, Terminate) => {
            sys.error(
              "It really doesn't make any sense to terminate a SimulationScenario in parallel with some StochasticProcess!"
            );
          }
          case (InParallel, sp) => {
            val jsp = toJava(sp);
            lastSP match {
              case Some(lsp) => jsp.startAtSameTimeWith(lsp); lastSP = Some(jsp);
              case None      => sys.error("Can't start StochasticProcess in parallel with nothing! Start another one first!")
            }
          }
          case (AtTime(time), Terminate) => {
            this.terminateAt(time);
          }
          case (AtTime(time), sp) => {
            val jsp = toJava(sp);
            jsp.startAt(time);
            lastSP = Some(jsp);
          }
        }
      }

      private def toJava(sp: se.sics.kompics.sl.simulator.StochasticProcess): StochasticProcess = {
        new StochasticProcess() {
          //println(s"Converting SSP to JSP: $sp")
          eventInterarrivalTime(sp.eventInterarrivalTime);
          for (op <- sp.operations) {
            generators.add(op);
          }
        }
      }
    }
  }
}
