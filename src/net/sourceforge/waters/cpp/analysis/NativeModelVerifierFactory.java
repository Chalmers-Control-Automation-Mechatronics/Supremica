//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.cpp.analysis
//# CLASS:   NativeModelVerifierFactory
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.cpp.analysis;

import net.sourceforge.waters.model.analysis.CommandLineArgumentBoolean;
import net.sourceforge.waters.model.analysis.CommandLineArgumentFlag;
import net.sourceforge.waters.model.analysis.des.AbstractModelAnalyzerFactory;
import net.sourceforge.waters.model.des.ProductDESProxyFactory;


/**
 * A model verifier factory that produces model verifiers implemented in C++.
 *
 * @author Robi Malik
 */

public class NativeModelVerifierFactory
  extends AbstractModelAnalyzerFactory
{

  //#########################################################################
  //# Singleton Pattern
  public static NativeModelVerifierFactory getInstance()
  {
    return SingletonHolder.INSTANCE;
  }

  private static class SingletonHolder {
    private static final NativeModelVerifierFactory INSTANCE =
      new NativeModelVerifierFactory();
  }


  //#########################################################################
  //# Constructors
  private NativeModelVerifierFactory()
  {
  }


  //#########################################################################
  //# Overrides for
  //# net.sourceforge.waters.model.analysis.AbstractModelVerifierFactory
  @Override
  protected void addArguments()
  {
    super.addArguments();
    addArgument(new CommandLineArgumentDumpStateAware());
    addArgument(new CommandLineArgumentEventTree());
    addArgument(new CommandLineArgumentTarjan());
  }


  //#########################################################################
  //# Interface net.sourceforge.waters.model.analysis.ModelVerifierFactory
  @Override
  public NativeConflictChecker createConflictChecker
    (final ProductDESProxyFactory factory)
  {
    return new NativeConflictChecker(factory);
  }

  @Override
  public NativeControllabilityChecker createControllabilityChecker
    (final ProductDESProxyFactory factory)
  {
    return new NativeControllabilityChecker(factory);
  }

  @Override
  public NativeLanguageInclusionChecker createLanguageInclusionChecker
    (final ProductDESProxyFactory factory)
  {
    return new NativeLanguageInclusionChecker(factory);
  }


  //#########################################################################
  //# Inner Class CommandLineArgumentDumpStateAware
  private static class CommandLineArgumentDumpStateAware
    extends CommandLineArgumentBoolean
  {
    private CommandLineArgumentDumpStateAware()
    {
      super("-lds", "Enable or disable stopping in local deadlock states");
    }

    @Override
    public void configureAnalyzer(final Object verifier)
    {
      if (verifier instanceof NativeConflictChecker) {
        final NativeConflictChecker checker = (NativeConflictChecker) verifier;
        final boolean aware = getValue();
        checker.setDumpStateAware(aware);
      } else {
        fail("Command line option " + getName() +
             " is only supported for conflict check!");
      }
    }
  }


  //#########################################################################
  //# Inner Class CommandLineArgumentEventTree
  private static class CommandLineArgumentEventTree
    extends CommandLineArgumentBoolean
  {
    private CommandLineArgumentEventTree()
    {
      super("-et", "Enable or disable event decision tree");
    }

    @Override
    public void configureAnalyzer(final Object analyzer)
    {
      final NativeModelAnalyzer checker = (NativeModelAnalyzer) analyzer;
      final boolean enabled = getValue();
      checker.setEventTreeEnabled(enabled);
    }
  }


  //#########################################################################
  //# Inner Class CommandLineArgumentTarjan
  private static class CommandLineArgumentTarjan
    extends CommandLineArgumentFlag
  {
    private CommandLineArgumentTarjan()
    {
      super("-tarjan", "Use Tarjan's algorithm for conflict check");
    }

    @Override
    public void configureAnalyzer(final Object verifier)
    {
      if (verifier instanceof NativeConflictChecker) {
        final NativeConflictChecker checker = (NativeConflictChecker) verifier;
        checker.setConflictCheckMode(ConflictCheckMode.NO_BACKWARDS_TRANSITIONS);
      } else {
        fail("Command line option " + getName() +
             " is only supported for conflict check!");
      }
    }
  }

}
