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

import net.sourceforge.waters.model.analysis.des.AnalysisResult;


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
    mPeakComponents = -1;
    mPeakVerifierPairs = -1;
    mNumberOfIterations = -1;
  }


  //#########################################################################
  //# Simple Access Methods
  /**
   * Gets the peak number of strongly connected components encountered
   * during OP-Search.
   * @return Number of strongly connected components
   *         or <CODE>-1</CODE> if not set.
   */
  public int getPeakNumberOfComponents()
  {
    return mPeakComponents;
  }

  /**
   * Gets the peak number of verifier pairs constructed during OP-Search.
   * @return Number of pairs or <CODE>-1</CODE> if not set.
   */
  public int getPeakNumberOfVerifierPairs()
  {
    return mPeakVerifierPairs;
  }

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
    mPeakComponents =
      Math.max(mPeakComponents, result.mPeakComponents);
    mPeakVerifierPairs =
      Math.max(mPeakVerifierPairs, result.mPeakVerifierPairs);
    mNumberOfIterations =
      mergeAdd(mNumberOfIterations, result.mNumberOfIterations);
  }

  @Override
  public void print(final PrintWriter writer)
  {
    super.print(writer);
    if (mPeakComponents >= 0) {
      writer.println("Peak number of strongly connected components: " +
                     mPeakComponents);
    }
    if (mPeakVerifierPairs >= 0) {
      writer.println("Peak number of OP-verifier pairs: " +
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
    if (mPeakComponents >= 0) {
      writer.print(mPeakComponents);
    }
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
    writer.print(",Comps");
    writer.print(",VPairs");
    writer.print(",Iter");
  }


  //#########################################################################
  //# Gathering
  void recordComponents(final int numComps)
  {
    if (numComps > mPeakComponents) {
      mPeakComponents = numComps;
    }
  }

  void recordVerifier(final int numPairs)
  {
    if (numPairs > mPeakVerifierPairs) {
      mPeakVerifierPairs = numPairs;
    }
  }

  void recordIteration()
  {
    mNumberOfIterations = mergeAdd(mNumberOfIterations, 1);
  }


  //#########################################################################
  //# Data Members
  private int mPeakComponents;
  private int mPeakVerifierPairs;
  private int mNumberOfIterations;

}
