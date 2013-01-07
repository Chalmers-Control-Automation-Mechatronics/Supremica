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
import gnu.trove.TIntHashSet;
import gnu.trove.TLongHashSet;
import gnu.trove.TLongIterator;
import gnu.trove.TObjectHashingStrategy;
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
import net.sourceforge.waters.analysis.tr.IntListBuffer;
import net.sourceforge.waters.analysis.tr.IntListBuffer.ReadOnlyIterator;
import net.sourceforge.waters.model.analysis.AbstractConflictChecker;
import net.sourceforge.waters.model.analysis.AbstractProductDESBuilder;
import net.sourceforge.waters.model.analysis.AnalysisException;
import net.sourceforge.waters.model.analysis.EventNotFoundException;
import net.sourceforge.waters.model.analysis.KindTranslator;
import net.sourceforge.waters.model.analysis.OverflowException;
import net.sourceforge.waters.model.analysis.OverflowKind;
import net.sourceforge.waters.model.analysis.ProxyResult;
import net.sourceforge.waters.model.analysis.SupervisorSynthesizer;
import net.sourceforge.waters.model.base.NamedProxy;
import net.sourceforge.waters.model.base.ProxyVisitor;
import net.sourceforge.waters.model.base.VisitorException;
import net.sourceforge.waters.model.des.AutomatonProxy;
import net.sourceforge.waters.model.des.AutomatonTools;
import net.sourceforge.waters.model.des.EventProxy;
import net.sourceforge.waters.model.des.ProductDESEqualityVisitor;
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
 * @author Robi Malik, fq11
 */

public class MonolithicSynthesizer extends AbstractProductDESBuilder
  implements SupervisorSynthesizer
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
  public void setSupervisorReductionEnabled(final boolean enable)
  {
    mSupervisorReductionEnabled = enable;
  }

  public boolean getSupervisorReductionEnabled()
  {
    return mSupervisorReductionEnabled;
  }

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

  public void setConfiguredDefaultMarking(EventProxy marking)
    throws EventNotFoundException
  {
    if (marking == null) {
      final ProductDESProxy model = getModel();
      marking = AbstractConflictChecker.getMarkingProposition(model);
    }
    final Collection<EventProxy> props = Collections.singletonList(marking);
    setPropositions(props);
  }

  //#########################################################################
  //# Interface net.sourceforge.waters.model.analysis.ModelAnalyser
  public boolean run() throws AnalysisException
  {
    try {
      setUp();
      listX1 = 0;
      listY1 = 0;
      total = 0;
      // initial search
      while (!mGlobalStack.isEmpty()) {
        final int[] sentinel = new int[1];
        final int[] encodedRoot = mGlobalStack.pop();
        final int r = mGlobalVisited.get(encodedRoot);
        if (mSafeStates.get(r) || mBadStates.get(r)) {
          continue;
        }
        mLocalStack.push(encodedRoot);
        mLocalVisited.add(encodedRoot);
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
            final int n = addEncodedNewState(current);
            mSafeStates.set(n);
          }
          for (final int[] current : mLocalVisited) {
            mCtrlInitialReachabilityExplorer.explore(current);
          }
        } else {
          for (final int[] current : mBackTrace) {
            if (current == sentinel) {
              continue;
            } else {
              int n = 0;
              if (mGlobalVisited.containsKey(current)) {
                n = mGlobalVisited.get(current);
                if (n < mNumInitialStates) {
                  return setBooleanResult(false);
                }
              } else {
                n = addEncodedNewState(current);
              }
              mBadStates.set(n);
            }
          }
        }
        mLocalStack.clear();
        mLocalVisited.clear();
        mBackTrace.clear();
      }

      mMustContinue = false;
      do {
        // mark non-coreachable states (trim)
        mNonCoreachableStates.set(0, mNumStates);
        tuples: for (int t = 0; t < mStateTuples.size(); t++) {
          if (!mBadStates.get(t)) {
            final int[] tuple = new int[mNumAutomata];
            final int[] encodedTuple = mStateTuples.get(t);
            decode(encodedTuple, tuple);
            for (int aut = 0; aut < tuple.length; aut++) {
              if (mStateMarkings[aut][tuple[aut]].isEmpty()) {
                continue tuples;
              }
            }
            mUnvisited.add(encodedTuple);
            mNonCoreachableStates.set(t, false);
            while (!mUnvisited.isEmpty()) {
              final int[] s = mUnvisited.remove();
              mCoreachabilityExplorer.explore(s);
            }
          }
        }

        for (int b = 0; b < mNumInitialStates; b++) {
          if (!mBadStates.get(b) && mNonCoreachableStates.get(b)) {
            initialIsBad = true;
          }
        }
        // mark uncontrollable states
        mMustContinue = false;
        mUnvisited.clear();
        for (int state = 0; state < mStateTuples.size(); state++) {
          if (!mBadStates.get(state) && mNonCoreachableStates.get(state)) {
            mBadStates.set(state);
            mUnvisited.offer(mStateTuples.get(state));
            while (!mUnvisited.isEmpty()) {
              final int[] s = mUnvisited.remove();
              mSuccessorStatesExplorer.explore(s);
            }
          }
        }
      } while (mMustContinue);

      //final search
      mUnvisited.clear();
      mFinalReachabilityExplorer.permutations(mNumAutomata, null, -1);
      while (!mUnvisited.isEmpty()) {
        final int[] s = mUnvisited.remove();
        if (mGlobalVisited.containsKey(s)) {
          mFinalReachabilityExplorer.explore(s);
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
        final AutomatonProxy aut;
        if (mSupervisorReductionEnabled) {
          mReduction.mainProcedure();
          aut = mReduction.createReducedAutomaton();
        } else {
          aut = createAutomaton();
        }
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
    int[][][][] mTransitions;
    int[][][][] mReverseTransitions;
    int[][] mEventAutomata;
    int[][] mReverseEventAutomata;
    int[][] mNDTuple;
    int[][] mNDTupleFinal;

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
    mDeadlock = new boolean[mNumAutomata][];
    mTargetTuple = new int[mNumAutomata];
    mDeadlockState = -1;

    //transitions indexed first by automaton then by event then by source state
    mTransitions = new int[mNumAutomata][mNumProperEvents][][];
    mReverseTransitions = new int[mNumAutomata][mNumProperEvents][][];
    mEventAutomata = new int[mNumProperEvents][];
    mReverseEventAutomata = new int[mNumProperEvents][];
    mNDTuple = new int[mNumAutomata][];

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
    mUnvisited = new ArrayDeque<int[]>(100);
    mNumStates = 0;

    // get encoding information
    mNumBits = new int[mNumAutomata];
    mNumBitsMasks = new int[mNumAutomata];

    // get mNumBits
    mNumInts = 1;
    int totalBits = SIZE_INT;
    int counter = 0;
    for (int aut = 0; aut < mNumAutomata; aut++) {
      final int bits = AutomatonTools.log2(mOriginalStates[aut].length);
      mNumBits[counter] = bits;
      mNumBitsMasks[counter] = (1 << bits) - 1;
      if (totalBits >= bits) { // if current buffer can store this automaton
        totalBits -= bits;
      } else {
        mNumInts++;
        totalBits = SIZE_INT - bits;
      }
      counter++;
    }

    // get index
    counter = 0;
    totalBits = SIZE_INT;
    mIndexAutomata = new int[mNumInts + 1];
    mIndexAutomata[0] = counter++;
    for (int i = 0; i < mNumAutomata; i++) {
      if (totalBits >= mNumBits[i]) {
        totalBits -= mNumBits[i];
      } else {
        mIndexAutomata[counter++] = i;
        totalBits = SIZE_INT - mNumBits[i];
      }
    }
    mIndexAutomata[mNumInts] = mNumAutomata;

    mCtrlInitialReachabilityExplorer =
      new CtrlInitialReachabilityExplorer(mEventAutomata, mTransitions,
                                          mNDTuple, mNumUncontrollableEvents,
                                          mNumProperEvents - 1);

    mCtrlInitialReachabilityExplorer.permutations(mNumAutomata, null, -1);
    mNDTupleFinal = Arrays.copyOf(mNDTuple, mNumAutomata);
    mNumInitialStates = mNumStates;

    mUnctrlInitialReachabilityExplorer =
      new UnctrlInitialReachabilityExplorer(mEventAutomata, mTransitions,
                                            mNDTuple, 0,
                                            mNumUncontrollableEvents);
    mCoreachabilityExplorer =
      new CoreachabilityExplorer(mReverseEventAutomata, mReverseTransitions,
                                 mNDTuple, 0, mNumProperEvents - 1);
    mSuccessorStatesExplorer =
      new SuccessorStatesExplorer(mReverseEventAutomata, mReverseTransitions,
                                  mNDTuple, 0, mNumUncontrollableEvents - 1);
    mFinalReachabilityExplorer =
      new FinalReachabilityExplorer(mEventAutomata, mTransitions,
                                    mNDTupleFinal, 0, mNumProperEvents - 1);
    mReduction = new Reduction(mTransitions);
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
    mDeadlock = null;
    mGlobalVisited = null;
    mStateTuples = null;
    mUnvisited = null;
    mTransitionBuffer = null;
    mTargetTuple = null;
    mCurrentDeadlock = null;
  }

  //#########################################################################
  //# Auxiliary Methods

  /**
   * It will take a single state tuple as a parameter and encode it.
   *
   * @param stateCodes
   *          state tuple that will be encoded
   * @return encoded state tuple
   */
  private int[] encode(final int[] stateCodes)
  {
    final int encoded[] = new int[mNumInts];
    int i, j;
    for (i = 0; i < mNumInts; i++) {
      for (j = mIndexAutomata[i]; j < mIndexAutomata[i + 1]; j++) {
        encoded[i] <<= mNumBits[j];
        encoded[i] |= stateCodes[j];
      }
    }
    return encoded;
  }

  /**
   * It will take an encoded state tuple as a parameter and decode it. Decoded
   * result will be contained in the second parameter
   *
   * @param encodedStateCodes
   *          state tuple that will be decoded
   * @param currTuple
   *          the decoded state tuple will be stored here
   */
  private void decode(final int[] encodedStateCodes, final int[] currTuple)
  {
    int tmp, mask, i, j;
    for (i = 0; i < mNumInts; i++) {
      tmp = encodedStateCodes[i];
      for (j = mIndexAutomata[i + 1] - 1; j >= mIndexAutomata[i]; j--) {
        mask = mNumBitsMasks[j];
        currTuple[j] = tmp & mask;
        tmp = tmp >>> mNumBits[j];
      }
    }
  }

  private int addDecodedNewState(final int[] decodedTuple)
    throws OverflowException
  {
    final int[] encoded = encode(decodedTuple);
    return addEncodedNewState(encoded);
  }

  private int addEncodedNewState(final int[] encodedTuple)
    throws OverflowException
  {
    if (mGlobalVisited.containsKey(encodedTuple)) {
      return mGlobalVisited.get(encodedTuple);
    } else {
      final int code = mNumStates++;
      final int limit = getNodeLimit();
      if (mNumStates >= limit) {
        throw new OverflowException(limit);
      }
      mGlobalVisited.put(encodedTuple, code);
      mStateTuples.add(encodedTuple);
      return code;
    }
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
        final int[] tuple = new int[mNumAutomata];
        decode(mStateTuples.get(code), tuple);
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
        final StateProxy state = new MemStateProxy(code, unique, initial);
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
        kind.toString() + " " + aut.getName() + " : [" + tuple[i] + "] "
          + state.getName() + "\n";
    }
    return msg;
  }

  @SuppressWarnings("unused")
  private int[] showTuple(final int[] encodedTuple)
  {
    final int[] tuple = new int[mNumAutomata];
    decode(encodedTuple, tuple);
    return tuple;
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
    private MemStateProxy(final int name, final Collection<EventProxy> props,
                          final boolean isInitial)
    {
      mName = name;
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
      return new MemStateProxy(mName, mProps, mIsInitial);
    }

    @SuppressWarnings("unused")
    public int getCode()
    {
      return mName;
    }

    public String getName()
    {
      return "S:" + mName;
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


  //#########################################################################
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


  //#########################################################################
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


  //#########################################################################
  //# Inner Class StateExplorer
  private abstract class StateExplorer
  {
    public StateExplorer(final int[][] theEventAutomata,
                         final int[][][][] theTransitions,
                         final int[][] theNDTuple, final int theFirstEvent,
                         final int theLastEvent)
    {
      mmFirstEvent = theFirstEvent;
      mmLastEvent = theLastEvent;
      mmEventAutomata = theEventAutomata;
      mmTransitions = theTransitions;
      mmNDTuple = theNDTuple;
      mmDecodedTuple = new int[mNumAutomata];
    }

    public boolean explore(final int[] encodedTuple) throws OverflowException
    {
      events: for (int e = mmFirstEvent; e <= mmLastEvent; e++) {
        Arrays.fill(mmNDTuple, null);
        for (final int a : mmEventAutomata[e]) {
          if (mmTransitions[a][e] != null) {
            decode(encodedTuple, mmDecodedTuple);
            final int[] succ = mmTransitions[a][e][mmDecodedTuple[a]];
            if (succ == null) {
              continue events;
            }
            mmNDTuple[a] = succ;
          }
        }
        if (!permutations(mNumAutomata, mmDecodedTuple, e)) {
          return false;
        }
      }
      return true;
    }

    public boolean permutations(int a, final int[] decodedSource,
                                final int event) throws OverflowException
    {
      if (a == 0) {
        if (!processNewState(decodedSource, event, decodedSource == null)) {
          return false;
        }
      } else {
        a--;
        final int[] codes = mmNDTuple[a];
        if (codes == null) {
          mTargetTuple[a] = decodedSource[a];
          if (!permutations(a, decodedSource, event)) {
            return false;
          }
        } else {
          for (int i = 0; i < codes.length; i++) {
            mTargetTuple[a] = codes[i];
            if (!permutations(a, decodedSource, event)) {
              return false;
            }
          }
        }
      }
      return true;
    }

    public abstract boolean processNewState(final int[] decodedSource,
                                            final int event,
                                            final boolean isInitial)
      throws OverflowException;

    protected final int[][] mmEventAutomata;
    protected final int[][][][] mmTransitions;
    protected final int[][] mmNDTuple;
    protected int mmFirstEvent;
    protected int mmLastEvent;
    protected int[] mmDecodedTuple;
  }


  //#########################################################################
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

    public boolean processNewState(final int[] decodedSource,
                                   final int event, final boolean isInitial)
      throws OverflowException
    {
      final int currentNumStates = mNumStates;
      final int t = addDecodedNewState(mTargetTuple);
      if (currentNumStates != mNumStates) {
        mGlobalStack.push(mStateTuples.get(t));
      }
      return true;
    }
  }


  //# Inner Class UnctrlInitialReachabilityExplorer
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
    public boolean explore(final int[] encodedTuple) throws OverflowException
    {
      events: for (int e = 0; e < mNumUncontrollableEvents; e++) {
        Arrays.fill(mmNDTuple, null);
        for (final int a : mmEventAutomata[e]) {
          if (mmTransitions[a][e] != null) {
            decode(encodedTuple, mmDecodedTuple);
            final int[] succ = mmTransitions[a][e][mmDecodedTuple[a]];
            if (succ == null) {
              if (a >= mNumPlants) {
                return false;
              } else {
                continue events;
              }
            }
            mmNDTuple[a] = succ;
          }
        }
        if (!permutations(mNumAutomata, mmDecodedTuple, e)) {
          return false;
        }
      }
      return true;
    }

    public boolean processNewState(final int[] decodedSource,
                                   final int event, final boolean isInitial)
      throws OverflowException
    {
      final int[] encoded = encode(mTargetTuple);
      if (mLocalVisited.contains(encoded)) {
        return true;
      } else if (mGlobalVisited.containsKey(encoded)) {
        final int s = mGlobalVisited.get(encoded);
        if (mBadStates.get(s)) {
          return false;
        } else if (mSafeStates.get(s)) {
          return true;
        }
      }
      mLocalVisited.add(encoded);
      mLocalStack.push(encoded);
      return true;
    }
  }


  //#########################################################################
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

    public boolean processNewState(final int[] decodedSource,
                                   final int event, final boolean isInitial)
      throws OverflowException
    {
      int source = 0;
      if (decodedSource != null) {
        source = mGlobalVisited.get(encode(decodedSource));
      }
      final int target = mGlobalVisited.get(encode(mTargetTuple));
      //if (mBadStates.get(target)) {
      //return true;
      //} else if (mReachableStates.get(target)) {
      //addTransition(source, event, target);
      //} else {
      //addTransition(source, event, target);
      //mUnvisited.offer(mStateTuples.get(target));
      //mReachableStates.set(target);
      //}
      if (!mBadStates.get(target) && !mReachableStates.get(target)) {
        mUnvisited.offer(mStateTuples.get(target));
        mReachableStates.set(target);
      }
      if (!isInitial) {
        addTransition(source, event, target);
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

    public boolean processNewState(final int[] decodedSource,
                                   final int event, final boolean isInitial)
    {
      final int[] encoded = encode(mTargetTuple);
      if (mGlobalVisited.containsKey(encoded)) {
        final int s = mGlobalVisited.get(encoded);
        if (!mBadStates.get(s) && mNonCoreachableStates.get(s)) {
          mUnvisited.offer(mStateTuples.get(s));
          mNonCoreachableStates.set(s, false);
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

    public boolean processNewState(final int[] decodedSource,
                                   final int event, final boolean isInitial)
    {
      final int[] encoded = encode(mTargetTuple);
      if (mGlobalVisited.containsKey(encoded)) {
        final int s = mGlobalVisited.get(encoded);
        if (!mBadStates.get(s)) {
          mUnvisited.offer(mStateTuples.get(s));
          mBadStates.set(s);
          mMustContinue = true;
        }
      }
      return true;
    }
  }


  //#########################################################################
  private class Reduction
  {
    public Reduction(final int[][][][] transitions)
    {
      this.mmTransitions = transitions;
    }

    public void setUpClasses()
    {
      mStateToClass = new int[mNumStates];
      mClasses = new IntListBuffer();
      for (int s = 0; s < mNumStates; s++) {
        if (mReachableStates.get(s)) {
          final int list = mClasses.createList();
          mClasses.add(list, s);
          mStateToClass[s] = list;
        } else {
          mStateToClass[s] = IntListBuffer.NULL;
        }
      }
    }

    public void mainProcedure()
    {
      setUpClasses();
      for (int i = 0; i < mNumStates - 1; i++) {
        if ((mStateToClass[i] == IntListBuffer.NULL) || (i > getMinimum(i))) {
          continue;
        }
        for (int j = i + 1; j < mNumStates; j++) {
          if ((mStateToClass[j] == IntListBuffer.NULL) || (j > getMinimum(j))) {
            continue;
          }
          TLongHashSet mergedPairs = new TLongHashSet();
          mShadowClasses = new IntListBuffer();
          mShadowStateToClass = new int[mNumStates];
          for (int s = 0; s < mNumStates; s++) {
            mShadowStateToClass[s] = IntListBuffer.NULL;
          }

          //System.err.print("(" + i + "," + j + ") ... ");
          if (checkMergibility(i, j, i, j, mergedPairs)) {
            //System.err.println("true");
            merge(mergedPairs);
          } else {
            //System.err.println("false");
          }

          mergedPairs = null;
          mShadowClasses = null;
          mShadowStateToClass = null;
        }
      }
    }

    public boolean checkMergibility(final int x, final int y, final int x0,
                                    final int y0,
                                    final TLongHashSet mergedPairs)
    {
      return checkMergibilityNew(x, y, x0, y0, mergedPairs);
      //return checkMergibilityOld(x, y, x0, y0, mergedPairs);
    }

    @SuppressWarnings("unused")
    public boolean checkMergibilityNew(final int x, final int y,
                                       final int x0, final int y0,
                                       final TLongHashSet mergedPairs)
    {
      if (mStateToClass[x] == mStateToClass[y]) {
        return true;
      }

      final int minX = getMinimum(x);
      final int minY = getMinimum(y);
      if (minX == minY) {
        return true;
      }
      final long p1 = constructPair(minX, minY);
      final long p2 = constructPair(x0, y0);
      if (compare(p1, p2) < 0) {
        return false;
      }

      copyIfShadowNull(x);
      copyIfShadowNull(y);

      final int lx = mShadowStateToClass[x];
      final int ly = mShadowStateToClass[y];
      final int[] listX = mShadowClasses.toArray(lx);
      final int[] listY = mShadowClasses.toArray(ly);

      //merge shadow-classes [x1]' and [y1]';
      final int l = mergeLists(lx, ly, mShadowClasses);
      updateStateToClass(l, mShadowStateToClass, mShadowClasses);

      final long pair = constructPair(x, y);
      mergedPairs.add(pair);

      event: for (int event = 0; event < mNumProperEvents; event++) {
        final TIntHashSet xSuccSet = new TIntHashSet();
        final TIntHashSet ySuccSet = new TIntHashSet();
        final TIntArrayList xSuccList = new TIntArrayList();
        final TIntArrayList ySuccList = new TIntArrayList();
        for (final int xx : listX) {
          final int xSucc = getSuccessorState(event, xx);
          if (xSucc != -1 && mReachableStates.get(xSucc)) {
            final int xmin = getMinimum(xSucc);
            if (xSuccSet.add(xmin)) {
              xSuccList.add(xmin);
            }
          }
        }
        if(xSuccList.isEmpty()){
          continue event;
        }
        for (final int yy : listY) {
          final int ySucc = getSuccessorState(event, yy);
          if (ySucc != -1 && mReachableStates.get(ySucc)) {
            final int ymin = getMinimum(ySucc);
            if (ySuccSet.add(ymin)) {
              ySuccList.add(ymin);
            }
          }
        }
        for (int i = 0; i < xSuccList.size(); i++) {
          for (int j = 0; j < ySuccList.size(); j++) {
            if (!checkMergibilityNew(xSuccList.get(i), ySuccList.get(j), x0, y0, mergedPairs)) {
              return false;
            }
          }
        }
      }
      for (final int xx : listX) {
        for (final int yy : listY) {
          if (!isInRelation(xx, yy)) {
            return false;
          }
        }
      }
      return true;
    }

    @SuppressWarnings("unused")
    public boolean checkMergibilityOld(final int x, final int y,
                                       final int x0, final int y0,
                                       final TLongHashSet mergedPairs)
    {
      if (mStateToClass[x] == mStateToClass[y]) {
        return true;
      }

      final int minX = getMinimum(x);
      final int minY = getMinimum(y);
      if (minX == minY) {
        return true;
      }
      final long p1 = constructPair(minX, minY);
      final long p2 = constructPair(x0, y0);
      if (compare(p1, p2) < 0) {
        return false;
      }

      copyIfShadowNull(x);
      copyIfShadowNull(y);

      final int lx = mShadowStateToClass[x];
      final int ly = mShadowStateToClass[y];
      final int[] listX = mShadowClasses.toArray(lx);
      final int[] listY = mShadowClasses.toArray(ly);

      //merge shadow-classes [x1]' and [y1]';
      final int l = mergeLists(lx, ly, mShadowClasses);
      updateStateToClass(l, mShadowStateToClass, mShadowClasses);

      final long pair = constructPair(x, y);
      mergedPairs.add(pair);

      for (final int xx : listX) {
        for (final int yy : listY) {
          if (!isInRelation(xx, yy)) {
            return false;
          }
          for (int event = 0; event < mNumProperEvents; event++) {
            final int xSucc = getSuccessorState(event, xx);
            final int ySucc = getSuccessorState(event, yy);
            if ((xSucc != -1) && (ySucc != -1)
                && (mReachableStates.get(xSucc))
                && (mReachableStates.get(ySucc))) {
              if (!checkMergibilityOld(xSucc, ySucc, x0, y0, mergedPairs)) {
                return false;
              }
            }
          }
        }
      }
      return true;
    }

    public boolean isInRelation(final int state1, final int state2)
    {
      for (int e = mNumUncontrollableEvents; e < mNumProperEvents; e++) {
        final int state1Succ = getSuccessorState(e, state1);
        final int state2Succ = getSuccessorState(e, state2);
        if (state1Succ != -1 && state2Succ != -1) {
          final boolean s1SuccBad = !mReachableStates.get(state1Succ);
          final boolean s2SuccBad = !mReachableStates.get(state2Succ);
          if ((s1SuccBad != s2SuccBad)) {
            return false;
          }
        }
      }
      return true;
    }

    public int getSuccessorState(final int event, final int source)
    {
      final int[] sourceTuple = new int[mNumAutomata];
      decode(mStateTuples.get(source), sourceTuple);
      for (int aut = 0; aut < mNumAutomata; aut++) {
        if (mmTransitions[aut][event] == null) {
          mTargetTuple[aut] = sourceTuple[aut];
        } else {
          if (mmTransitions[aut][event][sourceTuple[aut]] == null) {
            return -1;
          } else {
            mTargetTuple[aut] =
              mmTransitions[aut][event][sourceTuple[aut]][0];
          }
        }
      }
      final int[] encoded = encode(mTargetTuple);
      if (mGlobalVisited.contains(encoded)) {
        return mGlobalVisited.get(encoded);
      } else {
        return -1;
      }
    }

    public void merge(final TLongHashSet mergedPairs)
    {
      final TLongIterator itr = mergedPairs.iterator();
      while (itr.hasNext()) {
        final long pair = itr.next();
        final int hi = getState(0, pair);
        final int lo = getState(1, pair);
        if (mStateToClass[hi] != mStateToClass[lo]) {
          final int list1 = mStateToClass[hi];
          final int list2 = mStateToClass[lo];
          final int list3 = mergeLists(list1, list2, mClasses);
          updateStateToClass(list3, mStateToClass, mClasses);
        }
      }
    }

    public int mergeLists(final int list1, final int list2,
                          final IntListBuffer classes)
    {
      final int x = classes.getFirst(list1);
      final int y = classes.getFirst(list2);
      if (x < y) {
        return classes.catenateDestructively(list1, list2);
      } else if (x > y) {
        return classes.catenateDestructively(list2, list1);
      }
      return list1;
    }

    public void copyIfShadowNull(final int state)
    {
      if (mShadowStateToClass[state] == IntListBuffer.NULL) {
        final int newlist =
          mShadowClasses.copy(mStateToClass[state], mClasses);
        final ReadOnlyIterator iter =
          mShadowClasses.createReadOnlyIterator(newlist);
        iter.reset(newlist);
        while (iter.advance()) {
          final int current = iter.getCurrentData();
          mShadowStateToClass[current] = newlist;
        }
      }
    }

    public void updateStateToClass(final int list, final int[] stateToClass,
                                   final IntListBuffer classes)
    {
      final ReadOnlyIterator iter = classes.createReadOnlyIterator(list);
      iter.reset(list);
      while (iter.advance()) {
        final int current = iter.getCurrentData();
        stateToClass[current] = list;
      }
    }

    public int compare(final long pair1, final long pair2)
    {
      if (pair1 < pair2) {
        return -1;
      } else if (pair1 > pair2) {
        return 1;
      } else {
        return 0;
      }
    }

    public int getMinimum(final int state)
    {
      if (mShadowStateToClass == null || mShadowStateToClass[state] == IntListBuffer.NULL) {
        return mClasses.getFirst(mStateToClass[state]);
      } else {
        return mShadowClasses.getFirst(mShadowStateToClass[state]);
      }
    }

    public int getState(final int position, final long pair)
    {
      if (position == 0) {
        return (int) (pair >>> 32);
      } else if (position == 1) {
        return (int) (pair & 0xffffffff);
      }
      return -1;
    }

    public long constructPair(int state1, int state2)
    {
      if (state1 > state2) {
        state1 = state1 + state2;
        state2 = state1 - state2;
        state1 = state1 - state2;
      }
      final long pair = (long) state2 | ((long) state1 << 32);
      return pair;
    }

    @SuppressWarnings("unused")
    public int[] showClassList(final IntListBuffer classes, final int list)
    {
      final int[] array = classes.toArray(list);
      return array;
    }

    private AutomatonProxy createReducedAutomaton()
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
      final int[] tuple = new int[mNumAutomata];
      int mini = 0;
      boolean initial = false;

      for (int i = 0; i < mNumStates; i++) {
        if (mStateToClass[i] != IntListBuffer.NULL) {
          final ReadOnlyIterator iter =
            mClasses.createReadOnlyIterator(mStateToClass[i]);
          iter.reset(mStateToClass[i]);
          if (iter.advance()) {
            final int firstElement = iter.getCurrentData();
            if (firstElement == i) {
              mini = firstElement;
              initial = firstElement < mNumInitialStates;
              final List<EventProxy> marking =
                new ArrayList<EventProxy>(numProps);
              do {
                final int curr = iter.getCurrentData();
                decode(mStateTuples.get(curr), tuple);
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
              } while (iter.advance());
              Collections.sort(marking);
              final List<EventProxy> unique = getUniqueMarking(marking);
              final StateProxy state =
                new MemStateProxy(mini, unique, initial);
              states.add(state);
              stateArray[mini] = state;
            }
          }
        }
      }

      final ProductDESProxyFactory factory = getFactory();
      final int bufferSize = mTransitionBuffer.size();
      final ArrayList<TransitionProxy> transitions =
        new ArrayList<TransitionProxy>(bufferSize / 3);

      final TObjectHashingStrategy<TransitionProxy> strategy =
        ProductDESEqualityVisitor.getInstance().getTObjectHashingStrategy();
      final THashSet<TransitionProxy> proxyHashSet =
        new THashSet<TransitionProxy>(strategy);

      int t = 0;
      int min = 0;
      while (t < bufferSize) {
        //source
        int code = mTransitionBuffer.get(t++);
        if (!mReachableStates.get(code)) {
          t += 2;
          continue;
        }
        min = getMinimum(code);
        final StateProxy source = stateArray[min];
        //event
        code = mTransitionBuffer.get(t++);
        final EventProxy event = mEvents[code];
        //target
        code = mTransitionBuffer.get(t++);
        if (!mReachableStates.get(code)) {
          continue;
        }
        min = getMinimum(code);
        final StateProxy target = stateArray[min];

        final TransitionProxy proxy =
          factory.createTransitionProxy(source, event, target);
        final boolean isNewTransition = proxyHashSet.add(proxy);
        if (isNewTransition) {
          transitions.add(proxy);
        }
      }

      final String name = computeOutputName();
      final ComponentKind kind = ComponentKind.SUPERVISOR;
      return factory.createAutomatonProxy(name, kind, events, states,
                                          transitions);
    }

    int[][][][] mmTransitions;
  }

  //#########################################################################
  //# Data Members
  private boolean mSupervisorReductionEnabled = false;
  //# Variables used for encoding/decoding
  /** a list contains number of bits needed for each automaton */
  private int mNumBits[];

  /** a list contains masks needed for each automaton */
  private int mNumBitsMasks[];

  /** a number of integers used to encode synchronized state */
  private int mNumInts;

  /** an index of first automaton in each integer buffer */
  private int mIndexAutomata[];

  private List<AutomatonProxy> mAutomata;
  private EventProxy[] mEvents;
  private Collection<EventProxy> mCurrentPropositions;
  private Collection<EventProxy> mUsedPropositions;
  private int mTransitionBufferLimit;
  private TIntArrayList mTransitionBuffer;

  private int mNumAutomata;
  private static int mNumPlants;
  private int mNumStates;
  private int mNumInitialStates;
  private int mNumEvents;
  private int mNumProperEvents;
  private int mNumUncontrollableEvents;

  private StateProxy[][] mOriginalStates;
  private Map<List<EventProxy>,List<EventProxy>> mAllMarkings;
  private List<?>[][] mStateMarkings;
  private boolean[][] mDeadlock;
  private boolean[] mCurrentDeadlock;
  private int mDeadlockState;

  private List<int[]> mStateTuples;
  private TObjectIntHashMap<int[]> mGlobalVisited;
  private Deque<int[]> mGlobalStack;
  private Deque<int[]> mLocalStack;
  private Deque<int[]> mBackTrace;
  private Set<int[]> mLocalVisited;
  private Queue<int[]> mUnvisited;
  private int[] mTargetTuple;

  private BitSet mNonCoreachableStates;
  private BitSet mBadStates;
  private BitSet mSafeStates;
  private BitSet mReachableStates;
  private boolean initialIsBad;
  private boolean mMustContinue;

  private StateExplorer mCtrlInitialReachabilityExplorer;
  private StateExplorer mUnctrlInitialReachabilityExplorer;
  private StateExplorer mFinalReachabilityExplorer;
  private StateExplorer mCoreachabilityExplorer;
  private StateExplorer mSuccessorStatesExplorer;

  private int[] mStateToClass;
  private IntListBuffer mClasses;
  private int[] mShadowStateToClass;
  private IntListBuffer mShadowClasses;
  private Reduction mReduction;

  //#########################################################################
  //# Class Constants
  private static final int MAX_TABLE_SIZE = 500000;
  private static final int SIZE_INT = 32;

  @SuppressWarnings("unused")
  private int listX1;
  @SuppressWarnings("unused")
  private int listY1;
  @SuppressWarnings("unused")
  private int total;
}
