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

import gnu.trove.set.hash.THashSet;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.BitSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import net.sourceforge.waters.analysis.sd.NerodeDiagnostics;
import net.sourceforge.waters.analysis.sd.NerodeEquVerificationResult;
import net.sourceforge.waters.analysis.sd.NerodeKindTranslator;
import net.sourceforge.waters.model.analysis.AnalysisAbortException;
import net.sourceforge.waters.model.analysis.AnalysisException;
import net.sourceforge.waters.model.analysis.OverflowException;
import net.sourceforge.waters.model.analysis.des.AbstractSafetyVerifier;
import net.sourceforge.waters.model.analysis.des.SafetyVerifier;
import net.sourceforge.waters.model.analysis.kindtranslator.KindTranslator;
import net.sourceforge.waters.model.base.ComponentKind;
import net.sourceforge.waters.model.base.EventKind;
import net.sourceforge.waters.model.des.AutomatonProxy;
import net.sourceforge.waters.model.des.EventProxy;
import net.sourceforge.waters.model.des.ProductDESProxy;
import net.sourceforge.waters.model.des.ProductDESProxyFactory;
import net.sourceforge.waters.model.des.SafetyCounterExampleProxy;
import net.sourceforge.waters.model.des.StateProxy;
import net.sourceforge.waters.model.des.TraceProxy;
import net.sourceforge.waters.model.des.TraceStepProxy;
import net.sourceforge.waters.model.des.TransitionProxy;

/**
 * <P>A monolithic implementation of Nerode Equivalence checker.</P>
 *
 * <P>The {@link #run()} method of this model checker does a Equivalence
 * check, and finds whether the given model satisfies SD Property iii.2.</P>
 *
 * @author Mahvash Baloch
 */

public class MonolithicNerodeEChecker
  extends AbstractSafetyVerifier
  implements SafetyVerifier
{

  //#########################################################################
  //# Constructors
  public MonolithicNerodeEChecker(final ProductDESProxyFactory factory)
  {
    this(null, factory);
  }

  public MonolithicNerodeEChecker(final ProductDESProxy model,
                                    final ProductDESProxyFactory factory)
  {
    super(model,
          NerodeKindTranslator.getInstance(),
          NerodeDiagnostics.getInstance(),
          factory);
  }
  //#########################################################################
  //# Invocation
  /**
   * Runs this Nerode Equivalence checker.
   * This method starts the model checking process on the model given
   * as parameter to the constructor of this object. On termination,
   * the result of checking the property is known and can be queried
   * using the {@link
   * net.sourceforge.waters.model.analysis.des.ModelVerifier#isSatisfied()
   * isSatisfied()} and {@link #getCounterExample()} methods.
   * @return <CODE>true</CODE> if the model satisfied Nerode equivalence, or
   *         <CODE>false</CODE> if it does not.
   *         The same value can be queried using the {@link
   *         net.sourceforge.waters.model.analysis.des.ModelVerifier#isSatisfied()
   *         isSatisfied()} method.
   */
  @Override
  public boolean run()
    throws AnalysisException
  {
    try {

      setUp();
      mControllable = true;
      mSSFoundList.add(mEncodedInitialStateTuple);
      mSSProcessList.add(mEncodedInitialStateTuple);
      mVisitedList.add(mEncodedInitialStateTuple);

      StateTuple mCurrTuple = new StateTuple(encode(mInitialStateTuple));

      while (!mSSProcessList.isEmpty()) {
        mCurrTuple = mSSProcessList.remove(0);
        AnalyzeSampState(mCurrTuple);
        doCheckNerodeCells();
        if(!mControllable) {
          return false;
      }
           }

      if(pNerFail.isEmpty())  {

        return setSatisfiedResult();
     }
     mControllable = ReCheckNerodeCells();

     // Empty case
      if (mNumAutomata == 0) {
        return setSatisfiedResult();
      }

      if (mControllable) {
        return setSatisfiedResult();
      } else {
        final StateTuple q1 = errorPair.getTuple1();
        final StateTuple q2 = errorPair.getTuple2();
        final int currTuple1[] = new int[mNumAutomata];
        final int currTuple2[] = new int[mNumAutomata];
        decode(q1.getCodes(), currTuple1);
        decode(q2.getCodes(), currTuple2);
        mSystemState = currTuple1;
        for (int i = 0; i < mNumEvent; i++)  // for all events
          if (eventAvailable(currTuple1, i))
               { mErrorEvent = i;}
        final SafetyCounterExampleProxy counterexample = computeCounterExample();
        mSystemState = currTuple2;
        for (int i = 0; i < mNumEvent; i++)  // for all events
          if (eventAvailable(currTuple2, i))
               { mErrorEvent = i;}
        tCounterExample = computeCounterExample2();
        return setFailedResult(counterexample);
      }
          }
    finally {
      tearDown();

    }

  }

  /**
   * This method visits each state tuple in the synchronous product.
   * and analyses the concurrent behaviour for sampled state zss.
   * The method builds the reachability tree starting at zss until all nodes
   * termintae at a tick and stores all those strings in Sconc
   *
   */
  private void AnalyzeSampState(final StateTuple zss)
    throws AnalysisException
  {
    checkAbort();

    final THashSet<StateTuple> systemSet = new THashSet<StateTuple>();
    systemSet.add(zss);
    mStateSpace.add(zss);
    mIndexList.add(mStateSpace.size()-1);
    int indexSize = 0;
    StateTuple nextTuple = null;

    final BitSet occuI = new BitSet(mNumEvent);
    BitSet occPrNode = new BitSet(mNumEvent);

    occuI.clear();
    final Node iniNode = new Node(zss,occuI);

     nodeSet = new THashSet<Node>();
     mPendingList.clear();
     mPendingList.add(iniNode);

     // new memory allocation to store current state tuple
    final int currTuple[] = new int[mNumAutomata];

    while(!mPendingList.isEmpty())
      {
          indexSize = mIndexList.size();
          final Node prNode = mPendingList.remove(0);
          final StateTuple tuple= prNode.getTuple();
          decode(tuple.getCodes(), currTuple);
          occPrNode = prNode.getOccu();
          BitSet oc = (BitSet) occPrNode.clone();
          mVisitedList.add(tuple);
          if (systemSet.add(tuple)) {
            mStateSpace.add(tuple);
            if (mStateSpace.size() > getNodeLimit()) {
            throw new OverflowException(getNodeLimit());
          } else {
            checkAbort();
          }
        }

      for (int i = 0; i < mNumEvent; i++) { // for all events
        final EventProxy event = mEventList.get(i);
        if (eventAvailable(currTuple, i)) {
          nextTuple = new StateTuple(encode(mNextTuple));

          if(event.equals(tick))
          {
            occPrNode = prNode.getOccu();
            final Node conc = new Node(nextTuple,occPrNode);
            sConc.add(conc);

            if(!mSSFoundList.contains(nextTuple))
            {
              mSSFoundList.add(nextTuple);
              mSSProcessList.add(nextTuple);
            }
          }
          else
          {
            occPrNode = prNode.getOccu();
            oc = (BitSet) occPrNode.clone();
            oc.set(i);
            final BitSet nextOc = oc;
            final Node nextNode = new Node(nextTuple,nextOc);

            if(nodeSet.add(nextNode))
             mPendingList.add(nextNode);
          }
        }
      }
      if (mStateSpace.size() != mIndexList.get(indexSize - 1) + 1)
        mIndexList.add(mStateSpace.size() - 1);
      }
   }

  private void doCheckNerodeCells()
  throws AnalysisException
{
  checkAbort();
  final List<StateTuple> zEqu = new ArrayList<StateTuple>(SIZE_BUFFER);
  boolean sameCell;
  while(true) {
    if(sConc.isEmpty())
    return;
    zEqu.clear();
    final Node first = sConc.remove(0);
    final StateTuple z = first.getTuple();
    final BitSet occU=first.getOccu();
    zEqu.add(z);
    sameCell = true;

    for (int i=0;i<sConc.size();i++) {

    final Node e = sConc.get(i);
    final BitSet occ=e.getOccu();

    if(occU.equals(occ))
    {
      final StateTuple zPr= e.getTuple();
      zEqu.add(zPr);
      sConc.remove(i);
        if(!z.equals(zPr))
        { sameCell = false;}
    }
    if(!sameCell)
      pNerFail.add(zEqu);
   }
    }
}

   /**
   * It checks whether a state Tuple is marked or not.
   * @return true if State tuple is marked, false otherwise.
   */
  private boolean isMarked(final StateTuple tuple)
  {
    final int[] temp = new int[mNumAutomata];
    decode(tuple.getCodes(), temp);
    boolean marked = true;

    for (int i = 0; i < mNumAutomata; i++)
      if (mMarkedStates[i][temp[i]] != 1) {
        marked = false;
      }
    return marked;
  }

  private boolean ReCheckNerodeCells()
  {
    final List<StateTuplePair> Visited= new ArrayList<StateTuplePair>();
    Visited.clear();
    List<StateTuple> Zequ = new ArrayList<StateTuple>();
    while(!pNerFail.isEmpty())
    {
      Zequ =  pNerFail.remove(0);
      if(!recheckNerodecells(Zequ,Visited))
        return false;
     }
    return true;
  }

  private boolean recheckNerodecells
                  (final List<StateTuple> Zequ, final List<StateTuplePair> Visited)
  {

    boolean eventinOne = true;
    boolean eventinTwo = true;
    StateTuple nextTuple1, nextTuple2;
    final StateTuple z1 = Zequ.remove(0);
    final List<StateTuplePair> Pending = new ArrayList<StateTuplePair>();
    Pending.clear();
    while (!Zequ.isEmpty())
      {
          final StateTuple z2 = Zequ.remove(0);
          final StateTuplePair mPair= new StateTuplePair(z1,z2);
          final StateTuplePair tempPair = new StateTuplePair(z2,z1);
          Pending.add(mPair);
          Visited.add(mPair);  Visited.add(tempPair);
      }

    while(!Pending.isEmpty())
    {
      final StateTuplePair statePair = Pending.remove(0);

      final StateTuple q1 = statePair.getTuple1();
      final StateTuple q2 = statePair.getTuple2();

   // new memory allocation to store current state tuple
      final int currTuple1[] = new int[mNumAutomata];
      final int currTuple2[] = new int[mNumAutomata];
      decode(q1.getCodes(), currTuple1);
      decode(q2.getCodes(), currTuple2);

      nextTuple1 = new StateTuple(encode(currTuple1));
      nextTuple2 = new StateTuple(encode(currTuple2));

      if(isMarked(q1) && !isMarked(q2))
      { errorPair = new StateTuplePair(q1,q2);
        errorMark = true;
        mSystemState = currTuple1;
        for (int i = 0; i < mNumEvent; i++) { // for all events
            if (eventAvailable(currTuple1, i))
              {
           mErrorEvent = i;
           break; }
              }
        return false; }
        else

      if(isMarked(q2) && !isMarked(q1))
       { errorPair = new StateTuplePair(q2,q1);
         errorMark= true;
         mSystemState = currTuple2;
         for (int i = 0; i < mNumEvent; i++) { // for all events
           if (eventAvailable(currTuple2, i))
             {
          mErrorEvent = i;
          break; }
           }
         return false;
         }

      for (int i = 0; i < mNumEvent; i++) { // for all events
       // final EventProxy event = mEventList.get(i);
        if (eventAvailable(currTuple1, i))
            {
          nextTuple1 = new StateTuple(encode(mNextTuple));
          eventinOne = true;
            }
        else
          {
          eventinOne = false;
          }

        if (eventAvailable(currTuple2, i))
            {
         nextTuple2 = new StateTuple(encode(mNextTuple));
          eventinTwo = true;
            }
        else
          {
          eventinTwo = false;
          }

        if(!eventinOne && !eventinTwo)
        { }
        else
        if(!eventinOne || !eventinTwo)
          {errorPair = new StateTuplePair(q1,q2);
           return false;
          }
      }

        final StateTuplePair nextPair = new StateTuplePair(nextTuple1,nextTuple2);
        final StateTuplePair tnextPair = new StateTuplePair(nextTuple2,nextTuple1);

        if ((!Visited.contains(nextPair)) && (!Visited.contains(tnextPair)))
        {
          Visited.add(nextPair);   Visited.add(tnextPair);
          Pending.add(nextPair);
        }
      }

    return true;
  }
  //#########################################################################
  //# Interface net.sourceforge.waters.model.analysis.ModelAnalyser
  @Override
  public boolean supportsNondeterminism()
  {
    return false;
  }

  //#########################################################################
  //# Interface net.sourceforge.waters.model.ModelAnalyser
  @Override
  public NerodeEquVerificationResult getAnalysisResult()
  {
    return (NerodeEquVerificationResult) super.getAnalysisResult();
  }

  @Override
  public NerodeEquVerificationResult createAnalysisResult()
  {
    return new NerodeEquVerificationResult(this);
  }

  @Override
  protected void addStatistics()
  {
    super.addStatistics();
    final NerodeEquVerificationResult stats = getAnalysisResult();
    final int numstates = mStateSpace.size();
    stats.setNumberOfAutomata(mNumAutomata);
    stats.setNumberOfStates(numstates);
    stats.setPeakNumberOfNodes(numstates);
    stats.setCounterExample2(tCounterExample);
  }

  //#########################################################################
  //# Algorithm
  @Override
  protected void setUp()
    throws AnalysisException
  {
    super.setUp();
    final ProductDESProxy des = getModel();
    final KindTranslator translator = getKindTranslator();

    mControllable = true;
    mAutomataList = new ArrayList<AutomatonProxy>();
    mEventList = new ArrayList<EventProxy>();
    mTransitionList = new ArrayList<ArrayList<TransitionProxy>>();
    pNerFail = new ArrayList<List<StateTuple>>();
    mStateSpace = new BlockedArrayList<StateTuple>(StateTuple.class);
    mIndexList = new ArrayList<Integer>();


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


    for (final EventProxy eProxy: des.getEvents()) {
      final EventKind kind = translator.getEventKind(eProxy);
      switch (kind) {
      case CONTROLLABLE:
        mEventList.add(eProxy);
        if((eProxy.getName().equals("tick")))
          tick=eProxy;
        break;
      case UNCONTROLLABLE:
         mEventList.add(eProxy);
        break;
      case PROPOSITION:
          marking = eProxy;
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
    mMarkedStates = new int[mNumAutomata][100];
    int i = 0;
    for (final AutomatonProxy aProxy: mAutomataList) {
      int j = 0;
      for (final StateProxy sProxy: aProxy.getStates()) {


        if(sProxy.getPropositions().contains(marking)) {
         mMarkedStates[i][j]= 1;

         }
        else
          mMarkedStates[i][j] = 0;

        if (sProxy.isInitial() == true) {
          mInitialStateTuple[i] = j;
           }
        j++;
      }
      i++;
    }

    // set a buffer for storing next state tuple
    mNextTuple = new int[mNumAutomata];
    // set the initial state tuple
    mEncodedInitialStateTuple =
      new StateTuple(encode(mInitialStateTuple));

    mVisitedList = new ArrayList<StateTuple>(SIZE_BUFFER);
    mSSFoundList = new ArrayList<StateTuple>(SIZE_BUFFER);
    mSSProcessList = new ArrayList<StateTuple>(SIZE_BUFFER);
    mPendingList = new ArrayList<Node>();
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
          mErrorAutomaton=i;
          return false;
        }
      }
      else { // event is not in the automaton
        mNextTuple[i] = currTuple[i];
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
  /**
   * Gets a counterexample if the model was found to not satisfy Nerode
   * equivalence representing a error trace. The errortrace is a nonempty
   * sequence of events such that all except the last event in the list
   * can be executed by the model. The last event in list is an event that is
   * accepted by the System but the concurrent string which has the same occurence
   * image is not accepted by the System in the model. Thus, the last
   * step demonstrates why the model is not Nerode equivalent.
   * @return A trace object representing the counterexample.
   *         The returned trace is constructed for the input product DES
   *         of this Nerode equivalence checker and shares its automata and
   *         event objects.
   * @throws IllegalStateException if this method is called before
   *         model checking has completed, i.e., before {@link #run() run()}
   *         has been called, or model checking has found that the
   *         property is satisfied and there is no counterexample.
   */
  private SafetyCounterExampleProxy computeCounterExample()
    throws AnalysisAbortException, OverflowException
  {
    final ProductDESProxyFactory factory = getFactory();
    final ProductDESProxy des = getModel();
    final List<TraceStepProxy> steps = new LinkedList<TraceStepProxy>();
    int k; boolean found = false;
    final int indexSize = mIndexList.size();
    final int[] errorState = new int[mNumAutomata];
    EventProxy event0 = mEventList.get(mErrorEvent);
    final AutomatonProxy aut = mAutomataList.get(mErrorAutomaton);
    final List<StateProxy> codes0 = new ArrayList<StateProxy>(aut.getStates());
    final int code0 = mSystemState[mErrorAutomaton];
    final StateProxy state0 = codes0.get(code0);
    final TraceStepProxy step0 = factory.createTraceStepProxy(event0);
    steps.add(0, step0);

   while (true) {
      for (int i = 0; i < mNumAutomata; i++) {
        errorState[i] = mSystemState[i];
         }
     if(Arrays.equals(errorState,mInitialStateTuple))
        break;
      for (k = 0;k <= mIndexList.get(indexSize - 1); k++) {
        decode((mStateSpace.get(k)).getCodes(), mSystemState);
          for (int j = 0; j < mNumEvent; j++) {
          if (eventAvailable(mSystemState, j)) {
            if (Arrays.equals(mNextTuple, errorState)) {
                found = true;
                final EventProxy event = mEventList.get(j);
                final TraceStepProxy step = factory.createTraceStepProxy(event);
                steps.add(0, step);
                break;
            }

            }
          else continue;
            }
        if (found) {
          found = false;
          break;
        }
        checkAbort();
       }

    }

  if(errorMark)
  {
    final TraceStepProxy append = factory.createTraceStepProxy(marking);
    steps.add(append);
    event0 = marking;
  }
    final TraceStepProxy init = factory.createTraceStepProxy(null);
    steps.add(0, init);
    final String tracename = getTraceName();
    final String comment = getTraceComment(event0, aut, state0);
    final TraceProxy trace = factory.createTraceProxy(steps);
    return factory.createSafetyCounterExampleProxy
      (tracename, comment, null, des, null, trace);
  }

  /**
   * Computation for the second counterexample...
   * This is to be used by the Modular checker.
   */
  private SafetyCounterExampleProxy computeCounterExample2()
      throws AnalysisAbortException, OverflowException
    {
      final ProductDESProxyFactory factory = getFactory();
      final ProductDESProxy des = getModel();
      final List<TraceStepProxy> steps = new LinkedList<TraceStepProxy>();
      int k; boolean found = false;
      final int indexSize = mIndexList.size();
      final int[] errorState = new int[mNumAutomata];
      EventProxy event0 = mEventList.get(mErrorEvent);
      final AutomatonProxy aut = mAutomataList.get(mErrorAutomaton);
      final List<StateProxy> codes0 = new ArrayList<StateProxy>(aut.getStates());
      final int code0 = mSystemState[mErrorAutomaton];
      final StateProxy state0 = codes0.get(code0);
      final TraceStepProxy step0 = factory.createTraceStepProxy(event0);
      steps.add(0, step0);

     while (true) {
        for (int i = 0; i < mNumAutomata; i++) {
          errorState[i] = mSystemState[i];
           }
       if(Arrays.equals(errorState,mInitialStateTuple))
          break;
        for (k = 0;k <= mIndexList.get(indexSize - 1); k++) {
          decode((mStateSpace.get(k)).getCodes(), mSystemState);
            for (int j = 0; j < mNumEvent; j++) {
            if (eventAvailable(mSystemState, j)) {
              if (Arrays.equals(mNextTuple, errorState)) {
                  found = true;
                  final EventProxy event = mEventList.get(j);
                  final TraceStepProxy step = factory.createTraceStepProxy(event);
                  steps.add(0, step);
                  break;
              }

              }
            else continue;
              }
          if (found) {
            found = false;
            break;
          }
          checkAbort();
         }

      }
     if(!errorMark)
     steps.remove(step0);
   if(errorMark)
    {
     final TraceStepProxy append = factory.createTraceStepProxy(marking);
     steps.add(append);
     event0 = marking;
    }
    final TraceStepProxy init = factory.createTraceStepProxy(null);
    steps.add(0, init);
    final String tracename = getTraceName();
    final String comment = getTraceComment(event0, aut, state0);
    final TraceProxy trace = factory.createTraceProxy(steps);
    return factory.createSafetyCounterExampleProxy
      (tracename, comment, null, des, null, trace);
  }


  //#########################################################################
  //# Data Members
  /** a sentinel that states if the model is control loop free. */
  private boolean mControllable;

  /** the Special event tick.    */
  private EventProxy tick;
  private EventProxy marking;

  /** a list of automata in the model */
  private ArrayList<AutomatonProxy> mAutomataList;

  /** number of automata in the model */
  private int mNumAutomata;

  /** a list of events in the model */
  private ArrayList<EventProxy> mEventList;

  /** number of all events in the model */
  private int mNumEvent;

  /** a list of transitions in the model */
  private ArrayList<ArrayList<TransitionProxy>> mTransitionList;

  /**A List of StateTuples to be checked */
  private ArrayList<List<StateTuple>>pNerFail;

    /** a list of unvisited state tuple. */
  final List<Node> sConc= new ArrayList<Node>();
  private List<StateTuple> mVisitedList;
  private List<StateTuple> mSSFoundList;
  private List<StateTuple> mSSProcessList;
  private List<Node> mPendingList;
  private Set<Node> nodeSet;

  /** it holds the initial state tuple of the model. */
  private int mInitialStateTuple[];

  /** it holds the initial encoded state tuple of the model. */
  private StateTuple mEncodedInitialStateTuple;

  /** a map of state tuple in synchronised model */
  private List<StateTuple> mStateSpace;

  /** global event map: true is controllable, false is uncontrollable */
  private boolean mGlobalEventMap[];

  private static int[][][] mMap;
  private static int[][] mMarkedStates;

  /** a global state tuple for storing next state tuple */
  private int mNextTuple[];

  /** for CounterExample */
  private int[] mSystemState;
  private int mErrorEvent;
  private int mErrorAutomaton;
  private StateTuplePair errorPair;
  private boolean errorMark;
  private SafetyCounterExampleProxy tCounterExample;

  //#########################################################################
  //# Variables used for encoding/decoding
  /** a list contains number of bits needed for each automaton */
  private int mNumBits[];

  /** a list contains masks needed for each automaton */
  private int mNumBitsMasks[];

  /** a number of integers used to encode synchronised state */
  private int mNumInts;

  /** an index of first automaton in each integer buffer */
  private int mIndexAutomata[];
  private List<Integer> mIndexList;

  //#########################################################################
  //# Class Constants
  /** Constant: number of bits for integer buffer */
  private static final int SIZE_INT = 32;

  /** Constant: size of global array list buffer for growing */
  private static final int SIZE_BUFFER = 1024;

}
