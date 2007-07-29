package jatran;

import antlr.ASTFactory
import antlr.collections.AST

import jatran.core.AS2Printer
import jatran.core.AS3Printer
import jatran.core.ScalaPrinter
import jatran.core.SourcePrinter

import jatran.antlr.JavaLexer
import jatran.antlr.JavaRecognizer

def cli = new CliBuilder(usage: "jatran [args] -i src-path")

cli.i(argName:"path", longOpt:"input", args:1, required:false, "src file or folder to translate")
cli.o(argName:"path", longOpt:"output", args:1, required:false, "output folder; if this is not provided, defaults to jatran-out under current dir")
cli.l(argName:"lang", longOpt:"lang", args:1, required:false, "as2, as3, scala for actionscript version; defaults to as2")
cli.h(longOpt:"help", "this message")

def options = cli.parse(args)

if (!options || !options.i || options.h) {
	println "Java Transformer Version 0.0.1 2006-2007"
	cli.usage()
	return
}

def f    = new File(options.i)
def out  = !options.o ? "jatran-out" : options.o
def lang = !options.l ? "as2" : options.l

switch (lang) {
	case "as2":
	case "as3":
	case "scala":
		break;
	default:
		throw new Exception("language not suppoorted")
}		
		
parse(f, out, lang)

def parse(src, out, lang) {
    if (src.isDirectory()) {
	    try {
			src.eachFileRecurse({file -> parseFile(file, out, lang)})
	    } catch (Exception e) {
	    	println e
	    }
    } else {
    	try {
    		parseFile(src, out, lang)
    	} catch (Exception e) {
    		println "exception while parsing: ${e}"
    	}
    }
}

def parseFile(file, out, lang) {
	if (file.name.endsWith(".java") && 5 <= file.name.length()) {
		try {
			// Create a scanner that reads from the input stream passed to us
			input = new BufferedReader(new FileReader(file))
			JavaLexer lexer = new JavaLexer(input)
			lexer.setFilename(file.name)

			// Create a parser that reads from the scanner
			JavaRecognizer parser = new JavaRecognizer(lexer)
			parser.setFilename(file.name)

			// start parsing at the compilationUnit rule
			parser.compilationUnit()

			// Create a root AST node with id 0, and its child is the AST produced by the parser:
			factory = new ASTFactory()
			AST root = factory.create(SourcePrinter.ROOT_ID,"AST ROOT")
			root.setFirstChild(parser.getAST())

			printer = "as3".equals(lang) ? new AS3Printer() : "scala".equals(lang) ? new ScalaPrinter() : new AS2Printer()

			pkg = getPackageName(file)
		    File f = new File(out + File.separator + pkg.replace(".", File.separator))
		    
			f.mkdirs()
		    
		    fname = f.getAbsolutePath() + File.separator + getClassName(file) + ("scala".equals(lang) ? ".scala" : ".as")
		    
		    File fl = new File(fname)			
			if (fl.exists())
				fl.delete();
		    
			out = new PrintStream(new FileOutputStream(fname))
			
			printer.print(root, out)
		} catch (Exception e) {
			println "parser exception: ${e}"
			e.printStackTrace();		
		}
	}
}


def getFullClassName(file) {
    s = getPackageName(file)

    if (!s.trim().equals(""))
		s += "."
    
    s += getClassName(file)

    return s
}

def getPackageName(file) {
    s = ""
    e = new Exception()
  
    try {
		file.eachLine({line -> if (line != null && line.trim().startsWith("package")) {
		    len = line.trim().length()
		    s = line.trim().substring(7, len - 1).trim()
		    throw(e); // groovy trick to break out of eachLine loop; break doesn't work; and we have only one package
		}})
    } catch (Exception e) {
    }

    return s
}


def getClassName(file) {
   	len = file.name.lastIndexOf('.')
    return file.name.substring(0, len)
}
