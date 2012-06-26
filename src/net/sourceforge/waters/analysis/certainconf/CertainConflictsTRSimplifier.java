package net.sourceforge.waters.analysis.certainconf;

import java.util.ArrayList;
import java.util.Collections;

import gnu.trove.TIntArrayList;
import gnu.trove.TIntHashSet;
import gnu.trove.TIntStack;

import net.sourceforge.waters.analysis.abstraction.AbstractMarkingTRSimplifier;
import net.sourceforge.waters.analysis.tr.EventEncoding;
import net.sourceforge.waters.analysis.tr.IntSetBuffer;
import net.sourceforge.waters.analysis.tr.IntStateBuffer;
import net.sourceforge.waters.analysis.tr.ListBufferTransitionRelation;
import net.sourceforge.waters.analysis.tr.OneEventCachingTransitionIterator;
import net.sourceforge.waters.analysis.tr.PreTransitionBuffer;
import net.sourceforge.waters.analysis.tr.TauClosure;
import net.sourceforge.waters.analysis.tr.TransitionIterator;
import net.sourceforge.waters.model.analysis.AnalysisException;
import net.sourceforge.waters.model.analysis.OverflowException;
import net.sourceforge.waters.model.analysis.OverflowKind;


class CertainConflictsTRSimplifier extends AbstractMarkingTRSimplifier {

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
        if (rel.isUsedEvent(EventEncoding.TAU)) {
          mIsDeterministic = false;
          final TauClosure closure =
            rel.createSuccessorsTauClosure(mTransitionLimit);
          mTauIterator = closure.createIterator();
          mEventIterator = closure.createPostEventClosureIterator(-1);
        } else if (!rel.isDeterministic()) {
          mIsDeterministic = false;
          mTauIterator = null;
          final TransitionIterator iter = rel.createSuccessorsReadOnlyIterator();
          mEventIterator = new OneEventCachingTransitionIterator(iter);
        } else {
          mIsDeterministic = true;
          return;
        }

        final int numEvents = rel.getNumberOfProperEvents();
        int index = 0;
        for (int event = EventEncoding.NONTAU; event < numEvents; event++) {
          if (rel.isUsedEvent(event)) {
            index++;
          }
        }
        mEventIndexes = new int[index];
        index = 0;
        for (int event = EventEncoding.NONTAU; event < numEvents; event++) {
          if (rel.isUsedEvent(event)) {
            mEventIndexes[index++] = event;
          }
        }

        final int numStates = rel.getNumberOfStates();
        mSetOffsets = new TIntArrayList(numStates);
        mStateSetBuffer = new IntSetBuffer(numStates, 0, -1);
        mTransitionBuffer = new PreTransitionBuffer(numEvents, mTransitionLimit);
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
    @Override
    public int getPreferredInputConfiguration()
    {
      return ListBufferTransitionRelation.CONFIG_SUCCESSORS;
    }

    @Override
    protected boolean runSimplifier() throws AnalysisException
    {
      if (mIsDeterministic) {
        return false;
      } else {
        // 1. Collect initial state set.
        final ListBufferTransitionRelation rel = getTransitionRelation();

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
        if (!init.isEmpty()) {
          final int offset = mStateSetBuffer.add(init);
          mSetOffsets.add(offset);
          last = offset;
        } else if (numStates == 0) {
          return false;
        }
        // 2. Expand subset states.
        final IntSetBuffer.IntSetIterator iter = mStateSetBuffer.iterator();
        final ArrayList<Integer> current = new ArrayList<Integer>();
        final ArrayList<Integer> leadingcurrent = new ArrayList<Integer>();

        for (int source = 0; source < mSetOffsets.size(); source++) {
          final int set = mSetOffsets.get(source);

          for (final int event : mEventIndexes) {
            checkAbort();

            // set iterators
            mEventIterator.resetEvent(event);
            iter.reset(set);
            // first state in set
            int leading = 0;
            boolean firstiter = false;
            // for each state in current set
            while (iter.advance()) {
              final int state = iter.getCurrentData();
              if (!firstiter) leading = state;
              firstiter = true;
              mEventIterator.resume(state);
              while (mEventIterator.advance()) {
                final int target = mEventIterator.getCurrentTargetState();
                current.add(target);
                if (state == leading) {
                  leadingcurrent.add(target);
                }
              }
            }
            if (current.isEmpty()) continue;
            if (!leadingcurrent.isEmpty()) {
              for (int l = 0; l < leadingcurrent.size(); l++) {
                // swap leading to the front
                Collections.swap(current, 0, current.indexOf(leadingcurrent.get(l)));
                // prepare for add to stateSetBuffer
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
            }
            current.clear();
            leadingcurrent.clear();
          }
        }

        // 3. Build new transition relation.
        applyResultPartitionAutomatically();
        return true;
      }
    }
    @Override
    protected void applyResultPartition()
    throws AnalysisException
    {
      if (mSetOffsets != null) {
        final ListBufferTransitionRelation rel = getTransitionRelation();
        final int numDetStates = mSetOffsets.size();
        final int numProps = rel.getNumberOfPropositions();
        final long usedProps = rel.getUsedPropositions();
        final IntStateBuffer detStates = new IntStateBuffer(numDetStates, numProps, usedProps);
        detStates.setInitial(0, true);
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
        rel.removeEvent(EventEncoding.TAU);
        mTransitionBuffer.addOutgoingTransitions(rel);
        rel.reconfigure(ListBufferTransitionRelation.CONFIG_PREDECESSORS);

        certainconfalgo(rel);
        rel.reconfigure(ListBufferTransitionRelation.CONFIG_SUCCESSORS);
        rel.checkReachability();
        rel.removeProperSelfLoopEvents();

      }

    }

    protected void certainconfalgo(final ListBufferTransitionRelation rel) throws AnalysisException
    {
      // to determine when no new states have been added to bad states
      boolean brothersAdded = false;
      // keep track of bad states
      mBadStates = new TIntHashSet();
      // total number of states
      final int numStates = rel.getNumberOfStates();

      do
      {
          final int before = updateBadStates(rel);
          if (before == 0) return;
          // find brothers of bad states (they'll be added to mBadStates
          // and go again if new states were added.
          brothersAdded = findBrothers() != before;
      } while (brothersAdded);

      // select dump state as first item
      final int[] allBadStates = mBadStates.toArray();
      final int dumpstate = allBadStates[0];

      for (int i = 0; i < numStates; i++) {
          final TransitionIterator iter =  rel.createPredecessorsModifyingIterator();
          iter.resetState(i);
          while (iter.advance())
          {
              final int from = iter.getCurrentSourceState();
              // remove transitions into the dump state from other bad states
              if (i == dumpstate )
              {
                  if (mBadStates.contains(from)) iter.remove();
                  continue;
              }
              // redirect transitions from good states, from bad to dump
              else if (mBadStates.contains(i) && !mBadStates.contains(from))
              {
                  rel.addTransition(from, iter.getCurrentEvent(), dumpstate);
                  if (rel.isInitial(i))
                      rel.setInitial(dumpstate, true);
              }

              if (mBadStates.contains(i) || from == dumpstate)
                  iter.remove();
          }
      }
    }

    @SuppressWarnings("unchecked")
    protected int findBrothers()
    {
      final int[] arr_mBadStates = mBadStates.toArray();

      final IntSetBuffer.IntSetIterator iter = mStateSetBuffer.iterator();

      for (int i = 0; i < arr_mBadStates.length; i++)
      {
          final int badstate = arr_mBadStates[i];
          iter.reset(badstate);

          // get the current set of states
          final ArrayList<Integer> t = new ArrayList<Integer>();
          while (iter.advance())
              t.add(iter.getCurrentData());

          // now permute set
          for (int j = 1; j < t.size(); j++)
          {
              final ArrayList<Integer> perm = (ArrayList<Integer>)t.clone();
              Collections.swap(perm, 0, j);
              // TODO: improve. (only an int[])
              Collections.sort(perm.subList(1, perm.size()));

              final int[] t_arr = new int[perm.size()];
              for (int k = 0; k < t_arr.length; k++)
                  t_arr[k] = perm.get(k);
              final int indexinset = mStateSetBuffer.get(t_arr);
              if (indexinset > -1)
                  mBadStates.add(indexinset);
          }
      }
      return mBadStates.size();
    }

    protected void testfunc()
    {

    }

    // this function updates the values in the variable mBadStates
    // it returns the number of states in mBadStates
    protected int updateBadStates(final ListBufferTransitionRelation rel) throws AnalysisException
    {
        final TransitionIterator prediter = rel.createPredecessorsReadOnlyIterator();
        final int defaultID = getDefaultMarkingID();

        final int numStates = rel.getNumberOfStates();
        final TIntHashSet coreachableStates = new TIntHashSet(numStates);
        final TIntStack unvisitedStates = new TIntStack();
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
                // remove marking
                rel.setMarked(sourceID, getDefaultMarkingID(), false);
            }
        }

        return mBadStates.size();
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
    private IntSetBuffer mStateSetBuffer;
    private PreTransitionBuffer mTransitionBuffer;
}