package jatran.core;

import jatran.antlr.JavaTokenTypes;
import antlr.collections.AST;

public class AS2Printer extends SourcePrinter implements JavaTokenTypes {
	// The top of the tree looks like this:
	//  ROOT_ID  "Whatever.java"
	//   package
	//   imports
	//   class definition
	//print(getChild(ast, PACKAGE_DEF));
	// either print a class def or an interface definition
	@Override protected void printRoot(final AST ast) {
		String header = "";
		out.print(header);

		printImports(ast);

		br();
		br();

		print(getChild(ast, CLASS_DEF));
		print(getChild(ast, INTERFACE_DEF));
		br();
	}

	// import has exactly one child
	@Override protected void printImport(final AST ast) {
		print("import ");
		print(ast.getFirstChild());
		printEmptyStatement();
		br();
	}

	@Override protected void printDefinition(final AST ast, final AST parent) {
		print(getChild(ast, MODIFIERS));
		if (ast.getType() == CLASS_DEF)
			print("class ");
		else
			print("interface ");

		try {
			print(getChild(parent, PACKAGE_DEF).getFirstChild());
			print(".");
		} catch(Exception e) {
		}

		print(getChild(ast, IDENT));

		print(" ");
		print(getChild(ast, EXTENDS_CLAUSE));
		print(getChild(ast, IMPLEMENTS_CLAUSE));

		startBlock();
		print(getChild(ast, OBJBLOCK));
		endBlock();
	}

	// the typical order of things within the output is:
	//
	// ctor
	// methods
	// static initialization block
	// instance initialization block
	// variable definitions
	// inner classes
	@Override protected void printObjectBlock(final AST ast) {
		if (printChildren(ast, "\n",  CTOR_DEF))
			br();
		if (printChildren(ast, "\n",  METHOD_DEF))
			br();
		if (printChildren(ast, "\n",  STATIC_INIT))
			br();
		if (printChildren(ast, "\n",  INSTANCE_INIT))
			br();
		if (printChildren(ast, "\n",  VARIABLE_DEF))
			br();

		printChildren(ast, "\n",  CLASS_DEF);
	}

	//TODO: anonymous functions and function asst.
	//out("// the next method throws the following errors: ");
	//print(getChild(ast, LITERAL_throws));
	@Override protected void printMethodDefinition(final AST ast) {
		print(getChild(ast, MODIFIERS));

		print("function ");

		AST ident = getChild(ast, IDENT);
		print(ident);

		print(getChild(ast, PARAMETERS));

		if (ast.getType() != CTOR_DEF) {
			print(":");
			print(getChild(ast, TYPE));
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
		//out.println();
		print(getChild(ast, MODIFIERS));
		print(getChild(ast, IDENT));
		print(":");
		print(getChild(ast, TYPE));
	}

	@Override protected void printVariableDef(final AST ast, final AST parent) {
		print(getChild(ast, MODIFIERS));
		print("var ");
		print(getChild(ast, IDENT));
		print(":");
		print(getChild(ast, TYPE));
		print(getChild(ast, ASSIGN));
		// don't always suffix with ';': example: "for (int i=0; i<l; i++)" the semi after the
		// 0 is put there by the FOR rule, not the variable_def rule.
		printSemi(parent);
		if (parent!= null && parent.getType() == OBJBLOCK)
			printEmptyStatement();
	}

	@Override protected void printImplementsClause(final AST ast) {
		if (hasChildren(ast)) {
			print("implements ");
			printExpressionList(ast);
			print(" ");
		}
	}

	@Override protected void printExtendsClause(final AST ast) {
		if (hasChildren(ast)) {
			print("extends ");
			printExpressionList(ast);
			print(" ");
		}
	}

	@Override protected void printClassLiteral() {
		print("class");
	}

	@Override protected void printSuperConstructorCall() {
		//TODO: fix super ctor call
		print("super(); //TODO: fix this");
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

		print(child2);	// the "then" clause is an SLIST

		if (child3 != null) {
			print("else ");
			print(child3);	// optional "else" clause: an SLIST
		}
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
		print("default:");
		print(child1);	// an EXPR
	}

	@Override protected void printForLoop(final AST ast) {
		print("for (");
		print(getChild(ast, FOR_INIT));
		print("; ");
		print(getChild(ast, FOR_CONDITION));
		print("; ");
		print(getChild(ast, FOR_ITERATOR));
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
		print(";");	// empty statement
	}


	@Override protected void printArrayDeclarator(final AST ast) {
		//TODO: parse out array declr
		if (ast == null) {		// I'm not sure what this case is :(
			//out("[]");
		} else if (ast.getType() == EXPR) {
			print("[");
			print(ast);
			print("]");
		} else
			print("Array");	// we prefer "int[] x" to "int x[]"
	}

	@Override protected void printArrayInitialization(final AST ast) {
		//comment out array initializations for now
		//out.println("// -- [array init commented out of code] // ");

		print("[");
		printExpressionList(ast);
		print("]");
	}

	@Override protected void printIndexOperator(final AST child1, final AST child2) {
		print(child1);		// an IDENT
		print("[");
		print(child2);	// an EXPR
		print("]");
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

		print(type);
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
			print("*");
	}

	@Override protected void printStaticInit(final AST child1) {
		print("static ");
		printInstanceInit(child1);
	}

	@Override protected void printTypeCast(final AST child1, final AST child2) {
		print(child1);
		print("(");
		print(child2);
		print(") ");
	}

	@Override protected void printInstanceInit(final AST child1) {
		startBlock();
		print(child1);
		endBlock();
	}

	@Override protected void printReturn(final AST child1) {
		print("return ");
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
		if (child2.getType() != ARRAY_DECLARATOR)
			print("(");
		print(child2);
		if (child2.getType() != ARRAY_DECLARATOR)
			print(")");
		// "new String[] {...}": the stuff in {} is child3
		if (child3 != null) {
			print(" ");
			print(child3);
		}
	}

	@Override protected void setupTokenNames() {
		if (null != TOKEN_NAMES)
			return;
		super.setupTokenNames();
		TOKEN_NAMES[ABSTRACT]="";
		TOKEN_NAMES[LITERAL_package]="package";
		TOKEN_NAMES[LITERAL_import]="import";
		TOKEN_NAMES[LITERAL_void]="Void";
		TOKEN_NAMES[LITERAL_boolean]="Boolean";
		TOKEN_NAMES[LITERAL_byte]="Object";
		TOKEN_NAMES[LITERAL_char]="String";
		TOKEN_NAMES[LITERAL_short]="Number";
		TOKEN_NAMES[LITERAL_int]="Number";
		TOKEN_NAMES[LITERAL_float]="Number";
		TOKEN_NAMES[LITERAL_long]="Number";
		TOKEN_NAMES[LITERAL_double]="Number";
		TOKEN_NAMES[LITERAL_private]="private";
		TOKEN_NAMES[LITERAL_public]="public";
		TOKEN_NAMES[LITERAL_protected]="private /*protected*/";
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
		TOKEN_NAMES[LITERAL_break]="break";
		TOKEN_NAMES[LITERAL_continue]="continue";
		TOKEN_NAMES[LITERAL_return]="return";
		TOKEN_NAMES[LITERAL_switch]="switch";
		TOKEN_NAMES[LITERAL_throw]="throw";
		TOKEN_NAMES[LITERAL_case]="case";
		TOKEN_NAMES[LITERAL_default]="default";
		TOKEN_NAMES[LITERAL_try]="try";
		TOKEN_NAMES[LITERAL_finally]="finally";
		TOKEN_NAMES[LITERAL_catch]="catch";
		TOKEN_NAMES[LITERAL_instanceof]="instanceof";
		TOKEN_NAMES[LITERAL_this]="this";
		TOKEN_NAMES[LITERAL_super]="super";
		TOKEN_NAMES[LITERAL_true]="true";
		TOKEN_NAMES[LITERAL_false]="false";
		TOKEN_NAMES[LITERAL_null]="null";
		TOKEN_NAMES[LITERAL_new]="new";
	}
}
