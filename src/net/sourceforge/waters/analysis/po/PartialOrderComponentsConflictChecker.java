//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# Copyright (C) 2004-2018 Robi Malik
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
import java.util.LinkedList;
import java.util.List;

import net.sourceforge.waters.analysis.monolithic.StateHashSet;
import net.sourceforge.waters.model.analysis.AnalysisAbortException;
import net.sourceforge.waters.model.analysis.AnalysisException;
import net.sourceforge.waters.model.analysis.ConflictKindTranslator;
import net.sourceforge.waters.model.analysis.KindTranslator;
import net.sourceforge.waters.model.analysis.des.AbstractConflictChecker;
import net.sourceforge.waters.model.analysis.des.ConflictChecker;
import net.sourceforge.waters.model.analysis.des.EventNotFoundException;
import net.sourceforge.waters.model.des.AutomatonProxy;
import net.sourceforge.waters.model.des.ConflictTraceProxy;
import net.sourceforge.waters.model.des.EventProxy;
import net.sourceforge.waters.model.des.ProductDESProxy;
import net.sourceforge.waters.model.des.ProductDESProxyFactory;
import net.sourceforge.waters.model.des.StateProxy;
import net.sourceforge.waters.model.des.TraceProxy;
import net.sourceforge.waters.model.des.TraceStepProxy;
import net.sourceforge.waters.xsd.base.ComponentKind;
import net.sourceforge.waters.xsd.des.ConflictKind;

/**
 * <P>
 * A Java implementation of the controllability check algorithm. This
 * algorithm does a brute-force state exploration to check whether the given
 * model is controllable.
 * </P>
 *
 * @author Adrian Shaw
 */

public class PartialOrderComponentsConflictChecker
extends PartialOrderComponentsModelVerifier implements ConflictChecker
{
  //#########################################################################
  //# Constructors
  /**
   * Creates a new conflict checker without a model or marking proposition.
   */
  public PartialOrderComponentsConflictChecker(final ProductDESProxyFactory factory)
  {
    this(null,factory);
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
  public PartialOrderComponentsConflictChecker(final ProductDESProxy model,
                                   final ProductDESProxyFactory factory)
  {
    super(model, factory, ConflictKindTranslator.getInstanceUncontrollable());
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
  public PartialOrderComponentsConflictChecker(final ProductDESProxy model,
                                   final EventProxy marking,
                                   final ProductDESProxyFactory factory)
  {
    super(model, factory, ConflictKindTranslator.getInstanceUncontrollable());
    mConfiguredMarking = marking;
    mUsedMarking = null;
  }


  //#########################################################################
  //# Interface net.sourceforge.waters.model.analysis.ConflictChecker
  @Override
  public void setConfiguredDefaultMarking(final EventProxy marking)
  {
    mConfiguredMarking = marking;
    mUsedMarking = null;
  }

  @Override
  public EventProxy getConfiguredDefaultMarking()
  {
    return mConfiguredMarking;
  }

  @Override
  public void setConfiguredPreconditionMarking(final EventProxy marking)
  {
  }

  @Override
  public EventProxy getConfiguredPreconditionMarking()
  {
    return null;
  }

  @Override
  public ConflictTraceProxy getCounterExample()
  {
    return (ConflictTraceProxy) super.getCounterExample();
  }

  @Override
  public void tearDown()
  {
    super.tearDown();
    mUsedMarking = null;
  }


  //#########################################################################
  //# Auxiliary Methods
  @Override
  protected int[][] setupTransitions(final List<StateProxy> codes,
                                     final ComponentKind kind)
  {
    final int stateSize = codes.size();
    final int[][] atransition = stateSize > 0 ? new int[stateSize+1][mNumEvents]:
      new int[stateSize][mNumEvents];
    for (int i = 0; i < stateSize; i++) {
      for (int j = 0; j < mNumEvents; j++) {
        atransition[i][j] = -1;
        atransition[stateSize][j] = stateSize;
      }
      try {
        if (codes.get(i).getPropositions().contains(getUsedDefaultMarking())){
          atransition[i][0] = stateSize;
        }
      } catch (final EventNotFoundException e) {
        e.printStackTrace();
      }
    }
    return atransition;
  }

  @Override
  protected boolean isSupportedEvent(final EventProxy event)
    throws EventNotFoundException
  {
    final KindTranslator translator = getKindTranslator();
    switch (translator.getEventKind(event)) {
    case CONTROLLABLE:
    case UNCONTROLLABLE:
      return true;
    case PROPOSITION:
      return event == getUsedDefaultMarking();
    default:
      throw new IllegalArgumentException
        ("Unknown event kind " + translator.getEventKind(event) + "!");
    }
  }

  @Override
  protected boolean isValid(final int[] sState) throws AnalysisException{
    mDepthIndex = 0;
    mComponentNumber = 0;
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
      final PartialOrderStateTuplePairing current =
        mStack.remove(mStack.size() - 1);
      final PartialOrderStateTuple state = current.getState();
      final PartialOrderStateTuple prev = current.getPrev();
      if (current.getReq() == PartialOrderParingRequest.VISIT &&
                                                !state.getComponentVisited()){
        state.setComponentVisited(true);
        state.setRootIndex(mDepthIndex++);
        final int[] events = ample(state);
        if(events.length == 0){
          state.setComponent(++mComponentNumber);
          if(!isMarked(state)){
            setConflictResult(ConflictKind.DEADLOCK);
            return false;
          }
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
              prev.setComponent(++mComponentNumber);
              if(!isMarked(prev)){
                if (!canReachExternalComponent(prev)){
                  setConflictResult(ConflictKind.LIVELOCK);
                  return false;
                }
              }
              continue;
            }
            for (int i = lastIndex; i >= componentRootIndex && !fullyExpanded; i--) {
              fullyExpanded |= mComponentStack.get(i).getFullyExpanded();
            }
            if (fullyExpanded) {
              boolean blocking = true;
              mComponentNumber++;
              for (int i = lastIndex; i >= componentRootIndex; i--) {
                final PartialOrderStateTuple temp = mComponentStack.remove(i);

                temp.setComponent(mComponentNumber);
                if(blocking){
                  if(isMarked(temp)){
                    blocking = false;
                  }
                  else if(mComponentNumber != 1){
                    if(canReachExternalComponent(temp)){
                      blocking = false;
                    }
                  }
                }
              }
              if(blocking){
                setConflictResult(ConflictKind.LIVELOCK);
                return false;
              }
            } else {
              expand(prev, enabled(prev).toArray(), false);
              prev.setFullyExpanded(true);
              mFullExpansions++;
            }
          }
        }
      }
    }
    return true;
  }

  private boolean isMarked(final PartialOrderStateTuple current){
    final int[] tempState = new int[mNumAutomata];;
    decode(current, tempState);
    boolean marked = true;
    for (int i = 0; i < mNumAutomata; i++){
      marked &=  mPlantTransitionMap.get(i)[tempState[i]][0] > -1 ||
                                               mPlantEventList.get(i)[0] == 0;
    }
    return marked;
  }

  private boolean canReachExternalComponent(final PartialOrderStateTuple current){

    final int[] tempSuccessor = new int[mNumAutomata];
    final int[] tempState = new int[mNumAutomata];
    int i;
    decode(current, tempState);
    final int[] enabledEvents =  enabled(current).toArray();
    for(final int e: enabledEvents){
      for (i = 0; i < mNumAutomata; i++){
        final boolean plant = i < mNumPlants;
        final int si = i - mNumPlants;
        if ((plant ?
          mPlantEventList.get(i)[e]:mSpecEventList.get(si)[e]) != 1){
          tempSuccessor[i] = tempState[i];

        }
        else {
          tempSuccessor[i] = plant ? mPlantTransitionMap.get(i)[tempState[i]][e] :
            mSpecTransitionMap.get(si)[tempState[i]][e];
        }
      }
      PartialOrderStateTuple successor = new PartialOrderStateTuple(mStateTupleSize);
      encode(tempSuccessor,successor);
      successor = mStateSet.get(successor);
      if(successor!=null && successor.isInComponent()){
        if(successor.getComponent() != mComponentNumber){
          return true;
        }
      }
    }
    return false;
  }

  private void setConflictResult(final ConflictKind result){
    mConflictResult = result;
  }

  private ConflictKind getConflictResult(){
    return mConflictResult;
  }

  @Override
  protected boolean isErrorState(final PartialOrderStateTuple current){
    if (current.getComponent() == mComponentNumber){
      mErrorState = current;
      return true;
    }
    return false;
  }

  @Override
  protected TraceProxy computePOCounterExample() throws AnalysisAbortException
  {
    final ProductDESProxyFactory factory = getFactory();
    final ProductDESProxy des = getModel();
    final List<TraceStepProxy> steps = new LinkedList<TraceStepProxy>();

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
        for (j = 1; j < mNumEvents; j++){
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
    final List<AutomatonProxy> automata = Arrays.asList(mAutomata);
    final ConflictTraceProxy trace =
      factory.createConflictTraceProxy(tracename, null, null, des, automata,
                                     steps,getConflictResult());
    return trace;
  }

  @Override
  protected TraceProxy noInitialCounterexample(final AutomatonProxy ap,
                                    final ProductDESProxy model,
                                    final Collection<AutomatonProxy> automata)
  {
    return null;
  }


  //#########################################################################
  //# Auxiliary Methods
  /**
   * Gets the marking proposition to be used.
   * This method returns the marking proposition specified by the {@link
   * #setConfiguredDefaultMarking(EventProxy) setMarkingProposition()} method,
   * if non-null, or the default marking proposition of the input model.
   * @throws EventNotFoundException to indicate that the a
   *         <CODE>null</CODE> marking was specified, but input model does
   *         not contain any proposition with the default marking name.
   */
  private EventProxy getUsedDefaultMarking()
    throws EventNotFoundException
  {
    if (mUsedMarking == null) {
      if (mConfiguredMarking == null) {
        final ProductDESProxy model = getModel();
        mUsedMarking = AbstractConflictChecker.getMarkingProposition(model);
      } else {
        mUsedMarking = mConfiguredMarking;
      }
    }
    return mUsedMarking;
  }

  /**
   * Gets a name that can be used for a counterexample for the current model.
   */
  private String getTraceName()
  {
    final ProductDESProxy model = getModel();
    return AbstractConflictChecker.getTraceName(model);
  }


  //#########################################################################
  //# Data Members
  // Conflict information
  private ConflictKind mConflictResult;

  // Component information
  private int mComponentNumber;

  //Marking information
  private EventProxy mUsedMarking;
  private EventProxy mConfiguredMarking;

}
