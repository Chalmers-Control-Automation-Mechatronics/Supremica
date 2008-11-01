//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.model.compiler.graph
//# CLASS:   NondeterministicModuleException
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.model.compiler.graph;

import net.sourceforge.waters.model.base.Proxy;
import net.sourceforge.waters.model.des.EventProxy;
import net.sourceforge.waters.model.expr.EvalException;
import net.sourceforge.waters.model.module.SimpleComponentProxy;
import net.sourceforge.waters.model.module.SimpleNodeProxy;


/**
 * An exception thrown by the {@link ModuleGraphCompiler} to indicate that
 * a graph compiles to a nondeterministic automaton although it is
 * declared to be deterministic.
 *
 * @author Robi Malik
 */

public class NondeterministicModuleException extends EvalException {

  //#########################################################################
  //# Constructors
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
   * @param comp     The simple component that causes the problem.
   * @param node     One of the initial states.
   */
  public NondeterministicModuleException(final SimpleComponentProxy comp,
                                         final SimpleNodeProxy node)
  {
    super("Automaton '" + comp.getName() +
          "' has more than one initial state!", node);
  }

  /**
   * Constructs a new exception indicating that a state has more than one
   * outgoing transition with the same event.
   * @param comp     The simple component that causes the problem.
   * @param node     The state with nondeterministic outgoing transitions.
   * @param event    The event that causes the trouble.
   */
  public NondeterministicModuleException(final SimpleComponentProxy comp,
                                         final SimpleNodeProxy node,
                                         final EventProxy event)
  {
    super("Multiple transitions labelled '" + event.getName() +
          "' originating from state '" + node.getName() +
          "' in automaton '" + comp.getName() + "'!", node);
  }
  
  
  //#########################################################################
  //# Static Class Variables
  public static final long serialVersionUID = 1;

}
