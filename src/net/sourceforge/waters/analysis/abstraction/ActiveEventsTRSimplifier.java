//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters Analysis
//# PACKAGE: net.sourceforge.waters.analysis.abstraction
//# CLASS:   ActiveEventsTRSimplifier
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.analysis.abstraction;

import gnu.trove.list.array.TIntArrayList;
import gnu.trove.map.hash.TIntIntHashMap;
import gnu.trove.map.hash.TIntObjectHashMap;
import gnu.trove.set.hash.THashSet;
import gnu.trove.set.hash.TIntHashSet;

import java.io.PrintWriter;
import java.io.StringWriter;

import net.sourceforge.waters.analysis.tr.EventEncoding;
import net.sourceforge.waters.analysis.tr.IntListBuffer;
import net.sourceforge.waters.analysis.tr.IntSetBuffer;
import net.sourceforge.waters.analysis.tr.ListBufferTransitionRelation;
import net.sourceforge.waters.analysis.tr.TauClosure;
import net.sourceforge.waters.analysis.tr.TransitionIterator;
import net.sourceforge.waters.model.analysis.AnalysisException;


/**
 * <P>An implementation of the <I>Active Events Rule</I>.</P>
 *
 * <P>This rule merges all states that are incoming equivalent and have
 * equal sets of eligible events.</P>
 *
 * <P><STRONG>Proposed algorithm:</STRONG></P>
 * <OL>
 * <LI>Create initial partition based on active events and initial state
 *     property.</LI>
 * <LI>Refine partition based on incoming equivalence between equivalence
 *     classes. Separate states that do or do not have incoming transitions
 *     with a given event and source state, where the source state belongs
 *     to a different equivalence class.</LI>
 * <LI>Refine partition based on incoming equivalence within equivalence
 *     classes. Separate states that do or do not have incoming transitions
 *     with a given event from within their own equivalence class.</LI>
 * <LI>Repeat steps 2. and&nbsp;3. for any equivalence classes that have been
 *     split according to step&nbsp;3.</LI>
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
    return ListBufferTransitionRelation.CONFIG_SUCCESSORS;
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
    mTauClosure = rel.createSuccessorsTauClosure(mTransitionLimit);
    mSetBuffer = new IntSetBuffer(numEvents + 1);
    mSetReadIterator = mSetBuffer.iterator();
    mTauIterator = rel.createSuccessorsReadOnlyIterator();
    mTauIterator.resetEvent(EventEncoding.TAU);
    mFullEventClosureIterator = mTauClosure.createFullEventClosureIterator();
    mListBuffer = new IntListBuffer();
    mListReadIterator = mListBuffer.createReadOnlyIterator();
    mListWriteIterator = mListBuffer.createModifyingIterator();
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

    // 1. Create initial partition based on active events and initial states
    createInitialEquivalenceClasses();
    if (mNumProperCandidates == 0) {
      return false;
    }

    // 2. Refine partition based on incoming equivalence between classes
    mPredecessors = new int[numStates];
    for (int s = 0; s < numStates; s++) {
      splitOtherClasses(s);
      if (mNumProperCandidates == 0) {
        return false;
      }
    }

    // 3. Refine partition based on incoming equivalence within classes

    // 4. Apply result partition.
    return false;
  }


  @Override
  protected void tearDown()
  {
    super.tearDown();
    mTauIterator = null;
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
  /**
   * Creates active event sets and adds to {@link #mSetBuffer},
   * and stores them for each state in {@link #mActiveEventSets}.
   * Creates equivalence classes for groups of two more states that
   * have the same active event sets.
   * Sets {@link #mNumProperCandidates} to the number of equivalence
   * classes created.
   */
  private void createInitialEquivalenceClasses()
  {
    final ListBufferTransitionRelation rel = getTransitionRelation();
    final int numStates = rel.getNumberOfReachableStates();
    final int numEvents = rel.getNumberOfReachableStates();
    final int INITIAL = numEvents;
    final TransitionIterator tauIter = mTauClosure.createIterator();
    final TIntHashSet initialStates = new TIntHashSet();
    for (int s = 0; s < numStates; s++) {
      if (rel.isInitial(s) && initialStates.add(s)) {
        tauIter.resetState(s);
        while (tauIter.advance()) {
          final int t = tauIter.getCurrentTargetState();
          initialStates.add(t);
        }
      }
    }
    mActiveEventSets = new int[numStates];
    final TIntIntHashMap counts = new TIntIntHashMap(numStates);
    final TransitionIterator eventIter = rel.createSuccessorsReadOnlyIterator();
    eventIter.resetEvents(EventEncoding.NONTAU, numEvents - 1);
    int defaultID = getDefaultMarkingID();
    if (!rel.isPropositionUsed(defaultID)) {
      defaultID = -1;
    }
    mNumProperCandidates = 0;
    final TIntHashSet events = new TIntHashSet();
    int maxSize = 0;
    for (int s = 0; s < numStates; s++) {
      if (rel.isReachable(s)) {
        int size = 0;
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
            size++;
          }
        }
        if (initialStates.contains(s)) {
          events.add(INITIAL);
        }
        final int set = mSetBuffer.add(events);
        mActiveEventSets[s] = set;
        if (counts.adjustOrPutValue(set, 1, 1) == 2) {
          mNumProperCandidates++;
        }
        events.clear();
        if (size > maxSize) {
          maxSize = size;
        }
      } else {
        mActiveEventSets[s] = -1;
      }
    }
    if (mNumProperCandidates > 0) {
      final TIntArrayList buffer = new TIntArrayList(maxSize);
      final TIntObjectHashMap<EquivalenceClass> classMap =
        new TIntObjectHashMap<>(mNumProperCandidates);
      for (int s = 0; s < numStates; s++) {
        final int set = mActiveEventSets[s];
        if (set >= 0 && counts.get(set) > 1) {
          EquivalenceClass clazz = classMap.get(set);
          if (clazz == null) {
            final boolean init = initialStates.contains(s);
            final int active = getReducedActiveEventsSet(set, init, buffer);
            clazz = new EquivalenceClass(active);
            mActiveEventSets[s] = active;
          } else {
            mActiveEventSets[s] = clazz.getActiveEvents();
          }
          clazz.addState(s);
        }
      }
    }
  }

  /**
   * Returns a reduced active event set that does not contain the
   * special codes for initial or marked states.
   */
  private int getReducedActiveEventsSet(final int set,
                                        final boolean init,
                                        final TIntArrayList buffer)
  {
    if (!init) {
      mSetReadIterator.reset(set);
      if (!mSetReadIterator.advance() ||
          mSetReadIterator.getCurrentData() != OMEGA) {
        return set;
      }
    }
    final ListBufferTransitionRelation rel = getTransitionRelation();
    final int numEvents = rel.getNumberOfReachableStates();
    mSetReadIterator.reset(set);
    while (mSetReadIterator.advance()) {
      final int event = mSetReadIterator.getCurrentData();
      if (event > EventEncoding.TAU && event < numEvents) {
        buffer.add(event);
      }
    }
    final int reduced = mSetBuffer.add(buffer);
    buffer.clear();
    return reduced;
  }

  private void splitOtherClasses(final int source)
  {
    final int active = mActiveEventSets[source];
    if (active >= 0) {
      mSetReadIterator.reset(active);
      while (mSetReadIterator.advance()) {
        final int event = mSetReadIterator.getCurrentData();
        splitOtherClasses(source, event);
        if (mNumProperCandidates == 0) {
          return;
        }
      }
    }
  }

  private void splitOtherClasses(final int source, final int event)
  {
    final THashSet<EquivalenceClass> splitClasses = new THashSet<>();
    final EquivalenceClass sourceClass = mStateToClass[source];
    mFullEventClosureIterator.reset(source, event);
    while (mFullEventClosureIterator.advance()) {
      final int target = mFullEventClosureIterator.getCurrentTargetState();
      final EquivalenceClass targetClass = mStateToClass[target];
      if (targetClass != null && targetClass != sourceClass) {
        targetClass.moveToSplitList(target);
        splitClasses.add(targetClass);
      }
    }
    for (final EquivalenceClass splitClass : splitClasses) {
      splitClass.splitUsingSplitList();
    }
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
  private class EquivalenceClass
  {
    //#######################################################################
    //# Constructors
    private EquivalenceClass(final int active)
    {
      mSize = 0;
      mList = mListBuffer.createList();
      mSplitList = IntListBuffer.NULL;
      mSplitSize = -1;
      mActiveEvents = active;
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
      mActiveEvents = active;
      setUpStateToClass();
    }

    //#######################################################################
    //# Simple Access
    @SuppressWarnings("unused")
    private int size()
    {
      return mSize;
    }

    @SuppressWarnings("unused")
    private int getFirstState()
    {
      mListReadIterator.reset(mList);
      mListReadIterator.advance();
      return mListReadIterator.getCurrentData();
    }

    private int getActiveEvents()
    {
      return mActiveEvents;
    }

    //#######################################################################
    //# Initialisation
    private void addState(final int state)
    {
      mListBuffer.append(mList, state);
      mSize++;
      mStateToClass[state] = this;
    }

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
        new EquivalenceClass(splitList, splitSize, preds, mActiveEvents);
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
     * ActiveEvents'TRSimplifier#mListBuffer}.
     */
    private int mList;
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
     * The active events set common to all states in this class.
     */
    private final int mActiveEvents;
  }


  //#########################################################################
  //# Data Members
  // Configuration
  private int mTransitionLimit;

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

  // Tools
  private TauClosure mTauClosure;
  private TransitionIterator mTauIterator;
  private TransitionIterator mFullEventClosureIterator;

  private IntListBuffer mListBuffer;
  private IntListBuffer.ReadOnlyIterator mListReadIterator;
  private IntListBuffer.ModifyingIterator mListWriteIterator;
  private IntSetBuffer.IntSetIterator mSetReadIterator;


  //#########################################################################
  //# Class Constants
  private static final int OMEGA = EventEncoding.TAU;

}