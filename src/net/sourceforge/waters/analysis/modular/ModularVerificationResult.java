//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# Copyright (C) 2004-2015 Robi Malik
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








