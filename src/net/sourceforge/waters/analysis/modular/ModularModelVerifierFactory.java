//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters Analysis
//# PACKAGE: net.sourceforge.waters.analysis.modular
//# CLASS:   ModularModelVerifierFactory
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.analysis.modular;

import net.sourceforge.waters.analysis.monolithic.MonolithicSCCControlLoopChecker;
import net.sourceforge.waters.cpp.analysis.NativeControllabilityChecker;
import net.sourceforge.waters.cpp.analysis.NativeLanguageInclusionChecker;
import net.sourceforge.waters.model.analysis.AnalysisConfigurationException;
import net.sourceforge.waters.model.analysis.CommandLineArgumentChain;
import net.sourceforge.waters.model.analysis.CommandLineArgumentEnum;
import net.sourceforge.waters.model.analysis.des.AbstractModelAnalyzerFactory;
import net.sourceforge.waters.model.analysis.des.ModelVerifier;
import net.sourceforge.waters.model.analysis.des.SafetyVerifier;
import net.sourceforge.waters.model.analysis.des.SupervisorSynthesizer;
import net.sourceforge.waters.model.des.ProductDESProxyFactory;


/**
 * A factory that produces modular/incremental model verifiers.
 *
 * @author Robi Malik, Andrew Holland
 */

public class ModularModelVerifierFactory
  extends AbstractModelAnalyzerFactory
{

  //#########################################################################
  //# Constructors
  public ModularModelVerifierFactory()
  {
  }


  //#########################################################################
  //# Overrides for
  //# net.sourceforge.waters.model.analysis.AbstractModelVerifierFactory
  @Override
  protected void addArguments()
  {
    super.addArguments();
    addArgument(new SecondaryFactoryArgument());
    addArgument(ModularHeuristicFactory.getMethodArgument());
    addArgument(ModularHeuristicFactory.getPreferenceArgument());
    addArgument(new MergeVersionAgument());
    addArgument(new SelectVersionArgument());
    addArgument(new DetectorVersionArgument());
  }


  //#########################################################################
  //# Interface net.sourceforge.waters.model.analysis.ModelVerifierFactory
  @Override
  public ModularControllabilityChecker createControllabilityChecker
    (final ProductDESProxyFactory factory)
  {
    return new ModularControllabilityChecker
      (null, factory, new NativeControllabilityChecker(factory));
  }

  @Override
  public ModularControlLoopChecker createControlLoopChecker
    (final ProductDESProxyFactory factory)
  {
    return new ModularControlLoopChecker(factory);
  }

  @Override
  public ModularLanguageInclusionChecker createLanguageInclusionChecker
    (final ProductDESProxyFactory factory)
  {
    final SafetyVerifier mono =
      new NativeLanguageInclusionChecker(factory);
    return new ModularLanguageInclusionChecker(null, factory, mono);
  }

  @Override
  public SupervisorSynthesizer createSupervisorSynthesizer
    (final ProductDESProxyFactory factory)
  {
    return new ModularControllabilitySynthesizer(factory);
  }


  //#########################################################################
  //# Factory Instantiation
  public static ModularModelVerifierFactory getInstance()
  {
    if (theInstance == null) {
      theInstance = new ModularModelVerifierFactory();
    }
    return theInstance;
  }


  //#########################################################################
  //# Inner Class SecondaryFactoryArgyment
  private static class SecondaryFactoryArgument
    extends CommandLineArgumentChain
  {
    //#######################################################################
    //# Constructors
    private SecondaryFactoryArgument()
    {
    }

    //#######################################################################
    //# Overrides for Abstract Base Class
    //# net.sourceforge.waters.model.analysis.CommandLineArgument
    @Override
    public void configureAnalyzer(final Object analyzer)
      throws AnalysisConfigurationException
    {
      if (analyzer instanceof AbstractModularSafetyVerifier) {
        final ModelVerifier verifier = (ModelVerifier) analyzer;
        final AbstractModularSafetyVerifier modular =
          (AbstractModularSafetyVerifier) verifier;
        final SafetyVerifier secondaryVerifier =
          (SafetyVerifier) createSecondaryAnalyzer(verifier);
        modular.setMonolithicVerifier(secondaryVerifier);
      } else {
        failUnsupportedAnalyzerClass(analyzer);
      }
    }
  }


  //#########################################################################
  //# Inner Class MergeVersionAgument
  private static class MergeVersionAgument
    extends CommandLineArgumentEnum<AutomataGroup.MergeVersion>
  {
    //#######################################################################
    //# Constructors
    private MergeVersionAgument()
    {
      super("-merge", "Method used to select the secondary automaton for merging",
            AutomataGroup.MergeVersion.class);
    }

    //#######################################################################
    //# Overrides for Abstract Base Class
    //# net.sourceforge.waters.model.analysis.CommandLineArgument
    @Override
    public void configureAnalyzer(final Object verifier)
    {
      final AutomataGroup.MergeVersion method = getValue();
      if (verifier instanceof ModularControlLoopChecker) {
        final ModularControlLoopChecker checker =
          (ModularControlLoopChecker) verifier;
        checker.setMergeVersion(method);
      } else {
        fail(getName() + " option only supported for modular control loop checker!");
      }
    }
  }


  //#########################################################################
  //# Inner Class SelectVersionArgument
  private static class SelectVersionArgument
  extends CommandLineArgumentEnum<AutomataGroup.SelectVersion>
  {
    //#######################################################################
    //# Constructors
    private SelectVersionArgument()
    {
      super("-select", "Method used to select the primary automaton for merging",
            AutomataGroup.SelectVersion.class);
    }

    //#######################################################################
    //# Overrides for Abstract Base Class
    //# net.sourceforge.waters.model.analysis.CommandLineArgument
    @Override
    public void configureAnalyzer(final Object verifier)
    {
      final AutomataGroup.SelectVersion method = getValue();
      if (verifier instanceof ModularControlLoopChecker) {
        final ModularControlLoopChecker checker =
          (ModularControlLoopChecker) verifier;
        checker.setSelectVersion(method);
      } else {
        fail(getName() + " option only supported for modular control loop checker!");
      }
    }
  }


  //#########################################################################
  //# Inner Class DetectorVersion
  private static class DetectorVersionArgument
    extends CommandLineArgumentEnum<MonolithicSCCControlLoopChecker.CLDetector>
  {
    //#######################################################################
    //# Constructors
    private DetectorVersionArgument()
    {
      super("-detect", "Method used to select the control loop of a synchronous product",
            MonolithicSCCControlLoopChecker.CLDetector.class);
    }

    //#######################################################################
    //# Overrides for Abstract Base Class
    //# net.sourceforge.waters.model.analysis.CommandLineArgument
    @Override
    public void configureAnalyzer(final Object verifier)
    {
      final MonolithicSCCControlLoopChecker.CLDetector method = getValue();
      if (verifier instanceof ModularControlLoopChecker) {
        final ModularControlLoopChecker checker =
          (ModularControlLoopChecker) verifier;
        checker.setControlLoopDetection(method);
      } else {
        fail(getName() + " option only supported for modular control loop checker!");
      }
    }
  }


  //#########################################################################
  //# Class Variables
  private static ModularModelVerifierFactory theInstance = null;

}
