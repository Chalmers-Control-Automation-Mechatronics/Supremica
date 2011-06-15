//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters Analysis
//# PACKAGE: net.sourceforge.waters.analysis.op
//# CLASS:   CompositionalVerificationResult
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.analysis.op;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Formatter;
import java.util.List;

import net.sourceforge.waters.model.analysis.VerificationResult;


/**
 * A result record that can be returned by a compositional verification
 * algorithms such as {@link OPConflictChecker}.
 *
 * In addition to a standard verification result ({@link VerificationResult}),
 * the compositional verification result contains detailed statistics about
 * individual abstraction rules and the final (monolithic) verification.
 *
 * @author Rachel Francis, Robi Malik
 */

public class CompositionalVerificationResult extends VerificationResult
{

  //#########################################################################
  //# Constructors
  /**
   * Creates a verification result representing an incomplete verification run.
   */
  public CompositionalVerificationResult()
  {
    mTotalCompositionCount = 0;
    mUnsuccessfulCompositionCount = 0;
    mSimplifierStatistics = null;
    mMonolithicVerificationResult = null;
  }


  //#########################################################################
  //# Simple Access Methods
  /**
   * Gets the total number of times a candidate is chosen and composition and
   * abstraction is attempted. The attempts may or may not have been successful
   * in producing a reduced size model with no overflow exceptions occurring.
   */
  public int getTotalCompositionCount()
  {
    return mTotalCompositionCount;
  }

  /**
   * Gets the number of times an unsuccessful candidate is chosen and an
   * overflow exception occurs during composition or abstraction.
   */
  public int getUnsuccessfulCompositionCount()
  {
    return mUnsuccessfulCompositionCount;
  }

  /**
   * Gets the statistics for the individual abstraction rules used.
   */
  public List<TRSimplifierStatistics> getSimplifierStatistics()
  {
    return mSimplifierStatistics;
  }

  /**
   * Gets the statistics for the final (monolithic) verification step.
   */
  public VerificationResult getMonolithicVerificationResult()
  {
    return mMonolithicVerificationResult;
  }


  //#########################################################################
  //# Providing Statistics
  public void addCompositionAttempt()
  {
    mTotalCompositionCount++;
  }

  public void addUnsuccessfulComposition()
  {
    mUnsuccessfulCompositionCount++;
  }

  public void setSimplifierStatistics
    (final TransitionRelationSimplifier simplifier)
  {
    if (simplifier instanceof ChainTRSimplifier) {
      final ChainTRSimplifier chain = (ChainTRSimplifier) simplifier;
      setSimplifierStatistics(chain);
    } else {
      final TRSimplifierStatistics stats = simplifier.getStatistics();
      mSimplifierStatistics = Collections.singletonList(stats);
    }
  }

  public void setSimplifierStatistics(final ChainTRSimplifier chain)
  {
    final int size = chain.size();
    mSimplifierStatistics = new ArrayList<TRSimplifierStatistics>(size);
    for (int index = 0; index < size; index++) {
      final TransitionRelationSimplifier step = chain.getStep(index);
      final TRSimplifierStatistics stats = step.getStatistics();
      mSimplifierStatistics.add(stats);
    }
  }

  public void setMonolithicVerificationResult(final VerificationResult result)
  {
    mMonolithicVerificationResult = result;
  }


  //#########################################################################
  //# Printing
  @Override
  public void print(final PrintWriter writer)
  {
    super.print(writer);
    writer.print("Total number of compositions: ");
    writer.println(mTotalCompositionCount);
    writer.print("Number of unsuccessful compositions: ");
    writer.println(mUnsuccessfulCompositionCount);
    if (mUnsuccessfulCompositionCount > 0) {
      final Formatter formatter = new Formatter(writer);
      final float probability =
        (float) (mTotalCompositionCount - mUnsuccessfulCompositionCount) /
        (float) mTotalCompositionCount;
      formatter.format
        ("Probability of a candidate selection being successful: %.2f%%\n",
         100.0f * probability);
    }
    if (mSimplifierStatistics != null) {
      for (final TRSimplifierStatistics ruleStats : mSimplifierStatistics) {
        writer.println("--------------------------------------------------");
        ruleStats.print(writer);
      }
    }
    if (mMonolithicVerificationResult != null) {
      writer.println("--------------------------------------------------");
      writer.println("FINAL MONOLITHIC VERIFICATION");
      mMonolithicVerificationResult.print(writer);
    }
  }

  @Override
  public void printCSVHorizontalHeadings(final PrintWriter writer)
  {
    super.printCSVHorizontalHeadings(writer);
    writer.print(",Compositions");
    writer.print(",Overflows");
    if (mSimplifierStatistics != null) {
      for (final TRSimplifierStatistics ruleStats : mSimplifierStatistics) {
        ruleStats.printCSVHorizontalHeadings(writer);
      }
    }
    if (mMonolithicVerificationResult != null) {
      writer.print(',');
      mMonolithicVerificationResult.printCSVHorizontalHeadings(writer);
    }
  }

  @Override
  public void printCSVHorizontal(final PrintWriter writer)
  {
    super.printCSVHorizontal(writer);
    writer.print(',');
    writer.print(mTotalCompositionCount);
    writer.print(',');
    writer.print(mUnsuccessfulCompositionCount);
    if (mSimplifierStatistics != null) {
      for (final TRSimplifierStatistics ruleStats : mSimplifierStatistics) {
        ruleStats.printCSVHorizontal(writer);
      }
    }
    if (mMonolithicVerificationResult != null) {
      writer.print(',');
      mMonolithicVerificationResult.printCSVHorizontal(writer);
    }
  }


  //#########################################################################
  //# Data Members
  private int mUnsuccessfulCompositionCount;
  private int mTotalCompositionCount;
  private List<TRSimplifierStatistics> mSimplifierStatistics;
  private VerificationResult mMonolithicVerificationResult;

}
