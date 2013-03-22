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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
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
    this(null, factory);
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
    this(model, null, factory);
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
    this(model, marking, factory, ConflictAbstractionProcedureFactory.OEQ);
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
   * @param factory
   *          Factory used for trace construction.
   * @param abstractionFactory
   *          Factory to define the abstraction sequence to be used.
   */
  public EnabledEventsCompositionalConflictChecker(final ProductDESProxy model,
                                                   final EventProxy marking,
                                                   final ProductDESProxyFactory factory,
                                                   final ConflictAbstractionProcedureFactory abstractionFactory)
  {
    super(model, marking, factory, abstractionFactory,
          new PreselectingMethodFactory(), new SelectingMethodFactory());
  }

  @Override
  protected void addEventsToAutomata(final AutomatonProxy aut)
  {
    super.addEventsToAutomata(aut);

    final EventProxy marking = getUsedDefaultMarking();
    final Boolean markingUsed = aut.getEvents().contains(marking);

    //Set<EventProxy> foundEvents = new THashSet<EventProxy>();
    final Map<EventProxy,Set<StateProxy>> foundEvents =
      new HashMap<EventProxy,Set<StateProxy>>();

    //put something that detects if a state is a dump state
    final Set<StateProxy> nonDumpStates =
      new THashSet<StateProxy>(aut.getStates().size());

    for (final TransitionProxy trans : aut.getTransitions()) {
      //go through transitions

      if (markingUsed)
        nonDumpStates.add(trans.getSource());   //List of states with with outgoing transitions

      final EventProxy event = trans.getEvent();        //get event of this transition

      Set<StateProxy> set = foundEvents.get(event); //create for each event, remember which states that event is enabled
      if (set == null) {            //if set not created yet
        set = new THashSet<StateProxy>();
        foundEvents.put(event, set);

      }
      final StateProxy state = trans.getSource();
      set.add(state);           //adds the event

    }

    if (markingUsed) {
      for (final StateProxy state : aut.getStates()) {
        //add those with marking
        if (state.getPropositions().contains(marking)) {
          nonDumpStates.add(state);             //It is not a dump state if it is marked
        }
      }
    }

    int numStates;
    if (markingUsed) {
      numStates = nonDumpStates.size();     //only care about non dump states
    } else {
      numStates = aut.getStates().size();
    }
    //Check if each event is enabled
    for (final EventProxy event : aut.getEvents()) {
      final EnabledEventsEventInfo eventInfo = getEventInfo(event);
      if (eventInfo != null) {

        final Set<StateProxy> set = foundEvents.get(event);
        //event is not in automaton or it is not enabled in all nondump states
        if (set == null || set.size() != numStates) {
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

  /**
   * The preselecting method that considers every set of automata with at
   * least one local event as a candidate.
   */
  public static final PreselectingMethod MustLE = new PreselectingMethod(
    "MustLE") {
    @Override
    PreselectingHeuristic createHeuristic(final AbstractCompositionalModelAnalyzer verifier)
    {
      final EnabledEventsCompositionalConflictChecker everifier =
        (EnabledEventsCompositionalConflictChecker) verifier;
      return everifier.new HeuristicMustLE();
    }
  };

  /**
   * The selecting method that chooses the candidate with the highest
   * proportion of local events.
   */
  public static final SelectingMethod MaxLE = new SelectingMethod("MaxLE") {
    @Override
    Comparator<Candidate> createComparator(final AbstractCompositionalModelAnalyzer verifier)
    {
      final EnabledEventsCompositionalConflictChecker everifier =
        (EnabledEventsCompositionalConflictChecker) verifier;
      return everifier.new ComparatorMaxLE();
    }
  };

  /**
   * The selecting method that chooses the candidate with the minimum
   * estimated number of states in the synchronous product.
   */
  public static final SelectingMethod MinSE = new SelectingMethod("MinSE") {
    @Override
    Comparator<Candidate> createComparator(final AbstractCompositionalModelAnalyzer verifier)
    {
      final EnabledEventsCompositionalConflictChecker everifier =
        (EnabledEventsCompositionalConflictChecker) verifier;
      return everifier.new ComparatorMinSE();
    }
  };


  //# Inner Class PreselectingMethodFactory
  protected static class PreselectingMethodFactory extends
    CompositionalConflictChecker.PreselectingMethodFactory
  {
    //#######################################################################
    //# Constructors
    protected PreselectingMethodFactory()
    {
      register(MustLE);
    }
  }


  //#########################################################################
  //# Inner Class SelectingMethodFactory
  protected static class SelectingMethodFactory extends
    AbstractCompositionalModelVerifier.SelectingMethodFactory
  {
    //#######################################################################
    //# Constructors
    protected SelectingMethodFactory()
    {
      register(MaxLE);
      register(MinSE);
    }
  }


  //#########################################################################
  //# Inner Class HeuristicMustLE
  private class HeuristicMustLE implements PreselectingHeuristic
  {

    //#######################################################################
    //# Interface PreselectingHeuristic
    @Override
    public Collection<Candidate> findCandidates()
    {
      final Collection<Candidate> candidates = new LinkedList<Candidate>(); //Create collection to store candidates to return
      final Collection<EventProxy> events = getCurrentEvents(); //Get all events and put into a collection

      final Collection<List<AutomatonProxy>> found = //This is the automaton combinations we have already found
        new THashSet<List<AutomatonProxy>>(events.size());

      for (final EventProxy event : events) //For all events
      {
        final EnabledEventsEventInfo info = getEventInfo(event); //Get the event info
        assert info.getNumberOfAutomata() > 0; //Make sure that this event is being used by an automaton
        if (info.getNumberOfAutomata() > 1) { //If the event is being used by more than one automaton
          List<AutomatonProxy> list = null;
          if (info.mDisablingAutomata.size() > 1) //If there is more than one automata disabling the event
          {
            list = info.getDisablingAutomataList(); //Get the list of automaton that the event is disabled in
          } else //if(info.mDisablingAutomata.size() == 1)  //If only one automata disables the event
          {
            list = info.getSortedAutomataList(); //Get the list of all automata using the event

          }
          if (isPermissibleCandidate(list) && found.add(list)) { //Checks to see if it is a possible candidate, adds it to the found list and checks if it was already in the list
            final Set<EventProxy> localEvents = identifyLocalEvents(list); //When the automata are combined, some events will become local, list all of these
            final Candidate candidate = new Candidate(list, localEvents); //creates a candidate by giving it the list of automaton and the events that will be local
            candidates.add(candidate);
          }
        }

      }
      return candidates;
    }
  }


  //#########################################################################
  //# Inner Class ComparatorMaxLE
  private class ComparatorMaxLE extends SelectingComparator
  {

    //#######################################################################
    //# Overrides for SelectingComparator
    @Override
    double getHeuristicValue(final Candidate candidate)
    {
      int alwaysEnabledEvents = 0;

      final List<AutomatonProxy> automataList = candidate.getAutomata();

      for (final EventProxy event : candidate.getOrderedEvents()) //For each event in the candidate
      {
        final EnabledEventsEventInfo info = getEventInfo(event);

        if (info != null) //when would info be null? Right at start?
          if (info.getDisablingAutomata() != null)
            //If the event is never disabled, or only disabled in one automaton, or all the automaton it is disabled in are getting merged
            if (info.mDisablingAutomata.size() == 0
                || info.mDisablingAutomata.size() == 1
                || automataList.containsAll(info.getDisablingAutomata()))
              alwaysEnabledEvents++;
      }

      return -(candidate.getLocalEventCount() + 0.5 * alwaysEnabledEvents)
             / candidate.getNumberOfEvents();
    }

  }


  //#########################################################################
  //# Inner Class ComparatorMinSE
  private class ComparatorMinSE extends SelectingComparator
  {

    //#######################################################################
    //# Overrides for SelectingComparator
    @Override
    double getHeuristicValue(final Candidate candidate)
    {
      double product = 1.0;
      for (final AutomatonProxy aut : candidate.getAutomata()) { //for all automata in the candidate
        product *= aut.getStates().size(); //multiply the number of each of states together.
      }
      final double totalEvents = candidate.getNumberOfEvents();
      final double localEvents = candidate.getLocalEventCount();
      int alwaysEnabledEvents = 0;

      final List<AutomatonProxy> automataList = candidate.getAutomata();

      for (final EventProxy event : candidate.getOrderedEvents()) //For each event in the candidate
      {
        final EnabledEventsEventInfo info = getEventInfo(event);

        if (info != null) //propositions
          if (info.getDisablingAutomata() != null)
            //If the event is never disabled, or only disabled in one automaton, or all the automaton it is disabled in are getting merged
            if (info.mDisablingAutomata.size() == 0
                || info.mDisablingAutomata.size() == 1
                || automataList.containsAll(info.getDisablingAutomata())) {
              alwaysEnabledEvents++;

            }

      }

      return product
             * (totalEvents - localEvents - 0.5 * alwaysEnabledEvents)
             / totalEvents;
    }

  }


  //INNER CLASS
  static class EnabledEventsEventInfo extends EventInfo
  {
    //List of automata the disable this event
    private final Set<AutomatonProxy> mDisablingAutomata;

    private EnabledEventsEventInfo(final EventProxy event)
    {

      super(event);
      mDisablingAutomata = new THashSet<AutomatonProxy>();

    }

    public Set<AutomatonProxy> getDisablingAutomata()
    {
      return mDisablingAutomata;
    }

    public List<AutomatonProxy> getDisablingAutomataList()
    {
      //This seems to be needed because some methods want the disabling automata as a list.
      final List<AutomatonProxy> disablingList =
        new ArrayList<AutomatonProxy>(mDisablingAutomata);
      Collections.sort(disablingList);
      return disablingList;
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

      return mDisablingAutomata.size() == 0
             || (mDisablingAutomata.size() == 1 && mDisablingAutomata
               .contains(aut));

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
