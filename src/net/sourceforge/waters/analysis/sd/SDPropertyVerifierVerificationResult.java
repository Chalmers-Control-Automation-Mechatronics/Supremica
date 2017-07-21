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

package net.sourceforge.waters.analysis.sd;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

import net.sourceforge.waters.model.analysis.DefaultVerificationResult;
import net.sourceforge.waters.model.analysis.VerificationResult;
import net.sourceforge.waters.model.analysis.des.ModelVerifier;


/**
 * A result record returned by a {@link SDSingularProhibitableBehaviorVerifier}.
 * A verification result contains the information on whether the property
 * checked is true or false, and in the latter case, it also contains a
 * counterexample. In addition, it contains individual statistics about the
 * Language Inclusion Checker run for every prohibitable event.
 *
 * @author Rachel Francis, Mahvash Baloch
 */
public class SDPropertyVerifierVerificationResult
  extends DefaultVerificationResult
{
  //#########################################################################
  //# Constructors
  /**
   * Creates a verification result representing an incomplete run.
   * @param  verifier The model analyser creating this result.
   */
  public SDPropertyVerifierVerificationResult(final ModelVerifier verifier)
  {
    this(verifier.getClass());
  }

  /**
   * Creates a verification result representing an incomplete run.
   * @param  clazz    The class of the model verifier creating this result.
   */
  public SDPropertyVerifierVerificationResult(final Class<?> clazz)
  {
    super(clazz);
    mCheckerStats = new ArrayList<VerificationResult>();
  }


  //#########################################################################
  //# Simple Access Methods
  /**
   * Get all results from Modular Language Inclusion checker runs (one result for each
   * prohibitable event).
   */
  public List<? extends VerificationResult> getCheckerResults()
  {
    return mCheckerStats;
  }


  //#########################################################################
  //# Providing Statistics
  /**
   * Sets all the language Inclusion checker results for this SD property verification
   * (one result for each prohibitable event).
   */
  public void setCheckerResult
    (final List<? extends VerificationResult> results)
  {
    mCheckerStats = results;
  }


  //#########################################################################
  //# Printing
  @Override
  public void print(final PrintWriter writer)
  {
    super.print(writer);
    for (final VerificationResult result : mCheckerStats) {
      result.print(writer);
    }
  }

  @Override
  public void printCSVHorizontal(final PrintWriter writer)
  {
    super.printCSVHorizontal(writer);
    for (final VerificationResult result : mCheckerStats) {
      writer.print(',');
      result.printCSVHorizontal(writer);
    }
  }

  @Override
  public void printCSVHorizontalHeadings(final PrintWriter writer)
  {
    super.printCSVHorizontalHeadings(writer);
    for (final VerificationResult result : mCheckerStats) {
      writer.print(',');
      result.printCSVHorizontalHeadings(writer);
    }
  }


  //#########################################################################
  //# Data Members
  private List<? extends VerificationResult> mCheckerStats;

}
