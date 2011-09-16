//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters Analysis
//# PACKAGE: net.sourceforge.waters.analysis.abstraction
//# CLASS:   ObservationEquivalenceTRSimplifier
//###########################################################################
//# $Id: ObservationEquivalenceTRSimplifier.java 6451 2011-09-04 07:13:54Z robi $
//###########################################################################

package net.sourceforge.waters.analysis.abstraction;

import gnu.trove.THashSet;
import gnu.trove.TIntArrayList;
import gnu.trove.TIntHashSet;
import gnu.trove.TIntIntHashMap;
import gnu.trove.TLongObjectHashMap;
import gnu.trove.TLongObjectIterator;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Queue;

import net.sourceforge.waters.analysis.tr.EventEncoding;
import net.sourceforge.waters.analysis.tr.IntListBuffer;
import net.sourceforge.waters.analysis.tr.ListBufferTransitionRelation;
import net.sourceforge.waters.analysis.tr.OneEventCachingTransitionIterator;
import net.sourceforge.waters.analysis.tr.TauClosure;
import net.sourceforge.waters.analysis.tr.TransitionIterator;
import net.sourceforge.waters.model.analysis.AnalysisException;
import net.sourceforge.waters.model.analysis.OverflowException;
import net.sourceforge.waters.model.des.AutomatonTools;


/**
 * <P>A bisimulation and observation equivalence partitioning algorithm.</P>
 *
 * <P>This transition relation simplifier can simplify a given deterministic or
 * nondeterministic automaton according to common equivalences such as
 * bisimulation and observation equivalence. The implementation is based on the
 * bisimulation algorithm by Jean-Claude Fernandez. When computing observation
 * equivalence, it can additionally be configured to remove redundant
 * transitions before and after partitioning.</P>
 *
 * <P>
 * <I>References.</I><BR>
 * Jean-Claude Fernandez. An Implementation of an Efficient Algorithm for
 * Bisimulation Equivalence. Science of Computer Programming,
 * <STRONG>13</STRONG>, 219-236, 1990.<BR>
 * J. E. Hopcroft. An <I>n</I>&nbsp;log&nbsp;<I>n</I> Algorithm for Minimizing
 * States in a Finite Automaton. In: Z. Kohavi and A. Paz, eds., Theory of
 * Machines and Computations, Academic Press, New York, 397-419, 1971.<BR>
 * Jaana Eloranta. Minimizing the Number of Transitions with Respect to
 * Observation Equivalence. BIT, <STRONG>31</STRONG>(4), 397-419, 1991.
 * </P>
 *
 * @author Robi Malik, Simon Ware, Rachel Francis
 */

public class SynthesisAbstractionTRSimplifier
  extends AbstractTRSimplifier
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
   * Determines the propositions used when partitioning.
   * The propositions mask is only used to distinguish states when setting up
   * an initial partition. When merging states, all propositions present in
   * the transition relation are merged.
   * @param  mask   The bit mask of the significant propositions,
   *                or <CODE>-1</CODE> to indicate that all propositions
   *                are significant.
   * @see #setUpInitialPartitionBasedOnMarkings()
   */
  public void setPropositionMask(final long mask)
  {
    mPropositionMask = mask;
  }

  /**
   * Gets the mask of significant propositions.
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
   * @param limit
   *          The new transition limit, or {@link Integer#MAX_VALUE} to allow an
   *          unlimited number of transitions.
   */
  public void setTransitionLimit(final int limit)
  {
    mTransitionLimit = limit;
  }

  /**
   * Gets the transition limit.
   * @see #setTransitionLimit(int) setTransitionLimit()
   */
  public int getTransitionLimit()
  {
    return mTransitionLimit;
  }
  /**
   * Sets the code of the last local uncontrollable event.
   * Events are encoded such that all local events appear before all shared
   * events, and all uncontrollable local events appear before controllable
   * local events. The tau event code ({@link EventEncoding#TAU} is not used.
   * Therefore, the range of uncontrollable local events is from
   * {@link EventEncoding#NONTAU} to {@link
   * #getLastUncontrollableLocalEvent()} inclusive.
   */
  public void setLastUncontrollableLocalEvent(final int event)
  {
    mLastUncontrollableLocalEvent = event;
  }

  /**
   * Gets the code of the last local uncontrollable event.
   * @see #getLastUncontrollableLocalEvent()
   */
  public int getLastUncontrollableLocalEvent(){
      return mLastUncontrollableLocalEvent;
  }

  /**
   * Sets the code of the last local controllable event.
   * Events are encoded such that all local events appear before all shared
   * events, and all uncontrollable local events appear before controllable
   * local events. Therefore, the range of controllable local events is from
   * {@link #getLastUncontrollableLocalEvent()}+1 to {@link
   * #getLastControllableLocalEvent()} inclusive.
   */
  public void setLastControllableLocalEvent(final int event)
  {
    mLastControllableLocalEvent = event;
  }

  /**
   * Gets the code of the last local controllable event.
   * @see #getLastControllableLocalEvent()
   */
  public int getLastControllableLocalEvent(){
      return mLastControllableLocalEvent;
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
    mTauClosure = null;
    mTauIterator = mEventIterator = null;
  }


  //#########################################################################
  //# Initial Partition
  /**
   * Sets up an initial partition for the bisimulation algorithm based on
   * the markings of states in the transition relation.
   * States with equal sets of markings are placed in the same class.
   * This method replaces any previously set initial partition.
   * This method is called by default during each {@link #run()} unless
   * the user provides an alternative initial partition.
   */
  public void setUpInitialPartitionBasedOnMarkings()
  throws OverflowException
  {
    final long mask = getPropositionMask();
    setUpInitialPartitionBasedOnMarkings(mask);
  }

  /**
   * Sets up an initial partition for the bisimulation algorithm based on
   * the markings of states in the transition relation.
   * States with equal sets of markings are placed in the same class.
   * This method replaces any previously set initial partition.
   * This method is called by default during each {@link #run()} unless
   * the user provides an alternative initial partition.
   * @param  mask   Marking pattern identifying the markings to be considered.
   *                Only markings in this pattern will be taken into account
   *                for the partition.
   */
  public void setUpInitialPartitionBasedOnMarkings(final long mask)
  throws OverflowException
  {
    final ListBufferTransitionRelation rel = getTransitionRelation();
    final int numStates = rel.getNumberOfStates();
    final long notMarked = rel.createMarkings();

    mHasModifications = false;
    setUpTauClosure();
    final TransitionIterator iter = mTauClosure.createIterator();
    for (int state = 0; state < numStates; state++) {
      if (rel.isReachable(state)) {
        final long marking = rel.getAllMarkings(state) & mask;
        if (marking != notMarked) {
          iter.resetState(state);
          while (iter.advance()) {
            final int pred = iter.getCurrentSourceState();
            if (rel.addMarkings(pred, marking)) {
              mHasModifications = true;
            }
          }
        }
      }
    }

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
  protected void setUp()
  throws AnalysisException
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
    if (mInitialInfoSize >= 0) {
      final int numTrans = rel.getNumberOfTransitions();
      if (mNumReachableStates > 0) {
        mInitialInfoSize = numTrans / mNumReachableStates;
      } else {
        mInitialInfoSize = 0;
      }
    }
    final int numStates = rel.getNumberOfStates();
    mTempClass = new TIntArrayList(numStates);
  }

  @Override
  protected boolean runSimplifier()
  throws AnalysisException
  {
    for (Splitter splitter = mSplitters.poll();
         splitter != null && mNumClasses < mNumReachableStates;
         splitter = mSplitters.poll()) {
      splitter.splitOn();
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
  }

  /**
   * Destructively applies the computed partitioning to the simplifier's
   * transition relation. This method merges any states found to be equivalent
   * during the last call to {@link #run()}, and depending on configuration,
   * performs a second pass to remove redundant transitions.
   * @see TransitionRemoval
   */
  @Override
  protected void applyResultPartition()
  throws AnalysisException
  {
    final ListBufferTransitionRelation rel = getTransitionRelation();
    if (getResultPartition() != null) {
      mTauClosure = null;
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
   // we have to do tauclosure here diffrently.
      if (mTauClosure == null) {
        final ListBufferTransitionRelation rel = getTransitionRelation();
        final int limit = getTransitionLimit();
        final int config = rel.getConfiguration();
        if ((config & ListBufferTransitionRelation.CONFIG_PREDECESSORS) != 0) {
          mTauClosure = rel.createPredecessorsTauClosure(limit);
        } else {
          mTauClosure = rel.createSuccessorsTauClosure(limit);
        }
        mTauIterator = mTauClosure.createIterator();
        if (mInitialInfoSize < 0) {
          mTauIterator = new OneEventCachingTransitionIterator
                               (mTauIterator, EventEncoding.TAU);
        }
        mEventIterator = mTauClosure.createFullEventClosureIterator(-1);
      }
    
  }

  private TransitionIterator getPredecessorIterator(final int event)
  {
    if (event == EventEncoding.TAU) {
      mTauIterator.reset();
      return mTauIterator;
    } else {
      mEventIterator.resetEvent(event);
      return mEventIterator;
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
          if (sec == null) {
            final int[] clazz = new int[1];
            clazz[0] = state;
            partition.add(clazz);
          } else {
            final int[] clazz = sec.putResult(state);
            if (clazz != null) {
              partition.add(clazz);
            }
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
    final ListBufferTransitionRelation rel = getTransitionRelation();
    final StringWriter writer = new StringWriter();
    final PrintWriter printer = new PrintWriter(writer);
    final Collection<EquivalenceClass> printed =
      new THashSet<EquivalenceClass>(mNumClasses);
    for (int s = 0; s < mStateToClass.length; s++) {
      final EquivalenceClass clazz = mStateToClass[s];
      if (clazz == null) {
        if (rel.isReachable(s)) {
          if (s > 0) {
            printer.println();
          }
          printer.print('[');
          printer.print(s);
          printer.print(']');
        }
      } else if (printed.add(clazz)) {
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
  private class EquivalenceClass
    implements Splitter
  {

    //#######################################################################
    //# Constructors
    private EquivalenceClass()
    {
      mSize = 0;
      mList = mClassLists.createList();
      mOverflowList = IntListBuffer.NULL;
      mOverflowSize = -1;
      mNumClasses++;
      mIsOpenSplitter = false;
    }

    private EquivalenceClass(final int list,
                             final int size,
                             final boolean preds)
    {
      mSize = size;
      mList = list;
      mOverflowList = IntListBuffer.NULL;
      mOverflowSize = preds ? 0 : -1;
      mNumClasses++;
      mIsOpenSplitter = false;
    }

    private EquivalenceClass(final int[] states)
    {
      mSize = states.length;
      mList = mClassLists.createList(states);
      mOverflowList = IntListBuffer.NULL;
      mOverflowSize = -1;
      mNumClasses++;
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
    }

    void setUpStateToClass()
    {
      if (mSize == 1) {
        reset(mClassReadIterator);
        mClassReadIterator.advance();
        final int state = mClassReadIterator.getCurrentData();
        mStateToClass[state] = null;
      } else {
        reset(mClassReadIterator);
        while (mClassReadIterator.advance()) {
          final int state = mClassReadIterator.getCurrentData();
          mStateToClass[state] = this;
        }
      }
    }

    void setPredecessorsInvalid()
    {
      mOverflowSize = -1;
    }

    //#######################################################################
    //# Simple Access
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
    public void splitOn()
    {
      mIsOpenSplitter = false;
      final ListBufferTransitionRelation rel = getTransitionRelation();
      final Collection<EquivalenceClass> splitClasses =
        new THashSet<EquivalenceClass>();
      collect(mTempClass);
      final int size = getSize();
      // get the first event in the encoding.
      final int first = EventEncoding.NONTAU;
      for (int event = first; event < mNumEvents; event++) {
        if (rel.isUsedEvent(event)) {
          final TransitionIterator transIter =
            getPredecessorIterator(event);
          for (int i = 0; i < size; i++) {
            final int state = mTempClass.get(i);
            transIter.resume(state);
            while (transIter.advance()) {
              final int pred = transIter.getCurrentSourceState();
              final EquivalenceClass splitClass = mStateToClass[pred];
              if (splitClass != null) {
                splitClass.moveToOverflowList(pred);
                splitClasses.add(splitClass);
              }
            }
          }
          for (final EquivalenceClass splitClass : splitClasses) {
            splitClass.splitUsingOverflowList();
          }
          splitClasses.clear();
        }
      }
      mTempClass.clear();
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
    void doSimpleSplit(final int overflowList,
                       final int overflowSize,
                       final boolean preds)
    {
      final int size = getSize();
      final int newSize = size - overflowSize;
      final EquivalenceClass overflowClass;
      if (newSize >= overflowSize) {
        overflowClass = new EquivalenceClass(overflowList, overflowSize, preds);
        setSize(newSize);
      } else {
        final int list = getList();
        overflowClass = new EquivalenceClass(list, newSize, preds);
        setList(overflowList, overflowSize);
      }
      if (getSize() == 1) {
        setUpStateToClass();
      }
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
    private int[] mArray;
    private boolean mIsOpenSplitter;
  }


  //#########################################################################
  //# Data Members
  private long mPropositionMask = ~0;
  private int mTransitionLimit = Integer.MAX_VALUE;
  private int mInitialInfoSize = -1;
  private int mLastControllableLocalEvent;
  private int mLastUncontrollableLocalEvent;

  private int mNumReachableStates;
  private int mNumEvents;
  private int mNumClasses;
  private boolean mHasModifications;

  private TauClosure mTauClosure;
  private TransitionIterator mTauIterator;
  private TransitionIterator mEventIterator;
  private IntListBuffer mClassLists;
  private IntListBuffer.ReadOnlyIterator mClassReadIterator;
  private IntListBuffer.ModifyingIterator mClassWriteIterator;
  private EquivalenceClass[] mStateToClass;
  private int[] mPredecessors;
  private Queue<Splitter> mSplitters;
  private TIntArrayList mTempClass;

}
