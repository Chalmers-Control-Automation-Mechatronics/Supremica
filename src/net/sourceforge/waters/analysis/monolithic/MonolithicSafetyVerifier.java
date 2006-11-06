//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.analysis.monolithic
//# CLASS:   MonolithicSafetyVerifier
//###########################################################################
//# $Id: MonolithicSafetyVerifier.java,v 1.3 2006-11-06 03:23:35 robi Exp $
//###########################################################################

package net.sourceforge.waters.analysis.monolithic;

import gnu.trove.THashSet;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.Arrays;

import net.sourceforge.waters.model.analysis.AbstractModelVerifier;
import net.sourceforge.waters.model.analysis.KindTranslator;
import net.sourceforge.waters.model.analysis.SafetyVerifier;
import net.sourceforge.waters.model.des.EventProxy;
import net.sourceforge.waters.model.des.ProductDESProxy;
import net.sourceforge.waters.model.des.ProductDESProxyFactory;
import net.sourceforge.waters.model.des.SafetyTraceProxy;
import net.sourceforge.waters.model.des.StateProxy;
import net.sourceforge.waters.model.des.AutomatonProxy;
import net.sourceforge.waters.model.des.TransitionProxy;
import net.sourceforge.waters.xsd.base.ComponentKind;
import net.sourceforge.waters.xsd.base.EventKind;


/**
 * <P>A Java implementation of the controllability check algorithm.
 * This algorithm does a brute-force state exploration to check whether
 * the given model is controllable.</P>
 *
 * @author Jinjian Shi
 */

public class MonolithicSafetyVerifier
  extends AbstractModelVerifier
  implements SafetyVerifier
{

  //#########################################################################
  //# Constructors
  /**
   * Creates a new safety verifier to check a particular model.
   * @param  translator  The kind translator is used to remap component and
   *                     event kinds.
   * @param  factory     The factory used for trace construction.
   */
  public MonolithicSafetyVerifier(final KindTranslator translator,
                                  final ProductDESProxyFactory factory)
  {
    this(null, translator, factory);
  }

  /**
   * Creates a new safety verifier to check a particular model.
   * @param  model       The model to be checked by this verifier.
   * @param  translator  The kind translator is used to remap component and
   *                     event kinds.
   * @param  factory     The factory used for trace construction.
   */
  public MonolithicSafetyVerifier(final ProductDESProxy model,
                                  final KindTranslator translator,
                                  final ProductDESProxyFactory factory)
  {
    super(model, factory);
    mKindTranslator = translator;
    mAutomatonSet = model.getAutomata();
    mPlantTransitionMap = new ArrayList<int[][]>();
    mSpecTransitionMap = new ArrayList<int[][]>();

    mIndexList = new ArrayList<Integer>();
    mStateList = new BlockedArrayList<StateTuple>(StateTuple.class);
    mEventCodingList = new ArrayList<EventProxy>(model.getEvents());
    mPlantEventList = new ArrayList<byte[]>();
    mSpecEventList = new ArrayList<byte[]>();
  }


  //#########################################################################
  //# Invocation
  public boolean run()
  {
    Set<StateProxy> stateSet;
    int i,j,k = 0;
    int ck = 0;
    int bl = 0;
    int mask = 0;
    int codeLength = 0;
    int cp = 0;

    eventSize = mEventCodingList.size();
    automatonSize = mAutomatonSet.size();

    // Empty case
    if (automatonSize == 0) {
      return setSatisfiedResult();
    }

    mBitLengthList = new int[automatonSize];
    mMaskList = new int[automatonSize];
    mCodePosition = new int[automatonSize];

    // Count Plant size
    for (AutomatonProxy ap : mAutomatonSet) {
      final ComponentKind kind = mKindTranslator.getComponentKind(ap);
      if (kind == ComponentKind.PLANT) {
        plantSize++;
      }
    }
 
    systemState = new int[automatonSize];

    // Separate the automatons by kind
    for (AutomatonProxy ap : mAutomatonSet) {
      // Get all states
      stateSet = ap.getStates();
      // Encoding states to binary values
      final List<StateProxy> codes = new ArrayList<StateProxy>(stateSet);
      // Encoding events to binary values
      final byte[] aneventCodingList = new byte[eventSize];
      for (EventProxy evp : ap.getEvents()) {
        aneventCodingList[mEventCodingList.indexOf(evp)] = 1;
      }
      // Encoding transitions to binary values
      int stateSize = codes.size();
      int[][] atransition = new int[stateSize][eventSize];
      for (i=0;i<stateSize;i++) {
        for (j=0;j<eventSize;j++) {
          atransition[i][j] = -1;
        }
      }
      for (TransitionProxy tp : ap.getTransitions()) {
        atransition
          [codes.indexOf(tp.getSource())]
          [mEventCodingList.indexOf(tp.getEvent())]
          = codes.indexOf(tp.getTarget());
      }
      // Compute bit length and mask
      bl = BigInteger.valueOf(stateSize).bitLength();
      mask = (1 << bl) - 1;

      // Find initial state
      StateProxy initialState = null;
      for (final StateProxy sp : stateSet) {
        if (sp.isInitial()) {
          initialState = sp;
          break;
        }
      }
      // Store all the information by automaton type
      final ComponentKind kind = mKindTranslator.getComponentKind(ap);
      switch (kind) {
      case PLANT:
        systemState[ck] = codes.indexOf(initialState);
        mPlantEventList.add(aneventCodingList);
        mPlantTransitionMap.add(atransition);
        mBitLengthList[ck] = bl;
        mMaskList[ck] = mask;
        ck++;
        break;
      case SPEC:
        systemState[k+plantSize] = codes.indexOf(initialState);
        mSpecEventList.add(aneventCodingList);
        mSpecTransitionMap.add(atransition);
        mBitLengthList[k + plantSize] = bl;
        mMaskList[k + plantSize] = mask;
        k++;
        break;
      default:
        break;
      }
    }

    // Set the mCodePosition list
    for (i = 0; i < automatonSize; i++) {
      codeLength += mBitLengthList[i];
      if (codeLength <= 32){
        mCodePosition[i] = cp;
      } else {
        codeLength = mBitLengthList[i];
        cp++;
        mCodePosition[i] = cp;
      }
    }
    stSize = cp + 1;

    if (isControllable(systemState)) {
      return setSatisfiedResult();
    } else {
      final SafetyTraceProxy counterexample = computeCounterExample();
      return setFailedResult(counterexample);
    }
  }


  //#########################################################################
  //# Interface net.sourceforge.waters.model.analysis.SafetyVerifier
  public void setKindTranslator(KindTranslator translator)
  {
    mKindTranslator = translator;
    clearAnalysisResult();
  }

  public KindTranslator getKindTranslator()
  {
    return mKindTranslator;
  }

  public SafetyTraceProxy getCounterExample()
  {
    return (SafetyTraceProxy) super.getCounterExample();
  }


  //#########################################################################
  //# Auxiliary Methods
  /**
   * Check the controllability of the model with a parameter of
   * initial synchronous product.
   * @parameter sState The initial synchronous product of the model
   * @return <CODE>true</CODE> if the model is controllable, or
   *         <CODE>false</CODE> if it is not.
   */
  private boolean isControllable(int[] sState)
  {
    THashSet systemSet = new THashSet();
    boolean enabled = true;

    // Add the initial synchronous product in systemSet and mStateList
    successor = new int[automatonSize];
    stateTuple = new StateTuple(stSize);
    encode(sState,stateTuple);
    systemSet.add(stateTuple);
    mStateList.add(stateTuple);
    mIndexList.add(mStateList.size()-1);

    int indexSize = 0;
    int eventSize = mEventCodingList.size();
    int i,j,k,temp;

    while(true){
      // For each current state in the current level, check its controllability
      indexSize = mIndexList.size();
      for (j = (indexSize == 1) ? 0 : (mIndexList.get(indexSize-2)+1);
           j <= mIndexList.get(indexSize-1);
           j++) {
        decode(mStateList.get(j),systemState);
        for (int e=0;e<eventSize;e++) {
          // Retrieve all enabled events
          enabled = true;
          for (i = 0; i < plantSize; i++) {
            if (mPlantEventList.get(i)[e] == 1){
              temp = mPlantTransitionMap.get(i)[systemState[i]][e];
              if (temp == -1) {
                enabled = false;
                break;
              }
              else if (temp > -1){
                successor[i] = temp;
                continue;
              }
            }
            successor[i] = systemState[i];
          }
          if (!enabled) {
            continue;
          }

          // Check controllability of current state
          final EventProxy event = mEventCodingList.get(e);
          final EventKind kind = mKindTranslator.getEventKind(event);
          if (kind == EventKind.UNCONTROLLABLE) {
            for (i = 0; i < automatonSize - plantSize; i++) {
              if (mSpecEventList.get(i)[e] == 1) {
                temp = mSpecTransitionMap.get(i)[systemState[i+plantSize]][e];
                if (temp == -1) {
                  errorEvent = e;
                  return false;
                }
                if (temp > -1) {
                  successor[i + plantSize] = temp;
                  continue;
                }
              }
              successor[i + plantSize] = systemState[i + plantSize];
            }
          } else {
            for (k = 0; k < automatonSize - plantSize; k++){
              if (mSpecEventList.get(k)[e] == 1) {
                temp = mSpecTransitionMap.get(k)[systemState[k+plantSize]][e];
                if (temp == -1) {
                  enabled = false;
                  break;
                }
                if (temp > -1){
                  successor[k+plantSize] = temp;
                  continue;
                }
              }
              successor[k+plantSize] = systemState[k+plantSize];
            }
            if (!enabled) {
              continue;
            }
          }

          // Encode the new system state and put it into mStateList
          stateTuple = new StateTuple(stSize);
          encode(successor,stateTuple);
          if (systemSet.add(stateTuple)) {
            mStateList.add(stateTuple);
          }
        }
      }
      // If mStateList has added a new state, update mIndexList at the last loop
      // of current level
      if (mStateList.size() != mIndexList.get(indexSize - 1) + 1) {
        mIndexList.add(mStateList.size() - 1);
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
   * @param sState The state to be encoded
   * @param sTuple The encoded StateTuple
   */
  private void encode(final int[] sState, final StateTuple sTuple)
  {
    int i;
    int k = 0;
    int result = 0;
    final int[] codes = sTuple.getCodes();
    for (i = 0; i < automatonSize; i++) {
      if (mCodePosition[i] == k) {
        result <<= mBitLengthList[i];
        result |= sState[i];
      } else {
        codes[k] = result;
        result = sState[i];
        k++;
      }
      if (i == automatonSize - 1) {
        codes[k] = result;
      }
    }
  }


  //#########################################################################
  //# Decoding
  /**
   * Decode the StateTuple
   * @parameter sTuple The StateTuple to be decoded
   * @parameter state  The decoded state
   */
  private void decode(StateTuple sTuple, int[] state)
  {
    int i;
    int result;
    int k = mCodePosition[automatonSize - 1];
    int temp = sTuple.get(k);
    for (i = automatonSize - 1; i > -1; i--) {
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
   * representing a controllability error trace. A controllability error
   * trace is a nonempty sequence of events such that all except the last
   * event in the list can be executed by the model. The last event in list
   * is an uncontrollable event that is possible in all plant automata, but
   * not in all specification automata present in the model. Thus, the last
   * step demonstrates why the model is not controllable.
   * @return A trace object representing the counterexample.
   *         The returned trace is constructed for the input product DES
   *         of this controllability checker and shares its automata and
   *         event objects.
   * @throws IllegalStateException if this method is called before
   *         model checking has completed, i.e., before {@link #run() run()}
   *         has been called, or model checking has found that the
   *         property is satisfied and there is no counterexample.
   */
  private SafetyTraceProxy computeCounterExample()
  {
    // The following creates a trace that consists of all the events in
    // the input model.
    final ProductDESProxyFactory factory = getFactory();
    final ProductDESProxy des = getModel();
    final String desname = des.getName();
    final String tracename = desname + ":uncontrollable";
    final List<EventProxy> tracelist = new LinkedList<EventProxy>();

    boolean enabled;
    boolean found = false;
    int i, j, k, temp;
    int indexSize = mIndexList.size();
    int[] errorState = new int[automatonSize];

    tracelist.add(0, mEventCodingList.get(errorEvent));

    while(true){
      for (i = 0; i < automatonSize; i++) {
        errorState[i] = systemState[i];
      }
      mIndexList.remove(--indexSize);
      if(mIndexList.size()==0) break;
      // Backward search the previous level states, compute their
      // successors and compare them with the error state
      for (j = indexSize == 1 ? 0 : mIndexList.get(indexSize - 2) + 1;
           j <= mIndexList.get(indexSize - 1);
           j++) {
        decode(mStateList.get(j), systemState);
        for (int e = 0; e < eventSize; e++) {
          enabled = true;
          for (i = 0; i < plantSize; i++) {
            if (mPlantEventList.get(i)[e] == 1){
              temp = mPlantTransitionMap.get(i)[systemState[i]][e];
              if (temp == -1) {
                enabled = false;
                break;
              } else if (temp > -1){
                successor[i] = temp;
                continue;
              }
            }
            successor[i] = systemState[i];
          }
          if (!enabled) {
            continue;
          }

          for (k = 0; k < automatonSize - plantSize; k++) {
            if (mSpecEventList.get(k)[e] == 1) {
              temp = mSpecTransitionMap.get(k)[systemState[k+plantSize]][e];
              if (temp == -1) {
                enabled = false;
                break;
              }
              if (temp > -1) {
                successor[k + plantSize] = temp;
                continue;
              }
            }
            successor[k + plantSize] = systemState[k + plantSize];
          }
          if (!enabled) {
            continue;
          }

          if (Arrays.equals(successor, errorState)) {
            found = true;
            tracelist.add(0, mEventCodingList.get(e));
            break;
          }
        }
        if (found) {
          found = false;
          break;
        }
      }
    }
    final SafetyTraceProxy trace =
      factory.createSafetyTraceProxy(tracename, des, tracelist);
    return trace;
  }


  //#########################################################################
  //# Data Members
  private KindTranslator mKindTranslator;

  private Set<AutomatonProxy> mAutomatonSet;

  // Transition map
  private ArrayList<int[][]> mPlantTransitionMap;
  private ArrayList<int[][]> mSpecTransitionMap;

  // Level states storage
  private ArrayList<Integer> mIndexList;
  private BlockedArrayList<StateTuple> mStateList;

  // For encoding/decoding
  private ArrayList<EventProxy> mEventCodingList;
  private ArrayList<byte[]> mPlantEventList;
  private ArrayList<byte[]> mSpecEventList;
  private int[] mBitLengthList;
  private int[] mMaskList;
  private int[] mCodePosition;
  private StateTuple stateTuple;

  // Size
  private int automatonSize;
  private int eventSize;
  private int plantSize;
  private int stSize;

  // For computing successor and counterexample
  private int[] systemState;
  private int[] successor;
  private int errorEvent;

}
