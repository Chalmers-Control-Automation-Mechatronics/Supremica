//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# Copyright (C) 2004-2019 Robi Malik
//###########################################################################
//# This file is part of Waters.
//# Waters is free software: you can redistribute it and/or modify it under
//# the terms of the GNU General Public License as published by the Free
//# Software Foundation, either version 2 of the License, or (at your option)
//# any later version.
//# Waters is distributed in the hope that it will be useful, but WITHOUT ANY
//# WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
//# FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
//# details.
//# You should have received a copy of the GNU General Public License along
//# with Waters. If not, see <http://www.gnu.org/licenses/>.
//#
//# Linking Waters statically or dynamically with other modules is making a
//# combined work based on Waters. Thus, the terms and conditions of the GNU
//# General Public License cover the whole combination.
//# In addition, as a special exception, the copyright holders of Waters give
//# you permission to combine Waters with code included in the standard
//# release of Supremica under the Supremica Software License Agreement (or
//# modified versions of such code, with unchanged license). You may copy and
//# distribute such a system following the terms of the GNU GPL for Waters and
//# the licenses of the other code concerned.
//# Note that people who make modified versions of Waters are not obligated to
//# grant this special exception for their modified versions; it is their
//# choice whether to do so. The GNU General Public License gives permission
//# to release a modified version without this exception; this exception also
//# makes it possible to release a modified version which carries forward this
//# exception.
//###########################################################################

package net.sourceforge.waters.plain.des;

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
