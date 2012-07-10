
package org.supremica.automata;

import net.sourceforge.waters.model.analysis.InvalidModelException;
import net.sourceforge.waters.model.module.EventDeclProxy;
import net.sourceforge.waters.model.module.NodeProxy;

/**
 * NondeterministicEFAException for Extended Automaton models
 * 
 * @author Mohammad Reza Shoaei (shoaei@chalmers.se)
 * @version %I%, %G%
 * @since 1.0
 */
public class NondeterministicEFAException extends InvalidModelException {
  //#########################################################################
  //# Constructors
  /**
   * Constructs a new exception with <CODE>null</CODE> as its detail message.
   */
  public NondeterministicEFAException()
  {
  }

  /**
   * Constructs a new exception with the given detail message.
   */
  public NondeterministicEFAException(final String msg)
  {
    super(msg);
  }

  /**
   * Constructs a new exception indicating that an automaton has more than
   * one initial state.
   * @param efa      The extended automaton that causes the problem.
   */
  public NondeterministicEFAException(final ExtendedAutomaton efa)
  {
    super("EFA '" + efa.getName() +
          "' has more than one initial state!");
  }

  /**
   * Constructs a new exception indicating that a state has more than one
   * outgoing transition with the same event.
   * @param efa      The extended automaton that causes the problem.
   * @param state    The state with nondeterministic outgoing transitions.
   * @param event    The event that causes the trouble.
   */
  public NondeterministicEFAException(final ExtendedAutomaton efa,
                                      final NodeProxy state,
                                      final EventDeclProxy event)
  {
    super("Multiple transitions labelled '" + event.getName() +
          "' originating from state '" + state.getName() +
          "' in EFA '" + efa.getName() + "'!");
  }


  //#########################################################################
  //# Static Class Variables
  public static final long serialVersionUID = 1;    
}
