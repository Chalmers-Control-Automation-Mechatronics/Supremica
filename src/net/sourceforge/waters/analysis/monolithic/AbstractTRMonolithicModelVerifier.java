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

import gnu.trove.list.array.TIntArrayList;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import net.sourceforge.waters.analysis.tr.IntArrayBuffer;
import net.sourceforge.waters.analysis.tr.ListBufferTransitionRelation;
import net.sourceforge.waters.analysis.tr.TRAutomatonProxy;
import net.sourceforge.waters.model.analysis.AnalysisException;
import net.sourceforge.waters.model.analysis.AnalysisResult;
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

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


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
  //# Configuration
  public void setDepthMapEnabled(final boolean enabled)
  {
    mDepthMapEnabled = enabled;
  }

  public boolean isDepthMapEnabled()
  {
    return mDepthMapEnabled;
  }


  //#########################################################################
  //# Interface net.sourceforge.waters.model.analysis.ModelAnalyzer
  @Override
  public List<Option<?>> getOptions(final LeafOptionPage db)
  {
    final List<Option<?>> options = super.getOptions(db);
    db.prepend(options, TRMonolithicModelAnalyzerFactory.
                        OPTION_AbstractTRMonolithicModelVerifier_DepthMapEnabled);
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
    } else if (option.hasID(TRMonolithicModelAnalyzerFactory.
                            OPTION_AbstractTRMonolithicModelVerifier_DepthMapEnabled)) {
      final BooleanOption boolOption = (BooleanOption) option;
      setDepthMapEnabled(boolOption.getBooleanValue());
    } else {
      super.setOption(option);
    }
  }


  //#########################################################################
  //# Interface net.sourceforge.waters.model.analysis.des.ModelVerifier
  @Override
  public MonolithicVerificationResult createAnalysisResult()
  {
    return new MonolithicVerificationResult(this);
  }

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
  public MonolithicVerificationResult getAnalysisResult()
  {
    return (MonolithicVerificationResult) super.getAnalysisResult();
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
  public boolean run()
    throws AnalysisException
  {
    try {
      setUp();
      final VerificationResult result = getAnalysisResult();
      if (!result.isFinished()) {
        exploreStateSpace();
        if (!result.isFinished()) {
          result.setSatisfied(true);
        }
      }
      return result.isSatisfied();
    } catch (final AnalysisException exception) {
      throw setExceptionResult(exception);
    } catch (final OutOfMemoryError error) {
      tearDown();
      final Logger logger = LogManager.getLogger();
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
  //# Depth Map
  @Override
  protected void setUp()
    throws AnalysisException
  {
    super.setUp();
    if (mDepthMapEnabled) {
      mDepthMap = new TIntArrayList();
    }
  }

  @Override
  protected void exploreStateSpace()
    throws AnalysisException
  {
    if (mDepthMap == null) {
      super.exploreStateSpace();
    } else {
      final AnalysisResult result = getAnalysisResult();
      final IntArrayBuffer stateSpace = getStateSpace();
      mDepthMap.add(0);
      int current = 0;
      int nextLevel = storeInitialStates();
      while (current < nextLevel && !result.isFinished()) {
        mDepthMap.add(nextLevel);
        for (; current < nextLevel && !result.isFinished(); current++) {
          checkAbort();
          expandState(current);
        }
        nextLevel = stateSpace.size();
      }
    }
  }

  @Override
  protected void tearDown()
  {
    setStateCallback(null);
    mDepthMap = null;
    super.tearDown();
  }


  //#########################################################################
  //# Counterexamples
  protected CounterExampleCallback prepareForCounterExample(final int target)
    throws AnalysisException
  {
    final CounterExampleCallback callback;
    if (mDepthMap == null) {
      setUpReverseTransitions();
      callback = new BackwardCounterExampleCallback();
    } else {
      final int depth = getTraceDepth(target);
      callback = new ForwardCounterExampleCallback(depth);
    }
    setStateCallback(callback);
    return callback;
  }

  protected List<TraceStepProxy> buildTraceToBadState(int target)
    throws AnalysisException
  {
    final CounterExampleCallback callback = prepareForCounterExample(target);
    final int numAut = getStateTupleSize();
    final Map<AutomatonProxy,StateProxy> stateMap = new HashMap<>(numAut);
    final int numInit = getNumberOfInitialStates();
    final ProductDESProxyFactory factory = getFactory();
    final List<TraceStepProxy> steps = new LinkedList<>();

    // Until we reach the start state...
    while (target >= numInit) {
      callback.findPredecessor(target);
      final EventInfo eventInfo = callback.getFoundEvent();
      final EventProxy event = eventInfo.getEvent();
      callback.populateStateMap(target, stateMap);
      final TraceStepProxy step = factory.createTraceStepProxy(event, stateMap);
      steps.add(0, step);
      stateMap.clear();
      target = callback.getFoundSource();
    }
    callback.populateStateMap(target, stateMap);
    final TraceStepProxy step = factory.createTraceStepProxy(null, stateMap);
    steps.add(0, step);
    return steps;
  }

  protected int getTraceDepth(final int target)
  {
    int lo = 0;
    int up = getDepthMapSize() - 2;
    while (lo < up) {
      final int mid = (lo + up) >> 1;
      if (mDepthMap.get(mid + 1) <= target) {
        lo = mid + 1;
      } else {
        up = mid;
      }
    }
    return lo;
  }

  protected int getDepthMapSize()
  {
    return mDepthMap.size();
  }


  /**
   * Expands the given state for counterexample search.
   * This method calculates all transitions originating from the given state
   * recording the reached state with the smallest index and the associated
   * event using a {@link BackwardCounterExampleCallback}.
   * @param  encoded  Compressed state tuple to be expanded.
   * @param  decoded  Decompressed version of the same state tuple.
   * @param  callback Counterexample callback in use.
   */
  protected void expandState(final int[] encoded,
                             final int[] decoded,
                             final BackwardCounterExampleCallback callback)
    throws AnalysisException
  {
    for (final EventInfo info : getEventInfo()) {
      callback.setEvent(info);
      if (!info.expandState(encoded, decoded)) {
        break;
      }
    }
  }


  //#########################################################################
  //# Inner Class CounterExampleCallback
  abstract class CounterExampleCallback implements StateCallback
  {
    //#######################################################################
    //# Constructor
    private CounterExampleCallback()
    {
      final int numWords = getStateTupleEncoding().getNumberOfWords();
      final int tupleSize = getStateTupleSize();
      mEncodedSource = new int[numWords];
      mDecodedSource = new int[tupleSize];
      mEncodedTarget = new int[numWords];
      mFoundSource = Integer.MAX_VALUE;
      mFoundEvent = null;
    }

    //#######################################################################
    //# Hooks
    abstract void findPredecessor(int target) throws AnalysisException;

    //#######################################################################
    //# Auxiliary Methods
    int[] getEncodedSource()
    {
      return mEncodedSource;
    }

    int[] getDecodedSource()
    {
      return mDecodedSource;
    }

    void setFound(final int source, final EventInfo event)
    {
      mFoundSource = source;
      mFoundEvent = event;
    }

    int getFoundSource()
    {
      return mFoundSource;
    }

    void populateStateMap(final int target,
                          final Map<AutomatonProxy,StateProxy> stateMap)
    {
      final TRAutomatonProxy[] automataArray = getTRAutomata();
      getStateSpace().getContents(target, mEncodedSource);
      getStateTupleEncoding().decode(mEncodedSource, mDecodedSource);
      for (int i = 0; i < automataArray.length; i++) {
        final AutomatonProxy aut = getInputAutomaton(i);
        final int s = mDecodedSource[i];
        final StateProxy state = getInputState(i, s);
        stateMap.put(aut, state);
      }
    }

    EventInfo getFoundEvent()
    {
      return mFoundEvent;
    }

    int getStateIndex(final int[] decoded)
    {
      getStateTupleEncoding().encode(decoded, mEncodedTarget);
      return getStateSpace().getIndex(mEncodedTarget);
    }

    //#######################################################################
    //# Data Members
    private final int[] mEncodedSource;
    private final int[] mDecodedSource;
    private final int[] mEncodedTarget;
    private int mFoundSource;
    private EventInfo mFoundEvent;
  }


  //#########################################################################
  //# Inner Class ForwardCounterExampleCallback
  private class ForwardCounterExampleCallback extends CounterExampleCallback
  {
    //#######################################################################
    //# Constructor
    private ForwardCounterExampleCallback(final int depth)
    {
      mLevel = depth - 1;
    }

    //#######################################################################
    //# Overrides for net.sourceforge.waters.analysis.monolithic.
    //# AbstractTRMonolithicModelVerifier.CounterExampleCallback
    @Override
    void findPredecessor(final int target)
      throws AnalysisException
    {
      mTarget = target;
      mFound = false;
      final int[] encodedSource = getEncodedSource();
      final int[] decodedSource = getDecodedSource();
      final int start = mDepthMap.get(mLevel);
      final int end = mDepthMap.get(mLevel + 1);
      outer:
      for (int s = start; s < end; s++) {
        checkAbort();
        getStateSpace().getContents(s, encodedSource);
        getStateTupleEncoding().decode(encodedSource, decodedSource);
        for (final EventInfo event : getEventInfo()) {
          event.expandState(encodedSource, decodedSource);
          if (mFound) {
            setFound(s, event);
            break outer;
          }
        }
      }
      assert mFound;
      mLevel--;
    }

    //#######################################################################
    //# Interface net.sourceforge.waters.analysis.monolithic.
    //# TRAbstractModelAnalyzer.StateCallback
    @Override
    public boolean newState(final int[] decoded)
      throws OverflowException
    {
      if (getStateIndex(decoded) == mTarget) {
        mFound = true;
      }
      return false;
    }

    //#######################################################################
    //# Data Members
    private int mLevel;
    private int mTarget;
    private boolean mFound;
  }


  //#########################################################################
  //# Inner Class BackwardCounterExampleCallback
  protected class BackwardCounterExampleCallback extends CounterExampleCallback
  {
    //#######################################################################
    //# Simple Access
    public void setEvent(final EventInfo info)
    {
      mCurrentEvent = info;
    }

    //#######################################################################
    //# Overrides for net.sourceforge.waters.analysis.monolithic.
    //# AbstractTRMonolithicModelVerifier.CounterExampleCallback
    @Override
    void findPredecessor(final int target)
      throws AnalysisException
    {
      final int[] encodedSource = getEncodedSource();
      final int[] decodedSource = getDecodedSource();
      getStateSpace().getContents(target, encodedSource);
      getStateTupleEncoding().decode(encodedSource, decodedSource);
      expandState(encodedSource, decodedSource, this);
    }

    //#######################################################################
    //# Interface net.sourceforge.waters.analysis.monolithic.
    //# TRAbstractModelAnalyzer.StateCallback
    @Override
    public boolean newState(final int[] decoded)
      throws OverflowException
    {
      final int currentStateIndex = getStateIndex(decoded);
      // if -1; then not visited, so skip
      if (currentStateIndex != -1 &&
          currentStateIndex < getFoundSource()) {
        setFound(currentStateIndex, mCurrentEvent);
      }
      return false;
    }

    //#######################################################################
    //# Data Members
    private EventInfo mCurrentEvent;
  }


  //#######################################################################
  //# Data Members
  private boolean mDepthMapEnabled = true;
  private TIntArrayList mDepthMap;

}
