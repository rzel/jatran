package jatran.core;

import antlr.collections.AST;

/**
 * 1. ctor must be public
 *
 *
 * @author eokyere
 */
public class AS3Printer extends AS2Printer {
	@Override protected void printRoot(final AST ast) {
		String header = "";
		out.print(header);

		br();

		print(getChild(ast, CLASS_DEF));
		print(getChild(ast, INTERFACE_DEF));

		br();
	}

	/**
	 * We defer printing package and import statements till we are in
	 * the class definition level, so we can easily surround the class
	 * with the proper package block, and add imports in the right location
	 */
	@Override protected void printDefinition(final AST ast, final AST parent) {
		try {
			print("package ");
			print(getChild(parent, PACKAGE_DEF).getFirstChild());
			print(" ");
		} catch(Exception e) {
		}

		startBlock();

		printImports(parent);

		br();

		print(getChild(ast, MODIFIERS));

		print(ast.getType() == CLASS_DEF ? "class " : "interface ");
		print(getChild(ast, IDENT));
		print(" ");
		print(getChild(ast, EXTENDS_CLAUSE));
		print(getChild(ast, IMPLEMENTS_CLAUSE));

		startBlock();

		print(getChild(ast, OBJBLOCK));

		endBlock(); //object block
		br();
		endBlock(); //package block
	}

	@Override protected void setupTokenNames() {
		if (null != TOKEN_NAMES)
			return;
		super.setupTokenNames();
		TOKEN_NAMES[ABSTRACT]="";
		TOKEN_NAMES[LITERAL_void]="void";
		TOKEN_NAMES[LITERAL_boolean]="Boolean";
		TOKEN_NAMES[LITERAL_byte]="Object";
		TOKEN_NAMES[LITERAL_char]="String";
		TOKEN_NAMES[LITERAL_short]="Number";
		TOKEN_NAMES[LITERAL_int]="int";
		TOKEN_NAMES[LITERAL_float]="Number";
		TOKEN_NAMES[LITERAL_long]="Number";
		TOKEN_NAMES[LITERAL_double]="Number";
		TOKEN_NAMES[LITERAL_protected]="protected";
		TOKEN_NAMES[LITERAL_transient]="";
		TOKEN_NAMES[LITERAL_native]="";
		TOKEN_NAMES[LITERAL_threadsafe]="";
		TOKEN_NAMES[LITERAL_synchronized]="";
		TOKEN_NAMES[LITERAL_volatile]="";
		TOKEN_NAMES[LITERAL_instanceof]="instanceof";
	}
}
