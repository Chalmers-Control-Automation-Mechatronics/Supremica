//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.analysis.composing
//# CLASS:   ComposeModelVerifierFactory
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.analysis.abstraction;

import java.io.PrintStream;
import java.util.List;

import net.sourceforge.waters.model.analysis.AbstractModelVerifierFactory;
import net.sourceforge.waters.model.analysis.CommandLineArgumentEnum;
import net.sourceforge.waters.model.analysis.CommandLineArgumentFlag;
import net.sourceforge.waters.model.analysis.CommandLineArgumentInteger;
import net.sourceforge.waters.model.analysis.CommandLineArgumentString;
import net.sourceforge.waters.model.analysis.EnumFactory;
import net.sourceforge.waters.model.analysis.ModelVerifier;
import net.sourceforge.waters.model.des.ProductDESProxyFactory;


/**
 * A factory that produces compositional model verifiers.
 *
 * @author Robi Malik
 */

public class CompositionalModelVerifierFactory extends AbstractModelVerifierFactory
{

  //#########################################################################
  //# Singleton Pattern
  public static CompositionalModelVerifierFactory getInstance()
  {
    return SingletonHolder.INSTANCE;
  }

  public static CompositionalModelVerifierFactory getInstance
    (final List<String> cmdline)
  {
    return new CompositionalModelVerifierFactory(cmdline);
  }

  private static class SingletonHolder {
    private static final CompositionalModelVerifierFactory INSTANCE =
      new CompositionalModelVerifierFactory();
  }


  //#########################################################################
  //# Constructors
  private CompositionalModelVerifierFactory()
  {
  }

  private CompositionalModelVerifierFactory(final List<String> arglist)
  {
    super(arglist);
    addArgument(new MonolithicStateLimitArgument());
    addArgument(new InternalStateLimitArgument());
    addArgument(new LowerInternalStateLimitArgument());
    addArgument(new UpperInternalStateLimitArgument());
    addArgument(new MonolithicTransitionLimitArgument());
    addArgument(new InternalTransitionLimitArgument());
    addArgument(new AbstractionMethodArgument());
    addArgument(new PreselectingMethodArgument());
    addArgument(new SelectingMethodArgument());
    addArgument(new SubsumptionArgument());
  }


  //#########################################################################
  //# Interface net.sourceforge.waters.model.analysis.ModelVerifierFactory
  @Override
  public CompositionalConflictChecker createConflictChecker
    (final ProductDESProxyFactory factory)
  {
    final CompositionalConflictChecker.AbstractionMethod method =
      CompositionalConflictChecker.AbstractionMethod.OEQ;
    return new CompositionalConflictChecker(method, factory);
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
            "Maximum number of states constructed in final\n" +
            "monolithic composition");
    }

    //#######################################################################
    //# Overrides for Abstract Base Class
    //# net.sourceforge.waters.model.analysis.CommandLineArgument
    @Override
    protected void configure(final ModelVerifier verifier)
    {
      final int limit = getValue();
      final AbstractCompositionalModelVerifier composer =
        (AbstractCompositionalModelVerifier) verifier;
      composer.setMonolithicStateLimit(limit);
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
    protected void configure(final ModelVerifier verifier)
    {
      final int limit = getValue();
      final AbstractCompositionalModelVerifier composer =
        (AbstractCompositionalModelVerifier) verifier;
      composer.setInternalStateLimit(limit);
    }

  }


  //#########################################################################
  //# Inner Class LowerInternalStateLimitArgument
  private static class LowerInternalStateLimitArgument extends
      CommandLineArgumentInteger
  {

    //#######################################################################
    //# Constructors
    private LowerInternalStateLimitArgument()
    {
      super("-lslimit",
            "Initial maximum number of states for abstraction attempts");
    }

    //#######################################################################
    //# Overrides for Abstract Base Class
    //# net.sourceforge.waters.model.analysis.CommandLineArgument
    @Override
    protected void configure(final ModelVerifier verifier)
    {
      final int limit = getValue();
      final AbstractCompositionalModelVerifier composer =
        (AbstractCompositionalModelVerifier) verifier;
      composer.setLowerInternalStateLimit(limit);
    }

  }


  //#########################################################################
  //# Inner Class UpperInternalStateLimitArgument
  private static class UpperInternalStateLimitArgument extends
      CommandLineArgumentInteger
  {

    //#######################################################################
    //# Constructors
    private UpperInternalStateLimitArgument()
    {
      super("-uslimit",
            "Final maximum number of states for abstraction attempts");
    }

    //#######################################################################
    //# Overrides for Abstract Base Class
    //# net.sourceforge.waters.model.analysis.CommandLineArgument
    @Override
    protected void configure(final ModelVerifier verifier)
    {
      final int limit = getValue();
      final AbstractCompositionalModelVerifier composer =
        (AbstractCompositionalModelVerifier) verifier;
      composer.setUpperInternalStateLimit(limit);
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
            "Maximum number of transitions constructed in final\n" +
            "monolithic composition");
    }

    //#######################################################################
    //# Overrides for Abstract Base Class
    //# net.sourceforge.waters.model.analysis.CommandLineArgument
    @Override
    protected void configure(final ModelVerifier verifier)
    {
      final int limit = getValue();
      final AbstractCompositionalModelVerifier composer =
        (AbstractCompositionalModelVerifier) verifier;
      composer.setMonolithicTransitionLimit(limit);
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
            "Maximum number of transitions constructed in abstraction\n" +
            "attempts");
    }

    //#######################################################################
    //# Overrides for Abstract Base Class
    //# net.sourceforge.waters.model.analysis.CommandLineArgument
    @Override
    protected void configure(final ModelVerifier verifier)
    {
      final int limit = getValue();
      final AbstractCompositionalModelVerifier composer =
        (AbstractCompositionalModelVerifier) verifier;
      composer.setInternalTransitionLimit(limit);
    }

  }


  //#########################################################################
  //# Inner Class AbstractionMethodArgument
  private static class AbstractionMethodArgument
    extends CommandLineArgumentEnum<CompositionalConflictChecker.AbstractionMethod>
  {

    //#######################################################################
    //# Constructors
    private AbstractionMethodArgument()
    {
      super("-method", "Abstraction method used for conflict check",
            CompositionalConflictChecker.AbstractionMethod.class);
    }

    //#######################################################################
    //# Overrides for Abstract Base Class
    //# net.sourceforge.waters.model.analysis.CommandLineArgument
    @Override
    protected void configure(final ModelVerifier verifier)
    {
      final CompositionalConflictChecker.AbstractionMethod method =
        getValue();
      if (verifier instanceof CompositionalConflictChecker) {
        final CompositionalConflictChecker composer =
          (CompositionalConflictChecker) verifier;
        composer.setAbstractionMethod(method);
      } else {
        fail(getName() + " option only supported for conflict check!");
      }
    }

  }


  //#########################################################################
  //# Inner Class PreselectingMethodArgument
  private static class PreselectingMethodArgument
    extends CommandLineArgumentString
  {

    //#######################################################################
    //# Constructors
    private PreselectingMethodArgument()
    {
      super("-presel", "Preselecting heuristic for candidate selection");
    }

    //#######################################################################
    //# Overrides for Abstract Base Class
    //# net.sourceforge.waters.model.analysis.CommandLineArgument
    @Override
    protected void configure(final ModelVerifier verifier)
    {
      if (verifier instanceof CompositionalConflictChecker) {
        final String name = getValue();
        final CompositionalConflictChecker composer =
          (CompositionalConflictChecker) verifier;
        final EnumFactory<AbstractCompositionalModelVerifier.
                          PreselectingMethod>
          factory = composer.getPreselectingMethodFactory();
        final AbstractCompositionalModelVerifier.PreselectingMethod method =
          factory.getEnumValue(name);
        if (method == null) {
          System.err.println("Bad value for " + getName() + " option!");
          factory.dumpEnumeration(System.err, 0);
          System.exit(1);
        }
        composer.setPreselectingMethod(method);
      } else {
        fail(getName() + " option only supported for conflict check!");
      }
    }

    //#######################################################################
    //# Printing
    @Override
    protected void dump(final PrintStream stream,
                        final ModelVerifier verifier)
    {
      if (verifier instanceof CompositionalConflictChecker) {
        super.dump(stream, verifier);
        final CompositionalConflictChecker composer =
          (CompositionalConflictChecker) verifier;
        final EnumFactory<AbstractCompositionalModelVerifier.
                          PreselectingMethod>
          factory = composer.getPreselectingMethodFactory();
        factory.dumpEnumeration(stream, INDENT);
      }
    }

  }


  //#########################################################################
  //# Inner Class SelectingMethodArgument
  private static class SelectingMethodArgument
  extends CommandLineArgumentString
  {

    //#######################################################################
    //# Constructors
    private SelectingMethodArgument()
    {
      super("-sel", "Selecting heuristic for candidate selection");
    }

    //#######################################################################
    //# Overrides for Abstract Base Class
    //# net.sourceforge.waters.model.analysis.CommandLineArgument
    @Override
    protected void configure(final ModelVerifier verifier)
    {
      if (verifier instanceof CompositionalConflictChecker) {
        final String name = getValue();
        final CompositionalConflictChecker composer =
          (CompositionalConflictChecker) verifier;
        final EnumFactory<AbstractCompositionalModelVerifier.SelectingMethod>
          factory = composer.getSelectingMethodFactory();
        final AbstractCompositionalModelVerifier.SelectingMethod method =
          factory.getEnumValue(name);
        if (method == null) {
          System.err.println("Bad value for " + getName() + " option!");
          factory.dumpEnumeration(System.err, 0);
          System.exit(1);
        }
        composer.setSelectingMethod(method);
      } else {
        fail(getName() + " option only supported for conflict check!");
      }
    }

    //#######################################################################
    //# Printing
    @Override
    protected void dump(final PrintStream stream,
                        final ModelVerifier verifier)
    {
      if (verifier instanceof CompositionalConflictChecker) {
        super.dump(stream, verifier);
        final CompositionalConflictChecker composer =
          (CompositionalConflictChecker) verifier;
        final EnumFactory<AbstractCompositionalModelVerifier.SelectingMethod>
          factory = composer.getSelectingMethodFactory();
        factory.dumpEnumeration(stream, INDENT);
      }
    }

  }


  //#########################################################################
  //# Inner Class SubsumptionArgument
  private static class SubsumptionArgument extends CommandLineArgumentFlag
  {

    //#######################################################################
    //# Constructors
    private SubsumptionArgument()
    {
      super("-sub", "Check for subsumption in selecting heuristics");
    }

    //#######################################################################
    //# Overrides for Abstract Base Class
    //# net.sourceforge.waters.model.analysis.CommandLineArgument
    @Override
    protected void configure(final ModelVerifier verifier)
    {
      final AbstractCompositionalModelVerifier composer =
        (AbstractCompositionalModelVerifier) verifier;
      composer.setSubumptionEnabled(true);
    }

  }

}
