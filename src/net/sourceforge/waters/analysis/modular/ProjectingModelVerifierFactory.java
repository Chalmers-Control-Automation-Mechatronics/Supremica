//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.analysis.modular
//# CLASS:   ProjectingModelVerifierFactory
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.analysis.modular;

import java.util.List;

import net.sourceforge.waters.cpp.analysis.NativeControllabilityChecker;
import net.sourceforge.waters.cpp.analysis.NativeLanguageInclusionChecker;
import net.sourceforge.waters.model.analysis.AbstractModelVerifierFactory;
import net.sourceforge.waters.model.analysis.CommandLineArgumentInteger;
import net.sourceforge.waters.model.analysis.LanguageInclusionChecker;
import net.sourceforge.waters.model.analysis.ModelVerifier;
import net.sourceforge.waters.model.analysis.SafetyVerifier;
import net.sourceforge.waters.model.des.ProductDESProxyFactory;


/**
 * A factory that produces projecting model verifiers.
 *
 * @author Robi Malik
 */

public class ProjectingModelVerifierFactory
  extends AbstractModelVerifierFactory
{

  //#########################################################################
  //# Singleton Pattern
  public static ProjectingModelVerifierFactory getInstance()
  {
    return SingletonHolder.INSTANCE;
  }

  public static ProjectingModelVerifierFactory
    getInstance(final List<String> cmdline)
  {
    return new ProjectingModelVerifierFactory(cmdline);
  }

  private static class SingletonHolder {
    private static final ProjectingModelVerifierFactory INSTANCE =
      new ProjectingModelVerifierFactory();
  }


  //#########################################################################
  //# Constructors
  private ProjectingModelVerifierFactory()
  {
  }

  private ProjectingModelVerifierFactory(final List<String> arglist)
  {
    super(arglist);
    addArgument(new MonolithicStateLimitArgument());
    addArgument(new InternalStateLimitArgument());
    addArgument(new MonolithicTransitionLimitArgument());
    addArgument(new InternalTransitionLimitArgument());
    addArgument(ModularHeuristicFactory.getMethodArgument());
    addArgument(ModularHeuristicFactory.getPreferenceArgument());
  }


  //#########################################################################
  //# Interface net.sourceforge.waters.model.analysis.ModelVerifierFactory
  public ProjectingControllabilityChecker createControllabilityChecker
    (final ProductDESProxyFactory factory)
  {
    final SafetyProjectionBuilder projector = new Projection2(factory);
    return new ProjectingControllabilityChecker
      (factory, new NativeControllabilityChecker(factory), projector);
  }

  @Override
  public LanguageInclusionChecker createLanguageInclusionChecker
    (final ProductDESProxyFactory factory)
  {
    final SafetyVerifier mono =
      new NativeLanguageInclusionChecker(factory);
    final SafetyProjectionBuilder projector = new Projection2(factory);
    final SafetyVerifier cont =
      new ProjectingControllabilityChecker(factory, mono, projector);
    return new ModularLanguageInclusionChecker(null, factory, cont);
  }


  //#########################################################################
  //# Inner Class FinalStateLimitArgument
  private static class MonolithicStateLimitArgument extends
      CommandLineArgumentInteger
  {

    //#######################################################################
    //# Constructors
    private MonolithicStateLimitArgument()
    {
      super("-mslimit",
            "Maximum number of states constructed in final monolithic composition");
    }

    //#######################################################################
    //# Overrides for Abstract Base Class
    //# net.sourceforge.waters.model.analysis.CommandLineArgument
    protected void configure(final ModelVerifier verifier)
    {
      final int limit = getValue();
      verifier.setNodeLimit(limit);
    }

  }


  //#########################################################################
  //# Inner Class InternalStateLimitArgument
  private static class InternalStateLimitArgument extends
      CommandLineArgumentInteger
  {

    //#######################################################################
    //# Constructors
    private InternalStateLimitArgument()
    {
      super("-islimit",
            "Maximum number of states constructed in abstraction attempts");
    }

    //#######################################################################
    //# Overrides for Abstract Base Class
    //# net.sourceforge.waters.model.analysis.CommandLineArgument
    protected void configure(final ModelVerifier verifier)
    {
      final int limit = getValue();
      verifier.setNodeLimit(limit);
    }

  }


  //#########################################################################
  //# Inner Class FinalTransitionLimitArgument
  private static class MonolithicTransitionLimitArgument extends
      CommandLineArgumentInteger
  {

    //#######################################################################
    //# Constructors
    private MonolithicTransitionLimitArgument()
    {
      super("-mtlimit",
            "Maximum number of transitions constructed in final monolithic composition");
    }

    //#######################################################################
    //# Overrides for Abstract Base Class
    //# net.sourceforge.waters.model.analysis.CommandLineArgument
    protected void configure(final ModelVerifier verifier)
    {
      final int limit = getValue();
      verifier.setTransitionLimit(limit);
    }

  }


  //#########################################################################
  //# Inner Class InternalTransitionLimitArgument
  private static class InternalTransitionLimitArgument extends
      CommandLineArgumentInteger
  {

    //#######################################################################
    //# Constructors
    private InternalTransitionLimitArgument()
    {
      super("-itlimit",
          "Maximum number of transitions constructed in abstraction attempts");
    }

    //#######################################################################
    //# Overrides for Abstract Base Class
    //# net.sourceforge.waters.model.analysis.CommandLineArgument
    protected void configure(final ModelVerifier verifier)
    {
      final int limit = getValue();
      verifier.setTransitionLimit(limit);
    }

  }
}
