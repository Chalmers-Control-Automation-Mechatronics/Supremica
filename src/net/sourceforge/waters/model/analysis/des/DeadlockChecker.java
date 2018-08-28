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

package net.sourceforge.waters.model.analysis.des;

import net.sourceforge.waters.model.des.ConflictCounterExampleProxy;

/**
 * <P>A model verifier that checks whether a system of composed automata
 * contains a deadlock.
 * A conflict checker analyses the input model and returns with success if
 * the synchronous product of the automata has no deadlock state, i.e.,
 * all states have at least one outgoing transition;
 * otherwise it returns with failure and produces a <I>conflict
 * counterexample</I> ({@link ConflictCounterExampleProxy}).</P>
 *
 * @author Robi Malik
 */

public interface DeadlockChecker extends ModelVerifier
{

  //#########################################################################
  //# More Specific Access to the Results
  /**
   * Gets a counterexample if the model was found contain a deadlock.
   * A deadlock counterexample is a sequence of events that takes the model to
   * a state without any enabled transitions.
   * @return A trace object representing the counterexample.
   *         The returned trace is constructed for the input product DES
   *         of this deadlock checker and shares its automata and
   *         event objects.
   * @throws IllegalStateException if this method is called before
   *         model checking has completed, i.e., before {@link #run()}
   *         has been called, or model checking has found that the
   *         property is satisfied and there is no counterexample.
   */
  @Override
  public ConflictCounterExampleProxy getCounterExample();

}
