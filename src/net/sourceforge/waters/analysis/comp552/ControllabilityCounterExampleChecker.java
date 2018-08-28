//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# Copyright (C) 2004-2018 Robi Malik
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

package net.sourceforge.waters.analysis.comp552;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import net.sourceforge.waters.model.analysis.AnalysisException;
import net.sourceforge.waters.model.des.AutomatonProxy;
import net.sourceforge.waters.model.des.AutomatonTools;
import net.sourceforge.waters.model.des.CounterExampleProxy;
import net.sourceforge.waters.model.des.EventProxy;
import net.sourceforge.waters.model.des.ProductDESProxy;
import net.sourceforge.waters.model.des.SafetyCounterExampleProxy;
import net.sourceforge.waters.model.des.StateProxy;
import net.sourceforge.waters.model.des.TraceProxy;
import net.sourceforge.waters.xsd.base.EventKind;


/**
 * <P>A tool to check whether a controllability counterexample is a correct
 * counterexample to show that a given product DES is not controllable.</P>
 *
 * <P>To use this class, an instance must be obtained from the constructor.
 * Afterwards, {@link #checkCounterExample(ProductDESProxy,CounterExampleProxy)
 * checkCounterExample()} can be called repeatedly. If a check fails,
 * {@link #getDiagnostics()} can be called to retrieve an explanation.</P>
 *
 * <P>
 * <CODE>ControllabilityCounterExampleChecker checker =
 *   new {@link #ControllabilityCounterExampleChecker()
 *   ControllabilityCounterExampleChecker}();</CODE><BR>
 * <CODE>if (checker.{@link #checkCounterExample(ProductDESProxy,CounterExampleProxy)
 *   checkCounterExample}(</CODE><I>des</I><CODE>, </CODE><I>trace</I><CODE>))
 *   {</CODE><BR>
 * <CODE>&nbsp;&nbsp;System.out.println(&quot;OK&quot;);</CODE><BR>
 * <CODE>} else {</CODE><BR>
 * <CODE>&nbsp;&nbsp;String msg = checker.{@link #getDiagnostics()
 *   getDiagnostics}();</CODE><BR>
 * <CODE>&nbsp;&nbsp;System.out.println(msg);</CODE><BR>
 * <CODE>}</CODE>
 * </P>
 *
 * @author Robi Malik
 */

public class ControllabilityCounterExampleChecker
  extends AbstractCounterExampleChecker
{

  //#########################################################################
  //# Constructors
  /**
   * Creates a new controllability counterexample checker.
   */
  public ControllabilityCounterExampleChecker()
  {
    this(true);
  }

  /**
   * Creates a new controllability counterexample checker.
   * @param fullDiag Whether the diagnostic text should include the
   *                 name of the trace. The default is <CODE>true</CODE>.
   */
  public ControllabilityCounterExampleChecker(final boolean fullDiag)
  {
    super(fullDiag);
  }


  //#########################################################################
  //# Configuration
  /**
   * Returns whether the counterexample checker computes the end state
   * for traces that are accepted by all automata.
   * @see #getEndState()
   */
  boolean getEndStateEnabled()
  {
    return mEndStateEnabled;
  }

  /**
   * Sets whether the counterexample checker computes the end state
   * for traces that are accepted by all automata. This option is disabled
   * by default.
   * @see #getEndState()
   */
  void setEndStateEnabled(final boolean enabled)
  {
    mEndStateEnabled = enabled;
  }


  //#########################################################################
  //# Invocation
  /**
   * Checks a controllability error trace.
   * @param  des       The product DES that was verified.
   * @param  counter   The counterexample to be checked.
   * @return <CODE>true</CODE> if the given counterexample demonstrates that
   *         the given product DES is not controllable, <CODE>false</CODE>
   *         otherwise.
   * @throws AnalysisException to indicate a problem while attempting to
   *                   verify the counterexample.
   */
  @Override
  public boolean checkCounterExample(final ProductDESProxy des,
                                     final CounterExampleProxy counter)
    throws AnalysisException
  {
    if (!super.checkCounterExample(des, counter)) {
      return false;
    } else if (!(counter instanceof SafetyCounterExampleProxy)) {
      reportMalformedCounterExample(counter, "is not a SafetyCounterExampleProxy");
      return false;
    }
    if (mEndStateEnabled) {
      final int numAutomata = des.getAutomata().size();
      mEndStateMap = new HashMap<>(numAutomata);
    }
    final SafetyCounterExampleProxy safetyCounter =
      (SafetyCounterExampleProxy) counter;
    final TraceProxy trace = safetyCounter.getTrace();
    final List<EventProxy> traceEvents = trace.getEvents();
    if (traceEvents.isEmpty()) {
      reportMalformedCounterExample(counter, "does not have any event");
      findEndState(des, safetyCounter);
      return false;
    }
    final int numSteps = traceEvents.size();
    final EventProxy lastEvent = traceEvents.get(numSteps - 1);
    if (lastEvent.getKind() == EventKind.CONTROLLABLE) {
      reportMalformedCounterExample(counter, "ends with controllable event",
                                    lastEvent, -1);
      findEndState(des, safetyCounter);
      return false;
    }
    boolean gotRejectingSpec = false;
    for (final AutomatonProxy aut : des.getAutomata()) {
      final int steps = checkCounterExample(aut, safetyCounter);
      switch (aut.getKind()) {
      case PLANT:
        if (steps < numSteps) {
          reportMalformedCounterExample
            (safetyCounter, "is rejected by plant", aut, steps);
          return false;
        }
        break;
      case SPEC:
        if (steps < numSteps - 1) {
          reportMalformedCounterExample
            (safetyCounter, "is rejected too early by spec", aut, steps);
          return false;
        } else if (steps == numSteps - 1) {
          gotRejectingSpec = true;
        }
        break;
      default:
        break;
      }
    }
    if (!gotRejectingSpec) {
      reportMalformedCounterExample
        (safetyCounter, "is accepted by all specifications");
      return false;
    }
    return true;
  }

  /**
   * Returns the state tuple reached after accepting the complete trace.
   * If the last call to {@link #checkCounterExample(ProductDESProxy, CounterExampleProxy)
   * checkCounterExample()} has found the counterexample to be accepted by
   * all automata in the model (in which case it is not a correct
   * controllability counterexample), this method can be used to return the
   * state tuple reached at the end of the trace.
   * @return  A map that associated each automaton ({@link AutomatonProxy})
   *          in the model with the state ({@link StateProxy}) reached at
   *          the end of the trace, or <CODE>null</CODE>.
   *          If the counterexample is not fully accepted by all automata,
   *          or if end states are configured to be disabled,
   *          then this method returns <CODE>null</CODE>.
   * @see #setEndStateEnabled(boolean) setEndStateEnabled()
   */
  Map<AutomatonProxy,StateProxy> getEndState()
  {
    return mEndStateMap;
  }


  //#########################################################################
  //# Hooks
  @Override
  String getTraceLabel()
  {
    return "Controllability";
  }


  //#########################################################################
  //# Auxiliary Methods
  private void findEndState(final ProductDESProxy des,
                            final SafetyCounterExampleProxy counter)
  {
    if (mEndStateMap != null) {
      final Iterator<AutomatonProxy> iter = des.getAutomata().iterator();
      while (iter.hasNext() && mEndStateMap != null) {
        final AutomatonProxy aut = iter.next();
        checkCounterExample(aut, counter);
      }
    }
  }

  private int checkCounterExample(final AutomatonProxy aut,
                                  final SafetyCounterExampleProxy counter)
  {
    StateProxy current = AutomatonTools.getFirstInitialState(aut);
    if (current == null) {
      mEndStateMap = null;
      return -1;
    }
    int steps = 0;
    final Collection<EventProxy> events = aut.getEvents();
    final TraceProxy trace = counter.getTrace();
    final List<EventProxy> traceEvents = trace.getEvents();
    for (final EventProxy event : traceEvents) {
      if (events.contains(event) && event.getKind() != EventKind.PROPOSITION) {
        current = AutomatonTools.getFirstSuccessorState(aut, current, event);
        if (current == null) {
          mEndStateMap = null;
          return steps;
        }
      }
      steps++;
    }
    if (mEndStateMap != null) {
      mEndStateMap.put(aut, current);
    }
    return steps;
  }


  //#########################################################################
  //# Data Members
  private boolean mEndStateEnabled = false;
  private Map<AutomatonProxy,StateProxy> mEndStateMap = null;

}
