//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters/Supremica GUI
//# PACKAGE: net.sourceforge.waters.model.analysis
//# CLASS:   SynchrononousProductStateMap
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.analysis.gnonblocking;

import java.util.Collection;
import net.sourceforge.waters.model.des.StateProxy;


/**
 * Additional information to be returned by the observation equivalence
 * and similar abstraction rules to identify sets of original states after
 * merging.
 *
 * @author Rachel Francis
 */

public interface MergingStateMap
{
  /**
   * Gets the automaton that was simplified.
   */
  // public AutomatonProxy getInputAutomaton();

  /**
   * Gets the set of states of the original automaton, which were merged
   * into the given state.
   */
  public Collection<StateProxy> getOriginalStates(final StateProxy state);
}
