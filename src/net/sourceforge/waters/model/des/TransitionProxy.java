//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.model.des
//# CLASS:   TransitionProxy
//###########################################################################
//# $Id: TransitionProxy.java,v 1.3 2005-11-03 01:24:16 robi Exp $
//###########################################################################

package net.sourceforge.waters.model.des;

import net.sourceforge.waters.model.base.ComparableProxy;

/**
 * A transition of an automaton.
 * This class represents simple transitions labelled by a single event,
 * leading from a source state to a target state.
 *
 * @author Robi Malik
 */

public interface TransitionProxy
  extends ComparableProxy<TransitionProxy>
{

  //#########################################################################
  //# Getters and Setters
  /**
   * Gets the source state of this transition.
   */
  public StateProxy getSource();

  /**
   * Gets the event associated with this transition.
   * Events associated with a transition are always either
   * controllable or uncontrollable; propositions cannot appear
   * in this context.
   */
  public EventProxy getEvent();

  /**
   * Gets the target state of this transition.
   */
  public StateProxy getTarget();

}
