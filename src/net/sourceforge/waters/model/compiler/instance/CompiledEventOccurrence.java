//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.model.compiler.instance
//# CLASS:   CompiledEventOccurrence
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.model.compiler.instance;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import net.sourceforge.waters.model.compiler.context.CompiledRange;
import net.sourceforge.waters.model.compiler.context.SourceInfo;
import net.sourceforge.waters.model.expr.EvalException;
import net.sourceforge.waters.model.module.SimpleExpressionProxy;


class CompiledEventOccurrence implements CompiledEvent
{

  //#########################################################################
  //# Constructor
  CompiledEventOccurrence(final CompiledEvent event,
			  final SourceInfo info)
  {
    mEvent = event;
    mSourceInfo = info;
  }
    

  //#########################################################################
  //# Interface net.sourceforge.waters.model.compiler.instance.CompiledEvent
  public int getKindMask()
  {
    return mEvent.getKindMask();
  }

  public boolean isObservable()
  {
    return mEvent.isObservable();
  }

  public List<CompiledRange> getIndexRanges()
  {
    return mEvent.getIndexRanges();
  }

  public CompiledEvent find(SimpleExpressionProxy index)
    throws EvalException
  {
    return mEvent.find(index);
  }

  public SourceInfo getSourceInfo()
  {
    return mSourceInfo;
  }

  public Iterator<CompiledEvent> getChildrenIterator()
  {
    return Collections.singletonList(mEvent).iterator();
  }


  //#########################################################################
  //# Data Members
  private final CompiledEvent mEvent;
  private final SourceInfo mSourceInfo;

}