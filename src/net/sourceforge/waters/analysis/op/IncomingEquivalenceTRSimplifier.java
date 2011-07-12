//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters Analysis
//# PACKAGE: net.sourceforge.waters.analysis.op
//# CLASS:   ActiveEventsTRSimplifier
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.analysis.op;

import gnu.trove.HashFunctions;
import gnu.trove.TIntHashSet;
import gnu.trove.TIntIntProcedure;
import gnu.trove.TIntProcedure;

import net.sourceforge.waters.analysis.tr.EventEncoding;
import net.sourceforge.waters.analysis.tr.IntListBuffer;
import net.sourceforge.waters.analysis.tr.ListBufferTransitionRelation;
import net.sourceforge.waters.analysis.tr.TauClosure;
import net.sourceforge.waters.analysis.tr.TransitionIterator;
import net.sourceforge.waters.analysis.tr.WatersIntHashingStrategy;
import net.sourceforge.waters.analysis.tr.WatersIntIntHashMap;
import net.sourceforge.waters.model.analysis.AnalysisException;
import net.sourceforge.waters.model.des.AutomatonTools;


/**
 * <P>A combined implementation of the <I>Silent Continuation Rule</I> and
 * <I>Active Events Rule</I> </P>
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

@SuppressWarnings("unused")
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
  //# Interface net.sourceforge.waters.analysis.op.TransitionRelationSimplifier
  @Override
  public int getPreferredInputConfiguration()
  {
    return ListBufferTransitionRelation.CONFIG_ALL;
  }


  //#########################################################################
  //# Overrides for net.sourceforge.waters.analysis.op.AbstractTRSimplifier
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
    mCurrentSet0 = new TIntHashSet();
    mCurrentSet1 = new TIntHashSet();
    mPredecessorsTauClosure =
      rel.createPredecessorsTauClosure(mTransitionLimit);
    mPredecessorsTauClosureIterator = mPredecessorsTauClosure.createIterator();
    mPredecessorsEventClosureIterator =
      mPredecessorsTauClosure.createFullEventClosureIterator();
  }

  @Override
  protected boolean runSimplifier()
  throws AnalysisException
  {
    final ListBufferTransitionRelation rel = getTransitionRelation();
    final int numStates = rel.getNumberOfStates();
    for (int state = 0; state < numStates; state++) {
      if (rel.isReachable(state)) {
        mStateClasses[state] = new ClassInfo(state);
      }
    }
    checkAbort();

    final WatersIntHashingStrategy strategy = new IncomingEquivalenceHash();
    mIncomingEquivalenceMap =
      new WatersIntIntHashMap(numStates, IntListBuffer.NULL, strategy);
    int newlist = mListBuffer.createList();
    for (int state = 0; state < numStates; state++) {
      final ClassInfo info = mStateClasses[state];
      // No need for info.getFirstState() == state in initial setup
      if (info != null && info.getFirstState() == state) {
        final int oldlist = mIncomingEquivalenceMap.putIfAbsent(state, newlist);
        if (oldlist == IntListBuffer.NULL) {
          mListBuffer.append(newlist, state);
          newlist = mListBuffer.createList();
        } else {
          mListBuffer.append(oldlist, state);
        }
      }
    }
    mListBuffer.dispose(newlist);

    mIncomingEquivalenceMap.forEachEntry(mActiveEventsHash);

    // TODO

    return true;
  }

  @Override
  protected void tearDown()
  {
    super.tearDown();
    mListBuffer = null;
    mStateListReadIterator = mClassListReadIterator =
      mClassListWriteIterator = null;
    mCurrentSet0 = mCurrentSet1 = null;
    mPredecessorsTauClosure = null;
    mPredecessorsTauClosureIterator = mPredecessorsEventClosureIterator = null;
    mStateClasses = null;
    mIncomingEquivalenceMap = null;
    mSuccessorsTauClosure = null;
    mSuccessorsEventIterator = mSuccessorsPreEventClosureIterator =
      mSuccessorsFullEventClosureIterator = null;
  }

  @Override
  protected void applyResultPartition()
    throws AnalysisException
  {
    super.applyResultPartition();
    final ListBufferTransitionRelation rel = getTransitionRelation();
    rel.removeTauSelfLoops();
    rel.removeProperSelfLoopEvents();
  }


  //#########################################################################
  //# Auxiliary Methods
  private void setUpSuccessorsTauClosure()
  {
    final ListBufferTransitionRelation rel = getTransitionRelation();
    mSuccessorsTauClosure = rel.createSuccessorsTauClosure(0);
    mSuccessorsEventIterator = rel.createSuccessorsReadOnlyIterator();
    mSuccessorsPreEventClosureIterator =
      mSuccessorsTauClosure.createPreEventClosureIterator();
    mSuccessorsFullEventClosureIterator =
      mSuccessorsTauClosure.createFullEventClosureIterator();
  }

  private boolean isWeaklyInitial(final int state)
  {
    final ListBufferTransitionRelation rel = getTransitionRelation();
    if (rel.isInitial(state)) {
      return true;
    } else {
      mPredecessorsTauClosureIterator.resetState(state);
      while (mPredecessorsTauClosureIterator.advance()) {
        final int pred = mPredecessorsTauClosureIterator.getCurrentSourceState();
        if (rel.isInitial(pred)) {
          return true;
        }
      }
      return false;
    }
  }

  private boolean hasOutgoingTau(final int state)
  {
    mSuccessorsEventIterator.reset(state, EventEncoding.TAU);
    return mSuccessorsEventIterator.advance();
  }


  //#######################################################################
  //# Inner Class ClassInfo
  private class ClassInfo
  {

    //#######################################################################
    //# Constructors
    /**
     * Initialises a class that contains a single state.
     */
    private ClassInfo(final int state)
    {
      mStates = mListBuffer.createList();
      mListBuffer.append(mStates, state);
      mPredecessor = mIncomingEquivqalenceHashCode = mActiveEventsHashCode = -1;
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
     * Returns whether all states in this class have at least one outgoing
     * tau-transition.
     */
    private boolean isTauOnly()
    {
      mStateListReadIterator.reset(mStates);
      while (mStateListReadIterator.advance()) {
        final int state = mStateListReadIterator.getCurrentData();
        if (!hasOutgoingTau(state)) {
          return false;
        }
      }
      return true;
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
      if (mIncomingEquivqalenceHashCode < 0) {
        final ListBufferTransitionRelation rel = getTransitionRelation();
        final int numEvents = rel.getNumberOfProperEvents();
        final int root = getFirstState();
        int result = isWeaklyInitial(root) ? INIT_HASH : 0;
        for (int event = EventEncoding.NONTAU; event < numEvents; event++) {
          final int eshift = event << mEventShift;
          mPredecessorsEventClosureIterator.reset(root, event);
          while (mPredecessorsEventClosureIterator.advance()) {
            final int pred =
              mPredecessorsEventClosureIterator.getCurrentSourceState();
            final ClassInfo info = mStateClasses[pred];
            final int key = info.getFirstState() | eshift;
            if (mCurrentSet0.add(key)) {
              result += HashFunctions.hash(key);
            }
          }
        }
        mCurrentSet0.clear();
        mIncomingEquivqalenceHashCode = result < 0 ? -result : result;
      }
      return mIncomingEquivqalenceHashCode;
    }

    /**
     * Determines whether the states in this class are incoming equivalent
     * to the given state.
     */
    private boolean incomingEquivalenceEquals(final int root1)
    {
      if (mStateClasses[root1] == this) {
        return true;
      }
      final int root0 = getFirstState();
      if (isWeaklyInitial(root0) ^ isWeaklyInitial(root1)) {
        return false;
      }
      final ListBufferTransitionRelation rel = getTransitionRelation();
      final int numEvents = rel.getNumberOfProperEvents();
      for (int event = EventEncoding.NONTAU; event < numEvents; event++) {
        try {
          mPredecessorsEventClosureIterator.reset(root0, event);
          while (mPredecessorsEventClosureIterator.advance()) {
            final int pred0 =
              mPredecessorsEventClosureIterator.getCurrentSourceState();
            final ClassInfo info0 = mStateClasses[pred0];
            final int key0 = info0.getFirstState();
            mCurrentSet0.add(key0);
          }
          mPredecessorsEventClosureIterator.reset(root1, event);
          while (mPredecessorsEventClosureIterator.advance()) {
            final int pred1 =
              mPredecessorsEventClosureIterator.getCurrentSourceState();
            final ClassInfo info1 = mStateClasses[pred1];
            final int key1 = info1.getFirstState();
            if (mCurrentSet1.add(key1)) {
              if (!mCurrentSet0.contains(key1) ||
                  mCurrentSet1.size() > mCurrentSet0.size()) {
                return false;
              }
            }
          }
        } finally {
          mCurrentSet0.clear();
          mCurrentSet1.clear();
        }
      }
      return true;
    }

    /**
     * Returns a hash code that identifies this class with respect to
     * active events. The set of active events is determined by exploring
     * the successors of all states in this class.
     */
    private int activeEventsHashCode()
    {
      if (mActiveEventsHashCode < 0) {
        final ListBufferTransitionRelation rel = getTransitionRelation();
        final int numEvents = rel.getNumberOfProperEvents();
        int result = 0;
        events:
        for (int event = EventEncoding.NONTAU; event < numEvents; event++) {
          mStateListReadIterator.reset(mStates);
          while (mStateListReadIterator.advance()) {
            final int state = mStateListReadIterator.getCurrentData();
            mSuccessorsPreEventClosureIterator.reset(state, event);
            if (mSuccessorsPreEventClosureIterator.advance()) {
              result += HashFunctions.hash(event);
              continue events;
            }
          }
        }
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
      }
      final ListBufferTransitionRelation rel = getTransitionRelation();
      final int numEvents = rel.getNumberOfProperEvents();
      events:
      for (int event = EventEncoding.NONTAU; event < numEvents; event++) {
        boolean found0 = false;
        mStateListReadIterator.reset(mStates);
        while (mStateListReadIterator.advance()) {
          final int state0 = mStateListReadIterator.getCurrentData();
          mSuccessorsPreEventClosureIterator.reset(state0, event);
          if (mSuccessorsPreEventClosureIterator.advance()) {
            found0 = true;
            break;
          }
        }
        mStateListReadIterator.reset(info1.mStates);
        while (mStateListReadIterator.advance()) {
          final int state1 = mStateListReadIterator.getCurrentData();
          mSuccessorsPreEventClosureIterator.reset(state1, event);
          if (mSuccessorsPreEventClosureIterator.advance()) {
            if (found0) {
              continue events;
            } else {
              return false;
            }
          }
        }
        if (found0) {
          return false;
        }
      }
      return true;
    }

    //#######################################################################
    //# Merging
    /**
     * Merges the given other class into this class.
     * @param  info   Class to be merged into this class.
     *                All states from the given class into this class.
     *                The other class becomes invalid and should no longer
     *                be used after this operation.
     */
    private void merge(final ClassInfo info)
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
      if (mActiveEventsHashCode != info.mActiveEventsHashCode) {
        mActiveEventsHashCode = -1;
      }
    }

    //#######################################################################
    //# Data Members
    /**
     * List of states in the class, smallest first.
     */
    private int mStates;
    /**
     * List node of predecessor in list of incoming equivalent classes,
     * or -1 if not yet listed.
     */
    private final int mPredecessor;
    /**
     * Cached incoming equivalence hash code, or -1.
     */
    private int mIncomingEquivqalenceHashCode;
    /**
     * Cached active events code, or -1.
     */
    private int mActiveEventsHashCode;

  }


  //#########################################################################
  //# Inner Class IncomingEquivalenceHash
  private class IncomingEquivalenceHash implements WatersIntHashingStrategy
  {

    //#######################################################################
    //# Interface net.sourceforge.waters.analysis.op.WatersIntHashingStrategy
    public int computeHashCode(final int root)
    {
      final ClassInfo info = mStateClasses[root];
      return info.incomingEquivalenceHashCode();
    }

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
    implements WatersIntHashingStrategy, TIntIntProcedure
  {

    //#######################################################################
    //# Interface net.sourceforge.waters.analysis.op.WatersIntHashingStrategy
    public int computeHashCode(final int root)
    {
      final ClassInfo info = mStateClasses[root];
      return info.activeEventsHashCode();
    }

    public boolean equals(final int root1, final int root2)
    {
      final ClassInfo info = mStateClasses[root1];
      return info.activeEventsEquals(root2);
    }

    //#######################################################################
    //# Interface gnu.trove.TIntIntProcedure
    public boolean execute(final int root, final int list)
    {
      if (mListBuffer.isStrictlyLongerThan(list, 1)) {
        setUpSuccessorsTauClosure();
        final int size = mListBuffer.getLength(list);
        final WatersIntIntHashMap map =
          new WatersIntIntHashMap(size, IntListBuffer.NULL, this);
        int newlist = mListBuffer.createList();
        boolean merging = false;
        mClassListWriteIterator.reset(list);
        while (mClassListWriteIterator.advance()) {
          final int code = mClassListWriteIterator.getCurrentData();
          final ClassInfo info = mStateClasses[code];
          if (info.isTauOnly()) {
            merging = true;
          } else {
            final int oldlist = map.putIfAbsent(code, newlist);
            if (oldlist == IntListBuffer.NULL) {
              mClassListWriteIterator.moveTo(newlist);
              newlist = mListBuffer.createList();
            } else {
              mClassListWriteIterator.moveTo(oldlist);
              merging = true;
            }
          }
        }
        mListBuffer.dispose(newlist);
        if (merging) {
          map.forEachValue(mMergeProcedure);
          // TODO Auto-generated method stub
        }
      }
      return true;
    }

    //#######################################################################
    //# Class Constants
    private static final long serialVersionUID = 1L;

  }


  //#########################################################################
  //# Inner Class MergeProcedure
  private class MergeProcedure implements TIntProcedure
  {

    //#######################################################################
    //# Interface gnu.trove.TIntProcedure
    public boolean execute(final int list)
    {
      if (mListBuffer.isStrictlyLongerThan(list, 1)) {
        mClassListReadIterator.reset(list);
        mClassListReadIterator.advance();
        int state = mClassListReadIterator.getCurrentData();
        final ClassInfo root = mStateClasses[state];
        while (mClassListReadIterator.advance()) {
          state = mClassListReadIterator.getCurrentData();
          final ClassInfo info = mStateClasses[state];
          root.merge(info);
        }
        //root.clearSuccessorsIncomingEquivalence();
      }
      return true;
    }

  }


  //#########################################################################
  //# Data Members
  private int mTransitionLimit = Integer.MAX_VALUE;

  private int mEventShift;
  private IntListBuffer mListBuffer;
  private IntListBuffer.Iterator mStateListReadIterator;
  private IntListBuffer.Iterator mClassListReadIterator;
  private IntListBuffer.ModifyingIterator mClassListWriteIterator;
  private TIntHashSet mCurrentSet0;
  private TIntHashSet mCurrentSet1;
  private TauClosure mPredecessorsTauClosure;
  private TransitionIterator mPredecessorsTauClosureIterator;
  private TransitionIterator mPredecessorsEventClosureIterator;

  private ClassInfo[] mStateClasses;
  private WatersIntIntHashMap mIncomingEquivalenceMap;
  private TauClosure mSuccessorsTauClosure;
  private TransitionIterator mSuccessorsEventIterator;
  private TransitionIterator mSuccessorsPreEventClosureIterator;
  private TransitionIterator mSuccessorsFullEventClosureIterator;

  private final ActiveEventsHash mActiveEventsHash = new ActiveEventsHash();
  private final MergeProcedure mMergeProcedure = new MergeProcedure();


  //#########################################################################
  //# Class Constants
  private static final int INIT_HASH = 0xabababab;

}
