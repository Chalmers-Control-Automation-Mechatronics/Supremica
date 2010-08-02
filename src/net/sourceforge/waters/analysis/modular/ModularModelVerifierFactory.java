//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.cpp.analysis
//# CLASS:   ModularModelVerifierFactory
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.analysis.modular;

import java.util.List;

import net.sourceforge.waters.analysis.monolithic.MonolithicSCCControlLoopChecker;
import net.sourceforge.waters.analysis.op.ObserverProjectionConflictChecker;
import net.sourceforge.waters.cpp.analysis.NativeControllabilityChecker;
import net.sourceforge.waters.model.analysis.AbstractModelVerifierFactory;
import net.sourceforge.waters.model.analysis.CommandLineArgumentEnum;
import net.sourceforge.waters.model.analysis.CommandLineArgumentInteger;
import net.sourceforge.waters.model.analysis.ModelVerifier;
import net.sourceforge.waters.model.des.ProductDESProxyFactory;


/**
 * A factory that produces modular/incremental model verifiers.
 *
 * @author Robi Malik
 */

public class ModularModelVerifierFactory
  extends AbstractModelVerifierFactory
{

  //#########################################################################
  //# Constructors
  public ModularModelVerifierFactory()
  {
  }

  public ModularModelVerifierFactory(final List<String> arglist)
  {
    super(arglist);
    addArgument(ModularHeuristicFactory.getMethodArgument());
    addArgument(ModularHeuristicFactory.getPreferenceArgument());
    addArgument(new MergeVersion());
    addArgument(new SelectVersion());
    addArgument(new DetectorVersion());
  }


  //#########################################################################
  //# Interface net.sourceforge.waters.model.analysis.ModelVerifierFactory
  public ModularControllabilityChecker createControllabilityChecker
    (final ProductDESProxyFactory factory)
  {
    return new ModularControllabilityChecker
      (null,
       factory,
       new NativeControllabilityChecker(factory),
       false);
  }

  public ModularControlLoopChecker createControlLoopChecker
    (final ProductDESProxyFactory factory)
  {
    return new ModularControlLoopChecker(factory);
  }

  public ObserverProjectionConflictChecker createConflictChecker
    (final ProductDESProxyFactory factory)
  {
    return new ObserverProjectionConflictChecker(null, factory);
  }

  public ModularLanguageInclusionChecker createLanguageInclusionChecker
    (final ProductDESProxyFactory factory)
  {
    return new ModularLanguageInclusionChecker
      (null,
       factory,
       createControllabilityChecker(factory));
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

  public static ModularModelVerifierFactory
    getInstance(final List<String> cmdline)
  {
    return new ModularModelVerifierFactory(cmdline);
  }

  //#########################################################################
  //# Inner Class MergeVersion

  private static class MergeVersion
  extends CommandLineArgumentEnum<AutomataGroup.MergeVersion>
  {

    //#######################################################################
    //# Constructors
    private MergeVersion()
    {
      super("-merge", "Method used to select the secondary automaton for merging",
            AutomataGroup.MergeVersion.class);
    }

    //#######################################################################
    //# Overrides for Abstract Base Class
    //# net.sourceforge.waters.model.analysis.CommandLineArgument
    protected void configure(final ModelVerifier verifier)
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
  //# Inner Class TimeOut
  @SuppressWarnings("unused")
  private static class TimeOut
    extends CommandLineArgumentInteger
  {

    private TimeOut()
    {
      super("-timeOut", "Number of ms before the program is aborted. This is only useful for wcheck");
    }

    protected void configure(final ModelVerifier verifier)
    {
      final int time = getValue();
      if (verifier instanceof ModularControlLoopChecker) {
        final ModularControlLoopChecker checker =
          (ModularControlLoopChecker) verifier;
        checker.setTimeOut((long)time);
      } else {
        fail(getName() + " option only supported for modular control loop checker!");
      }
    }
  }


  //#########################################################################
  //# Inner Class SelectVersion

  private static class SelectVersion
  extends CommandLineArgumentEnum<AutomataGroup.SelectVersion>
  {

    //#######################################################################
    //# Constructors
    private SelectVersion()
    {
      super("-select", "Method used to select the primary automaton for merging",
            AutomataGroup.SelectVersion.class);
    }

    //#######################################################################
    //# Overrides for Abstract Base Class
    //# net.sourceforge.waters.model.analysis.CommandLineArgument
    protected void configure(final ModelVerifier verifier)
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

  private static class DetectorVersion
  extends CommandLineArgumentEnum<MonolithicSCCControlLoopChecker.CLDetector>
  {

    //#######################################################################
    //# Constructors
    private DetectorVersion()
    {
      super("-detect", "Method used to select the control loop of a synchronous product",
            MonolithicSCCControlLoopChecker.CLDetector.class);
    }

    //#######################################################################
    //# Overrides for Abstract Base Class
    //# net.sourceforge.waters.model.analysis.CommandLineArgument
    protected void configure(final ModelVerifier verifier)
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
