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
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import net.sourceforge.waters.analysis.tr.EventEncoding;
import net.sourceforge.waters.analysis.tr.ListBufferTransitionRelation;
import net.sourceforge.waters.analysis.tr.TauClosure;
import net.sourceforge.waters.analysis.tr.TransitionIterator;
import net.sourceforge.waters.model.analysis.KindTranslator;
import net.sourceforge.waters.model.analysis.OverflowException;
import net.sourceforge.waters.model.des.AutomatonProxy;
import net.sourceforge.waters.model.des.EventProxy;
import net.sourceforge.waters.model.des.ProductDESProxy;
import net.sourceforge.waters.model.des.ProductDESProxyFactory;


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
  protected void initialiseEventsToAutomata()
    throws OverflowException
  {
    super.initialiseEventsToAutomata();
    for(final AutomatonProxy aut : getCurrentAutomata())
    {
      addDisablingAutomaton(aut);
    }
  }

  @Override
  protected void updateEventsToAutomata
  (final AutomatonProxy autToAdd,
   final List<AutomatonProxy> autToRemove)
  throws OverflowException
  {
    super.updateEventsToAutomata(autToAdd, autToRemove);
    addDisablingAutomaton(autToAdd);
  }

  @Override
  protected void addEventsToAllAutomata()
  throws OverflowException
  {
    super.addEventsToAllAutomata();
    for(final AutomatonProxy aut: getCurrentAutomata())
    {
    addDisablingAutomaton(aut);
    }
  }


  private void addDisablingAutomaton(final AutomatonProxy aut)
    throws OverflowException
  {
    final KindTranslator translator = getKindTranslator();
    int markingID = -1;

    //create event encoding to check for tau (local events)

    //go through all events in aut, ask for event info, ask if it is local only here
    final Collection<AutomatonProxy> collection =
      Collections.singletonList(aut);
    final EventEncoding encoding = new EventEncoding();
    for (final EventProxy event : aut.getEvents()) {
      final EventInfo info = getEventInfo(event);
      if (info == null) {
        final int code = encoding.addEvent(event, translator, (byte)0);
        if (event == getUsedDefaultMarking()) {
          markingID = code;
        }    super.addEventsToAutomata(aut);


      } else if (info.isLocal(collection)) {
        encoding.addSilentEvent(event);
      } else {
        encoding.addEvent(event, translator, (byte)0);
      }
    }
    if (markingID == -1) {
      return;
    }

    final ListBufferTransitionRelation transrel =
      new ListBufferTransitionRelation(aut,
                                       encoding,
                                       ListBufferTransitionRelation.CONFIG_SUCCESSORS);
    final TauClosure tauClosure =
      transrel.createSuccessorsTauClosure(getInternalTransitionLimit());
    final TransitionIterator preClosureIterator =
      tauClosure.createPreEventClosureIterator();
    final TransitionIterator normalTransIterator =
      transrel.createSuccessorsReadOnlyIterator();

    //for each event e
    //for each state
    //if state canNOT execute tauchain followed by event
    //and not a dump state
    //then it is a disabling aut
    //add aut to info
    //break

    for (int e = EventEncoding.NONTAU; e < encoding
                                        .getNumberOfProperEvents(); e++) {
      for (int s = 0; s < transrel.getNumberOfStates(); s++) {
        if (transrel.isReachable(s)) {
          preClosureIterator.reset(s, e);
          if (preClosureIterator.advance()) {
            //it has outgoing transitions
          } else {
            normalTransIterator.resetState(s);
            if (normalTransIterator.advance()
                || transrel.isMarked(s, markingID)) {
              //it's not a dump state
              //record we have found disabling automaton
              final EventProxy event = encoding.getProperEvent(e);
              getEventInfo(event).addDisablingAutomaton(aut);
              break;
            }
          }
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
            if (automataList.containsAll(info.getDisablingAutomata()))
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
            if (automataList.containsAll(info.getDisablingAutomata())) {
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
