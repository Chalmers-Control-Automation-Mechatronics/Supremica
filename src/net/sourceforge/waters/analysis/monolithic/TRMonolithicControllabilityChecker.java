//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# Copyright (C) 2004-2021 Robi Malik
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

package net.sourceforge.waters.analysis.monolithic;

import java.util.Collection;
import java.util.List;

import net.sourceforge.waters.analysis.tr.EventEncoding;
import net.sourceforge.waters.analysis.tr.ListBufferTransitionRelation;
import net.sourceforge.waters.model.analysis.AnalysisException;
import net.sourceforge.waters.model.analysis.OverflowException;
import net.sourceforge.waters.model.analysis.VerificationResult;
import net.sourceforge.waters.model.analysis.des.ControllabilityChecker;
import net.sourceforge.waters.model.analysis.des.ControllabilityDiagnostics;
import net.sourceforge.waters.model.analysis.des.SafetyDiagnostics;
import net.sourceforge.waters.model.analysis.kindtranslator.ControllabilityKindTranslator;
import net.sourceforge.waters.model.des.AutomatonProxy;
import net.sourceforge.waters.model.des.EventProxy;
import net.sourceforge.waters.model.des.ProductDESProxy;
import net.sourceforge.waters.model.des.ProductDESProxyFactory;
import net.sourceforge.waters.model.des.SafetyCounterExampleProxy;
import net.sourceforge.waters.model.des.StateProxy;
import net.sourceforge.waters.model.des.TraceProxy;
import net.sourceforge.waters.model.des.TraceStepProxy;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


/**
 * A Java implementation of the monolithic deadlock check algorithm,
 * based on {@link ListBufferTransitionRelation} as automaton representation.
 *
 * @author Robi Malik
 */

public class TRMonolithicControllabilityChecker
  extends TRAbstractModelVerifier
  implements ControllabilityChecker
{

  //#########################################################################
  //# Constructors
  public TRMonolithicControllabilityChecker()
  {
    super(ControllabilityKindTranslator.getInstance());
  }

  public TRMonolithicControllabilityChecker(final ProductDESProxy model)
  {
    super(model, ControllabilityKindTranslator.getInstance());
  }


  //#########################################################################
  //# Interface
  //# net.sourceforge.waters.model.analysis.des.SafetyVerifier
  @Override
  public SafetyCounterExampleProxy getCounterExample()
  {
    return (SafetyCounterExampleProxy) super.getCounterExample();
  }

  @Override
  public SafetyDiagnostics getDiagnostics()
  {
    return ControllabilityDiagnostics.getInstance();
  }


  //#########################################################################
  //# Overrides for net.sourceforge.waters.model.analysis.monolithic.
  //# AbstractTRMonolithicModelAnalyzer
  @Override
  public boolean isSensitiveToControllability()
  {
    return true;
  }


  //#########################################################################
  //# Invocation
  @Override
  public boolean run()
    throws AnalysisException
  {
    try {
      setUp();
      final VerificationResult result = getAnalysisResult();
      if (!isTriviallyControllable()) {
        exploreStateSpace();
        if (!result.isFinished()) {
          result.setSatisfied(true);
        }
      }
      return result.isSatisfied();
    } catch (final AnalysisException exception) {
      throw setExceptionResult(exception);
    } catch (final OutOfMemoryError error) {
      tearDown();
      final Logger logger = LogManager.getLogger();
      logger.debug("<out of memory>");
      final OverflowException exception = new OverflowException(error);
      throw setExceptionResult(exception);
    } catch (final StackOverflowError error) {
      final OverflowException exception = new OverflowException(error);
      throw setExceptionResult(exception);
    } finally {
      tearDown();
    }
  }

  @Override
  protected void tearDown()
  {
    setStateCallback(null);
    super.tearDown();
  }


  @Override
  protected boolean handleUncontrollableState(final int event, final int spec)
    throws AnalysisException
  {
    final int state = getCurrentSource();
    final SafetyCounterExampleProxy counterexample =
      buildCounterExample(state, event, spec);
    setFailedResult(counterexample);
    return false;
  }


  //#########################################################################
  //# Auxiliary Methods
  /**
   * Checks whether the model can be shown to be controllable without
   * state space exploration. For the model to be not controllable, it must
   * include an uncontrollable event that is disabled by at least one
   * specification. If it can be determined from the event information that
   * there is no such event, the method sets a true verification result and
   * returns <CODE>true</CODE>, otherwise it returns <CODE>false</CODE>.
   */
  private boolean isTriviallyControllable()
  {
    for (final EventInfo info : getEventInfo()) {
      if (info.canCauseUncontrollability()) {
        return false;
      }
    }
    return setSatisfiedResult();
  }


  //#########################################################################
  //# Counterexamples
  private SafetyCounterExampleProxy buildCounterExample(final int target,
                                                        final int e,
                                                        final int spec)
    throws AnalysisException
  {
    final ProductDESProxyFactory factory = getFactory();
    TraceStepProxy finalStep = null;
    for (final EventInfo info : getEventInfo()) {
      if (info.getOutputCode() == e) {
        finalStep = info.buildFinalTraceStep(this, target);
        break;
      }
    }
    final List<TraceStepProxy> steps = buildTraceToBadState(target);
    steps.add(finalStep);
    final TraceProxy trace = factory.createTraceProxy(steps);
    final EventEncoding eventEnc = getOutputEventEncoding();
    final EventProxy event = eventEnc.getProperEvent(e);
    final ProductDESProxy des = getModel();
    final Collection<AutomatonProxy> automata = des.getAutomata();
    final AutomatonProxy aut = getInputAutomaton(spec);
    final StateTupleEncoding stateEnc = getStateTupleEncoding();
    final int[] encoded = new int[stateEnc.getNumberOfWords()];
    getStateSpace().getContents(target, encoded);
    final int s = stateEnc.get(encoded, spec);
    final StateProxy state = getInputState(spec, s);
    final SafetyDiagnostics diag = getDiagnostics();
    final String name = diag.getTraceName(des);
    final String comment = diag.getTraceComment(des, event, aut, state);
    return
      factory.createSafetyCounterExampleProxy(name, comment, null, des,
                                              automata, trace);
  }

}
