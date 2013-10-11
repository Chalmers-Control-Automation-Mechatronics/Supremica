//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.model.expr
//# CLASS:   AbstractOperatorTable
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.model.expr;

import java.util.HashMap;
import java.util.Map;


/**
 * <P>An empty operator table.</P>
 *
 * @author Robi Malik
 */

public abstract class AbstractOperatorTable implements OperatorTable {

  //#########################################################################
  //# Constructors
  protected AbstractOperatorTable(final int size,
                                  final int minchar,
                                  final int maxchar)
  {
    mMinOpChar = minchar;
    mMaxOpChar = maxchar;
    mOperatorTable = new HashMap<String,Entry>(size);
    mOperatorOrdering = new HashMap<Operator,Integer>(size);
    mOperatorChar = new boolean[maxchar - minchar];
    mFunctionTable = new HashMap<String,BuiltInFunction>(size);
    mFunctionKeyCharacter = 0;
  }


  //#########################################################################
  //# Interface net.sourceforge.waters.model.expr.OperatorTable
  @Override
  public UnaryOperator getUnaryOperator(final String name)
  {
    final Entry entry = getOperatorEntry(name);
    if (entry == null) {
      return null;
    } else {
      return entry.getUnaryOperator();
    }
  }

  @Override
  public BinaryOperator getBinaryOperator(final String name)
  {
    final Entry entry = getOperatorEntry(name);
    if (entry == null) {
      return null;
    } else {
      return entry.getBinaryOperator();
    }
  }

  @Override
  public BuiltInFunction getBuiltInFunction(final String name)
  {
    return mFunctionTable.get(name);
  }

  @Override
  public boolean containsOperator(final String name)
  {
    return mOperatorTable.containsKey(name);
  }

  @Override
  public boolean isOperatorCharacter(final char ch)
  {
    return
      ch >= mMinOpChar && ch < mMaxOpChar && mOperatorChar[ch - mMinOpChar];
  }

  @Override
  public int getOperatorValue(final Operator op)
  {
    return mOperatorOrdering.get(op);
  }

  @Override
  public char getFunctionKeyCharacter()
  {
    return mFunctionKeyCharacter;
  }


  //#########################################################################
  //# Initialisation
  protected void store(final BinaryOperator op, final int orderindex)
  {
    storeOperator(op);
    mOperatorOrdering.put(op, orderindex);
  }

  protected void store(final UnaryOperator op, final int orderindex)
  {
    storeOperator(op);
    mOperatorOrdering.put(op, orderindex);
  }

  protected void store(final BinaryOperator op)
  {
    storeOperator(op);
  }

  protected void store(final UnaryOperator op)
  {
    storeOperator(op);
  }

  protected void store(final BuiltInFunction function)
  {
    final String name = function.getName();
    mFunctionTable.put(name, function);
    if (mFunctionKeyCharacter == 0) {
      mFunctionKeyCharacter = name.charAt(0);
    } else {
      assert mFunctionKeyCharacter == name.charAt(0);
    }
  }


  //#########################################################################
  //# Auxiliary Methods
  private void storeOperator(final Operator op)
  {
    final String name = op.getName();
    final Entry entry = mOperatorTable.get(name);
    if (entry == null) {
      final Entry newentry = new Entry(op);
      mOperatorTable.put(name, newentry);
    } else {
      entry.putOperator(op);
    }
    storeChars(name);
  }

  private Entry getOperatorEntry(final String name)
  {
    return mOperatorTable.get(name);
  }

  private void storeChars(final String name)
  {
    for (int i = 0; i < name.length(); i++) {
      final char ch = name.charAt(i);
      mOperatorChar[ch - mMinOpChar] = true;
    }
  }


  //#########################################################################
  //# Local Class Entry
  private static class Entry
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
    private UnaryOperator getUnaryOperator()
    {
      return mUnaryOperator;
    }

    private BinaryOperator getBinaryOperator()
    {
      return mBinaryOperator;
    }

    //#######################################################################
    //# Data Members
    private UnaryOperator mUnaryOperator;
    private BinaryOperator mBinaryOperator;
  }


  //#########################################################################
  //# Data Members
  private final Map<String,Entry> mOperatorTable;
  private final Map<Operator,Integer> mOperatorOrdering;
  private final boolean mOperatorChar[];
  private final int mMinOpChar;
  private final int mMaxOpChar;
  private final Map<String,BuiltInFunction> mFunctionTable;
  private char mFunctionKeyCharacter;

}
