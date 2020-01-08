//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# Copyright (C) 2004-2019 Robi Malik
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

package net.sourceforge.waters.analysis.compositional;

import gnu.trove.set.hash.THashSet;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;

import net.sourceforge.waters.analysis.options.BooleanOption;
import net.sourceforge.waters.analysis.options.Option;
import net.sourceforge.waters.analysis.options.OptionPage;
import net.sourceforge.waters.model.analysis.AnalysisException;
import net.sourceforge.waters.model.analysis.OverflowException;
import net.sourceforge.waters.model.analysis.VerificationResult;
import net.sourceforge.waters.model.analysis.des.AbstractModelAnalyzerFactory;
import net.sourceforge.waters.model.analysis.des.EventNotFoundException;
import net.sourceforge.waters.model.analysis.des.ModelVerifier;
import net.sourceforge.waters.model.analysis.des.SynchronousProductBuilder;
import net.sourceforge.waters.model.analysis.des.SynchronousProductResult;
import net.sourceforge.waters.model.analysis.des.SynchronousProductStateMap;
import net.sourceforge.waters.model.analysis.kindtranslator.KindTranslator;
import net.sourceforge.waters.model.des.AutomatonProxy;
import net.sourceforge.waters.model.des.AutomatonTools;
import net.sourceforge.waters.model.des.CounterExampleProxy;
import net.sourceforge.waters.model.des.EventProxy;
import net.sourceforge.waters.model.des.ProductDESProxy;
import net.sourceforge.waters.model.des.ProductDESProxyFactory;
import net.sourceforge.waters.model.des.StateProxy;
import net.sourceforge.waters.model.des.TraceProxy;
import net.sourceforge.waters.model.des.TraceStepProxy;
import net.sourceforge.waters.model.des.TransitionProxy;

import org.apache.logging.log4j.LogManager;


/**
 * A general compositional model verifier to be subclassed for different
 * properties. This class adds to the general base class {@link
 * AbstractCompositionalModelAnalyzer} a common {@link #run()} that
 * supports counter example computation.
 *
 * <I>References:</I><BR>
 * Hugo Flordal, Robi Malik. Compositional Verification in Supervisory Control.
 * SIAM Journal of Control and Optimization, 48(3), 1914-1938, 2009.<BR>
 * Robi Malik, Ryan Leduc. A Compositional Approach for Verifying Generalised
 * Nonblocking, Proc. 7th International Conference on Control and Automation,
 * ICCA'09, 448-453, Christchurch, New Zealand, 2009.<BR>
 *
 * @author Robi Malik
 */

public abstract class AbstractCompositionalModelVerifier
  extends AbstractCompositionalModelAnalyzer
  implements ModelVerifier
{

  //#########################################################################
  //# Constructors
  /**
   * Creates an abstracting model verifier without a model.
   * @param factory
   *          Factory used for trace construction.
   * @param translator
   *          Kind translator used to determine event and component kinds.
   * @param abstractionFactory
   *          Factory to define the abstraction sequence to be used.
   */
  protected AbstractCompositionalModelVerifier
    (final ProductDESProxyFactory factory,
     final KindTranslator translator,
     final AbstractionProcedureCreator abstractionFactory)
  {
    super(factory, translator, abstractionFactory);
  }

  /**
   * Creates an abstracting model verifier without a model.
   * @param factory
   *          Factory used for trace construction.
   * @param translator
   *          Kind translator used to determine event and component kinds.
   * @param abstractionFactory
   *          Factory to define the abstraction sequence to be used.
   * @param preselectingMethodFactory
   *          Enumeration factory that determines possible candidate
   *          preselection methods.
   */
  protected AbstractCompositionalModelVerifier
    (final ProductDESProxyFactory factory,
     final KindTranslator translator,
     final AbstractionProcedureCreator abstractionFactory,
     final PreselectingMethodFactory preselectingMethodFactory)
  {
    super(factory, translator, abstractionFactory,
          preselectingMethodFactory);
  }

  /**
   * Creates an abstracting model verifier to check the given model.
   * @param model
   *          The model to be checked by this model verifier.
   * @param factory
   *          Factory used for trace construction.
   * @param translator
   *          Kind translator used to determine event and component kinds.
   * @param abstractionFactory
   *          Factory to define the abstraction sequence to be used.
   */
  protected AbstractCompositionalModelVerifier
    (final ProductDESProxy model,
     final ProductDESProxyFactory factory,
     final KindTranslator translator,
     final AbstractionProcedureCreator abstractionFactory)
  {
    super(model, factory, translator, abstractionFactory);
  }

  /**
   * Creates an abstracting model verifier to check the given model.
   * @param model
   *          The model to be checked by this model verifier.
   * @param factory
   *          Factory used for trace construction.
   * @param translator
   *          Kind translator used to determine event and component kinds.
   * @param abstractionFactory
   *          Factory to define the abstraction sequence to be used.
   * @param preselectingMethodFactory
   *          Enumeration factory that determines possible candidate
   *          preselection methods.
   */
  protected AbstractCompositionalModelVerifier
    (final ProductDESProxy model,
     final ProductDESProxyFactory factory,
     final KindTranslator translator,
     final AbstractionProcedureCreator abstractionFactory,
     final PreselectingMethodFactory preselectingMethodFactory)
  {
    super(model, factory, translator, abstractionFactory,
          preselectingMethodFactory);
  }


  //#########################################################################
  //# Configuration
  /**
   * Sets whether counterexample checking is enabled.
   * If enabled, the generated counterexample is checked for correctness
   * after each step during counterexample. This is a very slow process,
   * and only recommend for testing and debugging.
   * This setting is disabled by default.
   */
  public void setTraceCheckingEnabled(final boolean checking)
  {
    mTraceCheckingEnabled = checking;
  }

  /**
   * Returns whether counterexample checking is enabled.
   * @see #setTraceCheckingEnabled(boolean) setTraceCheckingEnabled()
   */
  public boolean isTraceCheckingEnabled()
  {
    return mTraceCheckingEnabled;
  }


  //#########################################################################
  //# Specific Access
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
  //# Invocation
  @Override
  public boolean run() throws AnalysisException
  {
    try {
      setUp();
      runCompositionalMinimisation();
      final CompositionalVerificationResult result = getAnalysisResult();
      if (result.isSatisfied()) {
        return true;
      } else {
        final AbstractionProcedure proc = getAbstractionProcedure();
        proc.resetStatistics();
        if (isCounterExampleEnabled()) {
          CounterExampleProxy counter = result.getCounterExample();
          counter = expandCounterExample(counter);
          return setFailedResult(counter);
        } else {
          return setFailedResult(null);
        }
      }
    } catch (final AnalysisException exception) {
      throw setExceptionResult(exception);
    } catch (final OutOfMemoryError error) {
      System.gc();
      final OverflowException overflow = new OverflowException(error);
      throw setExceptionResult(overflow);
    } finally {
      tearDown();
    }
  }


  //#########################################################################
  //# Interface net.sourceforge.waters.model.analysis.des.ModelVerifier
  @Override
  public void setShortCounterExampleRequested(final boolean req)
  {
    mShortCounterExampleRequested = req;
  }

  @Override
  public boolean isShortCounterExampleRequested()
  {
    return mShortCounterExampleRequested;
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
  public CompositionalVerificationResult getAnalysisResult()
  {
    return (CompositionalVerificationResult) super.getAnalysisResult();
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

  @Override
  public CompositionalVerificationResult createAnalysisResult()
  {
    return new CompositionalVerificationResult(this);
  }


  //#########################################################################
  //# Interface net.sourceforge.waters.model.analysis.des.ModelAnalyzer
  @Override
  public List<Option<?>> getOptions(final OptionPage db)
  {
    final List<Option<?>> options = super.getOptions(db);
    db.prepend(options, CompositionalModelAnalyzerFactory.
                        OPTION_AbstractCompositionalModelVerifier_TraceCheckingEnabled);
    db.prepend(options, AbstractModelAnalyzerFactory.
                        OPTION_ModelVerifier_ShortCounterExampleRequested);
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
    } else if (option.hasID(AbstractModelAnalyzerFactory.
                            OPTION_ModelVerifier_ShortCounterExampleRequested)) {
      final BooleanOption boolOption = (BooleanOption) option;
      setShortCounterExampleRequested(boolOption.getBooleanValue());
    } else if (option.hasID(CompositionalModelAnalyzerFactory.
                            OPTION_AbstractCompositionalModelVerifier_TraceCheckingEnabled)) {
      final BooleanOption boolOption = (BooleanOption) option;
      setTraceCheckingEnabled(boolOption.getBooleanValue());
    } else {
      super.setOption(option);
    }
  }


  //#########################################################################
  //# Overrides for net.sourceforge.waters.model.AbstractModelAnalyser
  @Override
  protected void setUp()
    throws AnalysisException
  {
    mAbstractionSteps = new ArrayList<AbstractionStep>();
    super.setUp();
  }

  @Override
  protected void tearDown()
  {
    super.tearDown();
    mAbstractionSteps = null;
  }


  //#########################################################################
  //# Hooks
  @Override
  protected void initialiseEventsToAutomata()
    throws AnalysisException
  {
    super.initialiseEventsToAutomata();
    for (final AutomatonProxy aut : getCurrentAutomata()) {
      if (AutomatonTools.getFirstInitialState(aut) == null) {
        setSatisfiedResult();
        break;
      }
    }
  }

  @Override
  protected ModelVerifier getCurrentMonolithicAnalyzer()
  {
    return (ModelVerifier) super.getCurrentMonolithicAnalyzer();
  }

  @Override
  protected void setupMonolithicAnalyzer()
    throws EventNotFoundException
  {
    super.setupMonolithicAnalyzer();
    final ModelVerifier mono = getCurrentMonolithicAnalyzer();
    if (mono != null) {
      mono.setShortCounterExampleRequested(mShortCounterExampleRequested);
    }
  }

  @Override
  protected boolean isPermissibleCandidate(final List<AutomatonProxy> automata)
  {
    return
      super.isPermissibleCandidate(automata) &&
      automata.size() < getCurrentAutomata().size();
  }

  @Override
  protected void recordAbstractionStep(final AbstractionStep step)
  {
    mAbstractionSteps.add(step);
  }

  @Override
  protected HidingStep createSynchronousProductStep
    (final Collection<AutomatonProxy> automata,
     final AutomatonProxy sync,
     final Collection<EventProxy> hidden,
     final EventProxy tau)
  {
    final SynchronousProductBuilder builder = getSynchronousProductBuilder();
    final SynchronousProductResult result = builder.getAnalysisResult();
    final SynchronousProductStateMap stateMap = result.getStateMap();
    return new HidingStep(this, sync, hidden, tau, stateMap);
  }

  @Override
  protected boolean doMonolithicAnalysis
    (final List<AutomatonProxy> automata)
    throws AnalysisException
  {
    if (automata.isEmpty()) {
      return setSatisfiedResult();
    } else {
      final ProductDESProxy des = createProductDESProxy(automata);
      reportMonolithicAnalysis(des);
      final ModelVerifier monolithicVerifier =
        getCurrentMonolithicAnalyzer();
      monolithicVerifier.setModel(des);
      monolithicVerifier.run();
      // Do not clean up before run, keep data just in case of overflow ...
      removeEventsToAutomata(automata);
      final VerificationResult subresult =
        monolithicVerifier.getAnalysisResult();
      recordStatistics(subresult);
      if (subresult.isSatisfied()) {
        return true;
      } else {
        final CompositionalVerificationResult result = getAnalysisResult();
        final CounterExampleProxy counter =
          monolithicVerifier.getCounterExample();
        result.setCounterExample(counter);
        final boolean confirmed = confirmMonolithicCounterExample();
        return !confirmed;
      }
    }
  }

  /**
   * Checks whether the currently stored counterexample for the current
   * subsystem can be considered as a counterexample for the entire system,
   * included completed and postponed subsystems. This method may change
   * the stored counterexample if needed, or remove it if it is found that
   * it cannot be extended.
   * @return <CODE>true</CODE> if the counterexample has successfully
   *         been lifted to the entire system;
   *         <CODE>false</CODE> if the counterexample cannot be lifted to
   *         the entire system and has been removed.
   */
  protected boolean confirmMonolithicCounterExample()
  throws AnalysisException
  {
    return true;
  }

  /**
   * Creates a counterexample. This hook is invoked to create a counterexample
   * from a given list trace steps. It should create trace of the correct type.
   * @param  automata     Automata to be put in the counterexample.
   * @param  steps        List of steps constituting the counterexample.
   */
  protected abstract CounterExampleProxy createCounterExample
    (final Collection<AutomatonProxy> automata,
     final List<TraceStepProxy> steps);

  /**
   * Checks whether the given sequence of steps forms a correct counterexample
   * for the given automata. This method is used for debugging only.
   */
  protected abstract void testCounterExample
    (final List<TraceStepProxy> steps,
     final Collection<AutomatonProxy> automata)
  throws AnalysisException;

  /**
   * Returns a list of all automata that should be included when starting
   * to construct a counterexample.
   */
  protected Collection<AutomatonProxy> getAllTraceAutomata()
  {
    return getCurrentAutomata();
  }


  //#########################################################################
  //# Trace Computation
  private CounterExampleProxy expandCounterExample
    (final CounterExampleProxy counter)
    throws AnalysisException
  {
    LogManager.getLogger().debug("Property NOT satisfied --- expanding trace ...");
    final TraceProxy trace = counter.getTraces().get(0);
    final List<TraceStepProxy> unsat = trace.getTraceSteps();
    final Collection<AutomatonProxy> currentAutomata = getAllTraceAutomata();
    List<TraceStepProxy> traceSteps =
      getSaturatedTraceSteps(unsat, currentAutomata);
    final int size = mAbstractionSteps.size();
    final ListIterator<AbstractionStep> iter =
      mAbstractionSteps.listIterator(size);
    final Collection<AutomatonProxy> check;
    if (mTraceCheckingEnabled) {
      check = new THashSet<AutomatonProxy>(currentAutomata);
      testCounterExample(traceSteps, check);
    } else {
      check = null;
    }
    while (iter.hasPrevious()) {
      final AbstractionStep step = iter.previous();
      traceSteps = step.convertTraceSteps(traceSteps);
      if (mTraceCheckingEnabled) {
        check.removeAll(step.getResultAutomata());
        check.addAll(step.getOriginalAutomata());
        testCounterExample(traceSteps, check);
      }
    }
    final ProductDESProxy model = getModel();
    final Collection<AutomatonProxy> modelAutomata = model.getAutomata();
    return createCounterExample(modelAutomata, traceSteps);
  }

  /**
   * Fills in the target states in the state maps for each step of the trace
   * for the result automaton.
   */
  private List<TraceStepProxy> getSaturatedTraceSteps
    (final List<TraceStepProxy> steps,
     final Collection<AutomatonProxy> automata)
  {
    final ProductDESProxyFactory factory = getFactory();
    final int numAutomata = automata.size();
    final int numSteps = steps.size();
    final List<TraceStepProxy> convertedSteps =
        new ArrayList<TraceStepProxy>(numSteps);
    final Iterator<TraceStepProxy> iter = steps.iterator();

    final TraceStepProxy firstStep = iter.next();
    final Map<AutomatonProxy,StateProxy> firstMap = firstStep.getStateMap();
    final Map<AutomatonProxy,StateProxy> convertedFirstMap =
      new HashMap<AutomatonProxy,StateProxy>(numAutomata);
    for (final AutomatonProxy aut : automata) {
      final StateProxy state = getInitialState(aut, firstMap);
      convertedFirstMap.put(aut, state);
    }
    final TraceStepProxy convertedFirstStep =
      factory.createTraceStepProxy(null, convertedFirstMap);
    convertedSteps.add(convertedFirstStep);
    Map<AutomatonProxy,StateProxy> previousStepMap = convertedFirstMap;
    while (iter.hasNext()) {
      final TraceStepProxy step = iter.next();
      final EventProxy event = step.getEvent();
      final Map<AutomatonProxy,StateProxy> stepMap = step.getStateMap();
      final Map<AutomatonProxy,StateProxy> convertedStepMap =
        new HashMap<AutomatonProxy,StateProxy>(numAutomata);
      for (final AutomatonProxy aut : automata) {
        final StateProxy prev = previousStepMap.get(aut);
        final StateProxy state = findSuccessor(aut, event, prev, stepMap);
        convertedStepMap.put(aut, state);
      }
      final TraceStepProxy convertedStep =
        factory.createTraceStepProxy(event, convertedStepMap);
      convertedSteps.add(convertedStep);
      previousStepMap = convertedStepMap;
    }
    return convertedSteps;
  }

  /**
   * Finds the initial state of an automaton in a trace.
   * A trace step's map is passed for the case of multiple initial states.
   */
  private StateProxy getInitialState
    (final AutomatonProxy aut, final Map<AutomatonProxy,StateProxy> stepMap)
  {
    // If there is more than one initial state, the trace has the info.
    StateProxy initial = stepMap.get(aut);
    // Otherwise there is only one initial state.
    if (initial == null) {
      for (final StateProxy state : aut.getStates()) {
        if (state.isInitial()) {
          initial = state;
          break;
        }
      }
    }
    return initial;
  }

  /**
   * Finds the successor state in trace, from a given state in an automaton.
   * A trace step's map is passed for the case of multiple successor states.
   */
  private StateProxy findSuccessor(final AutomatonProxy aut,
                                   final EventProxy event,
                                   final StateProxy sourceState,
                                   final Map<AutomatonProxy,StateProxy> stepMap)
  {
    // If there is more than one successor state, the trace has the info.
    final StateProxy targetState = stepMap.get(aut);
    // Otherwise there is only one successor state.
    if (targetState == null) {
      if (aut.getEvents().contains(event)) {
        for (final TransitionProxy trans : aut.getTransitions()) {
          if (trans.getEvent() == event && trans.getSource() == sourceState) {
            return trans.getTarget();
          }
        }
      } else {
        return sourceState;
      }
    }
    return targetState;
  }


  //#########################################################################
  //# Statistics
  private void recordStatistics(final VerificationResult result)
  {
    final CompositionalVerificationResult global = getAnalysisResult();
    global.addMonolithicAnalysisResult(result);
  }


  //#########################################################################
  //# Data Members
  private boolean mShortCounterExampleRequested = false;
  private boolean mTraceCheckingEnabled = false;

  private List<AbstractionStep> mAbstractionSteps;

}
