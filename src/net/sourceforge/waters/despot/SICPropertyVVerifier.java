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

import net.sourceforge.waters.model.analysis.AbstractModelVerifier;
import net.sourceforge.waters.model.analysis.AnalysisException;
import net.sourceforge.waters.model.analysis.ConflictChecker;
import net.sourceforge.waters.model.analysis.ConflictKindTranslator;
import net.sourceforge.waters.model.des.ConflictTraceProxy;
import net.sourceforge.waters.model.des.EventProxy;
import net.sourceforge.waters.model.des.ProductDESProxy;
import net.sourceforge.waters.model.des.ProductDESProxyFactory;


public class SICPropertyVVerifier extends AbstractModelVerifier
{

  //#########################################################################
  //# Constructors
  public SICPropertyVVerifier(final ConflictChecker checker,
                              final ProductDESProxyFactory factory)
  {
    this(checker, null, factory);
  }

  public SICPropertyVVerifier(final ConflictChecker checker,
                              final ProductDESProxy model,
                              final ProductDESProxyFactory factory)
  {
    super(model, factory, ConflictKindTranslator.getInstance());
    mChecker = checker;
  }


  //#########################################################################
  //# Invocation
  public boolean run()
    throws AnalysisException
  {
    final ProductDESProxy model = getModel();
    final SICPropertyVBuilder builder =
        new SICPropertyVBuilder(model, getFactory());
    final List<EventProxy> answers =
        (List<EventProxy>) builder.getAnswerEvents();
    setConflictCheckerMarkings(builder);
    ProductDESProxy convertedModel = null;
    boolean result = true;
    for (final EventProxy answer : answers) {
      convertedModel = builder.createModelForAnswer(answer);
      mChecker.setModel(convertedModel);
      result &= mChecker.run();
      if (!result) {
        final ConflictTraceProxy counterexample = mChecker.getCounterExample();
        final ConflictTraceProxy convertedTrace =
            builder.convertTraceToOriginalModel(counterexample, answer);
        mFailedAnswer = answer;
        return setFailedResult(convertedTrace);
      }
    }
    return setSatisfiedResult();
  }

  public EventProxy getFailedAnswer()
  {
    return mFailedAnswer;
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


  //#########################################################################
  //# Data Members
  private final ConflictChecker mChecker;

  private EventProxy mFailedAnswer;

}
