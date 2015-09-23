//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# Copyright (C) 2004-2015 Robi Malik
//###########################################################################
//# This file is part of Waters.
//# Waters is free software: you can redistribute it and/or modify it under
//# the terms of the GNU General Public License as published by the Free
//# Software Foundation, either version 2 of the License, or (at your option)
//# any later version.
//# Waters is distributed in the hope that it will be useful, but WITHOUT ANY
//# WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
//# FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
//# details.
//# You should have received a copy of the GNU General Public License along
//# with Waters. If not, see <http://www.gnu.org/licenses/>.
//#
//# Linking Waters statically or dynamically with other modules is making a
//# combined work based on Waters. Thus, the terms and conditions of the GNU
//# General Public License cover the whole combination.
//# In addition, as a special exception, the copyright holders of Waters give
//# you permission to combine Waters with code included in the standard
//# release of Supremica under the Supremica Software License Agreement (or
//# modified versions of such code, with unchanged license). You may copy and
//# distribute such a system following the terms of the GNU GPL for Waters and
//# the licenses of the other code concerned.
//# Note that people who make modified versions of Waters are not obligated to
//# grant this special exception for their modified versions; it is their
//# choice whether to do so. The GNU General Public License gives permission
//# to release a modified version without this exception; this exception also
//# makes it possible to release a modified version which carries forward this
//# exception.
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
