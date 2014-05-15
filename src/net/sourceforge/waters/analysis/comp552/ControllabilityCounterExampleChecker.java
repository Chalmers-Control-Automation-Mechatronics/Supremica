//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.analysis.comp552
//# CLASS:   ControllabilityCounterExampleChecker
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.analysis.comp552;

import java.util.Collection;
import java.util.List;

import net.sourceforge.waters.model.analysis.AnalysisException;
import net.sourceforge.waters.model.base.NamedProxy;
import net.sourceforge.waters.model.des.AutomatonProxy;
import net.sourceforge.waters.model.des.EventProxy;
import net.sourceforge.waters.model.des.ProductDESProxy;
import net.sourceforge.waters.model.des.ProductDESProxyFactory;
import net.sourceforge.waters.model.des.SafetyTraceProxy;
import net.sourceforge.waters.model.des.StateProxy;
import net.sourceforge.waters.model.des.TransitionProxy;
import net.sourceforge.waters.xsd.base.EventKind;


/**
 * <P>A tool to check whether a controllability error trace is a correct
 * counterexample to show that a given product DES is not controllable.</P>
 *
 * <P>To use this class, it must be initialised with a
 * {@link ProductDESProxyFactory}. Afterwards, {@link
 * #checkCounterExample(ProductDESProxy,SafetyTraceProxy)
 * checkCounterExample()} can be called repeatedly. If a check fails,
 * {@link #getDiagnostics()} can be called to retrieve an explanation.</P>
 *
 * <P>
 * <CODE>ControllabilityCounterExampleChecker checker =
 *   new {@link #ControllabilityCounterExampleChecker()
 *   ControllabilityCounterExampleChecker}();</CODE><BR>
 * <CODE>if (checker.{@link
 *   #checkCounterExample(ProductDESProxy,SafetyTraceProxy)
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
{

  //#########################################################################
  //# Constructors
  /**
   * Creates a new controllability counterexample checker.
   */
  public ControllabilityCounterExampleChecker()
  {
  }


  //#########################################################################
  //# Simple Access
  /**
   * Retrieves a diagnostic message that explains why the last counterexample
   * check is not correct controllability error trace.
   * @return  Descriptive string or <CODE>null</CODE> if the last check
   *          has passed.
   */
  public String getDiagnostics()
  {
    if (mDiagnostics == null) {
      return null;
    } else {
      return mDiagnostics.toString();
    }
  }


  //#########################################################################
  //# Invocation
  /**
   * Checks a controllability error trace.
   * @param  des       The product DES that was verified.
   * @param  trace     The counterexample to be checked.
   * @return <CODE>true</CODE> if the given counterexample demonstrates that
   *         the given product DES is not controllable, <CODE>false</CODE>
   *         otherwise.
   * @throws AnalysisException to indicate a problem while attempting to
   *                   verify the counterexample.
   */
  public boolean checkCounterExample(final ProductDESProxy des,
                                     final SafetyTraceProxy trace)
  throws AnalysisException
  {
    if (trace == null) {
      reportMalformedCounterExample(trace, "is NULL", null);
      return false;
    }
    final List<EventProxy> traceEvents = trace.getEvents();
    final Collection<EventProxy> events = des.getEvents();
    if (traceEvents.isEmpty()) {
      reportMalformedCounterExample(trace, "does not have any event", null);
    }
    for (final EventProxy event : traceEvents) {
      if (event == null) {
        reportMalformedCounterExample(trace, "contains NULL event", null);
        return false;
      } else if (event.getKind() == EventKind.PROPOSITION) {
        reportMalformedCounterExample(trace, "contains proposition", event);
        return false;
      } else if (!events.contains(event)) {
        reportMalformedCounterExample(trace, "contains unknown event", event);
        return false;
      }
    }
    final int numSteps = traceEvents.size();
    final EventProxy lastEvent = traceEvents.get(numSteps - 1);
    if (lastEvent.getKind() == EventKind.CONTROLLABLE) {
      reportMalformedCounterExample(trace, "ends with controllable event",
                                    lastEvent);
    }
    boolean gotRejectingSpec = false;
    for (final AutomatonProxy aut : des.getAutomata()) {
      final int steps = checkCounterExample(aut, trace);
      switch (aut.getKind()) {
      case PLANT:
        if (steps < numSteps) {
          reportMalformedCounterExample
            (trace, "is not accepted by plant", aut);
          return false;
        }
        break;
      case SPEC:
        if (steps < numSteps - 1) {
          reportMalformedCounterExample
            (trace,
             "is rejected too early (in step " + (steps+1) + ") by spec",
             aut);
          return false;
        } else {
          gotRejectingSpec = true;
        }
        break;
      default:
        break;
      }
    }
    if (!gotRejectingSpec) {
      reportMalformedCounterExample
        (trace, "is accepted by all specifications", null);
      return false;
    }
    return true;
  }


  //#########################################################################
  //# Auxiliary Methods
  private int checkCounterExample(final AutomatonProxy aut,
                                  final SafetyTraceProxy trace)
  {
    final Collection<EventProxy> events = aut.getEvents();
    final Collection<StateProxy> states = aut.getStates();
    final Collection<TransitionProxy> transitions = aut.getTransitions();
    StateProxy current = null;
    for (final StateProxy state : states) {
      if (state.isInitial()) {
        current = state;
        break;
      }
    }
    if (current == null) {
      return -1;
    }
    int steps = 0;
    final List<EventProxy> traceEvents = trace.getEvents();
    for (final EventProxy event : traceEvents) {
      if (events.contains(event) && event.getKind() != EventKind.PROPOSITION) {
        boolean found = false;
        for (final TransitionProxy trans : transitions) {
          if (trans.getSource() == current && trans.getEvent() == event) {
            current = trans.getTarget();
            found = true;
            break;
          }
        }
        if (!found) {
          return steps;
        }
        steps++;
      }
    }
    return steps;
  }

  private void reportMalformedCounterExample(final SafetyTraceProxy trace,
                                             final String msg,
                                             final NamedProxy item)
  {
    mDiagnostics = new StringBuilder();
    mDiagnostics.append("Controllability error trace ");
    final String name = trace.getName();
    if (name != null) {
      mDiagnostics.append('\'');
      mDiagnostics.append(trace.getName());
      mDiagnostics.append("' ");
    }
    mDiagnostics.append(msg);
    if (item != null) {
      mDiagnostics.append(" '");
      mDiagnostics.append(item.getName());
      mDiagnostics.append('\'');
    }
    mDiagnostics.append('.');
  }


  //#########################################################################
  //# Data Members
  private StringBuilder mDiagnostics;

}
