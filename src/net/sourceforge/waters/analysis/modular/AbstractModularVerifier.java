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

package net.sourceforge.waters.analysis.modular;

import gnu.trove.set.hash.THashSet;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import net.sourceforge.waters.analysis.abstraction.TraceFinder;
import net.sourceforge.waters.analysis.tr.ListBufferTransitionRelation;
import net.sourceforge.waters.analysis.tr.TRAutomatonProxy;
import net.sourceforge.waters.model.analysis.AnalysisAbortException;
import net.sourceforge.waters.model.analysis.AnalysisConfigurationException;
import net.sourceforge.waters.model.analysis.AnalysisException;
import net.sourceforge.waters.model.analysis.OverflowException;
import net.sourceforge.waters.model.analysis.VerificationResult;
import net.sourceforge.waters.model.analysis.des.AbstractModelAnalyzerFactory;
import net.sourceforge.waters.model.analysis.des.AbstractModelVerifier;
import net.sourceforge.waters.model.analysis.des.ModelVerifier;
import net.sourceforge.waters.model.analysis.kindtranslator.KindTranslator;
import net.sourceforge.waters.model.base.ComponentKind;
import net.sourceforge.waters.model.base.EventKind;
import net.sourceforge.waters.model.base.ProxyTools;
import net.sourceforge.waters.model.base.WatersRuntimeException;
import net.sourceforge.waters.model.des.AutomatonProxy;
import net.sourceforge.waters.model.des.AutomatonTools;
import net.sourceforge.waters.model.des.CounterExampleProxy;
import net.sourceforge.waters.model.des.EventProxy;
import net.sourceforge.waters.model.des.ProductDESProxy;
import net.sourceforge.waters.model.des.ProductDESProxyFactory;
import net.sourceforge.waters.model.des.StateProxy;
import net.sourceforge.waters.model.des.TraceProxy;
import net.sourceforge.waters.model.des.TraceStepProxy;
import net.sourceforge.waters.model.options.BooleanOption;
import net.sourceforge.waters.model.options.ChainedAnalyzerOption;
import net.sourceforge.waters.model.options.EnumOption;
import net.sourceforge.waters.model.options.LeafOptionPage;
import net.sourceforge.waters.model.options.Option;
import net.sourceforge.waters.model.options.PositiveIntOption;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


/**
 * <P>A common superclass for all modular or verifiers.</P>
 *
 * <P>This class implements the core algorithm of incremental or modular
 * verification. Subclasses are used to verify different property
 * such as controllability ({@link ModularControllabilityChecker}),
 * language inclusion ({@link ModularLanguageInclusionChecker}),
 * or coobservability.</P>
 *
 * <P><I>Reference:</I><BR>
 * Bertil A. Brandin, Robi Malik, Petra Malik. Incremental verification
 * and synthesis of discrete-event systems guided by counter-examples.
 * IEEE Transactions on Control Systems Technology,
 * <STRONG>12</STRONG>&nbsp;(3), 387&ndash;401, 2004.</P>
 *
 * @author Simon Ware, Robi Malik
 */

abstract class AbstractModularVerifier
  extends AbstractModelVerifier
{

  //#########################################################################
  //# Constructors
  public AbstractModularVerifier(final ProductDESProxy model,
                                 final ProductDESProxyFactory factory,
                                 final KindTranslator translator,
                                 final ModelVerifier mono)
  {
    super(model, factory, translator);
    mMonolithicVerifier = mono;
    mStartsWithSmallestSpec = true;
    mHeuristicMethod = HeuristicFactory.Method.MaxCommonEvents;
    mHeuristicPreference = HeuristicFactory.Preference.NOPREF;
    mCollectsFailedSpecs = false;
  }


  //#########################################################################
  //# Configuration
  public ModelVerifier getMonolithicVerifier()
  {
    return mMonolithicVerifier;
  }

  public void setMonolithicVerifier(final ModelVerifier verifier)
  {
    mMonolithicVerifier = verifier;
  }

  public void setStartsWithSmallestSpec(final boolean least)
  {
    mStartsWithSmallestSpec = least;
  }

  public boolean getStartsWithSmallestSpec()
  {
    return mStartsWithSmallestSpec;
  }

  public HeuristicFactory.Preference getHeuristicPreference()
  {
    return mHeuristicPreference;
  }

  public void setHeuristicPreference(final HeuristicFactory.Preference pref)
  {
    mHeuristicPreference = pref;
  }

  public HeuristicFactory.Method getHeuristicMethod()
  {
    return mHeuristicMethod;
  }

  public void setHeuristicMethod(final HeuristicFactory.Method method)
  {
    mHeuristicMethod = method;
  }

  public void setCollectsFailedSpecs(final boolean collect)
  {
    mCollectsFailedSpecs = collect;
  }

  public boolean getCollectsFailedSpecs()
  {
    return mCollectsFailedSpecs;
  }


  //#########################################################################
  //# Interface net.sourceforge.waters.model.analysis.ModelAnalyser
  @Override
  public List<Option<?>> getOptions(final LeafOptionPage db)
  {
    final List<Option<?>> options = super.getOptions(db);
    db.prepend(options, ModularModelVerifierFactory.
               OPTION_AbstractModularVerifier_CollectsFailedSpecs);
    db.prepend(options, ModularModelVerifierFactory.
               OPTION_AbstractModularVerifier_HeuristicMethod);
    db.prepend(options, ModularModelVerifierFactory.
               OPTION_AbstractModularVerifier_HeuristicPreference);
    db.prepend(options, ModularModelVerifierFactory.
               OPTION_AbstractModularVerifier_StartsWithSmallestSpec);
    db.append(options, AbstractModelAnalyzerFactory.
              OPTION_ModelAnalyzer_FinalStateLimit);
    db.append(options, AbstractModelAnalyzerFactory.
              OPTION_ModelAnalyzer_FinalTransitionLimit);
    return options;
  }

  @SuppressWarnings("unchecked")
  @Override
  public void setOption(final Option<?> option)
  {
    if (option.hasID(ModularModelVerifierFactory.
                     OPTION_AbstractModularVerifier_StartsWithSmallestSpec)) {
      final BooleanOption boolOption = (BooleanOption) option;
      setStartsWithSmallestSpec(boolOption.getBooleanValue());
    } else if (option.hasID(ModularModelVerifierFactory.
                            OPTION_AbstractModularVerifier_HeuristicPreference)) {
      final EnumOption<HeuristicFactory.Preference> enumOption =
        (EnumOption<HeuristicFactory.Preference>) option;
      setHeuristicPreference(enumOption.getValue());
    } else if (option.hasID(ModularModelVerifierFactory.
                            OPTION_AbstractModularVerifier_HeuristicMethod)) {
      final EnumOption<HeuristicFactory.Method> enumOption =
        (EnumOption<HeuristicFactory.Method>) option;
      setHeuristicMethod(enumOption.getValue());
    } else if (option.hasID(ModularModelVerifierFactory.
                            OPTION_AbstractModularVerifier_CollectsFailedSpecs)) {
      final BooleanOption boolOption = (BooleanOption) option;
      setCollectsFailedSpecs(boolOption.getBooleanValue());
    } else if (option.hasID(AbstractModelAnalyzerFactory.
                            OPTION_ModelAnalyzer_FinalStateLimit)) {
      final PositiveIntOption intOption = (PositiveIntOption) option;
      setNodeLimit(intOption.getIntValue());
    } else if (option.hasID(AbstractModelAnalyzerFactory.
                            OPTION_ModelAnalyzer_FinalTransitionLimit)) {
      final PositiveIntOption intOption = (PositiveIntOption) option;
      setTransitionLimit(intOption.getIntValue());
    } else if (option.hasID(ModularModelVerifierFactory.
                            OPTION_ModularControllabilityChecker_Chain) ||
               option.hasID(ModularModelVerifierFactory.
                            OPTION_ModularCoobservabilityChecker_Chain) ||
               option.hasID(ModularModelVerifierFactory.
                            OPTION_ModularLanguageInclusionChecker_Chain)) {
      try {
        final ChainedAnalyzerOption chain = (ChainedAnalyzerOption) option;
        final ProductDESProxyFactory factory = getFactory();
        final ModelVerifier secondaryAnalyzer =
          (ModelVerifier) chain.createAndConfigureModelAnalyzer(factory);
        setMonolithicVerifier(secondaryAnalyzer);
      } catch (final AnalysisConfigurationException exception) {
        throw new WatersRuntimeException(exception);
      }
    } else {
      super.setOption(option);
    }
  }

  @Override
  public boolean supportsNondeterminism()
  {
    return mMonolithicVerifier.supportsNondeterminism();
  }


  //#########################################################################
  //# Hooks
  protected boolean isMultiSpecsEnabled()
  {
    return true;
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
  //# Invocation
  @Override
  public void setUp()
    throws AnalysisException
  {
    super.setUp();

    final ProductDESProxy des = getModel();
    final Collection<AutomatonProxy> automata = des.getAutomata();
    final int numAutomata = automata.size();
    final Comparator<AutomatonProxy> comparator =
      new SpecComparator(mStartsWithSmallestSpec);
    mRemainingSpecs = new TreeSet<>(comparator);
    mRemainingPlants = new THashSet<>(numAutomata);
    mSpecsConvertedToPlants = new THashSet<>(numAutomata);
    mAutomataMap = new LinkedHashMap<>(numAutomata);

    final KindTranslator translator = getKindTranslator();
    for (final AutomatonProxy aut : automata) {
      final ComponentKind kind = translator.getComponentKind(aut);
      if (kind == ComponentKind.PLANT || kind == ComponentKind.SPEC) {
        final TRAutomatonProxy tr = TRAutomatonProxy.createTRAutomatonProxy
          (aut, translator, ListBufferTransitionRelation.CONFIG_SUCCESSORS);
        mAutomataMap.put(tr, aut);
        switch (kind) {
        case PLANT:
          mRemainingPlants.add(tr);
          break;
        case SPEC:
          mRemainingSpecs.add(tr);
          break;
        default:
          break;
        }
      }
    }

    final HeuristicFactory factory = HeuristicFactory.getInstance();
    final HeuristicTraceChecker checker = createHeuristicTraceChecker();
    mHeuristicEvaluator = factory.createEvaluator
      (mHeuristicPreference, mHeuristicMethod, translator, checker);
    mMonolithicVerifier.setNodeLimit(getNodeLimit());
    mMonolithicVerifier.setTransitionLimit(getTransitionLimit());
    mMonolithicVerifier.setCounterExampleEnabled(true);
    final KindTranslator subTranslator =
      new KindTranslator() {
        @Override
        public EventKind getEventKind(final EventProxy event)
        {
          return translator.getEventKind(event);
        }

        @Override
        public ComponentKind getComponentKind(final AutomatonProxy aut)
        {
          return mRemainingSpecs.contains(aut) ?
                 ComponentKind.SPEC : ComponentKind.PLANT;
        }
      };
    mMonolithicVerifier.setKindTranslator(subTranslator);
  }

  @Override
  public boolean run()
    throws AnalysisException
  {
    try {
      setUp();

      final int numAutomata = mRemainingSpecs.size() + mRemainingPlants.size();
      final List<TRAutomatonProxy> subsystem = new ArrayList<>(numAutomata);
      final Logger logger = LogManager.getLogger();

      specLoop:
      while (!mRemainingSpecs.isEmpty()) {
        checkAbort();
        final TRAutomatonProxy spec = mRemainingSpecs.first();
        logger.debug("Checking specification {} ...", spec.getName());
        subsystem.clear();
        subsystem.add(spec);
        final Set<TRAutomatonProxy> uncomposedSpecs;
        if (isMultiSpecsEnabled()) {
          uncomposedSpecs = new TreeSet<>(mRemainingSpecs);
          uncomposedSpecs.remove(spec);
        } else {
          uncomposedSpecs = Collections.emptySet();
        }
        final Set<TRAutomatonProxy> uncomposedPlants =
          new TreeSet<>(mRemainingPlants);
        final Set<TRAutomatonProxy> uncomposedSpecPlants =
          new TreeSet<>(mSpecsConvertedToPlants);
        ProductDESProxy subDES = createDES(subsystem);
        mMonolithicVerifier.setModel(subDES);

        while (!mMonolithicVerifier.run()) {
          final VerificationResult subResult =
            mMonolithicVerifier.getAnalysisResult();
          recordStats(subResult);
          final CounterExampleProxy counter = subResult.getCounterExample();
          final Collection<TRAutomatonProxy> selectedAutomata =
            mHeuristicEvaluator.collectNonAccepting(subDES, counter,
                                                    uncomposedPlants,
                                                    uncomposedSpecPlants,
                                                    uncomposedSpecs);
          checkAbort();
          if (selectedAutomata.isEmpty()) {
            logger.debug("Specification {} has failed.", spec.getName());
            if (!getAnalysisResult().isFinished()) {
              final CounterExampleProxy extended = extendToModel(counter);
              setFailedResult(extended);
            }
            if (mCollectsFailedSpecs) {
              collectFailedSpecs(counter, spec);
              continue specLoop;
            } else {
              return false;
            }
          }
          if (logger.isDebugEnabled()) {
            final KindTranslator translator =
              mMonolithicVerifier.getKindTranslator();
            for (final AutomatonProxy aut : selectedAutomata) {
              logger.debug("Adding {} {} as {} ...",
                           aut.getKind().name().toLowerCase(),
                           aut.getName(),
                           translator.getComponentKind(aut).name().toLowerCase());
            }
          }
          subsystem.addAll(selectedAutomata);
          uncomposedPlants.removeAll(selectedAutomata);
          uncomposedSpecPlants.removeAll(selectedAutomata);
          uncomposedSpecs.removeAll(selectedAutomata);
          subDES = createDES(subsystem);
          configureMonolithicVerifier(subDES);
        }

        final VerificationResult subResult =
          mMonolithicVerifier.getAnalysisResult();
        recordStats(subResult);
        for (final TRAutomatonProxy aut : subsystem) {
          if (mRemainingSpecs.remove(aut)) {
            mSpecsConvertedToPlants.add(aut);
          }
        }
      }

      if (getAnalysisResult().isFinished()) {
        return false;
      } else {
        logger.debug("All specifications have passed, verification successful.");
        return setSatisfiedResult();
      }
    } catch (final AnalysisException exception) {
      throw setExceptionResult(exception);
    } catch (final OutOfMemoryError error) {
      System.gc();
      final OverflowException overflow = new OverflowException(error);
      throw setExceptionResult(overflow);
    } catch (final StackOverflowError error) {
      final OverflowException overflow = new OverflowException(error);
      throw setExceptionResult(overflow);
    } finally {
      tearDown();
    }
  }

  @Override
  public void tearDown()
  {
    super.tearDown();
    mHeuristicEvaluator = null;
    mRemainingSpecs = null;
    mRemainingPlants = null;
    mSpecsConvertedToPlants = null;
    mAutomataMap = null;
  }


  //#########################################################################
  //# Hooks
  protected void configureMonolithicVerifier(final ProductDESProxy des)
  {
    mMonolithicVerifier.setModel(des);
  }


  //#########################################################################
  //# Trace Computation
  protected HeuristicTraceChecker createHeuristicTraceChecker()
  {
    return new DefaultHeuristicTraceChecker();
  }

  protected CounterExampleProxy extendToModel(final CounterExampleProxy counter)
    throws AnalysisAbortException, OverflowException
  {
    if (isCounterExampleEnabled()) {
      final Set<AutomatonProxy> traceAutomata =
        new THashSet<>(counter.getAutomata());
      final List<TraceProxy> oldTraces = counter.getTraces();
      final List<TraceProxy> newTraces = new ArrayList<>(oldTraces.size());
      for (final TraceProxy oldTrace : oldTraces) {
        final TraceProxy newTrace = extendToModel(oldTrace, traceAutomata);
        newTraces.add(newTrace);
        checkAbort();
      }
      final Collection<AutomatonProxy> newAutomata = mAutomataMap.values();
      return createExtendedCounterexample(counter, newAutomata, newTraces);
    } else {
      return null;
    }
  }

  protected abstract CounterExampleProxy createExtendedCounterexample
    (CounterExampleProxy counter,
     Collection<AutomatonProxy> newAutomata,
     List<TraceProxy> newTraces);

  private TraceProxy extendToModel(final TraceProxy trace,
                                   final Set<AutomatonProxy> traceAutomata)
    throws AnalysisAbortException, OverflowException
  {
    final ProductDESProxyFactory factory = getFactory();
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
      for (final Map.Entry<TRAutomatonProxy,AutomatonProxy> entry :
           mAutomataMap.entrySet()) {
        final TRAutomatonProxy tr = entry.getKey();
        final AutomatonProxy aut = entry.getValue();
        if (traceAutomata.contains(tr)) {
          final StateProxy oldState = oldMap.get(tr);
          if (oldState != null) {
            final StateProxy newState = tr.getOriginalState(oldState);
            if (newMap == null) {
              newMap = new HashMap<>(mAutomataMap.size());
            }
            newMap.put(aut, newState);
          }
        } else {
          final ComponentKind kind = translator.getComponentKind(aut);
          final TraceFinder finder =
            mHeuristicEvaluator.getTraceFinder(tr, kind);
          final TraceFinder.Result result = finder.examine(trace);
          if (kind == ComponentKind.SPEC &&
              depth > result.getTotalAcceptedSteps()) {
            // Found nonaccepting spec --- trace ends here.
            endOfTrace = true;
            continue;
          }
          final StateProxy state = result.getStateAt(0, depth);
          if (state != null) {
            if (newMap == null) {
              newMap = new HashMap<>(mAutomataMap.size());
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
    final String name = trace.getName();
    final int loopIndex = trace.getLoopIndex();
    return factory.createTraceProxy(name, newSteps, loopIndex);
  }

  private void collectFailedSpecs(final CounterExampleProxy counter,
                                  final TRAutomatonProxy spec)
  {
    final ModularVerificationResult verificationResult = getAnalysisResult();
    if (isMultiSpecsEnabled()) {
      final KindTranslator translator = getKindTranslator();
      for (final AutomatonProxy counterAut : counter.getAutomata()) {
        final AutomatonProxy aut = mAutomataMap.get(counterAut);
        if (translator.getComponentKind(aut) == ComponentKind.SPEC) {
          final TraceFinder finder =
            mHeuristicEvaluator.getTraceFinder(aut, ComponentKind.SPEC);
          final TraceFinder.Result result = finder.examine(counter);
          if (!result.isAccepted()) {
            final TRAutomatonProxy tr = (TRAutomatonProxy) counterAut;
            mRemainingSpecs.remove(tr);
            verificationResult.addFailedSpec(aut);
          }
        }
      }
    } else {
      mRemainingSpecs.remove(spec);
      final AutomatonProxy aut = mAutomataMap.get(spec);
      verificationResult.addFailedSpec(aut);
    }
  }


  //#########################################################################
  //# Collecting Statistics
  protected void recordStats(final VerificationResult subresult)
  {
    final ModularVerificationResult result = getAnalysisResult();
    result.updateNumberOfAutomata(subresult.getTotalNumberOfAutomata());
    result.updateNumberOfStates(subresult.getTotalNumberOfStates());
    result.updateNumberOfTransitions(subresult.getTotalNumberOfTransitions());
  }


  //#########################################################################
  //# Auxiliary Methods
  private ProductDESProxy createDES(final List<TRAutomatonProxy> subsystem)
  {
    final String name = getModel().getName();
    final TRAutomatonProxy spec = subsystem.get(0);
    final StringBuilder builder = new StringBuilder();
    builder.append("Automatically generated by ");
    builder.append(ProxyTools.getShortClassName(this));
    builder.append(" to check specification '");
    builder.append(spec.getName());
    builder.append("' of product DES '");
    builder.append(name);
    builder.append("'.");
    final String comment = builder.toString();
    final ProductDESProxyFactory factory = getFactory();
    return AutomatonTools.createProductDESProxy(name, comment,
                                                subsystem, factory);
  }


  //#########################################################################
  //# Inner Class
  private static class SpecComparator implements Comparator<AutomatonProxy>
  {
    //#######################################################################
    //# Constructor
    private SpecComparator(final boolean ascending)
    {
      mDefault = ascending ? -1 : 1;
    }

    //#######################################################################
    //# Interface java.util.Comparator<AutomatonProxy>
    @Override
    public int compare(final AutomatonProxy aut1, final AutomatonProxy aut2)
    {
      final int numStates1 = aut1.getStates().size();
      final int numStates2 = aut2.getStates().size();
      if (numStates1 < numStates2) {
        return mDefault;
      } else if (numStates1 > numStates2) {
        return -mDefault;
      }
      final int numTrans1 = aut1.getTransitions().size();
      final int numTrans2 = aut2.getTransitions().size();
      if (numTrans1 < numTrans2) {
        return mDefault;
      } else if (numTrans1 > numTrans2) {
        return -mDefault;
      }
      final int numEvents1 = aut1.getEvents().size();
      final int numEvents2 = aut2.getEvents().size();
      if (numEvents1 < numEvents2) {
        return mDefault;
      } else if (numEvents1 > numEvents2) {
        return -mDefault;
      }
      return aut1.compareTo(aut2);
    }

    //#######################################################################
    //# Data Members
    private final int mDefault;
  }


  //#########################################################################
  //# Data Members
  // Configuration
  private ModelVerifier mMonolithicVerifier;
  private boolean mStartsWithSmallestSpec;
  private HeuristicFactory.Method mHeuristicMethod;
  private HeuristicFactory.Preference mHeuristicPreference;
  private boolean mCollectsFailedSpecs;

  // Data structures during run
  private HeuristicEvaluator mHeuristicEvaluator;
  private SortedSet<TRAutomatonProxy> mRemainingSpecs;
  private Set<TRAutomatonProxy> mRemainingPlants;
  private Set<TRAutomatonProxy> mSpecsConvertedToPlants;
  private Map<TRAutomatonProxy,AutomatonProxy> mAutomataMap;

}
