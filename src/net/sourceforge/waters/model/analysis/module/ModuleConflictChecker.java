//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# Copyright (C) 2004-2018 Robi Malik
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

package net.sourceforge.waters.model.analysis.module;

import net.sourceforge.waters.model.des.ConflictTraceProxy;
import net.sourceforge.waters.model.module.EventDeclProxy;
import net.sourceforge.waters.model.module.IdentifierProxy;


/**
 * <P>A module verifier that checks whether a module
 * is <I>nonblocking</I>.
 * A conflict checker analyses the input model and returns with success if
 * the synchronous product of the automata in the model is nonblocking;
 * otherwise it returns with failure and produces a <I>conflict error
 * trace</I> ({@link ConflictTraceProxy}).</P>
 *
 * @author Robi Malik, Sahar Mohajerani
 */

public interface ModuleConflictChecker extends ModuleVerifier
{

  //#########################################################################
  //# Configuration
  /**
   * <P>Sets the <I>marking proposition</I> to be used for conflict
   * checking.</P>
   * <P>The marking proposition defines which states are marked. Every state
   * has a list of propositions attached to it; the conflict checker
   * considers only those states as marked that are labelled by
   * <CODE>marking</CODE>, i.e., their list of propositions must contain
   * this event (exactly the same object).</P>
   * <P>A marking proposition of&nbsp;<CODE>null</CODE> may be specified to
   * use the <I>default marking</I>. In this case, the model must contain a
   * proposition event named {@link EventDeclProxy#DEFAULT_MARKING_NAME},
   * which is used as marking proposition. It is an error to request default
   * marking, if no suitable event is present.</P>
   * @param  marking  The name of the marking proposition to be used,
   *                  or <CODE>null</CODE> to use the default marking
   *                  proposition of the model.
   */
  public void setConfiguredDefaultMarking(IdentifierProxy marking);

  /**
   * Gets the <I>marking proposition</I> used for conflict checking.
   * @return The name of the current marking proposition or <CODE>null</CODE> to
   *         indicate default marking.
   * @see #setConfiguredDefaultMarking(IdentifierProxy)
   */
  public IdentifierProxy getConfiguredDefaultMarking();

  /**
   * <P>Sets the precondition (alpha marking) for a generalised nonblocking
   * check.</P>
   * <P>If non-null, the precondition defines the set of states from which
   * marked states need to be reachable. If null, all states are assumes to
   * be marked by the precondition, i.e., a standard nonblocking check is
   * carried out.</P>
   * @param  marking  The name of the precondition marking to be used,
   *                  or <CODE>null</CODE> for a standard nonblocking check.
   */
  public void setConfiguredPreconditionMarking(IdentifierProxy marking);

  /**
   * Gets the precondition (alpha marking) for a generalised nonblocking
   * check.
   * @return The name of the current precondition or <CODE>null</CODE> to
   *         indicate standard conflict check.
   * @see #setConfiguredPreconditionMarking(IdentifierProxy)
   */
  public IdentifierProxy getConfiguredPreconditionMarking();


  //#########################################################################
  //# More Specific Access to the Results
  /**
   * Gets a counterexample if the model was found to be conflicting,
   * representing a conflict error trace. A conflict error trace is a
   * sequence of events that takes the model to a state that is not
   * coreachable. That is, after executing the counterexample, the automata
   * are in a state from where it is no longer possible to reach a state
   * where all automata are marked at the same time.
   * @return A trace object representing the counterexample.
   *         The returned trace is constructed for the input product DES
   *         of this conflict checker and shares its automata and
   *         event objects.
   * @throws IllegalStateException if this method is called before
   *         model checking has completed, i.e., before {@link #run()}
   *         has been called, or model checking has found that the
   *         property is satisfied and there is no counterexample.
   */
  @Override
  public ConflictTraceProxy getCounterExample();

}
