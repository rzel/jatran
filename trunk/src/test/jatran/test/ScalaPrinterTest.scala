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
    Main.transform(test, "tmp/generated", false)
  }
  
  
  @Test def mynewTest {
    val t = true
    assert(t)
  }
  
  @Test def fooIsAFile {
    test("Foo.scala")
  }
  
  @Test def staticMembersAreChangedToMembersOfCompanionObject {
    test("StaticMembersToCompanionObject.scala")
  }

  @Test def forLoopsChangedToWhileLoopEquivalents {
    test("ForLoopsChangeToWhileLoops.scala")
  }
  
  @Test def helloWorldApplication {
    test("HelloWorldApplication.scala")
  }
  
  @Test def extensionWithCtorsNoInstMembersAndAStaticMember {
    test("ExtWithCtorsNoInstMembersAndAStaticMember.scala")
  }
  
  @Test def testDiffFileClass {
    test("FileDiff.scala")
  }
  
  @Test def literalClassToClassOf {
    test("LiteralClassToClassOf.scala")
  }
  
  @Test def upperBoundFormalTypeParameter {
    test("FooUpperBoundFTP.scala")
  }
  
  @Test def multipleImplementedInterfaces {
    test("FooImplMultipleInterfaces.scala")
  }
  
  @Test def aFairlyComplexClassImplementingAStrutsAction {
    test("AStrutsAction.scala")
  }
  
  @Test def tryCatchFinallyTransformations {
    test("TryCatchPatterns.scala")
  }
  
  @Test def whenToPrintReturnStatement {
    test("ReturnStatementPatterns.scala")
  }
  
  def test(name:String) {
    val f = stub/name
    val o = gstub/name
    
    assert(f.isFile)
    assert(o.isFile)
    
    val diff = new FileDiff(f, o, false) //diff the files and discard newlines
    assert(0 == diff.diffs.size())
  }
}
