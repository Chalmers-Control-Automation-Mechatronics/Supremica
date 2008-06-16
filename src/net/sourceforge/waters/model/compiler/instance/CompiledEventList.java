//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.model.compiler.instance
//# CLASS:   CompiledEventList
//###########################################################################
//# $Id: CompiledEventList.java,v 1.1 2008-06-16 07:09:51 robi Exp $
//###########################################################################

package net.sourceforge.waters.model.compiler.instance;

import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.NoSuchElementException;

import net.sourceforge.waters.model.compiler.context.CompiledRange;
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

  public Iterator<CompiledSingleEvent> getEventIterator()
  {
    return new NestedIterator();
  }

  public List<CompiledRange> getIndexRanges()
  {
    return Collections.emptyList();
  }

  public CompiledEvent find(final SimpleExpressionProxy index)
    throws IndexOutOfRangeException
  {
    throw new IndexOutOfRangeException(this);
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

  CompiledEvent getSimplified()
  {
    if (mList.size() == 1) {
      return mList.get(0);
    } else {
      return this;
    }
  }


  //#########################################################################
  //# Local Class NestedIterator
  private class NestedIterator implements Iterator<CompiledSingleEvent>
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

    public CompiledSingleEvent next()
    {
      if (mListIterator != null) {
	final CompiledSingleEvent result = mInnerIterator.next();
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
	  final CompiledEvent value = mListIterator.next();
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
    private Iterator<CompiledEvent> mListIterator;
    private Iterator<CompiledSingleEvent> mInnerIterator;

  }


  //#########################################################################
  //# Data Members
  private final List<CompiledEvent> mList;
  private final int mAllowedKindMask;
  private int mKindMask;
  private boolean mIsObservable;

}
