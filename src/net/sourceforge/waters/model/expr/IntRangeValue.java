//###########################################################################
//# PROJECT: Waters
//# PACKAGE: waters.model.expr
//# CLASS:   IntRangeValue
//###########################################################################
//# $Id: IntRangeValue.java,v 1.1 2005-02-17 01:43:35 knut Exp $
//###########################################################################

package net.sourceforge.waters.model.expr;

import java.util.Iterator;
import java.util.NoSuchElementException;


public class IntRangeValue implements RangeValue
{

  //#########################################################################
  //# Constructors
  public IntRangeValue(final int lower, final int upper)
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
      final IntRangeValue range = (IntRangeValue) partner;
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
      final int gotvalue = intvalue.getValue();
      return mLower <= gotvalue && gotvalue <= mUpper ? gotvalue - mLower : -1;
    } else {
      return -1;
    }
  }

  public boolean contains(Value value)
  {
    return indexOf(value) >= 0;
  }

  public Iterator iterator()
  {
    return new IntRangeIterator();
  }


  //#########################################################################
  //# Data Members
  private final int mLower;
  private final int mUpper;


  //#########################################################################
  //# Local Class IntRangeIterator
  private class IntRangeIterator implements Iterator {

    //#######################################################################
    //# Constructors
    IntRangeIterator()
    {
      mNext = mLower;
    }


    //#######################################################################
    //# Interface java.util.Iterator
    public boolean hasNext()
    {
      return mNext <= mUpper;
    }

    public Object next()
    {
      if (mNext <= mUpper) {
	return new IntValue(mNext++);
      } else {
	throw new NoSuchElementException
	  ("Reading past end of IntRangeValue!");
      }
    }

    public void remove()
    {
      throw new UnsupportedOperationException
	("Can't remove from immutable class IntRangeValue!");
    }


    //#########################################################################
    //# Data Members
    private int mNext;

  }
    
}