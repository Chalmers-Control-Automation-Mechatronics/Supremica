//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters Analysis
//# PACKAGE: net.sourceforge.waters.analysis.abstraction
//# CLASS:   IncomingEquivalenceTRSimplifier
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.analysis.abstraction;

import gnu.trove.list.array.TIntArrayList;
import gnu.trove.procedure.TIntProcedure;
import gnu.trove.set.hash.TIntHashSet;
import gnu.trove.stack.TIntStack;
import gnu.trove.stack.array.TIntArrayStack;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;

import net.sourceforge.waters.analysis.tr.EventEncoding;
import net.sourceforge.waters.analysis.tr.IntListBuffer;
import net.sourceforge.waters.analysis.tr.ListBufferTransitionRelation;
import net.sourceforge.waters.analysis.tr.TRPartition;
import net.sourceforge.waters.analysis.tr.TauClosure;
import net.sourceforge.waters.analysis.tr.TransitionIterator;
import net.sourceforge.waters.analysis.tr.WatersIntHashingStrategy;
import net.sourceforge.waters.analysis.tr.WatersIntIntHashMap;
import net.sourceforge.waters.model.analysis.AnalysisException;
import net.sourceforge.waters.model.des.AutomatonTools;


/**
 * <P>A combined implementation of the <I>Silent Continuation Rule</I> and
 * <I>Active Events Rule</I>.</P>
 *
 * <P>This rule merges all states that are incoming equivalent and have
 * at least one outgoing silent transition, and all states that are incoming
 * equivalent and have equal sets of eligible events.</P>
 *
 * <P><I>Reference:</I> Hugo Flordal, Robi Malik. Compositional Verification
 * in Supervisory Control. SIAM Journal of Control and Optimization,
 * 48(3), 1914-1938, 2009.</P>
 *
 * @author Robi Malik
 */

public class IncomingEquivalenceTRSimplifier
  extends AbstractMarkingTRSimplifier
{

  //#######################################################################
  //# Constructors
  public IncomingEquivalenceTRSimplifier()
  {
  }

  public IncomingEquivalenceTRSimplifier(final ListBufferTransitionRelation rel)
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
  //# Interface
  //# net.sourceforge.waters.analysis.abstraction.TransitionRelationSimplifier
  @Override
  public int getPreferredInputConfiguration()
  {
    return ListBufferTransitionRelation.CONFIG_ALL;
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
    final int numStates = rel.getNumberOfStates();
    mEventShift = AutomatonTools.log2(numStates);
    mListBuffer = new IntListBuffer();
    mStateListReadIterator = mListBuffer.createReadOnlyIterator();
    mClassListReadIterator = mListBuffer.createReadOnlyIterator();
    mClassListWriteIterator = mListBuffer.createModifyingIterator();
    mNewList = mListBuffer.createList();
    mStack1 = new TIntArrayStack();
    mStack2 = new TIntArrayStack();
    mEvents0 = new TIntHashSet();
    mEvents1 = new TIntHashSet();
    mPredecessorsTauClosure =
      rel.createPredecessorsTauClosure(mTransitionLimit);
    mPredecessorsTauClosureIterator = mPredecessorsTauClosure.createIterator();
    mPredecessorsPreEventClosureIterator =
      mPredecessorsTauClosure.createPreEventClosureIterator();
    mPredecessorsFullEventClosureIterator =
      mPredecessorsTauClosure.createFullEventClosureIterator(-1);
    mSuccessorsTauClosure = rel.createSuccessorsTauClosure(0);
    mSuccessorsTauIterator = mSuccessorsTauClosure.createIterator();
    mSuccessorsEventIterator1 = rel.createSuccessorsReadOnlyIterator();
    mSuccessorsEventIterator2 = rel.createSuccessorsReadOnlyIterator();
  }

  @Override
  protected boolean runSimplifier()
  throws AnalysisException
  {
    final ListBufferTransitionRelation rel = getTransitionRelation();
    final int numStates = rel.getNumberOfStates();
    mStateClasses = new ClassInfo[numStates];
    for (int state = 0; state < numStates; state++) {
      if (rel.isReachable(state)) {
        mStateClasses[state] = new ClassInfo(state);
      }
    }
    checkAbort();

    final TIntArrayList mergeCandidates = new TIntArrayList();
    final WatersIntHashingStrategy strategy = new IncomingEquivalenceHash();
    mIncomingEquivalenceMap =
      new WatersIntIntHashMap(2 * numStates, IntListBuffer.NULL, strategy);
    boolean trivial = true;
    boolean change;
    do {
      change = false;
      for (int state = 0; state < numStates; state++) {
        final ClassInfo info = mStateClasses[state];
        if (info != null && info.getFirstState() == state) {
          final int list = addToListMap(mIncomingEquivalenceMap, state);
          if (list != IntListBuffer.NULL &&
              !mListBuffer.isStrictlyLongerThan(list, 2)) {
            mergeCandidates.add(list);
          }
        }
      }
      mIncomingEquivalenceMap.clear();
      checkAbort();
      change = false;
      final int numCandidates = mergeCandidates.size();
      for (int i = 0; i < numCandidates; i++) {
        final int list = mergeCandidates.get(i);
        change |= attemptMerge(list);
      }
      mergeCandidates.clear();
      trivial &= !change;
      checkAbort();
    } while (change);

    if (trivial) {
      setResultPartition(null);
      return false;
    } else {
      final List<int[]> classes = new ArrayList<>();
      final int[] codes = new int[numStates];
      final int[] offsets = new int[numStates];
      for (int state = 0; state < numStates; state++) {
        final ClassInfo info = mStateClasses[state];
        if (info != null) {
          final int root = info.getFirstState();
          if (root == state) {
            final int size = info.size();
            final int[] clazz = new int[size];
            clazz[0] = root;
            codes[root] = classes.size();
            offsets[root] = 1;
            classes.add(clazz);
          } else {
            final int code = codes[root];
            final int[] clazz = classes.get(code);
            final int offset = offsets[root]++;
            clazz[offset] = state;
          }
        }
      }
      final TRPartition partition = new TRPartition(classes, numStates);
      setResultPartition(partition);
      applyResultPartitionAutomatically();
      return true;
    }
  }

  @Override
  protected void tearDown()
  {
    super.tearDown();
    mListBuffer = null;
    mStateListReadIterator = mClassListReadIterator =
      mClassListWriteIterator = null;
    mStack1 = mStack2 = null;
    mEvents0 = mEvents1 = null;
    mPredecessorsTauClosure = mSuccessorsTauClosure = null;
    mPredecessorsTauClosureIterator = mPredecessorsPreEventClosureIterator =
      mPredecessorsFullEventClosureIterator =
      mSuccessorsTauIterator = mSuccessorsEventIterator1 =
      mSuccessorsEventIterator2 = null;
    mStateClasses = null;
    mIncomingEquivalenceMap = mActiveEventsMap = null;
  }

  @Override
  protected void applyResultPartition()
    throws AnalysisException
  {
    super.applyResultPartition();
    final ListBufferTransitionRelation rel = getTransitionRelation();
    rel.removeTauSelfLoops();
    removeProperSelfLoopEvents();
  }


  //#########################################################################
  //# Algorithm
  private boolean attemptMerge(final int list)
  {
    if (mListBuffer.isStrictlyLongerThan(list, 1)) {
      if (mActiveEventsMap == null) {
        mActiveEventsMap =
          new WatersIntIntHashMap(0, IntListBuffer.NULL, mActiveEventsHash);
      }
      mClassListWriteIterator.reset(list);
      while (mClassListWriteIterator.advance()) {
        moveToListMap(mActiveEventsMap, mClassListWriteIterator);
      }
      mActiveEventsHash.reset(list);
      mActiveEventsMap.forEachValue(mActiveEventsHash);
      mActiveEventsMap.clear();
      mActiveEventsHash.mergeList(list, false);
      return mActiveEventsHash.hasMerged();
    } else {
      return false;
    }
  }

  //#########################################################################
  //# Auxiliary Methods
  private int addToListMap(final WatersIntIntHashMap map, final int state)
  {
    final int oldlist = map.putIfAbsent(state, mNewList);
    if (oldlist == IntListBuffer.NULL) {
      mListBuffer.append(mNewList, state);
      mNewList = mListBuffer.createList();
      return IntListBuffer.NULL;
    } else {
      mListBuffer.append(oldlist, state);
      return oldlist;
    }
  }

  private boolean moveToListMap(final WatersIntIntHashMap map,
                                final IntListBuffer.ModifyingIterator iter)
  {
    final int state = iter.getCurrentData();
    final int oldlist = map.putIfAbsent(state, mNewList);
    if (oldlist == IntListBuffer.NULL) {
      iter.moveTo(mNewList);
      mNewList = mListBuffer.createList();
      return true;
    } else {
      iter.moveTo(oldlist);
      return false;
    }
  }


  //#######################################################################
  //# Inner Class ClassInfo
  private class ClassInfo
  {

    //#######################################################################
    //# Constructors
    /**
     * Initialises an empty class.
     */
    private ClassInfo()
    {
      mStates = mListBuffer.createList();
      mIncomingEquivalenceHashCode = mActiveEventsHashCode = -1;
      mHasOutgoingTau = null;
    }

    /**
     * Initialises a class that contains a single state.
     */
    private ClassInfo(final int state)
    {
      this();
      mListBuffer.append(mStates, state);
    }

    //#######################################################################
    //# Simple Access
    /**
     * Returns the state ID of the first state in this class.
     * This is the smallest state number in the class, which uniquely
     * identifies it.
     */
    private int getFirstState()
    {
      return mListBuffer.getFirst(mStates);
    }

    /**
     * Returns the number of states in this class.
     */
    private int size()
    {
      return mListBuffer.getLength(mStates);
    }

    /**
     * Returns whether at least one state in this class has an outgoing
     * tau-transition to a class other than this class. If this is true,
     * the merged class has an outgoing tau-transition and can be merged
     * into incoming equivalent class that also has an outgoing
     * tau-transition.
     */
    private boolean hasOutgoingTau()
    {
      if (mHasOutgoingTau == null) {
        mStateListReadIterator.reset(mStates);
        while (mStateListReadIterator.advance()) {
          final int state = mStateListReadIterator.getCurrentData();
          mSuccessorsTauIterator.resetState(state);
          mSuccessorsTauIterator.advance();
          while (mSuccessorsTauIterator.advance()) {
            final int succ = mSuccessorsTauIterator.getCurrentTargetState();
            if (mStateClasses[succ] != this) {
              mHasOutgoingTau = true;
              return true;
            }
          }
        }
        mHasOutgoingTau = false;
      }
      return mHasOutgoingTau;
    }

    //#######################################################################
    //# Hash Codes
    /**
     * Returns a hash code that identifies this class with respect to
     * incoming equivalence. As all classes consist of incoming equivalent
     * states, this hash code can be computed by checking only the
     * predecessors of its first state.
     */
    private int incomingEquivalenceHashCode()
    {
      if (mIncomingEquivalenceHashCode < 0) {
        final TIntHashSet keys = new TIntHashSet();
        final ListBufferTransitionRelation rel = getTransitionRelation();
        final int root = getFirstState();
        boolean init = false;
        int result = 0;
        mPredecessorsTauClosureIterator.resetState(root);
        mPredecessorsPreEventClosureIterator.resetEvents(EventEncoding.NONTAU,
                                                         Integer.MAX_VALUE);
        while (mPredecessorsTauClosureIterator.advance()) {
          final int taupred =
            mPredecessorsTauClosureIterator.getCurrentSourceState();
          if (!init && rel.isInitial(taupred)) {
            init = true;
            result += INIT_HASH;
          }
          mPredecessorsPreEventClosureIterator.resume(taupred);
          while (mPredecessorsPreEventClosureIterator.advance()) {
            final int pred =
              mPredecessorsPreEventClosureIterator.getCurrentSourceState();
            final int event =
              mPredecessorsPreEventClosureIterator.getCurrentEvent();
            final ClassInfo info = mStateClasses[pred];
            final int key = info.getFirstState() | (event << mEventShift);
            if (keys.add(key)) {
              result += key * key;
            }
          }
        }
        result *= 31;
        mIncomingEquivalenceHashCode = result < 0 ? -result : result;
      }
      return mIncomingEquivalenceHashCode;
    }

    /**
     * Determines whether the states in this class are incoming equivalent
     * to the given state.
     */
    @SuppressWarnings("unused")
    private boolean newIncomingEquivalenceEquals(final int root1)
    {
      final ClassInfo info1 = mStateClasses[root1];
      if (info1 == this) {
        return true;
      } else if (incomingEquivalenceHashCode() !=
                 info1.incomingEquivalenceHashCode()) {
        return false;
      }
      final ListBufferTransitionRelation rel = getTransitionRelation();
      final int numEvents = rel.getNumberOfProperEvents();
      final int root0 = getFirstState();
      boolean init0 = false;
      mPredecessorsTauClosureIterator.resetState(root0);
      while (mPredecessorsTauClosureIterator.advance()) {
        final int taupred0 =
          mPredecessorsTauClosureIterator.getCurrentSourceState();
        if (rel.isInitial(taupred0)) {
          init0 = true;
          break;
        }
      }
      mPredecessorsTauClosureIterator.resetState(root1);
      while (mPredecessorsTauClosureIterator.advance()) {
        final int taupred1 =
          mPredecessorsTauClosureIterator.getCurrentSourceState();
        if (rel.isInitial(taupred1)) {
          if (init0) {
            init0 = false;
            break;
          } else {
            return false;
          }
        }
      }
      if (init0) {
        return false;
      }
      for (int event = EventEncoding.NONTAU; event < numEvents; event++) {
        final TIntHashSet preds0 = new TIntHashSet();
        mPredecessorsFullEventClosureIterator.reset(root0, event);
        while (mPredecessorsFullEventClosureIterator.advance()) {
          final int pred0 =
            mPredecessorsFullEventClosureIterator.getCurrentSourceState();
          final ClassInfo info0 = mStateClasses[pred0];
          final int code0 = info0.getFirstState();
          preds0.add(code0);
        }
        final int numPreds = preds0.size();
        final TIntHashSet preds1 = new TIntHashSet(numPreds);
        mPredecessorsFullEventClosureIterator.reset(root1, event);
        while (mPredecessorsFullEventClosureIterator.advance()) {
          final int pred1 =
            mPredecessorsFullEventClosureIterator.getCurrentSourceState();
          final ClassInfo predinfo1 = mStateClasses[pred1];
          final int code1 = predinfo1.getFirstState();
          if (preds1.add(code1) && !preds0.contains(code1)) {
            return false;
          }
        }
        if (preds1.size() != numPreds) {
          return false;
        }
      }
      return true;
    }

    /**
     * Determines whether the states in this class are incoming equivalent
     * to the given state.
     */
    private boolean incomingEquivalenceEquals(final int root1)
    {
      final ClassInfo info1 = mStateClasses[root1];
      if (info1 == this) {
        return true;
      } else if (incomingEquivalenceHashCode() !=
                 info1.incomingEquivalenceHashCode()) {
        return false;
      }
      mPredecessorsPreEventClosureIterator.resetEvents(EventEncoding.NONTAU,
                                                       Integer.MAX_VALUE);
      final ListBufferTransitionRelation rel = getTransitionRelation();
      final TIntHashSet keys0 = new TIntHashSet();
      boolean init0 = false;
      final int root0 = getFirstState();
      mPredecessorsTauClosureIterator.resetState(root0);
      while (mPredecessorsTauClosureIterator.advance()) {
        final int taupred0 =
          mPredecessorsTauClosureIterator.getCurrentSourceState();
        init0 |= rel.isInitial(taupred0);
        mPredecessorsPreEventClosureIterator.resume(taupred0);
        while (mPredecessorsPreEventClosureIterator.advance()) {
          final int pred0 =
            mPredecessorsPreEventClosureIterator.getCurrentSourceState();
          final int event0 =
            mPredecessorsPreEventClosureIterator.getCurrentEvent();
          final ClassInfo info0 = mStateClasses[pred0];
          final int key0 = info0.getFirstState() | (event0 << mEventShift);
          keys0.add(key0);
        }
      }
      mPredecessorsPreEventClosureIterator.reset();
      final int numKeys0 = keys0.size();
      final TIntHashSet keys1 = new TIntHashSet(numKeys0);
      boolean init1 = false;
      mPredecessorsTauClosureIterator.resetState(root1);
      while (mPredecessorsTauClosureIterator.advance()) {
        final int taupred1 =
          mPredecessorsTauClosureIterator.getCurrentSourceState();
        if (!init1 && rel.isInitial(taupred1)) {
          if (init0) {
            init1 = true;
          } else {
            return false;
          }
        }
        mPredecessorsPreEventClosureIterator.resume(taupred1);
        while (mPredecessorsPreEventClosureIterator.advance()) {
          final int pred1 =
            mPredecessorsPreEventClosureIterator.getCurrentSourceState();
          final int event1 =
            mPredecessorsPreEventClosureIterator.getCurrentEvent();
          final ClassInfo predinfo1 = mStateClasses[pred1];
          final int key1 = predinfo1.getFirstState() | (event1 << mEventShift);
          if (keys1.add(key1) && !keys0.contains(key1)) {
            return false;
          }
        }
      }
      return init0 == init1 && numKeys0 == keys1.size();
    }


    /**
     * Returns a hash code that identifies this class with respect to
     * active events. The set of active events is determined by exploring
     * the successors of all states in this class.
     */
    private int activeEventsHashCode()
    {
      if (mActiveEventsHashCode < 0) {
        final TIntHashSet visited = new TIntHashSet();
        final ListBufferTransitionRelation rel = getTransitionRelation();
        final int tau = EventEncoding.TAU;
        final int omega = getDefaultMarkingID();
        boolean marked = false;
        int result = 0;
        mStateListReadIterator.reset(mStates);
        while (mStateListReadIterator.advance()) {
          final int state = mStateListReadIterator.getCurrentData();
          mStack1.push(state);
          while (mStack1.size() > 0) {
            final int tausucc = mStack1.pop();
            if (!marked && rel.isMarked(tausucc, omega)) {
              marked = true;
              result += OMEGA_HASH;
            }
            mSuccessorsEventIterator1.resetState(tausucc);
            while (mSuccessorsEventIterator1.advance()) {
              final int event = mSuccessorsEventIterator1.getCurrentEvent();
              if (event == tau) {
                final int succ =
                  mSuccessorsEventIterator1.getCurrentTargetState();
                if (visited.add(succ)) {
                  mStack1.push(succ);
                }
              } else if (mEvents0.add(event)) {
                result += event * event;
              }
            }
          }
        }
        result *= 31;
        mEvents0.clear();
        mActiveEventsHashCode = result < 0 ? -result : result;
      }
      return mActiveEventsHashCode;
    }

    /**
     * Determines whether the states in this class have the same active
     * events as the class of the given other state.
     */
    private boolean activeEventsEquals(final int root1)
    {
      final ClassInfo info1 = mStateClasses[root1];
      if (info1 == this) {
        return true;
      } else if (activeEventsHashCode() != info1.activeEventsHashCode()) {
        return false;
      }
      final ListBufferTransitionRelation rel = getTransitionRelation();
      final int tau = EventEncoding.TAU;
      final int omega = getDefaultMarkingID();
      try {
        final TIntHashSet visited = new TIntHashSet();
        mSuccessorsEventIterator1.resetEvent(-1);
        mStateListReadIterator.reset(mStates);
        while (mStateListReadIterator.advance()) {
          final int state0 = mStateListReadIterator.getCurrentData();
          mStack1.push(state0);
          while (mStack1.size() > 0) {
            final int tausucc0 = mStack1.pop();
            if (rel.isMarked(tausucc0, omega)) {
              mEvents0.add(OMEGA_EVENT);
            }
            mSuccessorsEventIterator1.resetState(tausucc0);
            while (mSuccessorsEventIterator1.advance()) {
              final int event0 = mSuccessorsEventIterator1.getCurrentEvent();
              if (event0 == tau) {
                final int succ0 =
                  mSuccessorsEventIterator1.getCurrentTargetState();
                if (visited.add(succ0)) {
                  mStack1.push(succ0);
                }
              } else {
                mEvents0.add(event0);
              }
            }
          }
        }
        visited.clear();
        mStateListReadIterator.reset(info1.mStates);
        while (mStateListReadIterator.advance()) {
          final int state1 = mStateListReadIterator.getCurrentData();
          mStack1.push(state1);
          while (mStack1.size() > 0) {
            final int tausucc1 = mStack1.pop();
            if (rel.isMarked(tausucc1, omega)) {
              if (!mEvents0.contains(OMEGA_EVENT)) {
                return false;
              } else {
                mEvents1.add(OMEGA_EVENT);
              }
            }
            mSuccessorsEventIterator1.resetState(tausucc1);
            while (mSuccessorsEventIterator1.advance()) {
              final int event1 = mSuccessorsEventIterator1.getCurrentEvent();
              if (event1 == tau) {
                final int succ1 =
                  mSuccessorsEventIterator1.getCurrentTargetState();
                if (visited.add(succ1)) {
                  mStack1.push(succ1);
                }
              } else if (mEvents1.add(event1) && !mEvents0.contains(event1)) {
                return false;
              }
            }
          }
        }
        return mEvents0.size() == mEvents1.size();
      } finally {
        mEvents0.clear();
        mEvents1.clear();
        mStack1.clear();
      }
    }

    //#######################################################################
    //# Merging
    /**
     * Merges the given other class into this class.
     * @param  info   Class to be merged into this class.
     *                All states from the given class into this class.
     *                The other class becomes invalid and should no longer
     *                be used after this operation.
     * @param  activeEventsHashCode  Whether the active events sets of the
     *                merged classes are known to be equal. If this is
     *                <CODE>false</CODE>, any cached hash code will be
     *                cleared.
     */
    private void merge(final ClassInfo info, final boolean activeEventsEquals)
    {
      mStateListReadIterator.reset(info.mStates);
      while (mStateListReadIterator.advance()) {
        final int state = mStateListReadIterator.getCurrentData();
        mStateClasses[state] = this;
      }
      if (getFirstState() < info.getFirstState()) {
        mStates = mListBuffer.catenateDestructively(mStates, info.mStates);
      } else {
        mStates = mListBuffer.catenateDestructively(info.mStates, mStates);
      }
      if (!activeEventsEquals) {
        mActiveEventsHashCode = -1;
      }
      if (mHasOutgoingTau != null) {
        if (info.mHasOutgoingTau == null) {
          mHasOutgoingTau = null;
        } else if (mHasOutgoingTau != info.mHasOutgoingTau) {
          mHasOutgoingTau = null;
        }
      }
    }

    /**
     * Notifies all successors of states in this class that some of their
     * predecessors have been merged, so their incoming equivalence hash code
     * needs to be re-evaluated. This method is called after merging of a
     * class to reset cached values.
     */
    private void setIncomingEquivalenceChanged()
    {
      final int tau = EventEncoding.TAU;
      final TIntHashSet visited1 = new TIntHashSet();
      final TIntHashSet visited2 = new TIntHashSet();
      mSuccessorsEventIterator1.resetEvent(-1);
      mSuccessorsEventIterator2.resetEvent(tau);
      mStateListReadIterator.reset(mStates);
      while (mStateListReadIterator.advance()) {
        final int root = mStateListReadIterator.getCurrentData();
        if (visited1.add(root)) {
          mStack1.push(root);
          while (mStack1.size() > 0) {
            final int state1 = mStack1.pop();
            mSuccessorsEventIterator1.resetState(state1);
            while (mSuccessorsEventIterator1.advance()) {
              final int succ1 =
                mSuccessorsEventIterator1.getCurrentTargetState();
              if (mSuccessorsEventIterator1.getCurrentEvent() == tau) {
                if (visited1.add(succ1)) {
                  mStack1.push(succ1);
                }
              } else if (visited2.add(succ1)) {
                mStack2.push(succ1);
                while (mStack2.size() > 0) {
                  final int state2 = mStack2.pop();
                  final ClassInfo info2 = mStateClasses[state2];
                  info2.mIncomingEquivalenceHashCode = -1;
                  mSuccessorsEventIterator2.resetState(state2);
                  while (mSuccessorsEventIterator2.advance()) {
                    final int succ2 =
                      mSuccessorsEventIterator2.getCurrentTargetState();
                    if (visited2.add(succ2)) {
                      mStack2.push(succ2);
                    }
                  }
                }
              }
            }
          }
        }
      }
    }

    //#######################################################################
    //# Debugging
    @Override
    public String toString()
    {
      final StringWriter writer = new StringWriter();
      final PrintWriter printer = new PrintWriter(writer);
      mListBuffer.dumpList(printer, mStates);
      return writer.toString();
    }

    //#######################################################################
    //# Data Members
    /**
     * List of states in the class, smallest first.
     */
    private int mStates;
    /**
     * Cached incoming equivalence hash code, or -1.
     */
    private int mIncomingEquivalenceHashCode;
    /**
     * Cached active events code, or -1.
     */
    private int mActiveEventsHashCode;
    /**
     * Cached result indicating whether at least one state in this class has
     * an outgoing tau-transition.
     */
    private Boolean mHasOutgoingTau;

  }


  //#########################################################################
  //# Inner Class IncomingEquivalenceHash
  private class IncomingEquivalenceHash implements WatersIntHashingStrategy
  {

    //#######################################################################
    //# Interface net.sourceforge.waters.analysis.abstraction.WatersIntHashingStrategy
    @Override
    public int computeHashCode(final int root)
    {
      final ClassInfo info = mStateClasses[root];
      return info.incomingEquivalenceHashCode();
    }

    @Override
    public boolean equals(final int root1, final int root2)
    {
      final ClassInfo info = mStateClasses[root1];
      return info.incomingEquivalenceEquals(root2);
    }

    //#######################################################################
    //# Class Constants
    private static final long serialVersionUID = 1L;

  }


  //#########################################################################
  //# Inner Class ActiveEventsHash
  private class ActiveEventsHash
    implements WatersIntHashingStrategy, TIntProcedure
  {

    //#######################################################################
    //# Interface net.sourceforge.waters.analysis.abstraction.WatersIntHashingStrategy
    @Override
    public int computeHashCode(final int root)
    {
      final ClassInfo info = mStateClasses[root];
      return info.activeEventsHashCode();
    }

    @Override
    public boolean equals(final int root1, final int root2)
    {
      final ClassInfo info = mStateClasses[root1];
      return info.activeEventsEquals(root2);
    }

    //#######################################################################
    //# Interface gnu.trove.TIntProcedure
    @Override
    public boolean execute(final int list)
    {
      final ClassInfo root = mergeList(list, true);
      if (root.hasOutgoingTau()) {
        final int state = root.getFirstState();
        mListBuffer.append(mTauCollector, state);
      }
      return true;
    }

    //#######################################################################
    //# Invocation
    void reset(final int tauCollector)
    {
      mTauCollector = tauCollector;
      mHasMerged = false;
    }

    boolean hasMerged()
    {
      return mHasMerged;
    }

    //#######################################################################
    //# Auxiliary Methods
    private ClassInfo mergeList(final int list, final boolean activeEventsEquals)
    {
      if (mListBuffer.isEmpty(list)) {
        return null;
      } else if (!mListBuffer.isStrictlyLongerThan(list, 1)) {
        final int state = mListBuffer.getFirst(list);
        mListBuffer.dispose(list);
        return mStateClasses[state];
      } else {
        mClassListReadIterator.reset(list);
        mClassListReadIterator.advance();
        int state = mClassListReadIterator.getCurrentData();
        final ClassInfo root = mStateClasses[state];
        while (mClassListReadIterator.advance()) {
          state = mClassListReadIterator.getCurrentData();
          final ClassInfo info = mStateClasses[state];
          root.merge(info, activeEventsEquals);
        }
        mListBuffer.dispose(list);
        mHasMerged = true;
        root.setIncomingEquivalenceChanged();
        return root;
      }
    }

    //#######################################################################
    //# Data Members
    private int mTauCollector = IntListBuffer.NULL;
    private boolean mHasMerged = false;

    //#######################################################################
    //# Class Constants
    private static final long serialVersionUID = 1L;

  }


  //#########################################################################
  //# Data Members
  private int mTransitionLimit = Integer.MAX_VALUE;

  private int mEventShift;
  private IntListBuffer mListBuffer;
  private IntListBuffer.Iterator mStateListReadIterator;
  private IntListBuffer.Iterator mClassListReadIterator;
  private IntListBuffer.ModifyingIterator mClassListWriteIterator;
  private int mNewList;
  private TIntStack mStack1;
  private TIntStack mStack2;
  private TIntHashSet mEvents0;
  private TIntHashSet mEvents1;
  private TauClosure mPredecessorsTauClosure;
  private TransitionIterator mPredecessorsTauClosureIterator;
  private TransitionIterator mPredecessorsPreEventClosureIterator;
  private TransitionIterator mPredecessorsFullEventClosureIterator;
  private TauClosure mSuccessorsTauClosure;
  private TransitionIterator mSuccessorsTauIterator;
  private TransitionIterator mSuccessorsEventIterator1;
  private TransitionIterator mSuccessorsEventIterator2;

  private ClassInfo[] mStateClasses;
  private WatersIntIntHashMap mIncomingEquivalenceMap;
  private WatersIntIntHashMap mActiveEventsMap;

  private final ActiveEventsHash mActiveEventsHash = new ActiveEventsHash();


  //#########################################################################
  //# Class Constants
  private static final int INIT_HASH = 0xabababab;
  private static final int OMEGA_HASH = 0xbabababa;
  private static final int OMEGA_EVENT = -1;

}

