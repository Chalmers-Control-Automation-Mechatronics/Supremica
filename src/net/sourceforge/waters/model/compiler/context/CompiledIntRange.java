//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.model.compiler.context
//# CLASS:   CompiledIntRange
//###########################################################################
//# $Id: CompiledIntRange.java,v 1.1 2008-06-16 07:09:51 robi Exp $
//###########################################################################

package net.sourceforge.waters.model.compiler.context;

import java.util.AbstractList;
import java.util.List;

import net.sourceforge.waters.model.module.IntConstantProxy;
import net.sourceforge.waters.model.module.SimpleExpressionProxy;
import net.sourceforge.waters.plain.module.IntConstantElement;


class CompiledIntRange implements CompiledRange
{

  //#########################################################################
  //# Constructors
  CompiledIntRange(final int lower, final int upper)
  {
    mLower = lower;
    mUpper = upper;
  }


  //#########################################################################
  //# Getters
  int getLower()
  {
    return mLower;
  }

  int getUpper()
  {
    return mUpper;
  }


  //#########################################################################
  //# Overrides for java.lang.Object
  public String toString()
  {
    return mLower + ".." + mUpper;
  }


  //#########################################################################
  //# Interface net.sourceforge.waters.model.expr.RangeValue
  public int size()
  {
    return mUpper - mLower + 1;
  }

  public int indexOf(final SimpleExpressionProxy value)
  {
    if (value instanceof IntConstantProxy) {
      final IntConstantProxy intconst = (IntConstantProxy) value;
      return indexOf(intconst);
    } else {
      return -1;
    }
  }

  public boolean contains(final SimpleExpressionProxy value)
  {
    return indexOf(value) >= 0;
  }

  public List<IntConstantProxy> getValues()
  {
    return new IntRangeList();
  }


  //#########################################################################
  //# More Specific Access
  int indexOf(final IntConstantProxy intconst)
  {
    final int value = intconst.getValue();
    return mLower <= value && value <= mUpper ? value - mLower : -1;
  }


  //#########################################################################
  //# Local Class IntRangeList
  private class IntRangeList extends AbstractList<IntConstantProxy>
  {

    //#######################################################################
    //# Interface java.util.List
    public int size()
    {
      return CompiledIntRange.this.size();
    }

    public IntConstantProxy get(final int index)
    {
      final int value = mLower + index;
      if (value >= mLower && value <= mUpper) {
        return new IntConstantElement(value);
      } else {
        throw new IndexOutOfBoundsException
          ("Index " + index + " out of bounds for integer range " +
           CompiledIntRange.this + "!");
      }
    }

  }


  //#########################################################################
  //# Data Members
  private final int mLower;
  private final int mUpper;
    
}
