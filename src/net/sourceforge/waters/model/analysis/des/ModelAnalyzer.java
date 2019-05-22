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

import java.util.List;

import net.sourceforge.waters.analysis.options.Parameter;
import net.sourceforge.waters.model.analysis.Abortable;
import net.sourceforge.waters.model.analysis.AnalysisException;
import net.sourceforge.waters.model.analysis.AnalysisResult;
import net.sourceforge.waters.model.analysis.KindTranslator;
import net.sourceforge.waters.model.analysis.OverflowException;
import net.sourceforge.waters.model.des.AutomatonProxy;
import net.sourceforge.waters.model.des.ProductDESProxy;
import net.sourceforge.waters.model.des.ProductDESProxyFactory;


/**
 * <P>The main model analyser interface.</P>
 *
 * <P>A model analyser takes a finite-state machine model as input and
 * performs a particular kind of analysis on it. This may be verification
 * ({@link ModelVerifier}) to determine whether a certain property is
 * satisfied, or a synthesis operation that computes some new automaton
 * from the input model.</P>
 *
 * <P>The input model may contain several automata, in which case the
 * system to be analysed is their synchronous product.</P>
 *
 * @author Robi Malik
 */

public interface ModelAnalyzer
  extends Abortable
{

  //#########################################################################
  //# Invocation
  /**
   * Runs the analysis operation associated with this model analyser.
   * @return <CODE>true</CODE> if analysis was completed with successful
   *         result, <CODE>false</CODE> if analysis was completed with
   *         unsuccessful result,
   * @throws NullPointerException to indicate that no model has been
   *         specified.
   * @throws AnalysisException to indicate that analysis was aborted or
   *         could not be completed due to errors in the model or due to
   *         resource limitations.
   */
  public boolean run() throws AnalysisException;


  //#########################################################################
  //# Simple Access Methods
  /**
   * Gets the factory used by this model analyser to create result
   * objects.
   */
  public ProductDESProxyFactory getFactory();

  /**
   * Gets the model under investigation by this model analyser.
   */
  public ProductDESProxy getModel();

  /**
   * Sets a new model to be investigated by this model analyser.
   */
  public void setModel(ProductDESProxy model);

  /**
   * Sets a new automaton to be investigated by this model analyser.
   * This method creates initialises the model analyser to check a model
   * consisting of a single automaton.
   */
  public void setModel(AutomatonProxy aut);

  /**
   * Sets a kind translator to be used by this model analyser.
   */
  public void setKindTranslator(KindTranslator translator);

  /**
   * Gets the kind translator used by this model analyser.
   * The kind translator is used to remap component and event kinds
   * for the sake of a single algorithm, and thus implement different
   * checks using the same code.
   */
  public KindTranslator getKindTranslator();



  //#########################################################################
  //# Parameters
  /**
   * Sets whether computation of full output is enabled.
   * If set to <CODE>true</CODE> (the default), the model analyser should
   * compute detailed results (e.g., counterexamples or supervisors) in
   * all cases where it is applicable. If disabled, the model analyser only
   * needs to return a Boolean result, which may enable it to run faster.
   */
  public void setDetailedOutputEnabled(boolean enable);

  /**
   * Returns whether computation of full output is enabled.
   * @see #setDetailedOutputEnabled(boolean) setDetailedOutputEnabled()
   */
  public boolean isDetailedOutputEnabled();

  /**
   * Returns whether or not this model analyser supports nondeterministic
   * automata. Only model analysers that return <CODE>true</CODE> on this
   * call are guaranteed to give correct results when presented an input
   * containing nondeterministic automata. Model analysers not supporting
   * nondeterministic automata should throw
   * {@link NondeterministicDESException} when encountering a nondeterministic
   * automaton in their input.
   */
  public boolean supportsNondeterminism();

  /**
   * Sets the node limit for this model analyser.
   * If set, the node limit is the maximum number of nodes the analyser
   * is allowed to keep in memory at any one time. If this number is
   * exceeded, an {@link OverflowException} is thrown.
   * A 'node' here represents a basic unit of memory such as a state
   * in a synchronous product or a BDD node.
   * @param  limit  The new node limit, or {@link Integer#MAX_VALUE} to
   *                indicate that no node limit is to be used.
   */
  public void setNodeLimit(final int limit);

  /**
   * Gets the node limit for this model analyser.
   * @return The current node limit, or {@link Integer#MAX_VALUE} to indicate
   *         that no node limit is used.
   * @see    #setNodeLimit(int)
   */
  public int getNodeLimit();

  /**
   * Sets the transition limit for this model analyser.
   * If set, the transition limit is the maximum number of transitions the
   * analyser is allowed to keep in memory at any one time. If this number
   * is exceeded, an {@link OverflowException} is thrown.
   * Many algorithms do not explicitly store any transitions and can ignore
   * this parameter.
   * @param  limit  The new transition limit, or {@link Integer#MAX_VALUE} to
   *                indicate that no transition limit is to be used. A value
   *                of&nbsp;0 can be used to request that transitions should
   *                not be stored explicitly.
   */
  public void setTransitionLimit(final int limit);

  /**
   * Gets the transition limit for this model analyser.
   * @return The current transition limit, or {@link Integer#MAX_VALUE} to
   *         indicate that no transition limit is used.
   * @see    #setTransitionLimit(int)
   */
  public int getTransitionLimit();

  /**
   * Returns a list of configurable parameters supported by this model analyser.
   */
  public List<Parameter> getParameters();


  //#########################################################################
  //# Accessing the Result
  /**
   * Gets the result of the last analysis run.
   * @return An object containing all information associated with the
   *         analysis result, or <CODE>null</CODE> if this method is
   *         called before {@link #run() run()} has been called.
   */
  public AnalysisResult getAnalysisResult();

  /**
   * Resets the analysis result computed by this model analyser.
   * This method is used when the model or other parameters are changed
   * after a run, to indicate that the previous results are no longer
   * valid.
   */
  public void clearAnalysisResult();

  /**
   * Creates an  empty analysis result of the type returned by this model
   * analyser. This factory method merely creates the result record, without
   * associating it with the model analyser.
   */
  public AnalysisResult createAnalysisResult();

}
