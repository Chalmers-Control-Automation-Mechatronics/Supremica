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

package net.sourceforge.waters.analysis.compositional;

import gnu.trove.iterator.TObjectByteIterator;
import gnu.trove.set.hash.THashSet;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Set;

import net.sourceforge.waters.analysis.abstraction.HalfWaySynthesisTRSimplifier;
import net.sourceforge.waters.analysis.abstraction.ProjectingSupervisorReductionFactory;
import net.sourceforge.waters.analysis.abstraction.SupervisorReductionFactory;
import net.sourceforge.waters.analysis.abstraction.SupervisorReductionMainMethod;
import net.sourceforge.waters.analysis.abstraction.SupervisorReductionProjectionMethod;
import net.sourceforge.waters.analysis.abstraction.SupervisorReductionSimplifier;
import net.sourceforge.waters.analysis.abstraction.TransitionRelationSimplifier;
import net.sourceforge.waters.analysis.monolithic.MonolithicSynchronousProductBuilder;
import net.sourceforge.waters.analysis.monolithic.MonolithicSynthesizer;
import net.sourceforge.waters.analysis.options.BooleanOption;
import net.sourceforge.waters.analysis.options.EnumOption;
import net.sourceforge.waters.analysis.options.Option;
import net.sourceforge.waters.analysis.options.OptionMap;
import net.sourceforge.waters.analysis.tr.EventEncoding;
import net.sourceforge.waters.analysis.tr.EventStatus;
import net.sourceforge.waters.analysis.tr.ListBufferTransitionRelation;
import net.sourceforge.waters.analysis.tr.TRPartition;
import net.sourceforge.waters.analysis.tr.TransitionIterator;
import net.sourceforge.waters.model.analysis.AnalysisException;
import net.sourceforge.waters.model.analysis.OverflowException;
import net.sourceforge.waters.model.analysis.des.AbstractModelAnalyzerFactory;
import net.sourceforge.waters.model.analysis.des.IsomorphismChecker;
import net.sourceforge.waters.model.analysis.des.SupervisorTooBigException;
import net.sourceforge.waters.model.analysis.kindtranslator.IdenticalKindTranslator;
import net.sourceforge.waters.model.analysis.kindtranslator.KindTranslator;
import net.sourceforge.waters.model.base.ComponentKind;
import net.sourceforge.waters.model.base.ProxyTools;
import net.sourceforge.waters.model.des.AutomatonProxy;
import net.sourceforge.waters.model.des.EventProxy;
import net.sourceforge.waters.model.des.ProductDESProxy;
import net.sourceforge.waters.model.des.ProductDESProxyFactory;
import net.sourceforge.waters.model.des.StateProxy;
import net.sourceforge.waters.model.des.TransitionProxy;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


/**
 * An implementation of the compositional synthesis algorithm.
 *
 * <I>References:</I><BR>
 * Sahar Mohajerani, Robi Malik, Simon Ware, Martin Fabian. On the Use of
 * Observation Equivalence in Synthesis Abstraction. Proc. 3rd IFAC Workshop
 * on Dependable Control of Discrete Systems, DCDS&nbsp;2011,
 * Saarbr&uuml;cken, Germany, 2011.<BR>
 * Sahar Mohajerani, Robi Malik, Martin Fabian. Nondeterminism Avoidance in
 * Compositional Synthesis of Discrete Event Systems, Proc. 7th International
 * Conference on Automation Science and Engineering, CASE&nbsp;2011, Trieste,
 * Italy.
 *
 * @author Sahar Mohajerani, Robi Malik
 */

public class CompositionalAutomataSynthesizer
  extends AbstractCompositionalSynthesizer
{

  //#########################################################################
  //# Constructors
  /**
   * Creates a compositional synthesiser without a model.
   *
   * @param factory
   *          Factory used for trace construction.
   */
  public CompositionalAutomataSynthesizer(final ProductDESProxyFactory factory)
  {
    this(factory, AutomataSynthesisAbstractionProcedureFactory.WSOE);
  }


  /**
   * Creates a compositional synthesiser without a model.
   *
   * @param factory
   *          Factory used for trace construction.
   * @param abstractionCreator
   *          Factory to define the abstraction sequence to be used.
   */
  public CompositionalAutomataSynthesizer
    (final ProductDESProxyFactory factory,
     final AbstractionProcedureCreator abstractionCreator)
  {
    this(factory, IdenticalKindTranslator.getInstance(), abstractionCreator);
  }

  /**
   * Creates a compositional synthesiser without a model.
   *
   * @param factory
   *          Factory used for trace construction.
   * @param translator
   *          Kind translator used to determine event and component kinds.
   * @param abstractionCreator
   *          Factory to define the abstraction sequence to be used.
   */
  public CompositionalAutomataSynthesizer
    (final ProductDESProxyFactory factory,
     final KindTranslator translator,
     final AbstractionProcedureCreator abstractionCreator)
  {
    this(null, factory, translator, abstractionCreator);
  }

  /**
   * Creates a compositional synthesiser without a model.
   *
   * @param factory
   *          Factory used for trace construction.
   * @param translator
   *          Kind translator used to determine event and component kinds.
   * @param abstractionCreator
   *          Factory to define the abstraction sequence to be used.
   * @param preselectingMethodFactory
   *          Enumeration factory that determines possible candidate
   *          preselection methods.
   */
  public CompositionalAutomataSynthesizer
    (final ProductDESProxyFactory factory,
     final KindTranslator translator,
     final AbstractionProcedureCreator abstractionCreator,
     final PreselectingMethodFactory preselectingMethodFactory)
  {
    this(null, factory, translator, abstractionCreator,
         preselectingMethodFactory);
  }

  /**
   * Creates a compositional synthesiser to compute a supervisor for the given
   * model.
   *
   * @param model
   *          The model to be checked by this model verifier.
   * @param factory
   *          Factory used for trace construction.
   * @param translator
   *          Kind translator used to determine event and component kinds.
   * @param abstractionCreator
   *          Factory to define the abstraction sequence to be used.
   */
  public CompositionalAutomataSynthesizer
    (final ProductDESProxy model,
     final ProductDESProxyFactory factory,
     final KindTranslator translator,
     final AbstractionProcedureCreator abstractionCreator)
  {
    this(model, factory, translator, abstractionCreator,
         new PreselectingMethodFactory());
  }

  /**
   * Creates a compositional synthesiser to compute a supervisor for the given
   * model.
   *
   * @param model
   *          The model to be checked by this model verifier.
   * @param factory
   *          Factory used for trace construction.
   * @param translator
   *          Kind translator used to determine event and component kinds.
   * @param abstractionCreator
   *          Factory to define the abstraction sequence to be used.
   * @param preselectingMethodFactory
   *          Enumeration factory that determines possible candidate
   *          preselection methods.
   */
  public CompositionalAutomataSynthesizer
    (final ProductDESProxy model,
     final ProductDESProxyFactory factory,
     final KindTranslator translator,
     final AbstractionProcedureCreator abstractionCreator,
     final PreselectingMethodFactory preselectingMethodFactory)
  {
    super(model, factory, translator, abstractionCreator,
          preselectingMethodFactory);
    setSelectionHeuristic(CompositionalSelectionHeuristicFactory.MinSync);
  }


  //#########################################################################
  //# Configuration
  @Override
  public AutomataSynthesisAbstractionProcedureFactory
    getAbstractionProcedureFactory()
  {
    return AutomataSynthesisAbstractionProcedureFactory.getInstance();
  }


  //#########################################################################
  //# Interface net.sourceforge.waters.model.analysis.Abortable
  @Override
  public void requestAbort()
  {
    super.requestAbort();
    if (mPreSupervisorReducer != null) {
        mPreSupervisorReducer.requestAbort();
      }
    if (mMainSupervisorReducer != null) {
        mMainSupervisorReducer.requestAbort();
      }
    if (mHalfwaySimplifier != null) {
      mHalfwaySimplifier.requestAbort();
    }
  }

  @Override
  public void resetAbort()
  {
    super.resetAbort();
    if (mPreSupervisorReducer != null) {
        mPreSupervisorReducer.resetAbort();
      }
    if (mMainSupervisorReducer != null) {
        mMainSupervisorReducer.resetAbort();
      }
    if (mHalfwaySimplifier != null) {
      mHalfwaySimplifier.resetAbort();
    }
  }


  //#########################################################################
  //# Interface net.sourceforge.waters.model.analysis.SupervisorSynthesizer
  @Override
  public void setSupervisorReductionFactory(final SupervisorReductionFactory factory)
  {
    mSupervisorReductionFactory = factory;
  }

  @Override
  public SupervisorReductionFactory getSupervisorReductionFactory()
  {
    return mSupervisorReductionFactory;
  }

  @Override
  public void setSupervisorLocalizationEnabled(final boolean enable)
  {
    mSupervisorLocalizationEnabled = enable;
  }

  @Override
  public boolean isSupervisorLocalizationEnabled()
  {
    return mSupervisorLocalizationEnabled;
  }


  //#########################################################################
  //# Interface net.sourceforge.waters.model.analysis.ModelAnalyzer
  @Override
  public List<Option<?>> getOptions(final OptionMap db)
  {
    final List<Option<?>> options = super.getOptions(db);
    final ListIterator<Option<?>> iter = options.listIterator();
    while (iter.hasNext()) {
      final Option<?> option = iter.next();
      if (option.hasID(CompositionalModelAnalyzerFactory.
                       OPTION_AbstractCompositionalModelAnalyzer_SubumptionEnabled)) {
        Option<?> addition;
        addition = db.get(CompositionalModelAnalyzerFactory.
                          OPTION_CompositionalAutomataSynthesizer_AbstractionProcedureCreator);
        iter.add(addition);
        addition = db.get(AbstractModelAnalyzerFactory.
                          OPTION_SupervisorSynthesizer_SupervisorReductionMainMethod);
        iter.add(addition);
        addition = db.get(AbstractModelAnalyzerFactory.
                          OPTION_SupervisorSynthesizer_SupervisorReductionProjectionMethod);
        iter.add(addition);
        addition = db.get(AbstractModelAnalyzerFactory.
                          OPTION_SupervisorSynthesizer_SupervisorLocalisationEnabled);
        iter.add(addition);
        break;
      }
    }
    return options;
  }

  @SuppressWarnings("unchecked")
  @Override
  public void setOption(final Option<?> option)
  {
    if (option.hasID(CompositionalModelAnalyzerFactory.
                     OPTION_CompositionalAutomataSynthesizer_AbstractionProcedureCreator)) {
      final EnumOption<AbstractionProcedureCreator> enumOption =
        (EnumOption<AbstractionProcedureCreator>) option;
      setAbstractionProcedureCreator(enumOption.getValue());
    } else if (option.hasID(AbstractModelAnalyzerFactory.
                            OPTION_SupervisorSynthesizer_SupervisorReductionMainMethod)) {
      final EnumOption<SupervisorReductionMainMethod> enumOption =
        (EnumOption<SupervisorReductionMainMethod>) option;
      ProjectingSupervisorReductionFactory.configureSynthesizer
        (this, enumOption.getValue());
    } else if (option.hasID(AbstractModelAnalyzerFactory.
                            OPTION_SupervisorSynthesizer_SupervisorReductionProjectionMethod)) {
      final EnumOption<SupervisorReductionProjectionMethod> enumOption =
        (EnumOption<SupervisorReductionProjectionMethod>) option;
      ProjectingSupervisorReductionFactory.configureSynthesizer
        (this, enumOption.getValue());
    } else if (option.hasID(AbstractModelAnalyzerFactory.
                            OPTION_SupervisorSynthesizer_SupervisorLocalisationEnabled)) {
      final BooleanOption boolOption = (BooleanOption) option;
      setSupervisorLocalizationEnabled(boolOption.getBooleanValue());
    } else {
      super.setOption(option);
    }
  }


  //#########################################################################
  //# Invocation
  @Override
  public boolean run() throws AnalysisException
  {
    try {
      setUp();
      final CompositionalAutomataSynthesisResult result = getAnalysisResult();
      if (!result.isFinished()) {
        runCompositionalMinimisation();
      }
      if (!result.isFinished()) {
        result.setSatisfied(true);
        final ProductDESProxyFactory factory = getFactory();
        result.close(factory, getOutputName());
      }
      final Logger logger = LogManager.getLogger();
      logger.debug("{} done.", ProxyTools.getShortClassName(this));
      result.setNumberOfRenamings(mDistinguisherInfoList.size());
      return result.isSatisfied();
    } catch (final AnalysisException exception) {
      throw setExceptionResult(exception);
    } catch (final OutOfMemoryError error) {
      System.gc();
      final OverflowException overflow = new OverflowException(error);
      throw setExceptionResult(overflow);
    } finally {
      tearDown();
    }
  }


  //#########################################################################
  //# Overrides for net.sourceforge.waters.model.AbstractModelAnalyser
  @Override
  public CompositionalAutomataSynthesisResult createAnalysisResult()
  {
    return new CompositionalAutomataSynthesisResult(this,
                                                    isDetailedOutputEnabled());
  }

  @Override
  public CompositionalAutomataSynthesisResult getAnalysisResult()
  {
    return (CompositionalAutomataSynthesisResult) super.getAnalysisResult();
  }

  @Override
  protected void setUp() throws AnalysisException
  {
    mDistinguisherInfoList = new LinkedList<DistinguisherInfo>();
    mBackRenaming = new HashMap<EventProxy,EventProxy>();
    mHalfwaySimplifier = new HalfWaySynthesisTRSimplifier();
    mHalfwaySimplifier.setOutputMode
      (HalfWaySynthesisTRSimplifier.OutputMode.PSEUDO_SUPERVISOR);
    if (isDetailedOutputEnabled()) {
      mMainSupervisorReducer = mSupervisorReductionFactory.
        createSupervisorReducer(mSupervisorLocalizationEnabled);
      if (mMainSupervisorReducer != null) {
        final int stateLimit = getInternalStateLimit();
        mMainSupervisorReducer.setStateLimit(stateLimit);
        final int transitionLimit = getInternalTransitionLimit();
        mMainSupervisorReducer.setTransitionLimit(transitionLimit);
        mPreSupervisorReducer =
          mSupervisorReductionFactory.createInitialMinimizer(true);
        if (mPreSupervisorReducer != null) {
          mPreSupervisorReducer.setStateLimit(stateLimit);
          mPreSupervisorReducer.setTransitionLimit(transitionLimit);
          final int config = mMainSupervisorReducer.getPreferredInputConfiguration();
          mPreSupervisorReducer.setPreferredOutputConfiguration(config);
        }
        if (mSupervisorLocalizationEnabled) {
          final ProductDESProxyFactory factory = getFactory();
          mIsomorphismChecker = new IsomorphismChecker(factory, false, false);
        }
      }
    }
    super.setUp();
  }

  @Override
  protected void tearDown()
  {
    super.tearDown();
    mDistinguisherInfoList = null;
    mBackRenaming = null;
    mMainSupervisorReducer = null;
    mHalfwaySimplifier = null;
  }


  //#########################################################################
  //# Hooks
  @Override
  protected SynthesisEventInfo createEventInfo(final EventProxy event)
  {
    if (isUsingFailingEvents()) {
      return new RenamedEventInfo(event);
    } else {
      return new SynthesisEventInfo(event);
    }
  }

  @Override
  protected void recordCandidateApplication(final Candidate candidate,
                                            final List<AbstractionStep> steps,
                                            final AutomatonProxy aut)
    throws AnalysisException
  {
    if (isUsingFailingEvents()) {
      for (final AbstractionStep step : steps) {
        createRenamedEventInfo(step);
      }
    }
    super.recordCandidateApplication(candidate, steps, aut);
  }

  @Override
  protected void recordAbstractionStep(final AbstractionStep step)
    throws AnalysisException
  {
    if (step instanceof SynthesisAbstractionStep) {
      final CompositionalAutomataSynthesisResult result = getAnalysisResult();
      final ProductDESProxyFactory factory = getFactory();
      final SynthesisAbstractionStep synthStep =
        (SynthesisAbstractionStep) step;
      mTempEventEncoding = synthStep.getEventEncoding();
      final ListBufferTransitionRelation supervisor = synthStep.getSupervisor();
      if (supervisor != null) {
        if (supervisor.isEmpty()) {
          result.setSatisfied(false);
          return;
        } else {
          final EventEncoding enc = synthStep.getEventEncoding();
          final int defaultMarking = enc.getEventCode(getUsedDefaultMarking());
          result.addUnrenamedSupervisor(supervisor, defaultMarking);
          if (isDetailedOutputEnabled()) {
            createSupervisor(supervisor);
          }
        }
      }

      // Apply inverse renaming to other automata
      final Map<EventProxy,List<EventProxy>> renaming = synthStep.getRenaming();
      if (renaming != null) {
        final AutomatonProxy originalAut = synthStep.getOriginalAutomaton();
        final Set<AutomatonProxy> affectedAutomata =
          new THashSet<AutomatonProxy>();
        for (final EventProxy event : renaming.keySet()) {
          final EventInfo info = getEventInfo(event);
          if (info != null) {
            final TObjectByteIterator<AutomatonProxy> iter =
              info.getAutomataIterator();
            while (iter.hasNext()) {
              iter.advance();
              final AutomatonProxy aut = iter.key();
              affectedAutomata.add(aut);
            }
          }
        }

        for (final AutomatonProxy aut : affectedAutomata) {
          if (aut != originalAut) {
            final Collection<EventProxy> oldAlphabet = aut.getEvents();
            final Collection<EventProxy> newAlphabet =
              new ArrayList<EventProxy>(oldAlphabet.size());
            final Collection<TransitionProxy> oldTransitions =
              aut.getTransitions();
            final Collection<TransitionProxy> newTransitions =
              new ArrayList<TransitionProxy>(oldTransitions.size());
            for (final EventProxy event : oldAlphabet) {
              if (renaming.containsKey(event)) {
                newAlphabet.addAll(renaming.get(event));
              } else {
                newAlphabet.add(event);
              }
            }
            for (final TransitionProxy trans : oldTransitions) {
              final StateProxy source = trans.getSource();
              final StateProxy target = trans.getTarget();
              final EventProxy event = trans.getEvent();
              final List<EventProxy> replacement = renaming.get(event);
              if (replacement != null) {
                for (final EventProxy e : replacement) {
                  final TransitionProxy newTransition =
                    factory.createTransitionProxy(source, e, target);
                  newTransitions.add(newTransition);
                }
              } else {
                newTransitions.add(trans);
              }
            }
            final AutomatonProxy newAut =
              factory.createAutomatonProxy(aut.getName(), aut.getKind(),
                                           newAlphabet, aut.getStates(),
                                           newTransitions);
            updateEventsToAutomata(newAut, Collections.singletonList(aut));
            replaceDirtyAutomaton(newAut, aut);
          }
        }
      }
    }
  }

  @Override
  protected boolean doMonolithicAnalysis(final List<AutomatonProxy> automata)
    throws AnalysisException
  {
    AutomatonProxy automaton = null;
    switch (automata.size()) {
    case 0:
      return true;
    case 1:
      automaton = automata.get(0);
      break;
    default:
      final Logger logger = LogManager.getLogger();
      if (logger.isDebugEnabled()) {
        double estimate = 1.0;
        for (final AutomatonProxy aut : automata) {
          estimate *= aut.getStates().size();
        }
        logger.debug("Monolithically composing " + automata.size() +
                     " automata, estimated " + estimate + " states.");
      }
      final MonolithicSynchronousProductBuilder syncBuilder =
        getSynchronousProductBuilder();
      final ProductDESProxy des = createProductDESProxy(automata);
      reportMonolithicAnalysis(des);
      syncBuilder.setModel(des);
      final int slimit = getMonolithicStateLimit();
      syncBuilder.setNodeLimit(slimit);
      final int tlimit = getMonolithicTransitionLimit();
      syncBuilder.setTransitionLimit(tlimit);
      syncBuilder.setDetailedOutputEnabled(true);
      syncBuilder.setPropositions(getPropositions());
      syncBuilder.run();
      automaton = syncBuilder.getComputedAutomaton();
      break;
    }
    final CompositionalAutomataSynthesisResult result = getAnalysisResult();
    result.addSynchSize(automaton);
    mTempEventEncoding = createSynthesisEventEncoding(automaton);
    final ListBufferTransitionRelation supervisor =
      synthesise(automaton, mTempEventEncoding);
    if (supervisor != null) {
      reportSupervisor("monolithic", supervisor);
      if (supervisor.getNumberOfReachableStates() == 0) {
        result.setSatisfied(false);
        return false;
      } else {
        final int defaultMarking =
          mTempEventEncoding.getEventCode(getUsedDefaultMarking());
        result.addUnrenamedSupervisor(supervisor, defaultMarking);
        if (isDetailedOutputEnabled()) {
          createSupervisor(supervisor);
        }
        return true;
      }
    } else {
      return true;
    }
  }


  //#########################################################################
  //# Renaming
  /**
   * Gets the back-renamed original for the given event.
   * @param  event  An event in the current context, which may or may not
   *                have been renamed.
   * @return The event corresponding to the given event in the original model.
   */
  EventProxy getOriginalEvent(final EventProxy event)
  {
    final EventProxy lookup = mBackRenaming.get(event);
    if (lookup == null) {
      return event;
    } else {
      return lookup;
    }
  }

  void recordDistinguisherInfo(final Map<EventProxy,List<EventProxy>> renamings,
                              final AutomatonProxy distinguisherAutomaton)
  {
    final DistinguisherInfo info = new DistinguisherInfo(distinguisherAutomaton);
    for (final Map.Entry<EventProxy,List<EventProxy>> entry :
         renamings.entrySet()) {
      final EventProxy original = entry.getKey();
      final List<EventProxy> replacements = entry.getValue();
      info.addReplacement(original, replacements);
      final EventProxy backRenamed = getOriginalEvent(original);
      for (final EventProxy replacement : replacements) {
        mBackRenaming.put(replacement, backRenamed);
      }
    }
    mDistinguisherInfoList.add(info);
  }

  private void createRenamedEventInfo(final AbstractionStep step)
  {
    if (step instanceof SynthesisAbstractionStep) {
      final SynthesisAbstractionStep synthStep =
        (SynthesisAbstractionStep) step;
      final Map<EventProxy,List<EventProxy>> renaming = synthStep.getRenaming();
      if (renaming != null) {
        for (final Map.Entry<EventProxy,List<EventProxy>> entry :
             renaming.entrySet()) {
          final EventProxy originalEvent = entry.getKey();
          final RenamedEventInfo originalInfo =
            (RenamedEventInfo) getEventInfo(originalEvent);
          for (final EventProxy replacementEvent : entry.getValue()) {
            final RenamedEventInfo replacementInfo =
              new RenamedEventInfo(replacementEvent, originalInfo);
            putEventInfo(replacementEvent, replacementInfo);
          }
        }
      }
    }
  }

  private EventEncoding createSynthesisEventEncoding(final AutomatonProxy aut)
    throws OverflowException
  {
    final KindTranslator translator = getKindTranslator();
    final Collection<EventProxy> props = getPropositions();
    final Collection<EventProxy> filter;
    if (props == null) {
      filter = Collections.emptyList();
    } else {
      filter = props;
    }
    final EventEncoding encoding =
      new EventEncoding(aut, translator, filter,
                        EventEncoding.FILTER_PROPOSITIONS);
    for (int e = EventEncoding.NONTAU; e < encoding.getNumberOfProperEvents(); e++) {
      final byte status = encoding.getProperEventStatus(e);
      encoding.setProperEventStatus(e, status | EventStatus.STATUS_LOCAL);
    }
    encoding.sortProperEvents((byte) ~EventStatus.STATUS_LOCAL,
                              EventStatus.STATUS_CONTROLLABLE);
    return encoding;
  }

  private ListBufferTransitionRelation synthesise(final AutomatonProxy aut,
                                                  final EventEncoding eventEnc)
    throws AnalysisException
  {
    final CompositionalSynthesisResult result = getAnalysisResult();
    recordStatistics(aut);
    final ListBufferTransitionRelation rel =
      new ListBufferTransitionRelation(aut,
                                       eventEnc.clone(),
                                       ListBufferTransitionRelation.CONFIG_PREDECESSORS);
    mHalfwaySimplifier.setTransitionRelation(rel);
    final EventProxy defaultMarking = getUsedDefaultMarking();
    final int defaultID = eventEnc.getEventCode(defaultMarking);
    if (defaultID < 0) {
      return null;
    }
    mHalfwaySimplifier.setDefaultMarkingID(defaultID);
    mHalfwaySimplifier.run();
    // TODO Reachability check correct?
    result.addMonolithicAnalysisResult(null);
    final ListBufferTransitionRelation supervisor =
      mHalfwaySimplifier.getTransitionRelation();
    final TRPartition partition = mHalfwaySimplifier.getResultPartition();
    if (partition == null) {
      return null;
    } else if (partition.isEmpty()) {
      return supervisor;
    }
    final TransitionIterator iter =
      supervisor.createAllTransitionsReadOnlyIterator();
    while (iter.advance()) {
      final int t = iter.getCurrentTargetState();
      if (partition.getClassCode(t) < 0) {
        final int e = iter.getCurrentEvent();
        final byte status = rel.getProperEventStatus(e);
        if (EventStatus.isControllableEvent(status)) {
          return supervisor;
        }
      }
    }
    return null;
  }


  private void createSupervisor(ListBufferTransitionRelation rel)
    throws AnalysisException
  {
    final String name = getUniqueSupervisorName(rel);
    while (true) {
      DistinguisherInfo firstDeferred = null;
      final Set<EventProxy> deferredEvents = new THashSet<>();
      final ListIterator<DistinguisherInfo> iter =
        mDistinguisherInfoList.listIterator(mDistinguisherInfoList.size());
      while (iter.hasPrevious()) {
        final DistinguisherInfo info = iter.previous();
        if (!info.containsReplacedEvent(mTempEventEncoding)) {
          // skip
          continue;
        } else if (info.containsReplacedEvent(deferredEvents) ||
                   !isDeterministic(rel, info)) {
          // defer
          info.addDeferredEvents(deferredEvents);
          if (firstDeferred == null) {
            firstDeferred = info;
          }
        } else {
          // apply renaming
          rel = createRenamedSupervisor(rel, info);
        }
      }
      if (firstDeferred == null) {
        break;
      } else {
        rel = createSynchronizedSupervisor(rel, firstDeferred);
      }
    }
    rel.setName(name);
    localiseSupervisor(rel);
  }

  private boolean isDeterministic(final ListBufferTransitionRelation rel,
                                  final DistinguisherInfo info)
    throws AnalysisException
  {
    final int numOfStates = rel.getNumberOfStates();
    final TransitionIterator transitionIter =
      rel.createSuccessorsReadOnlyIterator();
    for (final DistinguisherReplacement pair : info.getReplacements()) {
      for (int state = 0; state < numOfStates; state++) {
        checkAbort();
        int foundSuccessor = -1;
        for (final EventProxy event : pair.getReplacedEvents()) {
          final int e = getUsedEvent(event, mTempEventEncoding, rel);
          final int successor;
          if (e < 0) {
            // not in alphabet - selflooped in all states
            successor = state;
          } else {
            transitionIter.reset(state, e);
            if (transitionIter.advance()) {
              successor = transitionIter.getCurrentTargetState();
            } else {
              successor = -1;
            }
          }
          if (successor < 0 || successor == foundSuccessor) {
            //nothing
          } else if (foundSuccessor < 0) {
            foundSuccessor = successor;
          } else {
            return false;
          }
        }
      }
    }
    return true;
  }

  private ListBufferTransitionRelation createRenamedSupervisor
    (final ListBufferTransitionRelation rel,
     final DistinguisherInfo info)
    throws AnalysisException
  {
    List<EventProxy> events = null;
    for (final DistinguisherReplacement pair : info.getReplacements()) {
      final EventProxy original = pair.getOriginalEvent();
      final List<EventProxy> replacement = pair.getReplacedEvents();
      // If an event is not in the encoding, and there is no
      // nondeterminism, these events will be all-selfloops.
      boolean selfloop = false;
      for (final EventProxy event : replacement) {
        if (getUsedEvent(event, mTempEventEncoding, rel) < 0) {
          selfloop = true;
          break;
        }
      }
      if (selfloop) {
        for (final EventProxy event : replacement) {
          final int e = mTempEventEncoding.getEventCode(event);
          if (e >= 0) {
            checkAbort();
            rel.removeEvent(e);
          }
        }
      } else {
        int r = -1;
        for (final EventProxy event : replacement) {
          final int e = mTempEventEncoding.getEventCode(event);
          if (r < 0) {
            r = e;
          } else {
            checkAbort();
            rel.replaceEvent(e, r);
            final byte status = rel.getProperEventStatus(e);
            rel.setProperEventStatus(e, status | EventStatus.STATUS_UNUSED);
          }
        }
        assert r >= 0 : "Replacement event not found!";
        if (mTempEventEncoding.getEventCode(original) < 0) {
          if (events == null) {
            final int size = mTempEventEncoding.getNumberOfEvents();
            events = new ArrayList<>(size);
            events.add(null);
            events.addAll(mTempEventEncoding.getUsedEvents());
          }
          events.set(r, original);
        }
      }
    }
    if (events != null) {
      final KindTranslator translator = getKindTranslator();
      mTempEventEncoding = new EventEncoding(events, translator);
    }
    return rel;
  }

  private ListBufferTransitionRelation createSynchronizedSupervisor
    (final ListBufferTransitionRelation rel,
     final DistinguisherInfo info)
    throws AnalysisException
  {
    final AutomatonProxy distinguisher = info.getDistinguisher();
    final ProductDESProxyFactory factory = getFactory();
    final AutomatonProxy oldSupervisor =
      rel.createAutomaton(factory, mTempEventEncoding);
    final List<AutomatonProxy> automata = new ArrayList<AutomatonProxy>(2);
    automata.add(oldSupervisor);
    automata.add(distinguisher);
    final ProductDESProxy model = createProductDESProxy(automata);
    final MonolithicSynchronousProductBuilder builder =
      getSynchronousProductBuilder();
    builder.setNodeLimit(getMonolithicStateLimit());
    builder.setTransitionLimit(getMonolithicTransitionLimit());
    builder.setDetailedOutputEnabled(true);
    builder.setPropositions(getPropositions());
    builder.setModel(model);
    for (final DistinguisherReplacement pair : info.getReplacements()) {
      final EventProxy original = pair.getOriginalEvent();
      final List<EventProxy> replacement = pair.getReplacedEvents();
      builder.addMask(replacement, original);
    }
    try {
      builder.run();
    } catch (final OverflowException exception) {
      throw new SupervisorTooBigException(exception);
    } finally {
      builder.clearMask();
    }
    final AutomatonProxy newSupervisor = builder.getComputedAutomaton();
    final KindTranslator translator = getKindTranslator();
    mTempEventEncoding = new EventEncoding(newSupervisor, translator);
    mTempEventEncoding.sortProperEvents(EventStatus.STATUS_CONTROLLABLE);
    return new ListBufferTransitionRelation(newSupervisor,
                                            mTempEventEncoding.clone(),
                                            ListBufferTransitionRelation.CONFIG_SUCCESSORS);
  }


  private void localiseSupervisor(final ListBufferTransitionRelation rel)
    throws AnalysisException
  {
    if (mMainSupervisorReducer == null) {
      final AutomatonProxy sup = createSupervisorAutomaton(rel);
      recordSupervisor(sup);
    } else {
      // TODO Creating dump state---could this have been done before?
      final EventProxy marking = getUsedDefaultMarking();
      final int markingID = mTempEventEncoding.getEventCode(marking);
      mMainSupervisorReducer.setDefaultMarkingID(markingID);
      if (mPreSupervisorReducer != null) {
        mPreSupervisorReducer.setDefaultMarkingID(markingID);
      }
      final int dumpIndex = rel.getDumpStateIndex();
      rel.reconfigure(ListBufferTransitionRelation.CONFIG_SUCCESSORS);
      final TransitionIterator iter =
        rel.createAllTransitionsModifyingIterator();
      while (iter.advance()) {
        final int target = iter.getCurrentToState();
        if (target != dumpIndex && rel.isDeadlockState(target, markingID)) {
          iter.setCurrentToState(dumpIndex);
          rel.setReachable(dumpIndex, true);
          if (rel.isInitial(target)) {
            rel.setInitial(dumpIndex, true);
            rel.setInitial(target, false);
          }
          rel.setReachable(target, false);
        }
      }
      // No local events before supervisor reduction
      final int numEvents = rel.getNumberOfProperEvents();
      for (int e = EventEncoding.NONTAU; e < numEvents; e++) {
        final byte status = rel.getProperEventStatus(e);
        rel.setProperEventStatus(e, status & ~EventStatus.STATUS_LOCAL);
      }
      final Logger logger = LogManager.getLogger();
      if (mSupervisorLocalizationEnabled) {
        final List<AutomatonProxy> results = new ArrayList<>(numEvents);
        final String prefix = rel.getName();
        final ListBufferTransitionRelation preReduced =
          reduceSupervisorStep1(rel);
        try {
          for (int e = EventEncoding.NONTAU; e < numEvents; e++) {
            if (MonolithicSynthesizer.isEverDisabledControllableEvent(rel, e)) {
              final EventProxy event = mTempEventEncoding.getProperEvent(e);
              logger.debug("Reducing localised supervisor for controllable {} ...",
                           event.getName());
              final ListBufferTransitionRelation reduced =
                reduceSupervisorStep2(preReduced, e);
              final String name = prefix + ":" + event.getName();
              reduced.setName(name);
              final AutomatonProxy sup = createSupervisorAutomaton(reduced);
              if (isNewSupervisor(sup, results)) {
                results.add(sup);
                logger.debug("Supervisor to be included.");
              } else {
                logger.debug("Duplicate supervisor suppressed.");
              }
            }
          }
        } catch (OutOfMemoryError | OverflowException exception) {
          // Overflow in supervisor reduction must be caught here -
          // otherwise errors moving to next candidate in main loop.
          System.gc();
          logger.debug(UNREDUCED_SUPERVISOR);
          final AutomatonProxy sup = createSupervisorAutomaton(preReduced);
          recordSupervisor(sup);
          return;
        }
        for (final AutomatonProxy sup : results) {
          recordSupervisor(sup);
        }
      } else {
        logger.debug("Reducing supervisor ...");
        ListBufferTransitionRelation reduced = reduceSupervisorStep1(rel);
        try {
          reduced = reduceSupervisorStep2(reduced, -1);
          logger.debug("Supervisor added.");
        } catch (OutOfMemoryError | OverflowException exception) {
          // Overflow in supervisor reduction must be caught here.
          System.gc();
          logger.debug(UNREDUCED_SUPERVISOR);
        }
        final AutomatonProxy sup = createSupervisorAutomaton(reduced);
        recordSupervisor(sup);
      }
    }
  }

  private ListBufferTransitionRelation reduceSupervisorStep1
    (final ListBufferTransitionRelation sup)
    throws AnalysisException
  {
    if (mPreSupervisorReducer != null) {
      final Logger logger = LogManager.getLogger();
      if (mSupervisorLocalizationEnabled) {
        logger.debug("Common steps of supervisor reduction ...");
      }
      sup.logSizes(logger);
      mPreSupervisorReducer.createStatistics();
      mPreSupervisorReducer.setTransitionRelation(sup);
      mPreSupervisorReducer.run();
      final CompositionalAutomataSynthesisResult result = getAnalysisResult();
      result.addPreSupervisorReductionStatistics(mPreSupervisorReducer);
      return mPreSupervisorReducer.getTransitionRelation();
    } else {
      return sup;
    }
  }

  private ListBufferTransitionRelation reduceSupervisorStep2
    (ListBufferTransitionRelation sup,
     final int supervisedEvent)
    throws AnalysisException
  {
    if (supervisedEvent >= 0) {
      final Logger logger = LogManager.getLogger();
      sup.logSizes(logger);
      final int config = mMainSupervisorReducer.getPreferredInputConfiguration();
      sup = new ListBufferTransitionRelation(sup, config);
      MonolithicSynthesizer.removeOtherControllableDisablements
        (sup, supervisedEvent);
    }
    mMainSupervisorReducer.createStatistics();
    mMainSupervisorReducer.setTransitionRelation(sup);
    mMainSupervisorReducer.setSupervisedEvent(supervisedEvent);
    mMainSupervisorReducer.run();
    final CompositionalAutomataSynthesisResult result = getAnalysisResult();
    result.addMainSupervisorReductionStatistics(mMainSupervisorReducer);
    return mMainSupervisorReducer.getTransitionRelation();
  }

  private String getUniqueSupervisorName(final ListBufferTransitionRelation rel)
  {
    final CompositionalAutomataSynthesisResult result = getAnalysisResult();
    final Collection<AutomatonProxy> supervisors =
      result.getComputedAutomata();
    String supname;
    int index = 0;
    boolean found;
    do {
      if (index == 0) {
        supname = getOutputName() + ":" + rel.getName();
      } else {
        supname = getOutputName() + index + ":" + rel.getName();
      }
      found = false;
      for (final AutomatonProxy aut : supervisors) {
        if (aut.getName().equals(supname)) {
          found = true;
          break;
        }
      }
      index++;
    } while (found);
    return supname;
  }

  private AutomatonProxy createSupervisorAutomaton
    (final ListBufferTransitionRelation rel)
  {
    rel.reconfigure(ListBufferTransitionRelation.CONFIG_SUCCESSORS);
    final EventProxy defaultMarking = getUsedDefaultMarking();
    final int defaultID = mTempEventEncoding.getEventCode(defaultMarking);
    rel.removeDeadlockStateTransitions(defaultID);
    final ProductDESProxyFactory factory = getFactory();
    rel.setKind(ComponentKind.SUPERVISOR);
    return rel.createAutomaton(factory, mTempEventEncoding);
  }

  private boolean isNewSupervisor(final AutomatonProxy aut,
                                  final Collection<AutomatonProxy> results)
    throws AnalysisException
  {
    final Logger logger = LogManager.getLogger();
    logger.debug("Comparing with existing supervisors ...");
    for (final AutomatonProxy existing : results) {
      if (mIsomorphismChecker.checkBisimulation(aut, existing)) {
        return false;
      }
    }
    return true;
  }

  private void recordSupervisor(final AutomatonProxy sup)
  {
    final CompositionalAutomataSynthesisResult result = getAnalysisResult();
    result.addBackRenamedSupervisor(sup);
  }

  private int getUsedEvent(final EventProxy event,
                           final EventEncoding enc,
                           final ListBufferTransitionRelation rel)
  {
    final int e = enc.getEventCode(event);
    if (e < 0) {
      return -1;
    } else {
      final byte status = rel.getProperEventStatus(e);
      return EventStatus.isUsedEvent(status) ? e : -1;
    }
  }


  //#########################################################################
  //# Debugging
  @Override
  void reportAbstractionResult(final AutomatonProxy aut,
                               final AutomatonProxy dist)
  {
    final Logger logger = LogManager.getLogger();
    if (logger.isDebugEnabled()) {
      /*
       * final boolean nonblocking = AnalysisTools.isNonBlocking(aut); final
       * String msg1 = "Simplified automaton is " + (nonblocking ?
       * "nonblocking." : "BLOCKING."); logger.debug(msg1);
       */
      if (dist != null) {
        final String msg2 =
          "Creating distinguisher '" + dist.getName() + "' with "
            + dist.getStates().size() + " states.";
        logger.debug(msg2);
      }
    }
  }

  @Override
  void reportSupervisor(final String kind,
                        final ListBufferTransitionRelation sup)
  {
    final Logger logger = LogManager.getLogger();
    if (logger.isDebugEnabled() && sup != null) {
      final String msg =
        "Got " + kind + " supervisor '" + sup.getName() + "' with " +
        sup.getNumberOfReachableStates() + " states and " +
        sup.getNumberOfTransitions() + " transitions.";
      logger.debug(msg);
    }
  }

  @SuppressWarnings("unused")
  private int getNumberOfDumpStates(final ListBufferTransitionRelation rel)
  {
    int numBadState = 0;
    for (int s = 0; s < rel.getNumberOfStates(); s++) {
      final TransitionIterator iter = rel.createSuccessorsReadOnlyIterator();
      iter.resetState(s);
      if (!iter.advance() & rel.isReachable(s) & rel.getAllMarkings(s) == 0) {
        numBadState++;
      }
    }
    return numBadState;
  }


  //#########################################################################
  //# Inner Class DistinguisherInfo
  /**
   * A record to hold information about distinguishers.
   * Contains a distinguisher automaton and a list of associated
   * event replacements.
   */
  private static class DistinguisherInfo
  {
    //#######################################################################
    //# Constructor
    private DistinguisherInfo(final AutomatonProxy distinguisher)
    {
      mDistinguisher = distinguisher;
      mReplacements = new LinkedList<DistinguisherReplacement>();
    }

    //#######################################################################
    //# Simple Access
    private AutomatonProxy getDistinguisher()
    {
      return mDistinguisher;
    }

    private List<DistinguisherReplacement> getReplacements()
    {
      return mReplacements;
    }

    private void addReplacement(final EventProxy original,
                                final List<EventProxy> replacements)
    {
      final DistinguisherReplacement pair =
        new DistinguisherReplacement(original, replacements);
      mReplacements.add(pair);
    }

    //#######################################################################
    //# Auxiliary Methods
    private boolean containsReplacedEvent(final Set<EventProxy> events)
    {
      for (final DistinguisherReplacement pair : mReplacements) {
        if (pair.containsReplacedEvent(events)) {
          return true;
        }
      }
      return false;
    }

    private boolean containsReplacedEvent(final EventEncoding enc)
    {
      for (final DistinguisherReplacement pair : mReplacements) {
        if (pair.containsReplacedEvent(enc)) {
          return true;
        }
      }
      return false;
    }

    private void addDeferredEvents(final Collection<EventProxy> deferredEvents)
    {
      deferredEvents.addAll(mDistinguisher.getEvents());
      for (final DistinguisherReplacement pair : mReplacements) {
        deferredEvents.add(pair.getOriginalEvent());
      }
    }

    //#######################################################################
    //# Debugging
    @Override
    public String toString()
    {
      final StringBuilder buffer = new StringBuilder();
      buffer.append(mDistinguisher.getName());
      for (final DistinguisherReplacement pair : mReplacements) {
        buffer.append("\n  [");
        boolean first = true;
        for (final EventProxy event : pair.getReplacedEvents()) {
          if (first) {
            first = false;
          } else {
            buffer.append(", ");
          }
          buffer.append(event.getName());
        }
        buffer.append("] -> ");
        final EventProxy original = pair.getOriginalEvent();
        buffer.append(original.getName());
      }
      return buffer.toString();
    }

    //#######################################################################
    //# Data Members
    private final AutomatonProxy mDistinguisher;
    private final List<DistinguisherReplacement> mReplacements;

  }


  //#########################################################################
  //# Inner Class DistinguisherReplacement
  /**
   * An event replacement of a distinguisher.
   * These objects are part of a {@link DistinguisherInfo}.
   */
  private static class DistinguisherReplacement
  {
    //#######################################################################
    //# Constructor
    private DistinguisherReplacement(final EventProxy original,
                                     final List<EventProxy> replacement)
    {
      mOriginalEvent = original;
      mReplacedEvents = replacement;
    }

    //#######################################################################
    //# Simple Access
    private EventProxy getOriginalEvent()
    {
      return mOriginalEvent;
    }

    private List<EventProxy> getReplacedEvents()
    {
      return mReplacedEvents;
    }

    //#######################################################################
    //# Auxiliary Methods
    private boolean containsReplacedEvent(final Set<EventProxy> events)
    {
      for (final EventProxy event : mReplacedEvents) {
        if (events.contains(event)) {
          return true;
        }
      }
      return false;
    }

    private boolean containsReplacedEvent(final EventEncoding enc)
    {
      for (final EventProxy event : mReplacedEvents) {
        final int e = enc.getEventCode(event);
        if (e >= 0) {
          final byte status = enc.getProperEventStatus(e);
          if (EventStatus.isUsedEvent(status)) {
            return true;
          }
        }
      }
      return false;
    }

    //#######################################################################
    //# Debugging
    @Override
    public String toString()
    {
      return mReplacedEvents.toString() + " -> " + mOriginalEvent.getName();
    }

    //#######################################################################
    //# Data Members
    private final EventProxy mOriginalEvent;
    private final List<EventProxy> mReplacedEvents;

  }


  //#########################################################################
  //# Inner Class SynthesisEventInfo
  /**
   * An event information record for compositional synthesis with renaming.
   * This is presently only used to implement failing events correctly,
   * so that an event is only considered failing if all its renamed instances
   * are failing. The renamed event information records the original event
   * an event is renamed to, and for the original event, maintains the
   * number of non-failing events it has been replaced by.
   */
  private final class RenamedEventInfo extends SynthesisEventInfo
  {
    //#######################################################################
    //# Constructor
    private RenamedEventInfo(final EventProxy event)
    {
      super(event);
      mOriginal = this;
      mNumNonFailingInstances = 1;
    }

    private RenamedEventInfo(final EventProxy event,
                             final RenamedEventInfo original)
    {
      super(event);
      mOriginal = original.mOriginal;
      mOriginal.mNumNonFailingInstances++;
    }

    //#######################################################################
    //# Event Status
    @Override
    protected boolean isFailing()
    {
      return super.isFailing() && mOriginal.mNumNonFailingInstances == 0;
    }

    @Override
    void addAutomaton(final AutomatonProxy aut, final byte status)
    {
      final boolean wasFailing = super.isFailing();
      super.addAutomaton(aut, status);
      if (!wasFailing && isFailing()) {
        mOriginal.mNumNonFailingInstances--;
      }
    }

    @Override
    void removeAutomata(final Collection<AutomatonProxy> victims)
    {
      super.removeAutomata(victims);
      if (isEmpty() && !isFailing()) {
        mOriginal.mNumNonFailingInstances--;
      }
    }

    //#######################################################################
    //# Instance Variable
    /**
     * The original (un-renamed) event this event maps back to.
     */
    private final RenamedEventInfo mOriginal;
    /**
     * The number of non-failing events replacing this event.
     * An event is considered as failing if its status has been set to
     * failing, and its original has no non-failing instances recorded.
     */
    private int mNumNonFailingInstances;
  }


  //#########################################################################
  //# Data Members
  private SupervisorReductionFactory mSupervisorReductionFactory =
    new ProjectingSupervisorReductionFactory();
  private boolean mSupervisorLocalizationEnabled = false;

  private HalfWaySynthesisTRSimplifier mHalfwaySimplifier;
  private TransitionRelationSimplifier mPreSupervisorReducer;
  private SupervisorReductionSimplifier mMainSupervisorReducer;
  private IsomorphismChecker mIsomorphismChecker;
  private List<DistinguisherInfo> mDistinguisherInfoList;
  /**
   * The current back-renaming map. Maps renamed events to their original
   * names. There are no entries for events that are not renamed.
   */
  private Map<EventProxy,EventProxy> mBackRenaming;
  /**
   * A temporary event encoding for supervisor back-renaming.
   */
  private EventEncoding mTempEventEncoding;


  //#########################################################################
  //# Class Constants
  private static final String UNREDUCED_SUPERVISOR =
    "Overflow during supervisor reduction - using unreduced supervisor.";


}
