//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.analysis.modular
//# CLASS:   AbstractModularHeuristic
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.analysis.modular;

import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import net.sourceforge.waters.model.analysis.KindTranslator;
import net.sourceforge.waters.model.des.AutomatonProxy;
import net.sourceforge.waters.model.des.EventProxy;
import net.sourceforge.waters.model.des.StateProxy;
import net.sourceforge.waters.model.des.TraceProxy;
import net.sourceforge.waters.model.des.TransitionProxy;
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
  //# Interface net.sourceforge.waters.analysis.modular.ModularHeuristic
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


  //#########################################################################
  //# Auxiliary Methods
  AutomatonProxy checkAutomata(final boolean specs,
			       final Set<AutomatonProxy> automata,
			       final Comparator<AutomatonProxy> comp,
			       final TraceProxy counterExample,
			       final KindTranslator translator)
  {
    return checkAutomata
      (null, specs, automata, comp, counterExample, translator);
  }

  AutomatonProxy checkAutomata(AutomatonProxy bestautomaton,
			       final boolean specs,
			       final Set<AutomatonProxy> automata,
			       final Comparator<AutomatonProxy> comp,
			       final TraceProxy counterExample,
			       final KindTranslator translator)
  {
    for (final AutomatonProxy automaton : automata) {
      final int i = accepts(automaton, counterExample);
      if (i != counterExample.getEvents().size()) {
        if (!specs ||
	    translator.getEventKind(counterExample.getEvents().get(i)) ==
	    EventKind.CONTROLLABLE) {
          if (bestautomaton == null ||
	      comp.compare(bestautomaton, automaton) < 0) {
            bestautomaton = automaton;
          }
        }
      }
    }
    return bestautomaton;
  }
  
  static boolean acc(final AutomatonProxy automaton,
		     final TraceProxy counterExample)
  {
    return
      counterExample.getEvents().size() ==
      accepts(automaton, counterExample);
  }
  
  static int accepts(final AutomatonProxy automaton,
		     final TraceProxy counterExample)
  {
    Map<Key, StateProxy> mapAutomaton = createMap(automaton);
    int i = 0;
    StateProxy state = null;
    for (final StateProxy s : automaton.getStates()) {
      if (s.isInitial()) {
        state = s;
        break;
      }
    }
    for (final EventProxy e : counterExample.getEvents()) {
      if (automaton.getEvents().contains(e)) {
        final Key k = new Key(state, e);
        state = mapAutomaton.get(k);
        if (state == null) {
          break;
        }
      }
      i++;
    }
    return i;
  }
  
  static Map<Key, StateProxy> createMap(final AutomatonProxy automaton) 
  {
    final Map<Key, StateProxy> mapAutomaton =
      new HashMap<Key, StateProxy>(automaton.getTransitions().size());
    for (final TransitionProxy trans : automaton.getTransitions()) {
      mapAutomaton.put(new Key(trans.getSource(), trans.getEvent()),
		       trans.getTarget());
    }
    return mapAutomaton;
  }


  //#########################################################################
  //# Inner Class Key
  private static final class Key
  {
    private final StateProxy mSource;
    private final EventProxy mEvent;
    
    public Key(StateProxy source, EventProxy event)
    {
      mSource = source;
      mEvent = event;
    }
    
    public boolean equals(Object o)
    {
      if (!(o instanceof Key)) {
        return false;
      }
      Key k = (Key)o;
      return k.mSource.equals(mSource) && mEvent.equals(k.mEvent);
    }
    
    public int hashCode()
    {
      int hashCode = 17;
      hashCode += 31 * mSource.hashCode();
      hashCode += 31 * mEvent.hashCode();
      return hashCode;
    }
  }


  //#########################################################################
  //# Class Constants
  static final String HEURISTIC_SUFFIX = "Heuristic";

}
