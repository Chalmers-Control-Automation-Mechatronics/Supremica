//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.model.analysis
//# CLASS:   SynchronousProductBuilder
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.model.analysis;


/**
 * <P>The synchronous product algorithm. A synchronous product builder
 * takes a finite-state machine model ({@link
 * net.sourceforge.waters.model.des.ProductDESProxy ProductDESProxy}) as input
 * and computes a single automaton representing the synchronous product
 * of all automata contained in the input model.</P>
 *
 * @author Robi Malik
 */

public interface SynchronousProductBuilder extends ModelAnalyser
{

  //#########################################################################
  //# More Specific Access to the Results
  /**
   * Gets a state map that can be used to decompose the states of the
   * computed synchronous product automaton and map them to the states
   * of the automata in the original model.
   */
  public SynchronousProductStateMap getStateMap();

}
