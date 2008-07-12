//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.model.compiler.instance
//# CLASS:   CompiledEventList
//###########################################################################
//# $Id: CompiledEventList.java,v 1.2 2008-06-18 09:35:34 robi Exp $
//###########################################################################

package net.sourceforge.waters.model.compiler.instance;

import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import net.sourceforge.waters.model.compiler.context.CompiledRange;
import net.sourceforge.waters.model.compiler.context.SourceInfo;
import net.sourceforge.waters.model.expr.EvalException;
import net.sourceforge.waters.model.module.SimpleExpressionProxy;


/**
 * <P>A compiler-internal object representing a list of compiled
 * events.</P>
 *
 * <P>Event lists are collections of events corresponding to the edges of
 * graphs, where each event in the list produces its own transition.  They
 * also occur as values of event aliases or the actual parameters for
 * module instantiations</P>
 *
 * <P>Technically, an event list value is an implementation of the {@link
 * net.sourceforge.waters.model.compiler.instance.CompiledEvent} interface
 * with additional capability to access controllability information, and
 * the list of contained events.</P>
 *
 * @author Robi Malik
 */

class CompiledEventList implements CompiledEvent
{

  //#########################################################################
  //# Constructor
  CompiledEventList()
  {
    this(EventKindMask.TYPEMASK_ANY);
  }

  CompiledEventList(final int allowedKindMask)
  {
    mList = new LinkedList<CompiledEvent>();
    mAllowedKindMask = allowedKindMask;
    mKindMask = 0;
    mIsObservable = true;
  }


  //#########################################################################
  //# Interface net.sourceforge.waters.model.compiler.CompiledEvent
  public int getKindMask()
  {
    return mKindMask;
  }

  public boolean isObservable()
  {
    return mIsObservable;
  }

  public List<CompiledRange> getIndexRanges()
  {
    final CompiledEvent event = getSingleEvent();
    if (event == null) {
      return Collections.emptyList();
    } else {
      return event.getIndexRanges();
    }
  }

  public CompiledEvent find(final SimpleExpressionProxy index)
    throws EvalException
  {
    final CompiledEvent event = getSingleEvent();
    if (event == null) {
      throw new IndexOutOfRangeException(this);
    } else {
      return event.find(index);
    }
  }

  public SourceInfo getSourceInfo()
  {
    return null;
  }

  public Iterator<CompiledEvent> getChildrenIterator()
  {
    return mList.iterator();
  }


  //#########################################################################
  //# Specific Access
  void addEvent(final CompiledEvent value)
    throws EventKindException
  {
    final int mask = value.getKindMask();
    final int badmask = mask & ~mAllowedKindMask;
    if (badmask != 0) {
      throw new EventKindException(value, badmask);
    }
    mKindMask |= mask;
    mIsObservable &= value.isObservable();
    mList.add(value);
  }


  //#########################################################################
  //# Auxiliary Methods
  private CompiledEvent getSingleEvent()
  {
    final Iterator<CompiledEvent> iter = mList.iterator();
    if (iter.hasNext()) {
      final CompiledEvent event = iter.next();
      return iter.hasNext() ? null : event;
    } else {
      return null;
    }
  }


  //#########################################################################
  //# Data Members
  private final List<CompiledEvent> mList;
  private final int mAllowedKindMask;
  private int mKindMask;
  private boolean mIsObservable;

}
