//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# Copyright (C) 2004-2021 Robi Malik
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

import java.io.File;
import java.util.List;

import net.sourceforge.waters.analysis.abstraction.TransitionRelationSimplifier;
import net.sourceforge.waters.analysis.compositional.SelectionHeuristic;
import net.sourceforge.waters.analysis.options.BooleanOption;
import net.sourceforge.waters.analysis.options.EnumOption;
import net.sourceforge.waters.analysis.options.FileOption;
import net.sourceforge.waters.analysis.options.Option;
import net.sourceforge.waters.analysis.options.OptionPage;
import net.sourceforge.waters.analysis.options.PositiveIntOption;
import net.sourceforge.waters.model.analysis.AnalysisConfigurationException;
import net.sourceforge.waters.model.analysis.EnumFactory;
import net.sourceforge.waters.model.analysis.des.AbstractModelAnalyzerFactory;
import net.sourceforge.waters.model.analysis.des.ModelAnalyzer;
import net.sourceforge.waters.model.analysis.kindtranslator.KindTranslator;
import net.sourceforge.waters.model.des.ProductDESProxy;


/**
 * <P>An abstract base class for compositional model analysers based that
 * work by delegation.</P>
 *
 * <P>A delegating model analyser performs some model transformations on its
 * input, and passes one or more transformed models to another compositional
 * model analyser (the delegate), which does the actual work. The results
 * (e.g., counterexamples) obtained from the delegate may have to be
 * transformed back to match the original input.</P>
 *
 * <P>This abstract base class implements the delegation mechanism by
 * passing on most configuration options to the delegate.</P>
 *
 * @author Robi Malik
 */

public abstract class AbstractTRDelegatingAnalyzer
  extends AbstractTRAnalyzer
{

  //#########################################################################
  //# Constructors
  public AbstractTRDelegatingAnalyzer(final ProductDESProxy model,
                                      final KindTranslator translator,
                                      final AbstractTRAnalyzer delegate)
  {
    super(model, translator);
    mDelegate = delegate;
    mDelegate.setPreservingEncodings(true);
  }

  //#########################################################################
  //# Interface net.sourceforge.waters.model.analysis.ModelAnalyzer
  @Override
  public List<Option<?>> getOptions(final OptionPage db)
  {
    final List<Option<?>> options = super.getOptions(db);
    db.append(options, TRCompositionalModelAnalyzerFactory.
                       OPTION_AbstractTRCompositionalModelAnalyzer_PreselectionHeuristic);
    db.append(options, TRCompositionalModelAnalyzerFactory.
                       OPTION_AbstractTRCompositionalModelAnalyzer_SelectionHeuristic);
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
    } else {
      super.setOption(option);
    }
  }


  //#########################################################################
  //# Interface for net.sourceforge.waters.model.analysis.des.ModelAnalyser
  @Override
  public boolean supportsNondeterminism()
  {
    return mDelegate.supportsNondeterminism();
  }

  @Override
  public void setNodeLimit(final int limit)
  {
    mDelegate.setNodeLimit(limit);
  }

  @Override
  public int getNodeLimit()
  {
    return mDelegate.getNodeLimit();
  }

  @Override
  public void setTransitionLimit(final int limit)
  {
    mDelegate.setTransitionLimit(limit);
  }

  @Override
  public int getTransitionLimit()
  {
    return mDelegate.getTransitionLimit();
  }

  @Override
  public void setDetailedOutputEnabled(final boolean enable)
  {
    mDelegate.setDetailedOutputEnabled(enable);
  }

  @Override
  public boolean isDetailedOutputEnabled()
  {
    return mDelegate.isDetailedOutputEnabled();
  }


  //#########################################################################
  //# Overrides for net.sourceforge.waters.analysis.trcomp.AbstractTRAnalyzer
  @Override
  public EnumFactory<TRToolCreator<TransitionRelationSimplifier>>
    getTRSimplifierFactory()
  {
    return mDelegate.getTRSimplifierFactory();
  }

  @Override
  public void setSimplifierCreator
    (final TRToolCreator<TransitionRelationSimplifier> creator)
  {
    mDelegate.setSimplifierCreator(creator);
  }

  @Override
  public TRToolCreator<TransitionRelationSimplifier> getSimplifierCreator()
  {
    return mDelegate.getSimplifierCreator();
  }

  @Override
  public EnumFactory<TRPreselectionHeuristic> getPreselectionHeuristicFactory()
  {
    return mDelegate.getPreselectionHeuristicFactory();
  }

  @Override
  public void setPreselectionHeuristic(final TRPreselectionHeuristic heu)
  {
    mDelegate.setPreselectionHeuristic(heu);
  }

  @Override
  public TRPreselectionHeuristic getPreselectionHeuristic()
  {
    return mDelegate.getPreselectionHeuristic();
  }

  @Override
  public EnumFactory<SelectionHeuristic<TRCandidate>>
    getSelectionHeuristicFactory()
  {
    return mDelegate.getSelectionHeuristicFactory();
  }

  @Override
  public void setSelectionHeuristic(final SelectionHeuristic<TRCandidate> heu)
  {
    mDelegate.setSelectionHeuristic(heu);
  }

  @Override
  public SelectionHeuristic<TRCandidate> getSelectionHeuristic()
  {
    return mDelegate.getSelectionHeuristic();
  }

  @Override
  public void setMonolithicAnalyzer(final ModelAnalyzer mono)
  {
    mDelegate.setMonolithicAnalyzer(mono);
  }

  @Override
  public ModelAnalyzer getMonolithicAnalyzer()
  {
    return mDelegate.getMonolithicAnalyzer();
  }

  @Override
  public void setInternalStateLimit(final int limit)
  {
    mDelegate.setInternalStateLimit(limit);
  }

  @Override
  public int getInternalStateLimit()
  {
    return mDelegate.getInternalStateLimit();
  }

  @Override
  public void setMonolithicStateLimit(final int limit)
  {
    mDelegate.setMonolithicStateLimit(limit);
  }

  @Override
  public int getMonolithicStateLimit()
  {
    return mDelegate.getMonolithicStateLimit();
  }

  @Override
  public void setInternalTransitionLimit(final int limit)
  {
    mDelegate.setInternalTransitionLimit(limit);
  }

  @Override
  public int getInternalTransitionLimit()
  {
    return mDelegate.getInternalTransitionLimit();
  }

  @Override
  public void setMonolithicTransitionLimit(final int limit)
  {
    mDelegate.setMonolithicTransitionLimit(limit);
  }

  @Override
  public int getMonolithicTransitionLimit()
  {
    return mDelegate.getMonolithicTransitionLimit();
  }

  @Override
  public void setBlockedEventsEnabled(final boolean enable)
  {
    mDelegate.setBlockedEventsEnabled(enable);
  }

  @Override
  public boolean isBlockedEventsEnabled()
  {
    return mDelegate.isBlockedEventsEnabled();
  }

  @Override
  public void setFailingEventsEnabled(final boolean enable)
  {
    mDelegate.setFailingEventsEnabled(enable);
  }

  @Override
  public boolean isFailingEventsEnabled()
  {
    return mDelegate.isFailingEventsEnabled();
  }

  @Override
  public void setSelfloopOnlyEventsEnabled(final boolean enable)
  {
    mDelegate.setSelfloopOnlyEventsEnabled(enable);
  }

  @Override
  public boolean isSelfloopOnlyEventsEnabled()
  {
    return mDelegate.isSelfloopOnlyEventsEnabled();
  }

  @Override
  public void setAlwaysEnabledEventsEnabled(final boolean enable)
  {
    mDelegate.setAlwaysEnabledEventsEnabled(enable);
  }

  @Override
  public boolean isAlwaysEnabledEventsEnabled()
  {
    return mDelegate.isAlwaysEnabledEventsEnabled();
  }

  @Override
  public void setMonolithicDumpFile(final File file)
  {
    mDelegate.setMonolithicDumpFile(file);
  }

  @Override
  public File getMonolithicDumpFile()
  {
    return mDelegate.getMonolithicDumpFile();
  }

  @Override
  public void setOutputCheckingEnabled(final boolean checking)
  {
    mDelegate.setOutputCheckingEnabled(checking);
  }

  @Override
  public boolean isOutputCheckingEnabled()
  {
    return mDelegate.isOutputCheckingEnabled();
  }

  @Override
  protected AbstractTRCompositionalModelAnalyzer getCompositionalAnalyzer()
  {
    return mDelegate.getCompositionalAnalyzer();
  }

  @Override
  protected int getPreferredInputConfiguration()
    throws AnalysisConfigurationException
  {
    return mDelegate.getPreferredInputConfiguration();
  }


  //#########################################################################
  //# Interface net.sourceforge.waters.model.analysis.Abortable
  @Override
  public void requestAbort()
  {
    super.requestAbort();
    mDelegate.requestAbort();
  }

  @Override
  public void resetAbort()
  {
    super.resetAbort();
    mDelegate.resetAbort();
  }


  //#########################################################################
  //# Simple Access
  protected AbstractTRAnalyzer getDelegate()
  {
    return mDelegate;
  }


  //#########################################################################
  //# Data Members
  private final AbstractTRAnalyzer mDelegate;

}
