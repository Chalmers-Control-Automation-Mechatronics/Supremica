//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.subject.module
//# CLASS:   EventListExpressionSubject
//###########################################################################
//# $Id: EventListExpressionSubject.java,v 1.7 2006-09-20 16:24:13 robi Exp $
//###########################################################################

package net.sourceforge.waters.subject.module;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

import net.sourceforge.waters.model.base.EqualCollection;
import net.sourceforge.waters.model.base.Proxy;
import net.sourceforge.waters.model.module.EventListExpressionProxy;
import net.sourceforge.waters.model.unchecked.Casting;
import net.sourceforge.waters.subject.base.AbstractSubject;
import net.sourceforge.waters.subject.base.ArrayListSubject;
import net.sourceforge.waters.subject.base.ListSubject;


/**
 * The subject implementation of the {@link EventListExpressionProxy} interface.
 *
 * @author Robi Malik
 */

public abstract class EventListExpressionSubject
  extends ExpressionSubject
  implements EventListExpressionProxy
{

  //#########################################################################
  //# Constructors
  /**
   * Creates a new event list expression.
   * @param eventList The list of events of the new event list expression, or <CODE>null</CODE> if empty.
   */
  protected EventListExpressionSubject(final Collection<? extends Proxy> eventList)
  {
    if (eventList == null) {
      mEventList = new ArrayListSubject<AbstractSubject>();
    } else {
      mEventList = new ArrayListSubject<AbstractSubject>
        (eventList, AbstractSubject.class);
    }
    mEventList.setParent(this);
  }

  /**
   * Creates a new event list expression using default values.
   * This constructor creates an event list expression with
   * an empty list of events.
   */
  protected EventListExpressionSubject()
  {
    this(null);
  }


  //#########################################################################
  //# Cloning
  public EventListExpressionSubject clone()
  {
    final EventListExpressionSubject cloned = (EventListExpressionSubject) super.clone();
    cloned.mEventList = mEventList.clone();
    cloned.mEventList.setParent(cloned);
    return cloned;
  }


  //#########################################################################
  //# Equality and Hashcode
  public boolean equalsByContents(final Proxy partner)
  {
    if (super.equalsByContents(partner)) {
      final EventListExpressionSubject downcast = (EventListExpressionSubject) partner;
      return
        EqualCollection.isEqualListByContents
          (mEventList, downcast.mEventList);
    } else {
      return false;
    }
  }

  public int hashCodeByContents()
  {
    int result = super.hashCodeByContents();
    result *= 5;
    result += EqualCollection.getListHashCodeByContents(mEventList);
    return result;
  }


  //#########################################################################
  //# Interface net.sourceforge.waters.model.module.EventListExpressionProxy
  public List<Proxy> getEventList()
  {
    final List<Proxy> downcast = Casting.toList(mEventList);
    return Collections.unmodifiableList(downcast);
  }


  //#########################################################################
  //# Setters
  /**
   * Gets the modifiable list of events consituting this event list expression.
   */
  public ListSubject<AbstractSubject> getEventListModifiable()
  {
    return mEventList;
  }


  //#########################################################################
  //# Data Members
  private ListSubject<AbstractSubject> mEventList;

}
