package net.sourceforge.waters.analysis.certainconf;

import gnu.trove.list.array.TIntArrayList;
import gnu.trove.set.hash.TIntHashSet;
import gnu.trove.stack.TIntStack;
import gnu.trove.stack.array.TIntArrayStack;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import net.sourceforge.waters.analysis.abstraction.AbstractMarkingTRSimplifier;
import net.sourceforge.waters.analysis.tr.EventEncoding;
import net.sourceforge.waters.analysis.tr.IntSetBuffer;
import net.sourceforge.waters.analysis.tr.IntStateBuffer;
import net.sourceforge.waters.analysis.tr.ListBufferTransitionRelation;
import net.sourceforge.waters.analysis.tr.MemStateProxy;
import net.sourceforge.waters.analysis.tr.OneEventCachingTransitionIterator;
import net.sourceforge.waters.analysis.tr.PreTransitionBuffer;
import net.sourceforge.waters.analysis.tr.StateEncoding;
import net.sourceforge.waters.analysis.tr.TauClosure;
import net.sourceforge.waters.analysis.tr.TransitionIterator;
import net.sourceforge.waters.model.analysis.AnalysisException;
import net.sourceforge.waters.model.analysis.OverflowException;
import net.sourceforge.waters.model.analysis.OverflowKind;
import net.sourceforge.waters.model.des.AutomatonProxy;
import net.sourceforge.waters.model.des.EventProxy;
import net.sourceforge.waters.model.des.ProductDESProxyFactory;
import net.sourceforge.waters.model.des.StateProxy;
import net.sourceforge.waters.model.des.TransitionProxy;
import net.sourceforge.waters.xsd.base.ComponentKind;


public class CertainConflictsTRSimplifier extends AbstractMarkingTRSimplifier {

  public CertainConflictsTRSimplifier()
  {
  }

  public CertainConflictsTRSimplifier(final ListBufferTransitionRelation rel)
  {
      super(rel);
  }

  @Override
  protected void setUp()  throws AnalysisException {
    super.setUp();
    final ListBufferTransitionRelation rel = getTransitionRelation();
    mOldRel = new ListBufferTransitionRelation(rel, rel.getConfiguration());
    final int numEvents = rel.getNumberOfProperEvents();
    int index = 0;
    for (int event = 0; event < numEvents; event++) {
      if ((rel.getProperEventStatus(event) & EventEncoding.STATUS_UNUSED) == 0) {
        index++;
      }
    }
    mEventIndexes = new int[index];
    index = 0;
    for (int event = 0; event < numEvents; event++) {
      if ((rel.getProperEventStatus(event) & EventEncoding.STATUS_UNUSED) == 0) {
        mEventIndexes[index++] = event;
      }
    }

    final int numStates = rel.getNumberOfStates();
    mSetOffsets = new TIntArrayList(numStates);
    mStateSetBuffer = new IntSetBuffer(numStates, 0, -1);
    mTransitionBuffer = new PreTransitionBuffer(numEvents, mTransitionLimit);
    isFindingCounterExample = false;
  }

  @Override
  protected void tearDown() {
    super.tearDown();
  }

  @Override
  public void reset()
  {
    super.reset();
    mSetOffsets = null;
    mStateSetBuffer = null;
    mTransitionBuffer = null;
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
  public boolean isDeadlockAware()
  {
    return true;
  }


  //#########################################################################
  //# Overrides for net.sourceforge.waters.analysis.abstraction.AbstractTRSimplifier
  @Override
  protected boolean runSimplifier() throws AnalysisException
  {
    final ListBufferTransitionRelation rel = getTransitionRelation();
    mBadStates = new TIntHashSet();
    mInitialBadStates = updateBadStates(rel, true, -1);

    if (mInitialBadStates == 0) return false;

    if ((rel.getProperEventStatus(EventEncoding.TAU) &
         EventEncoding.STATUS_UNUSED) == 0) {
      mIsDeterministic = false;
      final TauClosure closure = rel.createSuccessorsTauClosure(mTransitionLimit);
      mTauIterator = closure.createIterator();
      mEventIterator = closure.createPostEventClosureIterator(-1);
    } else if (!rel.isDeterministic()) {
      mIsDeterministic = false;
      mTauIterator = null;
      final TransitionIterator iter = rel.createSuccessorsReadOnlyIterator();
      mEventIterator = new OneEventCachingTransitionIterator(iter);
    } else {
      mIsDeterministic = true;
      mTauIterator = null;
      final TransitionIterator iter = rel.createSuccessorsReadOnlyIterator();
      mEventIterator = new OneEventCachingTransitionIterator(iter);
    }

    // handle deterministic automata separately
    if (mIsDeterministic)
    {
      if (mInitialBadStates == 1) return false;
      // run deterministic version of algorithm
      final int dumpstate = findDeterministicCertainConflicts(rel);
      // cleanup and return
      rel.reconfigure(ListBufferTransitionRelation.CONFIG_SUCCESSORS);
      if (dumpstate > -1) rel.removeOutgoingTransitions(dumpstate);
      rel.checkReachability();
      removeProperSelfLoopEvents();
      return true;
    }

    // Collect initial state set.
    final int numStates = rel.getNumberOfStates();
    final TIntHashSet init = new TIntHashSet();
    for (int state = 0; state < numStates; state++) {
      if (rel.isInitial(state)) {
        if (mTauIterator == null) {
          init.add(state);
        } else {
          checkAbort();
          mTauIterator.resetState(state);
          while (mTauIterator.advance()) {
            final int tausucc = mTauIterator.getCurrentTargetState();
            init.add(tausucc);
          }
        }
      }
    }
    int last = 0;
    // convert to array so we can compute permutations
    final int[] arrInit = init.toArray();
    Arrays.sort(arrInit);
    if (!init.isEmpty()) {
        numInit = arrInit.length;
        for (int i = 0; i < arrInit.length; i++)
        {
            arraySwap(arrInit, 0, i);
            final int offset = mStateSetBuffer.add(arrInit);
            mSetOffsets.add(offset);
            last = offset;
        }
    } else if (numStates == 0) {
        return false;
    }
    // Expand subset states.
    final IntSetBuffer.IntSetIterator iter = mStateSetBuffer.iterator();
    final ArrayList<Integer> current = new ArrayList<Integer>();
    final ArrayList<Integer> currentLeading = new ArrayList<Integer>();

    for (int source = 0; source < mSetOffsets.size(); source++) {
      final int set = mSetOffsets.get(source);
      for (final int event : mEventIndexes) {
        checkAbort();
        // set iterators
        mEventIterator.resetEvent(event);
        iter.reset(set);
        // first state in set
        int firstStateInSet = 0;
        boolean firstiter = true;
        // for each state in current set
        while (iter.advance()) {
          final int state = iter.getCurrentData();
          if (firstiter) firstStateInSet = state;
          firstiter = false;
          mEventIterator.resume(state);
          while (mEventIterator.advance()) {
            final int target = mEventIterator.getCurrentTargetState();
            current.add(target);
            if (state == firstStateInSet) {
              currentLeading.add(target);
            }
          }
        }
        if (current.isEmpty()) continue;
        for (final int leadingState : currentLeading) {
          // swap leading to the front
          Collections.swap(current, 0, current.indexOf(leadingState));
          // sort remainder of list and convert to array
          Collections.sort(current.subList(1, current.size()));
          final int[] acurrent = new int[current.size()];
          for (int i = 0; i < acurrent.length; i++) acurrent[i] = current.get(i);

          final int offset = mStateSetBuffer.add(acurrent);
          final int target;
          if (offset > last) {
            target = mSetOffsets.size();
            if (target >= mStateLimit) {
              throw new OverflowException(OverflowKind.STATE, mStateLimit);
            }
            mSetOffsets.add(offset);
            last = offset;
          } else {
            target = mSetOffsets.binarySearch(offset);
          }
          // add transitions for each permutation we just added
          mTransitionBuffer.addTransition(source, event, target);
        }
        current.clear();
        currentLeading.clear();
      }
    }

    // Build new transition relation.
    applyResultPartitionAutomatically();
    return true;
  }
    @Override
    protected void applyResultPartition()
    throws AnalysisException
    {
      if (mSetOffsets != null) {
        ListBufferTransitionRelation rel = getTransitionRelation();
        final int numDetStates = mSetOffsets.size();
        final int numProps = rel.getNumberOfPropositions();
        final long usedProps = rel.getUsedPropositions();
        final IntStateBuffer detStates = new IntStateBuffer(numDetStates, numProps, usedProps);
        for (int init = 0; init < numInit; init++)
        {
          detStates.setInitial(init, true);
        }

        final IntSetBuffer.IntSetIterator iter = mStateSetBuffer.iterator();
        for (int detstate = 0; detstate < numDetStates; detstate++) {
          final int offset = mSetOffsets.get(detstate);
          iter.reset(offset);
          iter.advance();
          final int state = iter.getCurrentData();
          final long stateMarkings = rel.getAllMarkings(state);
          detStates.setAllMarkings(detstate, stateMarkings);
        }
        detStates.removeRedundantPropositions();

        final int numTrans = mTransitionBuffer.size();
        rel.reset(detStates, numTrans, ListBufferTransitionRelation.CONFIG_SUCCESSORS);
        mTransitionBuffer.addOutgoingTransitions(rel);
        rel.reconfigure(ListBufferTransitionRelation.CONFIG_PREDECESSORS);

        final int dumpstate = findCertainConflicts(rel);
        rel = getTransitionRelation();
        rel.reconfigure(ListBufferTransitionRelation.CONFIG_SUCCESSORS);
        if (dumpstate > -1) rel.removeOutgoingTransitions(dumpstate);
        rel.checkReachability();
        removeProperSelfLoopEvents();
      }
    }

    protected int findDeterministicCertainConflicts(final ListBufferTransitionRelation rel) throws AnalysisException
    {
      // mBadStates was already populated at beginning
      final int[] allBadStates = mBadStates.toArray();
      Arrays.sort(allBadStates);
      final int dumpstate = allBadStates[0];
      mBadStatesLevels = new int[rel.getNumberOfStates()];
      for (int i = 0; i < mBadStatesLevels.length; i++) mBadStatesLevels[i] = -1;
      for (final int i : allBadStates) {
        mBadStatesLevels[i] = 0;
      }

      for (int i = 0; i < mBadStates.size(); i++)
      {
        final TransitionIterator iter = rel.createPredecessorsModifyingIterator();
        final int s = allBadStates[i];
        iter.resetState(s);
        while (iter.advance())
        {
          final int from = iter.getCurrentSourceState();

          if (i == 0 && !mBadStates.contains(from))
            continue;

          if (!mBadStates.contains(from))
          {
            rel.addTransition(from, iter.getCurrentEvent(), dumpstate);
            if (rel.isInitial(s))
            {
              rel.setInitial(dumpstate, true);
              rel.setInitial(s, false); // its being set afterwards
            }
          }
          iter.remove();
        }
      }
      return dumpstate;
    }

    protected int findCertainConflicts(ListBufferTransitionRelation rel) throws AnalysisException
    {
      // to determine when no new states have been added to bad states
      boolean brothersAdded = false;
      // keep track of bad states
      mBadStates = new TIntHashSet();
      // level of certain conflict for each bad state (initialise to -1)
      mBadStatesLevels = new int[rel.getNumberOfStates()];
      for (int i = 0; i < mBadStatesLevels.length; i++) mBadStatesLevels[i] = -1;

      int level = 0;
      do
      {
        final int before = updateBadStates(rel, false, level++);
        if (before == 0) return -1;
        // find brothers of bad states (they'll be added to mBadStates)
        // and go again if new states were added.
        brothersAdded = findBrothers(level) != before;
      } while (brothersAdded);

      // select dump state as first item
      int[] allBadStates = mBadStates.toArray();
      mOptimisationUsed = false;

      if (CheckAllBad() && mOldRel.getNumberOfStates() < rel.getNumberOfStates() && mOldRel.getNumberOfTransitions() < rel.getNumberOfTransitions())
      {
        mOptimisationUsed = true;
        // stop here if re-running for counterexample (only need to know that optimisation was used)
        if (isFindingCounterExample) return -1;

        // replace rel with oldrel
        setTransitionRelation(mOldRel);
        rel = getTransitionRelation();
        mBadStates = new TIntHashSet();

        // replace bad states with states the original automaton knows about
        for (int i = 0; i < allBadStates.length; i++)
        {
          final int offset = mSetOffsets.get(allBadStates[i]);
          mBadStates.add(mStateSetBuffer.getSet(offset)[0]);
        }
        allBadStates = mBadStates.toArray();
      }

      Arrays.sort(allBadStates);

      if (isFindingCounterExample) return -1;

      final int dumpstate = allBadStates[0];
      rel.setMarked(dumpstate, getDefaultMarkingID(), false);

      // redirect all bad states to the dump state
      for (int i = 0; i < mBadStates.size(); i++)
      {
        final TransitionIterator iter = rel.createPredecessorsModifyingIterator();
        final int badState = allBadStates[i];
        iter.resetState(badState);
        if (rel.isInitial(badState))
        {
          rel.setInitial(badState, false);
          rel.setInitial(dumpstate, true);
        }
        while (iter.advance())
        {
          final int from = iter.getCurrentSourceState();
          if (i == 0 && !mBadStates.contains(from))
              continue;
          if (!mBadStates.contains(from))
          {
              rel.addTransition(from, iter.getCurrentEvent(), dumpstate);
          }
          iter.remove();
        }
      }
      return dumpstate;
    }

    protected boolean CheckAllBad()
    {
      boolean returnVal = true;
      mTotalBadStates = new TIntHashSet();
      final int[] allBadStates = mBadStates.toArray();
      for (int i = 0; i < mBadStates.size(); i++)
      {
        final int badSetoffset = mSetOffsets.get(allBadStates[i]);
        final int[] badSet = mStateSetBuffer.getSet(badSetoffset);
        final int originalBad = badSet[0];

        for (int j = 0; j < mStateSetBuffer.size(); j++)
        {
          final int testoffset = mSetOffsets.get(j);
          final int[] test = mStateSetBuffer.getSet(testoffset);
          final int nTest = test[0];
          if (nTest == originalBad)
          {
            if (!mBadStates.contains(j))
            {
              returnVal = false;
            }
          }
        }
        if (!returnVal) return false;
        else mTotalBadStates.add(i);
      }
      return returnVal;
    }


    protected int findBrothers(final int level)
    {
      final int[] arr_mBadStates = mBadStates.toArray();

      for (int i = 0; i < arr_mBadStates.length; i++)
      {
        final int currentbadoffset = mSetOffsets.get(arr_mBadStates[i]);

        final int[] badStateSet = mStateSetBuffer.getSet(currentbadoffset);

        //now permute badStateSet
        // first order it, but remember what was originally first, no need to do that one again

        for (int j = 1; j < badStateSet.length; j++)
        {
          if (badStateSet[j] < badStateSet[j-1])
            arraySwap(badStateSet, j, j-1);
          else break;
        }

        // now permute
        for (int j = 0; j < badStateSet.length; j++)
        {
          arraySwap(badStateSet, 0, j);

          final int testinbuffer = mStateSetBuffer.get(badStateSet);

          if (testinbuffer > -1) {
            final int stateNum = mSetOffsets.binarySearch(testinbuffer);
            if (mBadStates.add(stateNum)) mBadStatesLevels[stateNum] = level;
          }
        }
      }
      return mBadStates.size();
    }

    protected void arraySwap(final int[] arr, final int swap1, final int swap2)
    {
        if (swap1 == swap2) return;
        final int temp = arr[swap1];
        arr[swap1] = arr[swap2];
        arr[swap2] = temp;
    }

    // this function updates the values in the variable mBadStates
    // it returns the number of states in mBadStates
    protected int updateBadStates(final ListBufferTransitionRelation rel, final boolean intial, final int level) throws AnalysisException
    {
        final TransitionIterator prediter = rel.createPredecessorsReadOnlyIterator();
        final int defaultID = getDefaultMarkingID();
        final int numStates = rel.getNumberOfStates();
        final TIntHashSet coreachableStates = new TIntHashSet(numStates);
        final TIntStack unvisitedStates = new TIntArrayStack();
        // Creates a hash set of all states which can reach an omega marked or alpha
        // marked state
        for (int sourceID = 0; sourceID < numStates; sourceID++)
        {
          if (rel.isMarked(sourceID, defaultID) &&
            rel.isReachable(sourceID) &&
            !mBadStates.contains(sourceID) &&
            coreachableStates.add(sourceID) )
          {
            checkAbort();
            unvisitedStates.push(sourceID);

            while (unvisitedStates.size() > 0) {
              final int newSource = unvisitedStates.pop();

              prediter.resetState(newSource);

              while (prediter.advance()) {
                final int predID = prediter.getCurrentSourceState();
                if (rel.isReachable(predID) && !mBadStates.contains(predID) && predID != newSource && coreachableStates.add(predID))
                {
                  unvisitedStates.push(predID);
                }
              }
            }
          }
        }
        // Blacklist states which cannot reach a state marked.
        for (int sourceID = 0; sourceID < numStates; sourceID++) {
          if (rel.isReachable(sourceID) && !coreachableStates.contains(sourceID)) {
            mBadStates.add(sourceID);
            if (level > -1 && mBadStatesLevels[sourceID] == -1) // level == -1 if initial badstates check
            {
              mBadStatesLevels[sourceID] = level;
            }

            // remove marking
            if (!intial && isFindingCounterExample) rel.setMarked(sourceID, getDefaultMarkingID(), false);
          }
        }
        return mBadStates.size();
    }

    public int[] runForCE() throws AnalysisException
    {
      setUp();
      isFindingCounterExample = true;
      runSimplifier();
      return mBadStatesLevels;
    }

    public AutomatonProxy createTestAutomaton(final ProductDESProxyFactory factory,
                                              final EventEncoding eventEnc,
                                              final StateEncoding testAutomatonStateEncoding,
                                              final int initTest,
                                              final EventProxy checkedProposition,
                                              final int level)
    {

      final ListBufferTransitionRelation rel = getTransitionRelation();
      final int numEvents = eventEnc.getNumberOfEvents();
      final Collection<EventProxy> events = new ArrayList<EventProxy>(numEvents);
      for (int e = 0; e < eventEnc.getNumberOfProperEvents(); e++) {
        if ((rel.getProperEventStatus(e) & EventEncoding.STATUS_UNUSED) == 0) {
          final EventProxy event = eventEnc.getProperEvent(e);
          if (event != null) {
            events.add(event);
          }
        }
      }
      events.add(checkedProposition);
      final int numStates = rel.getNumberOfStates();
      int numReachable = 0;
      int numCritical = 0;
      for (int state = 0; state < numStates; state++) {
        if (rel.isReachable(state)) {
          numReachable++;
          if (mBadStatesLevels[state] <= level && mBadStatesLevels[state] > -1) {
            numCritical++;
          }
        }
      }
      final StateProxy[] states = new StateProxy[numStates];
      final List<StateProxy> reachable = new ArrayList<StateProxy>(numReachable);
      final int numTrans = rel.getNumberOfTransitions();
      final Collection<TransitionProxy> transitions =
        new ArrayList<TransitionProxy>(numTrans + numCritical);
      int code = 0;
      for (int state = 0; state < numStates; state++) {
        if (rel.isReachable(state)) {
          final boolean init =
            initTest >= 0 ? state == initTest : rel.isInitial(state);
          final StateProxy memstate = new MemStateProxy(code++, init);
          states[state] = memstate;
          reachable.add(memstate);
          final int info = mBadStatesLevels[state];
          if (info != -1 && info <= level) {
            final TransitionProxy trans =
              factory.createTransitionProxy(memstate, checkedProposition, memstate);
            transitions.add(trans);
          }
        }
      }
      testAutomatonStateEncoding.init(states);
      final TransitionIterator iter = rel.createAllTransitionsReadOnlyIterator();
      while (iter.advance()) {
        final int s = iter.getCurrentSourceState();
        if (rel.isReachable(s)) {
          final int t = iter.getCurrentTargetState();
          final StateProxy source = states[s];
          final int e = iter.getCurrentEvent();
          final EventProxy event = eventEnc.getProperEvent(e);
          final StateProxy target = states[t];
          final TransitionProxy trans =
            factory.createTransitionProxy(source, event, target);
          transitions.add(trans);
        }
      }
      final String name = rel.getName() + ":certainconf:" + level;
      final ComponentKind kind = ComponentKind.PLANT;
      return factory.createAutomatonProxy(name, kind,
                                          events, reachable, transitions);
    }

    public int[] getStateSet(final int state)
    {
      int[] set;
      if (mIsDeterministic)
      {
        set = new int[1];
        set[0] = state;
      } else {
        final int offset = mSetOffsets.get(state);
        set = mStateSetBuffer.getSet(offset);
      }

      return set;
    }

    public ArrayList<Integer> getStateSetArrayList(final int state)
    {
        final ArrayList<Integer> ali = new ArrayList<Integer>();
        final int[] set = getStateSet(state);
        for (int i = 0; i < set.length; i++)
        {
          ali.add(set[i]);
        }
        return ali;
    }

    public int findStateFromSet(final int[] stateSet)
    {
      if (mIsDeterministic)
      {
        if (stateSet.length > 1) return -1;
        return stateSet[0];
      } else {
        final int testinbuffer = mStateSetBuffer.get(stateSet);
        if (testinbuffer == -1) return -1;
        return mSetOffsets.binarySearch(testinbuffer);
      }
    }

    public boolean getWasOptimisationUsed()
    {
      return mOptimisationUsed;
    }


    //#########################################################################
    //# Data Members
    private final int mStateLimit = Integer.MAX_VALUE;
    private final int mTransitionLimit = Integer.MAX_VALUE;

    private boolean mIsDeterministic;
    private int[] mEventIndexes;
    private TransitionIterator mTauIterator;
    private TransitionIterator mEventIterator;
    private TIntArrayList mSetOffsets;
    private TIntHashSet mBadStates;
    private int[] mBadStatesLevels;
    private IntSetBuffer mStateSetBuffer;
    private PreTransitionBuffer mTransitionBuffer;
    private ListBufferTransitionRelation mOldRel;
    private int numInit; // number of initial states
    private boolean isFindingCounterExample;
    private boolean mOptimisationUsed;
    private int mInitialBadStates;
    private TIntHashSet mTotalBadStates;

}
