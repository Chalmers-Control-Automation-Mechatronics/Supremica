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

import net.sourceforge.waters.model.analysis.AnalysisResult;
import net.sourceforge.waters.model.analysis.CoobservabilityVerificationResult;
import net.sourceforge.waters.model.analysis.des.ModelAnalyzer;
import net.sourceforge.waters.model.des.CoobservabilityCounterExampleProxy;


/**
 * A result record that can returned by a modular coobservability
 * verification algorithm.
 *
 * @author Robi Malik
 */

public class ModularCoobservabilityVerificationResult
  extends ModularVerificationResult
  implements CoobservabilityVerificationResult
{

  //#########################################################################
  //# Constructors
  /**
   * Creates a verification result representing an incomplete run.
   * @param  analyzer The model analyser creating this result.
   */
  public ModularCoobservabilityVerificationResult(final ModelAnalyzer analyzer)
  {
    this(analyzer.getClass());
  }

  /**
   * Creates a verification result representing an incomplete run.
   * @param  clazz    The class of the model verifier creating this result.
   */
  public ModularCoobservabilityVerificationResult(final Class<?> clazz)
  {
    super(clazz);
    mNumberOfSites = -1;
  }


  //#########################################################################
  //# Interface
  //# net.sourceforge.waters.model.analysis.CoobservabilityVerificationResult
  @Override
  public int getNumberOfSites()
  {
    return mNumberOfSites;
  }

  @Override
  public void setNumberOfSites(final int numSites)
  {
    mNumberOfSites = numSites;
  }

  @Override
  public int getPeakNumberOfSites()
  {
    final CoobservabilityVerificationResult mono = getMonolithicResult();
    if (mono != null) {
      return mono.getPeakNumberOfSites();
    } else {
      return 0;
    }
  }

  @Override
  public CoobservabilityCounterExampleProxy getCounterExample()
  {
    return (CoobservabilityCounterExampleProxy) super.getCounterExample();
  }


  //#########################################################################
  //# Overrides for net.sourceforge.waters.model.analysis.AnalysisResult
  @Override
  public CoobservabilityVerificationResult getMonolithicResult()
  {
    return (CoobservabilityVerificationResult) super.getMonolithicResult();
  }


  //#########################################################################
  //# Overrides for net.sourceforge.waters.model.analysis.AnalysisResult
  @Override
  public void merge(final AnalysisResult other)
  {
    super.merge(other);
    final ModularCoobservabilityVerificationResult result =
      (ModularCoobservabilityVerificationResult) other;
    mNumberOfSites =
      Math.max(mNumberOfSites, result.mNumberOfSites);
  }


  //#########################################################################
  //# Printing
  @Override
  protected void printPart1(final PrintWriter writer)
  {
    super.printPart1(writer);
    if (mNumberOfSites >= 0) {
      writer.print("Number of supervisor sites in model: ");
      writer.println(mNumberOfSites);
    }
  }

  @Override
  protected void printCSVHorizontalHeadingsPart1(final PrintWriter writer)
  {
    super.printCSVHorizontalHeadingsPart1(writer);
    writer.print(",NumSites");
  }

  @Override
  protected void printCSVHorizontalPart1(final PrintWriter writer)
  {
    super.printCSVHorizontalPart1(writer);
    writer.print(',');
    if (mNumberOfSites >= 0) {
      writer.print(mNumberOfSites);
    }
  }


  //#########################################################################
  //# Data Members
  private int mNumberOfSites;

}
