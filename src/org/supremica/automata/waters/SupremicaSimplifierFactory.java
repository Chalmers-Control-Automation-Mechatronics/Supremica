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

package org.supremica.automata.waters;

import java.util.List;

import net.sourceforge.waters.analysis.abstraction.AutomatonSimplifierCreator;
import net.sourceforge.waters.analysis.abstraction.AutomatonSimplifierFactory;
import net.sourceforge.waters.analysis.abstraction.TransitionRelationSimplifier;
import net.sourceforge.waters.analysis.options.BooleanOption;
import net.sourceforge.waters.analysis.options.OptionMap;
import net.sourceforge.waters.model.analysis.des.AutomatonBuilder;
import net.sourceforge.waters.model.des.ProductDESProxyFactory;

import org.supremica.automata.algorithms.EquivalenceRelation;


/**
 * A factory class to provide access to all transition relation simplifiers
 * ({@link TransitionRelationSimplifier}) and their options. Used for
 * creating GUI.
 *
 * @author Benjamin Wheeler
 */

public class SupremicaSimplifierFactory extends AutomatonSimplifierFactory
{

  //#########################################################################
  //# Constructor
  private SupremicaSimplifierFactory()
  {
    super();
  }

  @Override
  public String toString()
  {
    return "Supremica Simplifiers";
  }

  //#########################################################################
  //# Options
  @Override
  public void registerOptions(final OptionMap db)
  {
    super.registerOptions(db);
    db.add(new BooleanOption
           (SupremicaAutomatonBuilder.OPTION_SupremicaAutomatonBuilder_AlsoTransitions,
            "Also minimise number of transitions",
            "",//TODO
            "-alsot",
            true));
    db.add(new BooleanOption
           (SupremicaAutomatonBuilder.OPTION_SupremicaAutomatonBuilder_IgnoreMarking,
            "Ignore marking of states",
            "",//TODO
            "-ignorem",
            false));
  }


  //#########################################################################
  //# Auxiliary Methods
  @Override
  protected void registerSimplifierCreators()
  {
    final List<AutomatonSimplifierCreator> creators = getSimplifierCreators();
    creators.add(new SupremicaSimplifierCreator
                 ("Language Equivalence",
                  "Returns a deterministic automaton representing the same\n" +
                  "language using a minimal number of states and transitions.\n" +
                  "  If the automaton is nondeterministic, it is first made\n" +
                  "deterministic.",
                  EquivalenceRelation.LANGUAGEEQUIVALENCE));
    creators.add(new SupremicaSimplifierCreator
                 ("Conflict Equivalence",
                  "This minimization algorithm is experimental! The\n" +
                  "result may not be minimal but should at least be\n" +
                  "conflict equivalent to the input.",
                  EquivalenceRelation.CONFLICTEQUIVALENCE));
    creators.add(new SupremicaSimplifierCreator
                 ("Supervision Equivalence",
                  "This minimization algorithm is experimental! The\n" +
                  "result may not be minimal but should at least be\n" +
                  "supervision equivalent to the input.",
                  EquivalenceRelation.SUPERVISIONEQUIVALENCE));
    creators.add(new SupremicaSimplifierCreator
                 ("Synthesis Abstraction",
                  "",
                  EquivalenceRelation.SYNTHESISABSTRACTION));
    creators.add(new SupremicaSimplifierCreator
                 ("Observation Equivalence",
                  "",
                  EquivalenceRelation.OBSERVATIONEQUIVALENCE));
  }

  public static SupremicaSimplifierFactory getInstance()
  {
    if (mInstance == null) {
      mInstance = new SupremicaSimplifierFactory();
    }
    return mInstance;
  }


  private class SupremicaSimplifierCreator extends AutomatonSimplifierCreator {

    protected SupremicaSimplifierCreator(final String name,
                                         final String description,
                                         final EquivalenceRelation relation)
    {
      super(name, description);
      mRelation = relation;
    }

    @Override
    public AutomatonBuilder createBuilder(final ProductDESProxyFactory factory)
    {
      return new SupremicaAutomatonBuilder(factory, mRelation);
    }

    private final EquivalenceRelation mRelation;

  }

  //#########################################################################
  //# Data Members
  private static SupremicaSimplifierFactory mInstance = null;


  //#########################################################################
  //# Class Constants
}
