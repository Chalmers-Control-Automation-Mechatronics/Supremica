//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters Analysis
//# PACKAGE: net.sourceforge.waters.analysis.trcomp
//# CLASS:   TRAbstractionStepCertainConflicts
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.analysis.trcomp;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import net.sourceforge.waters.analysis.abstraction.LimitedCertainConflictsTRSimplifier;
import net.sourceforge.waters.analysis.tr.EventEncoding;
import net.sourceforge.waters.analysis.tr.EventStatus;
import net.sourceforge.waters.analysis.tr.ListBufferTransitionRelation;
import net.sourceforge.waters.analysis.tr.TRAutomatonProxy;
import net.sourceforge.waters.model.analysis.AnalysisException;
import net.sourceforge.waters.model.des.AutomatonTools;
import net.sourceforge.waters.model.des.EventProxy;
import net.sourceforge.waters.model.des.ProductDESProxy;
import net.sourceforge.waters.model.des.ProductDESProxyFactory;
import net.sourceforge.waters.xsd.base.ComponentKind;
import net.sourceforge.waters.xsd.base.EventKind;

import org.apache.log4j.Logger;


/**
 * An abstraction step representing certain conflicts simplification.
 *
 * @author Robi Malik
 */

class TRAbstractionStepCertainConflicts
  extends TRAbstractionStep
{

  //#########################################################################
  //# Constructor
  TRAbstractionStepCertainConflicts(final TRAbstractionStep pred,
                                    final EventEncoding eventEncoding,
                                    final int defaultMarking,
                                    final LimitedCertainConflictsTRSimplifier simplifier)
  {
    super(pred.getName());
    mPredecessor = pred;
    mEventEncoding = eventEncoding;
    mDefaultMarking = defaultMarking;
    mSimplifier = simplifier;
    mLevels = simplifier.getLevels();
  }


  //#########################################################################
  //# Simple Access
  TRAbstractionStep getPredecessor()
  {
    return mPredecessor;
  }

  int[] getLevels()
  {
    return mLevels;
  }


  //#########################################################################
  //# Interface net.sourceforge.waters.analysis.trcomp.TRAbstractionStep
  @Override
  public Collection<TRAbstractionStep> getPredecessors()
  {
    return Collections.singletonList(mPredecessor);
  }

  @Override
  public TRAutomatonProxy createOutputAutomaton(final int preferredConfig)
    throws AnalysisException
  {
    mSimplifier.setDefaultMarkingID(mDefaultMarking);
    mSimplifier.setPreferredOutputConfiguration(preferredConfig);
    final int inputConfig = mSimplifier.getPreferredInputConfiguration();
    final TRAutomatonProxy inputAut =
      mPredecessor.getOutputAutomaton(inputConfig);
    // We are going to destructively change this automaton,
    // so we need to clear the copy cached on the predecessor.
    mPredecessor.clearOutputAutomaton();
    final ListBufferTransitionRelation inputRel =
      inputAut.getTransitionRelation();
    final EventEncoding inputEventEncoding = new EventEncoding(mEventEncoding);
    final ListBufferTransitionRelation outputRel =
      new ListBufferTransitionRelation(inputRel, inputEventEncoding, inputConfig);
    mSimplifier.setTransitionRelation(outputRel);
    mSimplifier.run();
    return new TRAutomatonProxy(inputEventEncoding, outputRel);
  }

  @Override
  public void expandTrace(final TRTraceProxy trace,
                          final AbstractTRCompositionalAnalyzer analyzer)
    throws AnalysisException
  {
    final TRCompositionalConflictChecker checker =
      (TRCompositionalConflictChecker) analyzer;
    final TraceExpander expander = new TraceExpander(checker);
    expander.pruneTrace(trace);
    trace.replaceAutomaton(this, mPredecessor);
    int level = expander.getLevelOfEndState(trace);
    if (level < 0 || level >= 2) {
      int nextLevel = level < 0 ? Integer.MAX_VALUE : (level | 1) - 2;
      expander.createLanguageInclusionAutomata(trace, nextLevel);
      while (true) {
        final TRTraceProxy extension = expander.findTraceExtension();
        if (extension == null) {
          break;
        }
        final int numSteps = extension.getNumberOfSteps() - 2;
        trace.append(extension, numSteps);
        level = expander.getLevelOfEndState(trace);
        if (level < 2) {
          break;
        }
        nextLevel = (level | 1) - 2;
        expander.setNextLevel(trace, nextLevel);
      }
    }
    assert level < 0 || (level & 1) == 0 :
      "Certain conflicts trace expansion not yet fully implemented!";
  }


  //#########################################################################
  //# Debugging
  @Override
  public void report(final Logger logger)
  {
    if (logger.isDebugEnabled()) {
      logger.debug("Expanding certain conflicts of " + getName() + " ...");
    }
  }


  //#########################################################################
  //# Inner Class TraceExpander
  private class TraceExpander
  {
    //#######################################################################
    //# Constructor
    private TraceExpander(final TRCompositionalConflictChecker checker)
      throws AnalysisException
    {
      mLanguageInclusionChecker = checker.getLanguageInclusionChecker();
      final int config = mLanguageInclusionChecker.getPreferredInputConfiguration();
      final ProductDESProxyFactory factory = checker.getFactory();
      mCertainConflictEvent = factory.createEventProxy
        (CERTAIN_CONFLICT_EVENT_NAME, EventKind.UNCONTROLLABLE);
      final EventEncoding propertyEnc = new EventEncoding();
      propertyEnc.addProperEvent(mCertainConflictEvent, EventStatus.STATUS_NONE);
      final ListBufferTransitionRelation rel = new ListBufferTransitionRelation
        (PROPERTY_NAME, ComponentKind.PROPERTY, propertyEnc, 1, config);
      rel.setInitial(0, true);
      mPropertyAutomaton = new TRAutomatonProxy(propertyEnc, rel);
    }

    //#######################################################################
    //# Trace Expansion
    private void pruneTrace(final TRTraceProxy trace)
      throws AnalysisException
    {
      // Find the last trace step outside of certain conflicts in the
      // output automaton.
      int index;
      for (index = trace.getNumberOfSteps() - 1; index >= 0; index--) {
        final int s =
          trace.getState(TRAbstractionStepCertainConflicts.this, index);
        if (mLevels[s] < 0) {
          break;
        }
      }
      if (index < 0) {
        // The entire trace is in certain conflicts. Find an initial state
        // at the lowest (= most conflicting) level.
        final int init = findMostConflictingInitialState();
        trace.setState(TRAbstractionStepCertainConflicts.this, 0, init);
        index = 0;
      }
      trace.prune(index + 1);
    }

    private void createLanguageInclusionAutomata(final TRTraceProxy trace,
                                                 final int level)
      throws AnalysisException
    {
      mMaxLevel = level;
      final int config =
        mLanguageInclusionChecker.getPreferredInputConfiguration();
      final Set<TRAbstractionStep> steps = trace.getCoveredAbstractionSteps();
      mLanguageInclusionAutomata = new ArrayList<>(steps.size());
      mCertainConflictsAutomaton = new LanguageInclusionAutomaton
        (trace, TRAbstractionStepCertainConflicts.this,
         config, mCertainConflictEvent, mMaxLevel);
      mLanguageInclusionAutomata.add(mCertainConflictsAutomaton);
      for (final TRAbstractionStep step : steps) {
        if (step != mPredecessor) {
          final LanguageInclusionAutomaton aut =
            new LanguageInclusionAutomaton(trace, step, config);
          mLanguageInclusionAutomata.add(aut);
        }
      }
    }

    private void setNextLevel(final TRTraceProxy trace, final int level)
    {
      mCertainConflictsAutomaton.removeLevels
        (TRAbstractionStepCertainConflicts.this,
         mCertainConflictEvent, level + 1, mMaxLevel);
      for (final LanguageInclusionAutomaton aut : mLanguageInclusionAutomata) {
        aut.setCurrentState(trace);
      }
      mMaxLevel = level;
    }

    private TRTraceProxy findTraceExtension()
      throws AnalysisException
    {
      final ProductDESProxy des = createLanguageInclusionModel();
      mLanguageInclusionChecker.setModel(des);
      if (mLanguageInclusionChecker.run()) {
        return null;
      } else {
        final TRTraceProxy trace =
          (TRTraceProxy) mLanguageInclusionChecker.getCounterExample();
        for (final LanguageInclusionAutomaton laut : mLanguageInclusionAutomata) {
          final TRAutomatonProxy aut = laut.getAutomaton();
          final TRAbstractionStep step = laut.getAbstractionStep();
          trace.replaceInputAutomaton(aut, step);
        }
        return trace;
      }
    }

    //#######################################################################
    //# Auxiliary Methods
    private int findMostConflictingInitialState()
      throws AnalysisException
    {
      final int config =
        mLanguageInclusionChecker.getPreferredInputConfiguration();
      final TRAutomatonProxy inputAut = mPredecessor.getOutputAutomaton(config);
      final ListBufferTransitionRelation inputRel =
        inputAut.getTransitionRelation();
      final int numStates = inputRel.getNumberOfStates();
      int found = -1;
      int lowest = Integer.MAX_VALUE;
      for (int s = 0; s < numStates; s++) {
        if (inputRel.isInitial(s) && mLevels[s] < lowest) {
          found = s;
          lowest = mLevels[s];
        }
      }
      assert found >= 0 : "No initial state in certain conflicts automaton!";
      return found;
    }

    private int getLevelOfEndState(final TRTraceProxy trace)
    {
      final int last = trace.getNumberOfSteps() - 1;
      final int end = trace.getState(mPredecessor, last);
      return mLevels[end];
    }

    private ProductDESProxy createLanguageInclusionModel()
    {
      final int numAutomata = mLanguageInclusionAutomata.size() + 1;
      final List<TRAutomatonProxy> automata = new ArrayList<>(numAutomata);
      for (final LanguageInclusionAutomaton aut : mLanguageInclusionAutomata) {
        automata.add(aut.getAutomaton());
      }
      automata.add(mPropertyAutomaton);
      final ProductDESProxyFactory factory =
        mLanguageInclusionChecker.getFactory();
      return
        AutomatonTools.createProductDESProxy(PROPERTY_NAME, automata, factory);
    }

    //#######################################################################
    //# Data Members
    private final TRCompositionalLanguageInclusionChecker mLanguageInclusionChecker;
    private final EventProxy mCertainConflictEvent;
    private final TRAutomatonProxy mPropertyAutomaton;
    private List<LanguageInclusionAutomaton> mLanguageInclusionAutomata;
    private LanguageInclusionAutomaton mCertainConflictsAutomaton;
    private int mMaxLevel;
  }


  //#########################################################################
  //# Inner Class LanguageInclusionAutomaton
  private static class LanguageInclusionAutomaton
  {
    //#######################################################################
    //# Constructor
    private LanguageInclusionAutomaton(final TRTraceProxy trace,
                                       final TRAbstractionStep step,
                                       final int config)
      throws AnalysisException
    {
      mAbstractionStep = step;
      final TRAutomatonProxy inputAut = step.getOutputAutomaton(config);
      mLanguageInclusionAutomaton = new TRAutomatonProxy(inputAut, config);
      mCurrentState = getCurrentState(trace);
      final ListBufferTransitionRelation rel =
        mLanguageInclusionAutomaton.getTransitionRelation();
      final int numStates = rel.getNumberOfStates();
      for (int s = 0; s < numStates; s++) {
        rel.setInitial(s, s == mCurrentState);
      }
    }

    private LanguageInclusionAutomaton(final TRTraceProxy trace,
                                       final TRAbstractionStepCertainConflicts step,
                                       final int config,
                                       final EventProxy ccEvent,
                                       final int maxLevel)
      throws AnalysisException
    {
      mAbstractionStep = step.getPredecessor();
      final TRAutomatonProxy inputAut =
        mAbstractionStep.getOutputAutomaton(config);
      final EventEncoding inputEnc = inputAut.getEventEncoding();
      final EventEncoding langEnc = inputEnc.clone();
      final int cc = langEnc.addProperEvent(ccEvent, EventStatus.STATUS_NONE);
      final ListBufferTransitionRelation inputRel =
        inputAut.getTransitionRelation();
      final ListBufferTransitionRelation langRel =
        new ListBufferTransitionRelation(inputRel, langEnc, config);
      mCurrentState = getCurrentState(trace);
      final int numStates = langRel.getNumberOfStates();
      final int[] levels = step.getLevels();
      for (int s = 0; s < numStates; s++) {
        langRel.setInitial(s, s == mCurrentState);
        if (levels[s] >= 0 && levels[s] <= maxLevel) {
          langRel.addTransition(s, cc, s);
        }
      }
      mLanguageInclusionAutomaton = new TRAutomatonProxy(langEnc, langRel);
    }

    //#######################################################################
    //# Simple Access
    private TRAbstractionStep getAbstractionStep()
    {
      return mAbstractionStep;
    }

    private TRAutomatonProxy getAutomaton()
    {
      return mLanguageInclusionAutomaton;
    }

    //#######################################################################
    //# Auxiliary Methods
    private int getCurrentState(final TRTraceProxy trace)
    {
      final int last = trace.getNumberOfSteps() - 1;
      return trace.getState(mAbstractionStep, last);
    }

    private void setCurrentState(final TRTraceProxy trace)
    {
      final ListBufferTransitionRelation rel =
        mLanguageInclusionAutomaton.getTransitionRelation();
      rel.setInitial(mCurrentState, false);
      mCurrentState = getCurrentState(trace);
      rel.setInitial(mCurrentState, true);
    }

    private void removeLevels(final TRAbstractionStepCertainConflicts step,
                              final EventProxy ccEvent,
                              final int minRemoved,
                              final int maxRemoved)
    {
      final EventEncoding enc = mLanguageInclusionAutomaton.getEventEncoding();
      final int cc = enc.getEventCode(ccEvent);
      final ListBufferTransitionRelation rel =
        mLanguageInclusionAutomaton.getTransitionRelation();
      final int numStates = rel.getNumberOfStates();
      final int[] levels = step.getLevels();
      for (int s = 0; s < numStates; s++) {
        if (levels[s] >= minRemoved && levels[s] <= maxRemoved) {
          rel.removeTransition(s, cc, s);
        }
      }
    }

    //#######################################################################
    //# Debugging
    @Override
    public String toString()
    {
      return mLanguageInclusionAutomaton.getName();
    }

    //#######################################################################
    //# Data Members
    private final TRAbstractionStep mAbstractionStep;
    private final TRAutomatonProxy mLanguageInclusionAutomaton;
    private int mCurrentState;
  }


  //#########################################################################
  //# Data Members
  private final TRAbstractionStep mPredecessor;
  private final EventEncoding mEventEncoding;
  private final int mDefaultMarking;
  private final LimitedCertainConflictsTRSimplifier mSimplifier;
  private final int[] mLevels;


  //#########################################################################
  //# Class Constants
  private static final String CERTAIN_CONFLICT_EVENT_NAME = ":certainconf";
  private static final String PROPERTY_NAME = ":never";

}
