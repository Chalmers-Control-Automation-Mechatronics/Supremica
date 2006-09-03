//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.model.compiler
//# CLASS:   NondeterminismException
//###########################################################################
//# $Id: NondeterminismException.java,v 1.3 2006-09-03 06:38:43 robi Exp $
//###########################################################################

package net.sourceforge.waters.model.compiler;

import net.sourceforge.waters.model.base.Proxy;
import net.sourceforge.waters.model.des.EventProxy;
import net.sourceforge.waters.model.des.StateProxy;
import net.sourceforge.waters.model.expr.EvalException;


public class NondeterminismException extends EvalException {

  //#########################################################################
  //# Constructors
  /**
   * Constructs a new exception with <CODE>null</CODE> as its detail message.
   */
  public NondeterminismException()
  {
  }

  /**
   * Constructs a new exception with the specified detail message.
   */
  public NondeterminismException(final String message)
  {
    super(message);
  }

  /**
   * Constructs a new exception with the specified detail message
   * and originating expression.
   */
  public NondeterminismException(final String message, final Proxy location)
  {
    super(message, location);
  }

  /**
   * Constructs a new exception indicating that an automaton has more than
   * one initial state.
   * @param compname The name of the automaton that causes the problem.
   * @param state    One of the initial states.
   */
  public NondeterminismException(final String compname,
                                 final StateProxy state)
  {
    super("Automaton '" + compname + "' has more than one initial state!",
          state);
  }

  /**
   * Constructs a new exception indicating that a state has more than one
   * outgoing transition with the same event.
   * @param compname The name of the automaton that causes the problem.
   * @param state    The state with nondeterministic outgoing transitions.
   * @param event    The event that causes the trouble.
   */
  public NondeterminismException(final String compname,
                                 final StateProxy state,
                                 final EventProxy event)
  {
    super("Multiple transitions labelled '" + event.getName() +
          "' originating from state '" + state.getName() +
          "' in automaton '" + compname + "'!", state);
  }

}