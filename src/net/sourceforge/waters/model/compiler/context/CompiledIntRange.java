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

import net.sourceforge.waters.model.compiler.CompilerOperatorTable;
import net.sourceforge.waters.model.expr.BinaryOperator;
import net.sourceforge.waters.model.module.IntConstantProxy;
import net.sourceforge.waters.model.module.ModuleProxyFactory;
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
  @Override
  public boolean equals(final Object other)
  {
    if (other != null && getClass() == other.getClass()) {
      final CompiledIntRange range = (CompiledIntRange) other;
      return mLower == range.mLower && mUpper == range.mUpper;
    } else {
      return false;
    }
  }

  @Override
  public int hashCode()
  {
    int result = getClass().hashCode();
    result *= 31;
    result += mLower;
    result *= 31;
    result += mUpper;
    return result;
  }

  @Override
  public String toString()
  {
    return mLower + ".." + mUpper;
  }


  //#########################################################################
  //# Interface net.sourceforge.waters.model.expr.RangeValue
  @Override
  public int size()
  {
    return mUpper - mLower + 1;
  }

  @Override
  public boolean isEmpty()
  {
    return mLower > mUpper;
  }

  @Override
  public int indexOf(final SimpleExpressionProxy value)
  {
    if (value instanceof IntConstantProxy) {
      final IntConstantProxy intconst = (IntConstantProxy) value;
      return indexOf(intconst);
    } else {
      return -1;
    }
  }

  @Override
  public boolean contains(final SimpleExpressionProxy value)
  {
    return indexOf(value) >= 0;
  }

  @Override
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

  @Override
  public CompiledIntRange intersection(final CompiledRange range)
  {
    if (range instanceof CompiledIntRange) {
      final CompiledIntRange intrange = (CompiledIntRange) range;
      return intersection(intrange);
    } else {
      return this;  // TODO BUG? Should this not be empty?
    }
  }

  @Override
  public CompiledIntRange union(final CompiledRange range)
  {
    if (range instanceof CompiledIntRange) {
      final CompiledIntRange intrange = (CompiledIntRange) range;
      return union(intrange);
    } else {
      return null;  // TODO BUG? Should this not be an error?
    }
  }

  @Override
  public CompiledIntRange remove(final SimpleExpressionProxy value)
  {
    if (value instanceof IntConstantProxy) {
      final IntConstantProxy intconst = (IntConstantProxy) value;
      return remove(intconst);
    } else {
      return this;
    }
  }

  @Override
  public List<IntConstantProxy> getValues()
  {
    return new IntRangeList();
  }

  @Override
  public SimpleExpressionProxy createExpression(final ModuleProxyFactory factory,
                                                final CompilerOperatorTable optable)
  {
    final IntConstantProxy lower = factory.createIntConstantProxy(mLower);
    final IntConstantProxy upper = factory.createIntConstantProxy(mUpper);
    final BinaryOperator op = optable.getRangeOperator();
    return factory.createBinaryExpressionProxy(op, lower, upper);
  }


  //#########################################################################
  //# More Specific Access
  public boolean isBooleanRange()
  {
    return mLower >= 0 && mUpper <= 1;
  }

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

  public CompiledIntRange union(final CompiledIntRange range)
  {
    if (range.mLower <= mLower && mUpper <= range.mUpper) {
      return range;
    } else if (mLower <= range.mLower && range.mUpper <= mUpper) {
      return this;
    } else if (mLower < range.mLower) {
      return new CompiledIntRange(mLower, range.mUpper);
    } else {
      return new CompiledIntRange(range.mLower, mUpper);
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
    @Override
    public int size()
    {
      return CompiledIntRange.this.size();
    }

    @Override
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
