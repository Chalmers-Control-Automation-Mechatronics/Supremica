//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.model.compiler
//# CLASS:   NondeterministicModuleException
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.model.compiler;

import net.sourceforge.waters.model.base.Proxy;
import net.sourceforge.waters.model.des.EventProxy;
import net.sourceforge.waters.model.des.StateProxy;
import net.sourceforge.waters.model.expr.EvalException;


/**
 * An exception thrown by the {@link ModuleCompiler} to indicate that
 * a graph compiles to a nondeterministic automaton although it is
 * declared to be deterministic.
 *
 * @author Robi Malik
 */

public class NondeterministicModuleException extends EvalException {

  //#########################################################################
  //# Constructors
  /**
   * Constructs a new exception with <CODE>null</CODE> as its detail message.
   */
  public NondeterministicModuleException()
  {
  }

  /**
   * Constructs a new exception with the specified detail message.
   */
  public NondeterministicModuleException(final String message)
  {
    super(message);
  }

  /**
   * Constructs a new exception with the specified detail message
   * and originating expression.
   */
  public NondeterministicModuleException(final String message,
                                         final Proxy location)
  {
    super(message, location);
  }

  /**
   * Constructs a new exception indicating that an automaton has more than
   * one initial state.
   * @param compname The name of the automaton that causes the problem.
   * @param state    One of the initial states.
   */
  public NondeterministicModuleException(final String compname,
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
  public NondeterministicModuleException(final String compname,
                                         final StateProxy state,
                                         final EventProxy event)
  {
    super("Multiple transitions labelled '" + event.getName() +
          "' originating from state '" + state.getName() +
          "' in automaton '" + compname + "'!", state);
  }
  
  
  //#########################################################################
  //# Static Class Variables
  public static final long serialVersionUID = 1;

}
