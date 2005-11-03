//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: waters.model.compiler
//# CLASS:   CompiledEventListValue
//###########################################################################
//# $Id: CompiledEventListValue.java,v 1.2 2005-11-03 01:24:15 robi Exp $
//###########################################################################

package net.sourceforge.waters.model.compiler;

import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.NoSuchElementException;

import net.sourceforge.waters.model.expr.RangeValue;
import net.sourceforge.waters.model.module.EventDeclProxy;


/**
 * <P>A {@link net.sourceforge.waters.model.expr.Value} object representing
 * a list of events.</P>
 *
 * <P>Event lists are collections of events corresponding to the edges of
 * graphs, where each event in the list produces its own transition.  They
 * also occur as values of event aliases or the actual parameters for
 * module instantiations</P>
 *
 * <P>Technically, an event list value is an implementation of the
 * {@link net.sourceforge.waters.model.expr.Value} interface with additional
 * capability to access controllability information, and the list
 * of contained events.</P>
 *
 * @author Robi Malik
 */

class CompiledEventListValue implements EventValue
{

  //#########################################################################
  //# Constructor
  CompiledEventListValue()
  {
    this(EventKindMask.TYPEMASK_ANY);
  }

  CompiledEventListValue(final int allowedKindMask)
  {
    mList = new LinkedList<EventValue>();
    mAllowedKindMask = allowedKindMask;
    mKindMask = 0;
    mIsObservable = true;
  }


  //#########################################################################
  //# Overrides for java.lang.Object
  public String toString()
  {
    final StringBuffer buffer = new StringBuffer("{");
    final Iterator<EventValue> iter = mList.iterator();
    while (iter.hasNext()) {
      final EventValue value = iter.next();
      buffer.append(value);
      if (iter.hasNext()) {
	buffer.append(", ");
      }
    }
    buffer.append('}');
    return buffer.toString();
  }

  public int hashCode()
  {
    return mList.hashCode();
  }

  public boolean equals(final Object partner)
  {
    if (partner != null && getClass() == partner.getClass()) {
      final CompiledEventListValue value = (CompiledEventListValue) partner;
      return mList.equals(value.mList);
    } else {
      return false;
    }    
  }


  //#########################################################################
  //# Interface net.sourceforge.waters.model.compiler.EventValue
  public int getKindMask()
  {
    return mKindMask;
  }

  public boolean isObservable()
  {
    return mIsObservable;
  }

  public Iterator<CompiledSingleEventValue> getEventIterator()
  {
    return new NestedIterator();
  }

  public List<RangeValue> getIndexRanges()
  {
    return Collections.emptyList();
  }


  //#########################################################################
  //# Specific Access
  void addEvent(final EventValue value)
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

  EventValue getSimplified()
  {
    if (mList.size() == 1) {
      return mList.get(0);
    } else {
      return this;
    }
  }


  //#########################################################################
  //# Local Class NestedIterator
  private class NestedIterator implements Iterator<CompiledSingleEventValue>
  {

    //#######################################################################
    //# Constructor
    NestedIterator()
    {
      mListIterator = mList.iterator();
      mInnerIterator = null;
      advance();
    }


    //#######################################################################
    //# Interface java.util.Iterator
    public boolean hasNext()
    {
      return mListIterator != null;
    }

    public CompiledSingleEventValue next()
    {
      if (mListIterator != null) {
	final CompiledSingleEventValue result = mInnerIterator.next();
	advance();
	return result;
      } else {
	throw new NoSuchElementException
	  ("No more events in compiled event list iteration!");
      }
    }
	
    public void remove()
    {
      throw new UnsupportedOperationException
	("Can't remove from compiled event list!");
    }

    //#######################################################################
    //# Auxiliary Methods
    private void advance()
    {
      while (mInnerIterator == null || !mInnerIterator.hasNext()) {
	if (mListIterator.hasNext()) {
	  final EventValue value = mListIterator.next();
	  mInnerIterator = value.getEventIterator();
	} else {
	  mListIterator = null;
	  mInnerIterator = null;
	  return;
	}
      }
    }

    //#######################################################################
    //# Data Members
    private Iterator<EventValue> mListIterator;
    private Iterator<CompiledSingleEventValue> mInnerIterator;

  }


  //#########################################################################
  //# Data Members
  private final List<EventValue> mList;
  private final int mAllowedKindMask;
  private int mKindMask;
  private boolean mIsObservable;


}
