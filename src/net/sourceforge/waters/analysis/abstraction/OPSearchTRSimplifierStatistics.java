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

package net.sourceforge.waters.analysis.abstraction;

import java.io.PrintWriter;
import net.sourceforge.waters.model.analysis.DefaultAnalysisResult;


/**
 * A record holding performance statistics about an application of the
 * OP-Search algorithm. In addition to standard transition relation simplifier
 * statistics, this record contains specific statistics about the OP-Search
 * algorithm.
 *
 * @author Robi Malik
 */

public class OPSearchTRSimplifierStatistics extends TRSimplifierStatistics
{

  //#########################################################################
  //# Constructors
  public OPSearchTRSimplifierStatistics(final Object simplifier,
                                        final boolean trans,
                                        final boolean markings)
  {
    super(simplifier, trans, markings);
    mPeakVerifierPairs = -1;
    mNumberOfIterations = 0;
  }


  //#########################################################################
  //# Simple Access Methods
  /**
   * Gets the number of iterations performed by OP-Search.
   * The number of iterations is equal to the number of transitions changed
   * from <I>invisible</I> to <I>visible</I> by the algorithm.
   * @return Number of iterations or <CODE>-1</CODE> if not set.
   */
  public int getNumberOfIterations()
  {
    return mNumberOfIterations;
  }

  /**
   * Gets the peak number of verifier pairs constructed during OP-Search.
   * @return Number of pairs or <CODE>-1</CODE> if not set.
   */
  public int getPeakNumberOfVerifierPairs()
  {
    return mPeakVerifierPairs;
  }


  //#########################################################################
  //# Overrides for net.sourceforge.waters.model.analysis.AnalysisResult
  @Override
  public void merge(final TRSimplifierStatistics other)
  {
    super.merge(other);
    final OPSearchTRSimplifierStatistics stats =
      (OPSearchTRSimplifierStatistics) other;
    mPeakVerifierPairs = Math.max(mPeakVerifierPairs,
                                  stats.mPeakVerifierPairs);
    mNumberOfIterations =
      DefaultAnalysisResult.mergeAdd(mNumberOfIterations,
                                     stats.mNumberOfIterations);
  }

  @Override
  public void print(final PrintWriter writer)
  {
    super.print(writer);
    if (mPeakVerifierPairs >= 0) {
      writer.println("Peak number of verifier pairs: " +
                     mPeakVerifierPairs);
    }
    if (mNumberOfIterations >= 0) {
      writer.println("Total number of OP-search iterations: " +
                     mNumberOfIterations);
    }
  }

  @Override
  public void printCSVHorizontal(final PrintWriter writer)
  {
    super.printCSVHorizontal(writer);
    writer.print(',');
    if (mPeakVerifierPairs >= 0) {
      writer.print(mPeakVerifierPairs);
    }
    writer.print(',');
    if (mNumberOfIterations >= 0) {
      writer.print(mNumberOfIterations);
    }
  }

  @Override
  public void printCSVHorizontalHeadings(final PrintWriter writer)
  {
    super.printCSVHorizontalHeadings(writer);
    writer.print(",VPairs,Iter");
  }


  //#########################################################################
  //# Gathering
  void recordVerifier(final int numPairs)
  {
    if (numPairs > mPeakVerifierPairs) {
      mPeakVerifierPairs = numPairs;
    }
  }

  void recordIterations(final int iter)
  {
    mNumberOfIterations =
      DefaultAnalysisResult.mergeAdd(mNumberOfIterations, iter);
  }


  //#########################################################################
  //# Data Members
  private int mPeakVerifierPairs;
  private int mNumberOfIterations;

}
