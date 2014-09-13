//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters Analysis Algorithms
//# PACKAGE: net.sourceforge.waters.analysis.modular
//# CLASS:   ModularLanguageInclusionChecker
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.analysis.modular;

import gnu.trove.set.hash.THashSet;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import net.sourceforge.waters.model.analysis.AnalysisException;
import net.sourceforge.waters.model.analysis.KindTranslator;
import net.sourceforge.waters.model.analysis.LanguageInclusionKindTranslator;
import net.sourceforge.waters.model.analysis.VerificationResult;
import net.sourceforge.waters.model.analysis.des.LanguageInclusionChecker;
import net.sourceforge.waters.model.analysis.des.LanguageInclusionDiagnostics;
import net.sourceforge.waters.model.analysis.des.SafetyVerifier;
import net.sourceforge.waters.model.des.AutomatonProxy;
import net.sourceforge.waters.model.des.EventProxy;
import net.sourceforge.waters.model.des.ProductDESProxy;
import net.sourceforge.waters.model.des.ProductDESProxyFactory;
import net.sourceforge.waters.model.des.SafetyTraceProxy;
import net.sourceforge.waters.model.des.TraceProxy;
import net.sourceforge.waters.model.des.TraceStepProxy;
import net.sourceforge.waters.xsd.base.ComponentKind;
import net.sourceforge.waters.xsd.base.EventKind;


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
 * or {@link ProjectingControllabilityChecker}. The modular language
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
    setNodeLimit(10000000);
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
        switch (translator.getComponentKind(aut)) {
        case PLANT:
          automata.add(aut);
          break;
        case SPEC:
          properties.add(aut);
          break;
        default:
          break;
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
  private boolean setFailedResult(final TraceProxy counterexample,
                                  final AutomatonProxy property)
  {
    final ProductDESProxyFactory factory = getFactory();
    final ProductDESProxy des = getModel();
    final String desname = des.getName();
    final String propname = property.getName();
    final String cleanedname = propname.replaceAll(":", "-");
    final String tracename = desname + '-' + cleanedname;
    final Collection<AutomatonProxy> automata = counterexample.getAutomata();
    final List<TraceStepProxy> steps = counterexample.getTraceSteps();
    final SafetyTraceProxy wrapper =
      factory.createSafetyTraceProxy(tracename, null, null,
                                     des, automata, steps);
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

