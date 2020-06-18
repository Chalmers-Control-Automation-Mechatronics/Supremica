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

package net.sourceforge.waters.analysis.po;

import gnu.trove.list.array.TIntArrayList;
import gnu.trove.set.hash.THashSet;
import gnu.trove.set.hash.TIntHashSet;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import net.sourceforge.waters.analysis.monolithic.BlockedArrayList;
import net.sourceforge.waters.analysis.monolithic.StateHashSet;
import net.sourceforge.waters.analysis.options.Option;
import net.sourceforge.waters.analysis.options.OptionPage;
import net.sourceforge.waters.analysis.options.PositiveIntOption;
import net.sourceforge.waters.analysis.tr.WatersIntHeap;
import net.sourceforge.waters.model.analysis.AnalysisAbortException;
import net.sourceforge.waters.model.analysis.AnalysisException;
import net.sourceforge.waters.model.analysis.InvalidModelException;
import net.sourceforge.waters.model.analysis.OverflowException;
import net.sourceforge.waters.model.analysis.VerificationResult;
import net.sourceforge.waters.model.analysis.des.AbstractModelAnalyzerFactory;
import net.sourceforge.waters.model.analysis.des.AbstractModelVerifier;
import net.sourceforge.waters.model.analysis.des.EventNotFoundException;
import net.sourceforge.waters.model.analysis.des.ModelVerifier;
import net.sourceforge.waters.model.analysis.des.NondeterministicDESException;
import net.sourceforge.waters.model.analysis.kindtranslator.KindTranslator;
import net.sourceforge.waters.model.base.ComponentKind;
import net.sourceforge.waters.model.base.EventKind;
import net.sourceforge.waters.model.des.AutomatonProxy;
import net.sourceforge.waters.model.des.CounterExampleProxy;
import net.sourceforge.waters.model.des.EventProxy;
import net.sourceforge.waters.model.des.ProductDESProxy;
import net.sourceforge.waters.model.des.ProductDESProxyFactory;
import net.sourceforge.waters.model.des.StateProxy;
import net.sourceforge.waters.model.des.TransitionProxy;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * <P>
 * A Java implementation of an abstract model checker using an iterative
 * version of Tarjan's algorithm combined with Partial Order ample methods.
 * This abstract class may be used for either conflict or controllability
 * verification.
 * </P>
 *
 * @author Adrian Shaw
 */

public abstract class PartialOrderComponentsModelVerifier
  extends AbstractModelVerifier implements ModelVerifier
{

  //#########################################################################
  //# Constructors
  /**
   * Creates a new model verifier to check a particular model.
   *
   * @param translator
   *          The kind translator is used to remap component and event kinds.
   * @param factory
   *          The factory used for trace construction.
   */
  public PartialOrderComponentsModelVerifier(final ProductDESProxyFactory factory,
                                              final KindTranslator translator)
  {
    this(null,factory,translator);
  }

  public PartialOrderComponentsModelVerifier(final ProductDESProxy model,
                               final ProductDESProxyFactory factory,
                               final KindTranslator translator)
  {
    super(model, factory, translator);
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
      //System.out.println("Start");
      setUp();
      final ProductDESProxy model = getModel();
      final KindTranslator translator = getKindTranslator();

      Set<StateProxy> stateSet;
      int i,j;
      int k = 0;
      int ck = 0;
      int bl = 0;
      int mask = 0;
      int codeLength = 0;
      int cp = 0;

      mNumReducedSets = 0;
      mNumIndependentPairings = 0;

      mNumAutomata = 0;
      mNumEvents = 0;
      mNumPlants = 0;
      mStateTupleSize = 0;

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

      final Collection<EventProxy> modelEvents = model.getEvents();
      mEventCodingList = new ArrayList<>(modelEvents.size());
      for (final EventProxy event : modelEvents) {
        if (isSupportedEvent(event)) {
          mEventCodingList.add(event);
        }
      }
      mNumEvents = mEventCodingList.size();

      mLocalSet = new TIntHashSet();
      mPlantTransitionMap = new ArrayList<>();
      mSpecTransitionMap = new ArrayList<>();
      mIndexList = new TIntArrayList();
      mStateList = new BlockedArrayList<>(PartialOrderStateTuple.class);
      mPlantEventList = new ArrayList<>();
      mSpecEventList = new ArrayList<>();
      mPlantEventHash = new ArrayList<>();
      mSpecEventHash = new ArrayList<>();

      mNumAutomata = automata.size();
      mAutomata = new AutomatonProxy[mNumAutomata];
      if(this instanceof PartialOrderComponentsControllabilityChecker){
        mDumpStates = new int[mNumAutomata - mNumPlants];
      }

      mEnabledUnionList = new TIntArrayList(mNumEvents);

      // Empty case
      if (mNumAutomata == 0) {
        return setSatisfiedResult();
      }

      mBitLengthList = new int[mNumAutomata];
      mMaskList = new int[mNumAutomata];
      mCodePosition = new int[mNumAutomata];
      mSystemState = new int[mNumAutomata];
      final List<AutomatonProxy>[] automataContainingEvents = orderEvents();
      // Separate the automatons by kind
      AutomatonProxy initUncontrollable = null;
      for (final AutomatonProxy ap : automata) {
        // Get all states
        stateSet = ap.getStates();
        final ComponentKind kind = translator.getComponentKind(ap);
        // Encoding states to binary values
        final List<StateProxy> codes = new ArrayList<StateProxy>(stateSet);
        // Encoding events to binary values
        final byte[] aneventCodingList = new byte[mNumEvents];
        final int[] events = new int[ap.getEvents().size()];
        i = 0;
        for (final EventProxy evp : ap.getEvents()) {
          final int eventIndex = mEventCodingList.indexOf(evp);
          if (eventIndex >= 0) {
            events[i] = eventIndex;
            aneventCodingList[eventIndex] = 1;
            automataContainingEvents[eventIndex].add(ap);
            i++;
          }
        }
        // Encoding transitions to binary values
        final int stateSize = codes.size();
        final int[][] atransition = setupTransitions(codes,kind);
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
        if(kind == ComponentKind.SPEC){
          for(i = 0;
              i < mNumEvents &&
              mEventCodingList.get(i).getKind()== EventKind.UNCONTROLLABLE;
              i++){
            if(aneventCodingList[i]==1){
              for(j = 0;j < stateSize; j++){
                if(atransition[j][i] == -1){
                  atransition[j][i] = stateSize;
                }
              }
            }
          }
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

        if (initialState == null) {
          if (kind == ComponentKind.PLANT ||
            translator.getEventKind(KindTranslator.INIT) == EventKind.CONTROLLABLE) {
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
          mDumpStates[k] = stateSize;
          k++;
          break;
        default:
          break;
        }
      }
      if (initUncontrollable != null) {
        return setFailedResult(noInitialCounterexample(initUncontrollable,
                                                       model, automata));
      }
      setupDependencies(automataContainingEvents);
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

      if (isValid(mSystemState)) {
        return setSatisfiedResult();
      } else {
        convertToBreadthFirst();
        final CounterExampleProxy counterexample = computePOCounterExample();
        return setFailedResult(counterexample);
      }
    } catch (final AnalysisException exception) {
      throw setExceptionResult(exception);
    } catch (final OutOfMemoryError error) {
      tearDown();
      final Logger logger = LogManager.getLogger();
      logger.debug("<out of memory>");
      final OverflowException exception = new OverflowException(error);
      throw setExceptionResult(exception);
    } finally {
      tearDown();
    }
  }

  @Override
  public void tearDown(){
    super.tearDown();
    mStateList = null;
    mStateSet = null;
    mIndexList = null;
    mStack = null;
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
  /***
   * Computes an [event x event] map of dependencies between events storing
   * the result in mReducedEventDependencyMap. Considers each pair of events
   * in each automaton in which they both occur to estimate the dependency
   * relationship in the synchronous product. Events are considered independent
   * if they are either independent in all automata or they are found to be
   * exclusive in any automaton, otherwise they are considered dependent.
   *
   * @param automataContainingEvents
   *            An array of lists of automata, each of which containing the
   *            automata that enable the event indexed by the position in the
   *            array.
   * @throws InvalidModelException
   */
  private void setupDependencies(final List<AutomatonProxy>[] automataContainingEvents)
                                                  throws InvalidModelException{
    //Begin to compute dependency of events
    final PartialOrderEventDependencyKind[][] eventDependencyMap =
      PartialOrderEventDependencyKind.arrayOfDefault(mNumEvents);

    for (int i = 0; i < mEventCodingList.size(); i++) {
      // Consider every possible pairs of events in the model by looping
      // through events twice.
      final Collection<AutomatonProxy> outerAutomata =
        new THashSet<AutomatonProxy>(automataContainingEvents[i]);
      for (int j = 0; j < i; j++) {
        //ordering has no effect on dependency so only check events one way
        boolean commuting = true;
        //get the list of automata containing event at index j
        final Collection<AutomatonProxy> innerAutomata =
          automataContainingEvents[j];
        //compute the list of all automata that contain both of the events
        //currently being considered
        for (final AutomatonProxy ap : innerAutomata) {
          if (outerAutomata.contains(ap)) {
            final Set<StateProxy> stateSet = ap.getStates();
            //the two events can either be exclusive or not in any automata
            boolean exclusive = true;
            int[][] transitionMap = null;
            //get the appropriate transition map for the automata currently
            //being considered
            final int index = indexOfAutomaton(ap, mAutomata);
            if (index >= 0) {
              final KindTranslator translator = getKindTranslator();
              //transition maps are stored in different lists depending on the
              //kind of the automata
              if (translator.getComponentKind(ap) == ComponentKind.PLANT) {
                transitionMap = mPlantTransitionMap.get(index);
              } else if (translator.getComponentKind(ap) == ComponentKind.SPEC) {
                //specification automata begin to be indexed after all of the
                //plants in the list of automata
                transitionMap = mSpecTransitionMap.get(index - mNumPlants);
              }
            } else {
              throw new InvalidModelException("Cannot find automaton "
                                              + ap.getName());
            }

            //check every state in the current automaton and check
            //commutativity and exclusivity
            for (int k = 0; k < stateSet.size(); k++) {
              int targetIndex1;
              int targetIndex2;

              //check if both events are enabled in the current state and
              //store their targets
              if ((targetIndex1 = transitionMap[k][i]) > -1
                  && (targetIndex2 =
                    transitionMap[k][j]) > -1) {
                //when both events are enabled they commute iff when executed
                //in either sequence they result in the same state and they do
                //not disable one another. As soon as the two events are found
                //not to commute in any single automaton they will not commute
                //in the synchronous product unless they are found to be exclusive.
                commuting &=
                  transitionMap[targetIndex1][j] == transitionMap[targetIndex2][i]
                    && transitionMap[targetIndex1][j] != -1;
                //two events enabled in the same state are by definition not
                //exclusive in that automaton
                exclusive = false;
              }
            }
            //two events found to remain exclusive after checking all states
            //in any automaton where they both exist guarantees the
            //independence of those events in the synchronous product,
            //regardless of whether or not they commute in any or all automata
            if (exclusive) {
              eventDependencyMap[i][j] =
                PartialOrderEventDependencyKind.EXCLUSIVE;
              eventDependencyMap[j][i] =
                PartialOrderEventDependencyKind.EXCLUSIVE;
              break;
            }
          }
        }
        //if after checking all the states in which both events occur the
        //states are found to commute every time they are both enabled, then
        //those events will be independent in the synchronous product
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
    mDependencyWeightings = new int[mNumEvents];
    mReducedEventDependencyMap =
      new PartialOrderEventDependencyTuple[mNumEvents][];
    for (int i = 0; i < mNumEvents; i++) {
      final ArrayList<PartialOrderEventDependencyTuple> temp =
        new ArrayList<PartialOrderEventDependencyTuple>();
      for (int j = 0; j < mNumEvents; j++) {
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
      mDependencyWeightings[i] = temp.size();
    }
    numDependents/=2;
    mNumIndependentPairings = getTotalEventPairings() - numDependents;
    mComparator = new PartialOrderDependencyComparator(mDependencyWeightings);
  }

  @SuppressWarnings("unchecked")
  private List<AutomatonProxy>[] orderEvents(){
    final List<EventProxy> tempEventCodingList = new ArrayList<EventProxy>();
    final List<AutomatonProxy>[] automataContainingEvents = new ArrayList[mNumEvents];
    final EventKind kind = this instanceof PartialOrderComponentsConflictChecker ?
                              EventKind.PROPOSITION : EventKind.UNCONTROLLABLE;
    for (int i = 0; i < mNumEvents; i++){
      if (mEventCodingList.get(i).getKind() == kind)
        tempEventCodingList.add(0,mEventCodingList.get(i));
      else
        tempEventCodingList.add(mEventCodingList.get(i));
      automataContainingEvents[i] = new ArrayList<AutomatonProxy>();
    }
    mEventCodingList = tempEventCodingList;
    return automataContainingEvents;
  }

  protected abstract CounterExampleProxy noInitialCounterexample
    (AutomatonProxy ap,
     ProductDESProxy model,
     Collection<AutomatonProxy> automata);

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

  private int getTotalEventPairings(){
    return (mNumEvents * (mNumEvents - 1)) / 2;
  }

  protected void expand(final PartialOrderStateTuple current,
                       final int[] events,final boolean newState)
                                                     throws AnalysisException{
    int i;
    if(newState){
      mComponentStack.add(current);
    }
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
      PartialOrderStateTuple found = mStateSet.getOrAdd(mStateTuple);
      if (found == null) {
        found = mStateTuple;
      }
      mStack.add(new PartialOrderStateTuplePairing(found, current,
                                            PartialOrderParingRequest.VISIT));
      mStateTuple = new PartialOrderStateTuple(mStateTupleSize);
      if (mStateSet.size() > getNodeLimit()) {
        throw new OverflowException(getNodeLimit());
      } else {
        checkAbort();
      }
    }
  }

  protected WatersIntHeap enabled(final PartialOrderStateTuple current)
  {
    final WatersIntHeap heap = new WatersIntHeap(mComparator);
    mComparator.clearBits();
    decode(current,mSystemState);
    events:
    for (int i = 0; i < mNumEvents; i++){
      boolean selfLoop = true;
      for (int j = 0; j < mNumAutomata; j++){
        final boolean plant = j < mNumPlants;
        final int si = j - mNumPlants;
        if ((plant ? mPlantEventList.get(j)[i]:mSpecEventList.get(si)[i]) == 0){
          continue;
        }
        final int[][] transitionMap = plant ? mPlantTransitionMap.get(j) :
          mSpecTransitionMap.get(si);
        final int sourceState = mSystemState[j];
        final int targetState = transitionMap[sourceState][i];
        if(targetState == -1){
          continue events;
        }
        if(!plant){
          if(mDumpStates[si] == targetState){
            mErrorEvent = i;
            mErrorAutomaton = j;
            mErrorState = current;
            return null;
          }
        }
        selfLoop &= targetState == sourceState;
       // mSuccessor[j] = targetState;
      }
      if (selfLoop){
        continue events;
      }
      /*encode(mSuccessor,mStateTuple);
      if (mStateSet.contains(mStateTuple)){
        mComparator.reachesVisitedState(i);
      }*/
      heap.add(i);
    }
    current.setTotalSuccessors(heap.size());
    //mStateTuple = new PartialOrderStateTuple(mStateTupleSize);
    return heap;
  }

  protected int[] ample(final PartialOrderStateTuple current){
    final WatersIntHeap enabled = enabled(current);
    if (enabled == null){
      return null;
    }
    if (enabled.size() <= 1){
      current.setFullyExpanded(true);
     /* System.out.println(getEventName(enabled.peekFirst()));
      System.out.println("###########");*/
      return enabled.toArray();
    }

    final TIntArrayList ample = new TIntArrayList();
    final TIntHashSet ampleSet = new TIntHashSet();
    final TIntHashSet considered = new TIntHashSet();
    final TIntHashSet enabledSet = new TIntHashSet(enabled.toArray());
    int i,j;

    ample:
    while(!enabled.isEmpty()){
      final BitSet independents = new BitSet(mNumEvents);
      final int currentEvent = enabled.removeFirst();
      if (considered.contains(currentEvent)){
        continue;
      }
      final TIntArrayList dependentNonEnabled = new TIntArrayList();
      ample.add(currentEvent);
      ampleSet.add(currentEvent);
      independents.set(currentEvent);
      for (j = 0; j < ample.size(); j++) {
        final int ampleEvent = ample.get(j);
        if (!considered.contains(ampleEvent)) {
          for (final PartialOrderEventDependencyTuple t :
            mReducedEventDependencyMap[ampleEvent]) {
            final int dependentCandidate = t.getCoupling();
            if(!ampleSet.contains(dependentCandidate)){
              if (enabledSet.contains(dependentCandidate)) {
                ample.add(dependentCandidate);
                if(ample.size() == enabledSet.size()){
                  current.setFullyExpanded(ample.size() == enabledSet.size());
                  /*for (int g = 0; g < ample.size(); g++){
                    System.out.println(getEventName(ample.get(g)));
                  }
                  System.out.println("###########");*/
                  return ample.toArray();
                }
                ampleSet.add(dependentCandidate);
              }
              else {
                dependentNonEnabled.add(dependentCandidate);
              }

              independents.set(dependentCandidate);
            }
          }
          considered.add(ampleEvent);
        }
      }
      if(ample.size()+dependentNonEnabled.size()==mNumEvents){
        /*for (int t = 0; t < ample.size(); t++){
          System.out.println(getEventName(ample.get(t)));
        }
        System.out.println("###########");*/
        return ample.toArray();
      }
      boolean danger = false;
      for (j = 0; j < dependentNonEnabled.size(); j++){
        final int dependent = dependentNonEnabled.get(j);
        for (i = 0; i < mAutomata.length; i++){
          final boolean plant = i < mNumPlants;
          final byte[] eventList = plant ? mPlantEventList.get(i) :
                          mSpecEventList.get(i - mNumPlants);
          if (eventList[dependent] == 1){
            if (canBecomeEnabled(current,dependent,independents,i)){
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
      current.setFullyExpanded(ample.size() == enabledSet.size());
      mNumReducedSets += ample.size() < enabledSet.size() ? 1 : 0;
      /*for (int t = 0; t < ample.size(); t++){
        System.out.println(getEventName(ample.get(t)));
      }
      System.out.println("###########");*/

      return ample.toArray();
    }
    current.setFullyExpanded(true);

    final int[] enabledArray = enabledSet.toArray();
    /*for (final int t : enabledArray){
      System.out.println(getEventName(t));
    }
    System.out.println("###########");*/
    return enabledArray;
  }

  private boolean canBecomeEnabled(final PartialOrderStateTuple current,
                                   final int dependent, final BitSet independents,
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
      if (!independents.get(e)){
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

  protected void convertToBreadthFirst(){
    mStateList = new ArrayList<PartialOrderStateTuple>();
    mStateList.add(mInitialState);
    mInitialState.setVisited(true);
    int open = 0;
    mIndexList.add(open);
    mIndexList.add(mStateList.size());
    mStateTuple = new PartialOrderStateTuple(mStateTupleSize);
    if (this instanceof PartialOrderComponentsConflictChecker)
      mErrorState = mInitialState;

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
          if (isErrorState(tuple)){
            return;
          }
        }
      }
      if (open == mIndexList.get(mIndexList.size() - 1)){
        mIndexList.add(mStateList.size());
      }
    }
  }


  //#########################################################################
  //# Abstract methods
  protected boolean isSupportedEvent(final EventProxy event)
    throws EventNotFoundException
  {
    final KindTranslator translator = getKindTranslator();
    return translator.getEventKind(event) != EventKind.PROPOSITION;
  }

  protected abstract boolean isValid(final int[] sState)
    throws AnalysisException;

  protected abstract boolean isErrorState(final PartialOrderStateTuple current);

  protected abstract CounterExampleProxy computePOCounterExample()
    throws AnalysisAbortException;

  protected abstract int[][] setupTransitions(List<StateProxy> codes,
                                              ComponentKind kind);


  //#########################################################################
  //# Encoding and Decoding
  /**
   * Encode the synchronous product into StateTuple
   *
   * @param sState
   *          The state to be encoded
   * @param sTuple
   *          The encoded StateTuple
   */
  void encode(final int[] sState, final PartialOrderStateTuple sTuple)
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


  /**
   * Decode the StateTuple
   *
   * @param sTuple
   *          The StateTuple to be decoded
   * @param state
   *          The decoded state
   */
  void decode(final PartialOrderStateTuple sTuple, final int[] state)
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


  EventProxy getEvent(final int e)
  {
    return mEventCodingList.get(e);
  }


  //#########################################################################
  //# Debugging
  @SuppressWarnings("unused")
  private String dumpDecodedState(final PartialOrderStateTuple tuple)
  {
    final int[] decoded = new int[mNumAutomata];
    decode(tuple, decoded);
    final StringBuilder buffer = new StringBuilder();
    for (int a = 0; a < mNumAutomata; a++) {
      final AutomatonProxy aut = mAutomata[a];
      buffer.append(aut.getName());
      buffer.append(": ");
      final int s = decoded[a];
      final List<StateProxy> states =
        new ArrayList<StateProxy>(aut.getStates());
      final StateProxy state = states.get(s);
      buffer.append(state.getName());
      buffer.append("\n");
    }
    return buffer.toString();
  }


  //#########################################################################
  //# Data Members
  // Ample conditions
  private PartialOrderEventDependencyTuple[][] mReducedEventDependencyMap;
  private int[] mDependencyWeightings;
  private PartialOrderDependencyComparator mComparator;

  // Transition map
  protected List<int[][]> mPlantTransitionMap;
  protected List<int[][]> mSpecTransitionMap;

  // Level states storage
  protected TIntArrayList mIndexList;
  protected List<PartialOrderStateTuple> mStateList;
  private TIntArrayList mEnabledUnionList;

  //Stacks and sets
  protected List<PartialOrderStateTuplePairing> mStack;
  protected List<PartialOrderStateTuple> mComponentStack;
  protected StateHashSet<PartialOrderStateTuple> mStateSet;
  protected int mDepthIndex;
  private TIntHashSet mLocalSet;

  // For encoding/decoding
  protected AutomatonProxy[] mAutomata;
  private List<EventProxy> mEventCodingList;
  protected List<byte[]> mPlantEventList;
  protected List<byte[]> mSpecEventList;
  private int[] mBitLengthList;
  private int[] mMaskList;
  private int[] mCodePosition;
  protected PartialOrderStateTuple mStateTuple;
  private List<int[]> mPlantEventHash;
  private List<int[]> mSpecEventHash;
  protected int[] mDumpStates;

  // Size
  protected int mNumAutomata;
  protected int mNumEvents;
  protected int mNumPlants;
  protected int mStateTupleSize;

  // For computing successor and counterexample
  protected PartialOrderStateTuple mInitialState;
  protected int[] mSystemState;
  protected int[] mSuccessor;
  protected PartialOrderStateTuple mErrorState;
  protected int mErrorEvent;
  protected int mErrorAutomaton;

  //Statistics
  @SuppressWarnings("unused")
  private int mNumIndependentPairings;
  protected int mComponentCount;
  protected int mFullExpansions;
  @SuppressWarnings("unused")
  private int mNumReducedSets;
}
