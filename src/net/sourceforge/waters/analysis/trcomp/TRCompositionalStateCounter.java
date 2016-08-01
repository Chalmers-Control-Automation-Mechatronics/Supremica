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

import net.sourceforge.waters.analysis.abstraction.ObservationEquivalenceTRSimplifier.Equivalence;
import net.sourceforge.waters.analysis.abstraction.TransitionRelationSimplifier;
import net.sourceforge.waters.analysis.tr.TRAutomatonProxy;
import net.sourceforge.waters.model.analysis.AnalysisException;
import net.sourceforge.waters.model.analysis.EnumFactory;
import net.sourceforge.waters.model.analysis.KindTranslator;
import net.sourceforge.waters.model.analysis.des.ModelVerifier;
import net.sourceforge.waters.model.analysis.des.StateCounter;
import net.sourceforge.waters.model.des.ProductDESProxy;
import net.sourceforge.waters.model.des.TraceProxy;

/**
 * A compositional state counter based on {@link TRAutomatonProxy} that
 * can be configured to use different abstraction sequences for its
 * simplification steps.
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
    this (model, null, null);
  }

  public TRCompositionalStateCounter(final ProductDESProxy model,
                                     final KindTranslator translator,
                                     final ModelVerifier mono)
  {
    super(model, translator, mono);
  }

  //#########################################################################
  /**
   * Override this method?
   */
  /*@Override
  protected TRAbstractSynchronousProductBuilder createSynchronousProductBuilder()
  {
    final KindTranslator translator = getKindTranslator();
    final TRAbstractSynchronousProductBuilder builder =
      new TRSynchronousProductBuilder();
    builder.setDetailedOutputEnabled(true);
    builder.setKindTranslator(translator);
    builder.setRemovingSelfloops(true);
    builder.setNodeLimit(mInternalStateLimit);
    builder.setTransitionLimit(mInternalTransitionLimit);
    return builder;
  }*/

  /**
   * Inherited from the superclass {@link AbstractTRCompositionalAnalyzer}.
   * <p>
   * Because counterexamples are irrelevant in the computation of the state
   * count, this method always returns <code>null</code>.
   */
  @Override
  public TraceProxy getCounterExample()
  {
    return null;
  }



  @Override
  public EnumFactory<TRToolCreator<TransitionRelationSimplifier>> getTRSimplifierFactory()
  {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  protected boolean analyseSubsystemMonolithically(final TRSubsystemInfo subsys)
    throws AnalysisException
  {
    return false;
  }

  @Override
  protected TRTraceProxy createEmptyTrace(final ProductDESProxy des)
  {
    return null;
  }

  protected TransitionRelationSimplifier createStateCountEquivalentChain
    (final Equivalence reverseObservationEquivalence,
     final boolean b, final boolean c, final boolean d)
  {
    return null;
  }

  //#########################################################################
  //# Abstraction Chains
  /**
   * Reverse Observation Equivalence
   */
  /*public static final TRToolCreator<TransitionRelationSimplifier> ROEQ =
    new TRToolCreator<TransitionRelationSimplifier>("ROEQ")
  {
    @Override
    public TransitionRelationSimplifier create
      (final AbstractTRCompositionalAnalyzer analyzer)
        throws AnalysisConfigurationException
    {
      final TRCompositionalStateCounter checker =
        (TRCompositionalStateCounter) analyzer;
      return checker.createStateCountEquivalentChain
        (ObservationEquivalenceTRSimplifier.
         Equivalence.OBSERVATION_EQUIVALENCE,
         true, true, true);
    }
  };*/

  //#########################################################################
  //# Data Members
  // Configuration
  //private final int mInternalStateLimit = Integer.MAX_VALUE;
  //private final int mInternalTransitionLimit = Integer.MAX_VALUE;

}
