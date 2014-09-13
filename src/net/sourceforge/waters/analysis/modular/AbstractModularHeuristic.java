//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters Analysis
//# PACKAGE: net.sourceforge.waters.analysis.modular
//# CLASS:   AbstractModularHeuristic
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.analysis.modular;

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.sourceforge.waters.analysis.abstraction.TraceFinder;
import net.sourceforge.waters.model.analysis.KindTranslator;
import net.sourceforge.waters.model.des.AutomatonProxy;
import net.sourceforge.waters.model.des.EventProxy;
import net.sourceforge.waters.model.des.TraceProxy;
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

  public TraceFinder getTraceFinder(final AutomatonProxy aut)
  {
    TraceFinder finder = mTraceFinders.get(aut);
    if (finder == null) {
      finder = new TraceFinder(aut, mKindTranslator);
      mTraceFinders.put(aut, finder);
    }
    return finder;
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

  //#########################################################################
  //# Data Members
  private final KindTranslator mKindTranslator;
  private final Map<AutomatonProxy,TraceFinder> mTraceFinders;


  //#########################################################################
  //# Class Constants
  static final String HEURISTIC_SUFFIX = "Heuristic";

}
