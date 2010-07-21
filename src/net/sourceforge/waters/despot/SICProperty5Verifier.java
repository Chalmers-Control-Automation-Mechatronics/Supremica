//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.despot
//# CLASS:   SICProperty5Verifier
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.despot;

import java.util.ArrayList;
import java.util.List;

import net.sourceforge.waters.model.analysis.AnalysisException;
import net.sourceforge.waters.model.analysis.ConflictChecker;
import net.sourceforge.waters.model.analysis.VerificationResult;
import net.sourceforge.waters.model.des.ConflictTraceProxy;
import net.sourceforge.waters.model.des.EventProxy;
import net.sourceforge.waters.model.des.ProductDESProxy;
import net.sourceforge.waters.model.des.ProductDESProxyFactory;


/**
 * A model verifier to check SIC Property V.
 *
 * This wrapper can be used to check whether an HISC low-level model satisfies
 * Serial Interface Consistency (SIC) Property V or Low-Data Interface
 * Consistency (LDIC) Property V.
 *
 * The check is done by creating a generalised nonblocking verification problem
 * for each answer event in the model, and passing these models to a generalised
 * conflict checker.
 *
 * @see SICPropertyBuilder
 * @see ConflictChecker
 *
 * @author Rachel Francis
 */

public class SICProperty5Verifier extends AbstractSICConflictChecker
{

  //#########################################################################
  //# Constructors
  public SICProperty5Verifier(final ProductDESProxyFactory factory)
  {
    super(factory);
  }

  public SICProperty5Verifier(final ConflictChecker checker,
                              final ProductDESProxyFactory factory)
  {
    super(checker, factory);
  }

  public SICProperty5Verifier(final ConflictChecker checker,
                              final ProductDESProxy model,
                              final ProductDESProxyFactory factory)
  {
    super(checker, model, factory);
  }


  //#########################################################################
  //# Invocation
  public boolean run() throws AnalysisException
  {
    setUp();
    try {
      final ProductDESProxy model = getModel();
      final SICPropertyBuilder builder =
          new SICPropertyBuilder(model, getFactory());
      final List<EventProxy> answers =
          (List<EventProxy>) builder.getAnswerEvents();
      final int numAnswers = answers.size();
      mConflictCheckerStats = new ArrayList<VerificationResult>(numAnswers);
      setConflictCheckerMarkings(builder);
      final ConflictChecker checker = getConflictChecker();
      ProductDESProxy convertedModel = null;
      for (final EventProxy answer : answers) {
        convertedModel = builder.createSIC5Model(answer);
        checker.setModel(convertedModel);
        final VerificationResult result;
        try {
          checker.run();
        } finally {
          result = checker.getAnalysisResult();
          recordStatistics(result);
        }
        if (!result.isSatisfied()) {
          final ConflictTraceProxy counterexample =
              checker.getCounterExample();
          final ConflictTraceProxy convertedTrace =
              builder.convertTraceToOriginalModel(counterexample, answer);
          mFailedAnswer = answer;
          return setFailedResult(convertedTrace);
        }
      }
      return setSatisfiedResult();
    } finally {
      tearDown();
      mConflictCheckerStats = null;
    }
  }

  public EventProxy getFailedAnswer()
  {
    return mFailedAnswer;
  }


  //#########################################################################
  //# Interface net.sourceforge.waters.model.ModelAnalyser
  @Override
  public SICProperty5VerifierVerificationResult getAnalysisResult()
  {
    return (SICProperty5VerifierVerificationResult) super.getAnalysisResult();
  }


  //#########################################################################
  //# Overrides for net.sourceforge.waters.model.AbstractModelAnalyser
  @Override
  protected void setUp() throws AnalysisException
  {
    super.setUp();
    mPeakNumberOfNodes = -1;
    mTotalNumberOfStates = mTotalNumberOfTransitions = 0.0;
    mPeakNumberOfStates = mPeakNumberOfTransitions = -1.0;
  }

  @Override
  protected SICProperty5VerifierVerificationResult createAnalysisResult()
  {
    return new SICProperty5VerifierVerificationResult();
  }

  @Override
  protected void addStatistics()
  {
    super.addStatistics();
    final SICProperty5VerifierVerificationResult stats = getAnalysisResult();
    stats.setPeakNumberOfNodes(mPeakNumberOfNodes);
    stats.setTotalNumberOfStates(mTotalNumberOfStates);
    stats.setPeakNumberOfStates(mPeakNumberOfStates);
    stats.setTotalNumberOfTransitions(mTotalNumberOfTransitions);
    stats.setPeakNumberOfTransitions(mPeakNumberOfTransitions);
    stats.setConflictCheckerResult(mConflictCheckerStats);
  }


  //#########################################################################
  //# Auxiliary Methods
  private void setConflictCheckerMarkings(final SICPropertyBuilder builder)
  {
    builder.setDefaultMarkings();
    final EventProxy defaultMark = builder.getMarkingProposition();
    final EventProxy preconditionMark = builder.getGeneralisedPrecondition();
    final ConflictChecker checker = getConflictChecker();
    checker.setMarkingProposition(defaultMark);
    checker.setGeneralisedPrecondition(preconditionMark);
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


  //#########################################################################
  //# Data Members
  private EventProxy mFailedAnswer;

  private int mPeakNumberOfNodes;
  private double mTotalNumberOfStates;
  private double mPeakNumberOfStates;
  private double mTotalNumberOfTransitions;
  private double mPeakNumberOfTransitions;
  private List<VerificationResult> mConflictCheckerStats;

}
