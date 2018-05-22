//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# Copyright (C) 2004-2018 Robi Malik
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

import net.sourceforge.waters.analysis.monolithic.MonolithicNerodeEChecker;
import net.sourceforge.waters.model.analysis.DefaultVerificationResult;
import net.sourceforge.waters.model.analysis.des.ModelVerifier;
import net.sourceforge.waters.model.des.SafetyTraceProxy;


/**
 * A result record returned by a {@link MonolithicNerodeEChecker} A verification
 * result contains the information on whether the property checked is true or
 * false, and in the latter case, it also contains two counterexamples.
 *
 * @author Mahvash Baloch
 */
public class NerodeEquVerificationResult extends DefaultVerificationResult
{
  // #########################################################################
  // # Constructors
  /**
   * Creates a verification result representing an incomplete run.
   * @param  verifier The model analyser creating this result.
   */
  public NerodeEquVerificationResult(final ModelVerifier verifier)
  {
    this(verifier.getClass());
  }

  /**
   * Creates a verification result representing an incomplete run.
   * @param  clazz    The class of the model verifier creating this result.
   */
  public NerodeEquVerificationResult(final Class<?> clazz)
  {
    super(clazz);
    mCounterExample2 = null;
  }

  // #########################################################################
  // # Simple Access Methods
  /**
   * Get all results from conflict checker runs (one result for each answer
   * event).
   */

   public SafetyTraceProxy getCounterExample2()
  {
    return mCounterExample2;
  }
  //#########################################################################
  //# Providing Statistics
  /**
   * Sets all the conflict checker results for this SIC property V verification
   * (one result for each answer event).
   */

  public void setCounterExample2(final SafetyTraceProxy CounterExample)
  {
    mCounterExample2 = CounterExample;
  }

  //#########################################################################
  //# Data Members
  //private TraceProxy mCounterExample1;
  private SafetyTraceProxy mCounterExample2;

}
