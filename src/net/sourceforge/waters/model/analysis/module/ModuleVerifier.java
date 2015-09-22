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

package net.sourceforge.waters.model.analysis.module;

import net.sourceforge.waters.model.analysis.VerificationResult;
import net.sourceforge.waters.model.analysis.des.ModelAnalyzer;
import net.sourceforge.waters.model.des.TraceProxy;


/**
 * <P>A module verifier takes a module as input and
 * performs a particular kind of verification on it. In contrast to a
 * general {@link ModuleAnalyzer}, a verifier always produces a Boolean
 * result that states whether a particular property checked is satisfied or
 * not. Furthermore, if the property is found not to be satisfied, the
 * module verifier provides a counterexample in the form of a sequence of
 * events, explaining why the property that was checked is not
 * satisfied.</P>
 *
 * <P>This class is subclassed to implement model checking algorithms for
 * various properties.</P>
 *
 * @author Robi Malik, Sahar Mohajerani
 */

public interface ModuleVerifier extends ModuleAnalyzer
{

  //#########################################################################
  //# More Specific Access to the Results
  /**
   * Gets the result of model checking.
   * @return <CODE>true</CODE> if the property checked is satisfied,
   *         <CODE>false</CODE> otherwise.
   * @throws IllegalStateException if this method is called before
   *         model checking has completed, i.e., before
   *         {@link ModelAnalyzer#run() run()} has been called.
   */
  public boolean isSatisfied();

  /**
   * Gets a counterexample if model checking has found that the
   * property checked is not satisfied.
   * @return A trace object constructed for the model that was checked.
   *         It shares events and automata with the input model.
   * @throws IllegalStateException if this method is called before
   *         model checking has completed, i.e., before {@link
   *         ModelAnalyzer#run() run()} has been called, or model checking has
   *         found that the property is satisfied and there is no
   *         counterexample.
   */
  public TraceProxy getCounterExample();

  @Override
  public VerificationResult getAnalysisResult();

}








