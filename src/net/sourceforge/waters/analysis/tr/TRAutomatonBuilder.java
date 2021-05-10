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

package net.sourceforge.waters.analysis.tr;

import java.util.List;

import net.sourceforge.waters.analysis.abstraction.StepSimplifierFactory;
import net.sourceforge.waters.analysis.abstraction.TransitionRelationSimplifier;
import net.sourceforge.waters.analysis.options.Option;
import net.sourceforge.waters.analysis.options.OptionPage;
import net.sourceforge.waters.analysis.options.PropositionOption;
import net.sourceforge.waters.model.analysis.AnalysisException;
import net.sourceforge.waters.model.analysis.des.AbstractAutomatonBuilder;
import net.sourceforge.waters.model.analysis.des.AutomatonBuilder;
import net.sourceforge.waters.model.analysis.des.ModelAnalyzer;
import net.sourceforge.waters.model.analysis.kindtranslator.KindTranslator;
import net.sourceforge.waters.model.des.AutomatonProxy;
import net.sourceforge.waters.model.des.EventProxy;
import net.sourceforge.waters.model.des.ProductDESProxy;
import net.sourceforge.waters.model.des.ProductDESProxyFactory;
import net.sourceforge.waters.model.des.StateProxy;


/**
 * A utility to invoke {@link TransitionRelationSimplifier} through
 * the {@link ModelAnalyzer} interface.
 *
 * @author Benjamin Wheeler, Robi Malik
 */

public class TRAutomatonBuilder extends AbstractAutomatonBuilder
  implements AutomatonBuilder
{

  //#########################################################################
  //# Constructor
  public TRAutomatonBuilder(final ProductDESProxyFactory factory,
                            final TransitionRelationSimplifier simp) {
    super(factory);
    mSimplifier = simp;
  }


  //#########################################################################
  //# Interface net.sourceforge.waters.model.analysis.options.Configurable
  @Override
  public List<Option<?>> getOptions(final OptionPage db)
  {
    final List<Option<?>> options = mSimplifier.getOptions(db);
    return options;
  }

  @Override
  public void setOption(final Option<?> option)
  {

    if (option.hasID(StepSimplifierFactory.
                     OPTION_AbstractMarking_PreconditionMarkingID)) {
      final PropositionOption propOption = (PropositionOption) option;
      mPreconditionMarking = propOption.getValue();
    } else if (option.hasID(StepSimplifierFactory.
                            OPTION_AbstractMarking_DefaultMarkingID)) {
      final PropositionOption propOption = (PropositionOption) option;
      mDefaultMarking = propOption.getValue();
    } else {
      mSimplifier.setOption(option);
    }
  }


  //#########################################################################
  //# Interface net.sourceforge.waters.model.analysis.des.ModelAnalyzer
  @Override
  public void setUp()
    throws AnalysisException
  {
    super.setUp();

    final ProductDESProxy des = getModel();
    final AutomatonProxy aut = des.getAutomata().iterator().next();
    int config = mSimplifier.getPreferredInputConfiguration();
    if ((config & ListBufferTransitionRelation.CONFIG_ALL) == 0) {
      config |= ListBufferTransitionRelation.CONFIG_SUCCESSORS;
    }
    if (aut instanceof TRAutomatonProxy) {
      mTrAut = new TRAutomatonProxy((TRAutomatonProxy) aut, config);
    } else {
      final KindTranslator translator = getKindTranslator();
      final StateProxy dumpState = TRAutomatonProxy.findDumpState(aut);
      mTrAut = new TRAutomatonProxy(aut, translator, dumpState, config);
    }
    final EventEncoding enc = mTrAut.getEventEncoding();
    final int preconditionID = enc.getEventCode(mPreconditionMarking);
    final int defaultID = enc.getEventCode(mDefaultMarking);
    mSimplifier.setPropositions(preconditionID, defaultID);
  }

  @Override
  public boolean run() throws AnalysisException
  {
    try {
      setUp();
      final ListBufferTransitionRelation tr = mTrAut.getTransitionRelation();
      mSimplifier.setTransitionRelation(tr);
      mSimplifier.run();
      mTrAut.setName(mOutputName);
      return setProxyResult(mTrAut);
    } finally {
      tearDown();
    }
  }

  @Override
  protected void tearDown()
  {
    mTrAut = null;
    super.tearDown();
  }

  @Override
  public boolean supportsNondeterminism()
  {
    return true;
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


  //#########################################################################
  //# Data Members
  private final TransitionRelationSimplifier mSimplifier;

  private EventProxy mPreconditionMarking;
  private EventProxy mDefaultMarking;

  private TRAutomatonProxy mTrAut;

  private String mOutputName;

}
