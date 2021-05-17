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

package net.sourceforge.waters.model.analysis.des;

import java.util.List;

import net.sourceforge.waters.analysis.abstraction.ProjectingSupervisorReductionFactory;
import net.sourceforge.waters.analysis.abstraction.SupervisorReductionFactory;
import net.sourceforge.waters.analysis.abstraction.SupervisorReductionMainMethod;
import net.sourceforge.waters.analysis.abstraction.SupervisorReductionProjectionMethod;
import net.sourceforge.waters.model.options.BooleanOption;
import net.sourceforge.waters.model.options.EnumOption;
import net.sourceforge.waters.model.options.LeafOptionPage;
import net.sourceforge.waters.model.options.Option;
import net.sourceforge.waters.model.options.PositiveIntOption;
import net.sourceforge.waters.model.options.PropositionOption;
import net.sourceforge.waters.model.options.StringOption;
import net.sourceforge.waters.model.analysis.AnalysisException;
import net.sourceforge.waters.model.analysis.kindtranslator.ConflictKindTranslator;
import net.sourceforge.waters.model.analysis.kindtranslator.IdenticalKindTranslator;
import net.sourceforge.waters.model.analysis.kindtranslator.KindTranslator;
import net.sourceforge.waters.model.des.EventProxy;
import net.sourceforge.waters.model.des.ProductDESProxy;
import net.sourceforge.waters.model.des.ProductDESProxyFactory;


/**
 * A skeleton supervisor synthesiser class.
 * This includes basic implementations of the options specific in the
 * {@link SupervisorSynthesizer} interface.
 *
 * @author Robi Malik
 */

public abstract class AbstractSupervisorSynthesizer
  extends AbstractProductDESBuilder
  implements SupervisorSynthesizer
{

  //#########################################################################
  //# Constructors
  public AbstractSupervisorSynthesizer(final ProductDESProxyFactory factory)
  {
    super(factory);
  }

  public AbstractSupervisorSynthesizer(final ProductDESProxy model,
                                       final ProductDESProxyFactory factory)
  {
    super(model, factory);
  }

  public AbstractSupervisorSynthesizer(final ProductDESProxy model,
                                       final ProductDESProxyFactory factory,
                                       final KindTranslator translator)
  {
    super(model, factory, translator);
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
  public void setConfiguredDefaultMarking(final EventProxy marking)
  {
    mConfiguredMarking = marking;
    mUsedMarking = null;
  }

  @Override
  public EventProxy getConfiguredDefaultMarking()
  {
    return mConfiguredMarking;
  }

  @Override
  public void setNondeterminismEnabled(final boolean enable)
  {
    mNondeterminismEnabled = enable;
  }

  @Override
  public SupervisorReductionFactory getSupervisorReductionFactory()
  {
    return mSupervisorReductionFactory;
  }

  @Override
  public void setSupervisorReductionFactory
    (final SupervisorReductionFactory factory)
  {
    mSupervisorReductionFactory = factory;
  }

  @Override
  public void setSupervisorLocalizationEnabled(final boolean enable)
  {
    mSupervisorLocalizationEnabled = enable;
  }

  @Override
  public boolean isSupervisorLocalizationEnabled()
  {
    return mSupervisorLocalizationEnabled;
  }


  //#########################################################################
  //# Interface net.sourceforge.waters.model.analysis.ModelAnalyzer
  @Override
  public boolean supportsNondeterminism()
  {
    return mNondeterminismEnabled;
  }

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
    db.append(options, AbstractModelAnalyzerFactory.OPTION_ModelAnalyzer_FinalStateLimit);
    db.append(options, AbstractModelAnalyzerFactory.OPTION_ModelAnalyzer_FinalTransitionLimit);
    db.append(options, AbstractModelAnalyzerFactory.
                       OPTION_SupervisorSynthesizer_SupervisorReductionMainMethod);
    db.append(options, AbstractModelAnalyzerFactory.
                       OPTION_SupervisorSynthesizer_SupervisorReductionProjectionMethod);
    db.append(options, AbstractModelAnalyzerFactory.
                       OPTION_SupervisorSynthesizer_SupervisorLocalisationEnabled);
    return options;
  }

  @SuppressWarnings("unchecked")
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
    } else if (option.hasID(AbstractModelAnalyzerFactory.OPTION_ModelAnalyzer_FinalStateLimit)) {
      final PositiveIntOption intOption = (PositiveIntOption) option;
      setNodeLimit(intOption.getIntValue());
    } else if (option.hasID(AbstractModelAnalyzerFactory.OPTION_ModelAnalyzer_FinalTransitionLimit)) {
      final PositiveIntOption intOption = (PositiveIntOption) option;
      setTransitionLimit(intOption.getIntValue());
    } else if (option.hasID(AbstractModelAnalyzerFactory.
                            OPTION_SupervisorSynthesizer_SupervisorReductionMainMethod)) {
      final EnumOption<SupervisorReductionMainMethod> enumOption =
        (EnumOption<SupervisorReductionMainMethod>) option;
      ProjectingSupervisorReductionFactory.configureSynthesizer
        (this, enumOption.getValue());
    } else if (option.hasID(AbstractModelAnalyzerFactory.
                            OPTION_SupervisorSynthesizer_SupervisorReductionProjectionMethod)) {
      final EnumOption<SupervisorReductionProjectionMethod> enumOption =
        (EnumOption<SupervisorReductionProjectionMethod>) option;
      ProjectingSupervisorReductionFactory.configureSynthesizer
        (this, enumOption.getValue());
    } else if (option.hasID(AbstractModelAnalyzerFactory.
                            OPTION_SupervisorSynthesizer_SupervisorLocalisationEnabled)) {
      final BooleanOption boolOption = (BooleanOption) option;
      setSupervisorLocalizationEnabled(boolOption.getBooleanValue());
    } else {
      super.setOption(option);
    }
  }


  //#########################################################################
  //# Overrides for
  //# net.sourceforge.waters.model.analysis.AbstractModelAnalyzer
  @Override
  protected void setUp()
    throws AnalysisException
  {
    super.setUp();
    setUpUsedDefaultMarking();
  }

  @Override
  protected void tearDown()
  {
    super.tearDown();
    mUsedMarking = null;
  }


  //#########################################################################
  //# Specific Access
  /**
   * <P>Determines the marking proposition to be used.</P>
   * <P>This method determines and stores the default marking proposition for
   * later retrieval by {@link #getUsedDefaultMarking()}. If synthesising
   * a nonblocking supervisor, the used default marking is either the
   * configured default marking or the default marking of the input model.
   * If not synthesising a nonblocking supervisor, the used default marking
   * is <CODE>null</CODE>.</P>
   * <P>This method is called at the beginning of {@link #setUp()}.</P>
   * @return The default marking that has been stored.
   * @see #setNonblockingSynthesis(boolean)
   * @see #setConfiguredDefaultMarking(EventProxy)
   * @see #getUsedDefaultMarking()
   */
  protected EventProxy setUpUsedDefaultMarking()
  {
    if (!mNonblockingSynthesis) {
      mUsedMarking = null;
    } else if (mConfiguredMarking != null) {
      mUsedMarking = mConfiguredMarking;
    } else {
      final ProductDESProxy des = getModel();
      mUsedMarking = AbstractConflictChecker.getMarkingProposition(des);
    }
    return mUsedMarking;
  }

  /**
   * Retrieves the default marking determined by
   * {@link #getUsedDefaultMarking()}.
   */
  protected EventProxy getUsedDefaultMarking()
  {
   return mUsedMarking;
  }

  /**
   * Suggests a name prefix for synthesised supervisors. The suggested name
   * prefix is equal to the output name ({@link #getOutputName()} if it is
   * non-<CODE>null</CODE>, otherwise it is <CODE>&quot;sup;&quot;</CODE>.
   */
  protected String getSupervisorNamePrefix()
  {
    final String outputName = getOutputName();
    return outputName == null ? "sup" : outputName;
  }


  //#########################################################################
  //# Data Members
  private boolean mNonblockingSynthesis = true;
  private EventProxy mConfiguredMarking = null;
  private EventProxy mUsedMarking = null;
  private boolean mNondeterminismEnabled = false;
  private SupervisorReductionFactory mSupervisorReductionFactory =
    new ProjectingSupervisorReductionFactory();
  private boolean mSupervisorLocalizationEnabled = false;

}
