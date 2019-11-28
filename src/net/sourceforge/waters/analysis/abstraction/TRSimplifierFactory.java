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

import net.sourceforge.waters.analysis.options.BooleanOption;
import net.sourceforge.waters.analysis.options.DoubleOption;
import net.sourceforge.waters.analysis.options.EnumOption;
import net.sourceforge.waters.analysis.options.EventSetOption;
import net.sourceforge.waters.analysis.options.OptionMap;
import net.sourceforge.waters.analysis.options.PositiveIntOption;
import net.sourceforge.waters.analysis.options.PropositionOption;
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

public class TRSimplifierFactory extends AutomatonSimplifierFactory
{

  //#########################################################################
  //# Constructor
  private TRSimplifierFactory()
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
    db.add(new BooleanOption
             (OPTION_AutomatonSimplifierFactory_KeepOriginal,
              "Keep Original",
              "Do not remove the input automaton from the analyzer " +
              "after this operation.",
              "-keep",
              true));

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

  public static TRSimplifierFactory getInstance()
  {
    if (mInstance == null) {
      mInstance = new TRSimplifierFactory();
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
  public static final String OPTION_ObservationEquivalence_PropositionMask =
    "ObservationEquivalenceSimplifier.PropositionMask";
  public static final String OPTION_ObservationEquivalence_UsingLocalEvents =
    "ObservationEquivalenceSimplifier.UsingLocalEvents";
  public static final String OPTION_ObservationEquivalence_InfoEnabled =
    "ObservationEquivalenceSimplifier.InfoEnabled";

  public static final String OPTION_SpecialEvents_LocalEvents =
    "SpecialEventsTRSimplifier.LocalEvents";

  public static final String OPTION_SynthesisObservationEquivalence_UsesWeakSynthesisObservationEquivalence =
    "SynthesisObservationEquivalenceTRSimplifier.UsesWeakSynthesisObservationEquivalence";
}
