//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.model.compiler.context
//# CLASS:   CompiledIntRange
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.model.compiler.context;

import java.util.AbstractList;
import java.util.List;

import net.sourceforge.waters.model.module.IntConstantProxy;
import net.sourceforge.waters.model.module.SimpleExpressionProxy;
import net.sourceforge.waters.plain.module.IntConstantElement;


public class CompiledIntRange implements CompiledRange
{

  //#########################################################################
  //# Constructors
  public CompiledIntRange(final int lower, final int upper)
  {
    mLower = lower;
    mUpper = upper;
  }


  //#########################################################################
  //# Simple Access
  public int getLower()
  {
    return mLower;
  }

  public int getUpper()
  {
    return mUpper;
  }

  //#########################################################################
  //# Overrides for java.lang.Object
  public boolean equals(final Object other)
  {
    if (other != null && getClass() == other.getClass()) {
      final CompiledIntRange range = (CompiledIntRange) other;
      return mLower == range.mLower && mUpper == range.mUpper;
    } else {
      return false;
    }
  }

  public int hashCode()
  {
    int result = getClass().hashCode();
    result *= 31;
    result += mLower;
    result *= 31;
    result += mUpper;
    return result;
  }

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

  public boolean intersects(final CompiledRange range)
  {
    if (range instanceof CompiledIntRange) {
      final CompiledIntRange intrange = (CompiledIntRange) range;
      return
        getUpper() >= intrange.getLower() &&
        intrange.getUpper() >= getLower();
    } else {
      return false;
    }
  }

  public CompiledIntRange intersection(final CompiledRange range)
  {
    if (range instanceof CompiledIntRange) {
      final CompiledIntRange intrange = (CompiledIntRange) range;
      return intersection(intrange);
    } else {
      return this;
    }
  }

  public CompiledIntRange remove(final SimpleExpressionProxy value)
  {
    if (value instanceof IntConstantProxy) {
      final IntConstantProxy intconst = (IntConstantProxy) value;
      return remove(intconst);
    } else {
      return this;
    }
  }

  public List<IntConstantProxy> getValues()
  {
    return new IntRangeList();
  }


  //#########################################################################
  //# More Specific Access
  public int indexOf(final IntConstantProxy intconst)
  {
    final int value = intconst.getValue();
    return mLower <= value && value <= mUpper ? value - mLower : -1;
  }

  public CompiledIntRange intersection(final CompiledIntRange range)
  {
    if (range.mLower <= mLower && mUpper <= range.mUpper) {
      return this;
    } else if (mLower <= range.mLower && range.mUpper <= mUpper) {
      return range;
    } else if (mLower < range.mLower) {
      return new CompiledIntRange(range.mLower, mUpper);
    } else {
      return new CompiledIntRange(mLower, range.mUpper);
    }
  }

  public CompiledIntRange remove(final IntConstantProxy intconst)
  {
    final int value = intconst.getValue();
    if (mLower == value) {
      return new CompiledIntRange(mLower + 1, mUpper);
    } else if (mUpper == value) {
      return new CompiledIntRange(mLower, mUpper - 1);
    } else {
      return this;
    }      
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
