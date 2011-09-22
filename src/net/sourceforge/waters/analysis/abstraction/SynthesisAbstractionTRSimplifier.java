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
import gnu.trove.TLongObjectHashMap;
import gnu.trove.TLongObjectIterator;

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
 * <P>
 * This transition relation simplifier can simplify a given deterministic (or
 * nondeterministic) automaton according synthesis equivalence. The algorithm
 * is based on the partitioning algorithms for bisimulation by Jean-Claude
 * Fernandez, modified for synthesis abstraction.
 * </P>
 *
 * <P>
 * <I>References.</I><BR>
 * Sahar Mohajerani, Robi Malik, Simon Ware, Martin Fabian.
 * On the Use of Observation Equivalence in Synthesis Abstraction.
 * Proc. 3rd IFAC Workshop on Dependable Control of Discrete Systems,
 * DCDS&nbsp;2011, Saarbr&uuml;cken, Germany, 2011.<BR>
 * Jean-Claude Fernandez. An Implementation of an Efficient Algorithm for
 * Bisimulation Equivalence. Science of Computer Programming,
 * <STRONG>13</STRONG>, 219-236, 1990.<BR>
 * </P>
 *
 * @author Sahar Mohajerani, Robi Malik
 */

public class SynthesisAbstractionTRSimplifier extends AbstractTRSimplifier
{

  //#########################################################################
  //# Constructors
  /**
   * Creates a new bisimulation simplifier without a transition relation.
   */
  public SynthesisAbstractionTRSimplifier()
  {
  }

  /**
   * Creates a new bisimulation simplifier for the given transition relation.
   */
  public SynthesisAbstractionTRSimplifier
    (final ListBufferTransitionRelation rel)
  {
    if (rel != null) {
      setTransitionRelation(rel);
    }
  }

  //#########################################################################
  //# Configuration
  /**
   * Determines the propositions used when partitioning. The propositions mask
   * is only used to distinguish states when setting up an initial partition.
   * When merging states, all propositions present in the transition relation
   * are merged.
   *
   * @param mask
   *          The bit mask of the significant propositions, or <CODE>-1</CODE>
   *          to indicate that all propositions are significant.
   * @see #setUpInitialPartitionBasedOnMarkings()
   */
  public void setPropositionMask(final long mask)
  {
    mPropositionMask = mask;
  }

  /**
   * Gets the mask of significant propositions.
   *
   * @see #setPropositionMask(long) setPropositionMask()
   */
  public long getPropositionMask()
  {
    return mPropositionMask;
  }

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

  public boolean isPartitioning()
  {
    return true;
  }

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
    mUncontrollableTauClosure = null;
    mPredecessorIterator = null;
    mUncontrollableTauIterator = null;
    mUncontrollableEventIterator = null;
  }


  //#########################################################################
  //# Initial Partition
  /**
   * Sets up an initial partition for the bisimulation algorithm based on the
   * markings of states in the transition relation. States with equal sets of
   * markings are placed in the same class. This method replaces any
   * previously set initial partition. This method is called by default during
   * each {@link #run()} unless the user provides an alternative initial
   * partition.
   */
  public void setUpInitialPartitionBasedOnMarkings() throws OverflowException
  {
    final long mask = getPropositionMask();
    setUpInitialPartitionBasedOnMarkings(mask);
  }

  /**
   * Sets up an initial partition for the bisimulation algorithm based on the
   * markings of states in the transition relation. States with equal sets of
   * markings are placed in the same class. This method replaces any
   * previously set initial partition. This method is called by default during
   * each {@link #run()} unless the user provides an alternative initial
   * partition.
   *
   * @param mask
   *          Marking pattern identifying the markings to be considered. Only
   *          markings in this pattern will be taken into account for the
   *          partition.
   */
  public void setUpInitialPartitionBasedOnMarkings(final long mask)
    throws OverflowException
  {
    final ListBufferTransitionRelation rel = getTransitionRelation();
    final int numStates = rel.getNumberOfStates();

    mHasModifications = false;
//    final long notMarked = rel.createMarkings();
//    setUpTauClosure();
//    final TransitionIterator iter = mAllTauIterator;
//    for (int state = 0; state < numStates; state++) {
//      if (rel.isReachable(state)) {
//        final long marking = rel.getAllMarkings(state) & mask;
//        if (marking != notMarked) {
//          iter.resetState(state);
//          while (iter.advance()) {
//            final int pred = iter.getCurrentSourceState();
//            if (rel.addMarkings(pred, marking)) {
//              mHasModifications = true;
//            }
//          }
//        }
//      }
//    }

    final int numProps = rel.getNumberOfMarkings();
    final int size = numProps > 8 ? 256 : 1 << numProps;
    setUpPartition(size);
    final TLongObjectHashMap<EquivalenceClass> prepartition =
      new TLongObjectHashMap<EquivalenceClass>();
    for (int state = 0; state < numStates; state++) {
      if (rel.isReachable(state)) {
        final long marking = rel.getAllMarkings(state) & mask;
        EquivalenceClass clazz = prepartition.get(marking);
        if (clazz == null) {
          clazz = new EquivalenceClass();
          prepartition.put(marking, clazz);
        }
        clazz.addState(state);
      }
    }
    final TLongObjectIterator<EquivalenceClass> mapIter =
      prepartition.iterator();
    while (mapIter.hasNext()) {
      mapIter.advance();
      final EquivalenceClass sec = mapIter.value();
      sec.setUpStateToClass();
      sec.enqueue();
    }
  }


  //#########################################################################
  //# Overrides for net.sourceforge.waters.analysis.abstraction.AbstractTRSimplifier
  @Override
  protected void setUp() throws AnalysisException
  {
    super.setUp();
    if (mSplitters == null) {
      mHasModifications = false;
      final boolean modified = mHasModifications;
      setUpInitialPartitionBasedOnMarkings();
      mHasModifications |= modified;
    }
    setUpTauClosure();
    final ListBufferTransitionRelation rel = getTransitionRelation();
    final int numStates = rel.getNumberOfStates();
    mHasUncontrollable = new boolean[numStates];
    final TransitionIterator itr = rel.createAllTransitionsReadOnlyIterator();
    itr.resetEvents(EventEncoding.NONTAU, mLastLocalUncontrollableEvent);
    while(itr.advance()){
      final int source = itr.getCurrentSourceState();
      mHasUncontrollable[source] = true;
    }
    itr.resetEvents(mLastLocalControllableEvent+1,
                    mLastSharedUncontrollableEvent);
    while(itr.advance()){
      final int source = itr.getCurrentSourceState();
      mHasUncontrollable[source] = true;
    }
    mTempClass = new TIntArrayList(numStates);
  }

  @Override
  protected boolean runSimplifier() throws AnalysisException
  {
    if (mNumClasses < mNumReachableStates) {
      int prevNumClasses = mNumClasses;
      while (true) {
        for (Splitter splitter = mSplitters.poll();
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
    buildResultPartition();
    applyResultPartitionAutomatically();
    return mHasModifications || mNumClasses < mNumReachableStates;
  }


  @Override
  protected void tearDown()
  {
    super.tearDown();
    mClassLists = null;
    mClassReadIterator = null;
    mClassWriteIterator = null;
    mStateToClass = null;
    mPredecessors = null;
    mSplitters = null;
    mTempClass = null;
    mHasUncontrollable = null;
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
      super.applyResultPartition();
      rel.removeTauSelfLoops();
      rel.removeProperSelfLoopEvents();
    } else {
      if (mHasModifications) {
        rel.removeProperSelfLoopEvents();
      }
    }
  }


  //#########################################################################
  //# Algorithm
  private void setUpPartition(final int numSplitters)
  {
    final ListBufferTransitionRelation rel = getTransitionRelation();
    mNumClasses = 0;
    mClassLists = new IntListBuffer();
    mClassReadIterator = mClassLists.createReadOnlyIterator();
    mClassWriteIterator = mClassLists.createModifyingIterator();
    final int numStates = rel.getNumberOfStates();
    mStateToClass = new EquivalenceClass[numStates];
    mPredecessors = new int[numStates];
    mSplitters = new PriorityQueue<Splitter>(numSplitters);
  }

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

  private void enqueueAlltheClasses(){
    for(final EquivalenceClass eq : mStateToClass){
      if(eq != null){
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
  //# Local Interface Splitter
  private interface Splitter extends Comparable<Splitter>
  {

    public int getSize();

    public void collect(TIntArrayList states);

    public void splitOn();

    public void enqueue();

    public void dump(PrintWriter printer);

  }


  //#########################################################################
  //# Inner Class EquivalenceClass
  private class EquivalenceClass implements Splitter
  {

    //#######################################################################
    //# Constructors
    private EquivalenceClass()
    {
      mSize = 0;
      mList = mClassLists.createList();
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
      mList = mClassLists.createList(states);
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
      mClassLists.append(mList, state);
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
    int getSmallestState(){
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
    //# Interface java.util.Comparable<Splitter>
    public int compareTo(final Splitter splitter)
    {
      return mSize - splitter.getSize();
    }

    //#######################################################################
    //# Interface Splitter
    public int getSize()
    {
      return mSize;
    }

    public void collect(final TIntArrayList states)
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

    public void splitOn()
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
      final int size = mTempClass.size();
      for (int event = mLastSharedUncontrollableEvent + 1;
           event < mNumEvents; event++) {
        if (rel.isUsedEvent(event)) {
          final Set <SearchRecord> visited = new THashSet<SearchRecord>();
          final TIntHashSet found = new TIntHashSet();
          final Collection<EquivalenceClass> splitClasses =
            new THashSet<EquivalenceClass>();
          for (int i = 0; i < size; i++) {
            final int state = mTempClass.get(i);
            explore(state, event, visited, found, splitClasses);
          }
          for (final EquivalenceClass splitClass : splitClasses) {
            splitClass.splitUsingOverflowList();
          }
        }
      }

      // Controllable local events
      final Set <SearchRecord> visited = new THashSet<SearchRecord>();
      final TIntHashSet found = new TIntHashSet();
      final Collection<EquivalenceClass> splitClasses =
        new THashSet<EquivalenceClass>();
      for (int i = 0; i < size; i++) {
        final int state = mTempClass.get(i);
        explore(state, mLastLocalControllableEvent, visited, found,
                splitClasses);
      }
      for (final EquivalenceClass splitClass : splitClasses) {
        splitClass.splitUsingOverflowList();
      }

      mTempClass.clear();
    }

    private void explore(final int endState, final int event,
                         final Set <SearchRecord> visited,
                         final TIntHashSet found,
                         final Collection<EquivalenceClass> splitClasses )
    {
      final Queue<SearchRecord> opened = new ArrayDeque<SearchRecord>();
      final EquivalenceClass endClass = mStateToClass[endState];
      final SearchRecord initial =
        new SearchRecord(endState, null, event <= mLastLocalControllableEvent);
      visited.add(initial);
      opened.add(initial);
      while(!opened.isEmpty()){
        final SearchRecord record = opened.remove();
        mPredecessorIterator.resetState(record.getState());
        if (record.getHasEvent()) {
          final int state = record.getState();
          if (mStateToClass[state] == record .getStartingClass()
            || record .getStartingClass() == null) {
            // output source state
            if(found.add(state)){
              final EquivalenceClass splitClass = mStateToClass[state];
              if (splitClass.getSize() > 1) {
                splitClass.moveToOverflowList(state);
                splitClasses.add(splitClass);
              }
            }
          }

          // Visit predecessors ...
          if (mHasUncontrollable[state] && !(mStateToClass[state] == endClass
             || mStateToClass[state] == record.getStartingClass())){
            if (record.getStartingClass() == null){
              mPredecessorIterator.resetEvents(mLastLocalUncontrollableEvent + 1,
                                               mLastLocalControllableEvent);
              while (mPredecessorIterator.advance()) {
                final int source = mPredecessorIterator.getCurrentSourceState();
                final SearchRecord next = new SearchRecord(source,
                                                     mStateToClass[state],
                                                     true);
                if (visited.add(next)){
                  opened.add(next);
                }
              }
            }
            mPredecessorIterator.resetEvents(EventEncoding.NONTAU,
                                             mLastLocalUncontrollableEvent);
            while (mPredecessorIterator.advance()) {
              final int source = mPredecessorIterator.getCurrentSourceState();
              final SearchRecord next = new SearchRecord(source,
                                                   record.getStartingClass(),
                                                   true);
              if (visited.add(next)){
                opened.add(next);
              }
            }
          } else {
            mPredecessorIterator.resetEvents(EventEncoding.NONTAU,
                                             mLastLocalControllableEvent);
            while (mPredecessorIterator.advance()) {
              final int source = mPredecessorIterator.getCurrentSourceState();
              final SearchRecord next = new SearchRecord(source,
                                                   record.getStartingClass(),
                                                   true);
              if (visited.add(next)){
                opened.add(next);
              }
            }

          }
        } else {  // !record.hasEvent()
          mPredecessorIterator.resetEvents(EventEncoding.NONTAU,
                                           mLastLocalControllableEvent);
          while (mPredecessorIterator.advance()) {
            final int source = mPredecessorIterator.getCurrentSourceState();
            final EquivalenceClass newStartingClass;
            if (mHasUncontrollable[source]) {
              final EquivalenceClass sourceClass = mStateToClass[source];
              if (sourceClass == endClass ||
                  record.getStartingClass() == sourceClass) {
                newStartingClass = record.getStartingClass();
              } else if (record.getStartingClass() == null){
                newStartingClass = sourceClass;
              } else {
                continue;
              }
            } else{
              newStartingClass = record.getStartingClass();
            }
            final SearchRecord next = new SearchRecord(source, newStartingClass,
                                                 false);
            if (visited.add(next)){
              opened.add(next);
            }
          }
          mPredecessorIterator.resetEvent(event);
          while (mPredecessorIterator.advance()){
            final int source = mPredecessorIterator.getCurrentSourceState();
            final SearchRecord next = new SearchRecord(source,
                                                 record.getStartingClass(),
                                                 true);
            if (visited.add(next)){
              opened.add(next);
            }
          }
        }
      }
    }

    public void enqueue()
    {
      if (!mIsOpenSplitter) {
        mIsOpenSplitter = true;
        mSplitters.add(this);
      }
    }

    //#######################################################################
    //# Overrides for EquivalenceClass
    void doSimpleSplit(final int overflowList, final int overflowSize,
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

    void moveToOverflowList(final int state)
    {
      final int tail;
      switch (mOverflowSize) {
      case -1:
        setUpPredecessors();
        // fall through ...
      case 0:
        mOverflowList = mClassLists.createList();
        mOverflowSize = 1;
        tail = IntListBuffer.NULL;
        break;
      default:
        mOverflowSize++;
        tail = mClassLists.getTail(mOverflowList);
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

    void splitUsingOverflowList()
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
    int[] putResult(final int state)
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
      for (int list = mClassLists.getHead(mList);
           list != IntListBuffer.NULL;
           list = mClassLists.getNext(list)) {
        final int state = mClassLists.getData(list);
        mPredecessors[state] = pred;
        pred = list;
      }
    }

    private void setUpSmallestState()
    {
      int smallest = Integer.MAX_VALUE;
      for (int list = mClassLists.getHead(mList);
           list != IntListBuffer.NULL;
           list = mClassLists.getNext(list)) {
        final int state = mClassLists.getData(list);
        if(state < smallest){
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
      mClassLists.dumpList(printer, mList);
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
        if (record.mState == mState
            && record.mStartingClass == mStartingClass
            && record.mHasEvent == mHasEvent) {
          return true;
        } else {
          return false;
        }
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
  //# Data Members
  private long mPropositionMask = ~0;
  private int mTransitionLimit = Integer.MAX_VALUE;
  private int mLastLocalUncontrollableEvent;
  private int mLastLocalControllableEvent;
  private int mLastSharedUncontrollableEvent;

  private int mNumReachableStates;
  private int mNumEvents;
  private int mNumClasses;
  private boolean mHasModifications;

  private TauClosure mUncontrollableTauClosure;
  private TransitionIterator mPredecessorIterator;
  private TransitionIterator mUncontrollableTauIterator;
  private TransitionIterator mUncontrollableEventIterator;
  private boolean[] mHasUncontrollable;
  private IntListBuffer mClassLists;
  private IntListBuffer.ReadOnlyIterator mClassReadIterator;
  private IntListBuffer.ModifyingIterator mClassWriteIterator;
  private EquivalenceClass[] mStateToClass;
  private int[] mPredecessors;
  private Queue<Splitter> mSplitters;
  private TIntArrayList mTempClass;

}
