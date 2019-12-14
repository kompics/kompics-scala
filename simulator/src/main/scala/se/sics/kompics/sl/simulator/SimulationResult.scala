package se.sics.kompics.sl.simulator

import scala.reflect.runtime.universe._

import scala.util.{Failure, Success, Try};

/**
  * A helper to share values between the simluation and the testing code
  *
  * Since simulations run with a different classloader than the code that spawns them,
  * it is usually difficult to share values between them.
  * This class allows doing so, using a string-indexed map.
  *
  * The underlying implementation is [[se.sics.kompics.simulator.result.SimulationResultSingleton]].
  *
  * @example {{{
  * SimulationResult += ("test" -> 1); /* Pass 1 to the simulation */
  * SimpleSimulation.scenario.simulate(classOf[LauncherComp]); /* The simulation runs and sets "test" to 2 */
  * SimulationResult[Int]("test") should be(2); /* Get the new value from the simulation */
  * }}}
  */
object SimulationResult {

  private val instance = se.sics.kompics.simulator.result.SimulationResultSingleton.getInstance;

  /**
    * Put a value into the map
    *
    * Overrides existing values, if any.
    *
    * @tparam V the type of the value to be put
    * @param t a key-value tuple
    *
    * @example {{{
    * SimulationResult += ("test" -> 1);
    * }}}
    */
  def +=[V](t: (String, V)): Unit = {
    instance.put(t._1, t._2);
  }

  /**
    * Get the value at `key` from the map cast to `V`
    *
    * Throws an exception, if the actual value can't be cast to `V` or the key doesn't exist.
    *
    * This only works for basic JVM types. If you need to pass a custom type, use [[#convert convert]] instead.
    *
    * @tparam V the type to cast the value to
    * @param key the key to look up
    *
    * @return the value at `key` cast to `V`
    *
    * @see [[se.sics.kompics.simulator.result.SimulationResultSingleton!.get* get]]
    */
  def apply[V: TypeTag](key: String): V = {
    val scalaType = typeOf[V];
    val javaType = se.sics.kompics.sl.asJavaClass[V](scalaType);
    instance.get(key, javaType);
  }

  /**
    * Get the value at `key` from the map cast to `V`, if possible
    *
    * Returns [[scala.None None]], if the actual value can't be cast to `V` or the key doesn't exist.
    *
    * This only works for basic JVM types. If you need to pass a custom type, use [[#convert convert]] instead.
    *
    * @tparam V the type to cast the value to
    * @param key the key to look up
    *
    * @return the value at `key` cast to `V` wrapped into an [[scala.Option Option]]
    *
    * @see [[se.sics.kompics.simulator.result.SimulationResultSingleton!.get* get]]
    */
  def get[V: TypeTag](key: String): Option[V] = {
    val scalaType = typeOf[V];
    val javaType = se.sics.kompics.sl.asJavaClass[V](scalaType);
    val v = instance.get(key, classOf[Object]);
    if (v == null) {
      None
    } else {
      val instanceClass = v.getClass();
      println(s"type=${javaType} vs. object=${instanceClass}");
      val res = Try {
        if (javaType.isPrimitive() && isPrimitiveWrapperOf(instanceClass, javaType)) {
          v.asInstanceOf[V]
        } else if (instanceClass.isPrimitive() && isPrimitiveWrapperOf(javaType, instanceClass)) {
          v.asInstanceOf[V]
        } else if (javaType.isAssignableFrom(instanceClass)) {
          v.asInstanceOf[V]
        } else {
          ???
        }
      };
      res match {
        case Success(value) => Some(value)
        case Failure(ex) => {
          Console.err.println(s"Could not convert $v ($instanceClass) to $javaType: $ex");
          None
        }
      }
    }
  }

  /**
    * Get the value at `key` from the map and try convert it to `V`, if possible
    *
    * This method tries to use [[scala.Serializable Serializable]] when the object can't simply be cast to `V`.
    * This happens in particular when it is of a custom class, with its instances loaded by two different classloaders.
    *
    * @tparam V the type to convert the value to
    * @param key the key to look up
    *
    * @return the value at `key` converted to `V` wrapped into an [[scala.util.Try Try]]
    *
    * @see [[se.sics.kompics.simulator.result.SimulationResultSingleton!.get* get]]
    */
  def convert[V <: Serializable: TypeTag](key: String): Try[V] = {
    import java.io._;

    val scalaType = typeOf[V];
    val javaType = se.sics.kompics.sl.asJavaClass[V](scalaType);
    val v = instance.get(key, classOf[Object]);
    if (v == null) {
      Failure(new NullPointerException(s"Object at key=$key was null"))
    } else {
      val instanceClass = v.getClass();
      println(s"type=${javaType} vs. object=${instanceClass}");

      if (javaType.isPrimitive() && isPrimitiveWrapperOf(instanceClass, javaType)) {
        Success(v.asInstanceOf[V])
      } else if (instanceClass.isPrimitive() && isPrimitiveWrapperOf(javaType, instanceClass)) {
        Success(v.asInstanceOf[V])
      } else if (javaType.isAssignableFrom(instanceClass)) {
        Success(v.asInstanceOf[V])
      } else if (javaType.getCanonicalName() == instanceClass.getCanonicalName()) {
        // they are actually the same class but loaded by different class loaders
        Try {
          //Serialization of object
          val bos = new ByteArrayOutputStream();
          val out = new ObjectOutputStream(bos);
          out.writeObject(v);

          //De-serialization of object
          val bis = new ByteArrayInputStream(bos.toByteArray());
          val in = new ObjectInputStream(bis);
          val copied = in.readObject().asInstanceOf[V];
          copied
        }
      } else {
        Failure(new ClassCastException(s"Object of type ${instanceClass} could not be converted to type ${javaType}."))
      }
    }
  }

  private val primitiveWrapperMap: Map[Class[_], Class[_]] =
    Map(
      classOf[Boolean] -> classOf[java.lang.Boolean],
      classOf[Byte] -> classOf[java.lang.Byte],
      classOf[Char] -> classOf[java.lang.Character],
      classOf[Double] -> classOf[java.lang.Double],
      classOf[Float] -> classOf[java.lang.Float],
      classOf[Int] -> classOf[java.lang.Integer],
      classOf[Long] -> classOf[java.lang.Long],
      classOf[Short] -> classOf[java.lang.Short]
    );

  private def isPrimitiveWrapperOf(targetClass: java.lang.Class[_], primitive: java.lang.Class[_]): Boolean = {
    if (!primitive.isPrimitive()) {
      throw new IllegalArgumentException("First argument has to be primitive type");
    }
    return primitiveWrapperMap(primitive) == targetClass;
  }

}
