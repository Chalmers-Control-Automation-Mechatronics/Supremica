//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# Copyright (C) 2004-2019 Robi Malik
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

package net.sourceforge.waters.model.analysis.des;

import net.sourceforge.waters.model.analysis.VerificationResult;
import net.sourceforge.waters.model.des.CounterExampleProxy;
import net.sourceforge.waters.model.des.ProductDESProxy;
import net.sourceforge.waters.model.des.ProductDESProxyFactory;
import net.sourceforge.waters.model.des.TraceProxy;


/**
 * <P>A model verifier takes a finite-state machine model as input and
 * performs a particular kind of verification on it. In contrast to a
 * general {@link ModelAnalyzer}, a verifier always produces a Boolean
 * result that states whether a particular property checked is satisfied or
 * not. Furthermore, if the property is found not to be satisfied, the
 * model verifier provides a counterexample in the form of a sequence of
 * events, explaining why the property that was checked is not
 * satisfied.</P>
 *
 * <P>To use a model verifier, the user first creates an instance of a
 * subclass of this class, and sets up the model to be checked as well as
 * any other parameters that may be needed. Then verification is started
 * using the {@link #run() run()} method. Afterwards results can be queried
 * using the {@link #isSatisfied()} and {@link #getCounterExample()}
 * methods. This all can be done with the following code.</P>
 *
 * <P>
 * <CODE>{@link net.sourceforge.waters.model.des.ProductDESProxyFactory}
 *   desfactory =
 *   {@link net.sourceforge.waters.plain.des.ProductDESElementFactory}.{@link
 *   net.sourceforge.waters.plain.des.ProductDESElementFactory#getInstance()
 *   getInstance}();</CODE><BR>
 * <CODE>{@link ModelAnalyzerFactory} vfactory =
 *   {@link net.sourceforge.waters.analysis.monolithic.MonolithicModelAnalyzerFactory}.{@link
 *   net.sourceforge.waters.analysis.monolithic.MonolithicModelAnalyzerFactory#getInstance()
 *   getInstance}(); //</CODE> <I>e.g.</I><BR>
 * <CODE>ModelVerifier verifier = vfactory.{@link
 *   ModelAnalyzerFactory#createControllabilityChecker(ProductDESProxyFactory)
 *   createControllabilityChecker}(desfactory); //</CODE> <I>e.g.</I><BR>
 * <CODE>verifier.{@link #setModel(ProductDESProxy) setModel}(des);</CODE><BR>
 * <CODE>verifier.{@link #run() run()};</CODE><BR>
 * <CODE>boolean result = verifier.{@link #isSatisfied()};</CODE><BR>
 * <CODE>if (result) {</CODE><BR>
 * <CODE>&nbsp;&nbsp;//</CODE> <I>property satisfied ...</I><BR>
 * <CODE>} else {</CODE><BR>
 * <CODE>&nbsp;&nbsp;//</CODE> <I>property not satisfied ...</I><BR>
 * <CODE>&nbsp;&nbsp;{@link TraceProxy}
 *   counterexample = verifier.{@link #getCounterExample()};</CODE><BR>
 * <CODE>}</CODE></P>
 *
 * <P>This class is subclassed to implement model checking algorithms for
 * various properties.</P>
 *
 * @author Robi Malik
 */

public interface ModelVerifier extends ModelAnalyzer
{

  //#########################################################################
  //# Configuration
  /**
   * <P>Requests the computation of a short counterexample.
   * With this setting the user can express the preference for a
   * counterexample that is as short as possible. When this setting is
   * disabled (the default), model verifiers will generally use the fastest
   * possible means to compute a counterexample. When the setting is enabled,
   * some model verifiers will use slower algorithms that give shorter or
   * minimal counterexamples. It depends on the specific model verifier
   * whether the request for a short counterexample is honoured, and whether
   * or not minimality can be guaranteed.
   */
  public void setShortCounterExampleRequested(final boolean req);

  /**
   * Returns whether a short counterexample is requested.
   * @see #setShortCounterExampleRequested(boolean) setEarlyDeadlockEnabled()
   */
  public boolean isShortCounterExampleRequested();


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
   *         counterexample, or counterexamples are disabled.
   */
  public CounterExampleProxy getCounterExample();

  /**
   * Sets whether counterexample computation is enabled.
   * If set to <CODE>true</CODE> (the default), the model verifier should
   * compute a counterexample in all cases where it determines that a
   * property is not satisfied. If disabled, the model verifier does not
   * need to provide for a counterexample, which may enable it to run
   * faster.
   */
  public void setCounterExampleEnabled(boolean enable);

  /**
   * Returns whether counterexample computation is enabled.
   * @see #setCounterExampleEnabled(boolean) setCounterExampleEnabled()
   */
  public boolean isCounterExampleEnabled();

  @Override
  public VerificationResult getAnalysisResult();

  @Override
  public VerificationResult createAnalysisResult();

}
