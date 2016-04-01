package se.sics.kompics.sl

import scala.language.implicitConversions
import scala.reflect.runtime.universe._

class Config(val original: se.sics.kompics.config.Config) {
    import Config._
    
    
    PrimitiveConverters.register();

    def readValue[T: TypeTag](key: String): Option[T] = {
        val valueType = typeOf[T];
        val valueClass = asJavaClass[T](valueType);
        original.readValue(key, valueClass);
    }

    @throws(classOf[ClassCastException])
    def getValue[T: TypeTag](key: String): T = {
        val valueType = typeOf[T];
        val valueClass = asJavaClass[T](valueType);
        original.getValue(key, valueClass);
    }
}

object Config {
    
    implicit def jconf2SConf(c: se.sics.kompics.config.Config): Config = new Config(c);
    implicit def optional2Option[T](o: com.google.common.base.Optional[T]): Option[T] = {
        if (o.isPresent()) {
            return Some(o.get);
        } else {
            return None
        }
    }
}