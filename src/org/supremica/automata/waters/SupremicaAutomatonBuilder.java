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

package org.supremica.automata.waters;

import gnu.trove.set.hash.THashSet;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.sourceforge.waters.analysis.options.BooleanOption;
import net.sourceforge.waters.analysis.options.Option;
import net.sourceforge.waters.analysis.options.OptionMap;
import net.sourceforge.waters.analysis.tr.EventEncoding;
import net.sourceforge.waters.analysis.tr.EventStatus;
import net.sourceforge.waters.analysis.tr.TRAutomatonProxy;
import net.sourceforge.waters.model.analysis.AnalysisException;
import net.sourceforge.waters.model.analysis.des.AutomatonBuilder;
import net.sourceforge.waters.model.analysis.des.AutomatonResult;
import net.sourceforge.waters.model.analysis.kindtranslator.IdenticalKindTranslator;
import net.sourceforge.waters.model.base.ComponentKind;
import net.sourceforge.waters.model.des.AutomatonProxy;
import net.sourceforge.waters.model.des.EventProxy;
import net.sourceforge.waters.model.des.ProductDESProxyFactory;

import org.supremica.automata.Automata;
import org.supremica.automata.Automaton;
import org.supremica.automata.IO.AutomataToWaters;
import org.supremica.automata.algorithms.EquivalenceRelation;
import org.supremica.automata.algorithms.minimization.AutomatonMinimizer;
import org.supremica.automata.algorithms.minimization.MinimizationOptions;
import org.supremica.properties.Config;

/**
 * @author Benjamin Wheeler
 */
public class SupremicaAutomatonBuilder extends SupremicaModelAnalyzer
  implements AutomatonBuilder
{

  public SupremicaAutomatonBuilder(final ProductDESProxyFactory factory,
                                   final EquivalenceRelation relation)
  {
    super(null, factory, IdenticalKindTranslator.getInstance(), false);

    mMinimizationOptions = new MinimizationOptions();
    mMinimizationOptions.setMinimizationType(relation);
    mFactory = factory;
    mObservableSwitchNameSet = new THashSet<>();
  }


  //#########################################################################
  //# Interface net.sourceforge.waters.model.analysis.options.Configurable
  @Override
  public List<Option<?>> getOptions(final OptionMap db)
  {
    final List<Option<?>> options = new LinkedList<>();
    final EquivalenceRelation rel = mMinimizationOptions.getMinimizationType();
    if (rel != EquivalenceRelation.LANGUAGEEQUIVALENCE) {
      db.append(options, SupremicaAutomatonBuilder.
                OPTION_SupremicaAutomatonBuilder_AlsoTransitions);
    }
    if (rel != EquivalenceRelation.CONFLICTEQUIVALENCE) {
      db.append(options, SupremicaAutomatonBuilder.
                OPTION_SupremicaAutomatonBuilder_IgnoreMarking);
    }
    return options;
  }

  @Override
  public void setOption(final Option<?> option)
  {
    if (option.hasID(SupremicaAutomatonBuilder.
                            OPTION_SupremicaAutomatonBuilder_AlsoTransitions)) {
      final BooleanOption propOption = (BooleanOption) option;
      mMinimizationOptions.setAlsoTransitions(propOption.getValue());
    } else if (option.hasID(SupremicaAutomatonBuilder.
                            OPTION_SupremicaAutomatonBuilder_IgnoreMarking)) {
      final BooleanOption propOption = (BooleanOption) option;
      mMinimizationOptions.setIgnoreMarking(propOption.getValue());
    } else {
      //TODO?
    }
  }

  @Override
  protected void setUp() throws AnalysisException
  {
    super.setUp();
  }

  @Override
  public boolean run() throws AnalysisException
  {
    try {
      final Map<EventProxy, EventProxy> forwardsMap = createForwardsMapping();
      setEventMap(forwardsMap);
      setUp();

      final Automata automata = getSupremicaAutomata();
      final Automaton aut = automata.getAutomatonAt(0);

      final Map<String, EventProxy> backwardsMap = createBackwardsMapping(forwardsMap);

      final AutomatonMinimizer minimizer = new AutomatonMinimizer(aut);

      try {
        final Automaton newAut = minimizer
          .getMinimizedAutomaton(mMinimizationOptions);
        newAut.setName(mOutputName);
        final AutomataToWaters importer =
          new AutomataToWaters(mFactory, backwardsMap, mObservableSwitchNameSet);
        mResult = importer.convertAutomaton(newAut);
      }
      catch (final Exception e) {
        //TODO
        e.printStackTrace();
      }

    }
    catch(final AnalysisException e) {
      //TODO
      e.printStackTrace();
    }
    finally {
      tearDown();
    }

    return false;
  }

  @Override
  public boolean supportsNondeterminism()
  {
    return true;
  }

  @Override
  public void setSynchronisingOnUnobservableEvents(final boolean sync)
  {
    mSynchronisingOnUnobservableEvents = sync;
  }

  @Override
  public boolean isSynchronisingOnUnobservableEvents()
  {
    return mSynchronisingOnUnobservableEvents;
  }







  @Override
  public void setOutputName(final String name)
  {
    mOutputName = name;
  }

  @Override
  public String getOutputName()
  {
    return mOutputName;
  }

  @Override
  public AutomatonProxy getComputedProxy()
  {
    //TODO Throw error if null?
    return mResult;
  }

  @Override
  public void setOutputKind(final ComponentKind kind)
  {
    mComponentKind = kind;
  }

  @Override
  public ComponentKind getOutputKind()
  {
    return mComponentKind;
  }

  @Override
  public AutomatonProxy getComputedAutomaton()
  {
    return getComputedProxy();
  }

  @Override
  public AutomatonResult getAnalysisResult()
  {
    // TODO Auto-generated method stub
    return null;
  }

  //#########################################################################
  //# Auxilliary Methods
  public Map<EventProxy, EventProxy> createForwardsMapping() throws AnalysisException {
    final EquivalenceRelation rel = mMinimizationOptions.getMinimizationType();
    final boolean tauCU = (rel == EquivalenceRelation.SUPERVISIONEQUIVALENCE
      || rel == EquivalenceRelation.SYNTHESISABSTRACTION);

    final AutomatonProxy aut = getModel()
      .getAutomata()
      .iterator()
      .next();

    final Map<EventProxy, EventProxy> eventMap = new HashMap<>();
    if (aut instanceof TRAutomatonProxy) {

      final Map<String, EventProxy> renameToReservedMap = new HashMap<>();
      final Map<String, EventProxy> renameFromReservedMap = new HashMap<>();
      final Set<EventProxy> observableSwitchSet = new THashSet<>();
      final String[] reservedNames = new String[] { TAU, TAU_C, TAU_U };

      final TRAutomatonProxy trAut = (TRAutomatonProxy) aut;
      final EventEncoding enc = trAut.getEventEncoding();
      for (final EventProxy event : aut.getEvents()) {

        final int code = enc.getEventCode(event);
        if (code == EventEncoding.TAU) {
          if (tauCU) throw new AnalysisException("Tau must preserve controllability " +
        "for this algorithm");
          renameToReservedMap.put(TAU, event);
          if (event.isObservable()) observableSwitchSet.add(event);
        }
        else {
          final byte status = enc.getProperEventStatus(code);
          if (EventStatus.isLocalEvent(status)) {
            if (!tauCU) throw new AnalysisException("Tau must not preserve controllability " +
          "for this algorithm.");
            if (EventStatus.isControllableEvent(status)) {
              renameToReservedMap.put(TAU_C, event);
            }
            else {
              renameToReservedMap.put(TAU_U, event);
            }
            if (event.isObservable()) observableSwitchSet.add(event);
          } else {
            //Normal event
            if (!event.isObservable()) observableSwitchSet.add(event);
          }
        }

        for (final String name : reservedNames) {
          if (event.getName().equals(name)) renameFromReservedMap.put(name, event);
        }

      }

      for (final String name : reservedNames) {
        //An event which must be renamed to a reserved name
        final EventProxy ev = renameToReservedMap.get(name);
        //An event which must be renamed from a reserved name
        final EventProxy en = renameFromReservedMap.get(name);
        if (ev == en) continue;
        if (ev != null) {
          addReplacementEventToMap(eventMap, ev, name, observableSwitchSet);
        }
        if (en != null && !renameToReservedMap.containsValue(en)) {
          final String newName = findUnusedEventName(TEMP+name, aut);
          addReplacementEventToMap(eventMap, en, newName, observableSwitchSet);
        }
      }

      for (final EventProxy event : observableSwitchSet) {
        addReplacementEventToMap(eventMap, event, event.getName(), observableSwitchSet);
      }

    }
    return eventMap;
  }

  public Map<String, EventProxy> createBackwardsMapping(final Map<EventProxy, EventProxy> forwardsMap) {
    final Map<String, EventProxy> backwardsMap = new HashMap<String, EventProxy>();
    for (final EventProxy event : getModel().getEvents()) {
      final String mappedName = forwardsMap.getOrDefault(event, event).getName();
      backwardsMap.put(mappedName, event);
    }
    return backwardsMap;
  }

  private String findUnusedEventName(String name, final AutomatonProxy aut) {
    for (;;) {
      boolean found = false;
      for (final EventProxy event : aut.getEvents()) {
        if (event.getName().equals(name)) {
          found = true;
          break;
        }
      }
      if (!found) return name;
      name = "_" + name;
    }
  }

  private void addReplacementEventToMap(final Map<EventProxy, EventProxy> eventMap,
                                        final EventProxy currentEvent,
                                        final String newName,
                                        final Set<EventProxy> observableSwitchSet) {
    final boolean observableSwitch = observableSwitchSet.contains(currentEvent);
    final EventProxy event = mFactory.createEventProxy(newName,
                                                 currentEvent.getKind(),
                                                 currentEvent.isObservable()
                                                 ^ observableSwitch);
    eventMap.put(currentEvent, event);
    if (observableSwitch) {
      observableSwitchSet.remove(currentEvent);
      mObservableSwitchNameSet.add(event.getName());
    }
  }

//#########################################################################
  //# Data Members
  private final ProductDESProxyFactory mFactory;

  private final MinimizationOptions mMinimizationOptions;

  private boolean mSynchronisingOnUnobservableEvents;
  private String mOutputName;
  private ComponentKind mComponentKind;

  private AutomatonProxy mResult;

  private final Set<String> mObservableSwitchNameSet;

  //#########################################################################
  //# Class Constants
  private static final String TAU =
    Config.MINIMIZATION_SILENT_EVENT_NAME.get();
  private static final String TAU_C =
    Config.MINIMIZATION_SILENT_CONTROLLABLE_EVENT_NAME.get();
  private static final String TAU_U =
    Config.MINIMIZATION_SILENT_UNCONTROLLABLE_EVENT_NAME.get();
  private static final String TEMP = "temp_name:";

  public static final String OPTION_SupremicaAutomatonBuilder_AlsoTransitions =
    "SupremicaAutomatonBuilder.AlsoTransitions";
  public static final String OPTION_SupremicaAutomatonBuilder_IgnoreMarking =
    "SupremicaAutomatonBuilder.IgnoreMarking";

}
