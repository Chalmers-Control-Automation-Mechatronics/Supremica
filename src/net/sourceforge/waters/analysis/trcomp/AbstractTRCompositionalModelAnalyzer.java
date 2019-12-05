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

package net.sourceforge.waters.analysis.trcomp;

import gnu.trove.set.hash.THashSet;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Queue;

import net.sourceforge.waters.analysis.abstraction.ChainTRSimplifier;
import net.sourceforge.waters.analysis.abstraction.ObservationEquivalenceTRSimplifier;
import net.sourceforge.waters.analysis.abstraction.ObservationEquivalenceTRSimplifier.Equivalence;
import net.sourceforge.waters.analysis.abstraction.SpecialEventsFinder;
import net.sourceforge.waters.analysis.abstraction.SpecialEventsTRSimplifier;
import net.sourceforge.waters.analysis.abstraction.TRSimplificationListener;
import net.sourceforge.waters.analysis.abstraction.TauLoopRemovalTRSimplifier;
import net.sourceforge.waters.analysis.abstraction.TransitionRelationSimplifier;
import net.sourceforge.waters.analysis.compositional.CompositionalAnalysisResult;
import net.sourceforge.waters.analysis.compositional.NumericSelectionHeuristic;
import net.sourceforge.waters.analysis.compositional.SelectionHeuristic;
import net.sourceforge.waters.analysis.monolithic.AbstractTRSynchronousProductBuilder;
import net.sourceforge.waters.analysis.monolithic.TRSynchronousProductBuilder;
import net.sourceforge.waters.analysis.monolithic.TRSynchronousProductResult;
import net.sourceforge.waters.analysis.options.BooleanOption;
import net.sourceforge.waters.analysis.options.EnumOption;
import net.sourceforge.waters.analysis.options.FileOption;
import net.sourceforge.waters.analysis.options.Option;
import net.sourceforge.waters.analysis.options.OptionMap;
import net.sourceforge.waters.analysis.options.PositiveIntOption;
import net.sourceforge.waters.analysis.tr.DuplicateFreeQueue;
import net.sourceforge.waters.analysis.tr.EventEncoding;
import net.sourceforge.waters.analysis.tr.EventStatus;
import net.sourceforge.waters.analysis.tr.ListBufferTransitionRelation;
import net.sourceforge.waters.analysis.tr.TRAutomatonProxy;
import net.sourceforge.waters.model.analysis.AnalysisConfigurationException;
import net.sourceforge.waters.model.analysis.AnalysisException;
import net.sourceforge.waters.model.analysis.AnalysisResult;
import net.sourceforge.waters.model.analysis.EnumFactory;
import net.sourceforge.waters.model.analysis.ListedEnumFactory;
import net.sourceforge.waters.model.analysis.OverflowException;
import net.sourceforge.waters.model.analysis.des.AbstractModelAnalyzerFactory;
import net.sourceforge.waters.model.analysis.des.EventNotFoundException;
import net.sourceforge.waters.model.analysis.des.ModelAnalyzer;
import net.sourceforge.waters.model.analysis.kindtranslator.EventOnlyKindTranslator;
import net.sourceforge.waters.model.analysis.kindtranslator.KindTranslator;
import net.sourceforge.waters.model.base.ComponentKind;
import net.sourceforge.waters.model.des.AutomatonProxy;
import net.sourceforge.waters.model.des.EventProxy;
import net.sourceforge.waters.model.des.ProductDESProxy;
import net.sourceforge.waters.model.des.ProductDESProxyFactory;
import net.sourceforge.waters.model.des.StateProxy;
import net.sourceforge.waters.model.marshaller.MarshallingTools;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


/**
 * <P>An abstract base class for compositional analysis algorithms based on
 * the {@link TRAutomatonProxy} representation of automata.</P>
 *
 * <P>This model analyser implements compositional minimisation
 * of the input model, and leaves it to the subclasses to decide what is
 * to be done with the minimisation result. This implementation is based
 * on transition relations ({@link TRAutomatonProxy}). It provides full
 * support for the special event types of <I>blocked</I>, <I>failing</I>,
 * <I>selfloop-only</I>, and <I>always enabled</I> events. It can be
 * configured to use different abstraction sequences and candidate selection
 * heuristics.</P>
 *
 * <P><I>References:</I><BR>
 * Hugo Flordal, Robi Malik. Compositional Verification in Supervisory Control.
 * SIAM Journal of Control and Optimization, 48(3), 1914-1938, 2009.<BR>
 * Robi Malik, Ryan Leduc. Compositional Nonblocking Verification Using
 * Generalised Nonblocking Abstractions, IEEE Transactions on Automatic
 * Control <STRONG>58</STRONG>(8), 1-13, 2013.<BR>
 * Colin Pilbrow, Robi Malik. Compositional Nonblocking Verification with
 * Always Enabled Events and Selfloop-only Events. Proc. 2nd International
 * Workshop on Formal Techniques for Safety-Critical Systems, FTSCS 2013,
 * 147-162, Queenstown, New Zealand, 2013.</P>
 *
 * @author Robi Malik
 */

public abstract class AbstractTRCompositionalModelAnalyzer
  extends AbstractTRAnalyzer
  implements ModelAnalyzer
{

  //#########################################################################
  //# Constructors
  public AbstractTRCompositionalModelAnalyzer(final ProductDESProxy model,
                                              final KindTranslator translator,
                                              final ModelAnalyzer mono)
  {
    super(model, translator);
    mTRSimplifierCreator = getTRSimplifierFactory().getDefaultValue();
    mPreselectionHeuristic = getPreselectionHeuristicFactory().getDefaultValue();
    final SelectionHeuristic<TRCandidate> heu =
      getSelectionHeuristicFactory().getDefaultValue();
    mSelectionHeuristic = heu.createDecisiveHeuristic();
    mSpecialEventsListener = new PartitioningListener();
    mMonolithicAnalyzer = mono;
  }


  //#########################################################################
  //# Interface for net.sourceforge.waters.model.analysis.des.ModelAnalyser
  @Override
  public void setNodeLimit(final int limit)
  {
    setMonolithicStateLimit(limit);
    setInternalStateLimit(limit);
  }

  @Override
  public int getNodeLimit()
  {
    final int limit1 = getMonolithicStateLimit();
    final int limit2 = getInternalStateLimit();
    return Math.max(limit1, limit2);
  }

  @Override
  public void setTransitionLimit(final int limit)
  {
    setMonolithicTransitionLimit(limit);
    setInternalTransitionLimit(limit);
  }

  @Override
  public int getTransitionLimit()
  {
    final int limit1 = getInternalTransitionLimit();
    final int limit2 = getMonolithicTransitionLimit();
    return Math.max(limit1, limit2);
  }


  //#########################################################################
  //# Configuration
  @Override
  public void setSimplifierCreator
    (final TRToolCreator<TransitionRelationSimplifier> creator)
  {
    mTRSimplifierCreator = creator;
  }

  @Override
  public TRToolCreator<TransitionRelationSimplifier> getSimplifierCreator()
  {
    return mTRSimplifierCreator;
  }

  @Override
  public EnumFactory<TRPreselectionHeuristic> getPreselectionHeuristicFactory()
  {
    return getPreselectionHeuristicFactoryStatic();
  }

  public static EnumFactory<TRPreselectionHeuristic> getPreselectionHeuristicFactoryStatic()
  {
    return
      new ListedEnumFactory<TRPreselectionHeuristic>() {
      {
        register(PRESEL_MustL, true);
        register(PRESEL_MustSp);
        register(PRESEL_Pairs);
        register(PRESEL_MaxS);
        register(PRESEL_MinT);
        register(PRESEL_MaxT);
        register(PRESEL_MinS);
      }
    };
  }

  @Override
  public void setPreselectionHeuristic(final TRPreselectionHeuristic heu)
  {
    mPreselectionHeuristic = heu;
  }

  @Override
  public TRPreselectionHeuristic getPreselectionHeuristic()
  {
    return mPreselectionHeuristic;
  }

  @Override
  public EnumFactory<SelectionHeuristic<TRCandidate>> getSelectionHeuristicFactory()
  {
    return getSelectionHeuristicFactoryStatic();
  }

  public static EnumFactory<SelectionHeuristic<TRCandidate>> getSelectionHeuristicFactoryStatic()
  {
    return
      new ListedEnumFactory<SelectionHeuristic<TRCandidate>>() {
      {
        register(SEL_MinS);
        register(SEL_MinSa);
        register(SEL_MinSSp);
        register(SEL_MinS0);
        register(SEL_MinS0a);
        register(SEL_MinSync, true);
        register(SEL_MinSyncA);
        register(SEL_MaxC);
        register(SEL_MaxL);
        register(SEL_MinE);
        register(SEL_MinF1);
        register(SEL_MinF2);
      }
    };
  }

  @Override
  public void setSelectionHeuristic(final SelectionHeuristic<TRCandidate> heu)
  {
    mSelectionHeuristic = heu.createDecisiveHeuristic();
  }

  @Override
  public SelectionHeuristic<TRCandidate> getSelectionHeuristic()
  {
    return mSelectionHeuristic;
  }

  @Override
  public void setMonolithicAnalyzer(final ModelAnalyzer mono)
  {
    mMonolithicAnalyzer = mono;
  }

  @Override
  public ModelAnalyzer getMonolithicAnalyzer()
  {
    return mMonolithicAnalyzer;
  }


  @Override
  public int getInternalStateLimit()
  {
    return mInternalStateLimit;
  }

  @Override
  public void setInternalStateLimit(final int limit)
  {
    mInternalStateLimit = limit > 0 ? limit : Integer.MAX_VALUE;
  }

  @Override
  public int getMonolithicStateLimit()
  {
    return super.getNodeLimit();
  }

  @Override
  public void setMonolithicStateLimit(final int limit)
  {
    super.setNodeLimit(limit);
  }

  @Override
  public int getInternalTransitionLimit()
  {
    return mInternalTransitionLimit;
  }

  @Override
  public void setInternalTransitionLimit(final int limit)
  {
    mInternalTransitionLimit = limit > 0 ? limit : Integer.MAX_VALUE;
  }

  @Override
  public int getMonolithicTransitionLimit()
  {
    return super.getTransitionLimit();
  }

  @Override
  public void setMonolithicTransitionLimit(final int limit)
  {
    super.setTransitionLimit(limit);
  }

  @Override
  public void setBlockedEventsEnabled(final boolean enable)
  {
    mBlockedEventsEnabled = enable;
  }

  @Override
  public boolean isBlockedEventsEnabled()
  {
    return mBlockedEventsEnabled;
  }

  @Override
  public void setFailingEventsEnabled(final boolean enable)
  {
    mFailingEventsEnabled = enable;
  }

  @Override
  public boolean isFailingEventsEnabled()
  {
    return mFailingEventsEnabled;
  }

  @Override
  public void setSelfloopOnlyEventsEnabled(final boolean enable)
  {
    mSelfloopOnlyEventsEnabled = enable;
  }

  @Override
  public boolean isSelfloopOnlyEventsEnabled()
  {
    return mSelfloopOnlyEventsEnabled;
  }

  @Override
  public void setAlwaysEnabledEventsEnabled(final boolean enable)
  {
    mAlwaysEnabledEventsEnabled = enable;
  }

  @Override
  public boolean isAlwaysEnabledEventsEnabled()
  {
    return mAlwaysEnabledEventsEnabled;
  }

  @Override
  public void setMonolithicDumpFile(final File file)
  {
    mMonolithicDumpFile = file;
  }

  @Override
  public File getMonolithicDumpFile()
  {
    return mMonolithicDumpFile;
  }

  @Override
  public void setOutputCheckingEnabled(final boolean checking)
  {
    mOutputCheckingEnabled = checking;
  }

  @Override
  public boolean isOutputCheckingEnabled()
  {
    return mOutputCheckingEnabled;
  }

  public void setUsingWeakObservationEquivalence(final boolean weak)
  {
    mUsingWeakObservationEquivalence = weak;
  }

  public boolean isUsingWeakObservationEquivalence()
  {
    return mUsingWeakObservationEquivalence;
  }


  //#########################################################################
  //# Interface net.sourceforge.waters.model.analysis.ModelAnalyser
  @Override
  public List<Option<?>> getOptions(final OptionMap db)
  {
    final List<Option<?>> options = super.getOptions(db);
    db.append(options, TRCompositionalModelAnalyzerFactory.
                       OPTION_AbstractTRCompositionalModelAnalyzer_PreselectionHeuristic);
    db.append(options, TRCompositionalModelAnalyzerFactory.
                       OPTION_AbstractTRCompositionalModelAnalyzer_SelectionHeuristic);
    db.append(options, TRCompositionalModelAnalyzerFactory.
                       OPTION_AbstractTRCompositionalModelAnalyzer_WeakObservationEquivalence);
    db.append(options, AbstractModelAnalyzerFactory.
                       OPTION_ModelAnalyzer_InternalStateLimit);
    db.append(options, AbstractModelAnalyzerFactory.
                       OPTION_ModelAnalyzer_InternalTransitionLimit);
    db.append(options, AbstractModelAnalyzerFactory.
                       OPTION_ModelAnalyzer_FinalStateLimit);
    db.append(options, AbstractModelAnalyzerFactory.
                       OPTION_ModelAnalyzer_FinalTransitionLimit);
    db.append(options, TRCompositionalModelAnalyzerFactory.
                       OPTION_AbstractTRCompositionalModelAnalyzer_BlockedEventsEnabled);
    db.append(options, TRCompositionalModelAnalyzerFactory.
                       OPTION_AbstractTRCompositionalModelAnalyzer_FailingEventsEnabled);
    db.append(options, TRCompositionalModelAnalyzerFactory.
                       OPTION_AbstractTRCompositionalModelAnalyzer_SelfloopOnlyEventsEnabled);
    db.append(options, TRCompositionalModelAnalyzerFactory.
                       OPTION_AbstractTRCompositionalModelAnalyzer_AlwaysEnabledEventsEnabled);
    db.append(options, TRCompositionalModelAnalyzerFactory.
                       OPTION_AbstractTRCompositionalModelAnalyzer_MonolithicDumpFile);
    return options;
  }

  @SuppressWarnings("unchecked")
  @Override
  public void setOption(final Option<?> option)
  {
    if (option.hasID(TRCompositionalModelAnalyzerFactory.
                     OPTION_AbstractTRCompositionalModelAnalyzer_PreselectionHeuristic)) {
      final EnumOption<TRPreselectionHeuristic> enumOption =
        (EnumOption<TRPreselectionHeuristic>) option;
      setPreselectionHeuristic(enumOption.getValue());
    } else if (option.hasID(TRCompositionalModelAnalyzerFactory.
                            OPTION_AbstractTRCompositionalModelAnalyzer_SelectionHeuristic)) {
      final EnumOption<SelectionHeuristic<TRCandidate>> enumOption =
        (EnumOption<SelectionHeuristic<TRCandidate>>) option;
      setSelectionHeuristic(enumOption.getValue());
    } else if (option.hasID(AbstractModelAnalyzerFactory.
                            OPTION_ModelAnalyzer_InternalStateLimit)) {
      final PositiveIntOption intOption = (PositiveIntOption) option;
      setInternalStateLimit(intOption.getIntValue());
    } else if (option.hasID(AbstractModelAnalyzerFactory.
                            OPTION_ModelAnalyzer_InternalTransitionLimit)) {
      final PositiveIntOption intOption = (PositiveIntOption) option;
      setInternalTransitionLimit(intOption.getIntValue());
    } else if (option.hasID(AbstractModelAnalyzerFactory.
                            OPTION_ModelAnalyzer_FinalStateLimit)) {
      final PositiveIntOption intOption = (PositiveIntOption) option;
      setMonolithicStateLimit(intOption.getIntValue());
    } else if (option.hasID(AbstractModelAnalyzerFactory.
                            OPTION_ModelAnalyzer_FinalTransitionLimit)) {
      final PositiveIntOption intOption = (PositiveIntOption) option;
      setMonolithicTransitionLimit(intOption.getIntValue());
    } else if (option.hasID(TRCompositionalModelAnalyzerFactory.
                            OPTION_AbstractTRCompositionalModelAnalyzer_BlockedEventsEnabled)) {
      final BooleanOption boolOption = (BooleanOption) option;
      setBlockedEventsEnabled(boolOption.getBooleanValue());
    } else if (option.hasID(TRCompositionalModelAnalyzerFactory.
                            OPTION_AbstractTRCompositionalModelAnalyzer_FailingEventsEnabled)) {
      final BooleanOption boolOption = (BooleanOption) option;
      setFailingEventsEnabled(boolOption.getBooleanValue());
    } else if (option.hasID(TRCompositionalModelAnalyzerFactory.
                            OPTION_AbstractTRCompositionalModelAnalyzer_SelfloopOnlyEventsEnabled)) {
      final BooleanOption boolOption = (BooleanOption) option;
      setSelfloopOnlyEventsEnabled(boolOption.getBooleanValue());
    } else if (option.hasID(TRCompositionalModelAnalyzerFactory.
                            OPTION_AbstractTRCompositionalModelAnalyzer_AlwaysEnabledEventsEnabled)) {
      final BooleanOption boolOption = (BooleanOption) option;
      setAlwaysEnabledEventsEnabled(boolOption.getBooleanValue());
    } else if (option.hasID(TRCompositionalModelAnalyzerFactory.
                            OPTION_AbstractTRCompositionalModelAnalyzer_MonolithicDumpFile)) {
      final FileOption fileOption = (FileOption) option;
      setMonolithicDumpFile(fileOption.getValue());
    } else if (option.hasID(TRCompositionalModelAnalyzerFactory.
                            OPTION_AbstractTRCompositionalModelAnalyzer_WeakObservationEquivalence)) {
      final BooleanOption boolOption = (BooleanOption) option;
      setUsingWeakObservationEquivalence(boolOption.getValue());
    } else {
      super.setOption(option);
    }
  }


  //#########################################################################
  //# Interface net.sourceforge.waters.model.analysis.Abortable
  @Override
  public void requestAbort()
  {
    super.requestAbort();
    if (mPreselectionHeuristic != null) {
      mPreselectionHeuristic.requestAbort();
    }
    if (mSelectionHeuristic != null) {
      mSelectionHeuristic.requestAbort();
    }
    if (mSpecialEventsFinder != null) {
      mSpecialEventsFinder.requestAbort();
    }
    if (mTRSimplifier != null) {
      mTRSimplifier.requestAbort();
    }
    if (mSynchronousProductBuilder != null) {
      mSynchronousProductBuilder.requestAbort();
    }
    if (mMonolithicAnalyzer != null) {
      mMonolithicAnalyzer.requestAbort();
    }
  }

  @Override
  public void resetAbort()
  {
    super.resetAbort();
    if (mPreselectionHeuristic != null) {
      mPreselectionHeuristic.resetAbort();
    }
    if (mSelectionHeuristic != null) {
      mSelectionHeuristic.resetAbort();
    }
    if (mSpecialEventsFinder != null) {
      mSpecialEventsFinder.resetAbort();
    }
    if (mTRSimplifier != null) {
      mTRSimplifier.resetAbort();
    }
    if (mSynchronousProductBuilder != null) {
      mSynchronousProductBuilder.resetAbort();
    }
    if (mMonolithicAnalyzer != null) {
      mMonolithicAnalyzer.resetAbort();
    }
  }


  //#########################################################################
  //# Invocation
  @Override
  protected void setUp()
    throws AnalysisException
  {
    super.setUp();

    mTRSimplifier = mTRSimplifierCreator.create(this);
    mTRSimplifier.setPreferredOutputConfiguration
      (ListBufferTransitionRelation.CONFIG_SUCCESSORS);
    mPreselectionHeuristic.setContext(this);
    mSelectionHeuristic.setContext(this);
    mSpecialEventsFinder = new SpecialEventsFinder();
    mSpecialEventsFinder.setAppliesPartitionAutomatically(false);
    mSpecialEventsFinder.setDefaultMarkingID(DEFAULT_MARKING);

    final CompositionalAnalysisResult result = getAnalysisResult();
    if (isBlockedEventsUsed()) {
      result.addBlockedEvents(0);
    }
    if (isFailingEventsUsed()) {
      result.addFailingEvents(0);
    }
    if (isSelfloopOnlyEventsUsed()) {
      result.addSelfloopOnlyEvents(0);
    }
    if (isAlwaysEnabledEventsUsed()) {
      result.addAlwaysEnabledEvents(0);
    }

    final ProductDESProxy model = getModel();
    final Collection<AutomatonProxy> automata = model.getAutomata();
    final int numAutomata = automata.size();
    final int numEvents = model.getEvents().size();
    if (isDetailedOutputEnabled()) {
      mAbstractionSequence = new ArrayList<>(4 * numAutomata);
      mCurrentAutomataMap = new HashMap<>(numAutomata);
    }
    mCurrentSubsystem = new TRSubsystemInfo(numAutomata, numEvents);
    final List<TRAutomatonProxy> trs = new ArrayList<>(numAutomata);
    for (final AutomatonProxy aut : automata) {
      final TRAutomatonProxy tr = createInitialAutomaton(aut);
      if (tr != null) {
        trs.add(tr);
      } else if (result.isFinished()) {
        return;
      }
    }
    mCurrentSubsystem.addAutomata(trs);
    result.setNumberOfAutomata(trs.size());

    mSpecialEventsFinder.setBlockedEventsDetected(isBlockedEventsUsed());
    mSpecialEventsFinder.setFailingEventsDetected(isFailingEventsUsed());
    mSpecialEventsFinder.setSelfloopOnlyEventsDetected(isSelfloopOnlyEventsUsed());
    mSpecialEventsFinder.setAlwaysEnabledEventsDetected(false);
    mSpecialEventsFinder.setControllabilityConsidered(isControllabilityConsidered());
    for (final TRAutomatonProxy aut : trs) {
      final ListBufferTransitionRelation rel = aut.getTransitionRelation();
      mSpecialEventsFinder.setTransitionRelation(rel);
      mSpecialEventsFinder.run();
      final byte[] status = mSpecialEventsFinder.getComputedEventStatus();
      mCurrentSubsystem.registerEvents(aut, status);
    }
    mSpecialEventsFinder.setAlwaysEnabledEventsDetected(isAlwaysEnabledEventsUsed());

    mSynchronousProductBuilder = createSynchronousProductBuilder();
    mSynchronousProductBuilder.setDetailedOutputEnabled(true);

    final ModelAnalyzer mono = getMonolithicAnalyzer();
    if (mono != null) {
      final KindTranslator translator = getKindTranslator();
      final KindTranslator eventOnly = new EventOnlyKindTranslator(translator);
      mono.setKindTranslator(eventOnly);
      mono.setNodeLimit(getMonolithicStateLimit());
      mono.setTransitionLimit(getMonolithicTransitionLimit());
      final AnalysisResult dummy = mono.createAnalysisResult();
      result.addMonolithicAnalysisResult(dummy);
    }

    mSubsystemQueue = new PriorityQueue<>();
    mNeedsSimplification = new SimplificationQueue(trs);
    mNeedsDisjointSubsystemsCheck = true;
    mAlwaysEnabledDetectedInitially = !isAlwaysEnabledEventsUsed();
    mStepNo = 0;

    result.addSimplifierStatistics(mSpecialEventsFinder);
    result.addSimplifierStatistics(mTRSimplifier);
  }

  @Override
  public boolean run() throws AnalysisException
  {
    try {
      final Logger logger = LogManager.getLogger();
      setUp();
      final AnalysisResult result = getAnalysisResult();
      if (result.isFinished()) {
        computeCounterExample();
        return result.isSatisfied();
      } else {
        do {
          if (logger.isDebugEnabled()) {
            final String name = mCurrentSubsystem.toString();
            if (name.length() <= 40) {
              logger.debug("Processing new subsystem " + name + " ...");
            } else {
              logger.debug("Processing new subsystem with " +
                           mCurrentSubsystem.getNumberOfAutomata() +
                           " automata ...");
            }
          }
          analyseCurrentSubsystemCompositionally();
          if (result.isFinished()) {
            computeCounterExample();
            return result.isSatisfied();
          }
          mCurrentSubsystem = mSubsystemQueue.poll();
        } while (mCurrentSubsystem != null);
        if (analyseSubsystemMonolithically(null)) {
          return setSatisfiedResult();
        } else {
          assert result.isFinished() && !result.isSatisfied() :
            "Failed analysis result not recorded correctly!";
          computeCounterExample();
          return false;
        }
      }
    } catch (final AnalysisException exception) {
      setExceptionResult(exception);
      throw exception;
    } catch (final OutOfMemoryError error) {
      System.gc();
      final OverflowException exception = new OverflowException(error);
      setExceptionResult(exception);
      throw exception;
    } catch (final StackOverflowError error) {
      final OverflowException exception = new OverflowException(error);
      setExceptionResult(exception);
      throw exception;
    } finally {
      tearDown();
    }
  }

  @Override
  protected void tearDown()
  {
    super.tearDown();
    mSelectionHeuristic.setContext(null);
    mTRSimplifier = null;
    mSynchronousProductBuilder = null;
    mSpecialEventsFinder = null;
    mAbstractionSequence = null;
    mIntermediateAbstractionSequence = null;
    mCurrentAutomataMap = null;
    mSubsystemQueue = null;
    mCurrentSubsystem = null;
    mNeedsSimplification = null;
  }


  //#########################################################################
  //# Hooks
  protected EventProxy getUsedDefaultMarking()
    throws EventNotFoundException
  {
    return null;
  }

  protected EventProxy getUsedPreconditionMarking()
  {
    return null;
  }

  /**
   * Returns whether simplification needs to distinguish controllable and
   * uncontrollable events. If this is the case, it can affect how events
   * are encoded, and how special events are recognised. For example,
   * only uncontrollable events are ever considered as always enabled.
   * @see #isAlwaysEnabledEventsEnabled()
   */
  protected boolean isControllabilityConsidered()
  {
    return false;
  }

  protected boolean isBlockedEventsUsed()
  {
    return isBlockedEventsEnabled();
  }

  protected boolean isFailingEventsUsed()
  {
    return isFailingEventsEnabled();
  }

  protected boolean isSelfloopOnlyEventsUsed()
  {
    return isSelfloopOnlyEventsEnabled();
  }

  protected boolean isAlwaysEnabledEventsUsed()
  {
    return
      isAlwaysEnabledEventsEnabled() &&
      mTRSimplifier.isAlwaysEnabledEventsSupported();
  }

  /**
   * Returns the minimum number of automata that can be retained in
   * a subsystem prior to monolithic analysis. The default for this
   * is 2, to avoid the expensive minimisation of a final automaton
   * prior to verification.
   */
  protected int getMonolithicAutomataLimit()
  {
    return 2;
  }

  protected boolean earlyTerminationCheck(final TRSubsystemInfo subsys)
    throws AnalysisException
  {
    return false;
  }

  protected AbstractTRSynchronousProductBuilder createSynchronousProductBuilder()
  {
    final KindTranslator translator = getKindTranslator();
    final KindTranslator eventOnly = new EventOnlyKindTranslator(translator);
    final AbstractTRSynchronousProductBuilder builder =
      new TRSynchronousProductBuilder();
    builder.setDetailedOutputEnabled(true);
    builder.setKindTranslator(eventOnly);
    builder.setRemovingSelfloops(true);
    builder.setNodeLimit(mInternalStateLimit);
    builder.setTransitionLimit(mInternalTransitionLimit);
    return builder;
  }


  //#########################################################################
  //# Specific Access
  protected TransitionRelationSimplifier getSimplifier()
  {
    return mTRSimplifier;
  }

  protected SpecialEventsFinder getSpecialEventsFinder()
  {
    return mSpecialEventsFinder;
  }

  protected PartitioningListener getSpecialEventsListener()
  {
    return mSpecialEventsListener;
  }

  protected TRSubsystemInfo getCurrentSubsystem()
  {
    return mCurrentSubsystem;
  }

  protected Collection<TRSubsystemInfo> getPendingSubsystems()
  {
    return mSubsystemQueue;
  }

  protected List<TRAbstractionStep> getAbstractionSequence()
  {
    return mAbstractionSequence;
  }

  protected IntermediateAbstractionSequence getIntermediateAbstractionSequence()
  {
    return mIntermediateAbstractionSequence;
  }

  @Override
  protected AbstractTRCompositionalModelAnalyzer getCompositionalAnalyzer()
  {
    return this;
  }

  @Override
  protected int getPreferredInputConfiguration()
    throws AnalysisConfigurationException
  {
    if (mTRSimplifier != null) {
      return mTRSimplifier.getPreferredInputConfiguration();
    } else {
      final TransitionRelationSimplifier simplifier =
        mTRSimplifierCreator.create(this);
      return simplifier.getPreferredInputConfiguration();
    }
  }


  //#########################################################################
  //# Algorithm
  private void analyseCurrentSubsystemCompositionally()
    throws AnalysisException
  {
    if (earlyTerminationCheck(mCurrentSubsystem)) {
      return;
    }
    final int limit = getMonolithicAutomataLimit();
    while (true) {
      checkAbort();
      final boolean simplified = simplifyAllAutomataIndividually();
      if (simplified && earlyTerminationCheck(mCurrentSubsystem)) {
        return;
      } else if (disjointSubsystemsCheck()) {
        return;
      } else if (mCurrentSubsystem.getNumberOfAutomata() <= limit) {
        break;
      }
      final Collection<TRCandidate> candidates =
        mPreselectionHeuristic.collectCandidates(mCurrentSubsystem);
      TRCandidate candidate = mSelectionHeuristic.select(candidates);
      while (candidate != null) {
        try {
          computeSynchronousProduct(candidate);
          mPreselectionHeuristic.removeOverflowCandidatesContaining(candidate);
          break;
        } catch (final OverflowException exception) {
          mPreselectionHeuristic.addOverflowCandidate(candidate);
          candidates.remove(candidate);
          candidate = mSelectionHeuristic.select(candidates);
        }
      }
      if (candidate == null) {
        break;
      } else if (earlyTerminationCheck(mCurrentSubsystem)) {
        return;
      }
    }
    assert mNeedsSimplification.isEmpty();
    analyseSubsystemMonolithically(mCurrentSubsystem);
  }

  private boolean simplifyAllAutomataIndividually()
    throws AnalysisException
  {
    final int limit = getMonolithicAutomataLimit();
    boolean simplified = false;
    int remaining =
      mAlwaysEnabledDetectedInitially ? 0 : mNeedsSimplification.size();
    while (!mNeedsSimplification.isEmpty() &&
           mCurrentSubsystem.getNumberOfAutomata() >= limit) {
      final TRAutomatonProxy aut = mNeedsSimplification.poll();
      simplified |= simplifyAutomatonIndividually(aut);
      if (remaining > 0) {
        mAlwaysEnabledDetectedInitially = (--remaining == 0);
      }
    }
    mNeedsSimplification.clear();  // in case of early exit from loop
    return simplified;
  }

  private boolean simplifyAutomatonIndividually(final TRAutomatonProxy aut)
    throws AnalysisException
  {
    // Log ...
    final Logger logger = LogManager.getLogger();
    final ListBufferTransitionRelation rel = aut.getTransitionRelation();
    if (logger.isDebugEnabled()) {
      logger.debug("Simplifying " + aut.getName() + " ...");
      rel.logSizes(logger);
    }
    final CompositionalAnalysisResult result = getAnalysisResult();
    result.addCompositionAttempt();
    recordStatistics(aut);
    // Set event status ...
    final EventEncoding enc = aut.getEventEncoding();
    final int numEvents = enc.getNumberOfProperEvents();
    for (int e = EventEncoding.NONTAU; e < numEvents; e++) {
      checkAbort();
      final byte oldStatus = enc.getProperEventStatus(e);
      if (EventStatus.isUsedEvent(oldStatus)) {
        final EventProxy event = enc.getProperEvent(e);
        final TREventInfo info = mCurrentSubsystem.getEventInfo(event);
        final byte newStatus = info.getEventStatus(aut);
        if (newStatus != oldStatus) {
          enc.setProperEventStatus(e, newStatus);
        }
      }
    }
    countSpecialEvents(enc);
    try {
      // Set up trace computation.
      mIntermediateAbstractionSequence =
        new IntermediateAbstractionSequence(aut);
      // Simplify.
      final int oldNumStates = rel.getNumberOfStates();
      mSpecialEventsListener.setEnabled(true);
      mTRSimplifier.setTransitionRelation(rel);
      final boolean simplified = mTRSimplifier.run();
      if (simplified) {
        mPreselectionHeuristic.removeOverflowCandidatesContaining(aut);
        if (rel.getNumberOfStates() != oldNumStates) {
          aut.resetStateNames();
        }
      }
      // Record steps and update event status.
      mIntermediateAbstractionSequence.commit();
      mNeedsSimplification.setSuppressed(aut);
      if (simplified || !mAlwaysEnabledDetectedInitially) {
        final EventEncoding oldEncoding =
          mIntermediateAbstractionSequence.getInputEventEncoding();
        updateEventStatus(aut, oldEncoding);
      }
      // Drop trivial automata if necessary.
      if (simplified && isTrivialAutomaton(aut)) {
        logger.debug("Dropping trivial automaton " + aut.getName());
        dropTrivialAutomaton(aut);
        mCurrentSubsystem.removeAutomaton(aut, mNeedsSimplification);
      }
      return simplified;
    } catch (final OverflowException exception) {
      recordUnsuccessfulComposition(exception);
      return false;
    } finally {
      mNeedsSimplification.clearSuppressed();
      mIntermediateAbstractionSequence = null;
    }
  }

  private TRAutomatonProxy computeSynchronousProduct
    (final TRCandidate candidate)
    throws AnalysisException
  {
    final Logger logger = LogManager.getLogger();
    if (logger.isDebugEnabled()) {
      mStepNo++;
      logger.debug("Composing " + candidate + " (step " + mStepNo +") ...");
      // mCurrentSubsystem.saveModule("before" + mStepNo + ".wmod");
    }
    try {
      // We are going to compose ...
      final CompositionalAnalysisResult result = getAnalysisResult();
      result.addCompositionAttempt();
      // Set up event encoding ...
      final EventEncoding candidateEncoding = candidate.getEventEncoding();
      countSpecialEvents(candidateEncoding);
      addAuxiliaryEvents(candidateEncoding);
      sortCandidateEvents(candidateEncoding);
      final EventEncoding syncEncoding = candidate.createSyncEventEncoding();
      // Synchronise ...
      final ProductDESProxyFactory factory = getFactory();
      final ProductDESProxy des = candidate.createProductDESProxy(factory);
      mSynchronousProductBuilder.setModel(des);
      mSynchronousProductBuilder.setEventEncoding(syncEncoding);
      mSynchronousProductBuilder.run();
      final TRSynchronousProductResult syncResult =
        mSynchronousProductBuilder.getAnalysisResult();
      candidate.setComposedSuccessfully();
      final CompositionalAnalysisResult combinedResult = getAnalysisResult();
      combinedResult.addSynchronousProductAnalysisResult(syncResult);
      final TRAutomatonProxy sync = syncResult.getComputedAutomaton();
      recordStatistics(sync);
      mIntermediateAbstractionSequence =
        new IntermediateAbstractionSequence(sync);
      // Set up trace computation ...
      if (isDetailedOutputEnabled()) {
        final List<TRAutomatonProxy> automata = candidate.getAutomata();
        final List<TRAbstractionStep> preds = getAbstractionSteps(automata);
        final EventEncoding enc = candidate.getEventEncoding();
        final TRAbstractionStep step = new TRAbstractionStepSync
            (preds, enc, factory, mSynchronousProductBuilder, syncResult);
        mIntermediateAbstractionSequence.append(step);
      }
      // Simplify ...
      final ListBufferTransitionRelation rel = sync.getTransitionRelation();
      rel.logSizes(logger);
      mSpecialEventsListener.setEnabled(false);
      mTRSimplifier.setTransitionRelation(rel);
      mTRSimplifier.run();
      // Update trace information ...
      if (isDetailedOutputEnabled()) {
        for (final TRAutomatonProxy aut : candidate.getAutomata()) {
          mCurrentAutomataMap.remove(aut);
        }
        mIntermediateAbstractionSequence.commit();
      }
      // Update event status ...
      mNeedsSimplification.setSuppressed(candidate, sync);
      if (isTrivialAutomaton(sync)) {
        logger.debug("Dropping trivial automaton " + sync.getName());
        dropTrivialAutomaton(sync);
      } else {
        mCurrentSubsystem.addAutomaton(sync);
      }
      updateEventStatus(sync, syncEncoding);
      for (final TRAutomatonProxy aut : candidate.getAutomata()) {
        mCurrentSubsystem.removeAutomaton(aut, mNeedsSimplification);
      }
      // mCurrentSubsystem.saveModule("after" + mStepNo + ".wmod");
      return sync;
    } catch (final OverflowException exception) {
      recordUnsuccessfulComposition(exception);
      throw exception;
    } finally {
      mNeedsSimplification.clearSuppressed();
      mIntermediateAbstractionSequence = null;
    }
  }

  private void sortCandidateEvents(final EventEncoding enc)
  {
    final byte pattern =
      EventStatus.STATUS_FAILING | EventStatus.STATUS_ALWAYS_ENABLED;
    final byte mask = (byte) (pattern | EventStatus.STATUS_UNUSED);
    final int numEvents = enc.getNumberOfProperEvents();
    boolean hasStronglyFailingEvent = false;
    for (int e = EventEncoding.NONTAU; e < numEvents; e++) {
      final byte status = enc.getProperEventStatus(e);
      if ((status & mask) == pattern) {
        hasStronglyFailingEvent = true;
        break;
      }
    }
    if (hasStronglyFailingEvent) {
      enc.sortProperEvents(~EventStatus.STATUS_ALWAYS_ENABLED,
                           ~EventStatus.STATUS_FAILING,
                           ~EventStatus.STATUS_LOCAL,
                           EventStatus.STATUS_CONTROLLABLE);
    } else {
      enc.sortProperEvents(~EventStatus.STATUS_LOCAL,
                           ~EventStatus.STATUS_ALWAYS_ENABLED,
                           EventStatus.STATUS_CONTROLLABLE);
    }
  }

  private boolean isTrivialAutomaton(final TRAutomatonProxy aut)
  {
    final ListBufferTransitionRelation rel = aut.getTransitionRelation();
    final int numEvents = rel.getNumberOfProperEvents();
    for (int e = EventEncoding.TAU; e < numEvents; e++) {
      final byte status = rel.getProperEventStatus(e);
      if (EventStatus.isUsedEvent(status)) {
        return false;
      }
    }
    boolean hasReachableState = false;
    for (int s = 0; s < rel.getNumberOfStates(); s++) {
      if (rel.isReachable(s)) {
        if (!rel.isInitial(s)) {
          return false;
        } else if (rel.isPropositionUsed(DEFAULT_MARKING) &&
                   !rel.isMarked(s, DEFAULT_MARKING)) {
          return false;
        } else if (rel.isPropositionUsed(PRECONDITION_MARKING) &&
                   !rel.isMarked(s, PRECONDITION_MARKING)) {
          return false;
        }
        hasReachableState = true;
      }
    }
    return hasReachableState;
  }

  private void updateEventStatus(final TRAutomatonProxy aut,
                                 final EventEncoding oldEncoding)
    throws AnalysisException
  {
    final EventEncoding enc = aut.getEventEncoding();
    final ListBufferTransitionRelation rel = aut.getTransitionRelation();
    final int numEvents = rel.getNumberOfProperEvents();
    mSpecialEventsFinder.setTransitionRelation(rel);
    mSpecialEventsFinder.run();
    final byte[] newStatus = mSpecialEventsFinder.getComputedEventStatus();
    for (int e = EventEncoding.NONTAU; e < numEvents; e++) {
      checkAbort();
      final byte oldStatus = oldEncoding.getProperEventStatus(e);
      if (EventStatus.isUsedEvent(oldStatus)) {
        final EventProxy event = enc.getProperEvent(e);
        final TREventInfo info = mCurrentSubsystem.getEventInfo(event);
        info.updateAutomatonStatus(aut, newStatus[e], mNeedsSimplification);
        if (info.isEmpty()) {
          mCurrentSubsystem.removeEvent(event);
        }
        mNeedsDisjointSubsystemsCheck |=
          !EventStatus.isLocalEvent(oldStatus) &&
          !EventStatus.isUsedEvent(newStatus[e]);
      }
    }
  }

  private boolean disjointSubsystemsCheck()
  {
    if (mNeedsDisjointSubsystemsCheck) {
      final long start = System.currentTimeMillis();
      mNeedsDisjointSubsystemsCheck = false;
      final List<TRSubsystemInfo> splits =
        mCurrentSubsystem.findEventDisjointSubsystems();
      final boolean splitSuccess = splits != null;
      if (splitSuccess) {
        mCurrentSubsystem = null;
        mSubsystemQueue.addAll(splits);
      }
      final long stop = System.currentTimeMillis();
      final CompositionalAnalysisResult result = getAnalysisResult();
      result.addSplitAttempt(splitSuccess, stop - start);
      return splitSuccess;
    } else {
      return false;
    }
  }

  abstract protected boolean analyseSubsystemMonolithically
    (final TRSubsystemInfo subsys)
    throws AnalysisException;


  protected TRAbstractionStep getAbstractionStep(final TRAutomatonProxy aut)
  {
    return mCurrentAutomataMap.get(aut);
  }

  protected List<TRAbstractionStep> getAbstractionSteps
    (final List<TRAutomatonProxy> automata)
  {
    final List<TRAbstractionStep> preds = new ArrayList<>(automata.size());
    for (final TRAutomatonProxy aut : automata) {
      final TRAbstractionStep pred = mCurrentAutomataMap.get(aut);
      preds.add(pred);
    }
    return preds;
  }

  protected TRTraceProxy computeCounterExample() throws AnalysisException
  {
    return null;
  }


  //#########################################################################
  //# Auxiliary Methods
  protected EventEncoding createInitialEventEncoding(final AutomatonProxy aut)
    throws AnalysisException
  {
    final EventEncoding enc = new EventEncoding();
    addAuxiliaryEvents(enc);
    final String name = aut.getName();
    enc.provideTauEvent(name);
    final KindTranslator translator = getKindTranslator();
    for (final EventProxy event : aut.getEvents()) {
      switch (translator.getEventKind(event)) {
      case CONTROLLABLE:
      case UNCONTROLLABLE:
        enc.addEvent(event, translator, EventStatus.STATUS_NONE);
        break;
      case PROPOSITION:
        final int p = enc.getEventCode(event);
        if (p >= 0) {
          enc.setPropositionUsed(p, true);
        }
        break;
      default:
        throw new IllegalArgumentException
          ("Unknown event kind " + translator.getEventKind(event) + "!");
      }
    }
    enc.sortProperEvents(EventStatus.STATUS_UNUSED,
                         EventStatus.STATUS_CONTROLLABLE);
    return enc;
  }

  protected void addAuxiliaryEvents(final EventEncoding enc)
    throws AnalysisException
  {
  }

  protected TRAutomatonProxy createInitialAutomaton(final AutomatonProxy aut)
    throws AnalysisException
  {
    final KindTranslator translator = getKindTranslator();
    final ComponentKind kind = translator.getComponentKind(aut);
    if (kind == null) {
      return null;
    }
    switch (kind) {
    case PLANT:
    case SPEC:
      final TRAbstractionStepInput step;
      if (isPreservingEncodings() && aut instanceof TRAutomatonProxy) {
        final TRAutomatonProxy tr = (TRAutomatonProxy) aut;
        step = new TRAbstractionStepInput(tr);
      } else {
        final EventEncoding eventEnc = createInitialEventEncoding(aut);
        final StateProxy dumpState = findDumpState(aut);
        step = new TRAbstractionStepInput(aut, eventEnc, dumpState);
      }
      final int config = mTRSimplifier.getPreferredInputConfiguration();
      final TRAutomatonProxy created = step.createOutputAutomaton(config);
      created.setKind(ComponentKind.PLANT);
      if (!hasInitialState(created)) {
        final Logger logger = LogManager.getLogger();
        logger.debug("Terminating early as automaton " + aut.getName() +
                     " has no initial state.");
        setSatisfiedResult();
        return null;
      }
      if (isDetailedOutputEnabled()) {
        addAbstractionStep(step, created);
      }
      return created;
    default:
      return null;
    }
  }

  protected StateProxy findDumpState(final AutomatonProxy aut)
    throws EventNotFoundException
  {
    return null;
  }

  protected boolean hasInitialState(final TRAutomatonProxy aut)
  {
    final ListBufferTransitionRelation rel = aut.getTransitionRelation();
    boolean hasInit = false;
    boolean hasAlpha = !rel.isPropositionUsed(PRECONDITION_MARKING);
    for (int s = 0; s < rel.getNumberOfStates(); s++) {
      hasInit |= rel.isInitial(s);
      hasAlpha |= rel.isMarked(s, PRECONDITION_MARKING);
      if (hasInit && hasAlpha) {
        return true;
      }
    }
    return false;
  }


  protected void dropPendingSubsystems()
  {
    for (final TRSubsystemInfo subsys : mSubsystemQueue) {
      dropSubsystem(subsys);
    }
    mSubsystemQueue.clear();
  }

  protected void dropSubsystem(final TRSubsystemInfo subsys)
  {
    if (subsys != null && isDetailedOutputEnabled()) {
      for (final TRAutomatonProxy aut : subsys.getAutomata()) {
        dropTrivialAutomaton(aut);
      }
    }
  }

  protected void dropTrivialAutomaton(final TRAutomatonProxy aut)
  {
    if (isDetailedOutputEnabled()) {
      final TRAbstractionStep step = createDropStep(aut);
      addAbstractionStep(step);
    }
  }

  protected List<TRAbstractionStep> createDropSteps
    (final TRSubsystemInfo subsys)
  {
    if (isDetailedOutputEnabled()) {
      final List<TRAutomatonProxy> automata = subsys.getAutomata();
      final List<TRAbstractionStep> steps = new ArrayList<>(automata.size());
      for (final TRAutomatonProxy aut : subsys.getAutomata()) {
        final TRAbstractionStep step = createDropStep(aut);
        steps.add(step);
      }
      return steps;
    } else {
      return Collections.emptyList();
    }
  }

  protected TRAbstractionStep createDropStep(final TRAutomatonProxy aut)
  {
    final TRAbstractionStep pred = mCurrentAutomataMap.remove(aut);
    final ListBufferTransitionRelation rel = aut.getTransitionRelation();
    final int init = rel.getFirstInitialState();
    return new TRAbstractionStepDrop(pred, init);
  }

  protected void addAbstractionStep(final TRAbstractionStep step,
                                    final TRAutomatonProxy outputAut)
  {
    addAbstractionStep(step);
    mCurrentAutomataMap.put(outputAut, step);
  }

  protected void addAbstractionStep(final TRAbstractionStep step)
  {
    mAbstractionSequence.add(step);
  }

  protected void addAbstractionSteps(final List<TRAbstractionStep> steps)
  {
    if (steps != null) {
      mAbstractionSequence.addAll(steps);
    }
  }


  /**
   * Stores a verification result indicating that the property checked
   * is satisfied and marks the run as completed.
   * @return <CODE>true</CODE>
   */
  protected boolean setSatisfiedResult()
  {
    return setBooleanResult(true);
  }


  //#########################################################################
  //# Statistics and Logging
  void countSpecialEvents(final EventEncoding enc)
  {
    final CompositionalAnalysisResult stats = getAnalysisResult();
    final Logger logger = LogManager.getLogger();
    final int numEvents = enc.getNumberOfProperEvents();
    for (int e = EventEncoding.NONTAU; e < numEvents; e++) {
      final byte status = enc.getProperEventStatus(e);
      if (EventStatus.isUsedEvent(status)) {
        final EventProxy event = enc.getProperEvent(e);
        final TREventInfo info = mCurrentSubsystem.getEventInfo(event);
        info.countSpecialEvents(status, stats, logger);
      }
    }
  }

  void recordStatistics(final TRAutomatonProxy aut)
  {
    final CompositionalAnalysisResult result = getAnalysisResult();
    final ListBufferTransitionRelation rel = aut.getTransitionRelation();
    final int numStates = rel.getNumberOfReachableStates();
    result.updateNumberOfStates(numStates);
    final int numTrans = rel.getNumberOfTransitions();
    result.updateNumberOfTransitions(numTrans);
    result.updatePeakMemoryUsage();
  }

  void recordUnsuccessfulComposition(final OverflowException exception)
  {
    LogManager.getLogger().debug(exception.getMessage());
    final CompositionalAnalysisResult result = getAnalysisResult();
    result.addUnsuccessfulComposition();
  }

  void recordMonolithicAttempt(final ProductDESProxy des)
  {
    final Logger logger = LogManager.getLogger();
    if (logger.isDebugEnabled()) {
      double estimate = 1.0;
      final Collection<AutomatonProxy> automata = des.getAutomata();
      for (final AutomatonProxy aut : automata) {
        final TRAutomatonProxy tr = (TRAutomatonProxy) aut;
        final ListBufferTransitionRelation rel = tr.getTransitionRelation();
        estimate *= rel.getNumberOfReachableStates();
      }
      final String code = estimate >= 1e7 ? "%e" : "%.0f";
      final String msg = String.format
        ("Monolithically composing %d automata, estimated " +
         code + " states.", automata.size(), estimate);
      logger.debug(msg);
    }
    if (mMonolithicDumpFile != null) {
      MarshallingTools.saveProductDESorModule(des, mMonolithicDumpFile);
    }
  }


  //#########################################################################
  //# Abstraction Chains
  /**
   * <P>The abstraction sequence that consists of only observation
   * equivalence. This tool creator produces a transition relation simplifier
   * consisting of:</P>
   * <UL>
   * <LI>Special events removal ({@link SpecialEventsTRSimplifier})</LI>
   * <LI>Tau-loop removal ({@link TauLoopRemovalTRSimplifier})</LI>
   * <LI>Observation equivalence ({@link ObservationEquivalenceTRSimplifier})</LI>
   * </UL>.
   */
  public static final TRToolCreator<TransitionRelationSimplifier> OEQ =
    new TRToolCreator<TransitionRelationSimplifier>("OEQ")
  {
    @Override
    public TransitionRelationSimplifier create
      (final AbstractTRCompositionalModelAnalyzer analyzer)
    {
      return analyzer.createObservationEquivalenceChain
        (analyzer.isUsingWeakObservationEquivalence()
         ? Equivalence.WEAK_OBSERVATION_EQUIVALENCE
         : Equivalence.OBSERVATION_EQUIVALENCE);
    }
  };

  /**
   * <P>The abstraction sequence that consists of only weak observation
   * equivalence. This tool creator produces a transition relation simplifier
   * consisting of:</P>
   * <UL>
   * <LI>Special events removal ({@link SpecialEventsTRSimplifier})</LI>
   * <LI>Tau-loop removal ({@link TauLoopRemovalTRSimplifier})</LI>
   * <LI>Weak observation equivalence
   *     ({@link ObservationEquivalenceTRSimplifier})</LI>
   * </UL>.
   */
  public static final TRToolCreator<TransitionRelationSimplifier> WOEQ =
    new TRToolCreator<TransitionRelationSimplifier>("WOEQ")
  {
    @Override
    public TransitionRelationSimplifier create
      (final AbstractTRCompositionalModelAnalyzer analyzer)
    {
      return analyzer.createObservationEquivalenceChain
        (ObservationEquivalenceTRSimplifier.
         Equivalence.WEAK_OBSERVATION_EQUIVALENCE);
    }
  };


  protected ChainTRSimplifier createObservationEquivalenceChain
    (final ObservationEquivalenceTRSimplifier.Equivalence equivalence)
  {
    final TRSimplificationListener specialEventsListener = getSpecialEventsListener();
    final TRSimplificationListener listener = new PartitioningListener();
    final int transitionLimit = getInternalTransitionLimit();
    final EventProxy usedPreconditionMarking = getUsedPreconditionMarking();

    final ChainTRSimplifier chain =
      ChainBuilder.createObservationEquivalenceChain(equivalence,
                                                   listener,
                                                   specialEventsListener);

    chain.setTransitionLimit(transitionLimit);

    final int precond =
      usedPreconditionMarking == null ? -1 : PRECONDITION_MARKING;
    chain.setPropositions(precond, DEFAULT_MARKING);
    chain.setPreferredOutputConfiguration
      (ListBufferTransitionRelation.CONFIG_SUCCESSORS);

    return chain;
  }

  protected ChainTRSimplifier startAbstractionChain()
  {
    final ChainTRSimplifier chain = new ChainTRSimplifier();
    final SpecialEventsTRSimplifier special = new SpecialEventsTRSimplifier();
    special.setSimplificationListener(mSpecialEventsListener);
    chain.add(special);
    return chain;
  }


  //#########################################################################
  //# Inner Class PartitioningListener
  class PartitioningListener implements TRSimplificationListener
  {
    //#######################################################################
    //# Simple Access
    void setEnabled(final boolean enabled)
    {
      mEnabled = enabled;
    }

    //#######################################################################
    //# Interface
    //# net.sourceforge.waters.analysis.abstraction.TRSimplificationListener
    @Override
    public boolean onSimplificationStart
      (final TransitionRelationSimplifier simplifier)
    {
      return mEnabled;
    }

    @Override
    public void onSimplificationFinish
      (final TransitionRelationSimplifier simplifier, final boolean result)
    {
      if (result && isDetailedOutputEnabled() &&
          mIntermediateAbstractionSequence != null) {
        final TRAbstractionStep last =
          mIntermediateAbstractionSequence.getLastIntermediateStep();
        if (last != null && last instanceof TRAbstractionStepPartition) {
          final TRAbstractionStepPartition partPred =
            (TRAbstractionStepPartition) last;
          partPred.merge(simplifier);
        } else {
          final TRAbstractionStep pred = last == null ?
            mIntermediateAbstractionSequence.getPredecessor() : last;
          final EventEncoding enc =
            mIntermediateAbstractionSequence.getCurrentEventEncoding();
          final TRAbstractionStep step =
            new TRAbstractionStepPartition(pred, enc, simplifier);
          mIntermediateAbstractionSequence.append(step);
        }
      }
    }

    //#######################################################################
    //# Data Members
    private boolean mEnabled = true;
  }


  //#########################################################################
  //# Inner Class IntermediateAbstractionSequence
  class IntermediateAbstractionSequence
  {
    //#######################################################################
    //# Constructor
    private IntermediateAbstractionSequence(final TRAutomatonProxy aut)
    {
      mPredecessor =
        mCurrentAutomataMap == null ? null : mCurrentAutomataMap.get(aut);
      mCurrentAutomaton = aut;
      mInputEventEncoding = mCurrentEventEncoding =
        new EventEncoding(aut.getEventEncoding());
      mSteps = new LinkedList<>();
    }

    //#######################################################################
    //# Access
    TRAbstractionStep getPredecessor()
    {
      return mPredecessor;
    }

    EventEncoding getInputEventEncoding()
    {
      return mInputEventEncoding;
    }

    EventEncoding getCurrentEventEncoding()
    {
      return mCurrentEventEncoding;
    }

    TRAbstractionStep getLastIntermediateStep()
    {
      return mSteps.peekLast();
    }

    TRAbstractionStep getLastIntermediateStepOrPredecessor()
    {
      final TRAbstractionStep last = getLastIntermediateStep();
      if (last != null) {
        return last;
      } else {
        return getPredecessor();
      }
    }

    void append(final TRAbstractionStep step)
    {
      mSteps.add(step);
      mCurrentEventEncoding =
        new EventEncoding(mCurrentAutomaton.getEventEncoding());
    }

    TransitionRelationSimplifier getLastPartitionSimplifier()
    {
      final TRAbstractionStep step = getLastIntermediateStep();
      if (step == null) {
        return null;
      } else if (step instanceof TRAbstractionStepPartition) {
        final TRAbstractionStepPartition partStep =
          (TRAbstractionStepPartition) step;
        return partStep.getLastSimplifier();
      } else {
        return null;
      }
    }

    void removeLastPartitionSimplifier(final EventEncoding enc)
    {
      final TRAbstractionStep step = mSteps.peekLast();
      if (step != null && step instanceof TRAbstractionStepPartition) {
        final TRAbstractionStepPartition partStep =
          (TRAbstractionStepPartition) step;
        partStep.removeLastSimplifier();
        if (partStep.isEmpty()) {
          mSteps.removeLast();
        }
      }
      mCurrentEventEncoding = enc;
    }

    //#######################################################################
    //# String Abstraction Steps
    private void commit()
    {
      final TRAbstractionStep last = mSteps.peekLast();
      if (last != null && isDetailedOutputEnabled()) {
        if (mPredecessor != null) {
          mPredecessor.clearOutputAutomaton();
        }
        last.provideOutputAutomaton(mCurrentAutomaton);
        mAbstractionSequence.addAll(mSteps);
        mCurrentAutomataMap.put(mCurrentAutomaton, last);
      }
    }

    //#######################################################################
    //# Data Members
    private final TRAbstractionStep mPredecessor;
    private final TRAutomatonProxy mCurrentAutomaton;
    private final EventEncoding mInputEventEncoding;
    private EventEncoding mCurrentEventEncoding;
    private final LinkedList<TRAbstractionStep> mSteps;
  }


  //#########################################################################
  //# Inner Class SimplificationQueue
  private static class SimplificationQueue
    extends DuplicateFreeQueue<TRAutomatonProxy>
  {
    //#######################################################################
    //# Constructor
    private SimplificationQueue(final Collection<TRAutomatonProxy> automata)
    {
      super(automata);
      mSupressed = Collections.emptySet();
    }

    //#######################################################################
    //# Interface java.util.Queue<TRAutomatonProxy>
    @Override
    public boolean offer(final TRAutomatonProxy aut)
    {
      if (mSupressed.contains(aut)) {
        return true;
      } else {
        return super.offer(aut);
      }
    }

    //#######################################################################
    //# Suppressing Automata Currently Being Modified
    private void setSuppressed(final TRAutomatonProxy aut)
    {
      mSupressed = Collections.singleton(aut);
    }

    private void setSuppressed(final TRCandidate candidate,
                               final TRAutomatonProxy aut)
    {
      final Collection<TRAutomatonProxy> automata = candidate.getAutomata();
      final int size = automata.size() + 1;
      if (size <= 4) {
        mSupressed = new ArrayList<>(size);
      } else {
        mSupressed = new THashSet<>(size);
      }
      mSupressed.addAll(automata);
      mSupressed.add(aut);
    }

    private void clearSuppressed()
    {
      mSupressed = Collections.emptySet();
    }

    //#######################################################################
    //# Data Members
    private Collection<TRAutomatonProxy> mSupressed;
  }


  //#########################################################################
  //# Data Members
  // Configuration
  private int mInternalStateLimit = 100000;
  private int mInternalTransitionLimit = Integer.MAX_VALUE;
  private boolean mBlockedEventsEnabled = true;
  private boolean mFailingEventsEnabled = false;
  private boolean mSelfloopOnlyEventsEnabled = false;
  private boolean mAlwaysEnabledEventsEnabled = false;
  private File mMonolithicDumpFile = null;
  private boolean mOutputCheckingEnabled = false;
  private boolean mUsingWeakObservationEquivalence = false;

  // Tools
  private TRPreselectionHeuristic mPreselectionHeuristic;
  private SelectionHeuristic<TRCandidate> mSelectionHeuristic;
  private final PartitioningListener mSpecialEventsListener;
  private SpecialEventsFinder mSpecialEventsFinder;
  private TRToolCreator<TransitionRelationSimplifier> mTRSimplifierCreator;
  private TransitionRelationSimplifier mTRSimplifier;
  private AbstractTRSynchronousProductBuilder mSynchronousProductBuilder;
  private ModelAnalyzer mMonolithicAnalyzer;

  // Data Structures
  private List<TRAbstractionStep> mAbstractionSequence;
  private IntermediateAbstractionSequence mIntermediateAbstractionSequence;
  private Map<TRAutomatonProxy,TRAbstractionStep> mCurrentAutomataMap;
  private Queue<TRSubsystemInfo> mSubsystemQueue;
  private TRSubsystemInfo mCurrentSubsystem;
  private SimplificationQueue mNeedsSimplification;
  private boolean mNeedsDisjointSubsystemsCheck;
  private boolean mAlwaysEnabledDetectedInitially;
  private int mStepNo;


  //#########################################################################
  //# Class Constants
  public static final TRPreselectionHeuristic PRESEL_MustL =
    new TRPreselectionHeuristicMustL();
  public static final TRPreselectionHeuristic PRESEL_MustSp =
    new TRPreselectionHeuristicMustSp();
  public static final TRPreselectionHeuristic PRESEL_Pairs =
    new TRPreselectionHeuristicPairs();
  public static final TRPreselectionHeuristic PRESEL_MinS =
    new TRPreselectionHeuristicMinS();
  public static final TRPreselectionHeuristic PRESEL_MaxS =
    new TRPreselectionHeuristicMaxS();
  public static final TRPreselectionHeuristic PRESEL_MinT =
    new TRPreselectionHeuristicMinT();
  public static final TRPreselectionHeuristic PRESEL_MaxT =
    new TRPreselectionHeuristicMaxT();

  public static final SelectionHeuristic<TRCandidate> SEL_MaxC =
    new SelectionHeuristicMaxC();
  public static final SelectionHeuristic<TRCandidate> SEL_MaxL =
    new SelectionHeuristicMaxL();
  public static final SelectionHeuristic<TRCandidate> SEL_MinE =
    new SelectionHeuristicMinE();
  public static final SelectionHeuristic<TRCandidate> SEL_MinF1 =
    new SelectionHeuristicMinF1();
  public static final SelectionHeuristic<TRCandidate> SEL_MinF2 =
    new SelectionHeuristicMinF2();
  public static final NumericSelectionHeuristic<TRCandidate> SEL_MinS0 =
    new SelectionHeuristicMinS0();
  public static final NumericSelectionHeuristic<TRCandidate> SEL_MinS0a =
    new SelectionHeuristicMinS0a();
  public static final SelectionHeuristic<TRCandidate> SEL_MinS =
    new SelectionHeuristicMinS(SEL_MinS0);
  public static final SelectionHeuristic<TRCandidate> SEL_MinSa =
    new SelectionHeuristicMinS(SEL_MinS0a);
  public static final SelectionHeuristic<TRCandidate> SEL_MinSSp =
    new SelectionHeuristicMinSSp(SEL_MinS0);
  public static final SelectionHeuristic<TRCandidate> SEL_MinSync =
    new SelectionHeuristicMinSync();
  public static final SelectionHeuristic<TRCandidate> SEL_MinSyncA =
    new SelectionHeuristicMinSyncA();

  static final int DEFAULT_MARKING = 0;
  static final int PRECONDITION_MARKING = 1;

}
