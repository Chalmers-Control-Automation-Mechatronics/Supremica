//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# Copyright (C) 2004-2015 Robi Malik
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

import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.Map;

import net.sourceforge.waters.model.base.ProxyVisitor;
import net.sourceforge.waters.model.base.VisitorException;
import net.sourceforge.waters.model.des.AutomatonProxy;
import net.sourceforge.waters.model.des.EventProxy;
import net.sourceforge.waters.model.des.ProductDESProxyVisitor;
import net.sourceforge.waters.model.des.StateProxy;
import net.sourceforge.waters.model.des.TraceStepProxy;
import net.sourceforge.waters.plain.base.Element;


/**
 * A step in a trace.
 * This is a simple immutable implementation of the {@link TraceStepProxy}
 * interface.
 *
 * @author Robi Malik
 */

public final class TraceStepElement
  extends Element
  implements TraceStepProxy
{

  //#########################################################################
  //# Constructors
  /**
   * Creates a new trace step.
   * @param  event        The event associated with the new step,
   *                      or <CODE>null</CODE>.
   * @param  statemap     The map that maps automata mentioned by the trace
   *                      to their states reached after the step represented
   *                      by this object. This map is copied when creating
   *                      the step object, so later changes to it will have
   *                      no impact on the object. This parameter may be
   *                      <CODE>null</CODE> for an empty map.
   */
  TraceStepElement(final EventProxy event,
                   final Map<AutomatonProxy,StateProxy> statemap)
  {
    mEvent = event;
    if (statemap == null) {
      mStateMap = Collections.emptyMap();
    } else {
      final Map<AutomatonProxy,StateProxy> modifiable =
        new IdentityHashMap<AutomatonProxy,StateProxy>(statemap);
      mStateMap = Collections.unmodifiableMap(modifiable);
    }
  }

  /**
   * Creates a new trace step with an empty state map.
   * @param  event        The event associated with the new step.
   */
  TraceStepElement(final EventProxy event)
  {
    this(event, null);
  }


  //#########################################################################
  //# Interface java.lang.Cloneable
  /**
   * Returns a copy of this trace step.
   */
  public TraceStepElement clone()
  {
    return (TraceStepElement) super.clone();
  }


  //#########################################################################
  //# Interface net.sourceforge.waters.model.base.Proxy
  public Object acceptVisitor(final ProxyVisitor visitor)
    throws VisitorException
  {
    final ProductDESProxyVisitor desvisitor = (ProductDESProxyVisitor) visitor;
    return desvisitor.visitTraceStepProxy(this);
  }


  //#########################################################################
  //# Interface net.sourceforge.waters.model.des.TraceStepProxy
  public EventProxy getEvent()
  {
    return mEvent;
  }

  public Map<AutomatonProxy,StateProxy> getStateMap()
  {
    return mStateMap;
  }


  //#########################################################################
  //# Equals and Hashcode
  public Class<TraceStepProxy> getProxyInterface()
  {
    return TraceStepProxy.class;
  }


  //#########################################################################
  //# Data Members
  private final EventProxy mEvent;
  private final Map<AutomatonProxy,StateProxy> mStateMap;


  //#########################################################################
  //# Class Constants
  private static final long serialVersionUID = 1L;

}








