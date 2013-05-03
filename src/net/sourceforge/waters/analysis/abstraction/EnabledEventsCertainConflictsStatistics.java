//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters Analysis
//# PACKAGE: net.sourceforge.waters.analysis.abstraction
//# CLASS:   CertainConflictsStatistics
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.analysis.abstraction;

import java.io.PrintWriter;


/**
 * A specialised record holding performance statistics about the application of
 * certain conflicts simplifier ({@link LimitedCertainConflictsTRSimplifier}).
 * In addition to the usual information, this record also stores the maximum
 * depth of certain conflicts encountered.
 *
 * @author Robi Malik
 */
public class EnabledEventsCertainConflictsStatistics extends TRSimplifierStatistics
{

  //#########################################################################
  //# Constructors
  public EnabledEventsCertainConflictsStatistics
    (final TransitionRelationSimplifier simplifier)
  {
    super(simplifier, true, true);
    mMaxLevel = -1;
    mNumEnabledEvents = -1;
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
  public int getNumEnabledEvents()
  {
    return mNumEnabledEvents;
  }


  //#########################################################################
  //# Providing Statistics
  public void recordMaxCertainConflictsLevel(final int level)
  {
    mMaxLevel = Math.max(mMaxLevel, level);
  }

  public void recordNumEnabledEvents(final int numEnabledEvents)
  {
    if(mNumEnabledEvents < 0)
      mNumEnabledEvents = 0;
    mNumEnabledEvents += numEnabledEvents;
  }

  @Override
  public void reset()
  {
    super.reset();
    mMaxLevel = -1;
    mNumEnabledEvents = -1;
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
      if (mNumEnabledEvents >= 0) {
        writer.print("Total Number of Always Enabled Events: ");
        writer.println(mNumEnabledEvents);
    }
  }

  @Override
  public void printCSVHorizontalHeadings(final PrintWriter writer)
  {
    super.printCSVHorizontalHeadings(writer);
    writer.print(",MaxLevel");
    writer.print(",AlwaysEnabledEvents");
  }

  @Override
  public void printCSVHorizontal(final PrintWriter writer)
  {
    super.printCSVHorizontal(writer);
    writer.print(',');
    writer.print(mMaxLevel);
    writer.print(',');
    writer.print(mNumEnabledEvents);
  }


  //#########################################################################
  //# Data Members
  private int mMaxLevel;
  private int mNumEnabledEvents;

}
