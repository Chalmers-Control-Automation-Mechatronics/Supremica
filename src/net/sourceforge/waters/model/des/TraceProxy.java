//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.model.des
//# CLASS:   TraceProxy
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.model.des;

import java.util.List;
import java.util.Set;

import net.sourceforge.waters.model.base.DocumentProxy;


/**
 * <P>A counterexample trace for some automata of a product DES.</P>
 *
 * @see ProductDESProxy
 *
 * @author Robi Malik
 */

public interface TraceProxy
  extends DocumentProxy
{

  //#########################################################################
  //# Getters
  /**
   * Gets the product DES for which this trace has been generated.
   */
  public ProductDESProxy getProductDES();

  /**
   * Gets the list of automata for this trace.
   * @return  An unmodifiable set of objects of type {@link AutomatonProxy}.
   */
  public Set<AutomatonProxy> getAutomata();

  /**
   * Gets the sequence of events constituting this trace.
   * @return  An unmodifiable list of objects of type {@link EventProxy}.
   */
  public List<EventProxy> getEvents();

  /**
   * Gets the sequence of states and events constituting this trace.
   * This method returns a list of {@link TraceStepProxy} objects,
   * each consisting of a pair of incoming event and target state.
   * The first entry has a <CODE>null</CODE> event and the initial state as
   * its target state, while every other entry has a non-null event
   * and the state reached after that event.
   * @return  An unmodifiable list of objects of type {@link TraceStepProxy}.
   */
  public List<TraceStepProxy> getTraceSteps();

}
