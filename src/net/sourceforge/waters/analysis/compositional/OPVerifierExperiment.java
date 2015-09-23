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

import gnu.trove.map.custom_hash.TObjectIntCustomHashMap;
import gnu.trove.map.hash.TIntObjectHashMap;
import gnu.trove.map.hash.TLongObjectHashMap;
import gnu.trove.map.hash.TObjectIntHashMap;
import gnu.trove.set.hash.THashSet;
import gnu.trove.set.hash.TIntHashSet;
import gnu.trove.strategy.HashingStrategy;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.sourceforge.waters.analysis.abstraction.ChainTRSimplifier;
import net.sourceforge.waters.analysis.abstraction.OPVerifierTRChain;
import net.sourceforge.waters.analysis.abstraction.ObservationEquivalenceTRSimplifier;
import net.sourceforge.waters.analysis.abstraction.ObservationEquivalenceTRSimplifier.TransitionRemoval;
import net.sourceforge.waters.analysis.abstraction.TRSimplifierStatistics;
import net.sourceforge.waters.analysis.abstraction.TauLoopRemovalTRSimplifier;
import net.sourceforge.waters.analysis.abstraction.TransitionRelationSimplifier;
import net.sourceforge.waters.analysis.tr.EventEncoding;
import net.sourceforge.waters.analysis.tr.ListBufferTransitionRelation;
import net.sourceforge.waters.analysis.tr.StateEncoding;
import net.sourceforge.waters.analysis.tr.TRPartition;
import net.sourceforge.waters.analysis.tr.TransitionIterator;
import net.sourceforge.waters.model.analysis.AnalysisException;
import net.sourceforge.waters.model.analysis.IdenticalKindTranslator;
import net.sourceforge.waters.model.analysis.KindTranslator;
import net.sourceforge.waters.model.analysis.OverflowException;
import net.sourceforge.waters.model.des.AutomatonProxy;
import net.sourceforge.waters.model.des.EventProxy;
import net.sourceforge.waters.model.des.ProductDESEqualityVisitor;
import net.sourceforge.waters.model.des.ProductDESProxyFactory;
import net.sourceforge.waters.model.des.StateProxy;
import net.sourceforge.waters.model.des.TransitionProxy;
import net.sourceforge.waters.plain.des.ProductDESElementFactory;
import net.sourceforge.waters.xsd.base.ComponentKind;
import net.sourceforge.waters.xsd.base.EventKind;


/**
 * @author Robi Malik
 */

public class OPVerifierExperiment
{

  //#########################################################################
  //# Singleton Pattern
  public static OPVerifierExperiment getInstance()
  {
    return SingletonHolder.INSTANCE;
  }

  private static class SingletonHolder {
    private static final OPVerifierExperiment INSTANCE =
      new OPVerifierExperiment("opverifier.csv");
  }


  //#########################################################################
  //# Constructor
  public OPVerifierExperiment()
  {
    mFactory = ProductDESElementFactory.getInstance();
    mKindTranslator = IdenticalKindTranslator.getInstance();
    mOPVerifier = new OPVerifierTRChain();
    mOEQChain = new ChainTRSimplifier();
    final TauLoopRemovalTRSimplifier loopRemover =
      new TauLoopRemovalTRSimplifier();
    mOEQChain.add(loopRemover);
    mOEQSimplifier = new ObservationEquivalenceTRSimplifier();
    mOEQSimplifier.setEquivalence
      (ObservationEquivalenceTRSimplifier.Equivalence.OBSERVATION_EQUIVALENCE);
    mOEQSimplifier.setAppliesPartitionAutomatically(false);
    mOEQSimplifier.setTransitionRemovalMode(TransitionRemoval.NONE);
    mOEQChain.add(mOEQSimplifier);
    mLogWriter = null;
    mHeaderWritten = false;
    mExperimentIndex = 0;
  }

  public OPVerifierExperiment(final String logFileName)
  {
    this();
    setLogStream(logFileName);
  }


  //#########################################################################
  //# Configuration
  public void setLogStream(final String filename)
  {
    try {
      final File file = new File(filename);
      final OutputStream stream = new FileOutputStream(file);
      final PrintWriter writer = new PrintWriter(stream);
      setLogStream(writer);
    } catch (final FileNotFoundException exception) {
      // Never mind ...
    }
  }

  public void setLogStream(final PrintWriter writer)
  {
    mLogWriter = writer;
    mHeaderWritten = false;
  }


  //#########################################################################
  //# Invocation
  public void runExperiment(final AutomatonProxy aut,
                            final EventProxy tau,
                            final EventProxy omega)
    throws OverflowException
  {
    // 1. Check OP for determinised automaton
    final AutomatonProxy detAut = makeDeterministic(aut, tau);
    final boolean op = runOPVerifiers(detAut, tau, omega);
    // 2. Find OP abstraction
    if (!op) {
      final AutomatonProxy opAut1 = findOPAbstraction(aut, tau, omega);
      if (opAut1 != null) {
        runOPVerifiers(opAut1, tau, omega);
      }
    }
  }


  //#########################################################################
  //# Auxiliary Methods
  private boolean runOPVerifiers(final AutomatonProxy aut,
                                 final EventProxy tau,
                                 final EventProxy omega)
  {
    // 1. Run OP-Verifier
    ListBufferTransitionRelation rel =
      createTransitionRelation(aut, tau, omega, mOPVerifier);
    if (rel == null) {
      return false;
    }
    try {
      mOPVerifier.run();
    } catch (final AnalysisException exception) {
      // Never mind ...
    }
    // 2. Run Observation Equivalence
    rel = createTransitionRelation(aut, tau, omega, mOEQChain);
    try {
      mOEQChain.run();
    } catch (final AnalysisException exception) {
      // Never mind ...
    }
    // 3. Write stats
    final List<TRSimplifierStatistics> stats =
      new LinkedList<TRSimplifierStatistics>();
    final TRSimplifierStatistics opChainStats = mOPVerifier.getStatistics();
    stats.add(opChainStats);
    mOPVerifier.collectStatistics(stats);
    final TRSimplifierStatistics oeqChainStats = mOEQChain.getStatistics();
    stats.add(oeqChainStats);
    mOEQChain.collectStatistics(stats);
    writeLog(aut, tau, omega, stats);
    // 4. Save automata
    /*
    if (rel.getNumberOfReachableStates() >= 10000 &&
        opChainStats.getOverflowCount() == 0 &&
        oeqChainStats.getOverflowCount() == 0) {
      mExperimentIndex++;
      final String filename = FILE_NAME_PREFIX + mExperimentIndex + ".wdes";
      MarshallingTools.saveProductDES(aut, filename);
    }
    */
    return mOPVerifier.getOPResult();
  }

  private ListBufferTransitionRelation createTransitionRelation
    (final AutomatonProxy aut,
     final EventProxy tau,
     final EventProxy omega,
     final TransitionRelationSimplifier simplifier)
  {
    try {
      final EventEncoding eventEnc =
        new EventEncoding(aut, mKindTranslator, tau);
      final StateEncoding stateEnc = new StateEncoding(aut);
      final int config = simplifier.getPreferredInputConfiguration();
      final ListBufferTransitionRelation rel =
        new ListBufferTransitionRelation(aut, eventEnc, stateEnc, config);
      simplifier.setTransitionRelation(rel);
      final int omegaID = eventEnc.getEventCode(omega);
      simplifier.setDefaultMarkingID(omegaID);
      simplifier.createStatistics();
      return rel;
    } catch (final OverflowException exception) {
      return null;
    }
  }

  private AutomatonProxy makeDeterministic(final AutomatonProxy aut,
                                           final EventProxy tau)
  {
    // 1. Check for determinism
    final Collection<StateProxy> states = aut.getStates();
    int numInit = 0;
    for (final StateProxy state : states) {
      if (state.isInitial()) {
        numInit++;
      }
    }
    final Collection<EventProxy> events = aut.getEvents();
    final int numEvents = events.size();
    final TObjectIntHashMap<EventProxy> eventFanOut =
      new TObjectIntHashMap<EventProxy>(numEvents);
    final HashingStrategy<TransitionProxy> strategy =
      ProductDESEqualityVisitor.getDeterminsiticTransitionHashingStrategy();
    final TObjectIntCustomHashMap<TransitionProxy> transitionFanOut =
      new TObjectIntCustomHashMap<TransitionProxy>(strategy);
    boolean det = true;
    final Collection<TransitionProxy> transitions = aut.getTransitions();
    for (final TransitionProxy trans : transitions) {
      final EventProxy event = trans.getEvent();
      if (event != tau) {
        final int fanout = transitionFanOut.adjustOrPutValue(trans, 1, 1);
        if (fanout > 1) {
          det = false;
          if (eventFanOut.get(event) < fanout) {
            eventFanOut.put(event, fanout);
          }
        }
      }
    }
    transitionFanOut.clear();
    if (numInit <= 1 && det) {
      return aut;
    }

    // 2. Create new event set
    final Set<String> names = new THashSet<String>(numEvents);
    final Map<EventProxy,List<EventProxy>> eventReplacements =
      new HashMap<EventProxy,List<EventProxy>>(eventFanOut.size());
    int newNumEvents = 0;
    boolean hastau = false;
    for (final EventProxy event : events) {
      final int fanout = eventFanOut.get(event);
      if (fanout > 1) {
        newNumEvents += fanout;
      } else {
        newNumEvents++;
        final String name = event.getName();
        names.add(name);
      }
      hastau |= (event == tau);
    }
    final boolean addtau = !hastau && numInit > 1;
    final Collection<EventProxy> newEvents =
      new ArrayList<EventProxy>(newNumEvents + (addtau ? 1 : 0));
    for (final EventProxy event : events) {
      final int fanout = eventFanOut.get(event);
      if (fanout > 1) {
        final String base = event.getName();
        final EventKind kind = event.getKind();
        final boolean obs = event.isObservable();
        final List<EventProxy> replacement = new ArrayList<EventProxy>(fanout);
        int next = 0;
        for (int i = 0; i < fanout; i++) {
          String name;
          do {
            name = base + '[' + (next++) + ']';
          } while (!names.add(name));
          final EventProxy newEvent =
            mFactory.createEventProxy(name, kind, obs);
          newEvents.add(newEvent);
          replacement.add(newEvent);
        }
        eventReplacements.put(event, replacement);
      } else {
        newEvents.add(event);
      }
    }
    if (addtau) {
      newEvents.add(tau);
    }

    // 3. Create new state set
    final Collection<StateProxy> newStates;
    Map<StateProxy,StateProxy> altStateMap;
    final StateProxy init;
    if (numInit <= 1) {
      newStates = aut.getStates();
      altStateMap = null;
      init = null;
    } else {
      final int numStates = states.size();
      final Collection<String> stateNames = new THashSet<String>(numStates);
      for (final StateProxy state : states) {
        stateNames.add(state.getName());
      }
      final String base = ":init";
      String name = base;
      if (stateNames.contains(name)) {
        int i = 0;
        do {
          name = base + (i++);
        } while (stateNames.contains(name));
      }
      init = mFactory.createStateProxy(name, true, null);
      newStates = new ArrayList<StateProxy>(numStates + 1);
      newStates.add(init);
      altStateMap = new HashMap<StateProxy,StateProxy>(numInit);
      for (final StateProxy state : states) {
        if (state.isInitial()) {
          name = state.getName();
          final Collection<EventProxy> props = state.getPropositions();
          final StateProxy newState =
            mFactory.createStateProxy(name, false, props);
          newStates.add(newState);
          altStateMap.put(state, newState);
        } else {
          newStates.add(state);
        }
      }
    }

    // 4. Create new transition list
    final int newNumTrans = transitions.size() + (numInit > 1 ? numInit : 0);
    final Collection<TransitionProxy> newTransitions =
      new ArrayList<TransitionProxy>(newNumTrans);
    if (numInit > 1) {
      for (final StateProxy state : states) {
        if (state.isInitial()) {
          final StateProxy altState = altStateMap.get(state);
          final TransitionProxy trans =
            mFactory.createTransitionProxy(init, tau, altState);
          newTransitions.add(trans);
        }
      }
    }
    for (final TransitionProxy trans : transitions) {
      final EventProxy event = trans.getEvent();
      final List<EventProxy> replacements = eventReplacements.get(event);
      final EventProxy altEvent;
      if (replacements == null) {
        altEvent = event;
      } else {
        int next = transitionFanOut.get(trans);
        altEvent = replacements.get(next++);
        transitionFanOut.put(trans, next);
      }
      final StateProxy source = trans.getSource();
      final StateProxy altSource = getAltState(altStateMap, source);
      final StateProxy target = trans.getTarget();
      final StateProxy altTarget = getAltState(altStateMap, target);
      if (altSource == source && altEvent == event && altTarget == target) {
        newTransitions.add(trans);
      } else {
        final TransitionProxy newTrans =
          mFactory.createTransitionProxy(altSource, altEvent, altTarget);
        newTransitions.add(newTrans);
      }
    }

    // 5. Create result automaton
    final String name = aut.getName();
    final ComponentKind kind = aut.getKind();
    final AutomatonProxy detAut = mFactory.createAutomatonProxy
      (name, kind, newEvents, newStates, newTransitions);
    return detAut;
  }

  private AutomatonProxy findOPAbstraction(final AutomatonProxy aut,
                                           final EventProxy tau,
                                           final EventProxy omega)
    throws OverflowException
  {
    final Collection<StateProxy> states = aut.getStates();
    boolean hasInit = false;
    for (final StateProxy state : states) {
      if (state.isInitial()) {
        if (hasInit) {
          return null;
        } else {
          hasInit = true;
        }
      }
    }

    final EventEncoding eventEnc =
      new EventEncoding(aut, mKindTranslator, tau);
    final StateEncoding stateEnc = new StateEncoding(aut);
    final int config = mOEQSimplifier.getPreferredInputConfiguration();
    final ListBufferTransitionRelation rel;
    try {
      rel = new ListBufferTransitionRelation(aut, eventEnc.clone(),
                                             stateEnc, config);
      mOEQSimplifier.setTransitionRelation(rel);
      final int omegaID = eventEnc.getEventCode(omega);
      mOEQSimplifier.setDefaultMarkingID(omegaID);
      mOEQSimplifier.run();
    } catch (final AnalysisException exception) {
      return null;
    }

    final int numStates = stateEnc.getNumberOfStates();
    final TRPartition partition = mOEQSimplifier.getResultPartition();
    if (partition == null) {
      return null;
    }
    final int[] stateToClass = new int[numStates];
    Arrays.fill(stateToClass, -1);
    int c = 0;
    for (final int[] clazz : partition.getClasses()) {
      if (clazz != null) {
        for (final int s : clazz) {
          stateToClass[s] = c;
        }
      }
      c++;
    }

    final int numEvents = eventEnc.getNumberOfProperEvents();
    final TIntHashSet splitEvents = new TIntHashSet(numEvents);
    rel.reconfigure(ListBufferTransitionRelation.CONFIG_SUCCESSORS);
    final TIntHashSet classSuccessors = new TIntHashSet();
    final TIntHashSet stateSuccessors = new TIntHashSet();
    final TransitionIterator iter = rel.createSuccessorsModifyingIterator();
    for (int e = 0; e < numEvents; e++) {
      final int limit = e == EventEncoding.TAU ? 1 : 2;
      int sourceClass = 0;
      for (final int[] clazz : partition.getClasses()) {
        if (clazz != null) {
          for (final int s : clazz) {
            iter.reset(s, e);
            while (iter.advance()) {
              final int t = iter.getCurrentTargetState();
              final int targetClass = stateToClass[t];
              if (e != EventEncoding.TAU || sourceClass != targetClass) {
                if (stateSuccessors.add(targetClass)) {
                  classSuccessors.add(targetClass);
                } else {
                  iter.remove();
                }
              }
            }
            stateSuccessors.clear();
          }
          if (classSuccessors.size() >= limit) {
            splitEvents.add(e);
          }
          classSuccessors.clear();
        }
        sourceClass++;
      }
    }

    final Collection<EventProxy> events = aut.getEvents();
    final Set<String> names = new THashSet<String>(numEvents);
    for (final EventProxy event : events) {
      final String name = event.getName();
      names.add(name);
    }

    final TransitionIterator all = rel.createAllTransitionsReadOnlyIterator();
    final int numTrans = rel.getNumberOfTransitions();
    final Collection<TransitionProxy> newTransitions =
      new ArrayList<TransitionProxy>(numTrans);
    final TLongObjectHashMap<EventProxy> eventMap =
      new TLongObjectHashMap<EventProxy>(numEvents);
    final TIntObjectHashMap<List<EventProxy>> replacements =
      new TIntObjectHashMap<List<EventProxy>>(numEvents);
    int newNumEvents = events.size();
    boolean keeptau = false;
    while (all.advance()) {
      final int s = all.getCurrentSourceState();
      final StateProxy source = stateEnc.getState(s);
      final int e = all.getCurrentEvent();
      final EventProxy event = eventEnc.getProperEvent(e);
      final int t = all.getCurrentTargetState();
      final StateProxy target = stateEnc.getState(t);
      if (splitEvents.contains(e) &&
          (e != EventEncoding.TAU || stateToClass[s] != stateToClass[t])) {
        final long code = e | ((long) stateToClass[t] << 32);
        EventProxy newEvent = eventMap.get(code);
        if (newEvent == null) {
          List<EventProxy> replacement = replacements.get(e);
          if (replacement != null || e == EventEncoding.TAU) {
            newNumEvents++;
          }
          if (replacement == null) {
            replacement = new LinkedList<EventProxy>();
            replacements.put(e, replacement);
          }
          int i = replacement.size();
          final String base = event.getName();
          String name;
          do {
            name = base + '[' + i +']';
            i++;
          } while (!names.add(name));
          final EventKind kind = event.getKind();
          newEvent = mFactory.createEventProxy(name, kind);
          replacement.add(newEvent);
          eventMap.put(code, newEvent);
        }
        final TransitionProxy newTrans =
          mFactory.createTransitionProxy(source, newEvent, target);
        newTransitions.add(newTrans);
      } else {
        final TransitionProxy newTrans =
          mFactory.createTransitionProxy(source, event, target);
        newTransitions.add(newTrans);
        keeptau |= e == EventEncoding.TAU;
      }
    }
    if (!keeptau) {
      return null;
    }

    final Collection<EventProxy> newEvents =
      new ArrayList<EventProxy>(newNumEvents);
    for (final EventProxy event : events) {
      if (event.getKind() == EventKind.PROPOSITION) {
        newEvents.add(event);
      } else {
        final int e = eventEnc.getEventCode(event);
        final List<EventProxy> replacement = replacements.get(e);
        if (replacement == null) {
          newEvents.add(event);
        } else if (e == EventEncoding.TAU) {
          newEvents.add(event);
          newEvents.addAll(replacement);
        } else {
          newEvents.addAll(replacement);
        }
      }
    }

    final String name = aut.getName();
    final ComponentKind kind = aut.getKind();
    final AutomatonProxy opAut = mFactory.createAutomatonProxy
      (name, kind, newEvents, states, newTransitions);
    return opAut;
  }

  private StateProxy getAltState(final Map<StateProxy,StateProxy> altStateMap,
                                 final StateProxy state)
  {
    if (altStateMap == null) {
      return state;
    } else {
      final StateProxy altState = altStateMap.get(state);
      return altState == null ? state : altState;
    }
  }

  private void writeLog(final AutomatonProxy aut,
                        final EventProxy tau,
                        final EventProxy omega,
                        final List<TRSimplifierStatistics> stats)
  {
    if (mLogWriter != null) {
      if (!mHeaderWritten) {
        mLogWriter.print("Name,NumEvents,NumStates,NumTrans,NumTau");
        for (final TRSimplifierStatistics stat : stats) {
          stat.printCSVHorizontalHeadings(mLogWriter);
        }
        mLogWriter.println();
        mHeaderWritten = true;
      }
      final String name = aut.getName();
      mLogWriter.print('\"');
      mLogWriter.print(name);
      mLogWriter.print("\",");
      final int numEvents = aut.getEvents().size();
      mLogWriter.print(numEvents);
      mLogWriter.print(',');
      final int numStates = aut.getStates().size();
      mLogWriter.print(numStates);
      mLogWriter.print(',');
      int numTrans = aut.getTransitions().size();
      if (aut.getEvents().contains(omega)) {
        for (final StateProxy state : aut.getStates()) {
          if (state.getPropositions().contains(omega)) {
            numTrans++;
          }
        }
      }
      mLogWriter.print(numTrans);
      mLogWriter.print(',');
      int numTau = 0;
      for (final TransitionProxy trans : aut.getTransitions()) {
        if (trans.getEvent() == tau) {
          numTau++;
        }
      }
      mLogWriter.print(numTau);
      for (final TRSimplifierStatistics stat : stats) {
        stat.printCSVHorizontal(mLogWriter);
      }
      mLogWriter.println();
      mLogWriter.flush();
    }
  }


  //#########################################################################
  //# Data Members
  private final ProductDESProxyFactory mFactory;
  private final KindTranslator mKindTranslator;
  private final OPVerifierTRChain mOPVerifier;
  private final ChainTRSimplifier mOEQChain;
  private final ObservationEquivalenceTRSimplifier mOEQSimplifier;

  private PrintWriter mLogWriter;
  private boolean mHeaderWritten;
  @SuppressWarnings("unused")
  private final int mExperimentIndex;


  //#########################################################################
  //# Class Constants
  @SuppressWarnings("unused")
  private static final String FILE_NAME_PREFIX = "opv";

}
