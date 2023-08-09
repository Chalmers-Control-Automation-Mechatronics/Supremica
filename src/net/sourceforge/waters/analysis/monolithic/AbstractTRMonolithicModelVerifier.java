//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# Copyright (C) 2004-2021 Robi Malik
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
import net.sourceforge.waters.model.analysis.AnalysisException;
import net.sourceforge.waters.model.analysis.DefaultVerificationResult;
import net.sourceforge.waters.model.analysis.OverflowException;
import net.sourceforge.waters.model.analysis.VerificationResult;
import net.sourceforge.waters.model.analysis.des.AbstractModelAnalyzerFactory;
import net.sourceforge.waters.model.analysis.des.ModelVerifier;
import net.sourceforge.waters.model.analysis.kindtranslator.KindTranslator;
import net.sourceforge.waters.model.des.AutomatonProxy;
import net.sourceforge.waters.model.des.CounterExampleProxy;
import net.sourceforge.waters.model.des.EventProxy;
import net.sourceforge.waters.model.des.ProductDESProxy;
import net.sourceforge.waters.model.des.ProductDESProxyFactory;
import net.sourceforge.waters.model.des.StateProxy;
import net.sourceforge.waters.model.des.TraceStepProxy;
import net.sourceforge.waters.model.options.BooleanOption;
import net.sourceforge.waters.model.options.LeafOptionPage;
import net.sourceforge.waters.model.options.Option;


/**
 * The abstract base class for all model verifiers that use
 * {@link ListBufferTransitionRelation} as their automata representation.
 *
 * @author Robi Malik
 */

public abstract class AbstractTRMonolithicModelVerifier
  extends AbstractTRMonolithicModelAnalyzer
  implements ModelVerifier
{

  //#########################################################################
  //# Constructors
  public AbstractTRMonolithicModelVerifier(final KindTranslator translator)
  {
    this(null, translator);
  }

  public AbstractTRMonolithicModelVerifier(final ProductDESProxy model,
                                           final KindTranslator translator)
  {
    super(model, translator);
  }


  //#########################################################################
  //# Interface net.sourceforge.waters.model.analysis.ModelAnalyzer
  @Override
  public List<Option<?>> getOptions(final LeafOptionPage db)
  {
    final List<Option<?>> options = super.getOptions(db);
    db.prepend(options, AbstractModelAnalyzerFactory.
                        OPTION_ModelVerifier_DetailedOutputEnabled);
    return options;
  }

  @Override
  public void setOption(final Option<?> option)
  {
    if (option.hasID(AbstractModelAnalyzerFactory.
                     OPTION_ModelVerifier_DetailedOutputEnabled)) {
      final BooleanOption boolOption = (BooleanOption) option;
      setDetailedOutputEnabled(boolOption.getBooleanValue());
    } else {
      super.setOption(option);
    }
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
  public CounterExampleProxy getCounterExample()
  {
    if (isSatisfied()) {
      throw new IllegalStateException("No trace for satisfied property!");
    } else {
      final VerificationResult result = getAnalysisResult();
      return result.getCounterExample();
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
  public VerificationResult createAnalysisResult()
  {
    return new DefaultVerificationResult(this);
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
  protected boolean setFailedResult(final CounterExampleProxy counterexample)
  {
    final VerificationResult result = getAnalysisResult();
    result.setCounterExample(counterexample);
    return setBooleanResult(false);
  }


  //#########################################################################
  //# Counterexamples
  @Override
  protected void tearDown()
  {
    setStateCallback(null);
    super.tearDown();
  }

  protected List<TraceStepProxy> buildTraceToBadState(int target)
    throws AnalysisException
  {
    setUpReverseTransitions();
    final CounterExampleCallback callback = new CounterExampleCallback();
    setStateCallback(callback);
    final AutomatonProxy[] automataArray= getTRAutomata();
    final int numAut = automataArray.length;
    final Map<AutomatonProxy,StateProxy> stateMap = new HashMap<>(numAut);
    final int numInit = getNumberOfInitialStates();
    final ProductDESProxyFactory factory = getFactory();
    final int[] encodedSource =
      new int[getStateTupleEncoding().getNumberOfWords()];
    final int[] decodedSource = new int[numAut];
    final List<TraceStepProxy> steps = new LinkedList<>();

    // Until we reach the start state...
    do {
      getStateSpace().getContents(target, encodedSource);
      getStateTupleEncoding().decode(encodedSource, decodedSource);
      for (final EventInfo event : getEventInfo()) {
        if (event.findDisabling(decodedSource) == null) {
          callback.setEvent(event.getEvent());
          createSuccessorStates(encodedSource, decodedSource, event);
        }
      }
      final int next = callback.getSmallestStateIndex();
      for (int i = 0; i < automataArray.length; i++) {
        final AutomatonProxy aut = getInputAutomaton(i);
        final int s = decodedSource[i];
        final StateProxy state = getInputState(i, s);
        stateMap.put(aut, state);
      }
      final TraceStepProxy step;
      if (target >= numInit) {
        final EventProxy event = callback.getSmallestStateEvent();
        step = factory.createTraceStepProxy(event, stateMap);
        stateMap.clear();
        target = next;
      } else {
        step = factory.createTraceStepProxy(null, stateMap);
        target = -1;
      }
      steps.add(0, step);
    } while (target >= 0);
    return steps;
  }


  //#########################################################################
  //# Inner Class CounterExampleCallback
  private class CounterExampleCallback implements StateCallback
  {

    //#######################################################################
    //# Constructor
    private CounterExampleCallback()
    {
      final int size = getStateTupleEncoding().getNumberOfWords();
      mEncoded = new int[size];
      mSmallestStateIndex = getCurrentSource();
    }

    //#######################################################################
    //# Interface net.sourceforge.waters.analysis.monolithic.
    //# TRAbstractModelAnalyzer.StateCallback
    @Override
    public boolean newState(final int[] tuple)
      throws OverflowException
    {
      getStateTupleEncoding().encode(tuple, mEncoded);
      final int currentStateIndex = getStateSpace().getIndex(mEncoded);
      // if -1; then not visited, so skip
      if (currentStateIndex != -1) {
        if (currentStateIndex < mSmallestStateIndex) {
          mSmallestStateIndex = currentStateIndex;
          mSmallestStateEvent = mCurrentEvent;
          return true;
        }
      }
      return false;
    }

    //#######################################################################
    //# Simple Access
    private void setEvent(final EventProxy event)
    {
      mCurrentEvent = event;
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
    private EventProxy mCurrentEvent;
    private EventProxy mSmallestStateEvent;
  }

}
