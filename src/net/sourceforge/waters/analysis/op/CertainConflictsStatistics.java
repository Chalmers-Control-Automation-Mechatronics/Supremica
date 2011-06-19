//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters Analysis
//# PACKAGE: net.sourceforge.waters.analysis.op
//# CLASS:   CertainConflictsStatistics
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.analysis.op;

import java.io.PrintWriter;


/**
 * A specialised record holding performance statistics about the application of
 * certain conflicts simplifier ({@link LimitedCertainConflictsTRSimplifier}).
 * In addition to the usual information, this record also stores the maximum
 * depth of certain conflicts encountered.
 *
 * @author Robi Malik
 */
public class CertainConflictsStatistics extends TRSimplifierStatistics
{

  //#########################################################################
  //# Constructors
  public CertainConflictsStatistics
    (final TransitionRelationSimplifier simplifier)
  {
    super(simplifier, true, true);
    mMaxLevel = -1;
  }


  //#########################################################################
  //# Simple Access Methods
  /**
   * Gets the maximum level of certain conflicts encountered.
   */
  public int getMaxCertainConflictsLevel()
  {
    return mMaxLevel;
  }


  //#########################################################################
  //# Providing Statistics
  public void recordMaxCertainConflictsLevel(final int level)
  {
    mMaxLevel = Math.max(mMaxLevel, level);
  }

  @Override
  public void reset()
  {
    super.reset();
    mMaxLevel = -1;
  }


  //#########################################################################
  //# Printing
  @Override
  public void print(final PrintWriter writer)
  {
    super.print(writer);
    if (mMaxLevel >= 0) {
      writer.print("Maximum level of certain conflicts: ");
      writer.println(mMaxLevel);
    }
  }

  @Override
  public void printCSVHorizontalHeadings(final PrintWriter writer)
  {
    super.printCSVHorizontalHeadings(writer);
    writer.print(",MaxLevel");
  }

  @Override
  public void printCSVHorizontal(final PrintWriter writer)
  {
    super.printCSVHorizontal(writer);
    writer.print(mMaxLevel);
  }


  //#########################################################################
  //# Data Members
  private int mMaxLevel;

}
