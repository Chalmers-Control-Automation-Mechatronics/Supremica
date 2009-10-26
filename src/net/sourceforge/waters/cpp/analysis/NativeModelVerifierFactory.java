//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.cpp.analysis
//# CLASS:   NativeModelVerifierFactory
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.cpp.analysis;

import java.util.List;

import net.sourceforge.waters.model.analysis.AbstractModelVerifierFactory;
import net.sourceforge.waters.model.analysis.CommandLineArgumentFlag;
import net.sourceforge.waters.model.analysis.ModelVerifier;
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
  //# Constructors
  private NativeModelVerifierFactory()
  {
  }

  private NativeModelVerifierFactory(final List<String> arglist)
  {
    super(arglist);
    addArgument(new CommandLineArgumentBroad());
    addArgument(new CommandLineArgumentNarrow());
  }


  //#########################################################################
  //# Interface net.sourceforge.waters.model.analysis.ModelVerifierFactory
  public NativeConflictChecker createConflictChecker
    (final ProductDESProxyFactory factory)
  {
    return new NativeConflictChecker(factory);
  }

  public NativeControllabilityChecker createControllabilityChecker
    (final ProductDESProxyFactory factory)
  {
    return new NativeControllabilityChecker(factory);
  }

  public NativeLanguageInclusionChecker createLanguageInclusionChecker
    (final ProductDESProxyFactory factory)
  {
    return new NativeLanguageInclusionChecker(factory);
  }


  //#########################################################################
  //# Factory Instantiation
  public static NativeModelVerifierFactory getInstance()
  {
    if (theInstance == null) {
      theInstance = new NativeModelVerifierFactory();
    }
    return theInstance;
  }

  public static NativeModelVerifierFactory
    getInstance(final List<String> cmdline)
  {
    return new NativeModelVerifierFactory(cmdline);
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

    protected void configure(final ModelVerifier verifier)
    {
      NativeModelVerifier nverifier = (NativeModelVerifier) verifier;
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

    protected void configure(final ModelVerifier verifier)
    {
      NativeModelVerifier nverifier = (NativeModelVerifier) verifier;
      nverifier.setExplorerMode(ExplorerMode.NARROW);
    }
  }


  //#########################################################################
  //# Class Variables
  private static NativeModelVerifierFactory theInstance = null;

}
