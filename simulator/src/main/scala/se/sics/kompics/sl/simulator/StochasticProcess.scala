package se.sics.kompics.sl.simulator

import se.sics.kompics.KompicsEvent
import se.sics.kompics.simulator.adaptor._
import se.sics.kompics.simulator.adaptor.distributions._
import scala.concurrent.duration.Duration

class StochasticProcess(val operations: Seq[OperationGenerator],
                        val eventIntegerArrivalTime: Distribution[java.lang.Long]) {
}

class StochasticProcessBuilder {
    private val ops = scala.collection.mutable.ListBuffer.empty[OperationGenerator];

    def and[E <: KompicsEvent](count: Int, op: Operation[E]): StochasticProcessBuilder = {
        require(count > 0);
        val generator = new OperationGenerator(new ConcreteOperation(op), count);
        ops += generator;
        this
    }

    def and[E <: KompicsEvent, N <: Number](count: Int, op: Operation1[E, N], d: Distribution[N]): StochasticProcessBuilder = {
        require(count > 0);
        val generator = new OperationGenerator(new ConcreteOperation(op, d), count);
        ops += generator;
        this
    }

    def and[E <: KompicsEvent, N <: Number, N2 <: Number](count: Int, op: Operation2[E, N, N2], d: Distribution[N], d2: Distribution[N2]): StochasticProcessBuilder = {
        require(count > 0);
        val generator = new OperationGenerator(new ConcreteOperation(op, d, d2), count);
        ops += generator;
        this
    }

    def and[E <: KompicsEvent, N <: Number, N2 <: Number, N3 <: Number](count: Int, op: Operation3[E, N, N2, N3], d: Distribution[N], d2: Distribution[N2], d3: Distribution[N3]): StochasticProcessBuilder = {
        require(count > 0);
        val generator = new OperationGenerator(new ConcreteOperation(op, d, d2, d3), count);
        ops += generator;
        this
    }

    def and[E <: KompicsEvent, N <: Number, N2 <: Number, N3 <: Number, N4 <: Number](count: Int, op: Operation4[E, N, N2, N3, N4], d: Distribution[N], d2: Distribution[N2], d3: Distribution[N3], d4: Distribution[N4]): StochasticProcessBuilder = {
        require(count > 0);
        val generator = new OperationGenerator(new ConcreteOperation(op, d, d2, d3, d4), count);
        ops += generator;
        this
    }

    def and[E <: KompicsEvent, N <: Number, N2 <: Number, N3 <: Number, N4 <: Number, N5 <: Number](count: Int, op: Operation5[E, N, N2, N3, N4, N5], d: Distribution[N], d2: Distribution[N2], d3: Distribution[N3], d4: Distribution[N4], d5: Distribution[N5]): StochasticProcessBuilder = {
        require(count > 0);
        val generator = new OperationGenerator(new ConcreteOperation(op, d, d2, d3, d4, d5), count);
        ops += generator;
        this
    }

    def arrival(interArrivalTime: Distribution[java.lang.Long]): StochasticProcess = {
        return new StochasticProcess(ops.toSeq, interArrivalTime);
    }
}

case object Terminate extends StochasticProcess(null, null)

class StochasticProcessChain extends Serializable {

    trait Connector
    case object Start extends Connector
    case class AfterTermination(time: Long) extends Connector {
        def millisAfter(next: StochasticProcess): StochasticProcessChain = {
            chain += (this -> next);
            StochasticProcessChain.this
        }
    }
    case class AfterStart(time: Long) extends Connector
    case class AtTime(time: Long) extends Connector
    case object InParallel extends Connector

    case class TimeLink(time: Duration) {
        def afterTermination(doNext: StochasticProcess): StochasticProcessChain = {
            chain += (AfterTermination(time.toMillis) -> doNext);
            StochasticProcessChain.this
        }
        def afterStart(doNext: StochasticProcess): StochasticProcessChain = {
            chain += (AfterStart(time.toMillis) -> doNext);
            StochasticProcessChain.this
        }
    }
    case class TimeLink2(time: Duration) {
        def start(doNext: StochasticProcess): StochasticProcessChain = {
            chain += (AtTime(time.toMillis) -> doNext);
            StochasticProcessChain.this
        }
    }

    @transient private val chain = scala.collection.mutable.ListBuffer.empty[Tuple2[Connector, StochasticProcess]]

    def startWith(sp: StochasticProcess): StochasticProcessChain = {
        chain += (Start -> sp);
        this
    }

    def andThen(time: Duration): TimeLink = {
        TimeLink(time)
    }

    def andThenAt(time: Duration): TimeLink2 = {
        TimeLink2(time)
    }

    def inParallel(doNext: StochasticProcess): StochasticProcessChain = {
        chain += (InParallel -> doNext);
        StochasticProcessChain.this
    }

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
                            case None      => sys.error("Can't terminate SimulationScenario after termination of nothing! Start another one first!");
                        }
                    }
                    case (AfterTermination(time), sp) => {
                        val jsp = toJava(sp);
                        lastSP match {
                            case Some(lsp) => jsp.startAfterTerminationOf(time, lsp); lastSP = Some(jsp);
                            case None      => sys.error("Can't start StochasticProcess after termination of nothing! Start another one first!")
                        }
                    }
                    case (AfterStart(time), Terminate) => {
                        lastSP match {
                            case Some(lsp) => this.terminateAfterTerminationOf(time, lsp); System.err.println("WARN: There is no primitive to terminate after the start of a previous StochasticProcess. Will terminate after termination instead.");
                            case None      => sys.error("Can't terminate SimulationScenario after start of nothing! Start another one first!");
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
                        sys.error("It really doesn't make any sense to terminate a SimulationScenario in parallel with some StochasticProcess!");                        
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
                    println(s"Converting SSP to JSP: $sp")
                    eventInterArrivalTime(sp.eventIntegerArrivalTime);
                    for (op <- sp.operations) {
                        generators.add(op);
                    }
                }
            }
        }
    }
}