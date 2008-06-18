//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.model.compiler.instance
//# CLASS:   CompiledArrayEvent
//###########################################################################
//# $Id: CompiledArrayEvent.java,v 1.2 2008-06-18 09:35:34 robi Exp $
//###########################################################################

package net.sourceforge.waters.model.compiler.instance;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import net.sourceforge.waters.model.compiler.context.CompiledRange;
import net.sourceforge.waters.model.compiler.context.SourceInfo;
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

  public SourceInfo getSourceInfo()
  {
    return null;
  }

  public Iterator<CompiledEvent> getChildrenIterator()
  {
    return new ArrayEventIterator();
  }


  //#########################################################################
  //# Local Class ArrayEventIterator
  private class ArrayEventIterator
    implements Iterator<CompiledEvent>
  {

    //#######################################################################
    //# Constructor
    ArrayEventIterator()
    {
      mPos = mIndexes.size();
      final CompiledRange range = mDecl.getRange(mPos);
      mRangeIterator = range.getValues().iterator();
      if (mRangeIterator.hasNext()) {
        mMoreIndexes = new ArrayList<SimpleExpressionProxy>(mPos + 1);
        mMoreIndexes.addAll(mIndexes);
        mMoreIndexes.add(null);
      } else {
        mMoreIndexes = null;
      }
    }

    //#######################################################################
    //# Interface java.util.Iterator
    public boolean hasNext()
    {
      return mRangeIterator.hasNext();
    }

    public CompiledEvent next()
    {
      final SimpleExpressionProxy next = mRangeIterator.next();
      mMoreIndexes.set(mPos, next);
      return mDecl.getCompiledEvent(mMoreIndexes);
    }

    public void remove()
    {
      throw new UnsupportedOperationException
        ("ArrayEventIterator does not support remove() operation!");
    } 

    //#######################################################################
    //# Data Members
    private final int mPos;
    private final Iterator<? extends SimpleExpressionProxy> mRangeIterator;
    private final List<SimpleExpressionProxy> mMoreIndexes;
  }


  //#######################################################################
  //# Data Members
  private final CompiledEventDecl mDecl;
  private final List<SimpleExpressionProxy> mIndexes;

}
