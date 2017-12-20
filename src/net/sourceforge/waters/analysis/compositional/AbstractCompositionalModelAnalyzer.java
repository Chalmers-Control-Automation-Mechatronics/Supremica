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

package net.sourceforge.waters.analysis.compositional;

import gnu.trove.iterator.TObjectByteIterator;
import gnu.trove.map.hash.TObjectByteHashMap;
import gnu.trove.set.hash.THashSet;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.Set;

import net.sourceforge.waters.analysis.monolithic.MonolithicSynchronousProductBuilder;
import net.sourceforge.waters.analysis.tr.EventEncoding;
import net.sourceforge.waters.analysis.tr.EventStatus;
import net.sourceforge.waters.analysis.tr.ListBufferTransitionRelation;
import net.sourceforge.waters.analysis.tr.StateEncoding;
import net.sourceforge.waters.model.analysis.AnalysisException;
import net.sourceforge.waters.model.analysis.AnalysisResult;
import net.sourceforge.waters.model.analysis.EnumFactory;
import net.sourceforge.waters.model.analysis.KindTranslator;
import net.sourceforge.waters.model.analysis.ListedEnumFactory;
import net.sourceforge.waters.model.analysis.OverflowException;
import net.sourceforge.waters.model.analysis.des.AbstractConflictChecker;
import net.sourceforge.waters.model.analysis.des.AbstractModelAnalyzer;
import net.sourceforge.waters.model.analysis.des.EventNotFoundException;
import net.sourceforge.waters.model.analysis.des.ModelAnalyzer;
import net.sourceforge.waters.model.analysis.des.SynchronousProductResult;
import net.sourceforge.waters.model.analysis.des.SynchronousProductStateMap;
import net.sourceforge.waters.model.base.ProxyTools;
import net.sourceforge.waters.model.des.AutomatonProxy;
import net.sourceforge.waters.model.des.AutomatonTools;
import net.sourceforge.waters.model.des.EventProxy;
import net.sourceforge.waters.model.des.ProductDESProxy;
import net.sourceforge.waters.model.des.ProductDESProxyFactory;
import net.sourceforge.waters.model.des.StateProxy;
import net.sourceforge.waters.model.des.TransitionProxy;
import net.sourceforge.waters.model.marshaller.MarshallingTools;
import net.sourceforge.waters.xsd.base.ComponentKind;
import net.sourceforge.waters.xsd.base.EventKind;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


/**
 * <P>A general compositional model analyser to be subclassed for different
 * algorithms.</P>
 *
 * <P>This model analyser implements compositional minimisation
 * of the input model, and leaves it to the subclasses to decide what is
 * to be done with the minimisation result. It provides a variety of
 * candidate selection heuristics, which can be configured by the user.</P>
 *
 * <P><I>References:</I><BR>
 * Hugo Flordal, Robi Malik. Compositional Verification in Supervisory Control.
 * SIAM Journal of Control and Optimization, 48(3), 1914-1938, 2009.<BR>
 * Robi Malik, Ryan Leduc. Compositional Nonblocking Verification Using
 * Generalised Nonblocking Abstractions, IEEE Transactions on Automatic
 * Control <STRONG>58</STRONG>(8), 1-13, 2013.</P>
 *
 * @author Robi Malik
 */

public abstract class AbstractCompositionalModelAnalyzer
  extends AbstractModelAnalyzer
{

  //#########################################################################
  //# Constructors
  /**
   * Creates an abstracting model verifier without a model.
   * @param factory
   *          Factory used for trace construction.
   * @param translator
   *          Kind translator used to determine event and component kinds.
   * @param abstractionFactory
   *          Factory to define the abstraction sequence to be used.
   */
  protected AbstractCompositionalModelAnalyzer
    (final ProductDESProxyFactory factory,
     final KindTranslator translator,
     final AbstractionProcedureCreator abstractionFactory)
  {
    this(factory, translator, abstractionFactory,
         new PreselectingMethodFactory());
  }

  /**
   * Creates an abstracting model verifier without a model.
   * @param factory
   *          Factory used for trace construction.
   * @param translator
   *          Kind translator used to determine event and component kinds.
   * @param abstractionFactory
   *          Factory to define the abstraction sequence to be used.
   * @param preselectingMethodFactory
   *          Enumeration factory that determines possible candidate
   *          preselection methods.
   */
  protected AbstractCompositionalModelAnalyzer
    (final ProductDESProxyFactory factory,
     final KindTranslator translator,
     final AbstractionProcedureCreator abstractionFactory,
     final PreselectingMethodFactory preselectingMethodFactory)
  {
    this(null, factory, translator, abstractionFactory,
         preselectingMethodFactory);
  }

  /**
   * Creates an abstracting model verifier to check the given model.
   * @param model
   *          The model to be checked by this model verifier.
   * @param factory
   *          Factory used for trace construction.
   * @param translator
   *          Kind translator used to determine event and component kinds.
   * @param abstractionFactory
   *          Factory to define the abstraction sequence to be used.
   */
  protected AbstractCompositionalModelAnalyzer
    (final ProductDESProxy model,
     final ProductDESProxyFactory factory,
     final KindTranslator translator,
     final AbstractionProcedureCreator abstractionFactory)
  {
    this(model, factory, translator, abstractionFactory,
         new PreselectingMethodFactory());
  }

  /**
   * Creates an abstracting model verifier to check the given model.
   * @param model
   *          The model to be checked by this model verifier.
   * @param factory
   *          Factory used for trace construction.
   * @param translator
   *          Kind translator used to determine event and component kinds.
   * @param abstractionFactory
   *          Factory to define the abstraction sequence to be used.
   * @param preselectingMethodFactory
   *          Enumeration factory that determines possible candidate
   *          preselection methods.
   */
  protected AbstractCompositionalModelAnalyzer
    (final ProductDESProxy model,
     final ProductDESProxyFactory factory,
     final KindTranslator translator,
     final AbstractionProcedureCreator abstractionFactory,
     final PreselectingMethodFactory preselectingMethodFactory)
  {
    super(model, factory, translator);
    mAbstractionProcedureCreator = abstractionFactory;
    mPreselectingMethodFactory = preselectingMethodFactory;
    // Defaults for all model analysers---please do not change.
    mPreselectingMethod = MustL;
    mSelectionHeuristic =
      CompositionalSelectionHeuristicFactory.MinS.createChainHeuristic();
    mSubsumptionEnabled = false;
    mLowerInternalStateLimit = mUpperInternalStateLimit = 100000;
    mInternalTransitionLimit = super.getTransitionLimit();
    mSynchronousProductBuilder =
      new MonolithicSynchronousProductBuilder(factory);
  }


  //#########################################################################
  //# Configuration
  /**
   * Gets the default (omega) marking to be used for conflict checks,
   * as specified by the user. This can be <CODE>null</CODE> if the
   * user wants to request the default.
   */
  public EventProxy getConfiguredDefaultMarking()
  {
    return mConfiguredDefaultMarking;
  }

  /**
   * Sets the default (omega) marking to be used for conflict checks.
   * @see #getConfiguredDefaultMarking()
   */
  public void setConfiguredDefaultMarking(final EventProxy event)
  {
    mConfiguredDefaultMarking = event;
  }

  /**
   * Gets the precondition (alpha) marking to be used for conflict checks,
   * as specified by the user. This can be <CODE>null</CODE> if the
   * user wants to request the default.
   */
  public EventProxy getConfiguredPreconditionMarking()
  {
    return mConfiguredPreconditionMarking;
  }

  /**
   * Sets the precondition (alpha) marking to be used for conflict checks.
   * @see #getConfiguredPreconditionMarking()
   */
  public void setConfiguredPreconditionMarking(final EventProxy event)
  {
    mConfiguredPreconditionMarking = event;
  }

  public abstract EnumFactory<AbstractionProcedureCreator>
    getAbstractionProcedureFactory();

  public void setAbstractionProcedureCreator
    (final AbstractionProcedureCreator creator)
  {
    mAbstractionProcedureCreator = creator;
  }

  public AbstractionProcedureCreator getAbstractionProcedureCreator()
  {
    return mAbstractionProcedureCreator;
  }

  /**
   * Gets the enumeration factory that provides the possible preselection
   * methods.
   * @see PreselectingMethod
   */
  public PreselectingMethodFactory getPreselectingMethodFactory()
  {
    return mPreselectingMethodFactory;
  }

  /**
   * Sets the preselecting heuristics used to choose candidates.
   * Possible heuristics are available as static instances of the
   * {@link AbstractCompositionalModelAnalyzer} class, or can be
   * obtained from the verifier's {@link PreselectingMethodFactory}.
   *
   * @see #MaxS
   * @see #MinT
   * @see #MustL
   * @see #Pairs
   * @see #getPreselectingMethodFactory()
   */
  public void setPreselectingMethod(final PreselectingMethod method)
  {
    mPreselectingMethod = method;
  }

  /**
   * Gets the preselecting heuristics used to choose candidates.
   * @see #setPreselectingMethod(PreselectingMethod) setPreselectingMethod()
   */
  public PreselectingMethod getPreselectingMethod()
  {
    return mPreselectingMethod;
  }

  /**
   * Gets the selection heuristic factory for this model analyser.
   * This method is used by user interface components to obtain all the
   * available heuristics and link them with their names.
   * @return A selection heuristic factory that contains selection
   *         heuristic creators for all the heuristics supported by
   *         this model analyser.
   */
  public CompositionalSelectionHeuristicFactory getSelectionHeuristicFactory()
  {
    return CompositionalSelectionHeuristicFactory.getInstance();
  }

  /**
   * Sets the selecting heuristic to be used to choose candidates.
   * Possible heuristics are available as static instances of the
   * {@link CompositionalSelectionHeuristicFactory} class and its subclasses.
   *
   * @see CompositionalSelectionHeuristicFactory#MaxC
   * @see CompositionalSelectionHeuristicFactory#MaxL
   * @see CompositionalSelectionHeuristicFactory#MinE
   * @see CompositionalSelectionHeuristicFactory#MinF
   * @see CompositionalSelectionHeuristicFactory#MinS
   * @see CompositionalSelectionHeuristicFactory#MinSync
   */
  public void setSelectionHeuristic(final SelectionHeuristicCreator creator)
  {
    final SelectionHeuristic<Candidate> chain = creator.createChainHeuristic();
    setSelectionHeuristic(chain);
  }

  /**
   * Sets the selecting heuristics to be used to choose candidates.
   * This is a low-level method that requires the user to construct the
   * heuristic explicitly. A simpler is provided as an overload that
   * accepts a selection heuristic creator.
   *
   * @see #setSelectionHeuristic(SelectionHeuristicCreator)
   */
  public void setSelectionHeuristic(final SelectionHeuristic<Candidate> heuristic)
  {
    mSelectionHeuristic = heuristic;
  }

  /**
   * Gets the selecting heuristics used to choose candidates.
   */
  public SelectionHeuristic<Candidate> getSelectionHeuristic()
  {
    return mSelectionHeuristic;
  }

  /**
   * Sets whether subsumption is enabled in the selecting heuristic.
   * If subsumption is enabled, and the heuristic returns a candidate that
   * is subsumed by another candidate, the a new candidate will be selected
   * from the list of all preselected candidates that subsume the originally
   * selected candidate. The selection heuristics will be used again to
   * resolve ties.
   * @see PreselectingMethod
   */
  public void setSubumptionEnabled(final boolean enable)
  {
    mSubsumptionEnabled = enable;
  }

  /**
   * Returns whether subsumption is enabled in the selecting heuristic.
   * @see #setSubumptionEnabled(boolean)
   */
  public boolean isSubsumptionEnabled()
  {
    return mSubsumptionEnabled;
  }

  /**
   * Sets whether special events are to be considered in abstraction.
   * This method enables or disables blocked events, selfloop-only events,
   * and failing events.
   * @see #setBlockedEventsEnabled(boolean) setBlockedEventsEnabled()
   * @see #setFailingEventsEnabled(boolean) setFailingEventsEnabled()
   * @see #setSelfloopOnlyEventsEnabled(boolean) setSelfloopOnlyEventsEnabled()
   */
  public void setUsingSpecialEvents(final boolean enable)
  {
    mBlockedEventsEnabled = mFailingEventsEnabled =
      mSelfloopOnlyEventsEnabled = enable;
  }

  /**
   * Returns whether all kinds of special events are considered in abstraction.
   * @see #setUsingSpecialEvents(boolean)
   * @see #isBlockedEventsEnabled()
   * @see #isFailingEventsEnabled()
   * @see #isSelfloopOnlyEventsEnabled()
   */
  public boolean isUsingSpecialEvents()
  {
    return
      mBlockedEventsEnabled && mFailingEventsEnabled && mSelfloopOnlyEventsEnabled;
  }

  /**
   * Sets whether blocked events are to be considered in abstraction.
   * @see #isBlockedEventsEnabled()
   */
  public void setBlockedEventsEnabled(final boolean enable)
  {
    mBlockedEventsEnabled = enable;
  }

  /**
   * Returns whether blocked events are considered in abstraction.
   * Blocked events are events that are disabled in all reachable states of
   * some automaton. If enabled, this will remove all transitions with blocked
   * events from the model.
   * @see #setBlockedEventsEnabled(boolean) setBlockedEventsEnabled()
   */
  public boolean isBlockedEventsEnabled()
  {
    return mBlockedEventsEnabled;
  }

  /**
   * Sets whether failing events are to be considered in abstraction.
   * @see #isFailingEventsEnabled()
   */
  public void setFailingEventsEnabled(final boolean enable)
  {
    mFailingEventsEnabled = enable;
  }

  /**
   * Returns whether failing events are considered in abstraction.
   * Failing events are events that always lead to a dump state in some
   * automaton. If enabled, this will redirect failing events in other
   * automata to dump states.
   * @see #setFailingEventsEnabled(boolean) setFailingEventsEnabled()
   */
  public boolean isFailingEventsEnabled()
  {
    return mFailingEventsEnabled;
  }

  /**
   * Sets whether selfloop-only events are to be considered in abstraction.
   * @see #isSelfloopOnlyEventsEnabled()
   */
  public void setSelfloopOnlyEventsEnabled(final boolean enable)
  {
    mSelfloopOnlyEventsEnabled = enable;
  }

  /**
   * Returns whether selfloop-only events are considered in abstraction.
   * Selfloop-only events are events that appear only as selfloops in the
   * entire model or in all but one automaton in the model. Events that
   * are selfloop-only in the entire model can be removed, while events
   * that are selfloop-only in all but one automaton can be used to
   * simplify that automaton.
   * @see #setSelfloopOnlyEventsEnabled(boolean) setSelfloopOnlyEventsEnabled()
   */
  public boolean isSelfloopOnlyEventsEnabled()
  {
    return mSelfloopOnlyEventsEnabled;
  }

  /**
   * Sets whether deadlock states are pruned in synchronous products.
   * @see MonolithicSynchronousProductBuilder#setPruningDeadlocks(boolean)
   */
  public void setPruningDeadlocks(final boolean pruning)
  {
    mSynchronousProductBuilder.setPruningDeadlocks(pruning);
  }

  /**
   * Returns whether deadlock states are pruned.
   * @see #setPruningDeadlocks(boolean) setPruningDeadlocks()
   */
  public boolean isPruningDeadlocks()
  {
    return mSynchronousProductBuilder.getPruningDeadlocks();
  }

  public int getInternalStateLimit()
  {
    return Math.max(mLowerInternalStateLimit, mUpperInternalStateLimit);
  }

  public void setInternalStateLimit(final int limit)
  {
    mLowerInternalStateLimit = mUpperInternalStateLimit = limit;
  }

  public int getLowerInternalStateLimit()
  {
    return mLowerInternalStateLimit;
  }

  public void setLowerInternalStateLimit(final int limit)
  {
    mLowerInternalStateLimit = limit;
  }

  public int getUpperInternalStateLimit()
  {
    return mUpperInternalStateLimit;
  }

  public void setUpperInternalStateLimit(final int limit)
  {
    mUpperInternalStateLimit = limit;
  }

  public int getMonolithicStateLimit()
  {
    return super.getNodeLimit();
  }

  public void setMonolithicStateLimit(final int limit)
  {
    super.setNodeLimit(limit);
  }

  @Override
  public void setTransitionLimit(final int limit)
  {
    super.setTransitionLimit(limit);
    setInternalTransitionLimit(limit);
  }

  @Override
  public int getTransitionLimit()
  {
    final int limit1 = getInternalTransitionLimit();
    final int limit2 = getMonolithicTransitionLimit();
    return Math.max(limit1, limit2);
  }

  public int getMonolithicTransitionLimit()
  {
    return super.getTransitionLimit();
  }

  public void setMonolithicTransitionLimit(final int limit)
  {
    super.setTransitionLimit(limit);
  }

  public int getInternalTransitionLimit()
  {
    return mInternalTransitionLimit;
  }

  public void setInternalTransitionLimit(final int limit)
  {
    mInternalTransitionLimit = limit;
  }

  /**
   * Sets a file name to dump abstracted models before monolithic
   * verification. If set, any abstracted model will be written to this file
   * before being sent for monolithic verification.
   */
  public void setMonolithicDumpFileName(final String fileName)
  {
    mMonolithicDumpFileName = fileName;
  }

  /**
   * Returns the file name abstracted models are written to.
   * @see #setMonolithicDumpFileName(String) setMonolithicDumpFileName()
   */
  public String getMonolithicDumpFileName()
  {
    return mMonolithicDumpFileName;
  }

  public void setSynchronousProductBuilder
    (final MonolithicSynchronousProductBuilder builder)
  {
    mSynchronousProductBuilder = builder;
  }

  public MonolithicSynchronousProductBuilder getSynchronousProductBuilder()
  {
    return mSynchronousProductBuilder;
  }


  //#########################################################################
  //# Interface net.sourceforge.waters.model.analysis.ModelAnalyser
  @Override
  public boolean supportsNondeterminism()
  {
    return mAbstractionProcedureCreator.supportsNondeterminism();
  }

  @Override
  public int getNodeLimit()
  {
    final int limit1 = getInternalStateLimit();
    final int limit2 = getMonolithicStateLimit();
    return Math.max(limit1, limit2);
  }

  @Override
  public void setNodeLimit(final int limit)
  {
    setInternalStateLimit(limit);
    setMonolithicStateLimit(limit);
  }

  public void setMonolithicAnalyzer(final ModelAnalyzer analyzer)
  {
    mMonolithicAnalyzer = analyzer;
  }

  public ModelAnalyzer getMonolithicAnalyzer()
  {
    return mMonolithicAnalyzer;
  }


  //#########################################################################
  //# Specific Access
  EventProxy getUsedDefaultMarking()
  {
    return mUsedDefaultMarking;
  }

  EventProxy getUsedPreconditionMarking()
  {
    return mUsedPreconditionMarking;
  }

  EventProxy createDefaultMarking()
    throws EventNotFoundException
  {
    if (mConfiguredDefaultMarking == null) {
      final ProductDESProxy model = getModel();
      return AbstractConflictChecker.getMarkingProposition(model);
    } else {
      return mConfiguredDefaultMarking;
    }
  }

  EventProxy createPreconditionMarking()
  {
    if (mConfiguredPreconditionMarking == null) {
      final ProductDESProxyFactory factory = getFactory();
      return factory.createEventProxy(ALPHA, EventKind.PROPOSITION);
    } else {
      return mConfiguredPreconditionMarking;
    }
  }

  void setPropositionsForMarkings(final EventProxy defaultMarking,
                                  final EventProxy preconditionMarking)
  {
    mUsedDefaultMarking = defaultMarking;
    mUsedPreconditionMarking = preconditionMarking;
    if (preconditionMarking == null && defaultMarking == null) {
      mPropositions = Collections.emptyList();
    } else if (preconditionMarking == null) {
      mPropositions = Collections.singletonList(defaultMarking);
    } else {
      final EventProxy[] props = new EventProxy[2];
      props[0] = defaultMarking;
      props[1] = preconditionMarking;
      mPropositions = Arrays.asList(props);
    }
  }

  public void setPropositions(final Collection<EventProxy> props)
  {
    mPropositions = props;
  }

  public Collection<EventProxy> getPropositions()
  {
    return mPropositions;
  }

  protected AbstractionProcedure getAbstractionProcedure()
  {
    return mAbstractionProcedure;
  }

  protected List<AutomatonProxy> getCurrentAutomata()
  {
    return mCurrentAutomata;
  }

  protected Collection<EventProxy> getCurrentEvents()
  {
    return mEventInfoMap.keySet();
  }

  protected Collection<SubSystem> getPostponedSubsystems()
  {
    return mPostponedSubsystems;
  }

  protected Collection<SubSystem> getProcessedSubsystems()
  {
    return mProcessedSubsystems;
  }

  protected int getCurrentInternalStateLimit()
  {
    return mCurrentInternalStateLimit;
  }


  protected void setupSynchronousProductBuilder()
  {
    mSynchronousProductBuilder.setPropositions(mPropositions);
    final KindTranslator translator = getKindTranslator();
    mSynchronousProductBuilder.setKindTranslator(translator);
    final int tlimit = getInternalTransitionLimit();
    mSynchronousProductBuilder.setTransitionLimit(tlimit);
  }

  protected void setCurrentMonolithicAnalyzer(final ModelAnalyzer analyzer)
  {
    mCurrentMonolithicAnalyzer = analyzer;
  }

  protected ModelAnalyzer getCurrentMonolithicAnalyzer()
  {
    return mCurrentMonolithicAnalyzer;
  }

  protected void setupMonolithicAnalyzer()
    throws EventNotFoundException
  {
    if (mCurrentMonolithicAnalyzer != null) {
      final int nlimit = getMonolithicStateLimit();
      mCurrentMonolithicAnalyzer.setNodeLimit(nlimit);
      final int tlimit = getMonolithicTransitionLimit();
      mCurrentMonolithicAnalyzer.setTransitionLimit(tlimit);
      final KindTranslator translator = getKindTranslator();
      mCurrentMonolithicAnalyzer.setKindTranslator(translator);
    }
  }


  //#########################################################################
  //# Interface net.sourceforge.waters.model.analysis.Abortable
  @Override
  public void requestAbort()
  {
    super.requestAbort();
    if (mAbstractionProcedure != null) {
      mAbstractionProcedure.requestAbort();
    }
    if (mSynchronousProductBuilder != null) {
      mSynchronousProductBuilder.requestAbort();
    }
    if (mCurrentMonolithicAnalyzer != null) {
      mCurrentMonolithicAnalyzer.requestAbort();
    }
  }

  @Override
  public void resetAbort()
  {
    super.resetAbort();
    if (mAbstractionProcedure != null) {
      mAbstractionProcedure.resetAbort();
    }
    if (mSynchronousProductBuilder != null) {
      mSynchronousProductBuilder.resetAbort();
    }
    if (mCurrentMonolithicAnalyzer != null) {
      mCurrentMonolithicAnalyzer.resetAbort();
    }
  }


  //#########################################################################
  //# Overrides for net.sourceforge.waters.model.AbstractModelAnalyser
  @Override
  protected void setUp()
    throws AnalysisException
  {
    super.setUp();
    setupMonolithicAnalyzer();
    final AnalysisResult result = getAnalysisResult();
    result.setNumberOfStates(0.0);
    result.setNumberOfTransitions(0.0);
    mAbstractionProcedure =
      mAbstractionProcedureCreator.createAbstractionProcedure(this);
    mAbstractionProcedure.storeStatistics();
    mPreselectingHeuristic = mPreselectingMethod.createHeuristic(this);
    mSelectionHeuristic.setContext(this);
    setupSynchronousProductBuilder();
    mOverflowCandidates = new THashSet<List<AutomatonProxy>>();
    mCurrentInternalStateLimit = mLowerInternalStateLimit;
    initialiseEventsToAutomata();
    mMayBeSplit = true;
  }

  @Override
  protected void tearDown()
  {
    super.tearDown();
    mUsedDefaultMarking = mUsedPreconditionMarking = null;
    mPropositions = null;
    mPreselectingHeuristic = null;
    mCurrentAutomata = null;
    mEventInfoMap = null;
    mDirtyAutomata = null;
    mRedundantEvents = null;
    mPostponedSubsystems = null;
    mProcessedSubsystems = null;
    mUsedEventNames = null;
    mOverflowCandidates = null;
    mCurrentMonolithicAnalyzer = null;
  }

  @Override
  public AnalysisResult createAnalysisResult()
  {
    return new CompositionalAnalysisResult(this);
  }

  @Override
  public CompositionalAnalysisResult getAnalysisResult()
  {
    return (CompositionalAnalysisResult) super.getAnalysisResult();
  }


  //#########################################################################
  //# Hooks
  /**
   * Converts the given specification to a plant.
   * This method is called initially on every specification automaton in the
   * input model to give compositional synthesis the opportunity to convert
   * specifications to plants.
   * @param  spec     Specification to be converted.
   * @return Plantified version of specification. The default implementation
   *         simply returns <CODE>null</CODE> to indicate that specs are
   *         not included in the check.
   */
  protected AutomatonProxy plantify(final AutomatonProxy spec)
    throws OverflowException
  {
    return null;
  }

  /**
   * Performs compositional minimisation of the model.
   * This method should be called as part of the {@link #run()} method of
   * subclasses extending this class. It performs compositional minimisation
   * of the set of automata in the input model and calls
   * {@link #doMonolithicAnalysis(List) doMonolithicAnalysis()} for each
   * subsystem that cannot be minimised further. It stops when one of these
   * calls sets the analysis result to be completed, or when no further
   * minimisation is possible. At this point, the list returned by
   * {@link #getCurrentAutomata()} contains the automata of the minimised
   * system. This method does not take steps to put further data
   * (such as counterexamples) into the analysis result.
   */
  protected void runCompositionalMinimisation()
    throws AnalysisException
  {
    final Logger logger = LogManager.getLogger();
    final AnalysisResult result = getAnalysisResult();
    // If simplify() returns true, the global system has been trivially
    // verified, and there is nothing left to do.
    if (!simplify()) {
      outer:
      do {
        boolean cancheck = true;
        OverflowException lastOverflow = null;
        do {
          List<Candidate> candidates =
            mPreselectingHeuristic.findCandidates();
          while (true) {
            checkAbort();
            /*
            if (logger.isDebugEnabled()) {
              for (final Candidate candidate : candidates) {
                final String info = mSelectionHeuristic.show(candidate);
                logger.debug(candidate + ": " + info);
              }
            }
            */
            final Candidate candidate = selectCandidate(candidates);
            if (candidate == null) {
              break;
            }
            try {
              mMayBeSplit = false;
              applyCandidate(candidate);
              cancheck = true;
            } catch (final OverflowException overflow) {
              recordUnsuccessfulComposition();
              final List<AutomatonProxy> automata = candidate.getAutomata();
              mOverflowCandidates.add(automata);
              candidates.remove(candidate);
              continue;
            }
            if (simplify()) {
              if (result.isFinished()) {
                break outer;
              } else {
                continue outer;
              }
            }
            candidates = mPreselectingHeuristic.findCandidates();
          }
          try {
            if (cancheck && !result.isFinished()) {
              doMonolithicAnalysis(mCurrentAutomata);
              lastOverflow = null;
            }
          } catch (final OutOfMemoryError error) {
            LogManager.getLogger().debug("<out of memory>");
            lastOverflow = new OverflowException(error);
            cancheck = false;
          } catch (final OverflowException overflow) {
            lastOverflow = overflow;
            cancheck = false;
          }
          if (lastOverflow != null) {
            if (mCurrentInternalStateLimit < mUpperInternalStateLimit) {
              mCurrentInternalStateLimit =
                Math.min(2 * mCurrentInternalStateLimit, mUpperInternalStateLimit);
              mOverflowCandidates.clear();
              if (logger.isDebugEnabled()) {
                final String msg =
                  "State limit increased to " + mCurrentInternalStateLimit + ".";
                logger.debug(msg);
              }
            } else {
              throw lastOverflow;
            }
          }
        } while (lastOverflow != null);
      } while (!result.isFinished() && popEventDisjointSubsystem());
    }
    if (!result.isFinished()) {
      final List<AutomatonProxy> empty = Collections.emptyList();
      doMonolithicAnalysis(empty);
    }
    restoreAutomata();
  }

  /**
   * Checks whether the given automata for a possible candidate for
   * composition. This method is called after construction of candidates
   * to check whether or not a given combination of automata may be
   * considered. The default implementation merely checks whether the
   * automata have been composed before resulting in an overflow, to
   * prevent a second attempt. It is recommended for subclasses overriding
   * this method to call the superclass method also.
   * @param  automata  List of automata to form a {@link Candidate}.
   * @return <CODE>true</CODE> if a {@link Candidate} should be formed,
   *         <CODE>false</CODE> otherwise.
   */
  protected boolean isPermissibleCandidate(final List<AutomatonProxy> automata)
  {
    return !mOverflowCandidates.contains(automata);
  }

  /**
   * Creates a product DES for an abstraction step. This hook is
   * invoked before composing the automata of a selected candidate or before
   * running a final monolithic check. It may be overridden by specialised
   * property verifiers that modify the set of automata prior to composition.
   * @param  automata     List of automata for monolithic model.
   * @return A product DES to be passed to a synchronous product builder or
   *         monolithic verifier.
   */
  protected ProductDESProxy createProductDESProxy
    (final List<AutomatonProxy> automata)
  {
    final ProductDESProxyFactory factory = getFactory();
    final String name = Candidate.getCompositionName(automata);
    final String comment =
      "Automatically generated by " + ProxyTools.getShortClassName(this);
    final Collection<EventProxy> events = Candidate.getOrderedEvents(automata);
    return factory.createProductDESProxy(name, comment, null,
                                         events, automata);
  }

  /**
   * Records an abstraction step.
   * This hook is called when an abstraction has been completed with success
   * to allow subclasses to perform necessary bookkeeping. The default
   * implementation does not nothing, as not all model analysers need
   * to record all abstraction steps.
   * @param  step         Detailed information about the abstraction that
   *                      has been performed, including all automata before
   *                      and after the step.
   */
  protected void recordAbstractionStep(final AbstractionStep step)
    throws AnalysisException
  {
  }

  /**
   * Creates an abstraction step representing a synchronous product operation.
   * This hook is called after two or more automata have been composed to
   * prepare an abstraction step, which may later be filed by a call to
   * {@link #recordAbstractionStep(AbstractionStep) recordAbstractionStep()}.
   * @param  automata     The automata that have been composed.
   * @param  sync         The synchronous product automaton that was created.
   * @param  hidden       The local events that have been hidden.
   * @param  tau          The tau event that replaces the local events in the
   *                      synchronous product.
   * @return A step object that can be filed.
   */
  protected HidingStep createSynchronousProductStep
    (final Collection<AutomatonProxy> automata,
     final AutomatonProxy sync,
     final Collection<EventProxy> hidden,
     final EventProxy tau)
  {
    return new HidingStep(this, automata, sync, hidden, tau);
  }

  /**
   * Returns whether failure events are considered in abstraction.
   * Failing Events are events that always lead to a dump state in some
   * automaton. If enabled, this will redirect failure events in other
   * automata to dump states.
   * @return <CODE>false</CODE>. Failure events are disabled by default
   *         but can be enabled by subclasses overriding this method.
   */
  protected boolean isUsingFailingEvents()
  {
    return false;
  }

  /**
   * Removes the given events from the model.
   * This method is called when redundant events have been identified to
   * remove them.
   * @return An abstraction step representing the event removal, or
   *         <CODE>null</CODE> to signal that no events can be removed
   *         after all.
   * @see #removeRedundantEvents()
   */
  protected EventRemovalStep removeEvents(final Set<EventProxy> removed,
                                          final Set<EventProxy> failing)
    throws AnalysisException
  {
    if (removed.isEmpty() && failing.isEmpty()) {
      return null;
    } else {
      showRemovedEvents("Removing events", removed);
      showRemovedEvents("Redirecting failing events", failing);
      final int numAutomata = mCurrentAutomata.size();
      final List<AutomatonProxy> originals =
        new ArrayList<AutomatonProxy>(numAutomata);
      final List<AutomatonProxy> results =
        new ArrayList<AutomatonProxy>(numAutomata);
      final ListIterator<AutomatonProxy> iter =
        mCurrentAutomata.listIterator();
      final Map<StateProxy,StateProxy> stateMap = new HashMap<>();
      while (iter.hasNext()) {
        final AutomatonProxy aut = iter.next();
        final AutomatonProxy newAut = removeEvents(aut, removed, stateMap);
        if (newAut != aut) {
          originals.add(aut);
          results.add(newAut);
          iter.set(newAut);
          // Clean up event info --- first replace automata ...
          for (final EventProxy event : newAut.getEvents()) {
            final EventInfo info = mEventInfoMap.get(event);
            if (info != null) {
              info.replaceAutomaton(aut, newAut);
            }
          }
          // Second, if we have removed a failing event, remove the automaton
          // from the event info ...
          if (isUsingFailingEvents() && mHasRemovedProperTransition) {
            final Set<EventProxy> newEvents = new THashSet<>(newAut.getEvents());
            for (final EventProxy event : aut.getEvents()) {
              final EventInfo info = mEventInfoMap.get(event);
              if (info != null && info.isFailing() &&
                  !newEvents.contains(event)) {
                info.removeAutomaton(aut);
              }
            }
          }
          if (mHasRemovedProperTransition) {
            setAutomatonDirty(newAut);
          }
        }
      }
      final CompositionalAnalysisResult stats = getAnalysisResult();
      final int numRemoved = removed.size();
      stats.addBlockedEvents(numRemoved);
      final int numFailing = failing.size();
      stats.addFailingEvents(numFailing);
      return
        new EventRemovalStep(this, results, originals, stateMap, failing);
    }
  }

  /**
   * Removes events from an automaton. This method is called to remove
   * redundant events from an automaton.
   * @param  aut      An automaton to be simplified.
   * @param  removed  Set of events to be removed.
   * @param  stateMap If states have to be replaced, mappings from old
   *                  to new states are added to this map.
   * @return New automaton representing result of event removal.
   *         May be the same as the input automaton, if no events can be
   *         removed.
   */
  protected AutomatonProxy removeEvents
    (final AutomatonProxy aut, final Set<EventProxy> removed,
     final Map<StateProxy,StateProxy> stateMap)
    throws AnalysisException
  {
    return removeEvents(aut, removed, stateMap, null);
  }

  /**
   * Checks whether the given automata form a trivial subsystem.
   * This hook is called before evaluating heuristics and choosing the
   * next candidate. It checks whether it can be determined directly
   * whether or not the current subsystem subsystem satisfies the property
   * in question, and whether this already leads to a result for the
   * global system.
   * @param  automata     The automata in the current subsystem, which are
   *                      to be checked by this method.
   * @return <CODE>true</CODE> if the subsystem can be determined to satisfy
   *         or not to satisfy the property being checked. If <CODE>true</CODE>
   *         is returned, the verification result is updated to contain the
   *         correct result and a counterexample for the abstracted model,
   *         if the property being checked is not satisfied.
   */
  protected boolean isSubsystemTrivial
    (final Collection<AutomatonProxy> automata)
  throws AnalysisException
  {
    return false;
  }

  /**
   * Uses a primitive (typically monolithic) algorithm to perform analysis
   * of the given automata. This hook is called when a subsystem has been
   * minimised as much as possible, for it to be processed by other means.
   * It may be called more than once if the model being analysed is split
   * into event-disjoint subsystems.
   * @param  automata   List of automata comprising the subsystem to be
   *                    analysed monolithically.
   * @return The Boolean result of analysis. If analysis of the subsystem
   *         leads to a global result for the entire system, the analysis
   *         result should be set to completed and appropriate data should
   *         be stored in it. If the analysis result is not set to completed,
   *         compositional minimisation may resume considering other
   *         subsystems.
   */
  protected abstract boolean doMonolithicAnalysis
    (final List<AutomatonProxy> automata)
    throws AnalysisException;

  protected void reportMonolithicAnalysis(final ProductDESProxy des)
  {
    final Logger logger = LogManager.getLogger();
    if (logger.isDebugEnabled()) {
      final Collection<AutomatonProxy> automata1 = des.getAutomata();
      double estimate = 1.0;
      for (final AutomatonProxy aut : automata1) {
        estimate *= aut.getStates().size();
      }
      logger.debug("Monolithically composing " + automata1.size() +
                   " automata, estimated " + estimate + " states.");
    }
    if (mMonolithicDumpFileName != null) {
      MarshallingTools.saveProductDESorModule(des, mMonolithicDumpFileName);
    }
  }


  //#########################################################################
  //# Events+Automata Maps
  /**
   * Maps the events in the model to a set of the automata that contain the
   * event in their alphabet.
   */
  protected void initialiseEventsToAutomata()
    throws AnalysisException
  {
    final ProductDESProxy model = getModel();
    final Collection<AutomatonProxy> automata = model.getAutomata();
    final int numAutomata = automata.size();
    final KindTranslator translator = getKindTranslator();
    mCurrentAutomata = new ArrayList<>(numAutomata);
    final int numEvents = model.getEvents().size();
    mEventInfoMap = new HashMap<>(numEvents);
    mDirtyAutomata = new LinkedList<>();
    for (AutomatonProxy aut : automata) {
      final ComponentKind kind = translator.getComponentKind(aut);
      if (kind != null) {
        switch (kind) {
        case SPEC:
          aut = plantify(aut);
          if (aut == null) {
            break;
          }
          // fall through ...
        case PLANT:
          if (!supportsNondeterminism()) {
            AutomatonTools.checkDeterministic(aut);
          }
          mCurrentAutomata.add(aut);
          addEventsToAutomata(aut);
          mDirtyAutomata.add(aut);
          break;
        default:
          break;
        }
      }
    }
    final AnalysisResult result = getAnalysisResult();
    result.setNumberOfAutomata(mCurrentAutomata.size());
    mUsedEventNames = new THashSet<String>(numEvents + numAutomata);
    for (final EventProxy event : mEventInfoMap.keySet()) {
      final String name = event.getName();
      mUsedEventNames.add(name);
    }
    mRedundantEvents = new LinkedList<EventProxy>();
    for (final EventInfo info : mEventInfoMap.values()) {
      if (info.isRemovable(mSelfloopOnlyEventsEnabled) || info.isFailing()) {
        final EventProxy event = info.getEvent();
        mRedundantEvents.add(event);
      }
    }
    mPostponedSubsystems = new PriorityQueue<SubSystem>();
    mProcessedSubsystems = new LinkedList<SubSystem>();
  }

  protected void addEventsToAllAutomata()
    throws AnalysisException
  {
    for (final AutomatonProxy aut : mCurrentAutomata) {
      addEventsToAutomata(aut);
    }
  }

  /**
   * Updates events and automata data structure after successful application
   * of a candidate.
   * @param candidate  The candidate that has been applied.
   * @param steps      The abstraction steps used to simplify the candidate.
   * @param aut        The simplified automaton replacing the automata
   *                   in the candidate.
   */
  protected void recordCandidateApplication(final Candidate candidate,
                                            final List<AbstractionStep> steps,
                                            final AutomatonProxy aut)
    throws AnalysisException
  {
    updateEventsToAutomata(aut, candidate.getAutomata());
    for (final AbstractionStep step : steps) {
      // Must be done after updateEventsToAutomata() ...
      recordAbstractionStep(step);
    }
  }

  protected void updateEventsToAutomata
    (final AutomatonProxy autToAdd,
     final List<AutomatonProxy> autToRemove)
    throws AnalysisException
  {
    mCurrentAutomata.removeAll(autToRemove);
    mCurrentAutomata.add(autToAdd);
    addEventsToAutomata(autToAdd);
    removeEventsToAutomata(autToRemove);
  }

  protected void replaceDirtyAutomaton(final AutomatonProxy newAut,
                                       final AutomatonProxy oldAut)
  {
    if (mDirtyAutomata.remove(oldAut)) {
      mDirtyAutomata.add(newAut);
    }
  }

  protected void setAutomatonDirty(final AutomatonProxy aut)
  {
    if (!mDirtyAutomata.contains(aut)) {
      mDirtyAutomata.add(aut);
    }
  }

  /**
   * Creates an event information record for the given event.
   * @see EventInfo
   */
  protected EventInfo createEventInfo(final EventProxy event)
  {
    return new EventInfo(event);
  }

  protected EventInfo getEventInfo(final EventProxy event)
  {
    return mEventInfoMap.get(event);
  }

  protected void putEventInfo(final EventProxy event, final EventInfo info)
  {
    mEventInfoMap.put(event, info);
  }

  protected void addEventsToAutomata(final AutomatonProxy aut)
    throws OverflowException
  {
    final Collection<EventProxy> events = aut.getEvents();
    Set<StateProxy> nonDumpStates = null;
    if (isUsingFailingEvents()) {
      final EventProxy omega = getUsedDefaultMarking();
      if (omega != null && events.contains(omega)) {
        //Find the nondump states
        nonDumpStates = new THashSet<>();
        for (final TransitionProxy trans : aut.getTransitions()) {
          nonDumpStates.add(trans.getSource());
        }
        for (final StateProxy state : aut.getStates()) {
          if (state.getPropositions().contains(omega)) {
            nonDumpStates.add(state);
          }
        }
      }
    }
    final int numEvents = events.size();
    final TObjectByteHashMap<EventProxy> statusMap =
      new TObjectByteHashMap<>(numEvents);
    if (mBlockedEventsEnabled || isUsingFailingEvents() ||
        mSelfloopOnlyEventsEnabled) {
      for (final TransitionProxy trans : aut.getTransitions()) {
        final EventProxy event = trans.getEvent();
        final byte status = statusMap.get(event);
        if (trans.getSource() == trans.getTarget()) {
          if (status == UNKNOWN_SELFLOOP) {
            statusMap.put(event, ONLY_SELFLOOP);
          } else if (status == FAILING) {
            statusMap.put(event, NOT_ONLY_SELFLOOP);
          }
        } else if (nonDumpStates != null &&
                   !nonDumpStates.contains(trans.getTarget())) {
          if (status == UNKNOWN_SELFLOOP) {
            statusMap.put(event, FAILING);
          } else if (status == ONLY_SELFLOOP) {
            statusMap.put(event, NOT_ONLY_SELFLOOP);
          }
        } else {
          statusMap.put(event, NOT_ONLY_SELFLOOP);
        }
      }
    }
    final KindTranslator translator = getKindTranslator();
    for (final EventProxy event : events) {
      if (translator.getEventKind(event) != EventKind.PROPOSITION) {
        EventInfo info = mEventInfoMap.get(event);
        if (info == null) {
          info = createEventInfo(event);
          mEventInfoMap.put(event, info);
        }
        byte status = statusMap.get(event);
        if (status == UNKNOWN_SELFLOOP) {
          status = mBlockedEventsEnabled ? BLOCKED : NOT_ONLY_SELFLOOP;
        }
        info.addAutomaton(aut, status);
      }
    }
  }

  /**
   * Removes the given automata from the current subsystem data structures.
   * This method is called after a candidate has been composed and simplified.
   * It removes entries from the event information map {@link #mEventInfoMap}),
   * and tests for events that become redundant. Events are redundant if
   * they are known to be globally disabled, or if they only ever appear as
   * selfloops, or if they only appear on transitions to deadlock states.
   * Such events are added to the list {@link #mRedundantEvents}.
   * @param victims
   *          Collection of automata to be removed.
   * @see #removeRedundantEvents()
   */
  protected void removeEventsToAutomata
    (final Collection<AutomatonProxy> victims)
  {
    mRedundantEvents.clear();
    final Iterator<Map.Entry<EventProxy,EventInfo>> iter =
      mEventInfoMap.entrySet().iterator();
    while (iter.hasNext()) {
      final Map.Entry<EventProxy,EventInfo> entry = iter.next();
      final EventInfo info = entry.getValue();
      info.removeAutomata(victims);
      if (info.isEmpty()) {
        iter.remove();
      } else if (info.isRemovable(mSelfloopOnlyEventsEnabled) || info.isFailing()) {
        final EventProxy event = entry.getKey();
        mRedundantEvents.add(event);
      }
    }
  }


  //#######################################################################
  //# Logging
  protected void showDebugLog(final ListBufferTransitionRelation rel)
  {
    final Logger logger = LogManager.getLogger();
    if (logger.isDebugEnabled()) {
      logger.debug("Simplifying " + rel.getName() + " ...");
      logger.debug(rel.getNumberOfReachableStates() + " states, " +
                   rel.getNumberOfTransitions() + " transitions, " +
                   rel.getNumberOfMarkings(false) + " markings.");
    }
  }

  private void showRemovedEvents(final String msg,
                                 final Set<EventProxy> removed)
  {
    final Logger logger = LogManager.getLogger();
    if (logger.isDebugEnabled() && !removed.isEmpty()) {
      final StringBuilder builder = new StringBuilder();
      final List<EventProxy> ordered = new ArrayList<>(removed);
      Collections.sort(ordered);
      boolean first = true;
      for (final EventProxy event : ordered) {
        if (first) {
          builder.append(msg);
          builder.append(':');
          first = false;
        } else {
          builder.append(',');
        }
        builder.append(' ');
        builder.append(event.getName());
      }
      logger.debug(builder);
    }
  }


  //#######################################################################
  //# Private Methods
  /**
   * Finds the set of events that are local to the given automata.
   */
  Set<EventProxy> identifyLocalEvents
    (final Collection<AutomatonProxy> automata)
  {
    final Set<EventProxy> events = Candidate.getAllEvents(automata);
    final Iterator<EventProxy> iter = events.iterator();
    while (iter.hasNext()) {
      final EventProxy event = iter.next();
      final EventInfo info = mEventInfoMap.get(event);
      if (info == null || !info.isLocal(automata)) {
        iter.remove();
      }
    }
    return events;
  }

  /**
   * Removes events that have been found to be redundant from the current
   * subsystem. This method removes any events contained in the list
   * {@link #mRedundantEvents} from all automata and records the abstraction
   * as an {@link EventRemovalStep}.
   * @return <CODE>true</CODE> if the model was modified, <CODE>false</CODE>
   *         otherwise.
   * @see #removeEventsToAutomata(Collection) removeEventsToAutomata()
   */
  private boolean removeRedundantEvents()
    throws AnalysisException
  {
    final int numRedundant = mRedundantEvents.size();
    final Set<EventProxy> removed = new THashSet<>(numRedundant);
    final Set<EventProxy> failing;
    if (isUsingFailingEvents()) {
      failing = new THashSet<>(numRedundant);
    } else {
      failing = Collections.emptySet();
    }
    for (final EventProxy event : mRedundantEvents) {
      final EventInfo info = getEventInfo(event);
      if (info.isFailing()) {
        failing.add(event);
      } else {
        removed.add(event);
      }
    }
    final AbstractionStep step = removeEvents(removed, failing);
    if (step != null) {
      recordAbstractionStep(step);
      for (final EventProxy event : removed) {
        final EventInfo info = mEventInfoMap.remove(event);
        mMayBeSplit |= info.getNumberOfAutomata() > 1;
      }
      for (final EventProxy event : failing) {
        final EventInfo info = getEventInfo(event);
        info.setReduced();
      }
      mRedundantEvents.clear();
      mMayBeSplit = true; // TODO bug ???
      return true;
    } else {
      return false;
    }
  }

  /**
   * Removes events from an automaton. This method is called to remove
   * redundant events from an automaton.
   * @param  aut       An automaton to be simplified.
   * @param  removed   Set of events to be removed.
   * @param  stateMap  If states have to be replaced, mappings from old
   *                   to new states are added to this map.
   * @param  dumpState Dump state to be used for failing events,
   *                   or <CODE>null</CODE>.
   * @return New automaton representing result of event removal.
   *         May be the same as the input automaton, if no events can be
   *         removed.
   */
  private AutomatonProxy removeEvents
    (final AutomatonProxy aut, Set<EventProxy> removed,
     final Map<StateProxy,StateProxy> stateMap, StateProxy dumpState)
    throws AnalysisException
  {
    checkAbort();
    mHasRemovedProperTransition = false;
    final Collection<EventProxy> events = aut.getEvents();
    boolean found = false;
    for (final EventProxy event : events) {
      final EventInfo info = getEventInfo(event);
      if (info != null && (removed.contains(event) || info.isFailing())) {
        found = true;
        break;
      }
    }
    if (!found) {
      // If the automaton has no removed or failing events, return it unchanged.
      return aut;
    }

    // Special case: if the automaton has two states, one of which is a dump
    // state, then treat failing events as removed if they appear in another
    // automaton.
    // TODO Use always enabled events for a more effective test.
    final Collection<StateProxy> states = aut.getStates();
    final Collection<TransitionProxy> transitions = aut.getTransitions();
    final EventProxy defaultMarking = getUsedDefaultMarking();
    final boolean usesMarking = events.contains(defaultMarking);
    if (isUsingFailingEvents() && states.size() == 2 &&
        usesMarking && dumpState == null) {
      StateProxy goodState = null;
      for (final StateProxy state : states) {
        if (state.isInitial() &&
            state.getPropositions().contains(defaultMarking) &&
            goodState == null) {
          goodState = state;
        } else if (!state.isInitial() &&
                   !state.getPropositions().contains(defaultMarking) &&
                   dumpState == null) {
          dumpState = state;
        }
      }
      if (goodState != null && dumpState != null) {
        for (final TransitionProxy trans : transitions) {
          if (trans.getSource() == dumpState) {
            dumpState = null;
            break;
          }
        }
        if (dumpState != null) {
          final Set<EventProxy> extraRemoved = new THashSet<>(transitions.size());
          for (final EventProxy event : events) {
            final EventInfo info = getEventInfo(event);
            if (info != null && info.isFailing() &&
                info.getNumberOfAutomata() > 1) {
              extraRemoved.add(event);
            }
          }
          if (!extraRemoved.isEmpty()) {
            extraRemoved.addAll(removed);
            removed = extraRemoved;
          }
        }
      }
    }

    // OK. We are removing/redirecting something ...
    // Make new event alphabet ...
    final ProductDESProxyFactory factory = getFactory();
    final int numEvents = events.size();
    final Collection<EventProxy> newEvents =
      new ArrayList<EventProxy>(numEvents - 1);
    for (final EventProxy event : events) {
      final EventInfo info = getEventInfo(event);
      if (info == null) {
        newEvents.add(event); // keep propositions
      } else if (!removed.contains(event)) {
        newEvents.add(event); // but do not keep removed events
      }
    }

    // Make new transitions (and states if necessary) ...
    Collection<StateProxy> newStates = states;
    final int numTrans = transitions.size();
    final Collection<TransitionProxy> newTransitions =
      new ArrayList<TransitionProxy>(numTrans);
    for (final TransitionProxy trans : transitions) {
      final EventProxy event = trans.getEvent();
      if (removed.contains(event)) { // Suppress removed event
        if (trans.getSource() != trans.getTarget()) {
          // Removing a non-selfloop blocked event can block other events
          mHasRemovedProperTransition = true;
        }
      } else if (getEventInfo(event).isFailing()) { // Redirect failing event
        if (dumpState == null) {
          // Find or create dump state ...
          if (usesMarking) {
            final Set<StateProxy> nonDumpStates = new THashSet<>(states.size());
            for (final StateProxy state : states) {
              // If the state is marked it is not a dump state
              if (state.getPropositions().contains(defaultMarking)) {
                nonDumpStates.add(state);
              }
            }
            for (final TransitionProxy tr : transitions) {
              // If the state has an outgoing transition it is not a dump state
              nonDumpStates.add(tr.getSource());
            }
            if (nonDumpStates.size() == states.size()) {
              dumpState = factory.createStateProxy(":dump");
              newStates = new ArrayList<>(states.size() +1);
              newStates.addAll(states);
              newStates.add(dumpState);
            } else {
              for (final StateProxy state : states) {
                if (!nonDumpStates.contains(state)) {
                  dumpState = state;
                  break;
                }
              }
            }
          } else {
            // The marking proposition is not in the alphabet ...
            dumpState = factory.createStateProxy(":dump");
            final AutomatonProxy copy =
              markStatesAndAddDump(aut, dumpState, stateMap);
            return removeEvents(copy, removed, stateMap, dumpState);
          }
        }
        if (trans.getTarget() == dumpState) {
          // If the transition already goes to the dump state, keep it
          newTransitions.add(trans);
        } else {
          // Otherwise redirect the transition to the dump state
          final TransitionProxy newTrans =
            factory.createTransitionProxy(trans.getSource(), event, dumpState);
          newTransitions.add(newTrans);
          // Redirecting a transition to dump can block other events
          mHasRemovedProperTransition = true;
        }
      } else { // Otherwise keep the transition
        newTransitions.add(trans);
      }
    }

    final String name = aut.getName();
    final ComponentKind kind = aut.getKind();
    final AutomatonProxy newAut = factory.createAutomatonProxy  //Create a new automaton with the new events and transitions
      (name, kind, newEvents, newStates, newTransitions);
    reportEventRemoval(newAut);
    return newAut;
  }

  private AutomatonProxy markStatesAndAddDump
    (final AutomatonProxy aut, final StateProxy dump,
     final Map<StateProxy,StateProxy> stateMap)
  {
    final ProductDESProxyFactory factory = getFactory();
    final EventProxy defaultMarking = getUsedDefaultMarking();
    final Collection<EventProxy> events = aut.getEvents();
    final Collection<EventProxy> newEvents = new ArrayList<>(events.size() + 1);
    newEvents.addAll(events);
    newEvents.add(defaultMarking);
    final Collection<StateProxy> states = aut.getStates();
    final Collection<StateProxy> newStates = new ArrayList<>(states.size() + 1);
    for (final StateProxy state : states) {
      final String name = state.getName();
      final boolean init = state.isInitial();
      final Collection<EventProxy> props = state.getPropositions();
      final Collection<EventProxy> newProps;
      if (props.isEmpty()) {
        newProps = Collections.singletonList(defaultMarking);
      } else {
        newProps = new ArrayList<>(props.size() + 1);
        newProps.addAll(props);
        newProps.add(defaultMarking);
      }
      final StateProxy newState =
        factory.createStateProxy(name, init, newProps);
      newStates.add(newState);
      stateMap.put(state, newState);
    }
    newStates.add(dump);
    final Collection<TransitionProxy> transitions = aut.getTransitions();
    final Collection<TransitionProxy> newTransitions =
      new ArrayList<>(transitions.size());
    for (final TransitionProxy trans : transitions) {
      final StateProxy source = trans.getSource();
      final StateProxy newSource = stateMap.get(source);
      final EventProxy event = trans.getEvent();
      final StateProxy target = trans.getTarget();
      final StateProxy newTarget = stateMap.get(target);
      final TransitionProxy newTrans =
        factory.createTransitionProxy(newSource, event, newTarget);
      newTransitions.add(newTrans);
    }
    final String name = aut.getName();
    final ComponentKind kind = aut.getKind();
    return factory.createAutomatonProxy
      (name, kind, newEvents, newStates, newTransitions);
  }

  /**
   * Checks whether the current subsystem can be split into event-disjoint
   * components, and if so, performs the split by replacing the current
   * subsystem by its smallest component and adding any split off subsystems
   * to the list {@link #mPostponedSubsystems}.
   */
  private boolean findEventDisjointSubsystems()
    throws AnalysisException
  {
    if (!mMayBeSplit || mEventInfoMap.isEmpty()) {
      return false;
    }
    final CompositionalAnalysisResult result = getAnalysisResult();
    final long start = System.currentTimeMillis();
    final KindTranslator translator = getKindTranslator();
    final int numEvents = getCurrentEvents().size();
    final Collection<AutomatonProxy> remainingAutomata =
      new THashSet<AutomatonProxy>(mCurrentAutomata);
    final List<SubSystem> tasks = new LinkedList<SubSystem>();
    while (!remainingAutomata.isEmpty()) {
      AutomatonProxy aut = remainingAutomata.iterator().next();
      remainingAutomata.remove(aut);
      final int numRemainingAutomata = remainingAutomata.size();
      final List<AutomatonProxy> subsystemAutomata =
        new ArrayList<AutomatonProxy>(numRemainingAutomata);
      subsystemAutomata.add(aut);
      final Set<EventProxy> subsystemEvents =
        new THashSet<EventProxy>(numEvents);
      for (int i = 0; i < subsystemAutomata.size(); i++) {
        aut = subsystemAutomata.get(i);
        for (final EventProxy event : aut.getEvents()) {
          checkAbort();
          if (translator.getEventKind(event) != EventKind.PROPOSITION &&
              subsystemEvents.add(event) && !remainingAutomata.isEmpty()) {
            final EventInfo info = mEventInfoMap.get(event);
            final TObjectByteIterator<AutomatonProxy> iter =
              info.getAutomataIterator();
            while (iter.hasNext()) {
              iter.advance();
              final AutomatonProxy next = iter.key();
              if (remainingAutomata.remove(next)) {
                if (remainingAutomata.isEmpty() && tasks.isEmpty()) {
                  final long stop = System.currentTimeMillis();
                  result.addSplitAttempt(false, stop - start);
                  return false;
                } else {
                  subsystemAutomata.add(next);
                }
              }
            }
          }
        }
      }
      Collections.sort(subsystemAutomata);
      final List<EventProxy> subsystemEventsList =
        new ArrayList<EventProxy>(subsystemEvents);
      Collections.sort(subsystemEventsList);
      final SubSystem task = new SubSystem(subsystemEventsList,
                                           subsystemAutomata,
                                           mCurrentInternalStateLimit);
      tasks.add(task);
    }
    final Iterator<SubSystem> iter = tasks.iterator();
    SubSystem task0 = iter.next();
    while (iter.hasNext()) {
      final SubSystem task = iter.next();
      if (task0.compareTo(task) < 0) {
        mPostponedSubsystems.add(task);
      } else {
        mPostponedSubsystems.add(task0);
        task0 = task;
      }
    }
    loadSubSystem(task0);
    final long stop = System.currentTimeMillis();
    result.addSplitAttempt(true, stop - start);
    mMayBeSplit = false;
    return true;
  }

  private boolean popEventDisjointSubsystem()
    throws AnalysisException
  {
    final SubSystem next = mPostponedSubsystems.poll();
    if (next == null) {
      return false;
    } else {
      final List<EventProxy> events =
        new ArrayList<EventProxy>(mEventInfoMap.keySet());
      Collections.sort(events);
      final SubSystem current =
        new SubSystem(events, mCurrentAutomata, mCurrentInternalStateLimit);
      mProcessedSubsystems.add(current);
      loadSubSystem(next);
      return true;
    }
  }

  private void loadSubSystem(final SubSystem task)
    throws AnalysisException
  {
    mCurrentAutomata = task.getAutomata();
    mCurrentInternalStateLimit = task.getStateLimit();
    mEventInfoMap.clear();
    addEventsToAllAutomata();
  }

  private void restoreAutomata()
  {
    for (final SubSystem task : mProcessedSubsystems) {
      final Collection<AutomatonProxy> automata = task.getAutomata();
      mCurrentAutomata.addAll(automata);
    }
    for (final SubSystem task : mPostponedSubsystems) {
      final Collection<AutomatonProxy> automata = task.getAutomata();
      mCurrentAutomata.addAll(automata);
    }
  }


  //#########################################################################
  //# Candidate Selection
  /**
   * Performs the second step of candidate selection.
   * @param  preselected  List of preselected candidates from step&nbsp;1.
   * @return Preferred candidate from the given list, taking subsumption
   *         into account, or <CODE>null</CODE> if no suitable candidate
   *         could be found within the state limits.
   */
  private Candidate selectCandidate(final List<Candidate> preselected)
    throws AnalysisException
  {
    if (preselected.isEmpty()) {
      return null;
    } else {
      final Candidate result = mSelectionHeuristic.select(preselected);
      if (mSubsumptionEnabled) {
        final List<Candidate> subsumedBy = new LinkedList<Candidate>();
        for (final Candidate candidate : preselected) {
          if (candidate.subsumes(result)) {
            subsumedBy.add(candidate);
          }
        }
        if (!subsumedBy.isEmpty()) {
          return selectCandidate(subsumedBy);
        }
      }
      return result;
    }
  }


  //#########################################################################
  //# Abstraction Steps
  /**
   * Attempts to simplify the current subsystem without composing automata.
   * This method attempts to simplify automata individually and to remove
   * redundant events. If events are removed, it also checks whether the
   * current subsystem can be split into event-disjoint components, and if
   * so, performs the split and replaces the current subsystem by its
   * smallest component.
   * @return <CODE>true</CODE> if the subsystem has been found to be
   *         trivially blocking or nonblocking. If <CODE>true</CODE>
   *         is returned and the result for the global property is known,
   *         the verification result is updated to contain the correct result
   *         and a counterexample for the abstracted model if appropriate.
   * @see #isSubsystemTrivial(Collection) isSubsystemTrivial()
   * @see #simplifyDirtyAutomata()
   * @see #removeRedundantEvents()
   * @see #findEventDisjointSubsystems()
   */
  private boolean simplify()
    throws AnalysisException
  {
    if (isSubsystemTrivial(mCurrentAutomata)) {
      return true;
    }
    final boolean change1 = simplifyDirtyAutomata();
    final boolean change2 = removeRedundantEvents();
    boolean change = change1 || change2;
    while (change) {
      if (isSubsystemTrivial(mCurrentAutomata)) {
        return true;
      }
      change = simplifyDirtyAutomata() && removeRedundantEvents();
    }
    findEventDisjointSubsystems();
    return false;
  }

  /**
   * Simplifies any automata that have been marked as <I>dirty</I>.
   * This method checks all automata in the list {@link #mDirtyAutomata}
   * and applies the current abstraction rule to each of them.
   * @return <CODE>true</CODE> if some automaton was changed by abstraction,
   *         <CODE>false</CODE> otherwise.
   * @see #mDirtyAutomata
   */
  private boolean simplifyDirtyAutomata()
    throws AnalysisException
  {
    final AnalysisResult analysisResult = getAnalysisResult();
    boolean result = false;
    while (!mDirtyAutomata.isEmpty() && !analysisResult.isFinished()) {
      final AutomatonProxy aut = mDirtyAutomata.remove();
      final Collection<EventProxy> events = aut.getEvents();
      final int numEvents = events.size();
      final Set<EventProxy> local = new THashSet<EventProxy>(numEvents);
      for (final EventProxy event : events) {
        final EventInfo info = mEventInfoMap.get(event);
        if (info != null && info.getNumberOfAutomata() == 1) {
          local.add(event);
        }
      }
      final List<AutomatonProxy> singleton = Collections.singletonList(aut);
      final Candidate candidate = new Candidate(singleton, local);
      try {
        result |= applyCandidate(candidate);
      } catch (final OverflowException exception) {
        // TODO Add to mOverflowCandidates? What about local events changing?
        recordUnsuccessfulComposition();
      }
    }
    return result;
  }

  /**
   * Applies the current abstraction rule to the given candidate.
   * @param  candidate   The candidate representing a set of automata to
   *                     be composed and simplified.
   * @return <CODE>true</CODE> if the current subsystem has been changed
   *         by abstraction, <CODE>false</CODE> otherwise.
   */
  private boolean applyCandidate(final Candidate candidate)
    throws AnalysisException
  {
    // assert mCurrentAutomata.containsAll(candidate.getAutomata());
    final List<AbstractionStep> steps = new LinkedList<AbstractionStep>();
    AutomatonProxy aut;
    try {
      final HidingStep syncStep = composeSynchronousProduct(candidate);
      final Collection<EventProxy> local = candidate.getLocalEvents();
      final int numLocal = local.size();
      // Local events not replaced by tau, for synthesis abstraction.
      final Collection<EventProxy> notReallyHidden =
        new ArrayList<EventProxy>(numLocal);
      for (final EventProxy event : local) {
        final EventInfo info = mEventInfoMap.get(event);
        if (info.canBeLocal() && !info.canBeTau()) {
          notReallyHidden.add(event);
        }
      }
      final EventProxy tau;
      if (syncStep == null) {
        aut = candidate.getAutomata().iterator().next();
        tau = null;
      } else {
        steps.add(syncStep);
        aut = syncStep.getResultAutomaton();
        tau = syncStep.getTauEvent();
        if (tau != null) {
          notReallyHidden.add(tau);
        }
      }
      recordStatistics(aut);
      final boolean simplified =
        mAbstractionProcedure.run(aut, notReallyHidden, steps, candidate);
      if (simplified) {
        final Collection<EventProxy> oldEvents = aut.getEvents();
        final int end = steps.size();
        final AbstractionStep last = steps.listIterator(end).previous();
        aut = last.getResultAutomaton();
        final KindTranslator translator = getKindTranslator();
        final Collection<EventProxy> newEvents =
          new THashSet<EventProxy>(aut.getEvents());
        for (final EventProxy event : oldEvents) {
          if (event != tau &&
              translator.getEventKind(event) != EventKind.PROPOSITION &&
              !local.contains(event) && !newEvents.contains(event)) {
            mMayBeSplit = true;
            break;
          }
        }
      }
    } catch (final OutOfMemoryError error) {
      System.gc();
      LogManager.getLogger().debug("<out of memory>");
      throw new OverflowException(error);
    }
    if (steps.isEmpty()) {
      return false;
    } else {
      // Beware of out-of-memory during updateEventsToAutomata() or
      // recordAbstractionStep() --- it is not recoverable ...
      recordCandidateApplication(candidate, steps, aut);
      return true;
    }
  }

  /**
   * Builds the synchronous product for a given candidate.
   */
  private HidingStep composeSynchronousProduct(final Candidate candidate)
    throws AnalysisException
  {
    final List<AutomatonProxy> automata0 = candidate.getAutomata();
    final Collection<EventProxy> local = candidate.getLocalEvents();
    final int numLocal = local.size();
    final Collection<EventProxy> hidden = new ArrayList<EventProxy>(numLocal);
    for (final EventProxy event : local) {
      final EventInfo info = mEventInfoMap.get(event);
      if (info.canBeTau()) {
        hidden.add(event);
      }
    }
    final EventProxy tau;
    if (hidden.isEmpty()) {
      tau = null;
    } else {
      final ProductDESProxyFactory factory = getFactory();
      tau = createSilentEvent(automata0, factory);
    }
    final ProductDESProxy des = createProductDESProxy(automata0);
    final Collection<AutomatonProxy> automata1 = des.getAutomata();
    if (automata1.size() > 1) {
      return composeSeveralAutomata(des, hidden, tau);
    } else {
      final AutomatonProxy aut = automata1.iterator().next();
      return composeOneAutomaton(aut, hidden, tau);
    }
  }

  private HidingStep composeOneAutomaton(final AutomatonProxy aut,
                                         final Collection<EventProxy> hidden,
                                         final EventProxy tau)
    throws OverflowException
  {
    final KindTranslator translator = getKindTranslator();
    final EventEncoding eventEnc = new EventEncoding();
    if (tau != null) {
      eventEnc.addSilentEvent(tau);
    }
    for (final EventProxy event : aut.getEvents()) {
      if (hidden.contains(event)) {
        eventEnc.addSilentEvent(event);
      } else if (translator.getEventKind(event) != EventKind.PROPOSITION ||
                 (mPropositions != null && mPropositions.contains(event))) {
        eventEnc.addEvent(event, translator, EventStatus.STATUS_NONE);
      }
    }
    final StateEncoding stateEnc = new StateEncoding(aut);
    final ListBufferTransitionRelation rel = new ListBufferTransitionRelation
      (aut, eventEnc, stateEnc,
       ListBufferTransitionRelation.CONFIG_SUCCESSORS);
    final boolean change1 = rel.checkReachability();
    final boolean change2 = rel.removeTauSelfLoops();
    final boolean change3 = removeProperSelfLoopEvents(eventEnc, rel);
    final boolean change4 = rel.removeRedundantPropositions();
    final EventProxy trueTau = change2 ? null : tau;
    mMayBeSplit |= change3;
    final ProductDESProxyFactory factory = getFactory();
    if (change4) {
      final StateEncoding newStateEnc = new StateEncoding();
      final AutomatonProxy abstracted =
        rel.createAutomaton(factory, eventEnc, newStateEnc);
      final SynchronousProductStateMap stateMap =
        new OneAutomatonStateMap(aut, stateEnc, newStateEnc);
      return new HidingStep(this, abstracted, hidden, trueTau, stateMap);
    } else if (tau != null || change1 || change2 || change3) {
      final AutomatonProxy abstracted =
        rel.createAutomaton(factory, eventEnc, stateEnc);
      return new HidingStep(this, abstracted, aut, hidden, trueTau);
    } else {
      return null;
    }
  }

  private HidingStep composeSeveralAutomata
    (final ProductDESProxy des,
     final Collection<EventProxy> hidden,
     final EventProxy tau)
    throws AnalysisException
  {
    mSynchronousProductBuilder.setModel(des);
    final Collection<EventProxy> events = des.getEvents();
    int expectedNumberOfEvents = events.size() - hidden.size();
    if (tau != null) {
      mSynchronousProductBuilder.addMask(hidden, tau);
      expectedNumberOfEvents++;
    }
    mSynchronousProductBuilder.setDetailedOutputEnabled(true);
    mSynchronousProductBuilder.setNodeLimit(mCurrentInternalStateLimit);
    mSynchronousProductBuilder.setTransitionLimit(mInternalTransitionLimit);
    mSynchronousProductBuilder.setStateCallback(null);
    mSynchronousProductBuilder.setPropositions(null);
    try {
      mSynchronousProductBuilder.run();
      final AutomatonProxy sync =
        mSynchronousProductBuilder.getComputedAutomaton();
      mMayBeSplit |= sync.getEvents().size() < expectedNumberOfEvents;
      final Collection<AutomatonProxy> automata = des.getAutomata();
      return createSynchronousProductStep(automata, sync, hidden, tau);
    } finally {
      final CompositionalAnalysisResult stats = getAnalysisResult();
      final SynchronousProductResult result =
        mSynchronousProductBuilder.getAnalysisResult();
      stats.addSynchronousProductAnalysisResult(result);
      mSynchronousProductBuilder.clearMask();
    }
  }

  /**
   * Removes events that are only selfloops from a transition relation,
   * possibly taking into account deadlock states.
   */
  private boolean removeProperSelfLoopEvents(final EventEncoding enc,
                                             final ListBufferTransitionRelation rel)
  {
    final EventProxy defaultMarking = getUsedDefaultMarking();
    if (defaultMarking == null) {
      return rel.removeProperSelfLoopEvents();
    } else if (getConfiguredPreconditionMarking() != null) {
      return rel.removeProperSelfLoopEvents();
    }
    final int defaultMarkingID = enc.getEventCode(defaultMarking);
    if (defaultMarkingID < 0) {
      return rel.removeProperSelfLoopEvents();
    } else {
      return rel.removeProperSelfLoopEvents(defaultMarkingID);
    }
  }

  /**
   * Creates a silent event for hiding within the given automata.
   * @return A new event named according to the candidate's automata.
   */
  private EventProxy createSilentEvent(final List<AutomatonProxy> automata,
                                       final ProductDESProxyFactory factory)
  {
    String name = Candidate.getCompositionName("tau:", automata);
    int prefix = 0;
    while (!mUsedEventNames.add(name)) {
      prefix++;
      name = Candidate.getCompositionName("tau" + prefix + ":", automata);
    }
    return factory.createEventProxy(name, EventKind.UNCONTROLLABLE, false);
  }


  //#########################################################################
  //# Statistics
  void recordStatistics(final AutomatonProxy aut)
  {
    final CompositionalAnalysisResult result = getAnalysisResult();
    result.addCompositionAttempt();
    final int numStates = aut.getStates().size();
    final int numTrans = aut.getTransitions().size();
    final double totalStates = result.getTotalNumberOfStates() + numStates;
    result.setTotalNumberOfStates(totalStates);
    final double peakStates =
      Math.max(result.getPeakNumberOfStates(), numStates);
    result.setPeakNumberOfStates(peakStates);
    final double totalTrans = result.getTotalNumberOfTransitions() + numTrans;
    result.setTotalNumberOfTransitions(totalTrans);
    final double peakTrans =
      Math.max(result.getPeakNumberOfTransitions(), numTrans);
    result.setPeakNumberOfTransitions(peakTrans);
    result.updatePeakMemoryUsage();
  }

  void recordUnsuccessfulComposition()
  {
    final CompositionalAnalysisResult result = getAnalysisResult();
    result.addUnsuccessfulComposition();
  }


  //#########################################################################
  //# Debugging
  @SuppressWarnings("unused")
  private EventInfo getEventInfo(final String name)
  {
    for (final Map.Entry<EventProxy,EventInfo> entry : mEventInfoMap.entrySet()) {
      final EventProxy event = entry.getKey();
      if (event.getName().equals(name)) {
        return entry.getValue();
      }
    }
    return null;
  }

  private void reportEventRemoval(final AutomatonProxy aut)
  {
    final Logger logger = LogManager.getLogger();
    if (logger.isDebugEnabled()) {
      final String msg =
        aut.getName() + " reduced to " + aut.getStates().size() +
        " states and " + aut.getTransitions().size() + " transitions.";
      logger.debug(msg);
    }
  }


  //#########################################################################
  //# Inner Class PreselectingMethod
  /**
   * The configuration setting to determine the {@link
   * AbstractCompositionalModelAnalyzer.PreselectingHeuristic
   * PreselectingHeuristic} used to
   * choose candidates during compositional verification. The preselecting
   * represents the first step of candidate selection. It generates a list
   * of candidates, from which the best candidate is to be chosen by the
   * selecting heuristic in the second step.
   */
  public abstract static class PreselectingMethod
  {
    //#######################################################################
    //# Constructors
    protected PreselectingMethod(final String name)
    {
      mName = name;
    }

    //#######################################################################
    //# Override for java.lang.Object
    @Override
    public String toString()
    {
      return mName;
    }

    //#######################################################################
    //# Heuristics
    /**
     * Gets the common method associated with this method.
     * Not all compositional model analysers support all preselecting
     * methods. By calling {@link #getCommonMethod()}, it should be
     * possible to obtain an alternative that is supported by all
     * compositional model analysers.
     */
    protected PreselectingMethod getCommonMethod()
    {
      return this;
    }

    /**
     * Creates the actual heuristics object implementing this preselecting
     * method.
     */
    abstract PreselectingHeuristic createHeuristic
      (AbstractCompositionalModelAnalyzer analyzer);

    //#######################################################################
    //# Data Members
    private final String mName;
  }


  //#########################################################################
  //# Inner Class PreselectingMethodFactory
  /**
   * The default preselecting method factory. This class can be used to
   * obtain a list of available preselecting heuristics, or to find
   * a preselecting heuristic given its name.
   *
   * Every compositional model analyser has its preselecting method factory
   * initialised by the constructor, but different subtypes may be initialised
   * with different factories.
   *
   * @see AbstractCompositionalModelAnalyzer#getPreselectingMethodFactory()
   * @see PreselectingMethod
   */
  protected static class PreselectingMethodFactory
    extends ListedEnumFactory<PreselectingMethod>
  {
    //#######################################################################
    //# Constructors
    protected PreselectingMethodFactory()
    {
      register(MustL);
      register(MaxS);
      register(MinT);
      register(Pairs);
    }

    //#######################################################################
    //# Migration
    /**
     * Returns a preselecting method from this factory with the same name
     * as the given method.
     * @return Preselecting method if found, otherwise <CODE>null</CODE>.
     */
    protected PreselectingMethod getEnumValue(final PreselectingMethod method)
    {
      final PreselectingMethod common = method.getCommonMethod();
      final String name = common.toString();
      return getEnumValue(name);
    }
  }


  //#########################################################################
  //# Preselection Methods
  /**
   * The preselecting method that produces candidates by pairing the
   * automaton with the most states to every other automaton in the model.
   */
  public static final PreselectingMethod MaxS =
      new PreselectingMethod("MaxS")
  {
    @Override
    PreselectingHeuristic createHeuristic
      (final AbstractCompositionalModelAnalyzer analyzer)
    {
      return analyzer.new HeuristicMaxS();
    }
  };

  /**
   * The preselecting method that produces candidates by pairing the
   * automaton with the fewest transitions to every other automaton in the
   * model.
   */
  public static final PreselectingMethod MinT =
      new PreselectingMethod("MinT")
  {
    @Override
    PreselectingHeuristic createHeuristic
      (final AbstractCompositionalModelAnalyzer analyzer)
    {
      return analyzer.new HeuristicMinT();
    }
  };

  /**
   * The preselecting method that considers every set of automata with at
   * least one local event as a candidate.
   */
  public static final PreselectingMethod MustL =
      new PreselectingMethod("MustL")
  {
    @Override
    PreselectingHeuristic createHeuristic
      (final AbstractCompositionalModelAnalyzer analyzer)
    {
      return analyzer.new HeuristicMustL();
    }
  };

  /**
   * The preselecting method that considers every pair of automata that
   * share at least one event.
   */
  public static final PreselectingMethod Pairs =
      new PreselectingMethod("Pairs")
  {
    @Override
    PreselectingHeuristic createHeuristic
      (final AbstractCompositionalModelAnalyzer analyzer)
    {
      return analyzer.new HeuristicPairs();
    }
  };


  //#########################################################################
  //# Inner Class SubSystem
  /**
   * A collection of automata and associated events.
   * This class is used to store subsystems to be checked later.
   * Essentially it holds the contents of a {@link ProductDESProxy},
   * but in a more lightweight form.
   */
  protected static class SubSystem
    implements Comparable<SubSystem>
  {

    //#######################################################################
    //# Constructors
    private SubSystem(final AutomatonProxy aut, final int limit)
    {
      final Collection<EventProxy> events = aut.getEvents();
      final int numEvents = events.size();
      mEvents = new ArrayList<EventProxy>(numEvents);
      for (final EventProxy event : events) {
        if (event.getKind() != EventKind.PROPOSITION) {
          mEvents.add(event);
        }
      }
      mAutomata = new ArrayList<AutomatonProxy>(1);
      mAutomata.add(aut);
      mStateLimit = limit;
    }

    private SubSystem(final List<EventProxy> events,
                      final List<AutomatonProxy> automata,
                      final int limit)
    {
      mEvents = events;
      mAutomata = automata;
      mStateLimit = limit;
    }

    //#######################################################################
    //# Interface java.util.Comparable<SubSystem>
    @Override
    public int compareTo(final SubSystem other)
    {
      final int aut1 = mAutomata.size();
      final int aut2 = other.mAutomata.size();
      if (aut1 != aut2) {
        return aut1 - aut2;
      }
      final int events1 = mEvents.size();
      final int events2 = other.mEvents.size();
      if (events1 != events2) {
        return events1 - events2;
      }
      final String name1 = Candidate.getCompositionName(mAutomata);
      final String name2 = Candidate.getCompositionName(other.mAutomata);
      return name1.compareTo(name2);
    }

    //#######################################################################
    //# Simple Access
    protected List<EventProxy> getEvents()
    {
      return mEvents;
    }

    protected List<AutomatonProxy> getAutomata()
    {
      return mAutomata;
    }

    protected int getStateLimit()
    {
      return mStateLimit;
    }

    //#######################################################################
    //# Data Members
    private final List<EventProxy> mEvents;
    private final List<AutomatonProxy> mAutomata;
    private final int mStateLimit;

  }


  //#########################################################################
  //# Inner Class EventInfo
  /**
   * A record to store information about the automata an event occurs in.
   * The event information record basically consists of the set of automata
   * it occurs in, plus information in which automata the event only appears
   * as selfloops.
   */
  protected static class EventInfo implements Comparable<EventInfo>
  {

    //#######################################################################
    //# Constructor
    /**
     * Creates a new EventInfo record.
     */
    protected EventInfo(final EventProxy event)
    {
      mEvent = event;
      mAutomataMap = new TObjectByteHashMap<AutomatonProxy>(0, 0.5f, (byte) -1);
      mSortedAutomataList = null;
      mNumNonSelfloopAutomata = 0;
      mIsBlocked = false;
      mFailingStatus = NOT_FAILING;
    }

    //#######################################################################
    //# Interface java.util.Comparable<EventInfo>
    @Override
    public int compareTo(final EventInfo info)
    {
      return mEvent.compareTo(info.mEvent);
    }

    //#######################################################################
    //# Event Status
    /**
     * Gets the event associated with this event information record.
     */
    protected EventProxy getEvent()
    {
      return mEvent;
    }

    /**
     * Returns whether this event should be treated as
     * {@link EventEncoding#TAU TAU}. Events treated as TAU are removed
     * during synchronous composition.
     */
    protected boolean canBeTau()
    {
      return true;
    }

    /**
     * Returns whether this event can be considered as local event. Local
     * events that have not been replaced by TAU during synchronous
     * compositions are passed to the abstraction procedure for special
     * treatment.
     */
    protected boolean canBeLocal()
    {
      return true;
    }

    /**
     * Returns whether this event is used at most by the given automata,
     * and thus can be hidden after composition of these automata.
     * @param  automata  Collection of automata to be composed or to
     *                   form a candidate.
     */
    protected boolean isLocal(final Collection<AutomatonProxy> automata)
    {
      if (canBeLocal()) {
        final TObjectByteIterator<AutomatonProxy> iter =
          mAutomataMap.iterator();
        while (iter.hasNext()) {
          iter.advance();
          final AutomatonProxy aut = iter.key();
          if (!automata.contains(aut)) {
            return false;
          }
        }
        return true;
      } else {
        return false;
      }
    }

    /**
     * Returns whether this event can be subject to selfloop removal.
     * Events subject to selfloop removal are removed from the model
     * when it is found that they appear only as selfloop events.
     */
    protected boolean isSubjectToSelfloopRemoval()
    {
      return canBeTau();
    }

    /**
     * Returns whether this event has been marked as failing.
     * In a nonblocking check, an event is failing if the model contains
     * an automaton such that every transition with this event has a
     * blocking target state.
     */
    protected boolean isFailing()
    {
      return mFailingStatus == FAILING && !mIsBlocked;
    }

   /**
     * Returns whether the given automaton uses this event.
     */
    boolean containsAutomaton(final AutomatonProxy aut)
    {
      return mAutomataMap.containsKey(aut);
    }

    /**
     * Returns an iterator over the automata using this event.
     * The keys of the returned iterator produce the event's automata
     * in random order. The iterator should only be used for reading,
     * otherwise the outcome is undefined.
     */
    protected TObjectByteIterator<AutomatonProxy> getAutomataIterator()
    {
      return mAutomataMap.iterator();
    }

    //#######################################################################
    //# Simple Access
    int getNumberOfAutomata()
    {
      return mAutomataMap.size();
    }

    int getNumberOfNonSelfloopAutomata()
    {
      return mNumNonSelfloopAutomata;
    }

    void addAutomaton(final AutomatonProxy aut, final byte status)
    {
      final byte present = mAutomataMap.get(aut);
      if (present != status) {
        assert status != REDUCED;  //Not Supported Presently.
        mAutomataMap.put(aut, status);
        mSortedAutomataList = null;
        if (present == NOT_ONLY_SELFLOOP || present == FAILING) {
          mNumNonSelfloopAutomata--;
        }
        if (status == NOT_ONLY_SELFLOOP || status == FAILING) {
          mNumNonSelfloopAutomata++;
        }
        mIsBlocked |= status == BLOCKED;
        if (status == FAILING && mFailingStatus == NOT_FAILING) {
          mFailingStatus = FAILING;
        }
      }
    }

    List<AutomatonProxy> getSortedAutomataList()
    {
      if (mSortedAutomataList == null) {
        final int size = mAutomataMap.size();
        final AutomatonProxy[] automata = new AutomatonProxy[size];
        mAutomataMap.keys(automata);
        Arrays.sort(automata);
        mSortedAutomataList = Arrays.asList(automata);
      }
      return mSortedAutomataList;
    }

    List<AutomatonProxy> getNonSelfloopAutomataList()
    {
      if (mNumNonSelfloopAutomata == 0) {
        return Collections.emptyList();
      }
      final List<AutomatonProxy> result =
        new ArrayList<AutomatonProxy>(mNumNonSelfloopAutomata);
      if (mSortedAutomataList != null) {
        for (final AutomatonProxy aut : mSortedAutomataList) {
          final byte status = mAutomataMap.get(aut);
          if (status == NOT_ONLY_SELFLOOP || status == FAILING) {
            result.add(aut);
          }
        }
      } else {
        final TObjectByteIterator<AutomatonProxy> iter = mAutomataMap.iterator();
        while (iter.hasNext()) {
          iter.advance();
          final byte status = iter.value();
          if (status == NOT_ONLY_SELFLOOP || status == FAILING) {
            result.add(iter.key());
          }
        }
        Collections.sort(result);
      }
      assert result.size() == mNumNonSelfloopAutomata;
      return result;
    }

    boolean isEmpty()
    {
      return mAutomataMap.isEmpty();
    }

    private boolean isRemovable(final boolean special)
    {
      if (mIsBlocked) {
        return true;
      } else if (special && mNumNonSelfloopAutomata == 0) {
        return isSubjectToSelfloopRemoval();
      } else {
        return false;
      }
    }

    void setReduced()
    {
      mFailingStatus = REDUCED;
      mNumNonSelfloopAutomata = getNumberOfAutomata();
      final TObjectByteIterator<AutomatonProxy> iter = mAutomataMap.iterator();
      while (iter.hasNext()) {
        iter.advance();
        iter.setValue(FAILING);
      }
    }

    void removeAutomata(final Collection<AutomatonProxy> automata)
    {
      for (final AutomatonProxy aut : automata) {
        removeAutomaton(aut);
      }
    }

    void removeAutomaton(final AutomatonProxy aut)
    {
      final byte code = mAutomataMap.remove(aut);
      mSortedAutomataList = null;
      if (code == NOT_ONLY_SELFLOOP) {
        mNumNonSelfloopAutomata--;
      }
    }

    boolean replaceAutomaton(final AutomatonProxy oldAut,
                             final AutomatonProxy newAut)
    {
      final byte code = mAutomataMap.remove(oldAut);
      if (code == UNKNOWN_SELFLOOP) {
        // not found in map ...
        return false;
      } else {
        mAutomataMap.put(newAut, code);
        mSortedAutomataList = null;
        return true;
      }
    }

    /**
     * Checks whether this event can be considered outside-only-selfloop
     * when composing and simplifying the given candidate.
     * @param  candidate  Candidate containing automata being composed and
     *         simplified.
     * @return <CODE>true</CODE> if all automata containing this event in
     *         non-selfloop transitions are contained in the given candidate.
     */
    boolean isOnlyNonSelfLoopCandidate(final Candidate candidate)
    {
      if (!isSubjectToSelfloopRemoval()) {
        return false;
      } else if (mNumNonSelfloopAutomata == 0) {
        return true;
      } else if (mNumNonSelfloopAutomata > candidate.getNumberOfAutomata()) {
        return false;
      } else {
        int remaining = mNumNonSelfloopAutomata;
        for (final AutomatonProxy aut : candidate.getAutomata()) {
          final byte status = mAutomataMap.get(aut);
          if (status == NOT_ONLY_SELFLOOP || status == FAILING) {
            remaining--;
            if (remaining == 0) {
              return true;
            }
          }
        }
        return false;
      }
    }

    //#######################################################################
    //# Debugging
    @Override
    public String toString()
    {
      final String name = mEvent.getName();
      final StringBuilder buffer = new StringBuilder(name);
      buffer.append(" [");
      boolean first = true;
      for (final AutomatonProxy aut : getSortedAutomataList()) {
        if (first) {
          first = false;
        } else {
          buffer.append(',');
        }
        buffer.append(aut.getName());
      }
      buffer.append(']');
      return buffer.toString();
    }

    //#######################################################################
    //# Data Members
    private final EventProxy mEvent;
    private final TObjectByteHashMap<AutomatonProxy> mAutomataMap;
    private List<AutomatonProxy> mSortedAutomataList;
    private int mNumNonSelfloopAutomata;
    private boolean mIsBlocked;
    private byte mFailingStatus;
  }


  //#########################################################################
  //# Local Interface PreselectingHeuristic
  protected interface PreselectingHeuristic
  {
    public List<Candidate> findCandidates();
  }


  //#########################################################################
  //# Inner Class PairingHeuristic
  protected abstract class PairingHeuristic
    implements PreselectingHeuristic, Comparator<AutomatonProxy>
  {

    //#######################################################################
    //# Interface PreselectingHeuristic
    @Override
    public List<Candidate> findCandidates()
    {
      if (mCurrentAutomata.isEmpty()) {
        return Collections.emptyList();
      } else {
        final AutomatonProxy chosenAut =
          Collections.min(mCurrentAutomata, this);
        return pairAutomaton(chosenAut, mCurrentAutomata);
      }
    }

    //#######################################################################
    //# Auxiliary Methods
    private List<Candidate> pairAutomaton
      (final AutomatonProxy chosenAut,
       final Collection<AutomatonProxy> automata)
    {
      final Set<EventProxy> chosenEvents =
        new THashSet<EventProxy>(chosenAut.getEvents());
      final List<Candidate> candidates = new LinkedList<Candidate>();
      for (final AutomatonProxy aut : automata) {
        if (aut != chosenAut && synchronises(chosenEvents, aut.getEvents())) {
          final List<AutomatonProxy> pair = new ArrayList<AutomatonProxy>(2);
          if (chosenAut.compareTo(aut) < 0) {
            pair.add(chosenAut);
            pair.add(aut);
          } else {
            pair.add(aut);
            pair.add(chosenAut);
          }
          if (isPermissibleCandidate(pair)) {
            final Set<EventProxy> localEvents = identifyLocalEvents(pair);
            final Candidate candidate = new Candidate(pair, localEvents);
            candidates.add(candidate);
          }
        }
      }
      return candidates;
    }

    private boolean synchronises(final Set<EventProxy> set,
                                 final Collection<EventProxy> collection)
    {
      final KindTranslator translator = getKindTranslator();
      for (final EventProxy event : collection) {
        if (translator.getEventKind(event) != EventKind.PROPOSITION &&
            set.contains(event)) {
          return true;
        }
      }
      return false;
    }
  }


  //#########################################################################
  //# Inner Class HeuristicMinT
  private class HeuristicMinT
    extends PairingHeuristic
  {

    //#######################################################################
    //# Interface java.util.Comparator<AutomatonProxy>
    @Override
    public int compare(final AutomatonProxy aut1, final AutomatonProxy aut2)
    {
      final int numtrans1 = aut1.getTransitions().size();
      final int numtrans2 = aut2.getTransitions().size();
      if (numtrans1 != numtrans2) {
        return numtrans1 - numtrans2;
      }
      final int numstates1 = aut1.getStates().size();
      final int numstates2 = aut2.getStates().size();
      if (numstates1 != numstates2) {
        return numstates1 - numstates2;
      }
      return aut1.compareTo(aut2);
    }

  }


  //#########################################################################
  //# Inner Class HeuristicMaxS
  /**
   * Performs step 1 of the approach to select the automata to compose. A
   * candidate is produced by pairing the automaton with the most states to
   * every other automaton in the model.
   */
  private class HeuristicMaxS
    extends PairingHeuristic
  {

    //#######################################################################
    //# Interface java.util.Comparator<AutomatonProxy>
    @Override
    public int compare(final AutomatonProxy aut1, final AutomatonProxy aut2)
    {
      final int numstates1 = aut1.getStates().size();
      final int numstates2 = aut2.getStates().size();
      if (numstates1 != numstates2) {
        return numstates2 - numstates1;
      }
      final int numtrans1 = aut1.getTransitions().size();
      final int numtrans2 = aut2.getTransitions().size();
      if (numtrans1 != numtrans2) {
        return numtrans2 - numtrans1;
      }
      return aut1.compareTo(aut2);
    }

  }


  //#########################################################################
  //# Inner Class HeuristicMustL
  private class HeuristicMustL
    implements PreselectingHeuristic
  {

    //#######################################################################
    //# Interface PreselectingHeuristic
    @Override
    public List<Candidate> findCandidates()
    {
      final List<Candidate> candidates = new LinkedList<Candidate>();
      final int size = mEventInfoMap.size();
      final Collection<List<AutomatonProxy>> found =
        new THashSet<List<AutomatonProxy>>(size);
      for (final EventInfo info : mEventInfoMap.values()) {
        assert info.getNumberOfAutomata() > 0;
        if (info.getNumberOfAutomata() > 1) {
          final List<AutomatonProxy> list = info.getSortedAutomataList();
          if (isPermissibleCandidate(list) && found.add(list)) {
            final Set<EventProxy> localEvents = identifyLocalEvents(list);
            final Candidate candidate = new Candidate(list, localEvents);
            candidates.add(candidate);
          }
        }
      }
      return candidates;
    }
  }


  //#########################################################################
  //# Inner Class HeuristicPairs
  private class HeuristicPairs
    implements PreselectingHeuristic
  {

    //#######################################################################
    //# Interface PreselectingHeuristic
    @Override
    public List<Candidate> findCandidates()
    {
      final List<Candidate> candidates = new LinkedList<Candidate>();
      final List<AutomatonProxy> automata = getCurrentAutomata();
      final int numAutomata = automata.size();
      for (int index1 = 0; index1 < numAutomata; index1++) {
        final AutomatonProxy aut1 = automata.get(index1);
        final int numEvents1 = aut1.getEvents().size();
        for (int index2 = index1 + 1; index2 < numAutomata; index2++) {
          final AutomatonProxy aut2 = automata.get(index2);
          final int numEvents2 = aut2.getEvents().size();
          if (numEvents1 <= numEvents2) {
            findCandidate(aut1, aut2, candidates);
          } else {
            findCandidate(aut2, aut1, candidates);
          }
        }
      }
      return candidates;
    }

    //#######################################################################
    //# Auxiliary Methods
    private void findCandidate(final AutomatonProxy aut1,
                               final AutomatonProxy aut2,
                               final List<Candidate> candidates)
    {
      for (final EventProxy event : aut1.getEvents()) {
        final EventInfo info = getEventInfo(event);
        if (info != null && info.containsAutomaton(aut2)) {
          final List<AutomatonProxy> automata = new ArrayList<>(2);
          if (aut1.compareTo(aut2) <= 0) {
            automata.add(aut1);
            automata.add(aut2);
          } else {
            automata.add(aut2);
            automata.add(aut1);
          }
          if (isPermissibleCandidate(automata)) {
            final Set<EventProxy> local = identifyLocalEvents(automata);
            final Candidate candidate = new Candidate(automata, local);
            candidates.add(candidate);
          }
          return;
        }
      }
    }

  }


  //#########################################################################
  //# Inner Class OneAutomatonStateMap
  private class OneAutomatonStateMap
    implements SynchronousProductStateMap
  {
    //#######################################################################
    //# Constructor
    private OneAutomatonStateMap(final AutomatonProxy inputAut,
                                 final StateEncoding inputEnc,
                                 final StateEncoding outputEnc)
    {
      mOriginalAutomaton = inputAut;
      final int numStates = inputEnc.getNumberOfStates();
      mStateMap = new HashMap<StateProxy,StateProxy>(numStates);
      for (int s = 0; s < numStates; s++) {
        final StateProxy inputState = inputEnc.getState(s);
        final StateProxy outputState = outputEnc.getState(s);
        mStateMap.put(outputState, inputState);
      }
    }


    //#######################################################################
    //# Interface
    //# net.sourceforge.waters.model.analysis.SynchronousProductStateMap
    @Override
    public Collection<AutomatonProxy> getInputAutomata()
    {
      return Collections.singletonList(mOriginalAutomaton);
    }

    @Override
    public StateProxy getOriginalState(final StateProxy tuple,
                                       final AutomatonProxy aut)
    {
      if (aut == mOriginalAutomaton) {
        return mStateMap.get(tuple);
      } else {
        throw new IllegalArgumentException
          ("Unexpected original automaton '" + aut.getName() + "' in " +
           ProxyTools.getShortClassName(this) + "!");
      }
    }

    //#######################################################################
    //# Data Members
    private final AutomatonProxy mOriginalAutomaton;
    private final Map<StateProxy,StateProxy> mStateMap;
  }


  //#########################################################################
  //# Data Members
  /**
   * The default (omega) marking to be used for conflict checks,
   * as specified by the user. This can be <CODE>null</CODE> if the
   * user wants to request the default.
   */
  private EventProxy mConfiguredDefaultMarking;

  /**
   * The currently used default (omega) marking.
   * This can be the configured marking ({@link #mConfiguredDefaultMarking})
   * or an automatically generated default, or <CODE>null</CODE> if the
   * algorithm does not depend on markings.
   */
  private EventProxy mUsedDefaultMarking;

  /**
   * The precondition (alpha) marking to be used for conflict checks,
   * as specified by the user. This can be <CODE>null</CODE> if the
   * user wants to request the default.
   */
  private EventProxy mConfiguredPreconditionMarking;

  /**
   * The currently used precondition (alpha) marking. This can be the
   * configured marking ({@link #mConfiguredPreconditionMarking})
   * or an automatically generated default, or <CODE>null</CODE> if the
   * algorithm does not depend on precondition markings.
   */
  private EventProxy mUsedPreconditionMarking;

  /**
   * The collection of propositions or markings respected by all simplifiers.
   * This collection is automatically set to contain the used default and
   * precondition markings ({@link #mUsedDefaultMarking} and
   * {@link #mUsedPreconditionMarking}) if present.
   */
  private Collection<EventProxy> mPropositions;

  private AbstractionProcedureCreator mAbstractionProcedureCreator;
  private final PreselectingMethodFactory mPreselectingMethodFactory;
  private PreselectingMethod mPreselectingMethod;
  private SelectionHeuristic<Candidate> mSelectionHeuristic;
  private boolean mSubsumptionEnabled;
  private boolean mBlockedEventsEnabled = true;
  private boolean mFailingEventsEnabled = false;
  private boolean mSelfloopOnlyEventsEnabled = false;
  private int mLowerInternalStateLimit;
  private int mUpperInternalStateLimit;
  private int mInternalTransitionLimit;
  private String mMonolithicDumpFileName = null;
  private ModelAnalyzer mMonolithicAnalyzer;
  private ModelAnalyzer mCurrentMonolithicAnalyzer;

  /**
   * The automata currently being analysed. This list is updated after each
   * abstraction step and represents the current state of the model. It may
   * contain abstractions of only part of the original model, if event-disjoint
   * subsystems are found.
   * @see #mPostponedSubsystems
   */
  private List<AutomatonProxy> mCurrentAutomata;

  private Map<EventProxy,EventInfo> mEventInfoMap =
      new HashMap<EventProxy,EventInfo>();
  /**
   * List of <I>dirty</I> automata that need simplifying. An automaton is added
   * to this list if there is the possibility that it can be simplified without
   * having to be composed with another automaton. Initially, all automata are
   * considered <I>dirty</I>, and certain abstractions such as event removal
   * may produce <I>dirty</I> automata at later stages. If there are dirty
   * automata, it is first attempted to simplify them individually, before
   * considering the next candidate for composition.
   * @see #simplifyDirtyAutomata()
   */
  private Queue<AutomatonProxy> mDirtyAutomata;
  /**
   * A flag indicating that a proper transition has been removed from an
   * automaton by the {@link #removeEvents(AutomatonProxy,Set,Map) removeEvents()}
   * method. This may trigger a reachability search.
   * @see #mDirtyAutomata
   */
  private boolean mHasRemovedProperTransition;
  /**
   * List of events found to be redundant and scheduled for removal by an
   * {@link EventRemovalStep}.
   * @see #removeEventsToAutomata(Collection) removeEventsToAutomata()
   * @see #removeRedundantEvents()
   */
  private Collection<EventProxy> mRedundantEvents;
  /**
   * A flag indicating that an event has disappeared unexpectedly.
   * This flag is set when a proper event has been found to be only selflooped
   * in an automaton after abstraction, and therefore has been removed from
   * the automaton alphabet. When set, this flag triggers the check for
   * event-disjoint subsystems.
   * @see #findEventDisjointSubsystems()
   */
  private boolean mMayBeSplit;
  /**
   * List of subsystems still to be analysed. If a model can be split into
   * event-disjoint subsystems, these subsystems are analysed one-by-one.
   * After splitting a subsystem, parts may be added to this list to be
   * analysed at a later stage.
   * @see #findEventDisjointSubsystems()
   */
  private Queue<SubSystem> mPostponedSubsystems;
  /**
   * List of subsystems that have been analysed.
   */
  private Collection<SubSystem> mProcessedSubsystems;
  private Set<String> mUsedEventNames;
  private Set<List<AutomatonProxy>> mOverflowCandidates;
  private int mCurrentInternalStateLimit;

  private MonolithicSynchronousProductBuilder mSynchronousProductBuilder;
  private AbstractionProcedure mAbstractionProcedure;
  private PreselectingHeuristic mPreselectingHeuristic;


  //#########################################################################
  //# Class Constants
  private static final String ALPHA = ":alpha";

  static final byte UNKNOWN_SELFLOOP = 0;
  static final byte ONLY_SELFLOOP = 1;
  static final byte NOT_ONLY_SELFLOOP = 2;
  static final byte BLOCKED = 3;

  static final byte NOT_FAILING = 4;
  static final byte FAILING = 5;
  static final byte REDUCED = 6;

}
