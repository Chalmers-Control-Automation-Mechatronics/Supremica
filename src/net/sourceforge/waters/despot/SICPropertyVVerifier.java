//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.despot
//# CLASS:   SICPropertyVVerifier
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.despot;

import java.util.ArrayList;
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

  // #########################################################################
  // # Constructors
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
    mConflictCheckerStats = new ArrayList<VerificationResult>();
  }

  // #########################################################################
  // # Configuration
  public void setConflictChecker(final ConflictChecker checker)
  {
    mChecker = checker;
  }

  // #########################################################################
  // # Invocation
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


  // #########################################################################
  // # Interface net.sourceforge.waters.model.ModelAnalyser
  @Override
  public SICPropertyVVerifierVerificationResult getAnalysisResult()
  {
    return (SICPropertyVVerifierVerificationResult) super.getAnalysisResult();
  }


  // #########################################################################
  // # Overrides for net.sourceforge.waters.model.AbstractModelAnalyser
  @Override
  protected void setUp() throws AnalysisException
  {
    super.setUp();
    mPeakNumberOfNodes = -1;
    mTotalNumberOfStates = mTotalNumberOfTransitions = 0.0;
    mPeakNumberOfStates = mPeakNumberOfTransitions = -1.0;
  }

  @Override
  protected SICPropertyVVerifierVerificationResult createAnalysisResult()
  {
    return new SICPropertyVVerifierVerificationResult();
  }

  @Override
  protected void addStatistics()
  {
    super.addStatistics();
    final SICPropertyVVerifierVerificationResult stats = getAnalysisResult();
    stats.setPeakNumberOfNodes(mPeakNumberOfNodes);
    stats.setTotalNumberOfStates(mTotalNumberOfStates);
    stats.setPeakNumberOfStates(mPeakNumberOfStates);
    stats.setTotalNumberOfTransitions(mTotalNumberOfTransitions);
    stats.setPeakNumberOfTransitions(mPeakNumberOfTransitions);
    stats.setConflictCheckerResult(mConflictCheckerStats);
  }


  // #########################################################################
  // # Auxiliary Methods
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
    mConflictCheckerStats.add(result);
  }

  // #########################################################################
  // # Data Members
  private ConflictChecker mChecker;

  private EventProxy mFailedAnswer;

  private int mPeakNumberOfNodes;
  private double mTotalNumberOfStates;
  private double mPeakNumberOfStates;
  private double mTotalNumberOfTransitions;
  private double mPeakNumberOfTransitions;
  private final List<VerificationResult> mConflictCheckerStats;

}
