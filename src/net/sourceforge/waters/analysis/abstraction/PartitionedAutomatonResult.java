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

package net.sourceforge.waters.analysis.abstraction;

import java.util.List;

import net.sourceforge.waters.analysis.tr.StateEncoding;
import net.sourceforge.waters.model.analysis.des.DefaultAutomatonResult;
import net.sourceforge.waters.model.des.AutomatonProxy;


/**
 * An analysis result returned by a bisimulation or similar automaton
 * partitioning algorithm.
 *
 * Partitioning algorithms are used to simplify an automaton by merging
 * equivalent states. The partitioned automaton result contains the simplified
 * automaton, plus the partitioning details that show how each state in the
 * simplified result relates to the states in the original automaton.
 *
 * @author Robi Malik
 */

public class PartitionedAutomatonResult
  extends DefaultAutomatonResult
{
  //#########################################################################
  //# Constructor
  /**
   * Creates a result representing an incomplete analysis run.
   */
  public PartitionedAutomatonResult(final Class<?> clazz)
  {
    super(clazz);
    mInputEncoding = mOutputEncoding = null;
    mPartition = null;
  }


  // #########################################################################
  // # Simple Access Methods
  /**
   * Gets the state encoding of the original automaton before the operation.
   */
  public StateEncoding getInputEncoding()
  {
    return mInputEncoding;
  }

  /**
   * Gets the state encoding of the resultant automaton after the operation.
   */
  public StateEncoding getOutputEncoding()
  {
    return mOutputEncoding;
  }

  /**
   * Gets the partition applied to the input automaton.
   * The partition is a list of arrays of state codes, each entry represents
   * the set of state codes in the input automaton that were merged to produce
   * the state in the output automaton with the code identified by its
   * position in the partition list.
   */
  public List<int[]> getPartition()
  {
    return mPartition;
  }


  //#########################################################################
  //# Providing Data
  /**
   * Stores the result automaton and its partition information.
   * @param  aut        The automaton resulting from the operation.
   * @param  inputEnc   The state encoding of the original automaton before
   *                    the operation.
   * @param  outputEnc  The state encoding of the resultant automaton after
   *                    the operation.
   * @param  partition  The partition that was applied to the input automaton.
   */
  public void setComputedAutomaton(final AutomatonProxy aut,
                                   final StateEncoding inputEnc,
                                   final StateEncoding outputEnc,
                                   final List<int[]> partition)
  {
    super.setComputedAutomaton(aut);
    mInputEncoding = inputEnc;
    mOutputEncoding = outputEnc;
    mPartition = partition;
  }


  //#########################################################################
  //# Data Members
  private StateEncoding mInputEncoding;
  private StateEncoding mOutputEncoding;
  private List<int[]> mPartition;

}
