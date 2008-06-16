//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.model.compiler.instance
//# CLASS:   CompiledArrayEvent
//###########################################################################
//# $Id: CompiledArrayEvent.java,v 1.1 2008-06-16 07:09:51 robi Exp $
//###########################################################################

package net.sourceforge.waters.model.compiler.instance;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

import net.sourceforge.waters.model.compiler.context.CompiledRange;
import net.sourceforge.waters.model.expr.EvalException;
import net.sourceforge.waters.model.module.SimpleExpressionProxy;
import net.sourceforge.waters.xsd.base.EventKind;


class CompiledArrayEvent implements CompiledEvent
{

  //#######################################################################
  //# Constructor
  CompiledArrayEvent(final CompiledEventDecl decl,
                     final List<? extends SimpleExpressionProxy> indexes)
  {
    mDecl = decl;
    mIndexes =  Collections.unmodifiableList(indexes);
  }


  //#######################################################################
  //# Interface net.sourceforge.waters.model.compiler.Event
  public int getKindMask()
  {
    final EventKind kind = mDecl.getKind();
    return EventKindMask.getMask(kind);
  }

  public boolean isObservable()
  {
    return mDecl.isObservable();
  }

  public Iterator<CompiledSingleEvent> getEventIterator()
  {
    return new ArrayEventIterator(this);
  }

  public List<CompiledRange> getIndexRanges()
  {
    final List<CompiledRange> ranges = mDecl.getRanges();
    final int start = mIndexes.size();
    final int end = ranges.size();
    return ranges.subList(start, end);
  }

  public CompiledEvent find(final SimpleExpressionProxy index)
    throws EvalException
  {
    final int lastpos = mIndexes.size();
    mDecl.checkIndex(lastpos, index);
    final List<SimpleExpressionProxy> indexes =
      new ArrayList<SimpleExpressionProxy>(lastpos + 1);
    indexes.addAll(mIndexes);
    indexes.add(index);
    return mDecl.getCompiledEvent(indexes);
  }


  //#########################################################################
  //# Local Class ArrayEventIterator
  private class ArrayEventIterator
    implements Iterator<CompiledSingleEvent>
  {

    //#######################################################################
    //# Constructor
    ArrayEventIterator(final CompiledArrayEvent value)
    {
      mPos = mIndexes.size();
      final CompiledRange range = mDecl.getRange(mPos);
      mRangeIterator = range.getValues().iterator();
      if (mRangeIterator.hasNext()) {
        final SimpleExpressionProxy first = mRangeIterator.next();
        mMoreIndexes = new ArrayList<SimpleExpressionProxy>(mPos + 1);
        mMoreIndexes.addAll(mIndexes);
        mMoreIndexes.add(first);
        final CompiledEvent event = mDecl.getCompiledEvent(mMoreIndexes);
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

    public CompiledSingleEvent next()
    {
      if (mInnerIterator != null) {
        final CompiledSingleEvent result = mInnerIterator.next();
        if (!mInnerIterator.hasNext()) {
          if (!mRangeIterator.hasNext()) {
            mMoreIndexes = null;
            mRangeIterator = null;
            mInnerIterator = null;
          } else {
            final SimpleExpressionProxy next = mRangeIterator.next();
            mMoreIndexes.set(mPos, next);
            final CompiledEvent event = mDecl.getCompiledEvent(mMoreIndexes);
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
    private List<SimpleExpressionProxy> mMoreIndexes;
    private Iterator<? extends SimpleExpressionProxy> mRangeIterator;
    private Iterator<CompiledSingleEvent> mInnerIterator;

  }


  //#######################################################################
  //# Data Members
  private final CompiledEventDecl mDecl;
  private final List<SimpleExpressionProxy> mIndexes;

}
