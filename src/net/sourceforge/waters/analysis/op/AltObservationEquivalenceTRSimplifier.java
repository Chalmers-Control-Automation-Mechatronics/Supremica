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
import gnu.trove.TIntIntProcedure;
import gnu.trove.TLongObjectHashMap;
import gnu.trove.TLongObjectIterator;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Queue;
import java.util.Set;

import net.sourceforge.waters.model.analysis.AnalysisException;
import net.sourceforge.waters.model.analysis.OverflowException;
import net.sourceforge.waters.model.base.ProxyTools;


/**
 * @author Simon Ware, Rachel Francis, Robi Malik
 */

public class AltObservationEquivalenceTRSimplifier
  extends AbstractTRSimplifier
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
    super(rel);
    if (rel != null) {
      mNumEvents = rel.getNumberOfProperEvents();
    }
    mEquivalence = Equivalence.OBSERVATION_EQUIVALENCE;
    mTransitionRemovalMode = TransitionRemoval.NONTAU;
    mMarkingMode = MarkingMode.UNCHANGED;
    mTransitionLimit = Integer.MAX_VALUE;
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
    mNumEvents = rel.getNumberOfProperEvents();
    mTauPreds = null;
    mClassLists = null;
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
    mClassLists = null;
    mStateToClass2 = null;
  }


  //#########################################################################
  //# Configuration
  /**
   * Sets the equivalence by which the transition relation is partitioned.
   * @see Equivalence
   */
  public void setEquivalence(final Equivalence mode)
  {
    mEquivalence = mode;
    if (mode == Equivalence.BISIMULATION) {
      mTransitionRemovalMode = TransitionRemoval.NONE;
    }
  }

  /**
   * Gets the equivalence by which the transition relation is partitioned.
   * @see Equivalence
   */
  public Equivalence getEquivalence()
  {
    return mEquivalence;
  }

  /**
   * Sets the mode which redundant transitions are to be removed.
   * @see TransitionRemoval
   */
  public void setTransitionRemovalMode(final TransitionRemoval mode)
  {
    mTransitionRemovalMode = mode;
  }

  /**
   * Gets the mode which redundant transitions are to be removed.
   * @see TransitionRemoval
   */
  public TransitionRemoval getTransitionRemovalMode()
  {
    return mTransitionRemovalMode;
  }

  /**
   * Sets the mode how implicit markings are handled.
   * @see MarkingMode
   */
  public void setMarkingMode(final MarkingMode mode)
  {
    mMarkingMode = mode;
  }

  /**
   * Gets the mode how implicit markings are handled.
   * @see MarkingMode
   */
  public MarkingMode getMarkingMode()
  {
    return mMarkingMode;
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
  public void setupInitialPartition(final Collection<int[]> partition)
  {
    final ListBufferTransitionRelation rel = getTransitionRelation();
    final int numStates = rel.getNumberOfStates();
    mStateToClass2 = new SimpleEquivalenceClass2[numStates];
    final int size = partition.size();
    mSimpleSplitters = new ArrayDeque<SimpleEquivalenceClass2>(size);
    for (final int[] clazz : partition) {
      final SimpleEquivalenceClass2 sec = new SimpleEquivalenceClass2(clazz);
      mSimpleSplitters.add(sec);
      if (clazz.length > 1) {
        for (final int state : clazz) {
          mStateToClass2[state] = sec;
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
  public void setupInitialPartitionBasedOnMarkings()
  throws OverflowException
  {
    final ListBufferTransitionRelation rel = getTransitionRelation();
    final int numStates = rel.getNumberOfStates();
    final long[] markings;
    if (mEquivalence == Equivalence.BISIMULATION ||
        mMarkingMode == MarkingMode.SATURATE) {
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
    if (mEquivalence != Equivalence.BISIMULATION) {
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
              switch (mMarkingMode) {
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
    mSimpleSplitters = new ArrayDeque<SimpleEquivalenceClass2>(size);
    final TLongObjectHashMap<SimpleEquivalenceClass2> prepartition =
      new TLongObjectHashMap<SimpleEquivalenceClass2>();
    for (int state = 0; state < numStates; state++) {
      if (rel.isReachable(state)) {
        final long marking;
        if (markings == null) {
          marking = rel.getAllMarkings(state);
        } else {
          marking = markings[state];
        }
        SimpleEquivalenceClass2 sec = prepartition.get(marking);
        if (sec == null) {
          sec = new SimpleEquivalenceClass2();
          prepartition.put(marking, sec);
          mSimpleSplitters.add(sec);
        }
        sec.addState(state);
      }
    }
    mStateToClass2 = new SimpleEquivalenceClass2[numStates];
    final IntListBuffer.Iterator listIter =
      mClassLists.createReadOnlyIterator();
    final TLongObjectIterator<SimpleEquivalenceClass2> mapIter =
      prepartition.iterator();
    while (mapIter.hasNext()) {
      mapIter.advance();
      final SimpleEquivalenceClass2 sec = mapIter.value();
      if (sec.getSize() > 1) {
        sec.reset(listIter);
        while (listIter.advance()) {
          final int state = listIter.getCurrentData();
          mStateToClass2[state] = sec;
        }
      }
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
    if (mEquivalence == Equivalence.BISIMULATION) {
      initialStates = null;
      iter = null;
    } else {
      initialStates = new TIntHashSet();
      setUpTauPredecessors();
      iter = mTauPreds.createIterator();
    }
    final Set<SimpleEquivalenceClass2> splitClasses =
      new THashSet<SimpleEquivalenceClass2>();
    for (int state = 0; state < numStates; state++) {
      if (rel.isInitial(state)) {
        if (initialStates == null) {
          final SimpleEquivalenceClass2 sec = mStateToClass2[state];
          if (sec != null && sec.getSize() > 1) {
            splitClasses.add(sec);
          }
        } else {
          iter.resetState(state);
          while (iter.advance()) {
            final int pred = iter.getCurrentSourceState();
            initialStates.add(pred);
            final SimpleEquivalenceClass2 sec = mStateToClass2[state];
            if (sec != null && sec.getSize() > 1) {
              splitClasses.add(sec);
            }
          }
        }
      }
    }

    // Try to split each equivalence class ...
    final IntListBuffer.ModifyingIterator writeIter =
      mClassLists.createModifyingIterator();
    final IntListBuffer.ReadOnlyIterator readIter =
      mClassLists.createReadOnlyIterator();
    for (final SimpleEquivalenceClass2 sec : splitClasses) {
      int alt = IntListBuffer.NULL;
      int initialCount = 0;
      sec.reset(writeIter);
      while (writeIter.advance()) {
        final int state = writeIter.getCurrentData();
        final boolean init;
        if (initialStates == null) {
          init = rel.isInitial(state);
        } else {
          init = initialStates.contains(state);
        }
        if (init) {
          initialCount++;
          if (alt == IntListBuffer.NULL) {
            alt = mClassLists.createList();
          }
          writeIter.moveTo(alt);
        }
      }
      final int size = sec.getSize();
      if (initialCount == size) {
        sec.setList(alt);
      } else {
        sec.setSize(size - initialCount);
        final SimpleEquivalenceClass2 extra =
          new SimpleEquivalenceClass2(alt, initialCount);
        mSimpleSplitters.add(extra);
        if (sec.getSize() == 1) {
          sec.reset(readIter);
          readIter.advance();
          final int state = readIter.getCurrentData();
          mStateToClass2[state] = null;
        }
        final SimpleEquivalenceClass2 entry =
          initialCount == 1 ? null : extra;
        extra.reset(readIter);
        while (readIter.advance()) {
          final int state = readIter.getCurrentData();
          mStateToClass2[state] = entry;
        }
      }
    }
  }


  //#########################################################################
  //# Overrides for net.sourceforge.waters.analysis.op.AbstractTRSimplifier
  @Override
  protected void setUp()
  throws AnalysisException
  {
    super.setUp();
    if (mEquivalence == Equivalence.WEAK_OBSERVATION_EQUIVALENCE) {
      mFirstSplitEvent = EventEncoding.NONTAU;
    } else {
      mFirstSplitEvent = EventEncoding.TAU;
    }
    final boolean doTau = mTransitionRemovalMode == TransitionRemoval.ALL;
    final boolean doNonTau =
      doTau || mTransitionRemovalMode == TransitionRemoval.NONTAU;
    if (mSimpleSplitters == null) {
      mHasModifications = false;
      removeRedundantTransitions(doTau, doNonTau);
      final boolean modified = mHasModifications;
      setupInitialPartitionBasedOnMarkings();
      mHasModifications |= modified;
    } else {
      removeRedundantTransitions(doTau, doNonTau);
    }
  }

  @Override
  protected boolean runSimplifier()
  throws AnalysisException
  {
    for (SimpleEquivalenceClass2 splitter = mSimpleSplitters.poll();
         splitter != null;
         splitter = mSimpleSplitters.poll()) {
      splitter.splitOn();
    }


    while (true) {
      checkAbort();
      final Iterator<? extends EquivalenceClass> it;
      if (!mWS.isEmpty()) {
        it = mWS.iterator();
      } else if (!mWC.isEmpty()) {
        it = mWC.iterator();
      } else {
        break;
      }
      final EquivalenceClass ec = it.next();
      it.remove();
      ec.splitOn();
    }
    final ListBufferTransitionRelation rel = getTransitionRelation();
    final int numStates = rel.getNumberOfStates();
    final int numClasses = mP.size();
    assert numClasses >= 0;
    assert numClasses <= numStates;
    mHasModifications |= numClasses != numStates;
    if (!mHasModifications) {
      return false;
    }
    buildResultPartition();
    applyResultPartitionAutomatically();
    return true;
  }

  @Override
  protected void tearDown()
  {
    mSimpleSplitters = null;
    mWS = null;
    mWC = null;
    mP = null;
    mStateToClass = null;
    super.tearDown();
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
    if (getResultPartition() != null) {
      doTau = doNonTau = mTransitionRemovalMode != TransitionRemoval.NONE;
    } else {
      doNonTau = mTransitionRemovalMode == TransitionRemoval.AFTER;
      doTau = doNonTau || mTransitionRemovalMode == TransitionRemoval.NONTAU;
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
  private void setUpTauPredecessors() throws OverflowException
  {
    if (mTauPreds == null && mEquivalence != Equivalence.BISIMULATION) {
      final ListBufferTransitionRelation rel = getTransitionRelation();
      mTauPreds = rel.createPredecessorsTauClosure(mTransitionLimit);
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
      final TransitionIterator iter0 =
        rel.createAllTransitionsModifyingIterator();
      final TransitionIterator iter1 = mTauPreds.createIterator();
      final TransitionIterator iter2 =
        rel.createPredecessorsReadOnlyIterator();
      final TransitionIterator iter3 = mTauPreds.createIterator();
      trans:
      while (iter0.advance()) {
        final int e = iter0.getCurrentEvent();
        if (e == tau && mTransitionRemovalMode == TransitionRemoval.NONTAU) {
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
    if (mEquivalence == Equivalence.BISIMULATION) {
      final ListBufferTransitionRelation rel = getTransitionRelation();
      return rel.createPredecessorsReadOnlyIterator(event);
    } else if (event == EventEncoding.TAU) {
      return mTauPreds.createIterator();
    } else {
      return new ProperEventClosureTransitionIterator(event);
    }
  }

  private void addToW(final SimpleEquivalenceClass sec, final int[] X1,
                      final int[] X2)
  {
    final SimpleEquivalenceClass child1 = new SimpleEquivalenceClass(X1);
    final SimpleEquivalenceClass child2 = new SimpleEquivalenceClass(X2);
    mP.remove(sec);
    if (mWS.remove(sec)) {
      mWS.add(child1);
      mWS.add(child2);
    } else {
      final ComplexEquivalenceClass comp =
          new ComplexEquivalenceClass(child1, child2, sec);
      if (sec.getParent() != null) {
        comp.setParent(sec.getParent());
        final ComplexEquivalenceClass p = sec.getParent();
        p.mChild1 = p.mChild1 == sec ? comp : p.mChild1;
        p.mChild2 = p.mChild2 == sec ? comp : p.mChild2;
      } else {
        mWC.add(comp);
      }
    }
  }

  private void addToW(final SimpleEquivalenceClass sec, int[] X1, int[] X2,
                      int[] X3)
  {
    if (X2.length < X1.length) {
      final int[] t = X1;
      X1 = X2;
      X2 = t;
    }
    if (X3.length < X1.length) {
      final int[] t = X1;
      X1 = X3;
      X3 = t;
    }
    final SimpleEquivalenceClass child1 = new SimpleEquivalenceClass(X1);
    final SimpleEquivalenceClass child2 = new SimpleEquivalenceClass(X2);
    final SimpleEquivalenceClass child3 = new SimpleEquivalenceClass(X3);
    mP.remove(sec);
    final ComplexEquivalenceClass X23 =
        new ComplexEquivalenceClass(child2, child3);
    final ComplexEquivalenceClass X123 =
        new ComplexEquivalenceClass(child1, X23, sec);
    if (sec.getParent() != null) {
      X123.setParent(sec.getParent());
      final ComplexEquivalenceClass p = sec.getParent();
      p.mChild1 = p.mChild1 == sec ? X123 : p.mChild1;
      p.mChild2 = p.mChild2 == sec ? X123 : p.mChild2;
    } else {
      mWC.add(X123);
    }
  }

  private void buildResultPartition()
  {
    final ListBufferTransitionRelation rel = getTransitionRelation();
    final int numStates = rel.getNumberOfStates();
    final int numClasses = mP.size();
    if (numClasses == numStates) {
      setResultPartitionList(null);
    } else {
      int nextCode = 0;
      final int[] index = new int[numClasses];
      final List<int[]> partition = new ArrayList<int[]>(numClasses);
      for (int state = 0; state < numStates; state++) {
        if (rel.isReachable(state)) {
          final SimpleEquivalenceClass sec = mStateToClass[state];
          int code = sec.getCode();
          final int[] clazz;
          if (code < 0) {
            code = nextCode++;
            sec.setCode(code);
            clazz = new int[sec.getSize()];
            partition.add(clazz);
          } else {
            clazz = partition.get(code);
          }
          final int i = index[code]++;
          clazz[i] = state;
        }
      }
      setResultPartitionList(partition);
    }
  }


  //#########################################################################
  //# Inner Class EquivalenceClass
  private abstract class EquivalenceClass
  {

    //#######################################################################
    //# Constructors
    EquivalenceClass()
    {
    }

    EquivalenceClass(final EquivalenceClass src)
    {
      if (src != null) {
        mInfo = src.mInfo;
      }
    }

    //#######################################################################
    //# Simple Access
    ComplexEquivalenceClass getParent()
    {
      return mParent;
    }

    void setParent(final ComplexEquivalenceClass parent)
    {
      mParent = parent;
    }

    TIntIntHashMap[] createInfo()
    {
      return mInfo = new TIntIntHashMap[mNumEvents];
    }

    void setInfo(final int event, final TIntIntHashMap info)
    {
      mInfo[event] = info;
    }

    void clearInfo()
    {
      mInfo = null;
    }

    //#######################################################################
    //# To be Overridden
    abstract int getSize();

    abstract void splitOn();

    TIntIntHashMap getInfo(final int event)
    {
      if (mInfo == null) {
        createInfo();
        return null;
      } else {
        return mInfo[event];
      }
    }

    //#######################################################################
    //# Data Members
    private ComplexEquivalenceClass mParent = null;
    private TIntIntHashMap[] mInfo = null;

  }


  //#########################################################################
  //# Inner Class SimpleEquivalenceClass
  private class SimpleEquivalenceClass2
  {

    //#######################################################################
    //# Constructors
    private SimpleEquivalenceClass2()
    {
      mList = mClassLists.createList();
      mSize = 0;
    }

    private SimpleEquivalenceClass2(final int list, final int size)
    {
      mList = list;
      mSize = size;
    }

    private SimpleEquivalenceClass2(final SimpleEquivalenceClass2 split)
    {
      mList = split.mOverflowList;
      mSize = split.mOverflowSize;
    }

    private SimpleEquivalenceClass2(final int[] states)
    {
      mList = mClassLists.createList(states);
      mSize = states.length;
    }

    //#######################################################################
    //# Initialisation
    private void addState(final int state)
    {
      mClassLists.append(mList, state);
      mSize++;
    }

    //#######################################################################
    //# Simple Access
    private int getSize()
    {
      return mSize;
    }

    private void reset(final IntListBuffer.Iterator iter)
    {
      iter.reset(mList);
    }

    private void setSize(final int size)
    {
      mSize = size;
    }

    private void setList(final int list)
    {
      mList = list;
    }

    //#######################################################################
    //# Algorithm
    private void splitOn()
    {
      /*
      final TIntIntHashMap info = new TIntIntHashMap();
      final List<SimpleEquivalenceClass2> splitClasses =
          new ArrayList<SimpleEquivalenceClass2>();
      final IntListBuffer.ModifyingIterator listIter =
        mClassLists.createModifyingIterator();
      for (int e = mFirstSplitEvent; e < mNumEvents; e++) {
        final TransitionIterator transIter = getPredecessorIterator(e);
        reset(listIter);
        while (listIter.advance()) {
          final int target = listIter.getCurrentData();
          transIter.resetState(target);
          while (transIter.advance()) {
            final int pred = transIter.getCurrentSourceState();
            final SimpleEquivalenceClass2 splitClass = mStateToClass2[pred];
            if (splitClass != null) {
              final int overflow = splitClass.createOverflowList();
              listIter.moveTo(overflow);
              info.increment(pred, e);
              splitClasses.add(splitClass);
            }
          }
        }
        for (final SimpleEquivalenceClass2 splitClass : splitClasses) {
          final int osize = splitClass.mOverflowSize;
          if (splitClass.mOverflowSize == splitClass.mSize) {
            splitClass.mList = splitClass.mOverflowList;
          } else {
            final SimpleEquivalenceClass2 newClass =
              new SimpleEquivalenceClass2(splitClass);
            final int[] X1 = new int[splitClass.getSize() - splitClass.mSplit1.size()];
            final int[] X2 = new int[splitClass.mSplit1.size()];
            int x1 = 0, x2 = 0;
            for (int s = 0; s < splitClass.mStates.length; s++) {
              final int state = splitClass.mStates[s];
              if (splitClass.mSplit1.contains(state)) {
                X2[x2] = state;
                x2++;
              } else {
                X1[x1] = state;
                x1++;
              }
            }
            addToW(splitClass, X1, X2);
          }
          splitClass.mOverflowList = IntListBuffer.NULL;
          splitClass.mOverflowSize = 0;
        }
        splitClasses.clear();
      }
      */
    }

    @SuppressWarnings("unused")
    private int createOverflowList()
    {
      mOverflowSize++;
      if (mOverflowList == IntListBuffer.NULL) {
        mOverflowList = mClassLists.createList();
      }
      return mOverflowList;
    }

    //#######################################################################
    //# Data Members
    int mList;
    int mSize;
    int mOverflowList;
    int mOverflowSize;

  }


  private class SimpleEquivalenceClass extends EquivalenceClass
  {

    //#######################################################################
    //# Constructor
    public SimpleEquivalenceClass(final int[] states)
    {
      mStates = states;
      mCode = -1;
      for (int i = 0; i < states.length; i++) {
        mStateToClass[states[i]] = this;
      }
      mP.add(this);
    }

    //#######################################################################
    //# Simple Access
    private int getCode()
    {
      return mCode;
    }

    private void setCode(final int code)
    {
      mCode = code;
    }

    //#######################################################################
    //# Overrides for EquivalenceClass
    @Override
    int getSize()
    {
      return mStates.length;
    }

    // TODO maybe keep track of what events an equivalence class has no
    // incoming events from
    @Override
    void splitOn()
    {
      final int size = getSize();
      final TIntIntHashMap[] info = createInfo();
      final List<SimpleEquivalenceClass> classes =
          new ArrayList<SimpleEquivalenceClass>();
      for (int e = mFirstSplitEvent; e < mNumEvents; e++) {
        final TransitionIterator iter = getPredecessorIterator(e);
        final TIntIntHashMap map = new TIntIntHashMap();
        info[e] = map;
        for (int s = 0; s < size; s++) {
          final int target = mStates[s];
          iter.resetState(target);
          while (iter.advance()) {
            final int pred = iter.getCurrentSourceState();
            final SimpleEquivalenceClass ec = mStateToClass[pred];
            TIntHashSet split = ec.mSplit1;
            if (split == null) {
              split = new TIntHashSet(ec.getSize());
              ec.mSplit1 = split;
              classes.add(ec);
            }
            split.add(pred);
            map.adjustOrPutValue(pred, 1, 1);
          }
        }
        for (final SimpleEquivalenceClass sec : classes) {
          if (sec.mSplit1.size() != sec.getSize()) {
            final int[] X1 = new int[sec.getSize() - sec.mSplit1.size()];
            final int[] X2 = new int[sec.mSplit1.size()];
            int x1 = 0, x2 = 0;
            for (int s = 0; s < sec.mStates.length; s++) {
              final int state = sec.mStates[s];
              if (sec.mSplit1.contains(state)) {
                X2[x2] = state;
                x2++;
              } else {
                X1[x1] = state;
                x1++;
              }
            }
            addToW(sec, X1, X2);
          }
          sec.mSplit1 = null;
        }
        classes.clear();
      }
    }

    @Override
    public TIntIntHashMap getInfo(final int event)
    {
      TIntIntHashMap info = super.getInfo(event);
      if (info == null) {
        info = new TIntIntHashMap();
        setInfo(event, info);
        final TransitionIterator iter = getPredecessorIterator(event);
        for (int s = 0; s < mStates.length; s++) {
          final int state = mStates[s];
          iter.resetState(state);
          while (iter.advance()) {
            final int pred = iter.getCurrentSourceState();
            info.adjustOrPutValue(pred, 1, 1);
          }
        }
      }
      return info;
    }


    //#######################################################################
    //# Data Members
    private final int[] mStates;
    private int mCode;
    private TIntHashSet mSplit1 = null;
    private TIntArrayList mX1 = null;
    private TIntArrayList mX2 = null;
    private TIntArrayList mX3 = null;
    private boolean mSplit = false;

  }


  // #########################################################################
  // # Inner Class ComplexEquivalenceClass
  private class ComplexEquivalenceClass extends EquivalenceClass
  {

    //#######################################################################
    //# Constructors
    ComplexEquivalenceClass(final EquivalenceClass child1,
                            final EquivalenceClass child2)
    {
      this(child1, child2, null);
    }

    public ComplexEquivalenceClass(final EquivalenceClass child1,
                                   final EquivalenceClass child2,
                                   final EquivalenceClass src)
    {
      super(src);
      final int size1 = child1.getSize();
      final int size2 = child2.getSize();
      mSize = size1 + size2;
      if (size1 < size2) {
        mChild1 = child1;
        mChild2 = child2;
      } else {
        mChild2 = child1;
        mChild1 = child2;
      }
      mChild1.setParent(this);
      mChild2.setParent(this);
    }

    //#######################################################################
    //# Overrides for EquivalenceClass
    @Override
    int getSize()
    {
      return mSize;
    }

    @Override
    void splitOn()
    {
      final ArrayList<SimpleEquivalenceClass> classes =
          new ArrayList<SimpleEquivalenceClass>();
      mChild2.mInfo = new TIntIntHashMap[mNumEvents];
      for (int e = 0; e < mNumEvents; e++) {
        final TIntIntHashMap info = getInfo(e);
        final TIntIntHashMap process = new TIntIntHashMap();
        final TIntIntHashMap info1 = mChild1.getInfo(e);
        info.forEachEntry(new TIntIntProcedure() {
          public boolean execute(final int state, int value)
          {
            if (value == 0) {
              System.out.println("zero value split");
              info.remove(state);
              return true;
            }
            final int value1 = info1.get(state);
            final SimpleEquivalenceClass sec = mStateToClass[state];
            if (!sec.mSplit) {
              classes.add(sec);
              sec.mSplit = true;
            }
            if (value == value1) {
              TIntArrayList X1 = sec.mX1;
              if (X1 == null) {
                X1 = new TIntArrayList();
                sec.mX1 = X1;
              }
              X1.add(state);
            } else if (value1 == 0) {
              TIntArrayList X2 = sec.mX2;
              if (X2 == null) {
                X2 = new TIntArrayList();
                sec.mX2 = X2;
              }
              X2.add(state);
            } else {
              TIntArrayList X3 = sec.mX3;
              if (X3 == null) {
                X3 = new TIntArrayList();
                sec.mX3 = X3;
              }
              X3.add(state);
            }
            value -= value1;
            if (value != 0) {
              process.put(state, value);
            }
            return true;
          }
        });
        mChild2.mInfo[e] = process;
        for (int c = 0; c < classes.size(); c++) {
          final SimpleEquivalenceClass sec = classes.get(c);
          int[] X1, X2, X3;
          int number = sec.mX1 != null ? 1 : 0;
          number = sec.mX2 != null ? number + 1 : number;
          number = sec.mX3 != null ? number + 1 : number;
          /*
           * System.out.println("number:" + number); System.out.println("X1:" +
           * (sec.X1 != null) + " X2:" + (sec.X2 != null) + " X3:" + (sec.X3 !=
           * null)); X1 = sec.X1 == null ? null : sec.X1.toNativeArray(); X2 =
           * sec.X2 == null ? null : sec.X2.toNativeArray(); X3 = sec.X3 == null
           * ? null : sec.X3.toNativeArray(); System.out.println("X1:" +
           * Arrays.toString(X1)); System.out.println("X2:" +
           * Arrays.toString(X2)); System.out.println("X3:" +
           * Arrays.toString(X3)); System.out.println("X:" +
           * Arrays.toString(sec.mStates));
           */
          if (number == 2) {
            X1 = sec.mX1 == null ? null : sec.mX1.toNativeArray();
            X2 = sec.mX2 == null ? null : sec.mX2.toNativeArray();
            X3 = sec.mX3 == null ? null : sec.mX3.toNativeArray();
            if (X1 == null) {
              X1 = X3;
            } else if (X2 == null) {
              X2 = X3;
            }
            addToW(sec, X1, X2);
          } else if (number == 3) {
            X1 = sec.mX1.toNativeArray();
            X2 = sec.mX2.toNativeArray();
            X3 = sec.mX3.toNativeArray();
            sec.mSplit = false;
            addToW(sec, X1, X2, X3);
          }
          sec.mX1 = null;
          sec.mX2 = null;
          sec.mX3 = null;
          sec.mSplit = false;
        }
        classes.clear();
      }
      // mChild2.mInfo = mInfo;
      clearInfo();
      mChild1.setParent(null);
      mChild2.setParent(null);
      if (mChild1 instanceof ComplexEquivalenceClass) {
        mWC.add((ComplexEquivalenceClass) mChild1);
      }
      if (mChild2 instanceof ComplexEquivalenceClass) {
        mWC.add((ComplexEquivalenceClass) mChild2);
      }
    }

    @Override
    TIntIntHashMap getInfo(final int event)
    {
      TIntIntHashMap info = super.getInfo(event);
      if (info == null) {
        TIntIntHashMap info1 = mChild1.getInfo(event);
        TIntIntHashMap info2 = mChild2.getInfo(event);
        if (info1.size() < info2.size()) {
          final TIntIntHashMap t = info1;
          info1 = info2;
          info2 = t;
        }
        info = new TIntIntHashMap(info1.size());
        setInfo(event, info);
        final TIntIntHashMap finfo = info;
        info1.forEachEntry(new TIntIntProcedure() {
          public boolean execute(final int state, final int value)
          {
            finfo.put(state, value);
            return true;
          }
        });
        info2.forEachEntry(new TIntIntProcedure() {
          public boolean execute(final int state, final int value)
          {
            finfo.adjustOrPutValue(state, value, value);
            return true;
          }
        });
      }
      return info;
    }

    //#######################################################################
    //# Data Members
    private final int mSize;
    private EquivalenceClass mChild1;
    private EquivalenceClass mChild2;

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
  //# Inner Enumeration Equivalence
  /**
   * Possible equivalences for partitioning a transition relation.
   */
  public enum Equivalence
  {
    /**
     * Bisimulation equivalence. Equivalent states must be able to reach
     * equivalent successors for all traces of events. There are no silent
     * events, and the tau-closure is not computed for this setting.
     */
    BISIMULATION,
    /**
     * Observation equivalence. Equivalent states must be able to reach
     * equivalent successors for all traces of observable events including the
     * empty trace. This setting is the default.
     */
    OBSERVATION_EQUIVALENCE,
    /**
     * Weak observation equivalence. Equivalent states must be able to reach
     * equivalent successors for all traces of observable events <I>not</I>
     * including the empty trace. This is implemented by not considering the
     * silent event for splitting equivalence classes.
     */
    WEAK_OBSERVATION_EQUIVALENCE
  }


  //#########################################################################
  //# Inner Enumeration TransitionRemoval
  /**
   * <P>Possible settings to control how an
   * {@link AltObservationEquivalenceTRSimplifier} handles the removal of redundant
   * transitions.</P>
   *
   * <P>A transition is redundant according to observation equivalence, if the
   * automaton contains other transitions such that the same target state can be
   * reached without the transition, using the same sequence of observable
   * events. For more details on redundant transitions see the following paper: <I>Jaana Eloranta: Minimizing
   * the number of transitions with respect to observation equivalence, BIT
   * <STRONG>31</STRONG>(4), 397-419, 1991</I>.</P>
   *
   * <P>This simplifier can perform two passes of redundant transition
   * removal.</P>
   *
   * <P>The first pass is performed before computing the state state partition.
   * This optional step may improve performance, but fails to remove all
   * redundant transitions if a non-trivial partition is found. Furthermore,
   * it cannot remove tau-transitions correctly if the input transition
   * relation contains tau-loops.</P>
   *
   * <P>The second pass is performed after computation of the partition, when
   * building the output transition relation. Only this pass can guarantee a
   * minimal result.</P>
   */
  public enum TransitionRemoval
  {
    /**
     * Disables removal of redundant transitions. This is the only option that
     * works when using bisimulation equivalence
     * ({@link Equivalence#BISIMULATION}), and it will be automatically selected
     * when bisimulation equivalence is configured.
     */
    NONE,
    /**
     * Enables the first pass to remove of redundant transitions for all events
     * except the silent event with code {@link EventEncoding#TAU}. This option
     * is safe for all automata, and is used as the default. The second pass is
     * performed in addition (even in case of trivial partition, to remove
     * tau-transitions).
     */
    NONTAU,
    /**
     * Enables the first pass to remove all redundant transitions, including
     * silent transitions. This option only is guaranteed to work correctly if
     * the input automaton does not contain any loops of silent events. If this
     * cannot be guaranteed, consider using {@link #NONTAU} instead. The second
     * pass is performed in addition, if a non-trivial partition has been found.
     */
    ALL,
    /**
     * Disables the first pass. Redundant transitions are removed only in the
     * second pass, which is performed even in case of a trivial partition.
     */
    AFTER
  }


  //#########################################################################
  //# Inner Enumeration MarkingMode
  /**
   * <P>Possible settings to control how an
   * {@link AltObservationEquivalenceTRSimplifier} handles implicit markings.</P>
   *
   * <P>When minimising for observation equivalence, states that have a string
   * of silent events leading to a marked states can be considered as marked
   * themselves. The marking method controls whether or not such states such
   * receive a marking in the output automaton.</P>
   *
   * <P>This setting only takes effect if the initial partition is set up
   * based on markings, i.e., if method {@link #createInitialPartition()}
   * is used.</P>
   */
  public enum MarkingMode
  {
    /**
     * Leaves markings unchanged. A state in the output automaton will be
     * marked with a given proposition if and only if at least one state
     * in its equivalence class is marked with that proposition. This is
     * the default setting.
     */
    UNCHANGED,
    /**
     * Adds markings to all states that are implicitly marked. A state is
     * marked by a given proposition, if there is a trace of silent
     * transitions leading to a state marked by that proposition.
     */
    SATURATE,
    /**
     * Tries to minimise the number of markings by removing implicit markings.
     * If, for some state&nbsp;<I>s</I> marked by a given proposition, there is
     * another silently reachable state marked by that proposition, the marking
     * will be removed from&nbsp;<I>s</I>. This option only is guaranteed to
     * work correctly if the input automaton does not contain any loops of
     * silent events.
     */
    MINIMIZE
  }


  //#########################################################################
  //# Data Members
  private Equivalence mEquivalence;
  private TransitionRemoval mTransitionRemovalMode;
  private MarkingMode mMarkingMode;
  private int mTransitionLimit;

  private int mNumEvents;
  private int mFirstSplitEvent;
  private TauClosure mTauPreds;
  private IntListBuffer mClassLists;
  private SimpleEquivalenceClass2[] mStateToClass2;
  private Queue<SimpleEquivalenceClass2> mSimpleSplitters;

  private THashSet<SimpleEquivalenceClass> mWS;
  private THashSet<ComplexEquivalenceClass> mWC;
  private THashSet<SimpleEquivalenceClass> mP;
  private SimpleEquivalenceClass[] mStateToClass;
  private boolean mHasModifications;

}
