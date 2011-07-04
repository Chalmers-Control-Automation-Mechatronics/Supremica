//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters Analysis
//# PACKAGE: net.sourceforge.waters.analysis.op
//# CLASS:   ObservationEquivalenceTRSimplifier
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.analysis.op;

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
import java.util.NoSuchElementException;
import java.util.PriorityQueue;
import java.util.Queue;

import net.sourceforge.waters.model.analysis.AnalysisException;
import net.sourceforge.waters.model.analysis.OverflowException;
import net.sourceforge.waters.model.base.ProxyTools;
import net.sourceforge.waters.model.des.AutomatonTools;


/**
 * @author Simon Ware, Rachel Francis, Robi Malik
 */

public class AltObservationEquivalenceTRSimplifier
  extends AbstractObservationEquivalenceTRSimplifier
{

  //#########################################################################
  //# Constructors
  /**
   * Creates a new bisimulation simplifier without a transition relation.
   */
  public AltObservationEquivalenceTRSimplifier()
  {
    setInfoEnabled(false);
  }

  /**
   * Creates a new bisimulation simplifier for the given transition relation.
   */
  public AltObservationEquivalenceTRSimplifier
    (final ListBufferTransitionRelation rel)
  {
    this();
    if (rel != null) {
      setTransitionRelation(rel);
    }
  }


  //#########################################################################
  //# Configuration
  public void setInfoEnabled(final boolean enabled)
  {
    mInitialInfoSize = enabled ? 0 : -1;
  }

  public boolean getInfoEnabled()
  {
    return mInitialInfoSize >= 0;
  }


  //#########################################################################
  //# Interface net.sourceforge.waters.analysis.op.TransitionRelationSimplifier
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
    mStateShift = AutomatonTools.log2(mNumEvents);
  }

  @Override
  public boolean isObservationEquivalentAbstraction()
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
    mTauPreds = null;
  }


  //#########################################################################
  //# Initial Partition
  /**
   * Sets an initial partition for the bisimulation algorithm.
   * The partition is copied into the simplifiers data structures, which will
   * be modified destructively when calling {@link #run()}, so it needs to be
   * set again for a second run.
   * @param partition
   *          A collection of classes constituting the initial partition. Each
   *          array in the collection represents a class of equivalent state
   *          codes.
   */
  @Override
  public void setUpInitialPartition(final Collection<int[]> partition)
  {
    final int size = partition.size();
    setUpPartition(size);
    for (final int[] clazz : partition) {
      final EquivalenceClass sec = createEquivalenceClass(clazz);
      mSplitters.add(sec);
      if (clazz.length > 1) {
        for (final int state : clazz) {
          mStateToClass[state] = sec;
        }
      }
    }
    mHasModifications = false;
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
  @Override
  public void setUpInitialPartitionBasedOnMarkings(final long mask)
  throws OverflowException
  {
    final Equivalence equivalence = getEquivalence();
    final MarkingMode mmode = getMarkingMode();
    final ListBufferTransitionRelation rel = getTransitionRelation();
    final int numStates = rel.getNumberOfStates();
    final long[] markings;
    if (equivalence == Equivalence.BISIMULATION ||
        mmode == MarkingMode.SATURATE) {
      markings = null;
    } else {
      markings = new long[numStates];
      for (int state = 0; state < numStates; state++) {
        if (rel.isReachable(state)) {
          markings[state] = rel.getAllMarkings(state) & mask;
        }
      }
    }
    mHasModifications = false;
    if (equivalence != Equivalence.BISIMULATION) {
      setUpTauPredecessors();
      final TransitionIterator iter = mTauPreds.createIterator();
      for (int state = 0; state < numStates; state++) {
        if (rel.isReachable(state)) {
          final long marking;
          if (markings == null) {
            marking = rel.getAllMarkings(state) & mask;
          } else {
            marking = markings[state];
          }
          iter.resetState(state);
          while (iter.advance()) {
            final int pred = iter.getCurrentSourceState();
            if (pred != state) {
              switch (mmode) {
              case MINIMIZE:
                mHasModifications |= rel.removeMarkings(pred, marking);
                // fall through ...
              case UNCHANGED:
                markings[pred] = rel.mergeMarkings(marking, markings[pred]);
                break;
              case SATURATE:
                mHasModifications |= rel.addMarkings(pred, marking);
                break;
              }
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
        final long marking;
        if (markings == null) {
          marking = rel.getAllMarkings(state) & mask;
        } else {
          marking = markings[state];
        }
        EquivalenceClass clazz = prepartition.get(marking);
        if (clazz == null) {
          clazz = createEquivalenceClass();
          prepartition.put(marking, clazz);
          mSplitters.add(clazz);
        }
        clazz.addState(state);
      }
    }
    final TLongObjectIterator<EquivalenceClass> mapIter =
      prepartition.iterator();
    while (mapIter.hasNext()) {
      mapIter.advance();
      final EquivalenceClass sec = mapIter.value();
      sec.setUpStateToClass(false);
    }
  }

  /**
   * Refines the current partition using initial states.
   * This method splits all equivalence classes in the current partition
   * such that two states can only be equivalent if either both of them
   * are initial or none of them is initial. When using observation
   * equivalence, a state is also considered as initial if an initial
   * state is reachable via a sequence of tau transitions.
   *
   * (This method is intended to support non-alpha determinisation
   * and only makes sense when the input transition relation is reversed.)
   *
   * @see NonAlphaDeterminisationTRSimplifier
   */
  @Override
  public void refinePartitionBasedOnInitialStates()
  throws OverflowException
  {
    // Build set of initial states (includes states reachable via tau
    // transitions from an initial state) ...
    final ListBufferTransitionRelation rel = getTransitionRelation();
    final int numStates = rel.getNumberOfStates();
    final TIntHashSet initialStates;
    final TransitionIterator iter;
    if (getEquivalence() == Equivalence.BISIMULATION) {
      initialStates = null;
      iter = null;
    } else {
      initialStates = new TIntHashSet();
      setUpTauPredecessors();
      iter = mTauPreds.createIterator();
    }
    final Collection<EquivalenceClass> splitClasses =
      new THashSet<EquivalenceClass>();
    for (int state = 0; state < numStates; state++) {
      if (rel.isInitial(state)) {
        if (initialStates == null) {
          final EquivalenceClass splitClass = mStateToClass[state];
          if (splitClass != null) {
            splitClass.moveToOverflowList(state);
            splitClasses.add(splitClass);
          }
        } else {
          iter.resetState(state);
          while (iter.advance()) {
            final int pred = iter.getCurrentSourceState();
            if (initialStates.add(pred)) {
              final EquivalenceClass splitClass = mStateToClass[pred];
              if (splitClass != null) {
                splitClass.moveToOverflowList(pred);
                splitClasses.add(splitClass);
              }
            }
          }
        }
      }
    }

    // Try to split each equivalence class ...
    for (final EquivalenceClass splitClass : splitClasses) {
      splitClass.splitUsingOverflowList();
    }
  }


  //#########################################################################
  //# Overrides for net.sourceforge.waters.analysis.op.AbstractTRSimplifier
  @Override
  protected void setUp()
  throws AnalysisException
  {
    super.setUp();
    if (getEquivalence() == Equivalence.WEAK_OBSERVATION_EQUIVALENCE) {
      mFirstSplitEvent = EventEncoding.NONTAU;
    } else {
      mFirstSplitEvent = EventEncoding.TAU;
    }
    final boolean doTau;
    final boolean doNonTau;
    switch (getTransitionRemovalMode()) {
    case NONE:
    case AFTER:
    case AFTER_IF_CHANGED:
      doTau = doNonTau = false;
      break;
    case NONTAU:
      doTau = false;
      doNonTau = true;
      break;
    case ALL:
      doTau = doNonTau = true;
      break;
    default:
      throw new IllegalStateException("Unknown transition removal mode " +
                                      getTransitionRemovalMode() + "!");
    }
    if (mSplitters == null) {
      mHasModifications = false;
      removeRedundantTransitions(doTau, doNonTau);
      final boolean modified = mHasModifications;
      setUpInitialPartitionBasedOnMarkings();
      mHasModifications |= modified;
    } else {
      removeRedundantTransitions(doTau, doNonTau);
    }
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
    mMaxInfoSize = 0;
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
    super.applyResultPartition();
    mTauPreds = null;
    final boolean trivial = (getResultPartition() == null);
    final boolean doTau;
    final boolean doNonTau;
    switch (getTransitionRemovalMode()) {
    case NONE:
      doTau = doNonTau = false;
      break;
    case NONTAU:
      doTau = true;
      doNonTau = !trivial;
      break;
    case ALL:
    case AFTER_IF_CHANGED:
      doTau = doNonTau = !trivial;
      break;
    case AFTER:
      doTau = doNonTau = true;
      break;
    default:
      throw new IllegalStateException("Unknown transition removal mode " +
                                      getTransitionRemovalMode() + "!");
    }
    removeRedundantTransitions(doTau, doNonTau);
    final ListBufferTransitionRelation rel = getTransitionRelation();
    rel.removeTauSelfLoops();
    rel.removeProperSelfLoopEvents();
  }


  //#########################################################################
  //# Auxiliary Methods
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

  private EquivalenceClass createEquivalenceClass()
  {
    if (mInitialInfoSize >= 0) {
      return new LeafEquivalenceClass();
    } else {
      return new PlainEquivalenceClass();
    }
  }

  private EquivalenceClass createEquivalenceClass(final int[] states)
  {
    if (mInitialInfoSize >= 0) {
      return new LeafEquivalenceClass(states);
    } else {
      return new PlainEquivalenceClass(states);
    }
  }

  private void setUpTauPredecessors() throws OverflowException
  {
    if (mTauPreds == null && getEquivalence() != Equivalence.BISIMULATION) {
      final ListBufferTransitionRelation rel = getTransitionRelation();
      final int limit = getTransitionLimit();
      mTauPreds = rel.createPredecessorsTauClosure(limit);
    }
  }

  private void removeRedundantTransitions(final boolean doTau,
                                          final boolean doNonTau)
    throws OverflowException
  {
    if (doTau || doNonTau) {
      setUpTauPredecessors();
      final ListBufferTransitionRelation rel = getTransitionRelation();
      final int tau = EventEncoding.TAU;
      final TransitionRemoval mode = getTransitionRemovalMode();
      final TransitionIterator iter0 =
        rel.createAllTransitionsModifyingIterator();
      final TransitionIterator iter1 = mTauPreds.createIterator();
      final TransitionIterator iter2 =
        rel.createPredecessorsReadOnlyIterator();
      final TransitionIterator iter3 = mTauPreds.createIterator();
      trans:
      while (iter0.advance()) {
        final int e = iter0.getCurrentEvent();
        if (!rel.isUsedEvent(e)) {
          continue;
        } else if (e == tau && mode == TransitionRemoval.NONTAU) {
          continue;
        }
        final int source0 = iter0.getCurrentSourceState();
        final int target0 = iter0.getCurrentTargetState();
        iter1.resetState(target0);
        while (iter1.advance()) {
          final int p1 = iter1.getCurrentSourceState();
          iter2.reset(p1, e);
          while (iter2.advance()) {
            final int p2 = iter2.getCurrentSourceState();
            if (e == tau) {
              if (doTau && p1 != target0 && p2 == source0) {
                iter0.remove();
                mHasModifications = true;
                continue trans;
              }
            } else {
              if (doNonTau && (p1 != target0 || p2 != source0)) {
                iter3.resetState(p2);
                while (iter3.advance()) {
                  final int p3 = iter3.getCurrentSourceState();
                  if (p3 == source0) {
                    iter0.remove();
                    mHasModifications = true;
                    continue trans;
                  }
                }
              }
            }
          }
        }
      }
    }
  }

  private TransitionIterator getPredecessorIterator(final int event)
  {
    if (getEquivalence() == Equivalence.BISIMULATION) {
      final ListBufferTransitionRelation rel = getTransitionRelation();
      return rel.createPredecessorsReadOnlyIterator(event);
    } else if (event == EventEncoding.TAU) {
      return mTauPreds.createIterator();
    } else {
      return new ProperEventClosureTransitionIterator(event);
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
    final StringWriter writer = new StringWriter();
    final PrintWriter printer = new PrintWriter(writer);
    final Collection<EquivalenceClass> printed =
      new THashSet<EquivalenceClass>(mNumClasses);
    for (int s = 0; s < mStateToClass.length; s++) {
      final EquivalenceClass clazz = mStateToClass[s];
      if (clazz == null) {
        if (s > 0) {
          printer.println();
        }
        printer.print('[');
        printer.print(s);
        printer.print(']');
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

    public ComplexEquivalenceClass getParent();

    public void setParent(ComplexEquivalenceClass parent);

    public InfoMap getInfo();

    public void setInfo(InfoMap info);

    public int getSize();

    public void collect(TIntArrayList states);

    public void splitOn();

    public void enqueue();

    public void dump(PrintWriter printer);

  }


  //#########################################################################
  //# Inner Class EquivalenceClass
  private abstract class EquivalenceClass
    implements Splitter
  {

    //#######################################################################
    //# Constructors
    EquivalenceClass()
    {
      mSize = 0;
      mList = mClassLists.createList();
      mOverflowList = IntListBuffer.NULL;
      mOverflowSize = -1;
      mNumClasses++;
    }

    EquivalenceClass(final int list,
                     final int size,
                     final boolean preds)
    {
      mSize = size;
      mList = list;
      mOverflowList = IntListBuffer.NULL;
      mOverflowSize = preds ? 0 : -1;
      mNumClasses++;
    }

    EquivalenceClass(final int[] states)
    {
      mSize = states.length;
      mList = mClassLists.createList(states);
      mOverflowList = IntListBuffer.NULL;
      mOverflowSize = -1;
      mNumClasses++;
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

    void setUpStateToClass(final boolean force)
    {
      if (mSize == 1) {
        if (force) {
          reset(mClassReadIterator);
          assert mClassReadIterator.advance();
          final int state = mClassReadIterator.getCurrentData();
          mStateToClass[state] = null;
        }
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

    void setUpPredecessors()
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
    //# Splitting
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

    abstract void doSimpleSplit(int overflowList,
                                int overflowSize,
                                boolean preds);

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

  }


  //#########################################################################
  //# Inner Class PlainEquivalenceClass
  private class PlainEquivalenceClass
    extends EquivalenceClass
  {

    //#######################################################################
    //# Constructors
    private PlainEquivalenceClass()
    {
      mIsOpenSplitter = false;
    }

    private PlainEquivalenceClass(final int list,
                                  final int size,
                                  final boolean preds)
    {
      super(list, size, preds);
      mIsOpenSplitter = false;
    }

    private PlainEquivalenceClass(final int[] states)
    {
      super(states);
    }

    //#######################################################################
    //# Interface Splitter
    public ComplexEquivalenceClass getParent()
    {
      return null;
    }

    public void setParent(final ComplexEquivalenceClass parent)
    {
    }

    public InfoMap getInfo()
    {
      return null;
    }

    public void setInfo(final InfoMap info)
    {
    }

    public void splitOn()
    {
      mIsOpenSplitter = false;
      final ListBufferTransitionRelation rel = getTransitionRelation();
      final Collection<EquivalenceClass> splitClasses =
        new THashSet<EquivalenceClass>();
      final TIntHashSet visited = new TIntHashSet();
      collect(mTempClass);
      final int size = getSize();
      for (int event = mFirstSplitEvent; event < mNumEvents; event++) {
        if (rel.isUsedEvent(event)) {
          final TransitionIterator transIter = getPredecessorIterator(event);
          for (int i = 0; i < size; i++) {
            final int state = mTempClass.get(i);
            transIter.resetState(state);
            while (transIter.advance()) {
              final int pred = transIter.getCurrentSourceState();
              if (visited.add(pred)) {
                final EquivalenceClass splitClass = mStateToClass[pred];
                if (splitClass != null) {
                  splitClass.moveToOverflowList(pred);
                  splitClasses.add(splitClass);
                }
              }
            }
          }
        }
        visited.clear();
        for (final EquivalenceClass splitClass : splitClasses) {
          splitClass.splitUsingOverflowList();
        }
        splitClasses.clear();
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
      final PlainEquivalenceClass overflowClass;
      if (newSize >= overflowSize) {
        overflowClass =
          new PlainEquivalenceClass(overflowList, overflowSize, preds);
        setSize(newSize);
      } else {
        final int list = getList();
        overflowClass = new PlainEquivalenceClass(list, newSize, preds);
        setList(overflowList, overflowSize);
      }
      if (getSize() == 1) {
        setUpStateToClass(true);
      }
      overflowClass.setUpStateToClass(true);
      overflowClass.enqueue();
      enqueue();
    }

    //#######################################################################
    //# Data Members
    private boolean mIsOpenSplitter;

  }


  //#########################################################################
  //# Inner Class LeafEquivalenceClass
  private class LeafEquivalenceClass extends EquivalenceClass
  {

    //#######################################################################
    //# Constructors
    private LeafEquivalenceClass()
    {
    }

    private LeafEquivalenceClass(final int list,
                                 final int size,
                                 final boolean preds)
    {
      super(list, size, preds);
    }

    private LeafEquivalenceClass(final int[] states)
    {
      super(states);
    }

    //#######################################################################
    //# Interface java.util.Comparable<Splitter>
    public int compareTo(final Splitter splitter)
    {
      if (mInfo == null) {
        if (splitter.getInfo() != null) {
          return 1;
        }
      } else {
        if (splitter.getInfo() == null) {
          return -1;
        }
      }
      return super.compareTo(splitter);
    }

    //#######################################################################
    //# Interface Splitter
    public ComplexEquivalenceClass getParent()
    {
      return mParent;
    }

    public void setParent(final ComplexEquivalenceClass parent)
    {
      mParent = parent;
    }

    public InfoMap getInfo()
    {
      return mInfo;
    }

    public void setInfo(final InfoMap info)
    {
      mInfo = info;
    }

    public void splitOn()
    {
      final ListBufferTransitionRelation rel = getTransitionRelation();
      final InfoMap info = new InfoMap();
      setInfo(info);
      final Collection<EquivalenceClass> splitClasses =
        new THashSet<EquivalenceClass>();
      final TIntHashSet visited = new TIntHashSet();
      collect(mTempClass);
      final int size = getSize();
      for (int event = mFirstSplitEvent; event < mNumEvents; event++) {
        if (rel.isUsedEvent(event)) {
          final TransitionIterator transIter = getPredecessorIterator(event);
          for (int i = 0; i < size; i++) {
            final int state = mTempClass.get(i);
            transIter.resetState(state);
            while (transIter.advance()) {
              final int pred = transIter.getCurrentSourceState();
              if (visited.add(pred)) {
                final EquivalenceClass splitClass = mStateToClass[pred];
                if (splitClass != null) {
                  splitClass.moveToOverflowList(pred);
                  splitClasses.add(splitClass);
                }
              }
              info.increment(pred, event);
            }
          }
        }
        visited.clear();
        for (final EquivalenceClass splitClass : splitClasses) {
          splitClass.splitUsingOverflowList();
        }
        splitClasses.clear();
      }
      mTempClass.clear();
      mMaxInfoSize = Math.max(mMaxInfoSize, info.size());
    }

    public void enqueue()
    {
      mParent = null;
    }

    //#######################################################################
    //# Overrides for EquivalenceClass
    @Override
    void doSimpleSplit(final int overflowList,
                       final int overflowSize,
                       final boolean preds)
    {
      final int size = getSize();
      final int newSize = size - overflowSize;
      final LeafEquivalenceClass overflowClass;
      if (newSize >= overflowSize) {
        overflowClass =
          new LeafEquivalenceClass(overflowList, overflowSize, preds);
        setSize(newSize);
      } else {
        final int list = getList();
        overflowClass = new LeafEquivalenceClass(list, newSize, preds);
        setList(overflowList, overflowSize);
      }
      if (getSize() == 1) {
        setUpStateToClass(true);
      }
      overflowClass.setUpStateToClass(true);
      final ComplexEquivalenceClass parent = getParent();
      if (parent != null) {
        final ComplexEquivalenceClass complex =
          new ComplexEquivalenceClass(overflowClass, this, parent, mInfo);
        parent.replaceChild(this, complex);
        mInfo = null;
      } else if (mInfo == null) {
        mSplitters.add(overflowClass);
      } else {
        final ComplexEquivalenceClass complex =
          new ComplexEquivalenceClass(overflowClass, this, mInfo);
        mSplitters.add(complex);
        mInfo = null;
      }
    }


    //#######################################################################
    //# Specific Methods
    void doComplexSplit(int overflow1, int size1, int overflow2, int size2)
    {
      if (size1 == 0 && size2 == 0) {
        return;
      }
      final int size = getSize();
      setPredecessorsInvalid();
      if (size1 == size) {
        setList(overflow1, size1);
        return;
      } else if (size2 == size) {
        setList(overflow2, size2);
        return;
      } else if (size1 == 0) {
        doSimpleSplit(overflow2, size2, false);
        return;
      } else if (size2 == 0) {
        doSimpleSplit(overflow1, size1, false);
        return;
      }
      final int newSize = size - size1 - size2;
      if (newSize == 0) {
        setList(overflow1, size);
        doSimpleSplit(overflow2, size2, false);
        return;
      }

      // OK, it really is a three-way split ...
      // Establish order: overflow1 < overflow2 < this
      if (size2 < size1) {
        int tmp = size1;
        size1 = size2;
        size2 = tmp;
        tmp = overflow1;
        overflow1 = overflow2;
        overflow2 = tmp;
      }
      if (newSize < size2) {
        int tmp = overflow2;
        overflow2 = getList();
        setList(tmp, size2);
        size2 = newSize;
        if (size2 < size1) {
          tmp = size1;
          size1 = size2;
          size2 = tmp;
          tmp = overflow1;
          overflow1 = overflow2;
          overflow2 = tmp;
        }
      } else {
        setSize(newSize);
      }
      if (getSize() == 1) {
        setUpStateToClass(true);
      }

      // Create and enqueue complex splitters ...
      final LeafEquivalenceClass class1 =
        new LeafEquivalenceClass(overflow1, size1, false);
      class1.setUpStateToClass(true);
      final LeafEquivalenceClass class2 =
        new LeafEquivalenceClass(overflow2, size2, false);
      class2.setUpStateToClass(true);
      final ComplexEquivalenceClass parent = getParent();
      if (parent != null || mInfo != null) {
        final ComplexEquivalenceClass complex2 =
          new ComplexEquivalenceClass(class2, this);
        final ComplexEquivalenceClass complex1 =
          new ComplexEquivalenceClass(class1, complex2, parent, mInfo);
        mInfo = null;
        complex2.setParent(complex1);
        if (parent == null) {
          mSplitters.add(complex1);
        } else {
          parent.replaceChild(this, complex1);
        }
      } else {
        mSplitters.add(class1);
        mSplitters.add(class2);
      }
    }

    //#######################################################################
    //# Data Members
    private ComplexEquivalenceClass mParent;
    private InfoMap mInfo;

  }


  // #########################################################################
  // # Inner Class ComplexEquivalenceClass
  private class ComplexEquivalenceClass implements Splitter
  {

    //#######################################################################
    //# Constructors
    ComplexEquivalenceClass(final Splitter little,
                            final Splitter big)
    {
      mSize = little.getSize() + big.getSize();
      mLittleChild = little;
      mBigChild = big;
      mLittleChild.setParent(this);
      mBigChild.setParent(this);
    }

    ComplexEquivalenceClass(final Splitter little,
                            final Splitter big,
                            final InfoMap info)
    {
      mSize = little.getSize() + big.getSize();
      mInfo = info;
      mLittleChild = little;
      mBigChild = big;
      mLittleChild.setParent(this);
      mBigChild.setParent(this);
    }

    ComplexEquivalenceClass(final Splitter little,
                            final Splitter big,
                            final ComplexEquivalenceClass parent,
                            final InfoMap info)
    {
      mSize = little.getSize() + big.getSize();
      mParent = parent;
      mInfo = info;
      mLittleChild = little;
      mBigChild = big;
      mLittleChild.setParent(this);
      mBigChild.setParent(this);
    }

    //#######################################################################
    //# Interface java.util.Comparable<Splitter>
    public int compareTo(final Splitter splitter)
    {
      if (mInfo == null) {
        if (splitter.getInfo() != null) {
          return 1;
        }
      } else {
        if (splitter.getInfo() == null) {
          return -1;
        }
      }
      return mSize - splitter.getSize();
    }

    //#######################################################################
    //# Interface Splitter
    public int getSize()
    {
      return mSize;
    }

    public ComplexEquivalenceClass getParent()
    {
      return mParent;
    }

    public void setParent(final ComplexEquivalenceClass parent)
    {
      mParent = parent;
    }

    public InfoMap getInfo()
    {
      return mInfo;
    }

    public void setInfo(final InfoMap info)
    {
      mInfo = info;
    }

    public void collect(final TIntArrayList states)
    {
      mLittleChild.collect(states);
      mBigChild.collect(states);
    }

    public void splitOn()
    {
      final ListBufferTransitionRelation rel = getTransitionRelation();
      mLittleChild.collect(mTempClass);
      final int tempSize = mTempClass.size();
      final InfoMap info = getInfo();
      final int infoSize = info.size();
      final InfoMap littleInfo = new InfoMap(infoSize);
      mLittleChild.setInfo(littleInfo);
      mBigChild.setInfo(info);
      final Collection<LeafEquivalenceClass> splitClasses =
        new THashSet<LeafEquivalenceClass>();
      final TIntHashSet visited = new TIntHashSet();
      for (int event = mFirstSplitEvent; event < mNumEvents; event++) {
        if (rel.isUsedEvent(event)) {
          final TransitionIterator transIter = getPredecessorIterator(event);
          for (int i = 0; i < tempSize; i++) {
            final int state = mTempClass.get(i);
            transIter.resetState(state);
            while (transIter.advance()) {
              final int pred = transIter.getCurrentSourceState();
              if (visited.add(pred)) {
                final EquivalenceClass splitClass = mStateToClass[pred];
                if (splitClass != null) {
                  final LeafEquivalenceClass leaf = (LeafEquivalenceClass) splitClass;
                  splitClasses.add(leaf);
                }
              }
              info.moveTo(littleInfo, pred, event);
            }
          }
          visited.clear();
          for (final LeafEquivalenceClass splitClass : splitClasses) {
            int size1 = 0;
            int size2 = 0;
            int overflow1 = IntListBuffer.NULL;
            int overflow2 = IntListBuffer.NULL;
            splitClass.reset(mClassWriteIterator);
            while (mClassWriteIterator.advance()) {
              final int state = mClassWriteIterator.getCurrentData();
              final SplitKind kind =
                info.getSplitKind(littleInfo, state, event);
              switch (kind) {
              case BOTH:
                if (size1++ == 0) {
                  overflow1 = mClassLists.createList();
                }
                mClassWriteIterator.moveTo(overflow1);
                break;
              case LITTLE:
                if (size2++ == 0) {
                  overflow2 = mClassLists.createList();
                }
                mClassWriteIterator.moveTo(overflow2);
                break;
              default:
                break;
              }
            }
            splitClass.doComplexSplit(overflow1, size1, overflow2, size2);
          }
          splitClasses.clear();
        }
      }
      mTempClass.clear();
      mLittleChild.enqueue();
      mBigChild.enqueue();
      mMaxInfoSize = Math.max(mMaxInfoSize, info.size());
      mMaxInfoSize = Math.max(mMaxInfoSize, littleInfo.size());
    }

    public void enqueue()
    {
      mParent = null;
      mSplitters.add(this);
    }


    //#######################################################################
    //# Simple Access
    void replaceChild(final EquivalenceClass oldChild,
                      final Splitter newChild)
    {
      if (mLittleChild == oldChild) {
        mLittleChild = newChild;
      } else {
        mBigChild = newChild;
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
      printer.write('<');
      mLittleChild.dump(printer);
      printer.write(", ");
      mBigChild.dump(printer);
      printer.write('>');
    }

    //#######################################################################
    //# Data Members
    private final int mSize;
    private ComplexEquivalenceClass mParent;
    private InfoMap mInfo;
    private Splitter mLittleChild;
    private Splitter mBigChild;

  }


  //#########################################################################
  //# Inner Class HashInfoMap
  private class InfoMap
    extends TIntIntHashMap
  {

    //#######################################################################
    //# Constructors
    private InfoMap()
    {
      this(mInitialInfoSize);
    }

    private InfoMap(final int initialSize)
    {
      super(initialSize);
    }

    //#######################################################################
    //# Interface InfoMap
    public int increment(final int state, final int event)
    {
      final int key = (state << mStateShift) | event;
      return adjustOrPutValue(key, 1, 1);
    }

    public void moveTo(final InfoMap little, final int state, final int event)
    {
      final int key = (state << mStateShift) | event;
      if (adjustOrPutValue(key, -1, 0) == 0) {
        remove(key);
      }
      little.adjustOrPutValue(key, 1, 1);
    }

    public SplitKind getSplitKind(final InfoMap little,
                                  final int state,
                                  final int event)
    {
      final int key = (state << mStateShift) | event;
      if (get(key) == 0) {
        return SplitKind.LITTLE;
      } else if (little.get(key) == 0) {
        return SplitKind.BIG;
      } else {
        return SplitKind.BOTH;
      }
    }

    //#######################################################################
    //# Debugging
    @Override
    public String toString()
    {
      final StringWriter writer = new StringWriter();
      final PrintWriter printer = new PrintWriter(writer);
      final int eventMask = (1 << mStateShift) - 1;
      final int[] keyArray = keys();
      Arrays.sort(keyArray);
      for (final int key : keyArray) {
        final int state = key >>> mStateShift;
        final int event = key & eventMask;
        final int info = get(key);
        printer.print("info(");
        printer.print(state);
        printer.print(',');
        printer.print(event);
        printer.print(")=");
        printer.println(info);
      }
      return writer.toString();
    }

  }


  //#########################################################################
  //# Inner Class ProperEventClosureTransitionIterator
  private class ProperEventClosureTransitionIterator implements
      TransitionIterator
  {

    //#######################################################################
    //# Constructor
    private ProperEventClosureTransitionIterator()
    {
      final ListBufferTransitionRelation rel = getTransitionRelation();
      mTauIterator1 = mTauPreds.createIterator();
      mInnerIterator = rel.createPredecessorsReadOnlyIterator();
      mTauIterator2 = mTauPreds.createIterator();
      mVisited = new TIntHashSet();
    }

    private ProperEventClosureTransitionIterator(final int event)
    {
      final ListBufferTransitionRelation rel = getTransitionRelation();
      mTauIterator1 = mTauPreds.createIterator();
      mInnerIterator = rel.createPredecessorsReadOnlyIterator();
      mTauIterator2 = mTauPreds.createIterator();
      mVisited = new TIntHashSet();
      mEvent = event;
    }

    //#######################################################################
    //# Interface net.sourceforge.waters.analysis.op.TransitionIterator
    public void reset()
    {
      mTauIterator1.resetState(mTarget);
      mInnerIterator.reset(mTarget, mEvent);
      mVisited.clear();
      mStart = true;
    }

    public void resetEvent(final int event)
    {
      mEvent = event;
      reset();
    }

    public void resetState(final int from)
    {
      mTarget = from;
      reset();
    }

    public void reset(final int from, final int event)
    {
      mEvent = event;
      resetState(from);
    }

    public boolean advance()
    {
      while (seek()) {
        final int state = mTauIterator2.getCurrentSourceState();
        if (mVisited.add(state)) {
          return true;
        }
      }
      mStart = true;
      return false;
    }

    public int getCurrentEvent()
    {
      return mEvent;
    }

    public int getCurrentSourceState()
    {
      return getCurrentToState();
    }

    public int getCurrentToState()
    {
      if (mStart) {
        throw new NoSuchElementException("Reading past end of list in " +
                                         ProxyTools.getShortClassName(this));
      } else {
        return mTauIterator2.getCurrentSourceState();
      }
    }

    public int getCurrentTargetState()
    {
      return mTarget;
    }

    public int getCurrentFromState()
    {
      return mTarget;
    }

    public void remove()
    {
      throw new UnsupportedOperationException
        (ProxyTools.getShortClassName(this) +
         " does not support transition removal!");
    }

    //#######################################################################
    //# Auxiliary Methods
    private boolean seek()
    {
      if (mStart) {
        mStart = false;
        mTauIterator1.advance(); // always succeeds and gives mTarget
        if (mInnerIterator.advance()) {
          final int pred = mInnerIterator.getCurrentSourceState();
          mTauIterator2.resetState(pred);
          return mTauIterator2.advance(); // always true
        } else {
          return false;
        }
      } else if (mTauIterator2.advance()) {
        return true;
      } else if (mInnerIterator.advance()) {
        final int pred = mInnerIterator.getCurrentSourceState();
        mTauIterator2.resetState(pred);
        return mTauIterator2.advance(); // always true
      } else {
        while (mTauIterator1.advance()) {
          int pred = mTauIterator1.getCurrentSourceState();
          mInnerIterator.resetState(pred);
          if (mInnerIterator.advance()) {
            pred = mInnerIterator.getCurrentSourceState();
            mTauIterator2.resetState(pred);
            return mTauIterator2.advance(); // always true
          }
        }
        return false;
      }
    }

    // #########################################################################
    // # Data Members
    private int mTarget;
    private int mEvent;
    private boolean mStart;

    private final TransitionIterator mTauIterator1;
    private final TransitionIterator mInnerIterator;
    private final TransitionIterator mTauIterator2;
    private final TIntHashSet mVisited;

  }


  //#########################################################################
  //# Inner Enumeration SplitKind
  private enum SplitKind
  {
    LITTLE,
    BIG,
    BOTH
  }


  //#########################################################################
  //# Data Members
  private int mNumReachableStates;
  private int mNumEvents;
  private int mInitialInfoSize;
  private int mNumClasses;
  private int mStateShift;
  private int mFirstSplitEvent;
  private boolean mHasModifications;

  private TauClosure mTauPreds;
  private IntListBuffer mClassLists;
  private IntListBuffer.ReadOnlyIterator mClassReadIterator;
  private IntListBuffer.ModifyingIterator mClassWriteIterator;
  private EquivalenceClass[] mStateToClass;
  private int[] mPredecessors;
  private Queue<Splitter> mSplitters;
  private TIntArrayList mTempClass;

  private int mMaxInfoSize;

}
