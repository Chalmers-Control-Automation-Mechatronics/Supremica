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
import java.util.Collection;
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
 * <P>A monolithic conflict checker implementation purely written in
 * Java. This conflict checker uses explict state enumeration with some
 * optimisation of data structures.</P>
 *
 * @author Sam Douglas
 */

public class MonolithicConflictChecker
  extends AbstractConflictChecker
{

  //#########################################################################
  //# Constructors
  /**
   * Creates a new conflict checker without a model or marking
   * proposition.
   */
  public MonolithicConflictChecker(final ProductDESProxyFactory factory)
  {
    super(factory);
  }

  /**
   * Creates a new conflict checker to check whether the given model
   * nonconflicting with respect to the default marking proposition.
   * @param  model      The model to be checked by this conflict checker.
   * @param  factory    Factory used for trace construction.
   */
  public MonolithicConflictChecker(final ProductDESProxy model,
                                   final ProductDESProxyFactory factory)
  {
    super(model, factory);
  }

  /**
   * Creates a new conflict checker to check a particular model.
   * @param  model      The model to be checked by this conflict checker.
   * @param  marking    The proposition event that defines which states
   *                    are marked. Every state has a list of propositions
   *                    attached to it; the conflict checker considers only
   *                    those states as marked that are labelled by
   *                    <CODE>marking</CODE>, i.e., their list of
   *                    propositions must contain this event(exactly the
   *                    same object).
   * @param  factory    Factory used for trace construction.
   */
  public MonolithicConflictChecker(final ProductDESProxy model,
                                   final EventProxy marking,
                                   final ProductDESProxyFactory factory)
  {
    super(model, marking, factory);
  }


  //#########################################################################
  //# Invocation
  public boolean run()
    throws OverflowException
  {
    try {
      // First get the model
      final ProductDESProxy model = getModel();
      mSyncProduct = new SyncProduct();
      mSyncProduct.build();

      // Set the marked states coreachable and explore their predecessors.
      final int numstates = mSyncProduct.getNumberOfStates();
      final BitSet coreachable = new BitSet(numstates);
      final TIntHashSet marked = mSyncProduct.mMarkedStates;
      final TIntIterator it = marked.iterator();
      // Use recursion, but be aware of stack overflow ...
      boolean ok = true;
      while (it.hasNext()) {
        final int stateid = it.next();
        ok &= exploreBackwards(stateid, coreachable, MAXDEPTH);
      }
      while (!ok) {
        ok = true;
        for (int stateid = 0; stateid < numstates; stateid++) {
          if (coreachable.get(stateid)) {
            ok &= exploreBackwards(stateid, coreachable, MAXDEPTH);
          }
        }
      }

      if (coreachable.cardinality() == numstates) {
        return setSatisfiedResult();
      } else {
        // Generate a counter example. As each state is numbered in the
        // order it is encountered, and a breadth first exploration
        // strategy is used, and all states are reachable, following the
        // transition to a state with the lowest id wil give a counterexample.
        final List<EventProxy> countertrace = new LinkedList<EventProxy>();
        final EventMap eventmap = mSyncProduct.eventmap;
        // Find the unchecked state with the lowest
        // id, as this should give the shortest counterexample.
        int trace_start = Integer.MAX_VALUE;
        for (int v = 0; v < numstates; v++) {
          if (!coreachable.get(v)) {
            trace_start = v;
            break;
          }
        }
        // Until we reach the start state...
        final TransitionSchema trans = mSyncProduct.transitions;
        while (trace_start != 0) {
          final TransitionData td = trans.getTransitions(trace_start);
          assert td != null: "This is impossible, every state is reachable!";
          TIntHashSet incoming = td.in;
          assert incoming != null:
            "Impossible? every state should be reachable!";
          int lowtrans = select_lowest_transition(incoming, trans);
          countertrace.add(0,
                           eventmap.getEvent(trans.store.getEvent(lowtrans)));
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
      mSyncProduct = null;
    }
  }


  //#########################################################################
  //# Setting the Result
  protected void addStatistics(final VerificationResult result)
  {
    final int numaut = mSyncProduct.getNumberOfAutomata();
    final int numstates = mSyncProduct.getNumberOfStates();
    result.setNumberOfAutomata(numaut);
    result.setNumberOfStates(numstates);
    result.setPeakNumberOfNodes(numstates);
  }


  //#########################################################################
  //# Auxiliary Methods
  /**
   * Performs backwards search to find coreachable states.
   * @param  stateid     The index of a state to be marked as coreachable
   *                     and explored further.
   * @param  coreachable The bitset to contain the indexes of all
   *                     coreachable states.
   * @param  maxdepth    The maximum allowable recursion depth.
   * @return <CODE>true</CODE> if exploration was successful within the
   *         given depth, <CODE>false</CODE> otherwise.
   */
  private boolean exploreBackwards(final int stateid,
                                   final BitSet coreachable,
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
   * Check the transition set for the state with the lowest
   * id, and return the id.
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


  //#########################################################################
  //# Auxiliary Static Methods
  private static String longbits(long x)
  {
    final StringBuilder sb = new StringBuilder();
    for (int i = 0; i < 64; i++) {
      sb.append((((x >> i) & 1) == 0) ? "0" : "1");
    }
    return sb.toString();
  }

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


  //#########################################################################
  //# Inner Class SyncProduct
  private class SyncProduct
  {

    //#######################################################################
    //# Constructor
    private SyncProduct()
      throws OverflowException
    {
      final ProductDESProxy model = getModel();
      final EventProxy marking = getUsedMarkingProposition();
      // Create a mapping of events to integer IDs to use
      // when generating the synchronous product.
      eventmap = new EventMap(model);
      // Create a new state schema for the product.
      schema = new SyncStateSchema(model, eventmap, marking);
    }


    //#######################################################################
    //# Simple Access
    private int getNumberOfAutomata()
    {
      return schema.getNumberOfAutomata();
    }

    private int getNumberOfStates()
    {
      return mStateMap.size();
    }


    //#######################################################################
    //# Accessing the Fringe
    private int fringeGet()
    {
      return mFringeIndex++;
    }

    private int fringeSize()
    {
      return mNextStateIndex - mFringeIndex;
    }


    //#######################################################################
    //# Auxiliary Methods
    /**
     * Register a state in the synchronous product.  If the state
     * already exists in the state mapping it will be given the existing
     * id, otherwise a new mapping will be assigned and the id returned.
     */
    private int registerState(long state)
      throws OverflowException
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
     * Look up the state id the state list to get its
     * 64 bit encoded version.
     */
    private long getStateFromId(int id)
    {
      return statelist.get(id);
    }

    private void build()
      throws OverflowException
    {
      // Compose the initial state.
      AutomatonSchema[] automata = schema.getOrdering();
      StateProxy[] state = new StateProxy[automata.length];
      for (int i = 0; i < automata.length; i++) {
        state[i] = automata[i].getInitialState();
        assert state[i] != null : "Every automaton must have an initial state";
      }
      long init = schema.encodeState(state);
      // Register the initial state.
      int initid = registerState(init);

      // Expand all the states in the fringe, until no more
      // are added. This implies we have explored the entire
      // synchronous product.
      while (fringeSize() > 0) {
        expand();
      }
    }

    /**
     * Expands the first available state in the fringe.
     * It is invalid to call this method with an empty fringe.
     */
    private void expand()
      throws OverflowException
    {
      assert fringeSize() > 0:
        "expand should not be called on an empty fringe";

      // Get the first unvisited state in the fringe.
      final int stateid = fringeGet();
      final long state = getStateFromId(stateid);
      // Decode the state.
      final int[] dstate = schema.decodeState(state);
      final int[] rstate = new int[dstate.length];
      final AutomatonSchema[] automata = schema.getOrdering();
      assert automata.length == dstate.length;

      // Does the current state need to be marked?
      boolean need_marking = true;
      for (int i = 0; i < automata.length; i++) {
        if (!automata[i].isMarked(dstate[i])) {
          need_marking = false;
          break;
        }
      }
      if (need_marking) {
        mMarkedStates.add(stateid);
      }

      // The strategy for creating the synchronous product is to find the
      // sets of enabled transitions for each automaton and group them
      // into sets by their event.
      // We can then check only the events that are enabled, to see
      // whether each automata has the event enabled in the current
      // state, or does not list the event.
      // If this does not hold for an automata, then the event cannot be
      // enabled in the synchronous product, and the event is removed
      // from the mapping(actually, the implementation is a little
      // messier than this to try and save reallocating lots of sets; see
      // OutgoingEventThingy for more details

      // Clear existing outevent mappings.
      clearEvents();

      // Collect up possible transitions for all automata and
      // map them to events.
      for (int i = 0; i < automata.length; i++) {
        // Get outgoing transitions.
        TIntHashSet trs = automata[i].getOutgoingTransitions(dstate[i]);
        if (trs == null) {
          continue;
        }
        // Add the transitions to the map
        addTransitions(i, trs);
      }

      // Go through each event that is enabled from the
      // current combined state.
      TIntIterator it = events.iterator();
      events:
      while (it.hasNext()) {
        int ev = it.next();
        // Check that every automata either does not list the event, or
        // has the a transition on it from the current event.
        for (int i = 0; i < automata.length; i++) {
          // If the automaton does not have an outgoing transition
          // for this transition, but it does list the event, then
          // it will not be enabled in the synchronous product. Remove
          // the event from the set of possible events.
          if (!hasTransitionForEvent(i, ev) &&
              automata[i].getEvents().contains(eventmap.getEvent(ev))) {
            it.remove();
            continue events;
          }
        }

        // If we get here, then the event is present in the synchronous
        // product. Construct a transition in the synchronous
        // product. In order to do this we copy the current state, then
        // loop through the available transitions and set the appropriate
        // part of the destination state for the transition.
        TLongHashSet trans = getTransitionSet(ev);
        TLongIterator ti = trans.iterator();

        // Copy the current state into the new state.
        System.arraycopy(dstate, 0, rstate, 0, dstate.length);

        while (ti.hasNext()) {
          long val = ti.next();
          int at = (int) (val >> 32);
          int tr = (int) (val & 0xFFFFFFFF);
          int tdest = automata[at].transitions.store.getDestination(tr);
          rstate[at] = tdest;
        }

        // Hopefully we have a new state! Encode away
        long newstate = schema.encodeState(rstate);

        // We should do some kind of thorough check for whether the
        // state has already been encountered, but for now, check whether
        // it has actually been expanded. This should save fringe
        // space somewhat.
        int newid = registerState(newstate);
        transitions.addTransition
          (transitions.transitionFactory(stateid, newid, ev));
      }
    }

    /**
     * Take an encoded source and destination state, and an event and
     * create a transition. The event index will be looked up in the
     * event map.
     */
    private int transitionFactory(int source, int dest, EventProxy e)
    {
      return transitions.transitionFactory(source, dest, eventmap.getId(e));
    }

    //#########################################################################
    //# OutgoingEventThingy Methods
    private TLongHashSet getTransitionSet(int event)
    {
      TLongHashSet hs = eventsets.get(event);
      if (hs == null) {
        hs = new TLongHashSet();
        eventsets.put(event, hs);
      }
      return hs;
    }

    private TIntHashSet getEnabledSet(int event)
    {
      TIntHashSet hs = enabled.get(event);
      if (hs == null) {
        hs = new TIntHashSet();
        enabled.put(event, hs);
      }

      return hs;
    }

    private void clearEvents()
    {
      events.clear();
    }

    private boolean hasTransitionForEvent(int automaton, int event)
    {
      TIntHashSet enset = getEnabledSet(event);
      return enset.contains(automaton);
    }

    private void addTransition(int automaton, int trans)
    {
      AutomatonSchema[] automata = schema.getOrdering();
      int event = automata[automaton].transitions.store.getEvent(trans);
      long packed = ((long) automaton << 32) | trans;
      //If this event is not in the set of events already then the set
      //might need clearing. This will also create it if necessary.
      TLongHashSet trset = getTransitionSet(event);
      TIntHashSet enset = getEnabledSet(event);
      if (!events.contains(event)) {
        trset.clear();
        enset.clear();
      }
      trset.add(packed);
      enset.add(automaton);   //Cast should be safe.
      events.add(event);
    }

    private void addTransitions(int automaton, TIntHashSet trs)
    {
      TIntIterator it = trs.iterator();
      while (it.hasNext()) {
        addTransition(automaton, it.next());
      }
    }

    //#########################################################################
    //# Data Members
    private final SyncStateSchema schema;
    private final EventMap eventmap;

    private final TransitionSchema transitions =
      new TransitionSchema(false, true);

    private final TIntHashSet mMarkedStates = new TIntHashSet();

    /**
     * A map of synchronous product states to integer
     * ids, to allow the states to be more efficiently
     * encoded.
     */
    private TLongIntHashMap mStateMap = new TLongIntHashMap();
    private TLongArrayList statelist = new TLongArrayList();

    /**
     * The index of the first open state of the fringe.
     */
    private int mFringeIndex = 0;
    /**
     * The index of the next state to be created.
     */
    private int mNextStateIndex = 0;

    /**
     * This is used when expanding states. (OutgoingEventThingy)
     */
    private TIntHashSet events = new TIntHashSet();
    private TIntObjectHashMap<TLongHashSet> eventsets =
      new TIntObjectHashMap<TLongHashSet>();
    private TIntObjectHashMap<TIntHashSet> enabled =
      new TIntObjectHashMap<TIntHashSet>();

  }


  //#########################################################################
  //# Inner Class EventMap
  /**
   * A mapping of EventProxy objects to integer
   * identifiers. These identifiers are used when
   * building the synchronous product.
   */
  private static class EventMap
  {

    //#######################################################################
    //# Constructor
    EventMap(final ProductDESProxy model)
    {
      Set<EventProxy> eventset = model.getEvents();
      //Create an array of the events (arbitrarily)
      events = eventset.toArray (new EventProxy[]{});
      eventmap = new TObjectIntHashMap<EventProxy>();
      for (int i = 0; i < events.length; i++) {
        eventmap.put(events[i], i);
      }
    }

    //#######################################################################
    //# Simple Access
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

    public int size()
    {
      return events.length;
    }

    //#######################################################################
    //# Data Members
    private final EventProxy[] events;
    private final TObjectIntHashMap<EventProxy> eventmap;
  }


  //#########################################################################
  //# Inner Class SyncStateSchema
  /**
   * Represents a 'schema' for interpreting a state
   * encoding for the synchronous composition of automata.
   *
   * Automata will be assigned an ordering when the schema is created,
   * and this is assumed when encoding states.
   */
  private static class SyncStateSchema
  {

    //#######################################################################
    //# Constructor
    /**
     * Construct a state schema given the automata
     * to be used in the synchronous product.
     */
    private SyncStateSchema(final ProductDESProxy model,
                            final EventMap eventmap,
                            final EventProxy marking)
      throws OverflowException
    {
      mEventMap = eventmap;
      // Build the schema for each automata. This gives
      // a fixed ordering for automata
      final Set<AutomatonProxy> automata = model.getAutomata();
      final int numaut = automata.size();
      final List<AutomatonSchema> list =
        new ArrayList<AutomatonSchema>(numaut);
      for (final AutomatonProxy aut : automata) {
        switch (aut.getKind()) {
        case PLANT:
        case SPEC:
          final AutomatonSchema schema =
            new AutomatonSchema(aut, eventmap, marking);
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
        mEncondings[i] = new StateEncodingData (bits, shamt);
        // Add the number of bits we encoded to the shift amount
        shamt += bits;
      }
      if (shamt > 64) {
        final String msg = "state encoding requires " + shamt +
          " bits, 64 is the maximum!";
        throw new OverflowException(msg);
      }
    }

    //#######################################################################
    //# Simple Access
    AutomatonSchema[] getOrdering()
    {
      return mAutomata;
    }

    int getNumberOfAutomata()
    {
      return mAutomata.length;
    }

    /**
     * A convenience method that encodes the state from
     * an array of state proxy objects by looking up the
     * state indexes in the automata schema.
     */
    long encodeState(StateProxy[] states)
    {
      assert states.length == mAutomata.length: "Wrong number of states given";
      int[] istates = new int[states.length];
      for (int i = 0; i < states.length; i++) {
        istates[i] = mAutomata[i].getStateNumber(states[i]);
      }
      return encodeState(istates);
    }

    /**
     * Encode a synchronous product state from an array
     * of state indexes. The ordering of the states
     * should match the ordering of automata given
     * by the getOrdering method.
     */
    long encodeState(int[] states)
    {
      assert states.length == mAutomata.length: "Wrong number of states given";
      //The to-be-encoded state in the synchronous product.
      long sstate = 0;
      for (int i = 0; i < states.length; i++) {
        sstate = mEncondings[i].encode (sstate, states[i]);
      }
      return sstate;
    }

    /**
     * Decode a state from the synchronous product into
     * its states in individual automata. The states are
     * given as integers.
     */
    int[] decodeState (long state)
    {
      int[] sarr = new int[mEncondings.length];
      for (int i = 0; i < mEncondings.length; i++) {
        sarr[i] = mEncondings[i].decode (state);
      }
      return sarr;
    }


    //#######################################################################
    //# Data Members
    private final AutomatonSchema[] mAutomata;
    /**
     * State encoding data for the synchronous product.
     */
    private final StateEncodingData[] mEncondings;
    /**
     * Mapping of events to integer IDs.
     */
    private final EventMap mEventMap;

  }


  //#########################################################################
  //# Inner Class AutomatonSchema
  /**
   * Represents an automaton. This class exists to help give
   * an ordering/enumeration to states and transitions in
   * an automata.
   */
  private static class AutomatonSchema
  {

    //#######################################################################
    //# Constructor
    AutomatonSchema(final AutomatonProxy automaton,
                    final EventMap eventmap,
                    final EventProxy marking)
    {
      mAutomaton = automaton;
      mEventMap = eventmap;
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
      int i = 0;
      StateProxy initial = null;
      for (final StateProxy state : states) {
        if (state.isInitial()) {
          initial = state;
        }
        //If the state contains the marking proposition,
        //add it to the set of marked states.
        if (mMarkedStates != null &&
            state.getPropositions().contains(marking)) {
          mMarkedStates.add(i);
        }
        mStates[i] = state;
        mStateMap.put(state, i);
        i++;
      }
      mInitialState = initial;
      transitions = new TransitionSchema(true, false);
      for (TransitionProxy t : automaton.getTransitions()) {
        int src = getStateNumber (t.getSource());
        int dest = getStateNumber (t.getTarget());
        int event = eventmap.getId (t.getEvent());
        int tt = transitions.transitionFactory (src,dest,event);
        transitions.addTransition (tt);
      }
    }

    //#######################################################################
    //# Simple Access
    /**
     * Get a state, given its number in the state
     * enumeration.
     */
    StateProxy getNumberedState(int state)
    {
      assert state >= 0 && state < mStates.length: "State out of range";
      return mStates[state];
    }

    /**
     * Get the state number, given a state. It is assumed
     * that the state belongs to the automaton, but if it
     * is not in the state mapping, this will return a
     * negative number.
     */
    int getStateNumber(StateProxy state)
    {
      if (!mStateMap.containsKey (state)) {
        return -1;
      }
      return mStateMap.get (state);
    }

    /**
     * Return the number of states in the automata.
     */
    int stateSize()
    {
      return mStates.length;
    }

    /**
     * Return the event set for the automaton.
     */
    Set<EventProxy> getEvents()
    {
      return mAutomaton.getEvents();
    }

    StateProxy getInitialState()
    {
      return mInitialState;
    }

    /**
     * A predicate to check whether an event is enabled
     * from a given state.
     */
    boolean isEventEnabled(int state, int e)
    {
      //If the automaton does not list the event, then
      //it is enabled in this state (implicit self-loop)
      if (!getEvents().contains(mEventMap.getEvent(e))) {
        return true;
      }
      TIntHashSet outgoing = transitions.getTransitions(state).out;

      //No outgoing states! The event isn't enabled.
      if (outgoing == null) {
        return false;
      }
      TIntIterator it = outgoing.iterator();

      while (it.hasNext()) {
        int trans = it.next();

        if (transitions.store.getEvent(trans) == e) {
          return true;
        }
      }

      return false;
    }

    TIntHashSet getOutgoingTransitions(int state)
    {
      TransitionData td = transitions.getTransitions(state);
      if (td == null) {
        return null;
      }
      return td.out;
    }

    boolean isMarked(int state)
    {
      if (mMarkedStates == null) {
        return true;
      } else {
        return mMarkedStates.contains(state);
      }
    }


    //#######################################################################
    //# Data Members
    /**
     * The automaton object for this schema.
     */
    private final AutomatonProxy mAutomaton;
    /**
     * An array of states in the automata, with a
     * fixed ordering.
     */
    private final StateProxy[] mStates;
    /**
     * Gives a mapping of StateProxy object instances to
     * their index in the state array.
     */
    private final TObjectIntHashMap<StateProxy> mStateMap =
      new TObjectIntHashMap<StateProxy>();
    private final TIntHashSet mMarkedStates;
    private final TransitionSchema transitions;
    /**
     * The initial state of the automaton.
     */
    private final StateProxy mInitialState;
    private final EventMap mEventMap;

  }


  //#########################################################################
  //# Inner Class TransitionSchema
  private static class TransitionSchema
  {
    //#######################################################################
    //# Constructor
    TransitionSchema(boolean forward, boolean reverse)
    {
      transitions = new TLongObjectHashMap<TransitionData>();
      storeforward = forward;
      storereverse = reverse;
    }

    //#######################################################################
    //# Simple Access
    int transitionFactory(int src, int dest, int e)
    {
      return store.allocate(src, dest, e);
    }

    void addTransition(int t)
    {
      //Add the forward transition. Get the transition data
      //for the source state and add the transition to that.
      TransitionData td;
      if (storeforward) {
        td = makeTransitionData(store.getSource(t));
        td.addOutTransition(t);
      }
      //Now do the same thing for the destination state, but
      //add it as an incoming transition.
      if (storereverse) {
        td = makeTransitionData(store.getDestination(t));
        td.addInTransition(t);
      }
    }

    TransitionData getTransitions(final int state)
    {
      return transitions.get(state);
    }

    //#######################################################################
    //# Auxiliary Methods
    /**
     * Get, or make transition data. This will get an
     * existing transition data object for the given
     * state, or it will create a new one and add it.
     */
    private TransitionData makeTransitionData(int state)
    {
      //Add the forwards transition data.
      TransitionData td = transitions.get(state);
      if (td == null) {
        td = new TransitionData();
        transitions.put(state, td);
      }
      return td;
    }

    //#######################################################################
    //# Data Members
    private TransitionStore store = new TransitionStore();
    private TLongObjectHashMap<TransitionData> transitions;

    private final boolean storeforward;
    private final boolean storereverse;

  }


  //#########################################################################
  //# Inner Class TransitionData
  private static class TransitionData
  {
    //#######################################################################
    //# Simple Access
    void addOutTransition(int t)
    {
      if (out == null) {
        out = new TIntHashSet();
      }
      out.add (t);
    }

    void addInTransition(int t)
    {
      if (in == null) {
        in = new TIntHashSet();
      }
      in.add (t);
    }

    //#######################################################################
    //# Data Members
    private TIntHashSet in = null;
    private TIntHashSet out = null;
  }


  //#########################################################################
  //# Inner Class Transition
  /**
   * A transition in the synchronous product.
   * the source and destination are stored as encoded
   * states in the automaton, and the event is stored
   * as an integer, the index of the event in the
   * events array.
   */
  private static class Transition implements Comparable<Transition>
  {
    //#######################################################################
    //# Data Members
    Transition (long src, long dest, int e)
    {
      source = src;
      destination = dest;
      event = e;
    }

    //#######################################################################
    //# Overrides for java.lang.Object
    public boolean equals (Object o)
    {
      if (o == null) return false;
      if (!(o instanceof Transition)) return false;

      Transition t = (Transition)o;
      return t.source == source &&
        t.destination == destination &&
        t.event == event;
    }

    public int hashCode()
    {
      //This is a crap hashcode, only implemented to
      //satisfy the equality contract.
      return (int) (source + destination + event);
    }

    public int compareTo(Transition t)
    {
      //What a horrible control flow hack, to avoid
      //repeating code to make sure the result is not
      //borked with a cast to integer. This could
      //arise if two states had a difference that could
      //not fit into a 32 bit integer.
      long x = t.source - source;
      do {
        if (x != 0)
          break;

        x = t.destination - destination;
        if (x != 0)
          break;

        x = t.event - event;
      } while (false);

      if (x > 0)
        return 1;
      if (x < 0)
        return -1;
      return 0;
    }

    //#######################################################################
    //# Data Members
    private final long source;
    private final long destination;
    private final int event;

  }

  //#########################################################################
  //# Inner Class TransitionStore
  /**
   * An attempt to efficiently allocate memory for
   * transitions in an automaton. This works by
   * allocating into arrays. There is very little
   * overhead as all transitions have the same lifetime
   * and so deallocating is not an issue.
   *
   * Unlike an array list, this class uses an array
   * of arrays, and simply allocates a new (possibly larger)
   * array when the existing one is full.
   *
   * The transition is returned as a 32 bit integer which
   * encodes an address into this store. The 8 lowest bits
   * indicate which 'chunk' the data is in, the rest indicate
   * the location in the current chunk.
   *
   * Each transition is stored as 3 integers, and chunk sizes
   * will always be a multiple of 3.
   */
  private static class TransitionStore
  {

    //#######################################################################
    //# Constructor
    public TransitionStore()
    {
      chunkptr = 0;
      pointer = 0;
      data[0] = allocate_chunk (chunk_size(0));
    }

    //#######################################################################
    //# Simple Access
    /**
     * Allocate a transition and set its source, destination
     * and event values.
     */
    public int allocate(int source, int destination, int event)
    {
      //Is the current chunk full? If so, allocate a new
      //one first and reset the allocation pointer.
      if (pointer >= data[chunkptr].length)
        {
          chunkptr++;
          data[chunkptr] = allocate_chunk (chunk_size (chunkptr));
          pointer = 0;
        }

      //Store and increment the allocation pointer.
      int t = pointer;
      pointer += TRANSITION_SIZE;
      transition_count++;

      //Mask the event to be 20 bits, and source/dest to be 22 bits
      event &= 0xFFFFF;
      source &= 0x3FFFFF;
      destination &= 0x3FFFFF;


      //Store the transition data.
      data[chunkptr][t] = source;
      data[chunkptr][t+1] = destination;
      data[chunkptr][t] |= (event << 22);
      data[chunkptr][t+1] |= ((event >> 10) << 22);

      //Now encode the address.
      return (t << 8) | (chunkptr & 255);
    }

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
      //Urgh. Decoding. Ugly.
      int val = (data[trans & 255][(trans >> 8)]) >> 22;
      return val | (((data[trans & 255][(trans >> 8)+1]) >> 22) << 10);
    }

    //#######################################################################
    //# Auxiliary Methods
    private int[] allocate_chunk(int size)
    {
      //System.err.format ("Transitions: Allocating chunk of size %d\n", size);
      return new int [size];
    }

    private int chunk_size(int chunknum)
    {
      //We don't want to keep doubling the size forever!
      //Once we get to chunk 11 [~= 2 million transitions/24MB]
      //just keep allocating in a linear fashion. This should
      //be sufficient.
      if (chunknum > 11)
        chunknum = 11;

      return (1024 * TRANSITION_SIZE) << chunknum;
    }

    //#######################################################################
    //# Data Members
    static final int TRANSITION_SIZE = 2;

    private int[][] data = new int[256][];

    private int chunkptr;
    private int pointer;
    private int transition_count = 0;

  }


  //#########################################################################
  //# Inner Class StateEncodingData
  /**
   * Data for encoding an automaton's state in the synchronous
   * product.  Bits is the number of bits needed to represent the
   * state, and shift is the amount required to shift the value into
   * position.
   *
   * |--------------------0011--------|
   *                      |4 |          bits
   *                         |-8------| shift
   */
  private static class StateEncodingData
  {

    //#######################################################################
    //# Constructor
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

    //#######################################################################
    //# Simple Access
    /**
     * Encode the state for the current automata (s)
     * into the synchronous product state sstate.
     */
    long encode (long sstate, int s)
    {
      //Shifted mask. This is used to clear the relevant bits
      long shmask = mask << shift;
      //Mask the value to be the correct size.
      long value = (s & mask) << shift;
      //Combine.
      return (sstate & ~shmask) | value;
    }

    int decode (long sstate)
    {
      //This cast should be fine, there will never be
      //more than 32bits of states in an automata.
      long val = (sstate & (mask << shift)) >> shift;
      assert val < Integer.MAX_VALUE:
        "Decoded value is way too big! (" + val + ")";
      return (int) val;
    }

    //#######################################################################
    //# Data Members
    private final long mask;
    private final int shift;

  }


  //#########################################################################
  //# Data Members
  private boolean result = false;
  private SyncProduct mSyncProduct;


  //#########################################################################
  //# Class Constants
  private static final int MAXDEPTH = 1024;

}
