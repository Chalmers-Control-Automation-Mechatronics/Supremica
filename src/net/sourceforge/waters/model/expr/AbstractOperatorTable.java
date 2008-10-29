//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.model.expr
//# CLASS:   AbstractOperatorTable
//###########################################################################
//# $Id: AbstractOperatorTable.java,v 1.2 2005-11-03 01:24:16 robi Exp $
//###########################################################################

package net.sourceforge.waters.model.expr;

import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

import net.sourceforge.waters.model.module.SimpleExpressionProxy;


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
  }


  //#########################################################################
  //# Interface net.sourceforge.waters.model.expr.OperatorTable
  public UnaryOperator getUnaryOperator(final String name)
  {
    final Entry entry = getOperatorEntry(name);
    if (entry == null) {
      return null;
    } else {
      return entry.getUnaryOperator();
    }
  }

  public BinaryOperator getBinaryOperator(final String name)
  {
    final Entry entry = getOperatorEntry(name);
    if (entry == null) {
      return null;
    } else {
      return entry.getBinaryOperator();
    }
  }

  public boolean contains(final String name)
  {
    return mOperatorTable.containsKey(name);
  }

  public boolean isOperatorCharacter(final char ch)
  {
    return
      ch >= mMinOpChar && ch < mMaxOpChar && mOperatorChar[ch - mMinOpChar];
  }

  public Comparator<SimpleExpressionProxy> getExpressionComparator()
  {
    if (mComparator == null) {
      mComparator = new ExpressionComparator(mOperatorOrdering);
    }
    return mComparator;
  }


  //#########################################################################
  //# Initialisation
  protected void store(final Operator op, final int orderindex)
  {
    store(op);
    mOperatorOrdering.put(op, orderindex);
  }

  protected void store(final Operator op)
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


  //#########################################################################
  //# Auxiliary Methods
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

  private Comparator<SimpleExpressionProxy> mComparator;

}
