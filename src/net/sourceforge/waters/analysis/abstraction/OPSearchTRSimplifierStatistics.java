//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters Analysis
//# PACKAGE: net.sourceforge.waters.analysis.abstraction
//# CLASS:   TRSimplifierStatistics
//###########################################################################
//# $Id$
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


  //#########################################################################
  //# Overrides for net.sourceforge.waters.model.analysis.AnalysisResult
  @Override
  public void merge(final TRSimplifierStatistics other)
  {
    super.merge(other);
    final OPSearchTRSimplifierStatistics stats =
      (OPSearchTRSimplifierStatistics) other;
    mNumberOfIterations =
      DefaultAnalysisResult.mergeAdd(mNumberOfIterations,
                                     stats.mNumberOfIterations);
  }

  @Override
  public void print(final PrintWriter writer)
  {
    super.print(writer);
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
    if (mNumberOfIterations >= 0) {
      writer.print(mNumberOfIterations);
    }
  }

  @Override
  public void printCSVHorizontalHeadings(final PrintWriter writer)
  {
    super.printCSVHorizontalHeadings(writer);
    writer.print(",Iter");
  }


  //#########################################################################
  //# Gathering
  void recordIterations(final int iter)
  {
    mNumberOfIterations =
      DefaultAnalysisResult.mergeAdd(mNumberOfIterations, iter);
  }


  //#########################################################################
  //# Data Members
  private int mNumberOfIterations;

}
