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
import gnu.trove.TIntIntHashMap;
import gnu.trove.TObjectByteHashMap;
import gnu.trove.TObjectByteIterator;
import gnu.trove.TObjectIntHashMap;
import java.util.ArrayDeque;
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
import java.util.Queue;
import java.util.Set;

import net.sourceforge.waters.analysis.gnonblocking.Candidate;
import net.sourceforge.waters.analysis.monolithic.
  MonolithicSynchronousProductBuilder;
import net.sourceforge.waters.cpp.analysis.NativeConflictChecker;
import net.sourceforge.waters.model.analysis.AbstractConflictChecker;
import net.sourceforge.waters.model.analysis.AnalysisException;
import net.sourceforge.waters.model.analysis.ConflictChecker;
import net.sourceforge.waters.model.analysis.EventNotFoundException;
import net.sourceforge.waters.model.analysis.KindTranslator;
import net.sourceforge.waters.model.analysis.OverflowException;
import net.sourceforge.waters.model.analysis.SynchronousProductBuilder;
import net.sourceforge.waters.model.analysis.SynchronousProductStateMap;
import net.sourceforge.waters.model.analysis.VerificationResult;
import net.sourceforge.waters.model.base.ProxyTools;
import net.sourceforge.waters.model.base.WatersRuntimeException;
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

public class OPConflictChecker
  extends AbstractConflictChecker
{

  //#########################################################################
  //# Constructors
  /**
   * Creates a new conflict checker without a model or marking proposition.
   */
  public OPConflictChecker(final ProductDESProxyFactory factory)
  {
    this(null, factory);
  }

  /**
   * Creates a new conflict checker to check whether the given model is
   * nonblocking with respect to the default marking.
   * @param model
   *          The model to be checked by this conflict checker.
   * @param factory
   *          Factory used for trace construction.
   */
  public OPConflictChecker(final ProductDESProxy model,
                           final ProductDESProxyFactory factory)
  {
    this(model, null, factory);
  }

  /**
   * Creates a new conflict checker to check whether the given model is
   * nonblocking.
   * @param model
   *          The model to be checked by this conflict checker.
   * @param marking
   *          The proposition event that defines which states are marked. Every
   *          state has a list of propositions attached to it; the conflict
   *          checker considers only those states as marked that are labelled by
   *          <CODE>marking</CODE>, i.e., their list of propositions must
   *          contain this event (exactly the same object).
   * @param factory
   *          Factory used for trace construction.
   */
  public OPConflictChecker(final ProductDESProxy model,
                           final EventProxy marking,
                           final ProductDESProxyFactory factory)
  {
    super(model, marking, factory);
    mMethod = Method.OEQ;
    mInternalStepNodeLimit = super.getNodeLimit();
    mInternalStepTransitionLimit = super.getTransitionLimit();
  }


  //#########################################################################
  //# Configuration
  public void setMethod(final Method method)
  {
    mMethod = method;
  }

  public Method getMethod()
  {
    return mMethod;
  }

  public void setNodeLimit(final int limit)
  {
    setInternalStepNodeLimit(limit);
    setFinalStepNodeLimit(limit);
  }

  public void setInternalStepNodeLimit(final int limit)
  {
    mInternalStepNodeLimit = limit;
  }

  public void setFinalStepNodeLimit(final int limit)
  {
    super.setNodeLimit(limit);
  }

  public int getNodeLimit()
  {
    final int limit1 = getInternalStepNodeLimit();
    final int limit2 = getFinalStepNodeLimit();
    return Math.max(limit1, limit2);
  }

  public int getInternalStepNodeLimit()
  {
    return mInternalStepNodeLimit;
  }

  public int getFinalStepNodeLimit()
  {
    return super.getNodeLimit();
  }

  @Override
  public void setTransitionLimit(final int limit)
  {
    super.setTransitionLimit(limit);
    setInternalStepTransitionLimit(limit);
  }

  public void setInternalStepTransitionLimit(final int limit)
  {
    mInternalStepTransitionLimit = limit;
  }

  public void setFinalStepTransitionLimit(final int limit)
  {
    super.setTransitionLimit(limit);
  }

  public int getInternalStepTransitionLimit()
  {
    return mInternalStepTransitionLimit;
  }

  public int getFinalStepTransitionLimit()
  {
    return super.getTransitionLimit();
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


  private SynchronousProductBuilder setupSynchronousProductBuilder()
  {
    if (mCurrentSynchronousProductBuilder == null) {
      if (mSynchronousProductBuilder == null) {
        final ProductDESProxyFactory factory = getFactory();
        mCurrentSynchronousProductBuilder =
          new MonolithicSynchronousProductBuilder(factory);
      } else {
        mCurrentSynchronousProductBuilder = mSynchronousProductBuilder;
      }
      mCurrentSynchronousProductBuilder.setPropositions(mPropositions);
      final int nlimit = getInternalStepNodeLimit();
      mCurrentSynchronousProductBuilder.setNodeLimit(nlimit);
      final int tlimit = getInternalStepTransitionLimit();
      mCurrentSynchronousProductBuilder.setTransitionLimit(tlimit);
    }
    return mCurrentSynchronousProductBuilder;
  }

  private ConflictChecker setupMonolithicConflictChecker()
    throws EventNotFoundException
  {
    if (mCurrentMonolithicConflictChecker == null) {
      if (mMonolithicConflictChecker == null) {
        final ProductDESProxyFactory factory = getFactory();
        mCurrentMonolithicConflictChecker = new NativeConflictChecker(factory);
      } else {
        mCurrentMonolithicConflictChecker = mMonolithicConflictChecker;
      }
      final int nlimit = getFinalStepNodeLimit();
      mCurrentMonolithicConflictChecker.setNodeLimit(nlimit);
      final int tlimit = getFinalStepTransitionLimit();
      mCurrentMonolithicConflictChecker.setTransitionLimit(tlimit);
      final KindTranslator translator = getKindTranslator();
      mCurrentMonolithicConflictChecker.setKindTranslator(translator);
      final EventProxy marking = getUsedMarkingProposition();
      mCurrentMonolithicConflictChecker.setMarkingProposition(marking);
    }
    return mCurrentMonolithicConflictChecker;
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
    try {
      setUp();
      initialiseEventsToAutomata();
      simplify(true);

      Collection<Candidate> candidates;
      boolean nonblocking = true;
      outer:
      do {
        subsystem:
        do {
          if (isTriviallyNonblockingSubsystem()) {
            continue outer;
          }
          candidates = mPreselectingHeuristic.findCandidates();
          while (!candidates.isEmpty()) {
            final Candidate candidate =
              Collections.min(candidates, mSelectingHeuristics);
            try {
              mEventHasDisappeared = false;
              applyCandidate(candidate);
              simplify(mEventHasDisappeared);
              break;
            } catch (final OverflowException overflow) {
              if (mNumOverflows++ >= MAX_OVERFLOWS) {
                break subsystem;
              }
              final List<AutomatonProxy> automata = candidate.getAutomata();
              mOverflowCandidates.add(automata);
              candidates.remove(candidate);
            }
          }
        } while (!candidates.isEmpty());
        nonblocking = runMonolithicConflictCheck();
      } while (nonblocking && popEventDisjointSubsystem());

      if (nonblocking) {
        return setSatisfiedResult();
      } else {
        restoreAutomata();
        final ConflictTraceProxy trace0 =
          mCurrentMonolithicConflictChecker.getCounterExample();
        final ConflictTraceProxy trace1 = expandTrace(trace0);
        return setFailedResult(trace1);
      }
    } catch (final AnalysisException exception) {
      throw setExceptionResult(exception);
    } finally {
      tearDown();
    }
  }


  //#########################################################################
  //# Overrides for net.sourceforge.waters.model.AbstractModelAnalyser
  /**
   * Initialises required variables to default values if the user has not
   * configured them.
   */
  protected void setUp()
    throws AnalysisException
  {
    super.setUp();
    final VerificationResult result = getAnalysisResult();
    result.setNumberOfStates(0.0);
    result.setNumberOfStates(0.0);
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
    switch (mMethod) {
    case OEQ:
      mAbstractionRule = new ObservationEquivalenceAbstractionRule
        (ObservationEquivalenceTRSimplifier.Equivalence.
         OBSERVATION_EQUIVALENCE);
      break;
    case OP:
      mAbstractionRule = new ObserverProjectionAbstractionRule();
      break;
    case OPSEARCH:
      mAbstractionRule = new OPSearchAbstractionRule();
      break;
    case WOEQ:
      mAbstractionRule = new ObservationEquivalenceAbstractionRule
        (ObservationEquivalenceTRSimplifier.Equivalence.
         WEAK_OBSERVATION_EQUIVALENCE);
      break;
    default:
      throw new IllegalStateException("Unknown method " + mMethod + " in " +
                                      ProxyTools.getShortClassName(this) + "!");
    }
    setupSynchronousProductBuilder();
    setupMonolithicConflictChecker();
    mModifyingSteps = new ArrayList<AbstractionStep>();
    mNumOverflows = 0;
    mOverflowCandidates = new THashSet<List<AutomatonProxy>>();
  }

  protected void tearDown()
  {
    mPropositions = null;
    mCurrentSynchronousProductBuilder = null;
    mCurrentMonolithicConflictChecker = null;
    mCurrentAutomata = null;
    mCurrentEvents = null;
    mEventInfoMap = null;
    mDirtyAutomata = null;
    mRedundantEvents = null;
    mPostponedSubsystems = null;
    mProcessedAutomata = null;
    mModifyingSteps = null;
    mUsedEventNames = null;
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
    final int numEvents = model.getEvents().size();
    mCurrentEvents = new THashSet<EventProxy>(numEvents);
    mEventInfoMap = new HashMap<EventProxy,EventInfo>(numEvents);
    mDirtyAutomata = new LinkedList<AutomatonProxy>();
    for (final AutomatonProxy aut : automata) {
      if (translator.getComponentKind(aut) != ComponentKind.PROPERTY) {
        mCurrentAutomata.add(aut);
        addEventsToAutomata(aut);
        mDirtyAutomata.add(aut);
      }
    }
    mUsedEventNames = new THashSet<String>(numEvents + numAutomata);
    for (final EventProxy event : mCurrentEvents) {
      final String name = event.getName();
      mUsedEventNames.add(name);
    }
    mRedundantEvents = new LinkedList<EventProxy>();
    for (final Map.Entry<EventProxy,EventInfo> entry :
         mEventInfoMap.entrySet()) {
      final EventInfo info = entry.getValue();
      if (info.isRemovable()) {
        final EventProxy event = entry.getKey();
        mRedundantEvents.add(event);
      }
    }
    mPostponedSubsystems = new LinkedList<SubSystem>();
    mProcessedAutomata = new LinkedList<AutomatonProxy>();
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
    final Collection<EventProxy> events = aut.getEvents();
    final int numEvents = events.size();
    final TObjectByteHashMap<EventProxy> statusMap =
      new TObjectByteHashMap<EventProxy>(numEvents);
    for (final TransitionProxy trans : aut.getTransitions()) {
      final EventProxy event = trans.getEvent();
      if (trans.getSource() != trans.getTarget()) {
        statusMap.put(event, NOT_ONLY_SELFLOOP);
      } else if (!statusMap.containsKey(event)) {
        statusMap.put(event, ONLY_SELFLOOP);
      }
    }
    final KindTranslator translator = getKindTranslator();
    for (final EventProxy event : events) {
      if (translator.getEventKind(event) != EventKind.PROPOSITION) {
        EventInfo info = mEventInfoMap.get(event);
        if (info == null) {
          info = new EventInfo();
          mEventInfoMap.put(event, info);
          mCurrentEvents.add(event);
        }
        final byte lookup = statusMap.get(event);
        final byte status = lookup == UNKNOWN_SELFLOOP ? BLOCKED : lookup;
        info.addAutomaton(aut, status);
      }
    }
  }

  private void removeEventsToAutomata(final Collection<AutomatonProxy> victims)
  {
    mRedundantEvents.clear();
    final Set<EventProxy> eventsToRemove = new THashSet<EventProxy>();
    for (final EventProxy event : mCurrentEvents) {
      final EventInfo info = mEventInfoMap.get(event);
      info.removeAutomata(victims);
      if (info.isEmpty()) {
        eventsToRemove.add(event);
      } else if (info.isRemovable()) {
        mRedundantEvents.add(event);
      }
    }
    for (final EventProxy event : eventsToRemove) {
      mCurrentEvents.remove(event);
      mEventInfoMap.remove(event);
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
    for (final EventProxy event : mCurrentEvents) {
      final EventInfo info = mEventInfoMap.get(event);
      if (info.containedIn(candidate)) {
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

  private boolean removeRedundantEvents()
  {
    if (mRedundantEvents.isEmpty()) {
      return false;
    } else {
      final int numRedundant = mRedundantEvents.size();
      final Set<EventProxy> redundant = new THashSet<EventProxy>(numRedundant);
      for (final EventProxy event : mRedundantEvents) {
        redundant.add(event);
        mCurrentEvents.remove(event);
        mEventInfoMap.remove(event);
      }
      mRedundantEvents.clear();
      final ProductDESProxyFactory factory = getFactory();
      final int numAutomata = mCurrentAutomata.size();
      final List<AutomatonProxy> originals =
        new ArrayList<AutomatonProxy>(numAutomata);
      final List<AutomatonProxy> results =
        new ArrayList<AutomatonProxy>(numAutomata);
      for (int i = 0; i < numAutomata; i++) {
        final AutomatonProxy aut = mCurrentAutomata.get(i);
        final Collection<EventProxy> events = aut.getEvents();
        boolean found = false;
        for (final EventProxy event : events) {
          if (redundant.contains(event)) {
            found = true;
            break;
          }
        }
        if (!found) {
          continue;
        }
        final int numEvents = events.size();
        final Collection<EventProxy> newEvents =
          new ArrayList<EventProxy>(numEvents - 1);
        for (final EventProxy event : events) {
          if (!redundant.contains(event)) {
            newEvents.add(event);
          }
        }
        final Collection<TransitionProxy> transitions = aut.getTransitions();
        final int numTrans = transitions.size();
        final Collection<TransitionProxy> newTransitions =
          new ArrayList<TransitionProxy>(numTrans);
        boolean dirty = false;
        for (final TransitionProxy trans : transitions) {
          final EventProxy event = trans.getEvent();
          if (!redundant.contains(event)) {
            newTransitions.add(trans);
          } else if (trans.getSource() != trans.getTarget()) {
            dirty = true;
          }
        }
        final String name = aut.getName();
        final ComponentKind kind = aut.getKind();
        final Collection<StateProxy> states = aut.getStates();
        final AutomatonProxy newAut = factory.createAutomatonProxy
          (name, kind, newEvents, states, newTransitions);
        originals.add(aut);
        results.add(newAut);
        mCurrentAutomata.set(i, newAut);
        for (final EventProxy event : newEvents) {
          final EventInfo info = mEventInfoMap.get(event);
          if (info != null) {
            info.replaceAutomaton(aut, newAut);
          }
        }
        if (dirty) {
          mDirtyAutomata.add(newAut);
        }
      }
      final AbstractionStep step = new EventRemovalStep(results, originals);
      mModifyingSteps.add(step);
      return true;
    }
  }

  private boolean findEventDisjointSubsystems()
    throws AnalysisException
  {
    if (mCurrentEvents.isEmpty()) {
      return false;
    }
    final Collection<AutomatonProxy> remainingAutomata =
      new THashSet<AutomatonProxy>(mCurrentAutomata);
    final List<EventProxy> remainingEvents =
      new LinkedList<EventProxy>(mCurrentEvents);
    Collections.sort(remainingEvents);
    final List<SubSystem> tasks = new LinkedList<SubSystem>();
    while (!remainingEvents.isEmpty()) {
      final int numAutomata = remainingAutomata.size();
      final int numEvents = remainingEvents.size();
      final Iterator<EventProxy> iter1 = remainingEvents.iterator();
      final EventProxy event1 = iter1.next();
      iter1.remove();
      final List<EventProxy> subSystemEvents =
        new ArrayList<EventProxy>(numEvents);
      subSystemEvents.add(event1);
      final EventInfo info1 = mEventInfoMap.get(event1);
      final Collection<AutomatonProxy> subSystemAutomata =
        info1.getAutomataSet();
      if (subSystemAutomata.size() == numAutomata) {
        subSystemEvents.addAll(remainingEvents);
        remainingEvents.clear();
      } else {
        boolean change;
        do {
          change = false;
          final Iterator<EventProxy> iter = remainingEvents.iterator();
          while (iter.hasNext()) {
            final EventProxy event = iter.next();
            final EventInfo info = mEventInfoMap.get(event);
            if (info.intersects(subSystemAutomata)) {
              if (info.addAutomataTo(subSystemAutomata)) {
                if (subSystemAutomata.size() == numAutomata) {
                  subSystemEvents.addAll(remainingEvents);
                  remainingEvents.clear();
                  change = false;
                  break;
                }
                change = true;
              }
              iter.remove();
              subSystemEvents.add(event);
            }
          }
        } while (change);
      }
      if (subSystemAutomata.size() < numAutomata) {
        remainingAutomata.removeAll(subSystemAutomata);
      } else if (tasks.isEmpty()) {
        return false;
      } else {
        remainingAutomata.clear();
      }
      final List<AutomatonProxy> subSystemAutomataList =
        new ArrayList<AutomatonProxy>(subSystemAutomata);
      Collections.sort(subSystemAutomataList);
      final SubSystem task =
        new SubSystem(subSystemEvents, subSystemAutomataList);
      tasks.add(task);
    }
    for (final AutomatonProxy aut : remainingAutomata) {
      final SubSystem task = new SubSystem(aut);
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
    return tasks.size() > 1;
  }

  private boolean popEventDisjointSubsystem()
  {
    if (mPostponedSubsystems.isEmpty()) {
      return false;
    } else {
      mProcessedAutomata.addAll(mCurrentAutomata);
      final SubSystem task = Collections.min(mPostponedSubsystems);
      mPostponedSubsystems.remove(task);
      loadSubSystem(task);
      return true;
    }
  }

  private void loadSubSystem(final SubSystem task)
  {
    mCurrentAutomata = task.getAutomata();
    mCurrentEvents = task.getEvents();
  }

  private void restoreAutomata()
  {
    mCurrentAutomata.addAll(mProcessedAutomata);
    for (final SubSystem task : mPostponedSubsystems) {
      final Collection<AutomatonProxy> automata = task.getAutomata();
      mCurrentAutomata.addAll(automata);
    }
  }

  private boolean isTriviallyNonblockingSubsystem()
    throws EventNotFoundException
  {
    final EventProxy marking = getUsedMarkingProposition();
    for (final AutomatonProxy aut : mCurrentAutomata) {
      final Collection<EventProxy> alphabet = aut.getEvents();
      if (alphabet.contains(marking)) {
        return false;
      }
    }
    return true;
  }

  private boolean runMonolithicConflictCheck()
    throws AnalysisException
  {
    final EventProxy marking = getUsedMarkingProposition();
    final int numEvents = mCurrentEvents.size() + 1;
    final List<EventProxy> events = new ArrayList<EventProxy>(numEvents);
    events.addAll(mCurrentEvents);
    events.add(marking);
    Collections.sort(events);
    final ProductDESProxy des = createDES(events, mCurrentAutomata);
    mCurrentMonolithicConflictChecker.setModel(des);
    removeEventsToAutomata(mCurrentAutomata);
    mCurrentMonolithicConflictChecker.run();
    final VerificationResult result =
      mCurrentMonolithicConflictChecker.getAnalysisResult();
    recordStatistics(result);
    return result.isSatisfied();
  }

  private ProductDESProxy createDES(final List<EventProxy> events,
                                    final List<AutomatonProxy> automata)
  {
    final ProductDESProxyFactory factory = getFactory();
    final String name = Candidate.getCompositionName(automata);
    return factory.createProductDESProxy(name, events, automata);
  }


  //#########################################################################
  //# Abstraction Steps
  private void simplify(final boolean eventsChanged)
    throws AnalysisException
  {
    final boolean change1 = simplifyDirtyAutomata();
    final boolean change2 = removeRedundantEvents();
    if (change1 || change2 || eventsChanged) {
      boolean change;
      do {
        change = simplifyDirtyAutomata() && removeRedundantEvents();
      } while (change);
      findEventDisjointSubsystems();
    }
  }

  private boolean simplifyDirtyAutomata()
    throws AnalysisException
  {
    boolean result = false;
    while (!mDirtyAutomata.isEmpty()) {
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
      result |= applyCandidate(candidate);
    }
    return result;
  }

  private boolean applyCandidate(final Candidate candidate)
    throws AnalysisException
  {
    final HidingStep syncStep = composeSynchronousProduct(candidate);
    final EventProxy tau;
    AutomatonProxy aut;
    if (syncStep == null) {
      aut = candidate.getAutomata().iterator().next();
      tau = null;
    } else {
      aut = syncStep.getResultAutomaton();
      tau = syncStep.getHiddenEvent();
    }
    recordStatistics(aut);
    final ObservationEquivalenceStep simpStep =
      mAbstractionRule.applyRule(aut, tau);
    if (syncStep != null || simpStep != null) {
      if (syncStep != null) {
        mModifyingSteps.add(syncStep);
      }
      if (simpStep != null) {
        final Collection<EventProxy> oldEvents = aut.getEvents();
        aut = simpStep.getResultAutomaton();
        final Collection<EventProxy> newEvents =
          new THashSet<EventProxy>(aut.getEvents());
        for (final EventProxy event : oldEvents) {
          if (event != tau && !newEvents.contains(event)) {
            mEventHasDisappeared = true;
            break;
          }
        }
        mModifyingSteps.add(simpStep);
      }
      updateEventsToAutomata(aut, candidate.getAutomata());
      return true;
    } else {
      return false;
    }
  }


  /**
   * Builds the synchronous product for a given candidate.
   */
  private HidingStep composeSynchronousProduct(final Candidate candidate)
    throws AnalysisException
  {
    if (candidate.getNumberOfAutomata() > 1) {
      return composeSeveralAutomata(candidate);
    } else {
      return composeOneAutomaton(candidate);
    }
  }

  private HidingStep composeOneAutomaton(final Candidate candidate)
    throws OverflowException
  {
    final List<AutomatonProxy> automata = candidate.getAutomata();
    assert automata.size() == 1 :
      "Candidate for hiding has more than one automaton!";
    final AutomatonProxy aut = automata.iterator().next();
    final ProductDESProxyFactory factory = getFactory();
    final EventProxy tau = createSilentEvent(candidate, factory);
    final EventEncoding eventEnc = new EventEncoding(aut, tau);
    final Collection<EventProxy> local = candidate.getLocalEvents();
    for (final EventProxy event : local) {
      eventEnc.addSilentEvent(event);
    }
    final StateEncoding stateEnc = new StateEncoding(aut);
    final ListBufferTransitionRelation rel = new ListBufferTransitionRelation
      (aut, eventEnc, stateEnc,
       ListBufferTransitionRelation.CONFIG_SUCCESSORS);
    final boolean change1 = rel.checkReachability();
    final boolean change2 = rel.removeTauSelfLoops();
    final boolean change3 = rel.removeProperSelfLoopEvents();
    final boolean change4 = rel.removeRedundantPropositions();
    final EventProxy trueTau = change2 ? null : tau;
    mEventHasDisappeared |= change3;
    if (change4) {
      final StateEncoding newStateEnc = new StateEncoding();
      final AutomatonProxy abstracted =
        rel.createAutomaton(factory, eventEnc, newStateEnc);
      final SynchronousProductStateMap stateMap =
        new OneAutomatonStateMap(aut, stateEnc, newStateEnc);
      return new HidingStep(abstracted, local, trueTau, stateMap);
    } else if (tau != null || change1 || change2 || change3) {
      final AutomatonProxy abstracted =
        rel.createAutomaton(factory, eventEnc, stateEnc);
      return new HidingStep(abstracted, aut, local, trueTau);
    } else {
      return null;
    }
  }

  private HidingStep composeSeveralAutomata(final Candidate candidate)
    throws AnalysisException
  {
    final ProductDESProxyFactory factory = getFactory();
    final ProductDESProxy des = candidate.createProductDESProxy(factory);
    mCurrentSynchronousProductBuilder.setModel(des);
    final Collection<EventProxy> events = des.getEvents();
    final Collection<EventProxy> local = candidate.getLocalEvents();
    final EventProxy tau = createSilentEvent(candidate, factory);
    int expectedNumberOfEvents = events.size() - local.size();
    if (tau != null) {
      mCurrentSynchronousProductBuilder.addMask(local, tau);
      expectedNumberOfEvents++;
    }
    try {
      mCurrentSynchronousProductBuilder.run();
      final AutomatonProxy sync =
        mCurrentSynchronousProductBuilder.getComputedAutomaton();
      mEventHasDisappeared |= sync.getEvents().size() < expectedNumberOfEvents;
      final SynchronousProductStateMap stateMap =
        mCurrentSynchronousProductBuilder.getStateMap();
      return new HidingStep(sync, local, tau, stateMap);
    } finally {
      mCurrentSynchronousProductBuilder.clearMask();
    }
  }

  /**
   * Creates a silent event for hiding using the given candidate.
   * @return A new event named according to the candidate's automata,
   *         or <CODE>null</CODE> if the candidate does not have any local
   *         events.
   */
  public EventProxy createSilentEvent(final Candidate candidate,
                                      final ProductDESProxyFactory factory)
  {
    final Collection<EventProxy> local = candidate.getLocalEvents();
    if (local.isEmpty()) {
      return null;
    } else {
      final List<AutomatonProxy> automata = candidate.getAutomata();
      String name = Candidate.getCompositionName("tau:", automata);
      int prefix = 0;
      while (!mUsedEventNames.add(name)) {
        prefix++;
        name = Candidate.getCompositionName("tau:" + prefix, automata);
      }
      return factory.createEventProxy(name, EventKind.UNCONTROLLABLE, false);
    }
  }


  //#########################################################################
  //# Trace Computation
  private ConflictTraceProxy expandTrace(final ConflictTraceProxy trace)
  {
    List<TraceStepProxy> traceSteps = getSaturatedTraceSteps(trace);
    final int size = mModifyingSteps.size();
    final ListIterator<AbstractionStep> iter =
      mModifyingSteps.listIterator(size);
    /*
    final Collection<AutomatonProxy> check =
      new THashSet<AutomatonProxy>(trace.getAutomata());
    */
    while (iter.hasPrevious()) {
      final AbstractionStep step = iter.previous();
      traceSteps = step.convertTraceSteps(traceSteps);
      /*
      check.removeAll(step.getResultAutomata());
      check.addAll(step.getOriginalAutomata());
      TraceChecker.checkCounterExample(traceSteps, check, true);
      */
    }
    final ProductDESProxyFactory factory = getFactory();
    final String tracename = getTraceName();
    final ProductDESProxy model = getModel();
    final Collection<AutomatonProxy> automata = model.getAutomata();
    return factory.createConflictTraceProxy(tracename,
                                            trace.getComment(),
                                            null,
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
    final int numAutomata = mCurrentAutomata.size();
    final List<TraceStepProxy> steps = trace.getTraceSteps();
    final int numSteps = steps.size();
    final List<TraceStepProxy> convertedSteps =
        new ArrayList<TraceStepProxy>(numSteps);
    final Iterator<TraceStepProxy> iter = steps.iterator();

    final TraceStepProxy firstStep = iter.next();
    final Map<AutomatonProxy,StateProxy> firstMap = firstStep.getStateMap();
    final Map<AutomatonProxy,StateProxy> convertedFirstMap =
      new HashMap<AutomatonProxy,StateProxy>(numAutomata);
    for (final AutomatonProxy aut : mCurrentAutomata) {
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
      for (final AutomatonProxy aut : mCurrentAutomata) {
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
  //# Statistics
  private void recordStatistics(final AutomatonProxy aut)
  {
    final int numStates = aut.getStates().size();
    final int numTrans = aut.getTransitions().size();
    recordStatistics(numStates, numTrans);
  }

  private void recordStatistics(final VerificationResult result)
  {
    final double numStates = result.getTotalNumberOfStates();
    final double numTrans = result.getTotalNumberOfTransitions();
    recordStatistics(numStates, numTrans);
  }

  private void recordStatistics(final double numStates, final double numTrans)
  {
    final VerificationResult result = getAnalysisResult();
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
  }


  //#########################################################################
  //# Inner Enumeration Method
  /**
   * The configuration setting to determine the abstraction method applied
   * to intermediate automata during compositional nonblocking verification.
   */
  public enum Method
  {
    /**
     * Automata are minimised according to <I>observation equivalence</I>.
     */
    OEQ,
    /**
     * Automata are minimised according using <I>observer projection</I>.
     * The present implementation determines a coarsest causal reporter
     * map satisfying the observer property. Nondeterminism in the projected
     * automata is not resolved, nondeterministic abstraction are used instead.
     */
    OP,
    /**
     * Automata are minimised according using an <I>observer projection</I>
     * obtained by the OP-search algorithm presented in the paper by
     * P. Pena, J.E.R. Cury, R. Malik, and S. Lafortune in WODES 2010.
     */
    OPSEARCH,
    /**
     * Automata are minimised according to <I>weak observation equivalence</I>.
     * Initial states and markings are not saturated, silent transitions
     * are retained instead in a bid to reduce the overall number of
     * transitions.
     */
    WOEQ
  }


  //#########################################################################
  //# Inner Class SubSystem
  /**
   * A collection of automata and associated events.
   * This class is used to store subsystems to be checked later.
   * Essentially it holds the contents of a {@link ProductDESProxy},
   * but in a more lightweight form.
   */
  private static class SubSystem
    implements Comparable<SubSystem>
  {

    //#######################################################################
    //# Constructors
    private SubSystem(final AutomatonProxy aut)
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
    }

    private SubSystem(final Collection<EventProxy> events,
                      final List<AutomatonProxy> automata)
    {
      mEvents = events;
      mAutomata = automata;
    }

    //#######################################################################
    //# Interface java.util.Comparable<SubSystem>
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
    private Collection<EventProxy> getEvents()
    {
      return mEvents;
    }

    private List<AutomatonProxy> getAutomata()
    {
      return mAutomata;
    }

    //#######################################################################
    //# Data Members
    private final Collection<EventProxy> mEvents;
    private final List<AutomatonProxy> mAutomata;

  }


  //#########################################################################
  //# Inner Class EventInfo
  /**
   * A record to store information about the automata an event occurs in.
   * The event information record basically consists of the set of automata
   * it occurs in, plus information in which automata the event only appears
   * as selfloops.
   */
  private static class EventInfo
  {

    //#######################################################################
    //# Constructor
    private EventInfo()
    {
      mAutomataMap = new TObjectByteHashMap<AutomatonProxy>();
      mNumNonSelfloopAutomata = 0;
      mIsBlocked = false;
    }

    //#######################################################################
    //# Simple Access
    private boolean addAutomataTo(final Collection<AutomatonProxy> target)
    {
      final TObjectByteIterator<AutomatonProxy> iter = mAutomataMap.iterator();
      boolean added = false;
      while (iter.hasNext()) {
        iter.advance();
        final AutomatonProxy aut = iter.key();
        added |= target.add(aut);
      }
      return added;
    }

    private void addAutomaton(final AutomatonProxy aut, final byte status)
    {
      final byte present = mAutomataMap.get(aut);
      if (present != status) {
        mAutomataMap.put(aut, status);
        if (present == NOT_ONLY_SELFLOOP) {
          mNumNonSelfloopAutomata--;
        }
        if (status == NOT_ONLY_SELFLOOP) {
          mNumNonSelfloopAutomata++;
        }
        mIsBlocked |= status == BLOCKED;
      }
    }

    private boolean containedIn(final Collection<AutomatonProxy> automata)
    {
      final TObjectByteIterator<AutomatonProxy> iter = mAutomataMap.iterator();
      while (iter.hasNext()) {
        iter.advance();
        final AutomatonProxy aut = iter.key();
        if (!automata.contains(aut)) {
          return false;
        }
      }
      return true;
    }

    private List<AutomatonProxy> getAutomataList()
    {
      final int size = mAutomataMap.size();
      final AutomatonProxy[] automata = new AutomatonProxy[size];
      mAutomataMap.keys(automata);
      return Arrays.asList(automata);
    }

    private Set<AutomatonProxy> getAutomataSet()
    {
      final int size = mAutomataMap.size();
      final Set<AutomatonProxy> automata = new THashSet<AutomatonProxy>(size);
      final TObjectByteIterator<AutomatonProxy> iter = mAutomataMap.iterator();
      while (iter.hasNext()) {
        iter.advance();
        final AutomatonProxy aut = iter.key();
        automata.add(aut);
      }
      return automata;
    }

    private int getNumberOfAutomata()
    {
      return mAutomataMap.size();
    }

    private boolean intersects(final Collection<AutomatonProxy> automata)
    {
      for (final AutomatonProxy aut : automata) {
        if (mAutomataMap.containsKey(aut)) {
          return true;
        }
      }
      return false;
    }

    private boolean isEmpty()
    {
      return mAutomataMap.isEmpty();
    }

    private boolean isRemovable()
    {
      return mIsBlocked || mNumNonSelfloopAutomata == 0;
    }

    private void removeAutomata(final Collection<AutomatonProxy> victims)
    {
      for (final AutomatonProxy aut : victims) {
        final byte code = mAutomataMap.remove(aut);
        if (code == NOT_ONLY_SELFLOOP) {
          mNumNonSelfloopAutomata--;
        }
      }
    }

    private boolean replaceAutomaton(final AutomatonProxy oldAut,
                                     final AutomatonProxy newAut)
    {
      final byte code = mAutomataMap.remove(oldAut);
      if (code == UNKNOWN_SELFLOOP) {
        // not found in map ...
        return false;
      } else {
        mAutomataMap.put(newAut, code);
        return true;
      }
    }

    //#######################################################################
    //# Data Members
    private final TObjectByteHashMap<AutomatonProxy> mAutomataMap;
    private int mNumNonSelfloopAutomata;
    private boolean mIsBlocked;

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
      for (final EventInfo info : mEventInfoMap.values()) {
        assert info.getNumberOfAutomata() > 0;
        if (info.getNumberOfAutomata() > 1) {
          final List<AutomatonProxy> list = info.getAutomataList();
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
  //# Inner Class AbstractionRule
  private abstract class AbstractionRule
  {

    //#######################################################################
    //# Rule Application
    abstract ObservationEquivalenceStep applyRule(final AutomatonProxy aut,
                                                  final EventProxy tau)
    throws AnalysisException;

    //#######################################################################
    //# Auxiliary Methods
    List<int[]> applySimplifier
      (final TransitionRelationSimplifier simplifier,
       final ListBufferTransitionRelation rel)
    throws AnalysisException
    {
      simplifier.run();
      if (simplifier.applyResultPartition()) {
        rel.removeTauSelfLoops();
        rel.removeProperSelfLoopEvents();
        rel.removeRedundantPropositions();
        return simplifier.getResultPartition();
      } else {
        return null;
      }
    }

    List<int[]> combinePartitions(final List<int[]> first,
                                  final List<int[]> second)
    {
      final int secondSize = second.size();
      final List<int[]> result = new ArrayList<int[]>(secondSize);
      final TIntArrayList current = new TIntArrayList();
      for (final int[] clazz2 : second) {
        for (final int state2 : clazz2) {
          final int[] clazz1 = first.get(state2);
          for (final int state1 : clazz1) {
            current.add(state1);
          }
        }
        result.add(current.toNativeArray());
        current.clear();
      }
      return result;
    }

  }


  //#########################################################################
  //# Inner Class ObservationEquivalenceAbstractionRule
  private class ObservationEquivalenceAbstractionRule
    extends AbstractionRule
  {

    //#######################################################################
    //# Constructor
    private ObservationEquivalenceAbstractionRule
      (final ObservationEquivalenceTRSimplifier.Equivalence eq)
    {
      mEquivalence = eq;
    }

    //#######################################################################
    //# Rule Application
    ObservationEquivalenceStep applyRule
      (final AutomatonProxy aut,
       final EventProxy tau)
    throws AnalysisException
    {
      final EventEncoding eventEnc =
        new EventEncoding(aut, tau, mPropositions,
                          EventEncoding.FILTER_PROPOSITIONS);
      final StateEncoding inputStateEnc = new StateEncoding(aut);
      final ListBufferTransitionRelation rel = new ListBufferTransitionRelation
        (aut, eventEnc, inputStateEnc,
         ListBufferTransitionRelation.CONFIG_PREDECESSORS);
      final TransitionRelationSimplifier loopRemover =
        new TauLoopRemovalTRSimplifier(rel);
      final List<int[]> loopPartition = applySimplifier(loopRemover, rel);
      final ObservationEquivalenceTRSimplifier bisimulator =
        new ObservationEquivalenceTRSimplifier(rel);
      bisimulator.setEquivalence(mEquivalence);
      bisimulator.setTransitionRemovalMode
        (ObservationEquivalenceTRSimplifier.TransitionRemoval.ALL);
      bisimulator.setMarkingMode
        (ObservationEquivalenceTRSimplifier.MarkingMode.SATURATE);
      final List<int[]> bisimPartition = applySimplifier(bisimulator, rel);
      if (loopPartition != null || bisimPartition != null) {
        final ProductDESProxyFactory factory = getFactory();
        final StateEncoding outputStateEnc = new StateEncoding();
        final AutomatonProxy convertedAut =
          rel.createAutomaton(factory, eventEnc, outputStateEnc);
        final List<int[]> partition;
        if (loopPartition == null) {
          partition = bisimPartition;
        } else if (bisimPartition == null) {
          partition = loopPartition;
        } else {
          partition = combinePartitions(loopPartition, bisimPartition);
        }
        return new ObservationEquivalenceStep(convertedAut, aut, tau,
                                              inputStateEnc, partition,
                                              outputStateEnc);
      } else {
        return null;
      }
    }

    //#######################################################################
    //# Data Members
    private final ObservationEquivalenceTRSimplifier.Equivalence mEquivalence;
  }


  //#########################################################################
  //# Inner Class ObserverProjectionAbstractionRule
  private class ObserverProjectionAbstractionRule
    extends AbstractionRule
  {

    //#######################################################################
    //# Rule Application
    ObservationEquivalenceStep applyRule(final AutomatonProxy aut,
                                         final EventProxy tau)
    throws AnalysisException
    {
      final ProductDESProxyFactory factory = getFactory();
      final String name = "vtau:" + aut.getName();
      final EventProxy vtau =
        factory.createEventProxy(name, EventKind.UNCONTROLLABLE);
      final EventEncoding eventEnc =
        new EventEncoding(aut, tau, mPropositions,
                          EventEncoding.FILTER_PROPOSITIONS);
      eventEnc.addEvent(vtau, false);
      final StateEncoding inputStateEnc = new StateEncoding(aut);
      final ListBufferTransitionRelation rel = new ListBufferTransitionRelation
        (aut, eventEnc, inputStateEnc,
         ListBufferTransitionRelation.CONFIG_PREDECESSORS);
      final int codeOfVTau = eventEnc.getEventCode(vtau);
      final ObservationEquivalenceTRSimplifier bisimulator =
        new ObservationEquivalenceTRSimplifier(rel);
      final List<int[]> partition =
        applySimplifier(bisimulator, rel, codeOfVTau);
      if (partition != null) {
        final StateEncoding outputStateEnc = new StateEncoding();
        final AutomatonProxy convertedAut =
          rel.createAutomaton(factory, eventEnc, outputStateEnc);
        /*
        final IsomorphismChecker checker =
          new IsomorphismChecker(factory, false);
        checker.checkObservationEquivalence(aut, convertedAut, tau);
        */
        return new ObservationEquivalenceStep(convertedAut, aut, tau,
                                              inputStateEnc, partition,
                                              outputStateEnc);
      } else {
        return null;
      }
    }

    //#######################################################################
    //# Auxiliary Methods
    private List<int[]> applySimplifier
      (final ObservationEquivalenceTRSimplifier simplifier,
       final ListBufferTransitionRelation rel,
       final int vtau)
      throws AnalysisException
    {
      final int tau = EventEncoding.TAU;
      final int numTransBefore = rel.getNumberOfTransitions();
      List<int[]> partition;
      while (true) {
        final boolean modified = simplifier.run();
        if (!modified && rel.getNumberOfTransitions() == numTransBefore) {
          return null;
        }
        partition = simplifier.getResultPartition();
        if (partition == null) {
          break;
        } else if (!makeEventsVisible(rel, partition, vtau)) {
          break;
        }
        simplifier.setInitialPartition(partition);
      }
      simplifier.applyResultPartition();
      rel.replaceEvent(vtau, tau);
      rel.removeEvent(vtau);
      rel.removeTauSelfLoops();
      rel.removeProperSelfLoopEvents();
      rel.removeRedundantPropositions();
      return partition;
    }

    private boolean makeEventsVisible
      (final ListBufferTransitionRelation rel,
       final List<int[]> partition,
       final int vtau)
    {
      final int numStates = rel.getNumberOfStates();
      final TIntIntHashMap pmap = new TIntIntHashMap(numStates);
      int code = 0;
      for (final int[] array : partition) {
        for (final int state : array) {
          pmap.put(state, code);
        }
        code++;
      }
      final TransitionIterator iter =
        rel.createPredecessorsModifyingIterator();
      final TIntArrayList victims = new TIntArrayList();
      final int tau = EventEncoding.TAU;
      boolean modified = false;
      for (int target= 0; target < numStates; target++) {
        if (rel.isReachable(target)) {
          final int targetClass = pmap.get(target);
          iter.reset(target, tau);
          while (iter.advance()) {
            final int source = iter.getCurrentSourceState();
            final int sourceClass = pmap.get(source);
            if (sourceClass != targetClass) {
              iter.remove();
              victims.add(source);
            }
          }
          if (!victims.isEmpty()) {
            modified = true;
            rel.addTransitions(victims, vtau, target);
            victims.clear();
          }
        }
      }
      return modified;
    }
  }


  //#########################################################################
  //# Inner Class OPSearchAbstractionRule
  private class OPSearchAbstractionRule
    extends AbstractionRule
  {

    //#######################################################################
    //# Rule Application
    ObservationEquivalenceStep applyRule(final AutomatonProxy aut,
                                         final EventProxy tau)
    throws AnalysisException
    {
      if (tau == null) {
        return null;
      }
      final ProductDESProxyFactory factory = getFactory();
      final Collection<EventProxy> hidden = Collections.singletonList(tau);
      final OPSearchAutomatonSimplifier simplifier =
        new OPSearchAutomatonSimplifier(aut, hidden, factory);
      simplifier.setPropositions(mPropositions);
      simplifier.run();
      final PartitionedAutomatonResult result = simplifier.getAnalysisResult();
      final AutomatonProxy convertedAut = result.getAutomaton();
      if (aut == convertedAut) {
        return null;
      }
      final StateEncoding inputEnc = result.getInputEncoding();
      final StateEncoding outputEnc = result.getOutputEncoding();
      final List<int[]> partition = result.getPartition();
      return new ObservationEquivalenceStep(convertedAut, aut, tau,
                                            inputEnc, partition, outputEnc);
    }

  }


  //#########################################################################
  //# Inner Class AbstractionStep
  private abstract class AbstractionStep
  {

    //#######################################################################
    //# Constructors
    AbstractionStep(final List<AutomatonProxy> results,
                    final List<AutomatonProxy> originals)
    {
      mResultAutomata = results;
      mOriginalAutomata = originals;
    }

    AbstractionStep(final AutomatonProxy result,
                    final Collection<AutomatonProxy> originals)
    {
      this(Collections.singletonList(result),
           new ArrayList<AutomatonProxy>(originals));
    }

    AbstractionStep(final AutomatonProxy result,
                    final AutomatonProxy original)
    {
      this(Collections.singletonList(result),
           Collections.singletonList(original));
    }

    //#######################################################################
    //# Simple Access
    List<AutomatonProxy> getResultAutomata()
    {
      return mResultAutomata;
    }

    AutomatonProxy getResultAutomaton()
    {
      if (mResultAutomata.size() == 1) {
        return mResultAutomata.iterator().next();
      } else {
        throw new IllegalStateException
          ("Attempting to get a single result automaton from " +
           ProxyTools.getShortClassName(this) + " with " +
           mResultAutomata.size() + " result automata!");
      }
    }

    List<AutomatonProxy> getOriginalAutomata()
    {
      return mOriginalAutomata;
    }

    AutomatonProxy getOriginalAutomaton()
    {
      if (mOriginalAutomata.size() == 1) {
        return mOriginalAutomata.iterator().next();
      } else {
        throw new IllegalStateException
          ("Attempting to get a single input automaton from " +
           ProxyTools.getShortClassName(this) + " with " +
           mOriginalAutomata.size() + " input automata!");
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
    private final List<AutomatonProxy> mResultAutomata;
    private final List<AutomatonProxy> mOriginalAutomata;

  }


  //#########################################################################
  //# Inner Class EventRemovalStep
  private class EventRemovalStep extends AbstractionStep
  {

    //#######################################################################
    //# Constructor
    private EventRemovalStep(final List<AutomatonProxy> results,
                             final List<AutomatonProxy> originals)
    {
      super(results, originals);
    }

    //#######################################################################
    //# Trace Computation
    List<TraceStepProxy> convertTraceSteps(final List<TraceStepProxy> steps)
    {
      final List<AutomatonProxy> results = getResultAutomata();
      final List<AutomatonProxy> originals = getOriginalAutomata();
      final int numAutomata = results.size();
      final Map<AutomatonProxy,AutomatonProxy> autMap =
        new HashMap<AutomatonProxy,AutomatonProxy>(numAutomata);
      final Iterator<AutomatonProxy> resultIter = results.iterator();
      final Iterator<AutomatonProxy> originalIter = originals.iterator();
      while (resultIter.hasNext()) {
        final AutomatonProxy result = resultIter.next();
        final AutomatonProxy original = originalIter.next();
        autMap.put(result, original);
      }
      final ProductDESProxyFactory factory = getFactory();
      final int numSteps = steps.size();
      final List<TraceStepProxy> newSteps =
        new ArrayList<TraceStepProxy>(numSteps);
      for (final TraceStepProxy step : steps) {
        final Map<AutomatonProxy,StateProxy> stepMap = step.getStateMap();
        final int size = stepMap.size();
        final Map<AutomatonProxy,StateProxy> newStepMap =
          new HashMap<AutomatonProxy,StateProxy>(size);
        for (final Map.Entry<AutomatonProxy,StateProxy> entry :
             stepMap.entrySet()) {
          final AutomatonProxy aut = entry.getKey();
          AutomatonProxy newAut = autMap.get(aut);
          if (newAut == null) {
            newAut = aut;
          }
          final StateProxy state = entry.getValue();
          newStepMap.put(newAut, state);
        }
        final EventProxy event = step.getEvent();
        final TraceStepProxy newStep =
          factory.createTraceStepProxy(event, newStepMap);
        newSteps.add(newStep);
      }
      return newSteps;
    }

  }


  //#########################################################################
  //# Inner Class CompositionStep
  private class HidingStep extends AbstractionStep
  {

    //#######################################################################
    //# Constructor
    private HidingStep(final AutomatonProxy composedAut,
                       final AutomatonProxy originalAut,
                       final Collection<EventProxy> localEvents,
                       final EventProxy tau)
    {
      super(composedAut, originalAut);
      mLocalEvents = localEvents;
      mHiddenEvent = tau;
      mStateMap = null;
    }

    private HidingStep(final AutomatonProxy composedAut,
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
        final StateProxy convertedState =
          convertedStepMap.remove(resultAutomaton);
        for (final AutomatonProxy aut : originalAutomata) {
          final StateProxy originalState =
            getOriginalState(convertedState, aut);
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

    final StateProxy getOriginalState(final StateProxy convertedState,
                                      final AutomatonProxy aut)
    {
      if (mStateMap == null) {
        return convertedState;
      } else {
        return mStateMap.getOriginalState(convertedState, aut);
      }
    }

    //#######################################################################
    //# Data Members
    private final Collection<EventProxy> mLocalEvents;
    private final EventProxy mHiddenEvent;
    private final SynchronousProductStateMap mStateMap;
  }


  //#########################################################################
  //# Inner Class ObservationEquivalenceStep
  private class ObservationEquivalenceStep extends AbstractionStep
  {

    //#######################################################################
    //# Constructor
    private ObservationEquivalenceStep
      (final AutomatonProxy resultAut,
       final AutomatonProxy originalAut,
       final EventProxy tau,
       final StateEncoding originalStateEnc,
       final List<int[]> partition,
       final StateEncoding resultStateEnc)
    {
      super(resultAut, originalAut);
      mTau = tau;
      mOriginalStateEncoding = originalStateEnc;
      mPartition = partition;
      mReverseOutputStateMap = resultStateEnc.getStateCodeMap();
    }

    //#######################################################################
    //# Trace Computation
    List<TraceStepProxy> convertTraceSteps
      (final List<TraceStepProxy> traceSteps)
    {
      final ProductDESProxyFactory factory = getFactory();
      final AutomatonProxy originalAutomaton = getOriginalAutomaton();
      final AutomatonProxy resultAutomaton = getResultAutomaton();
      final EventEncoding eventEnc =
        new EventEncoding(originalAutomaton, mTau, mPropositions,
                          EventEncoding.FILTER_PROPOSITIONS);
      try {
        mTransitionRelation = new ListBufferTransitionRelation
          (originalAutomaton, eventEnc, mOriginalStateEncoding,
           ListBufferTransitionRelation.CONFIG_SUCCESSORS);
      } catch (final OverflowException exception) {
        throw new WatersRuntimeException(exception);
      }

      final List<TraceStepProxy> convertedSteps =
        new LinkedList<TraceStepProxy>();

      // Make the trace begin in the correct initial state ...
      final Iterator<TraceStepProxy> iter = traceSteps.iterator();
      final TraceStepProxy initialStep = iter.next();
      Map<AutomatonProxy,StateProxy> stepsNewStateMap =
        new HashMap<AutomatonProxy,StateProxy>(initialStep.getStateMap());
      final StateProxy tracesInitialState =
        stepsNewStateMap.remove(resultAutomaton);
      final int tracesInitialStateID =
        mReverseOutputStateMap.get(tracesInitialState);
      final List<SearchRecord> initialRecords =
        beginTrace(tracesInitialStateID);
      assert initialRecords.size() > 0;
      appendTraceSteps(initialRecords, eventEnc,
                       stepsNewStateMap, convertedSteps);

      // Append internal steps, with intermittent tau as needed ...
      final int tau = EventEncoding.TAU;
      int originalSourceID =
        initialRecords.get(initialRecords.size() - 1).getState();
      while (iter.hasNext()) {
        final TraceStepProxy step = iter.next();
        final EventProxy stepEvent = step.getEvent();
        final int eventID = eventEnc.getEventCode(stepEvent);
        final Map<AutomatonProxy,StateProxy> stepsStateMap =
          step.getStateMap();
        if (eventID < 0) {
          // The event is not in the automaton being simplified:
          // add an idle step, using the new states of the other automata.
          stepsNewStateMap =
            new HashMap<AutomatonProxy,StateProxy>(stepsStateMap);
          stepsNewStateMap.remove(resultAutomaton);
          final StateProxy state =
            mOriginalStateEncoding.getState(originalSourceID);
          stepsNewStateMap.put(originalAutomaton, state);
          final TraceStepProxy convertedStep =
            factory.createTraceStepProxy(stepEvent, stepsNewStateMap);
          convertedSteps.add(convertedStep);
        } else if (eventID == tau) {
          // The event is tau: find and append a tau*-only trace
          final Map<AutomatonProxy,StateProxy> stepsAfterStateMap =
            stepsStateMap;
          final StateProxy resultTargetState =
            stepsAfterStateMap.get(resultAutomaton);
          final List<SearchRecord> subtrace =
            findSubTrace(originalSourceID, eventID,
                         mReverseOutputStateMap.get(resultTargetState));
          appendTraceSteps(subtrace, eventEnc,
                           stepsNewStateMap, convertedSteps);
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
            stepsAfterStateMap.remove(resultAutomaton);
          assert resultTargetState != null;
          final List<SearchRecord> subtrace =
            findSubTrace(originalSourceID, eventID,
                         mReverseOutputStateMap.get(resultTargetState));
          appendTraceSteps(subtrace, stepEvent, eventEnc, stepsNewStateMap,
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
    private List<SearchRecord> beginTrace(final int resultAutInitialStateClass)
    {
      final int[] targetArray = mPartition.get(resultAutInitialStateClass);
      final TIntHashSet targetSet = new TIntHashSet(targetArray);
      final Queue<SearchRecord> open = new ArrayDeque<SearchRecord>();
      final TIntHashSet visited = new TIntHashSet();
      // The dummy record ensures that the first real search record will
      // later be included in the trace.
      final SearchRecord dummy = new SearchRecord(-1);
      final int numStates = mTransitionRelation.getNumberOfStates();
      for (int state = 0; state < numStates; state++) {
        if (mTransitionRelation.isInitial(state)) {
          final SearchRecord record =
            new SearchRecord(state, false, -1, dummy);
          if (targetSet.contains(state)) {
            return Collections.singletonList(record);
          }
          open.add(record);
          visited.add(state);
        }
      }
      final int tau = EventEncoding.TAU;
      final TransitionIterator iter =
        mTransitionRelation.createSuccessorsReadOnlyIterator();
      while (true) {
        final SearchRecord current = open.remove();
        final int source = current.getState();
        iter.reset(source, tau);
        while (iter.advance()) {
          final int target = iter.getCurrentTargetState();
          if (!visited.contains(target)) {
            final SearchRecord record =
              new SearchRecord(target, false, tau, current);
            if (targetSet.contains(target)) {
              return buildSearchRecordTrace(record);
            }
            open.add(record);
            visited.add(target);
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
      final int[] targetArray = mPartition.get(targetClass);
      final TIntHashSet targetSet = new TIntHashSet(targetArray);
      final Queue<SearchRecord> open = new ArrayDeque<SearchRecord>();
      final TIntHashSet visited0 = new TIntHashSet(); // event not in trace
      final TIntHashSet visited1 = new TIntHashSet(); // event in trace
      // The given event may be tau. In this case, we must search for a
      // (possibly empty) string of tau events. This is achieved here by
      // by creating a first search record with the 'hasevent' property,
      // i.e., pretending the trace already has an event.
      final int tau = EventEncoding.TAU;
      SearchRecord record;
      if (event != tau) {
        record = new SearchRecord(originalSource);
        visited0.add(originalSource);
      } else if (!targetSet.contains(originalSource)) {
        record = new SearchRecord(originalSource, true, -1, null);
        visited1.add(originalSource);
      } else {
        return Collections.emptyList();
      }
      final TransitionIterator iter =
        mTransitionRelation.createSuccessorsReadOnlyIterator();
      open.add(record);
      while (true) {
        final SearchRecord current = open.remove();
        final int source = current.getState();
        final boolean hasEvent = current.hasProperEvent();
        final TIntHashSet visited = hasEvent ? visited1 : visited0;
        iter.reset(source, tau);
        while (iter.advance()) {
          final int target = iter.getCurrentTargetState();
          if (!visited.contains(target)) {
            record = new SearchRecord(target, hasEvent, tau, current);
            if (hasEvent && targetSet.contains(target)) {
              return buildSearchRecordTrace(record);
            }
            open.add(record);
            visited.add(target);
          }
        }
        if (!hasEvent) {
          iter.reset(source, event);
          while (iter.advance()) {
            final int target = iter.getCurrentTargetState();
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
     * @param eventEnc
     *          Event encoding to recognise silent events.
     * @param stepsStateMap
     *          The state map for the step before adding the new information.
     * @param outputTrace
     *          Trace steps created are appended to this list.
     */
    private void appendTraceSteps
      (final List<SearchRecord> recordTrace,
       final EventEncoding eventEnc,
       final Map<AutomatonProxy,StateProxy> stepsStateMap,
       final List<TraceStepProxy> outputTrace)
    {
      appendTraceSteps(recordTrace, null, eventEnc,
                       stepsStateMap, stepsStateMap, outputTrace);
    }

    private void appendTraceSteps
      (final List<SearchRecord> recordTrace,
       final EventProxy event,
       final EventEncoding eventEnc,
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
          stepEvent = eventEnc.getProperEvent(subStepEventID);
          if (stepEvent == event) {
            currentStateMap = afterEventStateMap;
          }
        } else {
          stepEvent = null;
        }
        final int subStepTargetStateID = record.getState();
        final StateProxy subStepTargetState =
          mOriginalStateEncoding.getState(subStepTargetStateID);
        currentStateMap.put(originalAutomaton, subStepTargetState);
        final TraceStepProxy convertedStep =
          factory.createTraceStepProxy(stepEvent, currentStateMap);
        outputTrace.add(convertedStep);
      }
    }


    //#######################################################################
    //# Data Members
    /**
     * The event that was hidden from the original automaton,
     * or <CODE>null</CODE>.
     */
    private final EventProxy mTau;
    /**
     * State encoding of original automaton. Maps state codes in the input
     * transition relation to state objects in the input automaton.
     */
    private final StateEncoding mOriginalStateEncoding;
    /**
     * Partition applied to original automaton.
     * Each entry lists states of the input encoding that have been merged.
     */
    private final List<int[]> mPartition;
    /**
     * Reverse encoding of output states. Maps states in output automaton
     * (simplified automaton) to state code in output transition relation.
     */
    private final TObjectIntHashMap<StateProxy> mReverseOutputStateMap;

    /**
     * Transition relation that was simplified.
     * Only constructed when expanding trace.
     */
    private ListBufferTransitionRelation mTransitionRelation;
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
    public Collection<AutomatonProxy> getInputAutomata()
    {
      return Collections.singletonList(mOriginalAutomaton);
    }

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
  //# Inner Class SearchRecord
  /**
   * A record to store information about a visited state while searching
   * to expand counterexamples.
   */
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
  private Collection<EventProxy> mPropositions;
  private int mInternalStepNodeLimit;
  private int mInternalStepTransitionLimit;

  private Method mMethod;
  private PreselectingHeuristic mPreselectingHeuristic;
  private Comparator<Candidate> mSelectingHeuristics;
  private SynchronousProductBuilder mSynchronousProductBuilder;
  private ConflictChecker mMonolithicConflictChecker;

  private List<AutomatonProxy> mCurrentAutomata;
  private Collection<EventProxy> mCurrentEvents;
  private Map<EventProxy,EventInfo> mEventInfoMap =
      new HashMap<EventProxy,EventInfo>();
  private Queue<AutomatonProxy> mDirtyAutomata;
  private Collection<EventProxy> mRedundantEvents;
  /**
   * A flag indicating that an event has disappeared unexpectedly.
   * This flag is set when a proper event has been found to be only selflooped
   * in an automaton after abstraction, and therefore has been removed from
   * the automaton alphabet.
   */
  private boolean mEventHasDisappeared;
  private Collection<SubSystem> mPostponedSubsystems;
  private Collection<AutomatonProxy> mProcessedAutomata;
  private List<AbstractionStep> mModifyingSteps;
  private Set<String> mUsedEventNames;
  private Set<List<AutomatonProxy>> mOverflowCandidates;
  private int mNumOverflows;
  private AbstractionRule mAbstractionRule;
  private SynchronousProductBuilder mCurrentSynchronousProductBuilder;
  private ConflictChecker mCurrentMonolithicConflictChecker;


  //#########################################################################
  //# Class Constants
  private static final int MAX_OVERFLOWS = 50;

  private static final byte UNKNOWN_SELFLOOP = 0;
  private static final byte ONLY_SELFLOOP = 1;
  private static final byte NOT_ONLY_SELFLOOP = 2;
  private static final byte BLOCKED = 3;

}
