//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.despot
//# CLASS:   SICProperty6Verifier
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.analysis.hisc;

import net.sourceforge.waters.model.analysis.AnalysisException;
import net.sourceforge.waters.model.analysis.ConflictChecker;
import net.sourceforge.waters.model.analysis.VerificationResult;
import net.sourceforge.waters.model.des.ConflictTraceProxy;
import net.sourceforge.waters.model.des.EventProxy;
import net.sourceforge.waters.model.des.ProductDESProxy;
import net.sourceforge.waters.model.des.ProductDESProxyFactory;


/**
 * A model verifier to check SIC Property VI.
 *
 * This wrapper can be used to check whether an HISC low-level model
 * satisfies Serial Interface Consistency (SIC) Property VI or
 * Low-Data Interface Consistency (LDIC) Property VI.
 *
 * The check is done by converting the verification problem to an
 * equivalent generalised nonblocking verification problem, and passing
 * that to a generalised conflict checker.
 *
 * @see SICPropertyBuilder
 * @see ConflictChecker
 *
 * @author Robi Malik
 */

public class SICProperty6Verifier extends AbstractSICConflictChecker
{

  //#########################################################################
  //# Constructors
  public SICProperty6Verifier(final ProductDESProxyFactory factory)
  {
    super(factory);
  }

  public SICProperty6Verifier(final ConflictChecker checker,
                              final ProductDESProxyFactory factory)
  {
    super(checker, factory);
  }

  public SICProperty6Verifier(final ConflictChecker checker,
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
      final EventProxy defaultMark = getUsedMarkingProposition();
      builder.setMarkingProposition(defaultMark);
      ProductDESProxy convertedModel = null;
      convertedModel = builder.createSIC6Model();
      final ConflictChecker checker = getConflictChecker();
      checker.setModel(convertedModel);
      checker.setMarkingProposition(defaultMark);
      final EventProxy preMark = builder.getGeneralisedPrecondition();
      checker.setPreconditionMarking(preMark);
      final VerificationResult result;
      try {
        checker.run();
      } finally {
        result = checker.getAnalysisResult();
        setAnalysisResult(result);
      }
      if (result.isSatisfied()) {
        return true;
      } else {
        final ConflictTraceProxy counterexample = checker.getCounterExample();
        final ConflictTraceProxy convertedTrace =
          builder.convertTraceToOriginalModel(counterexample, null);
        return setFailedResult(convertedTrace);
      }
    } finally {
      tearDown();
    }
  }

}
