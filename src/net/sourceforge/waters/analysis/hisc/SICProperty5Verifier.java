//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters Analysis
//# PACKAGE: net.sourceforge.waters.despot
//# CLASS:   SICProperty5Verifier
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.analysis.hisc;

import java.util.List;

import net.sourceforge.waters.model.analysis.AnalysisException;
import net.sourceforge.waters.model.analysis.VerificationResult;
import net.sourceforge.waters.model.analysis.des.AnalysisResult;
import net.sourceforge.waters.model.analysis.des.ConflictChecker;
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
      setConflictCheckerMarkings(builder);
      final ConflictChecker checker = getConflictChecker();
      ProductDESProxy convertedModel = null;
      for (final EventProxy answer : answers) {
        checkAbort();
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
    } catch (final AnalysisException exception) {
      final VerificationResult result = getAnalysisResult();
      result.setException(exception);
      throw exception;
    } finally {
      tearDown();
    }
  }

  public EventProxy getFailedAnswer()
  {
    return mFailedAnswer;
  }


  //#########################################################################
  //# Overrides for net.sourceforge.waters.model.analysis.AbstractModelAnalyser
  @Override
  protected void setUp()
  throws AnalysisException
  {
    super.setUp();
    mFirstResult = true;
    mFailedAnswer = null;
  }


  //#########################################################################
  //# Auxiliary Methods
  private void setConflictCheckerMarkings(final SICPropertyBuilder builder)
  {
    builder.setOutputMarkings();
    final EventProxy defaultMark = builder.getOutputMarking();
    final EventProxy preconditionMark = builder.getGeneralisedPrecondition();
    final ConflictChecker checker = getConflictChecker();
    checker.setConfiguredDefaultMarking(defaultMark);
    checker.setConfiguredPreconditionMarking(preconditionMark);
  }

  private void recordStatistics(final AnalysisResult result)
  {
     if (mFirstResult) {
       setAnalysisResult(result);
       mFirstResult = false;
     } else {
       final AnalysisResult present = getAnalysisResult();
       final int numaut1 = present.getTotalNumberOfAutomata();
       final int numaut2 = result.getTotalNumberOfAutomata();
       final int numaut = Math.max(numaut1, numaut2);
       present.merge(result);
       present.setNumberOfAutomata(numaut);
     }
  }


  //#########################################################################
  //# Data Members
  private EventProxy mFailedAnswer;
  private boolean mFirstResult;

}
