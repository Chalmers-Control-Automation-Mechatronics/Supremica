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

package net.sourceforge.waters.analysis.comp552;

import java.util.Collection;
import java.util.List;

import net.sourceforge.waters.model.analysis.AnalysisException;
import net.sourceforge.waters.model.base.EventKind;
import net.sourceforge.waters.model.base.NamedProxy;
import net.sourceforge.waters.model.des.CounterExampleProxy;
import net.sourceforge.waters.model.des.EventProxy;
import net.sourceforge.waters.model.des.ProductDESProxy;
import net.sourceforge.waters.model.des.TraceProxy;


/**
 * The common base class for counterexample checking tools.
 * For details how to use this class, please see the subclasses
 * {@link ConflictCounterExampleChecker} and
 * {@link ControllabilityCounterExampleChecker}.<P>
 *
 * @author Robi Malik
 */

abstract class AbstractCounterExampleChecker
{

  //#########################################################################
  //# Constructors
  /**
   * Creates a new counterexample checker.
   * @param fullDiag Whether the diagnostic text should include the
   *                 name of the trace. The default is <CODE>true</CODE>.
   */
  public AbstractCounterExampleChecker(final boolean fullDiag)
  {
    mFullDiagnostics = fullDiag;
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
   * Checks whether the given trace is a counterexample to the property
   * this counterexample checker is concerned with.
   * @param  des       The product DES that was verified.
   * @param  counter   The counterexample to be checked.
   * @return <CODE>true</CODE> if the given counterexample demonstrates that
   *         the given product DES violates the property at hand,
   *         <CODE>false</CODE> otherwise.
   * @throws AnalysisException to indicate a problem while attempting to
   *                   verify the counterexample.
   */
  public boolean checkCounterExample(final ProductDESProxy des,
                                     final CounterExampleProxy counter)
    throws AnalysisException
  {
    if (counter == null) {
      reportMalformedCounterExample(counter, "is NULL", null);
      return false;
    }
    final TraceProxy trace = counter.getTraces().get(0);
    final List<EventProxy> traceEvents = trace.getEvents();
    final Collection<EventProxy> events = des.getEvents();
    int step = 0;
    for (final EventProxy event : traceEvents) {
      if (event == null) {
        reportMalformedCounterExample(counter, "contains NULL event", null, step);
        return false;
      } else if (event.getKind() == EventKind.PROPOSITION) {
        reportMalformedCounterExample(counter, "contains proposition", event, step);
        return false;
      } else if (!events.contains(event)) {
        reportMalformedCounterExample(counter, "contains unknown event", event, step);
        return false;
      }
      step++;
    }
    return true;
  }


  //#########################################################################
  //# Hooks
  abstract String getTraceLabel();


  //#########################################################################
  //# Auxiliary Methods
  void reportCorrectCounterExample()
  {
    mDiagnostics = null;
  }

  void reportMalformedCounterExample(final CounterExampleProxy trace,
                                     final String msg)
  {
    reportMalformedCounterExample(trace, msg, null, -1);
  }

  void reportMalformedCounterExample(final CounterExampleProxy trace,
                                     final String msg,
                                     final NamedProxy item)
  {
    startDiagnostics(trace);
    mDiagnostics.append(msg);
    if (item != null) {
      mDiagnostics.append(" '");
      mDiagnostics.append(item.getName());
      mDiagnostics.append('\'');
    }
    mDiagnostics.append('.');
  }

  void reportMalformedCounterExample(final CounterExampleProxy trace,
                                     final String msg,
                                     final NamedProxy item,
                                     final int step)
  {
    startDiagnostics(trace);
    mDiagnostics.append(msg);
    if (item != null) {
      mDiagnostics.append(" '");
      mDiagnostics.append(item.getName());
      mDiagnostics.append('\'');
    }
    if (step >= 0) {
      mDiagnostics.append(" in step ");
      mDiagnostics.append(step + 1);
    }
    if (mFullDiagnostics) {
      mDiagnostics.append('.');
    }
  }

  void reportCounterCounterExample(final String msg,
                                   final CounterExampleProxy counter)
  {
    mDiagnostics.append("\n");
    mDiagnostics.append(msg);
    final TraceProxy trace = counter.getTraces().get(0);
    final List<EventProxy> traceEvents = trace.getEvents();
    if (traceEvents.isEmpty()) {
      mDiagnostics.append("\n  <empty>");
    } else {
      mDiagnostics.append("\n  ");
      int limit = mDiagnostics.length() + 74;
      boolean first = true;
      for (final EventProxy event : traceEvents) {
        if (first) {
          first = false;
        } else {
          mDiagnostics.append(", ");
        }
        final String name = event == null ? "(null)" : event.getName();
        if (mDiagnostics.length() + name.length() > limit) {
          mDiagnostics.append("\n  ");
          limit = mDiagnostics.length() + 74;
        }
        mDiagnostics.append(name);
      }
    }
  }

  void startDiagnostics(final CounterExampleProxy counter)
  {
    mDiagnostics = new StringBuilder();
    if (mFullDiagnostics) {
      mDiagnostics.append(getTraceLabel());
      mDiagnostics.append(" error trace ");
      final String name = counter.getName();
      if (name != null) {
        mDiagnostics.append('\'');
        mDiagnostics.append(counter.getName());
        mDiagnostics.append("' ");
      }
    }
  }


  //#########################################################################
  //# Data Members
  private final boolean mFullDiagnostics;
  private StringBuilder mDiagnostics;

}
