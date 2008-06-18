//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.model.compiler.instance
//# CLASS:   EventOutputIterable
//###########################################################################
//# $Id: EventOutputIterable.java,v 1.1 2008-06-18 09:35:34 robi Exp $
//###########################################################################

package net.sourceforge.waters.model.compiler.instance;

import java.util.Iterator;
import java.util.NoSuchElementException;

import net.sourceforge.waters.model.base.Proxy;
import net.sourceforge.waters.model.compiler.context.AliasBindingContext;
import net.sourceforge.waters.model.compiler.context.BindingContext;
import net.sourceforge.waters.model.compiler.context.SourceInfo;


/**
 * The compiler-internal algorithm to obtain a list of event output objects
 * ({@link SingleEventOutput}) from an any given compiled event ({@link
 * CompiledEvent}).
 *
 * @author Robi Malik
 */

class EventOutputIterable implements Iterable<SingleEventOutput>
{

  //#########################################################################
  //# Constructor
  EventOutputIterable(final CompiledEvent event)
  {
    mEvent = event;
  }


  //#########################################################################
  //# Interface java.lang.Iterable
  public Iterator<SingleEventOutput> iterator()
  {
    return createEventOutputIterator(mEvent);
  }


  //#########################################################################
  //# Auxiliary Methods
  private static Iterator<SingleEventOutput> createEventOutputIterator
    (final CompiledEvent event)
  {
    return createEventOutputIterator(event, null);
  }

  private static Iterator<SingleEventOutput> createEventOutputIterator
    (final CompiledEvent event, SourceInfo outerinfo)
  {
    final SourceInfo localinfo = event.getSourceInfo();
    final SourceInfo info;
    if (localinfo == null) {
      info = outerinfo;
    } else if (outerinfo == null) {
      info = localinfo;
    } else {
      final Proxy source = localinfo.getSourceObject();
      final BindingContext context = localinfo.getBindingContext();
      final BindingContext alias = new AliasBindingContext(outerinfo, context);
      info = new SourceInfo(source, alias);
    }
    if (event instanceof CompiledSingleEvent) {
      final CompiledSingleEvent single = (CompiledSingleEvent) event;
      return new SingleEventOutputIterator(single, info);
    } else {
      return new NestedEventOutputIterator(event, info);
    }
  }


  //#########################################################################
  //# Local Class SingleEventOutputIterator
  private static class SingleEventOutputIterator
    implements Iterator<SingleEventOutput>
  {

    //#######################################################################
    //# Constructor
    SingleEventOutputIterator(final CompiledSingleEvent event,
                              final SourceInfo info)
    {
      mOutput = new SingleEventOutput(event, info);
    }

    //#######################################################################
    //# Interface java.util.Iterator
    public boolean hasNext()
    {
      return mOutput != null;
    }

    public SingleEventOutput next()
    {
      if (mOutput != null) {
        final SingleEventOutput output = mOutput;
        mOutput = null;
	return output;
      } else {
	throw new NoSuchElementException
	  ("No more events in single event output iteration!");
      }
    }
	
    public void remove()
    {
      throw new UnsupportedOperationException
	("Can't remove from single event output iteration!");
    }

    //#######################################################################
    //# Data Members
    private SingleEventOutput mOutput;
  }


  //#########################################################################
  //# Local Class NestedEventOutputIterator
  private static class NestedEventOutputIterator
    implements Iterator<SingleEventOutput>
  {

    //#######################################################################
    //# Constructor
    NestedEventOutputIterator(final CompiledEvent event,
                              final SourceInfo info)
    {
      mSourceInfo = info;
      mOuterIterator = event.getChildrenIterator();
      mInnerIterator = null;
      advance();
    }

    //#######################################################################
    //# Interface java.util.Iterator
    public boolean hasNext()
    {
      return mOuterIterator != null;
    }

    public SingleEventOutput next()
    {
      if (mOuterIterator != null) {
	final SingleEventOutput output = mInnerIterator.next();
	advance();
	return output;
      } else {
	throw new NoSuchElementException
	  ("No more events in nested event output iteration!");
      }
    }
	
    public void remove()
    {
      throw new UnsupportedOperationException
	("Can't remove from nested event output iteration!");
    }

    //#######################################################################
    //# Auxiliary Methods
    private void advance()
    {
      while (mInnerIterator == null || !mInnerIterator.hasNext()) {
	if (mOuterIterator.hasNext()) {
	  final CompiledEvent event = mOuterIterator.next();
	  mInnerIterator = createEventOutputIterator(event, mSourceInfo);
	} else {
	  mOuterIterator = null;
	  mInnerIterator = null;
	  return;
	}
      }
    }

    //#######################################################################
    //# Data Members
    private final SourceInfo mSourceInfo;
    private Iterator<? extends CompiledEvent> mOuterIterator;
    private Iterator<SingleEventOutput> mInnerIterator;

  }


  //#########################################################################
  //# Data Members
  private final CompiledEvent mEvent;

}
