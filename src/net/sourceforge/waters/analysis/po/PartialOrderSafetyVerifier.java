//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.analysis.monolithic
//# CLASS:   MonolithicSafetyVerifier
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.analysis.po;

import gnu.trove.THashSet;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.Arrays;

import net.sourceforge.waters.analysis.monolithic.BlockedArrayList;
import net.sourceforge.waters.analysis.monolithic.StateTuple;
import net.sourceforge.waters.model.analysis.AbortException;
import net.sourceforge.waters.model.analysis.AbstractSafetyVerifier;
import net.sourceforge.waters.model.analysis.AnalysisException;
import net.sourceforge.waters.model.analysis.InvalidModelException;
import net.sourceforge.waters.model.analysis.KindTranslator;
import net.sourceforge.waters.model.analysis.NondeterministicDESException;
import net.sourceforge.waters.model.analysis.OverflowException;
import net.sourceforge.waters.model.analysis.SafetyDiagnostics;
import net.sourceforge.waters.model.analysis.SafetyVerifier;
import net.sourceforge.waters.model.analysis.VerificationResult;
import net.sourceforge.waters.model.des.EventProxy;
import net.sourceforge.waters.model.des.ProductDESProxy;
import net.sourceforge.waters.model.des.ProductDESProxyFactory;
import net.sourceforge.waters.model.des.SafetyTraceProxy;
import net.sourceforge.waters.model.des.StateProxy;
import net.sourceforge.waters.model.des.AutomatonProxy;
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
 * @author Jinjian Shi
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
  }

  //#########################################################################
  //# Invocation

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
      mStateSpace = new BlockedArrayList<StateTuple>(StateTuple.class);
      // TODO Order events so uncontrollables are first.
      mEventCodingList = new ArrayList<EventProxy>(model.getEvents());
      mPlantEventList = new ArrayList<byte[]>();
      mSpecEventList = new ArrayList<byte[]>();

      mNumEvents = mEventCodingList.size();
      mNumAutomata = automata.size();
      mAutomata = new AutomatonProxy[mNumAutomata];

      // Empty case
      if (mNumAutomata == 0) {
        return setSatisfiedResult();
      }

      mBitLengthList = new int[mNumAutomata];
      mMaskList = new int[mNumAutomata];
      mCodePosition = new int[mNumAutomata];
      mSystemState = new int[mNumAutomata];

      final List<AutomatonProxy>[] automataContainingEvents = new ArrayList[mNumEvents];
      Arrays.fill(automataContainingEvents,new ArrayList<AutomatonProxy>());

      // Separate the automatons by kind
      AutomatonProxy initUncontrollable = null;
      for (final AutomatonProxy ap : automata) {
        // Get all states
        stateSet = ap.getStates();
        // Encoding states to binary values
        final List<StateProxy> codes = new ArrayList<StateProxy>(stateSet);
        // Encoding events to binary values
        final byte[] aneventCodingList = new byte[mNumEvents];
        for (final EventProxy evp : ap.getEvents()) {
          final int eventIndex = mEventCodingList.indexOf(evp);
          aneventCodingList[eventIndex] = 1;
          automataContainingEvents[eventIndex].add(ap);
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
      mEventDependencyMap =
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
                mEventDependencyMap[i][j] =
                  PartialOrderEventDependencyKind.EXCLUSIVE;
                break;
              }
            }
          }
          //if after checking all the states in which both events occur the states are found to commute every time they are both enabled, then those events will be
          //independent in the synchronous product
          if (commuting) {
            if (mEventDependencyMap[i][j] !=
              PartialOrderEventDependencyKind.EXCLUSIVE) {
              mEventDependencyMap[i][j] =
                PartialOrderEventDependencyKind.COMMUTING;
            }
          }
        }
      }

      mReducedEventDependencyMap =
        new PartialOrderEventDependencyTuple[mNumEvents][];
      for (i = 0; i < mNumEvents; i++) {
        final ArrayList<PartialOrderEventDependencyTuple> temp =
          new ArrayList<PartialOrderEventDependencyTuple>();
        for (j = 0; j < mNumEvents; j++) {
          if (mEventDependencyMap[i][j] != PartialOrderEventDependencyKind.NONCOMMUTING) {
            temp.add(new PartialOrderEventDependencyTuple
                     (j, mEventDependencyMap[i][j]));
          }
        }
        mReducedEventDependencyMap[i] =
          temp.toArray(new PartialOrderEventDependencyTuple[temp.size()]);
      }

      // Compute stuttering of events
      // Set up initial conditions, all events labelled as stuttering
      setEnablings();

      //Non-stuttering if an event can take the system from a controllable
      //state to an uncontrollable state. Controllability of states depends
      //on the kinds of events (uncontrollable/controllable) that are enabled
      //in them and if they belong to a plant or spec automaton

      //Iterate over all uncontrollable events
      for (i = 0; i < mNumEvents; i++){
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
      }


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

      if (isControllable(mSystemState)) {
        return setSatisfiedResult();
      } else {
        final SafetyTraceProxy counterexample = computeCounterExample();
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
      final int size = mAutomata[j].getStates().size();
      //Each event can be enabled in any number of states initially so loop
      //over each state
      for (int i = 0; i < size; i++){
        //The index of the following loop will be the index of the event that
        //the enabling is being computed for
        for (int k = 0; k < mNumEvents; k++){
          //find the target state for the transition involving the currently
          //visited state and the event being considered for enablings
          final int target = transitionMap[i][k];
          if (target != -1){
            //if such a target exists then the event is enabled in that state
            //so now check all other uncontrollable events to see if they are
            //enabled or disabled in that target state and record the information
            //in the current partial order event
            for (int l = 0; l < mNumEvents; l++){
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

  //#########################################################################
  //# Interface net.sourceforge.waters.model.analysis.ModelAnalyser
  public boolean supportsNondeterminism()
  {
    return false;
  }

  //#########################################################################
  //# Interface net.sourceforge.waters.model.analysis.ModelVerifier
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
    super.addStatistics();
    final VerificationResult result = getAnalysisResult();
    final int numstates = mStateSpace.size();
    result.setNumberOfAutomata(mNumAutomata);
    result.setNumberOfStates(numstates);
    result.setPeakNumberOfNodes(numstates);
  }

  //#########################################################################
  //# Auxiliary Methods
  /**
   * Check the controllability of the model with a parameter of initial
   * synchronous product.
   *
   * @param sState
   *          The initial synchronous product of the model
   * @return <CODE>true</CODE> if the model is controllable, or
   *         <CODE>false</CODE> if it is not.
   */
  private boolean isControllable(final int[] sState) throws AnalysisException
  {
    final KindTranslator translator = getKindTranslator();
    final THashSet<StateTuple> systemSet = new THashSet<StateTuple>();
    boolean enabled = true;

    // Add the initial synchronous product in systemSet and mStateSpace
    mSuccessor = new int[mNumAutomata];
    mStateTuple = new StateTuple(mStateTupleSize);
    encode(sState, mStateTuple);
    systemSet.add(mStateTuple);
    mStateSpace.add(mStateTuple);
    mIndexList.add(mStateSpace.size() - 1);

    int indexSize = 0;
    final int mNumEvents = mEventCodingList.size();
    int i, j, k, temp;

    while (true) {
      // For each current state in the current level, check its controllability
      indexSize = mIndexList.size();
      for (j = (indexSize == 1) ? 0 : (mIndexList.get(indexSize - 2) + 1); j <= mIndexList
        .get(indexSize - 1); j++) {
        decode(mStateSpace.get(j), mSystemState);
        for (int e = 0; e < mNumEvents; e++) {
          // Retrieve all enabled events
          enabled = true;
          for (i = 0; i < mNumPlants; i++) {
            if (mPlantEventList.get(i)[e] == 1) {
              temp = mPlantTransitionMap.get(i)[mSystemState[i]][e];
              if (temp == -1) {
                enabled = false;
                break;
              } else if (temp > -1) {
                mSuccessor[i] = temp;
                continue;
              }
            }
            mSuccessor[i] = mSystemState[i];
          }
          if (!enabled) {
            continue;
          }

          // Check controllability of current state
          final EventProxy event = mEventCodingList.get(e);
          final EventKind kind = translator.getEventKind(event);
          if (kind == EventKind.UNCONTROLLABLE) {
            for (i = 0; i < mNumAutomata - mNumPlants; i++) {
              final int si = i + mNumPlants;
              if (mSpecEventList.get(i)[e] == 1) {
                temp = mSpecTransitionMap.get(i)[mSystemState[si]][e];
                if (temp == -1) {
                  mErrorEvent = e;
                  mErrorAutomaton = si;
                  return false;
                }
                if (temp > -1) {
                  mSuccessor[si] = temp;
                  continue;
                }
              }
              mSuccessor[si] = mSystemState[si];
            }
          } else {
            for (k = 0; k < mNumAutomata - mNumPlants; k++) {
              if (mSpecEventList.get(k)[e] == 1) {
                temp =
                  mSpecTransitionMap.get(k)[mSystemState[k + mNumPlants]][e];
                if (temp == -1) {
                  enabled = false;
                  break;
                }
                if (temp > -1) {
                  mSuccessor[k + mNumPlants] = temp;
                  continue;
                }
              }
              mSuccessor[k + mNumPlants] = mSystemState[k + mNumPlants];
            }
            if (!enabled) {
              continue;
            }
          }

          // Encode the new system state and put it into mStateSpace
          mStateTuple = new StateTuple(mStateTupleSize);
          encode(mSuccessor, mStateTuple);
          if (systemSet.add(mStateTuple)) {
            mStateSpace.add(mStateTuple);
            if (mStateSpace.size() > getNodeLimit()) {
              throw new OverflowException(getNodeLimit());
            } else {
              checkAbort();
            }
          }
        }
      }
      // If mStateSpace has added a new state, update mIndexList at the last
      // loop of current level
      if (mStateSpace.size() != mIndexList.get(indexSize - 1) + 1) {
        mIndexList.add(mStateSpace.size() - 1);
      } else {
        break;
      }
    }
    return true;
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
  private void encode(final int[] sState, final StateTuple sTuple)
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
  private void decode(final StateTuple sTuple, final int[] state)
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

  /**
   * Gets a counterexample if the model was found to be not controllable.
   * representing a controllability error trace. A controllability error trace
   * is a nonempty sequence of events such that all except the last event in
   * the list can be executed by the model. The last event in list is an
   * uncontrollable event that is possible in all plant automata, but not in
   * all specification automata present in the model. Thus, the last step
   * demonstrates why the model is not controllable.
   *
   * @return A trace object representing the counterexample. The returned
   *         trace is constructed for the input product DES of this
   *         controllability checker and shares its automata and event
   *         objects.
   * @throws IllegalStateException
   *           if this method is called before model checking has completed,
   *           i.e., before {@link #run() run()} has been called, or model
   *           checking has found that the property is satisfied and there is
   *           no counterexample.
   */
  private SafetyTraceProxy computeCounterExample() throws AbortException
  {
    final ProductDESProxyFactory factory = getFactory();
    final ProductDESProxy des = getModel();
    final List<TraceStepProxy> steps = new LinkedList<TraceStepProxy>();

    boolean enabled;
    boolean found = false;
    int i, j, k, temp;
    int indexSize = mIndexList.size();
    final int[] errorState = new int[mNumAutomata];
    final EventProxy event0 = mEventCodingList.get(mErrorEvent);
    final AutomatonProxy aut = mAutomata[mErrorAutomaton];
    final List<StateProxy> codes0 =
      new ArrayList<StateProxy>(aut.getStates());
    final int code0 = mSystemState[mErrorAutomaton];
    final StateProxy state0 = codes0.get(code0);
    final TraceStepProxy step0 = factory.createTraceStepProxy(event0);
    steps.add(0, step0);

    while (true) {
      for (i = 0; i < mNumAutomata; i++) {
        errorState[i] = mSystemState[i];
      }
      mIndexList.remove(--indexSize);
      if (mIndexList.size() == 0)
        break;
      // Backward search the previous level states, compute their
      // successors and compare them with the error state
      for (j = indexSize == 1 ? 0 : mIndexList.get(indexSize - 2) + 1; j <= mIndexList
        .get(indexSize - 1); j++) {
        decode(mStateSpace.get(j), mSystemState);
        for (int e = 0; e < mNumEvents; e++) {
          enabled = true;
          for (i = 0; i < mNumPlants; i++) {
            if (mPlantEventList.get(i)[e] == 1) {
              temp = mPlantTransitionMap.get(i)[mSystemState[i]][e];
              if (temp == -1) {
                enabled = false;
                break;
              } else if (temp > -1) {
                mSuccessor[i] = temp;
                continue;
              }
            }
            mSuccessor[i] = mSystemState[i];
          }
          if (!enabled) {
            continue;
          }
          for (k = 0; k < mNumAutomata - mNumPlants; k++) {
            if (mSpecEventList.get(k)[e] == 1) {
              temp =
                mSpecTransitionMap.get(k)[mSystemState[k + mNumPlants]][e];
              if (temp == -1) {
                enabled = false;
                break;
              }
              if (temp > -1) {
                mSuccessor[k + mNumPlants] = temp;
                continue;
              }
            }
            mSuccessor[k + mNumPlants] = mSystemState[k + mNumPlants];
          }
          if (!enabled) {
            continue;
          }
          if (Arrays.equals(mSuccessor, errorState)) {
            found = true;
            final EventProxy event = mEventCodingList.get(e);
            final TraceStepProxy step = factory.createTraceStepProxy(event);
            steps.add(0, step);
            break;
          }
        }
        if (found) {
          found = false;
          break;
        }
        checkAbort();
      }
    }
    final TraceStepProxy init = factory.createTraceStepProxy(null);
    steps.add(0, init);
    final String tracename = getTraceName();
    final String comment = getTraceComment(event0, aut, state0);
    final List<AutomatonProxy> automata = Arrays.asList(mAutomata);
    final SafetyTraceProxy trace =
      factory.createSafetyTraceProxy(tracename, comment, null, des, automata,
                                     steps);
    return trace;
  }

  //#########################################################################
  //# Data Members

  // Ample conditions
  private PartialOrderEventDependencyKind[][] mEventDependencyMap;
  private PartialOrderEventDependencyTuple[][] mReducedEventDependencyMap;

  //Event information
  private PartialOrderEvent[] mPartialOrderEvents;

  // Transition map
  private List<int[][]> mPlantTransitionMap;
  private List<int[][]> mSpecTransitionMap;

  // Level states storage
  private List<Integer> mIndexList;
  private List<StateTuple> mStateSpace;

  // For encoding/decoding
  private AutomatonProxy[] mAutomata;
  private List<EventProxy> mEventCodingList;
  private List<byte[]> mPlantEventList;
  private List<byte[]> mSpecEventList;
  private int[] mBitLengthList;
  private int[] mMaskList;
  private int[] mCodePosition;
  private StateTuple mStateTuple;

  // Size
  private int mNumAutomata;
  private int mNumEvents;
  private int mNumPlants;
  private int mStateTupleSize;

  // For computing successor and counterexample
  private int[] mSystemState;
  private int[] mSuccessor;
  private int mErrorEvent;
  private int mErrorAutomaton;

}
