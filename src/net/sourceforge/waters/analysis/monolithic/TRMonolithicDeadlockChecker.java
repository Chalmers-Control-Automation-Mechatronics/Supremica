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
import net.sourceforge.waters.model.analysis.des.AbstractDeadlockChecker;
import net.sourceforge.waters.model.analysis.des.DeadlockChecker;
import net.sourceforge.waters.model.analysis.kindtranslator.ConflictKindTranslator;
import net.sourceforge.waters.model.analysis.kindtranslator.KindTranslator;
import net.sourceforge.waters.model.des.AutomatonProxy;
import net.sourceforge.waters.model.des.ConflictCounterExampleProxy;
import net.sourceforge.waters.model.des.ConflictKind;
import net.sourceforge.waters.model.des.EventProxy;
import net.sourceforge.waters.model.des.ProductDESProxy;
import net.sourceforge.waters.model.des.ProductDESProxyFactory;
import net.sourceforge.waters.model.des.TraceProxy;
import net.sourceforge.waters.model.des.TraceStepProxy;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


/**
 * A Java implementation of the monolithic deadlock check algorithm,
 * based on {@link ListBufferTransitionRelation} as automaton representation.
 *
 * @author Hani al-Bahri
 */

public class TRMonolithicDeadlockChecker
  extends TRAbstractModelVerifier
  implements DeadlockChecker
{

  //#########################################################################
  //# Constructors
  public TRMonolithicDeadlockChecker()
  {
    super(ConflictKindTranslator.getInstanceControllable());
  }

  public TRMonolithicDeadlockChecker(final ProductDESProxy model)
  {
    super(model, ConflictKindTranslator.getInstanceControllable());
  }


  //#########################################################################
  //# Interface net.sourceforge.waters.model.analysis.des.DeadlockChecker
  @Override
  public ConflictCounterExampleProxy getCounterExample()
  {
    return (ConflictCounterExampleProxy) super.getCounterExample();
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
      if (!isTriviallyDeadlockFree()) {
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
  protected void expandState(final int[] encoded, final int[] decoded)
    throws AnalysisException
  {
    boolean isDeadlock = true;
    for (final EventInfo event : getEventInfo()) {
      if (event.findDisabling(decoded) == null) {
        createSuccessorStates(encoded, decoded, event);
        isDeadlock = false;
      }
    }
    if (isDeadlock) {
      final int target = getCurrentSource();
      final ConflictCounterExampleProxy counterexample =
        buildCounterExample(target);
      setFailedResult(counterexample);
    }
  }


  //#########################################################################
  //# Auxiliary Methods
  /**
   * Checks whether the model can be shown to be deadlock-free without
   * state space exploration. There can obviously be no deadlock if there
   * is an event unused in any component, or an event enabled in all states
   * of all components where it is used. In such a case, the method sets a
   * true verification result and returns <CODE>true</CODE>, otherwise it
   * returns <CODE>false</CODE>.
   */
  private boolean isTriviallyDeadlockFree()
  {
    final KindTranslator translator = getKindTranslator();
    final ProductDESProxy des = getModel();
    final EventEncoding enc = getOutputEventEncoding();
    for (final EventProxy event : des.getEvents()) {
      switch (translator.getEventKind(event)) {
      case CONTROLLABLE:
      case UNCONTROLLABLE:
        if (enc.getEventCode(event) < 0) {
          return setSatisfiedResult();
        }
        // fall through ...
      default:
        break;
      }
    }
    for (final EventInfo info : getEventInfo()) {
      if (info.isGloballyAlwaysEnabled()) {
        return setSatisfiedResult();
      }
    }
    return false;
  }


  //#########################################################################
  //# Counterexamples
  private ConflictCounterExampleProxy buildCounterExample(final int target)
    throws AnalysisException
  {
    final ProductDESProxyFactory factory = getFactory();
    final String traceName = getTraceName();
    final ProductDESProxy des = getModel();
    final Collection<AutomatonProxy> automata = des.getAutomata();
    final List<TraceStepProxy> steps = buildTraceToBadState(target);
    final TraceProxy trace = factory.createTraceProxy(steps);
    return
      factory.createConflictCounterExampleProxy(traceName, null, null, des,
                                                automata, trace,
                                                ConflictKind.DEADLOCK);
  }

  /**
   * Gets a name that can be used for a counterexample for the current model.
   */
  private String getTraceName()
  {
    final ProductDESProxy model = getModel();
    return AbstractDeadlockChecker.getTraceName(model);
  }

}
