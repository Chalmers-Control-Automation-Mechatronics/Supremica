//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# Copyright (C) 2004-2019 Robi Malik
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

import java.util.Collection;

import net.sourceforge.waters.model.analysis.kindtranslator.KindTranslator;
import net.sourceforge.waters.model.des.EventProxy;
import net.sourceforge.waters.model.des.LoopCounterExampleProxy;


/**
 * <P>A model verifier that checks for control loops. This model verifier
 * checks whether the synchronous composition of all automata in the input
 * model contains any non-empty loop consisting of controllable events
 * only. The set of loops events can be parameterised using a kind
 * translator.</P>
 *
 * @see KindTranslator
 * @author Robi Malik
 */

public interface ControlLoopChecker extends ModelVerifier
{

  //#########################################################################
  //# More Specific Access to the Results
  /**
   * Gets a counterexample if the model was found to be not control-loop free.
   * representing a control-loop error trace. A control-loop error
   * trace is a nonempty sequence of events that ends in a loop consisting of
   * controllable events only.
   * @return A trace object representing the counterexample.
   *         The returned trace is constructed for the input product DES
   *         of this control-loop checker and shares its automata and
   *         event objects.
   * @throws IllegalStateException if this method is called before
   *         model checking has completed, i.e., before {@link #run()}
   *         has been called, or model checking has found that the
   *         property is satisfied and there is no counterexample.
   */
  @Override
  public LoopCounterExampleProxy getCounterExample();

  /**
   * Gets a collection of events that are guaranteed not to be
   * contained in any control-loop after a failed call to
   * {@link #run()}. This is an optional method.
   * @return Collection of events. Algorithms not calculating non-loop events
   *         should return an empty collection.
   * @throws IllegalStateException if this method is called before
   *         {@link #run()}, or if the last call to to {@link #run()}
   *         returned true.
   */
  public Collection<EventProxy> getNonLoopEvents();
}
