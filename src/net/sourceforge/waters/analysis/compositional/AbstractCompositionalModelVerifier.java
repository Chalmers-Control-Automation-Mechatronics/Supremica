//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters Analysis
//# PACKAGE: net.sourceforge.waters.analysis.compositional
//# CLASS:   AbstractCompositionalModelVerifier
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.analysis.compositional;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;

import net.sourceforge.waters.model.analysis.AnalysisException;
import net.sourceforge.waters.model.analysis.KindTranslator;
import net.sourceforge.waters.model.analysis.VerificationResult;
import net.sourceforge.waters.model.analysis.des.EventNotFoundException;
import net.sourceforge.waters.model.analysis.des.ModelVerifier;
import net.sourceforge.waters.model.analysis.des.SynchronousProductBuilder;
import net.sourceforge.waters.model.analysis.des.SynchronousProductStateMap;
import net.sourceforge.waters.model.des.AutomatonProxy;
import net.sourceforge.waters.model.des.EventProxy;
import net.sourceforge.waters.model.des.ProductDESProxy;
import net.sourceforge.waters.model.des.ProductDESProxyFactory;
import net.sourceforge.waters.model.des.StateProxy;
import net.sourceforge.waters.model.des.TraceProxy;
import net.sourceforge.waters.model.des.TraceStepProxy;
import net.sourceforge.waters.model.des.TransitionProxy;

import org.apache.log4j.Logger;


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
     final AbstractionProcedureFactory abstractionFactory)
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
   * @param selectingMethodFactory
   *          Enumeration factory that determines possible candidate
   *          selection methods.
   */
  protected AbstractCompositionalModelVerifier
    (final ProductDESProxyFactory factory,
     final KindTranslator translator,
     final AbstractionProcedureFactory abstractionFactory,
     final PreselectingMethodFactory preselectingMethodFactory,
     final SelectingMethodFactory selectingMethodFactory)
  {
    super(factory, translator, abstractionFactory,
          preselectingMethodFactory, selectingMethodFactory);
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
     final AbstractionProcedureFactory abstractionFactory)
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
   * @param selectingMethodFactory
   *          Enumeration factory that determines possible candidate
   *          selection methods.
   */
  protected AbstractCompositionalModelVerifier
    (final ProductDESProxy model,
     final ProductDESProxyFactory factory,
     final KindTranslator translator,
     final AbstractionProcedureFactory abstractionFactory,
     final PreselectingMethodFactory preselectingMethodFactory,
     final SelectingMethodFactory selectingMethodFactory)
  {
    super(model, factory, translator, abstractionFactory,
          preselectingMethodFactory, selectingMethodFactory);
  }


  //#########################################################################
  //# Configuration
  protected void setMonolithicVerifier(final ModelVerifier verifier)
  {
    mMonolithicVerifier = verifier;
  }

  protected ModelVerifier getMonolithicVerifier()
  {
    return mMonolithicVerifier;
  }

  protected void setCurrentMonolithicVerifier(final ModelVerifier verifier)
  {
    mCurrentMonolithicVerifier = verifier;
  }

  protected ModelVerifier getCurrentMonolithicVerifier()
  {
    return mCurrentMonolithicVerifier;
  }

  protected void setupMonolithicVerifier()
    throws EventNotFoundException
  {
    final int nlimit = getMonolithicStateLimit();
    mCurrentMonolithicVerifier.setNodeLimit(nlimit);
    final int tlimit = getMonolithicTransitionLimit();
    mCurrentMonolithicVerifier.setTransitionLimit(tlimit);
    final KindTranslator translator = getKindTranslator();
    mCurrentMonolithicVerifier.setKindTranslator(translator);
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
  protected boolean setFailedResult(final TraceProxy counterexample)
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
        TraceProxy trace = result.getCounterExample();
        trace = expandTrace(trace);
        return setFailedResult(trace);
      }
    } catch (final AnalysisException exception) {
      throw setExceptionResult(exception);
    } finally {
      tearDown();
    }
  }


  //#########################################################################
  //# Interface net.sourceforge.waters.model.analysis.Abortable
  @Override
  public void requestAbort()
  {
    super.requestAbort();
    if (mCurrentMonolithicVerifier != null) {
      mCurrentMonolithicVerifier.requestAbort();
    }
  }


  //#########################################################################
  //# Interface net.sourceforge.waters.model.analysis.ModelVerifier
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
  public TraceProxy getCounterExample()
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
  protected CompositionalVerificationResult createAnalysisResult()
  {
    return new CompositionalVerificationResult();
  }


  //#########################################################################
  //# Overrides for net.sourceforge.waters.model.AbstractModelAnalyser
  @Override
  protected void setUp()
    throws AnalysisException
  {
    setupMonolithicVerifier();
    mAbstractionSteps = new ArrayList<AbstractionStep>();
    super.setUp();
  }

  @Override
  protected void tearDown()
  {
    super.tearDown();
    mCurrentMonolithicVerifier = null;
    mAbstractionSteps = null;
  }


  //#########################################################################
  //# Hooks
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
    final SynchronousProductStateMap stateMap =  builder.getStateMap();
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
      final Logger logger = getLogger();
      if (logger.isDebugEnabled()) {
        final Collection<AutomatonProxy> automata1 = des.getAutomata();
        double estimate = 1.0;
        for (final AutomatonProxy aut : automata1) {
          estimate *= aut.getStates().size();
        }
        logger.debug("Monolithically composing " + automata1.size() +
                     " automata, estimated " + estimate + " states.");
      }
      mCurrentMonolithicVerifier.setModel(des);
      mCurrentMonolithicVerifier.run();
      // Do not clean up before run, keep data just in case of overflow ...
      removeEventsToAutomata(automata);
      final VerificationResult subresult =
        mCurrentMonolithicVerifier.getAnalysisResult();
      recordStatistics(subresult);
      if (subresult.isSatisfied()) {
        return true;
      } else {
        final CompositionalVerificationResult result = getAnalysisResult();
        final TraceProxy trace =
          mCurrentMonolithicVerifier.getCounterExample();
        result.setCounterExample(trace);
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
  protected abstract TraceProxy createTrace
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
  private TraceProxy expandTrace(final TraceProxy trace)
    throws AnalysisException
  {
    final List<TraceStepProxy> unsat = trace.getTraceSteps();
    final Collection<AutomatonProxy> currentAutomata = getAllTraceAutomata();
    List<TraceStepProxy> traceSteps =
      getSaturatedTraceSteps(unsat, currentAutomata);
    final int size = mAbstractionSteps.size();
    final ListIterator<AbstractionStep> iter =
      mAbstractionSteps.listIterator(size);
    /*
    final Collection<AutomatonProxy> check =
      new THashSet<AutomatonProxy>(currentAutomata);
    testCounterExample(traceSteps, check);
    */
    while (iter.hasPrevious()) {
      final AbstractionStep step = iter.previous();
      traceSteps = step.convertTraceSteps(traceSteps);
      /*
      check.removeAll(step.getResultAutomata());
      check.addAll(step.getOriginalAutomata());
      testCounterExample(traceSteps, check);
      */
    }
    final ProductDESProxy model = getModel();
    final Collection<AutomatonProxy> modelAutomata = model.getAutomata();
    return createTrace(modelAutomata, traceSteps);
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
  private ModelVerifier mMonolithicVerifier;
  private ModelVerifier mCurrentMonolithicVerifier;
  private List<AbstractionStep> mAbstractionSteps;

}
