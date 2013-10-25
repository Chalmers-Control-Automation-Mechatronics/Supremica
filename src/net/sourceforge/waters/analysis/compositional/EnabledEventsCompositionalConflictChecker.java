//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters Analysis
//# PACKAGE: net.sourceforge.waters.analysis.compositional
//# CLASS:   EnabledEventsCompositionalConflictChecker
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.analysis.compositional;

import gnu.trove.set.hash.THashSet;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import net.sourceforge.waters.analysis.modular.ModularControllabilityChecker;
import net.sourceforge.waters.analysis.tr.EventEncoding;
import net.sourceforge.waters.analysis.tr.ListBufferTransitionRelation;
import net.sourceforge.waters.analysis.tr.TauClosure;
import net.sourceforge.waters.analysis.tr.TransitionIterator;
import net.sourceforge.waters.cpp.analysis.NativeControllabilityChecker;
import net.sourceforge.waters.model.analysis.AnalysisException;
import net.sourceforge.waters.model.analysis.KindTranslator;
import net.sourceforge.waters.model.analysis.OverflowException;
import net.sourceforge.waters.model.analysis.des.ControllabilityChecker;
import net.sourceforge.waters.model.des.AutomatonProxy;
import net.sourceforge.waters.model.des.EventProxy;
import net.sourceforge.waters.model.des.ProductDESProxy;
import net.sourceforge.waters.model.des.ProductDESProxyFactory;
import net.sourceforge.waters.xsd.base.ComponentKind;
import net.sourceforge.waters.xsd.base.EventKind;


/**
 * @author Colin Pilbrow
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
    this(model, marking, factory, ConflictAbstractionProcedureFactory.EENB);
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


  //#########################################################################
  //# Configuration
  /**
   * Sets the state limit for the language inclusion check to find
   * additional always enabled events.
   * @param  Maximum state in language check,
   *         or <CODE>0</CODE> to disable this search.
   */
  public void setEnabledEventSearchStateLimit(final int limit)
  {
    mEnabledEventSearchStateLimit = limit;
  }

  /**
   * Gets the state limit for the language inclusion check to find
   * additional always enabled events.
   * @see #setEnabledEventSearchStateLimit(int) setEnabledEventSearchStateLimit()
   */
  public int getEnabledEventSearchStateLimit()
  {
    return mEnabledEventSearchStateLimit;
  }


  //#########################################################################
  //# Interface net.sourceforge.waters.model.analysis.Abortable
  @Override
  public void requestAbort()
  {
    super.requestAbort();
    if (mControllabilityChecker != null) {
      mControllabilityChecker.requestAbort();
    }
  }

  @Override
  public void resetAbort()
  {
    super.resetAbort();
    if (mControllabilityChecker != null) {
      mControllabilityChecker.resetAbort();
    }
  }


  //#########################################################################
  //# Overrides for abstract base class
  //# net.sourceforge.waters.analysis.compositional.
  //# AbstractCompositionalModelAnalyzer
  @Override
  protected void setUp() throws AnalysisException
  {
    super.setUp();
    if (mEnabledEventSearchStateLimit > 0) {
      final ProductDESProxyFactory factory = getFactory();
      final ControllabilityChecker inner =
        new NativeControllabilityChecker(factory);
      mControllabilityChecker =
        new ModularControllabilityChecker(factory, inner);
      mControllabilityChecker.setNodeLimit(mEnabledEventSearchStateLimit);
    } else {
      mControllabilityChecker = null;
    }
  }

  @Override
  protected void tearDown()
  {
    super.tearDown();
    mControllabilityChecker = null;
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

  @Override
  protected void initialiseEventsToAutomata()
    throws OverflowException
  {
    super.initialiseEventsToAutomata();
    for (final AutomatonProxy aut : getCurrentAutomata()) {
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
    for (final AutomatonProxy aut : getCurrentAutomata()) {
      addDisablingAutomaton(aut);
    }
  }


  //#########################################################################
  //# Language Inclusion Search for Always Enabled Events
  /**
   * Returns a list of all always enabled events in the given automaton.
   * @param  aut       The automaton being simplified.
   * @param  candidate The candidate being simplified.
   */
  List<EventProxy> calculateAlwaysEnabledEvents
    (final AutomatonProxy aut, final Candidate candidate)
  throws AnalysisException
  {
    // If an automaton doesn't have tau or certain conflict states,
    // don't bother calculating always enabled.
    boolean autContainsTau = false;
    for (final EventProxy e : aut.getEvents()) {
      final EnabledEventsEventInfo eInfo = getEventInfo(e);
      if (eInfo == null && e.getKind() != EventKind.PROPOSITION) {
        autContainsTau = true;
        break;
      }
    }

    boolean autHasBlockingStates = false;
    if (!autContainsTau) {
      //Creates transRelIterator to find dump states
      final KindTranslator translator = getKindTranslator();
      int markingID = -1;
      final EventEncoding encoding = new EventEncoding();
      for (final EventProxy event : aut.getEvents()) {
        final EventInfo info = getEventInfo(event);
        if (info == null) {
          final int code = encoding.addEvent(event, translator, 0);
          if (event == getUsedDefaultMarking()) {
            markingID = code;
          }
          //only looks for dump states if aut has no tau so no silent events
        } else {
          encoding.addEvent(event, translator, 0);
        }
      }
      final ListBufferTransitionRelation transrel =
        new ListBufferTransitionRelation(aut, encoding,
                                         ListBufferTransitionRelation.CONFIG_SUCCESSORS);
      final TransitionIterator normalTransIterator =
        transrel.createSuccessorsReadOnlyIterator();
      for (int s = 0; s < transrel.getNumberOfStates(); s++) {
        if (transrel.isReachable(s)) {
          normalTransIterator.resetState(s);
          if (!(normalTransIterator.advance() || markingID < 0 || transrel
            .isMarked(s, markingID))) {
            //Then s is not a dump state
            autHasBlockingStates = true;
            break;
          }
        }
      }
    }

    final List<EventProxy> alwaysEnabledEventsList =
      new ArrayList<EventProxy>();
    if (autHasBlockingStates || autContainsTau) {
      //getCurrentAut - ones in candidate + aut
      final List<AutomatonProxy> modelList = new ArrayList<AutomatonProxy>();
      modelList.addAll(getCurrentAutomata());
      modelList.removeAll(candidate.getAutomata());
      modelList.add(aut);

      //for each of the events in aut
      for (final EventProxy event : aut.getEvents()) {
        //If it is not tau or proposition
        final EnabledEventsEventInfo info = getEventInfo(event);
        if (info != null && !info.isLocal(candidate.getAutomata())) {

          boolean isEventAlwaysEnabledAlready = false;
          //If an event is always enabled for this automaton we do not need to test it again
          if (info.isAlwaysEnabledCandidate(candidate)) {
            isEventAlwaysEnabledAlready = true;
            info.addAlwaysEnabledAutomaton(aut);
            //System.out.println("skipped");
          }
          //If the old rule finds this event is always enabled do not need to do hard test
          if (!isEventAlwaysEnabledAlready
              && info.isSingleDisablingCandidate(candidate)) {
            isEventAlwaysEnabledAlready = true;
            info.addAlwaysEnabledAutomaton(aut);
            //System.out.println("skipped because of old test");
          }
          if (isEventAlwaysEnabledAlready) {
            alwaysEnabledEventsList.add(event);
          } else {
            final List<AutomatonProxy> newModelList =
              new ArrayList<AutomatonProxy>();
            //Extend each of the specs to have helpful redundant transitions of this event
            for (final AutomatonProxy specAut : modelList) {
              //We don't want to change the plant here
              if (!specAut.equals(aut)) {
                if (specAut.getEvents().contains(event)) {
                  newModelList
                    .add(createAutomatonForAlwaysEnabledEvents(specAut, event));
                } else {
                  //Add unchanged aut to list
                  newModelList.add(specAut);
                }
              } else {
                newModelList.add(specAut);
              }
            }

            mControllabilityChecker.setModel(createProductDESProxy(newModelList));
            //Set new KindTranslator
            mControllabilityChecker.setKindTranslator(new KindTranslator() {
              //This event is set to uncontrollable, the rest are controllable
              @Override
              public EventKind getEventKind(final EventProxy e)
              {
                if (e == event) {
                  return EventKind.UNCONTROLLABLE;
                } else {
                  final KindTranslator parentTranslator = getKindTranslator();
                  switch (parentTranslator.getEventKind(e)) {
                  case UNCONTROLLABLE:
                  case CONTROLLABLE:
                    return EventKind.CONTROLLABLE;
                  default:
                    return EventKind.PROPOSITION;
                  }
                }
              }

              //Set aut as plant, the rest as spec
              @Override
              public ComponentKind getComponentKind(final AutomatonProxy a)
              {
                //aut is changed and not in model
                //a.equals(aut) getName()
                if (a == aut) {
                  return ComponentKind.PLANT;
                } else {
                  final KindTranslator parentTranslator = getKindTranslator();
                  switch (parentTranslator.getComponentKind(a)) {
                  case PLANT:
                  case SPEC:
                    return ComponentKind.SPEC;
                  default:
                    return null;
                  }
                }
              }
            });

            try {
              if (mControllabilityChecker.run()) {
                //If mChecker returns true then this event is always enabled in this aut
                alwaysEnabledEventsList.add(event);
                info.addAlwaysEnabledAutomaton(aut);
              }
            } catch (final OverflowException ex) {
              //If it runs out of space, assume it is not always enabled
              //System.err.println("Ran out of space while checking for Always Enabled Events");
            }
          }
        }
      }

    }
    //System.out.println("Found " + alwaysEnabledEventsList.size() + " always enabled events in aut " + aut.getName());
    return alwaysEnabledEventsList;

  }


  //#########################################################################
  //# Auxiliary Methods
  private void addDisablingAutomaton(final AutomatonProxy aut)
    throws OverflowException
  {
    final KindTranslator translator = getKindTranslator();
    int markingID = -1;
    final Collection<AutomatonProxy> collection =
      Collections.singletonList(aut);
    final EventEncoding encoding = new EventEncoding();
    for (final EventProxy event : aut.getEvents()) {
      final EventInfo info = getEventInfo(event);
      if (info == null) {
        final int code = encoding.addEvent(event, translator, 0);
        if (event == getUsedDefaultMarking()) {
          markingID = code;
        }
        super.addEventsToAutomata(aut);
      } else if (info.isLocal(collection)) {
        encoding.addSilentEvent(event);
      } else {
        encoding.addEvent(event, translator, 0);
      }
    }

    final ListBufferTransitionRelation transrel =
      new ListBufferTransitionRelation(aut, encoding,
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

    for (int e = EventEncoding.NONTAU;
         e < encoding.getNumberOfProperEvents(); e++) {
      for (int s = 0; s < transrel.getNumberOfStates(); s++) {
        if (transrel.isReachable(s)) {
          preClosureIterator.reset(s, e);
          if (preClosureIterator.advance()) {
            // It has outgoing transitions.
          } else {
            normalTransIterator.resetState(s);
            if (normalTransIterator.advance() ||
                markingID < 0 || transrel.isMarked(s, markingID)) {
              // It's not a dump state.
              // Record that we have found a disabling automaton.
              final EventProxy event = encoding.getProperEvent(e);
              getEventInfo(event).addDisablingAutomaton(aut);
              break;
            }
          }
        }
      }
    }
  }

  /*
   * Changes automata to automata that find more enabled events
   * Adds selfloops of every event to dump states,
   * adds in observation equivalent redundant transitions.
   */
  private AutomatonProxy createAutomatonForAlwaysEnabledEvents(final AutomatonProxy aut, final EventProxy AEEvent) throws OverflowException
  {
    final KindTranslator translator = getKindTranslator();
    int markingID = -1;
    final Collection<AutomatonProxy> collection = Collections.singletonList(aut);
    new EventEncoding(aut, translator);
    final EventEncoding encoding = new EventEncoding();
    for (final EventProxy event : aut.getEvents()) {
      final EventInfo info = getEventInfo(event);
      if (info == null) {
        final int code = encoding.addEvent(event, translator, (byte)0);
        if (event == getUsedDefaultMarking()) {
          markingID = code;
        }
      } else if (info.isLocal(collection)) {
        encoding.addSilentEvent(event);
      } else {
        encoding.addEvent(event, translator, (byte)0);
      }
    }

    final int AEEventCode = encoding.getEventCode(AEEvent);
    boolean automatonChanged = false;
    //transrel for finding dump states
    final ListBufferTransitionRelation transrel =
      new ListBufferTransitionRelation(aut, encoding,
                                       ListBufferTransitionRelation.CONFIG_SUCCESSORS);
    final ListBufferTransitionRelation transrelCopy =
      new ListBufferTransitionRelation(transrel,
                                       ListBufferTransitionRelation.CONFIG_PREDECESSORS);
    final TransitionIterator normalTransIterator = transrel.createSuccessorsReadOnlyIterator();

    for (int s = 0; s < transrel.getNumberOfStates(); s++) {
      if (transrel.isReachable(s)) {
        normalTransIterator.resetState(s);
        if (!(normalTransIterator.advance() || markingID < 0 || transrel
          .isMarked(s, markingID))) {
          //Then s is not a dump state
          automatonChanged |= transrel.addTransition(s, AEEventCode, s);
          //System.out.println("Transition added to dump state " + AEEvent.getName());
        }
      }
    }
    final TauClosure tauClosure = transrelCopy.createPredecessorsTauClosure(getTransitionLimit());
    final TransitionIterator tauTransIterator = tauClosure.createIterator();
    final TransitionIterator eventIterator = transrelCopy.createAllTransitionsReadOnlyIterator(AEEventCode);

    //Add OE redundant event
    //If this state has an outgoing AEEvent transition
    while(eventIterator.advance()) {
      final int s = eventIterator.getCurrentSourceState();
      tauTransIterator.resetState(s);
      //then add this transition to source states of incoming tau chains
      while(tauTransIterator.advance()){
        automatonChanged |= transrel.addTransition(tauTransIterator.getCurrentSourceState(), AEEventCode, eventIterator.getCurrentTargetState());
        //System.out.println("redundant event added " + AEEvent.getName());
      }
    }
    //If the automaton was not changed, don't create a new one.
    if(automatonChanged)
    return transrel.createAutomaton(getFactory(), encoding);
    else
      return aut;
  }


  //#########################################################################
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
    AbstractCompositionalModelVerifier.SelectionMethodFactory
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
  //# Inner Class SelectionHeuristicMaxLE
  private class SelectionHeuristicMaxLE
    extends AbstractNumericSelectionHeuristic<Candidate>
  {
    //#######################################################################
    //# Overrides for AbstractNumericSelectionHeuristic<Candidate>
    @Override
    public double getHeuristicValue(final Candidate candidate)
    {
      int alwaysEnabledEvents = 0;
      final List<AutomatonProxy> automataList = candidate.getAutomata();
      for (final EventProxy event : candidate.getOrderedEvents()) {
        final EnabledEventsEventInfo info = getEventInfo(event);
        if (info != null && // not a proposition
            info.getDisablingAutomata() != null &&
            automataList.containsAll(info.getDisablingAutomata())) {
          alwaysEnabledEvents++;
        }
      }
      return - (candidate.getLocalEventCount() + 0.5 * alwaysEnabledEvents) /
               candidate.getNumberOfEvents();
    }
  }


  //#########################################################################
  //# Inner Class SelectionHeuristicMinSE
  private class SelectionHeuristicMinSE
    extends AbstractNumericSelectionHeuristic<Candidate>
  {
    //#######################################################################
    //# Overrides for AbstractNumericSelectionHeuristic<Candidate>
    @Override
    public double getHeuristicValue(final Candidate candidate)
    {
      double product = 1.0;
      for (final AutomatonProxy aut : candidate.getAutomata()) {
        product *= aut.getStates().size();
      }
      final double totalEvents = candidate.getNumberOfEvents();
      final double localEvents = candidate.getLocalEventCount();
      int alwaysEnabledEvents = 0;
      final List<AutomatonProxy> automataList = candidate.getAutomata();
      for (final EventProxy event : candidate.getOrderedEvents()) {
        final EnabledEventsEventInfo info = getEventInfo(event);
        if (info != null && // not a proposition
            info.getDisablingAutomata() != null &&
            automataList.containsAll(info.getDisablingAutomata())) {
          alwaysEnabledEvents++;
        }
      }
      return product *
             (totalEvents - localEvents - 0.5 * alwaysEnabledEvents) /
             totalEvents;
    }
  }


  //########################################################################
  //# Inner Class EnabledEventsEventInfo
  static class EnabledEventsEventInfo extends EventInfo
  {
    //List of automata the disable this event
    private final Set<AutomatonProxy> mDisablingAutomata;
    //List of automata where this event is considered as being always enabled
    private final Set<AutomatonProxy> mAlwaysEnabledAutomata;

    private EnabledEventsEventInfo(final EventProxy event)
    {
      super(event);
      mDisablingAutomata = new THashSet<AutomatonProxy>();
      mAlwaysEnabledAutomata = new THashSet<AutomatonProxy>();
    }

    public Set<AutomatonProxy> getDisablingAutomata()
    {
      return mDisablingAutomata;
    }
    public Set<AutomatonProxy> getAlwaysEnabledAutomata()
    {
      return mAlwaysEnabledAutomata;
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

    private void addAlwaysEnabledAutomaton(final AutomatonProxy aut)
    {
      //If an event is always enabled from an automaton, add it to the list
      mAlwaysEnabledAutomata.add(aut);
    }

    @SuppressWarnings("unused")
    private AutomatonProxy getSingleDisablingAutomaton()
    {
      if (mDisablingAutomata.size() == 1)
        return mDisablingAutomata.iterator().next(); //If there is only one automata, return it.
      else
        return null;
    }

    /**
     * Returns true if the automaton passed in is the only automaton disabling
     * this event.
     */
    boolean isSingleDisablingAutomaton(final AutomatonProxy aut)
    {
      return mDisablingAutomata.size() == 0 ||
             (mDisablingAutomata.size() == 1 && mDisablingAutomata.contains(aut));
    }

    /**
     * Returns true if this automaton considers this event as always enabled
     * @param aut
     * @return
     */
    boolean isAlwaysEnabledAutomaton(final AutomatonProxy aut)
    {
      return mAlwaysEnabledAutomata.contains(aut);
    }

    boolean isAlwaysEnabledCandidate(final Candidate candidate)
    {
      boolean containsAlwaysEnabledAutomaton = false;
      for(final AutomatonProxy aut : candidate.getAutomata())
      {
        if(mAlwaysEnabledAutomata.contains(aut))
          containsAlwaysEnabledAutomaton = true;
      }
      return containsAlwaysEnabledAutomaton;
    }

    /**
     *
     * returns true if the candidate is the only disabling candidate
     */
    boolean isSingleDisablingCandidate(final Candidate candidate)
    {
      return candidate.getAutomata().containsAll(mDisablingAutomata);
    }

    @Override
    void removeAutomata(final Collection<AutomatonProxy> victims)
    {
      super.removeAutomata(victims);
      mDisablingAutomata.removeAll(victims);
      mAlwaysEnabledAutomata.removeAll(victims);
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
      if (mAlwaysEnabledAutomata.remove(oldAut)) {
        mAlwaysEnabledAutomata.add(newAut);
        result = true;
      }
      return result;
    }
  }


  //#########################################################################
  //# Data Members
  private int mEnabledEventSearchStateLimit = 0;
  private ModularControllabilityChecker mControllabilityChecker;


  //#########################################################################
  //# Class Constants
  /**
   * The preselecting method that considers every set of automata with at
   * least one local event as a candidate.
   */
  public static final PreselectingMethod MustLE = new PreselectingMethod("MustLE")
  {
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
   * proportion of local and always enabled events.
   */
  public static final SelectionMethod MaxLE = new SelectionMethod("MaxLE")
  {
    @Override
    AbstractSelectionHeuristic<Candidate> createBaseHeuristic
      (final AbstractCompositionalModelAnalyzer analyzer)
    {
      final EnabledEventsCompositionalConflictChecker everifier =
        (EnabledEventsCompositionalConflictChecker) analyzer;
      return everifier.new SelectionHeuristicMaxLE();
    }

    @Override
    AbstractSelectionHeuristic<Candidate> createChainHeuristic
      (final AbstractCompositionalModelAnalyzer analyzer)
    {
      return SelectionMethodFactory.createChainHeuristic
        (analyzer, MaxLE, MaxL, MaxC, MinE, MinS);
    }
  };

  /**
   * The selecting method that chooses the candidate with the minimum
   * estimated number of states in the synchronous product, while taking
   * into account always enabled events.
   */
  public static final SelectionMethod MinSE = new SelectionMethod("MinSE")
  {
    @Override
    AbstractSelectionHeuristic<Candidate> createBaseHeuristic
      (final AbstractCompositionalModelAnalyzer analyzer)
    {
      final EnabledEventsCompositionalConflictChecker everifier =
        (EnabledEventsCompositionalConflictChecker) analyzer;
      return everifier.new SelectionHeuristicMinSE();
    }

    @Override
    AbstractSelectionHeuristic<Candidate> createChainHeuristic
      (final AbstractCompositionalModelAnalyzer analyzer)
    {
      return SelectionMethodFactory.createChainHeuristic
        (analyzer, MinSE, MinS, MaxL, MaxC, MinE);
    }
  };

}
