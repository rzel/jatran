package jatran.test

import java.io._
import junitx.framework.FileAssert._

import org.testng.annotations._
import org.scalatest.testng.TestNGSuite

import jatran.main.Main

class ScalaPrinterTest extends  TestNGSuite {
  @BeforeSuite
  def generateStubs {
    val test = new File("src/stub")
    println("generating before suit")
    Main.parse(test, "tmp/generated", false)
  }
  
  
  @Test
  def mynewTest {
    val t = true
    assert(t)
  }
  
  @Test
  def fooIsAFile {
    val f = new File("src/stub/jatran/stub/Foo.scala")
    val o = new File("tmp/generated/jatran/stub/Foo.scala")
    
    assert(f.isFile)
    assert(o.isFile)
    
    assertBinaryEquals(f, o)
  }

}
