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

import org.scalatest._
import se.sics.kompics.config.TypesafeConfig
import se.sics.kompics.config.Conversions

class ConfigTestSuite extends fixture.FlatSpec with Matchers {

  case class FixtureParam(conf: Config)

  def withFixture(test: OneArgTest) = {
    val conf = new Config(TypesafeConfig.load());
    withFixture(test.toNoArgTest(FixtureParam(conf)));
  }

  "Config" should "read string values" in { f =>
    val conf = f.conf;
    val oval = conf.original.readValue[String]("config.test");
    oval.isPresent() shouldBe true
    oval.get shouldBe "testValue"
    val oval2 = conf.original.getValue("config.test", classOf[String]);
    oval2 shouldBe "testValue"
    // scala API
    val sval = conf.readValue[String]("config.test");
    sval shouldBe Some("testValue");
    val sval2 = conf.getValue[String]("config.test");
    sval2 shouldBe "testValue";
  }

  it should "read boolean values" in { f =>
    val conf = f.conf;
    val oval = conf.original.readValue[Boolean]("config.testb");
    oval.isPresent() shouldBe true
    oval.get shouldBe true
    val oval2 = conf.original.getValue("config.testb", classOf[Boolean]);
    oval2 shouldBe true
    // scala API
    val bval = conf.readValue[Boolean]("config.testb");
    bval shouldBe Some(true);
    val bval2 = conf.getValue[Boolean]("config.testb");
    bval2 shouldBe true;
  }

  it should "read integer values" in { f =>
    val conf = f.conf;
    println(Conversions.asString);
    // *** LONG ***
    // java API
    val oval = conf.original.readValue[Long]("config.testl");
    oval.isPresent() shouldBe true
    oval.get shouldBe 5l
    val oval2 = conf.original.getValue("config.testl", classOf[Long]);
    oval2 shouldBe 5l
    // scala API
    val lval = conf.readValue[Long]("config.testl");
    lval shouldBe Some(5l);
    val lval2 = conf.getValue[Long]("config.testl");
    //lval2 shouldBe a [Long];
    lval2 shouldBe 5l;
    // *** INT ***
    val ival = conf.readValue[Int]("config.testl");
    ival shouldBe Some(5);
    val ival2 = conf.getValue[Int]("config.testl");
    ival2 shouldBe 5;
    // *** SHORT ***
    val sval = conf.readValue[Short]("config.testl");
    sval shouldBe Some(5.toShort);
    val sval2 = conf.getValue[Short]("config.testl");
    sval2 shouldBe 5.toShort;
    // *** BYTE ***
    val bval = conf.readValue[Byte]("config.testl");
    bval shouldBe Some(5.toByte);
    val bval2 = conf.getValue[Byte]("config.testl");
    bval2 shouldBe 5.toByte;
    // *** CHAR ***
    val cval = conf.readValue[Char]("config.testl");
    cval shouldBe Some(5.toChar);
    val cval2 = conf.getValue[Char]("config.testl");
    cval2 shouldBe 5.toChar;
  }

  it should "read floating point values" in { f =>
    val conf = f.conf;
    // *** DOUBLE ***
    // java API
    val oval = conf.original.readValue[Double]("config.testl");
    oval.isPresent() shouldBe true
    oval.get shouldBe 5.0
    val oval2 = conf.original.getValue("config.testl", classOf[Double]);
    oval2 shouldBe 5.0
    // scala API
    val dval = conf.readValue[Double]("config.testl");
    dval shouldBe Some(5.0);
    val dval2 = conf.getValue[Double]("config.testl");
    dval2 shouldBe 5.0;
    // *** FLOAT ***
    val fval = conf.readValue[Float]("config.testl");
    fval shouldBe Some(5.0f);
    val fval2 = conf.getValue[Float]("config.testl");
    fval2 shouldBe 5.0f;
  }

}
