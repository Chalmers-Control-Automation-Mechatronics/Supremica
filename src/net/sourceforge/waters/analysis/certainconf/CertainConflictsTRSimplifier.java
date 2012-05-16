package net.sourceforge.waters.analysis.certainconf;

import java.util.ArrayList;
import java.util.Collections;

import gnu.trove.TIntArrayList;
import gnu.trove.TIntHashSet;

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
        System.out.println("Initial relation:");
        System.out.println(rel.toString());
        final int numStates = rel.getNumberOfStates();
        System.out.println("numstates="+numStates);
        final TIntHashSet init = new TIntHashSet();
        for (int state = 0; state < numStates; state++) {
          System.out.println("Now looking at state "+state);
          if (rel.isInitial(state)) {
            if (mTauIterator == null) {
              System.out.println("No tau events, adding state "+state);
              init.add(state);
            } else {
              checkAbort();
              mTauIterator.resetState(state);
              while (mTauIterator.advance()) {
                final int tausucc = mTauIterator.getCurrentTargetState();
                init.add(tausucc);
              }
            }
          } else {
            System.out.println("state is not initial state, skipping");
          }
        }
        int last = 0;
        if (!init.isEmpty()) {
          System.out.println("Found initial states.");
          final int offset = mStateSetBuffer.add(init);
          System.out.println("First offset is "+offset);
          mSetOffsets.add(offset);
          last = offset;
        } else if (numStates == 0) {
          return false;
        }
        System.out.println("~~~Part 2~~~");
        // 2. Expand subset states.
        final IntSetBuffer.IntSetIterator iter = mStateSetBuffer.iterator();
        final ArrayList<Integer> current = new ArrayList<Integer>();
        final ArrayList<Integer> leadingcurrent = new ArrayList<Integer>();

        for (int source = 0; source < mSetOffsets.size(); source++) {
          final int set = mSetOffsets.get(source);

          System.out.println("Offset number "+source+" is "+set);
          for (final int event : mEventIndexes) {
            System.out.println("Checking for e"+event+" in set #"+ set);
            checkAbort();

            // set iterators
            mEventIterator.resetEvent(event);
            iter.reset(set);
            // first state in set
            int leading = 0;
            boolean firstiter = false;
            System.out.println("leading state is #"+leading);
            // for each state in current set
            while (iter.advance()) {
              final int state = iter.getCurrentData();
              if (!firstiter) leading = state;
              firstiter = true;
              System.out.println("Looking at state "+state);
              mEventIterator.resume(state);
              while (mEventIterator.advance()) {
                final int target = mEventIterator.getCurrentTargetState();
                System.out.println("Event e"+event+" can go to state "+target+", adding "+target+" to current arraylist");
                current.add(target);
                if (state == leading) {
                  System.out.println("Adding target "+target+" to leading list");
                  leadingcurrent.add(target);
                } else {
                  System.out.println("Not adding target "+target+" to leading list");
                }

              }
            }
            if (current.isEmpty()) continue;
            if (!leadingcurrent.isEmpty()) {
              //current.sort();
              // add all permutations of current
              for (int l = 0; l < leadingcurrent.size(); l++) {
                // swap leading to the front
                //System.out.println("Looking at leadingcurrent number "+l+" which is "+leadingcurrent.get(l));
                //System.out.println("And it is in position "+current.indexOf(leadingcurrent.get(l))+" of current");
                Collections.swap(current, 0, current.indexOf(leadingcurrent.get(l)));
                // prepare for add to stateSetBuffer
                Collections.sort(current.subList(1, current.size()));
                final int[] acurrent = new int[current.size()];
                for (int i = 0; i < acurrent.length; i++) acurrent[i] = current.get(i);

                final int offset = mStateSetBuffer.add(acurrent);
                //System.out.println("new offset from adding new set to buffer is "+offset);

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


            } else {
              System.out.println("No matches found for e"+event+" in set #"+set);
            }
            current.clear();
            leadingcurrent.clear();
            System.out.println(mTransitionBuffer.toString());
            System.out.println("--------------------");
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
        final IntStateBuffer detStates =
          new IntStateBuffer(numDetStates, numProps, usedProps);
        detStates.setInitial(0, true);
        final IntSetBuffer.IntSetIterator iter = mStateSetBuffer.iterator();
        for (int detstate = 0; detstate < numDetStates; detstate++) {
          long markings = detStates.createMarkings();
          final int offset = mSetOffsets.get(detstate);
          iter.reset(offset);
          //while (iter.advance()) {
          iter.advance();
            final int state = iter.getCurrentData();
            final long stateMarkings = rel.getAllMarkings(state);
            markings = detStates.mergeMarkings(markings, stateMarkings);
          //}
          detStates.setAllMarkings(detstate, markings);
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

      }
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