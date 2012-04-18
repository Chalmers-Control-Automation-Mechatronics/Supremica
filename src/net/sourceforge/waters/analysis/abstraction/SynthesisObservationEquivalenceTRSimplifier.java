//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters Analysis
//# PACKAGE: net.sourceforge.waters.analysis.abstraction
//# CLASS:   SynthesisAbstractionTRSimplifier
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.analysis.abstraction;

import gnu.trove.THashSet;
import gnu.trove.TIntArrayList;
import gnu.trove.TIntHashSet;
import gnu.trove.TIntIntHashMap;

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
   * Sets the code of the last local uncontrollable event. Events are encoded
   * such that all local events appear before all shared events, and all
   * uncontrollable local events appear before controllable local events. The
   * tau event code ({@link EventEncoding#TAU} is not used. Therefore, the
   * range of uncontrollable local events is from {@link EventEncoding#NONTAU}
   * to {@link #getLastLocalUncontrollableEvent()} inclusive.
   */
  public void setLastLocalUncontrollableEvent(final int event)
  {
    mLastLocalUncontrollableEvent = event;
  }

  /**
   * Gets the code of the last local uncontrollable event.
   *
   * @see #setLastLocalUncontrollableEvent(int) setLastLocalUncontrollableEvent()
   */
  public int getLastLocalUncontrollableEvent()
  {
    return mLastLocalUncontrollableEvent;
  }

  /**
   * Sets the code of the last local controllable event. Events are encoded
   * such that all local events appear before all shared events, and all
   * uncontrollable local events appear before controllable local events.
   * Therefore, the range of controllable local events is from
   * {@link #getLastLocalUncontrollableEvent()}+1 to
   * {@link #getLastLocalControllableEvent()} inclusive.
   */
  public void setLastLocalControllableEvent(final int event)
  {
    mLastLocalControllableEvent = event;
  }

  /**
   * Gets the code of the last local controllable event.
   *
   * @see #setLastLocalControllableEvent(int) setLastLocalControllableEvent()
   */
  public int getLastLocalControllableEvent()
  {
    return mLastLocalControllableEvent;
  }

  /**
   * Sets the code of the last shared uncontrollable event. Events are encoded
   * such that all local events appear before all shared events, and all
   * uncontrollable local events appear before controllable events.
   * Therefore, the range of uncontrollable shared events is from
   * {@link #getLastLocalControllableEvent()}+1 to
   * {@link #getLastSharedUncontrollableEvent()} inclusive.
   */
  public void setLastSharedUncontrollableEvent(final int event)
  {
    mLastSharedUncontrollableEvent = event;
  }

  /**
   * Gets the code of the last shared uncontrollable event.
   *
   * @see #setLastSharedUncontrollableEvent(int) setLastSharedUncontrollableEvent()
   */
  public int getLastSharedUncontrollableEvent()
  {
    return mLastSharedUncontrollableEvent;
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
    return ListBufferTransitionRelation.CONFIG_PREDECESSORS;
  }

  @Override
  public void setTransitionRelation(final ListBufferTransitionRelation rel)
  {
    reset();
    super.setTransitionRelation(rel);
    mNumReachableStates = rel.getNumberOfReachableStates();
    mNumEvents = rel.getNumberOfProperEvents();
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
    mUncontrollableTauClosure = null;
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
    mNumClasses = 0;
    mListBuffer = new IntListBuffer();
    mClassReadIterator = mListBuffer.createReadOnlyIterator();
    mClassWriteIterator = mListBuffer.createModifyingIterator();
    final int numStates = rel.getNumberOfStates();
    mStateToClass = new EquivalenceClass[numStates];
    mPredecessors = new int[numStates];
    mSplitters = new PriorityQueue<EquivalenceClass>();
    if (mWeak) {
      mUncontrollableTransitionStorage =
        new UncontrollableTransitionStorage();
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
      rel.setUsedEvent(EventEncoding.TAU, false);
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
    mUncontrollableTransitionStorage = null;
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
      mUncontrollableTauClosure = null;
      mUncontrollableTauIterator = null;
      mUncontrollableEventIterator = null;
      final ListBufferTransitionRelation copy = new ListBufferTransitionRelation
        (rel,ListBufferTransitionRelation.CONFIG_SUCCESSORS);
      super.applyResultPartition();
      rel.removeTauSelfLoops();
      rel.removeProperSelfLoopEvents();
      rel.reconfigure(ListBufferTransitionRelation.CONFIG_SUCCESSORS);
      if (!rel.isDeterministic()) {
        mOriginalTransitionRelation = copy;
      }
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
      rel.setUsedEvent(EventEncoding.TAU, true);
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
    if (mUncontrollableTauClosure == null) {
      final ListBufferTransitionRelation rel = getTransitionRelation();
      final int limit = getTransitionLimit();
      mUncontrollableTauClosure =
        rel.createPredecessorsTauClosure(limit, EventEncoding.NONTAU,
                                         mLastLocalUncontrollableEvent);
      mPredecessorIterator = rel.createPredecessorsReadOnlyIterator();
      mUncontrollableTauIterator =
        new OneEventCachingTransitionIterator
          (mUncontrollableTauClosure.createIterator(), EventEncoding.TAU);
      mUncontrollableEventIterator =
        mUncontrollableTauClosure.createFullEventClosureIterator(-1);
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

    //#######################################################################
    //# Interface java.util.Comparable<EquivalenceClass>
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

      // Uncontrollable shared events
      for (int event = mLastLocalControllableEvent + 1;
           event <= mLastSharedUncontrollableEvent;
           event++) {
        if (rel.isUsedEvent(event)) {
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
      for (int event = mLastSharedUncontrollableEvent + 1;
           event < mNumEvents; event++) {
        if (rel.isUsedEvent(event)) {
          splitOnControllable(event);
        }
      }
      // ... including omega
      if (rel.isUsedEvent(EventEncoding.TAU)) {
        splitOnControllable(EventEncoding.TAU);
      }
      // Controllable local events
      splitOnControllable(mLastLocalControllableEvent);

      mTempClass.clear();
      if (mWeak) {
        mUncontrollableTransitionStorage.clearUniqueSuccessorCache();
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
      for (final EquivalenceClass splitClass : splitClasses) {
        splitClass.splitUsingOverflowList();
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
      final boolean local =
        event > EventEncoding.TAU && event <= mLastLocalControllableEvent;
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
            mPredecessorIterator.resetEvents(mLastLocalUncontrollableEvent + 1,
                                             mLastLocalControllableEvent);
            while (mPredecessorIterator.advance()) {
              final int source = mPredecessorIterator.getCurrentSourceState();
              final SearchRecord next =
                new SearchRecord(source, controllableStartClass, true);
              addSearchRecord(next, open, visited);
            }
          }
          // Visit predecessors for all local uncontrollable transitions ...
          mPredecessorIterator.resetEvents(EventEncoding.NONTAU,
                                           mLastLocalUncontrollableEvent);
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
            mPredecessorIterator.resetEvents(EventEncoding.NONTAU,
                                             mLastLocalControllableEvent);
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
              // uncontrollable event outgoing (usucc == MULTIPLE_SUCCESSORS),
              // or if it has a local uncontrollable successor state
              // not equal to the source state nor to the current state, nor
              // equivalent to the end state.
              final int usucc = mUncontrollableTransitionStorage.
                getUniqueSuccessor(source, endClass);
              if (usucc == MULTIPLE_SUCCESSORS ||
                  (usucc != NO_SUCCESSOR && usucc != state)) {
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

    private void splitUsingOverflowList()
    {
      if (mOverflowSize <= 0) {
        return;
      } else if (mOverflowSize == getSize()) {
        mList = mOverflowList;
      } else {
        doSimpleSplit(mOverflowList, mOverflowSize, mOverflowSize >= 0);
      }
      mOverflowSize = 0;
      mOverflowList = IntListBuffer.NULL;
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
  //# Inner Class UncontrollableTransitionStorage
  /**
   * <P>Auxiliary class to store local uncontrollable transitions.</P>
   *
   * <P>Stores for each state the list of successor states reached by
   * some local uncontrollable transition, excluding selfloops and
   * duplicates. States with an outgoing shared uncontrollable
   * transition are marked specially and are not associated with any
   * list of successor states.</P>
   *
   * <P>This class also implements a cache to check for states with
   * exactly one local uncontrollable transition to a state outside of
   * the class currently being split on, which reduces the complexity
   * of the backwards search for weak synthesis observation equivalence.</P>
   */
  private class UncontrollableTransitionStorage {

    //#######################################################################
    //# Constructor
    private UncontrollableTransitionStorage()
    {
      final ListBufferTransitionRelation rel = getTransitionRelation();
      final int numStates = rel.getNumberOfStates();
      mLists = new int[numStates];
      final int empty = mListBuffer.createList();
      int state;
      for (state = 0; state < numStates; state++) {
        mLists[state] = empty;
      }
      int ecount = numStates;
      for (state = 0; state < numStates; state++) {
        mPredecessorIterator.resetState(state);
        mPredecessorIterator.resetEvents(mLastLocalControllableEvent + 1,
                                         mLastSharedUncontrollableEvent);
        while (mPredecessorIterator.advance()) {
          final int pred = mPredecessorIterator.getCurrentSourceState();
          if (mLists[pred] == empty) {
            mLists[pred] = IntListBuffer.NULL;
            ecount--;
          }
        }
      }
      for (state = 0; state < numStates; state++) {
        mPredecessorIterator.resetState(state);
        mPredecessorIterator.resetEvents(EventEncoding.NONTAU,
                                         mLastLocalUncontrollableEvent);
        while (mPredecessorIterator.advance()) {
          final int pred = mPredecessorIterator.getCurrentSourceState();
          if (pred == state) {
            continue;
          }
          int list = mLists[pred];
          if (list == IntListBuffer.NULL) {
            continue;
          } else if (list == empty) {
            mLists[pred] = list = mListBuffer.createList();
            mListBuffer.prepend(list, state);
            ecount--;
          } else {
            mListBuffer.prependUnique(list, state);
          }
        }
      }
      if (ecount == 0) {
        mListBuffer.dispose(empty);
      }
      mIterator = mListBuffer.createReadOnlyIterator();
    }

    //#######################################################################
    //# Cache Access
    /**
     * Gets a unique uncontrollable successor of a given state.
     * This method first checks whether the given source state
     * <CODE>state</CODE> has a shared uncontrollable transition outgoing;
     * if so, it returns {@link #MULTIPLE_SUCCESSORS}.
     * Otherwise it checks for local uncontrollable transitions outgoing
     * from <CODE>state</CODE> that are not selfloops, and that do not lead
     * to a state in the given <CODE>endClass</CODE>. If there is no such
     * transition, it returns {@link #NO_SUCCESSORS}. If there is exactly
     * one such transition, it returns the target state of that transition.
     * If there is more than one such transition, it returns
     * {@link #MULTIPLE_SUCCESSORS}.
     */
    private int getUniqueSuccessor(final int state,
                                   final EquivalenceClass endClass)
    {
      final int list = mLists[state];
      if (list == IntListBuffer.NULL) {
        return MULTIPLE_SUCCESSORS;
      }
      if (mUniqueSuccessorCache == null) {
        mUniqueSuccessorCache = new TIntIntHashMap();
      } else if (mUniqueSuccessorCache.containsKey(state)) {
        return mUniqueSuccessorCache.get(state);
      }
      mIterator.reset(list);
      int result = NO_SUCCESSOR;
      while (mIterator.advance()) {
        final int succ = mIterator.getCurrentData();
        if (mStateToClass[succ] != endClass) {
          if (result == NO_SUCCESSOR) {
            result = succ;
          } else {
            mUniqueSuccessorCache.put(state, MULTIPLE_SUCCESSORS);
            return MULTIPLE_SUCCESSORS;
          }
        }
      }
      mUniqueSuccessorCache.put(state, result);
      return result;
    }

    /**
     * Clears the cache used by the
     * {@link #getUniqueSuccessor(int,EquivalenceClass) getUniqueSuccessor()}
     * method. The cache needs to be cleared when starting to split on a
     * new end class.
     */
    private void clearUniqueSuccessorCache()
    {
      mUniqueSuccessorCache = null;
    }

    //#######################################################################
    //# Data Members
    private final int[] mLists;
    private final IntListBuffer.ReadOnlyIterator mIterator;
    private TIntIntHashMap mUniqueSuccessorCache;
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
   * The code of the last local uncontrollable event.
   * Local uncontrollable events are thus stored in the range from
   * {@link EventEncoding#NONTAU} to mLastLocalUncontrollableEvent.
   */
  private int mLastLocalUncontrollableEvent;
  /**
   * The code of the last local controllable event.
   * Local controllable events are thus stored in the range from
   * {@link #mLastLocalUncontrollableEvent}+1 to
   * mLastLocalControllableEvent.
   */
  private int mLastLocalControllableEvent;
  /**
   * The code of the last shared uncontrollable event.
   * Shared uncontrollable events are thus stored in the range from
   * {@link #mLastLocalControllableEvent}+1 to
   * mLastSharedUncontrollableEvent.
   */
  private int mLastSharedUncontrollableEvent;
  /**
   * Whether or not weak synthesis observation equivalence is used
   */
  private boolean mWeak = true;

  private ListBufferTransitionRelation mOriginalTransitionRelation;

  private int mNumReachableStates;
  private int mNumEvents;
  private int mNumClasses;
  private boolean mHasModifications;

  private int mOmegaState;
  private TauClosure mUncontrollableTauClosure;
  private TransitionIterator mPredecessorIterator;
  private TransitionIterator mUncontrollableTauIterator;
  private TransitionIterator mUncontrollableEventIterator;
  private UncontrollableTransitionStorage mUncontrollableTransitionStorage;
  private IntListBuffer mListBuffer;
  private IntListBuffer.ReadOnlyIterator mClassReadIterator;
  private IntListBuffer.ModifyingIterator mClassWriteIterator;
  private EquivalenceClass[] mStateToClass;
  private int[] mPredecessors;
  private Queue<EquivalenceClass> mSplitters;
  private TIntArrayList mTempClass;


  //#########################################################################
  //# Class Constants
  /**
   * Constant indicating a state with no uncontrollable successor.
   * @see UncontrollableTransitionStorage#getUniqueSuccessor(int,EquivalenceClass)
   *      getUniqueSuccessor()
   */
  private static final int NO_SUCCESSOR = -1;
  /**
   * Constant indicating a state with a shared uncontrollable successor,
   * or multiple local uncontrollable successors.
   * @see UncontrollableTransitionStorage#getUniqueSuccessor(int,EquivalenceClass)
   *      getUniqueSuccessor()
   */
  private static final int MULTIPLE_SUCCESSORS = -2;
}
