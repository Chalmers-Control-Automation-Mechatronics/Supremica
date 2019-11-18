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
import net.sourceforge.waters.analysis.options.DoubleOption;
import net.sourceforge.waters.analysis.options.EnumOption;
import net.sourceforge.waters.analysis.options.OptionMap;
import net.sourceforge.waters.analysis.options.PositiveIntOption;
import net.sourceforge.waters.analysis.options.PropositionOption;


/**
 * A factory class to provide access to all transition relation simplifiers
 * ({@link TransitionRelationSimplifier}) and their options. Used for
 * creating GUI.
 *
 * @author Benjamin Wheeler
 */

public class TRSimplifierFactory
{

  //#########################################################################
  //# Constructor
  private TRSimplifierFactory()
  {
    registerSimplifierCreators();
  }


  //#########################################################################
  //# Options
  public void registerOptions(final OptionMap db)
  {
    db.add(new PositiveIntOption
             (OPTION_Abstract_StateLimit,
              "State Limit",
              "The maximum number of states that can be constructed " +
              "before aborting.",
              "-slimit"));
    db.add(new PositiveIntOption
             (OPTION_Abstract_TransitionLimit,
              "Transition Limit",
              "The maximum number of transitions that can be constructed " +
              "before aborting.",
              "-tlimit"));

    db.add(new BooleanOption
             (OPTION_TransitionRelationSimplifier_DumpStateAware,
              "Dump-state aware",
              "Do not explore beyond dump states, and ignore dump states " +
              "to determine whether an event is all selfloops and can be " +
              "removed.",
              "-dp",
              false));

    db.add(new PropositionOption
             (OPTION_AbstractMarking_DefaultMarkingID,
              "Marking proposition",
              "Default marking used for nonblocking verification or synthesis.",
              "-marking",
              PropositionOption.DefaultKind.DEFAULT_NULL));
    db.add(new PropositionOption
             (OPTION_AbstractMarking_PreconditionMarkingID,
              "Precondition marking",
              "Precondition marking used for generalised conflict check.",
              "-premarking",
              PropositionOption.DefaultKind.ALLOW_NULL));

    db.add(new EnumOption<ObservationEquivalenceTRSimplifier.Equivalence>
             (OPTION_ObservationEquivalence_Equivalence,
              "Equivalence",
              "The equivalence relation that defines which states can be merged.",
              "-equiv",
              ObservationEquivalenceTRSimplifier.Equivalence.values()));
    db.add(new EnumOption<ObservationEquivalenceTRSimplifier.TransitionRemoval>
             (OPTION_ObservationEquivalence_TransitionRemovalMode,
              "Transition Removal Mode",
              "The times at which to remove redundant transitions based " +
              "on observation equicvalence.",
              "-trm",
              ObservationEquivalenceTRSimplifier.TransitionRemoval.values()));
    db.add(new EnumOption<ObservationEquivalenceTRSimplifier.MarkingMode>
             (OPTION_ObservationEquivalence_MarkingMode,
              "Marking Mode",
              "How markings are handled when minimising for " +
              "observation equivalence.",
              "-mm",
              ObservationEquivalenceTRSimplifier.MarkingMode.values()));
    db.add(new BooleanOption
             (OPTION_ObservationEquivalence_InfoEnabled,
              "Use Info Data Structure",
              "Use the data structures proposed by Fernandez " +
              "that ensure an O(n log n) runtime but require more memory.",
              "-eqinfo",
              false));
    db.add(new BooleanOption
             (OPTION_ObservationEquivalence_UsingLocalEvents,
              "Use Local Events",
              "Consider all local events as silent " +
              "(as opposed to only the special event TAU).",
              "-eqlocal",
              false));

    db.add(new BooleanOption
             (OPTION_SubsetConstruction_FailingEventsAsSelfLoops,
              "Failing events as selfloops",
              "Enable this to create selfloops for failing events, " +
              "disable to create transitions to the dump state instead.",
              "-fesl",
              false));
    db.add(new DoubleOption
             (OPTION_SubsetConstruction_MaxIncrease,
              "Max Increase",
              "The maximum factor by which the number of states may increase " +
              "before aborting.",
              "-maxinc",
              Double.POSITIVE_INFINITY, 1.0, Double.POSITIVE_INFINITY));
  }


  //#########################################################################
  //# Auxiliary Methods
  private void registerSimplifierCreators()
  {
    mToolCreators.add(new TRSimplifierCreator("Partition Refinement",
      "Perform automaton minimisation by partition refinement," +
      "such as Hopcroft's minimisation algorithm or bisimulation.") {
      @Override
      protected TransitionRelationSimplifier createTRSimplifier()
      {
        return new ObservationEquivalenceTRSimplifier();
      }
    });
    mToolCreators.add(new TRSimplifierCreator("Subset Construction",
      "Make a nondeterministic automaton deterministic using the " +
      "subset construction algorithm.") {
      @Override
      protected TransitionRelationSimplifier createTRSimplifier()
      {
        return new SubsetConstructionTRSimplifier();
      }
    });
    mToolCreators.add(new TRSimplifierCreator("Hide Events",
      "Replace local events by the silent event. " +
      "May also perform other special event simplification.") {
      @Override
      protected TransitionRelationSimplifier createTRSimplifier()
      {
        return new SpecialEventsTRSimplifier();
      }
    });
  }

  public List<TRSimplifierCreator> getSimplifierCreators()
  {
    return mToolCreators;
  }

  public static TRSimplifierFactory getInstance()
  {
    if (mInstance == null) {
      mInstance = new TRSimplifierFactory();
    }
    return mInstance;
  }


  //#########################################################################
  //# Data Members
  private final List<TRSimplifierCreator> mToolCreators = new ArrayList<>();
  private static TRSimplifierFactory mInstance = null;


  //#########################################################################
  //# Class Constants
  public static final String OPTION_Abstract_StateLimit =
    "AbstractTRSimplifier.StateLimit";
  public static final String OPTION_Abstract_TransitionLimit =
    "AbstractTRSimplifier.TransitionLimit";

  public static final String OPTION_TransitionRelationSimplifier_DumpStateAware =
    "TransitionRelationSimplifier.DumpStateAware";

  public static final String OPTION_SubsetConstruction_MaxIncrease =
    "SubsetConstructionTRSimplifier.MaxIncrease";
  public static final String OPTION_SubsetConstruction_FailingEventsAsSelfLoops =
    "SubsetConstructionTRSimplifier.FailingEventsAsSelfLoops";
  public static final String OPTION_AbstractMarking_PreconditionMarkingID =
    "AbstractMarkingTRSimplifier.PreconditionID";
  public static final String OPTION_AbstractMarking_DefaultMarkingID =
    "AbstractMarkingTRSimplifier.DefaultID";

  public static final String OPTION_ObservationEquivalence_Equivalence =
    "ObservationEquivalenceSimplifier.Equivalence";
  public static final String OPTION_ObservationEquivalence_TransitionRemovalMode =
    "ObservationEquivalenceSimplifier.TransitionRemovalMode";
  public static final String OPTION_ObservationEquivalence_MarkingMode =
    "ObservationEquivalenceSimplifier.MarkingMode";
  //TODO Proposition mask?
  public static final String OPTION_ObservationEquivalence_UsingLocalEvents =
    "ObservationEquivalenceSimplifier.UsingLocalEvents";
  public static final String OPTION_ObservationEquivalence_InfoEnabled =
    "ObservationEquivalenceSimplifier.InfoEnabled";

}
