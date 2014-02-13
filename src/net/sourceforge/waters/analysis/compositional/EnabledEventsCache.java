//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters Analysis
//# PACKAGE: net.sourceforge.waters.analysis.compositional
//# CLASS:   EnabledEventsCache
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.analysis.compositional;

import gnu.trove.map.hash.TObjectByteHashMap;
import gnu.trove.map.hash.TObjectIntHashMap;
import gnu.trove.set.hash.THashSet;
import gnu.trove.set.hash.TIntHashSet;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import net.sourceforge.waters.analysis.tr.EventEncoding;
import net.sourceforge.waters.analysis.tr.ListBufferTransitionRelation;
import net.sourceforge.waters.analysis.tr.StateEncoding;
import net.sourceforge.waters.analysis.tr.TauClosure;
import net.sourceforge.waters.analysis.tr.TransitionIterator;
import net.sourceforge.waters.model.analysis.KindTranslator;
import net.sourceforge.waters.model.analysis.OverflowException;
import net.sourceforge.waters.model.des.AutomatonProxy;
import net.sourceforge.waters.model.des.EventProxy;
import net.sourceforge.waters.model.des.ProductDESProxyFactory;
import net.sourceforge.waters.model.des.StateProxy;
import net.sourceforge.waters.model.des.TransitionProxy;
import net.sourceforge.waters.xsd.base.EventKind;


/**
 * A tool to remember sets of always enabled events during compositional
 * conflict check.
 *
 * @author Colin Pilbrow, Robi Malik
 */

public class EnabledEventsCache
{

  //#########################################################################
  //# Constructors
  public EnabledEventsCache(final Candidate candidate,
                            final AutomatonProxy autToAbstract,
                            final EventEncoding autEncoding,
                            final Collection<AutomatonProxy> allAutomata,
                            final EventProxy omega,
                            final ProductDESProxyFactory factory,
                            final KindTranslator translator)
  {
    mFactory = factory;
    mKindTranslator = translator;
    mEventEncoding = new EventEncoding();
    for (int e = EventEncoding.NONTAU;
         e < autEncoding.getNumberOfProperEvents(); e++) {
      final EventProxy event = autEncoding.getProperEvent(e);
      mEventEncoding.addEvent(event, translator,
                              EventEncoding.STATUS_NONE);
    }
    mOmegaEvent = omega;
    mOmegaCode = mEventEncoding.addEvent(omega, translator,
                                         EventEncoding.STATUS_NONE);
    mTRInfo = new ArrayList<>(allAutomata.size() - 1);
    // Collect all automata that share events with autToAbstract.
    final List<AutomatonProxy> candidateAutomata = candidate.getAutomata();
    for (final AutomatonProxy aut : allAutomata) {
      if (!candidateAutomata.contains(aut)) {
        for (final EventProxy event : aut.getEvents()) {
          if (translator.getEventKind(event) != EventKind.PROPOSITION &&
              mEventEncoding.getEventCode(event) >= 0) {
            final TRInfo info = new TRInfo(aut);
            mTRInfo.add(info);
            break;
          }
        }
      }
    }
    // Determine which events are used in exactly one automaton.
    // Those will be added as silent events to the encoding
    final TObjectIntHashMap<EventProxy> eventsCounter =
      new TObjectIntHashMap<>();
    for (final TRInfo info : mTRInfo) {
      final AutomatonProxy aut = info.getAutomaton();
      for (final EventProxy event : aut.getEvents()) {
        if (translator.getEventKind(event) != EventKind.PROPOSITION) {
          // If we have not seen the event in previous automata, assign a count
          // of 1, otherwise add 1 to its count.
          eventsCounter.adjustOrPutValue(event, 1, 1);
        }
      }
    }
    // Events shared by the automaton to be abstracted do not need to be included
    for (final EventProxy event : autToAbstract.getEvents()) {
      eventsCounter.adjustOrPutValue(event, 1, 0);
    }
    for (final EventProxy event : eventsCounter.keySet()) {
      if (eventsCounter.get(event) == 1) {
        mEventEncoding.addSilentEvent(event);
      }
    }
  }


  //#########################################################################
  //# Configuration
  public int getTransitionLimit()
  {
    return mTransitionLimit;
  }

  public void setTransitionLimit(final int limit)
  {
    mTransitionLimit = limit;
  }


  //#########################################################################
  //# Simple Access
  public EventEncoding getEventEncoding()
  {
    return mEventEncoding;
  }


  //#########################################################################
  //# Cache Access
  /**
   * Returns whether at least one of the events in the set given event set is
   * enabled on all states in every other automaton in the system.
   * @param eventSet
   *          A set of encoded events.
   * @return <CODE>true</CODE> if the set is always enabled.
   */
  public boolean IsAlwaysEnabled(final TIntHashSet eventSet)
    throws OverflowException
  {
    //if(eventSet == null)
     // System.out.println("Null event set");

   // if(mEventSetCache == null)
    //  System.out.println("Null cache");

    // Check the eventSet cache to see if we have already tested this set.
    switch (mEventSetCache.get(eventSet)) {
    case ALWAYS_ENABLED:
      return true;
    case NOT_ALWAYS_ENABLED:
      return false;
    case VALUE_UNKNOWN:
      assert !eventSet.contains(EventEncoding.TAU) :
        "EnabledEventsCache does not support sets containing TAU --- " +
        "please check for TAU beforehand.";
      for (final TRInfo info : mTRInfo) {
        final AutomatonProxy aut = info.getAutomaton();
        // Only continue if ALL events from eventSet are in the alphabet
        // of the automaton to be tested
        int count = 0;
        for (final EventProxy event : aut.getEvents()) {
          if (mKindTranslator.getEventKind(event) != EventKind.PROPOSITION) {
            final int e = mEventEncoding.getEventCode(event);
            if (e > EventEncoding.TAU && eventSet.contains(e)) {
              count++;
            }
          }
        }
        if (count < eventSet.size()) {
          continue;
        }
        // Create transition relation if not yet available
        createTransitionRelation(info);
        // Test if every state has some event in eventSet enabled.
        final ListBufferTransitionRelation rel = info.getTransitionRelation();
        final int numStates = rel.getNumberOfStates();
        final TauClosure closure = info.getTauClosure();
        final TransitionIterator iter =
          closure.createPreEventClosureIterator();
        states:
        for (int s = 0; s < numStates; s++) {
          // If this state is a dump state then ignore it
          // (i.e., if it has no outgoing transition, and is not marked OMEGA)
          if (rel.isDeadlockState(s, mOmegaCode)) {
            continue states;
          }
          // Otherwise check whether it enables an event from the eventSet
          iter.resetState(s);
          while (iter.advance()) {
            final int e = iter.getCurrentEvent();
            if (eventSet.contains(e)) {
              continue states;
            }
          }
          // If this state has no transitions from the eventSet,
          // then the eventSet is not always enabled.
          mEventSetCache.put(eventSet, NOT_ALWAYS_ENABLED);
          return false;
        }
      }
      // Every state in every automaton has some transitions from the eventSet.
      mEventSetCache.put(eventSet, ALWAYS_ENABLED);
      return true;
    default:
      throw new IllegalStateException("Unexpected value in cache: " +
                                      mEventSetCache.get(eventSet) + "!");
    }
  }


  //#########################################################################
  //# Auxiliary Methods
  private void createTransitionRelation(final TRInfo info)
    throws OverflowException
  {

    if (info.getTransitionRelation() == null) {
      // Create a smaller version of the automaton with only relevant events
      final AutomatonProxy aut = info.getAutomaton();
      final Collection<StateProxy> states = aut.getStates();
      final boolean containsOmega = aut.getEvents().contains(mOmegaEvent);
      final Set<StateProxy> extraMarkedStates =
        containsOmega ? new THashSet<StateProxy>(states.size()) : null;
      final Collection<TransitionProxy> transitions = aut.getTransitions();
      final Collection<TransitionProxy> newTransitions =
        new ArrayList<>(transitions.size());
      for (final TransitionProxy trans : aut.getTransitions()) {
        // If the event is TAU or in the alphabet of the
        // automaton to be abstracted
        if (mEventEncoding.getEventCode(trans.getEvent()) >= 0) {
          // Then add this transition to the smaller automaton.
          newTransitions.add(trans);
        } else if (extraMarkedStates != null) {
          // Otherwise mark the state as a 'fantasy' state
          // to be marked OMEGA in the end
          extraMarkedStates.add(trans.getSource());
        }
      }
      // Create smaller automaton using the event set of the automaton to
      // be abstracted, the same state set, and the reduced transitions.
      final Collection<EventProxy> newEvents = mEventEncoding.getEvents();
      final AutomatonProxy newAut =
        mFactory.createAutomatonProxy(aut.getName(), aut.getKind(),
                                      newEvents, aut.getStates(),
                                      newTransitions);
      // Create transition relation
      final byte status = containsOmega ?
        EventEncoding.STATUS_NONE : EventEncoding.STATUS_UNUSED;
      mEventEncoding.setPropositionStatus(mOmegaCode, status);
      final StateEncoding stateEncoding = new StateEncoding(newAut);
      final ListBufferTransitionRelation newRel =
        new ListBufferTransitionRelation(newAut,
                                         mEventEncoding,
                                         stateEncoding,
                                         ListBufferTransitionRelation.CONFIG_SUCCESSORS);
      // Mark each 'fantasy' state in the transition relation OMEGA.
      if (extraMarkedStates != null) {
        for (final StateProxy state : extraMarkedStates) {
          final int s = stateEncoding.getStateCode(state);
          newRel.setMarked(s, mOmegaCode, true);
        }
      }
      // Store the transition relation
      info.setTransitionRelation(newRel, mTransitionLimit);
    }
  }


  //#########################################################################
  //# Inner Class TRInfo
  /**
   * An information record that stores automata with associated transition
   * relations and tau closures.
   */
  private static class TRInfo
  {
    //#######################################################################
    //# Constructor
    private TRInfo(final AutomatonProxy aut)
    {
      mAutomaton = aut;
    }

    //#######################################################################
    //# Simple Access
    private AutomatonProxy getAutomaton()
    {
      return mAutomaton;
    }

    private ListBufferTransitionRelation getTransitionRelation()
    {
      return mTransitionRelation;
    }

    private TauClosure getTauClosure()
    {
      return mTauClosure;
    }

    void setTransitionRelation(final ListBufferTransitionRelation rel,
                               final int limit)
    {
      mTransitionRelation = rel;
      mTauClosure = rel.createSuccessorsTauClosure(limit);
    }

    //#######################################################################
    //# Data Members
    private final AutomatonProxy mAutomaton;
    private ListBufferTransitionRelation mTransitionRelation;
    private TauClosure mTauClosure;
  }


  //#########################################################################
  //# Data Members
  private int mTransitionLimit = Integer.MAX_VALUE;

  private final ProductDESProxyFactory mFactory;
  private final KindTranslator mKindTranslator;
  private final EventEncoding mEventEncoding;
  private final EventProxy mOmegaEvent;
  private final int mOmegaCode;
  private final List<TRInfo> mTRInfo;

  private final TObjectByteHashMap<TIntHashSet> mEventSetCache = new TObjectByteHashMap<TIntHashSet>();


  //#########################################################################
  //# Class Constants
  private static final byte VALUE_UNKNOWN = 0;
  private static final byte ALWAYS_ENABLED = 1;
  private static final byte NOT_ALWAYS_ENABLED = 2;

}
