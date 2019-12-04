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

import java.util.List;

import net.sourceforge.waters.analysis.options.OptionMap;
import net.sourceforge.waters.analysis.tr.TRAutomatonBuilder;
import net.sourceforge.waters.model.analysis.des.AutomatonBuilder;
import net.sourceforge.waters.model.des.ProductDESProxyFactory;


/**
 * A factory class to provide access to all transition relation simplifiers
 * ({@link TransitionRelationSimplifier}) and their options. Used for
 * creating GUI.
 *
 * @author Benjamin Wheeler
 */

public class StepSimplifierFactory extends TRSimplifierFactory
{

  //#########################################################################
  //# Constructor
  private StepSimplifierFactory()
  {
    super();
  }

  @Override
  public String toString()
  {
    return "Transition Relation Simplifiers";
  }

  //#########################################################################
  //# Options
  @Override
  public void registerOptions(final OptionMap db)
  {
    super.registerOptions(db);
  }


  //#########################################################################
  //# Auxiliary Methods
  @Override
  protected void registerSimplifierCreators()
  {
    final List<AutomatonSimplifierCreator> creators = getSimplifierCreators();
    creators.add(new TRSimplifierCreator("Partition Refinement",
      "Perform automaton minimisation by partition refinement, " +
      "such as Hopcroft's minimisation algorithm or bisimulation.") {
      @Override
      protected TransitionRelationSimplifier createTRSimplifier()
      {
        return new ObservationEquivalenceTRSimplifier();
      }
    });
    creators.add(new TRSimplifierCreator("Subset Construction",
      "Make a nondeterministic automaton deterministic using the " +
      "subset construction algorithm.") {
      @Override
      protected TransitionRelationSimplifier createTRSimplifier()
      {
        return new SubsetConstructionTRSimplifier();
      }
    });
    creators.add(new TRSimplifierCreator("Special Events",
      "Hide local events, remove selfloops with selfloop-only events," +
      "remove blocked events, and redirect failing events.") {
      @Override
      protected TransitionRelationSimplifier createTRSimplifier()
      {
        return new SpecialEventsTRSimplifier();
      }
    });
    creators.add(new TRSimplifierCreator("Synthesis Observation Equivalence",
      "Perform synthesis abstraction using synthesis observation equivalence " +
      "or weak synthesis observation equivalence.") {
      @Override
      protected TransitionRelationSimplifier createTRSimplifier()
      {
        return new SynthesisObservationEquivalenceTRSimplifier();
      }
    });
  }

  public static StepSimplifierFactory getInstance()
  {
    if (mInstance == null) {
      mInstance = new StepSimplifierFactory();
    }
    return mInstance;
  }


  private abstract class TRSimplifierCreator extends AutomatonSimplifierCreator {

    protected TRSimplifierCreator(final String name, final String description)
    {
      super(name, description);
    }

    /**
     * Creates a tool to be used by the given model analyser.
     */
    @Override
    public AutomatonBuilder createBuilder(final ProductDESProxyFactory factory) {
      return new TRAutomatonBuilder(factory, createTRSimplifier());
    }

    /**
     * Creates a tool to be used by the given model analyser.
     */
    protected abstract TransitionRelationSimplifier createTRSimplifier();

  }


  //#########################################################################
  //# Data Members
  private static StepSimplifierFactory mInstance = null;

}
