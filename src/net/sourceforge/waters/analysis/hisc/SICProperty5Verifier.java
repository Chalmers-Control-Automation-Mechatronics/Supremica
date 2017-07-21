//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# Copyright (C) 2004-2017 Robi Malik
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

package net.sourceforge.waters.analysis.hisc;

import java.util.List;

import net.sourceforge.waters.model.analysis.AnalysisException;
import net.sourceforge.waters.model.analysis.AnalysisResult;
import net.sourceforge.waters.model.analysis.VerificationResult;
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
