//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# Copyright (C) 2004-2017 Robi Malik
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

package net.sourceforge.waters.analysis.annotation;

import java.io.PrintWriter;

import net.sourceforge.waters.analysis.tr.ListBufferTransitionRelation;
import net.sourceforge.waters.model.analysis.AnalysisResult;
import net.sourceforge.waters.model.analysis.DefaultAnalysisResult;

import gnu.trove.list.array.TIntArrayList;


/**
 * An analysis result returned by a conflict preorder checker.
 *
 * @see TRConflictPreorderChecker
 * @author Robi Malik
 */

public class ConflictPreorderResult
  extends DefaultAnalysisResult
{

  //#########################################################################
  //# Constructors
  /**
   * Creates a new verification result representing an incomplete run.
   */
  ConflictPreorderResult()
  {
    super(TRConflictPreorderChecker.class);
    mFirstAutomatonStates = mSecondAutomatonStates = -1;
    mTotalLCPairs = 0;
    mLevelSizes = new TIntArrayList();
  }


  //#########################################################################
  //# Simple Access
  public int getFirstAutomatonStates()
  {
    return mFirstAutomatonStates;
  }

  public int getSecondAutomatonStates()
  {
    return mSecondAutomatonStates;
  }

  public int getMaxLCLevel()
  {
    return mLevelSizes.size() - 1;
  }

  public int getTotalLCPairs()
  {
    return mTotalLCPairs;
  }

  public int getTotalLCPairs(final int level)
  {
    return mLevelSizes.get(level);
  }

  public int getTotalMCTriples()
  {
    return (int) getTotalNumberOfStates();
  }

  public int getPeakMCTriples()
  {
    return (int) getPeakNumberOfStates();
  }


  //#########################################################################
  //# Recording Statistics
  void addPair(final ListBufferTransitionRelation rel1,
               final ListBufferTransitionRelation rel2)
  {
    mFirstAutomatonStates =
      mergeAdd(mFirstAutomatonStates, rel1.getNumberOfStates());
    mSecondAutomatonStates =
      mergeAdd(mSecondAutomatonStates, rel2.getNumberOfStates());
    updateNumberOfAutomata(2);
  }

  void addLCPairs(final int value)
  {
    mTotalLCPairs = mergeAdd(mTotalLCPairs, value);
  }

  void addMCTriples(final int value)
  {
    updateNumberOfStates(value);
    updateNumberOfNodes(value);
  }

  void addLevel(final int level, final int size)
  {
    while (mLevelSizes.size() <= level) {
      mLevelSizes.add(0);
    }
    final int newSize = mLevelSizes.get(level) + size;
    mLevelSizes.set(level, newSize);
  }


  //#########################################################################
  //# Overrides for net.sourceforge.waters.model.analysis.AnalysisResult
  @Override
  public void merge(final AnalysisResult other)
  {
    super.merge(other);
    final ConflictPreorderResult result = (ConflictPreorderResult) other;
    mFirstAutomatonStates =
      mergeAdd(mFirstAutomatonStates, result.mFirstAutomatonStates);
    mSecondAutomatonStates =
      mergeAdd(mSecondAutomatonStates, result.mSecondAutomatonStates);
    mTotalLCPairs = mergeAdd(mTotalLCPairs, result.mTotalLCPairs);
    final int maxLevel =
      Math.max(mLevelSizes.size(), result.mLevelSizes.size());
    for (int l = 0; l < maxLevel; l++) {
      if (l >= result.mLevelSizes.size()) {
        break;
      }
      final int levelSize = result.mLevelSizes.get(l);
      if (l < mLevelSizes.size()) {
        final int newSize = mLevelSizes.get(l) + levelSize;
        mLevelSizes.set(l, newSize);
      } else {
        mLevelSizes.add(levelSize);
      }
    }
  }

  @Override
  public void print(final PrintWriter writer)
  {
    super.print(writer);
    if (mFirstAutomatonStates >= 0) {
      writer.println("States of first automaton: " + mFirstAutomatonStates);
    }
    if (mSecondAutomatonStates >= 0) {
      writer.println("States of second automaton: " + mSecondAutomatonStates);
    }
    if (mTotalLCPairs >= 0) {
      writer.println("Total number of LC-Pairs: " + mTotalLCPairs);
    }
    if (mLevelSizes.size() > 0) {
      writer.println("Maximum LC-level: " + (mLevelSizes.size() - 1));
      for (int l = 0; l < mLevelSizes.size(); l++) {
        writer.println("LC-pairs level " + l + ": " + mLevelSizes.get(l));
      }
    }
  }

  @Override
  public void printCSVHorizontalHeadings(final PrintWriter writer)
  {
    super.printCSVHorizontalHeadings(writer);
    writer.print(",FirstStates");
    writer.print(",SecondStates");
    writer.print(",TotLC");
    writer.print(",MaxLevel");
    writer.print(",Levels");
  }

  @Override
  public void printCSVHorizontal(final PrintWriter writer)
  {
    super.printCSVHorizontal(writer);
    writer.print(',');
    if (mFirstAutomatonStates >= 0) {
      writer.print(mFirstAutomatonStates);
    }
    writer.print(',');
    if (mSecondAutomatonStates >= 0) {
      writer.print(mSecondAutomatonStates);
    }
    writer.print(',');
    if (mTotalLCPairs >= 0) {
      writer.print(mTotalLCPairs);
    }
    writer.print(',');
    if (mLevelSizes.size() > 0) {
      writer.print(mLevelSizes.size() - 1);
      writer.print(",\"");
      boolean first = true;
      for (int l = 0; l < mLevelSizes.size(); l++) {
        if (first) {
          first = false;
        } else {
          writer.print('/');
        }
        writer.print(mLevelSizes.get(l));
      }
      writer.print('\"');
    } else {
      writer.print(',');
    }
  }


  //#########################################################################
  //# Data Members
  private int mFirstAutomatonStates;
  private int mSecondAutomatonStates;
  private int mTotalLCPairs;
  private final TIntArrayList mLevelSizes;

}
