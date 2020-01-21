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

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import net.sourceforge.waters.analysis.options.BooleanOption;
import net.sourceforge.waters.analysis.options.Option;
import net.sourceforge.waters.analysis.options.OptionPage;
import net.sourceforge.waters.analysis.options.PropositionOption;
import net.sourceforge.waters.analysis.tr.EventEncoding;
import net.sourceforge.waters.analysis.tr.EventStatus;
import net.sourceforge.waters.analysis.tr.TRAutomatonProxy;
import net.sourceforge.waters.model.analysis.AnalysisException;
import net.sourceforge.waters.model.analysis.AnalysisResult;
import net.sourceforge.waters.model.analysis.des.AutomatonBuilder;
import net.sourceforge.waters.model.analysis.des.AutomatonResult;
import net.sourceforge.waters.model.analysis.des.DefaultAutomatonResult;
import net.sourceforge.waters.model.analysis.kindtranslator.IdenticalKindTranslator;
import net.sourceforge.waters.model.base.ComponentKind;
import net.sourceforge.waters.model.base.EventKind;
import net.sourceforge.waters.model.des.AutomatonProxy;
import net.sourceforge.waters.model.des.EventProxy;
import net.sourceforge.waters.model.des.ProductDESProxy;
import net.sourceforge.waters.model.des.ProductDESProxyFactory;

import org.supremica.automata.Alphabet;
import org.supremica.automata.Automata;
import org.supremica.automata.Automaton;
import org.supremica.automata.LabeledEvent;
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
  }


  //#########################################################################
  //# Interface net.sourceforge.waters.model.analysis.options.Configurable
  @Override
  public List<Option<?>> getOptions(final OptionPage db)
  {
    final List<Option<?>> options = new LinkedList<>();
    final EquivalenceRelation rel = mMinimizationOptions.getMinimizationType();
    if (rel != EquivalenceRelation.CONFLICTEQUIVALENCE) {
      db.append(options, SupremicaSimplifierFactory.
                OPTION_SupremicaAutomatonBuilder_DefaultMarkingID);
    }
    if (rel != EquivalenceRelation.LANGUAGEEQUIVALENCE) {
      db.append(options, SupremicaSimplifierFactory.
                OPTION_SupremicaAutomatonBuilder_AlsoTransitions);
    }
    db.append(options, SupremicaSimplifierFactory.
              OPTION_SupremicaAutomatonBuilder_TreatUnobservableEventsAsLocal);
    return options;
  }

  @Override
  public void setOption(final Option<?> option)
  {
    if (option.hasID(SupremicaSimplifierFactory.
                     OPTION_SupremicaAutomatonBuilder_AlsoTransitions)) {
      final BooleanOption propOption = (BooleanOption) option;
      mMinimizationOptions.setAlsoTransitions(propOption.getValue());
    } else if (option.hasID(SupremicaSimplifierFactory.
                            OPTION_SupremicaAutomatonBuilder_DefaultMarkingID)) {
      final PropositionOption propOption = (PropositionOption) option;
      mDefaultMarking = propOption.getValue();
      mMinimizationOptions.setIgnoreMarking(mDefaultMarking == null);
    } else if (option.hasID(SupremicaSimplifierFactory.
                            OPTION_SupremicaAutomatonBuilder_TreatUnobservableEventsAsLocal)) {
      final BooleanOption propOption = (BooleanOption) option;
      mTreatUnobservableEventsAsLocal = propOption.getValue();
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

      final AutomatonMinimizer minimizer = new AutomatonMinimizer(aut);

      final ProductDESProxy context = getModel();

      createAlphabet(forwardsMap);

      try {
        final Automaton newAut = minimizer
          .getMinimizedAutomaton(mMinimizationOptions);
        newAut.setName(mOutputName);
        final AutomataToWaters importer =
          new AutomataToWaters(mFactory, context, mDefaultMarking, forwardsMap);
        mResult = importer.convertAutomaton(newAut);
        final AutomatonResult result = getAnalysisResult();
        result.setComputedAutomaton(mResult);
        return true;
      }
      catch (final Exception e) {
        throw new AnalysisException(e.getMessage(), e);
      }
    }
    finally {
      tearDown();
    }
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
    return getAnalysisResult().getComputedProxy();
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
    return (AutomatonResult) super.getAnalysisResult();
  }

  @Override
  public AnalysisResult createAnalysisResult()
  {
    return new DefaultAutomatonResult(this.getClass());
  }

  //#########################################################################
  //# Auxiliary Methods
  public Map<EventProxy, EventProxy> createForwardsMapping() throws AnalysisException {
    final AutomatonProxy aut = getModel()
      .getAutomata()
      .iterator()
      .next();


    final Map<EventProxy, EventProxy> eventMap = new HashMap<>();

    final String[] reservedNames = new String[] {
      Config.MINIMIZATION_SILENT_EVENT_NAME.getValue(),
      Config.MINIMIZATION_SILENT_CONTROLLABLE_EVENT_NAME.getValue(),
      Config.MINIMIZATION_SILENT_UNCONTROLLABLE_EVENT_NAME.getValue()
    };

    final EventEncoding enc = aut instanceof TRAutomatonProxy
      ? ((TRAutomatonProxy)aut).getEventEncoding() : null;
    for (final EventProxy event : aut.getEvents()) {
      if (event.getKind() == EventKind.PROPOSITION) continue;
      boolean isReservedName = false;
      for (final String name : reservedNames) {
        if (event.getName().equals(name)) {
          isReservedName = true;
          break;
        }
      }
      final String newName = isReservedName
        ? findUnusedEventName(TEMP+event.getName(), aut)
        : event.getName();

      if (enc != null && !mTreatUnobservableEventsAsLocal) {
        final int code = enc.getEventCode(event);
        final byte status = enc.getProperEventStatus(code);
        if (EventStatus.isLocalEvent(status)) {
          if (isReservedName || event.isObservable()) {
            addReplacementEventToMap(eventMap, event, newName, false);
          }
          continue;
        }
      }

      if (mTreatUnobservableEventsAsLocal) {
        if (isReservedName) {
          addReplacementEventToMap(eventMap, event, newName, event.isObservable());
        }
      }
      else if (isReservedName || !event.isObservable()) {
        addReplacementEventToMap(eventMap, event, newName, true);
      }

    }
    return eventMap;
  }

  public void createAlphabet(final Map<EventProxy, EventProxy> forwardsMap) {
    final Alphabet alphabet = new Alphabet();
    for (final EventProxy event : getModel().getEvents()) {
      final EventProxy mappedEvent = forwardsMap.getOrDefault(event, event);
      if (mappedEvent.isObservable()) {
        alphabet.add(new LabeledEvent(mappedEvent));
      }
    }
    mMinimizationOptions.setTargetAlphabet(alphabet);
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
                                        final boolean observable) {
    final EventProxy event = mFactory.createEventProxy(newName,
                                                 currentEvent.getKind(),
                                                 observable);
    eventMap.put(currentEvent, event);
  }

//#########################################################################
  //# Data Members
  private final ProductDESProxyFactory mFactory;

  private final MinimizationOptions mMinimizationOptions;

  private boolean mSynchronisingOnUnobservableEvents;
  private boolean mTreatUnobservableEventsAsLocal;
  private EventProxy mDefaultMarking;
  private String mOutputName;
  private ComponentKind mComponentKind;

  private AutomatonProxy mResult;

  //#########################################################################
  //# Class Constants
  private static final String TEMP = "temp_name:";

}
