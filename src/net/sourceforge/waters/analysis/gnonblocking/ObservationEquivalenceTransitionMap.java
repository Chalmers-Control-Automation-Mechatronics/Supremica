//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters/Supremica GUI
//# PACKAGE: net.sourceforge.waters.model.analysis
//# CLASS:   SynchrononousProductStateMap
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.analysis.gnonblocking;

import java.util.List;

import net.sourceforge.waters.model.des.StateProxy;
import net.sourceforge.waters.model.des.TransitionProxy;


/**
 * Additional information to be returned by the observation equivalence
 * abstraction rule to decompose transitions into their original components.
 *
 * @author Rachel Francis
 */

public interface ObservationEquivalenceTransitionMap
{
  /**
   * Gets the collection of automata composed.
   */
  // public Collection<AutomatonProxy> getInputAutomata();

  /**
   * Gets a list of transitions of an original automaton which were replaced.
   */
  public List<TransitionProxy> getOriginalTransitions(final StateProxy source,
                                                      final StateProxy target);
}
