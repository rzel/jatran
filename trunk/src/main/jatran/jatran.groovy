package jatran;

import antlr.ASTFactory
import antlr.collections.AST

import jatran.core.ScalaPrinter
import jatran.core.SourcePrinter

import jatran.antlr.JavaLexer
import jatran.antlr.JavaRecognizer

def cli = new CliBuilder(usage: "jatran [args] -i src-path")

//TODO: add no-semis option

cli.i(argName:"path", longOpt:"input", args:1, required:false, "src file or folder to transform")
cli.o(argName:"path", longOpt:"output", args:1, required:false, "output folder; defaults to jatran-out under current dir")
cli.u(argName:"untyped", longOpt:"untyped", args:0, required:false, "strips variable typing")
cli.h(longOpt:"help", "this message")

def options = cli.parse(args)

if (!options || !options.i || options.h) {
	println "Java Transformer Version 0.0.1 2006-2008"
	cli.usage()
	return
}

def f    = new File(options.i)
def out  = !options.o ? "jatran-out" : options.o
def untyped = options.u ? true : false
		
parse(f, out, untyped)

def parse(src, out, untyped) {
    if (src.isDirectory()) {
	    try {
			src.eachFileRecurse({file -> parseFile(file, out, untyped)})
	    } catch (Exception e) {
	    	println e
	    }
    } else {
    	try {
    		parseFile(src, out, untyped)
    	} catch (Exception e) {
    		println "exception while parsing: ${e}"
    	}
    }
}

def parseFile(file, out, untyped) {
	if (file.name.endsWith(".java") && 5 <= file.name.length()) {
		try {
			input = new BufferedReader(new FileReader(file))

			JavaLexer lexer = new JavaLexer(input)
			lexer.setFilename(file.name)

			JavaRecognizer parser = new JavaRecognizer(lexer)
			parser.setFilename(file.name)
			parser.compilationUnit()

			factory = new ASTFactory()
			AST root = factory.create(SourcePrinter.ROOT_ID,"AST ROOT")
			root.setFirstChild(parser.getAST())

			printer = new ScalaPrinter()

			pkg = getPackageName(file)
		    File f = new File(out + File.separator + pkg.replace(".", File.separator))
		    
			f.mkdirs()
		    
		    fname = f.getAbsolutePath() + File.separator + getClassName(file) + ".scala"
		    
		    File fl = new File(fname)			
			if (fl.exists())
				fl.delete();
		    
			out = new PrintStream(new FileOutputStream(fname))
			
			printer.print(root, out, untyped)
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
