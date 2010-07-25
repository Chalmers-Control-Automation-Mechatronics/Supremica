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

import net.sourceforge.waters.analysis.op.ObserverProjectionConflictChecker;
import net.sourceforge.waters.cpp.analysis.NativeControllabilityChecker;
import net.sourceforge.waters.model.analysis.AbstractModelVerifierFactory;
import net.sourceforge.waters.model.analysis.CommandLineArgumentEnum;
import net.sourceforge.waters.model.analysis.CommandLineArgumentInteger;
import net.sourceforge.waters.model.analysis.LanguageInclusionChecker;
import net.sourceforge.waters.model.analysis.ModelVerifier;
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
    addArgument(new MethodArgument());
    addArgument(ModularHeuristicFactory.getMethodArgument());
    addArgument(ModularHeuristicFactory.getPreferenceArgument());
    addArgument(new FinalStateLimitArgument());
    addArgument(new InternalStateLimitArgument());
    addArgument(new FinalTransitionLimitArgument());
    addArgument(new InternalTransitionLimitArgument());
  }


  //#########################################################################
  //# Interface net.sourceforge.waters.model.analysis.ModelVerifierFactory
  public ProjectingControllabilityChecker createControllabilityChecker
    (final ProductDESProxyFactory factory)
  {
    return new ProjectingControllabilityChecker
      (factory, new NativeControllabilityChecker(factory));
  }

  public ObserverProjectionConflictChecker createConflictChecker
    (final ProductDESProxyFactory factory)
  {
    return new ObserverProjectionConflictChecker(null, factory);
  }

  public LanguageInclusionChecker createLanguageInclusionChecker
    (final ProductDESProxyFactory factory)
  {
    return new ModularLanguageInclusionChecker(
       null, factory,
       /*new OneUncontrollableChecker(null, factory,
                                    createControllabilityChecker(factory)),*/
       createControllabilityChecker(factory));
  }


  //#########################################################################
  //# Inner Class FinalStateLimitArgument
  private static class FinalStateLimitArgument extends
      CommandLineArgumentInteger
  {

    //#######################################################################
    //# Constructors
    private FinalStateLimitArgument()
    {
      super("-fslimit",
            "Maximum number of states constructed in final composition");
    }

    //#######################################################################
    //# Overrides for Abstract Base Class
    //# net.sourceforge.waters.model.analysis.CommandLineArgument
    protected void configure(final ModelVerifier verifier)
    {
      final int limit = getValue();
      if (verifier instanceof ObserverProjectionConflictChecker) {
        final ObserverProjectionConflictChecker composer =
            (ObserverProjectionConflictChecker) verifier;
        composer.setFinalStepNodeLimit(limit);
      } else {
        verifier.setNodeLimit(limit);
      }
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
      if (verifier instanceof ObserverProjectionConflictChecker) {
        final ObserverProjectionConflictChecker composer =
            (ObserverProjectionConflictChecker) verifier;
        composer.setInternalStepNodeLimit(limit);
      } else {
        verifier.setNodeLimit(limit);
      }
    }

  }


  //#########################################################################
  //# Inner Class FinalTransitionLimitArgument
  private static class FinalTransitionLimitArgument extends
      CommandLineArgumentInteger
  {

    //#######################################################################
    //# Constructors
    private FinalTransitionLimitArgument()
    {
      super("-ftlimit",
            "Maximum number of transitions constructed in final composition");
    }

    //#######################################################################
    //# Overrides for Abstract Base Class
    //# net.sourceforge.waters.model.analysis.CommandLineArgument
    protected void configure(final ModelVerifier verifier)
    {
      final int limit = getValue();
      if (verifier instanceof ObserverProjectionConflictChecker) {
        final ObserverProjectionConflictChecker composer =
            (ObserverProjectionConflictChecker) verifier;
        composer.setFinalStepTransitionLimit(limit);
      } else {
        verifier.setTransitionLimit(limit);
      }
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
      if (verifier instanceof ObserverProjectionConflictChecker) {
        final ObserverProjectionConflictChecker composer =
            (ObserverProjectionConflictChecker) verifier;
        composer.setInternalStepTransitionLimit(limit);
      } else {
        verifier.setTransitionLimit(limit);
      }
    }

  }


  //#########################################################################
  //# Inner Class MethodArgument
  private static class MethodArgument
    extends CommandLineArgumentEnum<ObserverProjectionConflictChecker.Method>
  {

    //#######################################################################
    //# Constructors
    private MethodArgument()
    {
      super("-method", "Abstraction method used for conflict check",
            ObserverProjectionConflictChecker.Method.class);
    }

    //#######################################################################
    //# Overrides for Abstract Base Class
    //# net.sourceforge.waters.model.analysis.CommandLineArgument
    protected void configure(final ModelVerifier verifier)
    {
      final ObserverProjectionConflictChecker.Method method = getValue();
      if (verifier instanceof ObserverProjectionConflictChecker) {
        final ObserverProjectionConflictChecker composer =
            (ObserverProjectionConflictChecker) verifier;
        composer.setMethod(method);
      } else {
        fail(getName() + " option only supported for conflict check!");
      }
    }

  }

}
