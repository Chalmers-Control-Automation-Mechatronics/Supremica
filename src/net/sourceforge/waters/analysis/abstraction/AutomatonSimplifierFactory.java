//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# Copyright (C) 2004-2023 Robi Malik
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

import net.sourceforge.waters.model.options.BooleanOption;
import net.sourceforge.waters.model.options.LeafOptionPage;


/**
 * A factory class to provide access to all transition relation simplifiers
 * ({@link TransitionRelationSimplifier}) and their options. Used for
 * creating GUI.
 *
 * @author Benjamin Wheeler
 */

public abstract class AutomatonSimplifierFactory
{

  //#########################################################################
  //# Constructor
  protected AutomatonSimplifierFactory()
  {
    registerSimplifierCreators();
  }


  //#########################################################################
  //# Options
  public void registerOptions(final LeafOptionPage db)
  {
    db.register(new BooleanOption
             (OPTION_AutomatonSimplifierFactory_KeepOriginal,
              "Keep Original",
              "Do not remove the input automaton from the analyzer " +
              "after this operation.",
              "-keep",
              true));
  }


  //#########################################################################
  //# Auxiliary Methods
  protected abstract void registerSimplifierCreators();

  public List<AutomatonSimplifierCreator> getSimplifierCreators()
  {
    return mSimplifierCreators;
  }

  //#########################################################################
  //# Data Members
  private final List<AutomatonSimplifierCreator> mSimplifierCreators = new ArrayList<>();


  //#########################################################################
  //# Class Constants

  public static final String OPTION_AutomatonSimplifierFactory_KeepOriginal =
    "AutomatonSimplifierFactory.KeepOriginal";
}
