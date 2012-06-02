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
        mStateSetBuffer = new IntSetBuffer(numStates);
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
      return ListBufferTransitionRelation.CONFIG_ALL;
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
        mSetOffsets = null;
        mStateSetBuffer = null;
        final int numTrans = mTransitionBuffer.size();
        final int config = getPreferredInputConfiguration();
        rel.reset(detStates, numTrans, config);
        rel.removeEvent(EventEncoding.TAU);
        mTransitionBuffer.addOutgoingTransitions(rel);
mTransitionBuffer = null;
        rel.removeProperSelfLoopEvents();
        if (!certainconfalgo(rel)) System.out.println("Error");

      }

    }

    protected boolean certainconfalgo(final ListBufferTransitionRelation rel) throws AnalysisException
      {
        //final ListBufferTransitionRelation rel = getTransitionRelation();
        TIntHashSet blacklist = findbadstates(rel);
        if (blacklist.size() == 0) return true;

        final int dumpstate = blacklist.toArray()[0];
        // TODO: ensure dump state is not marked

        while(blacklist.size() > 1) { // keep going until no more changes (except dump state?)
          final int[] blacklist_arr = blacklist.toArray();
          for (int i = 0; i < blacklist.size(); i++) {
            // TODO: add brothers of items on blacklist to blacklist
          }

          // for each item on blacklist, all transitions in get redirected to dump state
          // then remove item from transition relation (set reachable false)
          for (int i = 0; i < blacklist.size(); i++) {
            rel.moveIncomingTransitions(blacklist_arr[i], dumpstate);
            rel.setReachable(blacklist_arr[i], false);
            // TODO: (if item is initial, set dump state to be initial)
          }

          blacklist = findbadstates(rel);
        }
        return true;
      }

    protected TIntHashSet findbadstates(final ListBufferTransitionRelation rel) throws AnalysisException {
        final TIntHashSet badstates = new TIntHashSet();

        final TransitionIterator prediter = rel.createPredecessorsReadOnlyIterator();
        final int alphaID = getPreconditionMarkingID();
        final int defaultID = getDefaultMarkingID();

        final int numStates = rel.getNumberOfStates();
        final TIntHashSet coreachableStates = new TIntHashSet(numStates);
        final TIntStack unvisitedStates = new TIntStack();
        // Creates a hash set of all states which can reach an omega marked or alpha
        // marked state.
        for (int sourceID = 0; sourceID < numStates; sourceID++) {
          System.out.println("marking a:"+rel.isMarked(sourceID, defaultID));
          System.out.println("marking b:"+rel.isMarked(sourceID, alphaID));
          System.out.println("reachable:"+rel.isReachable(sourceID));
          if ((rel.isMarked(sourceID, defaultID) ||
               rel.isMarked(sourceID, alphaID)) &&
              rel.isReachable(sourceID) &&
              coreachableStates.add(sourceID) ) {
            checkAbort();
            unvisitedStates.push(sourceID);
            while (unvisitedStates.size() > 0) {
              final int newSource = unvisitedStates.pop();
              prediter.resetState(newSource);
              while (prediter.advance()) {
                final int predID = prediter.getCurrentSourceState();
                if (rel.isReachable(predID) && coreachableStates.add(predID)) {
                  unvisitedStates.push(predID);
                }
              }
            }
          }
        }
        // Blacklist states which cannot reach a state marked alpha or omega.

        for (int sourceID = 0; sourceID < numStates; sourceID++) {
          if (rel.isReachable(sourceID) && !coreachableStates.contains(sourceID)) {
            badstates.add(sourceID);
          }
        }

        return badstates;
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
    private IntSetBuffer mStateSetBuffer;
    private PreTransitionBuffer mTransitionBuffer;
}