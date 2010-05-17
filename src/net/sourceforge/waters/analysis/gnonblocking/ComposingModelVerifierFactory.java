//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.analysis.composing
//# CLASS:   ComposeModelVerifierFactory
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.analysis.gnonblocking;

import java.util.List;

import net.sourceforge.waters.analysis.composing.ComposingControllabilityChecker;
import net.sourceforge.waters.analysis.composing.ComposingLanguageInclusionChecker;
import net.sourceforge.waters.analysis.composing.ComposingSafetyVerifier;
import net.sourceforge.waters.model.analysis.AbstractModelVerifierFactory;
import net.sourceforge.waters.model.analysis.CommandLineArgumentInteger;
import net.sourceforge.waters.model.analysis.CommandLineArgumentString;
import net.sourceforge.waters.model.analysis.ModelVerifier;
import net.sourceforge.waters.model.des.ProductDESProxyFactory;


/**
 * A factory that produces compositional model verifiers.
 *
 * @author Jinjian Shi, Robi Malik
 */

public class ComposingModelVerifierFactory
  extends AbstractModelVerifierFactory
{

  //#########################################################################
  //# Constructors
  private ComposingModelVerifierFactory()
  {
  }

  private ComposingModelVerifierFactory(final List<String> arglist)
  {
    super(arglist);
    addArgument(new FinalLimitArgument());
    addArgument(new InternalLimitArgument());
    addArgument(new HeuristicArgument());
  }


  //#########################################################################
  //# Interface net.sourceforge.waters.model.analysis.ModelVerifierFactory
  public ComposingControllabilityChecker createControllabilityChecker
    (final ProductDESProxyFactory factory)
  {
    return new ComposingControllabilityChecker(factory);
  }

  public CompositionalGeneralisedConflictChecker createConflictChecker
    (final ProductDESProxyFactory factory)
  {
    return new CompositionalGeneralisedConflictChecker(null, factory);
  }

  public ComposingLanguageInclusionChecker createLanguageInclusionChecker
    (final ProductDESProxyFactory factory)
  {
    return new ComposingLanguageInclusionChecker(factory);
  }


  //#########################################################################
  //# Factory Instantiation
  public static ComposingModelVerifierFactory getInstance()
  {
    if (theInstance == null) {
      theInstance = new ComposingModelVerifierFactory();
    }
    return theInstance;
  }

  public static ComposingModelVerifierFactory
    getInstance(final List<String> cmdline)
  {
    return new ComposingModelVerifierFactory(cmdline);
  }


  //#########################################################################
  //# Inner Class InternalLimitArgument
  private static class FinalLimitArgument
    extends CommandLineArgumentInteger
  {

    //#######################################################################
    //# Constructors
    private FinalLimitArgument()
    {
      super("-flimit",
            "Maximum number of states constructed in final composition");
    }

    //#######################################################################
    //# Overrides for Abstract Base Class
    //# net.sourceforge.waters.model.analysis.CommandLineArgument
    protected void configure(final ModelVerifier verifier)
    {
      final int limit = getValue();
      if (verifier instanceof ComposingSafetyVerifier) {
        final ComposingSafetyVerifier composer =
          (ComposingSafetyVerifier) verifier;
        composer.setNodeLimit(limit);
      } else if (verifier instanceof CompositionalGeneralisedConflictChecker) {
        final CompositionalGeneralisedConflictChecker composer =
          (CompositionalGeneralisedConflictChecker) verifier;
        composer.setFinalStepNodeLimit(limit);
      }
    }

  }


  //#########################################################################
  //# Inner Class InternalLimitArgument
  private static class InternalLimitArgument
    extends CommandLineArgumentInteger
  {

    //#######################################################################
    //# Constructors
    private InternalLimitArgument()
    {
      super("-ilimit",
            "Maximum number of states constructed in abstraction attempts");
    }

    //#######################################################################
    //# Overrides for Abstract Base Class
    //# net.sourceforge.waters.model.analysis.CommandLineArgument
    protected void configure(final ModelVerifier verifier)
    {
      final int limit = getValue();
      if (verifier instanceof ComposingSafetyVerifier) {
        final ComposingSafetyVerifier composer =
          (ComposingSafetyVerifier) verifier;
        composer.setProjectionNodeLimit(limit);
      } else if (verifier instanceof CompositionalGeneralisedConflictChecker) {
        final CompositionalGeneralisedConflictChecker composer =
          (CompositionalGeneralisedConflictChecker) verifier;
        composer.setInternalStepNodeLimit(limit);
      }
    }

  }


  //#########################################################################
  //# Inner Class HeuristicArgument
  private static class HeuristicArgument extends CommandLineArgumentString
  {
    //#######################################################################
    //# Constructors
    private HeuristicArgument()
    {
      super("-heur",
            "Heuristic adopted in composing candidate selection");
    }

    //#######################################################################
    //# Overrides for Abstract Base Class
    //# net.sourceforge.waters.model.analysis.CommandLineArgument
    protected void configure(final ModelVerifier verifier)
    {
      final ComposingSafetyVerifier composing =
        (ComposingSafetyVerifier) verifier;
      final String heuristic = getValue();
      composing.setHeuristic(heuristic);
    }

  }


  //#########################################################################
  //# Class Variables
  private static ComposingModelVerifierFactory theInstance = null;

}
