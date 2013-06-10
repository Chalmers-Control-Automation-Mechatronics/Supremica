//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters Analysis
//# PACKAGE: net.sourceforge.waters.analysis.annotation
//# CLASS:   ConflictPreorderResult
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.analysis.annotation;

import gnu.trove.list.array.TIntArrayList;

import java.io.PrintWriter;

import net.sourceforge.waters.analysis.tr.ListBufferTransitionRelation;
import net.sourceforge.waters.model.analysis.AnalysisResult;
import net.sourceforge.waters.model.analysis.DefaultAnalysisResult;


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
    mFirstAutomatonStates = mSecondAutomatonStates = -1;
    mTotalLCPairs = 0;
    mLevelSizes = new TIntArrayList();
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
