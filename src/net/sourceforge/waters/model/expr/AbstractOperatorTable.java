//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# Copyright (C) 2004-2020 Robi Malik
//###########################################################################
//# This file is part of Waters.
//# Waters is free software: you can redistribute it and/or modify it under
//# the terms of the GNU General Public License as published by the Free
//# Software Foundation, either version 2 of the License, or (at your option)
//# any later version.
//# Waters is distributed in the hope that it will be useful, but WITHOUT ANY
//# WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
//# FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
//# details.
//# You should have received a copy of the GNU General Public License along
//# with Waters. If not, see <http://www.gnu.org/licenses/>.
//#
//# Linking Waters statically or dynamically with other modules is making a
//# combined work based on Waters. Thus, the terms and conditions of the GNU
//# General Public License cover the whole combination.
//# In addition, as a special exception, the copyright holders of Waters give
//# you permission to combine Waters with code included in the standard
//# release of Supremica under the Supremica Software License Agreement (or
//# modified versions of such code, with unchanged license). You may copy and
//# distribute such a system following the terms of the GNU GPL for Waters and
//# the licenses of the other code concerned.
//# Note that people who make modified versions of Waters are not obligated to
//# grant this special exception for their modified versions; it is their
//# choice whether to do so. The GNU General Public License gives permission
//# to release a modified version without this exception; this exception also
//# makes it possible to release a modified version which carries forward this
//# exception.
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
