//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters Analysis
//# PACKAGE: net.sourceforge.waters.analysis.abstraction
//# CLASS:   CompositionalSynthesizer
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.analysis.abstraction;

import gnu.trove.THashSet;
import gnu.trove.TIntHashSet;
import gnu.trove.TIntObjectHashMap;
import gnu.trove.TLongObjectHashMap;

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

import net.sourceforge.waters.analysis.monolithic.MonolithicSynchronousProductBuilder;
import net.sourceforge.waters.analysis.tr.EventEncoding;
import net.sourceforge.waters.analysis.tr.ListBufferTransitionRelation;
import net.sourceforge.waters.analysis.tr.MemStateProxy;
import net.sourceforge.waters.analysis.tr.StateEncoding;
import net.sourceforge.waters.analysis.tr.TransitionIterator;
import net.sourceforge.waters.model.analysis.AbstractConflictChecker;
import net.sourceforge.waters.model.analysis.AnalysisException;
import net.sourceforge.waters.model.analysis.IdenticalKindTranslator;
import net.sourceforge.waters.model.analysis.KindTranslator;
import net.sourceforge.waters.model.analysis.OverflowException;
import net.sourceforge.waters.model.analysis.ProductDESBuilder;
import net.sourceforge.waters.model.analysis.ProductDESResult;
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
 * Sahar Mohajerani, Robi Malik, Simon Ware, Martin Fabian.
 * On the Use of Observation Equivalence in Synthesis Abstraction.
 * Proc. 3rd IFAC Workshop on Dependable Control of Discrete Systems,
 * DCDS&nbsp;2011, Saarbr&uuml;cken, Germany, 2011.<BR>
 * Sahar Mohajerani, Robi Malik, Martin Fabian.
 * Nondeterminism Avoidance in Compositional Synthesis of Discrete Event
 * Systems, Proc. 7th International Conference on Automation Science and
 * Engineering, CASE&nbsp;2011, Trieste, Italy.
 *
 * @author Sahar Mohajerani, Robi Malik
 */


public class CompositionalSynthesizer
  extends AbstractCompositionalModelAnalyzer
  implements ProductDESBuilder
{

  //#########################################################################
  //# Constructors
  /**
   * Creates a compositional synthesiser without a model.
   * @param factory
   *          Factory used for trace construction.
   */
  public CompositionalSynthesizer
    (final ProductDESProxyFactory factory)
  {
    this(factory, IdenticalKindTranslator.getInstance());
  }

  /**
   * Creates a compositional synthesiser without a model.
   * @param factory
   *          Factory used for trace construction.
   * @param translator
   *          Kind translator used to determine event and component kinds.
   */
  public CompositionalSynthesizer
    (final ProductDESProxyFactory factory,
     final KindTranslator translator)
  {
    super(factory, translator);
  }

  /**
   * Creates a compositional synthesiser without a model.
   * @param factory
   *          Factory used for trace construction.
   * @param translator
   *          Kind translator used to determine event and component kinds.
   * @param preselectingMethodFactory
   *          Enumeration factory that determines possible candidate
   *          preselection methods.
   * @param selectingMethodFactory
   *          Enumeration factory that determines possible candidate
   *          selection methods.
   */
  public CompositionalSynthesizer
    (final ProductDESProxyFactory factory,
     final KindTranslator translator,
     final PreselectingMethodFactory preselectingMethodFactory,
     final SelectingMethodFactory selectingMethodFactory)
  {
    super(factory, translator,
          preselectingMethodFactory, selectingMethodFactory);
  }

  /**
   * Creates a compositional synthesiser to compute a supervisor for the
   * given model.
   * @param model
   *          The model to be checked by this model verifier.
   * @param factory
   *          Factory used for trace construction.
   * @param translator
   *          Kind translator used to determine event and component kinds.
   */
  public CompositionalSynthesizer
    (final ProductDESProxy model,
     final ProductDESProxyFactory factory,
     final KindTranslator translator)
  {
    super(model, factory, translator);
  }

  /**
   * Creates a compositional synthesiser to compute a supervisor for the
   * given model.
   * @param model
   *          The model to be checked by this model verifier.
   * @param factory
   *          Factory used for trace construction.
   * @param translator
   *          Kind translator used to determine event and component kinds.
   * @param preselectingMethodFactory
   *          Enumeration factory that determines possible candidate
   *          preselection methods.
   * @param selectingMethodFactory
   *          Enumeration factory that determines possible candidate
   *          selection methods.
   */
  public CompositionalSynthesizer
    (final ProductDESProxy model,
     final ProductDESProxyFactory factory,
     final KindTranslator translator,
     final PreselectingMethodFactory preselectingMethodFactory,
     final SelectingMethodFactory selectingMethodFactory)
  {
    super(model, factory, translator,
          preselectingMethodFactory, selectingMethodFactory);
  }


  //#########################################################################
  //# Interface net.sourceforge.waters.model.analysis.ModelBuilder
  public void setOutputName(final String name)
  {
    mOutputName = name;
  }

  public String getOutputName()
  {
    return mOutputName;
  }

  public void setConstructsResult(final boolean construct)
  {
    mConstructsResult = construct;
  }

  public boolean getConstructsResult()
  {
    return mConstructsResult;
  }

  public ProductDESProxy getComputedProxy()
  {
    final ProductDESResult result = getAnalysisResult();
    return result.getComputedProductDES();
  }


  //#########################################################################
  //# Interface net.sourceforge.waters.model.analysis.ProductDESBuilder
  public ProductDESProxy getComputedProductDES()
  {
    return getComputedProxy();
  }


  //#########################################################################
  //# Configuration
  public void setMarkingProposition(final EventProxy marking)
  {
    mDefaultMarking = marking;
    mUsedDefaultMarking = null;
  }

  public EventProxy getMarkingProposition()
  {
    return mDefaultMarking;
  }


  //#########################################################################
  //# Invocation
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
  protected void setUp()
    throws AnalysisException
  {
    if (mDefaultMarking == null) {
      final ProductDESProxy model = getModel();
      mUsedDefaultMarking =
        AbstractConflictChecker.getMarkingProposition(model);
    } else {
      mUsedDefaultMarking = mDefaultMarking;
    }
    final Collection<EventProxy> props =
      Collections.singletonList(mUsedDefaultMarking);
    setPropositions(props);

    final ChainTRSimplifier chain = new ChainTRSimplifier();
    /*
    final TransitionRelationSimplifier loopRemover =
      new TauLoopRemovalTRSimplifier();
    chain.add(loopRemover);
     */
    final HalfWaySynthesisTRSimplifier halfWay =
      new HalfWaySynthesisTRSimplifier();
    chain.add(halfWay);
    final ObservationEquivalenceTRSimplifier bisimulator =
      new ObservationEquivalenceTRSimplifier();
    bisimulator.setEquivalence
      (ObservationEquivalenceTRSimplifier.Equivalence.BISIMULATION);
    chain.add(bisimulator);
    final SynthesisAbstractionTRSimplifier synthesisAbstraction= new
            SynthesisAbstractionTRSimplifier();
    final int limit = getInternalTransitionLimit();
    synthesisAbstraction.setTransitionLimit(limit);
    chain.add(synthesisAbstraction);
    final AbstractionProcedure proc =
      new SynthesisAbstractionProcedure(chain, synthesisAbstraction, halfWay);
    setAbstractionProcedure(proc);

    mDistinguisherInfoList = new LinkedList<DistinguisherInfo> ();
    mRenamedEvents = new THashSet<EventProxy>();
    super.setUp();
    proc.storeStatistics();
  }

  @Override
  protected void tearDown()
  {
    super.tearDown();
    mUsedDefaultMarking = null;
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

  @Override
  public boolean supportsNondeterminism()
  {
    return false;
  }


  //#########################################################################
  //# Hooks
  @Override
  protected void setupSynchronousProductBuilder()
  {
    super.setupSynchronousProductBuilder();
    final MonolithicSynchronousProductBuilder builder =
      getCurrentSynchronousProductBuilder();
    builder.setPruningDeadlocks(true);
  }

  @Override
  protected SynthesisEventInfo createEventInfo(final EventProxy event)
  {
    return new SynthesisEventInfo(event);
  }

  @Override
  protected void recordAbstractionStep(final AbstractionStep step)
    throws AnalysisException
  {
    final CompositionalSynthesisResult result = getAnalysisResult();
    final ProductDESProxyFactory factory = getFactory();
    if(step instanceof SynthesisAbstractionStep){
      final SynthesisAbstractionStep synStep = (SynthesisAbstractionStep) step;
      final EventEncoding eventEnc = synStep.getEventEncoding();
      final ListBufferTransitionRelation supervisor =
        synStep.getSupervisor();
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
            final List<AutomatonProxy> aut = info.getAutomataList();
            affectedAutomata.addAll(aut);
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
//    } else if(step instanceof EventRemovalStep){
//      final List<AutomatonProxy> before = step.getOriginalAutomata();
//      final List<AutomatonProxy> after = step.getResultAutomata();
//      final Iterator <AutomatonProxy> beforeIterator = before.iterator();
//      final Iterator <AutomatonProxy> afterIterator = after.iterator();
//      while (beforeIterator.hasNext()) {
//        final AutomatonProxy beforeAutomaton = beforeIterator.next();
//        final AutomatonProxy afterAutomaton = afterIterator.next();
//        final int sizeBefore = getNumControllableEvents(beforeAutomaton);
//        final int sizeAfter = getNumControllableEvents(afterAutomaton);
//        if (sizeBefore != sizeAfter) {
//          final String name = "dis:" + beforeAutomaton.getName();
//          final Collection <EventProxy> events = beforeAutomaton.getEvents();
//          final Collection <StateProxy> states = beforeAutomaton.getStates();
//          final Collection <TransitionProxy> transitions =
//            beforeAutomaton.getTransitions();
//          final AutomatonProxy distinguisher =
//            factory.createAutomatonProxy(name, ComponentKind.SUPERVISOR,
//                                         events, states, transitions);
//          result.addSupervisor(distinguisher);
//        }
//      }
  }

  @SuppressWarnings("unused")
  private int getNumControllableEvents(final AutomatonProxy aut)
  {
    final KindTranslator translator = getKindTranslator();
    final Collection<EventProxy> alphabet = aut.getEvents();
    int size = 0;
    for(final EventProxy event:alphabet){
      if(translator.getEventKind(event) == EventKind.CONTROLLABLE){
        size++;
      }
    }
   return size;
  }

  @Override
  protected boolean doMonolithicAnalysis
    (final List<AutomatonProxy> automata)
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
        getCurrentSynchronousProductBuilder();
      final ProductDESProxy des = createProductDESProxy(automata);
      syncBuilder.setModel(des);
      final int limit = getMonolithicStateLimit();
      syncBuilder.setNodeLimit(limit);
      syncBuilder.run();
      automaton = syncBuilder.getComputedAutomaton();
      break;
    }

    final EventEncoding coding = createSynthesisEventEncoding(automaton);
    final ListBufferTransitionRelation supervisor =
      synthesise(automaton, coding);
    if (supervisor != null) {
      final CompositionalSynthesisResult result = getAnalysisResult();
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
  private SynthesisAbstractionStep createDeterministicAutomaton
    (final AutomatonProxy originalAutomaton,
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
    final Map<EventProxy, List<EventProxy>> renaming =
      new HashMap<EventProxy, List<EventProxy>>(numOfEvents);
    final TransitionIterator iter =
      original.createSuccessorsReadOnlyIterator();
    for (int event = 0; event < numOfEvents; event++) {
      if (original.isUsedEvent(event)) {
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
              factory.createEventProxy("{" + eventProxy.getName() +
                                       ":" + i + "}",
                                       eventProxy.getKind(),
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
      if (original.isUsedEvent(event)) {
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
          if (simplified.isUsedEvent(event)) {
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
          for(int sourceClass = 0; sourceClass < partition.size();
              sourceClass++){
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
      factory.createAutomatonProxy(distinguisherName, ComponentKind.SUPERVISOR,
                                   distinguisherEvents, distinguisherStates,
                                   distinguisherTransitions);

    // Create distinguisher info
    for (final Map.Entry<EventProxy, List<EventProxy>> entry :
         renaming.entrySet()){
      final EventProxy event = entry.getKey();
      final List<EventProxy> replacement = entry.getValue();
      final DistinguisherInfo info = new DistinguisherInfo
        (event, replacement, distinguisherAutomaton);
      mDistinguisherInfoList.add(info);
      mRenamedEvents.addAll(replacement);
    }

    return new SynthesisAbstractionStep(simplifiedAutomaton,
                                        originalAutomaton,
                                        renaming,
                                        eventEnc);
  }

  private Collection<EventProxy> createAlphabet
    (final ListBufferTransitionRelation rel,
     final EventEncoding eventEnc,
     final Map<EventProxy,List<EventProxy>> eventMap)
  {
    final int numOfEvents = eventEnc.getNumberOfProperEvents();
    final Collection<EventProxy> events =
      new ArrayList<EventProxy>(numOfEvents);
    for (int event = 0; event < numOfEvents; event++) {
      if (rel.isUsedEvent(event)) {
        final EventProxy eventProxy = eventEnc.getProperEvent(event);
        if (eventMap.get(eventProxy) != null){
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
    final KindTranslator translator = getKindTranslator ();
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

  private ListBufferTransitionRelation synthesise
    (final AutomatonProxy automaton, final EventEncoding eventEnc)
    throws AnalysisException
  {
    final ListBufferTransitionRelation rel = new ListBufferTransitionRelation
      (automaton, eventEnc, ListBufferTransitionRelation.CONFIG_PREDECESSORS);
    final HalfWaySynthesisTRSimplifier synthesis =
      new HalfWaySynthesisTRSimplifier(rel);
    final KindTranslator translator = getKindTranslator ();
    final int numEvents = eventEnc.getNumberOfProperEvents();
    int numUncontrollables = 1;
    for (int event = EventEncoding.NONTAU; event< numEvents; event++) {
      final EventProxy proxy = eventEnc.getProperEvent(event);
      final EventKind kind = translator.getEventKind(proxy);
      if (EventKind.CONTROLLABLE == kind) {
        break;
      } else{
        numUncontrollables++;
      }
    }
    synthesis.setLastLocalControllableEvent(numEvents);
    synthesis.setLastLocalUncontrollableEvent(numUncontrollables - 1);
    synthesis.setLastSharedUncontrollableEvent(numEvents);
    final int defaultID = eventEnc.getEventCode(mUsedDefaultMarking);
    synthesis.setDefaultMarkingID(defaultID);
    synthesis.run();
    return synthesis.getSupervisor();
  }


  private AutomatonProxy createRenamedSupervisor
    (final ListBufferTransitionRelation rel, final EventEncoding encoding)
    throws AnalysisException
  {
    final ListIterator<DistinguisherInfo> listIter =
      mDistinguisherInfoList.listIterator(mDistinguisherInfoList.size());
    return createRenamedSupervisor(rel, encoding, listIter);
  }

  private AutomatonProxy createRenamedSupervisor
    (final ListBufferTransitionRelation rel,
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
      if (!found){
        continue;
      }
      renamings.add(info);
      if (!foundNondeterminism) {
        outer :
          for (int state = 0; state<numOfStates; state++) {
            int foundSuccessor = -1;
            for (final EventProxy event: replacement) {
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
    if( !foundNondeterminism) {
      final TransitionIterator modifyingIter =
        rel.createSuccessorsModifyingIterator();
      final int size = encoding.getNumberOfEvents();
      final List <EventProxy> events = new ArrayList <EventProxy> (size);
      events.add(null);
      events.addAll(encoding.getEvents());
      for (final DistinguisherInfo info:renamings) {
        final EventProxy original = info.getOriginalEvent();
        final List<EventProxy> replacement = info.getReplacement();
        final Iterator<EventProxy> iter = replacement.iterator();
        final EventProxy firstReplacement = iter.next();
        final int firstCode = encoding.getEventCode(firstReplacement);
        events.set(firstCode, original);
        while (iter.hasNext()) {
          final EventProxy nextReplacement = iter.next();
          final int nextCode = encoding.getEventCode(nextReplacement);
          for (int sourceState = 0; sourceState<numOfStates; sourceState++) {
            modifyingIter.reset(sourceState, nextCode);
            if (modifyingIter.advance()) {
              final int target = modifyingIter.getCurrentTargetState();
              modifyingIter.remove();
              rel.addTransition(sourceState, firstCode, target);
            }
          }
          rel.setUsedEvent(nextCode, false);
        }

      }
      final EventEncoding newEncoding = new EventEncoding(events, translator);
      return createRenamedSupervisor(rel, newEncoding, listIter);
    } else {
      final ProductDESProxyFactory factory = getFactory();
      final AutomatonProxy oldSupervisor =
        rel.createAutomaton(factory, encoding);
      final List<AutomatonProxy> automata = new ArrayList <AutomatonProxy>(2);
      automata.add(oldSupervisor);
      automata.add(distinguisher);
      final ProductDESProxy model = createProductDESProxy (automata);
      final MonolithicSynchronousProductBuilder builder =
        getCurrentSynchronousProductBuilder();
      try {
        builder.setModel(model);
        for(final DistinguisherInfo info:renamings) {
          final EventProxy original = info.getOriginalEvent();
          final List<EventProxy> replacement = info.getReplacement();
          builder.addMask(replacement, original);
        }
        builder.run();
      } finally {
        builder.clearMask();
      }
      final AutomatonProxy newSupervisor = builder.getComputedAutomaton();
      final EventEncoding newEncoding =
        new EventEncoding(newSupervisor, translator);
      final ListBufferTransitionRelation newRel =
        new ListBufferTransitionRelation (newSupervisor, newEncoding,
                                          ListBufferTransitionRelation.
                                          CONFIG_SUCCESSORS);
      return createRenamedSupervisor(newRel, newEncoding, listIter);
    }
  }

  private AutomatonProxy removeDumpStates(final ListBufferTransitionRelation rel,
                                          final EventEncoding coding)
    throws OverflowException
  {
    final int marking = coding.getEventCode(mUsedDefaultMarking);
    final int numOfStates = rel.getNumberOfStates();
    final TransitionIterator iter = rel.createSuccessorsReadOnlyIterator();
    for (int sourceState = 0; sourceState<numOfStates; sourceState++) {
      iter.resetState(sourceState);
      if (iter.advance()) {
        //nothing
      } else {
        if (marking >= 0 && !rel.isMarked(sourceState, marking)) {
          rel.setReachable(sourceState, false);
        }
      }
    }
    final ProductDESProxyFactory factory = getFactory();
    final String name = "sup:" + rel.getName();
    rel.setName(name);
    rel.setKind(ComponentKind.SUPERVISOR);
    return rel.createAutomaton(factory, coding);
  }


  //#########################################################################
  //# Inner Class SynthesisAbstractionProcedure
  protected class SynthesisAbstractionProcedure
    extends AbstractionProcedure
  {
    //#######################################################################
    //# Constructor
    protected SynthesisAbstractionProcedure
      (final TransitionRelationSimplifier simplifier,
       final SynthesisAbstractionTRSimplifier synthesisAbstraction,
       final HalfWaySynthesisTRSimplifier halfWaySynthesisSimplifier)
    {
      mSimplifier = simplifier;
      mSynthesisAbstraction = synthesisAbstraction;
      mHalfWaySynthesisSimplifier = halfWaySynthesisSimplifier;
    }

    //#######################################################################
    //# Overrides for AbstractionProcedure
    @Override
    protected AbstractionStep run(final AutomatonProxy aut,
                                  final Collection<EventProxy> local)
      throws AnalysisException
    {
      final TransitionRelationSimplifier simplifier = getSimplifier();
      try {
        final EventEncoding eventEnc = createEventEncoding(aut, local);
        final StateEncoding inputStateEnc = createStateEncoding(aut);
        final int config = simplifier.getPreferredInputConfiguration();
        final ListBufferTransitionRelation rel =
          new ListBufferTransitionRelation(aut, eventEnc,
                                           inputStateEnc, config);
        final int numStates = rel.getNumberOfReachableStates();
        final int numTrans = rel.getNumberOfTransitions();
        final int numMarkings = rel.getNumberOfMarkings();
        simplifier.setTransitionRelation(rel);
        if (simplifier.run()) {
          if (rel.getNumberOfReachableStates() == numStates &&
              rel.getNumberOfTransitions() == numTrans &&
              rel.getNumberOfMarkings() == numMarkings) {
            return null;
          }
          final ListBufferTransitionRelation original =
            mSynthesisAbstraction.getOriginalTransitionRelation();
          final ListBufferTransitionRelation supervisor =
            mHalfWaySynthesisSimplifier.getSupervisor();
          if (original == null) {
           final ProductDESProxyFactory factory = getFactory();
            final StateEncoding outputStateEnc = new StateEncoding();
            final AutomatonProxy convertedAut =
              rel.createAutomaton(factory, eventEnc, outputStateEnc);
            return new SynthesisAbstractionStep(convertedAut, aut,
                                                supervisor, eventEnc);
          } else {
            final List<int[]> partition =
              mSynthesisAbstraction.getResultPartition();
            final SynthesisAbstractionStep step =
              createDeterministicAutomaton(aut, original, rel,
                                           partition, eventEnc);
            step.setSupervisor(supervisor);
            return step;
          }
        } else {
          return null;
        }
      } finally {
        simplifier.reset();
      }
    }

    @Override
    protected void storeStatistics()
    {
      final CompositionalSynthesisResult result = getAnalysisResult();
      result.setSimplifierStatistics(mSimplifier);
    }

    @Override
    protected void resetStatistics()
    {
      mSimplifier.createStatistics();
    }

    //#######################################################################
    //# Interface net.sourceforge.waters.model.analysis.Abortable
    public void requestAbort()
    {
      mSimplifier.requestAbort();
    }

    public boolean isAborting()
    {
      return mSimplifier.isAborting();
    }

    //#######################################################################
    //# Simple Access
    protected TransitionRelationSimplifier getSimplifier()
    {
      return mSimplifier;
    }

    //#######################################################################
    //# Auxiliary Methods
    private EventEncoding createEventEncoding
      (final AutomatonProxy aut,
       final Collection<EventProxy> local)
    {
      final KindTranslator translator = getKindTranslator();
      final Collection<EventProxy> props = getPropositions();
      final Collection<EventProxy> filter;
      if (props == null) {
        filter = Collections.emptyList();
      } else {
        filter = props;
      }
      final Collection<EventProxy> autAlphabet = aut.getEvents();
      final Collection<EventProxy> localUncontrollableEvents =
        new ArrayList<EventProxy>(local.size());
      final Collection<EventProxy> sharedUncontrollableEvents =
        new ArrayList<EventProxy>(autAlphabet.size() - local.size());
      final Collection<EventProxy> localControllableEvents =
        new ArrayList<EventProxy>(local.size());
      final Collection<EventProxy> sharedControllableEvents =
        new ArrayList<EventProxy>(autAlphabet.size() - local.size());
      final Collection<EventProxy> encodedEvents =
        new ArrayList<EventProxy>(autAlphabet.size());
      int propositionSize = 0;
      for (final EventProxy event : autAlphabet) {
        if (translator.getEventKind(event) == EventKind.PROPOSITION) {
          propositionSize++;
        }
        if (local.contains(event)) {
          if (translator.getEventKind(event) == EventKind.CONTROLLABLE) {
            localControllableEvents.add(event);
          } else {
            localUncontrollableEvents.add(event);
          }
        } else {
          if (translator.getEventKind(event) == EventKind.CONTROLLABLE) {
            sharedControllableEvents.add(event);
          } else {
            sharedUncontrollableEvents.add(event);
          }
        }
      }
      final int lastUncontrollableLocalEvent =
        localUncontrollableEvents.size();
      final int lastControllableLocalEvent =
        localControllableEvents.size() + lastUncontrollableLocalEvent;
      final int lastUncontrollableSharedEvent =
        local.size() + sharedUncontrollableEvents.size();
      mSynthesisAbstraction.setLastLocalControllableEvent
        (lastControllableLocalEvent);
      mSynthesisAbstraction.setLastLocalUncontrollableEvent
        (lastUncontrollableLocalEvent);
      mSynthesisAbstraction.setLastSharedUncontrollableEvent
        (lastUncontrollableSharedEvent);
      mHalfWaySynthesisSimplifier.setLastLocalUncontrollableEvent
        (lastUncontrollableLocalEvent);
      mHalfWaySynthesisSimplifier.setLastLocalControllableEvent
        (lastControllableLocalEvent);
      mHalfWaySynthesisSimplifier.setLastSharedUncontrollableEvent
        (lastUncontrollableSharedEvent - propositionSize);
      encodedEvents.addAll(localUncontrollableEvents);
      encodedEvents.addAll(localControllableEvents);
      encodedEvents.addAll(sharedUncontrollableEvents);
      encodedEvents.addAll(sharedControllableEvents);
      final EventEncoding encoding =
        new EventEncoding(encodedEvents, translator, filter,
                          EventEncoding.FILTER_PROPOSITIONS);
      mHalfWaySynthesisSimplifier.
        setDefaultMarkingID(encoding.getEventCode(mUsedDefaultMarking));
      mSynthesisAbstraction.
        setDefaultMarkingID(encoding.getEventCode(mUsedDefaultMarking));
      final TIntHashSet renamedEventIndexes = new TIntHashSet();
      for (final EventProxy event: encodedEvents) {
        if (translator.getEventKind(event) == EventKind.CONTROLLABLE &&
            mRenamedEvents.contains(event)) {
          final int code = encoding.getEventCode(event);
          renamedEventIndexes.add(code);
        }
      }
      mHalfWaySynthesisSimplifier.setRenamedEvents(renamedEventIndexes);
      return encoding;
    }

    private StateEncoding createStateEncoding(final AutomatonProxy aut)
    {
      final StateEncoding encoding = new StateEncoding(aut);
      encoding.setNumberOfExtraStates(1);
      return encoding;
    }


    //#######################################################################
    //# Data Members
    private final TransitionRelationSimplifier mSimplifier;
    private final SynthesisAbstractionTRSimplifier mSynthesisAbstraction;
    private final HalfWaySynthesisTRSimplifier mHalfWaySynthesisSimplifier;
  }

  //#########################################################################
  //# Inner Class SynthesisAbstractionStep
  /**
   * An abstraction step representing synthesis abstraction and/or
   * halfway synthesis of a single automaton.
   */
  private class SynthesisAbstractionStep
    extends AbstractionStep
  {
    //#######################################################################
    //# Constructor
    private SynthesisAbstractionStep(final AutomatonProxy result,
                                     final AutomatonProxy original,
                                     final ListBufferTransitionRelation dis,
                                     final EventEncoding coding)
    {
      super(result, original);
      mSupervisor = dis;
      mEventEncoding = coding;
    }

    private SynthesisAbstractionStep(final AutomatonProxy result,
                                     final AutomatonProxy original,
                                     final Map<EventProxy, List<EventProxy>> renaming,
                                     final EventEncoding coding)
    {
      super(result, original);
      mRenaming = renaming;
      mEventEncoding = coding;
    }

    //#######################################################################
    //# Simple Access
    ListBufferTransitionRelation getSupervisor()
    {
      return mSupervisor;
    }

    private void setSupervisor(final ListBufferTransitionRelation supervisor)
    {
      mSupervisor = supervisor;
    }

    private Map<EventProxy, List<EventProxy>> getRenaming()
    {
      return mRenaming;
    }

    EventEncoding getEventEncoding()
    {
      return mEventEncoding;
    }

    //#######################################################################
    //# Data Members
    private ListBufferTransitionRelation mSupervisor;
    private final EventEncoding mEventEncoding;
    private Map<EventProxy, List<EventProxy>> mRenaming;
  }


  //#########################################################################
  //# Inner Class SynthesisEventInfo
  /**
   * An event information record for compositional synthesis.
   * In compositional synthesis, there are no tau events, yet all events
   * are subject to selfloop removal.
   */
  protected static class SynthesisEventInfo extends EventInfo
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
    protected boolean isTau()
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
    EventProxy getOriginalEvent ()
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
  private EventProxy mDefaultMarking;
  private EventProxy mUsedDefaultMarking;

  private List<DistinguisherInfo> mDistinguisherInfoList;
  private Set<EventProxy> mRenamedEvents;

}
