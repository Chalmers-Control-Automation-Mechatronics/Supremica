//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# Copyright (C) 2004-2020 Robi Malik
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

import net.sourceforge.waters.analysis.options.BooleanOption;
import net.sourceforge.waters.analysis.options.EnumOption;
import net.sourceforge.waters.analysis.options.OptionPage;
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


  @Override
  public void registerOptions(final OptionPage db)
  {
    super.registerOptions(db);
    db.add(new BooleanOption
             (OPTION_NativeModelAnalyzer_EventTreeEnabled,
              "Use branching program",
              "Compile the event enablement condition into a branching program " +
              "to speed up synchronous product computation.",
              "-et",
              true));
    db.add(new EnumOption<ConflictCheckMode>
             (OPTION_NativeConflictChecker_ConflictCheckMode,
              "Conflict check mode",
              "The strategy used to store or explore the reverse transition relation",
              "-mode",
              ConflictCheckMode.values(),
              ConflictCheckMode.NO_BACKWARDS_TRANSITIONS));
    db.add(new BooleanOption
           (OPTION_NativeConflictChecker_DumpStateAware,
            "Dump State Aware",
            "Enable or disable stopping in local deadlock states",
            "-lds",
            true));
  }


  //#########################################################################
  //# Class Constants
  public static final String OPTION_NativeModelAnalyzer_EventTreeEnabled =
    "NativeModelAnalyzer.EventTreeEnabled";
  public static final String OPTION_NativeConflictChecker_ConflictCheckMode =
    "NativeConflictChecker.ConflictCheckMode";
  public static final String OPTION_NativeConflictChecker_DumpStateAware =
    "NativeConflictChecker.DumpStateAware";

}
