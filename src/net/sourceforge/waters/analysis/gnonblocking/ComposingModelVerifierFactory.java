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

public class ComposingModelVerifierFactory extends AbstractModelVerifierFactory
{

  // #########################################################################
  // # Constructors
  private ComposingModelVerifierFactory()
  {
  }

  private ComposingModelVerifierFactory(final List<String> arglist)
  {
    super(arglist);
    addArgument(new FinalStateLimitArgument());
    addArgument(new InternalStateLimitArgument());
    addArgument(new FinalTransitionLimitArgument());
    addArgument(new InternalTransitionLimitArgument());
    addArgument(new SelectingHeuristicArgument());
    addArgument(new PreSelectingHeuristicArgument());
  }

  // #########################################################################
  // # Interface net.sourceforge.waters.model.analysis.ModelVerifierFactory
  public ComposingControllabilityChecker createControllabilityChecker(
                                                                      final ProductDESProxyFactory factory)
  {
    return new ComposingControllabilityChecker(factory);
  }

  public CompositionalGeneralisedConflictChecker createConflictChecker(
                                                                       final ProductDESProxyFactory factory)
  {
    return new CompositionalGeneralisedConflictChecker(null, factory);
  }

  public ComposingLanguageInclusionChecker createLanguageInclusionChecker(
                                                                          final ProductDESProxyFactory factory)
  {
    return new ComposingLanguageInclusionChecker(factory);
  }

  // #########################################################################
  // #########################################################################
  // # Class Variables
  private static ComposingModelVerifierFactory theInstance = null;

  // # Factory Instantiation
  public static ComposingModelVerifierFactory getInstance()
  {
    if (theInstance == null) {
      theInstance = new ComposingModelVerifierFactory();
    }
    return theInstance;
  }

  public static ComposingModelVerifierFactory getInstance(
                                                          final List<String> cmdline)
  {
    return new ComposingModelVerifierFactory(cmdline);
  }


  // #########################################################################
  // # Inner Class FinalStateLimitArgument
  private static class FinalStateLimitArgument extends
      CommandLineArgumentInteger
  {

    // #######################################################################
    // # Constructors
    private FinalStateLimitArgument()
    {
      super("-fslimit",
          "Maximum number of states constructed in final composition");
    }

    // #######################################################################
    // # Overrides for Abstract Base Class
    // # net.sourceforge.waters.model.analysis.CommandLineArgument
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


  // #########################################################################
  // # Inner Class InternalStateLimitArgument
  private static class InternalStateLimitArgument extends
      CommandLineArgumentInteger
  {

    // #######################################################################
    // # Constructors
    private InternalStateLimitArgument()
    {
      super("-islimit",
          "Maximum number of states constructed in abstraction attempts");
    }

    // #######################################################################
    // # Overrides for Abstract Base Class
    // # net.sourceforge.waters.model.analysis.CommandLineArgument
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


  // #########################################################################
  // # Inner Class FinalTransitionLimitArgument
  private static class FinalTransitionLimitArgument extends
      CommandLineArgumentInteger
  {

    // #######################################################################
    // # Constructors
    private FinalTransitionLimitArgument()
    {
      super("-ftlimit",
          "Maximum number of states constructed in final composition");
    }

    // #######################################################################
    // # Overrides for Abstract Base Class
    // # net.sourceforge.waters.model.analysis.CommandLineArgument
    protected void configure(final ModelVerifier verifier)
    {
      final int limit = getValue();
      if (verifier instanceof ComposingSafetyVerifier) {
        final ComposingSafetyVerifier composer =
            (ComposingSafetyVerifier) verifier;
        composer.setTransitionLimit(limit);
      } else if (verifier instanceof CompositionalGeneralisedConflictChecker) {
        final CompositionalGeneralisedConflictChecker composer =
            (CompositionalGeneralisedConflictChecker) verifier;
        composer.setFinalStepTransitionLimit(limit);
      }
    }

  }


  // #########################################################################
  // # Inner Class InternalTransitionLimitArgument
  private static class InternalTransitionLimitArgument extends
      CommandLineArgumentInteger
  {

    // #######################################################################
    // # Constructors
    private InternalTransitionLimitArgument()
    {
      super("-itlimit",
          "Maximum number of states constructed in abstraction attempts");
    }

    // #######################################################################
    // # Overrides for Abstract Base Class
    // # net.sourceforge.waters.model.analysis.CommandLineArgument
    protected void configure(final ModelVerifier verifier)
    {
      final int limit = getValue();
      if (verifier instanceof ComposingSafetyVerifier) {
        final ComposingSafetyVerifier composer =
            (ComposingSafetyVerifier) verifier;
        composer.setTransitionLimit(limit);
      } else if (verifier instanceof CompositionalGeneralisedConflictChecker) {
        final CompositionalGeneralisedConflictChecker composer =
            (CompositionalGeneralisedConflictChecker) verifier;
        composer.setInternalStepTransitionLimit(limit);
      }
    }

  }
  // #########################################################################
  // # Inner Class HeuristicArgument
  private static class PreSelectingHeuristicArgument extends CommandLineArgumentString
  {
    // ##addArgument(new SelectingHeuristicArgument());#####################################################################
    // # Constructors
    private PreSelectingHeuristicArgument()
    {
      super("-pheur", "PreSelecting Heuristic adopted in composing candidate selection");
    }

    // #######################################################################
    // # Overrides for Abstract Base Class
    // # net.sourceforge.waters.model.analysis.CommandLineArgument
    protected void configure(final ModelVerifier verifier)
    {
      final String name = getValue();
      if (verifier instanceof ComposingSafetyVerifier) {
        final ComposingSafetyVerifier composing =
          (ComposingSafetyVerifier) verifier;
        composing.setHeuristic(name);
      } else if (verifier instanceof CompositionalGeneralisedConflictChecker) {
        final CompositionalGeneralisedConflictChecker composer =
          (CompositionalGeneralisedConflictChecker) verifier;
        CompositionalGeneralisedConflictChecker.PreselectingHeuristic heuristic = null;
        if (name.equalsIgnoreCase("minT")) {
          heuristic = composer.createHeuristicMinT();
        } else {
                     if (name.equalsIgnoreCase("maxS") ){
                       heuristic = composer.createHeuristicMaxS();
                     }
                     else{
                           if (name.equalsIgnoreCase("mustL")){
                                  heuristic = composer.createHeuristicMustL();
                                 }
                           else{
                                 fail("Unknown PreSelecting heuristic '" + name + "'! Choose from the following : 1)minT 2)maxS 3)mustL.");
                               }
                         }
               }

           composer.setPreselectingHeuristic(heuristic);
        }
      }
  }

  // #########################################################################
  // # Inner Class HeuristicArgument
  private static class SelectingHeuristicArgument extends CommandLineArgumentString
  {
    // #######################################################################
    // # Constructors
    private SelectingHeuristicArgument()
    {
      super("-heur", "Heuristic adopted in composing candidate selection");
    }

    // #######################################################################
    // # Overrides for Abstract Base Class
    // # net.sourceforge.waters.model.analysis.CommandLineArgument
    protected void configure(final ModelVerifier verifier)
    {
      final String name = getValue();
      if (verifier instanceof ComposingSafetyVerifier) {
        final ComposingSafetyVerifier composing =
          (ComposingSafetyVerifier) verifier;
        composing.setHeuristic(name);
      } else if (verifier instanceof CompositionalGeneralisedConflictChecker) {
        final CompositionalGeneralisedConflictChecker composer =
          (CompositionalGeneralisedConflictChecker) verifier;
        CompositionalGeneralisedConflictChecker.SelectingHeuristic heuristic = null;
        if (name.equalsIgnoreCase("maxl")) {
          heuristic = composer.createHeuristicMaxL();
        } else {
                     if (name.equalsIgnoreCase("maxc") ){
                       heuristic = composer.createHeuristicMaxC();
                     }
                     else{
                           if (name.equalsIgnoreCase("minS")){
                                  heuristic = composer.createHeuristicMinS();
                                 }
                           else{
                                 fail("Unknown heuristic '" + name + "'! Choose from the following : 1)maxl 2)maxc 3)mins.");
                               }
                         }
               }

           composer.setSelectingHeuristic(heuristic);
        }
      }





  // #########################################################################
  // # Class Variables
  //private static ComposingModelVerifierFactory theInstance = null;

  }

  }
