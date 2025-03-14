//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# Copyright (C) 2004-2023 Robi Malik
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

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;

import net.sourceforge.waters.model.options.BooleanOption;
import net.sourceforge.waters.model.options.LeafOptionPage;
import net.sourceforge.waters.model.options.Option;
import net.sourceforge.waters.model.options.PropositionOption;
import net.sourceforge.waters.model.options.StringOption;
import net.sourceforge.waters.analysis.tr.EventEncoding;
import net.sourceforge.waters.analysis.tr.ListBufferTransitionRelation;
import net.sourceforge.waters.analysis.tr.StateEncoding;
import net.sourceforge.waters.analysis.tr.TransitionIterator;
import net.sourceforge.waters.model.analysis.AnalysisException;
import net.sourceforge.waters.model.analysis.OverflowException;
import net.sourceforge.waters.model.analysis.des.AbstractConflictChecker;
import net.sourceforge.waters.model.analysis.des.AbstractModelAnalyzerFactory;
import net.sourceforge.waters.model.analysis.des.ProductDESResult;
import net.sourceforge.waters.model.analysis.des.SupervisorSynthesizer;
import net.sourceforge.waters.model.analysis.kindtranslator.ConflictKindTranslator;
import net.sourceforge.waters.model.analysis.kindtranslator.IdenticalKindTranslator;
import net.sourceforge.waters.model.analysis.kindtranslator.KindTranslator;
import net.sourceforge.waters.model.base.ComponentKind;
import net.sourceforge.waters.model.base.EventKind;
import net.sourceforge.waters.model.des.AutomatonProxy;
import net.sourceforge.waters.model.des.EventProxy;
import net.sourceforge.waters.model.des.ProductDESProxy;
import net.sourceforge.waters.model.des.ProductDESProxyFactory;
import net.sourceforge.waters.model.des.StateProxy;
import net.sourceforge.waters.model.des.TransitionProxy;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


/**
 * An implementation of the compositional synthesis algorithm.
 *
 * <I>References:</I><BR>
 * Sahar Mohajerani, Robi Malik, Simon Ware, Martin Fabian. On the Use of
 * Observation Equivalence in Synthesis Abstraction. Proc. 3rd IFAC Workshop
 * on Dependable Control of Discrete Systems, DCDS&nbsp;2011,
 * Saarbr&uuml;cken, Germany, 2011.<BR>
 * Sahar Mohajerani, Robi Malik, Martin Fabian. Nondeterminism Avoidance in
 * Compositional Synthesis of Discrete Event Systems, Proc. 7th International
 * Conference on Automation Science and Engineering, CASE&nbsp;2011, Trieste,
 * Italy.
 *
 * @author Sahar Mohajerani, Robi Malik
 */

public abstract class AbstractCompositionalSynthesizer
  extends AbstractCompositionalModelAnalyzer
  implements SupervisorSynthesizer
{

  //#########################################################################
  //# Constructors
  /**
   * Creates a compositional synthesiser to compute a supervisor for the given
   * model.
   *
   * @param model
   *          The model to be checked by this model verifier.
   * @param factory
   *          Factory used for trace construction.
   * @param translator
   *          Kind translator used to determine event and component kinds.
   * @param abstractionFactory
   *          Factory to define the abstraction sequence to be used.
   * @param preselectingMethodFactory
   *          Enumeration factory that determines possible candidate
   *          preselection methods.
   */
  public AbstractCompositionalSynthesizer
    (final ProductDESProxy model,
     final ProductDESProxyFactory factory,
     final KindTranslator translator,
     final AbstractionProcedureCreator abstractionFactory,
     final PreselectingMethodFactory preselectingMethodFactory)
  {
    super(model, factory, translator, abstractionFactory,
          preselectingMethodFactory);
    setPruningDeadlocks(true);
  }


  //#########################################################################
  //# Interface net.sourceforge.waters.model.analysis.SupervisorSynthesizer
  @Override
  public void setNonblockingSynthesis(final boolean nonblocking)
  {
    mNonblockingSynthesis = nonblocking;
  }

  @Override
  public boolean isNonblockingSynthesis()
  {
    return mNonblockingSynthesis;
  }

  @Override
  public void setNondeterminismEnabled(final boolean enable)
  {
    mNondeterminismEnabled = enable;
  }


  //#########################################################################
  //# Interface net.sourceforge.waters.model.analysis.ProductDESBuilder
  @Override
  public ProductDESProxy getComputedProductDES()
  {
    return getComputedProxy();
  }


  //#########################################################################
  //# Interface net.sourceforge.waters.model.analysis.ModelBuilder
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
  public ProductDESProxy getComputedProxy()
  {
    final ProductDESResult result = getAnalysisResult();
    return result.getComputedProductDES();
  }


  //#########################################################################
  //# Interface net.sourceforge.waters.model.analysis.ModelAnalyzer
  @Override
  public List<Option<?>> getOptions(final LeafOptionPage db)
  {
    final List<Option<?>> options = super.getOptions(db);
    db.prepend(options, AbstractModelAnalyzerFactory.
                        OPTION_SupervisorSynthesizer_ConfiguredDefaultMarking);
    db.prepend(options, AbstractModelAnalyzerFactory.
                        OPTION_SupervisorSynthesizer_NonblockingSynthesis);
    db.prepend(options, AbstractModelAnalyzerFactory.
                        OPTION_SupervisorSynthesizer_ControllableSynthesis);
    db.prepend(options, AbstractModelAnalyzerFactory.
                        OPTION_SupervisorSynthesizer_OutputName);
    db.prepend(options, AbstractModelAnalyzerFactory.
                        OPTION_SupervisorSynthesizer_DetailedOutputEnabled);
    return options;
  }

  @Override
  public void setOption(final Option<?> option)
  {
    if (option.hasID(AbstractModelAnalyzerFactory.
                     OPTION_SupervisorSynthesizer_ConfiguredDefaultMarking)) {
      final PropositionOption propOption = (PropositionOption) option;
      setConfiguredDefaultMarking(propOption.getValue());
    } else if (option.hasID(AbstractModelAnalyzerFactory.
                            OPTION_SupervisorSynthesizer_NonblockingSynthesis)) {
      final BooleanOption boolOption = (BooleanOption) option;
      setNonblockingSynthesis(boolOption.getBooleanValue());
    } else if (option.hasID(AbstractModelAnalyzerFactory.
                            OPTION_SupervisorSynthesizer_ControllableSynthesis)) {
      final BooleanOption boolOption = (BooleanOption) option;
      final KindTranslator translator = boolOption.getBooleanValue() ?
        IdenticalKindTranslator.getInstance() :
        ConflictKindTranslator.getInstanceControllable();
      setKindTranslator(translator);
    } else if (option.hasID(AbstractModelAnalyzerFactory.
                            OPTION_SupervisorSynthesizer_OutputName)) {
      final StringOption stringOption = (StringOption) option;
      setOutputName(stringOption.getValue());
    } else if (option.hasID(AbstractModelAnalyzerFactory.
                            OPTION_SupervisorSynthesizer_DetailedOutputEnabled)) {
      final BooleanOption boolOption = (BooleanOption) option;
      setDetailedOutputEnabled(boolOption.getBooleanValue());
    } else {
      super.setOption(option);
    }
  }


  //#########################################################################
  //# Overrides for net.sourceforge.waters.model.AbstractModelAnalyser
  @Override
  public boolean supportsNondeterminism()
  {
    return mNondeterminismEnabled;
  }

  @Override
  public CompositionalSynthesisResult getAnalysisResult()
  {
    return (CompositionalSynthesisResult) super.getAnalysisResult();
  }

  @Override
  protected void setUp() throws AnalysisException
  {
    final EventProxy defaultMarking = createDefaultMarking();
    setPropositionsForMarkings(defaultMarking, null);
    super.setUp();
  }


  //#########################################################################
  //# Hooks
  @Override
  protected EventProxy createDefaultMarking()
  {
    if (!mNonblockingSynthesis) {
      return null;
    } else if (getConfiguredDefaultMarking() != null) {
      return getConfiguredDefaultMarking();
    } else {
      final ProductDESProxy des = getModel();
      return AbstractConflictChecker.getMarkingProposition(des);
    }
  }

  @Override
  protected AutomatonProxy plantify(final AutomatonProxy spec)
    throws OverflowException
  {
    final KindTranslator translator = getKindTranslator();
    final Collection<EventProxy> events = spec.getEvents();
    final int numEvents = events.size();
    final Collection<EventProxy> uncontrollables =
      new ArrayList<EventProxy>(numEvents);
    for (final EventProxy event : events) {
      if (translator.getEventKind(event) == EventKind.UNCONTROLLABLE) {
        uncontrollables.add(event);
      }
    }
    final EventEncoding eventEnc =
      new EventEncoding(uncontrollables, translator);
    final StateEncoding stateEnc = new StateEncoding(spec);
    final ListBufferTransitionRelation rel =
      new ListBufferTransitionRelation(spec,
                                       eventEnc,
                                       stateEnc,
                                       ListBufferTransitionRelation.CONFIG_SUCCESSORS);
    final int numStates = stateEnc.getNumberOfStates();
    final Collection<StateProxy> states =
      new ArrayList<StateProxy>(numStates + 1);
    states.addAll(spec.getStates());
    StateProxy dump = null;
    final Collection<TransitionProxy> transitions =
      new ArrayList<TransitionProxy>(spec.getTransitions());
    final TransitionIterator iter = rel.createSuccessorsReadOnlyIterator();
    final ProductDESProxyFactory factory = getFactory();
    for (int s = 0; s < numStates; s++) {
      final StateProxy state = stateEnc.getState(s);
      for (final EventProxy event : uncontrollables) {
        final int e = eventEnc.getEventCode(event);
        iter.reset(s, e);
        if (!iter.advance()) {
          if (dump == null) {
            dump = factory.createStateProxy(":dump");
            states.add(dump);
          }
          final TransitionProxy trans =
            factory.createTransitionProxy(state, event, dump);
          transitions.add(trans);
        }
      }
    }

    final String name = spec.getName();
    final EventProxy defaultMarking = getUsedDefaultMarking();
    if (dump != null & !events.contains(defaultMarking)) {
      final Collection<TransitionProxy> newTransitions =
        new ArrayList<TransitionProxy>();
      final Collection<EventProxy> newEvents =
        new ArrayList<EventProxy>(numEvents + 1);
      newEvents.addAll(events);
      newEvents.add(defaultMarking);
      final Collection<StateProxy> newStates =
        new ArrayList<StateProxy>(numStates + 1);
      final HashMap<StateProxy,StateProxy> mapStates =
        new HashMap<StateProxy,StateProxy>(numStates + 1);
      for (final StateProxy state : spec.getStates()) {
        final Collection<EventProxy> propositions = state.getPropositions();
        final Collection<EventProxy> newPropostions =
          new ArrayList<EventProxy>(propositions.size() + 1);
        newPropostions.addAll(propositions);
        newPropostions.add(defaultMarking);
        final StateProxy newState =
          factory.createStateProxy(state.getName(), state.isInitial(),
                                   newPropostions);
        newStates.add(newState);
        mapStates.put(state, newState);
      }
      newStates.add(dump);
      mapStates.put(dump, dump);
      for (final TransitionProxy trans : transitions) {
        final StateProxy sourceState = trans.getSource();
        final StateProxy targetState = trans.getTarget();
        final EventProxy event = trans.getEvent();
        final TransitionProxy newTransition =
          factory.createTransitionProxy(mapStates.get(sourceState), event,
                                        mapStates.get(targetState));
        newTransitions.add(newTransition);
      }
      return factory.createAutomatonProxy(name, ComponentKind.PLANT,
                                          newEvents, newStates,
                                          newTransitions);
    } else {
      return factory.createAutomatonProxy(name, ComponentKind.PLANT, events,
                                          states, transitions);
    }
  }

  @Override
  protected SynthesisEventInfo createEventInfo(final EventProxy event)
  {
    return new SynthesisEventInfo(event);
  }

  @Override
  protected boolean isPermissibleCandidate(final List<AutomatonProxy> automata)
  {
    return super.isPermissibleCandidate(automata)
           && automata.size() < getCurrentAutomata().size();
  }

  /**
   * Returns whether failure events are considered in abstraction.
   * @return <CODE>true</CODE> if failure events have been enabled by
   *         configuration (which they are by default).
   */
  @Override
  protected boolean isUsingFailingEvents()
  {
    return isFailingEventsEnabled();
  }


  //#########################################################################
  //# Debugging
  void reportAbstractionResult(final AutomatonProxy aut,
                               final AutomatonProxy dist)
  {
    final Logger logger = LogManager.getLogger();
    if (logger.isDebugEnabled()) {
      /*
       * final boolean nonblocking = AnalysisTools.isNonBlocking(aut); final
       * String msg1 = "Simplified automaton is " + (nonblocking ?
       * "nonblocking." : "BLOCKING."); logger.debug(msg1);
       */
      if (dist != null) {
        final String msg2 =
          "Creating distinguisher '" + dist.getName() + "' with "
            + dist.getStates().size() + " states.";
        logger.debug(msg2);
      }
    }
  }

  void reportSupervisor(final String kind,
                        final ListBufferTransitionRelation sup)
  {
    final Logger logger = LogManager.getLogger();
    if (logger.isDebugEnabled() && sup != null) {
      final String msg =
        "Got " + kind + " supervisor '" + sup.getName() + "' with " +
        sup.getNumberOfReachableStates() + " states.";
      logger.debug(msg);
    }
  }


  //#########################################################################
  //# Inner Class SynthesisEventInfo
  /**
   * An event information record for compositional synthesis. In compositional
   * synthesis, there are no tau events, yet all events are subject to
   * selfloop removal. Also, only uncontrollable events are considered
   * as failing.
   */
  protected class SynthesisEventInfo extends EventInfo
  {
    //#######################################################################
    //# Constructor
    protected SynthesisEventInfo(final EventProxy event)
    {
      super(event);
    }

    //#######################################################################
    //# Event Status
    @Override
    protected boolean canBeTau()
    {
      return false;
    }

    @Override
    protected boolean isSubjectToSelfloopRemoval()
    {
      return true;
    }

    @Override
    protected boolean isFailing()
    {
      if (super.isFailing()) {
        final KindTranslator translator = getKindTranslator();
        final EventProxy event = getEvent();
        return translator.getEventKind(event) == EventKind.UNCONTROLLABLE;
      } else {
        return false;
      }
    }
  }


  //#########################################################################
  //# Data Members
  private boolean mNonblockingSynthesis = true;
  private boolean mNondeterminismEnabled = false;
  private String mOutputName = "output";

}
