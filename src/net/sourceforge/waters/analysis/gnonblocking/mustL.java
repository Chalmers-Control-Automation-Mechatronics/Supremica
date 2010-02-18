//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters/Supremica GUI
//# PACKAGE: net.sourceforge.waters.analysis.gnonblocking
//# CLASS:   mustL
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.analysis.gnonblocking;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import net.sourceforge.waters.model.des.AutomatonProxy;
import net.sourceforge.waters.model.des.EventProxy;
import net.sourceforge.waters.model.des.ProductDESProxy;


/**
 * @author rmf18
 */
public class mustL implements Heuristic
{
  final ProductDESProxy mModel;

  public mustL(final ProductDESProxy model)
  {
    mModel = model;
  }

  /**
   * There is a candidate for every event in the model, each candidate contains
   * the set of automaton which use that event.
   */
  public Collection<Candidate> evaulate()
  {
    final HashMap<EventProxy,Candidate> eventCandidates =
        new HashMap<EventProxy,Candidate>();
    for (final AutomatonProxy aut : mModel.getAutomata()) {
      for (final EventProxy event : aut.getEvents()) {
        if (!eventCandidates.containsKey(event)) {
          final Set<AutomatonProxy> automata = new HashSet<AutomatonProxy>();
          final Candidate candidate = new Candidate(automata);
          eventCandidates.put(event, candidate);
        }
        eventCandidates.get(event).getAutomata().add(aut);
      }
    }
    return eventCandidates.values();
  }

}
