//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# Copyright (C) 2004-2023 Robi Malik
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

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.sourceforge.waters.analysis.tr.EventEncoding;
import net.sourceforge.waters.analysis.tr.ListBufferTransitionRelation;
import net.sourceforge.waters.analysis.tr.TRAutomatonProxy;
import net.sourceforge.waters.model.analysis.AnalysisException;
import net.sourceforge.waters.model.analysis.des.SafetyDiagnostics;
import net.sourceforge.waters.model.analysis.des.SafetyVerifier;
import net.sourceforge.waters.model.analysis.kindtranslator.KindTranslator;
import net.sourceforge.waters.model.des.AutomatonProxy;
import net.sourceforge.waters.model.des.EventProxy;
import net.sourceforge.waters.model.des.ProductDESProxy;
import net.sourceforge.waters.model.des.ProductDESProxyFactory;
import net.sourceforge.waters.model.des.SafetyCounterExampleProxy;
import net.sourceforge.waters.model.des.StateProxy;
import net.sourceforge.waters.model.des.TraceProxy;
import net.sourceforge.waters.model.des.TraceStepProxy;


/**
 * A Java implementation of a monolithic controllability or language
 * inclusion check algorithm, based on {@link ListBufferTransitionRelation}
 * as automaton representation.
 *
 * @author Robi Malik
 */

public abstract class TRMonolithicSafetyVerifier
  extends AbstractTRMonolithicModelVerifier
  implements SafetyVerifier
{

  //#########################################################################
  //# Constructors
  /**
   * Creates a new safety verifier.
   * @param  translator  The kind translator is used to remap component and
   *                     event kinds.
   * @param  diag        Diagnostics object to generate names and comments
   *                     for counterexamples.
   */
  public TRMonolithicSafetyVerifier(final KindTranslator translator,
                                    final SafetyDiagnostics diag)
  {
    this(null, translator, diag);
  }

  /**
   * Creates a new safety verifier to check a particular model.
   * @param  model       The model to be checked by this verifier.
   * @param  translator  The kind translator is used to remap component and
   *                     event kinds.
   * @param  diag        Diagnostics object to generate names and comments
   *                     for counterexamples.
   */
  public TRMonolithicSafetyVerifier(final ProductDESProxy model,
                                    final KindTranslator translator,
                                    final SafetyDiagnostics diag)
  {
    super(model, translator);
    mDiagnostics = diag;
  }


  //#########################################################################
  //# Interface net.sourceforge.waters.model.analysis.SafetyVerifier
  @Override
  public SafetyDiagnostics getDiagnostics()
  {
    return mDiagnostics;
  }

  @Override
  public SafetyCounterExampleProxy getCounterExample()
  {
    return (SafetyCounterExampleProxy) super.getCounterExample();
  }


  //#########################################################################
  //# Overrides for net.sourceforge.waters.analysis.monolithic.
  //# AbstractTRMonolithicModelAnalyzer
  @Override
  public boolean isSensitiveToControllability()
  {
    return true;
  }

  @Override
  protected void postProcessEventInfo(final List<EventInfo> eventInfoList)
    throws AnalysisException
  {
    boolean mayBeUncontrollable = false;
    for (final EventInfo info : eventInfoList) {
      if (info.canCauseUncontrollability()) {
        mayBeUncontrollable = true;
      } else {
        info.setControllable();
      }
    }
    if (mayBeUncontrollable) {
      super.postProcessEventInfo(eventInfoList);
    } else {
      setSatisfiedResult();
    }
  }

  @Override
  protected boolean handleUncontrollableState(final int event, final int spec)
    throws AnalysisException
  {
    final int state = getCurrentSource();
    final SafetyCounterExampleProxy counterExample =
      buildCounterExample(state, event, spec);
    setFailedResult(counterExample);
    return false;
  }


  //#########################################################################
  //# Counterexamples
  protected SafetyCounterExampleProxy buildCounterExample(final int target,
                                                          final int e,
                                                          final int spec)
    throws AnalysisException
  {
    final ProductDESProxyFactory factory = getFactory();
    final SafetyDiagnostics diag = getDiagnostics();
    final ProductDESProxy des = getModel();
    final List<TraceStepProxy> steps;
    final String comment;
    if (e < 0) {
      // failed property due to missing initial state
      final int numAut = getInputAutomata().length;
      final Map<AutomatonProxy,StateProxy> stateMap = new HashMap<>(numAut);
      for (int a = 0; a < numAut; a++) {
        final TRAutomatonProxy tr = getTRAutomaton(a);
        final ListBufferTransitionRelation rel = tr.getTransitionRelation();
        final int s = rel.getFirstInitialState();
        if (s >= 0) {
          final AutomatonProxy aut = getInputAutomaton(a);
          final StateProxy state = getInputState(a, s);
          stateMap.put(aut, state);
        }
      }
      final TraceStepProxy step = factory.createTraceStepProxy(null, stateMap);
      steps = Collections.singletonList(step);
      comment = null;
    } else {
      // failed property due to disabled event
      final EventInfo info = getEventInfo(e);
      final TraceStepProxy finalStep = info.buildFinalTraceStep(this, target);
      steps = buildTraceToBadState(target);
      steps.add(finalStep);
      final EventEncoding eventEnc = getOutputEventEncoding();
      final EventProxy event = eventEnc.getProperEvent(e);
      final AutomatonProxy aut = getInputAutomaton(spec);
      final StateTupleEncoding stateEnc = getStateTupleEncoding();
      final int[] encoded = new int[stateEnc.getNumberOfWords()];
      getStateSpace().getContents(target, encoded);
      final int s = stateEnc.get(encoded, spec);
      final StateProxy state = getInputState(spec, s);
      comment = diag.getTraceComment(des, event, aut, state);
    }
    final TraceProxy trace = factory.createTraceProxy(steps);
    final String name = diag.getTraceName(des);
    final Collection<AutomatonProxy> automata =
      Arrays.asList(getInputAutomata());
    return
      factory.createSafetyCounterExampleProxy(name, comment, null, des,
                                              automata, trace);
  }


  //#########################################################################
  //# Data Members
  private final SafetyDiagnostics mDiagnostics;

}
