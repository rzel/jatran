package jatran.core;

import java.util.ArrayList;
import java.util.List;

import antlr.collections.AST;

/**
 * NOTES:
 * 3. Move the primary constructor of class X extends Foo { ... } up.
 * 	  This yields a class X(arg1:T1,...,argsN:TN) extends Foo(...) { ... }.
 *    If you have several constructors, choose one to be the primary one.
 * 6. While you're at it, replace field declarations T x; with value or variable
 *    declarations var x:T = _;. If you don't supply a default value, your class must
 *    become abstract
 * 7. If you made Java fields just to store constructor arguments, omit those and
 *    add val to your primary constructor argument class X(arg1:T1,...,val argJ: TJ, ... argsN:TN).
 *    Sorry this does not work with mutable fields (var).
 * 9. Another thing about type declarationg: T x[] and T[] x both become x: Array[T].
 *    If you also want to initialize an array, turn T x[] = { y1,...,yN } into val x = Predef.Array[T](y1,...,yN).
 * 10. Turn all for loops into while loops. You could also use for-loops, but it takes to long to write.
 *     Pay attention to not forget the increment, decrement operations, which you of course have to move
 *     to the end of the block. for(int i = ...; test; inc) {body} becomes val i = ...; while(test) {body; inc}.
 *     Of course this pollutes the scope with variables (which in Java you might have reused in other loops),
 *     but those you can fix by renaming later.
 * 12. If you have static stuff in a class X, create an object X of the same name and
 *     move the translated thingies there (without the static modifier). Change access
 *     sites of a static method or field into qualified access ( i.e. foo becomes X.foo ).
 *     For most uses of static, this is enough, e.g. the main method.
 * 13. switch statements require more care. First turn case pat: into case pat =>.
 *     Now you should take some more involved measures: scala does not need a break,
 *     which is convenient, but an unmentioned default case will not be ignored by
 *     lead to a runtime error. If your switch does not have a default case, add one case _ =>
 *     (the right-hand side is empty.)
 * 14. remove all return statements. Those at the end you don't need, returns that are somewhere
 *     in the middle of your method body are either supported or unsupported, depending on whether
 *     they get translated via some syntactic quirks. Add exit flag variables in loops and stuff.
 *     The same holds for break statements in for/while loops. Get rid of those things.
 *
 * 15. You call super class constructor in class declaration itself.
 *
 * class Foo(a1:T1, a2:T2, ... an:Tn) extends Bar(a1, .., an) {
 * }
 *
 * References:
 * 		http://blogs.sun.com/sundararajan/entry/scala_for_java_programmers
 *		http://lamp.epfl.ch/~emir/bqbase/2005/01/21/java2scala.html
 * 		http://scala.sygneca.com/faqs/language
 *
 * @author eokyere
 */
public class ScalaPrinter extends SourcePrinter {
	@Override protected void printRoot(final AST ast) {
		String header = "";
		out.println(header);

		br();

		try {
			//first child is IDENT
			AST pkg = getChild(ast, PACKAGE_DEF).getFirstChild();
			if (!(null == pkg || pkg.getText().equals(""))) {
				print("package ");
				err.println("package type is: " + pkg.getType());
				err.print("package is: ");
				debug(pkg);
				if (pkg.getType() == ANNOTATIONS)
					pkg = getChild(pkg, DOT);
				print(pkg);
			}
		} catch(Exception e) {
		}

		br(2);

		printImports(ast);

		br(2);

		print(getChild(ast, CLASS_DEF));
		print(getChild(ast, INTERFACE_DEF));

		br();
	}

	@Override protected void printImport(final AST ast) {
		print("import ");
		print(ast.getFirstChild());
		printEmptyStatement();
		br();
	}


	/**
	 * We defer printing package and import statements till we are in
	 * the class definition level, so we can easily surround the class
	 * with the proper package block, and add imports in the right location
	 */
	@Override protected void printDefinition(final AST ast, final AST parent) {
		String deftype = ast.getType() == CLASS_DEF ? "class " : "trait ";

		AST obj = getChild(ast, OBJBLOCK);

		List<AST> methods = getChildren(obj, METHOD_DEF);
		List<AST> pureMethods = new ArrayList<AST>();
		List<AST> objMethods = new ArrayList<AST>();

		for (AST m : methods)
			if (hasStaticMod(m))
				objMethods.add(m);
			else
				pureMethods.add(m);

		List<AST> vars = getChildren(obj, VARIABLE_DEF);
		List<AST> pureVars = new ArrayList<AST>();
		List<AST> objVars = new ArrayList<AST>();

		for (AST v : vars)
			if (hasStaticMod(v))
				objVars.add(v);
			else
				pureVars.add(v);

		//TODO: output default ctr params here
		// print object block body
		List<AST> ctors = getChildren(obj, CTOR_DEF);

		print(deftype);
		print(getChild(ast, IDENT));
		print(" ");
		print(getChild(ast, EXTENDS_CLAUSE));
		print(getChild(ast, IMPLEMENTS_CLAUSE));

		startBlock();

		//printCtors();
		if(ctors.size() > 0) {
			print("/*");
			br();
			print(ctors);
			br();
			print("*/");
			br();
		}

		print(pureMethods);

		print (getChildren(obj, INSTANCE_INIT));
		print(pureVars);
		printChildren(obj, "\n",  CLASS_DEF);
		endBlock();



		if (objMethods.size() > 0 || objVars.size() > 0) {
			br(2);
			print("object ");
			print(getChild(ast, IDENT));
			print(" ");
			startBlock();
			print(getChildren(obj, STATIC_INIT));
			print(objMethods);
			print(objVars);
			endBlock();
		}
	}

	@Override protected void printExtendsClause(final AST ast) {
		if (hasChildren(ast)) {
			print("extends ");
			printExpressionList(ast);
			print(" ");
		}
	}


	/**
	 * instead of implements myInterface, write extends myInterface if its the only one.
	 * Write with i1 ... with iN if there are several (this is a oversimplified description,
	 * it might go wrong in some cases that I won't go into here.)
	 */
	@Override protected void printImplementsClause(final AST ast) {
		if (hasChildren(ast)) {
			print(2 <= ast.getNumberOfChildren() ? "with " : "extends ");
			printExpressionList(ast);
			print(" ");
		}
	}

	@Override protected void printCtorDefinition(final AST ast) {
	}

	//TODO: anonymous functions and function asst.
	//out("// the next method throws the following errors: ");
	//print(getChild(ast, LITERAL_throws));
	@Override protected void printMethodDefinition(final AST ast) {
		List<AST> modifiers = getChildren(getChild(ast, MODIFIERS));

		if (modifiers.size() > 0) {
			boolean t = false;
			for (AST m : modifiers)
				if (!(m.getType() == LITERAL_public ||
					  m.getType() == LITERAL_static ||
					  m.getType() == LITERAL_synchronized)){
					print(m);
					print(" ");
					t = true;
				}

			if (t)
				print(" ");
		}

		print("def ");

		AST ident = getChild(ast, IDENT);
		print(ident);

		print(getChild(ast, PARAMETERS));

		if (ast.getType() != CTOR_DEF) {
			print(":");
			print(getChild(ast, TYPE));
			print(" =");
			if (hasModifier(ast, LITERAL_synchronized))
				print(" synchronized");
		}


		//TODO see if type is void, and don't return anything in abstrat body

		AST body = getChild(ast, SLIST);
		if (null == body)
			print(" { // abstract\n\t return null; \n}\n");
		else {
			print(" ");
			print(body);
			br();
		}
	}

	@Override protected void printParameters(final AST ast) {
		print("(");
		printExpressionList(ast);
		print(")");
	}

	@Override protected void printParamDef(final AST ast) {
		print(getChild(ast, MODIFIERS));
		print(getChild(ast, IDENT));
		print(":");
		print(getChild(ast, TYPE));
	}

	@Override protected void printVariableDef(final AST ast, final AST parent) {
		List<AST> modifiers = getChildren(getChild(ast, MODIFIERS));

		if (modifiers.size() > 0) {
			boolean t = false;
			for (AST m : modifiers)
				if (!(m.getType() == LITERAL_public ||
					  m.getType() == LITERAL_static ||
					  m.getType() == FINAL)){
					print(m);
					print(" ");
					t = true;
				}

			if (t)
				print(" ");
		}

		print(isFinal(ast) ? "val " : "var ");

		print(getChild(ast, IDENT));
		print(":");
		print(getChild(ast, TYPE));

		AST assign = getChild(ast, ASSIGN);
		if (null == assign)
			print(" = _");
		else
			print(assign);

		printSemi(parent);

		if (parent!= null && parent.getType() == OBJBLOCK)
			printEmptyStatement();
	}

	@Override protected void printAnnotation(final AST ast) {
		AST ann = ast.getFirstChild();
		String txt = ann.getText();

		if(txt.equals("Override")) {
			print("override ");
			return;
		}

		print("@" + txt);

		List<AST> vps = getChildren(ast, ANNOTATION_MEMBER_VALUE_PAIR);

		if (vps.size() > 0) {
			print(vps.size() > 1 ? "{" : "(");
			print(vps.get(0));
			for (int i = 1; i < vps.size(); ++i) {
				print(", ");
				print(vps.get(i));
			}
			print(vps.size() > 1 ? "}" : ")");
		} else if (ann.getNextSibling() != null) {
			print("C");
			print(ann.getNextSibling());
			print(")");
		}
		br();
	}

	@Override protected void printForEach(final AST ast) {
		debug(ast);
	}


	@Override protected void printAnnotationMemberValuePair(final AST ast) {
		print(ast.getFirstChild());
		print(" = ");
		print(ast.getFirstChild().getNextSibling());
	}

	/**
	 * say goodbye to x++, change it to x and to x = x + 1;
	 */
	@Override protected void printPostAssignment(final AST ast, final AST child1) {
		String var = child1.getText();
		print(var + " = " + var + (ast.getText().equals("++") ? " + 1" : " - 1") );
	}

	@Override protected void printIncDec(final AST ast, final AST child1) {
		String var = child1.getText();
		print(var + " = " + var + (ast.getText().equals("++") ? " + 1" : " - 1") );
	}


	@Override protected void printExpressionList(final AST ast) {
		printChildren(ast, ", ");
	}


	@Override protected void printExpression(final AST parent, final AST child1) {
		print(child1);
		printSemi(parent);
	}

	@Override protected void printStatementList(final AST ast) {
		startBlock();

		if (printChildren(ast, "\n"))
			br();

		endBlock();
	}

	@Override protected void printIfStatement(final AST child1, final AST child2, final AST child3) {
		print("if (");
		print(child1);	// the "if" condition: an EXPR
		print(") ");

		startIndent(child2);
		print(child2);	// the "then" clause is an SLIST / RETURN or EXPR
		closeIndent(child2);

		if (child3 != null) {
			print(child2.getType() == SLIST ? " else " : "else ");
			startIndent(child3);
			print(child3);	// optional "else" clause: an SLIST
			closeIndent(child3);
		}
	}

	private void closeIndent(final AST ast) {
		if (indentable(ast)) {
			out.decreaseIndent();
			br();
		}
	}

	private void startIndent(final AST ast) {
		if (indentable(ast)) {
			br();
			out.increaseIndent();
		}
	}

	private boolean indentable(final AST ast) {
		return !(ast.getType() == SLIST || ast.getType() == LITERAL_if);
	}

	@Override protected void printSwitch(final AST ast, final AST child1) {
		print("switch (");
		print(child1);	// the EXPR to switch on
		print(") ");
		startBlock();
		printChildren(ast, "",  CASE_GROUP);
		endBlock();
	}

	@Override protected void printCaseGroup(final AST ast) {
		printChildren(ast, "\n",  LITERAL_case);
		printChildren(ast, "\n",  LITERAL_default);
		printChildren(ast, "",  SLIST);
	}

	@Override protected void printCaseExpression(final AST child1) {
		print("case ");
		print(child1);	// an EXPR
		print(":");
	}

	@Override protected void printDefaultCase(final AST child1) {
		print("case _ =>");
		print(child1);	// an EXPR
	}

	@Override protected void printForLoop(final AST ast) {
		print("for (");

		AST foreach = getChild(ast, FOR_EACH_CLAUSE);
		if (foreach != null)
			print(foreach);
		else {
			print(getChild(ast, FOR_INIT));
			print("; ");
			print(getChild(ast, FOR_CONDITION));
			print("; ");
			print(getChild(ast, FOR_ITERATOR));
		}
		print(") ");

		AST body = getChild(ast, SLIST);

		if (null == body)
			body = getChild(ast, EXPR);

		print(body);
	}

	@Override protected void printDoLoop(final AST child1, final AST child2) {
		print("do ");
		print(child1);		// an SLIST
		print(" while (");
		print(child2);		// an EXPR
		print(");");
	}

	@Override protected void printWhileLoop(final AST child1, final AST child2) {
		print("while (");
		print(child1);	// the "while" condition: an EXPR
		print(") ");
		print(child2);	// an SLIST
	}

	@Override protected void printContinueBreak(final AST ast) {
		printASTName(ast);
		printEmptyStatement();
	}

	@Override protected void printTry(final AST ast, final AST child1) {
		print("try ");
		print(child1);	// an SLIST
		printChildren(ast, " ", LITERAL_catch);
	}

	@Override protected void printCatch(final AST child1, final AST child2) {
		print("catch (");
		print(child1);	// a PARAMETER_DEF
		print(") ");
		print(child2);	// an SLIST
	}

	// the first child is the "try" and the second is the SLIST
	@Override protected void printFinally(final AST child1, final AST child2) {
		print(child1);
		print(" finally ");
		print(child2);	// an SLIST
	}

	@Override protected void printThrow(final AST child1) {
		print("throw ");
		print(child1);
		printEmptyStatement();
	}

	@Override protected void printEmptyStatement() {
		print("");	// empty statement
	}


	/**
	 * Another thing about type declaration: T x[] and T[] x both become x: Array[T].
	 */
	@Override protected void printArrayDeclarator(final AST ast) {
		if (ast == null)
			print("Array");
		else if (ast.getType() == EXPR) {
			print("[");
			print(ast);
			print("]");
		} else {
			print("Array[");
			print(ast);
			print("]");
		}
	}

	/**
	 * If you also want to initialize an array, turn T x[] = { y1,...,yN }
	 * into val x = Predef.Array[T](y1,...,yN).
	 */
	@Override protected void printArrayInitialization(final AST ast) {
		//TODO: figure out how to pull this into where it is actually used and blank method
		print("(");
		printExpressionList(ast);
		print(")");
	}

	@Override protected void printIndexOperator(final AST child1, final AST child2) {
		print(child1);		// an IDENT
		print("(");
		print(child2);	// an EXPR
		print(")");
	}

	// if we have two children, it's of the form "a=0"
	// if just one child, it's of the form "=0" (where the lhs is above this AST).
	@Override protected void printAssignment(final AST child1, final AST child2) {
		if (child2 != null) {
			print(child1);
			print(" = ");
			print(child2);
		} else {
			print(" = ");
			print(child1);
		}
	}

	// TYPE has exactly one child.
	@Override protected void printType(final AST ast) {
		AST type = ast.getFirstChild();
		if ("HashMap" == type.toString())
			type.setText("Object");

		AST typeargs = type.getNextSibling();
		print(type);
		print(typeargs);
	}

	//TODO: current!
	@Override protected void printTypeArguments(final List<AST> list) {
		print("[");
		for (AST t : list)
			print(t.getFirstChild());
		print("]");
	}

	@Override protected void printDot(final AST child1, final AST child2) {
		//always has exactly two children.
		print(child1);
		print(".");
		print(child2);
	}

	@Override protected void printModifiers(final AST ast) {
		if (hasChildren(ast)) {
			printChildren(ast, " ");
			print(" ");
		}
	}

	@Override protected void printTrinaryOp(final AST child1, final AST child2, final AST child3) {
		// the dreaded trinary operator
		print(child1);
		print(" ? ");
		print(child2);
		print(" : ");
		print(child3);
	}

	@Override protected void printThrows(final AST ast) {
		print("throws ");
		printExpressionList(ast);
	}

	@Override protected void printStar(final AST ast) {
		if (hasChildren(ast))
			printBinaryOperator(ast);
		else
			print("_");
	}

	@Override protected void printStaticInit(final AST child1) {
		print("static ");
		printInstanceInit(child1);
	}

	@Override protected void printTypeCast(final AST child1, final AST child2) {
		print(child2);
		print(".asInstanceof[");
		print(child1);
		print("] ");
	}

	@Override protected void printInstanceInit(final AST child1) {
		startBlock();
		print(child1);
		endBlock();
	}

	@Override protected void printReturn(final AST child1) {
		//output("return ");
		print(child1);
		printEmptyStatement();
	}

	@Override protected void printMethodCall(final AST child1, final AST child2) {
		print(child1);
		print("(");
		print(child2);
		print(")");
	}

	@Override protected void printUnary(final AST ast, final AST child1) {
		printASTName(ast);
		printWithParens(ast, child1);
	}

	@Override protected void printNew(final AST child1, final AST child2, final AST child3) {
		print("new ");
		print(child1);

		if (!(child2.getType() == ARRAY_DECLARATOR ||
			  child2.getType() == TYPE_ARGUMENTS))
			print("(");

		print(child2);

		if (!(child2.getType() == ARRAY_DECLARATOR ||
				  child2.getType() == TYPE_ARGUMENTS))
			print(")");
		// "new String[] {...}": the stuff in {} is child3
		if (child3 != null)
			if (child3.getType() == ELIST && child3.getNextSibling() == null)
				print("()");
			else {
				print(" ");
				print(child3);
			}
	}

	/**
	 * a instanceof b is a.isInstanceof[b] in scala
	 */
	@Override protected void printBinaryOperator(final AST ast) {
		boolean b = ast.getType() == LITERAL_instanceof;

		printWithParens(ast, ast.getFirstChild());
		print(b ? ".isInstanceof[" : " " + name(ast) + " ");
		printWithParens(ast, ast.getFirstChild().getNextSibling());

		if (b)
			print("]");
	}



	@Override protected void setupTokenNames() {
		if (null != TOKEN_NAMES)
			return;
		super.setupTokenNames();
		TOKEN_NAMES[ABSTRACT]="";
		TOKEN_NAMES[FINAL]="final";
		TOKEN_NAMES[LITERAL_package]="package";
		TOKEN_NAMES[LITERAL_import]="import";
		TOKEN_NAMES[LITERAL_void]="unit";
		TOKEN_NAMES[LITERAL_boolean]="boolean";
		TOKEN_NAMES[LITERAL_byte]="byte";
		TOKEN_NAMES[LITERAL_char]="char";
		TOKEN_NAMES[LITERAL_short]="short";
		TOKEN_NAMES[LITERAL_int]="int";
		TOKEN_NAMES[LITERAL_float]="float";
		TOKEN_NAMES[LITERAL_long]="long";
		TOKEN_NAMES[LITERAL_double]="double";
		TOKEN_NAMES[LITERAL_private]="private";
		TOKEN_NAMES[LITERAL_public]="public";
		TOKEN_NAMES[LITERAL_protected]="protected";
		TOKEN_NAMES[LITERAL_static]="static";
		TOKEN_NAMES[LITERAL_transient]="";
		TOKEN_NAMES[LITERAL_native]="";
		TOKEN_NAMES[LITERAL_threadsafe]="";
		TOKEN_NAMES[LITERAL_synchronized]="";
		TOKEN_NAMES[LITERAL_volatile]="";
		TOKEN_NAMES[LITERAL_class]="class";
		TOKEN_NAMES[LITERAL_extends]="extends";
		TOKEN_NAMES[LITERAL_interface]="interface";
		TOKEN_NAMES[LITERAL_implements]="implements";
		TOKEN_NAMES[LITERAL_throws]="throws";
		TOKEN_NAMES[LITERAL_if]="if";
		TOKEN_NAMES[LITERAL_else]="else";
		TOKEN_NAMES[LITERAL_for]="for";
		TOKEN_NAMES[LITERAL_while]="while";
		TOKEN_NAMES[LITERAL_do]="do";
		TOKEN_NAMES[LITERAL_break]="";
		TOKEN_NAMES[LITERAL_continue]="continue";
		TOKEN_NAMES[LITERAL_return]="return";
		TOKEN_NAMES[LITERAL_switch]="switch";
		TOKEN_NAMES[LITERAL_throw]="throw";
		TOKEN_NAMES[LITERAL_case]="case";
		TOKEN_NAMES[LITERAL_default]="default";
		TOKEN_NAMES[LITERAL_try]="try";
		TOKEN_NAMES[LITERAL_finally]="finally";
		TOKEN_NAMES[LITERAL_catch]="catch";
		TOKEN_NAMES[LITERAL_instanceof]="instanceOf";
		TOKEN_NAMES[LITERAL_this]="this";
		TOKEN_NAMES[LITERAL_super]="super";
		TOKEN_NAMES[LITERAL_true]="true";
		TOKEN_NAMES[LITERAL_false]="false";
		TOKEN_NAMES[LITERAL_null]="null";
		TOKEN_NAMES[LITERAL_new]="new";
	}

	private boolean hasStaticMod(final AST method) {
		return hasModifier(method, LITERAL_static);
	}

	private boolean isFinal(final AST ast) {
		return hasModifier(ast, FINAL);
	}

	private boolean hasModifier(final AST method, final int t) {
		AST modifiers = getChild(method, MODIFIERS);

		for (AST c : getChildren(modifiers))
			if(c.getType() == t)
				return true;
		return false;
	}

}
