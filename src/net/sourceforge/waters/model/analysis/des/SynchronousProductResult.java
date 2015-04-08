//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.model.analysis.des
//# CLASS:   SynchronousProductResult
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.model.analysis.des;


/**
 * A automaton result record returned by synchronous product and
 * similar computations. In addition to the computed automaton, the
 * synchronous product result contains a state map that links the states
 * in a synchronous composition to the states of the automata in the
 * original model.
 *
 * @author Robi Malik
 */

public interface SynchronousProductResult
  extends AutomatonResult
{

  //#########################################################################
  //# Simple Access Methods
  /**
   * Gets a state map that can be used to decompose the states of the
   * computed synchronous product automaton and map them to the states
   * of the automata in the original model.
   */
  public SynchronousProductStateMap getStateMap();

  /**
   * Sets the synchronous product state map for this result.
   */
  public void setStateMap(final SynchronousProductStateMap map);

}
