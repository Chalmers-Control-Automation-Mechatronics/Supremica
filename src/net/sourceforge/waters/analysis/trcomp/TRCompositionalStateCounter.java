//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# Copyright (C) 2004-2016 Robi Malik
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

import java.util.List;

import net.sourceforge.waters.analysis.abstraction.ChainTRSimplifier;
import net.sourceforge.waters.analysis.abstraction.ReverseObservationEquivalenceTRSimplifier;
import net.sourceforge.waters.analysis.abstraction.TRSimplificationListener;
import net.sourceforge.waters.analysis.abstraction.TauLoopRemovalTRSimplifier;
import net.sourceforge.waters.analysis.abstraction.TransitionRelationSimplifier;
import net.sourceforge.waters.analysis.compositional.CompositionalAnalysisResult;
import net.sourceforge.waters.analysis.monolithic.TRAbstractSynchronousProductBuilder;
import net.sourceforge.waters.analysis.monolithic.TRSynchronousProductBuilder;
import net.sourceforge.waters.analysis.tr.ListBufferTransitionRelation;
import net.sourceforge.waters.analysis.tr.TRAutomatonProxy;
import net.sourceforge.waters.model.analysis.AnalysisConfigurationException;
import net.sourceforge.waters.model.analysis.AnalysisException;
import net.sourceforge.waters.model.analysis.EnumFactory;
import net.sourceforge.waters.model.analysis.IdenticalKindTranslator;
import net.sourceforge.waters.model.analysis.KindTranslator;
import net.sourceforge.waters.model.analysis.ListedEnumFactory;
import net.sourceforge.waters.model.analysis.des.ModelAnalyzer;
import net.sourceforge.waters.model.analysis.des.StateCounter;
import net.sourceforge.waters.model.des.AutomatonTools;
import net.sourceforge.waters.model.des.ProductDESProxy;
import net.sourceforge.waters.model.des.ProductDESProxyFactory;

/**
 * A compositional state counter based on {@link TRAutomatonProxy}, which
 * uses <I>reverse observation equivalence</I> as its main abstraction
 * rule, in addition to <I>hiding</I> and <I>tau-loop removal</I>.
 *
 * @author Roger Su
 */
public class TRCompositionalStateCounter
  extends AbstractTRCompositionalAnalyzer
  implements StateCounter
{
  //#########################################################################
  //# Constructors
  public TRCompositionalStateCounter()
  {
    this(null);
  }

  public TRCompositionalStateCounter(final ProductDESProxy model)
  {
    this (model, IdenticalKindTranslator.getInstance(), null);
  }

  public TRCompositionalStateCounter(final ProductDESProxy model,
                                     final KindTranslator translator,
                                     final ModelAnalyzer mono)
  {
    super(model, translator, mono);
  }


  //#########################################################################
  // Overriding Methods
  @Override
  protected boolean analyseSubsystemMonolithically(final TRSubsystemInfo subsys)
    throws AnalysisException
  {
    if (subsys == null || subsys.getNumberOfAutomata() == 0) {
      // Do nothing.
    } else {
      // Construct the synchronous composition.
      final ProductDESProxyFactory factory = getFactory();
      final List<TRAutomatonProxy> automata = subsys.getAutomata();
      final ProductDESProxy des =
        AutomatonTools.createProductDESProxy("StateCount", automata, factory);
      final TRSynchronousProductBuilder syncBuilder =
        new TRSynchronousProductBuilder(des);
      syncBuilder.setNodeLimit(getMonolithicStateLimit());
      syncBuilder.setTransitionLimit(this.getMonolithicTransitionLimit());
      syncBuilder.setCountingStates(true);
      syncBuilder.run();
      // Retrieve the state count.
      final TRAutomatonProxy syncProduct = syncBuilder.getComputedProxy();
      final long trStateCount = syncProduct.getTransitionRelation().getTotalStateCount();
      mTotalStateCount *= trStateCount;
    }
    return true;
  }

  @Override
  protected TRAbstractSynchronousProductBuilder createSynchronousProductBuilder()
  {
    final KindTranslator translator = getKindTranslator();
    final TRAbstractSynchronousProductBuilder syncBuilder = new TRSynchronousProductBuilder();
    syncBuilder.setCountingStates(true);
    syncBuilder.setDetailedOutputEnabled(false);
    syncBuilder.setKindTranslator(translator);
    syncBuilder.setRemovingSelfloops(true);
    syncBuilder.setNodeLimit(getInternalStateLimit());
    syncBuilder.setTransitionLimit(getInternalTransitionLimit());
    return syncBuilder;
  }

  @Override
  protected void dropTrivialAutomaton(final TRAutomatonProxy aut)
  {
    mTotalStateCount *= aut.getTransitionRelation().getTotalStateCount();
    super.dropTrivialAutomaton(aut);
  }

  @Override
  public boolean run() throws AnalysisException
  {
    // Carry out the normal 'run' method.
    super.run();

    // Overwrite the state count in AnalysisResult.
    final CompositionalAnalysisResult result = getAnalysisResult();
    result.setTotalNumberOfStates(mTotalStateCount);

    // State counting always returns true.
    return true;
  }

  @Override
  protected void setUp() throws AnalysisException
  {
    // Reset
    mTotalStateCount = 1;

    // Carry out the normal 'setUp' method.
    super.setUp();

    // Ensures that the transition relations do state counting.
    final TRSubsystemInfo subsystem = getCurrentSubsystem();
    final List<TRAutomatonProxy> automata = subsystem.getAutomata();
    for (final TRAutomatonProxy aut : automata) {
      int config = aut.getTransitionRelation().getConfiguration();
      config |= ListBufferTransitionRelation.CONFIG_COUNT;
      aut.getTransitionRelation().reconfigure(config);
    }
  }


  //#########################################################################
  //# Abstraction Chain
  @Override
  public EnumFactory<TRToolCreator<TransitionRelationSimplifier>> getTRSimplifierFactory()
  {
    return
      new ListedEnumFactory<TRToolCreator<TransitionRelationSimplifier>>() {
      {
        register(ROEQ);
      }
    };
  }

  /**
   * Abstraction chain for compositional state counting:
   * <OL>
   * <LI>Hiding</LI>
   * <LI>Tau-loop removal</LI>
   * <LI>Reverse observation equivalence</LI>
   * </OL>
   */
  public static final TRToolCreator<TransitionRelationSimplifier> ROEQ =
    new TRToolCreator<TransitionRelationSimplifier>("ROEQ")
  {
    @Override
    public TransitionRelationSimplifier create
      (final AbstractTRCompositionalAnalyzer analyzer)
        throws AnalysisConfigurationException
    {
      // Configuration
      final int transitionLimit = analyzer.getInternalTransitionLimit();
      final TRSimplificationListener listener =
        analyzer.new PartitioningListener();

      // The initial abstraction chain
      final ChainTRSimplifier chain = analyzer.startAbstractionChain();

      // Tau-loop removal
      final TransitionRelationSimplifier loopRemover =
        new TauLoopRemovalTRSimplifier();
      loopRemover.setSimplificationListener(listener);
      chain.add(loopRemover);

      // Reverse observation equivalence
      final ReverseObservationEquivalenceTRSimplifier simplifier =
        new ReverseObservationEquivalenceTRSimplifier();
      simplifier.setTransitionLimit(transitionLimit);
      simplifier.setSimplificationListener(listener);
      //chain.add(simplifier);

      // Finialise
      chain.setPreferredOutputConfiguration
        (ListBufferTransitionRelation.CONFIG_SUCCESSORS);
      return chain;
    }
  };


  //#########################################################################
  //# Data Members
  private long mTotalStateCount;
}
