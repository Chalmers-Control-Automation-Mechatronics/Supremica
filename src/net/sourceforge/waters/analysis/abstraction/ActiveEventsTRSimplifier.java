//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# Copyright (C) 2004-2015 Robi Malik
//###########################################################################
//# This file is part of Waters.
//# Waters is free software: you can redistribute it and/or modify it under
//# the terms of the GNU General Public License as published by the Free
//# Software Foundation, either version 2 of the License, or (at your option)
//# any later version.
//# Waters is distributed in the hope that it will be useful, but WITHOUT ANY
//# WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
//# FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
//# details.
//# You should have received a copy of the GNU General Public License along
//# with Waters. If not, see <http://www.gnu.org/licenses/>.
//#
//# Linking Waters statically or dynamically with other modules is making a
//# combined work based on Waters. Thus, the terms and conditions of the GNU
//# General Public License cover the whole combination.
//# In addition, as a special exception, the copyright holders of Waters give
//# you permission to combine Waters with code included in the standard
//# release of Supremica under the Supremica Software License Agreement (or
//# modified versions of such code, with unchanged license). You may copy and
//# distribute such a system following the terms of the GNU GPL for Waters and
//# the licenses of the other code concerned.
//# Note that people who make modified versions of Waters are not obligated to
//# grant this special exception for their modified versions; it is their
//# choice whether to do so. The GNU General Public License gives permission
//# to release a modified version without this exception; this exception also
//# makes it possible to release a modified version which carries forward this
//# exception.
//###########################################################################

package net.sourceforge.waters.analysis.abstraction;

import gnu.trove.list.array.TIntArrayList;
import gnu.trove.map.hash.TIntIntHashMap;
import gnu.trove.map.hash.TIntObjectHashMap;
import gnu.trove.set.hash.THashSet;
import gnu.trove.set.hash.TIntHashSet;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.Set;

import net.sourceforge.waters.analysis.tr.EventEncoding;
import net.sourceforge.waters.analysis.tr.IntListBuffer;
import net.sourceforge.waters.analysis.tr.IntSetBuffer;
import net.sourceforge.waters.analysis.tr.ListBufferTransitionRelation;
import net.sourceforge.waters.analysis.tr.TRPartition;
import net.sourceforge.waters.analysis.tr.TauClosure;
import net.sourceforge.waters.analysis.tr.TransitionIterator;
import net.sourceforge.waters.model.analysis.AnalysisException;


/**
 * <P>An implementation of the <I>Active Events Rule</I> with relaxed
 * incoming equivalence.</P>
 *
 * <P>This rule merges all states that are weakly incoming equivalent and have
 * equal sets of eligible events. Am equivalence relation between states is a
 * weak incoming equivalence relation if for any two equivalent states,
 * either both or none are silently reachable from an initial states,
 * both states are reachable by the same events from state outside of their
 * equivalence class, and both states are reachable by the same events from
 * within their equivalence class.</P>
 *
 * <P><STRONG>Algorithm:</STRONG></P>
 * <OL>
 * <LI>Create initial partition based on active events and initial state
 *     property.</LI>
 * <LI>Refine partition based on incoming equivalence between equivalence
 *     classes. Separate states that do or do not have incoming transitions
 *     with a given event and source state, where the source state belongs
 *     to a different equivalence class.</LI>
 * <LI>Refine partition based on incoming equivalence within equivalence
 *     classes. Separate states that do or do not have incoming transitions
 *     with a given event from within their own equivalence class, and
 *     separate states with an outgoing transition to a different equivalent
 *     class by an active events from states that stay within the same class
 *     by that event.</LI>
 * <LI>Repeat steps 2. and&nbsp;3. for any equivalence classes that have been
 *     split according to step&nbsp;3.</LI>
 * <LI>Merge states that are still equivalent. If any states have been merged,
 *     update data structures and try to merge again starting from
 *     step&nbsp;1.</LI>
 * </OL>
 *
 * <P><I>Reference:</I> Hugo Flordal, Robi Malik. Compositional Verification
 * in Supervisory Control. SIAM Journal of Control and Optimization,
 * 48(3), 1914-1938, 2009.</P>
 *
 * @author Robi Malik
 */

public class ActiveEventsTRSimplifier
  extends AbstractMarkingTRSimplifier
{

  //#######################################################################
  //# Constructors
  public ActiveEventsTRSimplifier()
  {
  }

  public ActiveEventsTRSimplifier(final ListBufferTransitionRelation rel)
  {
    super(rel);
  }


  //#########################################################################
  //# Configuration
  /**
   * Sets the transition limit. The transition limit specifies the maximum
   * number of transitions (including stored silent transitions of the
   * transitive closure) that will be stored.
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
   * @see #setTransitionLimit(int) setTransitionLimit()
   */
  public int getTransitionLimit()
  {
    return mTransitionLimit;
  }


  //#########################################################################
  //# Interface
  //# net.sourceforge.waters.analysis.abstraction.TransitionRelationSimplifier
  @Override
  public int getPreferredInputConfiguration()
  {
    return ListBufferTransitionRelation.CONFIG_SUCCESSORS;
  }


  //#########################################################################
  //# Overrides for
  //# net.sourceforge.waters.analysis.abstraction.AbstractMarkingTRSimplifier
  @Override
  public boolean isDumpStateAware()
  {
    return true;
  }


  //#########################################################################
  //# Overrides for net.sourceforge.waters.analysis.abstraction.AbstractTRSimplifier
  @Override
  protected void setUp()
    throws AnalysisException
  {
    super.setUp();
    final ListBufferTransitionRelation rel = getTransitionRelation();
    final int numEvents = rel.getNumberOfProperEvents();
    mSetBuffer = new IntSetBuffer(numEvents + 1);
    mSetReadIterator = mSetBuffer.iterator();
    mListBuffer = new IntListBuffer();
    mListReadIterator = mListBuffer.createReadOnlyIterator();
    mListWriteIterator = mListBuffer.createModifyingIterator();
    createTauClosure();
  }

  @Override
  protected boolean runSimplifier()
    throws AnalysisException
  {
    final ListBufferTransitionRelation rel = getTransitionRelation();
    if (rel.getNumberOfReachableStates() <= 1) {
      return false;
    }

    // 1. Create active sets and set up initial states
    createInitialActiveEventSets();
    if (mNumProperCandidates == 0) {
      return false;
    }

    mExternalSplittters = new PriorityQueue<>(mNumProperCandidates);
    mInternalSplittters = new PriorityQueue<>(mNumProperCandidates);
    mActiveExitSplittters = new PriorityQueue<>(mNumProperCandidates);
    main:
    do {
      // 2. Create initial partition based on active events and initial states
      createEquivalenceClasses();
      createTauClosure();
      final int numStates = rel.getNumberOfStates();
      mPredecessors = new int[numStates];
      // 3. Refine partition based on incoming equivalence
      for (int s = 0; s < numStates; s++) {
        final EquivalenceClass clazz = mStateToClass[s];
        if (clazz == null) {
          splitOtherClasses(s);
          if (mNumProperCandidates == 0) {
            break main;
          }
        } else {
          clazz.enqueueExternal();
        }
      }
      while (!mExternalSplittters.isEmpty() ||
             !mInternalSplittters.isEmpty() ||
             !mActiveExitSplittters.isEmpty()) {
        if (!mExternalSplittters.isEmpty()) {
          final EquivalenceClass splitter = mExternalSplittters.poll();
          splitter.splitOtherClasses();
        } else if (!mInternalSplittters.isEmpty()) {
          final EquivalenceClass splitter = mInternalSplittters.poll();
          splitter.splitSameClass();
        } else {
          final EquivalenceClass splitter = mActiveExitSplittters.poll();
          splitter.splitOnActiveExits();
        }
        if (mNumProperCandidates == 0) {
          break main;
        }
      }
      // 4. Create and apply partition
      final TRPartition partition = createPartition();
      applyPartition(partition);
      updateActiveEventsSets(partition);
    } while (mNumProperCandidates > 0);

    // 5. Remove selfloops and return result
    final TRPartition partition = getResultPartition();
    if (partition != null) {
      rel.removeTauSelfLoops();
      removeProperSelfLoopEvents();
      return true;
    } else {
      return false;
    }
  }

  @Override
  protected void tearDown()
  {
    super.tearDown();
    mSetBuffer = null;
    mActiveEventSets = null;
    mActiveEventCounts = null;
    mStateToClass = null;
    mPredecessors = null;
    mExternalSplittters = null;
    mInternalSplittters = null;
    mActiveExitSplittters = null;
    mTauClosure = null;
    mFullEventClosureIterator = null;
    mListBuffer = null;
    mListReadIterator = null;
    mListWriteIterator = null;
    mSetReadIterator = null;
  }


  //#########################################################################
  //# Auxiliary Methods
  private void createTauClosure()
  {
    if (mTauClosure == null) {
      final ListBufferTransitionRelation rel = getTransitionRelation();
      mTauClosure = rel.createSuccessorsTauClosure(mTransitionLimit);
      mFullEventClosureIterator = mTauClosure.createFullEventClosureIterator();
    }
  }


  /**
   * Creates active event sets and adds to {@link #mSetBuffer},
   * and stores them for each state in {@link #mActiveEventSets}.
   * Also initialises {@link #mActiveEventCounts},
   * and stores the number of active events sets that are used more than
   * once in {@link #mNumProperCandidates}.
   */
  private void createInitialActiveEventSets()
  {
    createTauClosure();
    final ListBufferTransitionRelation rel = getTransitionRelation();
    final int numStates = rel.getNumberOfStates();
    final int numEvents = rel.getNumberOfProperEvents();
    final int INITIAL = numEvents;
    final TransitionIterator tauIter = mTauClosure.createIterator();
    final TIntHashSet initialStates = new TIntHashSet();
    int numReachable = 0;
    for (int s = 0; s < numStates; s++) {
      if (rel.isReachable(s)) {
        numReachable++;
        if (rel.isInitial(s) && initialStates.add(s)) {
          tauIter.resetState(s);
          while (tauIter.advance()) {
            final int t = tauIter.getCurrentTargetState();
            initialStates.add(t);
          }
        }
      }
    }
    mActiveEventSets = new int[numStates];
    mActiveEventCounts = new TIntIntHashMap(numReachable);
    final TransitionIterator eventIter = rel.createSuccessorsReadOnlyIterator();
    eventIter.resetEvents(EventEncoding.NONTAU, numEvents - 1);
    int defaultID = getDefaultMarkingID();
    if (!rel.isPropositionUsed(defaultID)) {
      defaultID = -1;
    }
    mNumProperCandidates = 0;
    final TIntHashSet events = new TIntHashSet();
    for (int s = 0; s < numStates; s++) {
      if (rel.isReachable(s)) {
        tauIter.resetState(s);
        while (tauIter.advance()) {
          final int t = tauIter.getCurrentTargetState();
          if (defaultID >= 0 && rel.isMarked(t, defaultID)) {
            events.add(OMEGA);
          }
          eventIter.resetState(t);
          while (eventIter.advance()) {
            final int e = eventIter.getCurrentEvent();
            events.add(e);
          }
        }
        if (initialStates.contains(s)) {
          events.add(INITIAL);
        }
        final int set = mSetBuffer.add(events);
        mActiveEventSets[s] = set;
        if (mActiveEventCounts.adjustOrPutValue(set, 1, 1) == 2) {
          mNumProperCandidates++;
        }
        events.clear();
      } else {
        mActiveEventSets[s] = -1;
      }
    }
  }

  /**
   * Updates {@link #mActiveEventSets} and {@link #mActiveEventCounts} after
   * a partition has been applied.
   * Stores the new number of active events sets that are used more than
   * once in {@link #mNumProperCandidates}.
   */
  private void updateActiveEventsSets(final TRPartition partition)
  {
    final ListBufferTransitionRelation rel = getTransitionRelation();
    final int numStates = rel.getNumberOfStates();
    final int[] activeEventSets = new int[numStates];
    mActiveEventCounts.clear();
    mNumProperCandidates = 0;
    int clazzNo = 0;
    for (final int[] clazz : partition.getClasses()) {
      if (clazz != null) {
        final int state = clazz[0];
        final int set = mActiveEventSets[state];
        activeEventSets[clazzNo] = set;
        if (mActiveEventCounts.adjustOrPutValue(set, 1, 1) == 2) {
          mNumProperCandidates++;
        }
      } else {
        activeEventSets[clazzNo] = -1;
      }
      clazzNo++;
    }
    mActiveEventSets = activeEventSets;
  }

  /**
   * Creates equivalence classes for groups of two or more states that
   * have the same active event sets. Assumes that {@link #mActiveEventCounts}
   * is set up, and that {@link #mNumProperCandidates} is nonzero and
   * contains the number of equivalence classes that need to be created.
   */
  private void createEquivalenceClasses()
  {
    final ListBufferTransitionRelation rel = getTransitionRelation();
    final int numStates = rel.getNumberOfStates();
    mStateToClass = new EquivalenceClass[numStates];
    final TIntObjectHashMap<EquivalenceClass> classMap =
      new TIntObjectHashMap<>(mNumProperCandidates);
    for (int s = 0; s < numStates; s++) {
      final int set = mActiveEventSets[s];
      if (set >= 0 && mActiveEventCounts.get(set) > 1) {
        EquivalenceClass clazz = classMap.get(set);
        if (clazz == null) {
          clazz = new EquivalenceClass(set);
          classMap.put(set, clazz);
        }
        clazz.addState(s);
      }
    }
  }

  private void splitOtherClasses(final int source)
  {
    final ListBufferTransitionRelation rel = getTransitionRelation();
    if (rel.isReachable(source)) {
      final int numEvents = rel.getNumberOfProperEvents();
      final int active = mActiveEventSets[source];
      mSetReadIterator.reset(active);
      while (mSetReadIterator.advance()) {
        final int event = mSetReadIterator.getCurrentData();
        if (event >= EventEncoding.NONTAU && event < numEvents) {
          splitOtherClasses(source, event);
          if (mNumProperCandidates == 0) {
            return;
          }
        }
      }
    }
  }

  private byte splitOtherClasses(final int source, final int event)
  {
    final Set<EquivalenceClass> splitClasses = new THashSet<>();
    final EquivalenceClass sourceClass = mStateToClass[source];
    byte result = 0;
    mFullEventClosureIterator.reset(source, event);
    while (mFullEventClosureIterator.advance()) {
      final int target = mFullEventClosureIterator.getCurrentTargetState();
      final EquivalenceClass targetClass = mStateToClass[target];
      if (targetClass == null) {
        if (source != target) {
          result |= EXIT;
        }
      } else {
        if (targetClass == sourceClass) {
          result |= INTERNAL;
        } else {
          targetClass.moveToSplitList(target);
          splitClasses.add(targetClass);
          result |= EXIT;
        }
      }
    }
    for (final EquivalenceClass splitClass : splitClasses) {
      splitClass.splitUsingSplitList();
    }
    return result;
  }

  private TRPartition createPartition()
  {
    if (mNumProperCandidates > 0) {
      final ListBufferTransitionRelation rel = getTransitionRelation();
      final int numStates = rel.getNumberOfStates();
      final List<int[]> classes = new ArrayList<int[]>(numStates);
      for (int state = 0; state < numStates; state++) {
        if (rel.isReachable(state)) {
          final EquivalenceClass clazz = mStateToClass[state];
          if (clazz == null) {
            final int[] states = new int[1];
            states[0] = state;
            classes.add(states);
          } else if (clazz.getFirstState() == state) {
            final int[] states = clazz.getStates();
            classes.add(states);
          }
        }
      }
      final int dumpIndex = rel.getDumpStateIndex();
      if (!rel.isReachable(dumpIndex)) {
        classes.add(null);
      }
      return new TRPartition(classes, numStates);
    } else {
      return null;
    }
  }

  private void applyPartition(final TRPartition newPartition)
  {
    final ListBufferTransitionRelation rel = getTransitionRelation();
    rel.merge(newPartition);
    final TRPartition oldPartition = getResultPartition();
    final TRPartition combinedPartition =
      TRPartition.combine(oldPartition, newPartition);
    setResultPartition(combinedPartition);
    mTauClosure = null;
    mFullEventClosureIterator = null;
  }


  //#########################################################################
  //# Debugging
  @Override
  public String toString()
  {
    final StringWriter writer = new StringWriter();
    final PrintWriter printer = new PrintWriter(writer);
    dump(printer);
    return writer.toString();
  }

  private void dump(final PrintWriter printer)
  {
    if (mStateToClass != null) {
      printer.println("EQUIVALENCE CLASSES");
      for (int s = 0; s < mStateToClass.length; s++) {
        final EquivalenceClass clazz = mStateToClass[s];
        if (clazz != null) {
          printer.print(s);
          printer.print(" : ");
          clazz.dump(printer);
          printer.println();
        }
      }
    }
  }

  @SuppressWarnings("unused")
  private void checkIntegrity()
  {
    if (mStateToClass != null) {
      for (int s = 0; s < mStateToClass.length; s++) {
        final EquivalenceClass clazz = mStateToClass[s];
        if (clazz != null) {
          clazz.checkIntegrity();
        }
      }
    }
    for (final EquivalenceClass splitter : mExternalSplittters) {
      splitter.checkIntegrity();
    }
    for (final EquivalenceClass splitter : mInternalSplittters) {
      splitter.checkIntegrity();
    }
    for (final EquivalenceClass splitter : mActiveExitSplittters) {
      splitter.checkIntegrity();
    }
  }


  //#########################################################################
  //# Inner Class EquivalenceClass
  /**
   * <P>A candidate class of incoming equivalent states.</P>
   *
   * <P>A candidate equivalence class is represented as a list of state
   * numbers.</P>
   *
   * <P>Initially, the algorithm creates two candidate equivalence classes
   * consisting of all initial states and all non-initial states. These are
   * split repeatedly until only classes of incoming equivalent states remain.
   * If an equivalence class consists of only a single state, it is
   * removed as nothing can be merged within this class.</P>
   *
   * <P>All candidate equivalence classes are recorded in the array {@link
   * ActiveEventsTRSimplifier#StateToClass mStateToClass}, which
   * maintains a map from state codes (of merged classes) to their
   * candidate classes. Classes consisting of a single state are represented
   * by a <CODE>null</CODE> entry in the array.</P>
   */
  private class EquivalenceClass implements Comparable<EquivalenceClass>
  {
    //#######################################################################
    //# Constructors
    private EquivalenceClass(final int active)
    {
      mSize = 0;
      mList = mListBuffer.createList();
      mFirstState = Integer.MAX_VALUE;
      mSplitList = IntListBuffer.NULL;
      mSplitSize = -1;
      mOpenEvents = active;
      mIsExternalSplitter = mIsInternalSplitter = false;
    }

    private EquivalenceClass(final int list,
                             final int size,
                             final boolean preds,
                             final int active)
    {
      mSize = size;
      mList = list;
      mSplitList = IntListBuffer.NULL;
      mSplitSize = preds ? 0 : -1;
      mOpenEvents = active;
      mIsExternalSplitter = mIsInternalSplitter = false;
      setUpStateToClass();
    }

    //#######################################################################
    //# Simple Access
    @SuppressWarnings("unused")
    private int size()
    {
      return mSize;
    }

    private int getFirstState()
    {
      return mFirstState;
    }

    private int[] getStates()
    {
      return mListBuffer.toArray(mList);
    }

    //#######################################################################
    //# Interface java.util.Comparable<EquivalenceClass>
    @Override
    public int compareTo(final EquivalenceClass clazz)
    {
      if (mSize != clazz.mSize) {
        return mSize - clazz.mSize;
      } else if (mSize == 0) {
        return 0;
      } else {
        return getFirstState() - clazz.getFirstState();
      }
    }

    //#######################################################################
    //# Initialisation
    private void addState(final int state)
    {
      mListBuffer.append(mList, state);
      mSize++;
      mStateToClass[state] = this;
      if (state < mFirstState) {
        mFirstState = state;
      }
    }

    private void setUpStateToClass()
    {
      mListReadIterator.reset(mList);
      if (mSize == 1) {
        mListReadIterator.advance();
        final int state = mListReadIterator.getCurrentData();
        mFirstState = state;
        mStateToClass[state] = null;
      } else {
        mFirstState = Integer.MAX_VALUE;
        while (mListReadIterator.advance()) {
          final int state = mListReadIterator.getCurrentData();
          mStateToClass[state] = this;
          if (state < mFirstState) {
            mFirstState = state;
          }
        }
      }
    }

    private void dispose()
    {
      mSize = 0;
      mList = IntListBuffer.NULL;
      mListBuffer.dispose(mList);
    }

    //#######################################################################
    //# Splitting
    private void enqueueExternal()
    {
      if (!mIsExternalSplitter && mSetBuffer.size(mOpenEvents) > 0) {
        mExternalSplittters.add(this);
        mIsExternalSplitter = true;
      }
    }

    private void enqueueInternal()
    {
      if (!mIsInternalSplitter && mSetBuffer.size(mOpenEvents) > 0 && mSize > 1) {
        mInternalSplittters.add(this);
        mIsInternalSplitter = true;
      }
    }

    private void enqueueActiveExits()
    {
      if (!mIsActiveExitSplitter && mSetBuffer.size(mActiveExits) > 0 && mSize > 1) {
        mActiveExitSplittters.add(this);
        mIsActiveExitSplitter = true;
      }
    }

    private void dequeue()
    {
      if (mIsExternalSplitter) {
        mExternalSplittters.remove(this);
        mIsExternalSplitter = false;
      }
      if (mIsInternalSplitter) {
        mInternalSplittters.remove(this);
        mIsInternalSplitter = false;
      }
      if (mIsActiveExitSplitter) {
        mActiveExitSplittters.remove(this);
        mIsActiveExitSplitter = false;
      }
    }

    private void splitOtherClasses()
    {
      final ListBufferTransitionRelation rel = getTransitionRelation();
      final int numEvents = rel.getNumberOfProperEvents();
      mSetReadIterator.reset(mOpenEvents);
      if (mSize == 1) {
        final int state = getFirstState();
        while (mSetReadIterator.advance()) {
          final int event = mSetReadIterator.getCurrentData();
          if (event > EventEncoding.TAU && event < numEvents) {
            ActiveEventsTRSimplifier.this.splitOtherClasses(state, event);
            if (mNumProperCandidates == 0) {
              return;
            }
          }
        }
        dispose();
      } else {
        final int numInternal = mSetBuffer.size(mOpenEvents);
        final TIntArrayList open = new TIntArrayList(numInternal);
        final TIntArrayList ambiguous = new TIntArrayList(numInternal);
        while (mSetReadIterator.advance()) {
          final int event = mSetReadIterator.getCurrentData();
          boolean exit = false;
          boolean nonexit = false;
          if (event > EventEncoding.TAU && event < numEvents) {
            mListReadIterator.reset(mList);
            while (mListReadIterator.advance()) {
              final int state = mListReadIterator.getCurrentData();
              final byte status =
                ActiveEventsTRSimplifier.this.splitOtherClasses(state, event);
              if ((status & INTERNAL) != 0) {
                open.add(event);
              }
              if ((status & EXIT) == 0) {
                nonexit = true;
              } else {
                exit = true;
              }
            }
            if (exit && nonexit) {
              ambiguous.add(event);
            }
          }
        }
        mOpenEvents = mSetBuffer.add(open);
        mActiveExits = mSetBuffer.add(ambiguous);
        enqueueInternal();
      }
      mIsExternalSplitter = false;
    }

    private boolean splitSameClass()
    {
      mIsInternalSplitter = false;
      if (mSize > 1 && mSetBuffer.size(mOpenEvents) > 0) {
        mSetReadIterator.reset(mOpenEvents);
        final int[] states = mListBuffer.toArray(mList);
        while (mSetReadIterator.advance()) {
          final int event = mSetReadIterator.getCurrentData();
          mFullEventClosureIterator.resetEvent(event);
          for (final int source : states) {
            mFullEventClosureIterator.resume(source);
            while (mFullEventClosureIterator.advance()) {
              final int target =
                mFullEventClosureIterator.getCurrentTargetState();
              if (mStateToClass[target] == this) {
                moveToSplitList(target);
              }
            }
          }
          if (splitUsingSplitList()) {
            return true;
          }
        }
      }
      enqueueActiveExits();
      return false;
    }

    private boolean splitOnActiveExits()
    {
      mIsActiveExitSplitter = false;
      if (mSize > 1 && mSetBuffer.size(mActiveExits) > 0) {
        mSetReadIterator.reset(mActiveExits);
        final int[] states = mListBuffer.toArray(mList);
        while (mSetReadIterator.advance()) {
          final int event = mSetReadIterator.getCurrentData();
          mFullEventClosureIterator.resetEvent(event);
          for (final int source : states) {
            mFullEventClosureIterator.resetState(source);
            while (mFullEventClosureIterator.advance()) {
              final int target =
                mFullEventClosureIterator.getCurrentTargetState();
              if (mStateToClass[target] != this) {
                moveToSplitList(source);
                break;
              }
            }
          }
          if (splitUsingSplitList()) {
            return true;
          }
        }
      }
      return false;
    }

    private void moveToSplitList(final int state)
    {
      final int tail;
      switch (mSplitSize) {
      case -1:
        setUpPredecessors();
        // fall through ...
      case 0:
        mSplitList = mListBuffer.createList();
        mSplitSize = 1;
        tail = IntListBuffer.NULL;
        break;
      default:
        mSplitSize++;
        tail = mListBuffer.getTail(mSplitList);
        break;
      }
      final int pred = mPredecessors[state];
      mPredecessors[state] = tail;
      mListWriteIterator.reset(mList, pred);
      mListWriteIterator.advance();
      mListWriteIterator.moveTo(mSplitList);
      if (mListWriteIterator.advance()) {
        final int next = mListWriteIterator.getCurrentData();
        mPredecessors[next] = pred;
      }
    }

    private boolean splitUsingSplitList()
    {
      if (mSplitSize <= 0) {
        return false;
      } else if (mSplitSize == mSize) {
        mList = mSplitList;
        mSplitSize = 0;
        mSplitList = IntListBuffer.NULL;
        return false;
      } else {
        doSimpleSplit(mSplitList, mSplitSize, mSplitSize >= 0);
        mSplitSize = 0;
        mSplitList = IntListBuffer.NULL;
        return true;
      }
    }

    private void doSimpleSplit(final int splitList,
                               final int splitSize,
                               final boolean preds)
    {
      dequeue();
      final int newSize = mSize - splitSize;
      final EquivalenceClass splitClass =
        new EquivalenceClass(splitList, splitSize, preds, mOpenEvents);
      mSize = newSize;
      if (mSize == 1) {
        mNumProperCandidates--;
      }
      setUpStateToClass();
      if (mSplitSize > 1) {
        mNumProperCandidates++;
      }
      enqueueExternal();
      splitClass.enqueueExternal();
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

    private void dump(final PrintWriter printer)
    {
      mListBuffer.dumpList(printer, mList);
      if (mSplitList != IntListBuffer.NULL) {
        printer.write('+');
        mListBuffer.dumpList(printer, mSplitList);
      }
    }

    private void checkIntegrity()
    {
      assert mSize == mListBuffer.getLength(mList);
      assert mListBuffer.contains(mList, mFirstState);
    }

    //#######################################################################
    //# Data Members
    /**
     * The number of states in this candidate equivalence class.
     */
    private int mSize;
    /**
     * The list of states constituting this candidate equivalence class,
     * represented as list identifier in {@link
     * ActiveEvents'TRSimplifier#mListBuffer}.
     */
    private int mList;
    /**
     * The smallest state number that belongs to this class.
     */
    private int mFirstState;
    /**
     * A list of states to be split off from this class,
     * represented as list identifier in {@link
     * ActiveEvents'TRSimplifier#mListBuffer}.
     */
    private int mSplitList;
    /**
     * The number of states in the {@link #mSplitList}.
     */
    private int mSplitSize;
    /**
     * A set of events originating from this class that require further
     * splitting on.
     */
    private int mOpenEvents;
    /**
     * A set of events originating from this class, where it is known that
     * some states in this class can reach another class via these events,
     * while other states cannot. Splitting may be required to ensure the
     * condition of equal sets of active events leading to a different class.
     */
    private int mActiveExits;
    /**
     * Whether the this equivalence class is in the queue
     * {@link #mExternalSplittters}.
     */
    private boolean mIsExternalSplitter;
    /**
     * Whether the this equivalence class is in the queue
     * {@link #mInternalSplittters}.
     */
    private boolean mIsInternalSplitter;
    /**
     * Whether the this equivalence class is in the queue
     * {@link #mActiveExitSplittters}.
     */
    private boolean mIsActiveExitSplitter;
  }


  //#########################################################################
  //# Data Members
  // Configuration
  private int mTransitionLimit = Integer.MAX_VALUE;

  // Active Events
  /**
   * Set buffer containing active event sets. Each computed active event
   * set has an entry in this set buffer.
   */
  private IntSetBuffer mSetBuffer;
  /**
   * The indexes of the active event sets of each state,
   * only containing proper events, no special tokens for initial or
   * marked states.
   */
  private int[] mActiveEventSets;
  /**
   * Maps each set index that appears in {@link #mActiveEventSets} to
   * the number of times it is appears.
   */
  private TIntIntHashMap mActiveEventCounts;

  // Equivalence Classes
  /**
   * The number of equivalence classes with more than one state.
   * Splitting is stopped when this number reaches zero.
   */
  private int mNumProperCandidates;
  /**
   * Map of states to equivalence classes.
   * States with a non-trivial class (class containing at least two states)
   * have an entry in this array, other states have <CODE>null</CODE> entries.
   */
  private EquivalenceClass[] mStateToClass;
  /**
   * Array of predecessors indexes in {@link #mListBuffer}.
   * This array maps each state that appears in the list of an equivalence
   * class candidate to its predecessor in that list, to facilitate moving
   * states to a split list when classes are split.
   */
  private int[] mPredecessors;
  /**
   * Queue of equivalence classes whose states are yet to be processed
   * to split other equivalence classes.
   */
  private Queue<EquivalenceClass> mExternalSplittters;
  /**
   * Queue of equivalence classes that are yet to be processed
   * to split themselves.
   */
  private Queue<EquivalenceClass> mInternalSplittters;
  /**
   * Queue of equivalence classes that are yet to be processed
   * for splitting into states with and without outgoing active events to
   * a different class.
   */
  private Queue<EquivalenceClass> mActiveExitSplittters;

  // Tools
  private TauClosure mTauClosure;
  private TransitionIterator mFullEventClosureIterator;
  private IntListBuffer mListBuffer;
  private IntListBuffer.ReadOnlyIterator mListReadIterator;
  private IntListBuffer.ModifyingIterator mListWriteIterator;
  private IntSetBuffer.IntSetIterator mSetReadIterator;


  //#########################################################################
  //# Class Constants
  private static final int OMEGA = EventEncoding.TAU;

  private static final byte EXIT = 0x01;
  private static final byte INTERNAL = 0x02;

}
