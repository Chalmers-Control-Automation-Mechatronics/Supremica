//###########################################################################
//# PROJECT: Waters
//# PACKAGE: waters.model.expr
//# CLASS:   OperatorTable
//###########################################################################
//# $Id: OperatorTable.java,v 1.1 2005-02-17 01:43:35 knut Exp $
//###########################################################################

package net.sourceforge.waters.model.expr;

import java.util.HashMap;
import java.util.Map;


/**
 * <P>The operator table.</P>
 *
 * <P>This class provides some static methods to facilitate the
 * identification of operators given their name.</P>
 *
 * @author Robi Malik
 */

class OperatorTable {

  //#########################################################################
  //# Accessing the Table
  static UnaryOperator findUnaryOperator(final String name)
    throws UnknownOperatorException
  {
    final UnaryOperator op = getUnaryOperator(name);
    if (op == null) {
      throw new UnknownOperatorException
	("Unknown unary operator '" + name + "'!");
    }
    return op;
  }

  static BinaryOperator findBinaryOperator(final String name)
    throws UnknownOperatorException
  {
    final BinaryOperator op = getBinaryOperator(name);
    if (op == null) {
      throw new UnknownOperatorException
	("Unknown binary operator '" + name + "'!");
    }
    return op;
  }

  static UnaryOperator getUnaryOperator(final String name)
  {
    final Entry entry = getOperatorEntry(name);
    if (entry == null) {
      return null;
    } else {
      return entry.getUnaryOperator();
    }
  }

  static BinaryOperator getBinaryOperator(final String name)
  {
    final Entry entry = getOperatorEntry(name);
    if (entry == null) {
      return null;
    } else {
      return entry.getBinaryOperator();
    }
  }

  static Entry getOperatorEntry(final String name)
  {
    return (Entry) sOperatorTable.get(name);
  }

  static boolean isOperatorCharacter(final char ch)
  {
    return
      ch >= OPCHAR_MIN && ch < OPCHAR_MAX && sOperatorChar[ch - OPCHAR_MIN];
  }


  //#########################################################################
  //# Static Initialisation of the Table
  private static void initTables()
  {
    for (int i = 0; i < OPCHAR_MAX - OPCHAR_MIN; i++) {
      sOperatorChar[i] = false;
    }

    store(EqualsExpressionProxy.getOperator());
    store(IntRangeExpressionProxy.getOperator());
    store(MinusExpressionProxy.getOperator());
    store(PlusExpressionProxy.getOperator());
  }

  private static void store(final Operator op)
  {
    final String name = op.getName();
    final Entry entry = (Entry) sOperatorTable.get(name);
    if (entry == null) {
      final Entry newentry = new Entry(op);
      sOperatorTable.put(name, newentry);
    } else {
      entry.putOperator(op);
    }
    storeChars(name);
  }

  private static void storeChars(final String name)
  {
    for (int i = 0; i < name.length(); i++) {
      final char ch = name.charAt(i);
      sOperatorChar[ch - OPCHAR_MIN] = true;
    }
  }


  //#########################################################################
  //# Local Class Entry
  static class Entry
  {
    //#######################################################################
    //# Constructors
    private Entry(final Operator op)
    {
      putOperator(op);
    }

    //#######################################################################
    //# Initialisation
    private void putOperator(final Operator op)
    {
      if (op instanceof UnaryOperator) {
	mUnaryOperator = (UnaryOperator) op;
      } else if (op instanceof BinaryOperator) {
	mBinaryOperator = (BinaryOperator) op;
      } else {
	throw new ClassCastException
	  ("Unknown operator type " + op.getClass().getName() + "!");
      }
    }

    //#######################################################################
    //# Simple Access
    UnaryOperator getUnaryOperator()
    {
      return mUnaryOperator;
    }

    BinaryOperator getBinaryOperator()
    {
      return mBinaryOperator;
    }

    //#######################################################################
    //# Data Members
    private UnaryOperator mUnaryOperator;
    private BinaryOperator mBinaryOperator;
  }


  //#########################################################################
  //# Class Constants
  static final String OPNAME_EQUALS = "==";
  static final String OPNAME_MINUS = "-";
  static final String OPNAME_PLUS = "+";
  static final String OPNAME_RANGE = "..";

  static final int PRIORITY_UNARY = 90;
  static final int PRIORITY_MULT = 80;
  static final int PRIORITY_PLUS = 70;
  static final int PRIORITY_RANGE = 60;
  static final int PRIORITY_EQU = 50;
  static final int PRIORITY_AND = 40;
  static final int PRIORITY_OR = 30;
  static final int PRIORITY_OUTER = 0;

  static final int ASSOC_NONE = 0;
  static final int ASSOC_LEFT = 1;
  static final int ASSOC_RIGHT = 2;

  private static final int OPCHAR_MIN = 32;
  private static final int OPCHAR_MAX = 128;


  //#########################################################################
  //# Static Class Variables
  private static final Map sOperatorTable = new HashMap(16);
  private static final boolean sOperatorChar[] =
    new boolean[OPCHAR_MAX - OPCHAR_MIN];

  static {initTables();}
}
