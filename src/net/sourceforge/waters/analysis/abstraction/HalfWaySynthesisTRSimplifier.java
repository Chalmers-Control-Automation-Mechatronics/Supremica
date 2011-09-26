//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters Analysis
//# PACKAGE: net.sourceforge.waters.analysis.abstraction
//# CLASS:   HalfWaySynthesisTRSimplifier
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.analysis.abstraction;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.Collection;

import gnu.trove.TIntStack;

import net.sourceforge.waters.analysis.tr.EventEncoding;
import net.sourceforge.waters.analysis.tr.ListBufferTransitionRelation;
import net.sourceforge.waters.analysis.tr.TransitionIterator;
import net.sourceforge.waters.model.analysis.AbortException;
import net.sourceforge.waters.model.analysis.AnalysisException;
import net.sourceforge.waters.model.analysis.KindTranslator;
import net.sourceforge.waters.model.des.AutomatonProxy;
import net.sourceforge.waters.model.des.EventProxy;
import net.sourceforge.waters.model.des.ProductDESProxyFactory;
import net.sourceforge.waters.model.des.StateProxy;
import net.sourceforge.waters.model.des.TransitionProxy;
import net.sourceforge.waters.xsd.base.ComponentKind;
import net.sourceforge.waters.xsd.base.EventKind;


/**
 * Transition relation simplifier that implements halfway synthesis.
 *
 * @author Robi Malik, Sahar Mohajerani
 */

public class HalfWaySynthesisTRSimplifier
  extends AbstractMarkingTRSimplifier
{

  //#########################################################################
  //# Static Invocation
  public static AutomatonProxy synthesise(final AutomatonProxy automaton,
                                          final EventProxy marking,
                                          final ProductDESProxyFactory factory,
                                          final KindTranslator translator)
    throws AnalysisException
  {
    final Collection<EventProxy> events = automaton.getEvents();
    final int numEvents = events.size();
    final Collection<EventProxy> uncontrollable =
      new ArrayList<EventProxy>(numEvents);
    final Collection<EventProxy> controllable =
      new ArrayList<EventProxy>(numEvents);
    for (final EventProxy event : events) {
      if (translator.getEventKind(event) == EventKind.UNCONTROLLABLE) {
        uncontrollable.add(event);
      } else {
        controllable.add(event);
      }
    }
    final Collection<EventProxy> orderedEvents =
      new ArrayList<EventProxy>(numEvents);
    orderedEvents.addAll(uncontrollable);
    orderedEvents.addAll(controllable);
    final EventEncoding encoding =
      new EventEncoding(orderedEvents, translator);
    final ListBufferTransitionRelation rel = new ListBufferTransitionRelation
      (automaton, encoding, ListBufferTransitionRelation.CONFIG_PREDECESSORS);
    final HalfWaySynthesisTRSimplifier synthesis =
      new HalfWaySynthesisTRSimplifier(rel);
    synthesis.setLastLocalControllableEvent(numEvents);
    synthesis.setLastLocalUncontrollableEvent(uncontrollable.size());
    synthesis.setLastSharedUncontrollableEvent(numEvents);
    final int defaultID = encoding.getEventCode(marking);
    synthesis.setDefaultMarkingID(defaultID);
    final boolean change = synthesis.run();
    final String name = "sup:" + automaton.getName();
    if (change) {
      rel.setName(name);
      rel.setKind(ComponentKind.SUPERVISOR);
      return rel.createAutomaton(factory, encoding);
    } else {
      final Collection <StateProxy> states = automaton.getStates();
      final Collection <TransitionProxy> transitions =
        automaton.getTransitions();
      return
        factory.createAutomatonProxy(name, ComponentKind.SUPERVISOR,
                                     events, states, transitions);
    }
  }


  //#########################################################################
  //# Constructors
  public HalfWaySynthesisTRSimplifier()
  {
  }

  public HalfWaySynthesisTRSimplifier(final ListBufferTransitionRelation rel)
  {
    super(rel);
  }


  //#########################################################################
  //# Interface net.sourceforge.waters.analysis.abstraction.TransitionRelationSimplifier
  @Override
  public int getPreferredInputConfiguration()
  {
    return ListBufferTransitionRelation.CONFIG_PREDECESSORS;
  }

  @Override
  public boolean isObservationEquivalentAbstraction()
  {
    return false;
  }


  /**
   * Sets the code of the last local uncontrollable event. Events are encoded
   * such that all local events appear before all shared events, and all
   * uncontrollable local events appear before controllable local events. The
   * tau event code ({@link EventEncoding#TAU} is not used. Therefore, the
   * range of uncontrollable local events is from {@link EventEncoding#NONTAU}
   * to {@link #getLastLocalUncontrollableEvent()} inclusive.
   */
  public void setLastLocalUncontrollableEvent(final int event)
  {
    mLastLocalUncontrollableEvent = event;
  }

  /**
   * Gets the code of the last local uncontrollable event.
   *
   * @see #setLastLocalUncontrollableEvent(int) setLastLocalUncontrollableEvent()
   */
  public int getLastLocalUncontrollableEvent()
  {
    return mLastLocalUncontrollableEvent;
  }

  /**
   * Sets the code of the last local controllable event. Events are encoded
   * such that all local events appear before all shared events, and all
   * uncontrollable local events appear before controllable local events.
   * Therefore, the range of controllable local events is from
   * {@link #getLastLocalUncontrollableEvent()}+1 to
   * {@link #getLastLocalControllableEvent()} inclusive.
   */
  public void setLastLocalControllableEvent(final int event)
  {
    mLastLocalControllableEvent = event;
  }

  /**
   * Gets the code of the last local controllable event.
   *
   * @see #setLastLocalControllableEvent(int) setLastLocalControllableEvent()
   */
  public int getLastLocalControllableEvent()
  {
    return mLastLocalControllableEvent;
  }

  /**
   * Sets the code of the last shared uncontrollable event. Events are encoded
   * such that all local events appear before all shared events, and all
   * uncontrollable local events appear before controllable events.
   * Therefore, the range of uncontrollable shared events is from
   * {@link #getLastLocalControllableEvent()}+1 to
   * {@link #getLastSharedUncontrollableEvent()} inclusive.
   */
  public void setLastSharedUncontrollableEvent(final int event)
  {
    mLastSharedUncontrollableEvent = event;
  }

  /**
   * Gets the code of the last shared uncontrollable event.
   *
   * @see #setLastSharedUncontrollableEvent(int) setLastSharedUncontrollableEvent()
   */
  public int getLastSharedUncontrollableEvent()
  {
    return mLastSharedUncontrollableEvent;
  }

  public ListBufferTransitionRelation getDistinguisher(){
    return mDistinguisher;
  }


  //#########################################################################
  //# Overrides for net.sourceforge.waters.analysis.abstraction.AbstractTRSimplifier
  @Override
  public void reset(){
    super.reset();
    mDistinguisher = null;
  }

  @Override
  protected void setUp() throws AnalysisException{
    super.setUp();
    mDistinguisher = null;
  }

  @Override
  protected boolean runSimplifier()
  throws AnalysisException
  {
    final int defaultID = getDefaultMarkingID();
    if (defaultID < 0){
      return false;
    }
    final ListBufferTransitionRelation rel = getTransitionRelation();
    final int numStates = rel.getNumberOfStates();
    boolean hasAdded;
    BitSet badStates = new BitSet(numStates);
    do {
      final BitSet newBadStates = new BitSet(numStates);
      findCoreachableStates(newBadStates, badStates);
      newBadStates.flip(0, numStates);
      badStates = newBadStates;
      hasAdded = findMoreBadStates(badStates);
    } while (hasAdded);

    final int dumpState = badStates.nextSetBit(0);
    if (dumpState < 0) {
      return false;
    }

    final TransitionIterator iter = rel.createPredecessorsModifyingIterator();
    boolean dumpStateUsed = false;
    boolean changed = false;
    boolean addDistinguisher = false;
    for (int state = badStates.nextSetBit(0); state >= 0;
         state = badStates.nextSetBit(state+1)) {
      if(rel.isReachable(state)) {
        iter.resetState(state);
        while (iter.advance()) {
          final int source = iter.getCurrentSourceState();
          final int event = iter.getCurrentEvent();
          if (badStates.get(source)) {
            iter.remove();
            changed = true;
          } else if (mLastLocalControllableEvent < event &&
            event <= mLastSharedUncontrollableEvent) {
            // shared uncontrollable
            if (state != dumpState) {
              iter.remove();
              rel.addTransition(source, event, dumpState);
              changed = true;
            }
            dumpStateUsed = true;
          } else {
            // local or shared controllable
            // (cannot be local uncontrollable, other source would be bad)
            iter.remove();
            changed = true;
            addDistinguisher = true;
          }
        }
        if (state != dumpState) {
          rel.setReachable(state, false);
          changed = true;
        }
      }
    }
    rel.reconfigure(ListBufferTransitionRelation.CONFIG_SUCCESSORS);
    if (dumpStateUsed) {
      changed |= rel.removeOutgoingTransitions(dumpState);
    } else {
      rel.setReachable(dumpState, false);
      changed = true;
    }
    changed |= rel.checkReachability();
    changed |= rel.removeProperSelfLoopEvents();

    if(addDistinguisher){
      mDistinguisher = new ListBufferTransitionRelation
          (rel, ListBufferTransitionRelation.CONFIG_SUCCESSORS);
      if (dumpStateUsed) {
        mDistinguisher.setReachable(dumpState, false);
      }
    }
    return changed;
  }

  @Override
  protected void applyResultPartition()
  throws AnalysisException
  {
    super.applyResultPartition();
    final ListBufferTransitionRelation rel = getTransitionRelation();
    rel.removeUnreachableTransitions();
    rel.removeTauSelfLoops();
    rel.removeProperSelfLoopEvents();
  }

  private void findCoreachableStates(final BitSet coreachable,
                                     final BitSet badStates)
    throws AbortException
  {
    final ListBufferTransitionRelation rel = getTransitionRelation();
    final TransitionIterator iter = rel.createPredecessorsReadOnlyIterator();
    final int defaultID = getDefaultMarkingID();
    final int numStates = rel.getNumberOfStates();
    final TIntStack unvisited = new TIntStack();
    // Creates a hash set of all states which can reach a marked state.
    for (int sourceID = 0; sourceID < numStates; sourceID++) {
      if (rel.isMarked(sourceID, defaultID) && rel.isReachable(sourceID)
        && !badStates.get(sourceID)
          && ! coreachable.get(sourceID)) {
        checkAbort();
        coreachable.set(sourceID);
        unvisited.push(sourceID);
        while (unvisited.size() > 0) {
          final int newSource = unvisited.pop();
          iter.resetState(newSource);
          while (iter.advance()) {
            final int predID = iter.getCurrentSourceState();
            if (rel.isReachable(predID) && !badStates.get(predID)
              && !coreachable.get(predID)) {
              coreachable.set(predID);
              unvisited.push(predID);
            }
          }
        }
      }
    }
  }

  private boolean findMoreBadStates(final BitSet badStates)
    throws AbortException
  {
    boolean hasAdded = false;
    final BitSet oldBadStates = (BitSet) badStates.clone();
    final ListBufferTransitionRelation rel = getTransitionRelation();
    final TransitionIterator iter = rel.createPredecessorsReadOnlyIterator();
    iter.resetEvents(EventEncoding.NONTAU, mLastLocalUncontrollableEvent);
    final TIntStack unvisited = new TIntStack();
    for (int state = oldBadStates.nextSetBit(0); state >= 0;
         state = oldBadStates.nextSetBit(state+1)) {
      unvisited.push(state);
      while (unvisited.size()>0) {
        final int current = unvisited.pop();
        iter.resetState(current);
        while(iter.advance()){
          final int source = iter.getCurrentSourceState();
          if(rel.isReachable(source) && !badStates.get(source)){
            hasAdded = true;
            badStates.set(source);
            unvisited.push(source);
          }
        }
      }
    }
    return hasAdded;
  }


  //#########################################################################
  //# Data Members
  private int mLastLocalUncontrollableEvent;
  private int mLastLocalControllableEvent;
  private int mLastSharedUncontrollableEvent;

  private ListBufferTransitionRelation mDistinguisher;
}
