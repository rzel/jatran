package jatran.test

import java.io.File
import junitx.framework.FileAssert.{assertBinaryEquals => be}
import org.testng.annotations._
import scalax.io.Implicits._

import org.scalatest.testng.TestNGSuite

import jatran.main.Main

import org.incava.util.diff._

class ScalaPrinterTest extends  TestNGSuite {
  val stub = new File("src/stub/jatran/stub")
  val gstub =  new File("tmp/generated/jatran/stub")
  
  @BeforeSuite
  def generateStubs {
    val test = new File("src/stub")
    Main.parse(test, "tmp/generated", false)
  }
  
  
  @Test 
  def mynewTest {
    val t = true
    assert(t)
  }
  
  @Test
  def fooIsAFile {
    test("Foo.scala")
  }
  
  @Test
  def testStaticMembersAreChangedToMembersOfCompanionObject {
    test("StaticMembersToCompanionObject.scala")
  }

  @Test
  def testForLoopsChangedToWhileLoopEquivalents {
    test("ForLoopsChangeToWhileLoops.scala")
  }
  
  @Test
  def testHelloWorldApplication {
    test("HelloWorldApplication.scala")
  }
  
  @Test
  def testExtWithCtorsNoInstMembersAndAStaticMember {
    test("ExtWithCtorsNoInstMembersAndAStaticMember.scala")
  }
  
  def test(name:String) {
    val f = stub/name
    val o = gstub/name
    
    assert(f.isFile)
    assert(o.isFile)
    
    val diff = new FileDiff(f, o)
    be(f, o)
  }
}
