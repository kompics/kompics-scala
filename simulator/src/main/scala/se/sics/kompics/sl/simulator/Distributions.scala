package se.sics.kompics.sl.simulator

import se.sics.kompics.simulator.adaptor.distributions._
import se.sics.kompics.simulator.adaptor.distributions.extra._
import scala.concurrent.duration._

import java.math.BigInteger;
import java.util.Random;

object Distributions {

  def constant(value: Double): Distribution[java.lang.Double] = {
    return new ConstantDistribution(classOf[java.lang.Double], value)
  }

  def constant(value: Long): Distribution[java.lang.Long] = {
    new ConstantDistribution(classOf[java.lang.Long], value)
  }

  def constant(value: Duration): Distribution[java.lang.Long] = {
    new ConstantDistribution(classOf[java.lang.Long], value.toMillis)
  }

  def constant(value: BigInteger): Distribution[BigInteger] = {
    new ConstantDistribution(classOf[BigInteger], value)
  }

  def uniform(min: Double, max: Double)(implicit random: Random): Distribution[java.lang.Double] = {
    new DoubleUniformDistribution(min, max, random)
  }

  def uniform(min: Long, max: Long)(implicit random: Random): Distribution[java.lang.Long] = {
    new LongUniformDistribution(min, max, random)
  }

  def uniform(min: Duration, max: Duration)(implicit random: Random): Distribution[java.lang.Long] = {
    new LongUniformDistribution(min.toMillis, max.toMillis, random)
  }

  def uniform(min: BigInteger, max: BigInteger)(implicit random: Random): Distribution[BigInteger] = {
    new BigIntegerUniformDistribution(min, max, random)
  }

  def uniform(numBits: Int)(implicit random: Random): Distribution[BigInteger] = {
    new BigIntegerUniformDistribution(numBits, random)
  }

  def exponential(mean: Double)(implicit random: Random): Distribution[java.lang.Double] = {
    new DoubleExponentialDistribution(mean, random)
  }

  def exponential(mean: Long)(implicit random: Random): Distribution[java.lang.Long] = {
    new LongExponentialDistribution(mean, random)
  }

  def exponential(mean: Duration)(implicit random: Random): Distribution[java.lang.Long] = {
    new LongExponentialDistribution(mean.toMillis, random)
  }

  def exponential(mean: BigInteger)(implicit random: Random): Distribution[BigInteger] = {
    new BigIntegerExponentialDistribution(mean, random)
  }

  def normal(mean: Double, variance: Double)(implicit random: Random): Distribution[java.lang.Double] = {
    new DoubleNormalDistribution(mean, variance, random)
  }

  def normal(mean: Long, variance: Long)(implicit random: Random): Distribution[java.lang.Long] = {
    new LongNormalDistribution(mean, variance, random)
  }

  def normal(mean: Duration, variance: Long)(implicit random: Random): Distribution[java.lang.Long] = {
    new LongNormalDistribution(mean.toMillis, variance, random)
  }

  def normal(mean: BigInteger, variance: BigInteger)(implicit random: Random): Distribution[BigInteger] = {
    new BigIntegerNormalDistribution(mean, variance, random)
  }

  def intSeq(range: Range): Distribution[Integer] = {
    new GenIntSequentialDistribution(range.toArray.map(_.asInstanceOf[Integer]));
  }

  def intSeq(start: Int): Distribution[Integer] = {
    new BasicIntSequentialDistribution(start);
  }
}
