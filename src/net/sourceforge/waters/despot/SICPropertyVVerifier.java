//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.despot
//# CLASS:   SICPropertyVVerifier
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.despot;

import java.util.List;

import net.sourceforge.waters.model.analysis.AbstractConflictChecker;
import net.sourceforge.waters.model.analysis.AnalysisException;
import net.sourceforge.waters.model.analysis.ConflictChecker;
import net.sourceforge.waters.model.analysis.VerificationResult;
import net.sourceforge.waters.model.des.ConflictTraceProxy;
import net.sourceforge.waters.model.des.EventProxy;
import net.sourceforge.waters.model.des.ProductDESProxy;
import net.sourceforge.waters.model.des.ProductDESProxyFactory;


public class SICPropertyVVerifier extends AbstractConflictChecker
{

  //#########################################################################
  //# Constructors
  public SICPropertyVVerifier(final ProductDESProxyFactory factory)
  {
    this(null, factory);
  }

  public SICPropertyVVerifier(final ConflictChecker checker,
                              final ProductDESProxyFactory factory)
  {
    this(checker, null, factory);
  }

  public SICPropertyVVerifier(final ConflictChecker checker,
                              final ProductDESProxy model,
                              final ProductDESProxyFactory factory)
  {
    super(model, factory);
    mChecker = checker;
  }


  //#########################################################################
  //# Configuration
  public void setConflictChecker(final ConflictChecker checker)
  {
    mChecker = checker;
  }


  //#########################################################################
  //# Invocation
  public boolean run() throws AnalysisException
  {
    setUp();
    try {
      final ProductDESProxy model = getModel();
      final SICPropertyVBuilder builder =
        new SICPropertyVBuilder(model, getFactory());
      final List<EventProxy> answers =
        (List<EventProxy>) builder.getAnswerEvents();
      setConflictCheckerMarkings(builder);
      ProductDESProxy convertedModel = null;
      for (final EventProxy answer : answers) {
        convertedModel = builder.createModelForAnswer(answer);
        mChecker.setModel(convertedModel);
        mChecker.run();
        final VerificationResult result = mChecker.getAnalysisResult();
        recordStatistics(result);
        if (!result.isSatisfied()) {
          final ConflictTraceProxy counterexample =
            mChecker.getCounterExample();
          final ConflictTraceProxy convertedTrace =
            builder.convertTraceToOriginalModel(counterexample, answer);
          mFailedAnswer = answer;
          return setFailedResult(convertedTrace);
        }
      }
      return setSatisfiedResult();
    } finally {
      tearDown();
    }
  }

  public EventProxy getFailedAnswer()
  {
    return mFailedAnswer;
  }


  //#########################################################################
  //# Overrides for net.sourceforge.waters.model.AbstractModelAnalyser
  protected void setUp() throws AnalysisException
  {
    super.setUp();
    mPeakNumberOfNodes = -1;
    mTotalNumberOfStates = mTotalNumberOfTransitions = 0.0;
    mPeakNumberOfStates = mPeakNumberOfTransitions = -1.0;
  }

  protected void addStatistics(final VerificationResult result)
  {
    super.addStatistics(result);
    result.setPeakNumberOfNodes(mPeakNumberOfNodes);
    result.setTotalNumberOfStates(mTotalNumberOfStates);
    result.setPeakNumberOfStates(mPeakNumberOfStates);
    result.setTotalNumberOfTransitions(mTotalNumberOfTransitions);
    result.setPeakNumberOfTransitions(mPeakNumberOfTransitions);
  }


  //#########################################################################
  //# Auxiliary Methods
  private void setConflictCheckerMarkings(final SICPropertyVBuilder builder)
  {
    builder.setDefaultMarkings();
    final EventProxy defaultMark = builder.getMarkingProposition();
    final EventProxy preconditionMark = builder.getGeneralisedPrecondition();
    mChecker.setMarkingProposition(defaultMark);
    mChecker.setGeneralisedPrecondition(preconditionMark);
  }

  private void recordStatistics(final VerificationResult result)
  {
    mPeakNumberOfNodes =
      Math.max(mPeakNumberOfNodes, result.getPeakNumberOfNodes());
    mTotalNumberOfStates += result.getPeakNumberOfStates();
    mPeakNumberOfStates =
      Math.max(mPeakNumberOfStates, result.getPeakNumberOfStates());
    mTotalNumberOfTransitions += result.getPeakNumberOfTransitions();
    mPeakNumberOfTransitions =
      Math.max(mPeakNumberOfTransitions, result.getPeakNumberOfTransitions());
  }


  //#########################################################################
  //# Data Members
  private ConflictChecker mChecker;

  private EventProxy mFailedAnswer;

  private int mPeakNumberOfNodes;
  private double mTotalNumberOfStates;
  private double mPeakNumberOfStates;
  private double mTotalNumberOfTransitions;
  private double mPeakNumberOfTransitions;

}
