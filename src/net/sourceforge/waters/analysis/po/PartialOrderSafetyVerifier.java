//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.analysis.po
//# CLASS:   PartialOrderSafetyVerifier
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.analysis.po;

import gnu.trove.list.array.TIntArrayList;
import gnu.trove.set.hash.THashSet;
import gnu.trove.set.hash.TIntHashSet;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.BitSet;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import net.sourceforge.waters.analysis.monolithic.BlockedArrayList;
import net.sourceforge.waters.analysis.monolithic.StateHashSet;
import net.sourceforge.waters.model.analysis.AbortException;
import net.sourceforge.waters.model.analysis.AnalysisException;
import net.sourceforge.waters.model.analysis.InvalidModelException;
import net.sourceforge.waters.model.analysis.KindTranslator;
import net.sourceforge.waters.model.analysis.OverflowException;
import net.sourceforge.waters.model.analysis.VerificationResult;
import net.sourceforge.waters.model.analysis.des.AbstractSafetyVerifier;
import net.sourceforge.waters.model.analysis.des.NondeterministicDESException;
import net.sourceforge.waters.model.analysis.des.SafetyDiagnostics;
import net.sourceforge.waters.model.analysis.des.SafetyVerifier;
import net.sourceforge.waters.model.des.AutomatonProxy;
import net.sourceforge.waters.model.des.EventProxy;
import net.sourceforge.waters.model.des.ProductDESProxy;
import net.sourceforge.waters.model.des.ProductDESProxyFactory;
import net.sourceforge.waters.model.des.SafetyTraceProxy;
import net.sourceforge.waters.model.des.StateProxy;
import net.sourceforge.waters.model.des.TraceStepProxy;
import net.sourceforge.waters.model.des.TransitionProxy;
import net.sourceforge.waters.xsd.base.ComponentKind;
import net.sourceforge.waters.xsd.base.EventKind;

import org.apache.log4j.Logger;


/**
 * <P>
 * A Java implementation of the controllability check algorithm. This
 * algorithm does a brute-force state exploration to check whether the given
 * model is controllable.
 * </P>
 *
 * @author Adrian Shaw
 */

public class PartialOrderSafetyVerifier extends AbstractSafetyVerifier
  implements SafetyVerifier
{

  //#########################################################################
  //# Constructors
  /**
   * Creates a new safety verifier to check a particular model.
   *
   * @param translator
   *          The kind translator is used to remap component and event kinds.
   * @param factory
   *          The factory used for trace construction.
   */
  public PartialOrderSafetyVerifier(final KindTranslator translator,
                                    final SafetyDiagnostics diag,
                                    final ProductDESProxyFactory factory)
  {
    this(null, translator, diag, factory);
  }

  /**
   * Creates a new safety verifier to check a particular model.
   *
   * @param model
   *          The model to be checked by this verifier.
   * @param translator
   *          The kind translator is used to remap component and event kinds.
   * @param factory
   *          The factory used for trace construction.
   */
  public PartialOrderSafetyVerifier(final ProductDESProxy model,
                                    final KindTranslator translator,
                                    final SafetyDiagnostics diag,
                                    final ProductDESProxyFactory factory)
  {
    super(model, translator, diag, factory);
    MAXDEPTH = 500;
  }

  //#########################################################################
  //# Invocation

  @Override
  @SuppressWarnings("unchecked")
  public boolean run() throws AnalysisException
  {
    try {
      setUp();
      final ProductDESProxy model = getModel();
      final KindTranslator translator = getKindTranslator();

      Set<StateProxy> stateSet;
      int i, j, k = 0;
      int ck = 0;
      int bl = 0;
      int mask = 0;
      int codeLength = 0;
      int cp = 0;

      mNumAutomata = 0;
      mNumEvents = 0;
      mNumPlants = 0;
      mStateTupleSize = 0;

      mLoopCount = 0;

      final Collection<AutomatonProxy> automata =
        new LinkedList<AutomatonProxy>();
      for (final AutomatonProxy aut : model.getAutomata()) {
        final ComponentKind kind = translator.getComponentKind(aut);
        if (kind != null) {
          switch (kind) {
          case PLANT:
            mNumPlants++;
            automata.add(aut);
            break;
          case SPEC:
            automata.add(aut);
            break;
          default:
            break;
          }
        }
      }

      mPlantTransitionMap = new ArrayList<int[][]>();
      mSpecTransitionMap = new ArrayList<int[][]>();
      mIndexList = new ArrayList<Integer>();
      mStateList = new BlockedArrayList<PartialOrderStateTuple>(PartialOrderStateTuple.class);
      // TODO Order events so uncontrollables are first.
      mEventCodingList = new ArrayList<EventProxy>(model.getEvents());
      mPlantEventList = new ArrayList<byte[]>();
      mSpecEventList = new ArrayList<byte[]>();
      mPlantEventHash = new ArrayList<int[]>();
      mSpecEventHash = new ArrayList<int[]>();

      mNumEvents = mEventCodingList.size();
      mNumAutomata = automata.size();
      mAutomata = new AutomatonProxy[mNumAutomata];

      mEnabledUnionList = new TIntArrayList(mNumEvents);

      // Empty case
      if (mNumAutomata == 0) {
        return setSatisfiedResult();
      }

      mBitLengthList = new int[mNumAutomata];
      mMaskList = new int[mNumAutomata];
      mCodePosition = new int[mNumAutomata];
      mSystemState = new int[mNumAutomata];

      final List<AutomatonProxy>[] automataContainingEvents = new ArrayList[mNumEvents];
      for (i = 0; i < mNumEvents; i++){
        automataContainingEvents[i] = new ArrayList<AutomatonProxy>();
      }

      // Separate the automatons by kind
      AutomatonProxy initUncontrollable = null;
      for (final AutomatonProxy ap : automata) {
        // Get all states
        stateSet = ap.getStates();
        // Encoding states to binary values
        final List<StateProxy> codes = new ArrayList<StateProxy>(stateSet);
        // Encoding events to binary values
        final byte[] aneventCodingList = new byte[mNumEvents];
        final int[] events = new int[ap.getEvents().size()];
        i = 0;
        for (final EventProxy evp : ap.getEvents()) {
          final int eventIndex = mEventCodingList.indexOf(evp);
          events[i] = eventIndex;
          aneventCodingList[eventIndex] = 1;
          automataContainingEvents[eventIndex].add(ap);
          i++;
        }
        // Encoding transitions to binary values
        final int stateSize = codes.size();
        final int[][] atransition = new int[stateSize][mNumEvents];
        for (i = 0; i < stateSize; i++) {
          for (j = 0; j < mNumEvents; j++) {
            atransition[i][j] = -1;
          }
        }
        for (final TransitionProxy tp : ap.getTransitions()) {
          final int source = codes.indexOf(tp.getSource());
          final int event = mEventCodingList.indexOf(tp.getEvent());
          if (atransition[source][event] >= 0) {
            throw new NondeterministicDESException(ap, tp.getSource(),
                                                   tp.getEvent());
          }
          final int target = codes.indexOf(tp.getTarget());
          atransition[source][event] = target;
        }
        // Compute bit length and mask
        bl = BigInteger.valueOf(stateSize).bitLength();
        mask = (1 << bl) - 1;

        // Find initial state
        StateProxy initialState = null;
        for (final StateProxy sp : stateSet) {
          if (sp.isInitial()) {
            if (initialState == null) {
              initialState = sp;
            } else {
              throw new NondeterministicDESException(ap, sp);
            }
          }
        }
        final ComponentKind kind = translator.getComponentKind(ap);
        if (initialState == null) {
          if (kind == ComponentKind.PLANT
              || translator.getEventKind(KindTranslator.INIT) == EventKind.CONTROLLABLE) {
            return setSatisfiedResult();
          } else {
            initUncontrollable = ap;
          }
        }
        // Store all the information by automaton type
        switch (kind) {
        case PLANT:
          mAutomata[ck] = ap;
          mSystemState[ck] = codes.indexOf(initialState);
          mPlantEventList.add(aneventCodingList);
          mPlantEventHash.add(events);
          mPlantTransitionMap.add(atransition);
          mBitLengthList[ck] = bl;
          mMaskList[ck] = mask;
          ck++;
          break;
        case SPEC:
          final int pk = k + mNumPlants;
          mAutomata[pk] = ap;
          mSystemState[pk] = codes.indexOf(initialState);
          mSpecEventList.add(aneventCodingList);
          mSpecEventHash.add(events);
          mSpecTransitionMap.add(atransition);
          mBitLengthList[pk] = bl;
          mMaskList[pk] = mask;
          k++;
          break;
        default:
          break;
        }
      }
      if (initUncontrollable != null) {
        final ProductDESProxyFactory factory = getFactory();
        final String tracename = getTraceName();
        final String comment =
          getTraceComment(null, initUncontrollable, null);
        final TraceStepProxy step = factory.createTraceStepProxy(null);
        final List<TraceStepProxy> steps = Collections.singletonList(step);
        final SafetyTraceProxy counterexample =
          factory.createSafetyTraceProxy(tracename, comment, null, model,
                                         automata, steps);
        return setFailedResult(counterexample);
      }

      //Begin to compute dependency of events
      final PartialOrderEventDependencyKind[][] eventDependencyMap =
        PartialOrderEventDependencyKind.arrayOfDefault(mNumEvents);

      for (i = 0; i < mEventCodingList.size(); i++) {
        // Consider every possible pairs of events in the model by looping
        // through events twice.
        final Collection<AutomatonProxy> outerAutomata =
          new THashSet<AutomatonProxy>(automataContainingEvents[i]);
        for (j = 0; j < i; j++) {
          //ordering has no effect on dependency so only check events one way
          boolean commuting = true;
          //get the list of automata containing event at index j
          final Collection<AutomatonProxy> innerAutomata =
            automataContainingEvents[j];
          //compute the list of all automata that contain both of the events currently being considered
          for (final AutomatonProxy ap : innerAutomata) {
            if (outerAutomata.contains(ap)) {
              stateSet = ap.getStates();
              //the two events can either be exclusive or not in any automata
              boolean exclusive = true;
              int[][] transitionMap = null;
              //get the appropriate transition map for the automata currently being considered
              final int index = indexOfAutomaton(ap, mAutomata);
              if (index >= 0) {
                //transition maps are stored in different lists depending on the kind of the automata
                if (translator.getComponentKind(ap) == ComponentKind.PLANT) {
                  transitionMap = mPlantTransitionMap.get(index);
                } else if (translator.getComponentKind(ap) == ComponentKind.SPEC) {
                  //specification automata begin to be indexed after all of the plants in the list of automata
                  transitionMap = mSpecTransitionMap.get(index - mNumPlants);
                }
              } else {
                throw new InvalidModelException("Cannot find automaton "
                                                + ap.getName());
              }

              //check every state in the current automaton and check commutativity and exclusivity
              for (k = 0; k < stateSet.size(); k++) {
                int targetIndex1;
                int targetIndex2;

                //check if both events are enabled in the current state and store their targets
                if ((targetIndex1 = transitionMap[k][i]) > -1
                    && (targetIndex2 =
                      transitionMap[k][j]) > -1) {
                  //when both events are enabled they commute iff when executed in either sequence they result in the same state and they do not disable one another.
                  //As soon as the two events are found not to commute in any single automaton they will not commute in the synchronous product unless they are found
                  //to be exclusive.
                  commuting &=
                    transitionMap[targetIndex1][j] == transitionMap[targetIndex2][i]
                      && transitionMap[targetIndex1][j] != -1;
                  //two events enabled in the same state are by definition not exclusive in that automaton
                  exclusive = false;
                }
              }
              //two events found to remain exclusive after checking all states in any automaton where they both exist guarantees the independence of those events
              //in the synchronous product, regardless of whether or not they commute in any or all automata
              if (exclusive) {
                eventDependencyMap[i][j] =
                  PartialOrderEventDependencyKind.EXCLUSIVE;
                eventDependencyMap[j][i] =
                  PartialOrderEventDependencyKind.EXCLUSIVE;
                break;
              }
            }
          }
          //if after checking all the states in which both events occur the states are found to commute every time they are both enabled, then those events will be
          //independent in the synchronous product
          if (commuting) {
            if (eventDependencyMap[i][j] !=
              PartialOrderEventDependencyKind.EXCLUSIVE) {
              eventDependencyMap[i][j] =
                PartialOrderEventDependencyKind.COMMUTING;
              eventDependencyMap[j][i] =
                PartialOrderEventDependencyKind.COMMUTING;
            }
          }
        }
      }
      int numDependents = 0;
      mReducedEventDependencyMap =
        new PartialOrderEventDependencyTuple[mNumEvents][];
      for (i = 0; i < mNumEvents; i++) {
        final ArrayList<PartialOrderEventDependencyTuple> temp =
          new ArrayList<PartialOrderEventDependencyTuple>();
        for (j = 0; j < mNumEvents; j++) {
          if (i != j){
            if (eventDependencyMap[i][j] == PartialOrderEventDependencyKind.NONCOMMUTING) {
            temp.add(new PartialOrderEventDependencyTuple
                     (j, eventDependencyMap[i][j]));
            numDependents++;
            }
          }
        }
        mReducedEventDependencyMap[i] =
          temp.toArray(new PartialOrderEventDependencyTuple[temp.size()]);
      }
      numDependents/=2;
      mNumIndependentPairings = getTotalEventPairings() - numDependents;

      // Compute stuttering of events
      // Set up initial conditions, all events labelled as stuttering
      //setEnablings();

      //Non-stuttering if an event can take the system from a controllable
      //state to an uncontrollable state. Controllability of states depends
      //on the kinds of events (uncontrollable/controllable) that are enabled
      //in them and if they belong to a plant or spec automaton

      //Iterate over all uncontrollable events
      /*for (i = 0; i < mNumEvents; i++){
        if (mEventCodingList.get(i).getKind() == EventKind.UNCONTROLLABLE){
          events:
          //Iterate over all events to consider every event pairing
          for (j = 0; j < mNumEvents; j++){
            //If a transition in a plant involving event x never leads to a
            //state in which an uncontrollable event y is enabled, then event
            //x can never lead to an uncontrollable state, and hence is a
            //stuttering event
            for (k = 0; k < mNumPlants; k++){
              if (!mPartialOrderEvents[j].eventEnablesUncontrollable(i,k)){
                continue events;
              }
            }
            //If a transition involving event x can lead to a state y in which
            //and uncontrollable state is enabled, then all transitions in all
            //specs involving x must lead to a state where that uncontrollable
            //event is enabled, otherwise x is non stuttering
            for (int l = mNumPlants; l < mNumAutomata; l++){
              if (mPartialOrderEvents[j].eventDisablesUncontrollable(i,l)){
                mPartialOrderEvents[j].setStutter(PartialOrderEventStutteringKind.NONSTUTTERING);
                break;
              }
            }
          }
        }
      }*/


      // Set the mCodePosition list
      for (i = 0; i < mNumAutomata; i++) {
        codeLength += mBitLengthList[i];
        if (codeLength <= 32) {
          mCodePosition[i] = cp;
        } else {
          codeLength = mBitLengthList[i];
          cp++;
          mCodePosition[i] = cp;
        }
      }
      mStateTupleSize = cp + 1;

      if (isControllableReduced(mSystemState)) {
        return setSatisfiedResult();
      } else {
        convertToBredthFirst();
        final SafetyTraceProxy counterexample = computePOCounterExample();
        return setFailedResult(counterexample);
      }
    } catch (final AnalysisException exception) {
      throw setExceptionResult(exception);
    } catch (final OutOfMemoryError error) {
      tearDown();
      final Logger logger = getLogger();
      logger.debug("<out of memory>");
      final OverflowException exception = new OverflowException(error);
      throw setExceptionResult(exception);
    } finally {
      tearDown();
    }
  }

  /**
   * Finds and returns the index of a given automaton in a given array
   *
   * @param ap
   *          - automaton to be found
   * @param automata
   *          - array to search
   * @return - index of ap in automaton or -1 if not contained
   */
  private int indexOfAutomaton(final AutomatonProxy ap,
                               final AutomatonProxy[] automata)
  {
    for (int i = 0; i < automata.length; i++) {
      if (automata[i] == ap) {
        return i;
      }
    }
    return -1;
  }

  /**
   * Initialises the array of partial order events and synchronises it with
   * the existing array of events. Calculates for every event which uncontrollable
   * events they either enable for each plant, or disable for each spec
   */
  @SuppressWarnings("unused")
  private void setEnablings(){
    //Initialising array of partial order events
    mPartialOrderEvents = new PartialOrderEvent[mNumEvents];
    for (int i = 0; i < mNumEvents; i++){
      mPartialOrderEvents[i] = new PartialOrderEvent(i,mNumAutomata,mNumPlants,mNumEvents);
    }
    //Begin to compute enablings/disablings for events
    //Each event has a different set of enablings for every automata, so loop
    //over each automata
    for (int j = 0; j < mNumAutomata; j++){
      //pick out the transition map for the current automata
      final int[][] transitionMap = j < mNumPlants ? mPlantTransitionMap.get(j) :
        mSpecTransitionMap.get(j - mNumPlants);
      final byte[] eventList = j < mNumPlants ? mPlantEventList.get(j) :
        mSpecEventList.get(j - mNumPlants);
      final int size = mAutomata[j].getStates().size();
      //Each event can be enabled in any number of states initially so loop
      //over each state
      for (int i = 0; i < size; i++){
        //The index of the following loop will be the index of the event that
        //the enabling is being computed for
        for (int k = 0; k < mNumEvents; k++){
          if (eventList[k] != 1){
            continue;
          }
          //find the target state for the transition involving the currently
          //visited state and the event being considered for enablings
          final int target = transitionMap[i][k];
          if (target != -1){
            //if such a target exists then the event is enabled in that state
            //so now check all other uncontrollable events to see if they are
            //enabled or disabled in that target state and record the information
            //in the current partial order event
            for (int l = 0; l < mNumEvents; l++){
              if (eventList[l] != 1){
                continue;
              }
              if (mEventCodingList.get(l).getKind() == EventKind.UNCONTROLLABLE){
                mPartialOrderEvents[k].addEnabled
                  (j, l, transitionMap[target][l] != -1);
              }
            }
          }
        }
      }
    }
  }

  private int getTotalEventPairings(){
    return (mNumEvents * (mNumEvents - 1)) / 2;
  }

  @Override
  public void tearDown(){
    super.tearDown();
    mStateList = null;
    mStateSet = null;
    mIndexList = null;
  }

  //#########################################################################
  //# Interface net.sourceforge.waters.model.analysis.ModelAnalyser
  @Override
  public boolean supportsNondeterminism()
  {
    return false;
  }

  //#########################################################################
  //# Interface net.sourceforge.waters.model.analysis.ModelVerifier
  @Override
  public void setKindTranslator(final KindTranslator translator)
  {
    super.setKindTranslator(translator);
    clearAnalysisResult();
  }

  //#########################################################################
  //# Setting the Result
  @Override
  protected void addStatistics()
  {
    //final int totalPairings = getTotalEventPairings();
    //System.out.println("Number of independent event pairings in " +
     //                   getModel().getName() + ": "
     //                   + mNumIndependentPairings + "/" + totalPairings +
     //                   "\nCycles closed: " + mLoopCount);
    super.addStatistics();
    final VerificationResult result = getAnalysisResult();
    result.setNumberOfAutomata(mNumAutomata);
    if (mStateSet != null){
      final int numstates = mStateSet.size();
      result.setNumberOfStates(numstates);
      result.setPeakNumberOfNodes(numstates);
    }
  }

  //#########################################################################
  //# Auxiliary Methods

  private boolean isControllableReduced(final int[] sState) throws AnalysisException{
    mStack = new ArrayList<PartialOrderStateTuple>();
    mStateSet = new StateHashSet<PartialOrderStateTuple>(PartialOrderStateTuple.class);
    mLocalSet = new TIntHashSet();
    mSuccessor = new int[mNumAutomata];

    final PartialOrderStateTuple dummy = new PartialOrderStateTuple(0);
    mInitialState = new PartialOrderStateTuple(mStateTupleSize);
    encode(sState, mInitialState);
    mInitialState.setPred(dummy);
    mStateSet.getOrAdd(mInitialState);
    mStack.add(mInitialState);
    mStateTuple = new PartialOrderStateTuple(mStateTupleSize);

    final List<PartialOrderStateTuple> backtrace = new ArrayList<PartialOrderStateTuple>();
    backtrace.add(dummy);
    PartialOrderStateTuple pred;
    PartialOrderStateTuple current;
    //mFullyExpanded = new THashSet<PartialOrderStateTuple>();

    while(true){
      if (mStack.isEmpty()){
        current = null;
        pred = dummy;
      }
      else{
        current = mStack.get(mStack.size() - 1);
        pred = current.getPred();
      }
      while (backtrace.get(backtrace.size() - 1) != pred){
        final PartialOrderStateTuple popped = backtrace.remove(backtrace.size() - 1);
        popped.setMayNeedExpansion(false);
        if (popped.FullyExpand()){
          current = popped;
          current.setFullyExpand(false);
          break;
        }
      }
      if (current == null){
        break;
      }
      backtrace.add(current);
      if (!mStack.isEmpty()){
        if (current == mStack.get(mStack.size() - 1)){
          mStack.remove(mStack.size() - 1);
          current.setMayNeedExpansion(true);
          final int[] ample;
          if ((ample = ample3(current)) == null){
            return false;
          }
          expand(current,ample);
        }
        else{
          expand(current, enabled(current));
        }
      }
      else{
        expand(current, enabled(current));
      }
    }
    return true;
  }

  private void expand(final PartialOrderStateTuple current, final int[] events) throws AnalysisException{
    int i;
    for (final int e : events){
      for (i = 0; i < mNumAutomata; i++){
        final boolean plant = i < mNumPlants;
        final int si = i - mNumPlants;
        if ((plant ?
            mPlantEventList.get(i)[e]:mSpecEventList.get(si)[e]) != 1){
          mSuccessor[i] = mSystemState[i];
        }
        else {
          mSuccessor[i] = plant ? mPlantTransitionMap.get(i)[mSystemState[i]][e] :
            mSpecTransitionMap.get(si)[mSystemState[i]][e];
        }
      }
      encode(mSuccessor, mStateTuple);
      final PartialOrderStateTuple found =mStateSet.getOrAdd(mStateTuple);
      if (found == null) {
        mStateTuple.setPred(current);
        mStack.add(mStateTuple);
        mStateTuple = new PartialOrderStateTuple(mStateTupleSize);
        if (mStateSet.size() > getNodeLimit()) {
          throw new OverflowException(getNodeLimit());
        } else {
          checkAbort();
        }
      }
      else{
        if (found.mayNeedExpansion()){
          found.setFullyExpand(true);
          mLoopCount++;
        }
      }
    }
  }

  private int[] enabled(final PartialOrderStateTuple current)
  {
    final KindTranslator translator = getKindTranslator();
    final TIntArrayList temp = new TIntArrayList();
    decode(current,mSystemState);
    events:
    for (int i = 0; i < mNumEvents; i++){
      final EventProxy event = mEventCodingList.get(i);
      final EventKind kind = translator.getEventKind(event);
      for (int j = 0; j < mNumAutomata; j++){
        final boolean plant = j < mNumPlants;
        final int si = j - mNumPlants;
        if ((plant ? mPlantEventList.get(j)[i]:mSpecEventList.get(si)[i]) == 0){
          continue;
        }
        final int[][] transitionMap = plant ? mPlantTransitionMap.get(j) :
          mSpecTransitionMap.get(j - mNumPlants);
        if (transitionMap[mSystemState[j]][i] == -1){
          if (kind == EventKind.UNCONTROLLABLE && !plant){
            mErrorEvent = i;
            mErrorAutomaton = j;
            mErrorState = current;
            return null;
          }
          else{
            continue events;
          }
        }
      }
      temp.add(i);
    }
    return temp.toArray();
  }

  @SuppressWarnings("unused")
  private int[] ample(final PartialOrderStateTuple current){
    final int[] enabled = enabled(current);
    if (enabled == null){
      return null;
    }
    if (enabled.length == 1){
      return enabled;
    }

    final TIntArrayList ample = new TIntArrayList();
    final TIntHashSet ampleSet = new TIntHashSet();
    final BitSet ampleDependencies = new BitSet(mNumEvents);

    int i, temp, e;
    int next = 0;
    final int[] ampleState = new int[mNumAutomata];
    PartialOrderStateTuple ampleStateTuple = new PartialOrderStateTuple(mStateTupleSize);

    ample:
    while (ample.size() < enabled.length){
      final int ampleCandidate = enabled[next];
      ample.add(ampleCandidate);
      ampleSet.add(ampleCandidate);
      if (ampleSet.containsAll(enabled)){
        return enabled;
      }
      for (final PartialOrderEventDependencyTuple t :
        mReducedEventDependencyMap[ampleCandidate]){
        ampleDependencies.set(t.getCoupling());
      }
      next++;

      final List<PartialOrderStateTuple> stack = new ArrayList<PartialOrderStateTuple>();
      final StateHashSet<PartialOrderStateTuple> localStateSet =
        new StateHashSet<PartialOrderStateTuple>(PartialOrderStateTuple.class);
      stack.add(current);
      localStateSet.getOrAdd(current);
      while(stack .size() > 0){
        final PartialOrderStateTuple newCurrent = stack.remove(stack.size() - 1);
        decode(newCurrent,ampleState);
        events:
        for (e = 0; e < mNumEvents; e++){
          for (i = 0; i < mNumAutomata; i++){
            final boolean plant = i < mNumPlants;
            final int si = i - mNumPlants;
            if ((plant ?
              mPlantEventList.get(i)[e]:mSpecEventList.get(si)[e]) != 1){
              mSuccessor[i] = ampleState[i];
            }
            else if ((temp = plant ? mPlantTransitionMap.get(i)[ampleState[i]][e] :
              mSpecTransitionMap.get(si)[ampleState[i]][e]) != -1){
              mSuccessor[i] = temp;
            }
            else{
              continue events;
            }
          }
          if (ampleSet.contains(e)){
            continue events;
          }
          if (ampleDependencies.get(e)){
            continue ample;
          }
          encode(mSuccessor, ampleStateTuple);
          if (localStateSet.getOrAdd(ampleStateTuple) == null) {
            stack.add(ampleStateTuple);
            if (stack.size() > MAXDEPTH){
              continue ample;
            }
          }
          ampleStateTuple = new PartialOrderStateTuple(mStateTupleSize);
        }
      }
      break;
    }
    return ample.toArray();
  }

  private int[] ample3(final PartialOrderStateTuple current){
    final int[] enabled = enabled(current);
    if (enabled == null){
      return null;
    }
    if (enabled.length == 1){
      return enabled;
    }
    final TIntHashSet enabledSet = new TIntHashSet(enabled);
    final TIntArrayList ample = new TIntArrayList();
    final TIntHashSet ampleSet = new TIntHashSet();
    final TIntHashSet considered = new TIntHashSet();
    int next = 0;
    int i,j,k;

    ample:
    for (i = 0; i < enabled.length; i++){
      if (considered.contains(enabled[next])){
        continue;
      }
      final TIntArrayList dependentNonEnabled = new TIntArrayList();

      ample.add(enabled[next]);
      ampleSet.add(enabled[next]);
      considered.add(enabled[next]);
      next++;

      for (j = 0; j < mEventCodingList.size(); j++){
        if (!ampleSet.contains(j)){
          final BitSet ampleDependencies = new BitSet(mNumEvents);
          for (final PartialOrderEventDependencyTuple t :
            mReducedEventDependencyMap[j]){
            ampleDependencies.set(t.getCoupling());
          }
          for (k = 0; k < ample.size(); k++){
            if (ampleDependencies.get(ample.get(k))){
              if (enabledSet.contains(j)){
                ample.add(j);
                ampleSet.add(j);
                considered.add(j);
                j = -1;
              }
              else{
                dependentNonEnabled.add(j);
              }
              break;
            }
          }
        }
      }
      final TIntHashSet unionSet =
        new TIntHashSet(ample.size() + dependentNonEnabled.size());
      for (j = 0; j < dependentNonEnabled.size(); j++){
        unionSet.add(dependentNonEnabled.get(j));
      }
      for (j = 0; j < ample.size(); j++){
        unionSet.add(ample.get(j));
      }
      if (unionSet.size() == mEventCodingList.size()){
        return ample.toArray();
      }
      final TIntHashSet eventsSetMinusUnion = new TIntHashSet();
      for (j = 0; j < mEventCodingList.size(); j++){
        if (!unionSet.contains(j)){
          eventsSetMinusUnion.add(j);
        }
      }
      boolean danger = false;
      for (k = 0; k < dependentNonEnabled.size(); k++){
        final int dependent = dependentNonEnabled.get(k);
        for (j = 0; j < mAutomata.length; j++){
          final boolean plant = j < mNumPlants;
          final byte[] eventList = plant ? mPlantEventList.get(j) :
                          mSpecEventList.get(j - mNumPlants);
          if (eventList[dependent] == 1){
            if (canBecomeEnabled(current,dependent,eventsSetMinusUnion,j)){
              danger = true;
              break;
            }
          }
        }
        if (danger){
          ample.clear();
          ampleSet.clear();
          continue ample;
        }
      }
      return ample.toArray();
    }
    return enabled;
  }

  private boolean canBecomeEnabled(final PartialOrderStateTuple current,
                                   final int dependent, final TIntHashSet unionList,
                                   final int automatonIndex){
    int i, e, temp;
    final boolean plant = automatonIndex < mNumPlants;
    final int si = automatonIndex - mNumPlants;

    final int[][] transMap = plant ? mPlantTransitionMap.get(automatonIndex):
      mSpecTransitionMap.get(si);
    final int[] eventArray = plant? mPlantEventHash.get(automatonIndex) :
      mSpecEventHash.get(si);

    for (i = 0; i < eventArray.length; i++){
      e = eventArray[i];
      if (unionList.contains(e)){
        mEnabledUnionList.add(e);
      }
    }

    if (mEnabledUnionList.size() > 0){
      final TIntArrayList stack = new TIntArrayList();
      final int[] ampleState = new int[mNumAutomata];

      decode(current,ampleState);
      stack.add(ampleState[automatonIndex]);
      mLocalSet.add(ampleState[automatonIndex]);

      while(stack .size() > 0){
        final int stateIndex = stack.removeAt(stack.size() - 1);
        if(transMap[stateIndex][dependent] != -1){
          mLocalSet.clear();
          mEnabledUnionList.clear();
          return true;
        }
        i = mEnabledUnionList.size();
        for (e = 0; e < i; e++){
          final int event = mEnabledUnionList.get(e);
          if ((temp = transMap[stateIndex][event]) != -1){
            if (mLocalSet.add(temp)){
              stack.add(temp);
            }
          }
        }
      }
      mEnabledUnionList.clear();
      mLocalSet.clear();
    }
    return false;
  }

  @SuppressWarnings("unused")
  private void orderStutterEvents(final int[] events){
    final TIntArrayList stutter = new TIntArrayList();
    final TIntArrayList nonStutter = new TIntArrayList();
    for (final int e: events){
      if (mPartialOrderEvents[e].getStutter() ==
            PartialOrderEventStutteringKind.STUTTERING){
        stutter.add(e);
      }
      else{
        nonStutter.add(e);
      }
    }
    for (int i = 0; i <  stutter.size(); i++){
      events[i] = stutter.get(i);
    }
    for (int i = 0; i < nonStutter.size(); i++){
      events[i + stutter.size()] = nonStutter.get(i);
    }
  }

  private void convertToBredthFirst(){
    mStateList = new ArrayList<PartialOrderStateTuple>();
    mStateList.add(mInitialState);
    mInitialState.setVisited(true);
    int open = 0;
    mIndexList.add(open);
    mIndexList.add(mStateList.size());
    mStateTuple = new PartialOrderStateTuple(mStateTupleSize);

    int i,j,temp;

    while (open < mStateList.size()){
      final PartialOrderStateTuple current = mStateList.get(open);
      open++;
      decode(current,mSystemState);
      events:
      for (i = 0; i < mNumEvents; i++){
        for (j = 0; j < mNumAutomata; j++){
          final boolean plant = j < mNumPlants;
          final int si = j - mNumPlants;
          if ((plant ? mPlantEventList.get(j)[i]:mSpecEventList.get(si)[i]) == 0){
            mSuccessor[j] = mSystemState[j];
          }
          else if ((temp = plant ? mPlantTransitionMap.get(j)[mSystemState[j]][i] :
            mSpecTransitionMap.get(si)[mSystemState[j]][i]) != -1){
            mSuccessor[j] = temp;
          }
          else{
            continue events;
          }
        }
        encode(mSuccessor, mStateTuple);
        final PartialOrderStateTuple tuple = mStateSet.get(mStateTuple);
        if (tuple != null && !tuple.getVisited()){
          mStateList.add(tuple);
          tuple.setVisited(true);
          if (tuple == mErrorState){
            return;
          }
        }
      }
      if (open == mIndexList.get(mIndexList.size() - 1)){
        mIndexList.add(mStateList.size());
      }
    }
    //assert false;
  }

  private SafetyTraceProxy computePOCounterExample() throws AbortException
  {
    final ProductDESProxyFactory factory = getFactory();
    final ProductDESProxy des = getModel();
    final List<TraceStepProxy> steps = new LinkedList<TraceStepProxy>();
    final EventProxy errorEvent = mEventCodingList.get(mErrorEvent);
    final AutomatonProxy errorAut = mAutomata[mErrorAutomaton];
    final List<StateProxy> states =
      new ArrayList<StateProxy>(errorAut.getStates());
    final int errorStateIndex = mSystemState[mErrorAutomaton];
    final StateProxy errorState = states.get(errorStateIndex);
    final TraceStepProxy errorStep = factory.createTraceStepProxy(errorEvent);
    steps.add(0, errorStep);

    PartialOrderStateTuple error = mErrorState;

    int i,j,k,temp;

    //Start searching at the second to last level
    int currentLevel = mIndexList.size() - 1;
    outer:
    while (!error.equals(mInitialState)){
      for (i = mIndexList.get(currentLevel - 1); i < mIndexList.get(currentLevel); i++){
        decode(mStateList.get(i),mSystemState);
        events:
        for (j = 0; j < mNumEvents; j++){
          for (k = 0; k < mNumAutomata; k++){
            final boolean plant = k < mNumPlants;
            final int si = k - mNumPlants;
            if ((plant ?
              mPlantEventList.get(k)[j]:mSpecEventList.get(si)[j]) == 0){
              mSuccessor[k] = mSystemState[k];
            }
            else if ((temp = plant ? mPlantTransitionMap.get(k)[mSystemState[k]][j] :
              mSpecTransitionMap.get(si)[mSystemState[k]][j]) != -1){
              mSuccessor[k] = temp;
            }
            else{
              continue events;
            }
          }
          encode(mSuccessor, mStateTuple);
          if (error.equals(mStateTuple)){
            error = mStateList.get(i);
            final EventProxy event = mEventCodingList.get(j);
            final TraceStepProxy step = factory.createTraceStepProxy(event);
            steps.add(0, step);
            currentLevel--;
            continue outer;
          }
        }
      }
    }
    final TraceStepProxy init = factory.createTraceStepProxy(null);
    steps.add(0, init);
    final String tracename = getTraceName();
    final String comment = getTraceComment(errorEvent,errorAut,errorState);
    final List<AutomatonProxy> automata = Arrays.asList(mAutomata);
    final SafetyTraceProxy trace =
      factory.createSafetyTraceProxy(tracename, comment, null, des, automata,
                                     steps);
    return trace;
  }


  //#########################################################################
  //# Encoding
  /**
   * Encode the synchronous product into StateTuple
   *
   * @param sState
   *          The state to be encoded
   * @param sTuple
   *          The encoded StateTuple
   */
  private void encode(final int[] sState, final PartialOrderStateTuple sTuple)
  {
    int i;
    int k = 0;
    int result = 0;
    final int[] codes = sTuple.getCodes();
    for (i = 0; i < mNumAutomata; i++) {
      if (mCodePosition[i] == k) {
        result <<= mBitLengthList[i];
        result |= sState[i];
      } else {
        codes[k] = result;
        result = sState[i];
        k++;
      }
      if (i == mNumAutomata - 1) {
        codes[k] = result;
      }
    }
  }

  //#########################################################################
  //# Decoding
  /**
   * Decode the StateTuple
   *
   * @param sTuple
   *          The StateTuple to be decoded
   * @param state
   *          The decoded state
   */
  private void decode(final PartialOrderStateTuple sTuple, final int[] state)
  {
    int i;
    int result;
    int k = mCodePosition[mNumAutomata - 1];
    int temp = sTuple.get(k);
    for (i = mNumAutomata - 1; i > -1; i--) {
      if (mCodePosition[i] == k) {
        result = temp;
        result &= mMaskList[i];
        state[i] = result;
        temp >>= mBitLengthList[i];
      } else if (mCodePosition[i] < k) {
        k--;
        temp = sTuple.get(k);
        result = temp;
        result &= mMaskList[i];
        state[i] = result;
        temp >>= mBitLengthList[i];
      }
    }
  }


  //#########################################################################
  //# Data Members

  // Ample conditions
  private PartialOrderEventDependencyTuple[][] mReducedEventDependencyMap;
  final int MAXDEPTH;

  //Event information
  private PartialOrderEvent[] mPartialOrderEvents;

  // Transition map
  private List<int[][]> mPlantTransitionMap;
  private List<int[][]> mSpecTransitionMap;

  // Level states storage
  private List<Integer> mIndexList;
  private List<PartialOrderStateTuple> mStateList;
  private TIntArrayList mEnabledUnionList;
  //private THashSet<PartialOrderStateTuple> mFullyExpanded;

  //Stacks and sets
  private List<PartialOrderStateTuple> mStack;
  private StateHashSet<PartialOrderStateTuple> mStateSet;
  private TIntHashSet mLocalSet;

  // For encoding/decoding
  private AutomatonProxy[] mAutomata;
  private List<EventProxy> mEventCodingList;
  private List<byte[]> mPlantEventList;
  private List<byte[]> mSpecEventList;
  private int[] mBitLengthList;
  private int[] mMaskList;
  private int[] mCodePosition;
  private PartialOrderStateTuple mStateTuple;
  private List<int[]> mPlantEventHash;
  private List<int[]> mSpecEventHash;

  // Size
  private int mNumAutomata;
  private int mNumEvents;
  private int mNumPlants;
  private int mStateTupleSize;

  // For computing successor and counterexample
  private PartialOrderStateTuple mInitialState;
  private int[] mSystemState;
  private int[] mSuccessor;
  private PartialOrderStateTuple mErrorState;
  private int mErrorEvent;
  private int mErrorAutomaton;

  //Statistics
  @SuppressWarnings("unused")
  private int mLoopCount;
  @SuppressWarnings("unused")
  private int mNumIndependentPairings;
}

