//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# Copyright (C) 2004-2017 Robi Malik
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

package net.sourceforge.waters.model.analysis.des;

import gnu.trove.map.hash.TObjectIntHashMap;
import gnu.trove.set.hash.THashSet;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.sourceforge.waters.analysis.abstraction.ObservationEquivalenceTRSimplifier;
import net.sourceforge.waters.analysis.tr.EventEncoding;
import net.sourceforge.waters.analysis.tr.ListBufferTransitionRelation;
import net.sourceforge.waters.analysis.tr.StateEncoding;
import net.sourceforge.waters.analysis.tr.TRPartition;
import net.sourceforge.waters.model.analysis.AnalysisException;
import net.sourceforge.waters.model.analysis.IdenticalKindTranslator;
import net.sourceforge.waters.model.analysis.KindTranslator;
import net.sourceforge.waters.model.des.AutomatonProxy;
import net.sourceforge.waters.model.des.EventProxy;
import net.sourceforge.waters.model.des.ProductDESProxyFactory;
import net.sourceforge.waters.model.des.StateProxy;
import net.sourceforge.waters.model.des.TransitionProxy;
import net.sourceforge.waters.xsd.base.ComponentKind;


/**
 * <P>A debugging tool to check whether two nondeterministic automata
 * are isomorphic, bisimilar, or observation equivalent.</P>
 *
 * <P>This tester receives two {@link AutomatonProxy} objects as input
 * and checks whether they have bisimilar transition structures.
 * State names do not have to be the same, only transition
 * structures, initial state status, and markings must match. It is
 * configurable whether events are matched by object identity or by
 * name.</P>
 *
 * <P>This implementation merges the two automata into a single
 * {@link ListBufferTransitionRelation} and then uses a
 * {@link ObservationEquivalenceTRSimplifier} to find the coarsest bisimulation
 * relation. Afterwards it tests whether the two automata have matching
 * initial states, and for isomorphism is also tests whether all equivalence
 * classes have equal numbers of states for both input automata.</P>
 *
 * @author Robi Malik
 */

public class IsomorphismChecker
{

  //#########################################################################
  //# Constructors
  /**
   * Creates a new isomorphism checker.
   * @param  factory     Factory used for automaton creation during the check.
   * @param  matchNames  <CODE>true</CODE> if events are to be matched by name,
   *                     <CODE>false</CODE> if events are to be matched by
   *                     object identity.
   */
  public IsomorphismChecker(final ProductDESProxyFactory factory,
                            final boolean matchNames,
                            final boolean throwingExceptions)
  {
    mFactory = factory;
    mMatchingNames = matchNames;
    mThrowingExceptions = throwingExceptions;
  }


  //#########################################################################
  //# Configuration
  /**
   * Gets the <I>matching names</I> setting of this isomorphism checker.
   * @return <CODE>true</CODE> if events are matched by name,
   *         <CODE>false</CODE> if events are matched by object identity.
   */
  public boolean isMatchingNames()
  {
    return mMatchingNames;
  }

  /**
   * Sets the <I>matching names</I> setting of this isomorphism checker.
   * @param  matchNames  <CODE>true</CODE> if events are to be matched by name,
   *                     <CODE>false</CODE> if events are to be matched by
   *                     object identity.
   */
  public void setMatchingNames(final boolean matchNames)
  {
    mMatchingNames = matchNames;
  }
  /**
   * Gets the <I>throwing exceptions</I> setting of this isomorphism checker.
   * @return <CODE>true</CODE> if the method checkBisimulation throw exceptions,
   *         <CODE>false</CODE> if the method checkBisimulation does not throw exceptions
   */
  public boolean isThrowingExceptions()
  {
    return mThrowingExceptions;
  }

  /**
   * Sets the <I>throwing exceptions</I> setting of this isomorphism checker.
   * @param  throwingExceptions  <CODE>true</CODE> if the method checkBisimulation is to throw exceptions,
   *                     <CODE>false</CODE> if the method checkBisimulation is not to throw exceptions
   */
  public void setThrowingExceptions(final boolean throwingExceptions)
  {
    mThrowingExceptions = throwingExceptions;
  }


  //#########################################################################
  //# Invocation
  /**
   * Checks whether the two given automata are isomorphic.
   * @param  aut1   The first automaton to be compared.
   * @param  aut2   The second automaton to be compared.
   * @throws AnalysisException if the input automata are not isomorphic.
   */
  public void checkIsomorphism(final AutomatonProxy aut1,
                               final AutomatonProxy aut2)
    throws AnalysisException
  {
    if (aut1.getStates().size() != 0 || aut2.getStates().size() != 0) {
      setupEventMap(aut1, aut2);
      final KindTranslator translator = IdenticalKindTranslator.getInstance();
      final AutomatonProxy aut = createTestAutomaton(aut1, aut2, true);
      final EventEncoding eventEnc = new EventEncoding(aut, translator);
      final StateEncoding stateEnc = new StateEncoding(aut);
      final ListBufferTransitionRelation rel = new ListBufferTransitionRelation
        (aut, eventEnc, stateEnc,
         ListBufferTransitionRelation.CONFIG_PREDECESSORS);
      final ObservationEquivalenceTRSimplifier bisimulator =
        new ObservationEquivalenceTRSimplifier(rel);
      bisimulator.setEquivalence
        (ObservationEquivalenceTRSimplifier.Equivalence.BISIMULATION);
      bisimulator.setTransitionRemovalMode
        (ObservationEquivalenceTRSimplifier.TransitionRemoval.NONE);
      bisimulator.setAppliesPartitionAutomatically(false);
      final boolean result = bisimulator.run();
      if (!result) {
        throw new IsomorphismException
          ("Bisimulator did not identify any states!");
      }
      final TRPartition partition = bisimulator.getResultPartition();
      checkIsomorphismPartition(partition, rel, stateEnc);
    }
  }

  /**
   * Checks whether the two given automata are bisimilar.
   * @param  aut1   The first automaton to be compared.
   * @param  aut2   The second automaton to be compared.
   * @return <CODE>true</CODE> if the automata have been found to be
   *         bisimilar, <CODE>false</CODE> otherwise.
   * @throws AnalysisException if the input automata are not isomorphic.
   */
  public boolean checkBisimulation(final AutomatonProxy aut1,
                                   final AutomatonProxy aut2)
    throws AnalysisException
  {
    if (aut1.getStates().size() != 0 || aut2.getStates().size() != 0) {
      setupEventMap(aut1, aut2);
      final KindTranslator translator = IdenticalKindTranslator.getInstance();
      final AutomatonProxy aut = createTestAutomaton(aut1, aut2, false);
      final EventEncoding eventEnc = new EventEncoding(aut, translator);
      final StateEncoding stateEnc = new StateEncoding(aut);
      final ListBufferTransitionRelation rel = new ListBufferTransitionRelation
        (aut, eventEnc, stateEnc,
         ListBufferTransitionRelation.CONFIG_PREDECESSORS);
      final ObservationEquivalenceTRSimplifier bisimulator =
        new ObservationEquivalenceTRSimplifier(rel);
      bisimulator.setEquivalence
        (ObservationEquivalenceTRSimplifier.Equivalence.BISIMULATION);
      bisimulator.setTransitionRemovalMode
        (ObservationEquivalenceTRSimplifier.TransitionRemoval.NONE);
      final boolean result = bisimulator.run();
      if (!result) {
        if (mThrowingExceptions) {
          throw new IsomorphismException
            ("Bisimulator did not identify any states!");
        } else{
          return false;
        }
      }
      final TRPartition partition = bisimulator.getResultPartition();
      if (!checkBisimulationPartition(partition, rel, stateEnc)) {
        return false;
      }
    }
    return true;
  }

  /**
   * Checks whether the two given automata are observation equivalent.
   * @param  aut1   The first automaton to be compared.
   * @param  aut2   The second automaton to be compared.
   * @param  tau    Silent event used for observation equivalence.
   * @throws AnalysisException if the input automata are not isomorphic.
   */
  public void checkObservationEquivalence(final AutomatonProxy aut1,
                                          final AutomatonProxy aut2,
                                          final EventProxy tau)
    throws AnalysisException
  {
    if (aut1.getStates().size() != 0 || aut2.getStates().size() != 0) {
      setupEventMap(aut1, aut2);
      final KindTranslator translator = IdenticalKindTranslator.getInstance();
      final AutomatonProxy aut = createTestAutomaton(aut1, aut2, false);
      final EventEncoding eventEnc = new EventEncoding(aut, translator, tau);
      final StateEncoding stateEnc = new StateEncoding(aut);
      final ListBufferTransitionRelation rel = new ListBufferTransitionRelation
        (aut, eventEnc, stateEnc,
         ListBufferTransitionRelation.CONFIG_PREDECESSORS);
      final ObservationEquivalenceTRSimplifier bisimulator =
        new ObservationEquivalenceTRSimplifier(rel);
      final boolean result = bisimulator.run();
      bisimulator.setEquivalence
        (ObservationEquivalenceTRSimplifier.Equivalence.OBSERVATION_EQUIVALENCE);
      bisimulator.setTransitionRemovalMode
        (ObservationEquivalenceTRSimplifier.TransitionRemoval.NONE);
      if (!result) {
        throw new IsomorphismException
          ("Bisimulator did not identify any states!");
      }
      final TRPartition partition = bisimulator.getResultPartition();
      checkBisimulationPartition(partition, rel, stateEnc);
    }
  }


  //#########################################################################
  //# Auxiliary Methods
  private void setupEventMap(final AutomatonProxy target,
                             final AutomatonProxy source)
    throws EventNotFoundException
  {
    final Collection<EventProxy> events1 = target.getEvents();
    final Collection<EventProxy> events2 = source.getEvents();
    mSelfloops1 = new ArrayList<EventProxy>(events2.size());
    mExtraProperties1 = new ArrayList<EventProxy>(events2.size());
    mSelfloops2 = new ArrayList<EventProxy>(events1.size());
    mExtraProperties2 = new ArrayList<EventProxy>(events2.size());
    if (mMatchingNames) {
      final int numevents = events1.size();
      if (numevents > events2.size()) {
        setupEventMap(source, target);
        return;
      }
      final Map<String,EventProxy> nameMap =
        new HashMap<String,EventProxy>(numevents);
      for (final EventProxy event : events1) {
        final String name = event.getName();
        nameMap.put(name, event);
      }
      mEventMap = new HashMap<EventProxy,EventProxy>(numevents);
      final Collection<EventProxy> eset2 = new THashSet<EventProxy>(numevents);
      for (final EventProxy event2 : events2) {
        final String name = event2.getName();
        final EventProxy event1 = nameMap.get(name);
        if (event1 == null) {
          addSelfLoop(event2, mSelfloops1, mExtraProperties1);
        } else if (event1.getKind() != event2.getKind()) {
          throw new EventNotFoundException
            (target, name, event1.getKind(), true);
        } else {
          mEventMap.put(event2, event1);
          eset2.add(event1);
        }
      }
      for (final EventProxy event1 : events1) {
        if (!eset2.contains(event1)) {
          addSelfLoop(event1, mSelfloops2, mExtraProperties2);
        }
      }
    } else {
      final Collection<EventProxy> eset1 = new THashSet<EventProxy>(events1);
      for (final EventProxy event2 : events2) {
        if (!eset1.contains(event2)) {
          addSelfLoop(event2, mSelfloops1, mExtraProperties1);
        }
      }
      final Collection<EventProxy> eset2 = new THashSet<EventProxy>(events2);
      for (final EventProxy event1 : events1) {
        if (!eset2.contains(event1)) {
          addSelfLoop(event1, mSelfloops2, mExtraProperties2);
        }
      }
    }
  }

  private void addSelfLoop(final EventProxy event,
                           final Collection<EventProxy> properEvents,
                           final Collection<EventProxy> props)
  {
    switch (event.getKind()) {
    case CONTROLLABLE:
    case UNCONTROLLABLE:
      properEvents.add(event);
      break;
    case PROPOSITION:
      props.add(event);
      break;
    default:
      throw new IllegalArgumentException
        ("Unknown event kind " + event.getKind() + "!");
    }
  }

  private AutomatonProxy createTestAutomaton(final AutomatonProxy aut1,
                                             final AutomatonProxy aut2,
                                             final boolean iso)
    throws IsomorphismException
  {
    final Collection<EventProxy> events1 = aut1.getEvents();
    final Collection<EventProxy> events2 = aut2.getEvents();
    final Collection<EventProxy> events;
    if (!iso) {
      events = createUnionAlphabet(events1, events2);
    } else if (checkEventSetsEqual(events1, events2)) {
      events = events1;
    } else {
      return null;
    }
    final Collection<StateProxy> states1 = aut1.getStates();
    final Collection<StateProxy> states2 = aut2.getStates();
    final int numstates1 = states1.size();
    final int numstates2 = states2.size();
    if (iso && numstates1 != numstates2) {
      if (mThrowingExceptions) {
        throw new IsomorphismException("Different number of states!");
      } else {
        return null;
      }
    }
    final int numstates = numstates1 + numstates2;
    final Collection<StateProxy> states =
      new ArrayList<StateProxy>(numstates);
    final Map<StateProxy,StateProxy> stateMap =
      new HashMap<StateProxy,StateProxy>(numstates);

    final Collection<TransitionProxy> transitions1 = aut1.getTransitions();
    final Collection<TransitionProxy> transitions2 = aut2.getTransitions();
    final int numtrans1 =
      transitions1.size() + mSelfloops1.size() * states1.size();
    final int numtrans2 =
      transitions2.size() + mSelfloops2.size() * states2.size();
    if (iso && numtrans1 != numtrans2) {
      if (mThrowingExceptions) {
        throw new IsomorphismException("Different number of transitions!");
      } else {
        return null;
      }
    }
    final Collection<TransitionProxy> transitions =
      new ArrayList<TransitionProxy>(numtrans1 + numtrans2);

    mSplitMap = new TObjectIntHashMap<StateProxy>(numstates);
    for (final StateProxy state1 : states1) {
      final StateProxy renamed = getAltState(state1, 1);
      states.add(renamed);
      mSplitMap.put(renamed, 0);
      stateMap.put(state1, renamed);
      for (final EventProxy event : mSelfloops1) {
        final TransitionProxy selfloop =
          mFactory.createTransitionProxy(renamed, event, renamed);
        transitions.add(selfloop);
      }
    }
    for (final StateProxy state2 : states2) {
      final StateProxy renamed;
      if (mMatchingNames) {
        final Collection<EventProxy> props2 = state2.getPropositions();
        if (props2.isEmpty()) {
          renamed = getAltState(state2, 2);
        } else {
          final int numprops = props2.size();
          final Collection<EventProxy> props1 =
            new ArrayList<EventProxy>(numprops);
          for (final EventProxy prop2 : props2) {
            final EventProxy prop1 = getMappedEvent(prop2);
            props1.add(prop1);
          }
          props1.addAll(mExtraProperties2);
          final String name = getAltStateName(state2, 2);
          final boolean initial = state2.isInitial();
          renamed = mFactory.createStateProxy(name, initial, props1);
        }
      } else {
        renamed = getAltState(state2, 2);
      }
      states.add(renamed);
      stateMap.put(state2, renamed);
      mSplitMap.put(renamed, 1);
      for (final EventProxy event : mSelfloops2) {
        final TransitionProxy selfloop =
          mFactory.createTransitionProxy(renamed, event, renamed);
        transitions.add(selfloop);
      }
    }

    addAltTransitions(transitions1, transitions, stateMap);
    addAltTransitions(transitions2, transitions, stateMap);
    final String name1 = aut1.getName();
    final String name2 = aut2.getName();
    final String name = '{' + name1 + '=' + name2 + '}';
    return mFactory.createAutomatonProxy
      (name, ComponentKind.PLANT, events, states, transitions);
  }

  private boolean checkEventSetsEqual(final Collection<EventProxy> events1,
                                      final Collection<EventProxy> events2)
    throws IsomorphismException
  {
    if (events1.size() == events2.size()) {
      if (mMatchingNames) {
        final int size = events1.size();
        final Set<String> set = new THashSet<String>(size);
        for (final EventProxy event : events1) {
          set.add(event.getName());
        }
        for (final EventProxy event : events1) {
          if (!set.contains(event.getName())) {
            if (mThrowingExceptions) {
              throw new IsomorphismException("Event '" + event.getName() +
                                             "' not in both automata!");
            } else {
              return false;
            }
          }
        }
        return true;
      } else {
        final Set<EventProxy> set = new THashSet<EventProxy>(events1);
        if (set.containsAll(events2)) {
          return true;
        } else if (mThrowingExceptions) {
          for (final EventProxy event : events2) {
            if (!set.contains(event)) {
              throw new IsomorphismException("Event '" + event.getName() +
                                             "' not in both automata!");
            }
          }
        }
      }
    } else if (mThrowingExceptions) {
      throw new IsomorphismException("Different number of events!");
    }
    return false;
  }

  private Collection<EventProxy> createUnionAlphabet
    (final Collection<EventProxy> events1, final Collection<EventProxy> events2)
  {
    final int size = events1.size() + events2.size();
    final Set<EventProxy> set = new THashSet<EventProxy>(size);
    set.addAll(events1);
    final List<EventProxy> list = new ArrayList<EventProxy>(size);
    list.addAll(events1);
    for (final EventProxy event2 : events2) {
      if (set.add(event2)) {
        final EventProxy event1 = getMappedEvent(event2);
        list.add(event1);
      }
    }
    return list;
  }

  private void checkIsomorphismPartition(final TRPartition partition,
                                         final ListBufferTransitionRelation rel,
                                         final StateEncoding enc)
    throws IsomorphismException
  {
    // TODO Not a proper isomorphism check. Must also match outgoing
    // transitions for each state.
    final int[] count = new int[2];
    final int[] initCount = new int[2];
    for (final int[] clazz : partition.getClasses()) {
      if (clazz != null) {
        Arrays.fill(count, 0);
        Arrays.fill(initCount, 0);
        for (int i = 0; i < clazz.length; i++) {
          final int s = clazz[i];
          final StateProxy state = enc.getState(s);
          final int split = mSplitMap.get(state);
          count[split]++;
          if (state.isInitial()) {
            initCount[split]++;
          }
        }
        if (count[0] != count[1]) {
          throw new IsomorphismException
          ("Automata contain non-isomorphic states!");
        } else if (initCount[0] != initCount[1]) {
          throw new IsomorphismException
          ("Initial states do not match!");
        }
      }
    }
  }

  private boolean checkBisimulationPartition
    (final TRPartition partition,
     final ListBufferTransitionRelation rel,
     final StateEncoding enc)
    throws IsomorphismException
  {
    final boolean[] count = new boolean[2];
    final boolean[] initCount = new boolean[2];
    for (final int[] clazz : partition.getClasses()) {
      if (clazz != null) {
        Arrays.fill(count, false);
        Arrays.fill(initCount, false);
        for (int i = 0; i < clazz.length; i++) {
          final int s = clazz[i];
          final StateProxy state = enc.getState(s);
          final int split = mSplitMap.get(state);
          count[split] = true;
          if (state.isInitial()) {
            initCount[split] = true;
          }
        }
        if (count[0] != count[1]) {
          if (mThrowingExceptions) {
            throw new IsomorphismException
              ("Automata contain non-bisimilar states!");
          } else {
            return false;
          }
        } else if (initCount[0] != initCount[1]) {
          if (mThrowingExceptions) {
            throw new IsomorphismException("Initial states do not match!");
          } else {
            return false;
          }
        }
      }
    }
    return true;
  }

  private StateProxy getAltState(final StateProxy state, final int index)
  {
    final String name = getAltStateName(state, index);
    final boolean init = state.isInitial();
    final Collection<EventProxy> props = state.getPropositions();
    final Collection<EventProxy> extra =
      index == 1 ? mExtraProperties2 : mExtraProperties1;
    final Collection<EventProxy> allProps;
    if (extra.isEmpty()) {
      allProps = props;
    } else {
      final int numProps = props.size() + extra.size();
      allProps = new ArrayList<EventProxy>(numProps);
      allProps.addAll(props);
      allProps.addAll(extra);
    }
    return mFactory.createStateProxy(name, init, allProps);
  }

  private String getAltStateName(final StateProxy state, final int index)
  {
    return state.getName() + ":" + index;
  }

  private void addAltTransitions(final Collection<TransitionProxy> in,
                                 final Collection<TransitionProxy> out,
                                 final Map<StateProxy,StateProxy> stateMap)
  {
    for (final TransitionProxy trans2 : in) {
      final StateProxy source2 = trans2.getSource();
      final StateProxy source1 = stateMap.get(source2);
      final EventProxy event2 = trans2.getEvent();
      final EventProxy event1 = getMappedEvent(event2);
      final StateProxy target2 = trans2.getTarget();
      final StateProxy target1 = stateMap.get(target2);
      final TransitionProxy trans1 =
        mFactory.createTransitionProxy(source1, event1, target1);
      out.add(trans1);
    }
  }

  private EventProxy getMappedEvent(final EventProxy event)
  {
    if (mMatchingNames) {
      final EventProxy mapped = mEventMap.get(event);
      return mapped == null ? event : mapped;
    } else {
      return event;
    }
  }


  //#########################################################################
  //# Inner Class IsomorphismException
  private static class IsomorphismException extends AnalysisException
  {
    //#######################################################################
    //# Constructor
    private IsomorphismException(final String msg)
    {
      super(msg);
    }

    //#######################################################################
    //# Class Constants
    private static final long serialVersionUID = 1L;
  }


  //#########################################################################
  //# Data Members
  private final ProductDESProxyFactory mFactory;

  private boolean mMatchingNames;
  private boolean mThrowingExceptions;

  private Map<EventProxy,EventProxy> mEventMap;
  private Collection<EventProxy> mSelfloops1;
  private Collection<EventProxy> mExtraProperties1;
  private Collection<EventProxy> mSelfloops2;
  private Collection<EventProxy> mExtraProperties2;
  private TObjectIntHashMap<StateProxy> mSplitMap;

}
