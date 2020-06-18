//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# Copyright (C) 2004-2020 Robi Malik
//###########################################################################
//# This file is part of Waters.
//# Waters is free software: you can redistribute it and/or modify it under
//# the terms of the GNU General Public License as published by the Free
//# Software Foundation, either version 2 of the License, or (at your option)
//# any later version.
//# Waters is distributed in the hope that it will be useful, but WITHOUT ANY
//# WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
//# FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
//# details.
//# You should have received a copy of the GNU General Public License along
//# with Waters. If not, see <http://www.gnu.org/licenses/>.
//#
//# Linking Waters statically or dynamically with other modules is making a
//# combined work based on Waters. Thus, the terms and conditions of the GNU
//# General Public License cover the whole combination.
//# In addition, as a special exception, the copyright holders of Waters give
//# you permission to combine Waters with code included in the standard
//# release of Supremica under the Supremica Software License Agreement (or
//# modified versions of such code, with unchanged license). You may copy and
//# distribute such a system following the terms of the GNU GPL for Waters and
//# the licenses of the other code concerned.
//# Note that people who make modified versions of Waters are not obligated to
//# grant this special exception for their modified versions; it is their
//# choice whether to do so. The GNU General Public License gives permission
//# to release a modified version without this exception; this exception also
//# makes it possible to release a modified version which carries forward this
//# exception.
//###########################################################################

package net.sourceforge.waters.analysis.monolithic;

import gnu.trove.list.array.TIntArrayList;
import gnu.trove.list.array.TLongArrayList;
import gnu.trove.map.hash.TLongIntHashMap;
import gnu.trove.map.hash.TObjectIntHashMap;
import gnu.trove.set.hash.TIntHashSet;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.sourceforge.waters.analysis.options.Option;
import net.sourceforge.waters.analysis.options.OptionPage;
import net.sourceforge.waters.analysis.options.PositiveIntOption;
import net.sourceforge.waters.model.analysis.AnalysisAbortException;
import net.sourceforge.waters.model.analysis.AnalysisException;
import net.sourceforge.waters.model.analysis.OverflowException;
import net.sourceforge.waters.model.analysis.VerificationResult;
import net.sourceforge.waters.model.analysis.des.AbstractConflictChecker;
import net.sourceforge.waters.model.analysis.des.AbstractModelAnalyzerFactory;
import net.sourceforge.waters.model.analysis.kindtranslator.KindTranslator;
import net.sourceforge.waters.model.base.ComponentKind;
import net.sourceforge.waters.model.base.EventKind;
import net.sourceforge.waters.model.des.AutomatonProxy;
import net.sourceforge.waters.model.des.AutomatonTools;
import net.sourceforge.waters.model.des.ConflictCounterExampleProxy;
import net.sourceforge.waters.model.des.ConflictKind;
import net.sourceforge.waters.model.des.EventProxy;
import net.sourceforge.waters.model.des.ProductDESProxy;
import net.sourceforge.waters.model.des.ProductDESProxyFactory;
import net.sourceforge.waters.model.des.StateProxy;
import net.sourceforge.waters.model.des.TraceProxy;
import net.sourceforge.waters.model.des.TraceStepProxy;
import net.sourceforge.waters.model.des.TransitionProxy;


/**
 * <P>
 * A monolithic conflict checker implementation purely written in Java. This
 * conflict checker uses explicit state enumeration with some optimisation of
 * data structures.
 * </P>
 *
 * @author Sam Douglas
 */

public class MonolithicConflictChecker extends AbstractConflictChecker
{

  //#########################################################################
  //# Constructors
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


  //#########################################################################
  //# Interface net.sourceforge.waters.model.analysis.ModelAnalyzer
  @Override
  public List<Option<?>> getOptions(final OptionPage db)
  {
    final List<Option<?>> options = super.getOptions(db);
    db.append(options, AbstractModelAnalyzerFactory.OPTION_ModelAnalyzer_FinalStateLimit);
    db.append(options, AbstractModelAnalyzerFactory.OPTION_ModelAnalyzer_FinalTransitionLimit);
    return options;
  }

  @Override
  public void setOption(final Option<?> option)
  {
    if (option.hasID(AbstractModelAnalyzerFactory.OPTION_ModelAnalyzer_FinalStateLimit)) {
      final PositiveIntOption intOption = (PositiveIntOption) option;
      setNodeLimit(intOption.getIntValue());
    } else if (option.hasID(AbstractModelAnalyzerFactory.OPTION_ModelAnalyzer_FinalTransitionLimit)) {
      final PositiveIntOption intOption = (PositiveIntOption) option;
      setTransitionLimit(intOption.getIntValue());
    } else {
      super.setOption(option);
    }
  }


  //#########################################################################
  //# Invocation
  @Override
  public boolean run() throws AnalysisException
  {
    try {
      setUp();

      final ProductDESProxy model = getModel();
      mEventMap = new EventMap(model);
      mSyncProduct = new SyncProduct();
      int firstBlockingState = mSyncProduct.build();

      // Set the marked states coreachable and explore their predecessors.
      final KindTranslator translator = getKindTranslator();
      final int numstates = mSyncProduct.getNumberOfStates();
      final BitSet coreachable = new BitSet(numstates);
      final EventProxy marking = setUpUsedDefaultMarking();
      final EventProxy preconditionMarking = getConfiguredPreconditionMarking();
      final SyncStateSchema stateSchema =
          new SyncStateSchema(model, translator, mEventMap, marking,
                              preconditionMarking);
      final AutomatonSchema[] automata = stateSchema.getOrdering();
      if (firstBlockingState >= 0) {
        final ConflictCounterExampleProxy counterexample =
          buildCounterExample(firstBlockingState, ConflictKind.DEADLOCK,
                              model, stateSchema, automata);
        return setFailedResult(counterexample);
      }

      final int numaut = automata.length;
      final int[] dstate = new int[numaut];
      boolean ok = true;
      for (int stateid = 0; stateid < numstates; stateid++) {
        final long state = mSyncProduct.getStateFromId(stateid);
        // Decode the state.
        stateSchema.decodeState(state, dstate);
        if (stateSchema.isMarked(dstate)) {
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
      if (preconditionMarking != null) {
        for (int stateid = 0; stateid < numstates; stateid++) {
          final long state = mSyncProduct.getStateFromId(stateid);
          // Decode the state.
          stateSchema.decodeState(state, dstate);
          // Check if the state has a precondition marking.
          boolean premarked = true;
          for (int i = 0; i < automata.length; i++) {
            if (!automata[i].isPreconditionMarked(dstate[i])) {
              premarked = false;
              break;
            }
          }
          // If the state had a precondition marking,
          // check if it is coreachable.
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
        // All states must be coreachable if there is only one marking type.
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
        final ConflictKind kind = preconditionMarking == null ?
                            ConflictKind.LIVELOCK : ConflictKind.CONFLICT;
        final ConflictCounterExampleProxy counterexample =
          buildCounterExample(firstBlockingState, kind,
                              model, stateSchema, automata);
        return setFailedResult(counterexample);
      }

    } catch (final AnalysisException exception) {
      throw setExceptionResult(exception);
    } catch (final OutOfMemoryError error) {
      System.gc();
      final OverflowException overflow = new OverflowException(error);
      throw setExceptionResult(overflow);
    } finally {
      tearDown();
    }
  }


  //#########################################################################
  //# Interface net.sourceforge.waters.model.analysis.ModelAnalyser
  @Override
  public boolean supportsNondeterminism()
  {
    return true;
  }


  //#########################################################################
  //# Overrides for net.sourceforge.waters.model.analysis.AbstractModelVerifier
  @Override
  protected void tearDown()
  {
    super.tearDown();
    mEventMap = null;
    mSyncProduct = null;
  }

  @Override
  protected void addStatistics()
  {
    super.addStatistics();
    final VerificationResult result = getAnalysisResult();
    if (mSyncProduct != null) {
      final int numaut = mSyncProduct.getNumberOfAutomata();
      final int numstates = mSyncProduct.getNumberOfStates();
      final int numtrans = mSyncProduct.getNumberOfTransitions();
      result.setNumberOfAutomata(numaut);
      result.setNumberOfStates(numstates);
      result.setNumberOfTransitions(numtrans);
      result.setPeakNumberOfNodes(numstates);
    }
  }


  //#########################################################################
  //# Auxiliary Methods
  private ConflictCounterExampleProxy buildCounterExample
    (final int firstBlockingState,
     final ConflictKind kind,
     final ProductDESProxy model,
     final SyncStateSchema stateSchema,
     final AutomatonSchema[] automata)
  {
    // Generate a counter example. As each state is numbered in the
    // order it is encountered, and a breadth first exploration
    // strategy is used, and all states are reachable, following the
    // transition to a state with the lowest id will give a
    // counterexample.
    final List<TraceStepProxy> steps = new LinkedList<TraceStepProxy>();
    final ProductDESProxyFactory factory = getFactory();
    final int numaut = automata.length;
    final Map<AutomatonProxy,StateProxy> stateMap =
      new HashMap<AutomatonProxy,StateProxy>(numaut);
    final int numInit = mSyncProduct.getNumberOfInitialStates();
    // Find the unchecked state with the lowest
    // id, as this should give the shortest counterexample.
    // or if a second marking condition is simultaneously used, look
    // for the first non-coreachable precondition marked state.
    int current = firstBlockingState;
    // Until we reach the start state...
    do {
      final int[] tuple = new int[numaut];
      final long packed = mSyncProduct.getStateFromId(current);
      stateSchema.decodeState(packed, tuple);
      for (int a = 0; a < automata.length; a++) {
        final AutomatonSchema schema = automata[a];
        final AutomatonProxy aut = schema.getAutomatonProxy();
        final int s = tuple[a];
        final StateProxy state = schema.getStateProxyFromID(s);
        stateMap.put(aut, state);
      }
      final TraceStepProxy step;
      if (current >= numInit) {
        final TIntArrayList preds = mSyncProduct.getPredecessors(current);
        final int pred = preds.get(0);
        final EventProxy event = mSyncProduct.findEvent(pred, current);
        step = factory.createTraceStepProxy(event, stateMap);
        stateMap.clear();
        current = pred;
      } else {
        step = factory.createTraceStepProxy(null, stateMap);
        current = -1;
      }
      steps.add(0, step);
    } while (current >= 0);
    final String traceName = getTraceName();
    final TraceProxy trace = factory.createTraceProxy(steps);
    return
      factory.createConflictCounterExampleProxy(traceName, null, null, model,
                                                model.getAutomata(), trace,
                                                kind);
  }

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
    throws AnalysisAbortException, OverflowException
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


  //#########################################################################
  //# Inner Class SyncProduct
  private class SyncProduct
  {

    //#######################################################################
    //# Constructor
    private SyncProduct() throws AnalysisException
    {
      final ProductDESProxy model = getModel();
      final KindTranslator translator = getKindTranslator();
      final EventProxy marking = setUpUsedDefaultMarking();
      final EventProxy preconditionMarking = getConfiguredPreconditionMarking();
      // Create a new state schema for the product.
      mStateSchema =
          new SyncStateSchema(model, translator, mEventMap, marking,
                              preconditionMarking);
      mDeadlockDetectionEnabled = preconditionMarking == null;
      final int numaut = getNumberOfAutomata();
      mSourceBuffer = new int[numaut];
      mTargetBuffer = new int[numaut];
      mNondeterministicAutomata = new int[numaut];
    }

    //#######################################################################
    //# Simple Access
    private int getNumberOfAutomata()
    {
      return mStateSchema.getNumberOfAutomata();
    }

    private int getNumberOfStates()
    {
      return mStateMap.size();
    }

    private int getNumberOfTransitions()
    {
      return mNumberOfTransitions;
    }

    private int getNumberOfInitialStates()
    {
      return mNumberOfInitialStates;
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

    private TIntArrayList getPredecessors(final int stateid)
    {
      return mPredecessors.get(stateid);
    }

    //#######################################################################
    //# Auxiliary Methods
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
            mNumberOfTransitions++;
          }
        }
        return oldid;
      } else {
        final int newid = mNextStateIndex++;
        if (mNextStateIndex > getNodeLimit()) {
          throw new OverflowException(getNodeLimit());
        }
        mStateMap.put(state, newid);
        mStateList.add(state);
        final TIntArrayList preds = new TIntArrayList();
        if (pred >= 0) {
          preds.add(pred);
          mNumberOfTransitions++;
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
      return mStateList.get(id);
    }

    private int build() throws AnalysisException
    {
      // Compose the initial states.
      final AutomatonSchema[] automata = mStateSchema.getOrdering();
      final int[] tuple = new int[automata.length];
      collectInitialStates(0, tuple);
      mNumberOfInitialStates = mNextStateIndex;
      // Expand all the states in the fringe, until no more
      // are added. This implies we have explored the entire
      // synchronous product.
      while (fringeSize() > 0) {
        final int deadlockState = expand();
        if (deadlockState >= 0) {
          return deadlockState;
        }
        checkAbort();
      }
      return -1;
    }

    private void collectInitialStates(final int a, final int[] tuple)
      throws OverflowException
    {
      if (a == tuple.length) {
        final long init = mStateSchema.encodeState(tuple);
        addNewState(init, -1);
      } else {
        final AutomatonSchema[] automata = mStateSchema.getOrdering();
        final AutomatonSchema schema = automata[a];
        final int[] initials = schema.getInitialStates();
        for (final int s : initials) {
          tuple[a] = s;
          collectInitialStates(a + 1, tuple);
        }
      }
    }

    /**
     * Expands the first available state in the fringe. It is invalid to call
     * this method with an empty fringe.
     */
    private int expand() throws OverflowException
    {
      assert fringeSize() > 0 : "expand should not be called on an empty fringe";

      // Get the first unvisited state in the fringe.
      final int stateid = fringeGet();
      final long state = getStateFromId(stateid);
      // Decode the state.
      mStateSchema.decodeState(state, mSourceBuffer);
      final AutomatonSchema[] automata = mStateSchema.getOrdering();

      // Explore transitions ...
      boolean deadlock = mDeadlockDetectionEnabled;
      nextevent:
      for (int eventid = 0; eventid < mEventMap.size(); eventid++) {
        boolean selfloop = true;
        int i = -1;
        for (int autid = 0; autid < automata.length; autid++) {
          final AutomatonSchema schema = automata[autid];
          final int src = mSourceBuffer[autid];
          final int[] targets = schema.getSuccessorStates(src, eventid);
          if (targets == null) {
            continue nextevent;
          } else if (targets.length == 1) {
            final int target = targets[0];
            mTargetBuffer[autid] = target;
            selfloop &= src == target;
          } else {
            i++;
            mNondeterministicAutomata[i] = autid;
            selfloop = false;
          }
        }
        deadlock &= selfloop;
        expandNondeterministic(automata, eventid, stateid, i);
      }
      deadlock &= !mStateSchema.isMarked(mSourceBuffer);
      return deadlock ? stateid : -1;
    }

    private void expandNondeterministic(final AutomatonSchema[] automata,
                                        final int eventID, final int sourceID,
                                        final int i) throws OverflowException
    {
      if (i >= 0) {
        final int autID = mNondeterministicAutomata[i];
        final AutomatonSchema schema = automata[autID];
        final int[][][] table = schema.mTransitionTable;
        final int[][] eventsSources = table[eventID];
        final int[] targets = eventsSources[mSourceBuffer[autID]];
        for (final int foundTarget : targets) {
          mTargetBuffer[autID] = foundTarget;
          expandNondeterministic(automata, eventID, sourceID, i - 1);
        }
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
          final int[] targets = schema.getSuccessorStates(source, eventid);
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

    //#######################################################################
    //# Data Members
    private final SyncStateSchema mStateSchema;

    /**
     * Whether synchronous product building should be aborted on encountering
     * a deadlock state. Disabled for generalised nonblocking.
     */
    private final boolean mDeadlockDetectionEnabled;

    private final List<TIntArrayList> mPredecessors =
        new BlockedArrayList<TIntArrayList>(TIntArrayList.class);

    /**
     * The number of initial states.
     */
    private int mNumberOfInitialStates;

    /**
     * A map of synchronous product states to integer IDs, to allow the states
     * to be more efficiently encoded.
     */
    private final TLongIntHashMap mStateMap = new TLongIntHashMap();
    private final TLongArrayList mStateList = new TLongArrayList();

    /**
     * The index of the first open state of the fringe.
     */
    private int mFringeIndex = 0;
    /**
     * The index of the next state to be created.
     */
    private int mNextStateIndex = 0;
    /**
     * The total number of transitions stored.
     */
    private int mNumberOfTransitions = 0;

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
  private static class EventMap
  {

    //#######################################################################
    //# Constructor
    private EventMap(final ProductDESProxy model)
    {
      final Set<EventProxy> eventset = model.getEvents();
      // Create an array of the events (arbitrarily)
      mEvents = eventset.toArray(new EventProxy[] {});
      mEventMap = new TObjectIntHashMap<EventProxy>();
      for (int i = 0; i < mEvents.length; i++) {
        mEventMap.put(mEvents[i], i);
      }
    }

    //#######################################################################
    //# Simple Access
    private int size()
    {
      return mEvents.length;
    }

    private EventProxy getEvent(final int id)
    {
      return mEvents[id];
    }

    private int getId(final EventProxy e)
    {
      assert mEventMap.containsKey(e);
      return mEventMap.get(e);
    }

    //#######################################################################
    //# Data Members
    private final EventProxy[] mEvents;
    private final TObjectIntHashMap<EventProxy> mEventMap;
  }


  //#########################################################################
  //# Inner Class SyncStateSchema
  /**
   * Represents a 'schema' for interpreting a state encoding for the synchronous
   * composition of automata.
   *
   * Automata will be assigned an ordering when the schema is created, and this
   * is assumed when encoding states.
   */
  private static class SyncStateSchema
  {

    //#######################################################################
    //# Constructor
    /**
     * Construct a state schema given the automata to be used in the synchronous
     * product.
     */
    private SyncStateSchema(final ProductDESProxy model,
                            final KindTranslator translator,
                            final EventMap eventmap, final EventProxy marking,
                            final EventProxy preconditionMarking)
        throws OverflowException
    {
      // Build the schema for each automata. This gives
      // a fixed ordering for automata
      final Set<AutomatonProxy> automata = model.getAutomata();
      final int numaut = automata.size();
      final List<AutomatonSchema> list =
        new ArrayList<AutomatonSchema>(numaut);
      for (final AutomatonProxy aut : automata) {
        final ComponentKind kind = translator.getComponentKind(aut);
        if (kind != null) {
          switch (kind) {
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
        final int numStates = mAutomata[i].stateSize();
        final int bits = AutomatonTools.log2(numStates);
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

    //#######################################################################
    //# Simple Access
    private AutomatonSchema[] getOrdering()
    {
      return mAutomata;
    }

    private int getNumberOfAutomata()
    {
      return mAutomata.length;
    }

    /**
     * Encodes a synchronous product state from an array of state indexes. The
     * ordering of the states should match the ordering of automata given by the
     * getOrdering method.
     */
    private long encodeState(final int[] states)
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
     * Decodes a state from the synchronous product into its states in
     * individual automata. The states are given as integers.
     */
    private void decodeState(final long state, final int[] tuple)
    {
      for (int i = 0; i < mEncondings.length; i++) {
        tuple[i] = mEncondings[i].decode(state);
      }
    }

    private boolean isMarked(final int[] tuple)
    {
      for (int a = 0; a < mAutomata.length; a++) {
        if (!mAutomata[a].isMarked(tuple[a])) {
          return false;
        }
      }
      return true;
    }

    //#######################################################################
    //# Data Members
    private final AutomatonSchema[] mAutomata;
    /**
     * State encoding data for the synchronous product.
     */
    private final StateEncodingData[] mEncondings;

  }


  //#########################################################################
  //# Inner Class AutomatonSchema
  /**
   * Represents an automaton. This class exists to help give an
   * ordering/enumeration to states and transitions in an automaton.
   */
  private static class AutomatonSchema
  {

    //#######################################################################
    //# Constructor
    @SuppressWarnings("unchecked")
    private AutomatonSchema(final AutomatonProxy automaton,
                            final EventMap eventmap, final EventProxy marking,
                            final EventProxy preMarking)
    {
      mAutomaton = automaton;
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
      final TIntArrayList initials = new TIntArrayList();
      for (final StateProxy state : states) {
        if (state.isInitial()) {
          initials.add(i);
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
      mInitialStates = initials.toArray();

      final int numevents = eventmap.size();
      mTransitionTable = new int[numevents][][];
      final Map<Integer,List<Integer>>[] tempTransitionTable =
          new HashMap[numevents];
      for (final EventProxy event : automaton.getEvents()) {
        if (event.getKind() != EventKind.PROPOSITION) {
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
    private int getStateNumber(final StateProxy state)
    {
      return mStateMap.get(state);
    }

    private StateProxy getStateProxyFromID(final int id)
    {
      return mStates[id];
    }

    private AutomatonProxy getAutomatonProxy()
    {
      return mAutomaton;
    }

    /**
     * Returns the number of states in the automaton.
     */
    private int stateSize()
    {
      return mStates.length;
    }

    private int[] getInitialStates()
    {
      return mInitialStates;
    }

    /**
     * Returns the successor states for the given source state and event, or
     * null if the event is not enabled.
     */
    private int[] getSuccessorStates(final int src, final int event)
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

    private boolean isMarked(final int state)
    {
      if (mMarkedStates == null) {
        return true;
      } else {
        return mMarkedStates.contains(state);
      }
    }

    private boolean isPreconditionMarked(final int state)
    {
      if (mPreconditionMarkedStates == null) {
        return true;
      } else {
        return mPreconditionMarkedStates.contains(state);
      }
    }

    //#######################################################################
    //# Data Members
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
     * Codes of the initial states of the automaton.
     */
    private final int[] mInitialStates;

    private final AutomatonProxy mAutomaton;

  }


  //#########################################################################
  //# Inner Class StateEncodingData
  /**
   * Data for encoding an automaton's state in the synchronous product. Bits is
   * the number of bits needed to represent the state, and shift is the amount
   * required to shift the value into position.
   *
   * |--------------------0011--------| |4 | bits |-8------| shift
   */
  private static class StateEncodingData
  {

    //#######################################################################
    //# Constructor
    private StateEncodingData(final int bits, final int shift)
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

    //#######################################################################
    //# Simple Access
    /**
     * Encode the state for the current automata (s) into the synchronous
     * product state sstate.
     */
    private long encode(final long sstate, final int s)
    {
      return sstate | (long) s << mShift;
    }

    private int decode(final long sstate)
    {
      return (int) ((sstate >>> mShift) & mMask);
    }

    //#######################################################################
    //# Data Members
    private final long mMask;
    private final int mShift;

  }

  //#########################################################################
  //# Data Members
  private EventMap mEventMap;
  private SyncProduct mSyncProduct;

  //#########################################################################
  //# Class Constants
  private static final int MAXDEPTH = 1024;

}
