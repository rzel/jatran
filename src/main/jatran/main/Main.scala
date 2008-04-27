package jatran.main

import java.io._
import scala.io._
import RichFile._

import antlr.ASTFactory
import antlr.collections.AST

import jatran.core.ScalaPrinter
import jatran.core.SourcePrinter

import jatran.lexing.JavaLexer
import jatran.lexing.JavaRecognizer

/**
 * @author eokyere
 */
object Main {
  def main(args:Array[String]) {
    val test = new File("src/main/jatran/core")

    parse(test, "jatran-out", false)
  }
	
  
  def parse(src:File, out:String, untyped:Boolean) {
    for (f <- src.flatten; if f.name.endsWith(".java") && 5 <= f.name.length) {
        val i = new BufferedReader(new FileReader(f))

        val lexer = new JavaLexer(i)
        lexer.setFilename(f.name)

        val parser = new JavaRecognizer(lexer)
        parser.setFilename(f.name)

        val root = new ASTFactory().create(SourcePrinter.ROOT_ID,"AST ROOT")
        parser.compilationUnit()
        root.setFirstChild(parser.getAST())

        val pkg = packageName(f)
        val folder = new File(out + File.separator + pkg.replace(".", File.separator))
        folder.mkdirs()
        val fname = folder.getAbsolutePath() + File.separator + getClassName(f) + ".scala"
    
        val fl = new File(fname)                        
        if (fl.exists())
          fl.delete()
    
        new ScalaPrinter().print(root, new PrintStream(new FileOutputStream(fname)), untyped)
    }
  }
  
  def packageName(file:File):String = {
    var s = ""
    val e = new Exception()
  
    try {
      file.lines.foreach {line => 
        if (line != null && line.trim().startsWith("package")) {
          val len = line.trim().length()
          s = line.trim().substring(7, len - 1).trim()
          throw(e); // trick to break out of eachLine loop; break doesn't work; and we have only one package
        }
      }
    } catch {
      case _ =>
    }

    s
  }

  def getClassName(file:File):String = {
    val len = file.name.lastIndexOf('.')
    file.name.substring(0, len)
  }
}


class RichFile(file: File) {
  def name = file.getName()
  def lines = scala.io.Source.fromFile(file).getLines
  
  def children = new Iterable[File] {
    def elements = if (file.isDirectory) file.listFiles.elements else Iterator.empty
  }
  
  def flatten : Iterable[File] = 
    (Seq.single(file) ++ children.flatMap(child => new RichFile(child).flatten))
}

object RichFile {
  implicit def toRichFile(file: File) = new RichFile(file)
}

