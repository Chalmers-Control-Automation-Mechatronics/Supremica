//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.plain.module
//# CLASS:   EventListExpressionElement
//###########################################################################
//# $Id: EventListExpressionElement.java,v 1.6 2006-07-20 02:28:37 robi Exp $
//###########################################################################

package net.sourceforge.waters.plain.module;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import net.sourceforge.waters.model.base.EqualCollection;
import net.sourceforge.waters.model.base.Proxy;
import net.sourceforge.waters.model.module.EventListExpressionProxy;


/**
 * An immutable implementation of the {@link EventListExpressionProxy} interface.
 *
 * @author Robi Malik
 */

public abstract class EventListExpressionElement
  extends ExpressionElement
  implements EventListExpressionProxy
{

  //#########################################################################
  //# Constructors
  /**
   * Creates a new event list expression.
   * @param eventList The list of events of the new event list expression, or <CODE>null</CODE> if empty.
   */
  protected EventListExpressionElement(final Collection<? extends Proxy> eventList)
  {
    if (eventList == null) {
      mEventList = Collections.emptyList();
    } else {
      final List<Proxy> eventListModifiable =
        new ArrayList<Proxy>(eventList);
      mEventList =
        Collections.unmodifiableList(eventListModifiable);
    }
  }

  /**
   * Creates a new event list expression using default values.
   * This constructor creates an event list expression with
   * an empty list of events.
   */
  protected EventListExpressionElement()
  {
    this(emptyProxyList());
  }


  //#########################################################################
  //# Cloning
  public EventListExpressionElement clone()
  {
    return (EventListExpressionElement) super.clone();
  }


  //#########################################################################
  //# Equality and Hashcode
  public boolean equalsByContents(final Proxy partner)
  {
    if (super.equalsByContents(partner)) {
      final EventListExpressionElement downcast = (EventListExpressionElement) partner;
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
    return mEventList;
  }


  //#########################################################################
  //# Auxiliary Methods
  private static List<Proxy> emptyProxyList()
  {
    return Collections.emptyList();
  }


  //#########################################################################
  //# Data Members
  private final List<Proxy> mEventList;

}
