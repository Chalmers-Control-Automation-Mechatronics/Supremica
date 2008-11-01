//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.model.compiler
//# CLASS:   CompiledArrayEventValue
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.model.compiler;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

import net.sourceforge.waters.model.expr.EvalException;
import net.sourceforge.waters.model.expr.IndexValue;
import net.sourceforge.waters.model.expr.RangeValue;
import net.sourceforge.waters.xsd.base.EventKind;


class CompiledArrayEventValue implements ArrayValue, EventValue
{

  //#######################################################################
  //# Constructor
  CompiledArrayEventValue(final CompiledEventDecl decl,
                          final List<? extends IndexValue> indexes)
  {
    mDecl = decl;
    mIndexes =  Collections.unmodifiableList(indexes);
  }


  //#########################################################################
  //# Overrides for java.lang.Object
  public String toString()
  {
    return mDecl.getIndexedName(mIndexes);
  }

  public int hashCode()
  {
    return 5 * mDecl.getName().hashCode() + mIndexes.hashCode();
  }

  public boolean equals(final Object partner)
  {
    if (partner != null && getClass() == partner.getClass()) {
      final CompiledArrayEventValue value = (CompiledArrayEventValue) partner;
      return mDecl == value.mDecl && mIndexes.equals(value.mIndexes);
    } else {
      return false;
    }
  }


  //#######################################################################
  //# Interface net.sourceforge.waters.model.compiler.ArrayValue
  public EventValue find(final IndexValue index)
    throws EvalException
  {
    final int lastpos = mIndexes.size();
    mDecl.checkIndex(lastpos, index);
    final List<IndexValue> indexes = new ArrayList<IndexValue>(lastpos + 1);
    indexes.addAll(mIndexes);
    indexes.add(index);
    return mDecl.getValue(indexes);
  }


  //#######################################################################
  //# Interface net.sourceforge.waters.model.compiler.EventValue
  public int getKindMask()
  {
    final EventKind kind = getKind();
    return EventKindMask.getMask(kind);
  }

  public boolean isObservable()
  {
    return mDecl.isObservable();
  }

  public Iterator<CompiledSingleEventValue> getEventIterator()
  {
    return new ArrayEventIterator(this);
  }

  public List<RangeValue> getIndexRanges()
  {
    final List<RangeValue> ranges = mDecl.getRanges();
    final int start = mIndexes.size();
    final int end = ranges.size();
    return ranges.subList(start, end);
  }


  //#######################################################################
  //# Specific Access
  EventKind getKind()
  {
    return mDecl.getKind();
  }


  //#########################################################################
  //# Local Class ArrayEventIterator
  private class ArrayEventIterator
    implements Iterator<CompiledSingleEventValue>
  {

    //#######################################################################
    //# Constructor
    ArrayEventIterator(final CompiledArrayEventValue value)
    {
      mPos = mIndexes.size();
      final RangeValue range = mDecl.getRange(mPos);
      mRangeIterator = range.getValues().iterator();
      if (mRangeIterator.hasNext()) {
        final IndexValue first = mRangeIterator.next();
        mMoreIndexes = new ArrayList<IndexValue>(mPos + 1);
        mMoreIndexes.addAll(mIndexes);
        mMoreIndexes.add(first);
        final EventValue event = mDecl.getValue(mMoreIndexes);
        mInnerIterator = event.getEventIterator();
      }
      if (mInnerIterator == null || !mInnerIterator.hasNext()) {
        mMoreIndexes = null;
        mRangeIterator = null;
        mInnerIterator = null;
      }
    }

    //#######################################################################
    //# Interface java.util.Iterator
    public boolean hasNext()
    {
      return mInnerIterator != null;
    }

    public CompiledSingleEventValue next()
    {
      if (mInnerIterator != null) {
        final CompiledSingleEventValue result = mInnerIterator.next();
        if (!mInnerIterator.hasNext()) {
          if (!mRangeIterator.hasNext()) {
            mMoreIndexes = null;
            mRangeIterator = null;
            mInnerIterator = null;
          } else {
            final IndexValue next = mRangeIterator.next();
            mMoreIndexes.set(mPos, next);
            final EventValue event = mDecl.getValue(mMoreIndexes);
            mInnerIterator = event.getEventIterator();
          }
        }
        return result;
      } else {
        throw new NoSuchElementException
          ("Out of elements in ArrayEventIterator!");
      }
    }

    public void remove()
    {
      throw new UnsupportedOperationException
        ("ArrayEventIterator does not support remove() operation!");
    } 

    //#######################################################################
    //# Data Members
    private final int mPos;
    private List<IndexValue> mMoreIndexes;
    private Iterator<IndexValue> mRangeIterator;
    private Iterator<CompiledSingleEventValue> mInnerIterator;

  }


  //#######################################################################
  //# Data Members
  private final CompiledEventDecl mDecl;
  private final List<IndexValue> mIndexes;

}
