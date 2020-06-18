//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# Copyright (C) 2004-2020 Robi Malik
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

package net.sourceforge.waters.analysis.hisc;

import java.io.PrintWriter;

import net.sourceforge.waters.analysis.annotation.ConflictPreorderResult;
import net.sourceforge.waters.analysis.compositional.CompositionalSimplificationResult;
import net.sourceforge.waters.model.analysis.AnalysisResult;
import net.sourceforge.waters.model.analysis.DefaultVerificationResult;
import net.sourceforge.waters.model.analysis.des.ModelVerifier;


/**
 * A verification result for the HISC-CP interface consistency check.
 *
 * @author Robi Malik
 */

class HISCCPVerificationResult
  extends DefaultVerificationResult
{

  //#########################################################################
  //# Constructors
  public HISCCPVerificationResult(final ModelVerifier verifier)
  {
    this(verifier.getClass());
  }

  /**
   * Creates a new verification result representing an incomplete run.
   */
  public HISCCPVerificationResult(final Class<?> clazz)
  {
    super(clazz);
    mSimplificationResult = null;
    mConflictPreorderResult = null;
  }


  //#########################################################################
  //# Simple Access
  CompositionalSimplificationResult getSimplificationResult()
  {
    return mSimplificationResult;
  }

  ConflictPreorderResult getConflictPreorderResult()
  {
    return mConflictPreorderResult;
  }


  //#########################################################################
  //# Recording Statistics
  void addSimplificationResult(final CompositionalSimplificationResult result)
  {
    if (mSimplificationResult == null) {
      mSimplificationResult = result;
    } else if (result != null) {
      mSimplificationResult.merge(result);
    }
  }

  void addConflictPreorderResult(final ConflictPreorderResult result)
  {
    if (mConflictPreorderResult == null) {
      mConflictPreorderResult = result;
    } else if (result != null) {
      mConflictPreorderResult.merge(result);
    }
  }


  //#########################################################################
  //# Overrides for net.sourceforge.waters.model.analysis.AnalysisResult
  @Override
  public void merge(final AnalysisResult other)
  {
    super.merge(other);
    final HISCCPVerificationResult result = (HISCCPVerificationResult) other;
    if (mSimplificationResult == null) {
      mSimplificationResult = result.mSimplificationResult;
    } else if (result.mSimplificationResult != null) {
      mSimplificationResult.merge(result.mSimplificationResult);
    }
    if (mConflictPreorderResult == null) {
      mConflictPreorderResult = result.mConflictPreorderResult;
    } else if (result.mConflictPreorderResult != null) {
      mConflictPreorderResult.merge(result.mConflictPreorderResult);
    }
  }

  @Override
  public void print(final PrintWriter writer)
  {
    super.print(writer);
    if (mSimplificationResult != null) {
      writer.println("--------------------------------------------------");
      mSimplificationResult.print(writer);
    }
    if (mConflictPreorderResult != null) {
      writer.println("--------------------------------------------------");
      mConflictPreorderResult.print(writer);
    }
  }

  @Override
  public void printCSVHorizontalHeadings(final PrintWriter writer)
  {
    super.printCSVHorizontalHeadings(writer);
    if (mSimplificationResult != null) {
      writer.print(",Simp,");
      mSimplificationResult.printCSVHorizontalHeadings(writer);
    }
    if (mConflictPreorderResult != null) {
      writer.print(",LC,");
      mConflictPreorderResult.printCSVHorizontalHeadings(writer);
    }
  }

  @Override
  public void printCSVHorizontal(final PrintWriter writer)
  {
    super.printCSVHorizontal(writer);
    if (mSimplificationResult != null) {
      writer.print(",,");
      mSimplificationResult.printCSVHorizontal(writer);
    }
    if (mConflictPreorderResult != null) {
      writer.print(",,");
      mConflictPreorderResult.printCSVHorizontal(writer);
    }
  }


  //#########################################################################
  //# Data Members
  private CompositionalSimplificationResult mSimplificationResult;
  private ConflictPreorderResult mConflictPreorderResult;

}
