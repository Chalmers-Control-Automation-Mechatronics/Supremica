//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# Copyright (C) 2004-2023 Robi Malik
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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.sourceforge.waters.analysis.abstraction.HalfWaySynthesisTRSimplifier;
import net.sourceforge.waters.analysis.abstraction.ProjectingSupervisorReductionFactory;
import net.sourceforge.waters.analysis.abstraction.SupervisorReductionFactory;
import net.sourceforge.waters.analysis.monolithic.MonolithicSynchronousProductBuilder;
import net.sourceforge.waters.analysis.tr.AbstractSynchronisationEncoding;
import net.sourceforge.waters.analysis.tr.EventEncoding;
import net.sourceforge.waters.analysis.tr.EventStatus;
import net.sourceforge.waters.analysis.tr.ListBufferTransitionRelation;
import net.sourceforge.waters.analysis.tr.StateEncoding;
import net.sourceforge.waters.analysis.tr.TRPartition;
import net.sourceforge.waters.analysis.tr.TransitionIterator;
import net.sourceforge.waters.model.analysis.AbortRequester;
import net.sourceforge.waters.model.analysis.AnalysisException;
import net.sourceforge.waters.model.analysis.OverflowException;
import net.sourceforge.waters.model.analysis.des.SynchronousProductBuilder;
import net.sourceforge.waters.model.analysis.des.SynchronousProductResult;
import net.sourceforge.waters.model.analysis.des.SynchronousProductStateMap;
import net.sourceforge.waters.model.analysis.kindtranslator.IdenticalKindTranslator;
import net.sourceforge.waters.model.analysis.kindtranslator.KindTranslator;
import net.sourceforge.waters.model.des.AutomatonProxy;
import net.sourceforge.waters.model.des.EventProxy;
import net.sourceforge.waters.model.des.ProductDESProxy;
import net.sourceforge.waters.model.des.ProductDESProxyFactory;
import net.sourceforge.waters.model.des.StateProxy;

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

public class CompositionalStateRepresentationSynthesizer extends
  AbstractCompositionalSynthesizer
{

  //#########################################################################
  //# Constructors
  /**
   * Creates a compositional synthesiser without a model.
   *
   * @param factory
   *          Factory used for trace construction.
   */
  public CompositionalStateRepresentationSynthesizer(final ProductDESProxyFactory factory)
  {
    this(factory, StateRepresentationSynthesisAbstractionProcedureFactory.WSOE);
  }

  /**
   * Creates a compositional synthesiser without a model.
   *
   * @param factory
   *          Factory used for trace construction.
   * @param abstractionCreator
   *          Factory to define the abstraction sequence to be used.
   */
  public CompositionalStateRepresentationSynthesizer
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
   * @param wsoe
   *          Factory to define the abstraction sequence to be used.
   */
  public CompositionalStateRepresentationSynthesizer
    (final ProductDESProxyFactory factory,
     final KindTranslator translator,
     final AbstractionProcedureCreator wsoe)
  {
    this(null, factory, translator, wsoe);
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
  public CompositionalStateRepresentationSynthesizer
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
  public CompositionalStateRepresentationSynthesizer
    (final ProductDESProxy model,
     final ProductDESProxyFactory factory,
     final KindTranslator translator,
     final AbstractionProcedureCreator abstractionCreator)
  {
    this(model, factory, translator, abstractionCreator, new PreselectingMethodFactory());
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
  public CompositionalStateRepresentationSynthesizer
    (final ProductDESProxy model,
     final ProductDESProxyFactory factory,
     final KindTranslator translator,
     final AbstractionProcedureCreator abstractionCreator,
     final PreselectingMethodFactory preselectingMethodFactory)
  {
    super(model, factory, translator, abstractionCreator,
          preselectingMethodFactory);
  }


  //#########################################################################
  //# Configuration
  @Override
  public StateRepresentationSynthesisAbstractionProcedureFactory
    getAbstractionProcedureFactory()
  {
    return StateRepresentationSynthesisAbstractionProcedureFactory.getInstance();
  }


  //#########################################################################
  //# Interface net.sourceforge.waters.model.analysis.Abortable
  @Override
  public void requestAbort(final AbortRequester sender)
  {
    super.requestAbort(sender);
    if (mHalfwaySimplifier != null) {
      mHalfwaySimplifier.requestAbort(sender);
    }
  }

  @Override
  public void resetAbort()
  {
    super.resetAbort();
    if (mHalfwaySimplifier != null) {
      mHalfwaySimplifier.resetAbort();
    }
  }


  //#########################################################################
  //# Interface net.sourceforge.waters.model.analysis.SupervisorSynthesizer
  @Override
  public void setSupervisorReductionFactory(final SupervisorReductionFactory factory)
  {
  }

  @Override
  public SupervisorReductionFactory getSupervisorReductionFactory()
  {
    return new ProjectingSupervisorReductionFactory();
  }

  @Override
  public void setSupervisorLocalizationEnabled(final boolean enable)
  {
  }

  @Override
  public boolean isSupervisorLocalizationEnabled()
  {
    return false;
  }


  //#########################################################################
  //# Invocation
  @Override
  public boolean run() throws AnalysisException
  {
    try {
      setUp();
      final CompositionalStateRepresentationSynthesisResult result =
        getAnalysisResult();
      if (!result.isFinished()) {
        runCompositionalMinimisation();
      }
      if (!result.isFinished()) {
        result.setSatisfied(true);
      }
      if (result.isSatisfied() && isDetailedOutputEnabled()) {
        result.addSynthesisStateSpace(mSynthesisStateSpace);
        final ProductDESProxyFactory factory = getFactory();
        final Collection<EventProxy> events = getModel().getEvents();
        result.close(factory, events, getOutputName());
      }
      final Logger logger = LogManager.getLogger();
      logger.debug("CompositionalSynthesizer done.");
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
  public CompositionalStateRepresentationSynthesisResult createAnalysisResult()
  {
    return new CompositionalStateRepresentationSynthesisResult(this);
  }

  @Override
  public CompositionalStateRepresentationSynthesisResult getAnalysisResult()
  {
    return (CompositionalStateRepresentationSynthesisResult) super.getAnalysisResult();
  }

  @Override
  protected void setUp() throws AnalysisException
  {
    mHalfwaySimplifier = new HalfWaySynthesisTRSimplifier();
    mHalfwaySimplifier.setOutputMode
      (HalfWaySynthesisTRSimplifier.OutputMode.PROPER_SUPERVISOR);
    final KindTranslator translator = getKindTranslator();
    final Set<AutomatonProxy> automata = getModel().getAutomata();
    mStateRepresentationMap = new HashMap<>(automata.size());
    mSynthesisStateSpace =
      new SynthesisStateSpace(getFactory(), getKindTranslator(),
                              getModel(), getOutputName());
    for (final AutomatonProxy automaton: automata) {
      switch (translator.getComponentKind(automaton)) {
      case PLANT:
      case SPEC:
        final StateEncoding encoding = new StateEncoding(automaton);
        final SynthesisStateSpace.SynthesisStateMap map =
          mSynthesisStateSpace.createStateEncodingMap(automaton, encoding);
        mStateRepresentationMap.put(automaton, map);
        break;
      default:
        break;
      }
    }
    super.setUp();
  }

  @Override
  protected void tearDown()
  {
    super.tearDown();
    mHalfwaySimplifier = null;
  }


  //#########################################################################
  //# Hooks
  @Override
  protected AutomatonProxy plantify(final AutomatonProxy spec)
    throws OverflowException
  {
    final AutomatonProxy plant = super.plantify(spec);
    final SynthesisStateSpace.SynthesisStateMap map =
      mStateRepresentationMap.remove(spec);
    mStateRepresentationMap.put(plant, map);
    return plant;
  }

  @Override
  protected void recordAbstractionStep(final AbstractionStep step)
    throws AnalysisException
  {
    if (step instanceof MergeStep) {
      final MergeStep merge = (MergeStep) step;
      final AutomatonProxy original = step.getOriginalAutomaton();
      final AutomatonProxy result = step.getResultAutomaton();
      if (result.getStates().isEmpty()) {
        setBooleanResult(false);
        return;
      }

      final SynthesisStateSpace.SynthesisStateMap parent =
        mStateRepresentationMap.remove(original);
      final TRPartition partition = merge.getPartition();
      if (partition == null) {
        mStateRepresentationMap.put(result, parent);
      } else {
        final StateEncoding resultEncoding = merge.getResultStateEncoding();
        final StateEncoding defaultEncoding = new StateEncoding(result);
        final TRPartition reencoding =
          TRPartition.createReencodingPartition(resultEncoding, defaultEncoding);
        final TRPartition combinedPartition =
          TRPartition.combine(partition, reencoding);
        final SynthesisStateSpace.SynthesisStateMap map =
          parent.compose(combinedPartition);
        mStateRepresentationMap.put(result, map);
      }
      return;
    } else if (step instanceof HidingStep) { // synchronous product
      final List<AutomatonProxy> originals = step.getOriginalAutomata();
      if (originals.size() >= 1) {
        final HidingStep hide = (HidingStep) step;
        final AutomatonProxy result = step.getResultAutomaton();
        final List<SynthesisStateSpace.SynthesisStateMap> parents =
          new ArrayList<SynthesisStateSpace.SynthesisStateMap>(originals.size());
        final int[] stateCounts = new int[originals.size()];
        final StateEncoding[] originalStateEncodings = new StateEncoding[originals.size()];
        int a = 0;
        for (final AutomatonProxy automaton : originals) {
          final SynthesisStateSpace.SynthesisStateMap parent =
            mStateRepresentationMap.remove(automaton);
          parents.add(parent);
          originalStateEncodings[a] = new StateEncoding(automaton);
          stateCounts[a] = originalStateEncodings[a].getNumberOfStates();
          a++;
        }
        final Set<StateProxy> synchStates = result.getStates();
        final AbstractSynchronisationEncoding synchEncoding =
          AbstractSynchronisationEncoding.createEncoding(stateCounts, synchStates.size());
        final StateEncoding synchStateEncoding = new StateEncoding(result);
        final int[] tuple = new int[originals.size()];
        states:
        for (final StateProxy synchState : synchStates) {
          a = 0;
          for (final AutomatonProxy automaton : originals) {
            final StateProxy originalState =
              hide.getOriginalState(synchState, automaton);
            if (originalState == null) {  // dumpstate
              continue states;
            }
            final int stateCode = originalStateEncodings[a].getStateCode(originalState);
            assert stateCode >= 0;
            tuple[a] = stateCode;
            a++;
          }
          final int synchStateCode = synchStateEncoding.getStateCode(synchState);
          synchEncoding.addState(tuple, synchStateCode);
        }
        final SynthesisStateSpace.SynthesisStateMap map =
          mSynthesisStateSpace.createSynchronisationMap(synchEncoding, parents);
        mStateRepresentationMap.put(result, map);
        return;
      }
    }

    final List<AutomatonProxy> originals = step.getOriginalAutomata();
    final List<AutomatonProxy> results = step.getResultAutomata();
    final Map<StateProxy,StateProxy> stateMap;
    if (step instanceof EventRemovalStep) {
      final EventRemovalStep removalStep = (EventRemovalStep) step;
      stateMap = removalStep.getStateMap();
    } else {
      stateMap = Collections.emptyMap();
    }
    for (int i = 0; i < originals.size(); i++) {
      final AutomatonProxy original = originals.get(i);
      final AutomatonProxy result = results.get(i);
      final StateEncoding originalEncoding = new StateEncoding(original);
      final StateEncoding resultEncoding = new StateEncoding(result);
      final TRPartition reencoding =
        TRPartition.createReencodingPartition
          (originalEncoding, resultEncoding, stateMap);
      final SynthesisStateSpace.SynthesisStateMap parent =
        mStateRepresentationMap.remove(original);
      if (reencoding == null) {
        mStateRepresentationMap.put(result, parent);
      } else {
        final SynthesisStateSpace.SynthesisStateMap map =
          parent.compose(reencoding);
        mStateRepresentationMap.put(result, map);
      }
    }
  }

  @Override
  protected HidingStep createSynchronousProductStep
    (final Collection<AutomatonProxy> automata,
     final AutomatonProxy sync,
     final Collection<EventProxy> hidden,
     final EventProxy tau)
  {
    final SynchronousProductBuilder builder = getSynchronousProductBuilder();
    final SynchronousProductResult result = builder.getAnalysisResult();
    final SynchronousProductStateMap stateMap = result.getStateMap();
    return new HidingStep(this, sync, hidden, tau, stateMap);
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
      syncBuilder.setModel(des);
      final int slimit = getMonolithicStateLimit();
      syncBuilder.setNodeLimit(slimit);
      final int tlimit = getMonolithicTransitionLimit();
      syncBuilder.setTransitionLimit(tlimit);
      syncBuilder.setDetailedOutputEnabled(true);
      syncBuilder.setPropositions(getPropositions());
      syncBuilder.run();
      automaton = syncBuilder.getComputedAutomaton();
      final Collection<EventProxy> localEvents = Collections.emptyList();
      final SynchronousProductResult result = syncBuilder.getAnalysisResult();
      final SynchronousProductStateMap stateMap = result.getStateMap();
      final HidingStep step =
        new HidingStep(this, automaton, localEvents, null, stateMap);
      recordAbstractionStep(step);
      break;
    }
    final CompositionalStateRepresentationSynthesisResult result = getAnalysisResult();
    result.addSynchSize(automaton);
    final EventEncoding encoding = createSynthesisEventEncoding(automaton);
    final boolean finalResult = synthesise(automaton, encoding);
    mStateRepresentationMap.remove(automaton);
    return finalResult;
  }


  //#########################################################################
  //# Synthesis
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

  private boolean synthesise
    (final AutomatonProxy aut, final EventEncoding eventEnc)
    throws AnalysisException
  {
    final SynthesisStateSpace.SynthesisStateMap parent =
      mStateRepresentationMap.get(aut);
    final ListBufferTransitionRelation rel =
      new ListBufferTransitionRelation(aut,
                                       eventEnc,
                                       ListBufferTransitionRelation.CONFIG_PREDECESSORS);
    mHalfwaySimplifier.setTransitionRelation(rel);
    final EventProxy defaultMarking = getUsedDefaultMarking();
    final int defaultID = eventEnc.getEventCode(defaultMarking);
    if (defaultID < 0) {
      mSynthesisStateSpace.addStateMap(parent);
      return true;
    }
    mHalfwaySimplifier.setDefaultMarkingID(defaultID);
    mHalfwaySimplifier.run();
    final TRPartition partition = mHalfwaySimplifier.getResultPartition();
    if (partition == null) {
      mSynthesisStateSpace.addStateMap(parent);
      return true;
    } else if (partition.isEmpty()) {
      setBooleanResult(false);
      return false;
    } else {
      final SynthesisStateSpace.SynthesisStateMap map =
        parent.compose(partition);
      mSynthesisStateSpace.addStateMap(map);
      return true;
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
        "Got " + kind + " supervisor '" + sup.getName() + "' with "
          + sup.getNumberOfReachableStates() + " states.";
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
  //# Data Members
  private HalfWaySynthesisTRSimplifier mHalfwaySimplifier;
  private Map<AutomatonProxy,SynthesisStateSpace.SynthesisStateMap> mStateRepresentationMap;
  private SynthesisStateSpace mSynthesisStateSpace;
}
