//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.build.jniglue
//# CLASS:   GlueFileParser
//###########################################################################
//# $Id: GlueFileParser.java,v 1.1 2005-02-18 01:30:10 robi Exp $
//###########################################################################

package net.sourceforge.waters.build.jniglue;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;


class GlueFileParser extends ErrorReporter {

  //#########################################################################
  //# Constructors
  GlueFileParser(final File filename)
    throws FileNotFoundException
  {
    super(filename);
    final Reader reader = new FileReader(filename);
    mScanner = new GlueFileScanner(reader);
    mClasses = new TreeMap();
    mObjectClass = new ObjectClassGlue(this);
    final String name = mObjectClass.getClassName();
    mClasses.put(name, mObjectClass);
    mPackageName = null;
  }

  GlueFileParser(final Reader reader)
  {
    mScanner = new GlueFileScanner(reader);
    mClasses = new TreeMap();
    mObjectClass = new ObjectClassGlue(this);
    final String name = mObjectClass.getClassName();
    mClasses.put(name, mObjectClass);
    mPackageName = null;
  }


  //#########################################################################
  //# Simple Access
  int getLineNo()
  {
    return mScanner.getLineNo();
  }


  //#########################################################################
  //# Parsing
  ClassGlueCollection parse()
  {
    try {
      parseFile();
    } catch (final IOException exception) {
      reportError(exception);
    }
    if (getNumErrors() == 0) {
      final Collection values = mClasses.values();
      final ClassGlueCollection result = new ClassGlueCollection(values);
      return result;
    } else {
      return null;
    }
  }

  void close()
    throws IOException
  {
    mScanner.close();
  }


  //#########################################################################
  //# Auxiliary Methods for Block Parsing
  private void parseFile()
    throws IOException
  {
    Token token;
    do {
      try {
	token = parseBlock();
      } catch (final ParseException exception) {
	reportError(exception);
	token = recover();
      }
    } while (token.getTokenType() != TokenTable.C_EOF);
  }

  private Token parseBlock()
    throws IOException, ParseException
  {
    Token token = nextToken();
    switch (token.getTokenType()) {
    case TokenTable.C_PACKAGE:
      token = parsePackage();
      break;
    case TokenTable.C_CLASS:
      token = parseForwardClassGlue();
      break;
    case TokenTable.C_ENUM:
      token = parseEnumClassGlue();
      break;
    case TokenTable.C_ARG:
    case TokenTable.C_REF:
    case TokenTable.C_GLUE:
      token = parseClassGlue(token);
      break;
    case TokenTable.C_EOF:
      break;
    default:
      throw createUnexpectedTokenException(token, "package or class");
    }
    return token;
  }

  private Token parsePackage()
    throws IOException, ParseException
  {
    final StringBuffer buffer = new StringBuffer();
    Token token = nextTokenIdentifier();
    String name = token.getTokenText();
    buffer.append(name);
    token = nextToken();
    while (token.getTokenType() == TokenTable.C_DOT) {
      buffer.append('/');
      token = nextTokenIdentifier();
      name = token.getTokenText();
      buffer.append(name);
      token = nextToken();
    }
    requireToken(token, TokenTable.T_SEMICOLON);
    mPackageName = buffer.toString();
    return token;
  }

  private Token parseEnumClassGlue()
    throws IOException, ParseException
  {
    requirePackageName();
    nextTokenKnown(TokenTable.T_CLASS);
    Token token = nextTokenIdentifier();
    final String name = token.getTokenText();
    ClassGlue classglue = (ClassGlue) mClasses.get(name);
    if (classglue == null) {
      classglue = new EnumClassGlue(mPackageName, name, mObjectClass, this);
    } else {
      throw createDuplicateClassException(name);
    }
    nextTokenKnown(TokenTable.T_OPENBRACE);
    token = nextToken();
    if (token.getTokenType() != TokenTable.C_CLOSEBRACE) {
      final TypeGlue type = new ClassTypeGlue(classglue);
      while (true) {
	requireIdentifier(token);
	final String itemname = token.getTokenText();
	final FieldGlue field = new FieldGlue(type, itemname);
	if (!classglue.addField(field, this)) {
	  final ParseException exception = createParseException
	    ("Duplicate item " + field.getFieldName() +
	     " in enumeration " + classglue.getClassName() + "!");
	  reportError(exception);
	}
	token = nextToken();
	if (token.getTokenType() != TokenTable.C_COMMA) {
	  break;
	}
	token = nextToken();
      }
      requireToken(token, TokenTable.T_CLOSEBRACE);
    }
    mClasses.put(name, classglue);
    return token;
  }

  private Token parseClassGlue(Token token)
    throws IOException, ParseException
  {
    requirePackageName();
    final ClassModifierToken modtoken = (ClassModifierToken) token;
    final ClassModifier mod = modtoken.getClassModifier();
    nextTokenKnown(TokenTable.T_CLASS);
    token = nextTokenIdentifier();
    final String name = token.getTokenText();
    ClassGlue classglue = (ClassGlue) mClasses.get(name);
    if (classglue == null) {
      classglue =
	new PlainClassGlue(mPackageName, name, mObjectClass, mod, this);
      mClasses.put(name, classglue);
    } else if (classglue.getModifier() != null) {
      throw createDuplicateClassException(name);
    } else if (classglue.getNeedsGlue() && !mod.includesGlueHeaders()) {
      throw createClassNeedsGlueException(name);
    } else {
      classglue.setModifier(mod);
    }
    nextTokenKnown(TokenTable.T_OPENBRACE);
    do {
      try {
	token = parseMethod(classglue);
      } catch (final ParseException exception) {
	reportError(exception);
	token = recover(true);
      }
    } while (token.getTokenType() == TokenTable.C_SEMICOLON);
    requireToken(token, TokenTable.T_CLOSEBRACE);
    return token;
  }

  private Token parseForwardClassGlue()
    throws IOException, ParseException
  {
    requirePackageName();
    final Token token = nextTokenIdentifier();
    final String name = token.getTokenText();
    final Token semi = nextTokenKnown(TokenTable.T_SEMICOLON);
    if (mClasses.containsKey(name)) {
      throw createDuplicateClassException(name);
    }
    final ClassGlue classglue =
      new PlainClassGlue(mPackageName, name, mObjectClass, this);
    mClasses.put(name, classglue);
    return semi;
  }


  //#########################################################################
  //# Auxiliary Methods for Method Parsing
  private Token parseMethod(final ClassGlue classglue)
    throws IOException, ParseException
  {
    TypeGlue returntype;
    String methodname;
    List parameters;
    MethodGlue method;
    Token token = nextToken();
    if (token.getTokenType() == TokenTable.C_CLOSEBRACE) {
      return token;
    } else if (token.getTokenType() == TokenTable.C_IDENTIFIER &&
	       token.getTokenText().equals(classglue.getClassName())) {
      token = nextToken();
      switch (token.getTokenType()) {
      case TokenTable.C_OPENPAREN:
	parameters = getParameterList();
	method = new ConstructorGlue(parameters);
	break;
      case TokenTable.C_IDENTIFIER:
	returntype = new ClassTypeGlue(classglue);
	methodname = token.getTokenText();
	nextTokenKnown(TokenTable.T_OPENPAREN);
	parameters = getParameterList();
	method = new PlainMethodGlue(returntype, methodname, parameters);
	break;
      default:
	throw createUnexpectedTokenException(token, "identifier");
      }
    } else {
      returntype = getType(token, false);
      token = nextTokenIdentifier();
      methodname = token.getTokenText();
      nextTokenKnown(TokenTable.T_OPENPAREN);
      parameters = getParameterList();
      method = new PlainMethodGlue(returntype, methodname, parameters);
    }
    if (!classglue.addMethod(method, this)) {
      throw createParseException
	("Redefinition of method " + method.getMethodName() +
	 " with same parameter types!");
    }
    return nextTokenKnown(TokenTable.T_SEMICOLON);
  }

  private List getParameterList()
    throws IOException, ParseException
  {
    final List parameters = new LinkedList();
    Token token = nextToken();
    if (token.getTokenType() != TokenTable.C_CLOSEPAREN) {
      while (true) {
	final TypeGlue paramtype = getType(token, true);
	token = nextTokenIdentifier();
	final String paramname = token.getTokenText();
	final ParameterGlue param = new ParameterGlue(paramname, paramtype);
	parameters.add(param);
	token = nextToken();
        if (token.getTokenType() != TokenTable.C_COMMA) {
	  break;
	}
	token = nextToken();
      }
      requireToken(token, TokenTable.T_CLOSEPAREN);
    }
    return parameters;
  }

  private TypeGlue getType(Token token, final boolean nonvoid)
    throws IOException, ParseException
  {
    final boolean useglue = (token.getTokenType() == TokenTable.C_GLUE);
    if (useglue) {
      token = nextTokenIdentifier();
    }
    switch (token.getTokenType()) {
    case TokenTable.C_SIMPLETYPE:
      final SimpleTypeToken typetoken = (SimpleTypeToken) token;
      final SimpleTypeGlue type = typetoken.getTypeGlue();
      if (nonvoid && type.isVoid()) {
	throw createUnexpectedTokenException(token, "non-void type");
      }
      return type;
    case TokenTable.C_IDENTIFIER:
      final String name = token.getTokenText();
      final ClassGlue classglue = (ClassGlue) mClasses.get(name);
      if (classglue == null) {
	throw createUndefinedClassException(name);
      } else if (!classglue.checkGlueHeaders(useglue)) {
	throw createClassNeedsGlueException(name);
      } else {
	return new ClassTypeGlue(classglue, useglue);
      }
    default:
      throw createUnexpectedTokenException(token, "return type");
    }
  }


  //#########################################################################
  //# Scanning
  private Token nextTokenIdentifier()
    throws IOException, ParseException
  {
    final Token token = nextToken();
    requireIdentifier(token);
    return token;
  }

  private Token nextTokenKnown(final Token expected)
    throws IOException, ParseException
  {
    final Token token = nextToken();
    requireToken(token, expected);
    return token;
  }

  private Token nextToken()
    throws IOException, ParseException
  {
    return mScanner.nextToken();
  }


  //#########################################################################
  //# Error Checking
  private void requirePackageName()
    throws ParseException
  {
    if (mPackageName == null) {
      throw createParseException
	("Class declaration without preceding package!");
    }
  }

  private void requireIdentifier(final Token token)
    throws ParseException
  {
    if (token.getTokenType() != TokenTable.C_IDENTIFIER) {
      throw createUnexpectedTokenException(token, "identifier");
    }
  }

  private void requireToken(final Token token, final Token expected)
    throws ParseException
  {
    if (!token.equals(expected)) {
      final String expectedname = "'" + expected.getTokenText() + "'";
      throw createUnexpectedTokenException(token, expectedname);
    }
  }


  //#########################################################################
  //# Error Handling
  private ParseException createDuplicateClassException(final String name)
  {
    return createParseException("Redeclaration of class " + name + "!");
  }

  private ParseException createUndefinedClassException(final String name)
  {
    return createParseException("Undefined class " + name + "!");
  }

  private ParseException createClassNeedsGlueException(final String name)
  {
    return createParseException
      ("Class " + name + " must be declared as glue!");
  }

  private ParseException createUnexpectedTokenException
    (final Token token, final String expected)
  {
    final StringBuffer buffer = new StringBuffer();
    if (token.getTokenType() == TokenTable.C_EOF) {
      buffer.append("Unexpected end of file");
    } else {
      buffer.append("Unexpected token '");
      buffer.append(token.getTokenText());
      buffer.append('\'');
    }
    buffer.append(", expecting ");
    buffer.append(expected);
    buffer.append('!');
    return createParseException(buffer.toString());
  }


  //#########################################################################
  //# Error Recovery
  private Token recover()
    throws IOException
  {
    return recover(false);
  }

  private Token recover(final boolean atbrace)
    throws IOException
  {
    do {
      try {
	final Token token = nextToken();
	switch (token.getTokenType()) {
	case TokenTable.C_EOF:
	case TokenTable.C_SEMICOLON:
	  return token;
	case TokenTable.C_CLOSEBRACE:
	  if (atbrace) {
	    return token;
	  }
	}
      } catch (final ParseException exception) {
	reportError(exception);
      }
    } while (true);
  }


  //#########################################################################
  //# Data Members
  private final GlueFileScanner mScanner;
  private final Map mClasses;
  private final ClassGlue mObjectClass;
  private String mPackageName;

}
