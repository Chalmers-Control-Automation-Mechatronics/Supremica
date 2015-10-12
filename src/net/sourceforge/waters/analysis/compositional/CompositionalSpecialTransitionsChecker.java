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

package net.sourceforge.waters.analysis.compositional;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import net.sourceforge.waters.analysis.monolithic.MonolithicSynchronousProductBuilder;
import net.sourceforge.waters.cpp.analysis.NativeConflictChecker;
import net.sourceforge.waters.cpp.analysis.NativeLanguageInclusionChecker;
import net.sourceforge.waters.model.analysis.AnalysisException;
import net.sourceforge.waters.model.analysis.ConflictKindTranslator;
import net.sourceforge.waters.model.analysis.KindTranslator;
import net.sourceforge.waters.model.analysis.des.AbstractConflictChecker;
import net.sourceforge.waters.model.analysis.des.ConflictChecker;
import net.sourceforge.waters.model.analysis.des.EventNotFoundException;
import net.sourceforge.waters.model.analysis.des.LanguageInclusionDiagnostics;
import net.sourceforge.waters.model.analysis.des.SafetyDiagnostics;
import net.sourceforge.waters.model.analysis.des.SafetyVerifier;
import net.sourceforge.waters.model.analysis.des.SynchronousProductBuilder;
import net.sourceforge.waters.model.analysis.des.SynchronousProductResult;
import net.sourceforge.waters.model.analysis.des.SynchronousProductStateMap;
import net.sourceforge.waters.model.analysis.des.TraceChecker;
import net.sourceforge.waters.model.des.AutomatonProxy;
import net.sourceforge.waters.model.des.ConflictTraceProxy;
import net.sourceforge.waters.model.des.EventProxy;
import net.sourceforge.waters.model.des.ProductDESEqualityVisitor;
import net.sourceforge.waters.model.des.ProductDESProxy;
import net.sourceforge.waters.model.des.ProductDESProxyFactory;
import net.sourceforge.waters.model.des.SafetyTraceProxy;
import net.sourceforge.waters.model.des.StateProxy;
import net.sourceforge.waters.model.des.TraceProxy;
import net.sourceforge.waters.model.des.TraceStepProxy;
import net.sourceforge.waters.model.des.TransitionProxy;
import net.sourceforge.waters.xsd.des.ConflictKind;

import gnu.trove.map.custom_hash.TObjectIntCustomHashMap;
import gnu.trove.strategy.HashingStrategy;


/**
 * This class will be used to find information about the transitions in systems
 * by investigating the synchronous product of the whole system.
 * This will begin by finding and counting always enabled and disabled transitions.
 *
 * @author Colin Pilbrow
 */

public class CompositionalSpecialTransitionsChecker
  extends AbstractCompositionalModelVerifier
  implements ConflictChecker
{

  //#########################################################################
  //# Constructors
  /**
   * Creates a new conflict checker without a model or marking proposition.
   * @param factory
   *          Factory used for trace construction.
   */
  public CompositionalSpecialTransitionsChecker(final ProductDESProxyFactory factory)
  {
    this(null, factory);
  }

  /**
   * Creates a new conflict checker without a model or marking proposition.
   * @param factory
   *          Factory used for trace construction.
   * @param abstractionCreator
   *          Factory to define the abstraction sequence to be used.
   */
  public CompositionalSpecialTransitionsChecker
    (final ProductDESProxyFactory factory,
     final AbstractionProcedureCreator abstractionCreator)
  {
    this(null, null, factory, abstractionCreator);
  }

  /**
   * Creates a new conflict checker to check whether the given model is
   * nonblocking with respect to its default marking.
   * @param model
   *          The model to be checked by this conflict checker.
   * @param factory
   *          Factory used for trace construction.
   */
  public CompositionalSpecialTransitionsChecker(final ProductDESProxy model,
                                                final ProductDESProxyFactory factory)
  {
    this(model, null, factory);
  }

  /**
   * Creates a new conflict checker to check whether the given model is
   * nonblocking.
   * @param model
   *          The model to be checked by this conflict checker.
   * @param marking
   *          The proposition event that defines which states are marked.
   *          Every state has a list of propositions attached to it; the
   *          conflict checker considers only those states as marked that are
   *          labelled by <CODE>marking</CODE>, i.e., their list of
   *          propositions must contain this event (exactly the same object).
   * @param factory
   *          Factory used for trace construction.
   */
  public CompositionalSpecialTransitionsChecker(final ProductDESProxy model,
                                      final EventProxy marking,
                                      final ProductDESProxyFactory factory)
  {
    this(model, marking, factory, ConflictAbstractionProcedureFactory.OEQ);
  }

  /**
   * Creates a new conflict checker to check whether the given model is
   * nonblocking.
   * @param model
   *          The model to be checked by this conflict checker.
   * @param marking
   *          The proposition event that defines which states are marked.
   *          Every state has a list of propositions attached to it; the
   *          conflict checker considers only those states as marked that are
   *          labelled by <CODE>marking</CODE>, i.e., their list of
   *          propositions must contain this event (exactly the same object).
   * @param factory
   *          Factory used for trace construction.
   * @param abstractionCreator
   *          Factory to define the abstraction sequence to be used.
   */
  public CompositionalSpecialTransitionsChecker
    (final ProductDESProxy model,
     final EventProxy marking,
     final ProductDESProxyFactory factory,
     final AbstractionProcedureCreator abstractionCreator)
  {
    this(model,
         marking,
         factory,
         abstractionCreator,
         new PreselectingMethodFactory());
  }

  /**
   * Creates a new conflict checker to check whether the given model is
   * nonblocking.
   * @param model
   *          The model to be checked by this conflict checker.
   * @param marking
   *          The proposition event that defines which states are marked.
   *          Every state has a list of propositions attached to it; the
   *          conflict checker considers only those states as marked that are
   *          labelled by <CODE>marking</CODE>, i.e., their list of
   *          propositions must contain this event (exactly the same object).
   * @param factory
   *          Factory used for trace construction.
   * @param abstractionCreator
   *          Factory to define the abstraction sequence to be used.
   */
  public CompositionalSpecialTransitionsChecker
    (final ProductDESProxy model,
     final EventProxy marking,
     final ProductDESProxyFactory factory,
     final AbstractionProcedureCreator abstractionCreator,
     final AbstractCompositionalModelAnalyzer.PreselectingMethodFactory preselectingMethodFactory)
  {
    super(model,
          factory,
          ConflictKindTranslator.getInstanceUncontrollable(),
          abstractionCreator,
          preselectingMethodFactory);
    setPruningDeadlocks(true);
    setConfiguredDefaultMarking(marking);
  }


  //#########################################################################
  //# Interface net.sourceforge.waters.model.analysis.ConflictChecker
  @Override
  public ConflictTraceProxy getCounterExample()
  {
    return (ConflictTraceProxy) super.getCounterExample();
  }


  //#########################################################################
  //# Configuration
  public void setCompositionalSafetyVerifier(final SafetyVerifier checker)
  {
    mCompositionalSafetyVerifier = checker;
  }

  public void setMonolithicSafetyVerifier(final SafetyVerifier checker)
  {
    mMonolithicSafetyVerifier = checker;
  }

  @Override
  public ConflictAbstractionProcedureFactory getAbstractionProcedureFactory()
  {
    return ConflictAbstractionProcedureFactory.getInstance();
  }

  @Override
  public void setPreselectingMethod(final PreselectingMethod method)
  {
    super.setPreselectingMethod(method);
    if (mCompositionalSafetyVerifier instanceof CompositionalSafetyVerifier) {
      final CompositionalSafetyVerifier safetyVerifier =
        (CompositionalSafetyVerifier) mCompositionalSafetyVerifier;
      final AbstractCompositionalModelVerifier.PreselectingMethodFactory
        factory = safetyVerifier.getPreselectingMethodFactory();
      final PreselectingMethod safetyMethod = factory.getEnumValue(method);
      if (safetyMethod != null) {
        safetyVerifier.setPreselectingMethod(safetyMethod);
      }
    }
  }

  @Override
  public CompositionalSelectionHeuristicFactory getSelectionHeuristicFactory()
  {
    return ConflictSelectionHeuristicFactory.getInstance();
  }

  @Override
  public void setSelectionHeuristic
    (final SelectionHeuristic<Candidate> heuristic)
  {
    super.setSelectionHeuristic(heuristic);
    if (mCompositionalSafetyVerifier instanceof
        AbstractCompositionalModelAnalyzer) {
      try {
        final AbstractCompositionalModelAnalyzer safetyVerifier =
          (AbstractCompositionalModelAnalyzer) mCompositionalSafetyVerifier;
        final SelectionHeuristic<Candidate> cloned = heuristic.clone();
        cloned.setContext(safetyVerifier);
        safetyVerifier.setSelectionHeuristic(cloned);
      } catch (final ClassCastException exception) {
        // If the safety verifier can't take it, never mind ...
      }
    }
  }

  @Override
  public void setSubumptionEnabled(final boolean enable)
  {
    super.setSubumptionEnabled(enable);
    if (mCompositionalSafetyVerifier instanceof CompositionalSafetyVerifier) {
      final CompositionalSafetyVerifier safetyVerifier =
        (CompositionalSafetyVerifier) mCompositionalSafetyVerifier;
      safetyVerifier.setSubumptionEnabled(enable);
    }
  }

  @Override
  public void setInternalStateLimit(final int limit)
  {
    super.setInternalStateLimit(limit);
    if (mCompositionalSafetyVerifier instanceof CompositionalSafetyVerifier) {
      final CompositionalSafetyVerifier safetyVerifier =
        (CompositionalSafetyVerifier) mCompositionalSafetyVerifier;
      safetyVerifier.setInternalStateLimit(limit);
    }
  }

  @Override
  public void setLowerInternalStateLimit(final int limit)
  {
    super.setLowerInternalStateLimit(limit);
    if (mCompositionalSafetyVerifier instanceof CompositionalSafetyVerifier) {
      final CompositionalSafetyVerifier safetyVerifier =
        (CompositionalSafetyVerifier) mCompositionalSafetyVerifier;
      safetyVerifier.setLowerInternalStateLimit(limit);
    }
  }

  @Override
  public void setUpperInternalStateLimit(final int limit)
  {
    super.setUpperInternalStateLimit(limit);
    if (mCompositionalSafetyVerifier instanceof CompositionalSafetyVerifier) {
      final CompositionalSafetyVerifier safetyVerifier =
        (CompositionalSafetyVerifier) mCompositionalSafetyVerifier;
      safetyVerifier.setUpperInternalStateLimit(limit);
    }
  }

  @Override
  public void setMonolithicStateLimit(final int limit)
  {
    super.setMonolithicStateLimit(limit);
    if (mCompositionalSafetyVerifier instanceof CompositionalSafetyVerifier) {
      final CompositionalSafetyVerifier safetyVerifier =
        (CompositionalSafetyVerifier) mCompositionalSafetyVerifier;
      safetyVerifier.setMonolithicStateLimit(limit);
    } else if (mMonolithicSafetyVerifier != null) {
      mMonolithicSafetyVerifier.setNodeLimit(limit);
    }
  }

  @Override
  public void setMonolithicTransitionLimit(final int limit)
  {
    super.setMonolithicTransitionLimit(limit);
    if (mCompositionalSafetyVerifier instanceof CompositionalSafetyVerifier) {
      final CompositionalSafetyVerifier safetyVerifier =
        (CompositionalSafetyVerifier) mCompositionalSafetyVerifier;
      safetyVerifier.setMonolithicTransitionLimit(limit);
    } else if (mMonolithicSafetyVerifier != null) {
      mMonolithicSafetyVerifier.setTransitionLimit(limit);
    }
  }

  @Override
  public void setInternalTransitionLimit(final int limit)
  {
    super.setInternalTransitionLimit(limit);
    if (mCompositionalSafetyVerifier instanceof CompositionalSafetyVerifier) {
      final CompositionalSafetyVerifier safetyVerifier =
        (CompositionalSafetyVerifier) mCompositionalSafetyVerifier;
      safetyVerifier.setInternalTransitionLimit(limit);
    }
  }


  //#########################################################################
  //# Specific Access
  @Override
  protected void setupMonolithicAnalyzer()
    throws EventNotFoundException
  {
    if (getCurrentMonolithicAnalyzer() == null) {
      final ConflictChecker configured =
        (ConflictChecker) getMonolithicAnalyzer();
      final ConflictChecker current;
      if (configured == null) {
        final ProductDESProxyFactory factory = getFactory();
        final NativeConflictChecker nativeChecker =
          new NativeConflictChecker(factory);
        final boolean aware = getConfiguredPreconditionMarking() == null;
        nativeChecker.setDumpStateAware(aware);
        current = nativeChecker;
      } else {
        current = configured;
      }
      final EventProxy defaultMarking = getUsedDefaultMarking();
      current.setConfiguredDefaultMarking(defaultMarking);
      final EventProxy preconditionMarking = getUsedPreconditionMarking();
      current.setConfiguredPreconditionMarking(preconditionMarking);
      setCurrentMonolithicAnalyzer(current);
      super.setupMonolithicAnalyzer();
    }
  }

  SafetyVerifier getCurrentCompositionalSafetyVerifier()
  {
    return mCurrentCompositionalSafetyVerifier;
  }

  private void setupSafetyVerifiers()
  {
    final ProductDESProxyFactory factory = getFactory();
    if (mCurrentMonolithicSafetyVerifier == null) {
      if (mMonolithicSafetyVerifier == null) {
        mCurrentMonolithicSafetyVerifier =
          new NativeLanguageInclusionChecker(factory);
      } else {
        mCurrentMonolithicSafetyVerifier = mMonolithicSafetyVerifier;
      }
      final int nlimit = getMonolithicStateLimit();
      mCurrentMonolithicSafetyVerifier.setNodeLimit(nlimit);
      final int tlimit = getMonolithicTransitionLimit();
      mCurrentMonolithicSafetyVerifier.setTransitionLimit(tlimit);
    }
    if (mCurrentCompositionalSafetyVerifier == null) {
      if (mCompositionalSafetyVerifier == null) {
        final SafetyDiagnostics diag =
          LanguageInclusionDiagnostics.getInstance();
        final CompositionalSafetyVerifier safetyVerifier =
          new CompositionalSafetyVerifier(factory, null, diag);
        final AbstractCompositionalModelVerifier.PreselectingMethodFactory
          pfactory = safetyVerifier.getPreselectingMethodFactory();
        final PreselectingMethod pmethod = getPreselectingMethod();
        final PreselectingMethod ppmethod = pfactory.getEnumValue(pmethod);
        if (ppmethod != null) {
          safetyVerifier.setPreselectingMethod(ppmethod);
        }
        try {
          final SelectionHeuristic<Candidate> heuristic =
            getSelectionHeuristic();
          final SelectionHeuristic<Candidate> cloned = heuristic.clone();
          cloned.setContext(safetyVerifier);
          safetyVerifier.setSelectionHeuristic(cloned);
        } catch (final ClassCastException exception) {
          // If the safety verifier can't take it, never mind ...
        }
        final boolean enable = isSubsumptionEnabled();
        safetyVerifier.setSubumptionEnabled(enable);
        final int lslimit = getLowerInternalStateLimit();
        safetyVerifier.setLowerInternalStateLimit(lslimit);
        final int uslimit = getUpperInternalStateLimit();
        safetyVerifier.setUpperInternalStateLimit(uslimit);
        final int mslimit = getMonolithicStateLimit();
        safetyVerifier.setMonolithicStateLimit(mslimit);
        final int itlimit = getInternalTransitionLimit();
        safetyVerifier.setInternalTransitionLimit(itlimit);
        final int mtlimit = getMonolithicTransitionLimit();
        safetyVerifier.setMonolithicTransitionLimit(mtlimit);
        mCurrentCompositionalSafetyVerifier  = safetyVerifier;
      } else {
        mCurrentCompositionalSafetyVerifier = mCompositionalSafetyVerifier;
      }
    }
  }


  //#########################################################################
  //# Interface net.sourceforge.waters.model.analysis.Abortable
  @Override
  public void requestAbort()
  {
    super.requestAbort();
    if (mCurrentCompositionalSafetyVerifier != null) {
      mCurrentCompositionalSafetyVerifier.requestAbort();
    }
  }

  @Override
  public void resetAbort()
  {
    super.resetAbort();
    if (mCurrentCompositionalSafetyVerifier != null) {
      mCurrentCompositionalSafetyVerifier.resetAbort();
    }
  }


  //#########################################################################
  //# Overrides for net.sourceforge.waters.model.AbstractModelAnalyser
  /**
   * Initialises required variables to default values if the user has not
   * configured them.
   */
  @Override
  protected void setUp()
    throws AnalysisException
  {
    final EventProxy defaultMarking = createDefaultMarking();
    final AbstractionProcedureCreator abstraction =
      getAbstractionProcedureCreator();
    final EventProxy preconditionMarking;
    if (abstraction.expectsAllMarkings()) {
      preconditionMarking = createPreconditionMarking();
    } else {
      preconditionMarking = getConfiguredPreconditionMarking();
    }
    setPropositionsForMarkings(defaultMarking, preconditionMarking);
    super.setUp();
    setupSafetyVerifiers();
  }

  @Override
  protected void tearDown()
  {
    super.tearDown();
    mCurrentCompositionalSafetyVerifier = null;
    mCurrentMonolithicSafetyVerifier = null;
    mAutomatonInfoMap = null;
  }

  @Override
  public boolean run()
  {
    try {
      setUp();
    analyseSystemTransitions();
    tearDown();
    } catch (final Exception e) {
      e.printStackTrace();
    }
    return true;
  }

  private void analyseSystemTransitions() throws AnalysisException
  {
    //Creates a hashmap transInfo that can store transitions.
    final HashingStrategy<TransitionProxy> strategy =
      ProductDESEqualityVisitor.getNonDeterminsiticTransitionHashingStrategy();
    final TObjectIntCustomHashMap<TransitionProxy> transInfo =
      new TObjectIntCustomHashMap<>(strategy, 0);

    //Because the automata can be nondeterministic, keep track of if we've already
    //seen an event in a state.
    final HashMap<StateProxy,ArrayList<EventProxy>> visitedStates =
      new HashMap<>();

    //Fill the hashmap with all the transitions in the original automata.
    for (final AutomatonProxy aut : getModel().getAutomata()) {
      for (final TransitionProxy trans : aut.getTransitions()) {
        transInfo.put(trans, 0);
      }
    }

    //Create the synchronous product of the system.
    final MonolithicSynchronousProductBuilder builder =
      getSynchronousProductBuilder();
    builder.setModel(getModel());
    builder.run();
    final AutomatonProxy sync = builder.getComputedAutomaton();
    final SynchronousProductResult result = builder.getAnalysisResult();
    final SynchronousProductStateMap synchronousStateMap = result.getStateMap();

    //For each transition of the synchronous product
    for (final TransitionProxy trans : sync.getTransitions()) {
      // which has the form X -e-> Y
      final EventProxy e = trans.getEvent();
      // where X = (x1,...,xn) and Y = (y1,...,yn) are state tuples
      // of the synchronous product

      //We do not want to double count the nondeterministic trans in sync
      final ArrayList<EventProxy> eventsSeen =
        visitedStates.get(trans.getSource());
      if (eventsSeen != null) {
        if (eventsSeen.contains(e)) {
          //Then we have already seen this event on this state and can stop
          continue;
        } else {
          //Otherwise add this event to the events we have seen
          eventsSeen.add(e);
          visitedStates.put(trans.getSource(), eventsSeen);
        }
      } else {
        //If we have never seen this state before create a List with the new event
        //and add it to visited states.
        final ArrayList<EventProxy> newStateFound = new ArrayList<>();
        newStateFound.add(e);
        visitedStates.put(trans.getSource(), newStateFound);
      }

      for (final AutomatonProxy inputAut : synchronousStateMap
        .getInputAutomata()) {
        //find the states xi and yi corresponding to Ai in X and Y
        final StateProxy originalSource =
          synchronousStateMap.getOriginalState(trans.getSource(), inputAut);
        final StateProxy originalTarget =
          synchronousStateMap.getOriginalState(trans.getTarget(), inputAut);
        //Build the original transition xi -e-> yi
        final TransitionProxy originalTransition =
          getFactory().createTransitionProxy(originalSource, e,
                                             originalTarget);

        //Counts the number of times a transition is seen in the synchronous product.
        final int numTimesSeen = transInfo.get(originalTransition);
        transInfo.put(originalTransition, numTimesSeen + 1);
      }
    }

    int alwaysEnabledTransCounter = 0;
    //For each automaton
    for (final AutomatonProxy inputAut : synchronousStateMap.getInputAutomata()) {
      final HashMap<StateProxy,Integer> stateCounter = new HashMap<>();
      for (final StateProxy state : inputAut.getStates()) {

        //Add all states to the hashmap
        stateCounter.put(state, 0);
      }

      //Loop through the states in the synchronous product
      for (final StateProxy syncState : sync.getStates()) {
        final StateProxy originalState =
          synchronousStateMap.getOriginalState(syncState, inputAut);
        //Count how many times each state appears in the synchronous product
        final int numTimesSeen = stateCounter.get(originalState);
        stateCounter.put(originalState, numTimesSeen + 1);
      }

      //If a transition with source state s1 has seen x times in sync
      //And s1 was in sync in x places
      //Then the transition must be always enabled.
      for (final TransitionProxy trans : inputAut.getTransitions()) {
        final StateProxy s1 = trans.getSource();
        final int transCount = transInfo.get(trans);
        final int stateCount = stateCounter.get(s1);
        if (transCount == stateCount) {
          //Found always enabled transition.
          alwaysEnabledTransCounter++;
          System.out.println("Found always enabled transition: "
                             + trans.getSource().getName() + " -"
                             + trans.getEvent().getName() + "-> "
                             + trans.getTarget().getName());
        }
        if (transCount > stateCount) {
          System.err.println("Too many transitions found " + transCount
                             + " trans vs " + stateCount + " states");
        }
      }
    }

    int alwaysDisabledTransCounter = 0;
    for (final TransitionProxy trans : transInfo.keySet()) {
      //if numTimesSeen is still 0 it is always disabled transition.

      if (transInfo.get(trans) == 0) {
        alwaysDisabledTransCounter++;
        System.out.println("Found always disabled transition: "
                           + trans.getSource().getName() + " -"
                           + trans.getEvent().getName() + "-> "
                           + trans.getTarget().getName());
      }
    }
    System.out.println("Found " + alwaysDisabledTransCounter
                       + " always disabled transitions in this model.");
    System.out.println("Found " + alwaysEnabledTransCounter
                       + " always enabled transitions in this model.");
  }

  //#########################################################################
  //# Hooks
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
    return new ConflictHidingStep(this, sync, hidden, tau, stateMap);
  }

  @Override
  protected boolean isSubsystemTrivial
    (final Collection<AutomatonProxy> automata)
    throws AnalysisException
  {

    final byte status = getSubsystemPropositionStatus(automata);
    if ((status & NONE_ALPHA) != 0) {
      // The global system is nonblocking.
      final CompositionalVerificationResult result = getAnalysisResult();
      result.setSatisfied(true);
      return true;
    } else if ((status & ALL_OMEGA) != 0) {
      // This subsystem is trivially nonblocking.
      return true;
    } else if ((status & NONE_OMEGA) != 0) {
      // The global system is blocking if and only if alpha is reachable
      checkAlphaReachable((status & ALL_ALPHA) == 0);
      return true;
    } else {
      return false;
    }
  }

  @Override
  protected boolean confirmMonolithicCounterExample()
    throws AnalysisException
  {
    return checkAlphaReachable(false);
  }

  @Override
  protected ConflictTraceProxy createTrace
    (final Collection<AutomatonProxy> automata,
     final List<TraceStepProxy> steps)
  {
    final ProductDESProxyFactory factory = getFactory();
    final ProductDESProxy model = getModel();
    final String tracename = AbstractConflictChecker.getTraceName(model);
    final CompositionalVerificationResult result = getAnalysisResult();
    final ConflictTraceProxy trace =
      (ConflictTraceProxy) result.getCounterExample();
    final ConflictKind kind = trace.getKind();
    return factory.createConflictTraceProxy(tracename,
                                            null,  // comment?
                                            null,
                                            model,
                                            automata,
                                            steps,
                                            kind);
  }

  @Override
  protected void testCounterExample
    (final List<TraceStepProxy> steps,
     final Collection<AutomatonProxy> automata)
    throws AnalysisException
  {
    final KindTranslator translator = getKindTranslator();
    final EventProxy defaultMarking = getUsedDefaultMarking();
    final EventProxy preconditionMarking = getUsedPreconditionMarking();
    TraceChecker.checkConflictCounterExample(steps, automata,
                                             preconditionMarking,
                                             defaultMarking,
                                             true, translator);
  }


  //#########################################################################
  //# Events+Automata Maps
  @Override
  protected void initialiseEventsToAutomata()
    throws AnalysisException
  {
    super.initialiseEventsToAutomata();
    final ProductDESProxy model = getModel();
    final Collection<AutomatonProxy> automata = model.getAutomata();
    final int numAutomata = automata.size();
    mAutomatonInfoMap =
      new HashMap<AutomatonProxy,AutomatonInfo>(numAutomata);
  }

  @Override
  protected void removeEventsToAutomata
    (final Collection<AutomatonProxy> victims)
  {
    for (final AutomatonProxy aut : victims) {
      mAutomatonInfoMap.remove(aut);
    }
    super.removeEventsToAutomata(victims);
  }


  //#########################################################################
  //# Proposition Analysis
  private byte getSubsystemPropositionStatus
    (final Collection<AutomatonProxy> automata)
  throws EventNotFoundException
  {
    byte all = ALL_ALPHA | ALL_OMEGA;
    byte none = 0;
    for (final AutomatonProxy aut : automata) {
      final AutomatonInfo info = getAutomatonInfo(aut);
      if (info.isNeverPreconditionMarked()) {
        return NONE_ALPHA;
      }
      if (info.isNeverDefaultMarked()) {
        none |= NONE_OMEGA;
      }
      if (!info.isAlwaysDefaultMarked()) {
        all &= ~ALL_OMEGA;
      }
      if (!info.isAlwaysPreconditionMarked()) {
        all &= ~ALL_ALPHA;
      }
    }
    return (byte) (all | none);
  }

  AutomatonInfo getAutomatonInfo(final AutomatonProxy aut)
  {
    AutomatonInfo info = mAutomatonInfoMap.get(aut);
    if (info == null) {
      info = new AutomatonInfo(aut);
      mAutomatonInfoMap.put(aut, info);
    }
    return info;
  }


  //#########################################################################
  //# Alpha-Reachability Check
  private boolean checkAlphaReachable(final boolean includeCurrent)
    throws AnalysisException
  {
    final EventProxy preconditionMarking = getConfiguredPreconditionMarking();
    final CompositionalVerificationResult result = getAnalysisResult();
    if (preconditionMarking == null) {
      if (result.getCounterExample() == null) {
        final ConflictTraceProxy trace = createInitialStateTrace();
        result.setCounterExample(trace);
      }
      return true;
    } else {
      final ProductDESProxyFactory factory = getFactory();
      final KindTranslator translator = getKindTranslator();
      final PropositionPropertyBuilder builder =
        new PropositionPropertyBuilder(factory,
                                       preconditionMarking,
                                       translator);
      final List<ConflictTraceProxy> traces =
        new LinkedList<ConflictTraceProxy>();
      final TraceProxy trace = result.getCounterExample();
      if (trace != null) {
        final ConflictTraceProxy conflict = (ConflictTraceProxy) trace;
        traces.add(conflict);
      }
      if (includeCurrent) {
        if (!isAlphaReachable(builder, getCurrentAutomata(), traces)) {
          result.setSatisfied(true);
          return false;
        }
      }
      for (final SubSystem subsys : getPostponedSubsystems()) {
        if (!isAlphaReachable(builder, subsys, traces)) {
          result.setSatisfied(true);
          return false;
        }
      }
      for (final SubSystem subsys : getProcessedSubsystems()) {
        if (!isAlphaReachable(builder, subsys, traces)) {
          result.setSatisfied(true);
          return false;
        }
      }
      final ConflictTraceProxy merged = mergeLanguageInclusionTraces(traces);
      result.setCounterExample(merged);
      return true;
    }
  }

  private boolean isAlphaReachable(final PropositionPropertyBuilder builder,
                                   final SubSystem subsys,
                                   final List<ConflictTraceProxy> traces)
    throws AnalysisException
  {
    final List<AutomatonProxy> automata = subsys.getAutomata();
    return isAlphaReachable(builder, automata, traces);
  }

  private boolean isAlphaReachable(final PropositionPropertyBuilder builder,
                                   final List<AutomatonProxy> automata,
                                   final List<ConflictTraceProxy> traces)
    throws AnalysisException
  {
    final ProductDESProxy des = createProductDESProxy(automata);
    builder.setInputModel(des);
    builder.run();
    final ProductDESProxy languageInclusionModel = builder.getOutputModel();
    final KindTranslator languageInclusionTranslator =
      builder.getKindTranslator();
    final SafetyVerifier checker;
    if (languageInclusionModel.getAutomata().size() > 2) {
      checker = mCurrentCompositionalSafetyVerifier;
    } else {
      checker = mCurrentMonolithicSafetyVerifier;
    }
    checker.setKindTranslator(languageInclusionTranslator);
    checker.setModel(languageInclusionModel);
    if (checker.run()) {
      return false;
    } else {
      final SafetyTraceProxy languageInclusionTrace =
        checker.getCounterExample();
      final ConflictTraceProxy conflictTrace =
        builder.getConvertedConflictTrace(languageInclusionTrace);
      traces.add(conflictTrace);
      return true;
    }
  }

  private ConflictTraceProxy createInitialStateTrace()
  {
    final ProductDESProxyFactory factory = getFactory();
    final ProductDESProxy model = getModel();
    final String name = model.getName() + ":initial";
    final String comment = "Initial state trace";
    final int numAutomata = model.getAutomata().size();
    final Collection<AutomatonProxy> automata =
      new ArrayList<AutomatonProxy>(numAutomata);
    automata.addAll(getCurrentAutomata());
    for (final SubSystem subsys : getPostponedSubsystems()) {
      final Collection<AutomatonProxy> moreAutomata = subsys.getAutomata();
      automata.addAll(moreAutomata);
    }
    for (final SubSystem subsys : getProcessedSubsystems()) {
      final Collection<AutomatonProxy> moreAutomata = subsys.getAutomata();
      automata.addAll(moreAutomata);
    }
    final TraceStepProxy step = factory.createTraceStepProxy(null);
    final List<TraceStepProxy> steps = Collections.singletonList(step);
    return factory.createConflictTraceProxy
      (name, comment, null, model, automata, steps, ConflictKind.CONFLICT);
  }

  private ConflictTraceProxy mergeLanguageInclusionTraces
    (final Collection<ConflictTraceProxy> traces)
  {
    int numAutomata = 0;
    int numSteps = 1;
    for (final ConflictTraceProxy trace : traces) {
      numAutomata += trace.getAutomata().size();
      numSteps += trace.getTraceSteps().size() - 1;
    }
    final Collection<AutomatonProxy> automata =
      new ArrayList<AutomatonProxy>(numAutomata);
    final Map<AutomatonProxy,StateProxy> initMap =
      new HashMap<AutomatonProxy,StateProxy>(numAutomata);
    final List<TraceStepProxy> steps = new ArrayList<TraceStepProxy>(numSteps);
    steps.add(null);
    for (final ConflictTraceProxy trace : traces) {
      automata.addAll(trace.getAutomata());
      final List<TraceStepProxy> traceSteps = trace.getTraceSteps();
      final Iterator<TraceStepProxy> iter = traceSteps.iterator();
      final TraceStepProxy traceInitStep = iter.next();
      initMap.putAll(traceInitStep.getStateMap());
      while (iter.hasNext()) {
        steps.add(iter.next());
      }
    }
    final ProductDESProxy model = getModel();
    final EventProxy preconditionMarking = getConfiguredPreconditionMarking();
    final String name = model.getName() + preconditionMarking.getName();
    final ProductDESProxyFactory factory = getFactory();
    final TraceStepProxy initStep = factory.createTraceStepProxy(null, initMap);
    steps.set(0, initStep);
    return factory.createConflictTraceProxy(name, null, null, model, automata,
                                            steps, ConflictKind.CONFLICT);
  }


  //#########################################################################
  //# Inner Class PreselectingMethodFactory
  protected static class PreselectingMethodFactory
    extends AbstractCompositionalModelAnalyzer.PreselectingMethodFactory
  {
    //#######################################################################
    //# Constructors
    protected PreselectingMethodFactory()
    {
      register(MinTa);
    }
  }


  //#########################################################################
  //# Preselection Methods
  /**
   * The preselecting method that produces candidates by pairing the
   * automaton with the fewest transitions connected to a precondition-marked
   * state to every other automaton in the model.
   */
  public static final PreselectingMethod MinTa =
      new PreselectingMethod("MinTa")
  {
    @Override
    PreselectingHeuristic createHeuristic
      (final AbstractCompositionalModelAnalyzer analyzer)
    {
      final CompositionalSpecialTransitionsChecker checker =
          (CompositionalSpecialTransitionsChecker) analyzer;
      return checker.new HeuristicMinTAlpha();
    }
    @Override
    protected PreselectingMethod getCommonMethod()
    {
      return MinT;
    }
  };


  //#########################################################################
  //# Inner Class AutomatonInfo
  /**
   * A record to store information about an automaton.
   * The automaton information record contains the number of precondition
   * and default markings.
   */
  class AutomatonInfo
  {

    //#######################################################################
    //# Constructor
    private AutomatonInfo(final AutomatonProxy aut)
    {
      mAutomaton = aut;
      mNumPreconditionMarkedStates = mNumDefaultMarkedStates =
        mNumPreconditionTransitions -1;
    }

    //#######################################################################
    //# Access Methods
    private boolean isNeverPreconditionMarked()
    {
      if (mNumPreconditionMarkedStates < 0) {
        countPropositions();
      }
      return mNumPreconditionMarkedStates == 0;
    }

    private boolean isAlwaysPreconditionMarked()
    {
      if (mNumPreconditionMarkedStates < 0) {
        countPropositions();
      }
      return mNumPreconditionMarkedStates == mAutomaton.getStates().size();
    }

    int getNumberOfPreconditionMarkedStates()
    {
      if (mNumPreconditionMarkedStates < 0) {
        countPropositions();
      }
      return mNumPreconditionMarkedStates;
    }

    private boolean isNeverDefaultMarked()
    {
      if (mNumDefaultMarkedStates < 0) {
        countPropositions();
      }
      return mNumDefaultMarkedStates == 0;
    }

    private boolean isAlwaysDefaultMarked()
    {
      if (mNumDefaultMarkedStates < 0) {
        countPropositions();
      }
      return mNumDefaultMarkedStates == mAutomaton.getStates().size();
    }

    private int getNumberOfPreconditionMarkedTransitions()
    {
      if (mNumPreconditionTransitions < 0) {
        final Collection<TransitionProxy> transitions =
          mAutomaton.getTransitions();
        if (isNeverPreconditionMarked()) {
          mNumPreconditionTransitions = 0;
        } else if (isAlwaysPreconditionMarked()) {
          mNumPreconditionTransitions = 2 * transitions.size();
        } else {
          final EventProxy alpha = getUsedPreconditionMarking();
          mNumPreconditionTransitions = 0;
          for (final TransitionProxy trans : transitions) {
            if (trans.getSource().getPropositions().contains(alpha)) {
              mNumPreconditionTransitions++;
            }
            if (trans.getTarget().getPropositions().contains(alpha)) {
              mNumPreconditionTransitions++;
            }
          }
        }
      }
      return mNumPreconditionTransitions;
    }

    //#######################################################################
    //# Auxiliary Methods
    private void countPropositions()
    {
      final EventProxy alpha = getUsedPreconditionMarking();
      final EventProxy omega = getUsedDefaultMarking();
      boolean usesAlpha = false;
      boolean usesOmega = false;
      for (final EventProxy event : mAutomaton.getEvents()) {
        if (event == omega) {
          usesOmega = true;
          if (usesAlpha || alpha == null) {
            break;
          }
        } else if (event == alpha) {
          usesAlpha = true;
          if (usesOmega) {
            break;
          }
        }
      }
      final Collection<StateProxy> states = mAutomaton.getStates();
      final int numStates = states.size();
      mNumDefaultMarkedStates = usesOmega ? 0 : numStates;
      mNumPreconditionMarkedStates = usesAlpha ? 0 : numStates;
      boolean hasinit = false;
      for (final StateProxy state : states) {
        hasinit |= state.isInitial();
        if (usesAlpha || usesOmega) {
          boolean containsAlpha = false;
          boolean containsOmega = false;
          for (final EventProxy prop : state.getPropositions()) {
            if (prop == omega) {
              containsOmega = true;
            } else if (prop == alpha) {
              containsAlpha = true;
            }
          }
          if (containsAlpha && usesAlpha) {
            mNumPreconditionMarkedStates++;
          }
          if (containsOmega && usesOmega) {
            mNumDefaultMarkedStates++;
          }
        }
      }
      if (!hasinit) {
        mNumPreconditionMarkedStates = 0;
      }
    }

    //#######################################################################
    //# Data Members
    private final AutomatonProxy mAutomaton;
    private int mNumPreconditionMarkedStates;
    private int mNumDefaultMarkedStates;
    private int mNumPreconditionTransitions;

  }


  //#########################################################################
  //# Inner Class HeuristicMinTAlpha
  private class HeuristicMinTAlpha
    extends PairingHeuristic
  {

    //#######################################################################
    //# Interface java.util.Comparator<AutomatonProxy>
    @Override
    public int compare(final AutomatonProxy aut1, final AutomatonProxy aut2)
    {
      final int numalpha1 =
        getAutomatonInfo(aut1).getNumberOfPreconditionMarkedTransitions();
      final int numalpha2 =
        getAutomatonInfo(aut2).getNumberOfPreconditionMarkedTransitions();
      if (numalpha1 != numalpha2) {
        return numalpha1 - numalpha2;
      }
      final int numtrans1 = aut1.getTransitions().size();
      final int numtrans2 = aut2.getTransitions().size();
      if (numtrans1 != numtrans2) {
        return numtrans1 - numtrans2;
      }
      final int numstates1 = aut1.getStates().size();
      final int numstates2 = aut2.getStates().size();
      if (numstates1 != numstates2) {
        return numstates1 - numstates2;
      }
      return aut1.compareTo(aut2);
    }

  }


  //#########################################################################
  //# Data Members
  private SafetyVerifier mCompositionalSafetyVerifier;
  private SafetyVerifier mMonolithicSafetyVerifier;

  private SafetyVerifier mCurrentCompositionalSafetyVerifier;
  private SafetyVerifier mCurrentMonolithicSafetyVerifier;

  /**
   * The automata currently being analysed. This list is updated after each
   * abstraction step and represents the current state of the model. It may
   * contain abstractions of only part of the original model, if event
   * disjoint subsystems are found.
   */
  private Map<AutomatonProxy,AutomatonInfo> mAutomatonInfoMap;


  //#########################################################################
  //# Class Constants
  private static final byte NONE_OMEGA = 0x01;
  private static final byte ALL_OMEGA = 0x02;
  private static final byte NONE_ALPHA = 0x04;
  private static final byte ALL_ALPHA = 0x08;

}
