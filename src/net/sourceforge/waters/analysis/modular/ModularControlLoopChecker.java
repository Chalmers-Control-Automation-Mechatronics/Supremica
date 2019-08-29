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

package net.sourceforge.waters.analysis.modular;

import gnu.trove.set.hash.THashSet;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.sourceforge.waters.analysis.monolithic.MonolithicSCCControlLoopChecker;
import net.sourceforge.waters.analysis.options.EnumParameter;
import net.sourceforge.waters.analysis.options.EventListParameter;
import net.sourceforge.waters.analysis.options.IntParameter;
import net.sourceforge.waters.analysis.options.Parameter;
import net.sourceforge.waters.analysis.options.ParameterIDs;
import net.sourceforge.waters.model.analysis.AnalysisException;
import net.sourceforge.waters.model.analysis.AnalysisResult;
import net.sourceforge.waters.model.analysis.KindTranslator;
import net.sourceforge.waters.model.analysis.des.AbstractControlLoopChecker;
import net.sourceforge.waters.model.base.ComponentKind;
import net.sourceforge.waters.model.base.EventKind;
import net.sourceforge.waters.model.des.AutomatonProxy;
import net.sourceforge.waters.model.des.AutomatonTools;
import net.sourceforge.waters.model.des.EventProxy;
import net.sourceforge.waters.model.des.LoopCounterExampleProxy;
import net.sourceforge.waters.model.des.ProductDESProxy;
import net.sourceforge.waters.model.des.ProductDESProxyFactory;
import net.sourceforge.waters.model.des.StateProxy;
import net.sourceforge.waters.model.des.TraceProxy;
import net.sourceforge.waters.model.des.TraceStepProxy;


/**
 * The modular control-loop check algorithm.
 *
 * <P><I>Reference:</I><BR>
 * Petra Malik, Robi Malik. Modular control-loop detection. Proc. 8th
 * International Workshop on Discrete Event Systems, WODES&nbsp;2006,
 * 119-124, Ann Arbor, MI, USA, 2006.</P>
 *
 * @author Andrew Holland
 */

public class ModularControlLoopChecker
  extends AbstractControlLoopChecker
{

  //#########################################################################
  //# Constructors
  /**
   * Creates a new control-loop checker without a model and with default
   * kind translator.
   * @param  factory    Factory used for trace construction.
   */
  public ModularControlLoopChecker(final ProductDESProxyFactory factory)
  {
    super(factory);
  }

  /**
   * Creates a new control-loop checker to check for loops with respect to
   * an alternative set of events.
   * @param  factory    Factory used for trace construction.
   * @param  translator Kind translator to determine loop events. The checker
   *                    will look for loops consisting of events designated
   *                    as controllable by the kind translator.
   */
  public ModularControlLoopChecker(final ProductDESProxyFactory factory,
                                   final KindTranslator translator)
  {
    super(factory, translator);
  }

  /**
   * Creates a new control-loop checker to check whether the given model
   * is control-loop free.
   * @param  model      The model to be checked by this control-loop checker.
   * @param  factory    Factory used for trace construction.
   */
  public ModularControlLoopChecker(final ProductDESProxy model,
                                   final ProductDESProxyFactory factory)
  {
    super(model, factory);
  }

  /**
   * Creates a new control-loop checker to check whether the given model
   * is loop free with respect to an alternative set of events.
   * @param  model      The model to be checked by this control-loop checker.
   * @param  factory    Factory used for trace construction.
   * @param  translator Kind translator to determine loop events. The checker
   *                    will look for loops consisting of events designated
   *                    as controllable by the kind translator.
   */
  public ModularControlLoopChecker(final ProductDESProxy model,
                                   final ProductDESProxyFactory factory,
                                   final KindTranslator translator)
  {
    super(model, factory, translator);
  }


  //#########################################################################
  //# Configuration
  /**
   * Sets the heuristic to determine which components to include in
   * subsequent verification attempts based on the counterexample from the
   * previous attempt.
   */
  public void setMergeVersion(final AutomataGroup.MergeVersion m)
  {
    AutomataGroup.setMergeVersion(m);
  }

  /**
   * Gets the heuristic to determine which components to include in
   * subsequent verification attempts based on the counterexample from the
   * previous attempt.
   * @see #setMergeVersion(AutomataGroup.MergeVersion)
   */
  public AutomataGroup.MergeVersion getMergeVersion()
  {
    return AutomataGroup.getMergeVersion();
  }

  public void setSelectVersion(final AutomataGroup.SelectVersion s)
  {
    AutomataGroup.setSelectVersion(s);
  }


  //#########################################################################
  //# Interface net.sourceforge.waters.model.analysis.ModelAnalyzer
  @Override
  public List<Parameter> getParameters()
  {
    final List<Parameter> list = super.getParameters();
    list.add(new EnumParameter<AutomataGroup.MergeVersion>
        (ParameterIDs.ModularControlLoopChecker_MergeVersion) {
      @Override
      public void commitValue() {
        setMergeVersion(getValue());
      }
    });
    list.add(new IntParameter
        (ParameterIDs.ModularControlLoopChecker_NodeLimit) {
      @Override
      public void commitValue() {
        setNodeLimit(getValue());
      }
    });
    list.add(new IntParameter
        (ParameterIDs.ModularControlLoopChecker_TransitionLimit) {
      @Override
      public void commitValue() {
        setTransitionLimit(getValue());
      }
    });

    list.add(new EventListParameter(ParameterIDs.testControlLoopHideParam) {
      @Override
      public void commitValue() {
        setKindTranslator(getKindTranslator());
      }
    });

    return list;
  }


  //#########################################################################
  //# Invocation
  @Override
  public boolean run() throws AnalysisException
  {
    try {
      setUp();
      final AnalysisResult result = getAnalysisResult();
      if (result.isFinished()) {
        return result.isSatisfied();
      }

      boolean removedLoopEvents = false;
      while (true)
      {
        do
        {
          removedLoopEvents = false;
          for (final AutomataGroup group : mAutoSets)
          {
            checkAbort();
            group.run(mMonolithicVerifier, mNodesRemaining);
            updateResult(group);
            final Collection<EventProxy> nonLoop = group.getNonLoopEvents();
            if (mLoopEvents.removeAll(nonLoop))
            {
              removedLoopEvents = true;
              for (final AutomataGroup invalidate : mAutoSets)
                invalidate.isChanged(nonLoop);
            }
            if (mLoopEvents.size() == 0) {
              setSatisfiedResult();
              return true;
            }
            mTranslator.removeLoopEvents(nonLoop);
            final LoopCounterExampleProxy loop = testOne(group);
            if (loop != null) {
              setLiftedLoopTrace(loop);
              return false;
            }
          }
        }
        while (removedLoopEvents);
        final LoopCounterExampleProxy loop = testAll();
        if (loop != null) {
          return setLiftedLoopTrace(loop);
        }
        AutomataGroup primary = null;
        int bestScore = Integer.MIN_VALUE;
        for (final AutomataGroup group : mAutoSets) {
          if (group.getScore() > bestScore) {
            bestScore = group.getScore();
            primary = group;
          }
        }
        if (primary == null) {
          return setSatisfiedResult();
        }
        bestScore = Integer.MIN_VALUE;
        AutomataGroup bestGroup = null;
        assert primary.getTrace() != null : "Primary has no trace!";
        for (final AutomataGroup checkLoop : mAutoSets) {
          if (checkLoop != primary) {
            final int score = checkLoop.isControlLoop(primary, mTranslator);
            if (score > bestScore) {
              bestGroup = checkLoop;
              bestScore = score;
            }
          }
        }
        assert bestGroup != null : "Could not find two automata for merging!";
        checkAbort();
        mAutoSets.remove(bestGroup);
        primary.merge(bestGroup);
        mTotalCompositions++;
        updateResult(primary);
      }
    } finally {
      tearDown();
    }
  }


  //#########################################################################
  //# Interface net.sourceforge.waters.model.analysis.Abortable
  @Override
  public void requestAbort()
  {
    super.requestAbort();
    if (mMonolithicVerifier != null) {
      mMonolithicVerifier.requestAbort();
    }
  }

  @Override
  public void resetAbort()
  {
    super.resetAbort();
    if (mMonolithicVerifier != null) {
      mMonolithicVerifier.resetAbort();
    }
  }


  //#########################################################################
  //# Interface net.sourceforge.waters.model.analysis.ModelAnalyzer
  @Override
  public boolean supportsNondeterminism()
  {
    return false;
  }

  @Override
  protected void setUp() throws AnalysisException
  {
    super.setUp();

    // Collect automata sets and used loop events
    final ProductDESProxy model = getModel();
    final KindTranslator translator = getKindTranslator();
    final Collection<EventProxy> events = model.getEvents();
    mLoopEvents = new THashSet<>(events.size());
    final Collection<AutomatonProxy> automata = model.getAutomata();
    mAutoSets = new ArrayList<>(automata.size());
    for (final AutomatonProxy aut: automata) {
      switch (translator.getComponentKind(aut)) {
      case PLANT:
      case SPEC:
        if (AutomatonTools.getFirstInitialState(aut) == null) {
          setSatisfiedResult();
          return;
        }
        mAutoSets.add(new AutomataGroup(aut));
        for (final EventProxy event : aut.getEvents()) {
          if (translator.getEventKind(event) == EventKind.CONTROLLABLE) {
            mLoopEvents.add(event);
          }
        }
        // fall through
      default:
        break;
      }
    }

    // If some controllable event is not used in any automaton: control loop!
    for (final EventProxy event : model.getEvents()) {
      if (translator.getEventKind(event) == EventKind.CONTROLLABLE &&
          !mLoopEvents.contains(event)) {
        setTrivialLoopTrace(event);
        return;
      }
    }
    // Otherwise if there are no controllable events: control-loop free!
    if (mLoopEvents.isEmpty()) {
      setSatisfiedResult();
      return;
    }

    mTranslator = new ManipulativeTranslator(translator);
    mMonolithicVerifier =
      new MonolithicSCCControlLoopChecker(mTranslator, getFactory());
    mPeakAutomata = 0;
    mPeakStates = 0;
    mPeakTransitions = 0;
    mTotalAutomata = getModel().getAutomata().size();
    mTotalStates = 0;
    mTotalTransitions = 0;
    mTotalCompositions = 0;
  }

  @Override
  protected void tearDown()
  {
    super.tearDown();
    mLoopEvents = null;
    mTranslator = null;
    mMonolithicVerifier = null;
    mAutoSets = null;
    mPeakAutomata = -1;
    mPeakStates = -1;
    mPeakTransitions = -1;
    mTotalAutomata = -1;
    mTotalStates = -1;
    mTotalTransitions = -1;
    mTotalCompositions = -1;
  }

  @Override
  public LoopResult createAnalysisResult()
  {
    return new LoopResult(this);
  }

  @Override
  public LoopResult getAnalysisResult()
  {
    return (LoopResult) super.getAnalysisResult();
  }

  @Override
  protected void addStatistics()
  {
    super.addStatistics();
    final LoopResult result = getAnalysisResult();
    result.setPeakNumberOfAutomata(mPeakAutomata);
    result.setPeakNumberOfStates(mPeakStates);
    result.setPeakNumberOfTransitions(mPeakTransitions);
    result.setTotalNumberOfAutomata(mTotalAutomata);
    result.setTotalNumberOfStates(mTotalStates);
    result.setTotalNumberOfTransitions(mTotalTransitions);
    result.setNumberOfCompositions(mTotalCompositions);
  }


  //#########################################################################
  //# Auxiliary Methods
  private void updateResult(final AutomataGroup newGroup)
  {
    if (newGroup.rerun()) {
      if (mPeakAutomata < newGroup.getStatistics().getTotalNumberOfAutomata() || mPeakAutomata == -1)
        mPeakAutomata = newGroup.getStatistics().getTotalNumberOfAutomata();
      if (mPeakStates < newGroup.getStatistics().getTotalNumberOfStates() || mPeakStates == -1)
        mPeakStates = newGroup.getStatistics().getTotalNumberOfStates();
      if (mPeakTransitions < newGroup.getStatistics().getTotalNumberOfTransitions() || mPeakTransitions == -1)
        mPeakTransitions = newGroup.getStatistics().getTotalNumberOfTransitions();
      mTotalStates += newGroup.getStatistics().getTotalNumberOfStates();
      mTotalTransitions += newGroup.getStatistics().getTotalNumberOfTransitions();
    }
  }

  private LoopCounterExampleProxy testAll()
  {
    LoopCounterExampleProxy output = null;
    for (final AutomataGroup auto : mAutoSets) {
      output = testOne(auto);
      if (output != null) {
        return output;
      }
    }
    return null;
  }

  private LoopCounterExampleProxy testOne(final AutomataGroup group)
  {
    if (group.getTrace() != null) {
      for (final AutomataGroup checkLoop : mAutoSets) {
        if (checkLoop != group &&
            checkLoop.isControlLoop(group,
                                    mTranslator) != Integer.MIN_VALUE) {
          return null;
        }
      }
      return group.getCounterExample();
    }
    return null;
  }

  private boolean setTrivialLoopTrace(final EventProxy event)
  {
    final ProductDESProxy model = getModel();
    final Collection<AutomatonProxy> automata = model.getAutomata();
    final Map<AutomatonProxy,StateProxy> stateMap = new HashMap<>(automata.size());
    for (final AutomatonProxy aut : automata) {
      final StateProxy state = AutomatonTools.getFirstInitialState(aut);
      stateMap.put(aut, state);
    }
    final List<TraceStepProxy> steps = new ArrayList<>(2);
    final ProductDESProxyFactory factory = getFactory();
    final TraceStepProxy step1 = factory.createTraceStepProxy(null, stateMap);
    steps.add(step1);
    final TraceStepProxy step2 = factory.createTraceStepProxy(event, stateMap);
    steps.add(step2);
    return setLoopTrace(null, automata, steps, 0);
  }

  private boolean setLiftedLoopTrace(final LoopCounterExampleProxy loop)
  {
    final String comment = loop.getComment();
    final Collection<AutomatonProxy> automata = loop.getAutomata();
    final TraceProxy trace = loop.getTrace();
    final List<TraceStepProxy> steps = trace.getTraceSteps();
    final int index = trace.getLoopIndex();
    return setLoopTrace(comment, automata, steps, index);
  }

  private boolean setLoopTrace(final String comment,
                               final Collection<AutomatonProxy> automata,
                               final List<TraceStepProxy> steps,
                               final int index)
  {
    final ProductDESProxyFactory factory = getFactory();
    final String name = getTraceName();
    final ProductDESProxy model = getModel();
    final TraceProxy trace = factory.createTraceProxy(steps, index);
    final LoopCounterExampleProxy counter =
      factory.createLoopCounterExampleProxy(name, comment, null, model,
                                            automata, trace);
    return setFailedResult(counter);
  }


  //#########################################################################
  //# Inner Class ManipulativeTranslator
  private class ManipulativeTranslator implements KindTranslator
  {

    public ManipulativeTranslator(final KindTranslator base)
    {
      mBase = base;
      mFauxUncontrollable = new THashSet<EventProxy>();
    }

    @SuppressWarnings("unused")
    private void removeLoopEvents(final EventProxy event)
    {
      mFauxUncontrollable.add(event);
    }

    public void removeLoopEvents(final Collection<EventProxy> event)
    {
      mFauxUncontrollable.addAll(event);
    }

    @Override
    public ComponentKind getComponentKind(final AutomatonProxy aut)
    {
      return mBase.getComponentKind(aut);
    }

    @Override
    public EventKind getEventKind(final EventProxy event)
    {
      if (mFauxUncontrollable.contains(event))
        return EventKind.UNCONTROLLABLE;
      else
        return mBase.getEventKind(event);
    }

    private final KindTranslator mBase;
    private final Set<EventProxy> mFauxUncontrollable;
  }


  //#########################################################################
  //# Data Members
  private ManipulativeTranslator mTranslator;
  private MonolithicSCCControlLoopChecker mMonolithicVerifier;
  private List<AutomataGroup> mAutoSets;
  private Set<EventProxy> mLoopEvents;
  private final int mNodesRemaining = 3000000;

  private int mPeakAutomata;
  private double mPeakStates;
  private double mPeakTransitions;
  private int mTotalAutomata;
  private double mTotalStates;
  private double mTotalTransitions;
  private int mTotalCompositions;
}
