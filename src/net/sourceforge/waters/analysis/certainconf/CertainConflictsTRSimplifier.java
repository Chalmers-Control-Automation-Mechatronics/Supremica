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
        nTemp = numStates;
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
        //mSetOffsets = null;
        //mStateSetBuffer = null; need it later!
        final int numTrans = mTransitionBuffer.size();
        rel.reset(detStates, numTrans, ListBufferTransitionRelation.CONFIG_SUCCESSORS);
        rel.removeEvent(EventEncoding.TAU);
        mTransitionBuffer.addOutgoingTransitions(rel);
        rel.reconfigure(ListBufferTransitionRelation.CONFIG_PREDECESSORS);
        //rel.removeProperSelfLoopEvents();
        if (!certainconfalgo(rel)) System.out.println("Error");
        rel.reconfigure(ListBufferTransitionRelation.CONFIG_SUCCESSORS);
        rel.checkReachability();
        rel.removeProperSelfLoopEvents();

      }

    }

    protected boolean certainconfalgo(final ListBufferTransitionRelation rel) throws AnalysisException
    {
      System.out.println("Welcome to the certain conflicts algorithm");
      boolean brothersAdded = false;
      //final int dumpstate = blacklist.toArray()[0];
      // TODO: ensure dump state is not marked
      // not necessary : only way a marked state could be in set is as a brother - not from this list
      mBadStates = new TIntHashSet();

      do
      {
        mBadStates = findbadstates(rel);
        System.out.println("Found bad states:");
        PrintIntSet(mBadStates);
        final int before = mBadStates.size();
        if (before == 0) return true;
          final int[] brothers = findbrothers(mBadStates);
          for (int j = 0; j < brothers.length; j++) mBadStates.add(brothers[j]);
          brothersAdded = mBadStates.size() != before;
      } while (brothersAdded);

      System.out.println("The finding of bad states is complete, here is badstates:");
      PrintIntSet(mBadStates);
      // select dump state as first item
      final int[] allBadStates = mBadStates.toArray();
      final int dumpstate = allBadStates[0];
      System.out.println("Selecting dump state: " + dumpstate);
      //

      for (int i = 1; i < allBadStates.length; i++) {
        final TransitionIterator iter =  rel.createPredecessorsModifyingIterator();
        iter.resetState(allBadStates[i]);
        while (iter.advance()){
          if (!mBadStates.contains(iter.getCurrentSourceState()))
          {
           // rel.moveIncomingTransitions(allBadStates[i], dumpstate);
            rel.addTransition(iter.getCurrentSourceState(), iter.getCurrentEvent(), dumpstate);

          }

          iter.remove();
        // outgoing from dump
        }
        System.out.println("Moving incoming transitions into " + allBadStates[i] + " to " + dumpstate);
      }

      return true;
    }

    @SuppressWarnings("unchecked")
    protected int[] findbrothers(final TIntHashSet badstateshashset)
    {
      final int[] badstates = badstateshashset.toArray();
      System.out.println("finding brothers of:");
     PrintIntSet(badstates);
      //final ArrayList<Integer> alBrothers = new ArrayList<Integer>();
      final TIntHashSet brothers = new TIntHashSet();
      final IntSetBuffer.IntSetIterator iter = mStateSetBuffer.iterator();
      for (int i = 0; i < badstates.length; i++)
      {
        final int badstate = badstates[i];
        iter.reset(badstate);
        System.out.println("Looking at state" + badstate);
        ArrayList<Integer> t = new ArrayList<Integer>();
        while (iter.advance())
        {
          t.add(iter.getCurrentData());
        }
        // now permute t
        System.out.println("State " + badstate + " is set:");
        PrintIntSet(t);
        final ArrayList<Integer> originalt = (ArrayList<Integer>) t.clone();
        System.out.println("Now permuting");
        for (int j = 1; j < t.size(); j++)
        {
          t = (ArrayList<Integer>) originalt.clone();
          Collections.swap(t, 0, j);
          // TODO: improve. (only an int[])
          Collections.sort(t.subList(1, t.size()));
          System.out.println("Permuted:");
          PrintIntSet(t);
          final int[] t_arr = new int[t.size()];
          for (int k = 0; k < t_arr.length; k++)
              t_arr[k] = t.get(k);
          final int indexinset = mStateSetBuffer.get(t_arr);
          if (indexinset > -1)
          {
            System.out.println("Found brother, state #" + indexinset);
              brothers.add(indexinset);
          }
        }
      }

      System.out.println("Returning " + brothers.size() + " brothers");
      return brothers.toArray();
    }

    protected void PrintIntSet(final int[] in) {
      String s = "[";
      for (int i = 0; i < in.length; i++) {
        s += in[i];
        if (i < in.length - 1) s += ",";
      }
      s += "]";
      System.out.println(s);
    }
    protected void PrintIntSet(final TIntArrayList in) {
      String s = "[";
      for (int i = 0; i < in.size(); i++) {
        s += in.get(i);
        if (i < in.size() - 1) s += ",";
      }
      s += "]";
      System.out.println(s);
    }
    protected void PrintIntSet(final ArrayList<Integer> in) {
      String s = "[";
      for (int i = 0; i < in.size(); i++) {
        s += in.get(i);
        if (i < in.size() - 1) s += ",";
      }
      s += "]";
      System.out.println(s);
    }
    protected void PrintIntSet(final TIntHashSet in) {
      PrintIntSet(in.toArray());
    }
    protected void PrintIntSet(final TIntStack in) {
      PrintIntSet(in.toNativeArray());

    }

    protected TIntHashSet findbadstates(final ListBufferTransitionRelation rel) throws AnalysisException
    {
      System.out.println("~~~~~findbadstates start~~~~~");
      System.out.println("Known bad states: ");
      PrintIntSet(mBadStates);
      final TIntHashSet badstates = new TIntHashSet();
      final TransitionIterator prediter = rel.createPredecessorsReadOnlyIterator();
      final int defaultID = getDefaultMarkingID();

      final int numStates = rel.getNumberOfStates();
      final TIntHashSet coreachableStates = new TIntHashSet(numStates);
      final TIntStack unvisitedStates = new TIntStack();
      // Creates a hash set of all states which can reach an omega marked or alpha
      // marked state
      for (int sourceID = 0; sourceID < numStates; sourceID++) {
        System.out.println("Looking at state " + sourceID);
        //final TIntHashSet old_coreachableStates = (TIntHashSet) coreachableStates.clone();
        System.out.println("Storing old value of coreachableStates: ");
        //PrintIntSet(old_coreachableStates);
        if (rel.isMarked(sourceID, defaultID) &&
            rel.isReachable(sourceID) &&
            !mBadStates.contains(sourceID) &&
            coreachableStates.add(sourceID) ) {
          System.out.println("State " + sourceID + " is marked and not bad, so added to coreachable");
          checkAbort();
          unvisitedStates.push(sourceID);
          System.out.println("unvisitedstates is now");
          PrintIntSet(unvisitedStates);


          while (unvisitedStates.size() > 0) {
            final int newSource = unvisitedStates.pop();

            System.out.println("Popped " + newSource + " from the unvisitedStates stack");
            prediter.resetState(newSource);
            //System.out.println("NOGOODPRED IS NOW TRUE");
            //boolean noGoodPred = true;

            while (prediter.advance()) {
              final int predID = prediter.getCurrentSourceState();
              System.out.println(predID + " leads into " + newSource);
              if (rel.isReachable(predID) && !mBadStates.contains(predID) && predID != newSource && coreachableStates.add(predID)) {
                //noGoodPred = false;
                //System.out.println("NOGOODPRED IS NOW FALSE");
                System.out.println(predID + " is a legit predecessor");
                unvisitedStates.push(predID);
              }
            }
           // if (rel.isInitial(newSource)) noGoodPred = false;
            System.out.println("co-reachable states now : ");
            PrintIntSet(coreachableStates);
            /*if (noGoodPred)
            {
              System.out.println("But they were all bad so reset CRS:");
              coreachableStates = (TIntHashSet) old_coreachableStates.clone();
              PrintIntSet(coreachableStates);
              PrintIntSet(old_coreachableStates);
              unvisitedStates.clear();
              break;
            }*/
          }
        }
      }
      // Blacklist states which cannot reach a state marked alpha or omega.

      for (int sourceID = 0; sourceID < numStates; sourceID++) {
        if (rel.isReachable(sourceID) && !coreachableStates.contains(sourceID)) {
          badstates.add(sourceID);
          // remove marking
          rel.setMarked(sourceID, getDefaultMarkingID(), false);
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
    private TIntHashSet mBadStates;
    private IntSetBuffer mStateSetBuffer;
    private PreTransitionBuffer mTransitionBuffer;
    private int nTemp;
}