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
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import gnu.trove.*;

import net.sourceforge.waters.model.analysis.AbstractConflictChecker;
import net.sourceforge.waters.model.analysis.OverflowException;
import net.sourceforge.waters.model.analysis.VerificationResult;
import net.sourceforge.waters.model.des.AutomatonProxy;
import net.sourceforge.waters.model.des.ConflictTraceProxy;
import net.sourceforge.waters.model.des.EventProxy;
import net.sourceforge.waters.model.des.ProductDESProxy;
import net.sourceforge.waters.model.des.ProductDESProxyFactory;
import net.sourceforge.waters.model.des.StateProxy;
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
      final EventProxy marking, final ProductDESProxyFactory factory)
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
      final EventProxy marking, final EventProxy preMarking,
      final ProductDESProxyFactory factory)
  {
    super(model, marking, preMarking, factory);
  }

  // #########################################################################
  // # Invocation
  public boolean run() throws OverflowException
  {
    try {
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
      boolean ok = true;

      for (int stateid = 0; stateid < numstates; stateid++) {
        final long state = mSyncProduct.getStateFromId(stateid);
        // Decode the state.
        final int[] dstate = stateSchema.decodeState(state);
        final AutomatonSchema[] automata = stateSchema.getOrdering();
        assert automata.length == dstate.length;
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
          final int[] dstate = stateSchema.decodeState(state);
          final AutomatonSchema[] automata = stateSchema.getOrdering();
          assert automata.length == dstate.length;
          // check if the state has a precondition marking
          boolean premarked = true;
          for (int i = 0; i < automata.length; i++) {
            if (!automata[i].isPreconditionMarked(dstate[i])) {
              premarked = false;
              break;
            }
          }
          //if the state had a premarking, check if it is coreachable
          if (premarked) {
            if (!coreachable.get(stateid)) {
              firstBlockingState = stateid;
              nonblocking = false;
              break;
            }
          }
        }
      }
      //all states must be coreachable if there is only one marking type
      else {
        for (int stateid = 0; stateid < numstates; stateid++) {
          if (!coreachable.get(stateid)) {
            firstBlockingState = stateid;
            nonblocking = false;
            break;
          }
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
        final List<EventProxy> countertrace = new LinkedList<EventProxy>();
        // Find the unchecked state with the lowest
        // id, as this should give the shortest counterexample.
        // or if a second marking condition is simultaneously used, look
        // for the
        // first non coreachable precondition marked state.
        int trace_start = firstBlockingState;

        // Until we reach the start state...
        final TransitionSchema trans = mSyncProduct.transitions;
        while (trace_start != 0) {
          final TransitionData td = trans.getTransitions(trace_start);
          assert td != null : "This is impossible, every state is reachable!";
          TIntHashSet incoming = td.in;
          assert incoming != null : "Impossible? every state should be reachable!";
          int lowtrans = select_lowest_transition(incoming, trans);
          countertrace.add(0, mEventMap
              .getEvent(trans.store.getEvent(lowtrans)));
          trace_start = trans.store.getSource(lowtrans);
        }
        final ProductDESProxyFactory desFactory = getFactory();
        final String modelname = model.getName();
        final String tracename = modelname + ":conflicting";
        final ConflictTraceProxy trace =
            desFactory.createConflictTraceProxy(tracename, model, countertrace,
                ConflictKind.CONFLICT);
        return setFailedResult(trace);
      }

    } finally {
      // So the garbage collector can clean up ...
      mEventMap = null;
      mSyncProduct = null;
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
      int maxdepth)
  {
    if (maxdepth-- > 0) {
      coreachable.set(stateid);
      boolean result = true;
      final TransitionSchema trans = mSyncProduct.transitions;
      final TransitionData td = trans.getTransitions(stateid);
      if (td != null) {
        final TIntHashSet incoming = td.in;
        if (incoming != null) {
          final TIntIterator it = incoming.iterator();
          while (it.hasNext()) {
            final int t = it.next();
            final int src = trans.store.getSource(t);
            if (!coreachable.get(src)) {
              result &= exploreBackwards(src, coreachable, maxdepth);
            }
          }
        }
      }
      return result;
    } else {
      return false;
    }
  }

  /**
   * Check the transition set for the state with the lowest id, and return the
   * id.
   */
  private int select_lowest_transition(TIntHashSet set, TransitionSchema ts)
  {
    int lowest = Integer.MAX_VALUE;
    int lowesttr = 0;
    TIntIterator it = set.iterator();
    while (it.hasNext()) {
      int tr = it.next();
      int v = ts.store.getSource(tr);
      if (v < lowest) {
        lowest = v;
        lowesttr = tr;
      }
    }
    return lowesttr;
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
    private SyncProduct() throws OverflowException
    {
      final ProductDESProxy model = getModel();
      final EventProxy marking = getUsedMarkingProposition();
      final EventProxy preconditionMarking = getGeneralisedPrecondition();
      // Create a new state schema for the product.
      mStateSchema =
          new SyncStateSchema(model, mEventMap, marking, preconditionMarking);
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

    // #######################################################################
    // # Auxiliary Methods
    /**
     * Register a state in the synchronous product. If the state already exists
     * in the state mapping it will be given the existing id, otherwise a new
     * mapping will be assigned and the id returned.
     */
    private int registerState(long state) throws OverflowException
    {
      if (mStateMap.containsKey(state)) {
        return mStateMap.get(state);
      }
      final int id = mNextStateIndex++;
      if (mNextStateIndex > getNodeLimit()) {
        throw new OverflowException(getNodeLimit());
      }
      mStateMap.put(state, id);
      statelist.add(state);
      return id;
    }

    /**
     * Look up the state id the state list to get its 64 bit encoded version.
     */
    private long getStateFromId(int id)
    {
      return statelist.get(id);
    }

    private void build() throws OverflowException
    {
      // Compose the initial state.
      AutomatonSchema[] automata = mStateSchema.getOrdering();
      StateProxy[] state = new StateProxy[automata.length];
      for (int i = 0; i < automata.length; i++) {
        state[i] = automata[i].getInitialState();
        assert state[i] != null : "Every automaton must have an initial state";
      }
      long init = mStateSchema.encodeState(state);
      registerState(init);

      // Expand all the states in the fringe, until no more
      // are added. This implies we have explored the entire
      // synchronous product.
      while (fringeSize() > 0) {
        expand();
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
      final int[] dstate = mStateSchema.decodeState(state);
      final int[] rstate = new int[dstate.length];
      final AutomatonSchema[] automata = mStateSchema.getOrdering();
      assert automata.length == dstate.length;

      /*
       * // Does the current state need to be marked? boolean need_marking =
       * true; for (int i = 0; i < automata.length; i++) { if
       * (!automata[i].isMarked(dstate[i])) { need_marking = false; break; } }
       * if (need_marking) { mMarkedStates.add(stateid); }
       *
       * // Does the current state need to have a precondition marking? boolean
       * need_premarking = true; for (int i = 0; i < automata.length; i++) { if
       * (!automata[i].isPreconditionMarked(dstate[i])) { need_premarking =
       * false; break; } } if (need_premarking) {
       * mPreconditionMarkedStates.add(stateid); }
       */

      // Explore transitions ...
      nextevent: for (int eventid = 0; eventid < mEventMap.size(); eventid++) {
        for (int autid = 0; autid < automata.length; autid++) {
          final AutomatonSchema schema = automata[autid];
          final int src = dstate[autid];
          final int target = schema.getSuccessorState(src, eventid);
          if (target < 0) {
            continue nextevent;
          }
          rstate[autid] = target;
        }
        // Hopefully we have a new state! Encode away ...
        final long newstate = mStateSchema.encodeState(rstate);
        final int newid = registerState(newstate);
        if (stateid != newid) {
          transitions.addTransition(transitions.transitionFactory(stateid,
              newid, eventid));
        }
      }
    }

    // #########################################################################
    // # Data Members
    private final SyncStateSchema mStateSchema;

    private final TransitionSchema transitions =
        new TransitionSchema(false, true);

    /**
     * A map of synchronous product states to integer ids, to allow the states
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
      Set<EventProxy> eventset = model.getEvents();
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

    EventProxy getEvent(int id)
    {
      assert (id >= 0 && id < events.length);
      return events[id];
    }

    int getId(EventProxy e)
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
        final EventProxy preconditionMarking) throws OverflowException
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
        int bits = clog2(mAutomata[i].stateSize());
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
    long encodeState(StateProxy[] states)
    {
      assert states.length == mAutomata.length : "Wrong number of states given";
      int[] istates = new int[states.length];
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
    long encodeState(int[] states)
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
    int[] decodeState(long state)
    {
      int[] sarr = new int[mEncondings.length];
      for (int i = 0; i < mEncondings.length; i++) {
        sarr[i] = mEncondings[i].decode(state);
      }
      return sarr;
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
   * ordering/enumeration to states and transitions in an automata.
   */
  private static class AutomatonSchema
  {

    // #######################################################################
    // # Constructor
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
      mTransitionTable = new int[numevents][];
      for (final EventProxy event : automaton.getEvents()) {
        final int eventid = eventmap.getId(event);
        mTransitionTable[eventid] = new int[numstates];
        for (int srcid = 0; srcid < numstates; srcid++) {
          mTransitionTable[eventid][srcid] = -1;
        }
      }
      for (final TransitionProxy trans : automaton.getTransitions()) {
        final int srcid = getStateNumber(trans.getSource());
        final int destid = getStateNumber(trans.getTarget());
        final int eventid = eventmap.getId(trans.getEvent());
        mTransitionTable[eventid][srcid] = destid;
      }
    }

    /**
     * Gets the state number, given a state.
     */
    int getStateNumber(final StateProxy state)
    {
      return mStateMap.get(state);
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
    int getSuccessorState(final int src, final int event)
    {
      final int[] targets = mTransitionTable[event];
      if (targets == null) {
        return src;
      } else {
        return targets[src];
      }
    }

    boolean isMarked(int state)
    {
      if (mMarkedStates == null) {
        return true;
      } else {
        return mMarkedStates.contains(state);
      }
    }

    boolean isPreconditionMarked(int state)
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
    private final int[][] mTransitionTable;
    /**
     * The initial state of the automaton.
     */
    private final StateProxy mInitialState;

  }


  // #########################################################################
  // # Inner Class TransitionSchema
  private static class TransitionSchema
  {
    // #######################################################################
    // # Constructor
    TransitionSchema(boolean forward, boolean reverse)
    {
      transitions = new TLongObjectHashMap<TransitionData>();
      storeforward = forward;
      storereverse = reverse;
    }

    // #######################################################################
    // # Simple Access
    int transitionFactory(int src, int dest, int e)
    {
      return store.allocate(src, dest, e);
    }

    void addTransition(int t)
    {
      // Add the forward transition. Get the transition data
      // for the source state and add the transition to that.
      TransitionData td;
      if (storeforward) {
        td = makeTransitionData(store.getSource(t));
        td.addOutTransition(t);
      }
      // Now do the same thing for the destination state, but
      // add it as an incoming transition.
      if (storereverse) {
        td = makeTransitionData(store.getDestination(t));
        td.addInTransition(t);
      }
    }

    TransitionData getTransitions(final int state)
    {
      return transitions.get(state);
    }

    // #######################################################################
    // # Auxiliary Methods
    /**
     * Get, or make transition data. This will get an existing transition data
     * object for the given state, or it will create a new one and add it.
     */
    private TransitionData makeTransitionData(int state)
    {
      // Add the forwards transition data.
      TransitionData td = transitions.get(state);
      if (td == null) {
        td = new TransitionData();
        transitions.put(state, td);
      }
      return td;
    }

    // #######################################################################
    // # Data Members
    private final TransitionStore store = new TransitionStore();
    private final TLongObjectHashMap<TransitionData> transitions;

    private final boolean storeforward;
    private final boolean storereverse;

  }


  // #########################################################################
  // # Inner Class TransitionData
  private static class TransitionData
  {
    // #######################################################################
    // # Simple Access
    void addOutTransition(int t)
    {
      if (out == null) {
        out = new TIntHashSet();
      }
      out.add(t);
    }

    void addInTransition(int t)
    {
      if (in == null) {
        in = new TIntHashSet();
      }
      in.add(t);
    }

    // #######################################################################
    // # Data Members
    private TIntHashSet in = null;
    private TIntHashSet out = null;
  }


  // #########################################################################
  // # Inner Class TransitionStore
  /**
   * An attempt to efficiently allocate memory for transitions in an automaton.
   * This works by allocating into arrays. There is very little overhead as all
   * transitions have the same lifetime and so deallocating is not an issue.
   *
   * Unlike an array list, this class uses an array of arrays, and simply
   * allocates a new (possibly larger) array when the existing one is full.
   *
   * The transition is returned as a 32 bit integer which encodes an address
   * into this store. The 8 lowest bits indicate which 'chunk' the data is in,
   * the rest indicate the location in the current chunk.
   *
   * Each transition is stored as 3 integers, and chunk sizes will always be a
   * multiple of 3.
   */
  private static class TransitionStore
  {

    // #######################################################################
    // # Constructor
    public TransitionStore()
    {
      chunkptr = 0;
      pointer = 0;
      data[0] = allocate_chunk(chunk_size(0));
    }

    // #######################################################################
    // # Simple Access
    /**
     * Allocate a transition and set its source, destination and event values.
     */
    public int allocate(int source, int destination, int event)
    {
      // Is the current chunk full? If so, allocate a new
      // one first and reset the allocation pointer.
      if (pointer >= data[chunkptr].length) {
        chunkptr++;
        data[chunkptr] = allocate_chunk(chunk_size(chunkptr));
        pointer = 0;
      }

      // Store and increment the allocation pointer.
      int t = pointer;
      pointer += TRANSITION_SIZE;
      transition_count++;

      // Mask the event to be 20 bits, and source/dest to be 22 bits
      event &= 0xFFFFF;
      source &= 0x3FFFFF;
      destination &= 0x3FFFFF;

      // Store the transition data.
      data[chunkptr][t] = source;
      data[chunkptr][t + 1] = destination;
      data[chunkptr][t] |= (event << 22);
      data[chunkptr][t + 1] |= ((event >> 10) << 22);

      // Now encode the address.
      return (t << 8) | (chunkptr & 255);
    }

    @SuppressWarnings("unused")
    int transitionCount()
    {
      return transition_count;
    }

    int getSource(int trans)
    {
      return data[trans & 255][(trans >> 8)] & 0x3FFFFF;
    }

    int getDestination(int trans)
    {
      return data[trans & 255][(trans >> 8) + 1] & 0x3FFFFF;
    }

    int getEvent(int trans)
    {
      // Urgh. Decoding. Ugly.
      int val = (data[trans & 255][(trans >> 8)]) >> 22;
      return val | (((data[trans & 255][(trans >> 8) + 1]) >> 22) << 10);
    }

    // #######################################################################
    // # Auxiliary Methods
    private int[] allocate_chunk(int size)
    {
      // System.err.format ("Transitions: Allocating chunk of size %d\n",
      // size);
      return new int[size];
    }

    private int chunk_size(int chunknum)
    {
      // We don't want to keep doubling the size forever!
      // Once we get to chunk 11 [~= 2 million transitions/24MB]
      // just keep allocating in a linear fashion. This should
      // be sufficient.
      if (chunknum > 11)
        chunknum = 11;

      return (1024 * TRANSITION_SIZE) << chunknum;
    }

    // #######################################################################
    // # Data Members
    static final int TRANSITION_SIZE = 2;

    private final int[][] data = new int[256][];

    private int chunkptr;
    private int pointer;
    private int transition_count = 0;

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
    StateEncodingData(int bits, int shift)
    {
      // Construct a mask for the state values.
      long tmask = 0;
      for (int i = 0; i < bits; i++) {
        tmask <<= 1;
        tmask |= 1;
      }
      mask = tmask;
      this.shift = shift;
    }

    // #######################################################################
    // # Simple Access
    /**
     * Encode the state for the current automata (s) into the synchronous
     * product state sstate.
     */
    long encode(long sstate, int s)
    {
      // Shifted mask. This is used to clear the relevant bits
      long shmask = mask << shift;
      // Mask the value to be the correct size.
      long value = (s & mask) << shift;
      // Combine.
      return (sstate & ~shmask) | value;
    }

    int decode(long sstate)
    {
      // This cast should be fine, there will never be
      // more than 32bits of states in an automata.
      long val = (sstate & (mask << shift)) >> shift;
      assert val < Integer.MAX_VALUE : "Decoded value is way too big! (" + val
          + ")";
      return (int) val;
    }

    // #######################################################################
    // # Data Members
    private final long mask;
    private final int shift;

  }

  private EventMap mEventMap;
  private SyncProduct mSyncProduct;

  // #########################################################################
  // # Class Constants
  private static final int MAXDEPTH = 1024;

}
