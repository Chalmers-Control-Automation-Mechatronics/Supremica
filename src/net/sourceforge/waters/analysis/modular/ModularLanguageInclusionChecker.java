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

package net.sourceforge.waters.analysis.modular;

import gnu.trove.set.hash.THashSet;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import net.sourceforge.waters.model.options.LeafOptionPage;
import net.sourceforge.waters.model.options.Option;
import net.sourceforge.waters.analysis.trcomp.TRControllabilityChecker;
import net.sourceforge.waters.model.analysis.AnalysisException;
import net.sourceforge.waters.model.analysis.VerificationResult;
import net.sourceforge.waters.model.analysis.des.LanguageInclusionChecker;
import net.sourceforge.waters.model.analysis.des.LanguageInclusionDiagnostics;
import net.sourceforge.waters.model.analysis.des.SafetyVerifier;
import net.sourceforge.waters.model.analysis.kindtranslator.KindTranslator;
import net.sourceforge.waters.model.analysis.kindtranslator.LanguageInclusionKindTranslator;
import net.sourceforge.waters.model.base.ComponentKind;
import net.sourceforge.waters.model.base.EventKind;
import net.sourceforge.waters.model.des.AutomatonProxy;
import net.sourceforge.waters.model.des.EventProxy;
import net.sourceforge.waters.model.des.ProductDESProxy;
import net.sourceforge.waters.model.des.ProductDESProxyFactory;
import net.sourceforge.waters.model.des.SafetyCounterExampleProxy;
import net.sourceforge.waters.model.des.TraceProxy;


/**
 * <P>The modular language inclusion check algorithm.</P>
 *
 * <P>The modular language inclusion checker is a simple wrapper to
 * split a model with several properties into several models with only
 * one property each. Each model is checked individually by another safety
 * verifier. If one check fails, language inclusion is found to be not
 * satisfied, otherwise it is satisfied.</P>
 *
 * <P>The model verifier checking the individual properties can be
 * configured. It typically is a {@link ModularControllabilityChecker}
 * or {@link TRControllabilityChecker}. The modular language
 * inclusion checker is only useful for models with more than one
 * property; if there is only one property, it will delegate the
 * complete task to the secondary model verifier.</P>
 *
 * @author Simon Ware
 */

public class ModularLanguageInclusionChecker
  extends AbstractModularSafetyVerifier
  implements LanguageInclusionChecker
{

  //#########################################################################
  //# Constructor
  public ModularLanguageInclusionChecker(final ProductDESProxyFactory factory,
                                         final SafetyVerifier mono)
  {
    this(null, factory, mono);
  }

  public ModularLanguageInclusionChecker(final ProductDESProxy model,
                                         final ProductDESProxyFactory factory,
                                         final SafetyVerifier mono)
  {
    super(model,
          LanguageInclusionKindTranslator.getInstance(),
          LanguageInclusionDiagnostics.getInstance(),
          factory,
          mono);
    mStates = 0;
  }


  //#########################################################################
  //# Configuration
  SafetyVerifier getInnerControllabilityChecker()
  {
    return mConfiguredControllabilityChecker;
  }

  void setInnerControllabilityChecker(final SafetyVerifier inner)
  {
    mConfiguredControllabilityChecker = inner;
  }


  //#########################################################################
  //# Interface net.sourceforge.waters.model.analysis.ModelAnalyzer
  @Override
  public List<Option<?>> getOptions(final LeafOptionPage db)
  {
    final List<Option<?>> options = super.getOptions(db);
    db.append(options, ModularModelVerifierFactory.
                       OPTION_ModularLanguageInclusionChecker_Chain);
    return options;
  }


  //#########################################################################
  //# Invocation
  @Override
  protected void setUp() throws AnalysisException
  {
    super.setUp();
    if (mConfiguredControllabilityChecker == null) {
      final ProductDESProxyFactory factory = getFactory();
      final SafetyVerifier mono = getMonolithicVerifier();
      mUsedControllabilityChecker =
        new ModularControllabilityChecker(null, factory, mono);
    } else {
      mUsedControllabilityChecker = mConfiguredControllabilityChecker;
    }
    mStates = 0;
  }

  @Override
  public boolean run()
    throws AnalysisException
    {
    setUp();
    try {
      final List<AutomatonProxy> properties = new ArrayList<>();
      final List<AutomatonProxy> automata =
        new ArrayList<>(getModel().getAutomata().size());
      final KindTranslator translator = getKindTranslator();
      for (final AutomatonProxy aut : getModel().getAutomata()) {
        final ComponentKind kind = translator.getComponentKind(aut);
        if (kind == ComponentKind.PLANT) {
          automata.add(aut);
        } else if (kind == ComponentKind.SPEC) {
          properties.add(aut);
        }
      }
      Collections.sort(properties);
      final int propIndex = automata.size();
      final ProductDESProxyFactory factory = getFactory();
      final Collection<EventProxy> events = getModel().getEvents();
      final String modelName = getModel().getName();
      for (final AutomatonProxy prop : properties) {
        automata.add(prop);
        final String name = modelName + "-" + prop.getName();
        final String comment = "Automatically generated to check property '" +
          prop.getName() + "' of model '" + modelName + "'.";
        final ProductDESProxy model =
          factory.createProductDESProxy(name, comment, null, events, automata);
        mUsedControllabilityChecker.setModel(model);
        final KindTranslator chain =
          new ChainKindTranslator(translator, properties);
        mUsedControllabilityChecker.setKindTranslator(chain);
        mUsedControllabilityChecker.setNodeLimit(getNodeLimit() - mStates);
        final boolean satisfied = mUsedControllabilityChecker.run();
        final VerificationResult result =
          mUsedControllabilityChecker.getAnalysisResult();
        mStates += result.getTotalNumberOfStates();
        if (!satisfied) {
          return setFailedResult
            (mUsedControllabilityChecker.getCounterExample(), prop);
        }
        automata.remove(propIndex);
      }
      return setSatisfiedResult();
    } finally {
      tearDown();
    }
  }

  @Override
  protected void tearDown()
  {
    mUsedControllabilityChecker = null;
    super.tearDown();
  }

  @Override
  protected void addStatistics()
  {
    super.addStatistics();
    final VerificationResult result = getAnalysisResult();
    result.setNumberOfStates(mStates);
  }


  //#########################################################################
  //# Auxiliary Methods
  private boolean setFailedResult(final SafetyCounterExampleProxy counter,
                                  final AutomatonProxy property)
  {
    final ProductDESProxyFactory factory = getFactory();
    final ProductDESProxy des = getModel();
    final String desname = des.getName();
    final String propname = property.getName();
    final String cleanedname = propname.replaceAll(":", "-");
    final String tracename = desname + '-' + cleanedname;
    final Collection<AutomatonProxy> automata = counter.getAutomata();
    final TraceProxy trace = counter.getTrace();
    final SafetyCounterExampleProxy wrapper =
      factory.createSafetyCounterExampleProxy(tracename, null, null,
                                              des, automata, trace);
    return setFailedResult(wrapper);
  }


  //#########################################################################
  //# Inner Class ChainKindTranslator
  private static class ChainKindTranslator implements KindTranslator
  {

    //#######################################################################
    //# Constructor
    private ChainKindTranslator(final KindTranslator master,
                                final Collection<AutomatonProxy> properties)
    {
      mMaster = master;
      mProperties = new THashSet<AutomatonProxy>(properties);
    }

    //#######################################################################
    //# Inner Class ChainKindTranslator
    @Override
    public ComponentKind getComponentKind(final AutomatonProxy aut)
    {
      if (mProperties.contains(aut)) {
        return ComponentKind.SPEC;
      } else {
        return ComponentKind.PLANT;
      }
    }

    @Override
    public EventKind getEventKind(final EventProxy event)
    {
      final EventKind kind = mMaster.getEventKind(event);
      if (kind == EventKind.CONTROLLABLE) {
        return EventKind.UNCONTROLLABLE;
      } else {
        return kind;
      }
    }

    //#######################################################################
    //# Data Members
    private final KindTranslator mMaster;
    private final Collection<AutomatonProxy> mProperties;

  }


  //#########################################################################
  //# Data Members
  private SafetyVerifier mConfiguredControllabilityChecker;
  private SafetyVerifier mUsedControllabilityChecker;
  private int mStates;

}
