//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters/Supremica GUI
//# PACKAGE: net.sourceforge.waters.model.analysis
//# CLASS:   SynchrononousProductStateMap
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.model.analysis;

import java.util.Collection;

import net.sourceforge.waters.model.des.AutomatonProxy;
import net.sourceforge.waters.model.des.StateProxy;


/**
 * Additional information to be returned by the synchronous product
 * algorithm to decompose states into their original components.
 *
 * @author Robi Malik
 */

public interface SynchronousProductStateMap
{
  /**
   * Gets the collection of automata composed.
   */
  public Collection<AutomatonProxy> getInputAutomata();

  /**
   * Gets the state of an original automaton corresponding to a given
   * state tuple.
   * @param  tuple  The synchronous product state to be examined.
   * @param  aut    The automaton of the original model for which a state
   *                is requested. This automaton must be contained in the
   *                collection returned by {@link #getInputAutomata()}.
   * @return The state of automaton <CODE>aut</CODE> that corresponds to the
   *         given state tuple <CODE>tuple</CODE>.
   */
  public StateProxy getOriginalState(final StateProxy tuple,
                                     final AutomatonProxy aut);
}
