//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# Copyright (C) 2004-2017 Robi Malik
//###########################################################################
//# This file is part of Waters.
//# Waters is free software: you can redistribute it and/or modify it under
//# the terms of the GNU General Public License as published by the Free
//# Software Foundation, either version 2 of the License, or (at your option)
//# any later version.
//# Waters is distributed in the hope that it will be useful, but WITHOUT ANY
//# WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
//# FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
//# details.
//# You should have received a copy of the GNU General Public License along
//# with Waters. If not, see <http://www.gnu.org/licenses/>.
//#
//# Linking Waters statically or dynamically with other modules is making a
//# combined work based on Waters. Thus, the terms and conditions of the GNU
//# General Public License cover the whole combination.
//# In addition, as a special exception, the copyright holders of Waters give
//# you permission to combine Waters with code included in the standard
//# release of Supremica under the Supremica Software License Agreement (or
//# modified versions of such code, with unchanged license). You may copy and
//# distribute such a system following the terms of the GNU GPL for Waters and
//# the licenses of the other code concerned.
//# Note that people who make modified versions of Waters are not obligated to
//# grant this special exception for their modified versions; it is their
//# choice whether to do so. The GNU General Public License gives permission
//# to release a modified version without this exception; this exception also
//# makes it possible to release a modified version which carries forward this
//# exception.
//###########################################################################

package net.sourceforge.waters.analysis.bdd;

import net.sourceforge.waters.model.analysis.CommandLineArgumentBoolean;
import net.sourceforge.waters.model.analysis.CommandLineArgumentEnum;
import net.sourceforge.waters.model.analysis.CommandLineArgumentInteger;
import net.sourceforge.waters.model.analysis.des.AbstractModelAnalyzerFactory;
import net.sourceforge.waters.model.des.ProductDESProxyFactory;


/**
 * A model verifier factory to produce BDD-based model verifiers.
 *
 * @author Robi Malik
 */

public class BDDModelVerifierFactory
  extends AbstractModelAnalyzerFactory
{

  //#########################################################################
  //# Singleton Implementation
  public static BDDModelVerifierFactory getInstance()
  {
    return SingletonHolder.theInstance;
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


  //#########################################################################
  //# Overrides for
  //# net.sourceforge.waters.model.analysis.AbstractModelVerifierFactory
  @Override
  protected void addArguments()
  {
    super.addArguments();
    addArgument(new CommandLineArgumentPack());
    addArgument(new CommandLineArgumentOrder());
    addArgument(new CommandLineArgumentInitialSize());
    addArgument(new CommandLineArgumentPartitioningStrategy());
    addArgument(new CommandLineArgumentPartitioningSizeLimit());
    addArgument(new CommandLineArgumentDynamic());
    addArgument(new CommandLineArgumentEarlyDeadlock());
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
  public BDDDeadlockChecker createDeadlockChecker
    (final ProductDESProxyFactory factory)
  {
    return new BDDDeadlockChecker(factory);
  }

  @Override
  public BDDLanguageInclusionChecker createLanguageInclusionChecker
    (final ProductDESProxyFactory factory)
  {
    return new BDDLanguageInclusionChecker(factory);
  }

  @Override
  public BDDStateCounter createStateCounter
    (final ProductDESProxyFactory factory)
  {
    return new BDDStateCounter(factory);
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
    public void configureAnalyzer(final Object analyzer)
    {
      final BDDModelVerifier bddVerifier = (BDDModelVerifier) analyzer;
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
    public void configureAnalyzer(final Object analyzer)
    {
      final BDDModelVerifier bddVerifier = (BDDModelVerifier) analyzer;
      final VariableOrdering ordering = getValue();
      bddVerifier.setVariableOrdering(ordering);
    }
  }


  //#########################################################################
  //# Inner Class CommandLineArgumentDynamic
  private static class CommandLineArgumentDynamic
    extends CommandLineArgumentBoolean
  {
    //#######################################################################
    //# Constructor
    private CommandLineArgumentDynamic()
    {
      super("-dynamic", "Enable or disable dynamic variable reordering");
    }

    //#######################################################################
    //# Overrides for
    //# net.sourceforge.waters.model.analysis.CommandLineArgument
    @Override
    public void configureAnalyzer(final Object analyzer)
    {
      final BDDModelVerifier bddVerifier = (BDDModelVerifier) analyzer;
      final boolean enabled = getValue();
      bddVerifier.setReorderingEnabled(enabled);
    }
  }


  //#########################################################################
  //# Inner Class CommandLineArgumentEarlyDeadlock
  private static class CommandLineArgumentEarlyDeadlock
    extends CommandLineArgumentBoolean
  {
    //#######################################################################
    //# Constructor
    private CommandLineArgumentEarlyDeadlock()
    {
      super("-edl", "Enable or disable early deadlock detection");
    }

    //#######################################################################
    //# Overrides for
    //# net.sourceforge.waters.model.analysis.CommandLineArgument
    @Override
    public void configureAnalyzer(final Object analyzer)
    {
      if (analyzer instanceof BDDConflictChecker) {
        final BDDConflictChecker bddVerifier = (BDDConflictChecker) analyzer;
        final boolean enabled = getValue();
        bddVerifier.setEarlyDeadlockEnabled(enabled);
      } else {
        fail("Command line option " + getName() +
             " is only supported for conflict check!");
      }
    }
  }


  //#########################################################################
  //# Inner Class CommandLineArgumentInitialSize
  private static class CommandLineArgumentInitialSize
    extends CommandLineArgumentInteger
  {
    //#######################################################################
    //# Constructor
    private CommandLineArgumentInitialSize()
    {
      super("-size", "Initial size of BDD node table");
    }

    //#######################################################################
    //# Overrides for
    //# net.sourceforge.waters.model.analysis.CommandLineArgument
    @Override
    public void configureAnalyzer(final Object analyzer)
    {
      final BDDModelVerifier bddVerifier = (BDDModelVerifier) analyzer;
      final int size = getValue();
      bddVerifier.setInitialSize(size);
    }
  }


  //#########################################################################
  //# Inner Class CommandLineArgumentOrder
  private static class CommandLineArgumentPartitioningStrategy
    extends CommandLineArgumentEnum<TransitionPartitioningStrategy>
  {
    //#######################################################################
    //# Constructor
    private CommandLineArgumentPartitioningStrategy()
    {
      super("-part", "Set transition partitioning strategy",
            TransitionPartitioningStrategy.class);
    }

    //#######################################################################
    //# Overrides for
    //# net.sourceforge.waters.model.analysis.CommandLineArgument
    @Override
    public void configureAnalyzer(final Object analyzer)
    {
      final BDDModelVerifier bddVerifier = (BDDModelVerifier) analyzer;
      final TransitionPartitioningStrategy strategy = getValue();
      bddVerifier.setTransitionPartitioningStrategy(strategy);
    }
  }


  //#########################################################################
  //# Inner Class CommandLineArgumentParitioningSizeLimit
  private static class CommandLineArgumentPartitioningSizeLimit
    extends CommandLineArgumentInteger
  {
    //#######################################################################
    //# Constructor
    private CommandLineArgumentPartitioningSizeLimit()
    {
      super("-plimit", "Maximum BDD size when merging partitioned BDDs");
    }

    //#######################################################################
    //# Overrides for
    //# net.sourceforge.waters.model.analysis.CommandLineArgument
    @Override
    public void configureAnalyzer(final Object analyzer)
    {
      final BDDModelVerifier bddVerifier = (BDDModelVerifier) analyzer;
      final int limit = getValue();
      bddVerifier.setPartitioningSizeLimit(limit);
    }
  }

}
