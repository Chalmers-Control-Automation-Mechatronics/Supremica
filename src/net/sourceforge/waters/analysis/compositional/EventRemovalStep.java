//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters Analysis
//# PACKAGE: net.sourceforge.waters.analysis.compositional
//# CLASS:   EventRemovalStep
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.analysis.compositional;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;

import net.sourceforge.waters.model.des.AutomatonProxy;
import net.sourceforge.waters.model.des.EventProxy;
import net.sourceforge.waters.model.des.ProductDESProxyFactory;
import net.sourceforge.waters.model.des.StateProxy;
import net.sourceforge.waters.model.des.TraceStepProxy;


/**
 * An abstraction step that consists of removing some events from the
 * model. Event removal is implemented by replacing automata with simplified
 * copies that use the same state objects, so trace expansion can be
 * achieved by replacing only the automata in a trace.
 *
 * @author Robi Malik
 */

class EventRemovalStep extends AbstractionStep
{

  //#######################################################################
  //# Constructor
  /**
   * Creates a new event removal step.
   * @param  results   List of automata after event removal.
   * @param  originals List of automata before removal, with indexes
   *                   matching those of results. The automaton at position
   *                   <I>i</I> in originals is replaced by the automaton
   *                   at the same position in results.
   */
  EventRemovalStep(final AbstractCompositionalModelAnalyzer analyzer,
                   final List<AutomatonProxy> results,
                   final List<AutomatonProxy> originals)
  {
    super(analyzer, results, originals);
  }

  //#######################################################################
  //# Trace Computation
  @Override
  List<TraceStepProxy> convertTraceSteps(final List<TraceStepProxy> steps)
  {
    final List<AutomatonProxy> results = getResultAutomata();
    final List<AutomatonProxy> originals = getOriginalAutomata();
    final int numAutomata = results.size();
    final Map<AutomatonProxy,AutomatonProxy> autMap =
      new HashMap<AutomatonProxy,AutomatonProxy>(numAutomata);
    final Iterator<AutomatonProxy> resultIter = results.iterator();
    final Iterator<AutomatonProxy> originalIter = originals.iterator();
    while (resultIter.hasNext()) {
      final AutomatonProxy result = resultIter.next();
      final AutomatonProxy original = originalIter.next();
      autMap.put(result, original);
    }
    final ProductDESProxyFactory factory = getFactory();
    final ListIterator<TraceStepProxy> iter = steps.listIterator();
    while (iter.hasNext()) {
      final TraceStepProxy step = iter.next();
      final Map<AutomatonProxy,StateProxy> stepMap = step.getStateMap();
      final int size = stepMap.size();
      final Map<AutomatonProxy,StateProxy> newStepMap =
        new HashMap<AutomatonProxy,StateProxy>(size);
      for (final Map.Entry<AutomatonProxy,StateProxy> entry :
           stepMap.entrySet()) {
        final AutomatonProxy aut = entry.getKey();
        AutomatonProxy newAut = autMap.get(aut);
        if (newAut == null) {
          newAut = aut;
        }
        final StateProxy state = entry.getValue();
        newStepMap.put(newAut, state);
      }
      final EventProxy event = step.getEvent();
      final TraceStepProxy newStep =
        factory.createTraceStepProxy(event, newStepMap);
      iter.set(newStep);
    }
    return steps;
  }

}
