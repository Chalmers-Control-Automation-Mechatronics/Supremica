//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.model.des
//# CLASS:   TraceStepProxy
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.model.des;

import java.util.Map;

import net.sourceforge.waters.model.base.Proxy;


/**
 * <P>A step in a trace.</P>
 *
 * @author Robi Malik
 */

public interface TraceStepProxy
  extends Proxy
{

  //#########################################################################
  //# Getters
  /**
   * Gets the event associated with this trace step.
   */
  public EventProxy getEvent();

  /**
   * Gets the target states reached after this trace step.
   * This method returns a map that maps automata mentioned by the trace
   * to their states reached after the step represented by this object.
   * The map only is guaranteed to contain entries for nondeterministic
   * transitions, to disambiguate their target state. In all other cases,
   * it may be undefined for the associated automaton. The map may include
   * <CODE>null</CODE> values as target states, to indicate that no target
   * state can be reached in the corresponding automaton. This can be used
   * to represent the final step in a safety trace.
   * @return An unmodifiable map.
   */
  public Map<AutomatonProxy,StateProxy> getStateMap();

}
