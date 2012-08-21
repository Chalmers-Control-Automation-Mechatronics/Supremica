package net.sourceforge.waters.analysis.certainconf;

import java.util.ArrayList;
import java.util.Arrays;
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
       // mOldRel = new ListBufferTransitionRelation(new AutomatonProxy(), 0, EventEncoding.FILTER_ALL);
        final int numEvents = rel.getNumberOfProperEvents();
        int index = 0;
        for (int event = 0; event < numEvents; event++) {
          if (rel.isUsedEvent(event)) {
            index++;
          }
        }
        mEventIndexes = new int[index];
        index = 0;
        for (int event = 0; event < numEvents; event++) {
          if (rel.isUsedEvent(event)) {
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
    @Override
    public int getPreferredInputConfiguration()
    {
      return ListBufferTransitionRelation.CONFIG_ALL;
    }

    protected void l(final String s) {
      if (logging) System.out.println(s);
    }
    protected void l(final String s, final TIntHashSet tihs) {
        l(s, tihs.toArray());
    }
    protected void l(final String s, final TIntArrayList tihs) {
      l(s, tihs.toNativeArray());
  }
    protected void l(final String s, final int[] ia)
    {
        if (!logging) return;
        System.out.print(s + " [");
        for (int i = 0; i < ia.length; i++) {
           if (i < ia.length - 1) System.out.print(ia[i]+",");
           else System.out.print(ia[i]+"]\n");
        }

    }
    @Override
    protected boolean runSimplifier() throws AnalysisException
    {
      final ListBufferTransitionRelation rel = getTransitionRelation();
    // if (rel.getName().equals("certainconf")) logging = true;
     //else logging = false;
      logging = false;
      l("Running on " + rel.getName());
      mBadStates = new TIntHashSet();
      if (updateBadStates(rel, true, -1) == 0) return false;
      l("is blocking");
      if (rel.isUsedEvent(EventEncoding.TAU)) {
        mIsDeterministic = false;
        l("tau used");
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
        mTauIterator = null;
        final TransitionIterator iter = rel.createSuccessorsReadOnlyIterator();
        mEventIterator = new OneEventCachingTransitionIterator(iter);
      }

      if (mIsDeterministic)
      {
        l("is deterministic...");
        final int dumpstate = detcertainconfalgo(rel);
        rel.reconfigure(ListBufferTransitionRelation.CONFIG_SUCCESSORS);
        if (dumpstate > -1) rel.removeOutgoingTransitions(dumpstate);
          rel.checkReachability();
          rel.removeProperSelfLoopEvents();
          return true;
      }
      l("not deterministic");
      // 1. Collect initial state set.
      final int numStates = rel.getNumberOfStates();
      final TIntArrayList init = new TIntArrayList();
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
      l("init states", init);
      int last = 0;
      final int[] arrInit = init.toNativeArray();
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
      // 2. Expand subset states.
      final IntSetBuffer.IntSetIterator iter = mStateSetBuffer.iterator();
      final ArrayList<Integer> current = new ArrayList<Integer>();
      final ArrayList<Integer> leadingcurrent = new ArrayList<Integer>();

      for (int source = 0; source < mSetOffsets.size(); source++) {
          final int set = mSetOffsets.get(source);
          l("~~~~~~~~set: "+ set, mStateSetBuffer.getSet(set));
          for (final int event : mEventIndexes) {
          checkAbort();
          l("looking at event " + event);
          // set iterators
          mEventIterator.resetEvent(event);
          iter.reset(set);
          // first state in set
          int leading = 0;
          boolean firstiter = true;
          // for each state in current set
          while (iter.advance()) {
            final int state = iter.getCurrentData();
            l("At state " + state);
            if (firstiter) leading = state;
            firstiter = false;
            mEventIterator.resume(state);
            while (mEventIterator.advance()) {
              final int target = mEventIterator.getCurrentTargetState();
              final int s = mEventIterator.getCurrentSourceState();
              l("iter advanced to " + target + " from " + s + " by event " + event);
              current.add(target);
              //if (event == 0) current.add(s);
              if (state == leading) {
                leadingcurrent.add(target);
              }
            }
          }
          if (current.isEmpty()) continue;
          logging = false;
          if (!leadingcurrent.isEmpty()) {
            for (int l = 0; l < leadingcurrent.size(); l++) {
              // swap leading to the front
              Collections.swap(current, 0, current.indexOf(leadingcurrent.get(l)));
              // prepare for add to stateSetBuffer
              Collections.sort(current.subList(1, current.size()));
              final int[] acurrent = new int[current.size()];
              for (int i = 0; i < acurrent.length; i++) acurrent[i] = current.get(i);

              final int offset = mStateSetBuffer.add(acurrent);
              l("Added new state set with id " + offset + ":", acurrent);
              final int target;
              if (offset > last) {
                  target = mSetOffsets.size();
                  if (target >= mStateLimit) {
                      throw new OverflowException(OverflowKind.STATE, mStateLimit);
                  }
                  mSetOffsets.add(offset);
                  l("mSetOffsets is now: ",mSetOffsets.toNativeArray());
                  last = offset;
              } else {
                  target = mSetOffsets.binarySearch(offset);
              }
              // add transitions for each permutation we just added
              mTransitionBuffer.addTransition(source, event, target);
              l("--New Transition from " + source + " to " + target + " with event " + event);
            }
          }
          logging = false;
          current.clear();
          leadingcurrent.clear();
        }
      }
      // 3. Build new transition relation.
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

        final int dumpstate = certainconfalgo(rel);
        rel = getTransitionRelation();
        l("dumpstate is " + dumpstate);
        rel.reconfigure(ListBufferTransitionRelation.CONFIG_SUCCESSORS);
        if (dumpstate > -1) rel.removeOutgoingTransitions(dumpstate);
        rel.checkReachability();
        rel.removeProperSelfLoopEvents();
        //logging = true;
        //l("BadStates",mBadStates);
       // l("Levels",mBadStatesLevels);
        //l(rel+"");
        //logging = false;

      }

    }

    protected int detcertainconfalgo(final ListBufferTransitionRelation rel) throws AnalysisException
    {
        // mBadStates was already populated at beginning
        final int[] allBadStates = mBadStates.toArray();
        Arrays.sort(allBadStates);
        final int dumpstate = allBadStates[0];

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
                        l("dSetting " + s + " no initial");
                    }
                }
                iter.remove();
            }
        }
        return dumpstate;
    }

    protected int certainconfalgo(ListBufferTransitionRelation rel) throws AnalysisException
    {
      logging = false;
      // to determine when no new states have been added to bad states
      boolean brothersAdded = false;
      // keep track of bad states
      mBadStates = new TIntHashSet();
      mBadStatesLevels = new int[rel.getNumberOfStates()];
      for (int i = 0; i < mBadStatesLevels.length; i++) mBadStatesLevels[i] = -1;

      // total number of states
      //final int numStates = rel.getNumberOfStates();
      l("start of certain conf");
      int level = 0;
      do
      {
          final int before = updateBadStates(rel, false, level++);
          l("mBadStates is now: ",mBadStates.toArray());
          if (before == 0) return -1;
          // find brothers of bad states (they'll be added to mBadStates
          // and go again if new states were added.
          brothersAdded = findBrothers(rel, level) != before;
      } while (brothersAdded);

      // select dump state as first item
      int[] allBadStates = mBadStates.toArray();

      l("!!!!!All Bad States: ", allBadStates);

      isFindingCounterExample = true;
      if (isFindingCounterExample) return -1;


          if (CheckAllBad() && mOldRel.getNumberOfStates() < rel.getNumberOfStates() && mOldRel.getNumberOfTransitions() < rel.getNumberOfTransitions())
          {
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



      final int dumpstate = allBadStates[0];
      rel.setMarked(dumpstate, getDefaultMarkingID(), false);

      for (int i = 0; i < mBadStates.size(); i++)
      {
          final TransitionIterator iter = rel.createPredecessorsModifyingIterator();
          final int s = allBadStates[i];
          l("looking at " + s);
          l(s + " is initial: " + rel.isInitial(s));
          iter.resetState(s);
          if (rel.isInitial(s))
          {
              rel.setInitial(s, false);
              rel.setInitial(dumpstate, true);
              l("Setting " + s + " no initial");
          }
          while (iter.advance())
          {

              final int from = iter.getCurrentSourceState();
l("Found a transition coming into " + s + " from " + from);
              if (i == 0 && !mBadStates.contains(from))
                  continue;
              if (!mBadStates.contains(from))
              {
                  l("redirecting to dump state");
                  rel.addTransition(from, iter.getCurrentEvent(), dumpstate);
              }
             l("aaand removing it");
              iter.remove();

          }
      }
      return dumpstate;
    }

    protected boolean CheckAllBad()
    {
        l("starting all bad check");
        final int[] allBadStates = mBadStates.toArray();
        l("there are "  + mBadStates.size() + " bad states");
        for (int i = 0; i < mBadStates.size(); i++)
        {
          l("looking at bad state " + allBadStates[i]);
            final int badSetoffset = mSetOffsets.get(allBadStates[i]);
            final int[] badSet = mStateSetBuffer.getSet(badSetoffset);
            l("it is ",badSet);
            final int originalBad = badSet[0];
            l("Finding all with start of " + originalBad);
            boolean returnVal = true;
            for (int j = 0; j < mStateSetBuffer.size(); j++)
            {
                final int testoffset = mSetOffsets.get(j);
                final int[] test = mStateSetBuffer.getSet(testoffset);
                l("Checking", test);
                final int nTest = test[0];
                if (nTest == originalBad)
                {
                    if (!mBadStates.contains(j))
                    {
                        l("Ah! Not bad!",test);
                        returnVal = false;
                    }
                    else
                    {
                        l("found at ");
                    }

                }
            }
            if (!returnVal) return false;
            else l("All ok");
        }

        return true;
    }


    protected int findBrothers(final ListBufferTransitionRelation rel, final int level)
    {
      l("========looking for brothers===========");
      final int[] arr_mBadStates = mBadStates.toArray();

      for (int i = 0; i < arr_mBadStates.length; i++)
      {
          final int currentbadoffset = mSetOffsets.get(arr_mBadStates[i]);

          final int[] badStateSet = mStateSetBuffer.getSet(currentbadoffset);
          l("looking for brothers of " + arr_mBadStates[i]);
          l("state set is", badStateSet);

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

                  l("brother found: " + testinbuffer);
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
        l("========looking for bad states===========");
        final int numStates = rel.getNumberOfStates();
        final TIntHashSet coreachableStates = new TIntHashSet(numStates);
        final TIntStack unvisitedStates = new TIntStack();
        // Creates a hash set of all states which can reach an omega marked or alpha
        // marked state
        for (int sourceID = 0; sourceID < numStates; sourceID++)
        {
            if (rel.isMarked(sourceID, defaultID)) l(sourceID + " is marked");
            if (rel.isMarked(sourceID, defaultID) &&
                rel.isReachable(sourceID) &&
                !mBadStates.contains(sourceID) &&
                coreachableStates.add(sourceID) )
            {
               // l(sourceID + " is marked and reachable");
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
                if (mBadStates.add(sourceID) && level > -1) mBadStatesLevels[sourceID] = level;
                // remove marking
                l("Found " + sourceID);
                if (!intial && isFindingCounterExample) rel.setMarked(sourceID, getDefaultMarkingID(), false);
            }
        }
        l("There are now " + mBadStates.size() + " bad states");
        return mBadStates.size();
    }

    public void runForCE() throws AnalysisException
    {

      setUp();
      isFindingCounterExample = true;
      runSimplifier();
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
    private boolean logging; // temporary variable to enable console messages
    private int numInit; // number of initial states
    private boolean isFindingCounterExample;

}
