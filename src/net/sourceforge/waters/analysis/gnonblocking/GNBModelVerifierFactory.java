//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# Copyright (C) 2004-2021 Robi Malik
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

package net.sourceforge.waters.analysis.gnonblocking;

import net.sourceforge.waters.analysis.options.EnumOption;
import net.sourceforge.waters.analysis.options.OptionPage;
import net.sourceforge.waters.model.analysis.des.AbstractModelAnalyzerFactory;
import net.sourceforge.waters.model.des.ProductDESProxyFactory;


/**
 * A factory that produces compositional model verifiers.
 * This provides access to experimental compositional model verifiers
 * written by Jinjian Shi and Rachel Francis.
 *
 * @see CompositionalGeneralisedConflictChecker
 *
 * @author Jinjian Shi, Rachel Francis, Robi Malik
 */

public class GNBModelVerifierFactory extends AbstractModelAnalyzerFactory
{

  //#########################################################################
  //# Singleton Pattern
  public static GNBModelVerifierFactory getInstance()
  {
    return SingletonHolder.INSTANCE;
  }

  private static class SingletonHolder {
    private static final GNBModelVerifierFactory INSTANCE =
      new GNBModelVerifierFactory();
  }


  //#########################################################################
  //# Constructors
  private GNBModelVerifierFactory()
  {
  }


  //#########################################################################
  //# Interface net.sourceforge.waters.model.analysis.ModelVerifierFactory
  @Override
  public CompositionalGeneralisedConflictChecker createConflictChecker
    (final ProductDESProxyFactory factory)
  {
    return new CompositionalGeneralisedConflictChecker(null, factory);
  }

  /*public AlphaNonBlockingChecker createConflictChecker
    (final ProductDESProxyFactory factory)
  {
    return new AlphaNonBlockingChecker(null, factory);
  }*/

  /*public CanonicalGeneralisedConflictChecker createConflictChecker
    (final ProductDESProxyFactory factory)
  {
    return new CanonicalGeneralisedConflictChecker(null, factory);
  }*/


  @Override
  public void registerOptions(final OptionPage db)
  {
    super.registerOptions(db);
    db.register(new EnumOption<PreselectingHeuristicFactory>
             (OPTION_CompositionalGeneralisedConflictChecker_PreselectingHeuristic,
              "Preselection method",
              "Preselection heuristic to generate groups of automata to consider " +
              "for composition.",
              "-presel",
              PreselectingHeuristicFactory.getInstance()));
    db.register(new EnumOption<SelectingHeuristicFactory>
             (OPTION_CompositionalGeneralisedConflictChecker_SelectingHeuristic,
              "Selection method",
              "Heuristic to choose the group of automata to compose and simplify " +
              "from the options produced by the preselection method.",
              "-sel",
              SelectingHeuristicFactory.getInstance()));
  }


  //#########################################################################
  //# Class Constants
  public static final String
    OPTION_CompositionalGeneralisedConflictChecker_PreselectingHeuristic =
    "CompositionalGeneralisedConflictChecker.PreselectingHeuristic";
  public static final String
    OPTION_CompositionalGeneralisedConflictChecker_SelectingHeuristic =
    "CompositionalGeneralisedConflictChecker.SelectingHeuristic";

}
