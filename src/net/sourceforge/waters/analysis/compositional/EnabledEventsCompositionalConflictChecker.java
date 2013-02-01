//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters Analysis
//# PACKAGE: net.sourceforge.waters.analysis.compositional
//# CLASS:   CompositionalConflictChecker
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.analysis.compositional;

import gnu.trove.THashSet;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import net.sourceforge.waters.model.des.AutomatonProxy;
import net.sourceforge.waters.model.des.EventProxy;
import net.sourceforge.waters.model.des.ProductDESProxy;
import net.sourceforge.waters.model.des.ProductDESProxyFactory;
import net.sourceforge.waters.model.des.StateProxy;
import net.sourceforge.waters.model.des.TransitionProxy;


/**
 * <P>
 * A compositional conflict checker that can be configured to use different
 * abstraction sequences for its simplification steps.
 * </P>
 *
 * <P>
 * <I>References:</I><BR>
 * Hugo Flordal, Robi Malik. Compositional Verification in Supervisory
 * Control. SIAM Journal of Control and Optimization, 48(3), 1914-1938, 2009.<BR>
 * Robi Malik, Ryan Leduc. A Compositional Approach for Verifying Generalised
 * Nonblocking, Proc. 7th International Conference on Control and Automation,
 * ICCA'09, 448-453, Christchurch, New Zealand, 2009.
 * </P>
 *
 * @author Robi Malik, Rachel Francis
 */

public class EnabledEventsCompositionalConflictChecker extends
  CompositionalConflictChecker
{

  //#########################################################################
  //# Constructors
  /**
   * Creates a new conflict checker without a model or marking proposition.
   *
   * @param factory
   *          Factory used for trace construction.
   */
  public EnabledEventsCompositionalConflictChecker(final ProductDESProxyFactory factory)
  {
    super(factory);
  }

  /**
   * Creates a new conflict checker to check whether the given model is
   * nonblocking with respect to its default marking.
   *
   * @param model
   *          The model to be checked by this conflict checker.
   * @param method
   *          Abstraction procedure used for simplification.
   * @param factory
   *          Factory used for trace construction.
   */
  public EnabledEventsCompositionalConflictChecker(final ProductDESProxy model,
                                                   final ProductDESProxyFactory factory)
  {
    super(model, factory);
  }

  /**
   * Creates a new conflict checker to check whether the given model is
   * nonblocking.
   *
   * @param model
   *          The model to be checked by this conflict checker.
   * @param marking
   *          The proposition event that defines which states are marked.
   *          Every state has a list of propositions attached to it; the
   *          conflict checker considers only those states as marked that are
   *          labelled by <CODE>marking</CODE>, i.e., their list of
   *          propositions must contain this event (exactly the same object).
   * @param method
   *          Abstraction procedure used for simplification.
   * @param factory
   *          Factory used for trace construction.
   */
  public EnabledEventsCompositionalConflictChecker(final ProductDESProxy model,
                                                   final EventProxy marking,
                                                   final ProductDESProxyFactory factory)
  {
    super(model, marking, factory);

  }

  @Override
  protected void addEventsToAutomata(final AutomatonProxy aut)
  {
    super.addEventsToAutomata(aut);

    //Set<EventProxy> foundEvents = new THashSet<EventProxy>();
    final Map<EventProxy,Set<StateProxy>> foundEvents = new HashMap<EventProxy, Set<StateProxy>>();
    for(final TransitionProxy trans :aut.getTransitions())
    {
      final EventProxy event = trans.getEvent();

      Set<StateProxy> set = foundEvents.get(event);
      if(set == null)
      {
        set = new THashSet<StateProxy>();
        foundEvents.put(event, set);


      }
      final StateProxy state = trans.getSource();
      set.add(state);

    }
    final int numStates = aut.getStates().size();
    //Check if each event is enabled
    for(final EventProxy event : aut.getEvents())
    {
      final EnabledEventsEventInfo eventInfo = getEventInfo(event);
      if(eventInfo != null)
      {

      final Set<StateProxy> set = foundEvents.get(event);
      if(set == null || set.size() != numStates)
      {
        eventInfo.addDisablingAutomaton(aut);
      }
      }
    }


  }

  @Override
  protected EventInfo createEventInfo(final EventProxy event)
  {
    
    return new EnabledEventsEventInfo(event);
  }

  @Override
  protected EnabledEventsEventInfo getEventInfo(final EventProxy event)
  {
    return (EnabledEventsEventInfo) super.getEventInfo(event);
  }


  static class EnabledEventsEventInfo extends EventInfo
  {
    private final Set<AutomatonProxy> mDisablingAutomata;

    private EnabledEventsEventInfo(final EventProxy event)
    {
      
      super(event);
      mDisablingAutomata = new THashSet<AutomatonProxy>();

    }

    private void addDisablingAutomaton(final AutomatonProxy aut)
    {
      //When given an automaton, it adds it to the list of automaton that this event disables.
      mDisablingAutomata.add(aut);
    }

    @SuppressWarnings("unused")
    private AutomatonProxy getSingleDisablingAutomaton()
    {
      if (mDisablingAutomata.size() == 1)
        return mDisablingAutomata.iterator().next(); //If there is only one automata, return it.
      else
        return null;
    }

    //Returns true if the automaton passed in is the only automaton disabling this event.
     boolean isSingleDisablingAutomaton(final AutomatonProxy aut)
    {

      return mDisablingAutomata.size() == 0 || (mDisablingAutomata.size() == 1   
             && mDisablingAutomata.contains(aut));

    }
    
    
    
    

    @Override
     void removeAutomata(final Collection<AutomatonProxy> victims)
    {
      super.removeAutomata(victims);
      mDisablingAutomata.removeAll(victims);
    }

    @Override
     boolean replaceAutomaton(final AutomatonProxy oldAut,
                                     final AutomatonProxy newAut)
    {

      boolean result = super.replaceAutomaton(oldAut, newAut);
      if (mDisablingAutomata.remove(oldAut)) {
        mDisablingAutomata.add(newAut);
        result = true;
      }

        return result;
    }

  }

}
