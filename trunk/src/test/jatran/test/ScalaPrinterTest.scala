package jatran.test

import java.io.File
import junitx.framework.FileAssert.{assertBinaryEquals => be}
import org.testng.annotations._
import scalax.io.Implicits._

import org.scalatest.testng.TestNGSuite

import jatran.main.Main

class ScalaPrinterTest extends  TestNGSuite {
  val stub = new File("src/stub/jatran/stub")
  val gstub =  new File("tmp/generated/jatran/stub")
  
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
    val name = "Foo.scala"
    val f = stub / name
    val o = gstub / name
    
    assert(f.isFile)
    assert(o.isFile)
    
    be(f, o)
  }
  
  @Test
  def testStaticMembersAreChangedToMembersOfCompanionObject {
    val name = "StaticMembersToCompanionObject.scala"
    val f = stub / name
    val o = gstub / name
    assert(f.isFile)
    assert(o.isFile)
    
    be(f, o)
  }

}
