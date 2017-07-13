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

package net.sourceforge.waters.cpp.analysis;

import net.sourceforge.waters.model.analysis.CommandLineArgumentBoolean;
import net.sourceforge.waters.model.analysis.CommandLineArgumentFlag;
import net.sourceforge.waters.model.analysis.des.AbstractModelAnalyzerFactory;
import net.sourceforge.waters.model.analysis.des.StateCounter;
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
  public NativeControlLoopChecker createControlLoopChecker
    (final ProductDESProxyFactory factory)
  {
    return new NativeControlLoopChecker(factory);
  }

  @Override
  public NativeDeadlockChecker createDeadlockChecker
    (final ProductDESProxyFactory factory)
  {
    return new NativeDeadlockChecker(factory);
  }

  @Override
  public NativeLanguageInclusionChecker createLanguageInclusionChecker
    (final ProductDESProxyFactory factory)
  {
    return new NativeLanguageInclusionChecker(factory);
  }

  @Override
  public StateCounter createStateCounter
    (final ProductDESProxyFactory factory)
  {
    return new NativeStateCounter(factory);
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
      } else if (verifier instanceof NativeControlLoopChecker) {
        // ignore
      } else {
        fail("Command line option " + getName() +
             " is only supported for conflict check!");
      }
    }
  }

}
