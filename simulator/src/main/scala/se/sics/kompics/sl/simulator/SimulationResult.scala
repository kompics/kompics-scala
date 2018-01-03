package se.sics.kompics.sl.simulator

import scala.reflect.runtime.universe._;

object SimulationResult {

  private val instance = se.sics.kompics.simulator.result.SimulationResultSingleton.getInstance;

  def +=[V](t: (String, V)): Unit = {
    instance.put(t._1, t._2);
  }

  def apply[V: TypeTag](key: String): V = {
    val scalaType = typeOf[V];
    val javaType = se.sics.kompics.sl.asJavaClass[V](scalaType);
    instance.get(key, javaType);
  }

  def get[V: TypeTag](key: String): Option[V] = {
    val scalaType = typeOf[V];
    val javaType = se.sics.kompics.sl.asJavaClass[V](scalaType);
    val v = instance.get(key, classOf[Object]);
    if (v == null) {
      None
    } else {
      if (javaType.isAssignableFrom(v.getClass)) {
        Some(v.asInstanceOf[V])
      } else {
        None
      }
    }
  }

}
