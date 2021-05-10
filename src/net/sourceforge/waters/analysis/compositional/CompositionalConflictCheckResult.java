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

package net.sourceforge.waters.analysis.compositional;

import java.io.PrintWriter;
import java.util.Formatter;

import net.sourceforge.waters.analysis.trcomp.TRCompositionalConflictChecker;
import net.sourceforge.waters.model.analysis.AnalysisResult;
import net.sourceforge.waters.model.analysis.des.ConflictChecker;


/**
 * <P>A result record that can be returned by a compositional conflict checker
 * algorithms such as {@link TRCompositionalConflictChecker}.</P>
 *
 * <P>In addition to a {@link CompositionalVerificationResult}, the conflict
 * check result records information about the number and runtime of language
 * inclusion checks during counterexample expansion
 *
 * @author Robi Malik
 */

public class CompositionalConflictCheckResult
  extends CompositionalVerificationResult
{

  //#########################################################################
  //# Constructors
  /**
   * Creates a verification result representing an incomplete run.
   * @param  verifier The model analyser creating this result.
   */
  public CompositionalConflictCheckResult(final ConflictChecker verifier)
  {
    this(verifier.getClass());
  }

  /**
   * Creates a verification result representing an incomplete run.
   * @param  clazz    The class of the model verifier creating this result.
   */
  public CompositionalConflictCheckResult(final Class<?> clazz)
  {
    super(clazz);
    mCCLanguageInclusionCount = -1;
    mCCLanguageInclusionTime = -1L;
  }


  //#########################################################################
  //# Simple Access
  /**
   * Gets the total number of language inclusion checks performed
   * during counterexample computation.
   */
  public int getCCLanguageInclusionCount()
  {
    return mCCLanguageInclusionCount;
  }

  /**
   * Gets the combined total time taken by all language inclusion checks
   * during counterexample computation, in milliseconds.
   */
  public long getCCLanguageInclusionTime()
  {
    return mCCLanguageInclusionTime;
  }

  /**
   * Registers additional language inclusion checks during counterexample
   * expansion. This method increases the number of language inclusion checks
   * and adds the given time to the total language inclusion time.
   * @param  count   The number of language inclusion checks to be added.
   * @param  time    The time taken by these language inclusion checks,
   *                 in milliseconds.
   */
  public void addCCLanguageInclusionChecks(final int count, final long time)
  {
    if (mCCLanguageInclusionCount < 0) {
      mCCLanguageInclusionCount = count;
      mCCLanguageInclusionTime = time;
    } else {
      mCCLanguageInclusionCount += count;
      if (time > 0) {
        mCCLanguageInclusionTime += time;
      }
    }
  }


  //#########################################################################
  //# Overrides for net.sourceforge.waters.model.analysis.AnalysisResult
  @Override
  public void merge(final AnalysisResult other)
  {
    super.merge(other);
    if (other instanceof CompositionalConflictCheckResult) {
      final CompositionalConflictCheckResult result =
        (CompositionalConflictCheckResult) other;
      mCCLanguageInclusionCount = mergeAdd(mCCLanguageInclusionCount,
                                           result.mCCLanguageInclusionCount);
      mCCLanguageInclusionTime = mergeAdd(mCCLanguageInclusionTime,
                                          result.mCCLanguageInclusionTime);
    }
  }


  //#########################################################################
  //# Printing
  @Override
  protected void printPart1(final PrintWriter writer)
  {
    super.printPart1(writer);
    if (mCCLanguageInclusionCount >= 0) {
      @SuppressWarnings("resource")
      final Formatter formatter = new Formatter(writer);
      formatter.format
        ("Number of language inclusion checks for counterexample: %d\n",
         mCCLanguageInclusionCount);
      formatter.format
        ("Language inclusion time for counterexample: %.3fs\n",
         0.001f * mCCLanguageInclusionTime);
    }
  }

  @Override
  protected void printCSVHorizontalPart1(final PrintWriter writer)
  {
    super.printCSVHorizontalPart1(writer);
    writer.print(',');
    if (mCCLanguageInclusionCount >= 0) {
      writer.print(mCCLanguageInclusionCount);
    }
    writer.print(',');
    if (mCCLanguageInclusionTime >= 0) {
      writer.print(mCCLanguageInclusionTime);
    }
  }

  @Override
  protected void printCSVHorizontalHeadingsPart1(final PrintWriter writer)
  {
    super.printCSVHorizontalHeadingsPart1(writer);
    writer.print(",CCLanguageInclusionCount,CCLanguageInclusionTime");
  }


  //#########################################################################
  //# Data Members
  private int mCCLanguageInclusionCount;
  private long mCCLanguageInclusionTime;

}
