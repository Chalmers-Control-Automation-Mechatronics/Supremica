//###########################################################################
//# PROJECT: Waters
//# PACKAGE: waters.model.compiler
//# CLASS:   SingleEventValue
//###########################################################################
//# $Id: SingleEventValue.java,v 1.1 2005-02-17 01:43:35 knut Exp $
//###########################################################################

package net.sourceforge.waters.model.compiler;

import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

import net.sourceforge.waters.model.des.EventProxy;
import net.sourceforge.waters.model.module.ColorGeometryProxy;
import net.sourceforge.waters.model.module.EventDeclProxy;
import net.sourceforge.waters.xsd.base.EventKind;


class SingleEventValue implements EventValue
{

  //#######################################################################
  //# Constructor
  SingleEventValue(final CompiledEventDecl decl, final List indexes)
  {
    mDecl = decl;
    mIndexes = indexes;
    mEvent = null;
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
      final SingleEventValue value = (SingleEventValue) partner;
      return mDecl == value.mDecl && mIndexes.equals(value.mIndexes);
    } else {
      return false;
    }    
  }


  //#######################################################################
  //# Interface net.sourceforge.waters.model.compiler.EventValue
  public Iterator getEventIterator()
  {
    return new SingleEventIterator(this);
  }

  public EventKind getKind()
  {
    return mDecl.getKind();
  }

  public ColorGeometryProxy getColorGeometry()
  {
    return mDecl.getColorGeometry();
  }

  public void checkParameterType(final EventDeclProxy decl)
    throws EventKindException
  {
    if (getKind() != decl.getKind()) {
      throw new EventKindException
	("Can't assign event '" + toString() + "' to parameter '" +
	 decl.getName() + ": expected " + decl.getKind() + ", got " +
	 getKind() + "!");
    } else if (decl.getArity() != 0) {
      throw new EventKindException
	("Can't assign event '" + toString() + "' to parameter '" +
	 decl.getName() + ": is not an array!");
    }
  }


  //#######################################################################
  //# Creating the Event
  EventProxy getEvent()
  {
    if (mEvent == null) {
      mEvent = mDecl.getEvent(mIndexes);
    }
    return mEvent;
  }


  //#########################################################################
  //# Local Class SingleEventIterator
  /**
   * An iterator over all events referred to by a SingleEventValue object.
   * This iterator produces only a single value of type EventProxy,
   * identifying the one event contained in the SingleEventValue object.
   */
  private static class SingleEventIterator implements Iterator
  {

    //#######################################################################
    //# Constructor
    SingleEventIterator(final SingleEventValue value)
    {
      mValue = value;
    }

    //#######################################################################
    //# Interface java.util.Iterator
    public boolean hasNext()
    {
      return mValue != null;
    }

    public Object next()
    {
      if (mValue != null) {
	final SingleEventValue value = mValue;
	mValue = null;
	return value.getEvent();
      } else {
	throw new NoSuchElementException
	  ("SingleEventIterator has already returned its only element!");
      }
    }

    public void remove()
    {
      throw new UnsupportedOperationException
	("SingleEventIterator does not support remove() operation!");
    } 

    //#######################################################################
    //# Data Members
    private SingleEventValue mValue;

  }


  //#######################################################################
  //# Data Members
  private final CompiledEventDecl mDecl;
  private final List mIndexes;
  private EventProxy mEvent;

}
