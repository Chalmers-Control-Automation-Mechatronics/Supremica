//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters Analysis
//# PACKAGE: net.sourceforge.waters.analysis.monolithic
//# CLASS:   MonolithicSynchronousProductBuilder
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.analysis.monolithic;

import gnu.trove.THashSet;
import gnu.trove.TIntArrayList;
import gnu.trove.TObjectIntHashMap;
import gnu.trove.TObjectIntIterator;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.BitSet;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Deque;
import java.util.Set;
import net.sourceforge.waters.analysis.tr.IntArrayHashingStrategy;
import net.sourceforge.waters.model.analysis.AbstractModelBuilder;
import net.sourceforge.waters.model.analysis.AnalysisException;
import net.sourceforge.waters.model.analysis.DefaultProductDESResult;
import net.sourceforge.waters.model.analysis.KindTranslator;
import net.sourceforge.waters.model.analysis.OverflowException;
import net.sourceforge.waters.model.analysis.OverflowKind;
import net.sourceforge.waters.model.analysis.ProductDESResult;
import net.sourceforge.waters.model.analysis.ProxyResult;
import net.sourceforge.waters.model.analysis.SupervisorSynthesizer;
import net.sourceforge.waters.model.analysis.SynchronousProductStateMap;
import net.sourceforge.waters.model.base.NamedProxy;
import net.sourceforge.waters.model.base.ProxyVisitor;
import net.sourceforge.waters.model.base.VisitorException;
import net.sourceforge.waters.model.des.AutomatonProxy;
import net.sourceforge.waters.model.des.AutomatonTools;
import net.sourceforge.waters.model.des.EventProxy;
import net.sourceforge.waters.model.des.ProductDESProxy;
import net.sourceforge.waters.model.des.ProductDESProxyFactory;
import net.sourceforge.waters.model.des.ProductDESProxyVisitor;
import net.sourceforge.waters.model.des.StateProxy;
import net.sourceforge.waters.model.des.TransitionProxy;
import net.sourceforge.waters.xsd.base.ComponentKind;
import net.sourceforge.waters.xsd.base.EventKind;

import org.apache.log4j.Logger;


/**
 * A Java implementation of the monolithic synchronous product algorithm. This
 * implementation supports nondeterministic automata and hiding. States are
 * stored in integer arrays without compression, so it is not recommended to
 * use this implementation to compose a large number of automata.
 *
 * @author Simon Ware, Rachel Francis, Robi Malik
 */

public class MonolithicSynthesizer extends
  AbstractModelBuilder<ProductDESProxy> implements SupervisorSynthesizer
{

  //#########################################################################
  //# Constructors
  public MonolithicSynthesizer(final ProductDESProxyFactory factory)
  {
    super(factory);
  }

  public MonolithicSynthesizer(final ProductDESProxy model,
                               final ProductDESProxyFactory factory)
  {
    super(model, factory);
  }

  public MonolithicSynthesizer(final ProductDESProxy model,
                               final ProductDESProxyFactory factory,
                               final KindTranslator translator)
  {
    super(model, factory, translator);
  }

  //#########################################################################
  //# Configuration

  //#########################################################################
  //# Interface net.sourceforge.waters.model.analysis.SynchronousProductBuilder
  public Collection<EventProxy> getPropositions()
  {
    return mUsedPropositions;
  }

  public void setPropositions(final Collection<EventProxy> props)
  {
    mUsedPropositions = props;
  }

  public SynchronousProductStateMap getStateMap()
  {
    final ProductDESProxy model = getModel();
    final Collection<AutomatonProxy> automata = model.getAutomata();
    final MemStateMap stateMap = new MemStateMap(automata, mDeadlockState);
    return stateMap;
  }

  @Override
  public ProductDESResult createAnalysisResult()
  {
    return new DefaultProductDESResult();
  }

  @Override
  public ProductDESResult getAnalysisResult()
  {
    return (ProductDESResult) super.getAnalysisResult();
  }

  @Override
  public ProductDESProxy getComputedProductDES()
  {
    return getAnalysisResult().getComputedProductDES();
  }

  //#########################################################################
  //# Interface net.sourceforge.waters.model.analysis.ModelAnalyser
  public boolean run() throws AnalysisException
  {
    try {
      setUp();

      while (!mGlobalStack.isEmpty()) {
        final int[] sentinel = new int[1];
        final int[] root = mGlobalStack.pop();
        final int r = mGlobalVisited.get(root);
        if (mSafeStates.get(r) || mBadStates.get(r)) {
          continue;
        }
        mLocalStack.push(root);
        mLocalVisited.add(root);
        mBackTrace.push(sentinel);
        boolean safe = true;
        while (!mLocalStack.isEmpty()) {
          if (mLocalStack.peek() == mBackTrace.peek()) {
            mLocalStack.pop();
            mBackTrace.pop();
          } else {
            final int[] current = mLocalStack.peek();
            mBackTrace.push(current);
            safe = mUnctrlInitialReachabilityExplorer.explore(current);
            if (!safe) {
              break;
            }
          }
        }
        if (safe) {
          for (final int[] current : mLocalVisited) {
            final int n = addNewState(current);
            mSafeStates.set(n);
          }
          for (final int[] current : mLocalVisited) {
            mCtrlInitialReachabilityExplorer.explore(current);
          }
        } else {
          for (final int[] current : mBackTrace) {
            if(current == sentinel){
              continue;
            }else{
              int n = 0;
              if(mGlobalVisited.containsKey(current)){
                n = mGlobalVisited.get(current);
                if (n < mNumInitialStates) {
                  return setBooleanResult(false);
                }
              }else{
                n = addNewState(current);
              }
              mBadStates.set(n);
            }
          }
        }
        mLocalStack.clear();
        mLocalVisited.clear();
        mBackTrace.clear();
      }
      //........................................................................
      //========================================================================
      while (mCoreachabilityChanged || mControllabilityChanged) {
        //trim
        mNonCoreachableStates.set(0, mNumStates);
        mNumCoreachableStates = 0;
        mCoreachabilityChanged = false;
        tuples: for (int t = 0; t < mStateTuples.size(); t++) {
          if (!mBadStates.get(t)) {
            final int[] aTuple = mStateTuples.get(t);
            for (int aut = 0; aut < aTuple.length; aut++) {
              if (!mStateMarkings[aut][aTuple[aut]].isEmpty()) {
                if (aut == aTuple.length - 1) {
                  mUnvisited.add(mStateTuples.get(t));
                  mNonCoreachableStates.set(t, false);
                  mNumCoreachableStates++;
                }
              } else {
                continue tuples;
              }
            }
            if (aTuple.length == 0) {
              mNonCoreachableStates.set(t, false);
              mNumCoreachableStates++;
            }
          }
        }
        while (!mUnvisited.isEmpty()) {
          final int[] ss = mUnvisited.remove();
          mCoreachabilityExplorer.explore(ss);
        }
        if (mNumCoreachableStates != mLastNumCoreachableStates) {
          mCoreachabilityChanged = true;
          mLastNumCoreachableStates = mNumCoreachableStates;
        }
        for (int b = 0; b < mNumInitialStates; b++) {
          if (!mBadStates.get(b) && mNonCoreachableStates.get(b)) {
            initialIsBad = true;
          }
        }
        mBadStates.or(mNonCoreachableStates);
        if (!mCoreachabilityChanged && !mControllabilityChanged) {
          break;
        }
        //mark uncontrollable states (bad states)
        mControllabilityChanged = false;
        for (int e = 0; e < mNumUncontrollableEvents; e++) {
          states: for (int state = 0; state < mStateTuples.size(); state++) {
            if (!mBadStates.get(state)) {
              mSuccessorStatesExplorer.explore(mStateTuples.get(state));
              for (int a = 0; a < mEventAutomata[e].length; a++) {
                final int aut = mEventAutomata[e][a];
                final int source = mStateTuples.get(state)[aut];
                if (mTransitions[aut][e][source] == null) {
                  if (aut < mNumPlants) {
                    continue states;
                  } else {
                    if (state < mNumInitialStates) {
                      initialIsBad = true;
                    }
                    mBadStates.set(state);
                    mControllabilityChanged = true;
                  }
                }
              }
            }
          }
        }
        if (!mCoreachabilityChanged && !mControllabilityChanged) {
          break;
        }
      }
      mUnvisited = new ArrayDeque<int[]>(100);
      mFinalReachabilityExplorer.permutations(mNumAutomata, null, -1, -1);
      while (!mUnvisited.isEmpty()) {
        final int[] tuple = mUnvisited.remove();
        if (mGlobalVisited.containsKey(tuple)) {
          mFinalReachabilityExplorer.explore(tuple);
        }
      }
      for (int b = 0; b < mNumInitialStates; b++) {
        if (!mReachableStates.get(b)) {
          initialIsBad = true;
        }
      }
      if (initialIsBad) {
        return setBooleanResult(false);
      }
      if (getConstructsResult()) {
        final AutomatonProxy aut = createAutomaton();
        final ProductDESProxy des =
          AutomatonTools.createProductDESProxy(aut, getFactory());
        return setProxyResult(des);
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
  protected void setUp() throws AnalysisException
  {
    super.setUp();

    final ProductDESProxy model = getModel();
    final Collection<EventProxy> events = model.getEvents();
    mNumEvents = events.size();

    final KindTranslator translator = getKindTranslator();

    ArrayList<AutomatonProxy> plants = new ArrayList<AutomatonProxy>();
    ArrayList<AutomatonProxy> specs = new ArrayList<AutomatonProxy>();
    for (final AutomatonProxy aut : model.getAutomata()) {
      if (translator.getComponentKind(aut) == ComponentKind.PLANT) {
        plants.add(aut);
      } else if (translator.getComponentKind(aut) == ComponentKind.SPEC) {
        specs.add(aut);
      }
    }
    mNumAutomata = plants.size() + specs.size();
    mNumPlants = plants.size();
    mAutomata = new ArrayList<AutomatonProxy>(mNumAutomata);
    mAutomata.addAll(plants);
    mAutomata.addAll(specs);
    plants = specs = null;

    TObjectIntHashMap<EventProxy> eventToIndex =
      new TObjectIntHashMap<EventProxy>(mNumEvents);
    if (mUsedPropositions == null) {
      mCurrentPropositions = new ArrayList<EventProxy>();
    } else {
      mCurrentPropositions = mUsedPropositions;
    }

    int numProperEvents = 0;
    for (final EventProxy event : events) {
      if (translator.getEventKind(event) != EventKind.PROPOSITION) {
        numProperEvents += 1;
      }
    }
    int unctrlEvents = 0;
    int ctrlEvents = numProperEvents - 1;
    for (final EventProxy event : events) {
      if (translator.getEventKind(event) == EventKind.PROPOSITION) {
        if (mUsedPropositions == null) {
          mCurrentPropositions.add(event);
        }
      } else if (translator.getEventKind(event) == EventKind.UNCONTROLLABLE) {
        eventToIndex.put(event, unctrlEvents++);
      } else if (translator.getEventKind(event) == EventKind.CONTROLLABLE) {
        eventToIndex.put(event, ctrlEvents--);
      }
    }
    mNumProperEvents = numProperEvents;
    mNumUncontrollableEvents = unctrlEvents;
    mCurrentDeadlock = new boolean[mNumProperEvents];
    mEvents = new EventProxy[numProperEvents];
    final TObjectIntIterator<EventProxy> iter = eventToIndex.iterator();
    while (iter.hasNext()) {
      iter.advance();
      final EventProxy event = iter.key();
      final int e = iter.value();
      mEvents[e] = event;
    }

    final int numProps = mCurrentPropositions.size();
    mOriginalStates = new StateProxy[mNumAutomata][];
    mAllMarkings = new HashMap<List<EventProxy>,List<EventProxy>>();
    mStateMarkings = new List<?>[mNumAutomata][];
    //transitions indexed first by automaton then by event then by source state
    mTransitions = new int[mNumAutomata][mNumProperEvents][][];
    mReverseTransitions = new int[mNumAutomata][mNumProperEvents][][];
    mEventAutomata = new int[mNumProperEvents][];
    mReverseEventAutomata = new int[mNumProperEvents][];

    mDeadlock = new boolean[mNumAutomata][];

    mTargetTuple = new int[mNumAutomata];
    mNDTuple = new int[mNumAutomata][];
    mNDTupleRvs = new int[mNumAutomata][];
    mDeadlockState = -1;

    int a = 0;
    for (final AutomatonProxy aut : mAutomata) {
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
      mDeadlock[a] = new boolean[numStates];

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

        mDeadlock[a][snum] = stateProps.isEmpty();

        snum++;
      }
      mNDTuple[a] = initials.toNativeArray();
      final TIntArrayList[][] autTransitionLists =
        new TIntArrayList[mNumProperEvents][numStates];
      final TIntArrayList[][] autTransitionListsRvs =
        new TIntArrayList[mNumProperEvents][numStates];
      for (final TransitionProxy trans : aut.getTransitions()) {
        final int event = eventToIndex.get(trans.getEvent());
        final int source = stateToIndex.get(trans.getSource());
        final int target = stateToIndex.get(trans.getTarget());
        TIntArrayList list = autTransitionLists[event][source];
        TIntArrayList listRvs = autTransitionListsRvs[event][target];
        if (list == null) {
          list = new TIntArrayList(1);
          autTransitionLists[event][source] = list;
        }
        list.add(target);
        if (source != target) {
          mDeadlock[a][source] = false;
        }
        if (listRvs == null) {
          listRvs = new TIntArrayList(1);
          autTransitionListsRvs[event][target] = listRvs;
        }
        listRvs.add(source);
      }
      for (final EventProxy event : localEvents) {
        if (translator.getEventKind(event) != EventKind.PROPOSITION) {
          final int e = eventToIndex.get(event);
          mTransitions[a][e] = new int[numStates][];
          mReverseTransitions[a][e] = new int[numStates][];
          for (int source = 0; source < numStates; source++) {
            final TIntArrayList list = autTransitionLists[e][source];
            if (list != null) {
              mTransitions[a][e][source] = list.toNativeArray();
            }
          }
          for (int target = 0; target < numStates; target++) {
            final TIntArrayList listRvs = autTransitionListsRvs[e][target];
            if (listRvs != null) {
              mReverseTransitions[a][e][target] = listRvs.toNativeArray();
            }
          }
        }
      }
      a++;
    }
    eventToIndex = null;

    mEventAutomata = new int[mNumProperEvents][];
    mReverseEventAutomata = new int[mNumProperEvents][];
    final List<AutomatonEventInfo> list =
      new ArrayList<AutomatonEventInfo>(mNumAutomata);
    final List<AutomatonEventInfo> listRvs =
      new ArrayList<AutomatonEventInfo>(mNumAutomata);
    for (int e = 0; e < mNumProperEvents; e++) {
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
          if (e >= mNumUncontrollableEvents) {
            final ControllableAutomatonEventInfo pair =
              new ControllableAutomatonEventInfo(a, avg);
            list.add(pair);
          } else {
            final UncontrollableAutomatonEventInfo pair =
              new UncontrollableAutomatonEventInfo(a, avg);
            list.add(pair);
          }
        }
        if (mReverseTransitions[a][e] != null) {
          final int numStates = mReverseTransitions[a][e].length;
          int countRvs = 0;
          for (int target = 0; target < numStates; target++) {
            if (mReverseTransitions[a][e][target] != null) {
              countRvs++;
            }
          }
          final double avgRvs = (double) countRvs / (double) numStates;
          final ControllableAutomatonEventInfo pairRvs =
            new ControllableAutomatonEventInfo(a, avgRvs);
          listRvs.add(pairRvs);
        }
      }
      Collections.sort(list);
      Collections.sort(listRvs);
      final int count = list.size();
      final int countRvs = listRvs.size();
      mEventAutomata[e] = new int[count];
      mReverseEventAutomata[e] = new int[countRvs];
      int i = 0;
      for (final AutomatonEventInfo info : list) {
        mEventAutomata[e][i++] = info.getAutomaton();
      }
      int j = 0;
      for (final AutomatonEventInfo info : listRvs) {
        mReverseEventAutomata[e][j++] = info.getAutomaton();
      }
      list.clear();
      listRvs.clear();
    }

    final int tableSize = Math.min(getNodeLimit(), MAX_TABLE_SIZE);
    mNonCoreachableStates = new BitSet(mNumStates);
    mReachableStates = new BitSet(mNumStates);
    mBadStates = new BitSet(mNumStates);
    mSafeStates = new BitSet(mNumStates);
    mCoreachabilityChanged = true;
    mControllabilityChanged = true;
    final IntArrayHashingStrategy strategy = new IntArrayHashingStrategy();
    mGlobalVisited = new TObjectIntHashMap<int[]>(strategy);
    mGlobalVisited.ensureCapacity(tableSize);
    mLocalVisited = new THashSet<int[]>(strategy);
    mGlobalStack = new ArrayDeque<int[]>();
    mLocalStack = new ArrayDeque<int[]>();
    mBackTrace = new ArrayDeque<int[]>();
    mStateTuples = new ArrayList<int[]>();
    mTransitionBuffer = new TIntArrayList();
    mTransitionBufferLimit = 3 * getTransitionLimit();
    mNumStates = 0;
    mUnvisited = new ArrayDeque<int[]>(100);

    mCtrlInitialReachabilityExplorer =
      new CtrlInitialReachabilityExplorer(mEventAutomata, mTransitions, mNDTuple,
                                      mNumUncontrollableEvents,
                                      mNumProperEvents - 1);

    mCtrlInitialReachabilityExplorer.permutations(mNumAutomata, null, -1, -1);
    mNDTupleFinal = Arrays.copyOf(mNDTuple, mNumAutomata);
    mNumInitialStates = mNumStates;

    mUnctrlInitialReachabilityExplorer =
      new UnctrlInitialReachabilityExplorer(mEventAutomata, mTransitions,
                                            mNDTuple, 0,
                                            mNumUncontrollableEvents);
    mFinalReachabilityExplorer =
      new FinalReachabilityExplorer(mEventAutomata, mTransitions,
                                    mNDTupleFinal, 0, mNumProperEvents - 1);
    mCoreachabilityExplorer =
      new CoreachabilityExplorer(mReverseEventAutomata, mReverseTransitions,
                                 mNDTupleRvs, 0, mNumProperEvents - 1);
    mSuccessorStatesExplorer =
      new SuccessorStatesExplorer(mEventAutomata, mTransitions, mNDTuple, 0,
                                  mNumUncontrollableEvents - 1);

  }

  @Override
  protected void addStatistics()
  {
    super.addStatistics();
    final ProxyResult<ProductDESProxy> result = getAnalysisResult();
    result.setNumberOfAutomata(mNumAutomata);
    result.setNumberOfStates(mNumStates);
    if (mTransitionBuffer != null) {
      result.setNumberOfTransitions(mTransitionBuffer.size() / 3);
    }
  }

  @Override
  protected void tearDown()
  {
    super.tearDown();
    mEvents = null;
    mCurrentPropositions = null;
    mOriginalStates = null;
    mAllMarkings = null;
    mStateMarkings = null;
    mTransitions = null;
    mDeadlock = null;
    mEventAutomata = null;
    mGlobalVisited = null;
    mStateTuples = null;
    mUnvisited = null;
    mTransitionBuffer = null;
    mNDTuple = null;
    mTargetTuple = null;
    mCurrentDeadlock = null;
  }

  //#########################################################################
  //# Auxiliary Methods

  private int addNewState(final int[] tuple) throws OverflowException
  {
    if (mGlobalVisited.containsKey(tuple)) {
      return mGlobalVisited.get(tuple);
    } else {
      final int code = mNumStates++;
      final int limit = getNodeLimit();
      if (mNumStates >= limit) {
        throw new OverflowException(limit);
      }
      final int[] newTuple = Arrays.copyOf(tuple, mNumAutomata);
      mGlobalVisited.put(newTuple, code);
      mStateTuples.add(newTuple);
      return code;
    }
  }

  private boolean isNewState(final int[] tuple) throws OverflowException
  {
    final int currentNumStates = mNumStates;
    addNewState(tuple);
    if (currentNumStates == mNumStates) {
      return false;
    }
    return true;
  }

  @SuppressWarnings("unused")
  private int getNewState() throws OverflowException
  {
    final int limit = getNodeLimit();
    if (mNumStates >= limit) {
      throw new OverflowException(limit);
    }
    return mNumStates++;
  }

  private void addTransition(final int source, final int event,
                             final int target) throws OverflowException
  {
    if (target == mDeadlockState) {
      if (mCurrentDeadlock[event]) {
        return;
      } else {
        mCurrentDeadlock[event] = true;
      }
    }
    if (mTransitionBuffer.size() >= mTransitionBufferLimit) {
      throw new OverflowException(OverflowKind.TRANSITION,
                                  getTransitionLimit());
    }
    mTransitionBuffer.add(source);
    mTransitionBuffer.add(event);
    mTransitionBuffer.add(target);
  }

  private AutomatonProxy createAutomaton()
  {
    final int numEvents = mNumEvents + mCurrentPropositions.size();
    final Collection<EventProxy> events =
      new ArrayList<EventProxy>(numEvents);
    for (final EventProxy event : mEvents) {
      events.add(event);
    }
    events.addAll(mCurrentPropositions);

    final int numProps = mCurrentPropositions.size();
    final List<StateProxy> states = new ArrayList<StateProxy>(mNumStates);
    final StateProxy[] stateArray = new StateProxy[mNumStates];
    for (int code = 0; code < mNumStates; code++) {
      if (mReachableStates.get(code)) {
        final boolean initial = code < mNumInitialStates;
        final int[] tuple = mStateTuples.get(code);
        final List<EventProxy> marking = new ArrayList<EventProxy>(numProps);
        props: for (final EventProxy prop : mCurrentPropositions) {
          for (int a = 0; a < mNumAutomata; a++) {
            final List<EventProxy> stateMarking =
              getStateMarking(a, tuple[a]);
            if (Collections.binarySearch(stateMarking, prop) < 0) {
              continue props;
            }
          }
          marking.add(prop);
        }
        Collections.sort(marking);
        final List<EventProxy> unique = getUniqueMarking(marking);
        final StateProxy state =
          new MemStateProxy(code, tuple, unique, initial);
        states.add(state);
        stateArray[code] = state;
      }
    }

    final ProductDESProxyFactory factory = getFactory();
    final int bufferSize = mTransitionBuffer.size();
    final ArrayList<TransitionProxy> transitions =
      new ArrayList<TransitionProxy>(bufferSize / 3);
    int t = 0;
    while (t < bufferSize) {
      int code = mTransitionBuffer.get(t++);
      if (!mReachableStates.get(code)) {
        t += 2;
        continue;
      }
      final StateProxy source = stateArray[code];
      code = mTransitionBuffer.get(t++);
      final EventProxy event = mEvents[code];
      code = mTransitionBuffer.get(t++);
      if (!mReachableStates.get(code)) {
        continue;
      }
      final StateProxy target = stateArray[code];
      transitions.add(factory.createTransitionProxy(source, event, target));
    }

    final String name = computeOutputName();
    final ComponentKind kind = ComponentKind.SUPERVISOR;
    return factory.createAutomatonProxy(name, kind, events, states,
                                        transitions);
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

  @SuppressWarnings("unused")
  private String showStateTuple(final int[] tuple)
  {
    String msg = "";
    for (int i = 0; i < tuple.length; i++) {
      final AutomatonProxy aut = mAutomata.get(i);
      final ComponentKind kind = getKindTranslator().getComponentKind(aut);
      final StateProxy state = mOriginalStates[i][tuple[i]];
      msg +=
        kind.toString() + " " + aut.getName() + " : state " + state.getName()
          + "\n";
    }
    return msg;
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
    public Collection<AutomatonProxy> getInputAutomata()
    {
      return mInputAutomata;
    }

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
     * Gets the index position of the given automaton in state tuples.
     * Presently linear complexity --- is this good enough?
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
    public Collection<EventProxy> getPropositions()
    {
      return mProps;
    }

    public boolean isInitial()
    {
      return mIsInitial;
    }

    public MemStateProxy clone()
    {
      return new MemStateProxy(mName, mStateTuple, mProps, mIsInitial);
    }

    public int getCode()
    {
      return mName;
    }

    public String getName()
    {
      return "S:" + mName;
    }

    public int[] getStateTuple()
    {
      return mStateTuple;
    }

    public boolean refequals(final NamedProxy o)
    {
      if (o instanceof MemStateProxy) {
        final MemStateProxy s = (MemStateProxy) o;
        return s.mName == mName;
      } else {
        return false;
      }
    }

    public int refHashCode()
    {
      return mName;
    }

    public Object acceptVisitor(final ProxyVisitor visitor)
      throws VisitorException
    {
      final ProductDESProxyVisitor desvisitor =
        (ProductDESProxyVisitor) visitor;
      return desvisitor.visitStateProxy(this);
    }

    public Class<StateProxy> getProxyInterface()
    {
      return StateProxy.class;
    }

    public int compareTo(final NamedProxy n)
    {
      return n.getName().compareTo(getName());
    }

    public String toString()
    {
      return getName();
    }

    //#######################################################################
    //# Data Members
    private final int mName;
    private final int[] mStateTuple;
    private final boolean mIsInitial;
    private final Collection<EventProxy> mProps;
  }


  //#########################################################################
  //# Inner Class AutomatonEventInfo
  private abstract class AutomatonEventInfo implements
    Comparable<AutomatonEventInfo>
  {
    public AutomatonEventInfo(final int aut, final double probability)
    {
      mAut = aut;
      mProbability = probability;
    }

    protected int getAutomaton()
    {
      return mAut;
    }

    protected double getProbability()
    {
      return mProbability;
    }

    private final int mAut;
    private final double mProbability;
  }


  //# Inner Class UncontrollableAutomatonEventInfo
  private class UncontrollableAutomatonEventInfo extends AutomatonEventInfo
  {
    public UncontrollableAutomatonEventInfo(final int aut,
                                            final double probability)
    {
      super(aut, probability);
    }

    public int compareTo(final AutomatonEventInfo info)
    {
      if (this.getAutomaton() < mNumPlants && this.getProbability() == 1.0f) {
        return 1;
      } else if (info.mAut < mNumPlants && info.mProbability == 1.0f) {
        return -1;
      } else if ((this.getAutomaton() < mNumPlants && info.mAut < mNumPlants)
                 || (this.getAutomaton() >= mNumPlants && info.mAut >= mNumPlants)) {
        return (this.getProbability() < info.mProbability) ? -1 : 1;
      } else {
        return (this.getAutomaton() < mNumPlants) ? -1 : 1;
      }
    }
  }


  //# Inner Class ControllableAutomatonEventInfo
  private class ControllableAutomatonEventInfo extends AutomatonEventInfo
  {
    public ControllableAutomatonEventInfo(final int aut,
                                          final double probability)
    {
      super(aut, probability);
    }

    public int compareTo(final AutomatonEventInfo info)
    {
      if (this.getProbability() < info.getProbability()) {
        return -1;
      } else if (this.getProbability() < info.getProbability()) {
        return 1;
      } else {
        return 0;
      }
    }
  }


  //#########################################################################################################
  //#########################################################################################################
  //# Inner Class StateExplorer
  private abstract class StateExplorer
  {
    public StateExplorer(final int[][] theEventAutomata,
                         final int[][][][] theTransitions,
                         final int[][] theNDTuple, final int theFirstEvent,
                         final int theLastEvent)
    {
      /*
       * mEventAutomata = eventAutomata; mTransitions = transitions; mNDTuple
       * = NDTuple;
       */
      mmFirstEvent = theFirstEvent;
      mmLastEvent = theLastEvent;
      mmEventAutomata = theEventAutomata;
      mmTransitions = theTransitions;
      mmNDTuple = theNDTuple;
    }

    public boolean explore(final int[] stateTuple) throws OverflowException
    {
      final int state = mGlobalVisited.get(stateTuple);
      events: for (int e = mmFirstEvent; e <= mmLastEvent; e++) {
        Arrays.fill(mmNDTuple, null);
        for (final int a : mmEventAutomata[e]) {
          if (mmTransitions[a][e] != null) {
            final int[] succ = mmTransitions[a][e][stateTuple[a]];
            if (succ == null) {
              continue events;
            }
            mmNDTuple[a] = succ;
          }
        }
        if (!permutations(mNumAutomata, stateTuple, state, e)) {
          return false;
        }
      }
      return true;
    }

    public boolean permutations(int a, final int[] stateTuple,
                                final int state, final int event)
      throws OverflowException
    {
      if (a == 0) {
        if (!processNewState(state, event, stateTuple == null)) {
          return false;
        }
      } else {
        a--;
        final int[] codes = mmNDTuple[a];
        if (codes == null) {
          mTargetTuple[a] = stateTuple[a];
          if (!permutations(a, stateTuple, state, event)) {
            return false;
          }
        } else {
          for (int i = 0; i < codes.length; i++) {
            mTargetTuple[a] = codes[i];
            if (!permutations(a, stateTuple, state, event)) {
              return false;
            }
          }
        }
      }
      return true;
    }

    public abstract boolean processNewState(final int source,
                                            final int event,
                                            final boolean isInitial)
      throws OverflowException;

    protected final int[][] mmEventAutomata;
    protected final int[][][][] mmTransitions;
    protected final int[][] mmNDTuple;
    protected int mmFirstEvent;
    protected int mmLastEvent;
  }


  //# Inner Class CtrlInitialReachabilityExplorer
  private class CtrlInitialReachabilityExplorer extends StateExplorer
  {
    public CtrlInitialReachabilityExplorer(final int[][] eventAutomata,
                                       final int[][][][] transitions,
                                       final int[][] NDTuple,
                                       final int firstEvent,
                                       final int lastEvent)
    {
      super(eventAutomata, transitions, NDTuple, firstEvent, lastEvent);
    }

    public boolean processNewState(final int source, final int event,
                                   final boolean isInitial)
      throws OverflowException
    {
      if (isNewState(mTargetTuple)) {
        final int[] newTuple = Arrays.copyOf(mTargetTuple, mNumAutomata);
        mGlobalStack.push(newTuple);///////////////////////////..............................
      }
      return true;
    }
  }


  //# Inner Class UCInitialReachabilityExplorer
  private class UnctrlInitialReachabilityExplorer extends StateExplorer
  {
    public UnctrlInitialReachabilityExplorer(final int[][] eventAutomata,
                                             final int[][][][] transitions,
                                             final int[][] NDTuple,
                                             final int firstEvent,
                                             final int lastEvent)
    {
      super(eventAutomata, transitions, NDTuple, firstEvent, lastEvent);
    }

    @Override
    public boolean explore(final int[] stateTuple) throws OverflowException
    {
      final int state = mGlobalVisited.get(stateTuple);
      events: for (int e = 0; e < mNumUncontrollableEvents; e++) {
        Arrays.fill(mmNDTuple, null);
        for (final int a : mmEventAutomata[e]) {
          if (mmTransitions[a][e] != null) {
            final int[] succ = mmTransitions[a][e][stateTuple[a]];
            if (succ == null) {
              if (a >= mNumPlants && mNumPlants != 0) {
                return false;
              } else {
                continue events;
              }
            }
            mmNDTuple[a] = succ;
          }
        }
        if (!permutations(mNumAutomata, stateTuple, state, e)) {
          return false;
        }
      }
      return true;
    }

    public boolean processNewState(final int source, final int event,
                                   final boolean isInitial)
      throws OverflowException
    {
      if (mLocalVisited.contains(mTargetTuple)) {
        return true;
      } else if (mGlobalVisited.containsKey(mTargetTuple)) {
        final int s = mGlobalVisited.get(mTargetTuple);
        if (mBadStates.get(s)) {
          return false;
        } else if (mSafeStates.get(s)) {
          return true;
        }
      }
      final int[] newTuple = Arrays.copyOf(mTargetTuple, mNumAutomata);///////////////.......................
      mLocalVisited.add(newTuple);
      mLocalStack.push(newTuple);
      return true;
    }
  }


  //# Inner Class FinalReachabilityExplorer
  private class FinalReachabilityExplorer extends StateExplorer
  {
    public FinalReachabilityExplorer(final int[][] eventAutomata,
                                     final int[][][][] transitions,
                                     final int[][] NDTuple,
                                     final int firstEvent, final int lastEvent)
    {
      super(eventAutomata, transitions, NDTuple, firstEvent, lastEvent);
    }

    public boolean processNewState(final int source, final int event,
                                   final boolean isInitial)
      throws OverflowException
    {
      if (mGlobalVisited.containsKey(mTargetTuple)) {
        final int s = mGlobalVisited.get(mTargetTuple);
        if (!mBadStates.get(s) && !mReachableStates.get(s)) {
          final int[] newTuple = Arrays.copyOf(mTargetTuple, mNumAutomata);////////////////......................
          mUnvisited.offer(newTuple);
          mReachableStates.set(s);
        }
        if (!isInitial) {
          addTransition(source, event, s);
        }
      }
      return true;
    }
  }


  //# Inner Class CoreachabilityExplorer
  private class CoreachabilityExplorer extends StateExplorer
  {
    public CoreachabilityExplorer(final int[][] eventAutomata,
                                  final int[][][][] transitions,
                                  final int[][] NDTuple,
                                  final int firstEvent, final int lastEvent)
    {
      super(eventAutomata, transitions, NDTuple, firstEvent, lastEvent);
    }

    public boolean processNewState(final int source, final int event,
                                   final boolean isInitial)
    {
      if (mGlobalVisited.containsKey(mTargetTuple)) {
        final int s = mGlobalVisited.get(mTargetTuple);
        if (!mBadStates.get(s) && mNonCoreachableStates.get(s)) {
          final int[] newTuple = Arrays.copyOf(mTargetTuple, mNumAutomata);//////////////////..................
          mUnvisited.offer(newTuple);
          mNonCoreachableStates.set(s, false);
          mNumCoreachableStates++;
        }
      }
      return true;
    }
  }


  //# Inner Class SuccessorStatesExplorer
  private class SuccessorStatesExplorer extends StateExplorer
  {
    public SuccessorStatesExplorer(final int[][] eventAutomata,
                                   final int[][][][] transitions,
                                   final int[][] NDTuple,
                                   final int firstEvent, final int lastEvent)
    {
      super(eventAutomata, transitions, NDTuple, firstEvent, lastEvent);
    }

    public boolean processNewState(final int source, final int event,
                                   final boolean isInitial)
    {
      if (mGlobalVisited.containsKey(mTargetTuple)) {
        final int s = mGlobalVisited.get(mTargetTuple);
        if (mBadStates.get(s)) {
          if (source < mNumInitialStates) {
            initialIsBad = true;
          }
          mBadStates.set(source);
          mControllabilityChanged = true;
        }
      }
      return true;
    }
  }

  //#########################################################################
  //# Data Members
  private List<AutomatonProxy> mAutomata;
  private Collection<EventProxy> mUsedPropositions;
  private int mNumAutomata;
  private static int mNumPlants;
  private int mNumProperEvents;
  private int mNumUncontrollableEvents;
  private int mNumEvents;
  private EventProxy[] mEvents;
  private Collection<EventProxy> mCurrentPropositions;
  private StateProxy[][] mOriginalStates;
  private Map<List<EventProxy>,List<EventProxy>> mAllMarkings;
  private List<?>[][] mStateMarkings;
  private boolean[][] mDeadlock;

  private int mNumStates;
  private int mNumInitialStates;
  private List<int[]> mStateTuples;
  private Queue<int[]> mUnvisited;
  private TIntArrayList mTransitionBuffer;
  private int mTransitionBufferLimit;
  private BitSet mNonCoreachableStates;
  private BitSet mBadStates;
  private BitSet mSafeStates;
  private BitSet mReachableStates;
  private boolean mCoreachabilityChanged;
  private boolean mControllabilityChanged;
  private boolean initialIsBad;
  private int mNumCoreachableStates;
  private int mLastNumCoreachableStates;

  private int[] mTargetTuple;
  private boolean[] mCurrentDeadlock;
  private int mDeadlockState;

  private int[][][][] mTransitions;////
  private int[][][][] mReverseTransitions;////
  private int[][] mEventAutomata;////
  private int[][] mReverseEventAutomata;////
  private int[][] mNDTuple;////
  private int[][] mNDTupleFinal;////
  private int[][] mNDTupleRvs;////

  private StateExplorer mCtrlInitialReachabilityExplorer;
  private StateExplorer mUnctrlInitialReachabilityExplorer;
  private StateExplorer mFinalReachabilityExplorer;
  private StateExplorer mCoreachabilityExplorer;
  private StateExplorer mSuccessorStatesExplorer;

  private Deque<int[]> mGlobalStack;
  private TObjectIntHashMap<int[]> mGlobalVisited;
  private Deque<int[]> mLocalStack;
  private Deque<int[]> mBackTrace;
  private Set<int[]> mLocalVisited;

  //#########################################################################
  //# Class Constants
  private static final int MAX_TABLE_SIZE = 500000;

}
