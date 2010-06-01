//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.analysis.monolithic
//# CLASS:   ControlLoopChecker
//###########################################################################
//# $Id: MonolithicControlLoopChecker.java 5540 2010-04-14 10:47:01Z robi $
//###########################################################################

package net.sourceforge.waters.analysis.monolithic;

import gnu.trove.THashSet;

import java.util.Collection;
import java.util.List;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Set;
import java.util.HashSet;
import java.util.Stack;

import net.sourceforge.waters.model.analysis.AbortException;
import net.sourceforge.waters.model.analysis.AbstractModelVerifier;
import net.sourceforge.waters.model.analysis.AnalysisException;
import net.sourceforge.waters.model.analysis.ControllabilityKindTranslator;
import net.sourceforge.waters.model.analysis.ControlLoopChecker;
import net.sourceforge.waters.model.analysis.KindTranslator;
import net.sourceforge.waters.model.analysis.OverflowException;
import net.sourceforge.waters.model.analysis.VerificationResult;
import net.sourceforge.waters.model.base.Pair;
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
 * @author Andrew Holland, Peter Yunil Park
 */

public class MonolithicSCCControlLoopChecker
  extends AbstractModelVerifier
  implements ControlLoopChecker
{
  //#########################################################################
  //# Constructors
  public MonolithicSCCControlLoopChecker(final ProductDESProxyFactory factory)
  {
    this(ControllabilityKindTranslator.getInstance(), factory);
  }

  public MonolithicSCCControlLoopChecker(final KindTranslator translator,
                                      final ProductDESProxyFactory factory)
  {
    this(null, translator, factory);
  }

  public MonolithicSCCControlLoopChecker(final ProductDESProxy model,
                                      final ProductDESProxyFactory factory)
  {
    this(model, ControllabilityKindTranslator.getInstance(), factory);
  }

  public MonolithicSCCControlLoopChecker(final ProductDESProxy model,
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

  public Collection<EventProxy> getNonLoopEvents()
  {
    final HashSet<EventProxy> output = new HashSet<EventProxy>();
    for (final EventProxy event : mEventList)
    {
      if (event.getKind() == EventKind.CONTROLLABLE)
      {
        if (!mLoopEvents.contains(event))
          output.add(event);
      }
    }
    return output;
  }

  //#########################################################################
  //# Setting the Result
  @Override
  protected void addStatistics()
  {
    super.addStatistics();
    final VerificationResult result = getAnalysisResult();
    final int numstates = mGlobalStateSet.size();
    result.setNumberOfAutomata(mNumAutomata);
    result.setNumberOfStates(numstates);
    result.setPeakNumberOfNodes(numstates);
  }


  //#########################################################################
  //# Algorithm
  protected void setUp()
    throws AnalysisException
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
    final int firstStackSize = stack.size();
    encodedCurrTuple.setVisited(true);
    numStates++;
    final int thisState = numStates;
    encodedCurrTuple.setRoot(numStates);
    // current state tuple now in currTuple
    decode(encodedCurrTuple.getCodes(), currTuple);
    for (int i = 0; i < mNumEvent; i++) { // for all events
      if (mGlobalEventMap[i]) { // CONTROLLABLE
        if (eventAvailable(currTuple, i)) {
          EncodedStateTuple encodedNextTuple =
            new EncodedStateTuple(encode(mNextTuple));
          if (addState(encodedNextTuple)) {
            visit(encodedNextTuple);
          }
          else {
            encodedNextTuple = mGlobalStateSet.get(encodedNextTuple);
            if (encodedNextTuple.getVisited() == false) {
              visit(encodedNextTuple);
            }
          }
          if (encodedNextTuple.getInComponent() == false) {
            if (encodedNextTuple.getRoot() < encodedCurrTuple.getRoot())
            {
              encodedCurrTuple.setRoot(encodedNextTuple.getRoot());
            }
          }
        }
      }  else { // UNCONTROLLABLE
        if (eventAvailable(currTuple, i)) {
          final EncodedStateTuple encodedNextTuple =
            new EncodedStateTuple(encode(mNextTuple));
          addState(encodedNextTuple);
        }
      }
    }
    if (encodedCurrTuple.getRoot() == thisState)
    {
      encodedCurrTuple.setInComponent(true);
      final int stackSize = stack.size();
      if (stackSize != 0)
      {
        final ArrayList<EncodedStateTuple> temp = new ArrayList<EncodedStateTuple>();
        temp.add(encodedCurrTuple);
        while (stack.size() >= firstStackSize)
        {
          final EncodedStateTuple popped = stack.pop();
          popped.setInComponent(true);
          temp.add(popped);
          if (stack.size() == 0)
            break;
        }
        mLoopEvents.addAll(getLoopEvents(temp));
      }
      if (stackSize != stack.size())
      {
        mControlLoopFree = false;
      }
      else
      {
        for (int i = 0; i < mNumEvent; i++) { // for all events
          if (mGlobalEventMap[i]) { // CONTROLLABLE
            if (eventAvailable(currTuple, i)) {
              EncodedStateTuple encodedNextTuple =
                new EncodedStateTuple(encode(mNextTuple));
              encodedNextTuple = mGlobalStateSet.get(encodedNextTuple);
              if (encodedNextTuple == encodedCurrTuple) // Self-loop
              {
                mControlLoopFree = false;
                System.out.println("DEBUG: Self-loop detected");
              }
            }
          }
        }
      }
    }
    else
      stack.push(encodedCurrTuple);
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
  List<EventProxy> tracelist = new LinkedList<EventProxy>();

  /* FIND COUNTEREXAMPLE TRACE HERE */
  /* Counterexample = The shortest path from mInitialStateTuple to
   *                  mRootStateTuple
   *                  +
   *                  The shortest path from mRootStateTuple
   *                  to mRootStateTuple
   */

  Set<TransitionProperty> loopStates =
    new THashSet<TransitionProperty>();
  List<EncodedStateTuple> list = new ArrayList<EncodedStateTuple>();
  ArrayList<Integer> indexList = new ArrayList<Integer>();

  // find a shortest path from mInitialStateTuple to mRootStateTuple:
  // for both controllable events and uncontrollable events
  // if mInitialStateTuple != mRootStateTuple

  System.out.println("DEBUG: Finding loop...");
  Pair<ArrayList<EncodedStateTuple>, ArrayList<Integer>> temp = findLoop();
  list = temp.getFirst();
  indexList = temp.getSecond();

  // Add to tracelist here
  System.out.println("DEBUG: Found loop, finding path");
  tracelist = addPathToLoop(list, indexList);

  //find a shortest path from mRootStateTuple to mRootStateTuple:
  // only for controllable events
  System.out.println("DEBUG: Found path, finding cycle");
  temp = findCycle();

  // BREAK
  System.out.println("DEBUG: Found cycle, finding transitions");
  loopStates = getTransitionProxies(temp.getFirst(), temp.getSecond());

  // BREAK

  final int loopIndex = tracelist.size();
  System.out.println("DEBUG: Found transitions, finding second path");
  tracelist = getSecondTraceList(tracelist, loopStates);
  System.out.println("DEBUG: Found second path, adding trace");
  final LoopTraceProxy trace =
    factory.createLoopTraceProxy(tracename, des, tracelist, loopIndex);
  return trace;
}

  private Pair<ArrayList<EncodedStateTuple>, ArrayList<Integer>> findLoop()
  {
    EncodedStateTuple encodedCurrTuple = new EncodedStateTuple(mNumInts);
    final ArrayList<EncodedStateTuple> layeredList = new ArrayList<EncodedStateTuple>();
    final THashSet<EncodedStateTuple> set = new THashSet<EncodedStateTuple>();
    final ArrayList<Integer> indexList = new ArrayList<Integer>();
    indexList.add(0);
    int indexSize = 0;
    layeredList.add(mEncodedInitialStateTuple);
    final List<EventProxy> fakeTraceList = new ArrayList<EventProxy>();
    loop2:
      while (true) {
        indexSize = indexList.size();
        for (int i = (indexSize==1) ? 0 : (indexList.get(indexSize-2)+1);
             i <= indexList.get(indexSize-1); i++) {
          encodedCurrTuple = layeredList.get(i);
          encodedCurrTuple = mGlobalStateSet.get(encodedCurrTuple);
          decode(encodedCurrTuple.getCodes(), mCurrTuple);
          for (int j = 0; j < mNumEvent; j++) {
            if (eventAvailable(mCurrTuple, j)) {
              EncodedStateTuple encodedNextTuple =
                new EncodedStateTuple(encode(mNextTuple));
              encodedNextTuple = mGlobalStateSet.get(encodedNextTuple);
              if (encodedCurrTuple.getRoot() == encodedNextTuple.getRoot()) {
                fakeTraceList.add(mEventList.get(j));
                mEncodedRootStateTuple = encodedCurrTuple;
                // now change the root of the loop
                break loop2;
              }
              if (set.add(encodedNextTuple)) {
                layeredList.add(encodedNextTuple);
              }
            }
          }
        }
        if (layeredList.size() != (indexList.get(indexSize-1)+1)) {
          indexList.add(layeredList.size()-1);
        } else {
          break;
        }
      }
    final Pair<ArrayList<EncodedStateTuple>, ArrayList<Integer>> output
    = new Pair<ArrayList<EncodedStateTuple>, ArrayList<Integer>>(layeredList, indexList);
  return output;
  }

  private ArrayList<EventProxy> addPathToLoop(final List<EncodedStateTuple> list, final ArrayList<Integer> indexList)
  {
    final List<EventProxy> fakeTraceList = new ArrayList<EventProxy>();
    final ArrayList<EventProxy> output = new ArrayList<EventProxy>();
    int[] target = new int[mCurrTuple.length];
    decode(mEncodedRootStateTuple.getCodes(), target);
    for (int i = indexList.size()-2; i >= 0; i--) {
      final int start = (indexList.get(i)==0)?0:indexList.get(i-1)+1;
      final int end = indexList.get(i);
       next2:
      for (int j = start; j <= end; j++) {
        final EncodedStateTuple encodedCurr = list.get(j);
        mCurrTuple = new int[mNumAutomata];
        decode(encodedCurr.getCodes(), mCurrTuple);
        for (int k = 0; k < mNumEvent; k++) {
          if (eventAvailable(mCurrTuple, k)) {
            if (compare(mNextTuple, target)) {
              fakeTraceList.add(mEventList.get(k));
              target = mCurrTuple;
              System.out.println("DEBUG: Added: " + k + " which corresponds to " + mEventList.get(k));
              break next2;
            }
          }
        }
      }
    }

    // Invert tracelist
    for (int looper = 0; looper < fakeTraceList.size(); looper++)
    {
      output.add(fakeTraceList.get(fakeTraceList.size() - looper - 1));
    }
    return output;
  }

  private Pair<ArrayList<EncodedStateTuple>,ArrayList<Integer>> findCycle() throws AbortException
  {
    final ArrayList<EncodedStateTuple> layeredList = new ArrayList<EncodedStateTuple>();
    final THashSet<EncodedStateTuple> set = new THashSet<EncodedStateTuple>();
    final ArrayList<Integer> indexList = new ArrayList<Integer>();
    indexList.add(0);
    int indexSize = 1;
    layeredList.add(mEncodedRootStateTuple);
    final int[] target = mCurrTuple;
    decode(mEncodedRootStateTuple.getCodes(), target); // target is mEncodedRootStateTuple (decoded)
    mCurrTuple = new int[mNumAutomata];
    EncodedStateTuple encodedCurrTuple;

    loop:
    while (true) {
      indexSize = indexList.size();
      for (int i = (indexSize==1) ? 0 : (indexList.get(indexSize-2)+1);
           i <= indexList.get(indexSize-1); i++) {
        checkAbort();
        encodedCurrTuple = layeredList.get(i);
        decode(encodedCurrTuple.getCodes(), mCurrTuple);
        for (int j = 0; j < mNumConEvent; j++) {
          if (mGlobalEventMap[j]) {
            if (eventAvailable(mCurrTuple, j)) {
              if (compare(mNextTuple, target)) {
                mLastEvent = j;
                mEncodedPreviousStateTuple = encodedCurrTuple;
                break loop;
              }
              final EncodedStateTuple encodedNextTuple =
                new EncodedStateTuple(encode(mNextTuple));
              if (set.add(encodedNextTuple)) {
                layeredList.add(encodedNextTuple);
              }
            }
          }
        }
      }
      if (layeredList.size() != (indexList.get(indexSize-1)+1)) {
        indexList.add(layeredList.size()-1);
      } else {
        break;
      }
    }
    final Pair<ArrayList<EncodedStateTuple>, ArrayList<Integer>> output
      = new Pair<ArrayList<EncodedStateTuple>, ArrayList<Integer>>(layeredList, indexList);
    return output;
  }

  private Set<TransitionProperty> getTransitionProxies(final ArrayList<EncodedStateTuple>layeredList, final ArrayList<Integer> indexList) throws AbortException
  {
    final Set<TransitionProperty> output = new THashSet<TransitionProperty>();
    if (indexList.size() == 1) { // single cycle loop
      output.add(new TransitionProperty(mEncodedRootStateTuple,
                                            mEncodedRootStateTuple,
                                            mLastEvent));
    } else {
      output.add(new TransitionProperty(mEncodedPreviousStateTuple,
                                            mEncodedRootStateTuple,
                                            mLastEvent));
      // swap memory location: mCurrTuple <-> target
      int[] target = new int[mNumAutomata];
      decode(mEncodedPreviousStateTuple.getCodes(), target);
      for (int i = indexList.size()-2; i >= 0; i--) {
        checkAbort();
        final int start =
            (indexList.get(i) == 0) ? 0 : indexList.get(i - 1) + 1;
        final int end = indexList.get(i);
        next:
        for (int j = start; j <= end; j++) {
          final EncodedStateTuple encodedCurr = layeredList.get(j);
          mCurrTuple = new int[mNumAutomata];
          decode(encodedCurr.getCodes(), mCurrTuple);

          for (int k = 0; k < mNumConEvent; k++) {
            if (mGlobalEventMap[k]) {
              if (eventAvailable(mCurrTuple, k)) {
                EncodedStateTuple encodedNext =
                    new EncodedStateTuple(encode(mNextTuple));
                encodedNext = mGlobalStateSet.get(encodedNext);
                if (compare(mNextTuple, target)) {
                  output.add(new TransitionProperty(encodedCurr,
                      encodedNext, k));
                  target = mCurrTuple;
                  break next;
                }
              }
            }
          }
        }
      }
    }
    return output;
  }

  private List<EventProxy> getSecondTraceList(final List<EventProxy> traceList, final Set<TransitionProperty> loopStates)
  {
    System.out.println("DEBUG: Loop states are:");
    for (final TransitionProperty tProp : loopStates)
    {
      System.out.println("DEBUG: " + tProp.getSourceTuple() + ", " + tProp.getTargetTuple() + ", " + tProp.getEvent()
                         + ". Root is " + mEncodedRootStateTuple + ", previous is " + mEncodedPreviousStateTuple
                         + " last event is " + mLastEvent);
    }
    while (loopStates.size() > 0) {
      for (final TransitionProperty tp: loopStates) {
        if (compare(mEncodedRootStateTuple.getCodes(),
            tp.getSourceTuple().getCodes())) {
          traceList.add(mEventList.get(tp.getEvent()));
          mEncodedRootStateTuple = tp.getTargetTuple();
          loopStates.remove(tp);
          break;
        }
      }
    }
    return traceList;
  }

  @SuppressWarnings("unused")
  private boolean arrayEqual(final int[] scan, final int[] val)
  {
    if (scan.length != val.length)
      return false;
    for (int looper = 0; looper < scan.length; looper++)
    {
      if (scan[looper] != val[looper])
        return false;
    }
    return true;
  }

  /**
   * It checks current state tuple is in the control loop
   * @param encodedCurrTuple encoded current state tuple
   * @param loopStates transitions in the control loop
   * @return return true if state tuple is in the loop, false otherwise
   */
  @SuppressWarnings("unused")
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
   * Looks for loop events
   * @param states All states in the strongly connected component
   * @return All events that connect all the states in the strongly connected component
   */
  private HashSet<EventProxy> getLoopEvents(final ArrayList<EncodedStateTuple> states)
  {
    final HashSet<EventProxy> output = new HashSet<EventProxy>();
    for (final EncodedStateTuple source : states)
    {
      final int[] currState = new int[mNumAutomata];
      decode(source.getCodes(), currState);
      for (int i = 0; i < mNumEvent; i++) { // for all events
        if (mGlobalEventMap[i] == true)
        {
          if (eventAvailable(currState, i)) {
            EncodedStateTuple encodedNextTuple =
              new EncodedStateTuple(encode(mNextTuple));
            encodedNextTuple = mGlobalStateSet.get(encodedNextTuple);
            if (encodedNextTuple != null) // If false, then this state hasn't been visited yet, it soon will
              if (encodedNextTuple.getRoot() == source.getRoot())
                output.add(mEventList.get(i));
          }
        }
      }
    }
    return output;
  }

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
        tmp = tmp >>> mNumBits[j];
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

  /** global event map: true is controllable, false is uncontrollable */
  private boolean mGlobalEventMap[];

  private static int[][][] mMap;

  /** a global integer array to store current decoded integer state tuple */
  private int mCurrTuple[];

  /** a global encoded state tuple for storing current state tuple */
  private EncodedStateTuple mEncodedCurrTuple;

  /** a global encoded state tuple for storing the node immediately before the root node in the detected control loop */
  private EncodedStateTuple mEncodedPreviousStateTuple;

  /** a global encoded state tuple for storing the root node of the detected control loop */
  private EncodedStateTuple mEncodedRootStateTuple;

  /** a global index of the last event in the detected control loop */
  private int mLastEvent;

  /** a global state tuple for storing next state tuple */
  private int mNextTuple[];

  /** a set of all the events which occur in all control loops */
  private final Set<EventProxy> mLoopEvents = new THashSet<EventProxy>();

  /** the number of states which have been visited */
  private int numStates = 0;

  /** used for the visit procedure */
  private final Stack<EncodedStateTuple> stack = new Stack<EncodedStateTuple>();

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