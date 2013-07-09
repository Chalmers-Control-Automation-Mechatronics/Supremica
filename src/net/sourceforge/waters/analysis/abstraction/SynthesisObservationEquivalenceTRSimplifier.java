//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters Analysis
//# PACKAGE: net.sourceforge.waters.analysis.abstraction
//# CLASS:   SynthesisAbstractionTRSimplifier
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.analysis.abstraction;

import gnu.trove.list.array.TIntArrayList;
import gnu.trove.map.hash.TIntObjectHashMap;
import gnu.trove.set.hash.THashSet;
import gnu.trove.set.hash.TIntHashSet;
import gnu.trove.set.hash.TLongHashSet;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.Set;

import net.sourceforge.waters.analysis.tr.EventEncoding;
import net.sourceforge.waters.analysis.tr.IntListBuffer;
import net.sourceforge.waters.analysis.tr.ListBufferTransitionRelation;
import net.sourceforge.waters.analysis.tr.OneEventCachingTransitionIterator;
import net.sourceforge.waters.analysis.tr.TauClosure;
import net.sourceforge.waters.analysis.tr.TransitionIterator;
import net.sourceforge.waters.model.analysis.AnalysisException;
import net.sourceforge.waters.model.analysis.OverflowException;


/**
 * <P>The synthesis abstraction algorithm.</P>
 *
 * <P>This transition relation simplifier can simplify a given deterministic
 * (or nondeterministic) automaton according to synthesis observation
 * equivalence or weak synthesis observation equivalence. The algorithm
 * is based on the partitioning algorithms for bisimulation by Jean-Claude
 * Fernandez, modified for synthesis observation equivalence.*</P>
 *
 * <P>
 * <I>References.</I><BR>
 * Sahar Mohajerani, Robi Malik, Simon Ware, Martin Fabian.
 * On the Use of Observation Equivalence in Synthesis Abstraction.
 * Proc. 3rd IFAC Workshop on Dependable Control of Discrete Systems,
 * DCDS&nbsp;2011, Saarbr&uuml;cken, Germany, 2011.<BR>
 * Jean-Claude Fernandez. An Implementation of an Efficient Algorithm for
 * Bisimulation Equivalence. Science of Computer Programming,
 * <STRONG>13</STRONG>, 219-236, 1990.
 * </P>
 *
 * @author Sahar Mohajerani, Robi Malik
 */

public class SynthesisObservationEquivalenceTRSimplifier
  extends AbstractMarkingTRSimplifier
{

  //#########################################################################
  //# Constructors
  /**
   * Creates a new bisimulation simplifier without a transition relation.
   */
  public SynthesisObservationEquivalenceTRSimplifier()
  {
  }

  /**
   * Creates a new bisimulation simplifier for the given transition relation.
   */
  public SynthesisObservationEquivalenceTRSimplifier
    (final ListBufferTransitionRelation rel)
  {
    if (rel != null) {
      setTransitionRelation(rel);
    }
  }

  //#########################################################################
  //# Configuration
  /**
   * Sets the transition limit. The transition limit specifies the maximum
   * number of transitions (including stored silent transitions of the
   * transitive closure) that will be stored.
   *
   * @param limit
   *          The new transition limit, or {@link Integer#MAX_VALUE} to allow
   *          an unlimited number of transitions.
   */
  public void setTransitionLimit(final int limit)
  {
    mTransitionLimit = limit;
  }

  /**
   * Gets the transition limit.
   *
   * @see #setTransitionLimit(int) setTransitionLimit()
   */
  public int getTransitionLimit()
  {
    return mTransitionLimit;
  }


  /**
   * Enables or disables weak synthesis observation equivalence.
   * @param weak
   *          <CODE>true</CODE> for weak synthesis observation equivalence
   *          (the default), <CODE>false</CODE> for synthesis observation
   *          equivalence.
   */
  public void setUsesWeakSynthesisObservationEquivalence(final boolean weak)
  {
    mWeak = weak;
  }

  /**
   * Returns whether weak synthesis observation equivalence is used.
   * @see #setUsesWeakSynthesisObservationEquivalence(boolean)
   *      setUsesWeakSynthesisObservationEquivalence()
   */
  public boolean getUsesWeakSynthesisObservationEquivalence()
  {
    return mWeak;
  }


  //#########################################################################
  //# Interface net.sourceforge.waters.analysis.abstraction.
  //# TransitionRelationSimplifier
  @Override
  public int getPreferredInputConfiguration()
  {
    if (mWeak) {
      return ListBufferTransitionRelation.CONFIG_ALL;
    } else {
      return ListBufferTransitionRelation.CONFIG_PREDECESSORS;
    }
  }

  @Override
  public void setTransitionRelation(final ListBufferTransitionRelation rel)
  {
    reset();
    super.setTransitionRelation(rel);
    mNumReachableStates = rel.getNumberOfReachableStates();
  }

  @Override
  public boolean isPartitioning()
  {
    return true;
  }

  @Override
  public TRSimplifierStatistics createStatistics()
  {
    final TRSimplifierStatistics stats =
      new TRSimplifierStatistics(this, true, false);
    return setStatistics(stats);
  }

  @Override
  public void reset()
  {
    super.reset();
    mOriginalTransitionRelation = null;
    mLocalUncontrollablePredecessorsTauClosure = null;
    mPredecessorIterator = null;
    mUncontrollableTauIterator = null;
    mUncontrollableEventIterator = null;
  }


  //#########################################################################
  //# Simple access
  public ListBufferTransitionRelation getOriginalTransitionRelation()
  {
    return mOriginalTransitionRelation;
  }


  //#########################################################################
  //# Overrides for net.sourceforge.waters.analysis.abstraction.AbstractTRSimplifier
  @Override
  protected void setUp() throws AnalysisException
  {
    super.setUp();
    mHasModifications = false;
    setUpTauClosure();
    final ListBufferTransitionRelation rel = getTransitionRelation();
    mListBuffer = new IntListBuffer();
    mClassReadIterator = mListBuffer.createReadOnlyIterator();
    mClassWriteIterator = mListBuffer.createModifyingIterator();
    mNumClasses = 0;
    final int numStates = rel.getNumberOfStates();
    mStateToClass = new EquivalenceClass[numStates];
    mPredecessors = new int[numStates];
    mSplitters = new PriorityQueue<EquivalenceClass>();
    if (mWeak) {
      mUncontrollableTransitionCache = new UncontrollableTransitionCache();
    }
    mTempClass = new TIntArrayList(numStates);
    mOriginalTransitionRelation = null;
  }

  @Override
  protected boolean runSimplifier() throws AnalysisException
  {
    setUpInitialPartitionBasedOnDefaultMarking();
    if (mNumClasses < mNumReachableStates) {
      int prevNumClasses = mNumClasses;
      while (true) {
        for (EquivalenceClass splitter = mSplitters.poll();
             splitter != null && mNumClasses < mNumReachableStates;
             splitter = mSplitters.poll()) {
          checkAbort();
          splitter.splitOn();
        }
        if (prevNumClasses == mNumClasses ||
            mNumClasses == mNumReachableStates) {
          break;
        }
        prevNumClasses = mNumClasses;
        enqueueAlltheClasses();
      }
    }
    if (mOmegaState >= 0) {
      final ListBufferTransitionRelation rel = getTransitionRelation();
      rel.setReachable(mOmegaState, false);
      final byte status = rel.getProperEventStatus(EventEncoding.TAU);
      rel.setProperEventStatus
        (EventEncoding.TAU, status | EventEncoding.STATUS_UNUSED);
      mNumClasses--;
      mNumReachableStates--;
    }
    buildResultPartition();
    applyResultPartitionAutomatically();
    return mHasModifications || mNumClasses < mNumReachableStates;
  }


  @Override
  protected void tearDown()
  {
    super.tearDown();
    mListBuffer = null;
    mClassReadIterator = null;
    mClassWriteIterator = null;
    mStateToClass = null;
    mPredecessors = null;
    mSplitters = null;
    mTempClass = null;
    mUncontrollableTransitionCache = null;
  }

  /**
   * Destructively applies the computed partitioning to the simplifier's
   * transition relation. This method merges any states found to be equivalent
   * during the last call to {@link #run()}, and depending on configuration,
   * performs a second pass to remove redundant transitions.
   */
  @Override
  protected void applyResultPartition() throws AnalysisException
  {
    final ListBufferTransitionRelation rel = getTransitionRelation();
    if (getResultPartition() != null) {
      mLocalUncontrollablePredecessorsTauClosure = null;
      mUncontrollableTauIterator = null;
      mUncontrollableEventIterator = null;
      final ListBufferTransitionRelation copy =
        new ListBufferTransitionRelation
          (rel, ListBufferTransitionRelation.CONFIG_SUCCESSORS);
      super.applyResultPartition();
      rel.removeTauSelfLoops();
      rel.removeProperSelfLoopEvents();
      // TODO Only need to keep original of the first synthesis abstraction,
      // and only when this or some later abstraction is nondeterministic.
      // Can we somehow save memory here?
      mOriginalTransitionRelation = copy;
    } else {
      if (mHasModifications) {
        rel.removeProperSelfLoopEvents();
      }
    }
  }


  //#########################################################################
  //# Initial Partition
  /**
   * Sets up an initial partition based on the default marking. This method is
   * called at the beginning of the {@link #runSimplifier()} method.
   */
  private void setUpInitialPartitionBasedOnDefaultMarking()
  {
    final ListBufferTransitionRelation rel = getTransitionRelation();
    final int numStates = rel.getNumberOfStates();
    final int defaultMarkingID = getDefaultMarkingID();
    final EquivalenceClass reachableClass = new EquivalenceClass();
    if (defaultMarkingID >= 0) {
      mOmegaState = -1;
      for (int state = numStates - 1; state >= 0; state--) {
        if (!rel.isReachable(state)) {
          mOmegaState = state;
          break;
        }
      }
      if (mOmegaState < 0) {
        throw new IllegalArgumentException("Cannot create an omega state");
      }
      final EquivalenceClass omegaClass = new EquivalenceClass();
      omegaClass.addState(mOmegaState);
      for (int state = 0; state < numStates; state++) {
        if (rel.isReachable(state)) {
          if (rel.isMarked(state, defaultMarkingID)) {
            rel.addTransition(state, EventEncoding.TAU, mOmegaState);
          }
          reachableClass.addState(state);
        }
      }
      omegaClass.enqueue();
      omegaClass.setUpStateToClass();
      final byte status = rel.getProperEventStatus(EventEncoding.TAU);
      rel.setProperEventStatus
        (EventEncoding.TAU, status & ~EventEncoding.STATUS_UNUSED);
      rel.setReachable(mOmegaState, true);
      mNumReachableStates++;
    } else {
      mOmegaState = -1;
      for (int state = 0; state < numStates; state++) {
        if (rel.isReachable(state)) {
          reachableClass.addState(state);
        }
      }
    }
    reachableClass.enqueue();
    reachableClass.setUpStateToClass();
    mHasModifications = false;
  }


  //#########################################################################
  //# Algorithm
  private void setUpTauClosure() throws OverflowException
  {
    if (mLocalUncontrollablePredecessorsTauClosure == null) {
      final ListBufferTransitionRelation rel = getTransitionRelation();
      final int limit = getTransitionLimit();
      mLocalUncontrollablePredecessorsTauClosure =
        rel.createPredecessorsTauClosureByStatus
          (limit,
           EventEncoding.STATUS_LOCAL,
           ~EventEncoding.STATUS_CONTROLLABLE);
      mPredecessorIterator = rel.createPredecessorsReadOnlyIterator();
      mUncontrollableTauIterator =
        new OneEventCachingTransitionIterator
          (mLocalUncontrollablePredecessorsTauClosure.createIterator(),
           EventEncoding.TAU);
      mUncontrollableEventIterator =
        mLocalUncontrollablePredecessorsTauClosure.createFullEventClosureIterator(-1);
    }
  }

  private void enqueueAlltheClasses()
  {
    for (final EquivalenceClass eq : mStateToClass) {
      if (eq != null) {
        eq.enqueue();
      }
    }
  }

  private void buildResultPartition()
  {
    if (mNumClasses < mNumReachableStates) {
      final ListBufferTransitionRelation rel = getTransitionRelation();
      final int numStates = rel.getNumberOfStates();
      final List<int[]> partition = new ArrayList<int[]>(mNumClasses);
      for (int state = 0; state < numStates; state++) {
        if (rel.isReachable(state)) {
          final EquivalenceClass sec = mStateToClass[state];
          final int[] clazz = sec.putResult(state);
          if (clazz != null) {
            partition.add(clazz);
          }
        }
      }
      setResultPartitionList(partition);
    } else {
      setResultPartitionList(null);
    }
  }

  /**
   * Returns whether an event with the given code is local in
   * the encoding of this simplifier.
   */
  boolean isLocalEvent(final int event)
  {
    if (event == EventEncoding.TAU) {
      // OMEGA is not local!
      return false;
    } else {
      final ListBufferTransitionRelation rel = getTransitionRelation();
      final byte status = rel.getProperEventStatus(event);
      return EventEncoding.isLocalEvent(status);
    }
  }


  //#########################################################################
  //# Debugging
  @Override
  public String toString()
  {
    final StringWriter writer = new StringWriter();
    final PrintWriter printer = new PrintWriter(writer);
    final Collection<EquivalenceClass> printed =
      new THashSet<EquivalenceClass>(mNumClasses);
    for (int s = 0; s < mStateToClass.length; s++) {
      final EquivalenceClass clazz = mStateToClass[s];
      if (printed.add(clazz)) {
        if (s > 0) {
          printer.println();
        }
        clazz.dump(printer);
      }
    }
    return writer.toString();
  }


  //#########################################################################
  //# Inner Class EquivalenceClass
  private class EquivalenceClass
    implements Comparable<EquivalenceClass>
  {

    //#######################################################################
    //# Constructors
    private EquivalenceClass()
    {
      mSize = 0;
      mList = mListBuffer.createList();
      mOverflowList = IntListBuffer.NULL;
      mOverflowSize = -1;
      mSmallestState = Integer.MAX_VALUE;
      mNumClasses++;
      mIsOpenSplitter = false;
    }

    private EquivalenceClass(final int list, final int size,
                             final boolean preds)
    {
      mSize = size;
      mList = list;
      mOverflowList = IntListBuffer.NULL;
      mOverflowSize = preds ? 0 : -1;
      mNumClasses++;
      mIsOpenSplitter = false;
      setUpSmallestState();
    }

    private EquivalenceClass(final int[] states)
    {
      mSize = states.length;
      mList = mListBuffer.createList(states);
      mOverflowList = IntListBuffer.NULL;
      mOverflowSize = -1;
      mNumClasses++;
      setUpSmallestState();
    }

    /**
     * Creates a dummy class only used as marker by
     * {@link UncontrollableTransitionCache}.
     */
    private EquivalenceClass(final UncontrollableTransitionCache cache)
    {
      mSize = 0;
      mList = mOverflowList = IntListBuffer.NULL;
      mOverflowSize = -1;
      mSmallestState = Integer.MAX_VALUE;
      mIsOpenSplitter = false;
    }

    //#######################################################################
    //# Initialisation
    void setSize(final int size)
    {
      mSize = size;
    }

    void setList(final int list, final int size)
    {
      mList = list;
      mSize = size;
    }

    void addState(final int state)
    {
      mListBuffer.append(mList, state);
      mSize++;
      if(state < mSmallestState){
        mSmallestState = state;
      }
    }

    void setUpStateToClass()
    {
      reset(mClassReadIterator);
      while (mClassReadIterator.advance()) {
        final int state = mClassReadIterator.getCurrentData();
        mStateToClass[state] = this;
      }
    }

    //#######################################################################
    //# Simple Access
    int getSmallestState()
    {
      return mSmallestState;
    }

    int getList()
    {
      return mList;
    }

    void reset(final IntListBuffer.Iterator iter)
    {
      iter.reset(mList);
    }

    boolean resetOverflowList(final IntListBuffer.Iterator iter)
    {
      if (mOverflowList != IntListBuffer.NULL) {
        iter.reset(mOverflowList);
        return true;
      } else {
        return false;
      }
    }

    //#######################################################################
    //# Interface java.util.Comparable<EquivalenceClass>
    @Override
    public int compareTo(final EquivalenceClass splitter)
    {
      return mSize - splitter.getSize();
    }

    //#######################################################################
    //# Splitting
    private int getSize()
    {
      return mSize;
    }

    private void collect(final TIntArrayList states)
    {
      reset(mClassReadIterator);
      while (mClassReadIterator.advance()) {
        final int state = mClassReadIterator.getCurrentData();
        states.add(state);
      }
    }

    /**
     * Splits all the classes connected to this base on the given transition
     * iterator.
     */
    private void splitOn(final TransitionIterator transIter){
      final Collection<EquivalenceClass> splitClasses =
        new THashSet<EquivalenceClass>();
      final int size = mTempClass.size();
      for (int i = 0; i < size; i++) {
        final int state = mTempClass.get(i);
        transIter.resume(state);
        while (transIter.advance()) {
          final int pred = transIter.getCurrentSourceState();
          final EquivalenceClass splitClass = mStateToClass[pred];
          if (splitClass.getSize() > 1) {
            splitClass.moveToOverflowList(pred);
            splitClasses.add(splitClass);
          }
        }
      }
      for (final EquivalenceClass splitClass : splitClasses) {
        splitClass.splitUsingOverflowList();
      }
    }

    private void splitOn()
    {
      final ListBufferTransitionRelation rel = getTransitionRelation();
      mIsOpenSplitter = false;
      collect(mTempClass);

      // Event codes
      final EventEncoding.OrderingInfo info = rel.getOrderingInfo();
      final int lastLocal =
        info.getLastEventIndex(EventEncoding.STATUS_LOCAL);
      final int firstShared =
        info.getFirstEventIndex(~EventEncoding.STATUS_LOCAL);
      final int lastSharedUncont =
        info.getLastEventIndex(~EventEncoding.STATUS_LOCAL,
                               ~EventEncoding.STATUS_CONTROLLABLE);
      final int firstSharedCont =
        info.getFirstEventIndex(~EventEncoding.STATUS_LOCAL,
                                EventEncoding.STATUS_CONTROLLABLE);
      final int lastSharedCont =
        info.getLastEventIndex(~EventEncoding.STATUS_LOCAL,
                               EventEncoding.STATUS_CONTROLLABLE);

      // Uncontrollable shared events
      for (int event = firstShared; event <= lastSharedUncont; event++) {
        if ((rel.getProperEventStatus(event) & EventEncoding.STATUS_UNUSED) == 0) {
          final TransitionIterator transIter = mUncontrollableEventIterator;
          transIter.resetEvent(event);
          splitOn(transIter);
        }
      }

      // Uncontrollable local events
      final TransitionIterator transIter = mUncontrollableTauIterator;
      transIter.reset();
      splitOn(transIter);

      // Controllable shared events
      for (int event = firstSharedCont; event <= lastSharedCont; event++) {
        if ((rel.getProperEventStatus(event) & EventEncoding.STATUS_UNUSED) == 0) {
          splitOnControllable(event);
        }
      }
      // ... including omega
      if ((rel.getProperEventStatus(EventEncoding.TAU) &
           EventEncoding.STATUS_UNUSED) == 0) {
        splitOnControllable(EventEncoding.TAU);
      }
      // Controllable local events
      // TODO What if there are none?
      splitOnControllable(lastLocal);

      mTempClass.clear();
      if (mWeak) {
        mUncontrollableTransitionCache.clearCache();
      }
    }

    private void splitOnControllable(final int event)
    {
      final int size = mTempClass.size();
      final Set <SearchRecord> visited = new THashSet<SearchRecord>();
      final TIntHashSet found = new TIntHashSet();
      final Collection<EquivalenceClass> splitClasses =
        new THashSet<EquivalenceClass>();
      for (int i = 0; i < size; i++) {
        final int state = mTempClass.get(i);
        exploreControllable(state, event, visited, found, splitClasses);
      }
      boolean split = false;
      for (final EquivalenceClass splitClass : splitClasses) {
        split |= splitClass.splitUsingOverflowList();
      }
      if (split && mWeak) {
        // If classes have changed, the cache may become invalid.
        mUncontrollableTransitionCache.clearCache();
      }
    }

    private void exploreControllable
      (final int endState,
       final int event,
       final Set<SearchRecord> visited,
       final TIntHashSet found,
       final Collection<EquivalenceClass> splitClasses)
    {
      final Queue<SearchRecord> open = new ArrayDeque<SearchRecord>();
      final EquivalenceClass endClass = mStateToClass[endState];
      final boolean local = isLocalEvent(event);
      // For SOE, search for local events starts on first part of path,
      // every other search starts on second part of path ...
      final SearchRecord initial =
        new SearchRecord(endState, null, local && !mWeak);
      addSearchRecord(initial, open, visited);
      while (!open.isEmpty()){
        final SearchRecord record = open.remove();
        final int state = record.getState();
        final EquivalenceClass stateClass = mStateToClass[state];
        mPredecessorIterator.resetState(state);
        if (record.getHasEvent()) {
          // In first part of path (before the event)
          final EquivalenceClass startingClass = record.getStartingClass();
          if (startingClass == null || startingClass == stateClass) {
            // Store full path if starting class OK or unassigned ...
            if (found.add(state)) {
              final EquivalenceClass splitClass = mStateToClass[state];
              if (splitClass.getSize() > 1) {
                splitClass.moveToOverflowList(state);
                splitClasses.add(splitClass);
              }
            }
            // Visit predecessors for local controllable transitions if this
            // state's class is or can be made the starting class ...
            final EquivalenceClass controllableStartClass =
              local && stateClass == endClass ? null : stateClass;
            mPredecessorIterator.resetEventsByStatus
              (EventEncoding.STATUS_LOCAL, EventEncoding.STATUS_CONTROLLABLE);
            while (mPredecessorIterator.advance()) {
              final int source = mPredecessorIterator.getCurrentSourceState();
              final SearchRecord next =
                new SearchRecord(source, controllableStartClass, true);
              addSearchRecord(next, open, visited);
            }
          }
          // Visit predecessors for all local uncontrollable transitions ...
          mPredecessorIterator.resetEventsByStatus
            (EventEncoding.STATUS_LOCAL, ~EventEncoding.STATUS_CONTROLLABLE);
          while (mPredecessorIterator.advance()) {
            final int source = mPredecessorIterator.getCurrentSourceState();
            final SearchRecord next =
              new SearchRecord(source, startingClass, true);
            addSearchRecord(next, open, visited);
          }
        } else {
          // In second part of path (after the event)
          if (mWeak) {
            // For weak synthesis observation equivalence,
            // visit predecessors for all local transitions ...
            mPredecessorIterator.resetEventsByStatus(EventEncoding.STATUS_LOCAL);
            while (mPredecessorIterator.advance()) {
              final int source = mPredecessorIterator.getCurrentSourceState();
              final EquivalenceClass sourceClass = mStateToClass[source];
              // If the source state is equivalent to the end state,
              // forget about it. It will be explored on its own.
              if (sourceClass == endClass) {
                continue;
              }
              // If the source state is uncontrollable, also forget about it.
              // The source state is uncontrollable, if it has a shared
              // uncontrollable event outgoing to a state not also reachable
              // from the end class (usucc == BAD_CLASS),
              // or if it has a local uncontrollable successor state
              // not equivalent to the source state, nor to the current state,
              // nor to the end state.
              final EquivalenceClass usucc = mUncontrollableTransitionCache.
                getUniqueSuccessorClass(source, endClass);
              if (usucc == mUncontrollableTransitionCache.BAD_CLASS ||
                  (usucc != mUncontrollableTransitionCache.NO_CLASS &&
                   usucc != stateClass)) {
                continue;
              }
              // Otherwise just explore ...
              final SearchRecord next = new SearchRecord(source, null, false);
              addSearchRecord(next, open, visited);
            }
          }
          if (local) {
            // For local events can switch to first path any time ...
            final SearchRecord next = new SearchRecord(state, null, true);
            addSearchRecord(next, open, visited);
          } else {
            // Or visit predecessors for the shared event ...
            mPredecessorIterator.resetEvent(event);
            while (mPredecessorIterator.advance()){
              final int source = mPredecessorIterator.getCurrentSourceState();
              final SearchRecord next = new SearchRecord(source, null, true);
              addSearchRecord(next, open, visited);
            }
          }
        }
      }
    }

    private boolean addSearchRecord(final SearchRecord record,
                                    final Queue<SearchRecord> queue,
                                    final Set<SearchRecord> set)
    {
      if (record.getStartingClass() != null) {
        final int state = record.getState();
        final boolean hasEvent = record.getHasEvent();
        final SearchRecord alt = new SearchRecord(state, null, hasEvent);
        if (set.contains(alt)) {
          return false;
        }
      }
      if (set.add(record)) {
        queue.add(record);
        return true;
      } else {
        return false;
      }
    }

    private void enqueue()
    {
      if (!mIsOpenSplitter) {
        mIsOpenSplitter = true;
        mSplitters.add(this);
      }
    }

    private void doSimpleSplit(final int overflowList,
                               final int overflowSize,
                               final boolean preds)
    {
      final int size = getSize();
      final int newSize = size - overflowSize;
      final EquivalenceClass overflowClass;
      if (newSize >= overflowSize) {
        overflowClass =
          new EquivalenceClass(overflowList, overflowSize, preds);
        setSize(newSize);
      } else {
        final int list = getList();
        overflowClass = new EquivalenceClass(list, newSize, preds);
        setList(overflowList, overflowSize);
      }
      setUpSmallestState();
      overflowClass.setUpStateToClass();
      overflowClass.enqueue();
      enqueue();
    }

    private void moveToOverflowList(final int state)
    {
      final int tail;
      switch (mOverflowSize) {
      case -1:
        setUpPredecessors();
        // fall through ...
      case 0:
        mOverflowList = mListBuffer.createList();
        mOverflowSize = 1;
        tail = IntListBuffer.NULL;
        break;
      default:
        mOverflowSize++;
        tail = mListBuffer.getTail(mOverflowList);
        break;
      }
      final int pred = mPredecessors[state];
      mPredecessors[state] = tail;
      mClassWriteIterator.reset(mList, pred);
      mClassWriteIterator.advance();
      mClassWriteIterator.moveTo(mOverflowList);
      if (mClassWriteIterator.advance()) {
        final int next = mClassWriteIterator.getCurrentData();
        mPredecessors[next] = pred;
      }
    }

    /**
     * Splits this class if necessary.
     * This method checks the overflow list of this class, and splits
     * off a new class if necessary.
     * @return <CODE>true</CODE> if the class has been split,
     *         <CODE>false</CODE> otherwise.
     */
    private boolean splitUsingOverflowList()
    {
      final boolean split;
      if (mOverflowSize <= 0) {
        return false;
      } else if (mOverflowSize == getSize()) {
        mList = mOverflowList;
        split = false;
      } else {
        doSimpleSplit(mOverflowList, mOverflowSize, mOverflowSize >= 0);
        split = true;
      }
      mOverflowSize = 0;
      mOverflowList = IntListBuffer.NULL;
      return split;
    }

    //#######################################################################
    //# Output
    private int[] putResult(final int state)
    {
      if (mArray == null) {
        final int size = getSize();
        mArray = new int[size];
        mArray[0] = state;
        mOverflowSize = 1;
        return mArray;
      } else {
        mArray[mOverflowSize++] = state;
        return null;
      }
    }

    //#######################################################################
    //# Auxiliary Methods
    private void setUpPredecessors()
    {
      int pred = IntListBuffer.NULL;
      for (int list = mListBuffer.getHead(mList);
           list != IntListBuffer.NULL;
           list = mListBuffer.getNext(list)) {
        final int state = mListBuffer.getData(list);
        mPredecessors[state] = pred;
        pred = list;
      }
    }

    private void setUpSmallestState()
    {
      int smallest = Integer.MAX_VALUE;
      for (int list = mListBuffer.getHead(mList);
           list != IntListBuffer.NULL;
           list = mListBuffer.getNext(list)) {
        final int state = mListBuffer.getData(list);
        if (state < smallest){
          smallest = state;
        }
      }
      mSmallestState = smallest;
    }

    //#######################################################################
    //# Debugging
    @Override
    public String toString()
    {
      final StringWriter writer = new StringWriter();
      final PrintWriter printer = new PrintWriter(writer);
      dump(printer);
      return writer.toString();
    }

    public void dump(final PrintWriter printer)
    {
      mListBuffer.dumpList(printer, mList);
      if (mOverflowList != IntListBuffer.NULL) {
        printer.print('+');
        mListBuffer.dumpList(printer, mOverflowList);
      }
    }

    //#######################################################################
    //# Data Members
    private int mSize;
    private int mList;
    private int mOverflowList;
    private int mOverflowSize;
    private int mSmallestState;
    private int[] mArray;
    private boolean mIsOpenSplitter;
  }


  //#########################################################################
  //# Inner Class SearchRecord
  private class SearchRecord
  {

    //#######################################################################
    //# Constructors
    private SearchRecord(final int state)
    {
      this(state, null, false);
    }

    private SearchRecord(final int state,
                         final EquivalenceClass startingClass,
                         final boolean hasEvent)
    {
      mState = state;
      mStartingClass = startingClass;
      mHasEvent = hasEvent;
    }

    //#######################################################################
    //# simple Access
    private int getState()
    {
      return mState;
    }

    private EquivalenceClass getStartingClass()
    {
      return mStartingClass;
    }

    private boolean getHasEvent()
    {
      return mHasEvent;
    }

    //#######################################################################
    //# Overrides for java.lang.Object
    @Override
    public int hashCode()
    {
      int result = mState * 31;
      if (mStartingClass != null) {
        result = result + mStartingClass.getSmallestState();
      }
      if (mHasEvent) {
        result = result + 0xabababab;
      }
      return result;
    }

    @Override
    public boolean equals(final Object other)
    {
      if (other != null && other.getClass() == getClass()) {
        final SearchRecord record = (SearchRecord) other;
        return
           record.mState == mState &&
           record.mStartingClass == mStartingClass &&
           record.mHasEvent == mHasEvent;
      } else {
        return false;
      }
    }

    @Override
    public String toString()
    {
      return "{" + mState + "," + mStartingClass + "," + mHasEvent + "}";
    }

    //#######################################################################
    //# Data Members
    private final int mState;
    private final EquivalenceClass mStartingClass;
    private final boolean mHasEvent;
  }


  //#########################################################################
  //# Inner Class UncontrollableTransitionCache
  /**
   * <P>Auxiliary class for caching of uncontrollable successors.</P>
   *
   * <P>The uncontrollable transition cache is reset before processing
   * the split on equivalence class with controllable events. For each
   * class being split, it collects two kinds of information.</P>
   *
   * <P>First, it stores the uncontrollable successor classes of the end
   * class obtained from the full-event closure via local uncontrollable
   * transitions. That is, it stores for a given uncontrollable event
   * <I>u</I>, the set of a classes [<I>y</I>] that can be reached by <I>u</I>
   * when it is possibly preceded and/or succeeded by some local
   * uncontrollable events. The information is stored in hash set of
   * event-class pairs (<I>u</I>, [<I>y</I>]), which are computed on demand
   * when the first transition with event <I>u</I> is encountered while
   * searching.</P>
   *
   * <P>Second, the cache stores for each state whether there is exactly one
   * class of states reachable by local uncontrollable transitions, which
   * is different from the class of the state and the class currently being
   * split on. This class is compared with the class of the target state
   * of a transition encountered in the backwards search for the second
   * path. This information is computed on demand when a state is first
   * encountered by the backwards search.</P>
   */
  private class UncontrollableTransitionCache {

    //#######################################################################
    //# Constructor
    private UncontrollableTransitionCache()
    {
      final ListBufferTransitionRelation rel = getTransitionRelation();
      mEventIterator = rel.createSuccessorsReadOnlyIterator();
      final int limit = getTransitionLimit();
      mLocalUncontrollableSuccessorsTauClosure =
        rel.createSuccessorsTauClosureByStatus
          (limit,
           EventEncoding.STATUS_LOCAL,
           ~EventEncoding.STATUS_CONTROLLABLE);
      mTauClosureIterator =
        mLocalUncontrollableSuccessorsTauClosure.createIterator();
      mFullEventClosureIterator =
        mLocalUncontrollableSuccessorsTauClosure.createFullEventClosureIterator();
      mClassReadIterator = mListBuffer.createReadOnlyIterator();
    }

    //#######################################################################
    //# Cache Access
    /**
     * <P>Gets a unique uncontrollable successor class of a given state.</P>
     *
     * <P>This method first checks whether the given source state
     * <CODE>state</CODE> can execute a shared uncontrollable event
     * (possibly preceded by local uncontrollable events) entering a
     * class not reachable from the given <CODE>endClass</CODE> by the same
     * shared uncontrollable event. If so, {@link #BAD_CLASS} is
     * returned.</P>
     *
     * <P>Otherwise it searches the tau-closure of local uncontrollable
     * transitions outgoing from <CODE>state</CODE> for reachable classes
     * not equal to the the class of <CODE>state</CODE> nor to
     * <CODE>endClass</CODE>. If there is no such class,
     * <CODE>{@link #NO_CLASS}</CODE> is returned. If there is exactly one
     * such class, that class is returned. If there is more than one such
     * class, {@link #BAD_CLASS} is returned.</P>
     */
    private EquivalenceClass getUniqueSuccessorClass
      (final int state, final EquivalenceClass endClass)
    {
      initCache();
      EquivalenceClass result = mUniqueSuccessorClassCache.get(state);
      if (result != null) {
        return result;
      }
      final EquivalenceClass stateClass = mStateToClass[state];
      mEventIterator.resetEventsByStatus(~EventEncoding.STATUS_LOCAL,
                                         ~EventEncoding.STATUS_CONTROLLABLE);
      mTauClosureIterator.resetState(state);
      outer:
      while (mTauClosureIterator.advance()) {
        final int succ = mTauClosureIterator.getCurrentTargetState();
        mEventIterator.resume(succ);
        while (mEventIterator.advance()) {
          final int event = mEventIterator.getCurrentEvent();
          final int esucc = mEventIterator.getCurrentTargetState();
          if (!lookupEventSuccessorCache(endClass, event, esucc)) {
            result = BAD_CLASS;
            break outer;
          }
        }
        final EquivalenceClass succClass = mStateToClass[succ];
        if (succClass != endClass && succClass != stateClass) {
          if (result == null) {
            result = succClass;
          } else {
            result = BAD_CLASS;
          }
        }
      }
      if (result == null) {
        result = NO_CLASS;
      }
      mUniqueSuccessorClassCache.put(state, result);
      return result;
    }

    /**
     * Initialises the cache used by the
     * {@link #getUniqueSuccessor(int,EquivalenceClass) getUniqueSuccessor()}
     * method if not yet initialised. This method is called automatically.
     */
    private void initCache()
    {
      if (mUniqueSuccessorClassCache == null) {
        mUniqueSuccessorClassCache = new TIntObjectHashMap<EquivalenceClass>();
        mEventSuccessorCache = new TLongHashSet();
      }
    }

    /**
     * Initialises the cache of uncontrollable successors for the given
     * class and event, if not yet initialised. This method is called
     * automatically.
     * @param endClass
     *          The class currently being split on.
     * @param event
     *          The shared uncontrollable event being checked.
     */
    private void initEventCache(final EquivalenceClass endClass,
                                final int event)
    {
      // Have we done this already for this class and event?
      final long eshift = ((long) event) << 32;
      final long ecode = 0xffffffffL | eshift;
      if (mEventSuccessorCache.add(ecode)) {
        mFullEventClosureIterator.resetEvent(event);
        for (int pass = 1; pass <= 2; pass++) {
          // For all states in endClass, normal or in overflow list ...
          if (pass == 1) {
            endClass.reset(mClassReadIterator);
          } else if (!endClass.resetOverflowList(mClassReadIterator)) {
            break;
          }
          while (mClassReadIterator.advance()) {
            final int state = mClassReadIterator.getCurrentData();
            // For all successors in tau-closure of event ...
            mFullEventClosureIterator.resume(state);
            while (mFullEventClosureIterator.advance()) {
              // Add transition to cache ...
              final int succ =
                mFullEventClosureIterator.getCurrentTargetState();
              final EquivalenceClass succClass = mStateToClass[succ];
              final int succCode = succClass.getSmallestState();
              final long code = succCode | eshift;
              mEventSuccessorCache.add(code);
            }
          }
        }
      }
    }

    /**
     * Checks for the existence of a shared uncontrollable transition in the
     * full-event closure from the class currently being split to class of
     * a given successor state.
     * @param endClass
     *          The class currently being split on.
     * @param event
     *          The shared uncontrollable event being checked.
     * @param succ
     *          The successor state to be checked.
     * @return <CODE>true</CODE> if a such a transition exists,
     *         <CODE>false</CODE> otherwise.
     */
    private boolean lookupEventSuccessorCache(final EquivalenceClass endClass,
                                              final int event,
                                              final int succ)
    {
      initEventCache(endClass, event);
      final EquivalenceClass succClass = mStateToClass[succ];
      final int succCode = succClass.getSmallestState();
      final long eshift = ((long) event) << 32;
      final long code = succCode | eshift;
      return mEventSuccessorCache.contains(code);
    }

    /**
     * Clears the cache used by the
     * {@link #getUniqueSuccessor(int,EquivalenceClass) getUniqueSuccessor()}
     * method. The cache needs to be cleared when starting to split on a
     * new end class.
     */
    private void clearCache()
    {
      mUniqueSuccessorClassCache = null;
      mEventSuccessorCache = null;
    }

    //#######################################################################
    //# Data Members
    private final TransitionIterator mEventIterator;
    private final TauClosure mLocalUncontrollableSuccessorsTauClosure;
    private final TransitionIterator mTauClosureIterator;
    private final TransitionIterator mFullEventClosureIterator;
    private final IntListBuffer.ReadOnlyIterator mClassReadIterator;
    private TIntObjectHashMap<EquivalenceClass> mUniqueSuccessorClassCache;
    private TLongHashSet mEventSuccessorCache;

    //#######################################################################
    //# Class Constants
    /**
     * Dummy equivalence class used as a result from {@link
     * #getUniqueSuccessorClass(int, EquivalenceClass)
     * getUniqueSuccessorClass()} to indicate that a state has no
     * local uncontrollable successors outside of its own class or the
     * class currently being split on.
     */
    private final EquivalenceClass NO_CLASS = new EquivalenceClass(this);
    /**
     * Dummy equivalence class used as a result from {@link
     * #getUniqueSuccessorClass(int, EquivalenceClass)
     * getUniqueSuccessorClass()} to indicate that a state has shared
     * uncontrollable transitions outgoing that do not match the class
     * currently being split on, or local uncontrollable successors in
     * more than one class besides of its own class and the class currently
     * being split on.
     */
    private final EquivalenceClass BAD_CLASS = new EquivalenceClass(this);
  }


  //#########################################################################
  //# Data Members
  /**
   * The maximum number of transitions (including stored silent transitions of
   * the transitive closure) that will be stored. A value of
   * {@link Integer#MAX_VALUE} indicates an unlimited number of transitions.
   */
  private int mTransitionLimit = Integer.MAX_VALUE;
  /**
   * Whether or not weak synthesis observation equivalence is used.
   */
  private boolean mWeak = true;

  private ListBufferTransitionRelation mOriginalTransitionRelation;

  private int mNumReachableStates;
  private int mNumClasses;
  private boolean mHasModifications;

  private int mOmegaState;
  private TauClosure mLocalUncontrollablePredecessorsTauClosure;
  private TransitionIterator mPredecessorIterator;
  private TransitionIterator mUncontrollableTauIterator;
  private TransitionIterator mUncontrollableEventIterator;
  private UncontrollableTransitionCache mUncontrollableTransitionCache;
  private IntListBuffer mListBuffer;
  private IntListBuffer.ReadOnlyIterator mClassReadIterator;
  private IntListBuffer.ModifyingIterator mClassWriteIterator;
  private EquivalenceClass[] mStateToClass;
  private int[] mPredecessors;
  private Queue<EquivalenceClass> mSplitters;
  private TIntArrayList mTempClass;

}

