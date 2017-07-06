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

package net.sourceforge.waters.analysis.monolithic;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import net.sourceforge.waters.analysis.tr.ListBufferTransitionRelation;
import net.sourceforge.waters.analysis.tr.TRAutomatonProxy;
import net.sourceforge.waters.model.analysis.AnalysisException;
import net.sourceforge.waters.model.analysis.ConflictKindTranslator;
import net.sourceforge.waters.model.analysis.OverflowException;
import net.sourceforge.waters.model.analysis.VerificationResult;
import net.sourceforge.waters.model.analysis.des.DeadlockChecker;
import net.sourceforge.waters.model.analysis.des.EventNotFoundException;
import net.sourceforge.waters.model.des.AutomatonProxy;
import net.sourceforge.waters.model.des.ConflictTraceProxy;
import net.sourceforge.waters.model.des.EventProxy;
import net.sourceforge.waters.model.des.ProductDESProxy;
import net.sourceforge.waters.model.des.ProductDESProxyFactory;
import net.sourceforge.waters.model.des.StateProxy;
import net.sourceforge.waters.model.des.TraceStepProxy;
import net.sourceforge.waters.xsd.des.ConflictKind;

import org.apache.log4j.Logger;


/**
 * A Java implementation of the monolithic deadlock check algorithm,
 * based on {@link ListBufferTransitionRelation} as automaton representation.
 *
 * @author Hani al-Bahri
 */

public class TRDeadlockChecker
  extends TRAbstractModelVerifier
  implements DeadlockChecker
{

  //#########################################################################
  //# Constructors
  public TRDeadlockChecker()
  {
    super(ConflictKindTranslator.getInstanceControllable());
  }

  public TRDeadlockChecker(final ProductDESProxy model)
  {
    super(model, ConflictKindTranslator.getInstanceControllable());
  }


  //#########################################################################
  //# Interface net.sourceforge.waters.model.analysis.des.DeadlockChecker
  @Override
  public ConflictTraceProxy getCounterExample()
  {
    return (ConflictTraceProxy) super.getCounterExample();
  }


  //#########################################################################
  //# Invocation
  @Override
  protected void setUp()
    throws AnalysisException
  {
    super.setUp();
  }

  @Override
  public boolean run()
    throws AnalysisException
  {

    try {
      setUp();
      exploreStateSpace();
      final VerificationResult result = getAnalysisResult();
      if (!result.isFinished()) {
        result.setSatisfied(true);
      }
      return result.isSatisfied();
    } catch (final AnalysisException exception) {
      throw setExceptionResult(exception);
    } catch (final OutOfMemoryError error) {
      tearDown();
      final Logger logger = getLogger();
      logger.debug("<out of memory>");
      final OverflowException exception = new OverflowException(error);
      throw setExceptionResult(exception);
    } catch (final StackOverflowError error) {
      final OverflowException exception = new OverflowException(error);
      throw setExceptionResult(exception);
    } finally {
      tearDown();
    }
  }

  @Override
  protected void tearDown()
  {
    super.tearDown();
  }


  @Override
  protected void expandState(final int[] encoded, final int[] decoded)
    throws OverflowException, EventNotFoundException
  {
    boolean isDeadlock = true;
    for (final EventInfo event : getEventInfo()) {
      if (event.isEnabled(decoded)) {
        createSuccessorStates(encoded, decoded, event);
        isDeadlock = false;
      }
    }
    if (isDeadlock) {
      final ConflictTraceProxy cp = createTrace(getCurrentSource());
      setFailedResult(cp);
    }
  }


  //#########################################################################
  //# Auxiliary Methods
  /**
   * Gets a name that can be used for a counterexample for the current model.
   */
  protected String getTraceName()
  {
    final ProductDESProxy model = getModel();
    return getTraceName(model);
  }

  /**
   * Gets a name that can be used for a counterexample for the given model.
   */
  public static String getTraceName(final ProductDESProxy model)
  {
    final String modelname = model.getName();
    return modelname + "-deadlock";
  }


  //#########################################################################
  //# Methods to Build the Counterexample
  private ConflictTraceProxy createTrace(final int source)
    throws OverflowException, EventNotFoundException
  {
    setUpReverseTransitions();
    final DeadlockCallback deadlockCallback = new DeadlockCallback();
    setStateCallback(deadlockCallback);
    final int firstDeadlockState = source;
    final ConflictTraceProxy counterexample =
      buildCounterExample(firstDeadlockState,
                          getTRAutomata(),
                          deadlockCallback);
    return counterexample;
  }

  private ConflictTraceProxy buildCounterExample(final int firstDeadlockState,
                                                 final TRAutomatonProxy[] automataArray,
                                                 final DeadlockCallback callback)
    throws OverflowException
  {
    // Generate a counter example. As each state is numbered in the
    // order it is encountered, and a breadth first exploration
    // strategy is used, and all states are reachable, following the
    // transition to a state with the lowest id will give a
    // counterexample.
    final List<TraceStepProxy> steps = new LinkedList<TraceStepProxy>();
    final int numaut = getTRAutomata().length;
    final Map<AutomatonProxy,StateProxy> stateMap =
      new HashMap<AutomatonProxy,StateProxy>(numaut);
    final int numInit = getNumberOfInitialStates();
    final ProductDESProxyFactory factory = getFactory();
    final int[] encodedSource =
      new int[getStateTupleEncoding().getNumberOfWords()];
    final int[] decodedSource =
      new int[getStateTupleEncoding().getNumberOfAutomata()];
    // Find the unchecked state with the lowest
    // id, as this should give the shortest counterexample.
    // or if a second marking condition is simultaneously used, look
    // for the first non-coreachable precondition marked state.
    int current = firstDeadlockState;
    getStateSpace().getContents(current, encodedSource);
    getStateTupleEncoding().decode(encodedSource, decodedSource);
    // Until we reach the start state...
    do {
      for (final EventInfo event : getEventInfo()) {
        if (event.isEnabled(decodedSource)) {
          callback.setEvent(event.getEvent());
          // expand ..
          createSuccessorStates(encodedSource, decodedSource, event);
        }
      }
      final int next = callback.getSmallestStateIndex();
      for (int i = 0; i < automataArray.length; i++) {
        final TRAutomatonProxy aut = automataArray[i];
        final int s = decodedSource[i];
        final StateProxy state = aut.getState(s);
        //getStateSpace()
        stateMap.put(aut, state);
        /*
         * final AutomatonSchema schema = automata[a]; final AutomatonProxy
         * aut = schema.getAutomatonProxy(); final int s = tuple[a]; final
         * StateProxy state = schema.getStateProxyFromID(s); stateMap.put(aut,
         * state);
         */
      }
      final TraceStepProxy step;
      if (current >= numInit) {
        final EventProxy event = callback.getSmallestStateEvent();
        step = factory.createTraceStepProxy(event, stateMap);
        stateMap.clear();
        current = next;
      } else {
        step = factory.createTraceStepProxy(null, stateMap);
        current = -1;
      }
      steps.add(0, step);
    } while (current >= 0);
    final String tracename = getTraceName();
    final ConflictTraceProxy trace =
      factory.createConflictTraceProxy(tracename, null, null, getModel(),
                                       getModel().getAutomata(),
                                       steps, ConflictKind.DEADLOCK);
    return trace;
  }


  //#########################################################################
  //# Inner Class DeadlockCallback
  private class DeadlockCallback implements StateCallback
  {

    //#######################################################################
    //# Constructor
    private DeadlockCallback()
    {
      final int size = getStateTupleEncoding().getNumberOfWords();
      mEncoded = new int[size];
    }

    //#######################################################################
    //# Interface net.sourceforge.waters.analysis.monolithic.
    //# TRAbstractModelAnalyzer.StateCallback
    @Override
    public boolean newState(final int[] tuple)
      throws OverflowException
    {
      getStateTupleEncoding().encode(tuple, mEncoded);
      final int stateIndex = getStateSpace().getIndex(mEncoded);
      // if -1; then not visited, so skip
      if (stateIndex !=-1) {
        if (stateIndex < mSmallestStateIndex) {
          mSmallestStateIndex = stateIndex;
          mSmallestStateEvent = mEvent;
          return true;
        }
      }
      return false;
    }

    //#######################################################################
    //# Simple Access
    private void setEvent(final EventProxy event)
    {
      mEvent = event;
    }

    private int getSmallestStateIndex()
    {
      return mSmallestStateIndex;
    }

    private EventProxy getSmallestStateEvent()
    {
      return mSmallestStateEvent;
    }

    //#######################################################################
    //# Data Members
    private final int[] mEncoded;
    private int mSmallestStateIndex;
    private EventProxy mEvent;
    private EventProxy mSmallestStateEvent;
  }


  //#########################################################################
  //# Data Members

}
