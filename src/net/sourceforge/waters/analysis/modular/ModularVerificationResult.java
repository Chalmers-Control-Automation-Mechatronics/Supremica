//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters Analysis
//# PACKAGE: net.sourceforge.waters.analysis.modular
//# CLASS:   ModularVerificationResult
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.analysis.modular;

import java.util.ArrayList;
import java.util.Collection;

import net.sourceforge.waters.model.analysis.AnalysisResult;
import net.sourceforge.waters.model.analysis.DefaultVerificationResult;
import net.sourceforge.waters.model.analysis.VerificationResult;
import net.sourceforge.waters.model.des.AutomatonProxy;


/**
 * A result record that can be returned by a modular analysis
 * algorithms.
 *
 * In addition to a standard verification result ({@link VerificationResult}),
 * the modular verification may contain a collection of specifications
 * found to be not controllable.
 *
 * @author Robi Malik
 */

public class ModularVerificationResult
  extends DefaultVerificationResult
{

  //#########################################################################
  //# Constructors
  /**
   * Creates a verification result representing an incomplete verification run.
   */
  public ModularVerificationResult()
  {
    mFailedSpecs = null;
  }


  //#########################################################################
  //# Simple Access Methods
  public Collection<AutomatonProxy> getFailedSpecs()
  {
    return mFailedSpecs;
  }

  //#########################################################################
  //# Providing Statistics
  void addFailedSpecs(final Collection<AutomatonProxy> failedSpecs)
  {
    if (mFailedSpecs == null) {
      mFailedSpecs = new ArrayList<AutomatonProxy>(failedSpecs);
    } else {
      mFailedSpecs.addAll(failedSpecs);
    }
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
    }
  }


  //#########################################################################
  //# Data Members
  private Collection<AutomatonProxy> mFailedSpecs;

}
