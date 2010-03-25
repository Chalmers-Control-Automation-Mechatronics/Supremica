//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters Tests
//# PACKAGE: net.sourceforge.waters.model.analysis
//# CLASS:   IsomorphismTester
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.model.analysis;

import gnu.trove.TObjectIntHashMap;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import net.sourceforge.waters.analysis.op.ObserverProjectionBisimulator;
import net.sourceforge.waters.analysis.op.ObserverProjectionTransitionRelation;
import net.sourceforge.waters.model.des.AutomatonProxy;
import net.sourceforge.waters.model.des.EventProxy;
import net.sourceforge.waters.model.des.ProductDESProxyFactory;
import net.sourceforge.waters.model.des.StateProxy;
import net.sourceforge.waters.model.des.TransitionProxy;
import net.sourceforge.waters.xsd.base.ComponentKind;

/**
 * <P>A testing tool to check whether two nondeterministic automata
 * are isomorphic.</P>
 *
 * <P>This tester receives two {@link AutomatonProxy} objects as input
 * and checks whether they have isomorphic transition structures. For
 * isomorphism, state names do not have to be the same, only transition
 * structures, initial state status, and marking must match. It is
 * configurable whether events are matched by object identity or by
 * name.</P>
 *
 * <P>This implementation merges the two automata into a single
 * {@link ObserverProjectionTransitionRelation} and then uses a
 * {@link ObserverProjectionBisimulator} to find the coarsest bisimulation
 * relation. Afterwards it tests whether the two automata have matching
 * initial states, and whether all equivalence classes have equal numbers
 * of states for both input automata.</P>
 *
 * @author Robi Malik
 */
public class IsomorphismChecker
{

  //#########################################################################
  //# Constructors
  public IsomorphismChecker(final ProductDESProxyFactory factory,
                            final boolean matchNames)
  {
    mFactory = factory;
    mMatchingNames = matchNames;
  }


  //#########################################################################
  //# Configuration
  public boolean isMatchingNames()
  {
    return mMatchingNames;
  }

  public void setMatchingNames(final boolean matchNames)
  {
    mMatchingNames = matchNames;
  }


  //#########################################################################
  //# Invocation
  /**
   * Checks whether the two given automata are isomorphic.
   * @throws AnalysisException if the input automata are not isomorphic.
   */
  public void checkIsomorphism(final AutomatonProxy aut1,
                               final AutomatonProxy aut2)
    throws AnalysisException
  {
    setupEventMap(aut1, aut2);
    final AutomatonProxy aut = createTestAutomaton(aut1, aut2);
    final ObserverProjectionTransitionRelation rel =
      new ObserverProjectionTransitionRelation(aut);
    final ObserverProjectionBisimulator bisimulator =
      new ObserverProjectionBisimulator(rel);
    bisimulator.run();
    final Map<Integer,int[]> map = bisimulator.getStateClasses();
    final Collection<int[]> partition = map.values();
    checkPartition(partition, rel);
  }


  //#########################################################################
  //# Auxiliary Methods
  private void setupEventMap(final AutomatonProxy target,
                             final AutomatonProxy source)
    throws EventNotFoundException
  {
    if (mMatchingNames) {
      final Collection<EventProxy> events1 = target.getEvents();
      final Collection<EventProxy> events2 = source.getEvents();
      final int numevents = events1.size();
      if (numevents > events2.size()) {
        setupEventMap(source, target); // will throw exception
      }
      final Map<String,EventProxy> nameMap =
        new HashMap<String,EventProxy>(numevents);
      for (final EventProxy event : events1) {
        final String name = event.getName();
        nameMap.put(name, event);
      }
      mEventMap = new HashMap<EventProxy,EventProxy>(numevents);
      for (final EventProxy event2 : events2) {
        final String name = event2.getName();
        final EventProxy event1 = nameMap.get(name);
        if (event1 == null || event1.getKind() != event2.getKind()) {
          throw new EventNotFoundException
            (target, name, event1.getKind(), true);
        }
        mEventMap.put(event2, event1);
      }
    }
  }

  private AutomatonProxy createTestAutomaton(final AutomatonProxy aut1,
                                             final AutomatonProxy aut2)
    throws IsomorphismException
  {
    final Collection<EventProxy> events = aut1.getEvents();
    final Collection<StateProxy> states1 = aut1.getStates();
    final Collection<StateProxy> states2 = aut2.getStates();
    final int numstates1 = states1.size();
    final int numstates2 = states2.size();
    if (numstates1 != numstates2) {
      throw new IsomorphismException("Different number of states!");
    }
    final int numstates = numstates1 + numstates2;
    final Collection<StateProxy> states =
      new ArrayList<StateProxy>(numstates);
    final Map<StateProxy,StateProxy> stateMap =
      new HashMap<StateProxy,StateProxy>(numstates2);
    mSplitMap = new TObjectIntHashMap<StateProxy>(numstates);
    for (final StateProxy state1 : states1) {
      states.add(state1);
      mSplitMap.put(state1, 0);
    }
    for (final StateProxy state2 : states2) {
      if (mMatchingNames) {
        final Collection<EventProxy> props2 = state2.getPropositions();
        final StateProxy state1;
        if (props2.isEmpty()) {
          state1 = state2;
        } else {
          final int numprops = props2.size();
          final Collection<EventProxy> props1 =
            new ArrayList<EventProxy>(numprops);
          for (final EventProxy prop2 : props2) {
            final EventProxy prop1 = getMappedEvent(prop2);
            props1.add(prop1);
          }
          final String name = state2.getName();
          final boolean initial = state2.isInitial();
          state1 = mFactory.createStateProxy(name, initial, props1);
        }
        states.add(state1);
        stateMap.put(state2, state1);
        mSplitMap.put(state1, 1);
      } else {
        states.add(state2);
        mSplitMap.put(state2, 1);
      }
    }
    final Collection<TransitionProxy> transitions1 = aut1.getTransitions();
    final Collection<TransitionProxy> transitions2 = aut2.getTransitions();
    final int numtrans1 = transitions1.size();
    final int numtrans2 = transitions2.size();
    if (numtrans1 != numtrans2) {
      throw new IsomorphismException("Different number of transitions!");
    }
    final Collection<TransitionProxy> transitions =
      new ArrayList<TransitionProxy>(numtrans1 + numtrans2);
    transitions.addAll(transitions1);
    if (mMatchingNames) {
      for (final TransitionProxy trans2 : transitions2) {
        final StateProxy source2 = trans2.getSource();
        final StateProxy source1 = stateMap.get(source2);
        final EventProxy event2 = trans2.getEvent();
        final EventProxy event1 = getMappedEvent(event2);
        final StateProxy target2 = trans2.getTarget();
        final StateProxy target1 = stateMap.get(target2);
        final TransitionProxy trans1 =
          mFactory.createTransitionProxy(source1, event1, target1);
        transitions.add(trans1);
      }
    } else {
      transitions.addAll(transitions2);
    }
    final String name1 = aut1.getName();
    final String name2 = aut2.getName();
    final String name = '{' + name1 + '=' + name2 + '}';
    return mFactory.createAutomatonProxy
      (name, ComponentKind.PLANT, events, states, transitions);
  }

  private void checkPartition(final Collection<int[]> partition,
                              final ObserverProjectionTransitionRelation rel)
    throws IsomorphismException
  {
    final StateProxy[] origMap = rel.getOriginalIntToStateMap();
    final int[] count = new int[2];
    final int[] initCount = new int[2];
    for (final int[] clazz : partition) {
      Arrays.fill(count, 0);
      Arrays.fill(initCount, 0);
      for (int i = 0; i < clazz.length; i++) {
        final int s = clazz[i];
        final StateProxy state = origMap[s];
        final int split = mSplitMap.get(state);
        count[split]++;
        if (state.isInitial()) {
          initCount[split]++;
        }
      }
      if (count[0] != count[1]) {
        throw new IsomorphismException
          ("Automata contain non-bisimilar states!");
      } else if (initCount[0] != initCount[1]) {
        throw new IsomorphismException
          ("Initial states do not match!");
      }
    }
  }

  private EventProxy getMappedEvent(final EventProxy event)
  {
    if (mMatchingNames) {
      return mEventMap.get(event);
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

  private Map<EventProxy,EventProxy> mEventMap;
  private TObjectIntHashMap<StateProxy> mSplitMap;

}
