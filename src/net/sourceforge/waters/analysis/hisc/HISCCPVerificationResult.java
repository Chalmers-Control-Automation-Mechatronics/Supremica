//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters HISC
//# PACKAGE: net.sourceforge.waters.analysis.hisc
//# CLASS:   HISCCPVerificationResult
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.analysis.hisc;

import java.io.PrintWriter;

import net.sourceforge.waters.analysis.annotation.ConflictPreorderResult;
import net.sourceforge.waters.analysis.compositional.CompositionalSimplificationResult;
import net.sourceforge.waters.model.analysis.AnalysisResult;
import net.sourceforge.waters.model.analysis.DefaultVerificationResult;


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
  /**
   * Creates a new verification result representing an incomplete run.
   */
  public HISCCPVerificationResult()
  {
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
