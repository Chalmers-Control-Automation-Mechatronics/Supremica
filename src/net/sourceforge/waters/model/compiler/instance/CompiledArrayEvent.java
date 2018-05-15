//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# Copyright (C) 2004-2018 Robi Malik
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
