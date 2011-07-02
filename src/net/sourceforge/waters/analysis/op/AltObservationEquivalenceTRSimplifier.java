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
    this(null);
  }

  /**
   * Creates a new bisimulation simplifier for the given transition relation.
   */
  public AltObservationEquivalenceTRSimplifier
    (final ListBufferTransitionRelation rel)
  {
    super(null);
    if (rel != null) {
      setTransitionRelation(rel);
    }
    setInfoEnabled(false);
  }


  //#########################################################################
  //# Configuration
  public void setInfoEnabled(final boolean enabled)
  {
    if (enabled) {
      mNoInfo = null;
    } else if (mNoInfo == null) {
      mNoInfo = new NoInfoMap();
    }
  }

  public boolean getInfoEnabled()
  {
    return mNoInfo == null;
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
    final int numTrans = rel.getNumberOfTransitions();
    if (mNumReachableStates > 0) {
      mInitialInfoSize = numTrans / mNumReachableStates;
    } else {
      mInitialInfoSize = 0;
    }
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
  public void setUpInitialPartition(final Collection<int[]> partition)
  {
    final int size = partition.size();
    setUpPartition(size);
    for (final int[] clazz : partition) {
      final SimpleEquivalenceClass sec = new SimpleEquivalenceClass(clazz);
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
   */
  public void setUpInitialPartitionBasedOnMarkings()
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
          markings[state] = rel.getAllMarkings(state);
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
            marking = rel.getAllMarkings(state);
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
    final TLongObjectHashMap<SimpleEquivalenceClass> prepartition =
      new TLongObjectHashMap<SimpleEquivalenceClass>();
    for (int state = 0; state < numStates; state++) {
      if (rel.isReachable(state)) {
        final long marking;
        if (markings == null) {
          marking = rel.getAllMarkings(state);
        } else {
          marking = markings[state];
        }
        SimpleEquivalenceClass sec = prepartition.get(marking);
        if (sec == null) {
          sec = new SimpleEquivalenceClass();
          prepartition.put(marking, sec);
          mSplitters.add(sec);
        }
        sec.addState(state);
      }
    }
    final TLongObjectIterator<SimpleEquivalenceClass> mapIter =
      prepartition.iterator();
    while (mapIter.hasNext()) {
      mapIter.advance();
      final SimpleEquivalenceClass sec = mapIter.value();
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
    final Collection<SimpleEquivalenceClass> splitClasses =
      new THashSet<SimpleEquivalenceClass>();
    for (int state = 0; state < numStates; state++) {
      if (rel.isInitial(state)) {
        if (initialStates == null) {
          final SimpleEquivalenceClass splitClass = mStateToClass[state];
          if (splitClass != null) {
            splitClass.moveToOverflowList(state);
            splitClasses.add(splitClass);
          }
        } else {
          iter.resetState(state);
          while (iter.advance()) {
            final int pred = iter.getCurrentSourceState();
            if (initialStates.add(pred)) {
              final SimpleEquivalenceClass splitClass = mStateToClass[pred];
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
    for (final SimpleEquivalenceClass splitClass : splitClasses) {
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
    final TransitionRemoval mode = getTransitionRemovalMode();
    final boolean doTau = mode == TransitionRemoval.ALL;
    final boolean doNonTau = doTau || mode == TransitionRemoval.NONTAU;
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
    final int numStates = rel.getNumberOfStates();
    mTempClass = new TIntArrayList(numStates);
    mMaxInfoSize = 0;
  }

  @Override
  protected boolean runSimplifier()
  throws AnalysisException
  {
    for (EquivalenceClass splitter = mSplitters.poll();
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
    final boolean doTau;
    final boolean doNonTau;
    final TransitionRemoval mode = getTransitionRemovalMode();
    if (getResultPartition() != null) {
      doTau = doNonTau = mode != TransitionRemoval.NONE;
    } else {
      doNonTau = mode == TransitionRemoval.AFTER;
      doTau = doNonTau || mode == TransitionRemoval.NONTAU;
    }
    if (doTau || doNonTau) {
      removeRedundantTransitions(doTau, doNonTau);
    }
    final ListBufferTransitionRelation rel = getTransitionRelation();
    rel.removeTauSelfLoops();
    rel.removeProperSelfLoopEvents();
  }


  //#########################################################################
  //# Auxiliary Methods
  private void setUpPartition(final int numSplitters)
  {
    mNumClasses = 0;
    mClassLists = new IntListBuffer();
    mClassReadIterator = mClassLists.createReadOnlyIterator();
    mClassWriteIterator = mClassLists.createModifyingIterator();
    final ListBufferTransitionRelation rel = getTransitionRelation();
    final int numStates = rel.getNumberOfStates();
    mStateToClass = new SimpleEquivalenceClass[numStates];
    mPredecessors = new int[numStates];
    mSplitters = new PriorityQueue<EquivalenceClass>(numSplitters);
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
        if (e == tau && mode == TransitionRemoval.NONTAU) {
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
          final SimpleEquivalenceClass sec = mStateToClass[state];
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
    final Collection<SimpleEquivalenceClass> printed =
      new THashSet<SimpleEquivalenceClass>(mNumClasses);
    for (int s = 0; s < mStateToClass.length; s++) {
      final SimpleEquivalenceClass clazz = mStateToClass[s];
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
  //# Inner Class EquivalenceClass
  private abstract class EquivalenceClass
    implements Comparable<EquivalenceClass>
  {

    //#######################################################################
    //# Constructors
    EquivalenceClass(final int size)
    {
      mSize = size;
    }

    EquivalenceClass(final InfoMap info, final int size)
    {
      mInfo = info;
      mSize = size;
    }

    EquivalenceClass(final ComplexEquivalenceClass parent,
                     final InfoMap info,
                     final int size)
    {
      mParent = parent;
      mInfo = info;
      mSize = size;
    }

    //#######################################################################
    //# Simple Access
    ComplexEquivalenceClass getParent()
    {
      return mParent;
    }

    InfoMap getInfo()
    {
      return mInfo;
    }

    void setParent(final ComplexEquivalenceClass parent)
    {
      mParent = parent;
    }

    InfoMap createInfo()
    {
      if (mNoInfo != null) {
        mInfo = mNoInfo;
      } else {
        mInfo = new HashInfoMap();
      }
      return mInfo;
    }

    void setInfo(final InfoMap info)
    {
      mInfo = info;
    }

    int getSize()
    {
      return mSize;
    }

    void setSize(final int size)
    {
      mSize = size;
    }

    void incSize()
    {
      mSize++;
    }

    //#######################################################################
    //# Interface java.util.Comparable<EquivalenceClass>
    public int compareTo(final EquivalenceClass clazz)
    {
      if (mInfo == null) {
        if (clazz.mInfo != null) {
          return 1;
        }
      } else {
        if (clazz.mInfo == null) {
          return -1;
        }
      }
      return mSize - clazz.mSize;
    }

    //#######################################################################
    //# To be Overridden
    abstract void splitOn();

    abstract void collect(TIntArrayList states);

    void enqueue()
    {
      mParent = null;
    }

    //#######################################################################
    //# Data Members
    @Override
    public String toString()
    {
      final StringWriter writer = new StringWriter();
      final PrintWriter printer = new PrintWriter(writer);
      dump(printer);
      return writer.toString();
    }

    abstract void dump(PrintWriter printer);

    //#######################################################################
    //# Data Members
    private ComplexEquivalenceClass mParent;
    private InfoMap mInfo;
    private int mSize;

  }


  //#########################################################################
  //# Inner Class SimpleEquivalenceClass
  private class SimpleEquivalenceClass extends EquivalenceClass
  {

    //#######################################################################
    //# Constructors
    private SimpleEquivalenceClass()
    {
      super(0);
      mList = mClassLists.createList();
      mOverflowList = IntListBuffer.NULL;
      mOverflowSize = -1;
      mNumClasses++;
    }

    private SimpleEquivalenceClass(final int list,
                                   final int size,
                                   final boolean preds)
    {
      super(size);
      mList = list;
      mOverflowList = IntListBuffer.NULL;
      mOverflowSize = preds ? 0 : -1;
      mNumClasses++;
    }

    private SimpleEquivalenceClass(final int[] states)
    {
      super(states.length);
      mList = mClassLists.createList(states);
      mOverflowList = IntListBuffer.NULL;
      mOverflowSize = -1;
      mNumClasses++;
    }

    //#######################################################################
    //# Initialisation
    void addState(final int state)
    {
      mClassLists.append(mList, state);
      incSize();
    }

    void setUpStateToClass(final boolean force)
    {
      if (getSize() == 1) {
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

    //#######################################################################
    //# Simple Access
    void reset(final IntListBuffer.Iterator iter)
    {
      iter.reset(mList);
    }

    //#######################################################################
    //# Algorithm
    @Override
    void splitOn()
    {
      final InfoMap info = createInfo();
      final Collection<SimpleEquivalenceClass> splitClasses =
        new THashSet<SimpleEquivalenceClass>();
      final TIntHashSet visited = new TIntHashSet();
      collect(mTempClass);
      final int size = getSize();
      for (int event = mFirstSplitEvent; event < mNumEvents; event++) {
        final TransitionIterator transIter = getPredecessorIterator(event);
        for (int i = 0; i < size; i++) {
          final int state = mTempClass.get(i);
          transIter.resetState(state);
          while (transIter.advance()) {
            final int pred = transIter.getCurrentSourceState();
            if (visited.add(pred)) {
              final SimpleEquivalenceClass splitClass = mStateToClass[pred];
              if (splitClass != null) {
                splitClass.moveToOverflowList(pred);
                splitClasses.add(splitClass);
              }
            }
            info.increment(pred, event);
          }
        }
        visited.clear();
        for (final SimpleEquivalenceClass splitClass : splitClasses) {
          splitClass.splitUsingOverflowList();
        }
        splitClasses.clear();
      }
      mTempClass.clear();
      mMaxInfoSize = Math.max(mMaxInfoSize, info.size());
    }

    @Override
    void collect(final TIntArrayList states)
    {
      reset(mClassReadIterator);
      while (mClassReadIterator.advance()) {
        final int state = mClassReadIterator.getCurrentData();
        states.add(state);
      }
    }

    void doSimpleSplit(final int overflowList, final int overflowSize)
    {
      final int size = getSize();
      final int newSize = size - overflowSize;
      final boolean preds = mOverflowSize >= 0;
      final SimpleEquivalenceClass overflowClass;
      if (newSize >= overflowSize) {
        overflowClass =
          new SimpleEquivalenceClass(overflowList, overflowSize, preds);
        setSize(newSize);
      } else {
        overflowClass = new SimpleEquivalenceClass(mList, newSize, preds);
        mList = overflowList;
        setSize(overflowSize);
      }
      if (getSize() == 1) {
        setUpStateToClass(true);
      }
      overflowClass.setUpStateToClass(true);
      final ComplexEquivalenceClass parent = getParent();
      final InfoMap info = getInfo();
      if (parent != null) {
        setInfo(null);
        final ComplexEquivalenceClass complex =
          new ComplexEquivalenceClass(overflowClass, this, parent, info);
        parent.replaceChild(this, complex);
      } else if (info == null) {
        mSplitters.add(overflowClass);
      } else if (info == mNoInfo) {
        setInfo(null);
        mSplitters.add(this);
        mSplitters.add(overflowClass);
      } else {
        setInfo(null);
        final ComplexEquivalenceClass complex =
          new ComplexEquivalenceClass(overflowClass, this, info);
        mSplitters.add(complex);
      }
    }

    void doComplexSplit(int overflow1, int size1, int overflow2, int size2)
    {
      if (size1 == 0 && size2 == 0) {
        return;
      }
      final int size = getSize();
      mOverflowSize = -1;
      if (size1 == size) {
        mList = overflow1;
        return;
      } else if (size2 == size) {
        mList = overflow2;
        return;
      } else if (size1 == 0) {
        doSimpleSplit(overflow2, size2);
        return;
      } else if (size2 == 0) {
        doSimpleSplit(overflow1, size1);
        return;
      }
      final int newSize =size - size1 - size2;
      if (newSize == 0) {
        mList = overflow1;
        doSimpleSplit(overflow2, size2);
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
        setSize(size2);
        size2 = newSize;
        int tmp = overflow2;
        overflow2 = mList;
        mList = tmp;
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
      final SimpleEquivalenceClass class1 =
        new SimpleEquivalenceClass(overflow1, size1, false);
      class1.setUpStateToClass(true);
      final SimpleEquivalenceClass class2 =
        new SimpleEquivalenceClass(overflow2, size2, false);
      class2.setUpStateToClass(true);
      final InfoMap info = getInfo();
      final ComplexEquivalenceClass parent = getParent();
      if (parent != null || info != null) {
        setInfo(null);
        final ComplexEquivalenceClass complex2 =
          new ComplexEquivalenceClass(class2, this);
        final ComplexEquivalenceClass complex1 =
          new ComplexEquivalenceClass(class1, complex2, parent, info);
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

    private void moveToOverflowList(final int state)
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

    private void splitUsingOverflowList()
    {
      if (mOverflowSize <= 0) {
        return;
      } else if (mOverflowSize == getSize()) {
        mList = mOverflowList;
      } else {
        doSimpleSplit(mOverflowList, mOverflowSize);
      }
      mOverflowSize = 0;
      mOverflowList = IntListBuffer.NULL;
    }

    //#######################################################################
    //# Debugging
    @Override
    void dump(final PrintWriter printer)
    {
      mClassLists.dumpList(printer, mList);
    }

    //#######################################################################
    //# Data Members
    int mList;
    int mOverflowList;
    int mOverflowSize;
    int[] mArray;

  }


  // #########################################################################
  // # Inner Class ComplexEquivalenceClass
  private class ComplexEquivalenceClass extends EquivalenceClass
  {

    //#######################################################################
    //# Constructors
    ComplexEquivalenceClass(final EquivalenceClass little,
                            final EquivalenceClass big)
    {
      super(little.getSize() + big.getSize());
      mLittleChild = little;
      mBigChild = big;
      mLittleChild.setParent(this);
      mBigChild.setParent(this);
    }

    ComplexEquivalenceClass(final EquivalenceClass little,
                            final EquivalenceClass big,
                            final InfoMap info)
    {
      super(info, little.getSize() + big.getSize());
      mLittleChild = little;
      mBigChild = big;
      mLittleChild.setParent(this);
      mBigChild.setParent(this);
    }

    ComplexEquivalenceClass(final EquivalenceClass little,
                            final EquivalenceClass big,
                            final ComplexEquivalenceClass parent,
                            final InfoMap info)
    {
      super(parent, info, little.getSize() + big.getSize());
      mLittleChild = little;
      mBigChild = big;
      mLittleChild.setParent(this);
      mBigChild.setParent(this);
    }

    //#######################################################################
    //# Simple Access
    void replaceChild(final EquivalenceClass oldChild,
                      final EquivalenceClass newChild)
    {
      if (mLittleChild == oldChild) {
        mLittleChild = newChild;
      } else {
        mBigChild = newChild;
      }

    }

    //#######################################################################
    //# Algorithm
    @Override
    void splitOn()
    {
      mLittleChild.collect(mTempClass);
      final int tempSize = mTempClass.size();
      final InfoMap info = getInfo();
      final int infoSize = info.size();
      final InfoMap littleInfo = new HashInfoMap(infoSize);
      mLittleChild.setInfo(littleInfo);
      mBigChild.setInfo(info);
      final Collection<SimpleEquivalenceClass> splitClasses =
        new THashSet<SimpleEquivalenceClass>();
      final TIntHashSet visited = new TIntHashSet();
      for (int event = mFirstSplitEvent; event < mNumEvents; event++) {
        final TransitionIterator transIter = getPredecessorIterator(event);
        for (int i = 0; i < tempSize; i++) {
          final int state = mTempClass.get(i);
          transIter.resetState(state);
          while (transIter.advance()) {
            final int pred = transIter.getCurrentSourceState();
            if (visited.add(pred)) {
              final SimpleEquivalenceClass splitClass = mStateToClass[pred];
              if (splitClass != null) {
                splitClasses.add(splitClass);
              }
            }
            info.moveTo(littleInfo, pred, event);
          }
        }
        visited.clear();
        for (final SimpleEquivalenceClass splitClass : splitClasses) {
          int size1 = 0;
          int size2 = 0;
          int overflow1 = IntListBuffer.NULL;
          int overflow2 = IntListBuffer.NULL;
          splitClass.reset(mClassWriteIterator);
          while (mClassWriteIterator.advance()) {
            final int state = mClassWriteIterator.getCurrentData();
            final SplitKind kind = info.getSplitKind(littleInfo, state, event);
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
      mTempClass.clear();
      mLittleChild.enqueue();
      mBigChild.enqueue();
      mMaxInfoSize = Math.max(mMaxInfoSize, info.size());
      mMaxInfoSize = Math.max(mMaxInfoSize, littleInfo.size());
    }

    @Override
    void collect(final TIntArrayList states)
    {
      mLittleChild.collect(states);
      mBigChild.collect(states);
    }

    @Override
    void enqueue()
    {
      super.enqueue();
      mSplitters.add(this);
    }

    //#######################################################################
    //# Debugging
    void dump(final PrintWriter printer)
    {
      printer.write('<');
      mLittleChild.dump(printer);
      printer.write(", ");
      mBigChild.dump(printer);
      printer.write('>');
    }

    //#######################################################################
    //# Data Members
    private EquivalenceClass mLittleChild;
    private EquivalenceClass mBigChild;

  }


  //#########################################################################
  //# Inner Interface InfoMap
  private interface InfoMap {

    public int increment(final int state, final int event);

    public void moveTo(final InfoMap little, final int state, final int event);

    public SplitKind getSplitKind(final InfoMap little,
                                  final int state,
                                  final int event);

    public int size();

    public int adjustOrPutValue(int key, int adjust, int init);

    public int get(int key);

  }


  //#########################################################################
  //# Inner Class NoInfoMap
  private class NoInfoMap
    implements InfoMap
  {

    //#######################################################################
    //# Interface InfoMap
    public SplitKind getSplitKind(final InfoMap little,
                                  final int state,
                                  final int event)
    {
      return null;
    }

    public int increment(final int state, final int event)
    {
      return 0;
    }

    public void moveTo(final InfoMap little, final int state, final int event)
    {
    }

    public int size()
    {
      return 0;
    }

    public int adjustOrPutValue(final int key, final int adjust, final int init)
    {
      return 0;
    }

    public int get(final int key)
    {
      return 0;
    }

  }


  //#########################################################################
  //# Inner Class HashInfoMap
  private class HashInfoMap
    extends TIntIntHashMap
    implements InfoMap
  {

    //#######################################################################
    //# Constructors
    private HashInfoMap()
    {
      this(mInitialInfoSize);
    }

    private HashInfoMap(final int initialSize)
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
  private SimpleEquivalenceClass[] mStateToClass;
  private int[] mPredecessors;
  private Queue<EquivalenceClass> mSplitters;
  private TIntArrayList mTempClass;
  private InfoMap mNoInfo;

  private int mMaxInfoSize;

}
