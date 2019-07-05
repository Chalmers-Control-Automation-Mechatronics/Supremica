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

package net.sourceforge.waters.analysis.trcomp;

import java.io.File;

import net.sourceforge.waters.analysis.abstraction.TransitionRelationSimplifier;
import net.sourceforge.waters.analysis.compositional.CompositionalAnalysisResult;
import net.sourceforge.waters.analysis.compositional.SelectionHeuristic;
import net.sourceforge.waters.analysis.tr.ListBufferTransitionRelation;
import net.sourceforge.waters.analysis.tr.TRAutomatonProxy;
import net.sourceforge.waters.model.analysis.AnalysisConfigurationException;
import net.sourceforge.waters.model.analysis.EnumFactory;
import net.sourceforge.waters.model.analysis.KindTranslator;
import net.sourceforge.waters.model.analysis.OverflowException;
import net.sourceforge.waters.model.analysis.des.AbstractModelAnalyzer;
import net.sourceforge.waters.model.analysis.des.ModelAnalyzer;
import net.sourceforge.waters.model.des.ProductDESProxy;
import net.sourceforge.waters.plain.des.ProductDESElementFactory;


/**
 * <P>An abstract base class for compositional model analysers based on
 * the {@link TRAutomatonProxy} representation of automata.</P>
 *
 * <P>This class almost only contains abstract methods, which describe the
 * interface of all compositional analysers. It is subclassed in two different
 * ways, depending on whether the compositional algorithm is implemented
 * directly ({@link AbstractTRCompositionalAnalyzer}) or by delegation
 * ({@link AbstractTRDelegatingAnalyzer}).</P>
 *
 * @author Robi Malik
 */

public abstract class AbstractTRAnalyzer
  extends AbstractModelAnalyzer
  implements ModelAnalyzer
{

  //#########################################################################
  //# Constructors
  public AbstractTRAnalyzer(final ProductDESProxy model,
                            final KindTranslator translator)
  {
    super(model, ProductDESElementFactory.getInstance(), translator);
  }


  //#########################################################################
  //# Interface for net.sourceforge.waters.model.analysis.des.ModelAnalyser
  @Override
  public boolean supportsNondeterminism()
  {
    return true;
  }

  @Override
  public CompositionalAnalysisResult getAnalysisResult()
  {
    return (CompositionalAnalysisResult) super.getAnalysisResult();
  }

  @Override
  public CompositionalAnalysisResult createAnalysisResult()
  {
    return new CompositionalAnalysisResult(getClass());
  }


  //#########################################################################
  //# Configuration
  /**
   * Gets the factory to obtain transition relation simplifiers for this
   * compositional analyser. The objects returned by this factory can be
   * passed to the {@link #setSimplifierCreator(TRToolCreator)
   * setSimplifierCreator()} method.
   */
  public abstract EnumFactory<TRToolCreator<TransitionRelationSimplifier>>
    getTRSimplifierFactory();

  /**
   * Sets the tool that creates the transition relation simplifier that
   * defines the abstraction chain. Possible arguments for this method
   * can be obtained from the factory returned by {@link
   * #getTRSimplifierFactory()}.
   */
  public abstract void setSimplifierCreator
    (final TRToolCreator<TransitionRelationSimplifier> creator);

  /**
   * Gets the tool that creates the transition relation simplifier that
   * defines the abstraction chain.
   * @see #setSimplifierCreator(TRToolCreator) setSimplifierCreator()
   */
  public abstract TRToolCreator<TransitionRelationSimplifier>
    getSimplifierCreator();

  /**
   * Gets the factory to obtain preselection heuristics for this
   * compositional analyser. The objects returned by this factory can be
   * passed to the {@link #setPreselectionHeuristic(TRPreselectionHeuristic)
   * setPreselectionHeuristic()} method.
   */
  public abstract EnumFactory<TRPreselectionHeuristic>
    getPreselectionHeuristicFactory();

  /**
   * Sets the preselection heuristic to create the possible candidates for
   * composition. Possible arguments for this method can be obtained from the
   * factory returned by {@link #getPreselectionHeuristicFactory()}.
   */
  public abstract void setPreselectionHeuristic
    (final TRPreselectionHeuristic heu);

  /**
   * Gets the preselection heuristic used to create the possible candidates
   * for composition.
   * @see #setPreselectionHeuristic(TRPreselectionHeuristic) setPreselectionHeuristic()
   */
  public abstract TRPreselectionHeuristic getPreselectionHeuristic();

  /**
   * Gets the factory to obtain selection heuristics for this
   * compositional analyser. The objects returned by this factory can be
   * passed to the {@link #setPreselectionHeuristic(TRPreselectionHeuristic)
   * setPreselectionHeuristic()} method.
   */
  public abstract EnumFactory<SelectionHeuristic<TRCandidate>>
    getSelectionHeuristicFactory();

  /**
   * Sets the selection heuristic to choose a candidates for composition from
   * the collection returned by the preselection heuristic. Possible arguments
   * for this method can be obtained from the factory returned by {@link
   * #getSelectionHeuristicFactory()}.
   */
  public abstract void setSelectionHeuristic
    (final SelectionHeuristic<TRCandidate> heu);

  /**
   * Gets the selection heuristic used to choose a candidates for composition
   * from the collection returned by the preselection heuristic.
   * @see #setSelectionHeuristic(SelectionHeuristic) setSelectionHeuristic()
   */
  public abstract SelectionHeuristic<TRCandidate> getSelectionHeuristic();

  /**
   * Sets the monolithic analyser. The monolithic analyser is applied to the
   * final abstraction when no further simplification is possible.
   */
  public abstract void setMonolithicAnalyzer(final ModelAnalyzer mono);

  /**
   * Gets the monolithic analyser.
   * @see #setMonolithicAnalyzer(ModelAnalyzer) setMonolithicAnalyzer()
   */
  public abstract ModelAnalyzer getMonolithicAnalyzer();

  /**
   * Sets the internal state limit. This is the maximum number of states
   * allowed in intermediate automata during compositional minimisation.
   * If no further abstraction is possible within the internal state limit,
   * the abstraction process must stop and call the monolithic analyser.
   */
  public abstract void setInternalStateLimit(final int limit);

  /**
   * Gets the internal state limit.
   * @see #setInternalStateLimit(int) setInternalStateLimit()
   */
  public abstract int getInternalStateLimit();

  /**
   * Sets the monolithic state limit. This is the maximum number of states
   * allowed for analysis of the final abstraction result that could be
   * reached within the internal state and transition limits. If the monolithic
   * analyser cannot obtain a result with the monolithic state limit, an
   * {@link OverflowException} will be thrown.
   */
  public abstract void setMonolithicStateLimit(final int limit);

  /**
   * Gets the monolithic state limit.
   * @see #setMonolithicStateLimit(int) setMonolithicStateLimit()
   */
  public abstract int getMonolithicStateLimit();

  /**
   * Sets the internal transition limit. This is the maximum number of
   * transitions allowed in intermediate automata during compositional
   * minimisation. If no further abstraction is possible within the internal
   * transition limit, the abstraction process must stop and call the
   * monolithic analyser.
   */
  public abstract void setInternalTransitionLimit(final int limit);

  /**
   * Gets the internal transition limit.
   * @see #setInternalTransitionLimit(int) setInternalTransitionLimit()
   */
  public abstract int getInternalTransitionLimit();

  /**
   * Sets the monolithic transition limit. This is the maximum number of
   * transitions allowed for analysis of the final abstraction result that
   * could be reached within the internal state and transition limits. If the
   * monolithic analyser cannot obtain a result with the monolithic transition
   * limit, an {@link OverflowException} will be thrown.
   */
  public abstract void setMonolithicTransitionLimit(final int limit);

  /**
   * Gets the monolithic transition limit.
   * @see #setMonolithicTransitionLimit(int) setMonolithicTransitionLimit()
   */
  public abstract int getMonolithicTransitionLimit();

  /**
   * Sets whether blocked events are to be considered in abstraction.
   * @see #isBlockedEventsEnabled()
   */
  public abstract void setBlockedEventsEnabled(final boolean enable);

  /**
   * Returns whether blocked events are considered in abstraction.
   * Blocked events are events that are disabled in all reachable states of
   * some automaton. If supported, this will remove all transitions with
   * blocked events from the model.
   * @see #setBlockedEventsEnabled(boolean) setBlockedEventsEnabled()
   */
  public abstract boolean isBlockedEventsEnabled();

  /**
   * Sets whether failing events are to be considered in abstraction.
   * @see #isFailingEventsEnabled()
   */
  public abstract void setFailingEventsEnabled(final boolean enable);

  /**
   * Returns whether failing events are considered in abstraction.
   * Failing events are events that always lead to a dump state in some
   * automaton. If supported, this will redirect failing events in other
   * automata to dump states.
   * @see #setFailingEventsEnabled(boolean) setFailingEventsEnabled()
   */
  public abstract boolean isFailingEventsEnabled();

  /**
   * Sets whether selfloop-only events are to be considered in abstraction.
   * @see #isSelfloopOnlyEventsEnabled()
   */
  public abstract void setSelfloopOnlyEventsEnabled(final boolean enable);

  /**
   * Returns whether selfloop-only events are considered in abstraction.
   * Selfloop-only events are events that appear only as selfloops in the
   * entire model or in all but one automaton in the model. Events that
   * are selfloop-only in the entire model can be removed, while events
   * that are selfloop-only in all but one automaton can be used to
   * simplify that automaton.
   * @see #setSelfloopOnlyEventsEnabled(boolean) setSelfloopOnlyEventsEnabled()
   */
  public abstract boolean isSelfloopOnlyEventsEnabled();

  /**
   * Sets whether always enabled events are to be considered in abstraction.
   * @see #isAlwaysEnabledEventsEnabled()
   */
  public abstract void setAlwaysEnabledEventsEnabled(final boolean enable);

  /**
   * Returns whether always enabled events are considered in abstraction.
   * Always enabled events are events that are enabled in all states of the
   * entire model or of all but one automaton in the model. Always enabled
   * events can help to simplify automata.
   * @see #setAlwaysEnabledEventsEnabled(boolean) setAlwaysEnabledEventsEnabled()
   */
  public abstract boolean isAlwaysEnabledEventsEnabled();

  /**
   * Sets whether state and event encodings are to be preserved when copying
   * input automata of type {@link TRAutomatonProxy}. If set, the input
   * automata will be used with the exact same encoding, which has to be
   * compatible with the expectations of the abstraction procedures.
   * Otherwise, the encoding may change, resulting in counterexamples
   * with possibly different encoding.
   */
  public void setPreservingEncodings(final boolean preserved)
  {
    mPreservingEncodings = preserved;
  }

  /**
   * Returns whether state and event encodings are to be preserved when
   * copying input automata of type {@link TRAutomatonProxy}.
   * @see #setPreservingEncodings(boolean) setPreservingEncodings()
   */
  public boolean isPreservingEncodings()
  {
    return mPreservingEncodings;
  }

  /**
   * Sets a file name to dump abstracted models before monolithic
   * verification. If set, any abstracted model will be written to this file
   * before being sent for monolithic verification.
   */
  public abstract void setMonolithicDumpFile(final File file);

  /**
   * Returns the file name abstracted models are written to.
   * @see #setMonolithicDumpFile(File) setMonolithicDumpFile()
   */
  public abstract File getMonolithicDumpFile();

  /**
   * Sets whether output checking is enabled.
   * If enabled, the generated output (typically counterexample) is checked
   * for correctness after each step during output computation. This can be a
   * very slow process, and is only recommend for testing and debugging.
   * This setting is disabled by default.
   */
  public abstract void setOutputCheckingEnabled(final boolean checking);

  /**
   * Returns whether output checking is enabled.
   * @see #setOutputCheckingEnabled(boolean) setOutputCheckingEnabled()
   */
  public abstract boolean isOutputCheckingEnabled();


  //#########################################################################
  //# Specific Access
  /**
   * Returns the truly compositional analyser used. This method returns
   * the analyser that performs the actual compositional minimisation task,
   * which may this analyser itself or its delegate.
   */
  protected abstract AbstractTRCompositionalAnalyzer getCompositionalAnalyzer();

  /**
   * Gets the preferred input configuration for transition relations.
   * This can be used to determine how best to construct transition
   * relations ({@link ListBufferTransitionRelation}, {@link TRAutomatonProxy})
   * to pass into the analysis process.
   * @return Combination of flags defined in
   *         {@link ListBufferTransitionRelation}.
   *         A return value of&nbsp;0 indicates no preference.
   * @throws AnalysisConfigurationException to indicate a failure while
   *         initialising the abstraction chain.
   * @see ListBufferTransitionRelation#CONFIG_SUCCESSORS
   * @see ListBufferTransitionRelation#CONFIG_PREDECESSORS
   * @see ListBufferTransitionRelation#CONFIG_ALL
   */
  protected abstract int getPreferredInputConfiguration()
    throws AnalysisConfigurationException;


  //#########################################################################
  //# Data Members
  private boolean mPreservingEncodings = false;

}
