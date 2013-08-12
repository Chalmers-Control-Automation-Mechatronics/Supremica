//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters Analysis
//# PACKAGE: net.sourceforge.waters.analysis.compositional
//# CLASS:   CompositionalSynthesizer
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.analysis.compositional;

import gnu.trove.iterator.TObjectByteIterator;
import gnu.trove.set.hash.THashSet;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Set;

import net.sourceforge.waters.analysis.abstraction.HalfWaySynthesisTRSimplifier;
import net.sourceforge.waters.analysis.abstraction.SupervisorReductionTRSimplifier;
import net.sourceforge.waters.analysis.monolithic.MonolithicSynchronousProductBuilder;
import net.sourceforge.waters.analysis.tr.EventEncoding;
import net.sourceforge.waters.analysis.tr.ListBufferTransitionRelation;
import net.sourceforge.waters.analysis.tr.StateEncoding;
import net.sourceforge.waters.analysis.tr.TransitionIterator;
import net.sourceforge.waters.model.analysis.AnalysisAbortException;
import net.sourceforge.waters.model.analysis.AnalysisException;
import net.sourceforge.waters.model.analysis.IdenticalKindTranslator;
import net.sourceforge.waters.model.analysis.KindTranslator;
import net.sourceforge.waters.model.analysis.OverflowException;
import net.sourceforge.waters.model.analysis.des.ProductDESResult;
import net.sourceforge.waters.model.analysis.des.SupervisorSynthesizer;
import net.sourceforge.waters.model.analysis.des.SupervisorTooBigException;
import net.sourceforge.waters.model.des.AutomatonProxy;
import net.sourceforge.waters.model.des.EventProxy;
import net.sourceforge.waters.model.des.ProductDESProxy;
import net.sourceforge.waters.model.des.ProductDESProxyFactory;
import net.sourceforge.waters.model.des.StateProxy;
import net.sourceforge.waters.model.des.TransitionProxy;
import net.sourceforge.waters.xsd.base.ComponentKind;
import net.sourceforge.waters.xsd.base.EventKind;

import org.apache.log4j.Logger;


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

public class CompositionalSynthesizer extends
  AbstractCompositionalModelAnalyzer implements SupervisorSynthesizer
{

  //#########################################################################
  //# Constructors
  /**
   * Creates a compositional synthesiser without a model.
   *
   * @param factory
   *          Factory used for trace construction.
   */
  public CompositionalSynthesizer(final ProductDESProxyFactory factory)
  {
    this(factory, SynthesisAbstractionProcedureFactory.WSOE);
  }

  /**
   * Creates a compositional synthesiser without a model.
   *
   * @param factory
   *          Factory used for trace construction.
   * @param abstractionFactory
   *          Factory to define the abstraction sequence to be used.
   */
  public CompositionalSynthesizer(final ProductDESProxyFactory factory,
                                  final SynthesisAbstractionProcedureFactory abstractionFactory)
  {
    this(factory, IdenticalKindTranslator.getInstance(), abstractionFactory);
  }

  /**
   * Creates a compositional synthesiser without a model.
   *
   * @param factory
   *          Factory used for trace construction.
   * @param translator
   *          Kind translator used to determine event and component kinds.
   * @param abstractionFactory
   *          Factory to define the abstraction sequence to be used.
   */
  public CompositionalSynthesizer(final ProductDESProxyFactory factory,
                                  final KindTranslator translator,
                                  final SynthesisAbstractionProcedureFactory abstractionFactory)
  {
    this(null, factory, translator, abstractionFactory);
  }

  /**
   * Creates a compositional synthesiser without a model.
   *
   * @param factory
   *          Factory used for trace construction.
   * @param translator
   *          Kind translator used to determine event and component kinds.
   * @param abstractionFactory
   *          Factory to define the abstraction sequence to be used.
   * @param preselectingMethodFactory
   *          Enumeration factory that determines possible candidate
   *          preselection methods.
   * @param selectingMethodFactory
   *          Enumeration factory that determines possible candidate selection
   *          methods.
   */
  public CompositionalSynthesizer(final ProductDESProxyFactory factory,
                                  final KindTranslator translator,
                                  final SynthesisAbstractionProcedureFactory abstractionFactory,
                                  final PreselectingMethodFactory preselectingMethodFactory,
                                  final SelectingMethodFactory selectingMethodFactory)
  {
    this(null, factory, translator, abstractionFactory,
         preselectingMethodFactory, selectingMethodFactory);
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
   * @param abstractionFactory
   *          Factory to define the abstraction sequence to be used.
   */
  public CompositionalSynthesizer(final ProductDESProxy model,
                                  final ProductDESProxyFactory factory,
                                  final KindTranslator translator,
                                  final SynthesisAbstractionProcedureFactory abstractionFactory)
  {
    this(model, factory, translator, abstractionFactory,
         new PreselectingMethodFactory(), new SelectingMethodFactory());
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
   * @param abstractionFactory
   *          Factory to define the abstraction sequence to be used.
   * @param preselectingMethodFactory
   *          Enumeration factory that determines possible candidate
   *          preselection methods.
   * @param selectingMethodFactory
   *          Enumeration factory that determines possible candidate selection
   *          methods.
   */
  public CompositionalSynthesizer(final ProductDESProxy model,
                                  final ProductDESProxyFactory factory,
                                  final KindTranslator translator,
                                  final SynthesisAbstractionProcedureFactory abstractionFactory,
                                  final PreselectingMethodFactory preselectingMethodFactory,
                                  final SelectingMethodFactory selectingMethodFactory)
  {
    super(model, factory, translator, abstractionFactory,
          preselectingMethodFactory, selectingMethodFactory);
    setPruningDeadlocks(true);
  }

  //#########################################################################
  //# Interface net.sourceforge.waters.model.analysis.ModelBuilder
  @Override
  public void setOutputName(final String name)
  {
    mOutputName = name;
  }

  @Override
  public String getOutputName()
  {
    return mOutputName;
  }

  @Override
  public void setConstructsResult(final boolean construct)
  {
    mConstructsResult = construct;
  }

  @Override
  public boolean getConstructsResult()
  {
    return mConstructsResult;
  }

  @Override
  public ProductDESProxy getComputedProxy()
  {
    final ProductDESResult result = getAnalysisResult();
    return result.getComputedProductDES();
  }

  //#########################################################################
  //# Interface net.sourceforge.waters.model.analysis.ProductDESBuilder
  @Override
  public ProductDESProxy getComputedProductDES()
  {
    return getComputedProxy();
  }

  //#########################################################################
  //# Interface net.sourceforge.waters.model.analysis.Abortable
  @Override
  public void requestAbort()
  {
    super.requestAbort();
    if (mSupervisorSimplifier != null) {
      mSupervisorSimplifier.requestAbort();
    }
    if (mHalfwaySimplifier != null) {
      mHalfwaySimplifier.requestAbort();
    }
  }

  //#########################################################################
  //# Configuration
  public void setSupervisorReductionEnabled(final boolean enable)
  {
    mSupervisorReductionEnabled = enable;
  }

  public boolean getSupervisorReductionEnabled()
  {
    return mSupervisorReductionEnabled;
  }

  //#########################################################################
  //# Invocation
  @Override
  public boolean run() throws AnalysisException
  {
    try {
      setUp();
      final CompositionalSynthesisResult result = getAnalysisResult();
      if (!result.isFinished()) {
        runCompositionalMinimisation();
      }
      if (!result.isFinished()) {
        result.setSatisfied(true);
        if (mConstructsResult) {
          final ProductDESProxyFactory factory = getFactory();
          result.close(factory, mOutputName);
        }
      }
      final Logger logger = getLogger();
      logger.debug("CompositionalSynthesizer done.");
      result.setRenamingIsUsed(mDistinguisherInfoList.size());
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
  protected void setUp() throws AnalysisException
  {
    final EventProxy defaultMarking = createDefaultMarking();
    setPropositionsForMarkings(defaultMarking, null);
    mDistinguisherInfoList = new LinkedList<DistinguisherInfo>();
    mBackRenaming = new HashMap<EventProxy,EventProxy>();
    mSupervisorSimplifier = new SupervisorReductionTRSimplifier();
    mHalfwaySimplifier = new HalfWaySynthesisTRSimplifier();
    mHalfwaySimplifier.setOutputMode
      (HalfWaySynthesisTRSimplifier.OutputMode.PSEUDO_SUPERVISOR);
    super.setUp();
  }

  @Override
  protected void tearDown()
  {
    super.tearDown();
    mDistinguisherInfoList = null;
    mBackRenaming = null;
    mSupervisorSimplifier = null;
    mHalfwaySimplifier = null;
  }

  @Override
  protected CompositionalSynthesisResult createAnalysisResult()
  {
    return new CompositionalSynthesisResult();
  }

  @Override
  public CompositionalSynthesisResult getAnalysisResult()
  {
    return (CompositionalSynthesisResult) super.getAnalysisResult();
  }

  //#########################################################################
  //# Hooks
  @Override
  protected AutomatonProxy plantify(final AutomatonProxy spec)
    throws OverflowException
  {
    final KindTranslator translator = getKindTranslator();
    final Collection<EventProxy> events = spec.getEvents();
    final int numEvents = events.size();
    final Collection<EventProxy> uncontrollables =
      new ArrayList<EventProxy>(numEvents);
    for (final EventProxy event : events) {
      if (translator.getEventKind(event) == EventKind.UNCONTROLLABLE) {
        uncontrollables.add(event);
      }
    }
    final EventEncoding eventEnc =
      new EventEncoding(uncontrollables, translator);
    final StateEncoding stateEnc = new StateEncoding(spec);
    final ListBufferTransitionRelation rel =
      new ListBufferTransitionRelation(
                                       spec,
                                       eventEnc,
                                       stateEnc,
                                       ListBufferTransitionRelation.CONFIG_SUCCESSORS);
    final int numStates = rel.getNumberOfStates();
    final Collection<StateProxy> states =
      new ArrayList<StateProxy>(numStates + 1);
    states.addAll(spec.getStates());
    StateProxy dump = null;
    final Collection<TransitionProxy> transitions =
      new ArrayList<TransitionProxy>(spec.getTransitions());
    final TransitionIterator iter = rel.createSuccessorsReadOnlyIterator();
    final ProductDESProxyFactory factory = getFactory();
    for (int s = 0; s < numStates; s++) {
      final StateProxy state = stateEnc.getState(s);
      for (final EventProxy event : uncontrollables) {
        final int e = eventEnc.getEventCode(event);
        iter.reset(s, e);
        if (!iter.advance()) {
          if (dump == null) {
            dump = factory.createStateProxy(":dump");
            states.add(dump);
          }
          final TransitionProxy trans =
            factory.createTransitionProxy(state, event, dump);
          transitions.add(trans);
        }
      }
    }

    final String name = spec.getName();
    final EventProxy defaultMarking = getUsedDefaultMarking();
    if (dump != null & !events.contains(defaultMarking)) {
      final Collection<TransitionProxy> newTransitions =
        new ArrayList<TransitionProxy>();
      final Collection<EventProxy> newEvents =
        new ArrayList<EventProxy>(numEvents + 1);
      newEvents.addAll(events);
      newEvents.add(defaultMarking);
      final Collection<StateProxy> newStates =
        new ArrayList<StateProxy>(numStates + 1);
      final HashMap<StateProxy,StateProxy> mapStates =
        new HashMap<StateProxy,StateProxy>(numStates + 1);
      for (final StateProxy state : spec.getStates()) {
        final Collection<EventProxy> propositions = state.getPropositions();
        final Collection<EventProxy> newPropostions =
          new ArrayList<EventProxy>(propositions.size() + 1);
        newPropostions.addAll(propositions);
        newPropostions.add(defaultMarking);
        final StateProxy newState =
          factory.createStateProxy(state.getName(), state.isInitial(),
                                   newPropostions);
        newStates.add(newState);
        mapStates.put(state, newState);
      }
      newStates.add(dump);
      mapStates.put(dump, dump);
      for (final TransitionProxy trans : transitions) {
        final StateProxy sourceState = trans.getSource();
        final StateProxy targetState = trans.getTarget();
        final EventProxy event = trans.getEvent();
        final TransitionProxy newTransition =
          factory.createTransitionProxy(mapStates.get(sourceState), event,
                                        mapStates.get(targetState));
        newTransitions.add(newTransition);
      }
      return factory.createAutomatonProxy(name, ComponentKind.PLANT,
                                          newEvents, newStates,
                                          newTransitions);
    } else {
      return factory.createAutomatonProxy(name, ComponentKind.PLANT, events,
                                          states, transitions);
    }
  }

  @Override
  protected SynthesisEventInfo createEventInfo(final EventProxy event)
  {
    return new SynthesisEventInfo(event);
  }

  @Override
  protected boolean isPermissibleCandidate(final List<AutomatonProxy> automata)
  {
    return super.isPermissibleCandidate(automata)
           && automata.size() < getCurrentAutomata().size();
  }

  @Override
  protected void recordAbstractionStep(final AbstractionStep step)
    throws AnalysisException
  {
    final CompositionalSynthesisResult result = getAnalysisResult();
    final ProductDESProxyFactory factory = getFactory();
    if (step instanceof SynthesisAbstractionStep) {
      final SynthesisAbstractionStep synStep =
        (SynthesisAbstractionStep) step;
      mTempEventEncoding = synStep.getEventEncoding();
      final ListBufferTransitionRelation supervisor = synStep.getSupervisor();
      if (supervisor != null) {
        if (supervisor.isEmpty()) {
          result.setSatisfied(false);
          return;
        } else {
          result.addUnrenamedSupervisor(supervisor);
          final AutomatonProxy newSupervisor = createSupervisor(supervisor);
          result.addBackRenamedSupervisor(newSupervisor);
        }
      }

      // Apply inverse renaming to other automata
      final Map<EventProxy,List<EventProxy>> renaming = synStep.getRenaming();
      if (renaming != null) {
        final AutomatonProxy originalAut = synStep.getOriginalAutomaton();
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
      final Logger logger = getLogger();
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
      syncBuilder.setConstructsResult(true);
      syncBuilder.setPropositions(getPropositions());
      syncBuilder.run();
      automaton = syncBuilder.getComputedAutomaton();
      break;
    }
    mTempEventEncoding = createSynthesisEventEncoding(automaton);
    final ListBufferTransitionRelation supervisor =
      synthesise(automaton, mTempEventEncoding);
    if (supervisor != null) {
      reportSupervisor("monolithic", supervisor);
      final CompositionalSynthesisResult result = getAnalysisResult();
      result.addSynchSize(automaton.getStates().size());
      if (supervisor.getNumberOfReachableStates() == 0) {
        result.setSatisfied(false);
        return false;
      } else {
        result.addUnrenamedSupervisor(supervisor);
        final AutomatonProxy renamedSup = createSupervisor(supervisor);
        result.addBackRenamedSupervisor(renamedSup);
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

  void recordDistinuisherInfo(final Map<EventProxy,List<EventProxy>> renaming,
                              final AutomatonProxy distinguisherAutomaton)
  {
    for (final Map.Entry<EventProxy,List<EventProxy>> entry :
         renaming.entrySet()) {
      final EventProxy event = entry.getKey();
      final List<EventProxy> replacements = entry.getValue();
      final DistinguisherInfo info =
        new DistinguisherInfo(event, replacements, distinguisherAutomaton);
      mDistinguisherInfoList.add(info);
      final EventProxy backRenamed = getOriginalEvent(event);
      for (final EventProxy replacement : replacements) {
        mBackRenaming.put(replacement, backRenamed);
      }
    }
  }

  private EventEncoding createSynthesisEventEncoding(final AutomatonProxy aut)
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
      encoding.setProperEventStatus(e, status | EventEncoding.STATUS_LOCAL);
    }
    encoding.sortProperEvents((byte) ~EventEncoding.STATUS_LOCAL,
                              EventEncoding.STATUS_CONTROLLABLE);
    return encoding;
  }

  private ListBufferTransitionRelation synthesise(final AutomatonProxy aut,
                                                  final EventEncoding eventEnc)
    throws AnalysisException
  {
    final ListBufferTransitionRelation rel =
      new ListBufferTransitionRelation(aut,
                                       eventEnc,
                                       ListBufferTransitionRelation.CONFIG_PREDECESSORS);
    final int numStates = rel.getNumberOfStates();
    mHalfwaySimplifier.setTransitionRelation(rel);
    final EventProxy defaultMarking = getUsedDefaultMarking();
    final int defaultID = eventEnc.getEventCode(defaultMarking);
    if (defaultID < 0) {
      return null;
    }
    mHalfwaySimplifier.setDefaultMarkingID(defaultID);
    mHalfwaySimplifier.run();
    final ListBufferTransitionRelation supervisor =
      mHalfwaySimplifier.getTransitionRelation();
    final List<int[]> partition = mHalfwaySimplifier.getResultPartition();
    if (partition == null) {
      return null;
    } else if (partition.isEmpty()) {
      return supervisor;
    }
    final BitSet safeStates =
      SynthesisAbstractionProcedure.getSafeStates(partition, numStates);
    final TransitionIterator iter =
      supervisor.createAllTransitionsReadOnlyIterator();
    while (iter.advance()) {
      final int t = iter.getCurrentTargetState();
      if (!safeStates.get(t)) {
        final int e = iter.getCurrentEvent();
        final byte status = rel.getProperEventStatus(e);
        if (EventEncoding.isControllableEvent(status)) {
          return supervisor;
        }
      }
    }
    return null;
  }


  private ListBufferTransitionRelation reduceSupervisor(final ListBufferTransitionRelation rel)
    throws AnalysisException
  {
    if (mSupervisorReductionEnabled) {
      try {
        mSupervisorSimplifier.setTransitionRelation(rel);//set TR
        mSupervisorSimplifier.setEvent(-1);//set event
        mSupervisorSimplifier.setBadStateIndex();//set bad state
        mSupervisorSimplifier.run();
        return mSupervisorSimplifier.getTransitionRelation();
      } catch (final OverflowException overflow) {
        // If supervisor reduction fails, just use an unreduced supervisor.
        return rel;
      }
    } else {
      return rel;
    }
  }

  private AutomatonProxy createSupervisor(ListBufferTransitionRelation rel)
    throws AnalysisException
  {
    final String name = getUniqueSupervisorName(rel);
    boolean reduced = false;
    List<DistinguisherInfo> distinguisherList =
      new LinkedList<DistinguisherInfo>(mDistinguisherInfoList);
    while (!distinguisherList.isEmpty()) {
      final ListIterator<DistinguisherInfo> listIter =
        distinguisherList.listIterator(distinguisherList.size());
      final List<DistinguisherInfo> deferredDistinguishers =
        new LinkedList<DistinguisherInfo>();
      final Set<EventProxy> deferredEvents = new THashSet<EventProxy>();
      boolean renamed = false;
      while (listIter.hasPrevious()) {
        final List<DistinguisherInfo> groupInfo =
          new LinkedList<DistinguisherInfo>();
        boolean isDeterministicGroup =
          isDeterministicGroup(rel, listIter, groupInfo, deferredEvents);
        if (isDeterministicGroup) {
          outer:
          for (final DistinguisherInfo info : groupInfo) {
            for (final EventProxy event : info.getReplacement()) {
              if (deferredEvents.contains(event)) {
                isDeterministicGroup = false;
                break outer;
              }
            }
          }
        }
        if (!groupInfo.isEmpty()) {
          if (isDeterministicGroup) {
            rel = createRenamedSupervisor(rel, groupInfo);
            renamed = true;
          } else {
            AutomatonProxy dist = null;
            final ListIterator<DistinguisherInfo> iter =
              groupInfo.listIterator(groupInfo.size());
            while (iter.hasPrevious()) {
              final DistinguisherInfo info = iter.previous();
              deferredDistinguishers.add(0, info);
              deferredEvents.add(info.getOriginalEvent());
              dist = info.getDistinguisher();
            }
            deferredEvents.addAll(dist.getEvents());
          }
        }
      }
      if (renamed && mReduceIncrementally) {
        rel = reduceSupervisor(rel);
        reduced = true;
      } else if (!deferredDistinguishers.isEmpty()) {
        final ListIterator<DistinguisherInfo> iter =
          deferredDistinguishers.listIterator(deferredDistinguishers.size());
        final List<DistinguisherInfo> list =
          new LinkedList<DistinguisherInfo>();
        AutomatonProxy distinguisher = null;
        while (iter.hasPrevious()) {
          final DistinguisherInfo info = iter.previous();
          if (distinguisher == null) {
            distinguisher = info.getDistinguisher();
          } else if (distinguisher != info.getDistinguisher()) {
            break;
          }
          list.add(0, info);
          iter.remove();
        }
        rel = createSynchronizedSupervisor(rel, list);
        reduced = false;
      }
      distinguisherList = deferredDistinguishers;
    }
    if (!reduced) {
      rel = reduceSupervisor(rel);
    }
    final EventProxy defaultMarking = getUsedDefaultMarking();
    final int defaultID = mTempEventEncoding.getEventCode(defaultMarking);
    rel.removeDeadlockStateTransitions(defaultID);
    final ProductDESProxyFactory factory = getFactory();
    rel.setName(name);
    rel.setKind(ComponentKind.SUPERVISOR);
    return rel.createAutomaton(factory, mTempEventEncoding);
  }

  private boolean isDeterministicGroup(final ListBufferTransitionRelation rel,
                                       final ListIterator<DistinguisherInfo> listIter,
                                       final List<DistinguisherInfo> groupInfo,
                                       final Set<EventProxy> deferredEvents)
    throws AnalysisAbortException, OverflowException
  {
    boolean foundNondeterminism = false;
    AutomatonProxy distinguisher = null;
    final int numOfStates = rel.getNumberOfStates();
    final TransitionIterator transitionIter =
      rel.createSuccessorsReadOnlyIterator();
    while (listIter.hasPrevious()) {
      checkAbort();
      final DistinguisherInfo info = listIter.previous();
      if (distinguisher == null) {
        distinguisher = info.getDistinguisher();
      } else if (distinguisher != info.getDistinguisher()) {
        listIter.next();
        break;
      }
      final Collection<EventProxy> replacement = info.getReplacement();
      boolean found = false;
      for (final EventProxy event : replacement) {
        if (deferredEvents.contains(event)) {
          foundNondeterminism = true;
          found = true;
        }
        if (mTempEventEncoding.getEventCode(event) >= 0) {
          found = true;
        }
      }
      if (!found) {
        continue;
      }
      groupInfo.add(0, info);
      if (!foundNondeterminism) {
        outer: for (int state = 0; state < numOfStates; state++) {
          int foundSuccessor = -1;
          for (final EventProxy event : replacement) {
            final int code = mTempEventEncoding.getEventCode(event);
            final int successor;
            if (code < 0) {
              // not in alphabet - selflooped in all states
              successor = state;
            } else {
              transitionIter.reset(state, code);
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
              foundNondeterminism = true;
              break outer;
            }
          }
        }
      }
    }
    return !foundNondeterminism;
  }

  private ListBufferTransitionRelation createRenamedSupervisor(final ListBufferTransitionRelation rel,
                                                               final List<DistinguisherInfo> renamings)
    throws AnalysisException
  {
    List<EventProxy> events = null;
    for (final DistinguisherInfo info : renamings) {
      final EventProxy original = info.getOriginalEvent();
      final List<EventProxy> replacement = info.getReplacement();
      // If an event is not in the encoding, and there is no
      // nondeterminism, these events will be all-selfloops.
      boolean selfloop = false;
      for (final EventProxy event : replacement) {
        final int e = mTempEventEncoding.getEventCode(event);
        if (e < 0) {
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
            rel.setProperEventStatus(e, status | EventEncoding.STATUS_UNUSED);
          }
        }
        assert r >= 0 : "Replacement event not found!";
        if (mTempEventEncoding.getEventCode(original) < 0) {
          if (events == null) {
            final int size = mTempEventEncoding.getNumberOfEvents();
            events = new ArrayList<EventProxy>(size);
            events.add(null);
            events.addAll(mTempEventEncoding.getEvents());
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

  private ListBufferTransitionRelation createSynchronizedSupervisor(final ListBufferTransitionRelation rel,
                                                                    final List<DistinguisherInfo> renamings)
    throws AnalysisException
  {
    final AutomatonProxy distinguisher = renamings.get(0).getDistinguisher();
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
    builder.setConstructsResult(true);
    builder.setPropositions(getPropositions());
    builder.setModel(model);
    for (final DistinguisherInfo info : renamings) {
      final EventProxy original = info.getOriginalEvent();
      final List<EventProxy> replacement = info.getReplacement();
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
    return new ListBufferTransitionRelation(newSupervisor,
                                            mTempEventEncoding,
                                            ListBufferTransitionRelation.CONFIG_SUCCESSORS);
  }

  private String getUniqueSupervisorName(final ListBufferTransitionRelation rel)
  {
    final CompositionalSynthesisResult result = getAnalysisResult();
    final Collection<AutomatonProxy> supervisors =
      result.getComputedAutomata();
    String supname;
    int index = 0;
    boolean found;
    do {
      if (index == 0) {
        supname = "sup:" + rel.getName();
      } else {
        supname = "sup" + index + ":" + rel.getName();
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


  //#########################################################################
  //# Debugging
  void reportAbstractionResult(final AutomatonProxy aut,
                               final AutomatonProxy dist)
  {
    final Logger logger = getLogger();
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

  void reportSupervisor(final String kind,
                        final ListBufferTransitionRelation sup)
  {
    final Logger logger = getLogger();
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
  //# Inner Class SynthesisEventInfo
  /**
   * An event information record for compositional synthesis. In compositional
   * synthesis, there are no tau events, yet all events are subject to
   * selfloop removal.
   */
  private final class SynthesisEventInfo extends EventInfo
  {
    //#######################################################################
    //# Constructor
    protected SynthesisEventInfo(final EventProxy event)
    {
      super(event);
    }

    //#######################################################################
    //# Event Status
    @Override
    protected boolean canBeTau()
    {
      return false;
    }

    @Override
    protected boolean isSubjectToSelfloopRemoval()
    {
      return true;
    }
  }


  //#########################################################################
  //# Inner Class DistinguisherInfo
  /**
   * Contains information about event replacement and associated
   * distinguishers.
   */
  private class DistinguisherInfo
  {
    //#######################################################################
    //# Constructor
    private DistinguisherInfo(final EventProxy original,
                              final List<EventProxy> replacement,
                              final AutomatonProxy distinguisher)
    {
      mOriginalEvent = original;
      mReplacement = replacement;
      mDistinguisher = distinguisher;
    }

    //#######################################################################
    //# Simple Access
    EventProxy getOriginalEvent()
    {
      return mOriginalEvent;
    }

    List<EventProxy> getReplacement()
    {
      return mReplacement;
    }

    AutomatonProxy getDistinguisher()
    {
      return mDistinguisher;
    }

    //#######################################################################
    //# Debugging
    @Override
    public String toString()
    {
      return mDistinguisher.getName() + " " + getReplacement().toString()
             + " -> " + mOriginalEvent.getName();
    }

    //#######################################################################
    //# Data Members
    private final EventProxy mOriginalEvent;
    private final List<EventProxy> mReplacement;
    private final AutomatonProxy mDistinguisher;

  }


  //#########################################################################
  //# Data Members
  private String mOutputName;
  private boolean mConstructsResult = true;
  private boolean mSupervisorReductionEnabled = false;
  private final boolean mReduceIncrementally = false;

  private SupervisorReductionTRSimplifier mSupervisorSimplifier;
  private HalfWaySynthesisTRSimplifier mHalfwaySimplifier;
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
}
