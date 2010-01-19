//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.analysis
//# CLASS:   MonolithicConflictChecker
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.analysis.monolithic;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import gnu.trove.*;

import net.sourceforge.waters.model.analysis.AbortException;
import net.sourceforge.waters.model.analysis.AbstractConflictChecker;
import net.sourceforge.waters.model.analysis.AnalysisException;
import net.sourceforge.waters.model.analysis.OverflowException;
import net.sourceforge.waters.model.analysis.VerificationResult;
import net.sourceforge.waters.model.des.AutomatonProxy;
import net.sourceforge.waters.model.des.ConflictTraceProxy;
import net.sourceforge.waters.model.des.EventProxy;
import net.sourceforge.waters.model.des.ProductDESProxy;
import net.sourceforge.waters.model.des.ProductDESProxyFactory;
import net.sourceforge.waters.model.des.StateProxy;
import net.sourceforge.waters.model.des.TraceStepProxy;
import net.sourceforge.waters.model.des.TransitionProxy;
import net.sourceforge.waters.xsd.des.ConflictKind;


/**
 * <P>
 * A monolithic conflict checker implementation purely written in Java. This
 * conflict checker uses explict state enumeration with some optimisation of
 * data structures.
 * </P>
 *
 * @author Sam Douglas
 */

public class MonolithicConflictChecker extends AbstractConflictChecker
{

  // #########################################################################
  // # Constructors
  /**
   * Creates a new conflict checker without a model or marking proposition.
   */
  public MonolithicConflictChecker(final ProductDESProxyFactory factory)
  {
    super(factory);
  }

  /**
   * Creates a new conflict checker to check whether the given model
   * nonconflicting with respect to the default marking proposition.
   *
   * @param model
   *          The model to be checked by this conflict checker.
   * @param factory
   *          Factory used for trace construction.
   */
  public MonolithicConflictChecker(final ProductDESProxy model,
                                   final ProductDESProxyFactory factory)
  {
    super(model, factory);
  }

  /**
   * Creates a new conflict checker to check a particular model.
   *
   * @param model
   *          The model to be checked by this conflict checker.
   * @param marking
   *          The proposition event that defines which states are marked. Every
   *          state has a list of propositions attached to it; the conflict
   *          checker considers only those states as marked that are labelled by
   *          <CODE>marking</CODE>, i.e., their list of propositions must
   *          contain this event(exactly the same object).
   * @param factory
   *          Factory used for trace construction.
   */
  public MonolithicConflictChecker(final ProductDESProxy model,
                                   final EventProxy marking,
                                   final ProductDESProxyFactory factory)
  {
    super(model, marking, factory);
  }

  /**
   * Creates a new conflict checker to check a particular model.
   *
   * @param model
   *          The model to be checked by this conflict checker.
   * @param marking
   *          The proposition event that defines which states are marked. Every
   *          state has a list of propositions attached to it; the conflict
   *          checker considers only those states as marked that are labelled by
   *          <CODE>marking</CODE>, i.e., their list of propositions must
   *          contain this event(exactly the same object).
   * @param preMarking
   *          The proposition event that defines which states have alpha
   *          (precondition) markings.
   * @param factory
   *          Factory used for trace construction.
   */
  public MonolithicConflictChecker(final ProductDESProxy model,
                                   final EventProxy marking,
                                   final EventProxy preMarking,
                                   final ProductDESProxyFactory factory)
  {
    super(model, marking, preMarking, factory);
  }

  // #########################################################################
  // # Invocation
  public boolean run() throws AnalysisException
  {
    try {
      setUp();
      // First get the model
      final ProductDESProxy model = getModel();
      mEventMap = new EventMap();
      mSyncProduct = new SyncProduct();
      mSyncProduct.build();

      // Set the marked states coreachable and explore their predecessors.
      final int numstates = mSyncProduct.getNumberOfStates();
      final BitSet coreachable = new BitSet(numstates);
      final EventProxy marking = getUsedMarkingProposition();
      final EventProxy preconditionMarking = getGeneralisedPrecondition();
      final SyncStateSchema stateSchema =
          new SyncStateSchema(model, mEventMap, marking, preconditionMarking);
      final AutomatonSchema[] automata = stateSchema.getOrdering();
      final int numaut = automata.length;
      final int[] dstate = new int[numaut];
      boolean ok = true;
      for (int stateid = 0; stateid < numstates; stateid++) {
        final long state = mSyncProduct.getStateFromId(stateid);
        // Decode the state.
        stateSchema.decodeState(state, dstate);
        // check if the state is marked
        boolean marked = true;
        for (int i = 0; i < automata.length; i++) {
          if (!automata[i].isMarked(dstate[i])) {
            marked = false;
            break;
          }
        }
        if (marked) {
          // state is marked so mark all predecessors as coreachable
          ok &= exploreBackwards(stateid, coreachable, MAXDEPTH);
        }
      }
      while (!ok) {
        ok = true;
        for (int stateid = 0; stateid < numstates; stateid++) {
          if (coreachable.get(stateid)) {
            ok &= exploreBackwards(stateid, coreachable, MAXDEPTH);
          }
        }
      }
      // if all precondition marked states are coreachable then the model
      // is nonblocking
      boolean nonblocking = true;
      int firstBlockingState = -1;
      if (preconditionMarking != null) {
        for (int stateid = 0; stateid < numstates; stateid++) {
          final long state = mSyncProduct.getStateFromId(stateid);
          // Decode the state.
          stateSchema.decodeState(state, dstate);
          // check if the state has a precondition marking
          boolean premarked = true;
          for (int i = 0; i < automata.length; i++) {
            if (!automata[i].isPreconditionMarked(dstate[i])) {
              premarked = false;
              break;
            }
          }
          // if the state had a premarking, check if it is coreachable
          if (premarked) {
            if (!coreachable.get(stateid)) {
              firstBlockingState = stateid;
              nonblocking = false;
              break;
            }
          }
          checkAbort();
        }
      } else {
        // all states must be coreachable if there is only one marking type
        for (int stateid = 0; stateid < numstates; stateid++) {
          if (!coreachable.get(stateid)) {
            firstBlockingState = stateid;
            nonblocking = false;
            break;
          }
          checkAbort();
        }
      }
      if (nonblocking) {
        return setSatisfiedResult();
      } else {
        // Generate a counter example. As each state is numbered in the
        // order it is encountered, and a breadth first exploration
        // strategy is used, and all states are reachable, following the
        // transition to a state with the lowest id will give a
        // counterexample.
        final List<TraceStepProxy> countertrace =
            new LinkedList<TraceStepProxy>();
        final ProductDESProxyFactory desFactory = getFactory();
        // Find the unchecked state with the lowest
        // id, as this should give the shortest counterexample.
        // or if a second marking condition is simultaneously used, look
        // for the first non-coreachable precondition marked state.
        int trace_start = firstBlockingState;
        // Until we reach the start state...
        while (trace_start != 0) {
          final TIntArrayList preds = mSyncProduct.getPredecessors(trace_start);
          final int pred = preds.get(0);
          final EventProxy event = mSyncProduct.findEvent(pred, trace_start);
          final Map<AutomatonProxy,StateProxy> statemap =
              new HashMap<AutomatonProxy,StateProxy>();
          for (@SuppressWarnings("unused")
          final AutomatonProxy aut : model.getAutomata()) {
            // statemap.put(aut, value);
          }
          final TraceStepProxy traceStep =
              desFactory.createTraceStepProxy(event, statemap);
          countertrace.add(0, traceStep);
          trace_start = pred;
        }
        final TraceStepProxy startPoint =
            desFactory.createTraceStepProxy(null, null);
        countertrace.add(0, startPoint);
        final String modelname = model.getName();
        final String tracename = modelname + ":conflicting";
        // final String comment =
        final ConflictTraceProxy trace =
            desFactory.createConflictTraceProxy(tracename, null, null, model,
                                                model.getAutomata(),
                                                countertrace,
                                                ConflictKind.CONFLICT);
        return setFailedResult(trace);
      }

    } finally {
      // So the garbage collector can clean up ...
      mEventMap = null;
      mSyncProduct = null;
      tearDown();
    }
  }

  // #########################################################################
  // # Setting the Result
  protected void addStatistics(final VerificationResult result)
  {
    final int numaut = mSyncProduct.getNumberOfAutomata();
    final int numstates = mSyncProduct.getNumberOfStates();
    result.setNumberOfAutomata(numaut);
    result.setNumberOfStates(numstates);
    result.setPeakNumberOfNodes(numstates);
  }

  // #########################################################################
  // # Auxiliary Methods
  /**
   * Performs backwards search to find coreachable states.
   *
   * @param stateid
   *          The index of a state to be marked as coreachable and explored
   *          further.
   * @param coreachable
   *          The bitset to contain the indexes of all coreachable states.
   * @param maxdepth
   *          The maximum allowable recursion depth.
   * @return <CODE>true</CODE> if exploration was successful within the given
   *         depth, <CODE>false</CODE> otherwise.
   */
  private boolean exploreBackwards(final int stateid, final BitSet coreachable,
                                   int maxdepth) throws AbortException
  {
    if (maxdepth-- > 0) {
      checkAbort();
      coreachable.set(stateid);
      boolean result = true;
      final TIntArrayList preds = mSyncProduct.getPredecessors(stateid);
      final int len = preds.size();
      for (int i = 0; i < len; i++) {
        final int src = preds.get(i);
        if (!coreachable.get(src)) {
          result &= exploreBackwards(src, coreachable, maxdepth);
        }
      }
      return result;
    } else {
      return false;
    }
  }

  // #########################################################################
  // # Auxiliary Static Methods
  private static int clog2(int x)
  {
    x--;
    int y = 0;
    while (x > 0) {
      x >>= 1;
      y++;
    }
    return y;
  }


  // #########################################################################
  // # Inner Class SyncProduct
  private class SyncProduct
  {

    // #######################################################################
    // # Constructor
    private SyncProduct() throws AnalysisException
    {
      final ProductDESProxy model = getModel();
      final EventProxy marking = getUsedMarkingProposition();
      final EventProxy preconditionMarking = getGeneralisedPrecondition();
      // Create a new state schema for the product.
      mStateSchema =
          new SyncStateSchema(model, mEventMap, marking, preconditionMarking);
      final int numaut = getNumberOfAutomata();
      mSourceBuffer = new int[numaut];
      mTargetBuffer = new int[numaut];
      mNondeterministicAutomata = new int[numaut];
    }

    // #######################################################################
    // # Simple Access
    private int getNumberOfAutomata()
    {
      return mStateSchema.getNumberOfAutomata();
    }

    private int getNumberOfStates()
    {
      return mStateMap.size();
    }

    // #######################################################################
    // # Accessing the Fringe
    private int fringeGet()
    {
      return mFringeIndex++;
    }

    private int fringeSize()
    {
      return mNextStateIndex - mFringeIndex;
    }

    private TIntArrayList getPredecessors(final int stateid)
    {
      return mPredecessors.get(stateid);
    }

    // #######################################################################
    // # Auxiliary Methods
    /**
     * Register a state in the synchronous product. If the state already exists
     * in the state mapping it will be given the existing id, otherwise a new
     * mapping will be assigned and the id returned.
     */
    private int addNewState(final long state, final int pred)
        throws OverflowException
    {
      if (mStateMap.containsKey(state)) {
        final int oldid = mStateMap.get(state);
        if (oldid != pred && pred >= 0) {
          final TIntArrayList preds = mPredecessors.get(oldid);
          final int len = preds.size();
          if (len == 0 || pred != preds.get(len - 1)) {
            preds.add(pred);
          }
        }
        return oldid;
      } else {
        final int newid = mNextStateIndex++;
        if (mNextStateIndex > getNodeLimit()) {
          throw new OverflowException(getNodeLimit());
        }
        mStateMap.put(state, newid);
        statelist.add(state);
        final TIntArrayList preds = new TIntArrayList();
        if (pred >= 0) {
          preds.add(pred);
        }
        mPredecessors.add(preds);
        return newid;
      }
    }

    /**
     * Look up the state id the state list to get its 64 bit encoded version.
     */
    private long getStateFromId(final int id)
    {
      return statelist.get(id);
    }

    private void build() throws AnalysisException
    {
      // Compose the initial state.
      final AutomatonSchema[] automata = mStateSchema.getOrdering();
      final StateProxy[] state = new StateProxy[automata.length];
      for (int i = 0; i < automata.length; i++) {
        state[i] = automata[i].getInitialState();
        assert state[i] != null : "Every automaton must have an initial state";
      }
      final long init = mStateSchema.encodeState(state);
      addNewState(init, -1);
      // Expand all the states in the fringe, until no more
      // are added. This implies we have explored the entire
      // synchronous product.
      while (fringeSize() > 0) {
        expand();
        checkAbort();
      }
    }

    /**
     * Expands the first available state in the fringe. It is invalid to call
     * this method with an empty fringe.
     */
    private void expand() throws OverflowException
    {
      assert fringeSize() > 0 : "expand should not be called on an empty fringe";

      // Get the first unvisited state in the fringe.
      final int stateid = fringeGet();
      final long state = getStateFromId(stateid);
      // Decode the state.
      mStateSchema.decodeState(state, mSourceBuffer);
      final AutomatonSchema[] automata = mStateSchema.getOrdering();
      int i = 0;

      // Explore transitions ...
      nextevent: for (int eventid = 0; eventid < mEventMap.size(); eventid++) {
        for (int autid = 0; autid < automata.length; autid++) {
          final AutomatonSchema schema = automata[autid];
          final int src = mSourceBuffer[autid];
          final int[] targets = schema.getSuccessorState(src, eventid);
          if (targets == null) {
            continue nextevent;
          } else if (targets.length == 1) {
            mTargetBuffer[autid] = targets[0];
          } else {
            mNondeterministicAutomata[i] = autid;
            i++;
          }
        }
        // Hopefully we have a new state! Encode away ...
        expandNondeterministic(automata, eventid, stateid, i);

      }
    }

    private void expandNondeterministic(final AutomatonSchema[] automata,
                                        final int eventID, final int sourceID,
                                        final int i) throws OverflowException
    {
      if (i > 0) {
        final AutomatonSchema schema = automata[i];
        final int[][][] table = schema.mTransitionTable;
        final int[][] eventsSources = table[eventID];
        /*
         * if (eventsSources == null) { mTargetBuffer[i] = mSourceBuffer[i];
         * expandNondeterministic(automata, eventID, sourceID, i - 1); } else {
         */
        // there are (maybe) target states, process them ...
        final int[] targets = eventsSources[mSourceBuffer[i]];
        if (targets != null) {
          for (final int foundTarget : targets) {
            mTargetBuffer[i] = foundTarget;
            expandNondeterministic(automata, eventID, sourceID, i - 1);
          }
        }
        // }
      } else {
        final long newstate = mStateSchema.encodeState(mTargetBuffer);
        addNewState(newstate, sourceID);
      }
    }

    private EventProxy findEvent(final int sourceid, final int targetid)
    {
      final AutomatonSchema[] automata = mStateSchema.getOrdering();
      final int numaut = automata.length;
      final long sourcepacked = getStateFromId(sourceid);
      mStateSchema.decodeState(sourcepacked, mSourceBuffer);
      final long targetpacked = getStateFromId(targetid);
      mStateSchema.decodeState(targetpacked, mTargetBuffer);
      final int numevents = mEventMap.size();
      nextevent: for (int eventid = 0; eventid < numevents; eventid++) {
        for (int autid = 0; autid < numaut; autid++) {
          final AutomatonSchema schema = automata[autid];
          final int source = mSourceBuffer[autid];
          final int[] targets = schema.getSuccessorState(source, eventid);
          if (targets == null) {
            continue nextevent;
          } else {
            boolean eventFound = false;
            for (final int target : targets) {
              if (target == mTargetBuffer[autid]) {
                eventFound = true;
                break;
              }
            }
            if (!eventFound) {
              continue nextevent;
            }
          }
        }
        return mEventMap.getEvent(eventid);
      }
      return null;
    }

    // #######################################################################
    // # Data Members
    private final SyncStateSchema mStateSchema;

    private final List<TIntArrayList> mPredecessors =
        new BlockedArrayList<TIntArrayList>(TIntArrayList.class);

    /**
     * A map of synchronous product states to integer IDs, to allow the states
     * to be more efficiently encoded.
     */
    private final TLongIntHashMap mStateMap = new TLongIntHashMap();
    private final TLongArrayList statelist = new TLongArrayList();

    /**
     * The index of the first open state of the fringe.
     */
    private int mFringeIndex = 0;
    /**
     * The index of the next state to be created.
     */
    private int mNextStateIndex = 0;

    private final int[] mSourceBuffer;
    private final int[] mTargetBuffer;

    final int[] mNondeterministicAutomata;

  }


  // #########################################################################
  // # Inner Class EventMap
  /**
   * A mapping of EventProxy objects to integer identifiers. These identifiers
   * are used when building the synchronous product.
   */
  private class EventMap
  {

    // #######################################################################
    // # Constructor
    EventMap()
    {
      final ProductDESProxy model = getModel();
      final Set<EventProxy> eventset = model.getEvents();
      // Create an array of the events (arbitrarily)
      events = eventset.toArray(new EventProxy[] {});
      eventmap = new TObjectIntHashMap<EventProxy>();
      for (int i = 0; i < events.length; i++) {
        eventmap.put(events[i], i);
      }
    }

    // #######################################################################
    // # Simple Access
    int size()
    {
      return events.length;
    }

    EventProxy getEvent(final int id)
    {
      assert (id >= 0 && id < events.length);
      return events[id];
    }

    int getId(final EventProxy e)
    {
      assert eventmap.containsKey(e);
      return eventmap.get(e);
    }

    // #######################################################################
    // # Data Members
    private final EventProxy[] events;
    private final TObjectIntHashMap<EventProxy> eventmap;
  }


  // #########################################################################
  // # Inner Class SyncStateSchema
  /**
   * Represents a 'schema' for interpreting a state encoding for the synchronous
   * composition of automata.
   *
   * Automata will be assigned an ordering when the schema is created, and this
   * is assumed when encoding states.
   */
  private static class SyncStateSchema
  {

    // #######################################################################
    // # Constructor
    /**
     * Construct a state schema given the automata to be used in the synchronous
     * product.
     */
    private SyncStateSchema(final ProductDESProxy model,
                            final EventMap eventmap, final EventProxy marking,
                            final EventProxy preconditionMarking)
        throws OverflowException
    {
      // Build the schema for each automata. This gives
      // a fixed ordering for automata
      final Set<AutomatonProxy> automata = model.getAutomata();
      final int numaut = automata.size();
      final List<AutomatonSchema> list = new ArrayList<AutomatonSchema>(numaut);
      for (final AutomatonProxy aut : automata) {
        switch (aut.getKind()) {
        case PLANT:
        case SPEC:
          final AutomatonSchema schema =
              new AutomatonSchema(aut, eventmap, marking, preconditionMarking);
          list.add(schema);
          break;
        default:
          break;
        }
      }
      // Calculate the encoding data. This should be in a
      // different method, but to preserve immutableness it
      // shall be done in the constructor.
      final int numenc = list.size();
      mAutomata = new AutomatonSchema[numenc];
      list.toArray(mAutomata);
      mEncondings = new StateEncodingData[numenc];
      // Accumulates the amount of shifting necessary.
      int shamt = 0;
      for (int i = 0; i < mAutomata.length; i++) {
        // The number of bits that will be required
        final int bits = clog2(mAutomata[i].stateSize());
        mEncondings[i] = new StateEncodingData(bits, shamt);
        // Add the number of bits we encoded to the shift amount
        shamt += bits;
      }
      if (shamt > 64) {
        final String msg =
            "state encoding requires " + shamt + " bits, 64 is the maximum!";
        throw new OverflowException(msg);
      }
    }

    // #######################################################################
    // # Simple Access
    AutomatonSchema[] getOrdering()
    {
      return mAutomata;
    }

    int getNumberOfAutomata()
    {
      return mAutomata.length;
    }

    /**
     * A convenience method that encodes the state from an array of state proxy
     * objects by looking up the state indexes in the automata schema.
     */
    long encodeState(final StateProxy[] states)
    {
      assert states.length == mAutomata.length : "Wrong number of states given";
      final int[] istates = new int[states.length];
      for (int i = 0; i < states.length; i++) {
        istates[i] = mAutomata[i].getStateNumber(states[i]);
      }
      return encodeState(istates);
    }

    /**
     * Encode a synchronous product state from an array of state indexes. The
     * ordering of the states should match the ordering of automata given by the
     * getOrdering method.
     */
    long encodeState(final int[] states)
    {
      assert states.length == mAutomata.length : "Wrong number of states given";
      // The to-be-encoded state in the synchronous product.
      long sstate = 0;
      for (int i = 0; i < states.length; i++) {
        sstate = mEncondings[i].encode(sstate, states[i]);
      }
      return sstate;
    }

    /**
     * Decode a state from the synchronous product into its states in individual
     * automata. The states are given as integers.
     */
    void decodeState(final long state, final int[] tuple)
    {
      for (int i = 0; i < mEncondings.length; i++) {
        tuple[i] = mEncondings[i].decode(state);
      }
    }

    // #######################################################################
    // # Data Members
    private final AutomatonSchema[] mAutomata;
    /**
     * State encoding data for the synchronous product.
     */
    private final StateEncodingData[] mEncondings;

  }


  // #########################################################################
  // # Inner Class AutomatonSchema
  /**
   * Represents an automaton. This class exists to help give an
   * ordering/enumeration to states and transitions in an automaton.
   */
  private static class AutomatonSchema
  {

    // #######################################################################
    // # Constructor
    @SuppressWarnings("unchecked")
    AutomatonSchema(final AutomatonProxy automaton, final EventMap eventmap,
                    final EventProxy marking, final EventProxy preMarking)
    {
      // Enumerate the state set for this automata.
      // This gives an ordering to the states.
      final Set<StateProxy> states = automaton.getStates();
      final int numstates = states.size();
      mStates = new StateProxy[numstates];
      if (automaton.getEvents().contains(marking)) {
        mMarkedStates = new TIntHashSet(numstates);
      } else {
        mMarkedStates = null;
      }
      if (automaton.getEvents().contains(preMarking)) {
        mPreconditionMarkedStates = new TIntHashSet(numstates);
      } else {
        mPreconditionMarkedStates = null;
      }
      int i = 0;
      StateProxy initial = null;
      for (final StateProxy state : states) {
        if (state.isInitial()) {
          initial = state;
        }
        // If the state contains the marking proposition,
        // add it to the set of marked states.
        if (mMarkedStates != null && state.getPropositions().contains(marking)) {
          mMarkedStates.add(i);
        }
        if (mPreconditionMarkedStates != null
            && state.getPropositions().contains(preMarking)) {
          mPreconditionMarkedStates.add(i);
        }
        mStates[i] = state;
        mStateMap.put(state, i);
        i++;
      }
      mInitialState = initial;

      final int numevents = eventmap.size();
      mTransitionTable = new int[numevents][][];
      final Map<Integer,List<Integer>>[] tempTransitionTable =
          new HashMap[numevents];
      for (final EventProxy event : automaton.getEvents()) {
        final int eventid = eventmap.getId(event);
        mTransitionTable[eventid] = new int[numstates][];
        tempTransitionTable[eventid] =
            new HashMap<Integer,List<Integer>>(numstates);
        for (int srcid = 0; srcid < numstates; srcid++) {
          // final List<Integer> targetlist = new ArrayList<Integer>(1);
          // targetlist.add(-1);
          tempTransitionTable[eventid].put(srcid, null);
          mTransitionTable[eventid][srcid] = null;
        }
      }
      for (final TransitionProxy trans : automaton.getTransitions()) {
        final int srcid = getStateNumber(trans.getSource());
        final int destid = getStateNumber(trans.getTarget());
        final int eventid = eventmap.getId(trans.getEvent());
        List<Integer> existingTargets = tempTransitionTable[eventid].get(srcid);
        if (existingTargets == null) {
          existingTargets = new ArrayList<Integer>();
          tempTransitionTable[eventid].put(srcid, existingTargets);
        }
        existingTargets.add(destid);
      }
      // copies target contents from tempTransitionTable into an array for
      // easier processing later
      for (int j = 0; j < numevents; j++) {
        final Map<Integer,List<Integer>> sources = tempTransitionTable[j];
        for (int k = 0; k < numstates; k++) {
          if (sources != null) {
            final List<Integer> targetList = sources.get(k);
            if (targetList != null) {
              final int numtargets = targetList.size();
              final int[] targets = new int[numtargets];
              for (int m = 0; m < numtargets; m++) {
                targets[m] = targetList.get(m);
              }
              mTransitionTable[j][k] = targets;
            }
          }
        }
      }
    }

    /**
     * Gets the state number, given a state.
     */
    int getStateNumber(final StateProxy state)
    {
      return mStateMap.get(state);
    }

    @SuppressWarnings("unused")
    StateProxy getStateProxyFromID(final int index)
    {
      return mStates[index];
    }

    /**
     * Return the number of states in the automata.
     */
    int stateSize()
    {
      return mStates.length;
    }

    StateProxy getInitialState()
    {
      return mInitialState;
    }

    /**
     * Returns the successor state for the given source state and event, or -1
     * if the event is not enabled.
     */
    int[] getSuccessorState(final int src, final int event)
    {
      final int[][] alltargets = mTransitionTable[event];
      if (alltargets == null) {
        final int[] test = new int[] {src};
        return test;
      } else {
        final int[] targets = mTransitionTable[event][src];
        return targets;
      }
    }

    boolean isMarked(final int state)
    {
      if (mMarkedStates == null) {
        return true;
      } else {
        return mMarkedStates.contains(state);
      }
    }

    boolean isPreconditionMarked(final int state)
    {
      if (mPreconditionMarkedStates == null) {
        return true;
      } else {
        return mPreconditionMarkedStates.contains(state);
      }
    }

    // #######################################################################
    // # Data Members
    /**
     * An array of states in the automata, with a fixed ordering.
     */
    private final StateProxy[] mStates;
    /**
     * Gives a mapping of StateProxy object instances to their index in the
     * state array.
     */
    private final TObjectIntHashMap<StateProxy> mStateMap =
        new TObjectIntHashMap<StateProxy>();
    private final TIntHashSet mMarkedStates;
    private final TIntHashSet mPreconditionMarkedStates;
    private final int[][][] mTransitionTable;
    /**
     * The initial state of the automaton.
     */
    private final StateProxy mInitialState;

  }


  // #########################################################################
  // # Inner Class StateEncodingData
  /**
   * Data for encoding an automaton's state in the synchronous product. Bits is
   * the number of bits needed to represent the state, and shift is the amount
   * required to shift the value into position.
   *
   * |--------------------0011--------| |4 | bits |-8------| shift
   */
  private static class StateEncodingData
  {

    // #######################################################################
    // # Constructor
    StateEncodingData(final int bits, final int shift)
    {
      // Construct a mask for the state values.
      long tmask = 0;
      for (int i = 0; i < bits; i++) {
        tmask <<= 1;
        tmask |= 1;
      }
      mMask = tmask;
      mShift = shift;
    }

    // #######################################################################
    // # Simple Access
    /**
     * Encode the state for the current automata (s) into the synchronous
     * product state sstate.
     */
    long encode(final long sstate, final int s)
    {
      return sstate | (long) s << mShift;
    }

    int decode(final long sstate)
    {
      return (int) ((sstate >>> mShift) & mMask);
    }

    // #######################################################################
    // # Data Members
    private final long mMask;
    private final int mShift;

  }

  // #########################################################################
  // # Data Members
  private EventMap mEventMap;
  private SyncProduct mSyncProduct;

  // #########################################################################
  // # Class Constants
  private static final int MAXDEPTH = 1024;

}
