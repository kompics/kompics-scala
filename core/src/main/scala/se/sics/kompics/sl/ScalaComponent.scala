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

import scala.collection.JavaConverters._
import scala.language.implicitConversions
import util.control.Breaks._
import se.sics.kompics.PortType
import se.sics.kompics.ComponentCore
import se.sics.kompics.ControlPort
import se.sics.kompics.Component
import se.sics.kompics.{ ComponentDefinition => JCD }
import se.sics.kompics.ConfigurationException
import se.sics.kompics.Negative
import se.sics.kompics.Start
import se.sics.kompics.Started
import se.sics.kompics.Stop
import se.sics.kompics.Stopped
import se.sics.kompics.Kill
import se.sics.kompics.Killed
import se.sics.kompics.PortCore
import java.util.LinkedList
import se.sics.kompics.Positive
import se.sics.kompics.Fault
import se.sics.kompics.SpinlockQueue
import se.sics.kompics.Kompics
import se.sics.kompics.KompicsEvent
import se.sics.kompics.config.{ Config => JConfig }
import se.sics.kompics.config.ConfigUpdate
import se.sics.kompics.config.ValueMerger
import se.sics.kompics.Update
import se.sics.kompics.UpdateAction
import se.sics.kompics.Fault.ResolveAction
import se.sics.kompics.Component.State
import se.sics.kompics.KompicsException
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicReference
import java.util.Objects
import org.slf4j.{ Logger, MDC };
import com.google.common.base.Optional;

/**
 * The <code>ScalaComponent</code> class.
 *
 * @author Lars Kroll {@literal <lkroll@kth.se>}
 * @version $Id: $
 */
protected[sl] class ScalaComponent(val component: ComponentDefinition) extends ComponentCore {

  private[sl] val positivePorts = scala.collection.mutable.HashMap.empty[Class[_ <: PortType], ScalaPort[_ <: PortType]];
  private[sl] val negativePorts = scala.collection.mutable.HashMap.empty[Class[_ <: PortType], ScalaPort[_ <: PortType]];

  private[sl] var negativeControl: ScalaPort[ControlPort] = null;
  private[sl] var positiveControl: ScalaPort[ControlPort] = null;

  private val executeNEvents = Kompics.maxNumOfExecutedEvents.get();

  // constructor
  setup();

  private def setup() {
    this.parent = ComponentCore.parentThreadLocal.get();
    this.conf = if (this.parent != null) {
      parent.config().copy(component.separateConfigId());
    } else {
      Kompics.getConfig().copy(component.separateConfigId());
    }
    if (ComponentCore.childUpdate.get().isPresent()) {
      val ci = conf.asInstanceOf[JConfig.Impl];
      ci.apply(ComponentCore.childUpdate.get().get(), ValueMerger.NONE);
      ComponentCore.childUpdate.set(Optional.absent());
    }
    ComponentCore.parentThreadLocal.set(null);
  }

  implicit private def optionalToOption[T](o: Optional[T]): Option[T] = if (o.isPresent()) Some(o.get) else None;

  override protected[sl] def doCreate[T <: se.sics.kompics.ComponentDefinition](
    definition: Class[T],
    initEvent:  Optional[se.sics.kompics.Init[T]]): Component = {
    doCreateScala(definition, initEvent, Optional.absent())
  }

  override protected[sl] def doCreate[T <: se.sics.kompics.ComponentDefinition](
    definition: Class[T],
    initEvent:  Optional[se.sics.kompics.Init[T]],
    update:     Optional[ConfigUpdate]): Component = {
    doCreateScala(definition, initEvent, update);
  }

  //    def doCreate(definition: Class[_ <: se.sics.kompics.ComponentDefinition], initEvent: se.sics.kompics.Init[_]): Component = doCreate(definition, initEvent, None)
  //
  //    def doCreate(definition: Class[_ <: se.sics.kompics.ComponentDefinition], initEvent: se.sics.kompics.Init[_], update: ConfigUpdate): Component = {
  //        if (update == null) {
  //            doCreate(definition, initEvent, None);
  //        } else {
  //            doCreate(definition, initEvent, Some(update));
  //        }
  //    }

  protected[sl] def doCreateScala[C <: se.sics.kompics.ComponentDefinition](
    definition: Class[C],
    initEvent:  Option[se.sics.kompics.Init[C]],
    update:     Optional[ConfigUpdate]): Component = {
    // create an instance of the implementing component type
    childrenLock.writeLock().lock();
    try {
      ComponentCore.parentThreadLocal.set(this);
      ComponentCore.childUpdate.set(update);

      val cdi = createInstance(definition, initEvent);
      val childCore = cdi.getComponentCore();

      //child.workCount.incrementAndGet();
      childCore.setScheduler(scheduler);

      children.add(childCore);

      return childCore;
    } catch {
      case e: Throwable => throw new RuntimeException(s"Cannot create component ${definition.getCanonicalName()}", e);
    } finally {
      childrenLock.writeLock().unlock();
    }
  }

  private def createInstance[T <: se.sics.kompics.ComponentDefinition](
    definition: Class[T],
    initEvent:  Option[se.sics.kompics.Init[T]]): T = {
    initEvent match {
      case None                               => return definition.newInstance();
      case Some(_: se.sics.kompics.Init.None) => return definition.newInstance();
      case Some(init) => {
        // look for a constructor that takes a single parameter
        // and is assigment compatible with the given init event
        val constr = definition.getConstructor(init.getClass);
        return constr.newInstance(init);
      }
    }
  }

  override def createNegativePort[P <: PortType](portType: Class[P]): Negative[P] = {
    if (!positivePorts.contains(portType)) {
      val pType = PortType.getPortType(portType);
      val positivePort = ScalaPort[P](true, pType, parent);
      val negativePort = ScalaPort[P](false, pType, this);

      negativePort.setPair(positivePort);
      positivePort.setPair(negativePort);

      positivePorts += (portType -> positivePort);

      return negativePort;
    }
    throw new RuntimeException("Cannot create multiple negative "
      + portType.getCanonicalName());
  }

  override def createPositivePort[P <: PortType](portType: Class[P]): Positive[P] = {
    if (!negativePorts.contains(portType)) {
      val pType = PortType.getPortType(portType);
      val positivePort = ScalaPort[P](true, pType, this);
      val negativePort = ScalaPort[P](false, pType, parent);

      negativePort.setPair(positivePort);
      positivePort.setPair(negativePort);

      negativePorts += (portType -> negativePort);

      return positivePort;
    }
    throw new RuntimeException("Cannot create multiple positive "
      + portType.getCanonicalName());
  }

  override def createControlPort(): Negative[ControlPort] = {

    val controlPortType = PortType.getPortType(classOf[ControlPort]);
    negativeControl = ScalaPort[ControlPort](false, controlPortType, this);
    positiveControl = ScalaPort[ControlPort](true, controlPortType, parent);

    positiveControl.setPair(negativeControl);
    negativeControl.setPair(positiveControl);

    negativeControl.doSubscribe(handleLifecycle);

    negativeControl.doSubscribe(handleFault);

    negativeControl.doSubscribe(configHandler);

    return negativeControl;
  }

  override protected[kompics] def cleanPorts() {
    negativePorts.values.foreach { port => port.cleanChannels() }
    positivePorts.values.foreach { port => port.cleanChannels() }
  }

  override protected[kompics] def logger(): Logger = this.component.log.underlying;

  override def getPositive[P <: PortType](portType: Class[P]): Positive[P] = {
    if (positivePorts.contains(portType)) {
      return positivePorts(portType).asInstanceOf[Positive[P]]
    }
    throw new RuntimeException(component + " has no positive "
      + portType.getCanonicalName());
  }

  override def provided[P <: PortType](portType: Class[P]): Positive[P] = getPositive(portType);

  override def getNegative[P <: PortType](portType: Class[P]): Negative[P] = {
    if (negativePorts.contains(portType)) {
      return negativePorts(portType).asInstanceOf[Negative[P]]
    }
    throw new RuntimeException(component + " has no positive "
      + portType.getCanonicalName());
  }

  override def required[P <: PortType](portType: Class[P]): Negative[P] = getNegative(portType);

  override def execute(wid: Int): Unit = {
    var previousState = state;
    if ((state == State.DESTROYED) || (state == State.FAULTY)) {
      return ; // don't schedule these components
    }

    this.wid = wid;

    // 1. pick a port with a non-empty handler queue
    // 2. execute all first handler
    // 3. repeat or reschedule

    var count = 0;
    var wc = workCount.get();

    this.component.setMDC();
    MDC.put(JCD.MDC_KEY_CSTATE, state.name());

    try {
      while ((count < executeNEvents) && wc > 0) {
        breakable {
          if (previousState != state) { // state might have changed between iterations
            if (state == State.FAULTY) {
              return ;
            }
            previousState = state;
            MDC.put(JCD.MDC_KEY_CSTATE, state.name());
          }
          var eventHandlers: (KompicsEvent, Seq[MatchedHandler]) = null;
          var nextPort: ScalaPort[_] = null;
          if ((state == State.PASSIVE) || (state == State.STARTING)) {
            eventHandlers = negativeControl.pollPreparedHandlers();
            nextPort = negativeControl;

            if (eventHandlers == null) {
              logger.debug("Not scheduling component.");
              // try again
              if (wc > 0) {
                schedule(wid);
              }
              return ; // Don't run anything else
            }
            readyPorts.remove(nextPort);
          } else {
            nextPort = readyPorts.poll() match {
              case sp: ScalaPort[_] => sp
              case null => {
                wc = workCount.decrementAndGet();
                count += 1;
                break;
              }
              case x => throw new RuntimeException(s"Incompatible port type: $x")
            }
            eventHandlers = nextPort.pollPreparedHandlers();
          }

          if (eventHandlers == null) {
            logger.debug("Couldn't find event to schedule: wc={}", wc);
            wc = workCount.decrementAndGet();
            count += 1;
            break
          }

          val (event, handlers) = eventHandlers;

          if (handlers != null) {
            breakable {
              handlers foreach { handler =>
                if (executeHandler(event, handler)) {
                  break
                }
              }
            }
          }
          wc = workCount.decrementAndGet();
          count += 1;
        }
      }

    } finally {
      MDC.clear();
    }

    if (wc > 0) {
      schedule(wid);
    }
  }

  private def executeHandler(event: KompicsEvent, handler: MatchedHandler): Boolean = {
    try {
      //Kompics.logger.trace("Executing handler for event {}", event);
      handler();
      return false;
    } catch {
      case ex: Throwable =>
        logger.error("Handling an event caused a fault! Might be handled later...", ex);
        markSubtreeAs(State.FAULTY);
        escalateFault(new Fault(ex, this, event));
        return true; // state changed
    }
  }

  //    def ++[P <: PortType](port: P): PositivePort[_ <: P] = {
  //        this ++ port.getClass();
  //    }
  //
  //    def ++[P <: PortType](portType: Class[P]): PositivePort[_ <: P] = {
  //        val port = provided(portType);
  //        port match {
  //            case pc: PositivePort[P] => return pc;
  //            case _                   => throw new ClassCastException
  //        }
  //    }
  //
  //    def --[P <: PortType](port: P): NegativePort[_ <: P] = {
  //        this -- (port.getClass());
  //    }
  //
  //    def --[P <: PortType](portType: Class[P]): NegativePort[_ <: P] = {
  //        val port = required(portType);
  //        port match {
  //            case pc: NegativePort[P] => return pc;
  //            case _                   => throw new ClassCastException
  //        }
  //    }

  override def getControl(): Positive[ControlPort] = positiveControl;

  override def control(): Positive[ControlPort] = positiveControl;

  override def getComponent(): se.sics.kompics.ComponentDefinition = component;

  def ctrl: PositivePort[ControlPort] = positiveControl;

  override def escalateFault(fault: Fault) {
    if (parent != null) {
      parent.control().doTrigger(fault, wid, this);
    } else {
      logger.error("A fault was escalated to the root component: \n{} \n\n", fault);
      escalateFaultToKompics(fault);
    }
  }

  val handleFault = handler {
    case f: Fault => () => {
      val ra = component.handleFault(f);
      import ResolveAction._
      ra match {
        case RESOLVED =>
          logger.info("Fault {} was resolved by user.", f);
        case IGNORE =>
          logger.info("Fault {} was declared to be ignored by user. Resuming component...", f);
          markSubtreeAtAs(f.getSource.getComponentCore, State.PASSIVE);
          f.getSourceCore.control().doTrigger(Start.event, wid, this);
        case DESTROY =>
          logger.info("User declared that Fault {} should destroy component tree...", f);
          destroyTreeAtParentOf(f.getSource.getComponentCore);
          logger.info("finished destroying the subtree.");
        case _ =>
          escalateFault(f);
      }
    }
  }

  private def confImpl = conf.asInstanceOf[JConfig.Impl];

  private var childrenMemo: Option[Seq[ComponentCore]] = None;
  private def childrenS = childrenMemo match {
    case Some(s) => s
    case None    => childrenMemo = Some(children.asScala); childrenMemo.get
  }

  val configHandler = handler {

    case event: Update => () => {
      val action = component.handleUpdate(event.update);
      import UpdateAction.Propagation._
      action.selfStrategy match {
        case ORIGINAL =>
          confImpl.apply(event.update, action.merger);
        case MAP =>
          confImpl.apply(
            action.selfMapper.map(
              event.update,
              event.update.modify(id())), action.merger);
        case SWALLOW => // nothing
      }
      if ((parent != null) && (event.forwarder == parent.id())) { // downwards
        action.downStrategy match {
          case ORIGINAL =>
            val forwardedEvent = new Update(event.update, id());
            childrenS.foreach { child =>
              child.getControl().asInstanceOf[PortCore[ControlPort]].doTrigger(
                forwardedEvent, wid, component.getComponentCore());
            }
          case MAP =>
            val mappedUpdate = action.downMapper.map(event.update, event.update.modify(id()));
            val forwardedEvent = new Update(mappedUpdate, id());
            childrenS.foreach { child =>
              child.getControl().asInstanceOf[PortCore[ControlPort]].doTrigger(
                forwardedEvent, wid, component.getComponentCore());
            }
          case SWALLOW => // do nothing
        }
      } else { // upwards and to other children
        action.downStrategy match {
          case ORIGINAL =>
            val forwardedEvent = new Update(event.update, id());
            childrenS.foreach { child =>
              if (child.id() != event.forwarder) {
                child.getControl().asInstanceOf[PortCore[ControlPort]].doTrigger(
                  forwardedEvent, wid, component.getComponentCore());
              }
            }
          case MAP =>
            val mappedUpdate = action.downMapper.map(event.update, event.update.modify(id()));
            val forwardedEvent = new Update(mappedUpdate, id());
            childrenS.foreach { child =>
              if (child.id() != event.forwarder) {
                child.getControl().asInstanceOf[PortCore[ControlPort]].doTrigger(
                  forwardedEvent, wid, component.getComponentCore());
              }
            }
          case SWALLOW => // do nothing
        }
        if (parent != null) {
          action.upStrategy match {
            case ORIGINAL =>
              val forwardedEvent = new Update(event.update, id());
              parent.getControl().asInstanceOf[PortCore[ControlPort]].doTrigger(
                forwardedEvent, wid, component.getComponentCore());
            case MAP =>
              val mappedUpdate = action.upMapper.map(event.update, event.update.modify(id()));
              val forwardedEvent = new Update(mappedUpdate, id());
              parent.getControl().asInstanceOf[PortCore[ControlPort]].doTrigger(
                forwardedEvent, wid, component.getComponentCore());

            case SWALLOW => // do nothing
          }
        }
      }
      component.postUpdate();
    }
  }

  protected[kompics] def doConfigUpdate(update: ConfigUpdate) {
    confImpl.apply(update, ValueMerger.NONE);
    val forwardedEvent = new Update(update, id());
    // forward down
    childrenS.foreach { child =>
      child.getControl().asInstanceOf[PortCore[ControlPort]].doTrigger(
        forwardedEvent, wid, this);
    }
    // forward up
    parent.getControl().asInstanceOf[PortCore[ControlPort]].doTrigger(
      forwardedEvent, wid, this);
    component.postUpdate();
  }

  override def equals(that: Any): Boolean = {
    that match {
      case that: ScalaComponent => this.id().equals(that.id());
      case _                    => false
    }
  }

  override def hashCode: Int = {
    var hash = 13;
    hash = 11 * hash + Objects.hashCode(this.id());
    return hash;
  }

  /*
     * === LIFECYCLE ===
     */

  private val activeSet = scala.collection.mutable.HashSet.empty[Component];

  //    override protected[kompics] def setInactive(child: Component) {
  //        activeSet.remove(child);
  //    }

  override protected[kompics] def setInactive(child: se.sics.kompics.Component): Unit = {

  }

  val handleLifecycle = handler {
    case _: Start => () => {
      if (state != State.PASSIVE) {
        throw new KompicsException(s"$this received a Start event while in $state state. "
          + "Duplicate Start events are not allowed!");
      }
      try {
        childrenLock.readLock().lock();
        if (!children.isEmpty()) {
          logger.debug("Starting...");
          state = Component.State.STARTING;
          childrenS.foreach { child =>
            logger.debug("Sending Start to child: {}", child);
            // start child
            child.getControl().asInstanceOf[PortCore[ControlPort]].doTrigger(
              Start.event, wid, component.getComponentCore());
          }
        } else {
          logger.debug("Started!");
          state = Component.State.ACTIVE;
          if (parent != null) {
            parent.getControl().asInstanceOf[PortCore[ControlPort]].doTrigger(
              new Started(component.getComponentCore()), wid, component.getComponentCore());
          }
        }
      } finally {
        childrenLock.readLock().unlock();
      }
    }
    case event: Started => () => {
      logger.debug("Got Started event from {}", event.component);
      activeSet.add(event.component);
      logger.debug("Active set has {} members", activeSet.size);
      try {
        childrenLock.readLock().lock();
        if ((activeSet.size == children.size()) && (state == State.STARTING)) {
          logger.debug("Started!");
          state = Component.State.ACTIVE;
          if (parent != null) {
            parent.getControl().asInstanceOf[PortCore[ControlPort]].doTrigger(new Started(component.getComponentCore()), wid, component.getComponentCore());
          }
        }
      } finally {
        childrenLock.readLock().unlock();
      }
    }
    case _: Stop => () => {
      if (state != Component.State.ACTIVE) {
        throw new KompicsException(s"$this received a Stop event while in $state state. "
          + "Duplicate Stop events are not allowed!");
      }
      try {
        childrenLock.readLock().lock();
        if (!children.isEmpty()) {
          logger.debug("Stopping...");
          state = Component.State.STOPPING;
          childrenS.foreach { child =>
            if (child.state() != Component.State.ACTIVE) {
              // don't send stop events to already stopping components
            } else {
              logger.debug("Sending Stop to child: {}", child);
              // stop child
              child.getControl().asInstanceOf[PortCore[ControlPort]].doTrigger(
                Stop.event, wid, component.getComponentCore());
            }
          }
        } else {
          logger.debug("Stopped!");
          state = Component.State.PASSIVE;
          component.tearDown();
          if (parent != null) {
            parent.getControl().asInstanceOf[PortCore[ControlPort]].doTrigger(
              new Stopped(component.getComponentCore()), wid, component.getComponentCore());
          } else {
            component.getComponentCore.synchronized {
              component.getComponentCore.notifyAll();
            }
          }
        }
      } finally {
        childrenLock.readLock().unlock();
      }
    }
    case event: Stopped => () => {
      logger.debug("Got Stopped event from {}", event.component);
      activeSet -= event.component;
      logger.debug(s"Active set has {} members", activeSet.size);
      if (activeSet.isEmpty && (state == State.STOPPING)) {
        logger.debug("Stopped!");
        state = State.PASSIVE;
        component.tearDown();
        if (parent != null) {
          parent.getControl().asInstanceOf[PortCore[ControlPort]].doTrigger(
            new Stopped(component.getComponentCore()), wid, component.getComponentCore());
        } else {
          component.getComponentCore.synchronized {
            component.getComponentCore().notifyAll();
          }
        }
      }
    }
    case _: Kill => () => {
      if (state != Component.State.ACTIVE) {
        throw new KompicsException(s"$this received a Kill event while in $state state. "
          + "Duplicate Kill events are not allowed!");
      }
      try {
        childrenLock.readLock().lock();
        if (!children.isEmpty()) {
          logger.debug("Slowly dying...");
          state = Component.State.STOPPING;
          getControl.getPair.asInstanceOf[PortCore[ControlPort]].cleanEvents; // if multiple kills are queued up just ignore everything

          childrenS.foreach { child =>
            if (child.state() != Component.State.ACTIVE) {
              // don't send stop events to already stopping components
            } else {
              logger.debug("Sending Kill to child: {}", child);
              // stop child
              child.getControl().asInstanceOf[PortCore[ControlPort]].doTrigger(
                Kill.event, wid, component.getComponentCore());
            }
          }
        } else {
          logger.debug("Dying...");
          state = Component.State.PASSIVE;
          getControl.getPair.asInstanceOf[PortCore[ControlPort]].cleanEvents; // if multiple kills are queued up just ignore everything
          component.tearDown();
          if (parent != null) {
            parent.getControl().asInstanceOf[PortCore[ControlPort]].doTrigger(
              new Killed(component.getComponentCore()), wid, component.getComponentCore());
          } else {
            component.getComponentCore.synchronized {
              component.getComponentCore().notifyAll();
            }
          }
        }
      } finally {
        childrenLock.readLock().unlock();
      }
    }
    case event: Killed => () => {
      logger.debug("Got Killed event from {}", event.component);

      activeSet -= event.component;
      doDestroy(event.component);
      logger.debug("Active set has {} members", activeSet.size);
      if (activeSet.isEmpty && (state == State.STOPPING)) {
        logger.debug("Stopped!");
        state = State.PASSIVE;
        component.tearDown();
        if (parent != null) {
          parent.getControl().asInstanceOf[PortCore[ControlPort]].doTrigger(
            new Killed(component.getComponentCore()), wid, component.getComponentCore());
        } else {
          component.getComponentCore.synchronized {
            component.getComponentCore().notifyAll();
          }
        }
      }
    }
  }
}

class ScalaComponentWrapper(component: Component) {

  def ++[P <: PortType](port: P): PositivePort[_ <: P] = {
    this ++ port.getClass();
  }

  def ++[P <: PortType](portType: Class[P]): PositivePort[_ <: P] = {
    val port = component.provided(portType);
    port match {
      case pp: PositivePort[P] => return pp;
      case pc: PortCore[P]     => return new PositiveWrapper[P](pc);
    }
  }

  def --[P <: PortType](port: P): NegativePort[_ <: P] = {
    this -- (port.getClass());
  }

  def --[P <: PortType](portType: Class[P]): NegativePort[_ <: P] = {
    val port = component.required(portType);
    port match {
      case np: NegativePort[P] => return np;
      case pc: PortCore[P]     => return new NegativeWrapper[P](pc);
    }
  }
}

/**
 * The <code>ScalaComponent</code> object.
 *
 * @author Lars Kroll <lkr@lars-kroll.com>
 * @version $Id: $
 */
object ScalaComponent {
  implicit def component2Scala(cc: Component): ScalaComponentWrapper = new ScalaComponentWrapper(cc);
}
