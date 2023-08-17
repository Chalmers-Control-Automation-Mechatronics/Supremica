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

import java.util.List;

import net.sourceforge.waters.model.options.ChainedAnalyzerOption;
import net.sourceforge.waters.model.options.EnumOption;
import net.sourceforge.waters.model.options.LeafOptionPage;
import net.sourceforge.waters.model.options.Option;
import net.sourceforge.waters.model.analysis.AnalysisConfigurationException;
import net.sourceforge.waters.model.analysis.des.AbstractSafetyVerifier;
import net.sourceforge.waters.model.analysis.des.SafetyDiagnostics;
import net.sourceforge.waters.model.analysis.des.SafetyVerifier;
import net.sourceforge.waters.model.analysis.kindtranslator.KindTranslator;
import net.sourceforge.waters.model.base.WatersRuntimeException;
import net.sourceforge.waters.model.des.ProductDESProxy;
import net.sourceforge.waters.model.des.ProductDESProxyFactory;


/**
 * <P>A common superclass for all modular verifiers. This class provides
 * common implementations for the {@link
 * net.sourceforge.waters.model.analysis.des.SafetyVerifier SafetyVerifier}
 * interface and enables uniform access to set the heuristic ({@link
 * ModularHeuristic}).</P>
 *
 * @author Robi Malik
 */

abstract class AbstractModularSafetyVerifier
  extends AbstractSafetyVerifier
  implements SafetyVerifier
{

  //#########################################################################
  //# Constructors
  public AbstractModularSafetyVerifier(final ProductDESProxy model,
                                       final ProductDESProxyFactory factory,
                                       final SafetyVerifier mono)
  {
    this(model, null, null, factory, mono);
  }

  public AbstractModularSafetyVerifier(final ProductDESProxy model,
                                       final KindTranslator translator,
                                       final SafetyDiagnostics diag,
                                       final ProductDESProxyFactory factory,
                                       final SafetyVerifier mono)
  {
    super(model, translator, diag, factory);
    mMonolithicVerifier = mono;
    mHeuristicMethod = ModularHeuristicFactory.Method.RelMaxCommonEvents;
    mHeuristicPreference = ModularHeuristicFactory.Preference.NOPREF;
  }


  //#########################################################################
  //# Configuration
  public SafetyVerifier getMonolithicVerifier()
  {
    return mMonolithicVerifier;
  }

  public void setMonolithicVerifier(final SafetyVerifier verifier)
  {
    mMonolithicVerifier = verifier;
  }

  public ModularHeuristicFactory.Method getHeuristicMethod()
  {
    return mHeuristicMethod;
  }

  public void setHeuristicMethod(final ModularHeuristicFactory.Method method)
  {
    mHeuristicMethod = method;
  }

  public ModularHeuristicFactory.Preference getHeuristicPreference()
  {
    return mHeuristicPreference;
  }

  public void setHeuristicPreference
    (final ModularHeuristicFactory.Preference preference)
  {
    mHeuristicPreference = preference;
  }

  public ModularHeuristic getHeuristic()
  {
    final ModularHeuristicFactory factory =
      ModularHeuristicFactory.getInstance();
    final KindTranslator translator = getKindTranslator();
    return
      factory.getHeuristic(mHeuristicMethod, mHeuristicPreference, translator);
  }


  //#########################################################################
  //# Interface net.sourceforge.waters.model.analysis.ModelAnalyser
  @Override
  public boolean supportsNondeterminism()
  {
    return mMonolithicVerifier.supportsNondeterminism();
  }

  //#########################################################################
  //# Interface net.sourceforge.waters.model.analysis.ModelAnalyzer
  @Override
  public List<Option<?>> getOptions(final LeafOptionPage db)
  {
    final List<Option<?>> options = super.getOptions(db);
    db.prepend(options, ModularModelVerifierFactory.
                        OPTION_AbstractModularSafetyVerifier_HeuristicPreference);
    db.prepend(options, ModularModelVerifierFactory.
                        OPTION_AbstractModularSafetyVerifier_HeuristicMethod);
    return options;
  }

  @SuppressWarnings("unchecked")
  @Override
  public void setOption(final Option<?> option)
  {
    if (option.hasID(ModularModelVerifierFactory.
                     OPTION_AbstractModularSafetyVerifier_HeuristicPreference)) {
      final EnumOption<ModularHeuristicFactory.Preference> enumOption =
        (EnumOption<ModularHeuristicFactory.Preference>) option;
      setHeuristicPreference(enumOption.getValue());
    } else if (option.hasID(ModularModelVerifierFactory.
                            OPTION_AbstractModularSafetyVerifier_HeuristicMethod)) {
      final EnumOption<ModularHeuristicFactory.Method> enumOption =
        (EnumOption<ModularHeuristicFactory.Method>) option;
      setHeuristicMethod(enumOption.getValue());
    } else if (option.hasID(ModularModelVerifierFactory.
                            OPTION_ModularControllabilityChecker_Chain) ||
               option.hasID(ModularModelVerifierFactory.
                            OPTION_ModularLanguageInclusionChecker_Chain)) {
      try {
        final ChainedAnalyzerOption chain = (ChainedAnalyzerOption) option;
        final ProductDESProxyFactory factory = getFactory();
        final SafetyVerifier secondaryAnalyzer =
          (SafetyVerifier) chain.createAndConfigureModelAnalyzer(factory);
        setMonolithicVerifier(secondaryAnalyzer);
      } catch (final AnalysisConfigurationException exception) {
        throw new WatersRuntimeException(exception);
      }
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
  //# Data Members
  private SafetyVerifier mMonolithicVerifier;
  private ModularHeuristicFactory.Method mHeuristicMethod;
  private ModularHeuristicFactory.Preference mHeuristicPreference;

}
