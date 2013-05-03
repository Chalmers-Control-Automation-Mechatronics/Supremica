//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.cpp.analysis
//# CLASS:   NativeModelVerifierFactory
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.cpp.analysis;

import net.sourceforge.waters.model.analysis.CommandLineArgumentFlag;
import net.sourceforge.waters.model.analysis.des.AbstractModelVerifierFactory;
import net.sourceforge.waters.model.analysis.des.ModelVerifier;
import net.sourceforge.waters.model.des.ProductDESProxyFactory;


/**
 * A model verifier factory that produces model verifiers implemented in C++.
 *
 * @author Robi Malik
 */

public class NativeModelVerifierFactory
  extends AbstractModelVerifierFactory
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
    addArgument(new CommandLineArgumentBroad());
    addArgument(new CommandLineArgumentNarrow());
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
  //# Inner Class CommandLineArgumentBroad
  private static class CommandLineArgumentBroad
    extends CommandLineArgumentFlag
  {
    private CommandLineArgumentBroad()
    {
      super("-broad", "Run state expansion in 'broad' mode");
    }

    @Override
    public void configure(final ModelVerifier verifier)
    {
      final NativeModelVerifier nverifier = (NativeModelVerifier) verifier;
      nverifier.setExplorerMode(ExplorerMode.BROAD);
    }
  }


  //#########################################################################
  //# Inner Class CommandLineArgumentNarrow
  private static class CommandLineArgumentNarrow
    extends CommandLineArgumentFlag
  {
    private CommandLineArgumentNarrow()
    {
      super("-narrow", "Run state expansion in 'narrow' mode");
    }

    @Override
    public void configure(final ModelVerifier verifier)
    {
      final NativeModelVerifier nverifier = (NativeModelVerifier) verifier;
      nverifier.setExplorerMode(ExplorerMode.NARROW);
    }
  }

}
