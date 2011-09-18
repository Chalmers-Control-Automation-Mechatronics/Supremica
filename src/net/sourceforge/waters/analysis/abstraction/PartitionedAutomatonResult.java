//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters Analysis
//# PACKAGE: net.sourceforge.waters.analysis.abstraction
//# CLASS:   PartitionedAutomatonResult
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.analysis.abstraction;

import java.util.List;

import net.sourceforge.waters.analysis.tr.StateEncoding;
import net.sourceforge.waters.model.analysis.AutomatonResult;
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
  extends AutomatonResult<AutomatonProxy>
{
  //#########################################################################
  //# Constructor
  /**
   * Creates a result representing an incomplete analysis run.
   */
  public PartitionedAutomatonResult()
  {
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
  public void setAutomaton(final AutomatonProxy aut,
                           final StateEncoding inputEnc,
                           final StateEncoding outputEnc,
                           final List<int[]> partition)
  {
    super.setComputedProxy(aut);
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
