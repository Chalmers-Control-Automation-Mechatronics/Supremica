//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.model.analysis
//# CLASS:   NondeterministicDESException
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.model.analysis;

import net.sourceforge.waters.model.des.AutomatonProxy;
import net.sourceforge.waters.model.des.EventProxy;
import net.sourceforge.waters.model.des.StateProxy;


/**
 * An exception indicating that nondeterminism has been detected in
 * a model by an analyser that requires deterministic input.
 *
 * @author Robi Malik
 */

public class NondeterministicDESException extends AnalysisException {

  //#########################################################################
  //# Constructors
  /**
   * Constructs a new exception with <CODE>null</CODE> as its detail message.
   */
  public NondeterministicDESException()
  {
  }

  /**
   * Constructs a new exception indicating that an automaton has more than
   * one initial state.
   * @param aut      The automaton that causes the problem.
   * @param state    One of the initial states.
   */
  public NondeterministicDESException(final AutomatonProxy aut,
                                      final StateProxy state)
  {
    super("Automaton '" + aut.getName() +
          "' has more than one initial state!");
  }

  /**
   * Constructs a new exception indicating that a state has more than one
   * outgoing transition with the same event.
   * @param aut      The automaton that causes the problem.
   * @param state    The state with nondeterministic outgoing transitions.
   * @param event    The event that causes the trouble.
   */
  public NondeterministicDESException(final AutomatonProxy aut,
                                      final StateProxy state,
                                      final EventProxy event)
  {
    super("Multiple transitions labelled '" + event.getName() +
          "' originating from state '" + state.getName() +
          "' in automaton '" + aut.getName() + "'!");
  }
  
  
  //#########################################################################
  //# Static Class Variables
  public static final long serialVersionUID = 1;

}
