//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# Copyright (C) 2004-2015 Robi Malik
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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import net.sourceforge.waters.analysis.monolithic.StateHashSet;
import net.sourceforge.waters.model.analysis.AnalysisAbortException;
import net.sourceforge.waters.model.analysis.AnalysisException;
import net.sourceforge.waters.model.analysis.KindTranslator;
import net.sourceforge.waters.model.analysis.des.SafetyDiagnostics;
import net.sourceforge.waters.model.analysis.des.SafetyVerifier;
import net.sourceforge.waters.model.des.AutomatonProxy;
import net.sourceforge.waters.model.des.EventProxy;
import net.sourceforge.waters.model.des.ProductDESProxy;
import net.sourceforge.waters.model.des.ProductDESProxyFactory;
import net.sourceforge.waters.model.des.SafetyTraceProxy;
import net.sourceforge.waters.model.des.StateProxy;
import net.sourceforge.waters.model.des.TraceProxy;
import net.sourceforge.waters.model.des.TraceStepProxy;
import net.sourceforge.waters.xsd.base.ComponentKind;

/**
 * <P>
 * A Java implementation of the controllability check algorithm. This
 * algorithm does a brute-force state exploration to check whether the given
 * model is controllable.
 * </P>
 *
 * @author Adrian Shaw
 */

public class PartialOrderComponentsSafetyVerifier
extends PartialOrderComponentsModelVerifier implements SafetyVerifier
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
  public PartialOrderComponentsSafetyVerifier(final KindTranslator translator,
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
  public PartialOrderComponentsSafetyVerifier(final ProductDESProxy model,
                                    final KindTranslator translator,
                                    final SafetyDiagnostics diag,
                                    final ProductDESProxyFactory factory)
  {
    super(model, factory, translator);
    mDiagnostics = diag;
  }

//#########################################################################
  //# Interface net.sourceforge.waters.model.analysis.SafetyVerifier
  @Override
  public SafetyDiagnostics getDiagnostics()
  {
    return mDiagnostics;
  }

  @Override
  public SafetyTraceProxy getCounterExample()
  {
    return (SafetyTraceProxy) super.getCounterExample();
  }

  //#########################################################################
  //# Auxiliary Methods
  @Override
  protected int[][] setupTransitions(final List<StateProxy> codes,
                                     final ComponentKind kind)
  {
    final int stateSize = codes.size();
    final int[][] atransition = kind == ComponentKind.SPEC ?
      new int[stateSize+1][mNumEvents]:new int[stateSize][mNumEvents];
    for (int i = 0; i < stateSize; i++) {
      for (int j = 0; j < mNumEvents; j++) {
        atransition[i][j] = -1;
        if(kind == ComponentKind.SPEC){
          atransition[stateSize][j] = stateSize;
        }
      }
    }
    return atransition;
  }

  @Override
  protected boolean isValid(final int[] sState) throws AnalysisException{
    mDepthIndex = 0;
    mComponentCount = 0;
    mFullExpansions = 0;
    mStack = new ArrayList<PartialOrderStateTuplePairing>();
    mStateSet = new StateHashSet<PartialOrderStateTuple>(PartialOrderStateTuple.class);
    mSuccessor = new int[mNumAutomata];

    mComponentStack = new ArrayList<PartialOrderStateTuple>();
    mInitialState = new PartialOrderStateTuple(mStateTupleSize);

    encode(sState, mInitialState);

    mStateSet.getOrAdd(mInitialState);
    mStack.add(new PartialOrderStateTuplePairing(mInitialState, null,
                                            PartialOrderParingRequest.VISIT));
    mStateTuple = new PartialOrderStateTuple(mStateTupleSize);

    while(!mStack.isEmpty()){
      final PartialOrderStateTuplePairing current = mStack.remove(mStack.size() - 1);
      final PartialOrderStateTuple state = current.getState();
      final PartialOrderStateTuple prev = current.getPrev();
      if (current.getReq() == PartialOrderParingRequest.VISIT &&
                                                !state.getComponentVisited()){
        state.setComponentVisited(true);
        state.setRootIndex(mDepthIndex++);
        int[] events;
        if((events = ample(state))==null)
          return false;
        if(events.length == 0){
          state.setComponent(++mComponentCount);
        }
        else{
          mStack.add(new PartialOrderStateTuplePairing(state, prev,
                                            PartialOrderParingRequest.CLOSE));
          expand(state,events,true);
        }
      }
      else if (prev != null){
        if (!state.isInComponent()){
          final int oldRoot = prev.getRootIndex();
          prev.setRootIndex(Math.min(prev.getRootIndex(), state.getRootIndex()));
          if (oldRoot != prev.getRootIndex()){
            prev.setRootChanged(true);
          }
        }
        final PartialOrderStateTuplePairing newTop = mStack.get(mStack.size()-1);
        if (!(newTop.getPrev() == prev &&
              newTop.getReq() == PartialOrderParingRequest.VISIT)) {
          if (!prev.getRootChanged()) {
            boolean fullyExpanded = false;
            final int componentRootIndex = mComponentStack.indexOf(prev);
            final int lastIndex = mComponentStack.size() - 1;
            if (lastIndex == componentRootIndex) {
              mComponentStack.remove(lastIndex);
              prev.setComponent(++mComponentCount);
              continue;
            }
            for (int i = lastIndex; i >= componentRootIndex && !fullyExpanded; i--) {
              fullyExpanded |= mComponentStack.get(i).getFullyExpanded();
            }
            if (fullyExpanded) {
              mComponentCount++;
              for (int i = lastIndex; i >= componentRootIndex; i--) {
                final PartialOrderStateTuple temp = mComponentStack.remove(i);
                temp.setComponent(mComponentCount);
              }
            } else {
              int[] enabled;
              if ((enabled = enabled(prev).toArray()) == null)
                return false;
              expand(prev, enabled, false);
              prev.setFullyExpanded(true);
              mFullExpansions++;
            }
          }
        }
      }
    }
    return true;
  }

  @Override
  protected boolean isErrorState(final PartialOrderStateTuple current){
    return current == mErrorState;
  }

  @Override
  protected TraceProxy noInitialCounterexample(final AutomatonProxy ap,
                                    final ProductDESProxy model,
                                    final Collection<AutomatonProxy> automata)
  {
    final ProductDESProxyFactory factory = getFactory();
    final String tracename = getTraceName();
    final String comment =
      getTraceComment(null, ap, null);
    final TraceStepProxy step = factory.createTraceStepProxy(null);
    final List<TraceStepProxy> steps = Collections.singletonList(step);
    return factory.createSafetyTraceProxy(tracename, comment, null, model,
                                          automata, steps);
  }

  @Override
  protected TraceProxy computePOCounterExample() throws AnalysisAbortException
  {
    final ProductDESProxyFactory factory = getFactory();
    final ProductDESProxy des = getModel();
    final List<TraceStepProxy> steps = new LinkedList<TraceStepProxy>();
    final EventProxy errorEvent = getEvent(mErrorEvent);
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
      for (i = mIndexList.get(currentLevel - 1);
                i < mIndexList.get(currentLevel); i++){
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
            final EventProxy event = getEvent(j);
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

  /**
   * Gets a name that can be used for a counterexample for the current model.
   */
  protected String getTraceName()
  {
    final ProductDESProxy des = getModel();
    if (mDiagnostics == null) {
      final String desname = des.getName();
      return desname + "-unsafe";
    } else {
      return mDiagnostics.getTraceName(des);
    }
  }

  /**
   * Generates a comment to be used for a counterexample generated for
   * the current model.
   * @param  event  The event that causes the safety property under
   *                investigation to fail.
   * @param  aut    The automaton that fails to accept the event,
   *                which causes the safety property under investigation to
   *                fail.
   * @param  state  The state in the automaton that fails to accept the event,
   *                which causes the safety property under investigation to
   *                fail.
   * @return An English string that describes why the safety property is
   *         violated, which can be used as a trace comment.
   */
  protected String getTraceComment(final EventProxy event,
                                   final AutomatonProxy aut,
                                   final StateProxy state)
  {
    if (mDiagnostics == null) {
      return null;
    } else {
      final ProductDESProxy des = getModel();
      return mDiagnostics.getTraceComment(des, event, aut, state);
    }
  }

  //#########################################################################
  //# Data Members

  //Statistics
  @SuppressWarnings("unused")
  private int mNumIndependentPairings;
  private final SafetyDiagnostics mDiagnostics;
}
