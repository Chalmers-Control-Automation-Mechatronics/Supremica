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

package net.sourceforge.waters.analysis.compositional;

import gnu.trove.set.hash.THashSet;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.sourceforge.waters.model.analysis.KindTranslator;
import net.sourceforge.waters.model.base.DuplicateNameException;
import net.sourceforge.waters.model.des.AutomatonProxy;
import net.sourceforge.waters.model.des.ConflictTraceProxy;
import net.sourceforge.waters.model.des.EventProxy;
import net.sourceforge.waters.model.des.ProductDESProxy;
import net.sourceforge.waters.model.des.ProductDESProxyFactory;
import net.sourceforge.waters.model.des.SafetyTraceProxy;
import net.sourceforge.waters.model.des.StateProxy;
import net.sourceforge.waters.model.des.TraceStepProxy;
import net.sourceforge.waters.model.des.TransitionProxy;
import net.sourceforge.waters.xsd.base.ComponentKind;
import net.sourceforge.waters.xsd.base.EventKind;
import net.sourceforge.waters.xsd.des.ConflictKind;


/**
 * A converter to create a language inclusion check model to determine
 * whether a state with a given proposition is reachable.
 *
 * @author Robi Malik
 */

public class PropositionPropertyBuilder
{

  //#########################################################################
  //# Constructors
  public PropositionPropertyBuilder(final ProductDESProxyFactory factory,
                                    final KindTranslator translator)
  {
    this(null, null, factory, translator);
  }

  public PropositionPropertyBuilder(final ProductDESProxyFactory factory,
                                    final EventProxy prop,
                                    final KindTranslator translator)
  {
    this(null, prop, factory, translator);
  }

  public PropositionPropertyBuilder(final ProductDESProxy model,
                                    final EventProxy prop,
                                    final ProductDESProxyFactory factory,
                                    final KindTranslator translator)
  {
    mInputModel = model;
    mFactory = factory;
    mParentKindTranslator = translator;
    mProposition = prop;
    mOutputModel = null;
    mKindTranslator = null;
  }


  //#########################################################################
  //# Configuration
  /**
   * Gets the model to be converted.
   */
  public ProductDESProxy getInputModel()
  {
    return mInputModel;
  }

  /**
   * Sets the model to be converted.
   */
  public void setInputModel(final ProductDESProxy model)
  {
    if (mInputModel != model) {
      mInputModel = model;
      mOutputModel = null;
    }
  }

  /**
   * Gets the proposition to be checked for.
   */
  public EventProxy getProposition()
  {
    return mProposition;
  }

  /**
   * Sets the proposition to be checked for.
   * The output model is used to check whether a state marked with this
   * proposition is reachable.
   */
  public void setProposition(final EventProxy prop)
  {
    if (mProposition != prop) {
      mProposition = prop;
      mOutputModel = null;
      mKindTranslator = null;
    }
  }


  //#########################################################################
  //# Invocation
  /**
   * Gets the converted model for language inclusion check.
   */
  public ProductDESProxy getOutputModel()
  {
    if (mOutputModel == null) {
      run();
    }
    return mOutputModel;
  }

  /**
   * Gets a kind translator to be passed to a language inclusion checker
   * in combination with the converted model.
   */
  public KindTranslator getKindTranslator()
  {
    if (mKindTranslator == null) {
      run();
    }
    return mKindTranslator;
  }

  public ProductDESProxy run()
  {
    final String desname = mInputModel.getName();
    final String propname = mProposition.getName();
    final String name = desname + ":" + propname;
    final String comment =
      "Automatically generated to test whether " + desname +
      " has a reachable state marked " + propname + ".";
    final Collection<EventProxy> inputEvents = mInputModel.getEvents();
    final int numEvents = inputEvents.size() + 1;
    final Collection<EventProxy> outputEvents =
      new ArrayList<EventProxy>(numEvents);
    outputEvents.addAll(inputEvents);
    if (!outputEvents.contains(mProposition)) {
      outputEvents.add(mProposition);
    }
    final Collection<AutomatonProxy> inputAutomata = mInputModel.getAutomata();
    final int numAutomata = inputAutomata.size();
    final Collection<String> autnames = new THashSet<String>(numAutomata);
    final Collection<AutomatonProxy> outputAutomata =
      new ArrayList<AutomatonProxy>(numAutomata + 1);
    for (final AutomatonProxy aut : inputAutomata) {
      if (mParentKindTranslator.getComponentKind(aut) == ComponentKind.PLANT) {
        final String autname = aut.getName();
        if (!autnames.add(autname)) {
          final String msg =
            "Product DES '" + desname +
            "' contains more than one automaton named '" + autname + "'!";
          throw new DuplicateNameException(msg);
        }
        final Collection<EventProxy> local = aut.getEvents();
        if (local.contains(mProposition)) {
          final AutomatonProxy selflooped = createSelfloopedAutomaton(aut);
          outputAutomata.add(selflooped);
        } else {
          outputAutomata.add(aut);
        }
      }
    }
    final AutomatonProxy spec = createPropertyAutomaton();
    outputAutomata.add(spec);
    mOutputModel = mFactory.createProductDESProxy(name, comment, null,
                                                  outputEvents, outputAutomata);
    mKindTranslator = new PropositionKindTranslator(mParentKindTranslator,
                                                    mProposition, spec);
    return mOutputModel;
  }

  public ConflictTraceProxy getConvertedConflictTrace
    (final SafetyTraceProxy trace)
  {
    final Collection<AutomatonProxy> inputAutomata = mInputModel.getAutomata();
    final Collection<AutomatonProxy> outputAutomata =
      mOutputModel.getAutomata();
    final int numAutomata = inputAutomata.size() - 1;
    final Map<String,AutomatonProxy> map =
      new HashMap<String,AutomatonProxy>(numAutomata);
    for (final AutomatonProxy aut : outputAutomata) {
      if (mKindTranslator.getComponentKind(aut) == ComponentKind.PLANT) {
        final String name = aut.getName();
        map.put(name, aut);
      }
    }
    for (final AutomatonProxy aut : inputAutomata) {
      final String name = aut.getName();
      if (map.containsKey(name)) {
        map.put(name, aut);
      }
    }
    final String tracename = trace.getName();
    final String comment = trace.getComment();
    final Collection<AutomatonProxy> outTraceAutomata = trace.getAutomata();
    final int numInTraceAutomata = outTraceAutomata.size();
    final Collection<AutomatonProxy> inTraceAutomata =
      new ArrayList<AutomatonProxy>(numInTraceAutomata);
    for (final AutomatonProxy outAut : outTraceAutomata) {
      final String name = outAut.getName();
      final AutomatonProxy inAut = map.get(name);
      if (inAut != null) {
        inTraceAutomata.add(inAut);
      }
    }
    final List<TraceStepProxy> outSteps = trace.getTraceSteps();
    final int numSteps = outSteps.size();
    final List<TraceStepProxy> inSteps =
      new ArrayList<TraceStepProxy>(numSteps);
    for (final TraceStepProxy outStep : outSteps) {
      final EventProxy event = outStep.getEvent();
      if (event != mKindTranslator.getProposition()) {
        final Map<AutomatonProxy,StateProxy> outStateMap =
          outStep.getStateMap();
        final int mapSize = outStateMap.size();
        final Map<AutomatonProxy,StateProxy> inStateMap =
          new HashMap<AutomatonProxy,StateProxy>(mapSize);
        for (final Map.Entry<AutomatonProxy,StateProxy> entry :
             outStateMap.entrySet()) {
          final AutomatonProxy outAut = entry.getKey();
          final String name = outAut.getName();
          final AutomatonProxy inAut = map.get(name);
          if (inAut != null) {
            final StateProxy state = entry.getValue();
            inStateMap.put(inAut, state);
          }
        }
        final TraceStepProxy inStep =
          mFactory.createTraceStepProxy(event, inStateMap);
        inSteps.add(inStep);
      }
    }
    return mFactory.createConflictTraceProxy(tracename, comment, null,
                                             mInputModel, inTraceAutomata,
                                             inSteps, ConflictKind.CONFLICT);
  }


  //#########################################################################
  //# Auxiliary Methods
  private AutomatonProxy createSelfloopedAutomaton(final AutomatonProxy aut)
  {
    final String name = aut.getName();
    final ComponentKind kind = ComponentKind.PLANT;
    final Collection<EventProxy> events = aut.getEvents();
    final Collection<StateProxy> states = aut.getStates();
    final Collection<TransitionProxy> inputTransitions = aut.getTransitions();
    final int numTrans = states.size() + inputTransitions.size();
    final Collection<TransitionProxy> outputTransitions =
      new ArrayList<TransitionProxy>(numTrans);
    outputTransitions.addAll(inputTransitions);
    for (final StateProxy state : states) {
      final Collection<EventProxy> props = state.getPropositions();
      if (props.contains(mProposition)) {
        final TransitionProxy selfloop =
          mFactory.createTransitionProxy(state, mProposition, state);
        outputTransitions.add(selfloop);
      }
    }
    return mFactory.createAutomatonProxy(name, kind,
                                         events, states, outputTransitions);
  }

  private AutomatonProxy createPropertyAutomaton()
  {
    final String name = "never:" + mProposition.getName();
    final ComponentKind kind = ComponentKind.SPEC;
    final Collection<EventProxy> events =
      Collections.singletonList(mProposition);
    final StateProxy state = mFactory.createStateProxy(name, true, null);
    final Collection<StateProxy> states = Collections.singletonList(state);
    return mFactory.createAutomatonProxy(name, kind, events, states, null);
  }


  //#########################################################################
  //# Inner Class PropositionKindTranslator
  private static class PropositionKindTranslator
    implements KindTranslator, Serializable
  {

    //#######################################################################
    //# Constructor
    private PropositionKindTranslator(final KindTranslator translator,
                                      final EventProxy prop,
                                      final AutomatonProxy spec)
    {
      mParentKindTranslator = translator;
      mProposition = prop;
      mSpecification = spec;
    }

    //#######################################################################
    //# Simple Access
    private EventProxy getProposition()
    {
      return mProposition;
    }

    //#######################################################################
    //# Interface net.sourceforge.waters.model.analysis.KindTranslator
    public ComponentKind getComponentKind(final AutomatonProxy aut)
    {
      if (aut == mSpecification) {
        return ComponentKind.SPEC;
      } else {
        return ComponentKind.PLANT;
      }
    }

    public EventKind getEventKind(final EventProxy event)
    {
      if (mParentKindTranslator.getEventKind(event) == EventKind.PROPOSITION &&
          event != mProposition) {
        return EventKind.PROPOSITION;
      } else {
        return EventKind.UNCONTROLLABLE;
      }
    }

    //#######################################################################
    //# Data Members
    private final KindTranslator mParentKindTranslator;
    private final EventProxy mProposition;
    private final AutomatonProxy mSpecification;

    //#######################################################################
    //# Class Constants
    private static final long serialVersionUID = 1L;

  }

  //#########################################################################
  //# Data Members
  private final ProductDESProxyFactory mFactory;

  private final KindTranslator mParentKindTranslator;

  /**
   * The model which is being changed.
   */
  private ProductDESProxy mInputModel;

  /**
   * The proposition to be checked for.
   */
  private EventProxy mProposition;

  /**
   * The model that has been created
   */
  private ProductDESProxy mOutputModel;

  /**
   * Kind translator to be used when checking output model.
   */
  private PropositionKindTranslator mKindTranslator;

}
