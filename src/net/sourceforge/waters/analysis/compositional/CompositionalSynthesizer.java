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
import gnu.trove.map.hash.TIntObjectHashMap;
import gnu.trove.map.hash.TLongObjectHashMap;
import gnu.trove.set.hash.THashSet;
import gnu.trove.set.hash.TIntHashSet;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Set;

import net.sourceforge.waters.analysis.abstraction.HalfWaySynthesisTRSimplifier;
import net.sourceforge.waters.analysis.monolithic.MonolithicSynchronousProductBuilder;
import net.sourceforge.waters.analysis.tr.EventEncoding;
import net.sourceforge.waters.analysis.tr.ListBufferTransitionRelation;
import net.sourceforge.waters.analysis.tr.MemStateProxy;
import net.sourceforge.waters.analysis.tr.StateEncoding;
import net.sourceforge.waters.analysis.tr.TransitionIterator;
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
    mRenamedEvents = new THashSet<EventProxy>();
    super.setUp();
  }

  @Override
  protected void tearDown()
  {
    super.tearDown();
    mDistinguisherInfoList = null;
    mRenamedEvents = null;
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
      final EventEncoding eventEnc = synStep.getEventEncoding();
      final ListBufferTransitionRelation supervisor = synStep.getSupervisor();
      if (supervisor != null) {
        if (supervisor.isEmpty()) {
          result.setSatisfied(false);
          return;
        } else {
          final AutomatonProxy newSupervisor =
            createRenamedSupervisor(supervisor, eventEnc);
          result.addSupervisor(newSupervisor);
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
        logger.debug("Monolithically composing " + automata.size()
                     + " automata, estimated " + estimate + " states.");
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
    final EventEncoding coding = createSynthesisEventEncoding(automaton);
    final ListBufferTransitionRelation supervisor =
      synthesise(automaton, coding);
    if (supervisor != null) {
      reportSupervisor("monolithic", supervisor);
      final CompositionalSynthesisResult result = getAnalysisResult();
      result.addSynchSize(automaton.getStates().size());
      if (supervisor.getNumberOfReachableStates() == 0) {
        result.setSatisfied(false);
        return false;
      } else {
        final AutomatonProxy renamedSup =
          createRenamedSupervisor(supervisor, coding);
        result.addSupervisor(renamedSup);
        return true;
      }
    } else {
      return true;
    }
  }

  //#########################################################################
  //# Renaming
  SynthesisAbstractionStep createDeterministicAutomaton(final AutomatonProxy originalAutomaton,
                                                        final ListBufferTransitionRelation original,
                                                        final ListBufferTransitionRelation simplified,
                                                        final List<int[]> partition,
                                                        final EventEncoding eventEnc)
  {
    final ProductDESProxyFactory factory = getFactory();
    final int numOfStates = original.getNumberOfStates();
    final int numOfEvents = eventEnc.getNumberOfProperEvents();

    // Set up reverse state map of partition
    final int[] recoding = new int[numOfStates];
    int code = 0;
    for (final int[] clazz : partition) {
      for (final int state : clazz) {
        recoding[state] = code;
      }
      code++;
    }

    // Find event replacements
    final Map<EventProxy,List<EventProxy>> renaming =
      new HashMap<EventProxy,List<EventProxy>>(numOfEvents);
    final TransitionIterator iter =
      original.createSuccessorsReadOnlyIterator();
    for (int event = 0; event < numOfEvents; event++) {
      if ((original.getProperEventStatus(event) & EventEncoding.STATUS_UNUSED) == 0) {
        int maxCount = 0;
        for (final int[] clazz : partition) {
          final TIntHashSet successors = new TIntHashSet();
          for (final int state : clazz) {
            iter.reset(state, event);
            while (iter.advance()) {
              final int target = iter.getCurrentTargetState();
              final int targetClass = recoding[target];
              successors.add(targetClass);
            }
          }
          final int count = successors.size();
          if (count > maxCount) {
            maxCount = count;
          }
        }
        if (maxCount > 1) {
          final List<EventProxy> replacement =
            new ArrayList<EventProxy>(maxCount);
          final EventProxy eventProxy = eventEnc.getProperEvent(event);
          for (int i = 0; i < maxCount; i++) {
            final EventProxy newEvent =
              factory.createEventProxy("{" + eventProxy.getName() + ":" + i
                                         + "}", eventProxy.getKind(),
                                       eventProxy.isObservable());
            replacement.add(newEvent);
          }
          renaming.put(eventProxy, replacement);
        }
      }
    }

    // Create distinguisher and simplified automaton alphabet
    final Collection<EventProxy> distinguisherEvents =
      createAlphabet(original, eventEnc, renaming);
    final Collection<EventProxy> simplifiedEvents =
      createAlphabet(simplified, eventEnc, renaming);

    // Create distinguisher and simplified automaton states
    final StateProxy[] distinguisherStatesArray =
      createStates(original, eventEnc);
    final StateProxy[] simplifiedStatesArray =
      createStates(simplified, eventEnc);
    final Collection<StateProxy> distinguisherStates =
      getNotNullStates(distinguisherStatesArray);
    final Collection<StateProxy> simplifiedStates =
      getNotNullStates(simplifiedStatesArray);

    // Create distinguisher and simplified automaton transitions
    final Collection<TransitionProxy> distinguisherTransitions =
      new ArrayList<TransitionProxy>(original.getNumberOfTransitions());
    final Collection<TransitionProxy> simplifiedTransitions =
      new ArrayList<TransitionProxy>(original.getNumberOfTransitions());
    final TransitionIterator originalIter =
      original.createAllTransitionsReadOnlyIterator();
    final TransitionIterator simplifiedIter =
      simplified.createAllTransitionsReadOnlyIterator();
    for (int event = 0; event < numOfEvents; event++) {
      if ((original.getProperEventStatus(event) & EventEncoding.STATUS_UNUSED) == 0) {
        final EventProxy eventProxy = eventEnc.getProperEvent(event);
        final List<EventProxy> replacement = renaming.get(eventProxy);
        if (replacement == null) {
          originalIter.resetEvent(event);
          while (originalIter.advance()) {
            final int s = originalIter.getCurrentSourceState();
            final int t = originalIter.getCurrentTargetState();
            if (original.isReachable(s) && original.isReachable(t)) {
              final StateProxy source = distinguisherStatesArray[s];
              final StateProxy target = distinguisherStatesArray[t];
              final TransitionProxy trans =
                factory.createTransitionProxy(source, eventProxy, target);
              distinguisherTransitions.add(trans);
            }
          }
          if ((simplified.getProperEventStatus(event) & EventEncoding.STATUS_UNUSED) == 0) {
            simplifiedIter.resetEvent(event);
            while (simplifiedIter.advance()) {
              final int s = simplifiedIter.getCurrentSourceState();
              final int t = simplifiedIter.getCurrentTargetState();
              if (simplified.isReachable(s) && simplified.isReachable(t)) {
                final StateProxy source = simplifiedStatesArray[s];
                final StateProxy target = simplifiedStatesArray[t];
                final TransitionProxy trans =
                  factory.createTransitionProxy(source, eventProxy, target);
                simplifiedTransitions.add(trans);
              }
            }
          }
        } else {
          for (int sourceClass = 0; sourceClass < partition.size(); sourceClass++) {
            final int[] clazz = partition.get(sourceClass);
            final TIntObjectHashMap<EventProxy> successors =
              new TIntObjectHashMap<EventProxy>(replacement.size());
            int next = 0;
            for (final int source : clazz) {
              iter.reset(source, event);
              while (iter.advance()) {
                final int target = iter.getCurrentTargetState();
                final int targetClass = recoding[target];
                EventProxy replacementEventProxy =
                  successors.get(targetClass);
                if (replacementEventProxy == null) {
                  replacementEventProxy = replacement.get(next);
                  next++;
                  successors.put(targetClass, replacementEventProxy);
                  final StateProxy sourceProxy =
                    simplifiedStatesArray[sourceClass];
                  final StateProxy targetProxy =
                    simplifiedStatesArray[targetClass];
                  final TransitionProxy trans =
                    factory.createTransitionProxy(sourceProxy,
                                                  replacementEventProxy,
                                                  targetProxy);
                  simplifiedTransitions.add(trans);
                }
                final StateProxy sourceProxy =
                  distinguisherStatesArray[source];
                final StateProxy targetProxy =
                  distinguisherStatesArray[target];
                final TransitionProxy trans =
                  factory.createTransitionProxy(sourceProxy,
                                                replacementEventProxy,
                                                targetProxy);
                distinguisherTransitions.add(trans);
              }
            }
          }
        }
      }
    }

    // Create distinguisher and simplified automata
    final String simplifiedName = original.getName();
    final String distinguisherName = "dis:" + simplifiedName;
    final AutomatonProxy simplifiedAutomaton =
      factory.createAutomatonProxy(simplifiedName, ComponentKind.PLANT,
                                   simplifiedEvents, simplifiedStates,
                                   simplifiedTransitions);
    final AutomatonProxy distinguisherAutomaton =
      factory.createAutomatonProxy(distinguisherName,
                                   ComponentKind.SUPERVISOR,
                                   distinguisherEvents, distinguisherStates,
                                   distinguisherTransitions);

    // Create distinguisher info
    for (final Map.Entry<EventProxy,List<EventProxy>> entry : renaming
      .entrySet()) {
      final EventProxy event = entry.getKey();
      final List<EventProxy> replacement = entry.getValue();
      final DistinguisherInfo info =
        new DistinguisherInfo(event, replacement, distinguisherAutomaton);
      mDistinguisherInfoList.add(info);
      mRenamedEvents.addAll(replacement);
    }
    reportAbstractionResult(simplifiedAutomaton, distinguisherAutomaton);

    return new SynthesisAbstractionStep(this, simplifiedAutomaton,
                                        originalAutomaton, renaming, eventEnc);
  }

  TIntHashSet getRenamedControllables(final EventEncoding encoding)
  {
    final KindTranslator translator = getKindTranslator();
    final int numEvents = encoding.getNumberOfProperEvents();
    final TIntHashSet result = new TIntHashSet(numEvents);
    for (int e = EventEncoding.NONTAU; e < numEvents; e++) {
      final EventProxy event = encoding.getProperEvent(e);
      if (translator.getEventKind(event) == EventKind.CONTROLLABLE
          && mRenamedEvents.contains(event)) {
        result.add(e);
      }
    }
    return result;
  }

  private Collection<EventProxy> createAlphabet(final ListBufferTransitionRelation rel,
                                                final EventEncoding eventEnc,
                                                final Map<EventProxy,List<EventProxy>> eventMap)
  {
    final int numOfEvents = eventEnc.getNumberOfProperEvents();
    final Collection<EventProxy> events =
      new ArrayList<EventProxy>(numOfEvents);
    for (int event = 0; event < numOfEvents; event++) {
      if ((rel.getProperEventStatus(event) & EventEncoding.STATUS_UNUSED) == 0) {
        final EventProxy eventProxy = eventEnc.getProperEvent(event);
        if (eventMap.get(eventProxy) != null) {
          events.addAll(eventMap.get(eventProxy));
        } else {
          events.add(eventProxy);
        }
      }
    }
    for (int p = 0; p < rel.getNumberOfPropositions(); p++) {
      if (rel.isUsedProposition(p)) {
        final EventProxy event = eventEnc.getProposition(p);
        events.add(event);
      }
    }
    return events;
  }

  private StateProxy[] createStates(final ListBufferTransitionRelation rel,
                                    final EventEncoding eventEnc)
  {
    final int numOfStates = rel.getNumberOfStates();
    final int numProps = rel.getNumberOfPropositions();
    final StateProxy[] states = new StateProxy[numOfStates];
    final TLongObjectHashMap<Collection<EventProxy>> markingsMap =
      new TLongObjectHashMap<Collection<EventProxy>>();
    int code = 0;
    for (int s = 0; s < numOfStates; s++) {
      if (rel.isReachable(s)) {
        final StateProxy state;
        final boolean init = rel.isInitial(s);
        final long markings = rel.getAllMarkings(s);
        Collection<EventProxy> props = markingsMap.get(markings);
        if (props == null) {
          props = new ArrayList<EventProxy>(numProps);
          for (int p = 0; p < numProps; p++) {
            if (rel.isMarked(s, p)) {
              final EventProxy prop = eventEnc.getProposition(p);
              props.add(prop);
            }
          }
          markingsMap.put(markings, props);
        }
        state = new MemStateProxy(code++, init, props);
        states[s] = state;
      }
    }
    return states;
  }

  private Collection<StateProxy> getNotNullStates(final StateProxy[] states)
  {
    final List<StateProxy> notNull = new ArrayList<StateProxy>(states.length);
    for (final StateProxy state : states) {
      if (state != null) {
        notNull.add(state);
      }
    }
    return notNull;
  }

  private EventEncoding createSynthesisEventEncoding(final AutomatonProxy aut)
  {
    final KindTranslator translator = getKindTranslator();
    final Collection<EventProxy> events = aut.getEvents();
    final int numEvents = events.size();
    final Collection<EventProxy> uncontrollable =
      new ArrayList<EventProxy>(numEvents);
    final Collection<EventProxy> controllable =
      new ArrayList<EventProxy>(numEvents);
    for (final EventProxy event : events) {
      if (translator.getEventKind(event) == EventKind.UNCONTROLLABLE) {
        uncontrollable.add(event);
      } else {
        controllable.add(event);
      }
    }
    final Collection<EventProxy> orderedEvents =
      new ArrayList<EventProxy>(numEvents);
    orderedEvents.addAll(uncontrollable);
    orderedEvents.addAll(controllable);
    return new EventEncoding(orderedEvents, translator);
  }

  private ListBufferTransitionRelation synthesise(final AutomatonProxy automaton,
                                                  final EventEncoding encoding)
    throws AnalysisException
  {
    final ListBufferTransitionRelation rel =
      new ListBufferTransitionRelation(
                                       automaton,
                                       encoding,
                                       ListBufferTransitionRelation.CONFIG_PREDECESSORS);
    final HalfWaySynthesisTRSimplifier synthesiser =
      new HalfWaySynthesisTRSimplifier(rel);
    final KindTranslator translator = getKindTranslator();
    final int numEvents = encoding.getNumberOfProperEvents();
    int numUncontrollables = 1;
    for (int event = EventEncoding.NONTAU; event < numEvents; event++) {
      final EventProxy proxy = encoding.getProperEvent(event);
      final EventKind kind = translator.getEventKind(proxy);
      if (EventKind.CONTROLLABLE == kind) {
        break;
      } else {
        numUncontrollables++;
      }
    }
    synthesiser.setLastLocalControllableEvent(numEvents);
    synthesiser.setLastLocalUncontrollableEvent(numUncontrollables - 1);
    synthesiser.setLastSharedUncontrollableEvent(numEvents);
    final EventProxy defaultMarking = getUsedDefaultMarking();
    final int defaultID = encoding.getEventCode(defaultMarking);
    synthesiser.setDefaultMarkingID(defaultID);
    final TIntHashSet renamed = getRenamedControllables(encoding);
    synthesiser.setRenamedEvents(renamed);
    synthesiser.run();
    return synthesiser.getPseudoSupervisor();
  }

  private AutomatonProxy createRenamedSupervisor(final ListBufferTransitionRelation rel,
                                                 final EventEncoding encoding)
    throws AnalysisException
  {
    final ListIterator<DistinguisherInfo> listIter =
      mDistinguisherInfoList.listIterator(mDistinguisherInfoList.size());
    return createRenamedSupervisor(rel, encoding, listIter);
  }

  private AutomatonProxy createRenamedSupervisor(final ListBufferTransitionRelation rel,
                                                 final EventEncoding encoding,
                                                 final ListIterator<DistinguisherInfo> listIter)
    throws AnalysisException
  {

    if (!listIter.hasPrevious()) {
      return removeDumpStates(rel, encoding);
    }
    final int numOfStates = rel.getNumberOfStates();
    final TransitionIterator transitionIter =
      rel.createSuccessorsReadOnlyIterator();
    final List<DistinguisherInfo> renamings =
      new LinkedList<DistinguisherInfo>();
    AutomatonProxy distinguisher = null;
    boolean foundNondeterminism = false;
    while (listIter.hasPrevious()) {
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
        if (encoding.getEventCode(event) >= 0) {
          found = true;
          break;
        }
      }
      if (!found) {
        continue;
      }
      renamings.add(info);
      if (!foundNondeterminism) {
        outer: for (int state = 0; state < numOfStates; state++) {
          int foundSuccessor = -1;
          for (final EventProxy event : replacement) {
            final int code = encoding.getEventCode(event);
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

    // build modified supervisor
    final KindTranslator translator = getKindTranslator();
    if (!foundNondeterminism) {
      final TransitionIterator modifyingIter =
        rel.createSuccessorsModifyingIterator();
      final int size = encoding.getNumberOfEvents();
      final List<EventProxy> events = new ArrayList<EventProxy>(size);
      events.add(null);
      events.addAll(encoding.getEvents());
      for (final DistinguisherInfo info : renamings) {
        final EventProxy original = info.getOriginalEvent();
        final List<EventProxy> replacement = info.getReplacement();
        final Iterator<EventProxy> iter = replacement.iterator();
        final EventProxy firstReplacement = iter.next();
        final int firstCode = encoding.getEventCode(firstReplacement);
        if (!events.contains(original))
          events.set(firstCode, original);
        while (iter.hasNext()) {
          final EventProxy nextReplacement = iter.next();
          final int nextCode = encoding.getEventCode(nextReplacement);
          if (nextCode >= 0) {
            for (int sourceState = 0; sourceState < numOfStates; sourceState++) {
              modifyingIter.reset(sourceState, nextCode);
              if (modifyingIter.advance()) {
                final int target = modifyingIter.getCurrentTargetState();
                modifyingIter.remove();
                rel.addTransition(sourceState, firstCode, target);
              }
            }
            final byte status = rel.getProperEventStatus(nextCode);
            rel
              .setProperEventStatus(nextCode,
                                    (byte) (status | EventEncoding.STATUS_UNUSED));
          }
        }
      }
      final EventEncoding newEncoding = new EventEncoding(events, translator);
      return createRenamedSupervisor(rel, newEncoding, listIter);
    } else {
      final ProductDESProxyFactory factory = getFactory();
      final AutomatonProxy oldSupervisor =
        rel.createAutomaton(factory, encoding);
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
      final EventEncoding newEncoding =
        new EventEncoding(newSupervisor, translator);
      final ListBufferTransitionRelation newRel =
        new ListBufferTransitionRelation(
                                         newSupervisor,
                                         newEncoding,
                                         ListBufferTransitionRelation.CONFIG_SUCCESSORS);
      return createRenamedSupervisor(newRel, newEncoding, listIter);
    }
  }

  private AutomatonProxy removeDumpStates(final ListBufferTransitionRelation rel,
                                          final EventEncoding coding)
    throws OverflowException
  {
    final EventProxy defaultMarking = getUsedDefaultMarking();
    final int defaultMarkingID = coding.getEventCode(defaultMarking);
    final boolean usesMarking = rel.isUsedProposition(defaultMarkingID);
    final int numOfStates = rel.getNumberOfStates();
    final TransitionIterator iter = rel.createSuccessorsReadOnlyIterator();
    for (int sourceState = 0; sourceState < numOfStates; sourceState++) {
      iter.resetState(sourceState);
      if (iter.advance()) {
        //nothing
      } else if (usesMarking && !rel.isMarked(sourceState, defaultMarkingID)) {
        rel.setReachable(sourceState, false);
      }
    }
    final ProductDESProxyFactory factory = getFactory();
    final String name = getUniqueSupervisorName(rel);
    rel.setName(name);
    rel.setKind(ComponentKind.SUPERVISOR);
    return rel.createAutomaton(factory, coding);
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

  private List<DistinguisherInfo> mDistinguisherInfoList;
  private Set<EventProxy> mRenamedEvents;

}
