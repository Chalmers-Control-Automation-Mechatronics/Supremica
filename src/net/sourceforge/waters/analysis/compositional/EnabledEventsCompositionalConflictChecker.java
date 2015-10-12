//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# Copyright (C) 2004-2015 Robi Malik
//###########################################################################
//# This file is part of Waters.
//# Waters is free software: you can redistribute it and/or modify it under
//# the terms of the GNU General Public License as published by the Free
//# Software Foundation, either version 2 of the License, or (at your option)
//# any later version.
//# Waters is distributed in the hope that it will be useful, but WITHOUT ANY
//# WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
//# FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
//# details.
//# You should have received a copy of the GNU General Public License along
//# with Waters. If not, see <http://www.gnu.org/licenses/>.
//#
//# Linking Waters statically or dynamically with other modules is making a
//# combined work based on Waters. Thus, the terms and conditions of the GNU
//# General Public License cover the whole combination.
//# In addition, as a special exception, the copyright holders of Waters give
//# you permission to combine Waters with code included in the standard
//# release of Supremica under the Supremica Software License Agreement (or
//# modified versions of such code, with unchanged license). You may copy and
//# distribute such a system following the terms of the GNU GPL for Waters and
//# the licenses of the other code concerned.
//# Note that people who make modified versions of Waters are not obligated to
//# grant this special exception for their modified versions; it is their
//# choice whether to do so. The GNU General Public License gives permission
//# to release a modified version without this exception; this exception also
//# makes it possible to release a modified version which carries forward this
//# exception.
//###########################################################################

package net.sourceforge.waters.analysis.compositional;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import net.sourceforge.waters.analysis.abstraction.SpecialEventsFinder;
import net.sourceforge.waters.analysis.abstraction.TauLoopRemovalTRSimplifier;
import net.sourceforge.waters.analysis.modular.ModularControllabilityChecker;
import net.sourceforge.waters.analysis.tr.EventEncoding;
import net.sourceforge.waters.analysis.tr.EventStatus;
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

import gnu.trove.set.hash.THashSet;


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
   * @param abstractionCreator
   *          Factory to define the abstraction sequence to be used.
   */
  public EnabledEventsCompositionalConflictChecker(final ProductDESProxy model,
                                                   final EventProxy marking,
                                                   final ProductDESProxyFactory factory,
                                                   final AbstractionProcedureCreator abstractionCreator)
  {
    super(model, marking, factory, abstractionCreator,
          new PreselectingMethodFactory());
  }


  //#########################################################################
  //# Configuration
  @Override
  public CompositionalSelectionHeuristicFactory getSelectionHeuristicFactory()
  {
    return EnabledEventsSelectionHeuristicFactory.getInstance();
  }

  /**
   * Sets the state limit for the language inclusion check to find
   * additional always enabled events.
   * @param  limit   Maximum number of states in language inclusion check,
   *                 or <CODE>0</CODE> to disable this search.
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
    throws AnalysisException
  {
    super.initialiseEventsToAutomata();
    for (final AutomatonProxy aut : getCurrentAutomata()) {
      updateAlwaysEnabledStatus(aut, AlwaysEnabledMode.INITIAL);
    }
  }

  @Override
  protected void updateEventsToAutomata
    (final AutomatonProxy autToAdd,
     final List<AutomatonProxy> autToRemove)
    throws AnalysisException
  {
    super.updateEventsToAutomata(autToAdd, autToRemove);
    updateAlwaysEnabledStatus(autToAdd, AlwaysEnabledMode.SIMPLIFIED);
  }

  @Override
  protected void addEventsToAllAutomata()
    throws AnalysisException
  {
    super.addEventsToAllAutomata();
    for (final AutomatonProxy aut : getCurrentAutomata()) {
      updateAlwaysEnabledStatus(aut, AlwaysEnabledMode.TAU_LOOP_FREE);
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
  private void updateAlwaysEnabledStatus(final AutomatonProxy aut,
                                         final AlwaysEnabledMode mode)
    throws AnalysisException
  {
    final Set<EventProxy> alwaysEnabledEvents;
    switch (mode) {
    case INITIAL:
      alwaysEnabledEvents = findAlwaysEnabledEvents(aut, false);
      break;
    case TAU_LOOP_FREE:
      alwaysEnabledEvents = findAlwaysEnabledEvents(aut, true);
      break;
    case SIMPLIFIED:
      final EnabledEventsThreeStepConflictEquivalenceAbstractionProcedure proc =
        (EnabledEventsThreeStepConflictEquivalenceAbstractionProcedure)
        getAbstractionProcedure();
      alwaysEnabledEvents = proc.getAlwaysEnabledEvents();
      break;
    default:
      throw new IllegalArgumentException
        ("Unknown always-enabled mode " + mode + "!");
    }
    final KindTranslator translator = getKindTranslator();
    for (final EventProxy event : aut.getEvents()) {
      if (translator.getEventKind(event) != EventKind.PROPOSITION &&
          !alwaysEnabledEvents.contains(event)) {
        final EnabledEventsEventInfo info = getEventInfo(event);
        info.addDisablingAutomaton(aut);
      }
    }
  }

  private Set<EventProxy> findAlwaysEnabledEvents
    (final AutomatonProxy aut, boolean tauLoopFree)
    throws AnalysisException
  {
    final KindTranslator translator = getKindTranslator();
    final Collection<AutomatonProxy> singletonAut =
      Collections.singletonList(aut);
    final EventEncoding encoding = new EventEncoding();
    final EventProxy defaultMarking = getUsedDefaultMarking();
    int defaultID = -1;
    for (final EventProxy event : aut.getEvents()) {
      if (event == defaultMarking) {
        defaultID = encoding.addEvent(event, translator, 0);
      } else if (translator.getEventKind(event) != EventKind.PROPOSITION) {
        final EventInfo info = getEventInfo(event);
        if (info.isLocal(singletonAut)) {
          encoding.addSilentEvent(event);
        } else {
          encoding.addEvent(event, translator, 0);
        }
      }
    }
    final ListBufferTransitionRelation rel =
      new ListBufferTransitionRelation(aut, encoding,
                                       ListBufferTransitionRelation.CONFIG_SUCCESSORS);
    tauLoopFree |= encoding.getProperEvent(EventEncoding.TAU) == null;
    if (!tauLoopFree) {
      final TauLoopRemovalTRSimplifier loopRemover =
        new TauLoopRemovalTRSimplifier(rel);
      tauLoopFree = !loopRemover.run();
    }
    final SpecialEventsFinder finder = new SpecialEventsFinder(rel);
    finder.setAlwaysEnabledEventsDetected(true);
    finder.setDefaultMarkingID(defaultID);
    finder.run();
    final byte[] computedStatus = finder.getComputedEventStatus();
    final Set<EventProxy> alwaysEnabledEvents =
      new THashSet<>(computedStatus.length);
    for (int e = 0; e < computedStatus.length; e++) {
      if (EventStatus.isAlwaysEnabledEvent(computedStatus[e])) {
        final EventProxy event = encoding.getProperEvent(e);
        alwaysEnabledEvents.add(event);
      }
    }
    if (!tauLoopFree) {
      for (int e = EventEncoding.NONTAU; e < rel.getNumberOfProperEvents(); e++) {
        final byte status = rel.getProperEventStatus(e);
        if (!EventStatus.isUsedEvent(status)) {
          final EventProxy event = encoding.getProperEvent(e);
          alwaysEnabledEvents.add(event);
        }
      }
    }
    return alwaysEnabledEvents;
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
      register(MustSp);
    }
  }


  //#########################################################################
  //# Inner Class HeuristicMustLE
  private class HeuristicMustLE implements PreselectingHeuristic
  {

    //#######################################################################
    //# Interface PreselectingHeuristic
    @Override
    public List<Candidate> findCandidates()
    {
      final List<Candidate> candidates = new LinkedList<Candidate>(); //Create collection to store candidates to return
      final Collection<EventProxy> events = getCurrentEvents(); //Get all events and put into a collection

      final Collection<List<AutomatonProxy>> found = //This is the automaton combinations we have already found
        new THashSet<List<AutomatonProxy>>(events.size());

      for (final EventProxy event : events) //For all events
      {
        final EnabledEventsEventInfo info = getEventInfo(event); //Get the event info
        assert info.getNumberOfAutomata() > 0; //Make sure that this event is being used by an automaton
        if (info.getNumberOfAutomata() > 1) { //If the event is being used by more than one automaton
          final List<AutomatonProxy> list;
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
  //# Inner Class HeuristicMustSp
  private class HeuristicMustSp implements PreselectingHeuristic
  {

    //#######################################################################
    //# Interface PreselectingHeuristic
    @Override
    public List<Candidate> findCandidates()
    {
      final List<Candidate> candidates = new LinkedList<Candidate>(); //Create collection to store candidates to return
      final Collection<EventProxy> events = getCurrentEvents(); //Get all events and put into a collection
      final Collection<List<AutomatonProxy>> found = //This is the automaton combinations we have already found
        new THashSet<List<AutomatonProxy>>(events.size());
      for (final EventProxy event : events) {
        final EnabledEventsEventInfo info = getEventInfo(event); //Get the event info
        final int numAutomata = info.getNumberOfAutomata();
        assert numAutomata > 0; //Make sure that this event is being used by an automaton
        if (numAutomata > 1) { //If the event is being used by more than one automaton
          boolean special = false;
          final int numDisabling = info.getDisablingAutomata().size();
          if (numDisabling > 1 && numDisabling < numAutomata) {
            // Create candidate consisting of all automata disabling this event
            final List<AutomatonProxy> automata = info.getDisablingAutomataList();
            addCandidate(automata, found, candidates);
            special = true;
          }
          final int numNonSelfloop = info.getNumberOfNonSelfloopAutomata();
          if (numNonSelfloop > 1 && numNonSelfloop < numAutomata) {
            // Create candidate consisting of all automata for which this
            // event is not selfloop-only
            final List<AutomatonProxy> automata = info.getNonSelfloopAutomataList();
            addCandidate(automata, found, candidates);
            special = true;
          }
          if (!special) {
            // If no candidate has been created above, create a candidate
            // consisting of all automata using this event.
            final List<AutomatonProxy> automata = info.getSortedAutomataList();
            addCandidate(automata, found, candidates);
            special = true;
          }
        }
      }
      return candidates;
    }

    //#######################################################################
    //# Auxiliary Methods
    private void addCandidate(final List<AutomatonProxy> automata,
                              final Collection<List<AutomatonProxy>> found,
                              final List<Candidate> candidates)
    {
      if (isPermissibleCandidate(automata) && found.add(automata)) {
        final Set<EventProxy> localEvents = identifyLocalEvents(automata);
        final Candidate candidate = new Candidate(automata, localEvents);
        candidates.add(candidate);
      }
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
     * Returns true if this automaton considers this event as always enabled.
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
  //# Enumeration AlwaysEnabledMode
  private static enum AlwaysEnabledMode
  {
    /**
     * Adding an input automaton that may have tau loops.
     */
    INITIAL,
    /**
     * Adding an input automaton known to be tau-loop free
     * (when re-introducing a deferred subsystem).
     */
    TAU_LOOP_FREE,
    /**
     * Adding an automaton obtained by simplification.
     * The always enabled status is available from the abstraction procedure.
     */
    SIMPLIFIED
  }


  //#########################################################################
  //# Data Members
  private int mEnabledEventSearchStateLimit = 0;
  private ModularControllabilityChecker mControllabilityChecker;


  //#########################################################################
  //# Class Constants
  /**
   * The preselecting method that considers every set of automata with at
   * least one always enabled event as a candidate.
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
   * The preselecting method that considers every set of automata with at
   * least one always enabled or selfloop-only event as a candidate.
   */
  public static final PreselectingMethod MustSp = new PreselectingMethod("MustSp")
  {
    @Override
    PreselectingHeuristic createHeuristic(final AbstractCompositionalModelAnalyzer verifier)
    {
      final EnabledEventsCompositionalConflictChecker everifier =
        (EnabledEventsCompositionalConflictChecker) verifier;
      return everifier.new HeuristicMustSp();
    }
  };

}
