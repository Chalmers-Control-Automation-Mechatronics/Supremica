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

package net.sourceforge.waters.model.des;

import java.util.List;
import java.util.Set;

import net.sourceforge.waters.model.base.DocumentProxy;


/**
 * <P>A counterexample to show that a model fails to satisfy some property.</P>
 *
 * <P>Counterexamples are typically produced by model verifiers and contain
 * traces that show how the system can perform undesired behaviour. Each
 * trace ({@link TraceProxy}) contains a sequence of events that can be
 * executed by the model, possibly with associated state information.</P>
 *
 * <P>This interface has only abstract classes to implement it. There are
 * different properties that lead to different types counterexamples
 * represented by subtypes of this interface. Most counterexamples contain of
 * a single trace of undesired behaviour, but some properties (such as
 * diagnosability) require more than one trace.</P>
 *
 * @author Robi Malik
 */

public interface CounterExampleProxy
  extends DocumentProxy
{

  //#########################################################################
  //# Getters
  /**
   * Gets the product DES for which this counterexample has been generated.
   */
  public ProductDESProxy getProductDES();

  /**
   * Gets the list of automata for this counterexample.
   * All the traces in the counterexample can be restricted to use only the
   * automata in this list.
   * @return  An unmodifiable set of objects of type {@link AutomatonProxy}.
   */
  public Set<AutomatonProxy> getAutomata();

  /**
   * Gets the list of traces constituting this counterexample. Most
   * counterexamples contain of a single trace of undesired behaviour,
   * but some properties (such as diagnosability) require more than one
   * trace.
   * @return  An unmodifiable list of objects of type {@link TraceProxy}.
   */
  public List<TraceProxy> getTraces();

}
