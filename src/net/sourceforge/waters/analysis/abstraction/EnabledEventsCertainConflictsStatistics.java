//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# Copyright (C) 2004-2021 Robi Malik
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
