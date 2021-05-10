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

package net.sourceforge.waters.analysis.abstraction;

import net.sourceforge.waters.analysis.options.BooleanOption;
import net.sourceforge.waters.analysis.options.DoubleOption;
import net.sourceforge.waters.analysis.options.EnumOption;
import net.sourceforge.waters.analysis.options.EventSetOption;
import net.sourceforge.waters.analysis.options.OptionPage;
import net.sourceforge.waters.analysis.options.PositiveIntOption;
import net.sourceforge.waters.analysis.options.PropositionOption;


/**
 * A factory class to provide access to all transition relation simplifiers
 * ({@link TransitionRelationSimplifier}) and their options. Used for
 * creating GUI.
 *
 * @author Benjamin Wheeler
 */

public abstract class TRSimplifierFactory extends AutomatonSimplifierFactory
{

  //#########################################################################
  //# Constructor
  public TRSimplifierFactory()
  {
    super();
  }

  //#########################################################################
  //# Options
  @Override
  public void registerOptions(final OptionPage db)
  {
    super.registerOptions(db);
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
              PropositionOption.DefaultKind.PREVENT_NULL));
    db.add(new PropositionOption
             (OPTION_AbstractMarking_PreconditionMarkingID,
              "Precondition marking",
              "Precondition marking used for generalised conflict check.",
              "-premarking",
              PropositionOption.DefaultKind.DEFAULT_NULL));

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
    db.add(new EventSetOption
             (OPTION_ObservationEquivalence_PropositionMask,
              "Propositions",
              "Propositions to be preserved by the equivalence.",
              "-props",
              EventSetOption.DefaultKind.PROPOSITION,
              "Selected Propositions",
              "Unselected Propositions"));
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
              "Consider all local events as silent in observation equivalence " +
              "(as opposed to only the special event TAU).",
              "-eqlocal",
              false));

    db.add(new BooleanOption
             (OPTION_SilentIncoming_RestrictsToUnreachableStates,
              "Ensure reduction with Silent Incoming Rule",
              "Apply the Silent Incoming Rule only to &tau;-transitions " +
              "that lead to a state that becomes unreachable by application " +
              "of the rule.",
              "-rtus",
              true));

    db.add(new EnumOption<SuWonhamSupervisorReductionTRSimplifier.PairOrdering>
             (OPTION_SuWonhamSupervisorReduction_PairOrdering,
               "Pair ordering",
               "The strategy to determine which pairs are merged first " +
               "by Su/Wonham suoervisor reduction.",
               "-por",
               SuWonhamSupervisorReductionTRSimplifier.PairOrdering.values()));

    db.add(new BooleanOption
             (OPTION_SubsetConstruction_FailingEventsAsSelfLoops,
              "Failing events as selfloops",
              "Enable this to create selfloops for failing events, " +
              "disable to create transitions to the dump state instead.",
              "-fesl",
              false));
    db.add(new DoubleOption
             (OPTION_SubsetConstruction_MaxIncrease,
              "Maximum Increase",
              "The maximum factor by which the number of states may increase " +
              "before aborting.",
              "-maxinc",
              Double.POSITIVE_INFINITY, 1.0, Double.POSITIVE_INFINITY));

    db.add(new BooleanOption
             (OPTION_SynthesisObservationEquivalence_UsesWeakSynthesisObservationEquivalence,
              "Weak Synthesis Observation Equivalence",
              "Use weak synthesis observation equivalence rather than " +
              "synthesis observation equivalence.",
              "-wsoe",
              true));
  }

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
    "ObservationEquivalenceTRSimplifier.Equivalence";
  public static final String OPTION_ObservationEquivalence_TransitionRemovalMode =
    "ObservationEquivalenceTRSimplifier.TransitionRemovalMode";
  public static final String OPTION_ObservationEquivalence_MarkingMode =
    "ObservationEquivalenceTRSimplifier.MarkingMode";
  public static final String OPTION_ObservationEquivalence_PropositionMask =
    "ObservationEquivalenceTRSimplifier.PropositionMask";
  public static final String OPTION_ObservationEquivalence_UsingLocalEvents =
    "ObservationEquivalenceTRSimplifier.UsingLocalEvents";
  public static final String OPTION_ObservationEquivalence_InfoEnabled =
    "ObservationEquivalenceTRSimplifier.InfoEnabled";

  public static final String OPTION_SilentIncoming_RestrictsToUnreachableStates =
    "SilentIncomingTRSimplifier.RestrictsToUnreachableStates";

  public static final String OPTION_SpecialEvents_LocalEvents =
    "SpecialEventsTRSimplifier.LocalEvents";

  public static final String OPTION_SuWonhamSupervisorReduction_PairOrdering =
    "SuWonhamSupervisorReductionTRSimplifier.PairOrdering";

  public static final String OPTION_SynthesisObservationEquivalence_UsesWeakSynthesisObservationEquivalence =
    "SynthesisObservationEquivalenceTRSimplifier.UsesWeakSynthesisObservationEquivalence";
}
