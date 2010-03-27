//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.analysis.gnonblocking
//# CLASS:   ObserverProjectionConflictChecker
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.analysis.op;

import gnu.trove.THashSet;
import gnu.trove.TIntArrayList;
import gnu.trove.TIntHashSet;
import gnu.trove.TIntIterator;
import gnu.trove.TIntObjectHashMap;
import gnu.trove.TIntObjectIterator;
import gnu.trove.TObjectIntHashMap;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import net.sourceforge.waters.analysis.gnonblocking.Candidate;
import net.sourceforge.waters.analysis.monolithic.MonolithicSynchronousProductBuilder;
import net.sourceforge.waters.cpp.analysis.NativeConflictChecker;
import net.sourceforge.waters.model.analysis.AbstractConflictChecker;
import net.sourceforge.waters.model.analysis.AnalysisException;
import net.sourceforge.waters.model.analysis.ConflictChecker;
import net.sourceforge.waters.model.analysis.EventNotFoundException;
import net.sourceforge.waters.model.analysis.KindTranslator;
import net.sourceforge.waters.model.analysis.OverflowException;
import net.sourceforge.waters.model.analysis.SynchronousProductBuilder;
import net.sourceforge.waters.model.analysis.SynchronousProductStateMap;
import net.sourceforge.waters.model.base.ProxyTools;
import net.sourceforge.waters.model.des.AutomatonProxy;
import net.sourceforge.waters.model.des.ConflictTraceProxy;
import net.sourceforge.waters.model.des.EventProxy;
import net.sourceforge.waters.model.des.ProductDESProxy;
import net.sourceforge.waters.model.des.ProductDESProxyFactory;
import net.sourceforge.waters.model.des.StateProxy;
import net.sourceforge.waters.model.des.TraceStepProxy;
import net.sourceforge.waters.model.des.TransitionProxy;
import net.sourceforge.waters.xsd.base.ComponentKind;
import net.sourceforge.waters.xsd.base.EventKind;


/**
 * A compositional conflict checker that uses only observation equivalence
 * or observer projection for simplification steps.
 *
 * @author Robi Malik, Rachel Francis
 */

public class ObserverProjectionConflictChecker
  extends AbstractConflictChecker
{

  //#########################################################################
  //# Constructors
  /**
   * Creates a new conflict checker without a model or marking proposition.
   */
  public ObserverProjectionConflictChecker
    (final ProductDESProxyFactory factory)
  {
    super(null, factory);
  }

  /**
   * Creates a new conflict checker to check whether the given model is
   * nonblocking.
   * @param model
   *          The model to be checked by this conflict checker.
   * @param factory
   *          Factory used for trace construction.
   */
  public ObserverProjectionConflictChecker
    (final ProductDESProxy model,
     final ProductDESProxyFactory factory)
  {
    super(model, factory);
  }

  /**
   * Creates a new conflict checker to check a particular model for
   * nonblocking.
   * @param model
   *          The model to be checked by this conflict checker.
   * @param marking
   *          The proposition event that defines which states are marked. Every
   *          state has a list of propositions attached to it; the conflict
   *          checker considers only those states as marked that are labelled by
   *          <CODE>marking</CODE>, i.e., their list of propositions must
   *          contain this event(exactly the same object).
   * @param factory
   *          Factory used for trace construction.
   */
  public ObserverProjectionConflictChecker
    (final ProductDESProxy model,
     final EventProxy marking,
     final ProductDESProxyFactory factory)
  {
    super(model, marking, factory);
  }


  //#########################################################################
  //# Configuration
  public void setNodeLimit(final int limit)
  {
    super.setNodeLimit(limit);
    setInternalStepNodeLimit(limit);
  }

  public void setInternalStepNodeLimit(final int limit)
  {
    mInternalStepNodeLimit = limit;
  }

  public void setFinalStepNodeLimit(final int limit)
  {
    super.setNodeLimit(limit);
  }

  public int getInternalStepNodeLimit()
  {
    return mInternalStepNodeLimit;
  }

  public int getFinalStepNodeLimit()
  {
    return super.getNodeLimit();
  }


  public void setSynchronousProductBuilder
    (final SynchronousProductBuilder builder)
  {
    mSynchronousProductBuilder = builder;
  }

  public void setMonolithicConflictChecker(final ConflictChecker checker)
  {
    mMonolithicConflictChecker = checker;
  }


  private SynchronousProductBuilder getSynchronousProductBuilder
    (final ProductDESProxy model)
  {
    if (mSynchronousProductBuilder == null) {
      final ProductDESProxyFactory factory = getFactory();
      mSynchronousProductBuilder =
        new MonolithicSynchronousProductBuilder(factory);
    }
    mSynchronousProductBuilder.setPropositions(mPropositions);
    final int limit = getInternalStepNodeLimit();
    mSynchronousProductBuilder.setNodeLimit(limit);
    mSynchronousProductBuilder.setModel(model);
    return mSynchronousProductBuilder;
  }

  private ConflictChecker getMonolithicConflictChecker
    (final ProductDESProxy model)
    throws EventNotFoundException
  {
    if (mMonolithicConflictChecker == null) {
      final ProductDESProxyFactory factory = getFactory();
      mMonolithicConflictChecker = new NativeConflictChecker(factory);
    }
    final int limit = getFinalStepNodeLimit();
    mMonolithicConflictChecker.setNodeLimit(limit);
    mMonolithicConflictChecker.setModel(model);
    final KindTranslator translator = getKindTranslator();
    mMonolithicConflictChecker.setKindTranslator(translator);
    final EventProxy marking = getUsedMarkingProposition();
    mMonolithicConflictChecker.setMarkingProposition(marking);
    return mMonolithicConflictChecker;
  }


  //#########################################################################
  //# Heuristics
  public HeuristicMinT createHeuristicMinT()
  {
    return new HeuristicMinT();
  }

  public HeuristicMaxS createHeuristicMaxS()
  {
    return new HeuristicMaxS();
  }

  public HeuristicMustL createHeuristicMustL()
  {
    return new HeuristicMustL();
  }

  public HeuristicMaxL createHeuristicMaxL()
  {
    return new HeuristicMaxL();
  }

  public HeuristicMaxC createHeuristicMaxC()
  {
    return new HeuristicMaxC();
  }

  public HeuristicMinS createHeuristicMinS()
  {
    return new HeuristicMinS();
  }

  public void setPreselectingHeuristic(final PreselectingHeuristic heuristic)
  {
    mPreselectingHeuristic = heuristic;
  }

  /**
   * Defines the preferred candidate selection heuristics.
   */
  public void setSelectingHeuristic(final SelectingHeuristic heuristic)
  {
    final List<SelectingHeuristic> list = new ArrayList<SelectingHeuristic>(3);
    list.add(heuristic);
    if (heuristic instanceof HeuristicMaxL) {
      list.add(new HeuristicMaxC());
      list.add(new HeuristicMinS());
    } else if (heuristic instanceof HeuristicMaxC) {
      list.add(new HeuristicMaxL());
      list.add(new HeuristicMinS());
    } else if (heuristic instanceof HeuristicMinS) {
      list.add(new HeuristicMaxL());
      list.add(new HeuristicMaxC());
    }
    setSelectingHeuristic(list);
  }

  /**
   * Defines the list of candidate selection heuristics in the chosen order.
   * @param heuristicList
   *          The first item in the list should be the first heuristic used to
   *          select a candidate to compose, the last item in the list should be
   *          the last option.
   */
  public void setSelectingHeuristic(final List<SelectingHeuristic> heuristicList)
  {
    mSelectingHeuristics = new SelectingComparator(heuristicList);
  }


  //#########################################################################
  //# Invocation
  public boolean run() throws AnalysisException
  {
    setUp();
    initialiseEventsToAutomata();
    simplifyInitialAutomata();

    Collection<Candidate> candidates;
    outer:
    do {
      candidates = mPreselectingHeuristic.findCandidates();
      while (!candidates.isEmpty()) {
        final Candidate candidate =
          Collections.min(candidates, mSelectingHeuristics);
        try {
          applyCandidate(candidate);
          break;
        } catch (final OverflowException overflow) {
          mNumOverflows++;
          if (mNumOverflows > MAX_OVERFLOWS) {
            break outer;
          }
          final List<AutomatonProxy> automata = candidate.getAutomata();
          mOverflowCandidates.add(automata);
          candidates.remove(candidate);
        }
      }
    } while (!candidates.isEmpty());

    final ProductDESProxy model = createCurrentModel();
    final ConflictChecker checker = getMonolithicConflictChecker(model);
    final boolean result = checker.run();
    if (result) {
      setSatisfiedResult();
    } else {
      final ConflictTraceProxy trace0 = checker.getCounterExample();
      final ConflictTraceProxy trace1 = expandTrace(trace0);
      setFailedResult(trace1);
    }

    tearDown();
    return result;
  }


  //#########################################################################
  //# Overrides for net.sourceforge.waters.model.AbstractModelAnalyser
  /**
   * Initialises required variables to default values if the user hasn't
   * configured them.
   */
  protected void setUp()
    throws AnalysisException
  {
    super.setUp();
    final EventProxy marking = getUsedMarkingProposition();
    mPropositions = Collections.singletonList(marking);
    if (mPreselectingHeuristic == null) {
      final PreselectingHeuristic defaultHeuristic = new HeuristicMustL();
      setPreselectingHeuristic(defaultHeuristic);
    }
    if (mSelectingHeuristics == null) {
      final SelectingHeuristic defaultHeuristic = new HeuristicMinS();
      setSelectingHeuristic(defaultHeuristic);
    }
    mModifyingSteps = new ArrayList<Step>();
    mNumOverflows = 0;
    mOverflowCandidates = new THashSet<List<AutomatonProxy>>();
  }

  protected void tearDown()
  {
    mPropositions = null;
    mSynchronousProductBuilder = null;
    mEventsToAutomata = null;
    mModifyingSteps = null;
    mOverflowCandidates = null;
    super.tearDown();
  }


  //#########################################################################
  //# Events+Automata Maps
  /**
   * Maps the events in the model to a set of the automata that contain the
   * event in their alphabet.
   */
  private void initialiseEventsToAutomata()
  {
    final ProductDESProxy model = getModel();
    final Collection<AutomatonProxy> automata = model.getAutomata();
    final int numAutomata = automata.size();
    final KindTranslator translator = getKindTranslator();
    mCurrentAutomata = new ArrayList<AutomatonProxy>(numAutomata);
    for (final AutomatonProxy aut : automata) {
      if (translator.getComponentKind(aut) != ComponentKind.PROPERTY) {
        mCurrentAutomata.add(aut);
      }
    }
    mEventsToAutomata =
        new HashMap<EventProxy,Set<AutomatonProxy>>(model.getEvents().size());
    for (final AutomatonProxy aut : mCurrentAutomata) {
      addEventsToAutomata(aut);
    }
  }

  private void updateEventsToAutomata(final AutomatonProxy autToAdd,
                                      final List<AutomatonProxy> autToRemove)
  {
    mCurrentAutomata.removeAll(autToRemove);
    mCurrentAutomata.add(autToAdd);
    addEventsToAutomata(autToAdd);
    removeEventsToAutomata(autToRemove);
  }

  private void addEventsToAutomata(final AutomatonProxy aut)
  {
    final KindTranslator translator = getKindTranslator();
    for (final EventProxy event : aut.getEvents()) {
      if (translator.getEventKind(event) != EventKind.PROPOSITION) {
        Set<AutomatonProxy> set = mEventsToAutomata.get(event);
        if (set == null) {
          set = new THashSet<AutomatonProxy>();
          mEventsToAutomata.put(event, set);
        }
        set.add(aut);
      }
    }
  }

  private void removeEventsToAutomata(final Collection<AutomatonProxy> victims)
  {
    final Set<EventProxy> eventsToRemove = new THashSet<EventProxy>();
    for (final Map.Entry<EventProxy,Set<AutomatonProxy>> entry :
         mEventsToAutomata.entrySet()) {
      final Set<AutomatonProxy> set = entry.getValue();
      set.removeAll(victims);
      if (set.isEmpty()) {
        final EventProxy event = entry.getKey();
        eventsToRemove.add(event);
      }
    }
    for (final EventProxy event : eventsToRemove) {
      mEventsToAutomata.remove(event);
    }
  }

  /**
   * Finds the set of events that are local to a candidate (i.e. a set of
   * automata).
   */
  private Set<EventProxy> identifyLocalEvents
    (final Collection<AutomatonProxy> candidate)
  {
    final Set<EventProxy> localEvents = new THashSet<EventProxy>();
    for (final EventProxy event : mEventsToAutomata.keySet()) {
      final Set<AutomatonProxy> autWithEvent = mEventsToAutomata.get(event);
      if (candidate.containsAll(autWithEvent)) {
        localEvents.add(event);
      }
    }
    return localEvents;
  }

  private boolean isPermissibleCandidate(final List<AutomatonProxy> automata)
  {
    return
      automata.size() < mCurrentAutomata.size() &&
      !mOverflowCandidates.contains(automata);
  }

  private ProductDESProxy createCurrentModel()
  {
    final ProductDESProxyFactory factory = getFactory();
    final ProductDESProxy model = getModel();
    final String name = model.getName();
    final Set<EventProxy> eventSet = new THashSet<EventProxy>();
    for (final AutomatonProxy aut : mCurrentAutomata) {
      eventSet.addAll(aut.getEvents());
    }
    final List<EventProxy> eventList = new ArrayList<EventProxy>(eventSet);
    Collections.sort(eventList);
    return factory.createProductDESProxy(name, eventList, mCurrentAutomata);
  }


  //#########################################################################
  //# Abstraction Steps
  private void simplifyInitialAutomata()
    throws AnalysisException
  {
    if (mCurrentAutomata.size() > 1) {
      final List<AutomatonProxy> automata = new LinkedList<AutomatonProxy>();
      final Map<AutomatonProxy,Set<EventProxy>> autToLocalEvents =
        new HashMap<AutomatonProxy,Set<EventProxy>>();
      for (final Map.Entry<EventProxy,Set<AutomatonProxy>> entry :
           mEventsToAutomata.entrySet()) {
        final Set<AutomatonProxy> autSet = entry.getValue();
        if (autSet.size() == 1) {
          final AutomatonProxy aut = autSet.iterator().next();
          Set<EventProxy> localEvents = autToLocalEvents.get(aut);
          if (localEvents == null) {
            localEvents = new THashSet<EventProxy>();
            autToLocalEvents.put(aut, localEvents);
            automata.add(aut);
          }
          final EventProxy event = entry.getKey();
          localEvents.add(event);
        }
      }
      Collections.sort(automata);
      for (final AutomatonProxy aut : automata) {
        final List<AutomatonProxy> singleton = Collections.singletonList(aut);
        final Set<EventProxy> localEvents = autToLocalEvents.get(aut);
        final Candidate candidate = new Candidate(singleton, localEvents);
        applyCandidate(candidate);
      }
    }
  }

  private void applyCandidate(final Candidate candidate)
    throws AnalysisException
  {
    final CompositionStep syncStep = composeSynchronousProduct(candidate);
    final EventProxy tau = syncStep.getHiddenEvent();
    AutomatonProxy autToAbstract = syncStep.getResultAutomaton();
    final ObservationEquivalenceStep oeStep =
      applyObservationEquivalence(autToAbstract, tau);
    mModifyingSteps.add(syncStep);
    if (oeStep != null) {
      autToAbstract = oeStep.getResultAutomaton();
      mModifyingSteps.add(oeStep);
    }
    updateEventsToAutomata(autToAbstract, candidate.getAutomata());
  }

  /**
   * Builds the synchronous product for a given candidate.
   */
  private CompositionStep composeSynchronousProduct(final Candidate candidate)
    throws AnalysisException
  {
    final ProductDESProxyFactory factory = getFactory();
    final ProductDESProxy des = candidate.createProductDESProxy(factory);
    final SynchronousProductBuilder builder = getSynchronousProductBuilder(des);
    final Collection<EventProxy> local = candidate.getLocalEvents();
    final EventProxy tau;
    if (local.isEmpty()) {
      tau = null;
    } else {
      final String tauname = "tau:" + des.getName();
      tau = factory.createEventProxy(tauname, EventKind.UNCONTROLLABLE, false);
      builder.addMask(local, tau);
    }
    try {
      builder.run();
      final AutomatonProxy sync = builder.getComputedAutomaton();
      final SynchronousProductStateMap stateMap = builder.getStateMap();
      return new CompositionStep(sync, local, tau, stateMap);
    } finally {
      builder.clearMask();
    }
  }

  private ObservationEquivalenceStep applyObservationEquivalence
    (final AutomatonProxy aut, final EventProxy tau)
    throws AnalysisException
  {
    final ObserverProjectionTransitionRelation rel =
        new ObserverProjectionTransitionRelation(aut, mPropositions);
    final int codeOfTau = rel.getEventInt(tau);
    final TransitionRelationSimplifier loopRemover =
      new TauLoopRemovalTRSimplifier(rel, codeOfTau);
    final boolean hadLoops = loopRemover.run();
    final TransitionRelationSimplifier bisimulator =
      new ObservationEquivalenceTRSimplifier(rel, codeOfTau);
    final boolean hadBisim = bisimulator.run();
    if (hadLoops || hadBisim) {
      final ProductDESProxyFactory factory = getFactory();
      final AutomatonProxy convertedAut = rel.createAutomaton(factory);
      final StateProxy[] inputMap = rel.getOriginalIntToStateMap();
      final TIntObjectHashMap<int[]> partition;
      if (!hadBisim) {
        partition = loopRemover.getStateClasses();
      } else if (!hadLoops) {
        partition = bisimulator.getStateClasses();
      } else {
        final TIntObjectHashMap<int[]> partition1 =
          loopRemover.getStateClasses();
        final TIntObjectHashMap<int[]> partition2 =
          bisimulator.getStateClasses();
        partition = combinePartitions(partition1, partition2);
      }
      final TObjectIntHashMap<StateProxy> outputMap =
        rel.getResultingStateToIntMap();
      return new ObservationEquivalenceStep(convertedAut, aut, tau,
                                            inputMap, partition, outputMap);
    } else {
      return null;
    }
  }

  private TIntObjectHashMap<int[]> combinePartitions
    (final TIntObjectHashMap<int[]> first,
     final TIntObjectHashMap<int[]> second)
  {
    final TIntObjectIterator<int[]> iter = second.iterator();
    while (iter.hasNext()) {
      iter.advance();
      final int[] class2 = iter.value();
      int count = 0;
      for (final int s2 : class2) {
        final int[] class1 = first.get(s2);
        count += class1.length;
      }
      final int[] newclass;
      if (class2.length == count) {
        newclass = class2;
      } else {
        newclass = new int[count];
        iter.setValue(newclass);
      }
      int i = 0;
      for (final int s2 : class2) {
        final int[] class1 = first.get(s2);
        for (final int s1 : class1) {
          newclass[i++] = s1;
        }
      }
    }
    return second;
  }


  //#########################################################################
  //# Trace Computation
  private ConflictTraceProxy expandTrace(final ConflictTraceProxy trace)
  {
    List<TraceStepProxy> traceSteps = getSaturatedTraceSteps(trace);
    final int size = mModifyingSteps.size();
    final ListIterator<Step> iter = mModifyingSteps.listIterator(size);
    while (iter.hasPrevious()) {
      final Step step = iter.previous();
      traceSteps = step.convertTraceSteps(traceSteps);
    }
    final ProductDESProxyFactory factory = getFactory();
    final ProductDESProxy model = getModel();
    final Collection<AutomatonProxy> automata = model.getAutomata();
    return factory.createConflictTraceProxy(trace.getName(),
                                            trace.getComment(),
                                            trace.getLocation(),
                                            model,
                                            automata,
                                            traceSteps,
                                            trace.getKind());
  }

  /**
   * Fills in the target states in the state maps for each step of the trace
   * for the result automaton.
   */
  private List<TraceStepProxy> getSaturatedTraceSteps
    (final ConflictTraceProxy trace)
  {
    final ProductDESProxyFactory factory = getFactory();
    final Collection<AutomatonProxy> automata = trace.getAutomata();
    final int numAutomata = automata.size();
    final List<TraceStepProxy> steps = trace.getTraceSteps();
    final int numSteps = steps.size();
    final List<TraceStepProxy> convertedSteps =
        new ArrayList<TraceStepProxy>(numSteps);
    final Iterator<TraceStepProxy> iter = steps.iterator();

    final TraceStepProxy firstStep = iter.next();
    final Map<AutomatonProxy,StateProxy> firstMap = firstStep.getStateMap();
    final Map<AutomatonProxy,StateProxy> convertedFirstMap =
      new HashMap<AutomatonProxy,StateProxy>(numAutomata);
    for (final AutomatonProxy aut : automata) {
      final StateProxy state = getInitialState(aut, firstMap);
      convertedFirstMap.put(aut, state);
    }
    final TraceStepProxy convertedFirstStep =
      factory.createTraceStepProxy(null, convertedFirstMap);
    convertedSteps.add(convertedFirstStep);
    Map<AutomatonProxy,StateProxy> previousStepMap = convertedFirstMap;
    while (iter.hasNext()) {
      final TraceStepProxy step = iter.next();
      final EventProxy event = step.getEvent();
      final Map<AutomatonProxy,StateProxy> stepMap = step.getStateMap();
      final Map<AutomatonProxy,StateProxy> convertedStepMap =
        new HashMap<AutomatonProxy,StateProxy>(numAutomata);
      for (final AutomatonProxy aut : automata) {
        final StateProxy prev = previousStepMap.get(aut);
        final StateProxy state = findSuccessor(aut, event, prev, stepMap);
        convertedStepMap.put(aut, state);
      }
      final TraceStepProxy convertedStep =
        factory.createTraceStepProxy(event, convertedStepMap);
      convertedSteps.add(convertedStep);
      previousStepMap = convertedStepMap;
    }
    return convertedSteps;
  }

  /**
   * Finds the initial state of an automaton in a trace.
   * A trace step's map is passed for the case of multiple initial states.
   */
  private StateProxy getInitialState
    (final AutomatonProxy aut, final Map<AutomatonProxy,StateProxy> stepMap)
  {
    // If there is more than one initial state, the trace has the info.
    StateProxy initial = stepMap.get(aut);
    // Otherwise there is only one initial state.
    if (initial == null) {
      for (final StateProxy state : aut.getStates()) {
        if (state.isInitial()) {
          initial = state;
          break;
        }
      }
    }
    return initial;
  }

  /**
   * Finds the successor state in trace, from a given state in an automaton.
   * A trace step's map is passed for the case of multiple successor states.
   */
  private StateProxy findSuccessor(final AutomatonProxy aut,
                                   final EventProxy event,
                                   final StateProxy sourceState,
                                   final Map<AutomatonProxy,StateProxy> stepMap)
  {
    // If there is more than one successor state, the trace has the info.
    final StateProxy targetState = stepMap.get(aut);
    // Otherwise there is only one successor state.
    if (targetState == null) {
      if (aut.getEvents().contains(event)) {
        for (final TransitionProxy trans : aut.getTransitions()) {
          if (trans.getEvent() == event && trans.getSource() == sourceState) {
            return trans.getTarget();
          }
        }
      } else {
        return sourceState;
      }
    }
    return targetState;
  }


  //#########################################################################
  //# Local Interface PreselectingHeuristic
  private interface PreselectingHeuristic
  {
    public Collection<Candidate> findCandidates();
  }


  //#########################################################################
  //# Inner Class HeuristicPairing
  private abstract class HeuristicPairing
    implements PreselectingHeuristic, Comparator<AutomatonProxy>
  {

    //#######################################################################
    //# Interface PreselectingHeuristic
    public Collection<Candidate> findCandidates()
    {
      final AutomatonProxy chosenAut = Collections.min(mCurrentAutomata, this);
      return pairAutomaton(chosenAut, mCurrentAutomata);
    }

    //#######################################################################
    //# Auxiliary Methods
    private Collection<Candidate> pairAutomaton
      (final AutomatonProxy chosenAut,
       final Collection<AutomatonProxy> automata)
    {
      final Set<EventProxy> chosenEvents =
        new THashSet<EventProxy>(chosenAut.getEvents());
      final Collection<Candidate> candidates = new LinkedList<Candidate>();
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
  //# Inner Class HeuristicPairing
  private class HeuristicMinT
    extends HeuristicPairing
  {

    //#######################################################################
    //# Interface java.util.Comparator<AutomatonProxy>
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
    extends HeuristicPairing
  {

    //#######################################################################
    //# Interface java.util.Comparator<AutomatonProxy>
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
    public Collection<Candidate> findCandidates()
    {
      final Collection<Candidate> candidates = new LinkedList<Candidate>();
      for (final Set<AutomatonProxy> set : mEventsToAutomata.values()) {
        assert set.size() > 0;
        if (set.size() > 1) {
          final List<AutomatonProxy> list = new ArrayList<AutomatonProxy>(set);
          Collections.sort(list);
          if (isPermissibleCandidate(list)) {
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
  //# Inner Class SelectionComparator
  private class SelectingComparator
    implements Comparator<Candidate>
  {

    //#######################################################################
    //# Constructor
    private SelectingComparator(final List<SelectingHeuristic> list)
    {
      mHeuristics = list;
    }

    //#######################################################################
    //# Interface java.util.Comparator<Candidate>
    public int compare(final Candidate cand1, final Candidate cand2)
    {
      for (final SelectingHeuristic heu : mHeuristics) {
        final int result = heu.compare(cand1, cand2);
        if (result != 0) {
          return result;
        }
      }
      return cand1.compareTo(cand2);
    }

    //#######################################################################
    //# Data Members
    private final List<SelectingHeuristic> mHeuristics;

  }


  //#########################################################################
  //# Inner Class SelectingHeuristic
  private abstract class SelectingHeuristic
    implements Comparator<Candidate>
  {

    //#######################################################################
    //# Interface java.util.Comparator<Candidate>
    public int compare(final Candidate cand1, final Candidate cand2)
    {
      final double heu1 = getHeuristicValue(cand1);
      final double heu2 = getHeuristicValue(cand2);
      if (heu1 < heu2) {
        return -1;
      } else if (heu1 > heu2) {
        return 1;
      } else {
        return 0;
      }
    }

    //#######################################################################
    //# Auxiliary Methods
    abstract double getHeuristicValue(final Candidate candidate);

  }


  //#########################################################################
  //# Inner Class HeuristicMaxL
  /**
   * Performs step 2 of the approach to select the automata to compose. The
   * chosen candidate is the one with the highest proportion of local events.
   */
  private class HeuristicMaxL extends SelectingHeuristic
  {

    //#######################################################################
    //# Overrides for SelectingHeuristic
    double getHeuristicValue(final Candidate candidate)
    {
      return - (double) candidate.getLocalEventCount() /
               (double) candidate.getNumberOfEvents();
    }

  }


  //#########################################################################
  //# Inner Class HeuristicMaxC
  /**
   * Performs step 2 of the approach to select the automata to compose. The
   * chosen candidate is the one with the highest proportion of common events.
   */
  private class HeuristicMaxC extends SelectingHeuristic
  {

    //#######################################################################
    //# Overrides for SelectingHeuristic
    double getHeuristicValue(final Candidate candidate)
    {
      final int local = candidate.getLocalEventCount();
      final int total = candidate.getLocalEventCount();
      return - (double) (total - local) / (double) total;
    }

  }


  //#########################################################################
  //# Inner Class HeuristicMinS
  private class HeuristicMinS extends SelectingHeuristic
  {

    //#######################################################################
    //# Overrides for SelectingHeuristic
    double getHeuristicValue(final Candidate candidate)
    {
      double product = 1.0;
      for (final AutomatonProxy aut : candidate.getAutomata()) {
        product *= aut.getStates().size();
      }
      final double totalEvents = candidate.getNumberOfEvents();
      final double localEvents = candidate.getLocalEventCount();
      return product * (totalEvents - localEvents) / totalEvents;
    }

  }


  //#########################################################################
  //# Inner Class Step
  private abstract class Step
  {

    //#######################################################################
    //# Constructors
    Step(final AutomatonProxy aut, final Collection<AutomatonProxy> originals)
    {
      mResultAutomaton = aut;
      mOriginalAutomata = originals;
    }

    Step(final AutomatonProxy resultAut, final AutomatonProxy originalAut)
    {
      this(resultAut, Collections.singletonList(originalAut));
    }

    //#######################################################################
    //# Simple Access
    AutomatonProxy getResultAutomaton()
    {
      return mResultAutomaton;
    }

    Collection<AutomatonProxy> getOriginalAutomata()
    {
      return mOriginalAutomata;
    }

    AutomatonProxy getOriginalAutomaton()
    {
      if (mOriginalAutomata.size() == 1) {
        return mOriginalAutomata.iterator().next();
      } else {
        throw new IllegalStateException(
            "Attempting to get a single input automaton from "
                + ProxyTools.getShortClassName(this) + " with "
                + mOriginalAutomata.size() + " input automata!");
      }
    }

    //#######################################################################
    //# Trace Computation
    /**
     * Assumes that a saturated trace is being passed.
     */
    abstract List<TraceStepProxy> convertTraceSteps
      (final List<TraceStepProxy> steps);

    //#######################################################################
    //# Data Members
    private final AutomatonProxy mResultAutomaton;
    private final Collection<AutomatonProxy> mOriginalAutomata;

  }


  // #########################################################################
  // # Inner Class CompositionStep
  private class CompositionStep extends Step
  {

    //#######################################################################
    //# Constructor
    private CompositionStep(final AutomatonProxy composedAut,
                            final Collection<EventProxy> localEvents,
                            final EventProxy tau,
                            final SynchronousProductStateMap stateMap)
    {
      super(composedAut, stateMap.getInputAutomata());
      mLocalEvents = localEvents;
      mHiddenEvent = tau;
      mStateMap = stateMap;
    }

    //#######################################################################
    //# Simple Access
    EventProxy getHiddenEvent()
    {
      return mHiddenEvent;
    }

    //#######################################################################
    //# Trace Computation
    List<TraceStepProxy> convertTraceSteps(final List<TraceStepProxy> steps)
    {
      final ProductDESProxyFactory factory = getFactory();
      final AutomatonProxy resultAutomaton = getResultAutomaton();
      final Collection<AutomatonProxy> originalAutomata = getOriginalAutomata();
      final int convertedNumAutomata =
        steps.iterator().next().getStateMap().size() +
        originalAutomata.size() - 1;
      final int numSteps = steps.size();
      final List<TraceStepProxy> convertedSteps =
          new ArrayList<TraceStepProxy>(numSteps);
      Map<AutomatonProxy,StateProxy> previousMap = null;
      for (final TraceStepProxy step : steps) {
        final Map<AutomatonProxy,StateProxy> stepMap = step.getStateMap();
        final Map<AutomatonProxy,StateProxy> convertedStepMap =
          new HashMap<AutomatonProxy,StateProxy>(convertedNumAutomata);
        convertedStepMap.putAll(stepMap);
        convertedStepMap.remove(resultAutomaton);
        final StateProxy convertedState = stepMap.get(resultAutomaton);
        for (final AutomatonProxy aut : originalAutomata) {
          final StateProxy originalState =
            mStateMap.getOriginalState(convertedState, aut);
          convertedStepMap.put(aut, originalState);
        }
        EventProxy event = step.getEvent();
        if (event != null && event == mHiddenEvent) {
          event = findEvent(previousMap, convertedStepMap);
        }
        final TraceStepProxy convertedStep =
          factory.createTraceStepProxy(event, convertedStepMap);
        convertedSteps.add(convertedStep);
        previousMap = convertedStepMap;
      }
      return convertedSteps;
    }

    private EventProxy findEvent(final Map<AutomatonProxy,StateProxy> sources,
                                 final Map<AutomatonProxy,StateProxy> targets)
    {
      final Collection<EventProxy> possible =
        new LinkedList<EventProxy>(mLocalEvents);
      for (final AutomatonProxy aut : getOriginalAutomata()) {
        if (possible.size() <= 1) {
          break;
        }
        final StateProxy source = sources.get(aut);
        final StateProxy target = targets.get(aut);
        final Collection<EventProxy> alphabet =
          new THashSet<EventProxy>(aut.getEvents());
        final int size = alphabet.size();
        final Collection<EventProxy> retained = new THashSet<EventProxy>(size);
        for (final TransitionProxy trans : aut.getTransitions()) {
          if (trans.getSource() == source && trans.getTarget() == target) {
            final EventProxy event = trans.getEvent();
            retained.add(event);
          }
        }
        final Iterator<EventProxy> iter = possible.iterator();
        while (iter.hasNext()) {
          final EventProxy event = iter.next();
          if (alphabet.contains(event)) {
            if (!retained.contains(event)) {
              iter.remove();
            }
          } else {
            if (source != target) {
              iter.remove();
            }
          }
        }
      }
      return possible.iterator().next();
    }

    //#######################################################################
    //# Data Members
    private final Collection<EventProxy> mLocalEvents;
    private final EventProxy mHiddenEvent;
    private final SynchronousProductStateMap mStateMap;
  }


  //#########################################################################
  //# Inner Class ObservationEquivalenceStep
  private class ObservationEquivalenceStep extends Step
  {

    //#######################################################################
    //# Constructor
    private ObservationEquivalenceStep
      (final AutomatonProxy resultAut,
       final AutomatonProxy originalAut,
       final EventProxy tau,
       final StateProxy[] originalStates,
       final TIntObjectHashMap<int[]> partition,
       final TObjectIntHashMap<StateProxy> reverseOutputStateMap)
    {
      super(resultAut, originalAut);
      mOriginalStates = originalStates;
      mClassMap = partition;
      mReverseOutputStateMap = reverseOutputStateMap;
      mTransitionRelation =
        new ObserverProjectionTransitionRelation(originalAut, mPropositions);
      mCodeOfTau = mTransitionRelation.getEventInt(tau);
    }

    //#######################################################################
    //# Trace Computation
    List<TraceStepProxy> convertTraceSteps
      (final List<TraceStepProxy> traceSteps)
    {
      final ProductDESProxyFactory factory = getFactory();
      final AutomatonProxy originalAutomaton = getOriginalAutomaton();
      final AutomatonProxy resultAutomaton = getResultAutomaton();
      final List<TraceStepProxy> convertedSteps =
        new LinkedList<TraceStepProxy>();

      // Make the trace begin in the correct initial state ...
      final TIntArrayList initialStates =
        mTransitionRelation.getAllInitialStates();
      final Iterator<TraceStepProxy> iter = traceSteps.iterator();
      final TraceStepProxy initialStep = iter.next();
      Map<AutomatonProxy,StateProxy> stepsNewStateMap =
        new HashMap<AutomatonProxy,StateProxy>(initialStep.getStateMap());
      final StateProxy tracesInitialState =
        stepsNewStateMap.get(resultAutomaton);
      stepsNewStateMap.remove(resultAutomaton);
      final int tracesInitialStateID =
        mReverseOutputStateMap.get(tracesInitialState);
      final List<SearchRecord> initialRecords =
        beginTrace(initialStates, tracesInitialStateID);
      assert initialRecords.size() > 0;
      appendTraceSteps(initialRecords, stepsNewStateMap, convertedSteps);

      // Append internal steps, with intermittent tau as needed ...
      int originalSourceID =
        initialRecords.get(initialRecords.size() - 1).getState();
      while (iter.hasNext()) {
        final TraceStepProxy step = iter.next();
        final EventProxy stepEvent = step.getEvent();
        final int eventID = mTransitionRelation.getEventInt(stepEvent);
        final Map<AutomatonProxy,StateProxy> stepsStateMap =
          step.getStateMap();
        if (eventID < 0) {
          // The event is not in the automaton being simplified:
          // add an idle step, using the new states of the other automata.
          stepsNewStateMap =
            new HashMap<AutomatonProxy,StateProxy>(stepsStateMap);
          stepsNewStateMap.remove(resultAutomaton);
          final StateProxy state = mOriginalStates[originalSourceID];
          stepsNewStateMap.put(originalAutomaton, state);
          final TraceStepProxy convertedStep =
            factory.createTraceStepProxy(stepEvent, stepsNewStateMap);
          convertedSteps.add(convertedStep);
        } else if (eventID == mCodeOfTau) {
          // The event is tau: find and append a tau*-only trace
          final Map<AutomatonProxy,StateProxy> stepsAfterStateMap =
            stepsStateMap;
          final StateProxy resultTargetState =
            stepsAfterStateMap.get(resultAutomaton);
          final List<SearchRecord> subtrace =
            findSubTrace(originalSourceID, eventID,
                         mReverseOutputStateMap.get(resultTargetState));
          appendTraceSteps(subtrace, stepsNewStateMap, convertedSteps);
          final int subsize = subtrace.size();
          if (subsize > 0) {
            originalSourceID = subtrace.get(subsize - 1).getState();
          }
        } else {
          // The event is non-tau:
          // find and append a tau*-event-tau* trace.
          final Map<AutomatonProxy,StateProxy> stepsAfterStateMap =
            new HashMap<AutomatonProxy,StateProxy>(stepsStateMap);
          final StateProxy resultTargetState =
            stepsAfterStateMap.get(resultAutomaton);
          assert resultTargetState != null;
          stepsAfterStateMap.remove(resultAutomaton);
          final List<SearchRecord> subtrace =
            findSubTrace(originalSourceID, eventID,
                         mReverseOutputStateMap.get(resultTargetState));
          appendTraceSteps(subtrace, stepEvent, stepsNewStateMap,
                           stepsAfterStateMap, convertedSteps);
          stepsNewStateMap = stepsAfterStateMap;
          final int subsize = subtrace.size();
          originalSourceID = subtrace.get(subsize - 1).getState();
        }
      }

      return convertedSteps;
    }

    /**
     * Creates the beginning of a trace by doing a breadth-first search to find
     * the correct initial state of the original automaton. Steps are added for
     * tau transitions (if necessary) until the initial state of the result
     * automaton is reached.
     *
     * @return A list of SearchRecords that represent each extra step needed
     *         for the start of the trace. (The first item being the very first
     *         state of the trace).
     */
    private List<SearchRecord> beginTrace(final TIntArrayList initialStateIDs,
                                          final int resultAutInitialStateClass)
    {
      final int[] targetArray = mClassMap.get(resultAutInitialStateClass);
      final TIntHashSet targetSet = new TIntHashSet(targetArray);
      final Queue<SearchRecord> open = new ArrayDeque<SearchRecord>();
      final TIntHashSet visited = new TIntHashSet();
      // The dummy record ensures that the first real search record will
      // later be included in the trace.
      final SearchRecord dummy = new SearchRecord(-1);
      final int numInit = initialStateIDs.size();
      for (int i = 0; i < numInit; i++) {
        final int initStateID = initialStateIDs.get(i);
        final SearchRecord record =
            new SearchRecord(initStateID, false, -1, dummy);
        if (targetSet.contains(initStateID)) {
          return Collections.singletonList(record);
        }
        open.add(record);
        visited.add(initStateID);
      }
      while (true) {
        final SearchRecord current = open.remove();
        final int source = current.getState();
        final TIntHashSet successors =
            mTransitionRelation.getSuccessors(source, mCodeOfTau);
        if (successors != null) {
          final TIntIterator iter = successors.iterator();
          while (iter.hasNext()) {
            final int target = iter.next();
            if (!visited.contains(target)) {
              final SearchRecord record =
                  new SearchRecord(target, false, mCodeOfTau, current);
              if (targetSet.contains(target)) {
                return buildSearchRecordTrace(record);
              }
              open.add(record);
              visited.add(target);
            }
          }
        }
      }
    }

    /**
     * Finds a partial trace in the original automaton before observation
     * equivalence. This method computes a sequence of tau transitions, followed
     * by a transition with the given event, followed by another sequence of tau
     * transitions linking the source state to some state in the class of the
     * target state in the simplified automaton.
     *
     * @param originalSource
     *          State number of the source state in the original automaton.
     * @param event
     *          Integer code of the event to be included in the trace.
     * @param targetClass
     *          State number of the state in the simplified automaton (code of
     *          state class).
     * @return List of search records describing the trace from source to
     *         target. The first entry in the list represents the first step
     *         after the source state, with its event and target state. The
     *         final step has a target state in the given target class. Events
     *         in the list can only be tau or the given event.
     */
    private List<SearchRecord> findSubTrace(final int originalSource,
                                            final int event,
                                            final int targetClass)
    {
      final int[] targetArray = mClassMap.get(targetClass);
      final TIntHashSet targetSet = new TIntHashSet(targetArray);
      final Queue<SearchRecord> open = new ArrayDeque<SearchRecord>();
      final TIntHashSet visited0 = new TIntHashSet(); // event not in trace
      final TIntHashSet visited1 = new TIntHashSet(); // event in trace
      // The given event may be tau. In this case, we must search for a
      // (possibly empty) string of tau events. This is achieved here by
      // by creating a first search record with the 'hasevent' property,
      // i.e., pretending the trace already has an event.
      SearchRecord record;
      if (event != mCodeOfTau) {
        record = new SearchRecord(originalSource);
        visited0.add(originalSource);
      } else if (!targetSet.contains(originalSource)) {
        record = new SearchRecord(originalSource, true, -1, null);
        visited1.add(originalSource);
      } else {
        return Collections.emptyList();
      }
      open.add(record);
      while (true) {
        final SearchRecord current = open.remove();
        final int source = current.getState();
        final boolean hasEvent = current.hasProperEvent();
        final TIntHashSet visited = hasEvent ? visited1 : visited0;
        if (mCodeOfTau >= 0) {
          final TIntHashSet successors =
            mTransitionRelation.getSuccessors(source, mCodeOfTau);
          if (successors != null) {
            final TIntIterator iter = successors.iterator();
            while (iter.hasNext()) {
              final int target = iter.next();
              if (!visited.contains(target)) {
                record = new SearchRecord(target, hasEvent, mCodeOfTau, current);
                if (hasEvent && targetSet.contains(target)) {
                  return buildSearchRecordTrace(record);
                }
                open.add(record);
                visited.add(target);
              }
            }
          }
        }
        if (!hasEvent) {
          final TIntHashSet successors =
            mTransitionRelation.getSuccessors(source, event);
          if (successors != null) {
            final TIntIterator iter = successors.iterator();
            while (iter.hasNext()) {
              final int target = iter.next();
              if (!visited1.contains(target)) {
                record = new SearchRecord(target, true, event, current);
                if (targetSet.contains(target)) {
                  return buildSearchRecordTrace(record);
                }
                open.add(record);
                visited1.add(target);
              }
            }
          }
        }
      }
    }

    private List<SearchRecord> buildSearchRecordTrace(SearchRecord record)
    {
      final List<SearchRecord> trace = new LinkedList<SearchRecord>();
      do {
        trace.add(0, record);
        record = record.getPredecessor();
      } while (record.getPredecessor() != null);
      return trace;
    }

    /**
     * Given a list of SearchRecord objects, a list of {@link TraceStepProxy}
     * objects is created and appended to a given list.
     * A {@link TraceStepProxy} is created for each SearchRecord.
     * @param recordTrace
     *          The list of search records to convert into steps of a trace.
     * @param stepsStateMap
     *          The state map for the step before adding the new information.
     * @param outputTrace
     *          Trace steps created are appended to this list.
     */
    private void appendTraceSteps
      (final List<SearchRecord> recordTrace,
       final Map<AutomatonProxy,StateProxy> stepsStateMap,
       final List<TraceStepProxy> outputTrace)
    {
      appendTraceSteps(recordTrace, null,
                       stepsStateMap, stepsStateMap, outputTrace);
    }

    private void appendTraceSteps
      (final List<SearchRecord> recordTrace,
       final EventProxy event,
       final Map<AutomatonProxy,StateProxy> beforeEventStateMap,
       final Map<AutomatonProxy,StateProxy> afterEventStateMap,
       final List<TraceStepProxy> outputTrace)
    {
      final ProductDESProxyFactory factory = getFactory();
      final AutomatonProxy originalAutomaton = getOriginalAutomaton();
      Map<AutomatonProxy,StateProxy> currentStateMap = beforeEventStateMap;
      for (final SearchRecord record : recordTrace) {
        final int subStepEventID = record.getEvent();
        final EventProxy stepEvent;
        if (subStepEventID >= 0) {
          stepEvent = mTransitionRelation.getEvent(subStepEventID);
          if (stepEvent == event) {
            currentStateMap = afterEventStateMap;
          }
        } else {
          stepEvent = null;
        }
        final int subStepTargetStateID = record.getState();
        currentStateMap.put(originalAutomaton,
                            mOriginalStates[subStepTargetStateID]);
        final TraceStepProxy convertedStep =
          factory.createTraceStepProxy(stepEvent, currentStateMap);
        outputTrace.add(convertedStep);
      }
    }

    //#######################################################################
    //# Data Members
    /**
     * Array of original states. Maps state codes in the input
     * transition relation to state objects in the input automaton.
     */
    private final StateProxy[] mOriginalStates;
    /**
     * Maps state codes of the output transition relation to list of state codes
     * in input transition relation. This gives the class of states merged to
     * form the given state in the simplified automaton.
     */
    private final TIntObjectHashMap<int[]> mClassMap;
    /**
     * Reverse encoding of output states. Maps states in output automaton
     * (simplified automaton) to state code in output transition relation.
     */
    private final TObjectIntHashMap<StateProxy> mReverseOutputStateMap;

    private final ObserverProjectionTransitionRelation mTransitionRelation;
    private final int mCodeOfTau;
  }


  // #########################################################################
  // # Inner Class SearchRecord
  private static class SearchRecord
  {

    //#######################################################################
    //# Constructors
    SearchRecord(final int state)
    {
      this(state, false, -1, null);
    }

    SearchRecord(final int state, final boolean hasEvent, final int event,
                 final SearchRecord pred)
    {
      mState = state;
      mHasProperEvent = hasEvent;
      mEvent = event;
      mPredecessor = pred;
    }

    //#######################################################################
    //# Getters
    boolean hasProperEvent()
    {
      return mHasProperEvent;
    }

    int getState()
    {
      return mState;
    }

    SearchRecord getPredecessor()
    {
      return mPredecessor;
    }

    int getEvent()
    {
      return mEvent;
    }

    //#######################################################################
    //# Data Members
    private final int mState;
    private final boolean mHasProperEvent;
    private final int mEvent;
    private final SearchRecord mPredecessor;
  }


  //#########################################################################
  //# Data Members
  private PreselectingHeuristic mPreselectingHeuristic;
  private Comparator<Candidate> mSelectingHeuristics;

  private Collection<EventProxy> mPropositions;
  private SynchronousProductBuilder mSynchronousProductBuilder;
  private ConflictChecker mMonolithicConflictChecker;
  private int mInternalStepNodeLimit;

  private Collection<AutomatonProxy> mCurrentAutomata;
  private Map<EventProxy,Set<AutomatonProxy>> mEventsToAutomata =
      new HashMap<EventProxy,Set<AutomatonProxy>>();
  private List<Step> mModifyingSteps;
  private Set<List<AutomatonProxy>> mOverflowCandidates;
  private int mNumOverflows;


  //#########################################################################
  //# Class Constants
  private static final int MAX_OVERFLOWS = 50;

}
