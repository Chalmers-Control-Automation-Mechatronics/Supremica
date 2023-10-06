//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# Copyright (C) 2004-2023 Robi Malik
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

package net.sourceforge.waters.analysis.modular;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;

import net.sourceforge.waters.model.analysis.AnalysisResult;
import net.sourceforge.waters.model.analysis.DefaultVerificationResult;
import net.sourceforge.waters.model.analysis.VerificationResult;
import net.sourceforge.waters.model.analysis.des.ModelVerifier;
import net.sourceforge.waters.model.des.AutomatonProxy;


/**
 * <P>A result record that can be returned by a modular analysis
 * algorithms.</P>
 *
 * <P>In addition to a standard verification result ({@link VerificationResult}),
 * the modular verification contains the cumulative analysis from all monolithic
 * verification attempts. It may also contain a collection of specifications
 * found to be not controllable.</P>
 *
 * @author Robi Malik
 */

public class ModularVerificationResult
  extends DefaultVerificationResult
{

  //#########################################################################
  //# Constructors
  public ModularVerificationResult(final ModelVerifier verifier)
  {
    this(verifier.getClass());
  }

  /**
   * Creates a verification result representing an incomplete verification run.
   */
  public ModularVerificationResult(final Class<?> clazz)
  {
    super(clazz);
    mFailedSpecs = null;
    mNumberOfMonolithicRuns = -1;
    mMonolithicStats = null;
  }


  //#########################################################################
  //# Simple Access Methods
  public Collection<AutomatonProxy> getFailedSpecs()
  {
    return mFailedSpecs;
  }

  public int getNumberOfMonolithicRuns()
  {
    return mNumberOfMonolithicRuns;
  }

  public VerificationResult getMonolithicResult()
  {
    return mMonolithicStats;
  }


  //#########################################################################
  //# Providing Statistics
  void addMonolithicResult(final VerificationResult result)
  {
    if (mMonolithicStats == null) {
      mNumberOfMonolithicRuns = 1;
      mMonolithicStats = result;
    } else {
      mNumberOfMonolithicRuns++;
      mMonolithicStats.merge(result);
    }
    updatePeakMemoryUsage(result.getPeakMemoryUsage());
  }

  void addMonolithicResults(final VerificationResult result)
  {
    if (result instanceof ModularVerificationResult) {
      final ModularVerificationResult modular = (ModularVerificationResult) result;
      mNumberOfMonolithicRuns =
        mergeAdd(mNumberOfMonolithicRuns, modular.mNumberOfMonolithicRuns);
      if (mMonolithicStats == null) {
        mMonolithicStats = modular.mMonolithicStats;
      } else if (modular.mMonolithicStats != null) {
        mMonolithicStats.merge(modular.mMonolithicStats);
      }
      updatePeakMemoryUsage(result.getPeakMemoryUsage());
    } else {
      addMonolithicResult(result);
    }
  }

  void addFailedSpecs(final Collection<? extends AutomatonProxy> failedSpecs)
  {
    if (mFailedSpecs == null) {
      mFailedSpecs = new ArrayList<>(failedSpecs);
    } else {
      mFailedSpecs.addAll(failedSpecs);
    }
  }

  void addFailedSpec(final AutomatonProxy spec)
  {
    if (mFailedSpecs == null) {
      mFailedSpecs = new LinkedList<>();
    }
    mFailedSpecs.add(spec);
  }


  //#########################################################################
  //# Overrides for net.sourceforge.waters.model.analysis.AnalysisResult
  @Override
  public void merge(final AnalysisResult other)
  {
    super.merge(other);
    if (other instanceof ModularVerificationResult) {
      final ModularVerificationResult modular =
        (ModularVerificationResult) other;
      if (mFailedSpecs == null) {
        mFailedSpecs = new ArrayList<AutomatonProxy>(modular.mFailedSpecs);
      } else if (modular.mFailedSpecs != null) {
        mFailedSpecs.addAll(modular.mFailedSpecs);
      }
      mNumberOfMonolithicRuns =
        mergeAdd(mNumberOfMonolithicRuns, modular.mNumberOfMonolithicRuns);
      if (mMonolithicStats == null) {
        mMonolithicStats = modular.mMonolithicStats;
      } else if (modular.mMonolithicStats != null) {
        mMonolithicStats.merge(modular.mMonolithicStats);
      }
    }
  }


  //#########################################################################
  //# Printing
  @Override
  public void print(final PrintWriter writer)
  {
    super.print(writer);
    if (mNumberOfMonolithicRuns >= 0) {
      writer.println("Number of monolithic verification runs: " +
                     mNumberOfMonolithicRuns);
    }
    if (mMonolithicStats != null) {
      writer.println("--------------------------------------------------");
      mMonolithicStats.print(writer);
    }
  }

  @Override
  public void printCSVHorizontalHeadings(final PrintWriter writer)
  {
    super.printCSVHorizontalHeadings(writer);
    writer.print(",Monolithic");
    if (mMonolithicStats != null) {
      writer.print(',');
      mMonolithicStats.printCSVHorizontalHeadings(writer);
    }
  }

  @Override
  public void printCSVHorizontal(final PrintWriter writer)
  {
    super.printCSVHorizontal(writer);
    writer.print(',');
    writer.print(mNumberOfMonolithicRuns);
    if (mMonolithicStats != null) {
      writer.print(',');
      mMonolithicStats.printCSVHorizontal(writer);
    }
  }


  //#########################################################################
  //# Data Members
  private Collection<AutomatonProxy> mFailedSpecs;
  private int mNumberOfMonolithicRuns;
  private VerificationResult mMonolithicStats;

}
