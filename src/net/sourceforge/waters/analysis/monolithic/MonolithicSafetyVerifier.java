//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# Copyright (C) 2004-2023 Robi Malik
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

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import net.sourceforge.waters.model.analysis.AnalysisAbortException;
import net.sourceforge.waters.model.analysis.AnalysisException;
import net.sourceforge.waters.model.analysis.OverflowException;
import net.sourceforge.waters.model.analysis.VerificationResult;
import net.sourceforge.waters.model.analysis.des.AbstractSafetyVerifier;
import net.sourceforge.waters.model.analysis.des.NondeterministicDESException;
import net.sourceforge.waters.model.analysis.des.SafetyDiagnostics;
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

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


/**
 * <P>A Java implementation of the controllability check algorithm.
 * This algorithm does a brute-force state exploration to check whether
 * the given model is controllable.</P>
 *
 * @author Jinjian Shi
 */

public class MonolithicSafetyVerifier
  extends AbstractSafetyVerifier
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
                                  final SafetyDiagnostics diag,
                                  final ProductDESProxyFactory factory)
  {
    this(null, translator, diag, factory);
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
                                  final SafetyDiagnostics diag,
                                  final ProductDESProxyFactory factory)
  {
    super(model, translator, diag, factory);
  }


  //#########################################################################
  //# Invocation
  @Override
  public boolean run()
    throws AnalysisException
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
          aneventCodingList[mEventCodingList.indexOf(evp)] = 1;
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
            throw new NondeterministicDESException
              (ap, tp.getSource(), tp.getEvent());
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
          if (kind == ComponentKind.PLANT ||
              translator.getEventKind(KindTranslator.INIT) ==
              EventKind.CONTROLLABLE) {
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
        final String traceName = getTraceName();
        final String comment =
          getTraceComment(null, initUncontrollable, null);
        final TraceStepProxy step = factory.createTraceStepProxy(null);
        final List<TraceStepProxy> steps = Collections.singletonList(step);
        final TraceProxy trace = factory.createTraceProxy(steps);
        final SafetyCounterExampleProxy counter =
          factory.createSafetyCounterExampleProxy(traceName, comment, null,
                                                  model, automata, trace);
        return setFailedResult(counter);
      }

      // Set the mCodePosition list
      for (i = 0; i < mNumAutomata; i++) {
        codeLength += mBitLengthList[i];
        if (codeLength <= 32){
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
        final SafetyCounterExampleProxy counter = computeCounterExample();
        return setFailedResult(counter);
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
   * Check the controllability of the model with a parameter of
   * initial synchronous product.
   * @param sState The initial synchronous product of the model
   * @return <CODE>true</CODE> if the model is controllable, or
   *         <CODE>false</CODE> if it is not.
   */
  private boolean isControllable(final int[] sState)
    throws AnalysisException
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
    mIndexList.add(mStateSpace.size()-1);

    int indexSize = 0;
    final int mNumEvents = mEventCodingList.size();
    int i,j,k,temp;

    while (true) {
      // For each current state in the current level, check its controllability
      indexSize = mIndexList.size();
      for (j = (indexSize == 1) ? 0 : (mIndexList.get(indexSize - 2) + 1);
           j <= mIndexList.get(indexSize-1);
           j++) {
        decode(mStateSpace.get(j),mSystemState);
        for (int e = 0; e < mNumEvents; e++) {
          // Retrieve all enabled events
          enabled = true;
          for (i = 0; i < mNumPlants; i++) {
            if (mPlantEventList.get(i)[e] == 1){
              temp = mPlantTransitionMap.get(i)[mSystemState[i]][e];
              if (temp == -1) {
                enabled = false;
                break;
              }
              else if (temp > -1){
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
                temp =
                  mSpecTransitionMap.get(i)[mSystemState[si]][e];
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
            for (k = 0; k < mNumAutomata - mNumPlants; k++){
              if (mSpecEventList.get(k)[e] == 1) {
                temp =
                  mSpecTransitionMap.get(k)[mSystemState[k + mNumPlants]][e];
                if (temp == -1) {
                  enabled = false;
                  break;
                }
                if (temp > -1){
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
   * @param sState The state to be encoded
   * @param sTuple The encoded StateTuple
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
   * @param sTuple The StateTuple to be decoded
   * @param state  The decoded state
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
  private SafetyCounterExampleProxy computeCounterExample()
    throws AnalysisAbortException
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
    final List<StateProxy> codes0 = new ArrayList<StateProxy>(aut.getStates());
    final int code0 = mSystemState[mErrorAutomaton];
    final StateProxy state0 = codes0.get(code0);
    final TraceStepProxy step0 = factory.createTraceStepProxy(event0);
    steps.add(0, step0);

    while(true){
      for (i = 0; i < mNumAutomata; i++) {
        errorState[i] = mSystemState[i];
      }
      mIndexList.remove(--indexSize);
      if(mIndexList.size()==0) break;
      // Backward search the previous level states, compute their
      // successors and compare them with the error state
      for (j = indexSize == 1 ? 0 : mIndexList.get(indexSize - 2) + 1;
           j <= mIndexList.get(indexSize - 1);
           j++) {
        decode(mStateSpace.get(j), mSystemState);
        for (int e = 0; e < mNumEvents; e++) {
          enabled = true;
          for (i = 0; i < mNumPlants; i++) {
            if (mPlantEventList.get(i)[e] == 1){
              temp = mPlantTransitionMap.get(i)[mSystemState[i]][e];
              if (temp == -1) {
                enabled = false;
                break;
              } else if (temp > -1){
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
              temp = mSpecTransitionMap.get(k)[mSystemState[k+mNumPlants]][e];
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
    final String traceName = getTraceName();
    final String comment = getTraceComment(event0, aut, state0);
    final List<AutomatonProxy> automata = Arrays.asList(mAutomata);
    final TraceProxy trace = factory.createTraceProxy(steps);
    return factory.createSafetyCounterExampleProxy(traceName, comment, null,
                                                   des, automata, trace);
  }


  //#########################################################################
  //# Data Members
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
