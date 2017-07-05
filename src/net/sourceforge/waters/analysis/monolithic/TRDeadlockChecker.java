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
import net.sourceforge.waters.model.analysis.DefaultVerificationResult;
import net.sourceforge.waters.model.analysis.KindTranslator;
import net.sourceforge.waters.model.analysis.OverflowException;
import net.sourceforge.waters.model.analysis.VerificationResult;
import net.sourceforge.waters.model.analysis.des.DeadlockChecker;
import net.sourceforge.waters.model.des.AutomatonProxy;
import net.sourceforge.waters.model.des.ConflictTraceProxy;
import net.sourceforge.waters.model.des.EventProxy;
import net.sourceforge.waters.model.des.ProductDESProxy;
import net.sourceforge.waters.model.des.ProductDESProxyFactory;
import net.sourceforge.waters.model.des.StateProxy;
import net.sourceforge.waters.model.des.TraceProxy;
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
  extends TRAbstractModelAnalyzer
  implements DeadlockChecker
{

  //#########################################################################
  //# Constructors
  public TRDeadlockChecker()
  {
  }

  public TRDeadlockChecker(final ProductDESProxy model)
  {
    super(model);
  }

  public TRDeadlockChecker
    (final ProductDESProxy model,
     final KindTranslator translator)
  {
    super(model, translator);
  }


  //#########################################################################
  //# Interface net.sourceforge.waters.model.analysis.des.ModelVerifier
  @Override
  public boolean isSatisfied()
  {
    final VerificationResult result = getAnalysisResult();
    if (result != null) {
      return result.isSatisfied();
    } else {
      throw new IllegalStateException("Call run() first!");
    }
  }

  @Override
  public ConflictTraceProxy getCounterExample()
  {
    if (isSatisfied()) {
      throw new IllegalStateException("No trace for satisfied property!");
    } else {
      final VerificationResult result = getAnalysisResult();
      return (ConflictTraceProxy) result.getCounterExample();
    }
  }

  @Override
  public VerificationResult getAnalysisResult()
  {
    return (VerificationResult) super.getAnalysisResult();
  }

  @Override
  public void setCounterExampleEnabled(final boolean enable)
  {
    setDetailedOutputEnabled(enable);
  }

  @Override
  public boolean isCounterExampleEnabled()
  {
    return isDetailedOutputEnabled();
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
  public DefaultVerificationResult createAnalysisResult()
  {
    return new DefaultVerificationResult(this);
  }

  @Override
  public boolean run()
    throws AnalysisException
  {

    try {
      setUp();
      exploreStateSpace();

      final VerificationResult result = getAnalysisResult();
      if (isDeadlockfound) {
        System.out.println("DEADLOCK ");
        System.out.println("mCurrentSource:"+ getCurrentSource());

        ConflictTraceProxy cp;
        try {
          cp = createTrace(getCurrentSource());
          return setFailedResult(cp);
        } catch (final Exception exception) {
          // TODO Auto-generated catch block
          exception.printStackTrace();
        }
        return false;
      } else {
        System.out.println( "deadlock free");
        return setSatisfiedResult();
      }
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
    throws OverflowException
  {
    boolean isDeadlock = true;
    for (final EventInfo event : getEventInfo()) {
      if (event.isEnabled(decoded)) {
        createSuccessorStates(encoded, decoded, event);
        isDeadlock = false;
      }
    }
    if (isDeadlock) {
      // TODO find counterexample
      try {
        System.out.println("Deadlock found ..");
        isDeadlockfound=true;
       // final ConflictTraceProxy cp = createTrace(getStateTupleEncoding(), encoded, decoded);
      //  setFailedResult(cp);

      }catch(final Exception ex) {

      }
    }
  }


  // ----Testing method
  public void copArrays (){

  }


  //#########################################################################
  //# Setting the Result
  /**
   * Stores a verification result indicating that the property checked
   * is satisfied and marks the run as completed.
   * @return <CODE>true</CODE>
   */
  protected boolean setSatisfiedResult()
  {
    return setBooleanResult(true);
  }

  /**
   * Stores a verification result indicating that the property checked
   * is not satisfied and marks the run as completed.
   * @param  counterexample The counterexample obtained by verification.
   * @return <CODE>false</CODE>
   */
  protected boolean setFailedResult(final TraceProxy counterexample)
  {
    final VerificationResult result = getAnalysisResult();
    result.setCounterExample(counterexample);
    return setBooleanResult(false);
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


    //********** Hani***********/
  // Methods to build the counter example
  private ConflictTraceProxy createTrace(final int source) throws Exception{
  //  setUpReverseTransitions();
    final DeadlockCallback deadlockCallback = new DeadlockCallback(getStateTupleEncoding().getNumberOfWords());
    setStateCallback(deadlockCallback);

    final int firstDeadlockState= source;
    System.out.println("No Exception yet in createTrace ..");

     final ConflictTraceProxy counterexample =
      buildCounterExample(firstDeadlockState,
                          getInputAutomata(),
                          deadlockCallback);

    return counterexample;
  }

  private ConflictTraceProxy buildCounterExample (final int firstDeadlockState,
                                                  final TRAutomatonProxy[] automataArray,
                                                  final DeadlockCallback callback) throws Exception
    {
  // Generate a counter example. As each state is numbered in the
  // order it is encountered, and a breadth first exploration
  // strategy is used, and all states are reachable, following the
  // transition to a state with the lowest id will give a
  // counterexample.
    final List<TraceStepProxy> steps = new LinkedList<TraceStepProxy>();
    final int numaut =  getInputAutomata().length;
    final Map<AutomatonProxy,StateProxy> stateMap =
      new HashMap<AutomatonProxy,StateProxy>(numaut);
    final int numInit = getNumberOfInitialStates();
    final ProductDESProxyFactory factory = getFactory();
    final int[] encodedSource = new int[getStateTupleEncoding().getNumberOfWords()];
    final int[] decodedSource = new int[getStateTupleEncoding().getNumberOfAutomata()];
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
        callback.setmEvent(event.getEvent());
        // expand ..
        createSuccessorStates(encodedSource, decodedSource, event);
      }
    }
    final int next = callback.getSmallestStateIndex();

    for(int i=0; i<automataArray.length; i++) {
    final TRAutomatonProxy aut= automataArray[i];
    final int s = decodedSource[i];
    final StateProxy state = aut.getState(s);
    //getStateSpace()
      stateMap.put(aut, state);

      /*final AutomatonSchema schema = automata[a];
      final AutomatonProxy aut = schema.getAutomatonProxy();
      final int s = tuple[a];
      final StateProxy state = schema.getStateProxyFromID(s);
      stateMap.put(aut, state);*/
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
                                       getModel().getAutomata(), steps, ConflictKind.DEADLOCK);
  return trace;
}



  //#########################################################################
  //# Data Members

    boolean isDeadlock=false;

    public boolean getIsDeadlock() {
      return isDeadlock;
    }



  //#########################################################################
  //*********** Hani*********/
  private class DeadlockCallback implements StateCallback{

    public DeadlockCallback(final int size) {
      mEncoded= new int[size];
    }

    @Override
    public boolean newState(final int[] tuple) throws OverflowException
    {
      getStateTupleEncoding().encode(tuple, mEncoded);
      final int mStateIndex = getStateSpace().getIndex(mEncoded);
      // if -1; then not visited, so skip
      if(mStateIndex !=-1) {
        if(mStateIndex< smallestStateIndex) {
          smallestStateIndex = mStateIndex;
          smallestStateEvent=mEvent;
          return true;
        }
      }
      // TODO Auto-generated method stub
      return false;
    }
    //###############################
    // Getters
    public int getSmallestStateIndex() {
      return smallestStateIndex;
    }

    public EventProxy getSmallestStateEvent() {
      return smallestStateEvent;
    }

    //###############################
    // Setters

    public void setmEvent(final EventProxy e) {
       mEvent=e;
    }
    //###############################
    // Data Members
    private final int[] mEncoded;
    private int  smallestStateIndex;
    private EventProxy mEvent;
    private EventProxy smallestStateEvent;

  }

}
