//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.model.compiler
//# CLASS:   CompiledIntRangeValue
//###########################################################################
//# $Id: CompiledIntRangeValue.java,v 1.3 2006-11-03 15:01:57 torda Exp $
//###########################################################################

package net.sourceforge.waters.model.compiler;

import java.util.AbstractList;
import java.util.List;

import net.sourceforge.waters.model.expr.IndexValue;
import net.sourceforge.waters.model.expr.IntValue;
import net.sourceforge.waters.model.expr.RangeValue;
import net.sourceforge.waters.model.expr.Value;
import net.sourceforge.waters.model.unchecked.Casting;


class CompiledIntRangeValue implements RangeValue
{

  //#########################################################################
  //# Constructors
  CompiledIntRangeValue(final int lower, final int upper)
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

  public int hashCode()
  {
    return mLower * 5 + mUpper;
  }

  public boolean equals(final Object partner)
  {
    if (partner != null && getClass() == partner.getClass()) {
      final CompiledIntRangeValue range = (CompiledIntRangeValue) partner;
      return mLower == range.mLower && mUpper == range.mUpper;
    } else {
      return false;
    }    
  }


  //#########################################################################
  //# Interface net.sourceforge.waters.model.expr.RangeValue
  public int size()
  {
    return mUpper - mLower + 1;
  }

  public int indexOf(final Value value)
  {
    if (value instanceof IntValue) {
      final IntValue intvalue = (IntValue) value;
      return indexOf(intvalue);
    } else {
      return -1;
    }
  }

  public boolean contains(final Value value)
  {
    return indexOf(value) >= 0;
  }

  public List<IndexValue> getValues()
  {
    return Casting.toList(getIntValues());
  }


  //#########################################################################
  //# More Specific Access
  List<CompiledIntValue> getIntValues()
  {
    return new IntRangeList();
  }

  int indexOf(final IntValue value)
  {
    final int gotvalue = value.getValue();
    return mLower <= gotvalue && gotvalue <= mUpper ? gotvalue - mLower : -1;
  }


  //#########################################################################
  //# Local Class IntRangeList
  private class IntRangeList extends AbstractList<CompiledIntValue>
  {

    //#######################################################################
    //# Interface java.util.List
    public int size()
    {
      return CompiledIntRangeValue.this.size();
    }

    public CompiledIntValue get(final int index)
    {
      final int value = mLower + index;
      if (value >= mLower && value <= mUpper) {
        return new CompiledIntValue(value);
      } else {
        throw new IndexOutOfBoundsException
          ("Index " + index + " out of bounds for integer range " +
           CompiledIntRangeValue.this + "!");
      }
    }

  }


  //#########################################################################
  //# Data Members
  private final int mLower;
  private final int mUpper;
    
}
