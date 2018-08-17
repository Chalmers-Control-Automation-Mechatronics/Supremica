//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# Copyright (C) 2004-2018 Robi Malik
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

package net.sourceforge.waters.analysis.modular;

import gnu.trove.set.hash.THashSet;

import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import net.sourceforge.waters.analysis.abstraction.TraceFinder;
import net.sourceforge.waters.model.analysis.AnalysisException;
import net.sourceforge.waters.model.analysis.ControllabilityKindTranslator;
import net.sourceforge.waters.model.analysis.KindTranslator;
import net.sourceforge.waters.model.analysis.OverflowException;
import net.sourceforge.waters.model.analysis.VerificationResult;
import net.sourceforge.waters.model.analysis.des.ControllabilityChecker;
import net.sourceforge.waters.model.analysis.des.SafetyVerifier;
import net.sourceforge.waters.model.des.AutomatonProxy;
import net.sourceforge.waters.model.des.AutomatonTools;
import net.sourceforge.waters.model.des.CounterExampleProxy;
import net.sourceforge.waters.model.des.EventProxy;
import net.sourceforge.waters.model.des.ProductDESProxy;
import net.sourceforge.waters.model.des.ProductDESProxyFactory;
import net.sourceforge.waters.model.des.SafetyCounterExampleProxy;
import net.sourceforge.waters.model.des.StateProxy;
import net.sourceforge.waters.model.des.TraceProxy;
import net.sourceforge.waters.model.des.TraceStepProxy;
import net.sourceforge.waters.xsd.base.ComponentKind;
import net.sourceforge.waters.xsd.base.EventKind;


/**
 * The modular controllability check algorithm.
 *
 * <P><I>Reference:</I><BR>
 * Bertil A. Brandin, Robi Malik, Petra Malik. Incremental verification
 * and synthesis of discrete-event systems guided by counter-examples.
 * IEEE Transactions on Control Systems Technology, 12&nbsp;(3), 387-401,
 * 2004.</P>
 *
 * @author Simon Ware
 */

public class ModularControllabilityChecker
  extends AbstractModularSafetyVerifier
  implements ControllabilityChecker
{

  //#########################################################################
  //# Constructors
  public ModularControllabilityChecker(final ProductDESProxyFactory factory,
                                       final SafetyVerifier secondary)
  {
    this(null, factory, secondary);
  }

  public ModularControllabilityChecker(final ProductDESProxy model,
                                       final ProductDESProxyFactory factory,
                                       final SafetyVerifier secondary)
  {
    super(model, factory, secondary);
    setKindTranslator(ControllabilityKindTranslator.getInstance());
  }


  //#########################################################################
  //# Configuration
  public void setCollectsFailedSpecs(final boolean collect)
  {
    mCollectsFailedSpecs = collect;
  }

  public boolean getCollectsFailedSpecs()
  {
    return mCollectsFailedSpecs;
  }

  public void setStartsWithSmallestSpec(final boolean least)
  {
    mStartsWithSmallestSpec = least;
  }

  public boolean getStartsWithSmallestSpec()
  {
    return mStartsWithSmallestSpec;
  }


  //#########################################################################
  //# Invocation
  @Override
  protected void setUp() throws AnalysisException
  {
    super.setUp();
    final SafetyVerifier mono = getMonolithicVerifier();
    mono.setNodeLimit(getNodeLimit());
  }

  @Override
  public boolean run()
    throws AnalysisException
  {
    try {
      setUp();
      final ProductDESProxy des = getModel();
      final Collection<AutomatonProxy> input = des.getAutomata();
      final int numAutomata = input.size();
      final Set<AutomatonProxy> plants = new THashSet<>(numAutomata);
      final Set<AutomatonProxy> specPlants = new THashSet<>(numAutomata);
      final SortedSet<AutomatonProxy> specs =
        new TreeSet<>(new Comparator<AutomatonProxy>() {
          @Override
          public int compare(final AutomatonProxy a1, final AutomatonProxy a2)
          {
            if (a1.getStates().size() < a2.getStates().size()) {
              return -1;
            } else if (a1.getStates().size() > a2.getStates().size()) {
              return 1;
            }
            if (a1.getTransitions().size() < a2.getTransitions().size()) {
              return -1;
            } else if (a1.getTransitions().size() > a2.getTransitions().size()) {
              return 1;
            }
            if (a1.getEvents().size() < a2.getEvents().size()) {
              return -1;
            } else if (a1.getEvents().size() > a2.getEvents().size()) {
              return 1;
            }
            return a1.compareTo(a2);
          }
        });

      final List<AutomatonProxy> automata = new ArrayList<>(numAutomata);
      for (final AutomatonProxy aut : input) {
        switch (getKindTranslator().getComponentKind(aut)) {
        case PLANT:
          automata.add(aut);
          plants.add(aut);
          break;
        case SPEC:
          automata.add(aut);
          specs.add(aut);
          break;
        default:
          break;
        }
      }

      final ProductDESProxyFactory factory = getFactory();
      final SafetyVerifier mono = getMonolithicVerifier();
      final ModularHeuristic heuristic = getHeuristic();
      SafetyCounterExampleProxy counter = null;
      final Collection<AutomatonProxy> subsystem = new ArrayList<>(numAutomata);
      specLoop:
      while (!specs.isEmpty()) {
        checkAbort();
        subsystem.clear();
        final Set<AutomatonProxy> uncomposedPlants = new TreeSet<>(plants);
        final Set<AutomatonProxy> uncomposedSpecPlants = new TreeSet<>(specPlants);
        final Set<AutomatonProxy> uncomposedSpecs = new TreeSet<>(specs);
        final AutomatonProxy spec =
          mStartsWithSmallestSpec ? specs.first() : specs.last();
        subsystem.add(spec);
        uncomposedSpecs.remove(spec);
        ProductDESProxy subDES =
          AutomatonTools.createProductDESProxy(spec, factory);
        mono.setModel(subDES);
        mono.setKindTranslator(new KindTranslator() {
          @Override
          public EventKind getEventKind(final EventProxy e)
          {
            return getKindTranslator().getEventKind(e);
          }

          @Override
          public ComponentKind getComponentKind(final AutomatonProxy a)
          {
            return specs.contains(a) ? ComponentKind.SPEC
              : ComponentKind.PLANT;
          }
        });
        while (!mono.run()) {
          recordStats(mono.getAnalysisResult());
          final Collection<AutomatonProxy> selectedAutomata =
            heuristic.heur(subDES,
                           uncomposedPlants,
                           uncomposedSpecPlants,
                           uncomposedSpecs,
                           mono.getCounterExample());
          checkAbort();
          if (selectedAutomata == null) {
            if (counter == null) {
              counter = mono.getCounterExample();
              counter = extendTrace(heuristic, counter, automata);
              setFailedResult(counter);
            }
            if (mCollectsFailedSpecs) {
              counter = mono.getCounterExample();
              final Collection<AutomatonProxy> failedSpecs =
                collectFailedSpecs(heuristic, counter);
              specs.removeAll(failedSpecs);
              if (!failedSpecs.contains(spec)) {
                specs.add(spec);
              }
              continue specLoop;
            } else {
              return false;
            }
          }
          subsystem.addAll(selectedAutomata);
          uncomposedPlants.removeAll(selectedAutomata);
          uncomposedSpecPlants.removeAll(selectedAutomata);
          uncomposedSpecs.removeAll(selectedAutomata);
          subDES = AutomatonTools.createProductDESProxy(spec.getName(),
                                                        subsystem, factory);
          mono.setModel(subDES);
        }
        recordStats(mono.getAnalysisResult());
        for (final AutomatonProxy automaton : subsystem) {
          if (specs.remove(automaton)) {
            specPlants.add(automaton);
          }
        }
      }
      if (counter != null) {
        return false;
      } else {
        return setSatisfiedResult();
      }
    } catch (final AnalysisException exception) {
      throw setExceptionResult(exception);
    } catch (final OutOfMemoryError error) {
      System.gc();
      final OverflowException overflow = new OverflowException(error);
      throw setExceptionResult(overflow);
    } finally {
      tearDown();
    }
  }


  //#########################################################################
  //# Overrides for net.sourceforge.waters.model.analysis.AbstractModelAnalyser
  @Override
  public ModularVerificationResult getAnalysisResult()
  {
    return (ModularVerificationResult) super.getAnalysisResult();
  }

  @Override
  public ModularVerificationResult createAnalysisResult()
  {
    return new ModularVerificationResult(this);
  }


  //#########################################################################
  //# Overrides for Abstract Base Class
  //# net.sourceforge.waters.model.analysis.AbstractModelVerifier
  @Override
  protected boolean setFailedResult(final CounterExampleProxy counterexample)
  {
    final ProductDESProxyFactory factory = getFactory();
    final ProductDESProxy des = getModel();
    final String desname = des.getName();
    final String tracename = desname + "-uncontrollable";
    final Collection<AutomatonProxy> automata = counterexample.getAutomata();
    final TraceProxy trace = counterexample.getTraces().get(0);
    final SafetyCounterExampleProxy wrapper =
      factory.createSafetyCounterExampleProxy(tracename, null, null,
                                              des, automata, trace);
    return super.setFailedResult(wrapper);
  }


  //#########################################################################
  //# Trace Computation
  private Collection<AutomatonProxy> collectFailedSpecs
    (final ModularHeuristic heuristic, final SafetyCounterExampleProxy counter)
  {
    final KindTranslator translator = getKindTranslator();
    final Collection<AutomatonProxy> failedSpecs = new LinkedList<>();
    for (final AutomatonProxy aut : counter.getAutomata()) {
      if (translator.getComponentKind(aut) == ComponentKind.SPEC) {
        final TraceFinder finder = heuristic.getTraceFinder(aut);
        if (finder.isRejectingSpec(counter)) {
          failedSpecs.add(aut);
        }
      }
    }
    assert !failedSpecs.isEmpty();
    final ModularVerificationResult result = getAnalysisResult();
    result.addFailedSpecs(failedSpecs);
    return failedSpecs;
  }

  private SafetyCounterExampleProxy extendTrace(final ModularHeuristic heuristic,
                                                final SafetyCounterExampleProxy counter,
                                                final List<AutomatonProxy> automata)
    throws AnalysisException
  {
    final Set<AutomatonProxy> oldAutomata = new THashSet<>(counter.getAutomata());
    boolean done = false;
    boolean det = true;
    for (final AutomatonProxy aut : automata) {
      if (!oldAutomata.contains(aut)) {
        done = false;
        final TraceFinder finder = heuristic.getTraceFinder(aut);
        det &= finder.isDeterministic();
      }
    }
    if (done) {
      return counter;
    }
    final ProductDESProxyFactory factory = getFactory();
    final String name = counter.getName();
    final String comment = counter.getComment();
    final URI location = counter.getLocation();
    final ProductDESProxy des = counter.getProductDES();
    final TraceProxy trace = counter.getTrace();
    if (det) {
      return factory.createSafetyCounterExampleProxy(name, comment, location,
                                                     des, automata, trace);
    }
    final List<TraceStepProxy> oldSteps = trace.getTraceSteps();
    final int numSteps = oldSteps.size();
    final KindTranslator translator = getKindTranslator();
    final List<TraceStepProxy> newSteps = new ArrayList<>(numSteps);
    int depth = 0;
    for (final TraceStepProxy oldStep : oldSteps) {
      checkAbort();
      final EventProxy event = oldStep.getEvent();
      final Map<AutomatonProxy,StateProxy> oldMap = oldStep.getStateMap();
      Map<AutomatonProxy,StateProxy> newMap = null;
      boolean endOfTrace = false;
      for (final AutomatonProxy aut : automata) {
        if (!oldAutomata.contains(aut)) {
          final TraceFinder finder = heuristic.getTraceFinder(aut);
          if (translator.getComponentKind(aut) == ComponentKind.SPEC &&
              depth > finder.computeNumberOfAcceptedSteps(trace)) {
            // Found nonaccepting spec --- trace ends here.
            endOfTrace = true;
            continue;
          }
          final StateProxy state = finder.getState(depth);
          if (state != null) {
            if (newMap == null) {
              newMap = new HashMap<>(oldMap);
            }
            newMap.put(aut, state);
          }
        }
      }
      if (newMap == null) {
        newSteps.add(oldStep);
      } else {
        final TraceStepProxy newStep =
          factory.createTraceStepProxy(event, newMap);
        newSteps.add(newStep);
      }
      if (endOfTrace) {
        break;
      }
      depth++;
    }
    final TraceProxy extended = factory.createTraceProxy(newSteps);
    return factory.createSafetyCounterExampleProxy(name, comment, location,
                                                   des, automata, extended);
  }


  //#########################################################################
  //# Collecting Statistics
  private void recordStats(final VerificationResult subresult)
  {
    final ModularVerificationResult result = getAnalysisResult();
    result.updateNumberOfAutomata(subresult.getTotalNumberOfAutomata());
    result.updateNumberOfStates(subresult.getTotalNumberOfStates());
    result.updateNumberOfTransitions(subresult.getTotalNumberOfTransitions());
  }


  //#########################################################################
  //# Data Members
  private boolean mCollectsFailedSpecs = false;
  private boolean mStartsWithSmallestSpec = false;

}
