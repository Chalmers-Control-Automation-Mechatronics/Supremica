//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# Copyright (C) 2004-2021 Robi Malik
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

package net.sourceforge.waters.model.compiler.graph;

import net.sourceforge.waters.model.base.Proxy;
import net.sourceforge.waters.model.des.AutomatonTools;
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
   * Constructs a new exception indicating that an automaton has no initial
   * state.
   * @param comp     The simple component that causes the problem.
   */
  public NondeterministicModuleException(final SimpleComponentProxy comp)
  {
    super(AutomatonTools.getComponentKindStringCapitalised(comp.getKind()) +
          " '" + comp.getName() + "' does not have any initial state!", comp);
  }

  /**
   * Constructs a new exception indicating that an automaton has more than
   * one initial state.
   * @param comp     The simple component that causes the problem.
   * @param node     One of the initial states.
   * @param location The error location to be associated with the exception.
   */
  public NondeterministicModuleException(final SimpleComponentProxy comp,
                                         final SimpleNodeProxy node,
                                         final Proxy location)
  {
    super(AutomatonTools.getComponentKindStringCapitalised(comp.getKind()) +
          " '" + comp.getName() + "' has more than one initial state!",
          location);
  }

  /**
   * Constructs a new exception indicating that a state has more than one
   * outgoing transition with the same event.
   * @param comp     The simple component that causes the problem.
   * @param node     The state with nondeterministic outgoing transitions.
   * @param event    The event that causes the trouble.
   * @param location The error location to be associated with the exception.
   */
  public NondeterministicModuleException(final SimpleComponentProxy comp,
                                         final SimpleNodeProxy node,
                                         final EventProxy event,
                                         final Proxy location)
  {
    super("Multiple transitions labelled '" + event.getName() +
          "' originating from state '" + node.getName() + "' in " +
          AutomatonTools.getComponentKindStringLowerCase(comp.getKind()) +
          " '" + comp.getName() + "'!", location);
  }


  //#########################################################################
  //# Static Class Variables
  public static final long serialVersionUID = 1;

}
