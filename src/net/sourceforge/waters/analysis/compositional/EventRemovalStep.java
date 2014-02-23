//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters Analysis
//# PACKAGE: net.sourceforge.waters.analysis.compositional
//# CLASS:   EventRemovalStep
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.analysis.compositional;

import java.util.Collection;
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
import net.sourceforge.waters.model.des.TransitionProxy;


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
   * @param  analyzer  The model analyser that owns this abstraction step.
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
    this(analyzer, results, originals, null);
  }

  /**
   * Creates a new event removal step.
   * @param  analyzer  The model analyser that owns this abstraction step.
   * @param  results   List of automata after event removal.
   * @param  originals List of automata before removal, with indexes
   *                   matching those of results. The automaton at position
   *                   <I>i</I> in originals is replaced by the automaton
   *                   at the same position in results.
   * @param  failing   Collection of failing events or <CODE>null</CODE>.
   *                   Failing events need special (slow) treatment in
   *                   trace expansion.
   */
  EventRemovalStep(final AbstractCompositionalModelAnalyzer analyzer,
                   final List<AutomatonProxy> results,
                   final List<AutomatonProxy> originals,
                   final Collection<EventProxy> failing)
  {
    super(analyzer, results, originals);
    if (failing == null || failing.isEmpty()){
      mFailingEvents = null;
    } else {
      mFailingEvents = failing;
    }
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
    TraceStepProxy previousStep = null;
    final ListIterator<TraceStepProxy> iter = steps.listIterator();
    while (iter.hasNext()) {
      final TraceStepProxy step = iter.next();
      final EventProxy event = step.getEvent();
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
        StateProxy target = entry.getValue();
        if (mFailingEvents != null && event != null &&
            mFailingEvents.contains(event) && previousStep != null) {
          final StateProxy source = previousStep.getStateMap().get(newAut);
          target = getAlternativeSuccessor(newAut, source, event, target);
        }
        newStepMap.put(newAut, target);
      }
      final TraceStepProxy newStep =
        factory.createTraceStepProxy(event, newStepMap);
      iter.set(newStep);
      previousStep = newStep;
    }
    return steps;
  }


  //#######################################################################
  //# Auxiliary Methods
  private static StateProxy getAlternativeSuccessor(final AutomatonProxy aut,
                                                    final StateProxy source,
                                                    final EventProxy event,
                                                    final StateProxy target)
  {
    if (aut.getEvents().contains(event)) {
      StateProxy result = null;
      for (final TransitionProxy trans : aut.getTransitions()) {
        if (trans.getSource() == source && trans.getEvent() == event) {
          if (trans.getTarget() == target) {
            return target;
          } else if (result == null) {
            result = trans.getTarget();
          }
        }
      }
      return result;
    } else {
      return target;
    }
  }


  //#######################################################################
  //# Data Members
  private final Collection<EventProxy> mFailingEvents;

}
