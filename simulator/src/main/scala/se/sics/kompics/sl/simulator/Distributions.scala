package se.sics.kompics.sl.simulator

import se.sics.kompics.simulator.adaptor.distributions._
import se.sics.kompics.simulator.adaptor.distributions.extra._
import scala.concurrent.duration._

import java.math.BigInteger;
import java.util.Random;

/**
  * Convenience DSL objects for building different kinds of distributions.
  */
object Distributions {

  /**
    * A constant distribution for double values
    *
    * @param value the value to return on every sample
    * @return a new distribution object
    *
    * @see [[se.sics.kompics.simulator.adaptor.distributions.ConstantDistribution]]
    */
  def constant(value: Double): Distribution[java.lang.Double] = {
    return new ConstantDistribution(classOf[java.lang.Double], value)
  }

  /**
    * A constant distribution for long values
    *
    * @param value the value to return on every sample
    * @return a new distribution object
    *
    * @see [[se.sics.kompics.simulator.adaptor.distributions.ConstantDistribution]]
    */
  def constant(value: Long): Distribution[java.lang.Long] = {
    new ConstantDistribution(classOf[java.lang.Long], value)
  }

  /**
    * A constant distribution for duration values
    *
    * Is translated internally to a long distribution of millisecond values.
    *
    * @param value the value to return on every sample
    * @return a new distribution object
    *
    * @see [[se.sics.kompics.simulator.adaptor.distributions.ConstantDistribution]]
    */
  def constant(value: Duration): Distribution[java.lang.Long] = {
    new ConstantDistribution(classOf[java.lang.Long], value.toMillis)
  }

  /**
    * A constant distribution for big integer values
    *
    * @param value the value to return on every sample
    * @return a new distribution object
    *
    * @see [[se.sics.kompics.simulator.adaptor.distributions.ConstantDistribution]]
    */
  def constant(value: BigInteger): Distribution[BigInteger] = {
    new ConstantDistribution(classOf[BigInteger], value)
  }

  /**
    * A uniform distribution over double values
    *
    * @param min the lower bound for sample values
    * @param max the upper bound for sample values
    *
    * @return a new distribution object
    *
    * @see [[se.sics.kompics.simulator.adaptor.distributions.DoubleUniformDistribution]]
    */
  def uniform(min: Double, max: Double)(implicit random: Random): Distribution[java.lang.Double] = {
    new DoubleUniformDistribution(min, max, random)
  }

  /**
    * A uniform distribution over long values
    *
    * @param min the lower bound for sample values
    * @param max the upper bound for sample values
    *
    * @return a new distribution object
    *
    * @see [[se.sics.kompics.simulator.adaptor.distributions.LongUniformDistribution]]
    */
  def uniform(min: Long, max: Long)(implicit random: Random): Distribution[java.lang.Long] = {
    new LongUniformDistribution(min, max, random)
  }

  /**
    * A uniform distribution over duration values
    *
    * Is translated internally to a long distribution of millisecond values.
    *
    * @param min the lower bound for sample values
    * @param max the upper bound for sample values
    *
    * @return a new distribution object
    *
    * @see [[se.sics.kompics.simulator.adaptor.distributions.LongUniformDistribution]]
    */
  def uniform(min: Duration, max: Duration)(implicit random: Random): Distribution[java.lang.Long] = {
    new LongUniformDistribution(min.toMillis, max.toMillis, random)
  }

  /**
    * A uniform distribution over big integer values
    *
    * @param min the lower bound for sample values
    * @param max the upper bound for sample values
    *
    * @return a new distribution object
    *
    * @see [[se.sics.kompics.simulator.adaptor.distributions.BigIntegerUniformDistribution]]
    */
  def uniform(min: BigInteger, max: BigInteger)(implicit random: Random): Distribution[BigInteger] = {
    new BigIntegerUniformDistribution(min, max, random)
  }

  /**
    * A uniform distribution over big integer values
    *
    * @param numBits the number of random bits to generate for each sample
    *
    * @return a new distribution object
    *
    * @see [[se.sics.kompics.simulator.adaptor.distributions.BigIntegerUniformDistribution]]
    */
  def uniform(numBits: Int)(implicit random: Random): Distribution[BigInteger] = {
    new BigIntegerUniformDistribution(numBits, random)
  }

  /**
    * An exponential distribution over double values
    *
    * @param mean the mean sample value
    *
    * @return a new distribution object
    *
    * @see [[se.sics.kompics.simulator.adaptor.distributions.DoubleExponentialDistribution]]
    */
  def exponential(mean: Double)(implicit random: Random): Distribution[java.lang.Double] = {
    new DoubleExponentialDistribution(mean, random)
  }

  /**
    * An exponential distribution over long values
    *
    * @param mean the mean sample value
    *
    * @return a new distribution object
    *
    * @see [[se.sics.kompics.simulator.adaptor.distributions.LongExponentialDistribution]]
    */
  def exponential(mean: Long)(implicit random: Random): Distribution[java.lang.Long] = {
    new LongExponentialDistribution(mean, random)
  }

  /**
    * An exponential distribution over duration values
    *
    * Is translated internally to a long distribution of millisecond values.
    *
    * @param mean the mean sample value
    *
    * @return a new distribution object
    *
    * @see [[se.sics.kompics.simulator.adaptor.distributions.LongExponentialDistribution]]
    */
  def exponential(mean: Duration)(implicit random: Random): Distribution[java.lang.Long] = {
    new LongExponentialDistribution(mean.toMillis, random)
  }

  /**
    * An exponential distribution over big integer values
    *
    * @param mean the mean sample value
    *
    * @return a new distribution object
    *
    * @see [[se.sics.kompics.simulator.adaptor.distributions.BigIntegerExponentialDistribution]]
    */
  def exponential(mean: BigInteger)(implicit random: Random): Distribution[BigInteger] = {
    new BigIntegerExponentialDistribution(mean, random)
  }

  /**
    * A normal distribution over double values
    *
    * @param mean the mean sample value
    * @param variance the variance of sampled values
    *
    * @return a new distribution object
    *
    * @see [[se.sics.kompics.simulator.adaptor.distributions.DoubleNormalDistribution]]
    */
  def normal(mean: Double, variance: Double)(implicit random: Random): Distribution[java.lang.Double] = {
    new DoubleNormalDistribution(mean, variance, random)
  }

  /**
    * A normal distribution over long values
    *
    * @param mean the mean sample value
    * @param variance the variance of sampled values
    *
    * @return a new distribution object
    *
    * @see [[se.sics.kompics.simulator.adaptor.distributions.LongNormalDistribution]]
    */
  def normal(mean: Long, variance: Long)(implicit random: Random): Distribution[java.lang.Long] = {
    new LongNormalDistribution(mean, variance, random)
  }

  /**
    * A normal distribution over duration values
    *
    * Is translated internally to a long distribution of millisecond values.
    *
    * @param mean the mean sample value
    * @param variance the variance of sampled values
    *
    * @return a new distribution object
    *
    * @see [[se.sics.kompics.simulator.adaptor.distributions.LongNormalDistribution]]
    */
  def normal(mean: Duration, variance: Long)(implicit random: Random): Distribution[java.lang.Long] = {
    new LongNormalDistribution(mean.toMillis, variance, random)
  }

  /**
    * A normal distribution over big integer values
    *
    * @param mean the mean sample value
    * @param variance the variance of sampled values
    *
    * @return a new distribution object
    *
    * @see [[se.sics.kompics.simulator.adaptor.distributions.BigIntegerNormalDistribution]]
    */
  def normal(mean: BigInteger, variance: BigInteger)(implicit random: Random): Distribution[BigInteger] = {
    new BigIntegerNormalDistribution(mean, variance, random)
  }

  /**
    * A concrete sequence where sampling always returns the next value
    *
    * @param range the range of integers to sample from
    * @return a new distribution object
    *
    * @see [[se.sics.kompics.simulator.adaptor.distributions.extra.GenIntSequentialDistribution]]
    */
  def intSeq(range: Range): Distribution[Integer] = {
    new GenIntSequentialDistribution(range.toArray.map(_.asInstanceOf[Integer]));
  }

  /**
    * A natural sequence where sampling always returns the next value
    *
    * The first returned value is `start` and then it's always `previous+1`
    *
    * @param start the beginning of the sequence
    * @return a new distribution object
    *
    * @see [[se.sics.kompics.simulator.adaptor.distributions.extra.BasicIntSequentialDistribution]]
    */
  def intSeq(start: Int): Distribution[Integer] = {
    new BasicIntSequentialDistribution(start);
  }
}
