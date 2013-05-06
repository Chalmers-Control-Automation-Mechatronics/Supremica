//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters Analysis
//# PACKAGE: net.sourceforge.waters.analysis.abstraction
//# CLASS:   ObservationEquivalenceTRSimplifier
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.analysis.abstraction;

import gnu.trove.iterator.TLongObjectIterator;
import gnu.trove.list.array.TIntArrayList;
import gnu.trove.map.hash.TIntIntHashMap;
import gnu.trove.map.hash.TLongObjectHashMap;
import gnu.trove.procedure.TIntProcedure;
import gnu.trove.set.hash.THashSet;
import gnu.trove.set.hash.TIntHashSet;

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
 * @author Robi Malik, Simon Ware, Rachel Francis, Colin Pilbrow
 */

public class ObservationEquivalenceTRSimplifier
  extends AbstractTRSimplifier
{

  //#########################################################################
  //# Constructors
  /**
   * Creates a new bisimulation simplifier without a transition relation.
   */
  public ObservationEquivalenceTRSimplifier()
  {
  }

  /**
   * Creates a new bisimulation simplifier for the given transition relation.
   */
  public ObservationEquivalenceTRSimplifier
    (final ListBufferTransitionRelation rel)
  {
    if (rel != null) {
      setTransitionRelation(rel);
    }
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
   * Determines the propositions used when partitioning.
   * The propositions mask is only used to distinguish states when setting up
   * an initial partition. When merging states, all propositions present in
   * the transition relation are merged.
   * @param  mask   The bit mask of the significant propositions,
   *                or&nbsp;<CODE>-1</CODE> to indicate that all propositions
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
   * Sets whether special events are to be considered in abstraction.
   * If enabled, events marked as selfloop-only in all other automata
   * will be treated specially. For such events, it is possible to assume
   * implicit selfloops on all states of the automaton being simplified,
   * potentially giving better state reduction.
   */
  public void setUsingSpecialEvents(final boolean enable)
  {
    mUsingSpecialEvents = enable;
  }

  /**
   * Returns whether special events are considered in abstraction.
   * @see #setUsesSpecialEvents(boolean)
   */
  public boolean isUsingSpecialEvents()
  {
    return mUsingSpecialEvents;
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
   * Sets whether the <I>info</I> data structure is used by the bisimulation
   * algorithm. When splitting an equivalence class, the <I>info</I> data
   * structure records for each state and event the number of successors in
   * the split class. Using this data structure, splitting can be sped up
   * when the same class has to be split a second time, ensuring
   * <I>n</I>&nbsp;log&nbsp;<I>n</I> complexity. However, the size of the
   * <I>info</I> data structure is determined by the size of the closure of
   * the transition relation, which can be very large when computing observation
   * equivalence. The <I>info</I> data structure is disabled by default, which
   * gives a quadratic time algorithm with much lower memory requirements.
   */
  public void setInfoEnabled(final boolean enabled)
  {
    mInitialInfoSize = enabled ? 0 : -1;
  }

  /**
   * Returns whether the <I>info</I> data structure is used by the bisimulation
   * algorithm.
   * @see #setInfoEnabled(boolean) setInfoEnabled()
   */
  public boolean getInfoEnabled()
  {
    return mInitialInfoSize >= 0;
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
    mStateShift = AutomatonTools.log2(mNumEvents);
    mFanout =
      mNumReachableStates > 0 ?
      (float) rel.getNumberOfTransitions() / (float) mNumReachableStates :
      0.0f;
  }

  @Override
  public void setPropositions(final int preconditionID, final int defaultID)
  {
    long mask = 0;
    if (preconditionID >= 0) {
      mask |= (1L << preconditionID);
    }
    if (defaultID >= 0) {
      mask |= (1L << defaultID);
    }
    setPropositionMask(mask);
  }

  @Override
  public boolean isPartitioning()
  {
    return true;
  }

  @Override
  public boolean isObservationEquivalentAbstraction()
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
    mTauClosure = null;
    mTauIterator = null;
    mEventClosureIterator = null;
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
      final EquivalenceClass sec =
        mEquivalence.createEquivalenceClass(this, clazz);
      sec.enqueue(true);
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
    final long[] markings;
    if (!mEquivalence.respectsTau() || mMarkingMode == MarkingMode.SATURATE) {
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
    if (mEquivalence.respectsTau()) {
      setUpTauClosure();
      final TransitionIterator iter = mTauClosure.createIterator();
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
          clazz = mEquivalence.createEquivalenceClass(this);
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
      sec.enqueue(true);
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
    if (mEquivalence.respectsTau()) {
      initialStates = new TIntHashSet();
      setUpTauClosure();
      iter = mTauClosure.createIterator();
    } else {
      initialStates = null;
      iter = null;
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
  //# Overrides for net.sourceforge.waters.analysis.abstraction.AbstractTRSimplifier
  @Override
  protected void setUp()
  throws AnalysisException
  {
    super.setUp();
    final ListBufferTransitionRelation rel = getTransitionRelation();
    mPlainEventIterator = rel.createPredecessorsReadOnlyIterator();
    if (mSplitters == null) {
      mHasModifications = false;
      removeRedundantTransitions(TransitionRemovalTime.BEFORE);
      final boolean modified = mHasModifications;
      setUpInitialPartitionBasedOnMarkings();
      mHasModifications |= modified;
    } else {
      removeRedundantTransitions(TransitionRemovalTime.BEFORE);
    }
    setUpTauClosure();
    if (mInitialInfoSize >= 0) {
      mInitialInfoSize = (int) Math.ceil(mFanout);
    }
    final int numStates = rel.getNumberOfStates();
    mTempClass = new TIntArrayList(numStates);
    mMaxInfoSize = 0;
    final int first = mEquivalence.getFirstSplitEvent();
    if (mUsingSpecialEvents) {
      final int numEvents = rel.getNumberOfProperEvents();
      mOnlySelfLoopEvents = new TIntArrayList(rel.getNumberOfProperEvents());
      for (int e = first; e < numEvents; e++) {
        if ((rel.getProperEventStatus(e) &
          EventEncoding.STATUS_OUTSIDE_ONLY_SELFLOOP) != 0) {
          mOnlySelfLoopEvents.add(e);
        }
      }
    } else if (first == EventEncoding.TAU) {
      mOnlySelfLoopEvents = new TIntArrayList(1);
      mOnlySelfLoopEvents.add(first);
    } else {
      mOnlySelfLoopEvents = null;
    }
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
    mPlainEventIterator = null;
    mClassLists = null;
    mClassReadIterator = null;
    mClassWriteIterator = null;
    mStateToClass = null;
    mPredecessors = null;
    mSplitters = null;
    mTempClass = null;
    mOnlySelfLoopEvents = null;
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
    mEquivalence.applyResultPartition(this);
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
    if (!mEquivalence.respectsTau()) {
      if (mEventClosureIterator == null) {
        final ListBufferTransitionRelation rel = getTransitionRelation();
        final TransitionIterator inner =
          rel.createPredecessorsReadOnlyIterator(-1, EventEncoding.TAU);
        mEventClosureIterator =
          new OneEventCachingTransitionIterator(inner, EventEncoding.TAU);
      }
    } else {
      if (mTauClosure == null) {
        final ListBufferTransitionRelation rel = getTransitionRelation();
        final int limit = getTransitionLimit();
        final int config = rel.getConfiguration();
        if ((config & ListBufferTransitionRelation.CONFIG_PREDECESSORS) != 0) {
          mTauClosure = rel.createPredecessorsTauClosure(limit);
        } else {
          mTauClosure = rel.createSuccessorsTauClosure(limit);
        }
        final TransitionIterator inner = mTauClosure.createIterator();
        mTauIterator = new OneEventCachingTransitionIterator
                             (inner, EventEncoding.TAU);
        mEventClosureIterator = mTauClosure.createFullEventClosureIterator(-1);
      }
    }
  }

  private TransitionIterator getPredecessorIterator(final int event)
  {
    if (event == EventEncoding.TAU && mEquivalence.respectsTau()) {
      mTauIterator.reset();
      return mTauIterator;
    } else {
      mEventClosureIterator.resetEvent(event);
      return mEventClosureIterator;
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

  private void applyObservationEquivalencePartition()
  throws AnalysisException
  {
    final ListBufferTransitionRelation rel = getTransitionRelation();
    if (getResultPartition() != null) {
      mTauClosure = null;
      super.applyResultPartition();
      removeRedundantTransitions(TransitionRemovalTime.AFTER_NONTRIVIAL);
      rel.removeTauSelfLoops();
      rel.removeProperSelfLoopEvents();
    } else {
      removeRedundantTransitions(TransitionRemovalTime.AFTER_TRIVIAL);
      if (mHasModifications) {
        rel.removeProperSelfLoopEvents();
      }
    }
  }

  private void applyWeakObservationEquivalencePartition()
  throws AnalysisException
  {
    final ListBufferTransitionRelation rel = getTransitionRelation();
    final List<int[]> partition = getResultPartition();
    if (partition != null) {
      mTauClosure = null;
      mTauIterator = null;
      mEventClosureIterator = null;
      final WeakObservationEquivalencePartitioning partitioner =
        new WeakObservationEquivalencePartitioning(rel, partition,
                                                   mPropositionMask,
                                                   mTransitionLimit);
      partitioner.applyPartition();
      removeRedundantTransitions(TransitionRemovalTime.AFTER_NONTRIVIAL);
      rel.removeTauSelfLoops();
      rel.removeProperSelfLoopEvents();
    } else {
      removeRedundantTransitions(TransitionRemovalTime.AFTER_TRIVIAL);
      if (mHasModifications) {
        rel.removeProperSelfLoopEvents();
      }
    }
  }

  private void removeRedundantTransitions(final TransitionRemovalTime time)
  throws AnalysisException
  {
    final TransitionRemoval mode = mEquivalence.getTransitionRemovalMode(this);
    final boolean doTau = mode.getDoTau(time);
    final boolean doNonTau = mode.getDoNonTau(time);
    if (doTau || doNonTau) {
      setUpTauClosure();
      final ListBufferTransitionRelation rel = getTransitionRelation();
      final int tau = EventEncoding.TAU;
      final int skipped = doTau ? -1 : tau;
      final TransitionIterator iter0 =
        rel.createAllTransitionsModifyingIterator();
      final TransitionIterator iter1 = mTauClosure.createIterator();
      final TransitionIterator iter2 = rel.createAnyReadOnlyIterator();
      final TransitionIterator iter3 = mTauClosure.createIterator();
      trans:
      while (iter0.advance()) {
        final int e = iter0.getCurrentEvent();
        if ((rel.getProperEventStatus(e) & EventEncoding.STATUS_UNUSED) != 0 ||
            e == skipped) {
          continue;
        }
        checkAbort();
        final int from0 = iter0.getCurrentFromState();
        final int to0 = iter0.getCurrentToState();
        iter1.resetState(from0);
        while (iter1.advance()) {
          final int p1 = iter1.getCurrentToState();
          iter2.reset(p1, e);
          while (iter2.advance()) {
            final int p2 = iter2.getCurrentToState();
            if (e == tau) {
              if (doTau && p1 != from0 && p2 == to0) {
                iter0.remove();
                mHasModifications = true;
                continue trans;
              }
            } else {
              if (doNonTau && (p1 != from0 || p2 != to0)) {
                iter3.resetState(p2);
                while (iter3.advance()) {
                  final int p3 = iter3.getCurrentToState();
                  if (p3 == to0) {
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


  //#########################################################################
  //# Auxiliary Methods
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

    public ComplexEquivalenceClass getParent();

    public void setParent(ComplexEquivalenceClass parent);

    public InfoMap getInfo();

    public void setInfo(InfoMap info);

    public int getSize();

    public void collect(TIntArrayList states);

    public void splitOn();

    public void enqueue(boolean force);

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
    @Override
    public int compareTo(final Splitter splitter)
    {
      return mSize - splitter.getSize();
    }

    //#######################################################################
    //# Interface Splitter
    @Override
    public int getSize()
    {
      return mSize;
    }

    @Override
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

    @Override
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
    @Override
    public ComplexEquivalenceClass getParent()
    {
      return null;
    }

    @Override
    public void setParent(final ComplexEquivalenceClass parent)
    {
    }

    @Override
    public InfoMap getInfo()
    {
      return null;
    }

    @Override
    public void setInfo(final InfoMap info)
    {
    }

    /**
     * Checks if other equivalence classes must be split based on this class,
     * and performs splits as necessary.
     */
    @Override
    public void splitOn()
    {
      mIsOpenSplitter = false;
      collect(mTempClass);
      final int size = getSize();
      assert size == mTempClass.size();
      final ListBufferTransitionRelation rel = getTransitionRelation();
      final int hashSize = Math.min(Math.round(size * mFanout), mNumEvents);
      // Find the set of events that can reach this class ...
      final TIntHashSet events = new TIntHashSet(hashSize);
      // First add all selfloop-only events (including tau) ...
      if (mOnlySelfLoopEvents != null) {
        for (int i = 0; i < mOnlySelfLoopEvents.size(); i++) {
          final int e = mOnlySelfLoopEvents.get(i);
          events.add(e);
        }
      }
      final int first = mEquivalence.getFirstSplitEvent();
      mPlainEventIterator.resetEvents(first, mNumEvents - 1);
      // Second search for regular events that can reach the class.
      if (mEquivalence.respectsTau()) {
        // Considering tau as silent event ...
        mTauIterator.reset();
        for (int i = 0; i < size; i++) {
          final int state = mTempClass.get(i);
          mTauIterator.resume(state);
          while (mTauIterator.advance()) {
            final int taupred = mTauIterator.getCurrentSourceState();
            mPlainEventIterator.resetState(taupred);
            while (mPlainEventIterator.advance()) {
              final int event = mPlainEventIterator.getCurrentEvent();
              events.add(event);
            }
          }
        }
      } else {
        // Considering tau as proper event (bisimulation) ...
        for (int i = 0; i < size; i++) {
          final int state = mTempClass.get(i);
          mPlainEventIterator.resetState(state);
          while (mPlainEventIterator.advance()) {
            final int event = mPlainEventIterator.getCurrentEvent();
            events.add(event);
          }
        }
      }

      // Look for predecessors with the events found ...
      final Collection<EquivalenceClass> splitClasses =
        new THashSet<EquivalenceClass>();
      events.forEach(new TIntProcedure() {
        @Override
        public boolean execute(final int event)
        {
          final boolean followTau =
            (rel.getProperEventStatus(event) &
             EventEncoding.STATUS_OUTSIDE_ONLY_SELFLOOP) != 0;
          TIntHashSet visitedStates = null;
          if (followTau) {
            if (mEquivalence.respectsTau()) {
              mTauIterator.reset();
            } else {
              visitedStates = new TIntHashSet();
            }
          }
          final TransitionIterator transIter = getPredecessorIterator(event);
          for (int i = 0; i < size; i++) {
            final int state = mTempClass.get(i);
            transIter.resume(state);
            while (transIter.advance()) {
              final int pred = transIter.getCurrentSourceState();
              // Store pred in cache so we do not visit it twice.
              if (followTau) {
                if (mEquivalence.respectsTau()) {
                  mTauIterator.addVisitedState(pred);
                } else {
                  visitedStates.add(pred);
                }
              }
              final EquivalenceClass splitClass = mStateToClass[pred];
              if (splitClass != null) {
                splitClass.moveToOverflowList(pred);
                splitClasses.add(splitClass);
              }
            }
          }
          // Special treatment for other selfloop-only events
          if (followTau) {
            if (mEquivalence.respectsTau()) {
              // If considering tau as silent event:
              // Add all states backwards reachable by tau as predecessors.
              for (int i = 0; i < size; i++) {
                final int state = mTempClass.get(i);
                mTauIterator.resume(state);
                while (mTauIterator.advance()) {
                  final int pred = mTauIterator.getCurrentSourceState();
                  final EquivalenceClass splitClass = mStateToClass[pred];
                  if (splitClass != null) {
                    splitClass.moveToOverflowList(pred);
                    splitClasses.add(splitClass);
                  }
                }
              }
            } else {
              // If considering tau as proper event:
              // Add only this state as predecessor.
              for (int i = 0; i < size; i++) {
                final int state = mTempClass.get(i);
                final EquivalenceClass splitClass = mStateToClass[state];
                if (splitClass != null && !visitedStates.contains(state)) {
                  splitClass.moveToOverflowList(state);
                  splitClasses.add(splitClass);
                }
              }
            }
          }
          for (final EquivalenceClass splitClass : splitClasses) {
            splitClass.splitUsingOverflowList();
          }
          splitClasses.clear();
          // Not using info or MINSTATE: stop if the class has split itself.
          return getSize() == size || !mEquivalence.mustEnqueueBigBrother();
        }
      });
      mTempClass.clear();
    }

    @Override
    public void enqueue(final boolean force)
    {
      if (!mIsOpenSplitter) {
        mIsOpenSplitter = true;
        mSplitters.add(this);
      }
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
        setUpStateToClass();
      }
      overflowClass.setUpStateToClass();
      overflowClass.enqueue(false);
      if (mEquivalence.mustEnqueueBigBrother()) {
        enqueue(false);
      }
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
    @Override
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
    @Override
    public ComplexEquivalenceClass getParent()
    {
      return mParent;
    }

    @Override
    public void setParent(final ComplexEquivalenceClass parent)
    {
      mParent = parent;
    }

    @Override
    public InfoMap getInfo()
    {
      return mInfo;
    }

    @Override
    public void setInfo(final InfoMap info)
    {
      mInfo = info;
    }

    @Override
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
      final int first = mEquivalence.getFirstSplitEvent();
      for (int event = first; event < mNumEvents; event++) {
        if ((rel.getProperEventStatus(event) & EventEncoding.STATUS_UNUSED) == 0) {
          final TransitionIterator transIter =
            getPredecessorIterator(event);
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
          visited.clear();
          for (final EquivalenceClass splitClass : splitClasses) {
            splitClass.splitUsingOverflowList();
          }
          splitClasses.clear();
        }
      }
      mTempClass.clear();
      mMaxInfoSize = Math.max(mMaxInfoSize, info.size());
    }

    @Override
    public void enqueue(final boolean force)
    {
      mParent = null;
      if (force) {
        mSplitters.add(this);
      }
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
        setUpStateToClass();
      }
      overflowClass.setUpStateToClass();
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
        setUpStateToClass();
      }

      // Create and enqueue complex splitters ...
      final LeafEquivalenceClass class1 =
        new LeafEquivalenceClass(overflow1, size1, false);
      class1.setUpStateToClass();
      final LeafEquivalenceClass class2 =
        new LeafEquivalenceClass(overflow2, size2, false);
      class2.setUpStateToClass();
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
    @Override
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
    @Override
    public int getSize()
    {
      return mSize;
    }

    @Override
    public ComplexEquivalenceClass getParent()
    {
      return mParent;
    }

    @Override
    public void setParent(final ComplexEquivalenceClass parent)
    {
      mParent = parent;
    }

    @Override
    public InfoMap getInfo()
    {
      return mInfo;
    }

    @Override
    public void setInfo(final InfoMap info)
    {
      mInfo = info;
    }

    @Override
    public void collect(final TIntArrayList states)
    {
      mLittleChild.collect(states);
      mBigChild.collect(states);
    }

    @Override
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
      final int first = mEquivalence.getFirstSplitEvent();
      for (int event = first; event < mNumEvents; event++) {
        if ((rel.getProperEventStatus(event) & EventEncoding.STATUS_UNUSED) == 0) {
          final TransitionIterator transIter =
            getPredecessorIterator(event);
          for (int i = 0; i < tempSize; i++) {
            final int state = mTempClass.get(i);
            transIter.resetState(state);
            while (transIter.advance()) {
              final int pred = transIter.getCurrentSourceState();
              if (visited.add(pred)) {
                final EquivalenceClass splitClass = mStateToClass[pred];
                if (splitClass != null) {
                  final LeafEquivalenceClass leaf =
                    (LeafEquivalenceClass) splitClass;
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
      mLittleChild.enqueue(false);
      mBigChild.enqueue(false);
      mMaxInfoSize = Math.max(mMaxInfoSize, info.size());
      mMaxInfoSize = Math.max(mMaxInfoSize, littleInfo.size());
    }

    @Override
    public void enqueue(final boolean force)
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

    @Override
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
  //# Inner Class InfoMap
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
  //# Inner Enumeration Equivalence

  /*
  /**
   * Possible equivalences for partitioning a transition relation.

 * Jean-Claude Fernandez. An Implementation of an Efficient Algorithm for
 * Bisimulation Equivalence. Science of Computer Programming,
 * <STRONG>13</STRONG>, 219-236, 1990.<BR>
 * J. E. Hopcroft. An <I>n</I>&nbsp;log&nbsp;<I>n</I> Algorithm for Minimizing
 * States in a Finite Automaton. In: Z. Kohavi and A. Paz, eds., Theory of
 * Machines and Computations, Academic Press, New York, 397-419, 1971.<BR>
 * Jaana Eloranta. Minimizing the Number of Transitions with Respect to
 * Observation Equivalence. BIT, <STRONG>31</STRONG>(4), 397-419, 1991.
   */
  public enum Equivalence
  {
    //#######################################################################
    //# Enumeration Values
    /**
     * <P>Language equivalence. This method only works for a deterministic
     * automaton, in which case it achieves minimisation according to Hopcroft's
     * algorithm. It is implemented like the bisimulation algorithm, but not
     * allowing tau events, and never splitting on the larger class resulting
     * from a split.</P>
     *
     * <P><I>Reference.</I> J. E. Hopcroft. An <I>n</I>&nbsp;log&nbsp;<I>n</I>
     * Algorithm for Minimizing States in a Finite Automaton. In: Z. Kohavi and
     * A. Paz, eds., Theory of Machines and Computations, Academic Press, New
     * York, 397-419, 1971.</P>
     */
    DETERMINISTIC_MINSTATE {
      @Override
      boolean respectsTau()
      {
        return false;
      }
      @Override
      EquivalenceClass createEquivalenceClass
        (final ObservationEquivalenceTRSimplifier simplifier)
      {
        return simplifier.new PlainEquivalenceClass();
      }
      @Override
      EquivalenceClass createEquivalenceClass
        (final ObservationEquivalenceTRSimplifier simplifier,
         final int[] states)
      {
        return simplifier.new PlainEquivalenceClass(states);
      }
      @Override
      boolean mustEnqueueBigBrother()
      {
        return false;
      }
    },
    /**
     * <P>Bisimulation equivalence. Equivalent states must be able to reach
     * equivalent successors for all traces of events. There are no silent
     * events, and the tau-closure is not computed for this setting.</P>
     *
     * <P><I>Reference.</I> Jean-Claude Fernandez. An Implementation of an
     * Efficient Algorithm for Bisimulation Equivalence. Science of Computer
     * Programming, <STRONG>13</STRONG>, 219-236, 1990.</P>
     */
    BISIMULATION {
      @Override
      boolean respectsTau()
      {
        return false;
      }
      @Override
      public int getFirstSplitEvent()
      {
        return EventEncoding.TAU;
      }
    },
    /**
     * Observation equivalence. Equivalent states must be able to reach
     * equivalent successors for all traces of observable events including the
     * empty trace. This setting is the default.
     */
    OBSERVATION_EQUIVALENCE,
    /**
     * <P>Weak observation equivalence. Equivalent states must be able to reach
     * equivalent successors for all traces of observable events <I>not</I>
     * including the empty trace. This is implemented by not considering the
     * silent event for splitting equivalence classes.</P>
     *
     * <P><I>Reference.</I> Rong Su, Jan H. van Schuppen, Jacobus E. Rooda,
     * Albert T. Hofkamp. Nonconflict check by using sequential automaton
     * abstractions based on weak observation equivalence. Automatica,
     * <STRONG>46</STRONG>(6), 968--978, 2010.</P>
     */
    WEAK_OBSERVATION_EQUIVALENCE {
      @Override
      public int getFirstSplitEvent()
      {
        return EventEncoding.NONTAU;
      }
      @Override
      void applyResultPartition
        (final ObservationEquivalenceTRSimplifier simplifier)
      throws AnalysisException
      {
        simplifier.applyWeakObservationEquivalencePartition();
      }
    };

    //#######################################################################
    //# Equivalence Characteristics
    boolean respectsTau()
    {
      return true;
    }

    EquivalenceClass createEquivalenceClass
      (final ObservationEquivalenceTRSimplifier simplifier)
    {
      return simplifier.createEquivalenceClass();
    }

    EquivalenceClass createEquivalenceClass
      (final ObservationEquivalenceTRSimplifier simplifier, final int[] states)
    {
      return simplifier.createEquivalenceClass(states);
    }

    int getFirstSplitEvent()
    {
      return respectsTau() ? EventEncoding.TAU : EventEncoding.NONTAU;
    }

    TransitionRemoval getTransitionRemovalMode
      (final ObservationEquivalenceTRSimplifier simplifier)
    {
      if (respectsTau()) {
        return simplifier.getTransitionRemovalMode();
      } else {
        return TransitionRemoval.NONE;
      }
    }

    /**
     * Returns whether the algorithm should always enqueue both equivalence
     * classes resulting from a split. When minimising deterministic
     * automata or when using the "info map" for bisimulation, only the
     * smaller class resulting from a split may need to be checked again.
     * Otherwise, both classes are enqueued to be checked again.
     */
    boolean mustEnqueueBigBrother()
    {
      return true;
    }

    void applyResultPartition
      (final ObservationEquivalenceTRSimplifier simplifier)
    throws AnalysisException
    {
      simplifier.applyObservationEquivalencePartition();
    }
  }


  //#########################################################################
  //# Inner Enumeration TransitionRemoval
  /**
   * <P>Possible settings to control how an
   * {@link ObservationEquivalenceTRSimplifier} handles the removal of redundant
   * transitions.</P>
   *
   * <P>A transition is redundant according to observation equivalence, if the
   * automaton contains other transitions such that the same target state can be
   * reached without the transition, using the same sequence of observable
   * events.</P>
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
   *
   * <P><I>Reference.</I><BR>
   * Jaana Eloranta. Minimizing the Number of Transitions with Respect to
   * Observation Equivalence. BIT, 31(4), 397-419, 1991.</P>
   */
  public enum TransitionRemoval
  {

    //#######################################################################
    //# Enumeration Values
    /**
     * Disables removal of redundant transitions. This is the only option that
     * works when using deterministic minimisation
     * ({@link Equivalence#DETERMINISTIC_MINSTATE}) or bisimulation equivalence
     * ({@link Equivalence#BISIMULATION}), and it will be automatically used
     * when these equivalences are configured.
     */
    NONE {
      @Override
      boolean getDoNonTau(final TransitionRemovalTime time)
      {
        return false;
      }
      @Override
      boolean getDoTau(final TransitionRemovalTime time)
      {
        return false;
      }
    },
    /**
     * Enables the first pass to remove of redundant transitions for all events
     * except the silent event with code {@link EventEncoding#TAU}. This option
     * is safe for all automata, and is used as the default. The second pass is
     * performed in addition (even in case of a trivial partition, to remove
     * tau-transitions).
     */
    NONTAU {
      @Override
      boolean getDoNonTau(final TransitionRemovalTime time)
      {
        return time != TransitionRemovalTime.AFTER_TRIVIAL;
      }
      @Override
      boolean getDoTau(final TransitionRemovalTime time)
      {
        return time != TransitionRemovalTime.BEFORE;
      }
    },
    /**
     * Enables the first pass to remove all redundant transitions, including
     * silent transitions. This option only is guaranteed to work correctly if
     * the input automaton does not contain any loops of silent events. If this
     * cannot be guaranteed, consider using {@link #NONTAU} instead. The second
     * pass is performed in addition, if a non-trivial partition has been found.
     */
    ALL {
      @Override
      boolean getDoNonTau(final TransitionRemovalTime time)
      {
        return time != TransitionRemovalTime.AFTER_TRIVIAL;
      }
      @Override
      boolean getDoTau(final TransitionRemovalTime time)
      {
        return time != TransitionRemovalTime.AFTER_TRIVIAL;
      }
    },
    /**
     * Disables the first pass. Redundant transitions are removed only in the
     * second pass, which is performed even in case of a trivial partition.
     */
    AFTER {
      @Override
      boolean getDoNonTau(final TransitionRemovalTime time)
      {
        return time != TransitionRemovalTime.BEFORE;
      }
      @Override
      boolean getDoTau(final TransitionRemovalTime time)
      {
        return time != TransitionRemovalTime.BEFORE;
      }
    },
    /**
     * Disables the first pass. Redundant transitions are removed only in the
     * second pass, which is performed only in case of a nontrivial partition.
     */
    AFTER_IF_CHANGED {
      @Override
      boolean getDoNonTau(final TransitionRemovalTime time)
      {
        return time == TransitionRemovalTime.AFTER_TRIVIAL;
      }
      @Override
      boolean getDoTau(final TransitionRemovalTime time)
      {
        return time == TransitionRemovalTime.AFTER_TRIVIAL;
      }
    };

    //#######################################################################
    //# Mode Selection
    abstract boolean getDoTau(TransitionRemovalTime time);
    abstract boolean getDoNonTau(TransitionRemovalTime time);

  }


  //#########################################################################
  //# Inner Enumeration MarkingMode
  /**
   * <P>Possible settings to control how an
   * {@link ObservationEquivalenceTRSimplifier} handles implicit markings.</P>
   *
   * <P>When minimising for observation equivalence, states that have a string
   * of silent events leading to a marked states can be considered as marked
   * themselves. The marking method controls whether or not such states such
   * receive a marking in the output automaton.</P>
   *
   * <P>This setting only takes effect if the initial partition is set up
   * based on markings, i.e., if method
   * {@link #setUpInitialPartitionBasedOnMarkings()} is used.</P>
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
  //# Inner Enumeration SplitKind
  private enum SplitKind
  {
    LITTLE,
    BIG,
    BOTH
  }


  //#########################################################################
  //# Inner Enumeration TransitionRemovalTime
  private enum TransitionRemovalTime
  {
    BEFORE,
    AFTER_TRIVIAL,
    AFTER_NONTRIVIAL
  }


  //#########################################################################
  //# Data Members
  private Equivalence mEquivalence = Equivalence.OBSERVATION_EQUIVALENCE;
  private TransitionRemoval mTransitionRemovalMode = TransitionRemoval.NONTAU;
  private MarkingMode mMarkingMode = MarkingMode.UNCHANGED;
  private long mPropositionMask = ~0;
  private boolean mUsingSpecialEvents = true;
  private int mTransitionLimit = Integer.MAX_VALUE;
  private int mInitialInfoSize = -1;

  private int mNumReachableStates;
  private int mNumEvents;
  private int mNumClasses;
  private int mStateShift;
  private float mFanout;
  private boolean mHasModifications;

  private TauClosure mTauClosure;
  private OneEventCachingTransitionIterator mTauIterator;
  private TransitionIterator mEventClosureIterator;
  private TransitionIterator mPlainEventIterator;
  private IntListBuffer mClassLists;
  private IntListBuffer.ReadOnlyIterator mClassReadIterator;
  private IntListBuffer.ModifyingIterator mClassWriteIterator;
  private EquivalenceClass[] mStateToClass;
  private int[] mPredecessors;
  private Queue<Splitter> mSplitters;
  private TIntArrayList mTempClass;
  /**
   * List of event numbers of events that are only-selfloop in other automata
   * (including tau), or <CODE>null</CODE> if no such events.
   */
  private TIntArrayList mOnlySelfLoopEvents;

  private int mMaxInfoSize;


}
