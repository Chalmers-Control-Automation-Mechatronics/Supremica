//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.analysis.monolithic
//# CLASS:   ControlLoopChecker
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.analysis.monolithic;

import java.util.List;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Set;
import java.util.HashSet;

import net.sourceforge.waters.model.analysis.AbortException;
import net.sourceforge.waters.model.analysis.AbstractModelVerifier;
import net.sourceforge.waters.model.analysis.AnalysisException;
import net.sourceforge.waters.model.analysis.ControllabilityKindTranslator;
import net.sourceforge.waters.model.analysis.ControlLoopChecker;
import net.sourceforge.waters.model.analysis.KindTranslator;
import net.sourceforge.waters.model.analysis.OverflowException;
import net.sourceforge.waters.model.analysis.VerificationResult;
import net.sourceforge.waters.model.des.AutomatonProxy;
import net.sourceforge.waters.model.des.StateProxy;
import net.sourceforge.waters.model.des.EventProxy;
import net.sourceforge.waters.model.des.TransitionProxy;
import net.sourceforge.waters.model.des.ProductDESProxy;
import net.sourceforge.waters.model.des.ProductDESProxyFactory;
import net.sourceforge.waters.model.des.LoopTraceProxy;

import net.sourceforge.waters.xsd.base.ComponentKind;
import net.sourceforge.waters.xsd.base.EventKind;

/**
 * <P>A monolithic implementation of a control-loop checker.</P>
 *
 * <P>The {@link #run()} method of this model checker does a control-loop
 * check, and finds whether the given model is control-loop free.</P>
 *
 * @author Peter Yunil Park
 */

public class MonolithicControlLoopChecker
  extends AbstractModelVerifier
  implements ControlLoopChecker
{

  //#########################################################################
  //# Constructors
  public MonolithicControlLoopChecker(final ProductDESProxyFactory factory)
  {
    this(ControllabilityKindTranslator.getInstance(), factory);
  }

  public MonolithicControlLoopChecker(final KindTranslator translator,
                                      final ProductDESProxyFactory factory)
  {
    this(null, translator, factory);
  }

  public MonolithicControlLoopChecker(final ProductDESProxy model,
                                      final ProductDESProxyFactory factory)
  {
    this(model, ControllabilityKindTranslator.getInstance(), factory);
  }

  public MonolithicControlLoopChecker(final ProductDESProxy model,
                                      final KindTranslator translator,
                                      final ProductDESProxyFactory factory)
  {
    super(model, factory, translator);
  }


  //#########################################################################
  //# Invocation
  /**
   * Runs this control loop checker.
   * This method starts the model checking process on the model given
   * as parameter to the constructor of this object. On termination,
   * the result of checking the property is known and can be queried
   * using the {@link
   * net.sourceforge.waters.model.analysis.ModelVerifier#isSatisfied()
   * isSatisfied()} and {@link #getCounterExample()} methods.
   * @return <CODE>true</CODE> if the model is control-loop free, or
   *         <CODE>false</CODE> if it is not.
   *         The same value can be queried using the {@link
   *         net.sourceforge.waters.model.analysis.ModelVerifier#isSatisfied()
   *         isSatisfied()} method.
   */
  public boolean run()
    throws AnalysisException
  {
    try {
      setUp();
      // insert initial state tuple to global state and state list
      mGlobalStateSet.getOrAdd(mEncodedInitialStateTuple);
      mUnvisitedList.add(mEncodedInitialStateTuple);
      int counter = 0;
      while (true) {
        if (counter < mUnvisitedList.size()) {
          mEncodedCurrTuple = mUnvisitedList.get(counter++);
          if (mEncodedCurrTuple.getVisited() == false) {
            visit(mEncodedCurrTuple);
          }
        } else {
          break;
        }
      }
      if (mControlLoopFree) {
        return setSatisfiedResult();
      } else {
        final LoopTraceProxy counterexample = computeCounterExample();
        return setFailedResult(counterexample);
      }
    } finally {
      tearDown();
    }
  }


  //#########################################################################
  //# Interface net.sourceforge.waters.model.analysis.ControlLoopChecker
  public void setKindTranslator(final KindTranslator translator)
  {
    super.setKindTranslator(translator);
    clearAnalysisResult();
  }

  /**
   * Gets a counterexample if the model was found to be not control-loop free.
   * representing a control-loop error trace. A control-loop error
   * trace is a nonempty sequence of events that ends in a loop consisting of
   * controllable events only.
   * @return A trace object representing the counterexample.
   *         The returned trace is constructed for the input product DES
   *         of this control loop checker and shares its automata and
   *         event objects.
   * @throws IllegalStateException if this method is called before
   *         model checking has completed, i.e., before {@link #run()}
   *         has been called, or model checking has found that the
   *         property is satisfied and there is no counterexample.
   */
  public LoopTraceProxy getCounterExample()
  {
    return (LoopTraceProxy) super.getCounterExample();
  }


  //#########################################################################
  //# Setting the Result
  protected void addStatistics(final VerificationResult result)
  {
    final int numstates = mGlobalStateSet.size();
    result.setNumberOfAutomata(mNumAutomata);
    result.setNumberOfStates(numstates);
    result.setPeakNumberOfNodes(numstates);
  }


  //#########################################################################
  //# Algorithm
  protected void setUp()
  {
    super.setUp();
    final ProductDESProxy des = getModel();
    final KindTranslator translator = getKindTranslator();

    mControlLoopFree = true;
    mAutomataList = new ArrayList<AutomatonProxy>();
    mEventList = new ArrayList<EventProxy>();
    mTransitionList = new ArrayList<ArrayList<TransitionProxy>>();

    // create Automaton list
    for (final AutomatonProxy aProxy : des.getAutomata()) {
      final ComponentKind kind = translator.getComponentKind(aProxy);
      switch (kind) {
      case PLANT:
      case SPEC:
        mAutomataList.add(aProxy);
        break;
      default:
        break;
      }
    }

    // create Event list: order controllable first, uncontrollable last
    // list only controllable and uncontrollable events
    mNumConEvent = 0;
    for (final EventProxy eProxy: des.getEvents()) {
      final EventKind kind = translator.getEventKind(eProxy);
      switch (kind) {
      case CONTROLLABLE:
        // controllable event: put it in the beginning of the list
        mEventList.add(0, eProxy);
        mNumConEvent++;
        break;
      case UNCONTROLLABLE:
        // uncontrollable event: put it in the end of the list
        mEventList.add(eProxy);
        break;
      default:
        break;
      }
    }

    // get number of automata
    mNumAutomata = mAutomataList.size();
    // get number of events
    mNumEvent = mEventList.size();
    // get encoding information
    mNumBits = new int[mNumAutomata];
    mNumBitsMasks = new int[mNumAutomata];

    mNumInts = 1;
    int totalBits = SIZE_INT;
    int counter = 0;
    for (final AutomatonProxy aProxy: mAutomataList) {
      final int bits = getBitLength(aProxy);
      mNumBits[counter] = bits;
      mNumBitsMasks[counter] = (1 << bits) - 1;
      if (totalBits >= bits) { // if current buffer can store this automaton
        totalBits -= bits;
      }
      else {
        mNumInts++;
        totalBits = SIZE_INT;
      }
      counter++;
    }

    // get index
    counter = 0;
    totalBits = SIZE_INT;
    mIndexAutomata = new int[mNumInts + 1];
    mIndexAutomata[0] = counter++;
    for (int i = 0; i < mNumAutomata; i++) {
      if (totalBits >= mNumBits[i]) {
        totalBits -= mNumBits[i];
      }
      else {
        mIndexAutomata[counter++] = i;
        totalBits = SIZE_INT;
      }
    }
    mIndexAutomata[mNumInts] = mNumAutomata;

    // create Transition list
    for (final AutomatonProxy aProxy: mAutomataList) {
      final ArrayList<TransitionProxy> tmpTran =
        new ArrayList<TransitionProxy>();
      for (final TransitionProxy tProxy: aProxy.getTransitions()) {
        tmpTran.add(tProxy);
      }
      mTransitionList.add(tmpTran);
    }

    // create global event map
    mGlobalEventMap = new boolean[mNumEvent];
    for (int i = 0; i < mNumEvent; i++) {
      final EventProxy event = mEventList.get(i);
      final EventKind kind = translator.getEventKind(event);
      if (kind == EventKind.CONTROLLABLE) {
        mGlobalEventMap[i] = true;
      }
    }

    // create maps
    mMap = new int[mNumEvent][mNumAutomata][];
    for (int i = 0; i < mNumEvent; i++) {
      for (int j = 0; j < mNumAutomata; j++) {
        mMap[i][j] = null;
      }
    }

    Set<StateProxy> stateSet;
    ArrayList<StateProxy> stateList;

    // create map
    int countAutomata = 0;
    int countEvent = 0;
    for (final AutomatonProxy aProxy: mAutomataList) {
      final int numState = aProxy.getStates().size();

      stateSet = aProxy.getStates();
      stateList = new ArrayList<StateProxy>(stateSet);

      for (final EventProxy eProxy: mEventList) {
        // find if the event is in current automaton
        final Set<EventProxy> eventSet = aProxy.getEvents();
        boolean eventAvailable = false;
        for (final EventProxy e: eventSet) {
          if (e.equals(eProxy)) {
            eventAvailable = true;
            break;
          }
        }

        if (eventAvailable) {
          mMap[countEvent][countAutomata] = new int[numState];
          for (int i = 0; i < numState; i++) {
            mMap[countEvent][countAutomata][i] = -1;
          }
        }

        countEvent++;
      }

      for (final TransitionProxy tProxy: mTransitionList.get(countAutomata)) {
        mMap[mEventList.indexOf(tProxy.getEvent())]
          [countAutomata][stateList.indexOf(tProxy.getSource())]
          = stateList.indexOf(tProxy.getTarget());
      }
      countEvent = 0;
      countAutomata++;
    }

    // create initial state tuple
    mInitialStateTuple = new int[mNumAutomata];
    int i = 0;
    for (final AutomatonProxy aProxy: mAutomataList) {
      int j = 0;
      for (final StateProxy sProxy: aProxy.getStates()) {
        if (sProxy.isInitial() == true) {
          mInitialStateTuple[i] = j;
          break;
        }
        j++;
      }
      i++;
    }

    // set a buffer for storing current state tuple
    mCurrTuple = new int[mNumAutomata];
    // set a buffer for storing next state tuple
    mNextTuple = new int[mNumAutomata];
    // set the initial state tuple
    mEncodedInitialStateTuple =
      new EncodedStateTuple(encode(mInitialStateTuple));
    // initialise state tuple list
    mGlobalStateSet = new StateHashSet(SIZE_BUFFER);
    mUnvisitedList = new ArrayList<EncodedStateTuple>(SIZE_BUFFER);
  }


  /**
   * This method visits each state tuple in the synchronous product.
   * If it tries to visit state tuple that has been visited before,
   * it detects a loop.
   * @param encodedCurrTuple current state tuple property
   */
  private void visit(final EncodedStateTuple encodedCurrTuple)
    throws AnalysisException
  {
    checkAbort();
    // new memory allocation to store current state tuple
    final int currTuple[] = new int[mNumAutomata];
    encodedCurrTuple.setVisited(true);
    // current state tuple now in currTuple
    decode(encodedCurrTuple.getCodes(), currTuple);
    for (int i = 0; i < mNumEvent; i++) { // for all events
      if (eventAvailable(currTuple, i)) {
        if (mGlobalEventMap[i]) { // CONTROLLABLE
          EncodedStateTuple encodedNextTuple =
            new EncodedStateTuple(encode(mNextTuple));
          if (addState(encodedNextTuple)) {
            visit(encodedNextTuple);
            if (!mControlLoopFree) {
              return;
            }
          }
          else {
            encodedNextTuple = mGlobalStateSet.get(encodedNextTuple);
            if (encodedNextTuple.getVisited() == false) {
              visit(encodedNextTuple);
              if (!mControlLoopFree) {
                return;
              }
            }
          }

          if (encodedNextTuple.getInComponent() == false) {
            if (encodedNextTuple.getVisited() == true) {
              // control loop detected here
              if (mControlLoopFree) {
                mControlLoopFree = false;
                mEncodedRootStateTuple = encodedCurrTuple;
              }
              return;
            }
          }
        }
        else { // UNCONTROLLABLE
          final EncodedStateTuple encodedNextTuple =
            new EncodedStateTuple(encode(mNextTuple));
          addState(encodedNextTuple);
        }
      }
    }
    encodedCurrTuple.setInComponent(true);
  }

  /**
   * Checks whether a state is new, and if so adds it to the global state set.
   * @return <CODE>true</CODE> if the given state was found to be new and has
   *         been added to the state space, <CODE>false</CODE> otherwise.
   * @throws OverflowException to indicate that the state limit has been
   *         exceeded.
   */
  private boolean addState(final EncodedStateTuple tuple)
    throws OverflowException
  {
    if (mGlobalStateSet.getOrAdd(tuple) == null) {
      mUnvisitedList.add(tuple);
      if (mGlobalStateSet.size() >= getNodeLimit()) {
        throw new OverflowException(getNodeLimit());
      }
      return true;
    } else {
      return false;
    }
  }

  /**
   * It checks event is available from current state tuple.
   * @param currTuple current state tuple
   * @param event current event index
   * @return <CODE>null</CODE> if event is not available from current event,
   *         or return next state tuple
   */
  private boolean eventAvailable(final int currTuple[], final int event)
  {
    int temp;

    for (int i = 0; i < mNumAutomata; i++) {
      final int map[] = mMap[event][i];
      if (map != null) {
        temp = map[currTuple[i]];
        if (temp > -1) { // next state exists
          mNextTuple[i] = temp;
        }
        else { // event is not available
          return false;
        }
      }
      else { // event is not in the automaton
        mNextTuple[i] = currTuple[i];
      }
    }

    return true;
  }

  private LoopTraceProxy computeCounterExample()
    throws AbortException
  {
    final ProductDESProxyFactory factory = getFactory();
    final ProductDESProxy des = getModel();
    final String desname = des.getName();
    final String tracename = desname + "-loop";
    final List<EventProxy> tracelist = new LinkedList<EventProxy>();

    /* FIND COUNTEREXAMPLE TRACE HERE */
    /* Counterexample = The shortest path from mInitialStateTuple to
     *                  mRootStateTuple
     *                  +
     *                  The shortest path from mRootStateTuple
     *                  to mRootStateTuple
     */
    // find a shortest path from mRootStateTuple to mRootStateTuple:
    // only for controllable events
    final Set<TransitionProperty> loopStates =
      new HashSet<TransitionProperty>();
    List<EncodedStateTuple> list = new LinkedList<EncodedStateTuple>();
    Set<EncodedStateTuple> set = new HashSet<EncodedStateTuple>();
    ArrayList<Integer> indexList = new ArrayList<Integer>();
    EncodedStateTuple encodedCurrTuple = new EncodedStateTuple(mNumInts);
    int lastEvent = -1;
    list.add(mEncodedRootStateTuple);
    indexList.add(0);
    int indexSize = 0;
    decode(mEncodedRootStateTuple.getCodes(), mCurrTuple);
    int target[] = mCurrTuple; // target is mEncodedRootStateTuple (decoded)
    mCurrTuple = new int[mNumAutomata];

    loop:
    while (true) {
      indexSize = indexList.size();
      for (int i = (indexSize==1) ? 0 : (indexList.get(indexSize-2)+1);
           i <= indexList.get(indexSize-1); i++) {
        checkAbort();
        encodedCurrTuple = list.get(i);
        decode(encodedCurrTuple.getCodes(), mCurrTuple);
        for (int j = 0; j < mNumConEvent; j++) {
          if (mGlobalEventMap[j]) {
            if (eventAvailable(mCurrTuple, j)) {
              if (compare(mNextTuple, target)) {
                lastEvent = j;
                break loop;
              }
              final EncodedStateTuple encodedNextTuple =
                new EncodedStateTuple(encode(mNextTuple));
              if (set.add(encodedNextTuple)) {
                list.add(encodedNextTuple);
              }
            }
          }
        }
      }
      if (list.size() != (indexList.get(indexSize-1)+1)) {
        indexList.add(list.size()-1);
      } else {
        break;
      }
    }
    if (indexList.size() == 1) { // single cycle loop
      loopStates.add(new TransitionProperty(mEncodedRootStateTuple,
                                            mEncodedRootStateTuple,
                                            lastEvent));
    } else {
      loopStates.add(new TransitionProperty(encodedCurrTuple,
                                            mEncodedRootStateTuple,
                                            lastEvent));
      // swap memory location: mCurrTuple <-> target
      int tmp[] = mCurrTuple;
      mCurrTuple = target;
      target = tmp;

      for (int i = indexList.size()-2; i >= 0; i--) {
        checkAbort();
        final int start =
            (indexList.get(i) == 0) ? 0 : indexList.get(i - 1) + 1;
        final int end = indexList.get(i);
        next:
        for (int j = start; j <= end; j++) {
          final EncodedStateTuple encodedCurr = list.get(j);
          decode(encodedCurr.getCodes(), mCurrTuple);

          for (int k = 0; k < mNumConEvent; k++) {
            if (mGlobalEventMap[k]) {
              if (eventAvailable(mCurrTuple, k)) {
                final EncodedStateTuple encodedNext =
                    new EncodedStateTuple(encode(mNextTuple));
                if (compare(mNextTuple, target)) {
                  loopStates.add(new TransitionProperty(encodedCurr,
                      encodedNext, k));
                  // swap memory location: mCurrTuple <-> target
                  tmp = mCurrTuple;
                  mCurrTuple = target;
                  target = tmp;
                  break next;
                }
              }
            }
          }
        }
      }
    }

    // find a shortest path from mInitialStateTuple to mRootStateTuple:
    // for both controllable events and uncontrollable events
    // if mInitialStateTuple != mRootStateTuple
    if (!mEncodedInitialStateTuple.equals(mEncodedRootStateTuple)) {
      list = new LinkedList<EncodedStateTuple>();
      set = new HashSet<EncodedStateTuple>();
      indexList = new ArrayList<Integer>();
      lastEvent = -1;
      list.add(mEncodedInitialStateTuple);
      indexList.add(0);
      indexSize = 0;

      loop2:
      while (true) {
        indexSize = indexList.size();
        for (int i = (indexSize==1) ? 0 : (indexList.get(indexSize-2)+1);
             i <= indexList.get(indexSize-1); i++) {
          encodedCurrTuple = list.get(i);
          decode(encodedCurrTuple.getCodes(), mCurrTuple);
          for (int j = 0; j < mNumEvent; j++) {
            if (eventAvailable(mCurrTuple, j)) {
              final EncodedStateTuple encodedNextTuple =
                new EncodedStateTuple(encode(mNextTuple));
              if (isInLoop(encodedNextTuple, loopStates)) {
                tracelist.add(0, mEventList.get(j));
                // swap memory location: mCurrTuple <-> target
                final int tmp[] = mCurrTuple;
                mCurrTuple = target;
                target = tmp;
                mEncodedRootStateTuple = encodedNextTuple;
                // now change the root of the loop
                break loop2;
              }
              if (set.add(encodedNextTuple)) {
                list.add(encodedNextTuple);
              }
            }
          }
        }

        if (list.size() != (indexList.get(indexSize-1)+1)) {
          indexList.add(list.size()-1);
        } else {
          break;
        }
      }

      // Add to tracelist here
      for (int i = indexList.size()-2; i >= 0; i--) {
        final int start = (indexList.get(i)==0)?0:indexList.get(i-1)+1;
        final int end = indexList.get(i);

        next2:
        for (int j = start; j <= end; j++) {
          final EncodedStateTuple encodedCurr = list.get(j);
          decode(encodedCurr.getCodes(), mCurrTuple);
          for (int k = 0; k < mNumEvent; k++) {
            if (eventAvailable(mCurrTuple, k)) {
              if (compare(mNextTuple, target)) {
                tracelist.add(0, mEventList.get(k));
                // swap memory location: mCurrTuple <-> target
                final int tmp[] = mCurrTuple;
                mCurrTuple = target;
                target = tmp;
                break next2;
              }
            }
          }
        }
      }
    }

    final int loopIndex = tracelist.size();
    while (loopStates.size() > 0) {
      for (final TransitionProperty tp: loopStates) {
        if (compare(mEncodedRootStateTuple.getCodes(),
		    tp.getSourceTuple().getCodes())) {
          tracelist.add(mEventList.get(tp.getEvent()));
          mEncodedRootStateTuple = tp.getTargetTuple();
          loopStates.remove(tp);
          break;
        }
      }
    }
    final LoopTraceProxy trace =
      factory.createLoopTraceProxy(tracename, des, tracelist, loopIndex);
    return trace;
  }

  /**
   * It checks current state tuple is in the control loop
   * @param encodedCurrTuple encoded current state tuple
   * @param loopStates transitions in the control loop
   * @return return true if state tuple is in the loop, false otherwise
   */
  private boolean isInLoop(final EncodedStateTuple encodedCurrTuple,
                           final Set<TransitionProperty> loopStates)
  {
    for (final TransitionProperty tp: loopStates) {
      if (encodedCurrTuple.equals(tp.getSourceTuple())) {
        return true;
      }
    }
    return false;
  }

  /**
   * It compares two state tuples.
   * @param tuple1 first tuple that will be compared with second tuple
   * @param tuple2 second tuple
   * @return true if two tuple are equivalent, false otherwise.
   */
  private boolean compare(final int[] tuple1, final int[] tuple2)
  {
    for (int i = 0; i < tuple1.length; i++) {
      if (tuple1[i] != tuple2[i]) {
        return false;
      }
    }
    return true;
  }


  //#########################################################################
  //# For encoding and decoding
  /**
   * It returns a number of bits used for encoding current automaton
   * @param automaton current automaton
   */
  private int getBitLength(final AutomatonProxy automaton)
  {
    int states = automaton.getStates().size();
    int bits = 0;
    states -= 1;
    while (states > 0) {
      states >>= 1;
      bits++;
    }
    return (bits == 0)?1:bits;
  }

  /**
   * It will take a single state tuple as a parameter and encode it.
   * @param stateCodes state tuple that will be encoded
   * @return encoded state tuple
   */
  private int[] encode(final int[] stateCodes)
  {
    final int encoded[] = new int[mNumInts];
    int i, j;
    for (i = 0; i < mNumInts; i++) {
      for (j = mIndexAutomata[i]; j < mIndexAutomata[i+1]; j++) {
        encoded[i] <<= mNumBits[j];
        encoded[i] |= stateCodes[j];
      }
    }
    return encoded;
  }

  /**
   * It will take an encoded state tuple as a parameter and decode it.
   * Decoded result will be contained in the second parameter
   * @param encodedStateCodes state tuple that will be decoded
   * @param currTuple the decoded state tuple will be stored here
   */
  private void decode(final int[] encodedStateCodes, final int[] currTuple)
  {
    int tmp, mask, value, i, j;
    for (i = 0; i < mNumInts; i++) {
      tmp = encodedStateCodes[i];
      for (j = mIndexAutomata[i+1]-1; j >= mIndexAutomata[i]; j--) {
        mask = mNumBitsMasks[j];
        value = tmp & mask;
        currTuple[j] = value;
        tmp = tmp >> mNumBits[j];
      }
    }
  }


  //#########################################################################
  //# Data Members
  /** a sentinel that states if the model is control loop free. */
  private boolean mControlLoopFree;

  /** a list of automata in the model */
  private ArrayList<AutomatonProxy> mAutomataList;

  /** number of automata in the model */
  private int mNumAutomata;

  /** a list of events in the model */
  private ArrayList<EventProxy> mEventList;

  /** number of all events in the model */
  private int mNumEvent;

  /** number of controllable events in the model */
  private int mNumConEvent;

  /** a list of transitions in the model */
  private ArrayList<ArrayList<TransitionProxy>> mTransitionList;

  /** a map of state tuple in synchronised model */
  private StateHashSet mGlobalStateSet;

  /** a list of unvisited state tuple. */
  private List<EncodedStateTuple> mUnvisitedList;

  /** it holds the initial state tuple of the model. */
  private int mInitialStateTuple[];

  /** it holds the initial encoded state tuple of the model. */
  private EncodedStateTuple mEncodedInitialStateTuple;

  /** for tracing counterexample: it holds the root encoded state of the
      control loop. */
  private EncodedStateTuple mEncodedRootStateTuple;

  /** global event map: true is controllable, false is uncontrollable */
  private boolean mGlobalEventMap[];

  private static int[][][] mMap;

  /** a global integer array to store current decoded integer state tuple */
  private int mCurrTuple[];

  /** a global encoded state tuple for storing current state tuple */
  private EncodedStateTuple mEncodedCurrTuple;

  /** a global state tuple for storing next state tuple */
  private int mNextTuple[];

  //#########################################################################
  //# Variables used for encoding/decoding
  /** a list contains number of bits needed for each automaton */
  private int mNumBits[];

  /** a list contains masks needed for each automaton */
  private int mNumBitsMasks[];

  /** a number of integers used to encode synchronized state */
  private int mNumInts;

  /** an index of first automaton in each integer buffer */
  private int mIndexAutomata[];


  //#########################################################################
  //# Class Constants
  /** Constant: number of bits for integer buffer */
  private static final int SIZE_INT = 32;

  /** Constant: size of global array list buffer for growing */
  private static final int SIZE_BUFFER = 1024;

}
