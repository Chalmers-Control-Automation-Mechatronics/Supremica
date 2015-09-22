//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# Copyright (C) 2004-2015 Robi Malik
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

package net.sourceforge.waters.analysis.trcomp;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.sourceforge.waters.analysis.tr.EventEncoding;
import net.sourceforge.waters.analysis.tr.EventStatus;
import net.sourceforge.waters.analysis.tr.ListBufferTransitionRelation;
import net.sourceforge.waters.analysis.tr.TRAutomatonProxy;
import net.sourceforge.waters.analysis.tr.TransitionIterator;
import net.sourceforge.waters.model.analysis.AnalysisException;
import net.sourceforge.waters.model.des.AutomatonProxy;
import net.sourceforge.waters.model.des.EventProxy;
import net.sourceforge.waters.model.des.ProductDESProxy;
import net.sourceforge.waters.model.des.SafetyTraceProxy;
import net.sourceforge.waters.model.des.StateProxy;
import net.sourceforge.waters.model.des.TraceProxy;
import net.sourceforge.waters.model.des.TraceStepProxy;

import org.apache.log4j.Logger;


/**
 * An abstraction step representing a subsystem that has failed monolithic
 * verification.
 *
 * @author Robi Malik
 */

class TRAbstractionStepMonolithic
  extends TRAbstractionStep
{

  //#########################################################################
  //# Constructor
  TRAbstractionStepMonolithic(final TRTraceProxy extension)
  {
    this("<monolithic result>", extension);
  }

  TRAbstractionStepMonolithic(final String name, final TRTraceProxy extension)
  {
    super(name);
    mPredecessors = extension.getCoveredAbstractionSteps();
    mTraceExtension = extension;
  }


  //#########################################################################
  //# Interface net.sourceforge.waters.analysis.trcomp.TRAbstractionStep
  @Override
  public Collection<TRAbstractionStep> getPredecessors()
  {
    return mPredecessors;
  }

  @Override
  public TRAutomatonProxy createOutputAutomaton(final int preferredConfig)
  {
    return null;
  }

  @Override
  public void expandTrace(final TRTraceProxy trace,
                          final AbstractTRCompositionalAnalyzer analyzer)
  {
    trace.widenAndAppend(mTraceExtension);
  }


  //#########################################################################
  //# Debugging
  static TRTraceProxy createTraceExtension
    (final TraceProxy trace,
     final Collection<TRAbstractionStep> preds,
     final AbstractTRCompositionalAnalyzer analyzer)
    throws AnalysisException
  {
    final ProductDESProxy des = trace.getProductDES();
    final List<EventProxy> events = trace.getEvents();
    final int numSteps = events.size() + 1;
    final TRTraceProxy extension = new TRConflictTraceProxy(des, events);

    final Map<String,TRAutomatonProxy> traceAutomataMap =
      new HashMap<>(preds.size());
    for (final AutomatonProxy aut : trace.getAutomata()) {
      final String name = aut.getName();
      final TRAutomatonProxy tr = (TRAutomatonProxy) aut;
      traceAutomataMap.put(name, tr);
    }

    for (final TRAbstractionStep pred : preds) {
      analyzer.checkAbort();
      final String name = pred.getName();
      final TRAutomatonProxy aut = traceAutomataMap.get(name);
      final EventEncoding enc = aut.getEventEncoding();
      final ListBufferTransitionRelation rel = aut.getTransitionRelation();
      rel.reconfigure(ListBufferTransitionRelation.CONFIG_SUCCESSORS);
      final TransitionIterator iter = rel.createSuccessorsReadOnlyIterator();
      final int[] states = new int[numSteps];
      int stepIndex = 0;
      int current = -1;
      for (final TraceStepProxy step : trace.getTraceSteps()) {
        final StateProxy state = step.getStateMap().get(aut);
        if (state != null) {
          current = aut.getStateIndex(state);
        } else if (stepIndex == 0) {
          current = rel.getFirstInitialState();
          assert current >= 0 || trace instanceof SafetyTraceProxy :
            "No initial state found in trace for automaton " + name + "!";
        } else if (current >= 0){
          final EventProxy event = step.getEvent();
          final int e = enc.getEventCode(event);
          if (e >= 0) {
            final byte status = enc.getProperEventStatus(e);
            if (EventStatus.isUsedEvent(status)) {
              iter.reset(current, e);
              if (iter.advance()) {
                current = iter.getCurrentTargetState();
                assert !iter.advance() :
                  "Nondeterministic successor states for trace found " +
                  "in automaton " + name + "!";
              } else {
                current = -1;
                assert trace instanceof SafetyTraceProxy :
                  "No successor state found in trace for automaton " +
                  name + "!";
              }
            }
          }
        }
        states[stepIndex++] = current;
      }
      extension.addAutomaton(pred, states);
    }

    return extension;
  }


  //#########################################################################
  //# Debugging
  @Override
  public void reportExpansion()
  {
    final Logger logger = getLogger();
    logger.debug("Converting monolithic trace ...");
  }


  //#########################################################################
  //# Data Members
  private final Collection<TRAbstractionStep> mPredecessors;
  private final TRTraceProxy mTraceExtension;

}








