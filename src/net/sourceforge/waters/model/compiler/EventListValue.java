//###########################################################################
//# PROJECT: Waters
//# PACKAGE: waters.model.compiler
//# CLASS:   EventListValue
//###########################################################################
//# $Id: EventListValue.java,v 1.1 2005-02-17 01:43:35 knut Exp $
//###########################################################################

package net.sourceforge.waters.model.compiler;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.TreeSet;

import net.sourceforge.waters.model.module.ColorGeometryProxy;
import net.sourceforge.waters.model.module.EventDeclProxy;
import net.sourceforge.waters.xsd.base.EventKind;


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

class EventListValue implements EventValue, EventValueConsumer
{
  //#########################################################################
  //# Constructor
  EventListValue()
  {
    mList = new ArrayList();
    mKind = null;
    mColorGeometry = null;
  }
  
  EventListValue(final int initsize)
  {
    mList = new ArrayList(initsize);
    mKind = null;
    mColorGeometry = null;
  }


  //#########################################################################
  //# Overrides for java.lang.Object
  public String toString()
  {
    final StringBuffer buffer = new StringBuffer("{");
    final Iterator iter = mList.iterator();
    while (iter.hasNext()) {
      final EventValue value = (EventValue) iter.next();
      buffer.append(value);
      if (iter.hasNext()) {
	buffer.append(", ");
      }
    }
    buffer.append("}");
    return buffer.toString();
  }

  public int hashCode()
  {
    return mList.hashCode();
  }

  public boolean equals(final Object partner)
  {
    if (partner != null && getClass() == partner.getClass()) {
      final EventListValue value = (EventListValue) partner;
      return mList.equals(value.mList);
    } else {
      return false;
    }    
  }


  //#########################################################################
  //# Interface net.sourceforge.waters.model.compiler.EventValueConsumer
  public void processValue(final EventValue value)
    throws EventKindException
  {
    if (mKind != value.getKind()) {
      if (mKind == null) {
	mKind = value.getKind();
      } else if (value.getKind() == null) {
	// nothing
      } else if (mKind == EventKind.CONTROLLABLE &&
		 value.getKind() == EventKind.UNCONTROLLABLE) {
	mKind = EventKind.UNCONTROLLABLE;
      } else if (mKind == EventKind.UNCONTROLLABLE &&
		 value.getKind() == EventKind.CONTROLLABLE) {
	// nothing
      } else {
	throw new EventKindException
	  ("Trying to mix to event of kind" + value.getKind() +
	   " to list of kind " + mKind + "!");
      }
    }
    mList.add(value);
  }


  //#########################################################################
  //# Interface net.sourceforge.waters.model.compiler.EventValue
  public Iterator getEventIterator()
  {
    return new NestedIterator();
  }

  public EventKind getKind()
  {
    return mKind;
  }

  public ColorGeometryProxy getColorGeometry()
  {
    if (mColorGeometry == null && mKind == EventKind.PROPOSITION) {
      final Iterator iter = getEventIterator();
      final Set colors = new TreeSet();
      while (iter.hasNext()) {
	final SingleEventValue event = (SingleEventValue) iter.next();
	final ColorGeometryProxy geo = event.getColorGeometry();
	if (geo != null) {
	  final Set geocolors = geo.getColorSet();
	  colors.addAll(geocolors);
	}
      }
      mColorGeometry = new ColorGeometryProxy(colors);
    }
    return mColorGeometry;
  }

  public void checkParameterType(final EventDeclProxy decl)
    throws EventKindException
  {
    final EventKind kind = getKind();
    final EventKind declkind = decl.getKind();
    if (kind != null && kind != declkind) {
      throw new EventKindException
	("Can't assign event list of kind " + kind + " to parameter '" +
	 decl.getName() + "' of kind " + declkind + "!");
    } else if (decl.getArity() != 0) {
      throw new EventKindException
	("Can't assign event list to parameter '" +
	 decl.getName() + "': an array is expected!");
    }
  }


  //#########################################################################
  //# Simplification
  EventValue getSimplified()
  {
    if (mList.size() == 1) {
      return (EventValue) mList.get(0);
    } else {
      return this;
    }
  }


  //#########################################################################
  //# Local Class NestedIterator
  private class NestedIterator implements Iterator
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

    public Object next()
    {
      if (mListIterator != null) {
	final Object result = mInnerIterator.next();
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
	  final EventValue value = (EventValue) mListIterator.next();
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
    private Iterator mListIterator;
    private Iterator mInnerIterator;

  }


  //#########################################################################
  //# Data Members
  private final List mList;
  private EventKind mKind;
  private ColorGeometryProxy mColorGeometry;

}
