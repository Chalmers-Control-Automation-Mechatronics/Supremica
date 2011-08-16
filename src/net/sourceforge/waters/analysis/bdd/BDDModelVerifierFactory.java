//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.analysis.bdd
//# CLASS:   BDDModelVerifierFactory
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.analysis.bdd;

import java.util.List;

import net.sourceforge.waters.model.analysis.AbstractModelVerifierFactory;
import net.sourceforge.waters.model.analysis.CommandLineArgumentEnum;
import net.sourceforge.waters.model.analysis.CommandLineArgumentFlag;
import net.sourceforge.waters.model.analysis.CommandLineArgumentInteger;
import net.sourceforge.waters.model.analysis.ModelVerifier;
import net.sourceforge.waters.model.des.ProductDESProxyFactory;


/**
 * A model verifier factory to produce BDD-based model verifiers.
 *
 * @author Robi Malik
 */

public class BDDModelVerifierFactory
  extends AbstractModelVerifierFactory
{

  //#########################################################################
  //# Singleton Implementation
  public static BDDModelVerifierFactory getInstance()
  {
    return SingletonHolder.theInstance;
  }

  public static BDDModelVerifierFactory getInstance(final List<String> cmdline)
  {
    return new BDDModelVerifierFactory(cmdline);
  }

  private static class SingletonHolder {
    private static final BDDModelVerifierFactory theInstance =
      new BDDModelVerifierFactory();
  }


  //#########################################################################
  //# Constructors
  private BDDModelVerifierFactory()
  {
  }

  private BDDModelVerifierFactory(final List<String> arglist)
  {
    super(arglist);
    addArgument(new CommandLineArgumentPack());
    addArgument(new CommandLineArgumentOrder());
    addArgument(new CommandLineArgumentDynamic());
    addArgument(new CommandLineArgumentParitioningSizeLimit());
  }


  //#########################################################################
  //# Interface net.sourceforge.waters.model.analysis.ModelVerifierFactory
  @Override
  public BDDConflictChecker createConflictChecker
    (final ProductDESProxyFactory factory)
  {
    return new BDDConflictChecker(factory);
  }

  @Override
  public BDDControllabilityChecker createControllabilityChecker
    (final ProductDESProxyFactory factory)
  {
    return new BDDControllabilityChecker(factory);
  }

  @Override
  public BDDLanguageInclusionChecker createLanguageInclusionChecker
    (final ProductDESProxyFactory factory)
  {
    return new BDDLanguageInclusionChecker(factory);
  }


  //#########################################################################
  //# Inner Class CommandLineArgumentPack
  private static class CommandLineArgumentPack
    extends CommandLineArgumentEnum<BDDPackage>
  {
    //#######################################################################
    //# Constructor
    private CommandLineArgumentPack()
    {
      super("-pack", "Specify BDD package", BDDPackage.class);
    }

    //#######################################################################
    //# Overrides for
    //# net.sourceforge.waters.model.analysis.CommandLineArgument
    @Override
    protected void configure(final ModelVerifier verifier)
    {
      final BDDModelVerifier bddVerifier = (BDDModelVerifier) verifier;
      final BDDPackage pack = getValue();
      bddVerifier.setBDDPackage(pack);
    }
  }


  //#########################################################################
  //# Inner Class CommandLineArgumentOrder
  private static class CommandLineArgumentOrder
    extends CommandLineArgumentEnum<VariableOrdering>
  {
    //#######################################################################
    //# Constructor
    private CommandLineArgumentOrder()
    {
      super("-order", "Set initial variable ordering method",
            VariableOrdering.class);
    }

    //#######################################################################
    //# Overrides for
    //# net.sourceforge.waters.model.analysis.CommandLineArgument
    @Override
    protected void configure(final ModelVerifier verifier)
    {
      final BDDModelVerifier bddVerifier = (BDDModelVerifier) verifier;
      final VariableOrdering ordering = getValue();
      bddVerifier.setVariableOrdering(ordering);
    }
  }


  //#########################################################################
  //# Inner Class CommandLineArgumentDynamic
  private static class CommandLineArgumentDynamic
    extends CommandLineArgumentFlag
  {
    //#######################################################################
    //# Constructor
    private CommandLineArgumentDynamic()
    {
      super("-dynamic", "Enable dynamic variable reordering");
    }

    //#######################################################################
    //# Overrides for
    //# net.sourceforge.waters.model.analysis.CommandLineArgument
    @Override
    protected void configure(final ModelVerifier verifier)
    {
      final BDDModelVerifier bddVerifier = (BDDModelVerifier) verifier;
      final boolean enable = getValue();
      bddVerifier.setReorderingEnabled(enable);
    }
  }


  //#########################################################################
  //# Inner Class CommandLineArgumentParitioningSizeLimit
  private static class CommandLineArgumentParitioningSizeLimit
    extends CommandLineArgumentInteger
  {
    //#######################################################################
    //# Constructor
    private CommandLineArgumentParitioningSizeLimit()
    {
      super("-part", "Maximum BDD size when merging partitioned BDDs");
    }

    //#######################################################################
    //# Overrides for
    //# net.sourceforge.waters.model.analysis.CommandLineArgument
    @Override
    protected void configure(final ModelVerifier verifier)
    {
      final BDDModelVerifier bddVerifier = (BDDModelVerifier) verifier;
      final int limit = getValue();
      bddVerifier.setPartitioningSizeLimit(limit);
    }
  }

}
