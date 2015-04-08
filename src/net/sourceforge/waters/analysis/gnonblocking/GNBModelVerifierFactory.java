//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.analysis.gnonblocking
//# CLASS:   GNBModelVerifierFactory
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.analysis.gnonblocking;

import net.sourceforge.waters.model.analysis.CommandLineArgumentInteger;
import net.sourceforge.waters.model.analysis.CommandLineArgumentString;
import net.sourceforge.waters.model.analysis.des.AbstractModelAnalyzerFactory;
import net.sourceforge.waters.model.des.ProductDESProxyFactory;


/**
 * A factory that produces compositional model verifiers.
 * This provides access to experimental compositional model verifiers
 * written by Jinjian Shi and Rachel Francis.
 *
 * @see CompositionalGeneralisedConflictChecker
 *
 * @author Jinjian Shi, Rachel Francis, Robi Malik
 */

public class GNBModelVerifierFactory extends AbstractModelAnalyzerFactory
{

  //#########################################################################
  //# Singleton Pattern
  public static GNBModelVerifierFactory getInstance()
  {
    return SingletonHolder.INSTANCE;
  }

  private static class SingletonHolder {
    private static final GNBModelVerifierFactory INSTANCE =
      new GNBModelVerifierFactory();
  }


  //#########################################################################
  //# Constructors
  private GNBModelVerifierFactory()
  {
  }


  //#########################################################################
  //# Overrides for
  //# net.sourceforge.waters.model.analysis.AbstractModelVerifierFactory
  @Override
  protected void addArguments()
  {
    super.addArguments();
    addArgument(new FinalStateLimitArgument());
    addArgument(new InternalStateLimitArgument());
    addArgument(new FinalTransitionLimitArgument());
    addArgument(new InternalTransitionLimitArgument());
    addArgument(new SelectingHeuristicArgument());
    addArgument(new PreSelectingHeuristicArgument());
  }


  //#########################################################################
  //# Interface net.sourceforge.waters.model.analysis.ModelVerifierFactory
  @Override
  public CompositionalGeneralisedConflictChecker createConflictChecker
    (final ProductDESProxyFactory factory)
  {
    return new CompositionalGeneralisedConflictChecker(null, factory);
  }

  /*public AlphaNonBlockingChecker createConflictChecker
    (final ProductDESProxyFactory factory)
  {
    return new AlphaNonBlockingChecker(null, factory);
  }*/

  /*public CanonicalGeneralisedConflictChecker createConflictChecker
    (final ProductDESProxyFactory factory)
  {
    return new CanonicalGeneralisedConflictChecker(null, factory);
  }*/


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
    @Override
    public void configureAnalyzer(final Object analyzer)
    {
      final int limit = getValue();
      if (analyzer instanceof CompositionalGeneralisedConflictChecker) {
        final CompositionalGeneralisedConflictChecker composer =
            (CompositionalGeneralisedConflictChecker) analyzer;
        composer.setFinalStepNodeLimit(limit);
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
    @Override
    public void configureAnalyzer(final Object analyzer)
    {
      final int limit = getValue();
      if (analyzer instanceof CompositionalGeneralisedConflictChecker) {
        final CompositionalGeneralisedConflictChecker composer =
            (CompositionalGeneralisedConflictChecker) analyzer;
        composer.setInternalStepNodeLimit(limit);
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
          "Maximum number of states constructed in final composition");
    }

    //#######################################################################
    //# Overrides for Abstract Base Class
    //# net.sourceforge.waters.model.analysis.CommandLineArgument
    @Override
    public void configureAnalyzer(final Object analyzer)
    {
      final int limit = getValue();
      if (analyzer instanceof CompositionalGeneralisedConflictChecker) {
        final CompositionalGeneralisedConflictChecker composer =
            (CompositionalGeneralisedConflictChecker) analyzer;
        composer.setFinalStepTransitionLimit(limit);
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
          "Maximum number of states constructed in abstraction attempts");
    }

    //#######################################################################
    //# Overrides for Abstract Base Class
    //# net.sourceforge.waters.model.analysis.CommandLineArgument
    @Override
    public void configureAnalyzer(final Object analyzer)
    {
      final int limit = getValue();
      if (analyzer instanceof CompositionalGeneralisedConflictChecker) {
        final CompositionalGeneralisedConflictChecker composer =
            (CompositionalGeneralisedConflictChecker) analyzer;
        composer.setInternalStepTransitionLimit(limit);
      }
    }

  }


  //#########################################################################
  //# Inner Class HeuristicArgument
  private static class PreSelectingHeuristicArgument
    extends CommandLineArgumentString
  {
    //#######################################################################
    //# Constructors
    private PreSelectingHeuristicArgument()
    {
      super("-pheur", "PreSelecting Heuristic adopted in composing candidate selection");
    }

    //#######################################################################
    //# Overrides for Abstract Base Class
    //# net.sourceforge.waters.model.analysis.CommandLineArgument
    @Override
    public void configureAnalyzer(final Object analyzer)
    {
      final String name = getValue();
      if (analyzer instanceof CompositionalGeneralisedConflictChecker) {
        final CompositionalGeneralisedConflictChecker composer =
          (CompositionalGeneralisedConflictChecker) analyzer;
        CompositionalGeneralisedConflictChecker.PreselectingHeuristic heuristic = null;
        if (name.equalsIgnoreCase("minT")) {
          heuristic = composer.createHeuristicMinT();
        } else if (name.equalsIgnoreCase("minTa")) {
          heuristic = composer.createHeuristicMinTa();
        } else if (name.equalsIgnoreCase("maxS")) {
          heuristic = composer.createHeuristicMaxS();
        } else if (name.equalsIgnoreCase("mustL")) {
          heuristic = composer.createHeuristicMustL();
        } else {
          fail("Unknown Preselecting heuristic '" + name +
               "'! Choose from the following : minT, minTa, maxS, mustL.");
        }
        composer.setPreselectingHeuristic(heuristic);
      }
    }
  }


  //#########################################################################
  //# Inner Class HeuristicArgument
  private static class SelectingHeuristicArgument
    extends CommandLineArgumentString
  {
    //#######################################################################
    //# Constructors
    private SelectingHeuristicArgument()
    {
      super("-heur", "Heuristic adopted in composing candidate selection");
    }

    //#######################################################################
    //# Overrides for Abstract Base Class
    //# net.sourceforge.waters.model.analysis.CommandLineArgument
    @Override
    public void configureAnalyzer(final Object analyzer)
    {
      final String name = getValue();
      if (analyzer instanceof CompositionalGeneralisedConflictChecker) {
        final CompositionalGeneralisedConflictChecker composer =
          (CompositionalGeneralisedConflictChecker) analyzer;
        CompositionalGeneralisedConflictChecker.SelectingHeuristic heuristic = null;
        if (name.equalsIgnoreCase("maxl")) {
          heuristic = composer.createHeuristicMaxL();
        } else if (name.equalsIgnoreCase("maxc")) {
          heuristic = composer.createHeuristicMaxC();
        } else if (name.equalsIgnoreCase("minS")) {
          heuristic = composer.createHeuristicMinS();
        } else {
          fail("Unknown heuristic '" + name +
               "'! Choose from the following : 1)maxl 2)maxc 3)mins.");
        }
        composer.setSelectingHeuristic(heuristic);
      }
    }
  }

}
