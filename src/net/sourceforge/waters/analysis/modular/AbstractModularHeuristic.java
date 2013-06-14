//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters Analysis
//# PACKAGE: net.sourceforge.waters.analysis.modular
//# CLASS:   AbstractModularHeuristic
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.analysis.modular;

import gnu.trove.set.hash.THashSet;

import java.net.URI;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.sourceforge.waters.analysis.abstraction.TraceFinder;
import net.sourceforge.waters.model.analysis.KindTranslator;
import net.sourceforge.waters.model.des.AutomatonProxy;
import net.sourceforge.waters.model.des.EventProxy;
import net.sourceforge.waters.model.des.ProductDESProxy;
import net.sourceforge.waters.model.des.ProductDESProxyFactory;
import net.sourceforge.waters.model.des.SafetyTraceProxy;
import net.sourceforge.waters.model.des.StateProxy;
import net.sourceforge.waters.model.des.TraceProxy;
import net.sourceforge.waters.model.des.TraceStepProxy;
import net.sourceforge.waters.xsd.base.ComponentKind;
import net.sourceforge.waters.xsd.base.EventKind;


/**
 * A general-purpose implementation of the {@link ModularHeuristic}
 * interface. This class merely contains several useful methods to help
 * implementing the various heuristics. The actual heuristic procedure
 * is implemented in each subclass using the tools provided here.
 *
 * @author Simon Ware
 */

abstract class AbstractModularHeuristic
  implements ModularHeuristic
{

  //#########################################################################
  //# Constructor
  AbstractModularHeuristic(final KindTranslator translator)
  {
    mKindTranslator = translator;
    mTraceFinders = new HashMap<AutomatonProxy,TraceFinder>();
  }


  //#########################################################################
  //# Simple Access
  KindTranslator getKindTranslator()
  {
    return mKindTranslator;
  }


  //#########################################################################
  //# Interface net.sourceforge.waters.analysis.modular.ModularHeuristic
  @Override
  public String getName()
  {
    final String fullname = getClass().getName();
    final int dotpos = fullname.lastIndexOf('.');
    final int start = dotpos + 1;
    if (fullname.endsWith(HEURISTIC_SUFFIX)) {
      final int end = fullname.length() - HEURISTIC_SUFFIX.length();
      return fullname.substring(start, end);
    } else {
      return fullname.substring(start);
    }
  }

  @Override
  public SafetyTraceProxy extendTrace(final ProductDESProxyFactory factory,
                                      final SafetyTraceProxy trace,
                                      final List<AutomatonProxy> automata)
  {
    final Set<AutomatonProxy> oldAutomata =
      new THashSet<AutomatonProxy>(trace.getAutomata());
    boolean done = false;
    boolean det = true;
    for (final AutomatonProxy aut : automata) {
      if (!oldAutomata.contains(aut)) {
        done = false;
        final TraceFinder finder = getTraceFinder(aut);
        det &= finder.isDeterministic();
      }
    }
    if (done) {
      return trace;
    }
    final String name = trace.getName();
    final String comment = trace.getComment();
    final URI location = trace.getLocation();
    final ProductDESProxy des = trace.getProductDES();
    final List<TraceStepProxy> oldSteps = trace.getTraceSteps();
    if (det) {
      return factory.createSafetyTraceProxy(name, comment, location,
                                            des, automata, oldSteps);
    }
    final int numSteps = oldSteps.size();
    final KindTranslator translator = getKindTranslator();
    final List<TraceStepProxy> newSteps =
      new ArrayList<TraceStepProxy>(numSteps);
    int depth = 0;
    for (final TraceStepProxy oldStep : oldSteps) {
      final EventProxy event = oldStep.getEvent();
      final Map<AutomatonProxy,StateProxy> oldMap = oldStep.getStateMap();
      Map<AutomatonProxy,StateProxy> newMap = null;
      for (final AutomatonProxy aut : automata) {
        if (!oldAutomata.contains(aut)) {
          final TraceFinder finder = getTraceFinder(aut);
          if (translator.getComponentKind(aut) == ComponentKind.SPEC &&
              finder.getNumberOfAcceptedSteps() == depth - 1) {
            // Don't try to find state for last step of spec.
            continue;
          }
          final StateProxy state = finder.getState(depth);
          if (state != null) {
            if (newMap == null) {
              newMap = new HashMap<AutomatonProxy,StateProxy>(oldMap);
            }
            newMap.put(aut, state);
          }
        }
      }
      if (newMap == null) {
        newSteps.add(oldStep);
      } else {
        final TraceStepProxy newStep =
          factory.createTraceStepProxy(event, newMap);
        newSteps.add(newStep);
      }
      depth++;
    }
    return factory.createSafetyTraceProxy(name, comment, location,
                                          des, automata, newSteps);
  }


  //#########################################################################
  //# Auxiliary Methods
  AutomatonProxy checkAutomata(final boolean specs,
                               final Set<AutomatonProxy> automata,
                               final Comparator<AutomatonProxy> comp,
                               final TraceProxy counterExample)
  {
    return checkAutomata
      (null, specs, automata, comp, counterExample);
  }

  AutomatonProxy checkAutomata(AutomatonProxy bestautomaton,
                               final boolean specs,
                               final Set<AutomatonProxy> automata,
                               final Comparator<AutomatonProxy> comp,
                               final TraceProxy counterExample)
  {
    for (final AutomatonProxy automaton : automata) {
      final KindTranslator translator = getKindTranslator();
      final int i = getNumberOfAcceptedEvents(automaton, counterExample);
      final List<EventProxy> events = counterExample.getEvents();
      if (i != events.size()) {
        final boolean rejected;
        if (i < 0 || !specs) {
          rejected = true;
        } else {
          final EventProxy event = counterExample.getEvents().get(i);
          rejected = translator.getEventKind(event) == EventKind.CONTROLLABLE;
        }
        if (rejected) {
          if (bestautomaton == null ||
              comp.compare(bestautomaton, automaton) < 0) {
            bestautomaton = automaton;
          }
        }
      }
    }
    return bestautomaton;
  }


  //#########################################################################
  //# Trace Checking
  boolean accepts(final AutomatonProxy aut, final TraceProxy trace)
  {
    return trace.getEvents().size() == getNumberOfAcceptedEvents(aut, trace);
  }

  int getNumberOfAcceptedEvents(final AutomatonProxy aut,
                                final TraceProxy trace)
  {
    final TraceFinder finder = getTraceFinder(aut);
    return finder.computeNumberOfAcceptedSteps(trace);
  }

  private TraceFinder getTraceFinder(final AutomatonProxy aut)
  {
    TraceFinder finder = mTraceFinders.get(aut);
    if (finder == null) {
      finder = new TraceFinder(aut, mKindTranslator);
      mTraceFinders.put(aut, finder);
    }
    return finder;
  }


  //#########################################################################
  //# Data Members
  private final KindTranslator mKindTranslator;
  private final Map<AutomatonProxy,TraceFinder> mTraceFinders;


  //#########################################################################
  //# Class Constants
  static final String HEURISTIC_SUFFIX = "Heuristic";

}

