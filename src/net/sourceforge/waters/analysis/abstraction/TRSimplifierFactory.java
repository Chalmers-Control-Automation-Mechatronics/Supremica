//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# Copyright (C) 2004-2019 Robi Malik
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

package net.sourceforge.waters.analysis.abstraction;

import java.util.ArrayList;
import java.util.List;

import net.sourceforge.waters.analysis.options.BooleanOption;
import net.sourceforge.waters.analysis.options.OptionMap;
import net.sourceforge.waters.analysis.options.PositiveIntOption;


/**
 * A default implementation of the {@link TRSimplifierFactory} interface.
 *
 * @author Benjamin Wheeler
 */

public abstract class AbstractTRSimplifierFactory
  implements TRSimplifierFactory
{

  //#########################################################################
  //# Constructors
  protected AbstractTRSimplifierFactory()
  {
    registerTRToolCreators();
  }


  //#########################################################################
  //# Configuration


  //#########################################################################
  //# Supremica Options
  @Override
  public void configureFromOptions(final TransitionRelationSimplifier simp)
  {
  }

  @Override
  public void registerOptions(final OptionMap db)
  {
    db.add(new PositiveIntOption
             (OPTION_Abstract_StateLimit,
              "State Limit",
              "",
              "-slimit",
              Integer.MAX_VALUE));
    db.add(new PositiveIntOption
           (OPTION_SubsetConstruction_MaxIncrease,
            "Max Increase",
            "",
            "-maxinc",
            Integer.MAX_VALUE));
    db.add(new PositiveIntOption
           (OPTION_Abstract_TransitionLimit,
            "Transition Limit",
            "",
            "-tlimit",
            Integer.MAX_VALUE));
    db.add(new BooleanOption
           (OPTION_SubsetConstruction_DumpStateAware,
            "Dump State Aware",
            "",
            "-dumpsa",
            false));
    db.add(new BooleanOption
           (OPTION_SubsetConstruction_FailingEventsAsSelfLoops,
            "Failing Events As Self Loops",
            "",
            "-fesl",
            false));
  }

  //#########################################################################
  //# Auxiliary Methods

  private void registerTRToolCreators()
  {

    mToolCreators.add(new TRSimplifierCreator("Subset Construction",
      "") {
      @Override
      public TransitionRelationSimplifier create()
      {
        return new SubsetConstructionTRSimplifier();
      }
    });

    mToolCreators.add(new TRSimplifierCreator("Special Events",
      "") {
      @Override
      public TransitionRelationSimplifier create()
      {
        return new SpecialEventsTRSimplifier();
      }
    });

  }

  public List<TRSimplifierCreator> getToolCreators()
  {
    return mToolCreators;
  }

  public static AbstractTRSimplifierFactory getInstance()
  {
    if (mInstance == null) {
      mInstance = new AbstractTRSimplifierFactory() {};
    }
    return mInstance;
  }


  //#########################################################################
  //# Data Members
  private final List<TRSimplifierCreator> mToolCreators = new ArrayList<>();
  private static AbstractTRSimplifierFactory mInstance = null;

  //#########################################################################
  //# Class Constants
  public static final String OPTION_Abstract_StateLimit =
    "SubsetConstruction.StateLimit";
  public static final String OPTION_Abstract_TransitionLimit =
    "SubsetConstruction.TransitionLimit";
  public static final String OPTION_SubsetConstruction_MaxIncrease =
    "SubsetConstruction.MaxIncrease";
  public static final String OPTION_SubsetConstruction_DumpStateAware =
    "SubsetConstruction.DumpStateAware";
  public static final String OPTION_SubsetConstruction_FailingEventsAsSelfLoops =
    "SubsetConstruction.FailingEventsAsSelfLoops";

}
