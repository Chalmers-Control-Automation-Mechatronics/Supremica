//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.plain.des
//# CLASS:   TraceStepElement
//###########################################################################
//# $Id: TraceStepElement.java,v 1.2 2006-07-20 02:28:37 robi Exp $
//###########################################################################

package net.sourceforge.waters.plain.des;

import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.Map;

import net.sourceforge.waters.model.base.Proxy;
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
  public boolean equalsByContents(final Proxy partner)
  {
    if (super.equalsByContents(partner)) {
      final TraceStepElement step = (TraceStepElement) partner;
      return
        (mEvent == step.mEvent) &&
        mStateMap.equals(step.mStateMap);
    } else {
      return false;
    }    
  }

  public int hashCodeByContents()
  {
    return
      super.hashCodeByContents() +
      5 * mEvent.hashCode() +
      5 * mStateMap.hashCode();
  }


  //#########################################################################
  //# Data Members
  private final EventProxy mEvent;
  private final Map<AutomatonProxy,StateProxy> mStateMap;

}
