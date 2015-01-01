//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters Analysis
//# PACKAGE: net.sourceforge.waters.analysis.abstraction
//# CLASS:   IncomingEquivalenceTRSimplifier
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.analysis.abstraction;

import gnu.trove.iterator.TIntIntIterator;
import gnu.trove.list.array.TIntArrayList;
import gnu.trove.map.hash.TIntIntHashMap;
import gnu.trove.procedure.TIntProcedure;
import gnu.trove.set.hash.THashSet;
import gnu.trove.set.hash.TIntHashSet;
import gnu.trove.stack.TIntStack;
import gnu.trove.stack.array.TIntArrayStack;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Arrays;
import java.util.Set;

import net.sourceforge.waters.analysis.tr.EventEncoding;
import net.sourceforge.waters.analysis.tr.EventStatus;
import net.sourceforge.waters.analysis.tr.IntListBuffer;
import net.sourceforge.waters.analysis.tr.IntSetBuffer;
import net.sourceforge.waters.analysis.tr.ListBufferTransitionRelation;
import net.sourceforge.waters.analysis.tr.TRPartition;
import net.sourceforge.waters.analysis.tr.TauClosure;
import net.sourceforge.waters.analysis.tr.TransitionIterator;
import net.sourceforge.waters.analysis.tr.TransitionListBuffer;
import net.sourceforge.waters.model.analysis.AnalysisException;
import net.sourceforge.waters.model.base.ProxyTools;
import net.sourceforge.waters.model.base.WatersRuntimeException;


/**
 * <P>A combined implementation of the <I>Silent Continuation Rule</I> and
 * <I>Active Events Rule</I>.</P>
 *
 * <P>This rule merges states that are incoming equivalent and have
 * at least one outgoing <I>always enabled</I> ({@link
 * EventStatus#STATUS_ALWAYS_ENABLED}) transition, and states
 * that are incoming equivalent and have equal sets of eligible events.</P>
 *
 * <P><I>Algorithm:</I></P>
 * <OL>
 * <LI>Create initial incoming equivalence classes. States that can be
 * silently reached from initial states form one class, and other states
 * form a second class.</LI>
 * <LI>Split classes based on incoming equivalence.
 * Following a similar approach as the bisimulation algorithm (see {@link
 * ObservationEquivalenceTRSimplifier}), the algorithm processes each
 * state and event to find the reachable states and separates them from
 * their equivalence classes.</LI>
 * <LI>Attempt to group states. Within each class of incoming equivalent
 * states, the algorithm first merges all states with equal sets of active
 * events, then merges states resulting from this merge that have an
 * outgoing silent or always enabled transition, and afterwards checks a
 * second time for any states with equal sets of active events.</LI>
 * <LI>Reevaluate incoming equivalence. The algorithm initialises new
 * incoming equivalence classes as in step&nbsp;1, but only considering
 * successor states of states merged in step&nbsp;3. Then step&nbsp;2 is
 * repeated, but only visiting the predecessors of the successors of the
 * states merged in step&nbsp;3.</LI>
 * </OL>
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
  //# Interface
  //# net.sourceforge.waters.analysis.abstraction.TransitionRelationSimplifier
  @Override
  public int getPreferredInputConfiguration()
  {
    return ListBufferTransitionRelation.CONFIG_ALL;
  }

  @Override
  public boolean isAlwaysEnabledEventsSupported()
  {
    return true;
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
    mTauIterator = rel.createSuccessorsReadOnlyIterator();
    mTauIterator.resetEvent(EventEncoding.TAU);
    mListBuffer = new IntListBuffer();
    mListReadIterator = mListBuffer.createReadOnlyIterator();
    mListWriteIterator = mListBuffer.createModifyingIterator();
    mSetBuffer = new IntSetBuffer(numEvents);
    mSetReadIterator = mSetBuffer.iterator();
    final TransitionListBuffer succBuf = rel.getSuccessorBuffer();
    mTauClosureIterator = new TauClosureIterator(succBuf);
    final TransitionIterator inner1 = rel.createSuccessorsReadOnlyIterator();
    mEventIterator = new EventIterator(inner1);
    mPostEventIterator = new PostEventClosureIterator();
    final TransitionIterator inner2 = rel.createSuccessorsReadOnlyIterator();
    mForwardEventIterator = new FullEventClosureIterator(succBuf, inner2);
  }

  @Override
  protected boolean runSimplifier()
    throws AnalysisException
  {
    final ListBufferTransitionRelation rel = getTransitionRelation();
    final int numStates = rel.getNumberOfStates();
    if (numStates <= 1) {
      return false;
    }

    // 1. Create StateInfo for all reachable states.
    mNumMergedStates = 0;
    mStateInfo = new StateInfo[numStates];
    for (int s = 0; s < numStates; s++) {
      if (rel.isReachable(s)) {
        mNumMergedStates++;
        mStateInfo[s] = new StateInfo(s);
      }
    }
    if (mNumMergedStates <= 1) {
      return false;
    }

    // 2. Create equivalence classes for initial and non-initial states
    createInitialEquivalenceClasses(mNumMergedStates);
    if (mNumProperCandidates == 0) {
      return false;
    }
    mPredecessors = new int[numStates];

    // 3. Repeatedly attempt merge within incoming equivalent classes
    boolean merged = false;
    final TIntArrayList mergedRoots = new TIntArrayList();
    do {
      splitOnIncomingEquivalence();
      if (mNumProperCandidates == 0) {
        break;
      }
      mergeOutgoingEquivalentClasses(mergedRoots);
      if (mergedRoots.size() == 0) {
        break;
      }
      merged = true;
      if (mNumMergedStates == 1) {
        break;
      }
      createNextEquivalenceClasses(mergedRoots);
      mergedRoots.clear();
    } while (mNumProperCandidates > 0);

    // 4. Apply result partition.
    if (merged) {
      int config = getPreferredOutputConfiguration();
      if (config == 0 || config == ListBufferTransitionRelation.CONFIG_ALL) {
        config = ListBufferTransitionRelation.CONFIG_SUCCESSORS;
      }
      rel.reconfigure(config);
      int classNo = 0;
      final int[] stateToClass = new int[numStates];
      for (int s = 0; s < numStates; s++) {
        final StateInfo info = mStateInfo[s];
        if (info == null) {
          stateToClass[s] = -1;
        } else if (info.getFirstState() == s) {
          info.setClassNumber(stateToClass, classNo++);
        }
      }
      if (mStateInfo[rel.getDumpStateIndex()] == null) {
        classNo++;
      }
      final TRPartition partition = new TRPartition(stateToClass, classNo);
      setResultPartition(partition);
      applyResultPartitionAutomatically();
    }
    return merged;
  }


  @Override
  protected void tearDown()
  {
    super.tearDown();
    mTauIterator = null;
    mTauClosureIterator = null;
    mEventIterator = null;
    mPostEventIterator = null;
    mForwardEventIterator = null;
    mBackwardEventIterator = null;
    mStateInfo = null;
    mListBuffer = null;
    mListReadIterator = null;
    mListWriteIterator = null;
    mSetReadIterator = null;
    mSetBuffer = null;
    mStateToClass = null;
    mPredecessors = null;
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
  //# Auxiliary Methods
  void createInitialEquivalenceClasses(final int numReachable)
  {
    final ListBufferTransitionRelation rel = getTransitionRelation();
    final int defaultID = getDefaultMarkingID();
    final int numStates = rel.getNumberOfStates();
    final TauClosure closure = rel.createSuccessorsTauClosure(0);
    final TransitionIterator tauIter = closure.createIterator();
    for (int s = 0; s < numStates; s++) {
      StateInfo sourceInfo = mStateInfo[s];
      if (sourceInfo != null) { // if state reachable
        final boolean init = rel.isInitial(s);
        if (init) {
          sourceInfo.setInitial();
        }
        if (defaultID >= 0 && rel.isMarked(s, defaultID)) {
          sourceInfo.setMarked();
        }
        tauIter.resetState(s);
        tauIter.advance();
        boolean hasTau = false;
        if (init || defaultID >= 0 && !sourceInfo.isMarked()) {
          while (tauIter.advance()) {
            final int t = tauIter.getCurrentTargetState();
            final StateInfo targetInfo = mStateInfo[t];
            if (init) {
              targetInfo.setInitial();
            }
            hasTau = true;
            if (defaultID >= 0 && rel.isMarked(t, defaultID)) {
              sourceInfo.setMarked();
              if (!init) {
                break;
              }
            }
          }
        } else {
          hasTau = tauIter.advance();
        }
        if (hasTau) {
          sourceInfo = mStateInfo[s];
          sourceInfo.setHasOutgoingTau();
        }
      }
    }
    final int init = mListBuffer.createList();
    final int nonInit = mListBuffer.createList();
    int numInit = 0;
    for (int s = 0; s < numStates; s++) {
      final StateInfo info = mStateInfo[s];
      if (info != null) { // if state reachable
        if (info.isInitial()) {
          mListBuffer.append(init, s);
          numInit++;
        } else {
          mListBuffer.append(nonInit, s);
        }
      }
    }
    mStateToClass = new EquivalenceClass[numStates];
    createEquivalenceClass(init, numInit);
    createEquivalenceClass(nonInit, numReachable - numInit);
  }

  void createNextEquivalenceClasses(final TIntArrayList mergedRoots)
  {
    final ListBufferTransitionRelation rel = getTransitionRelation();
    final int numEvents = rel.getNumberOfProperEvents();
    mForwardEventIterator.resetEvents(EventEncoding.NONTAU, numEvents - 1);
    if (mBackwardEventIterator == null) {
      final TransitionListBuffer predBuf = rel.getPredecessorBuffer();
      final TransitionIterator inner =
        rel.createPredecessorsReadOnlyIterator();
      mBackwardEventIterator = new FullEventClosureIterator(predBuf, inner);
    }
    mBackwardEventIterator.resetEvents(EventEncoding.NONTAU, numEvents - 1);
    final int init = mListBuffer.createList();
    final int nonInit = mListBuffer.createList();
    int numInit = 0;
    int numNonInit = 0;
    for (int i = 0; i < mergedRoots.size(); i++) {
      final int s = mergedRoots.get(i);
      StateInfo info = mStateInfo[s];
      if (info.getFirstState() == s) {
        mForwardEventIterator.resume(s);
        while (mForwardEventIterator.advance()) {
          info = mForwardEventIterator.getCurrentTargetStateInfo();
          final int t = info.getFirstState();
          if (info.isInitial()) {
            mListBuffer.append(init, t);
            numInit++;
          } else {
            mListBuffer.append(nonInit, t);
            numNonInit++;
          }
          mBackwardEventIterator.resume(t);
          while (mBackwardEventIterator.advance()) {
            // empty loop body - just collecting visited set in iterator
          }
        }
      }
    }
    mNumProperCandidates = 0;
    if (numInit > 1 || numNonInit > 1) {
      Arrays.fill(mStateToClass, null);
      createEquivalenceClass(init, numInit);
      createEquivalenceClass(nonInit, numNonInit);
    }
  }

  private void createEquivalenceClass(final int list, final int size)
  {
    if (size > 1) {
      new EquivalenceClass(list, size, false);
      mNumProperCandidates++;
    } else {
      mListBuffer.dispose(list);
    }
  }

  private void splitOnIncomingEquivalence()
  {
    if (mBackwardEventIterator == null) {
      final ListBufferTransitionRelation rel = getTransitionRelation();
      final int numStates = rel.getNumberOfStates();
      for (int s = 0; s < numStates && mNumProperCandidates > 0; s++) {
        splitOnIncomingEquivalence(s);
      }
    } else {
      final TIntHashSet splitters = mBackwardEventIterator.getVisitedStates();
      splitters.forEach(new TIntProcedure() {
        @Override
        public boolean execute(final int s)
        {
          return splitOnIncomingEquivalence(s);
        }
      });
    }
  }

  private boolean splitOnIncomingEquivalence(final int s)
  {
    final StateInfo info = mStateInfo[s];
    if (info != null && info.getFirstState() == s) {
      final ListBufferTransitionRelation rel = getTransitionRelation();
      final Set<EquivalenceClass> splits = new THashSet<EquivalenceClass>();
      boolean hasAlwaysEnabled = info.hasOutgoingAlwaysEnabled();
      final int[] tauClosure = info.computeTauClosure();
      final int active = info.computeActiveEvents(tauClosure);
      mSetReadIterator.reset(active);
      while (mSetReadIterator.advance()) {
        final int e = mSetReadIterator.getCurrentData();
        if (e == OMEGA) {
          continue;
        }
        mPostEventIterator.resetEvent(e);
        for (final int ss : tauClosure) {
          mPostEventIterator.resume(ss);
          while (mPostEventIterator.advance()) {
            final int t = mPostEventIterator.getCurrentTargetState();
            final EquivalenceClass clazz = mStateToClass[t];
            if (clazz != null) {
              clazz.moveToSplitList(t);
              splits.add(clazz);
            }
          }
        }
        if (!splits.isEmpty()) {
          for (final EquivalenceClass clazz : splits) {
            clazz.splitUsingSplitList();
          }
          if (mNumProperCandidates == 0) {
            return false;
          }
          splits.clear();
          if (!hasAlwaysEnabled) {
            final byte status = rel.getProperEventStatus(e);
            hasAlwaysEnabled =
              EventStatus.isAlwaysEnabledEvent(status);
          }
        }
      }
      if (hasAlwaysEnabled) {
        info.setHasOutgoingAlwaysEnabled();
      }
    }
    return true;
  }

  private void mergeOutgoingEquivalentClasses(final TIntArrayList mergedRoots)
  {
    final ListBufferTransitionRelation rel = getTransitionRelation();
    final int numStates = rel.getNumberOfStates();
    for (int s = 0; s < numStates; s++) {
      final EquivalenceClass clazz = mStateToClass[s];
      if (clazz != null && clazz.getFirstState() == s) {
        clazz.attemptMerge(mergedRoots);
      }
      mStateToClass[s] = null;
    }
  }


  private StateInfo mergeStates(final int list, final boolean equalActive)
  {
    int s0 = -1;
    boolean init = false;
    boolean marked = false;
    boolean hasTau = false;
    boolean hasAlwaysEnabled = false;
    final TIntHashSet activeHash = equalActive ? null : new TIntHashSet();
    int active = 0;
    int clazz = mListBuffer.createList();
    mNumMergedStates++;
    mListReadIterator.reset(list);
    while (mListReadIterator.advance()) {
      final int s = mListReadIterator.getCurrentData();
      if (s0 < 0) {
        s0 = s;
      }
      final StateInfo info = mStateInfo[s];
      final int clazz1 = info.getFormedClass();
      if (clazz1 == IntListBuffer.NULL) {
        mListBuffer.append(clazz, s);
      } else {
        clazz = mListBuffer.catenateDestructively(clazz, clazz1);
      }
      init |= info.isInitial();
      marked |= info.isMarked();
      hasTau |= info.hasOutgoingTau();
      hasAlwaysEnabled |= info.hasOutgoingAlwaysEnabled();
      active = info.getActiveEvents();
      if (!equalActive) {
        mSetBuffer.collect(active, activeHash);
      }
      mNumMergedStates--;
    }
    if (!equalActive) {
      active = mSetBuffer.add(activeHash);
    }
    final StateInfo info =
      new StateInfo(s0, init, marked, false, hasAlwaysEnabled, active, clazz);
    mListReadIterator.reset(clazz);
    while (mListReadIterator.advance()) {
      final int s = mListReadIterator.getCurrentData();
      mStateInfo[s] = info;
    }
    if (hasTau) {
      mListReadIterator.reset(clazz);
      outer:
      while (mListReadIterator.advance()) {
        final int s = mListReadIterator.getCurrentData();
        mTauIterator.resetState(s);
        while (mTauIterator.advance()) {
          final int t = mTauIterator.getCurrentTargetState();
          if (mStateInfo[t] != info) {
            info.setHasOutgoingTau();
            break outer;
          }
        }
      }
    }
    return info;
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
    if (mStateInfo != null) {
      printer.println("STATE INFO");
      for (int s = 0; s < mStateInfo.length; s++) {
        final StateInfo info = mStateInfo[s];
        if (info != null) {
          printer.print(s);
          printer.print(" : ");
          info.dump(printer);
          printer.println();
        }
      }
    }
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


  //#######################################################################
  //# Inner Class StateInfo
  /**
   * A state of the abstracted automaton.
   * Initially state information records are created for each state.
   * As states are merged, they are grouped together in a single state
   * information record for each merged state. Each state information
   * record is uniquely identified by its <I>state code</I>, which is
   * the code of its smallest state. All state information records are
   * recorded in the array {@link IncomingEquivalenceTRSimplifier#mStateInfo
   * mStateInfo}, which maps state numbers in the unabstracted automaton
   * to the states they have been merged into.
   */
  private class StateInfo
  {

    //#######################################################################
    //# Constructors
    private StateInfo(final int code)
    {
      mStateCode = code;
      mFormedClass = IntListBuffer.NULL;
    }

    private StateInfo(final int code,
                      final boolean init,
                      final boolean marked,
                      final boolean hasTau,
                      final boolean hasAlwaysEnabled,
                      final int active,
                      final int clazz)
    {
      mStateCode = code;
      mInitial = init;
      mMarked = marked;
      mHasOutgoingTau = hasTau;
      mHasOutgoingAlwaysEnabled = hasAlwaysEnabled;
      mActiveEvents = active;
      mFormedClass = clazz;
    }

    //#######################################################################
    //# Simple Access
    /**
     * Returns whether this state is initial, or can be reached
     * silently from an initial state.
     */
    private boolean isInitial()
    {
      return mInitial;
    }

    /**
     * Sets this state to be initial.
     */
    private void setInitial()
    {
      mInitial = true;
    }

    /**
     * Returns whether this state has the default marking.
     */
    private boolean isMarked()
    {
      return mMarked;
    }

    /**
     * Sets this state to be marked with the default marking.
     */
    private void setMarked()
    {
      mMarked = true;
    }

    /**
     * Returns whether this state or some other state in its class has an
     * outgoing tau transition to a class other than its own class.
     */
    private boolean hasOutgoingTau()
    {
      return mHasOutgoingTau;
    }

    /**
     * Sets that this state or some other state in its class has an
     * outgoing tau transition to a class other than its own class.
     */
    private void setHasOutgoingTau()
    {
      mHasOutgoingTau = true;
    }

    /**
     * Returns whether this state or some other state in its class has an
     * outgoing always enabled transition.
     */
    private boolean hasOutgoingAlwaysEnabled()
    {
      return mHasOutgoingAlwaysEnabled;
    }

    /**
     * Sets that this state or some other state in its class has an
     * outgoing always enabled transition.
     */
    private void setHasOutgoingAlwaysEnabled()
    {
      mHasOutgoingAlwaysEnabled = true;
    }

    /**
     * Gets the set of active events of this state or its class.
     * @return Integer identifying event set in
     *         {@link IncomingEquivalenceTRSimplifier#mSetBuffer}.
     */
    private int getActiveEvents()
    {
      assert mActiveEvents >= 0 : "Active events not yet initialised!";
      return mActiveEvents;
    }

    /**
     * Gets the list of states merged with this state, or
     * {@link IntListBuffer#NULL} if this state has not been merged with
     * any other state.
     */
    private int getFormedClass()
    {
      return mFormedClass;
    }

    /**
     * Gets the number of the first state merged with this state.
     */
    private int getFirstState()
    {
      if (mFormedClass == IntListBuffer.NULL) {
        return mStateCode;
      } else {
        return mListBuffer.getFirst(mFormedClass);
      }
    }

    //#######################################################################
    //# Auxiliary Methods
    private int[] computeTauClosure()
    {
      mTauClosureIterator.resetState(this);
      while (mTauClosureIterator.advance()) {
        // empty loop body - just collecting visited set in iterator
      }
      final TIntHashSet visited = mTauClosureIterator.getVisitedStates();
      final int[] closure = visited.toArray();
      mTauClosureIterator.reset();
      return closure;
    }

    private int computeActiveEvents(final int[] tauClosure)
    {
      if (mActiveEvents < 0) {
        final TIntHashSet events = new TIntHashSet();
        final ListBufferTransitionRelation rel = getTransitionRelation();
        final int numEvents = rel.getNumberOfProperEvents();
        mEventIterator.resetEvents(EventEncoding.NONTAU, numEvents - 1);
        for (final int s : tauClosure) {
          final StateInfo info = mStateInfo[s];
          if (info.isMarked()) {
            events.add(OMEGA);
          }
          mEventIterator.resume(s);
          while (mEventIterator.advance()) {
            final int event = mEventIterator.getCurrentEvent();
            events.add(event);
          }
        }
        mEventIterator.reset();
        mActiveEvents = mSetBuffer.add(events);
      }
      return mActiveEvents;
    }

    private void setClassNumber(final int[] stateToClass, final int classNo)
    {
      if (mFormedClass == IntListBuffer.NULL) {
        stateToClass[mStateCode] = classNo;
      } else {
        mListReadIterator.reset(mFormedClass);
        while (mListReadIterator.advance()) {
          final int s = mListReadIterator.getCurrentData();
          stateToClass[s] = classNo;
        }
      }
    }

    private void push(final TIntStack stack,
                      final IntListBuffer.Iterator iter)
    {
      if (mFormedClass == IntListBuffer.NULL) {
        stack.push(mStateCode);
      } else {
        iter.reset(mFormedClass);
        while (iter.advance()) {
          final int s = iter.getCurrentData();
          stack.push(s);
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
      dump(printer);
      return writer.toString();
    }

    private void dump(final PrintWriter printer)
    {
      if (mFormedClass == IntListBuffer.NULL) {
        printer.print('[');
        printer.print(mStateCode);
        printer.print(']');
      } else {
        mListBuffer.dumpList(printer, mFormedClass);
      }
    }

    //#######################################################################
    //# Data Members
    /**
     * The integer state code of this state.
     */
    private final int mStateCode;
    /**
     * A flag indicating whether this state is initial, or can be reached
     * silently from an initial state.
     */
    private boolean mInitial = false;
    /**
     * A flag indicating whether this state has the default marking.
     */
    private boolean mMarked = false;
    /**
     * A flag indicating whether this state (or its class) has an outgoing
     * tau transition to another class.
     */
    private boolean mHasOutgoingTau = false;
    /**
     * A flag indicating whether this state (or its class) has an outgoing
     * always enabled transition.
     */
    private boolean mHasOutgoingAlwaysEnabled = false;
    /**
     * Active events set of this state, represented as set identifier in
     * {@link IncomingEquivalenceTRSimplifier#mSetBuffer},
     * or <CODE>-1</CODE> if not yet known.
     */
    private int mActiveEvents = -1;
    /**
     * List of states merged with this state, represented as list identifier
     * in {@link IncomingEquivalenceTRSimplifier#mListBuffer}, or
     * {@link IntListBuffer#NULL} if this state has not been merged with any
     * other state.
     */
    private final int mFormedClass;
  }


  //#########################################################################
  //# Inner Class EquivalenceClass
  /**
   * <P>A candidate class of incoming equivalent states.</P>
   *
   * <P>A candidate equivalence class is represented as a list of state
   * numbers. If states have been merged in previous iterations, the state
   * number represent the class of merged states identified by the state code
   * of its smallest state, i.e., the code of its {@link StateInfo} record.</P>
   *
   * <P>Initially, the algorithm creates two candidate equivalence classes
   * consisting of all initial states and all non-initial states. These are
   * split repeatedly until only classes of incoming equivalent states remain.
   * If an equivalence class consists of only a single state, it is
   * removed as nothing can be merged within this class.</P>
   *
   * <P>All candidate equivalence classes are recorded in the array {@link
   * IncomingEquivalenceTRSimplifier#StateToClass mStateToClass}, which
   * maintains a map from state codes (of merged classes) to their
   * candidate classes. Classes consisting of a single state are represented
   * by a <CODE>null</CODE> entry in the array.</P>
   */
  private class EquivalenceClass
  {
    //#######################################################################
    //# Constructors
    private EquivalenceClass(final int list,
                             final int size,
                             final boolean preds)
    {
      mSize = size;
      mList = list;
      mSplitList = IntListBuffer.NULL;
      mSplitSize = preds ? 0 : -1;
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
      mListReadIterator.reset(mList);
      mListReadIterator.advance();
      return mListReadIterator.getCurrentData();
    }

    //#######################################################################
    //# Initialisation
    private void setUpStateToClass()
    {
      mListReadIterator.reset(mList);
      if (mSize == 1) {
        mListReadIterator.advance();
        final int state = mListReadIterator.getCurrentData();
        mStateToClass[state] = null;
        mListBuffer.dispose(mList);
        // BUG? What if called a second time?
      } else {
        while (mListReadIterator.advance()) {
          final int state = mListReadIterator.getCurrentData();
          mStateToClass[state] = this;
        }
      }
    }

    //#######################################################################
    //# Splitting
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

    private void splitUsingSplitList()
    {
      if (mSplitSize <= 0) {
        return;
      } else if (mSplitSize == mSize) {
        mList = mSplitList;
      } else {
        doSimpleSplit(mSplitList, mSplitSize, mSplitSize >= 0);
      }
      mSplitSize = 0;
      mSplitList = IntListBuffer.NULL;
    }

    private void doSimpleSplit(final int splitList,
                               final int splitSize,
                               final boolean preds)
    {
      final int newSize = mSize - splitSize;
      @SuppressWarnings("unused")
      final EquivalenceClass splitClass =
        new EquivalenceClass(splitList, splitSize, preds);
      mSize = newSize;
      if (mSize == 1) {
        setUpStateToClass();
        mNumProperCandidates--;
      }
      if (mSplitSize > 1) {
        mNumProperCandidates++;
      }
    }

    //#######################################################################
    //# Merging
    private void attemptMerge(final TIntArrayList mergedRoots)
    {
      TIntIntHashMap map = groupByActiveEvents();
      TIntIntIterator iter = map.iterator();
      final int lastMerge = mListBuffer.createList();
      boolean allInLastMerge = true;
      while (iter.hasNext()) {
        iter.advance();
        final int list = iter.value();
        mListReadIterator.reset(list);
        mListReadIterator.advance();
        final int state = mListReadIterator.getCurrentData();
        final StateInfo info;
        if (mListReadIterator.advance()) {
          info = mergeStates(list, true);
          mergedRoots.add(info.getFirstState());
        } else {
          info = mStateInfo[state];
        }
        mListBuffer.dispose(list);
        if (info.hasOutgoingTau() || info.hasOutgoingAlwaysEnabled()) {
          mListBuffer.append(lastMerge, state);
        } else {
          allInLastMerge = false;
        }
      }
      if (mListBuffer.isStrictlyLongerThan(lastMerge, 1)) {
        StateInfo info = mergeStates(lastMerge, false);
        mergedRoots.add(info.getFirstState());
        mListBuffer.dispose(lastMerge);
        if (!allInLastMerge) {
          map = groupByActiveEvents();
          iter = map.iterator();
          while (iter.hasNext()) {
            iter.advance();
            final int list = iter.value();
            if (mListBuffer.isStrictlyLongerThan(list, 1)) {
              info = mergeStates(list, true);
              mergedRoots.add(info.getFirstState());
            }
            mListBuffer.dispose(list);
          }
        }
      } else {
        mListBuffer.dispose(lastMerge);
      }
    }

    private TIntIntHashMap groupByActiveEvents()
    {
      final TIntIntHashMap map =
        new TIntIntHashMap(mSize, 0.5f, -1, IntListBuffer.NULL);
      mListReadIterator.reset(mList);
      while (mListReadIterator.advance()) {
        final int s = mListReadIterator.getCurrentData();
        final StateInfo info = mStateInfo[s];
        if (info.getFirstState() == s) {
          final int active = info.getActiveEvents();
          int list = map.get(active);
          if (list == IntListBuffer.NULL) {
            list = mListBuffer.createList();
            map.put(active, list);
          }
          mListBuffer.append(list, s);
        }
      }
      return map;
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

    //#######################################################################
    //# Data Members
    /**
     * The number of states in this candidate equivalence class.
     */
    private int mSize;
    /**
     * The list of states constituting this candidate equivalence class,
     * represented as list identifier in {@link
     * IncomingEquivalenceTRSimplifier#mListBuffer}.
     */
    private int mList;
    /**
     * A list of states to be split off from this class,
     * represented as list identifier in {@link
     * IncomingEquivalenceTRSimplifier#mListBuffer}.
     */
    private int mSplitList;
    /**
     * The number of states in the {@link #mSplitList}.
     */
    private int mSplitSize;
  }


  //#########################################################################
  //# Inner Class TauClosureIterator
  /**
   * An iterator over the tau closure of a merged class. Unlike the iterators
   * in class {@link net.sourceforge.waters.analysis.tr.TauClosure TauClosure},
   * this iterator can iterate over all states in a {@link
   * IncomingEquivalenceTRSimplifier.StateInfo StateInfo} record.
   */
  private class TauClosureIterator
    implements TransitionIterator
  {

    //#######################################################################
    //# Constructor
    private TauClosureIterator(final TransitionListBuffer buffer)
    {
      mTransitionListBuffer = buffer;
      mStack = new TIntArrayStack();
      mTransitionIterator = buffer.createReadOnlyIterator();
      mTransitionIterator.resetEvent(EventEncoding.TAU);
      mPushIterator = mListBuffer.createReadOnlyIterator();
      mVisited = new TIntHashSet();
      mIteratorState = IteratorState.DONE;
    }

    //#######################################################################
    //# Interface net.sourceforge.waters.analysis.tr.TransitionIterator
    @Override
    public TauClosureIterator clone()
    {
      try {
        final TauClosureIterator cloned =
          (TauClosureIterator) super.clone();
        cloned.mStack = new TIntArrayStack(mStack);
        cloned.mTransitionIterator = mTransitionIterator.clone();
        cloned.mPushIterator = mPushIterator.clone();
        cloned.mVisited = new TIntHashSet(mVisited);
        return cloned;
      } catch (final CloneNotSupportedException exception) {
        throw new WatersRuntimeException(exception);
      }
    }

    @Override
    public void reset()
    {
      resetState(mCurrentSourceState);
    }

    @Override
    public void resetEvent(final int event)
    {
      if (event == EventEncoding.TAU) {
        reset();
      } else {
        throwNonTauException();
      }
    }

    @Override
    public void resetEvents(final int first, final int last)
    {
      if (first == EventEncoding.TAU && last == EventEncoding.TAU) {
        reset();
      } else {
        throwNonTauException();
      }
    }

    @Override
    public void resetState(final int from)
    {
      resetState(mStateInfo[from]);
    }

    @Override
    public void reset(final int from, final int event)
    {
      if (event == EventEncoding.TAU) {
        resetState(from);
      } else {
        throwNonTauException();
      }
    }

    @Override
    public void resume(final int from)
    {
      resumeState(mStateInfo[from]);
    }

    @Override
    public int getFirstEvent()
    {
      return EventEncoding.TAU;
    }

    @Override
    public int getLastEvent()
    {
      return EventEncoding.TAU;
    }

    @Override
    public boolean advance()
    {
      int s;
      while (mIteratorState != IteratorState.DONE) {
        switch (mIteratorState) {
        case INIT:
          mCurrentTargetState = mCurrentSourceState;
          s = mCurrentSourceState.getFirstState();
          if (!mVisited.add(s)) {
            mIteratorState = IteratorState.DONE;
            break;
          } else {
            mCurrentTargetState.push(mStack, mPushIterator);
            mIteratorState = IteratorState.POP;
            return true;
          }
        case POP:
          if (mStack.size() == 0) {
            mIteratorState = IteratorState.DONE;
            break;
          } else {
            s = mStack.pop();
            mTransitionIterator.resetState(s);
            mIteratorState = IteratorState.ITERATE;
            // fall through ...
          }
        case ITERATE:
          while (mTransitionIterator.advance()) {
            s = mTransitionIterator.getCurrentToState();
            final StateInfo info = mStateInfo[s];
            s = info.getFirstState();
            if (mVisited.add(s)) {
              info.push(mStack, mPushIterator);
              mCurrentTargetState = info;
              return true;
            }
          }
          mIteratorState = IteratorState.POP;
          break;
        default:
          break;
        }
      }
      mCurrentTargetState = null;
      return false;
    }

    @Override
    public int getCurrentEvent()
    {
      return mTransitionIterator.getCurrentEvent();
    }

    @Override
    public int getCurrentSourceState()
    {
      return mTransitionListBuffer.getIteratorSourceState(this);
    }

    @Override
    public int getCurrentFromState()
    {
      return mCurrentSourceState.getFirstState();
    }

    @Override
    public int getCurrentTargetState()
    {
      return mTransitionListBuffer.getIteratorTargetState(this);
    }

    @Override
    public int getCurrentToState()
    {
      return mCurrentTargetState.getFirstState();
    }

    @Override
    public void remove()
    {
      throw new UnsupportedOperationException
        (ProxyTools.getShortClassName(this) +
         " does not support transition removal!");
    }

    @Override
    public void setCurrentToState(final int state)
    {
      throw new UnsupportedOperationException
        (ProxyTools.getShortClassName(this) +
         " does not support transition modification!");
    }

    //#######################################################################
    //# Auxiliary Methods
    private void resetState(final StateInfo info)
    {
      clear();
      resumeState(info);
    }

    private void resumeState(final StateInfo info)
    {
      mCurrentSourceState = info;
      mCurrentTargetState = null;
      mStack.clear();
      if (info != null) {
        info.push(mStack, mPushIterator);
        mIteratorState = IteratorState.INIT;
      } else {
        mIteratorState = IteratorState.DONE;
      }
    }

    @SuppressWarnings("unused")
    private StateInfo getCurrentSourceStateInfo()
    {
      return mCurrentSourceState;
    }

    private StateInfo getCurrentTargetStateInfo()
    {
      return mCurrentTargetState;
    }

    private TIntHashSet getVisitedStates()
    {
      return mVisited;
    }

    private void clear()
    {
      if (mVisited != null) {
        final int size = mVisited.size();
        if (size > HASH_REALLOC_THRESHOLD) {
          mVisited = new TIntHashSet();
        } else if (size > 0) {
          mVisited.clear();
        }
      }
    }

    private void throwNonTauException()
    {
      throw new UnsupportedOperationException
        (ProxyTools.getShortClassName(this) +
         " only iterates with tau event!");
    }

    //#######################################################################
    //# Data Members
    private final TransitionListBuffer mTransitionListBuffer;
    private TIntStack mStack;
    private TransitionIterator mTransitionIterator;
    private IntListBuffer.Iterator mPushIterator;
    private TIntHashSet mVisited;
    private IteratorState mIteratorState;
    private StateInfo mCurrentSourceState;
    private StateInfo mCurrentTargetState;
  }

  private static enum IteratorState {
    INIT, POP, ITERATE, DONE;
  }


  //#########################################################################
  //# Inner Class EventIterator
  /**
   * An iterator over the transitions of a merged class.
   * Unlike the iterators in class {@link ListBufferTransitionRelation},
   * this iterator can iterate over all states in a {@link
   * IncomingEquivalenceTRSimplifier.StateInfo StateInfo} record.
   */
  public class EventIterator implements TransitionIterator
  {

    //#######################################################################
    //# Constructor
    public EventIterator(final TransitionIterator inner)
    {
      mClassReadIterator = mListBuffer.createReadOnlyIterator();
      mTransitionIterator = inner;
    }

    //#######################################################################
    //# Interface net.sourceforge.waters.analysis.tr.TransitionIterator
    @Override
    public EventIterator clone()
    {
      try {
        final EventIterator cloned = (EventIterator) super.clone();
        cloned.mClassReadIterator = mClassReadIterator.clone();
        cloned.mTransitionIterator = mTransitionIterator.clone();
        return cloned;
      } catch (final CloneNotSupportedException exception) {
        throw new WatersRuntimeException(exception);
      }
    }

    @Override
    public void reset()
    {
      resetClassReadIteration();
      mTransitionIterator.reset();
    }

    @Override
    public void resetEvent(final int event)
    {
      resetClassReadIteration();
      mTransitionIterator.resetEvent(event);
    }

    @Override
    public void resetEvents(final int first, final int last)
    {
      resetClassReadIteration();
      mTransitionIterator.resetEvents(first, last);
    }

    @Override
    public void resetState(final int from)
    {
      resetState(mStateInfo[from]);
    }

    @Override
    public void reset(final int from, final int event)
    {
      final StateInfo info = mStateInfo[from];
      resumeClassReadIteration(info);
      mTransitionIterator.resetEvent(event);
    }

    @Override
    public void resume(final int from)
    {
      final StateInfo info = mStateInfo[from];
      resumeClassReadIteration(info);
    }

    @Override
    public int getFirstEvent()
    {
      return mTransitionIterator.getFirstEvent();
    }

    @Override
    public int getLastEvent()
    {
      return mTransitionIterator.getLastEvent();
    }

    @Override
    public boolean advance()
    {
      do {
        switch (mLevel) {
        case 1:
          if (!advanceClassReadIteration()) {
            break;
          }
          final int source = getCurrentStateInClassReadIteration();
          mTransitionIterator.resume(source);
          mLevel = 2;
          // fall through ...
        case 2:
          if (mTransitionIterator.advance()) {
            return true;
          }
          break;
        default:
          throw new IllegalStateException
            ("Unexpected level " + mLevel + " in " +
             ProxyTools.getShortClassName(this) + "!");
        }
        mLevel--;
      } while (mLevel > 0);
      return false;
    }

    @Override
    public int getCurrentEvent()
    {
      return mTransitionIterator.getCurrentEvent();
    }

    @Override
    public int getCurrentFromState()
    {
      return mCurrentState.getFirstState();
    }

    @Override
    public int getCurrentSourceState()
    {
      return getCurrentFromState();
    }

    @Override
    public int getCurrentToState()
    {
      final StateInfo info = getCurrentTargetStateInfo();
      return info.getFirstState();
    }

    @Override
    public int getCurrentTargetState()
    {
      return getCurrentToState();
    }

    @Override
    public void remove()
    {
      mTransitionIterator.remove();
    }

    @Override
    public void setCurrentToState(final int state)
    {
      mTransitionIterator.setCurrentToState(state);
    }

    //#######################################################################
    //# Auxiliary Methods
    private void resetState(final StateInfo info)
    {
      if (info == null) {
        resetClassReadIteration();
      } else {
        resumeClassReadIteration(info);
      }
      mTransitionIterator.reset();
    }

    @SuppressWarnings("unused")
    private StateInfo getCurrentSourceStateInfo()
    {
      return mCurrentState;
    }

    private StateInfo getCurrentTargetStateInfo()
    {
      final int to = mTransitionIterator.getCurrentToState();
      return mStateInfo[to];
    }

    private void resetClassReadIteration()
    {
      mClassReadIterator.reset();
      mCurrentState = null;
      mDummyIteration = -1;
      mLevel = 1;
    }

    private void resumeClassReadIteration(final StateInfo info)
    {
      final int list = info.getFormedClass();
      if (list == IntListBuffer.NULL) {
        mDummyIteration = 0;
      } else {
        mClassReadIterator.reset(list);
        mDummyIteration = -1;
      }
      mLevel = 1;
      mCurrentState = info;
    }

    private boolean advanceClassReadIteration()
    {
      switch (mDummyIteration) {
      case 0:
        mDummyIteration = 1;
        return true;
      case 1:
        mDummyIteration = 2;
        // fall through ...
      case 2:
        return false;
      default:
        return mClassReadIterator.advance();
      }
    }

    private int getCurrentStateInClassReadIteration()
    {
      switch (mDummyIteration) {
      case 0:
        throw new IllegalStateException("Attempting to read state in " +
                                        ProxyTools.getShortClassName(this) +
                                        " before advancing iteration!");
      case 1:
        return mCurrentState.getFirstState();
      case 2:
        throw new IllegalStateException("Attempting to read state in " +
                                        ProxyTools.getShortClassName(this) +
                                        " after end of iteration!");
      default:
        return mClassReadIterator.getCurrentData();
      }
    }

    //#######################################################################
    //# Data Members
    private IntListBuffer.ReadOnlyIterator mClassReadIterator;
    private TransitionIterator mTransitionIterator;
    private StateInfo mCurrentState;
    private int mDummyIteration = -1;
    private int mLevel = 1;
  }


  //#########################################################################
  //# Inner Class PostEventClosureIterator
  /**
   * An iterator over the event closure of a merged class.
   * This iterator explores successor states reachable by an event
   * followed by a sequence of silent transitions. Unlike the iterators in
   * class {@link net.sourceforge.waters.analysis.tr.TauClosure TauClosure},
   * this iterator can iterate over all states in a {@link
   * IncomingEquivalenceTRSimplifier.StateInfo StateInfo} record.
   */
  private class PostEventClosureIterator
    implements TransitionIterator
  {
    //#######################################################################
    //# Constructor
    private PostEventClosureIterator()
    {
      final ListBufferTransitionRelation rel = getTransitionRelation();
      final TransitionListBuffer succBuf = rel.getSuccessorBuffer();
      final TransitionIterator iter = succBuf.createReadOnlyIterator();
      mEventIterator = new EventIterator(iter);
      mTauIterator = new TauClosureIterator(succBuf);
      mFromState = null;
      mLevel = -1;
    }

    //#######################################################################
    //# Interface net.sourceforge.waters.analysis.tr.TransitionIterator
    @Override
    public PostEventClosureIterator clone()
    {
      try {
        final PostEventClosureIterator cloned =
          (PostEventClosureIterator) super.clone();
        cloned.mEventIterator = mEventIterator.clone();
        cloned.mTauIterator = mTauIterator.clone();
        return cloned;
      } catch (final CloneNotSupportedException exception) {
        throw new WatersRuntimeException(exception);
      }
    }

    @Override
    public void reset()
    {
      mEventIterator.resetState(mFromState);
      mTauIterator.reset();
      resume();
    }

    @Override
    public void resetEvent(final int event)
    {
      mEventIterator.resetEvent(event);
      reset();
    }

    @Override
    public void resetEvents(final int first, final int last)
    {
      mEventIterator.resetEvents(first, last);
      reset();
    }

    @Override
    public void resetState(final int from)
    {
      resetState(mStateInfo[from]);
    }

    @Override
    public void reset(final int from, final int event)
    {
      reset(mStateInfo[from], event);
    }

    @Override
    public void resume(final int from)
    {
      mFromState = mStateInfo[from];
      resume();
    }

    @Override
    public int getFirstEvent()
    {
      return mEventIterator.getFirstEvent();
    }

    @Override
    public int getLastEvent()
    {
      return mEventIterator.getLastEvent();
    }


    @Override
    public boolean advance()
    {
      int state;
      do {
        switch (mLevel) {
        case 1:
          if (!mEventIterator.advance()) {
            break;
          }
          state = mEventIterator.getCurrentTargetState();
          mTauIterator.resume(state);
          mLevel = 2;
          // fall through ...
        case 2:
          if (mTauIterator.advance()) {
            return true;
          }
          break;
        default:
          throw new IllegalStateException
            ("Unexpected level " + mLevel + " in " +
             ProxyTools.getShortClassName(this) + "!");
        }
        mLevel--;
      } while (mLevel > 0);
      return false;
    }

    @Override
    public int getCurrentEvent()
    {
      return mEventIterator.getCurrentEvent();
    }

    @Override
    public int getCurrentFromState()
    {
      return mFromState.getFirstState();
    }

    @Override
    public int getCurrentSourceState()
    {
      return getCurrentFromState();
    }

    @Override
    public int getCurrentToState()
    {
      return mTauIterator.getCurrentToState();
    }

    @Override
    public int getCurrentTargetState()
    {
      return getCurrentToState();
    }

    @Override
    public void setCurrentToState(final int state)
    {
      throw new UnsupportedOperationException
        (ProxyTools.getShortClassName(this) +
         " does not support transition modification!");
    }

    @Override
    public void remove()
    {
      throw new UnsupportedOperationException
        (ProxyTools.getShortClassName(this) +
         " does not support transition removal!");
    }

    //#######################################################################
    //# Auxiliary Methods
    private void reset(final StateInfo info, final int event)
    {
      mFromState = info;
      resetEvent(event);
    }

    private void resetState(final StateInfo info)
    {
      mFromState = info;
      reset();
    }

    private void resume()
    {
      mEventIterator.resetState(mFromState);
      mLevel = 1;
    }

    @SuppressWarnings("unused")
    private StateInfo getCurrentTargetStateInfo()
    {
      return mTauIterator.getCurrentTargetStateInfo();
    }

    //#######################################################################
    //# Data Members
    private StateInfo mFromState;
    private int mLevel;

    private EventIterator mEventIterator;
    private TauClosureIterator mTauIterator;
  }


  //#########################################################################
  //# Inner Class FullEventClosureIterator
  /**
   * An iterator over the event closure of a merged class.
   * This iterator explores predecessor or successor states reachable by
   * a sequence of silent transitions, followed by an event, followed by
   * another sequence of silent transitions. Unlike the iterators
   * in class {@link net.sourceforge.waters.analysis.tr.TauClosure TauClosure},
   * this iterator can iterate over all states in a {@link
   * IncomingEquivalenceTRSimplifier.StateInfo StateInfo} record.
   */
  private class FullEventClosureIterator
    implements TransitionIterator
  {
    //#######################################################################
    //# Constructor
    private FullEventClosureIterator(final TransitionListBuffer buffer,
                                     final TransitionIterator inner)
    {
      mTransitionListBuffer = buffer;
      mTauIterator1 = new TauClosureIterator(buffer);
      mEventIterator = new EventIterator(inner);
      mTauIterator2 = new TauClosureIterator(buffer);
      mFromState = null;
      mLevel = -1;
    }

    //#######################################################################
    //# Interface net.sourceforge.waters.analysis.tr.TransitionIterator
    @Override
    public FullEventClosureIterator clone()
    {
      try {
        final FullEventClosureIterator cloned =
          (FullEventClosureIterator) super.clone();
        cloned.mTauIterator1 = mTauIterator2.clone();
        cloned.mEventIterator = mEventIterator.clone();
        cloned.mTauIterator2 = mTauIterator2.clone();
        return cloned;
      } catch (final CloneNotSupportedException exception) {
        throw new WatersRuntimeException(exception);
      }
    }

    @Override
    public void reset()
    {
      mTauIterator1.resetState(mFromState);
      mEventIterator.reset();
      mTauIterator2.reset();
      resume();
    }

    @Override
    public void resetEvent(final int event)
    {
      mEventIterator.resetEvent(event);
      reset();
    }

    @Override
    public void resetEvents(final int first, final int last)
    {
      mEventIterator.resetEvents(first, last);
      reset();
    }

    @Override
    public void resetState(final int from)
    {
      resetState(mStateInfo[from]);
    }

    @Override
    public void reset(final int from, final int event)
    {
      reset(mStateInfo[from], event);
    }

    @Override
    public void resume(final int from)
    {
      mFromState = mStateInfo[from];
      resume();
    }

    @Override
    public int getFirstEvent()
    {
      return mEventIterator.getFirstEvent();
    }

    @Override
    public int getLastEvent()
    {
      return mEventIterator.getLastEvent();
    }

    @Override
    public boolean advance()
    {
      int state;
      do {
        switch (mLevel) {
        case 1:
          if (!mTauIterator1.advance()) {
            break;
          }
          state = mTauIterator1.getCurrentToState();
          mEventIterator.resume(state);
          mLevel = 2;
          // fall through ...
        case 2:
          if (!mEventIterator.advance()) {
            break;
          }
          state = mEventIterator.getCurrentToState();
          mTauIterator2.resume(state);
          mLevel = 3;
          // fall through ...
        case 3:
          if (mTauIterator2.advance()) {
            return true;
          }
          break;
        default:
          throw new IllegalStateException
            ("Unexpected level " + mLevel + " in " +
             ProxyTools.getShortClassName(this) + "!");
        }
        mLevel--;
      } while (mLevel > 0);
      return false;
    }

    @Override
    public int getCurrentEvent()
    {
      return mEventIterator.getCurrentEvent();
    }

    @Override
    public int getCurrentFromState()
    {
      return mFromState.getFirstState();
    }

    @Override
    public int getCurrentSourceState()
    {
      return mTransitionListBuffer.getIteratorSourceState(this);
    }

    @Override
    public int getCurrentToState()
    {
      return mTauIterator2.getCurrentToState();
    }

    @Override
    public int getCurrentTargetState()
    {
      return mTransitionListBuffer.getIteratorTargetState(this);
    }

    @Override
    public void setCurrentToState(final int state)
    {
      throw new UnsupportedOperationException
        (ProxyTools.getShortClassName(this) +
         " does not support transition modification!");
    }

    @Override
    public void remove()
    {
      throw new UnsupportedOperationException
        (ProxyTools.getShortClassName(this) +
         " does not support transition removal!");
    }

    //#######################################################################
    //# Auxiliary Methods
    private void reset(final StateInfo info, final int event)
    {
      mFromState = info;
      resetEvent(event);
    }

    private void resetState(final StateInfo info)
    {
      mFromState = info;
      reset();
    }

    private void resume()
    {
      mTauIterator1.resetState(mFromState);
      mLevel = 1;
    }

    private StateInfo getCurrentTargetStateInfo()
    {
      return mTauIterator2.getCurrentTargetStateInfo();
    }

    private TIntHashSet getVisitedStates()
    {
      return mTauIterator2.getVisitedStates();
    }

    //#######################################################################
    //# Data Members
    private StateInfo mFromState;
    private int mLevel;

    private final TransitionListBuffer mTransitionListBuffer;
    private TauClosureIterator mTauIterator1;
    private EventIterator mEventIterator;
    private TauClosureIterator mTauIterator2;
  }


  //#########################################################################
  //# Data Members
  /**
   * State information records for all states.
   * This array contains a {@link StateInfo} entry for all reachable states,
   * while unreachable states have <CODE>null</CODE> entries.
   */
  private StateInfo[] mStateInfo;
  /**
   * The current number of merged states.
   * Splitting is stopped when this number reaches one.
   */
  private int mNumMergedStates;
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

  private TransitionIterator mTauIterator;
  private TauClosureIterator mTauClosureIterator;
  private EventIterator mEventIterator;
  private PostEventClosureIterator mPostEventIterator;
  private FullEventClosureIterator mForwardEventIterator;
  private FullEventClosureIterator mBackwardEventIterator;

  private IntListBuffer mListBuffer;
  private IntListBuffer.ReadOnlyIterator mListReadIterator;
  private IntListBuffer.ModifyingIterator mListWriteIterator;
  private IntSetBuffer.IntSetIterator mSetReadIterator;
  private IntSetBuffer mSetBuffer;


  //#########################################################################
  //# Class Constants
  private static final int OMEGA = EventEncoding.TAU;
  private static final int HASH_REALLOC_THRESHOLD = 64;

}