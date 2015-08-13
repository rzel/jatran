A source to source transformer from Java to Scala, jatran takes an ANTLR Java 1.5 AST and produce reasonably formatted code from it, for another language.

The current drop includes implementations for Scala, as well as Actionscript 2 & 3

Typically, the AST node that you pass would be the root of a tree - the ROOT\_ID node that represents a Java compilation unit.

The Project is based on Andy Tripp's Java Emitter