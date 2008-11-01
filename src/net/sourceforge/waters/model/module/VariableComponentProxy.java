//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.model.module
//# CLASS:   VariableComponentProxy
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.model.module;

import java.util.List;


/**
 * <P>A component representing an EFA variable.</P>
 *
 * <P>An EFA variable can assume a finite number of different values, given
 * by its type. Like an automaton, it has an initial value, and possible
 * marked values.</P>
 *
 * <P>Automata synchronise with EFA variable through their guard/action
 * blocks ({@link GuardActionBlockProxy}). When a module is compiled, each
 * EFA variable is replaced by an automaton, and events are generated to
 * match the synchronisation constraints defined by the various guards and
 * actions.</P>
 *
 * <P>To support nondeterministic systems, the initial and marked states of
 * a variable are given as predicates rather than values. The predicate
 * includes an identifier that matches the name of the variable, which is
 * bound to the value of a state of interest prior to evaluation. For
 * example, to define state number&nbsp;<CODE>0</CODE> as the initial state
 * of an integer-range variable called&nbsp;<CODE>x</CODE>, the initial
 * state predicate would be <CODE>x==0</CODE>.</P>
 *
 * @author Robi Malik
 */
// @short variable

public interface VariableComponentProxy extends ComponentProxy {

  //#########################################################################
  //# Simple Access
  /**
   * Gets the range of this variable. The range defines the finite set of
   * values the variable can assume.
   */
  public SimpleExpressionProxy getType();

  /**
   * Returns whether this variable is deterministic. If a variable is set
   * to be deterministic, the compiler should generate error messages when
   * the automata of the model contain conflicting actions that would lead
   * to nondeterministic state change of the variable. If the variable is not
   * deterministic, the compiler may produce a nondeterministic automaton
   * for it.
   */
  public boolean isDeterministic();

  /**
   * Gets the initial state predicate for this variable.
   * The initial state predicate is an expression including an identifier
   * with the same name as the variable. A state is initial when the
   * initial state predicate evaluates to true, i.e., nonzero.
   */
  public SimpleExpressionProxy getInitialStatePredicate();

  /**
   * Gets the list of markings for this variable.
   * Each marking contains an identifier referring to a proposition event
   * and a predicate defining which states are marked by the proposition.
   */
  public List<VariableMarkingProxy> getVariableMarkings();

}
