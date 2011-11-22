//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters Analysis
//# PACKAGE: net.sourceforge.waters.analysis.abstraction
//# CLASS:   PartitionedAutomatonResult
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.analysis.abstraction;

import java.io.PrintWriter;
import net.sourceforge.waters.model.analysis.AnalysisResult;


/**
 * An analysis result returned by the OP-Search algorithm. In addition to
 * the result automaton and partition, this result record contains statistics
 * about the OP-Search algorithm.
 *
 * @see OPSearchAutomatonSimplifier
 *
 * @author Robi Malik
 */

public class OPSearchAutomatonResult
  extends PartitionedAutomatonResult
{
  //#########################################################################
  //# Constructor
  /**
   * Creates a result representing an incomplete analysis run.
   */
  public OPSearchAutomatonResult()
  {
    mNumberOfIterations = -1;
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
  //# Providing Data
  /**
   * Sets the number of iterations.
   * @see #getNumberOfIterations()
   */
  public void setNumberOfIterations(final int n)
  {
    mNumberOfIterations = n;
  }


  //#########################################################################
  //# Overrides for net.sourceforge.waters.model.analysis.AnalysisResult
  @Override
  public void merge(final AnalysisResult other)
  {
    super.merge(other);
    final OPSearchAutomatonResult result = (OPSearchAutomatonResult) other;
    mNumberOfIterations =
      mergeAdd(mNumberOfIterations, result.mNumberOfIterations);
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
  void recordIteration()
  {
    mNumberOfIterations = mergeAdd(mNumberOfIterations, 1);
  }


  //#########################################################################
  //# Data Members
  private int mNumberOfIterations;

}
