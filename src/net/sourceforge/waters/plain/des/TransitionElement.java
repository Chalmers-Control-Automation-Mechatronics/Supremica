//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.plain.des
//# CLASS:   TransitionElement
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.plain.des;

import net.sourceforge.waters.model.base.Proxy;
import net.sourceforge.waters.model.base.ProxyVisitor;
import net.sourceforge.waters.model.base.VisitorException;
import net.sourceforge.waters.model.des.EventProxy;
import net.sourceforge.waters.model.des.ProductDESProxyVisitor;
import net.sourceforge.waters.model.des.StateProxy;
import net.sourceforge.waters.model.des.TransitionProxy;
import net.sourceforge.waters.plain.base.Element;


/**
 * A transition of an automaton.
 * This is a simple immutable implementation of the {@link TransitionProxy}
 * interface.
 *
 * @author Robi Malik
 */

public final class TransitionElement
  extends Element
  implements TransitionProxy
{

  //#########################################################################
  //# Constructors
  /**
   * Creates a transition.
   * @param  source       The source state of the new transition.
   * @param  target       The target state of the new transition.
   * @param  event        The event associated with the new transition,
   *                      which must be controllable or uncontrollable.
   */
  TransitionElement(final StateProxy source,
                    final EventProxy event,
                    final StateProxy target)
  {
    mSource = source;
    mTarget = target;
    mEvent = event;
  }


  //#########################################################################
  //# Interface java.lang.Cloneable
  /**
   * Returns a copy of this transition.
   */
  public TransitionElement clone()
  {
    return (TransitionElement) super.clone();
  }


  //#########################################################################
  //# Interface net.sourceforge.waters.model.base.Proxy
  public Object acceptVisitor(final ProxyVisitor visitor)
    throws VisitorException
  {
    final ProductDESProxyVisitor desvisitor = (ProductDESProxyVisitor) visitor;
    return desvisitor.visitTransitionProxy(this);
  }


  //#########################################################################
  //# Interface net.sourceforge.waters.model.base.TransitionProxy
  public StateProxy getSource()
  {
    return mSource;
  }

  public StateProxy getTarget()
  {
    return mTarget;
  }

  public EventProxy getEvent()
  {
    return mEvent;
  }


  //#########################################################################
  //# Equals and Hashcode
  public Class<TransitionProxy> getProxyInterface()
  {
    return TransitionProxy.class;
  }

  /**
   * Checks whether this transition is equal to another.
   * Two transitions are considered as equal if their source and target
   * states, and their events all have the same names.
   */
  public boolean equalsByContents(final Proxy partner)
  {
    if (super.equalsByContents(partner)) {
      final TransitionProxy trans = (TransitionProxy) partner;
      return
        getSource().refequals(trans.getSource()) &&
        getTarget().refequals(trans.getTarget()) &&
        getEvent().refequals(trans.getEvent());
    } else {
      return false;
    }    
  }

  public int hashCodeByContents()
  {
    return
      getSource().refHashCode() +
      5 * getEvent().refHashCode() +
      25 * getTarget().refHashCode();
  }


  //#########################################################################
  //# Interface java.lang.Comparable
  public int compareTo(final TransitionProxy trans)
  {
    final int compsource = getSource().compareTo(trans.getSource());
    if (compsource != 0) {
      return compsource;
    }
    final int compevent = getEvent().compareTo(trans.getEvent());
    if (compevent != 0) {
      return compevent;
    }
    return getTarget().compareTo(trans.getTarget());
  }

 
  //#########################################################################
  //# Data Members
  private final StateProxy mSource;
  private final StateProxy mTarget;
  private final EventProxy mEvent;


  //#########################################################################
  //# Class Constants
  private static final long serialVersionUID = 1L;

}
