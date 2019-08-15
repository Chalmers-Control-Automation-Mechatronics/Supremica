//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# Copyright (C) 2004-2019 Robi Malik
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

import gnu.trove.iterator.TObjectByteIterator;
import gnu.trove.map.hash.TObjectByteHashMap;
import gnu.trove.set.hash.THashSet;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Set;

import net.sourceforge.waters.analysis.options.EnumParameter;
import net.sourceforge.waters.analysis.options.Parameter;
import net.sourceforge.waters.analysis.options.ParameterIDs;
import net.sourceforge.waters.analysis.tr.EventEncoding;
import net.sourceforge.waters.cpp.analysis.NativeSafetyVerifier;
import net.sourceforge.waters.model.analysis.AnalysisException;
import net.sourceforge.waters.model.analysis.KindTranslator;
import net.sourceforge.waters.model.analysis.des.EventNotFoundException;
import net.sourceforge.waters.model.analysis.des.SafetyDiagnostics;
import net.sourceforge.waters.model.analysis.des.SafetyVerifier;
import net.sourceforge.waters.model.analysis.des.TraceChecker;
import net.sourceforge.waters.model.base.ComponentKind;
import net.sourceforge.waters.model.base.EventKind;
import net.sourceforge.waters.model.des.AutomatonProxy;
import net.sourceforge.waters.model.des.CounterExampleProxy;
import net.sourceforge.waters.model.des.EventProxy;
import net.sourceforge.waters.model.des.ProductDESProxy;
import net.sourceforge.waters.model.des.ProductDESProxyFactory;
import net.sourceforge.waters.model.des.SafetyCounterExampleProxy;
import net.sourceforge.waters.model.des.StateProxy;
import net.sourceforge.waters.model.des.TraceProxy;
import net.sourceforge.waters.model.des.TraceStepProxy;
import net.sourceforge.waters.model.des.TransitionProxy;


/**
 * <P>A projecting safety verifier. This safety verifier implements
 * natural projection using subset construction and deterministic
 * minimisation to compute abstractions while composing automata.</P>
 *
 * <P><I>Reference:</I><BR>
 * Simon Ware, Robi Malik. The Use of Language Projection for Compositional
 * Verification of Discrete Event Systems. Proc. 9th International Workshop
 * on Discrete Event Systems, WODES&nbsp;2008, 322-327, G&ouml;teborg,
 * Sweden, 2008.</P>
 *
 * @author Robi Malik
 */

public class CompositionalSafetyVerifier
  extends AbstractCompositionalModelVerifier
  implements SafetyVerifier
{

  //#########################################################################
  //# Constructors
  /**
   * Creates a new safety verifier without a model.
   * @param factory
   *          Factory used for trace construction.
   * @param translator
   *          Kind translator that defines event controllability status
   *          and automata types.
   * @param diag
   *          Diagnostics object to produce commented counterexamples.
   */
  public CompositionalSafetyVerifier(final ProductDESProxyFactory factory,
                                     final KindTranslator translator,
                                     final SafetyDiagnostics diag)
  {
    this(null, factory, translator, diag);
  }

  /**
   * Creates a new safety verifier to check the given model.
   * @param model
   *          The model to be checked by this safety verifier.
   * @param factory
   *          Factory used for trace construction.
   * @param translator
   *          Kind translator that defines event controllability status
   *          and automata types.
   * @param diag
   *          Diagnostics object to produce commented counterexamples.
   */
  public CompositionalSafetyVerifier(final ProductDESProxy model,
                                     final ProductDESProxyFactory factory,
                                     final KindTranslator translator,
                                     final SafetyDiagnostics diag)
  {
    super(model, factory, translator,
          ProjectionAbstractionProcedureFactory.PROJ);
    mDiagnostics = diag;
  }


  //#########################################################################
  //# Interface net.sourceforge.waters.model.analysis.SafetyVerifier
  @Override
  public SafetyCounterExampleProxy getCounterExample()
  {
    return (SafetyCounterExampleProxy) super.getCounterExample();
  }

  @Override
  public SafetyDiagnostics getDiagnostics()
  {
    return mDiagnostics;
  }

  //#########################################################################
  //# Interface net.sourceforge.waters.model.analysis.des.ModelAnalyzer
  @Override
  public List<Parameter> getParameters()
  {
    final List<Parameter> list = super.getParameters();
    list.add(new EnumParameter<AbstractionProcedureCreator>(ParameterIDs.CompositionalSafetyVerifier_AbstractionProcedureCreator) {
      @Override
      public void commitValue() {
        setAbstractionProcedureCreator(getValue());
      }
    });
    return list;
  }

  //#########################################################################
  //# Configuration
  @Override
  public ProjectionAbstractionProcedureFactory getAbstractionProcedureFactory()
  {
    return ProjectionAbstractionProcedureFactory.getInstance();
  }


  //#########################################################################
  //# Overrides for net.sourceforge.waters.model.AbstractModelAnalyser
  @Override
  protected void tearDown()
  {
    super.tearDown();
    mProperties = null;
    mPropertyEventsMap = null;
    mCollectedPlants = null;
  }

  @Override
  protected void initialiseEventsToAutomata()
    throws AnalysisException
  {
    final ProductDESProxy model = getModel();
    final Collection<AutomatonProxy> automata = model.getAutomata();
    final int numAutomata = automata.size();
    final KindTranslator translator = getKindTranslator();
    mProperties = new ArrayList<AutomatonProxy>(numAutomata);
    final Collection<EventProxy> events = model.getEvents();
    final int numEvents = events.size();
    mPropertyEventsMap = new TObjectByteHashMap<EventProxy>(numEvents);
    for (final AutomatonProxy aut : automata) {
      if (translator.getComponentKind(aut) == ComponentKind.SPEC) {
        mProperties.add(aut);
        final Collection<EventProxy> local = aut.getEvents();
        final int numLocal = local.size();
        final Collection<EventProxy> used =
          new THashSet<EventProxy>(numLocal);
        for (final TransitionProxy trans : aut.getTransitions()) {
          used.add(trans.getEvent());
        }
        for (final EventProxy event : local) {
          if (translator.getEventKind(event) != EventKind.PROPOSITION) {
            final byte mode = used.contains(event) ? REGULAR : FORBIDDEN;
            mPropertyEventsMap.put(event, mode);
          }
        }
      }
    }
    mCollectedPlants = new ArrayList<AutomatonProxy>(numAutomata);
    super.initialiseEventsToAutomata();
  }

  String getPropertyEventsDump()
  {
    final StringBuilder buffer = new StringBuilder();
    final TObjectByteIterator<EventProxy> iter = mPropertyEventsMap.iterator();
    while (iter.hasNext()) {
      iter.advance();
      buffer.append(iter.key());
      buffer.append(" = ");
      buffer.append(iter.value() == REGULAR ? "REGULAR" : "FORBIDDEN");
      buffer.append("\n");
    }
    return buffer.toString();
  }

  @Override
  protected EventInfo createEventInfo(final EventProxy event)
  {
    return new SafetyEventInfo(event);
  }


  @Override
  protected boolean isSubsystemTrivial
    (final Collection<AutomatonProxy> automata)
  {
    if (mProperties.isEmpty()) {
      return setSatisfiedResult();
    } else {
      for (final AutomatonProxy aut : automata) {
        for (final EventProxy event : aut.getEvents()) {
          if (mPropertyEventsMap.containsKey(event)) {
            return false;
          }
        }
      }
      return true;
    }
  }

  @Override
  protected EventRemovalStep removeEvents(final Set<EventProxy> removed,
                                          final Set<EventProxy> failing)
    throws AnalysisException
  {
    final EventRemovalStep step = super.removeEvents(removed, failing);
    if (step != null) {
      final ListIterator<AutomatonProxy> iter = mProperties.listIterator();
      while (iter.hasNext()) {
        final Map<StateProxy,StateProxy> stateMap = new HashMap<>();
        final AutomatonProxy aut = iter.next();
        final AutomatonProxy newAut = removeEvents(aut, removed, stateMap);
        if (newAut == aut) {
          continue;
        } else {
          step.addAutomatonPair(newAut, aut, stateMap);
          iter.set(newAut);
        }
      }
      for (final EventProxy event : removed) {
        mPropertyEventsMap.remove(event);
      }
    }
    return step;
  }

  @Override
  protected boolean doMonolithicAnalysis(final List<AutomatonProxy> automata)
    throws AnalysisException
  {
    if (!getPostponedSubsystems().isEmpty()) {
      mCollectedPlants.addAll(automata);
      return true;
    } else {
      final int numAutomata =
        mCollectedPlants.size() + automata.size() + mProperties.size();
      final List<AutomatonProxy> plantsAndSpecs =
        new ArrayList<AutomatonProxy>(numAutomata);
      plantsAndSpecs.addAll(mCollectedPlants);
      plantsAndSpecs.addAll(automata);
      plantsAndSpecs.addAll(mProperties);
      final boolean result = super.doMonolithicAnalysis(plantsAndSpecs);
      mCollectedPlants.clear();
      if (result) {
        setSatisfiedResult();
      }
      return result;
    }
  }

  @Override
  protected void setupMonolithicAnalyzer()
    throws EventNotFoundException
  {
    if (getCurrentMonolithicAnalyzer() == null) {
      final SafetyVerifier configured =
        (SafetyVerifier) getMonolithicAnalyzer();
      final SafetyVerifier current;
      if (configured == null) {
        final KindTranslator translator = getKindTranslator();
        final ProductDESProxyFactory factory = getFactory();
        current = new NativeSafetyVerifier(translator, mDiagnostics, factory);
      } else {
        current = configured;
      }
      setCurrentMonolithicAnalyzer(current);
      super.setupMonolithicAnalyzer();
    }
  }

  @Override
  protected CounterExampleProxy createCounterExample
    (final Collection<AutomatonProxy> automata,
     final List<TraceStepProxy> steps)
  {
    final ProductDESProxyFactory factory = getFactory();
    final ProductDESProxy model = getModel();
    final String traceName = mDiagnostics.getTraceName(model);
    final CompositionalVerificationResult result = getAnalysisResult();
    final CounterExampleProxy counter = result.getCounterExample();
    final String comment = counter.getComment();
    final TraceProxy trace = factory.createTraceProxy(steps);
    return factory.createSafetyCounterExampleProxy
      (traceName, comment, null, model, automata, trace);
  }

  @Override
  protected void testCounterExample
    (final List<TraceStepProxy> steps,
     final Collection<AutomatonProxy> automata)
    throws AnalysisException
  {
    final KindTranslator translator = getKindTranslator();
    TraceChecker.checkSafetyTrace(steps, automata, true, translator);
  }

  @Override
  protected Collection<AutomatonProxy> getAllTraceAutomata()
  {
    final Collection<AutomatonProxy> current = getCurrentAutomata();
    final int size = current.size() + mProperties.size();
    final Collection<AutomatonProxy> all = new ArrayList<AutomatonProxy>(size);
    all.addAll(current);
    all.addAll(mProperties);
    return all;
  }


  //#########################################################################
  //# Auxiliary Methods
  byte getPropertyStatus(final EventProxy event)
  {
    return mPropertyEventsMap.get(event);
  }


  //#########################################################################
  //# Inner Class SafetyEventInfo
  private final class SafetyEventInfo
    extends EventInfo
  {
    //#######################################################################
    //# Constructor
    private SafetyEventInfo(final EventProxy event)
    {
      super(event);
    }

    //#######################################################################
    //# Overrides for net.sourceforge.waters.analysis.abstraction.
    //# AbstractCompositionalModelVerifier.EventInfo
    @Override
    protected boolean canBeTau()
    {
      final EventProxy event = getEvent();
      return !mPropertyEventsMap.containsKey(event);
    }

    @Override
    protected boolean canBeLocal()
    {
      final EventProxy event = getEvent();
      return mPropertyEventsMap.get(event) != REGULAR;
    }

    @Override
    protected boolean isSubjectToSelfloopRemoval()
    {
      return canBeTau();
    }
  }


  //#########################################################################
  //# Data Members
  /**
   * Diagnostics object used to determine name and comment for
   * counterexample.
   */
  private final SafetyDiagnostics mDiagnostics;

  /**
   * List of specification (or property) automata in the model.
   * These are kept separate from the plants to exclude them from
   * compositional minimisation. They will only be used in the final
   * monolithic verification step.
   */
  private List<AutomatonProxy> mProperties;

  /**
   * Status information for events used in properties. Events used in
   * properties cannot be hidden during compositional minimisation, but
   * <I>forbidden</I> events can be treated specially. An event is considered
   * as forbidden if it is disabled in all states of some property automaton.
   * These events cannot be replaced by {@link EventEncoding#TAU TAU}, but
   * they are treated specially in subset construction, because successor
   * states reached after these events do not need to be explored. Therefore,
   * forbidden events are assigned the status {@link #FORBIDDEN}, while other
   * property events are assigned the status {@link #REGULAR}. Non-property
   * events are not contained in the map, and a lookup results in the default
   * value {@link #NONPROPERTY}.
   */
  private TObjectByteHashMap<EventProxy> mPropertyEventsMap;

  /**
   * List of plants still to be checked.
   * Event-disjoint subsystems that share events with the properties cannot
   * be checked independently. Therefore, automata from event-disjoint
   * subsystems are collected in this list. When the last monolithic check is
   * requested, all plants are combined and checked together against the
   * properties.
   */
  private List<AutomatonProxy> mCollectedPlants;


  //#########################################################################
  //# Class Constants
  /**
   * Status of non-property events.
   * @see #mPropertyEventsMap
   */
  static final byte NONPROPERTY = 0;
  /**
   * Status of non-forbidden property events.
   * @see #mPropertyEventsMap
   */
  static final byte REGULAR = 1;
  /**
   * Status of forbidden property events.
   * @see #mPropertyEventsMap
   */
  static final byte FORBIDDEN = 2;

}
