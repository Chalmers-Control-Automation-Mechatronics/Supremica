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

package net.sourceforge.waters.analysis.gnonblocking;

import java.io.PrintStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import net.sourceforge.waters.model.analysis.AnalysisResult;
import net.sourceforge.waters.model.analysis.DefaultVerificationResult;


/**
 * A result record returned by a {@link CompositionalGeneralisedConflictChecker}
 * A verification result contains the information on whether a property checked
 * is true or false, and in the latter case, it also contains a counterexample.
 * In addition, it contains statistics about the analysis run.
 *
 * @author Rachel Francis
 */

public class CompositionalGeneralisedConflictCheckerVerificationResult
  extends DefaultVerificationResult
{

  //#########################################################################
  //# Constructors
  /**
   * Creates a verification result representing an incomplete verification run.
   */
  public CompositionalGeneralisedConflictCheckerVerificationResult()
  {
    super(CompositionalGeneralisedConflictChecker.class);
    mUnsuccessfulCompositionCount = 0;
    mSuccessfulCompositionCount = 0;
    mComposedModelNumberOfStates = 0;
    mComposedModelNumberOfTransitions = 0;
    mAbstractionRuleStats = null;
  }


  //#########################################################################
  //# Simple Access Methods
  /**
   * Gets the number of states in the final composed model.
   */
  public double getComposedModelStateCount()
  {
    return mComposedModelNumberOfStates;
  }

  /**
   * Gets the number of transitions in the final composed model.
   */
  public double getComposedModelTransitionCount()
  {
    return mComposedModelNumberOfTransitions;
  }

  /**
   * Gets the number of times a successful candidate is chosen, composed and
   * abstracted.
   */
  public int getCompositionCount()
  {
    return mSuccessfulCompositionCount;
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
   * Gets the total number of times a candidate is chosen and composition and
   * abstraction is attempted. The attempts may or may not have been successful
   * in producing a reduced size model with no overflow exceptions occurring.
   */
  public int getTotalCompositionCount()
  {
    return mSuccessfulCompositionCount + mUnsuccessfulCompositionCount;
  }

  /**
   * Gets the statistics that apply to the abstraction rules used.
   */
  public List<AbstractionRuleStatistics> getAbstractionRuleStatistics()
  {
    return mAbstractionRuleStats;
  }


  //#########################################################################
  //# Providing Statistics
  /**
   * Sets the number of states in the final composed model.
   */
  public void setComposedModelStateCount(final double stateCount)
  {
    mComposedModelNumberOfStates = stateCount;
  }

  /**
   * Sets the number of transitions in the final composed model.
   */
  public void setComposedModelTransitionCount(final double transitionCount)
  {
    mComposedModelNumberOfTransitions = transitionCount;
  }

  /**
   * Maps a list of AbstractionRuleStatistics which represent the statistics for
   * the abstraction rules used during the composition of the model.
   */
  public void setAbstractionRuleStats(final List<AbstractionRule> rules)
  {
    mAbstractionRuleStats =
        new ArrayList<AbstractionRuleStatistics>(rules.size());
    for (final AbstractionRule rule : rules) {
      final AbstractionRuleStatistics stats = rule.getStatistics();
      mAbstractionRuleStats.add(stats);
    }
  }

  /**
   * Sets the number of times a successful candidate is chosen, composed and
   * abstracted.
   */
  public void setSuccessfulCompositionCount(final int count)
  {
    mSuccessfulCompositionCount = count;
  }

  /**
   * Sets the number of times a candidate is chosen and an overflow exception
   * occurs during composition or abstraction.
   */
  public void setUnsuccessfulCompositionCount(final int count)
  {
    mUnsuccessfulCompositionCount = count;
  }


  //#########################################################################
  //# Merging
  @Override
  public void merge(final AnalysisResult other)
  {
    super.merge(other);
    final CompositionalGeneralisedConflictCheckerVerificationResult result =
      (CompositionalGeneralisedConflictCheckerVerificationResult) other;
    mSuccessfulCompositionCount += result.mSuccessfulCompositionCount;
    mUnsuccessfulCompositionCount += result.mUnsuccessfulCompositionCount;
    mComposedModelNumberOfStates =
      Math.max(mComposedModelNumberOfStates,
               result.mComposedModelNumberOfStates);
    mComposedModelNumberOfTransitions =
      Math.max(mComposedModelNumberOfTransitions,
               result.mComposedModelNumberOfTransitions);
    final Iterator<AbstractionRuleStatistics> iter1 =
      mAbstractionRuleStats.iterator();
    final Iterator<AbstractionRuleStatistics> iter2 =
      result.mAbstractionRuleStats.iterator();
    while (iter1.hasNext() && iter2.hasNext()) {
      final AbstractionRuleStatistics stats1 = iter1.next();
      final AbstractionRuleStatistics stats2 = iter2.next();
      stats1.merge(stats2);
    }
  }


  //#########################################################################
  //# Printing
  @Override
  public void print(final PrintWriter writer)
  {
    super.print(writer);
    writer.println("Number of times a model is successfully composed: " +
                   mSuccessfulCompositionCount);
    writer.println("Number of times a model is unsuccessfully composed: " +
                   mUnsuccessfulCompositionCount);
    final double probability =
        (double) mUnsuccessfulCompositionCount/
        (double) getTotalCompositionCount();
    writer.println("Probability of a candidate selection being unsuccessful: " +
                   probability);
    writer.println("Number of states in final composed model: " +
                   mComposedModelNumberOfStates);
    writer.println("Number of transitions in final composed model: " +
                   mComposedModelNumberOfTransitions);
    writer.println
      ("-----------------------Rule Results ----------------------");
    for (final AbstractionRuleStatistics ruleStats : mAbstractionRuleStats) {
      ruleStats.print(writer);
      writer.println();
    }
  }

  @Override
  public void printCSVHorizontalHeadings(final PrintWriter writer)
  {
    printCSVHorizontalHeadings(writer, mAbstractionRuleStats.size());
  }

  void printCSVHorizontalHeadings(final PrintStream stream, final int numRules)
  {
    final PrintWriter writer = new PrintWriter(stream);
    printCSVHorizontalHeadings(writer, numRules);
  }

  void printCSVHorizontalHeadings(final PrintWriter writer, final int numRules)
  {
    super.printCSVHorizontalHeadings(writer);
    writer.print(",Succ composed");
    writer.print(",Unsucc composed");
    writer.print(",Prob candidate unsucc");
    writer.print(",States final");
    writer.print(",Tansitions final");
    final AbstractionRuleStatistics ruleStats =
      new AbstractionRuleStatistics(null);
    for (int i = 0; i < numRules; i++) {
      ruleStats.printCSVHorizontalHeadings(writer);
    }
  }

  @Override
  public void printCSVHorizontal(final PrintWriter writer)
  {
    super.printCSVHorizontal(writer);
    writer.print("," + mSuccessfulCompositionCount);
    writer.print("," + mUnsuccessfulCompositionCount);
    final double probability =
        (double) mUnsuccessfulCompositionCount /
        (double) getTotalCompositionCount();
    writer.print("," + probability);
    writer.print("," + mComposedModelNumberOfStates);
    writer.print("," + mComposedModelNumberOfTransitions);
    for (final AbstractionRuleStatistics ruleStats : mAbstractionRuleStats) {
      ruleStats.printCSVHorizontal(writer);
    }
  }


  //#########################################################################
  //# Data Members
  private List<AbstractionRuleStatistics> mAbstractionRuleStats;
  private int mSuccessfulCompositionCount;
  private int mUnsuccessfulCompositionCount;
  private double mComposedModelNumberOfStates;
  private double mComposedModelNumberOfTransitions;

}
