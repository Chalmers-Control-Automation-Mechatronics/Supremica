//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# Copyright (C) 2004-2023 Robi Malik
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

package net.sourceforge.waters.model.des;

/**
 * <P>A counterexample that shows that a model fails a safety property.</P>
 *
 * <P>A safety counterexample contains a single trace that takes the system
 * from an initial state to an undesirable state. In addition, the trace
 * contains a final step ({@link TraceStepProxy}) that can only be executed
 * by some of the automata in the model, thus showing how the property is
 * violated.</P>
 *
 * <UL>
 * <LI>A <I>controllability</I> counterexample has a final trace step with an
 * uncontrollable event that can be executed by all plants, but is disabled
 * in at least one specification.</LI>
 * <LI>A <I>language inclusion</I> counterexample has a final trace step
 * that can be executed by all plants, specifications, and supervisors in
 * the model, but is disabled in at least one property automaton.</LI>
 * </UL>
 *
 * @author Robi Malik
 */

public interface SafetyCounterExampleProxy
extends CounterExampleProxy
{

  //#########################################################################
  //# Getters
  /**
   * Gets the single trace that constitutes this counterexample.
   * This method simply returns the first of the counterexample's
   * traces.
   * @see CounterExampleProxy#getTraces()
   */
  public TraceProxy getTrace();

}
