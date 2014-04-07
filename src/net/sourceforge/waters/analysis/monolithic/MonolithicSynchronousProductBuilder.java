//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters Analysis
//# PACKAGE: net.sourceforge.waters.analysis.monolithic
//# CLASS:   MonolithicSynchronousProductBuilder
//###########################################################################
//# $Id$
//###########################################################################


package net.sourceforge.waters.analysis.monolithic;

import gnu.trove.iterator.TObjectIntIterator;
import gnu.trove.list.array.TIntArrayList;
import gnu.trove.map.custom_hash.TObjectIntCustomHashMap;
import gnu.trove.map.hash.TObjectIntHashMap;
import gnu.trove.set.hash.THashSet;
import gnu.trove.set.hash.TIntHashSet;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;

import net.sourceforge.waters.analysis.tr.IntArrayHashingStrategy;
import net.sourceforge.waters.model.analysis.AnalysisException;
import net.sourceforge.waters.model.analysis.KindTranslator;
import net.sourceforge.waters.model.analysis.OverflowException;
import net.sourceforge.waters.model.analysis.OverflowKind;
import net.sourceforge.waters.model.analysis.des.AbstractAutomatonBuilder;
import net.sourceforge.waters.model.analysis.des.AutomatonResult;
import net.sourceforge.waters.model.analysis.des.SynchronousProductBuilder;
import net.sourceforge.waters.model.analysis.des.SynchronousProductStateMap;
import net.sourceforge.waters.model.base.NamedProxy;
import net.sourceforge.waters.model.base.ProxyVisitor;
import net.sourceforge.waters.model.base.VisitorException;
import net.sourceforge.waters.model.des.AutomatonProxy;
import net.sourceforge.waters.model.des.EventProxy;
import net.sourceforge.waters.model.des.ProductDESProxy;
import net.sourceforge.waters.model.des.ProductDESProxyFactory;
import net.sourceforge.waters.model.des.ProductDESProxyVisitor;
import net.sourceforge.waters.model.des.StateProxy;
import net.sourceforge.waters.model.des.TransitionProxy;
import net.sourceforge.waters.model.printer.ProxyPrinter;
import net.sourceforge.waters.xsd.base.ComponentKind;
import net.sourceforge.waters.xsd.base.EventKind;

import org.apache.log4j.Logger;


/**
 * A Java implementation of the monolithic synchronous product algorithm.
 * This implementation supports nondeterministic automata and hiding.
 * States are stored in integer arrays without compression, so it is not
 * recommended to use this implementation to compose a large number of
 * automata.
 *
 * @author Simon Ware, Rachel Francis, Robi Malik
 */

public class MonolithicSynchronousProductBuilder
  extends AbstractAutomatonBuilder
  implements SynchronousProductBuilder
{

  //#########################################################################
  //# Constructors
  public MonolithicSynchronousProductBuilder
    (final ProductDESProxyFactory factory)
  {
    super(factory);
  }

  public MonolithicSynchronousProductBuilder
    (final ProductDESProxy model,
     final ProductDESProxyFactory factory)
  {
    super(model, factory);
  }

  public MonolithicSynchronousProductBuilder
    (final ProductDESProxy model,
     final ProductDESProxyFactory factory,
     final KindTranslator translator)
  {
    super(model, factory, translator);
  }


  //#########################################################################
  //# Configuration
  /**
   * Sets a callback that specifies user-defined actions to be performed
   * before adding a new state.
   * @param  callback  The callback to be invoked when adding states,
   *                   or <CODE>null</CODE> to disable this feature.
   */
  public void setStateCallback(final StateCallback callback)
  {
    mStateCallback = callback;
  }

  /**
   * Gets the callback interface executed when adding a new state.
   * @see #setStateCallback(StateCallback) setStateCallback()
   */
  public StateCallback getStateCallback()
  {
    return mStateCallback;
  }

  /**
   * Sets the given event to be considered as forbidden.
   * Forbidden events are typically selfloop-only events with the property
   * that state exploration ends as soon as a state with a forbidden event
   * enabled is encountered. When state exploration encounters a state
   * with a forbidden event enabled, it suppresses any further outgoing
   * transitions from that state.
   */
  public void addForbiddenEvent(final EventProxy event)
  {
    final Collection<EventProxy> hidden = Collections.singletonList(event);
    addMask(hidden, event, true);
  }

  /**
   * Specifies an event mask for hiding. Events can be masked or hidden
   * by specifying a set of events to be masked and a replacement event.
   * When creating transitions of the output automaton, all events in the
   * mask will be replaced by the specified event. This method can be called
   * multiply; in this case, the result is undefined if the specified event
   * sets are not disjoint.
   * @param  hidden      A set of events to be replaced.
   * @param  replacement An event to be used instead of any of the hidden
   *                     events.
   * @param  forbidden   Whether the given events should be considered as
   *                     forbidden in addition.
   * @see #addForbiddenEvent(EventProxy) addForbiddenEvent()
   */
  public void addMask(final Collection<EventProxy> hidden,
                      final EventProxy replacement,
                      final boolean forbidden)
  {
    if (mMaskingPairs == null) {
      mMaskingPairs = new LinkedList<MaskingPair>();
    }
    final MaskingPair pair = new MaskingPair(hidden, replacement, forbidden);
    mMaskingPairs.add(pair);
  }

  /**
   * Sets whether redundant selfloops are to be removed.
   * If enabled, events that appear as selfloops on all states except dump
   * states and nowhere else are removed from the output, and markings
   * that appear on all states are also removed.
   */
  public void setRemovingSelfloops(final boolean removing)
  {
    mRemovingSelfloops = removing;
  }

  /**
   * Returns whether selfloops are removed.
   * @see #setRemovingSelfloops(boolean) setRemovingSelfloops()
   */
  public boolean getRemovingSelfloops()
  {
    return mRemovingSelfloops;
  }

  /**
   * Sets whether deadlock states are pruned. If enabled, the synchronous
   * product builder checks for deadlock states in the input automata, i.e.,
   * for states that are not marked by any of the configured propositions,
   * and which do not have any outgoing transitions. Synchronous product
   * states, of which at least one state component is a deadlock state, are
   * not expanded and instead merged into a single state.
   * @see #getPropositions()
   */
  public void setPruningDeadlocks(final boolean pruning)
  {
    mPruningDeadlocks = pruning;
  }

  /**
   * Returns whether deadlock states are pruned.
   * @see #setPruningDeadlocks(boolean) setPruningDeadlocks()
   */
  public boolean getPruningDeadlocks()
  {
    return mPruningDeadlocks;
  }


  //#########################################################################
  //# Interface net.sourceforge.waters.model.analysis.SynchronousProductBuilder
  @Override
  public Collection<EventProxy> getPropositions()
  {
    return mConfiguredPropositions;
  }

  @Override
  public void setPropositions(final Collection<EventProxy> props)
  {
    mConfiguredPropositions = props;
  }

  @Override
  public void addMask(final Collection<EventProxy> hidden,
                      final EventProxy replacement)
  {
    addMask(hidden, replacement, false);
  }

  @Override
  public void clearMask()
  {
    mMaskingPairs = null;
  }

  @Override
  public SynchronousProductStateMap getStateMap()
  {
    final ProductDESProxy model = getModel();
    final Collection<AutomatonProxy> automata = model.getAutomata();
    final MemStateMap stateMap = new MemStateMap(automata, mDeadlockState);
    return stateMap;
  }


  //#########################################################################
  //# Interface net.sourceforge.waters.model.analysis.ModelAnalyser
  @Override
  public boolean run()
    throws AnalysisException
  {
    try {
      setUp();
      final int tableSize = Math.min(getNodeLimit(), MAX_TABLE_SIZE);
      final IntArrayHashingStrategy strategy = new IntArrayHashingStrategy();
      mStates = new TObjectIntCustomHashMap<int[]>(strategy);
      mStates.ensureCapacity(tableSize);
      mStateTuples = new ArrayList<int[]>();
      mTransitionBuffer = new TIntArrayList();
      mTransitionBufferLimit = 3 * getTransitionLimit();
      mNumStates = 0;
      mUnvisited = new ArrayDeque<int[]>(100);
      permutations(mNumAutomata, null, -1, -1);
      mNumInitialStates = mNumStates;
      while (!mUnvisited.isEmpty()) {
        final int[] tuple = mUnvisited.remove();
        explore(tuple);
      }
      if (isDetailedOutputEnabled()) {
        final AutomatonProxy aut = createAutomaton();
        return setAutomatonResult(aut);
      } else {
        return true;
      }
    } catch (final AnalysisException exception) {
      throw setExceptionResult(exception);
    } catch (final OutOfMemoryError error) {
      tearDown();
      final Logger logger = getLogger();
      logger.debug("<out of memory>");
      final OverflowException exception = new OverflowException(error);
      throw setExceptionResult(exception);
    } catch (final StackOverflowError error) {
      final OverflowException exception = new OverflowException(error);
      throw setExceptionResult(exception);
    } finally {
      tearDown();
    }
  }

  @Override
  public boolean supportsNondeterminism()
  {
    return true;
  }


  //#########################################################################
  //# Callbacks
  @SuppressWarnings("unchecked")
  public final List<EventProxy> getStateMarking(final int aut, final int state)
  {
    return (List<EventProxy>) mStateMarkings[aut][state];
  }


  //#########################################################################
  //# Overrides for Base Class
  //# net.sourceforge.waters.model.analysis.AbstractModelAnalyser
  @Override
  protected void setUp()
  throws AnalysisException
  {
    super.setUp();

    final ProductDESProxy model = getModel();
    final Collection<EventProxy> events = model.getEvents();
    final Collection<AutomatonProxy> automata = model.getAutomata();
    mNumEvents = events.size();
    mNumAutomata = automata.size();

    TObjectIntHashMap<EventProxy> eventToIndex =
      new TObjectIntHashMap<EventProxy>(mNumInputEvents, 0.5f, -1);
    if (mConfiguredPropositions == null) {
      mCurrentPropositions = new ArrayList<EventProxy>();
    } else {
      mCurrentPropositions = new ArrayList<EventProxy>(mConfiguredPropositions);
    }
    final Collection<EventProxy> forbidden = new THashSet<EventProxy>();
    if (mMaskingPairs != null) {
      for (final MaskingPair pair : mMaskingPairs) {
        if (pair.isForbidden()) {
          final Collection<EventProxy> hidden = pair.getHiddenEvents();
          forbidden.addAll(hidden);
        }
      }
    }
    int nextForbidden = 0;
    int nextNormal = forbidden.size();
    final KindTranslator translator = getKindTranslator();
    for (final EventProxy event : events) {
      if (translator.getEventKind(event) == EventKind.PROPOSITION) {
        if (mConfiguredPropositions == null) {
          mCurrentPropositions.add(event);
        }
      } else if (forbidden.contains(event)) {
        eventToIndex.put(event, nextForbidden++);
      } else {
        eventToIndex.put(event, nextNormal++);
      }
    }
    mNumForbiddenEvents = nextForbidden;
    mNumInputEvents = nextNormal;
    final boolean pruning;
    if (!mPruningDeadlocks) {
      pruning = false;
    } else if (mConfiguredPropositions != null) {
      pruning = mConfiguredPropositions.containsAll(mCurrentPropositions);
    } else {
      pruning = !mCurrentPropositions.isEmpty();
    }
    if (mMaskingPairs != null) {
      mProjectionMask = new int[mNumInputEvents];
      for (int e = 0; e < mNumInputEvents; e++) {
        mProjectionMask[e] = e;
      }
      for (final MaskingPair pair : mMaskingPairs) {
        final EventProxy replacement = pair.getReplacement();
        final int r = eventToIndex.get(replacement);
        final int e;
        if (r >= 0) {
          e = r;
        } else {
          e = nextNormal++;
          eventToIndex.put(replacement, e);
        }
        for (final EventProxy hidden : pair.getHiddenEvents()) {
          final int h = eventToIndex.get(hidden);
          mProjectionMask[h] = e;
        }
      }
      mCurrentSuccessors = new TIntHashSet[nextNormal];
      for (int e = 0; e < nextNormal; e++) {
        mCurrentSuccessors[e] = new TIntHashSet();
      }
    } else if (pruning) {
      mCurrentDeadlock = new boolean[mNumInputEvents];
    }
    mNumEvents = nextNormal;
    mEvents = new EventProxy[nextNormal];
    final TObjectIntIterator<EventProxy> iter = eventToIndex.iterator();
    while (iter.hasNext()) {
      iter.advance();
      final EventProxy event = iter.key();
      final int e = iter.value();
      mEvents[e] = event;
    }
    if (mRemovingSelfloops) {
      mSelfloops = new int[mNumEvents];
    }

    final int numProps = mCurrentPropositions.size();
    mOriginalStates = new StateProxy[mNumAutomata][];
    mAllMarkings = new HashMap<List<EventProxy>,List<EventProxy>>();
    mStateMarkings = new List<?>[mNumAutomata][];
    // transitions indexed first by automaton then by event then by source state
    mTransitions = new int[mNumAutomata][mNumInputEvents][][];
    if (pruning) {
      mDeadlock = new boolean[mNumAutomata][];
    }
    mTargetTuple = new int[mNumAutomata];
    mNDTuple = new int[mNumAutomata][];
    mDeadlockState = -1;

    int a = 0;
    for (final AutomatonProxy aut : automata) {
      checkAbort();
      final Collection<EventProxy> localEvents = aut.getEvents();
      final List<EventProxy> nonLocalProps =
        new ArrayList<EventProxy>(numProps);
      for (final EventProxy prop : mCurrentPropositions) {
        if (!localEvents.contains(prop)) {
          nonLocalProps.add(prop);
        }
      }
      Collections.sort(nonLocalProps);
      final Collection<StateProxy> states = aut.getStates();
      final int numStates = states.size();
      final TObjectIntHashMap<StateProxy> stateToIndex =
        new TObjectIntHashMap<StateProxy>(numStates);
      final TIntArrayList initials = new TIntArrayList(1);
      int snum = 0;
      mOriginalStates[a] = new StateProxy[numStates];
      mStateMarkings[a] = new List<?>[numStates];
      if (mDeadlock != null) {
        mDeadlock[a] = new boolean[numStates];
      }
      for (final StateProxy state : states) {
        stateToIndex.put(state, snum);
        mOriginalStates[a][snum] = state;
        if (state.isInitial()) {
          initials.add(snum);
        }
        final Collection<EventProxy> props = state.getPropositions();
        final List<EventProxy> stateProps;
        if (props.isEmpty()) {
          stateProps = nonLocalProps;
        } else {
          stateProps = new ArrayList<EventProxy>(numProps + props.size());
          stateProps.addAll(nonLocalProps);
          for (final EventProxy prop : props) {
            if (mCurrentPropositions.contains(prop)) {
              stateProps.add(prop);
            }
          }
          Collections.sort(stateProps);
        }
        mStateMarkings[a][snum] = getUniqueMarking(stateProps);
        if (mDeadlock != null) {
          mDeadlock[a][snum] = stateProps.isEmpty();
        }
        snum++;
      }
      mNDTuple[a] = initials.toArray();
      final TIntArrayList[][] autTransitionLists =
        new TIntArrayList[mNumInputEvents][numStates];
      for (final TransitionProxy trans : aut.getTransitions()) {
        final int event = eventToIndex.get(trans.getEvent());
        final int source = stateToIndex.get(trans.getSource());
        final int target = stateToIndex.get(trans.getTarget());
        TIntArrayList list = autTransitionLists[event][source];
        if (list == null) {
          list = new TIntArrayList(1);
          autTransitionLists[event][source] = list;
        }
        list.add(target);
      }
      for (final EventProxy event : localEvents) {
        if (translator.getEventKind(event) != EventKind.PROPOSITION) {
          final int e = eventToIndex.get(event);
          mTransitions[a][e] = new int[numStates][];
          for (int source = 0; source < numStates; source++) {
            final TIntArrayList list = autTransitionLists[e][source];
            if (list != null) {
              mTransitions[a][e][source] = list.toArray();
              if (mDeadlock != null) {
                mDeadlock[a][source] = false;
              }
            }
          }
        }
      }
      a++;
    }
    eventToIndex = null;

    mEventAutomata = new int[mNumInputEvents][];
    final List<IntDouble> list = new ArrayList<IntDouble>(mNumAutomata);
    for (int e = 0; e < mNumInputEvents; e++) {
      for (a = 0; a < mNumAutomata; a++) {
        if (mTransitions[a][e] != null) {
          final int numStates = mTransitions[a][e].length;
          int count = 0;
          for (int source = 0; source < numStates; source++) {
            if (mTransitions[a][e][source] != null) {
              count++;
            }
          }
          final double avg = (double) count / (double) numStates;
          final IntDouble pair = new IntDouble(a, avg);
          list.add(pair);
        }
      }
      Collections.sort(list);
      final int count = list.size();
      mEventAutomata[e] = new int[count];
      int i = 0;
      for (final IntDouble pair : list) {
        mEventAutomata[e][i++] = pair.mInt;
      }
      list.clear();
    }
  }

  @Override
  protected void addStatistics()
  {
    super.addStatistics();
    final AutomatonResult result = getAnalysisResult();
    result.setNumberOfAutomata(mNumAutomata);
    result.setNumberOfStates(mNumStates);
    if (mTransitionBuffer != null) {
      result.setNumberOfTransitions(mTransitionBuffer.size() / 3);
    }
    if (mStateCallback != null) {
      mStateCallback.recordStatistics(result);
    }
  }

  @Override
  protected void tearDown()
  {
    super.tearDown();
    mEvents = null;
    mCurrentPropositions = null;
    mProjectionMask = null;
    mOriginalStates = null;
    mAllMarkings = null;
    mStateMarkings = null;
    mTransitions = null;
    mDeadlock = null;
    mEventAutomata = null;
    mStates = null;
    mStateTuples = null;
    mUnvisited = null;
    mTransitionBuffer = null;
    mSelfloops = null;
    mNDTuple = null;
    mTargetTuple = null;
    mCurrentSuccessors = null;
    mCurrentDeadlock = null;
  }


  //#########################################################################
  //# Auxiliary Methods
  private void explore(final int[] sourceTuple)
    throws AnalysisException
  {
    checkAbort();
    final int source = mStates.get(sourceTuple);
    if (mCurrentSuccessors != null) {
      for (int e = 0; e < mNumEvents; e++) {
        mCurrentSuccessors[e].clear();
      }
    } else if (mCurrentDeadlock != null) {
      Arrays.fill(mCurrentDeadlock, false);
    }
    forbidden:
    for (int e = 0; e < mNumForbiddenEvents; e++) {
      for (final int a : mEventAutomata[e]) {
        if (mTransitions[a][e] != null &&
            mTransitions[a][e][sourceTuple[a]] == null) {
          continue forbidden;
        }
      }
      addTransition(source, e, source);
      return;
    }
    events:
    for (int e = mNumForbiddenEvents; e < mNumInputEvents; e++) {
      Arrays.fill(mNDTuple, null);
      for (final int a : mEventAutomata[e]) {
        if (mTransitions[a][e] != null) {
          final int[] succ = mTransitions[a][e][sourceTuple[a]];
          if (succ == null) {
            continue events;
          }
          mNDTuple[a] = succ;
        }
      }
      permutations(mNumAutomata, sourceTuple, source, e);
    }
  }

  private void permutations(int a,
                            final int[] sourceTuple,
                            final int source,
                            final int event)
    throws OverflowException
  {
    if (a == 0) {
      addTargetState(source, event, sourceTuple == null); // data in mTuple
    } else {
      a--;
      final int[] codes = mNDTuple[a];
      if (codes == null) {
        mTargetTuple[a] = sourceTuple[a];
        permutations(a, sourceTuple, source, event);
      } else {
        for (int i = 0; i < codes.length; i++) {
          mTargetTuple[a] = codes[i];
          permutations(a, sourceTuple, source, event);
        }
      }
    }
  }

  private void addTargetState(final int source,
                              final int event,
                              final boolean isInitial)
    throws OverflowException
  {
    final int target;
    if (mStates.containsKey(mTargetTuple)) {
      target = mStates.get(mTargetTuple);
    } else if (mDeadlock != null && isDeadlockTuple()) {
      if (mDeadlockState < 0) {
        mDeadlockState = getNewState();
        final int[] newTuple = Arrays.copyOf(mTargetTuple, mNumAutomata);
        mStates.put(newTuple, mDeadlockState);
        mStateTuples.add(newTuple);
      }
      target = mDeadlockState;
    } else {
      if (mStateCallback != null && !mStateCallback.newState(mTargetTuple)) {
        return;
      }
      target = getNewState();
      final int[] newTuple = Arrays.copyOf(mTargetTuple, mNumAutomata);
      mStates.put(newTuple, target);
      mUnvisited.offer(newTuple);
      mStateTuples.add(newTuple);
    }
    // Only add a transition if not adding in an initial state,
    // and avoid duplicates.
    if (!isInitial) {
      addTransition(source, event, target);
    }
  }

  private int getNewState()
    throws OverflowException
  {
    final int limit = getNodeLimit();
    if (mNumStates >= limit) {
      throw new OverflowException(limit);
    }
    return mNumStates++;
  }

  private boolean isDeadlockTuple()
  {
    for (int a = 0; a < mNumAutomata; a++) {
      final int state = mTargetTuple[a];
      if (mDeadlock[a][state]) {
        return true;
      }
    }
    return false;
  }

  private void addTransition(final int source,
                             final int event,
                             final int target)
  throws OverflowException
  {
    final int masked;
    if (mProjectionMask == null) {
      masked = event;
      if (target == mDeadlockState) {
        if (mCurrentDeadlock[event]) {
          return;
        } else {
          mCurrentDeadlock[event] = true;
        }
      }
    } else {
      masked = mProjectionMask[event];
      if (!mCurrentSuccessors[masked].add(target)) {
        return;
      }
    }
    if (mTransitionBuffer.size() >= mTransitionBufferLimit) {
      throw new OverflowException(OverflowKind.TRANSITION,
                                  getTransitionLimit());
    }
    mTransitionBuffer.add(source);
    mTransitionBuffer.add(masked);
    mTransitionBuffer.add(target);
    if (mSelfloops != null) {
      if (source != target) {
        mSelfloops[masked] = -1;
      } else if (mSelfloops[masked] >= 0) {
        mSelfloops[masked]++;
      }
    }
  }

  private AutomatonProxy createAutomaton()
    throws AnalysisException
  {
    final int numSelfloopStates = mNumStates - (mDeadlockState >= 0 ? 1 : 0);
    final Set<EventProxy> skip;
    if (mMaskingPairs == null) {
      skip = Collections.emptySet();
    } else {
      skip = new THashSet<EventProxy>(mNumEvents);
      for (final MaskingPair pair : mMaskingPairs) {
        final Collection<EventProxy> hidden = pair.getHiddenEvents();
        skip.addAll(hidden);
      }
      for (final MaskingPair pair : mMaskingPairs) {
        final EventProxy replacement = pair.getReplacement();
        skip.remove(replacement);
      }
    }
    final int numEvents = mNumEvents + mCurrentPropositions.size() - skip.size();
    final Collection<EventProxy> events = new ArrayList<EventProxy>(numEvents);
    for (int e = 0; e < mNumEvents; e++) {
      final EventProxy event = mEvents[e];
      if (skip.contains(event) ||
          mSelfloops != null && mSelfloops[e] == numSelfloopStates) {
        mEvents[e] = null;
      } else {
        events.add(event);
      }
    }

    // Delete propositions that appear in all states ...
    int numProps = mCurrentPropositions.size();
    if (mRemovingSelfloops) {
      final Set<EventProxy> allProps =
        new THashSet<EventProxy>(mCurrentPropositions);
      final Set<EventProxy> localProps = new THashSet<EventProxy>(numProps);
      for (int code = 0; code < mNumStates; code++) {
        checkAbort();
        final int[] tuple = mStateTuples.get(code);
        localProps.addAll(mCurrentPropositions);
        for (int a = 0; a < mNumAutomata; a++) {
          final List<EventProxy> stateMarking = getStateMarking(a, tuple[a]);
          localProps.retainAll(stateMarking);
          if (localProps.isEmpty()) {
            break;
          }
        }
        allProps.retainAll(localProps);
        if (allProps.isEmpty()) {
          break;
        }
        localProps.clear();
      }
      if (!allProps.isEmpty()) {
        mCurrentPropositions.removeAll(allProps);
        numProps = mCurrentPropositions.size();
      }
    }
    events.addAll(mCurrentPropositions);

    final List<StateProxy> states = new ArrayList<StateProxy>(mNumStates);
    for (int code = 0; code < mNumStates; code++) {
      checkAbort();
      final boolean initial = code < mNumInitialStates;
      final int[] tuple = mStateTuples.get(code);
      final List<EventProxy> marking = new ArrayList<EventProxy>(numProps);
      props:
      for (final EventProxy prop : mCurrentPropositions) {
        for (int a = 0; a < mNumAutomata; a++) {
          final List<EventProxy> stateMarking = getStateMarking(a, tuple[a]);
          if (Collections.binarySearch(stateMarking, prop) < 0) {
            continue props;
          }
        }
        marking.add(prop);
      }
      Collections.sort(marking);
      final List<EventProxy> unique = getUniqueMarking(marking);
      final StateProxy state = new MemStateProxy(code, tuple, unique, initial);
      states.add(state);
    }

    final ProductDESProxyFactory factory = getFactory();
    final int bufferSize = mTransitionBuffer.size();
    final ArrayList<TransitionProxy> transitions =
      new ArrayList<TransitionProxy>(bufferSize / 3);
    int i = 0;
    while (i < bufferSize) {
      final int s = mTransitionBuffer.get(i++);
      final int e = mTransitionBuffer.get(i++);
      final int t = mTransitionBuffer.get(i++);
      final EventProxy event = mEvents[e];
      if (event != null) {
        checkAbort();
        final StateProxy source = states.get(s);
        final StateProxy target = states.get(t);
        transitions.add(factory.createTransitionProxy(source, event, target));
      }
    }

    final String name = computeOutputName();
    final ComponentKind kind = computeOutputKind();
    return factory.createAutomatonProxy
      (name, kind, events, states, transitions);
  }

  private List<EventProxy> getUniqueMarking(final List<EventProxy> marking)
  {
    final List<EventProxy> found = mAllMarkings.get(marking);
    if (found == null) {
      mAllMarkings.put(marking, marking);
      return marking;
    } else {
      return found;
    }
  }


  //#########################################################################
  //# Local Interface StateCallback
  /**
   * A callback interface to enable the user to perform custom actions
   * when a new state is encountered.
   */
  public interface StateCallback
  {

    /**
     * This method is called by the {@link MonolithicSynchronousProductBuilder}
     * before adding a new state to the synchronous product state space.
     * @param  tuple    Integer array representing state codes of a new
     *                  state tuple.
     * @return <CODE>true</CODE> if the state should be included in the
     *         synchronous product, <CODE>false</CODE> if it should be
     *         suppressed.
     */
    public boolean newState(int[] tuple) throws OverflowException;

    /**
     * This method is called by the {@link MonolithicSynchronousProductBuilder}
     * while populating its result.
     * @param  result   The result record being created. When the method is
     *                  called, the result already contains all the standard
     *                  information recorded by the synchronous product
     *                  builder.
     */
    public void recordStatistics(AutomatonResult result);

  }


  //#########################################################################
  //# Inner Class MaskingPair
  private static class MaskingPair
  {
    //#######################################################################
    //# Constructor
    private MaskingPair(final Collection<EventProxy> hidden,
                        final EventProxy replacement,
                        final boolean forbidden)
    {
      mHiddenEvents = hidden;
      mReplacement = replacement;
      mForbidden = forbidden;
    }

    //#######################################################################
    //# Simple Access
    private Collection<EventProxy> getHiddenEvents()
    {
      return mHiddenEvents;
    }

    private EventProxy getReplacement()
    {
      return mReplacement;
    }

    private boolean isForbidden()
    {
      return mForbidden;
    }

    //#######################################################################
    //# Data Members
    private final Collection<EventProxy> mHiddenEvents;
    private final EventProxy mReplacement;
    private final boolean mForbidden;
  }


  //#########################################################################
  //# Inner Class MemStateMap
  private static class MemStateMap implements SynchronousProductStateMap
  {
    //#######################################################################
    //# Constructor
    private MemStateMap(final Collection<AutomatonProxy> automata,
                        final int dumpState)
    {
      mInputAutomata = new ArrayList<AutomatonProxy>(automata);
      final int numaut = automata.size();
      mStateLists = new StateProxy[numaut][];
      // Assumes state codes are given by their ordering in the original
      // automata. If this is not good enough, need to provide method
      // setStateList(int a, StateProxy[] states).
      int a = 0;
      for (final AutomatonProxy aut : mInputAutomata) {
        final Collection<StateProxy> states = aut.getStates();
        final int size = states.size();
        mStateLists[a++] = states.toArray(new StateProxy[size]);
      }
      mDumpState = dumpState;
    }

    //#######################################################################
    //# Interface
    //# net.sourceforge.waters.model.analysis.SynchronousProductStateMap
    @Override
    public Collection<AutomatonProxy> getInputAutomata()
    {
      return mInputAutomata;
    }

    @Override
    public StateProxy getOriginalState(final StateProxy state,
                                       final AutomatonProxy aut)
    {
      final int a = getAutomatonIndex(aut);
      final MemStateProxy memstate = (MemStateProxy) state;
      if (memstate.getCode() == mDumpState) {
        return null;
      } else {
        final int[] tuple = memstate.getStateTuple();
        final int code = tuple[a];
        return mStateLists[a][code];
      }
    }

    //#######################################################################
    //# Auxiliary Methods
    /**
     * Gets the index position of the given automaton in state tuples. Presently
     * linear complexity --- is this good enough?
     */
    private int getAutomatonIndex(final AutomatonProxy aut)
    {
      return mInputAutomata.indexOf(aut);
    }

    //#######################################################################
    //# Data Members
    private final List<AutomatonProxy> mInputAutomata;
    private final StateProxy[][] mStateLists;
    private final int mDumpState;
  }


  //#########################################################################
  //# Inner Class MemStateProxy
  /**
   * Stores states, encoding the name as an int rather than a long string
   * value.
   */
  private static class MemStateProxy implements StateProxy
  {
    //#######################################################################
    //# Constructor
    private MemStateProxy(final int name, final int[] stateTuple,
                          final Collection<EventProxy> props,
                          final boolean isInitial)
    {
      mName = name;
      mStateTuple = stateTuple;
      mProps = props;
      mIsInitial = isInitial;
    }

    //#######################################################################
    //# Interface net.sourceforge.waters.model.des.StateProxy
    @Override
    public Collection<EventProxy> getPropositions()
    {
      return mProps;
    }

    @Override
    public boolean isInitial()
    {
      return mIsInitial;
    }

    @Override
    public MemStateProxy clone()
    {
      return new MemStateProxy(mName, mStateTuple, mProps, mIsInitial);
    }

    public int getCode()
    {
      return mName;
    }

    @Override
    public String getName()
    {
      return "S:" + mName;
    }

    public int[] getStateTuple()
    {
      return mStateTuple;
    }

    @Override
    public boolean refequals(final NamedProxy o)
    {
      if (o instanceof MemStateProxy) {
        final MemStateProxy s = (MemStateProxy) o;
        return s.mName == mName;
      } else {
        return false;
      }
    }

    @Override
    public int refHashCode()
    {
      return mName;
    }

    @Override
    public Object acceptVisitor(final ProxyVisitor visitor)
        throws VisitorException
    {
      final ProductDESProxyVisitor desvisitor =
          (ProductDESProxyVisitor) visitor;
      return desvisitor.visitStateProxy(this);
    }

    @Override
    public Class<StateProxy> getProxyInterface()
    {
      return StateProxy.class;
    }

    @Override
    public int compareTo(final NamedProxy n)
    {
      return n.getName().compareTo(getName());
    }

    //#######################################################################
    //# Debugging
    @Override
    public String toString()
    {
      return ProxyPrinter.getPrintString(this);
    }

    //#######################################################################
    //# Data Members
    private final int mName;
    private final int[] mStateTuple;
    private final boolean mIsInitial;
    private final Collection<EventProxy> mProps;
  }


  //#########################################################################
  //# Inner Class IntDouble
  private static class IntDouble implements Comparable<IntDouble>
  {
    public IntDouble(final int i, final double d)
    {
      mInt = i;
      mDouble = d;
    }

    @Override
    public int compareTo(final IntDouble pair)
    {
      if (mDouble < pair.mDouble) {
        return -1;
      } else if (mDouble > pair.mDouble) {
        return 1;
      } else {
        return 0;
      }
    }

    private final int mInt;
    private final double mDouble;
  }


  //#########################################################################
  //# Data Members
  private Collection<EventProxy> mConfiguredPropositions;
  private StateCallback mStateCallback;
  private Collection<MaskingPair> mMaskingPairs;
  private boolean mRemovingSelfloops;
  private boolean mPruningDeadlocks;

  private int mNumAutomata;
  private int mNumForbiddenEvents;
  private int mNumInputEvents;
  private int mNumEvents;
  private EventProxy[] mEvents;
  private Collection<EventProxy> mCurrentPropositions;
  private int[] mProjectionMask;
  private StateProxy[][] mOriginalStates;
  private Map<List<EventProxy>,List<EventProxy>> mAllMarkings;
  private List<?>[][] mStateMarkings;
  private int[][][][] mTransitions;
  private boolean[][] mDeadlock;
  private int[][] mEventAutomata;

  private int mNumStates;
  private int mNumInitialStates;
  private TObjectIntCustomHashMap<int[]> mStates;
  private List<int[]> mStateTuples;
  private Queue<int[]> mUnvisited;
  private TIntArrayList mTransitionBuffer;
  private int mTransitionBufferLimit;
  private int[] mSelfloops;

  private int[][] mNDTuple;
  private int[] mTargetTuple;
  private TIntHashSet[] mCurrentSuccessors;
  private boolean[] mCurrentDeadlock;
  private int mDeadlockState;


  //#########################################################################
  //# Class Constants
  private static final int MAX_TABLE_SIZE = 500000;

}

