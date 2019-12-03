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

package net.sourceforge.waters.analysis.trcomp;

import static net.sourceforge.waters.analysis.abstraction.TRSimplifierFactory.OPTION_AbstractMarking_DefaultMarkingID;
import static net.sourceforge.waters.analysis.abstraction.TRSimplifierFactory.OPTION_AbstractMarking_PreconditionMarkingID;
import static net.sourceforge.waters.analysis.abstraction.TRSimplifierFactory.OPTION_Abstract_StateLimit;
import static net.sourceforge.waters.analysis.abstraction.TRSimplifierFactory.OPTION_Abstract_TransitionLimit;
import static net.sourceforge.waters.analysis.abstraction.TRSimplifierFactory.OPTION_ObservationEquivalence_Equivalence;
import static net.sourceforge.waters.analysis.abstraction.TRSimplifierFactory.OPTION_ObservationEquivalence_InfoEnabled;
import static net.sourceforge.waters.analysis.abstraction.TRSimplifierFactory.OPTION_ObservationEquivalence_MarkingMode;
import static net.sourceforge.waters.analysis.abstraction.TRSimplifierFactory.OPTION_ObservationEquivalence_PropositionMask;
import static net.sourceforge.waters.analysis.abstraction.TRSimplifierFactory.OPTION_ObservationEquivalence_TransitionRemovalMode;
import static net.sourceforge.waters.analysis.abstraction.TRSimplifierFactory.OPTION_ObservationEquivalence_UsingLocalEvents;
import static net.sourceforge.waters.analysis.abstraction.TRSimplifierFactory.OPTION_SubsetConstruction_FailingEventsAsSelfLoops;
import static net.sourceforge.waters.analysis.abstraction.TRSimplifierFactory.OPTION_SubsetConstruction_MaxIncrease;
import static net.sourceforge.waters.analysis.abstraction.TRSimplifierFactory.OPTION_SynthesisObservationEquivalence_UsesWeakSynthesisObservationEquivalence;
import static net.sourceforge.waters.analysis.abstraction.TRSimplifierFactory.OPTION_TransitionRelationSimplifier_DumpStateAware;

import java.util.List;

import net.sourceforge.waters.analysis.abstraction.AutomatonSimplifierCreator;
import net.sourceforge.waters.analysis.abstraction.AutomatonSimplifierFactory;
import net.sourceforge.waters.analysis.abstraction.ChainTRSimplifier;
import net.sourceforge.waters.analysis.abstraction.ObservationEquivalenceTRSimplifier;
import net.sourceforge.waters.analysis.abstraction.ObservationEquivalenceTRSimplifier.Equivalence;
import net.sourceforge.waters.analysis.abstraction.TransitionRelationSimplifier;
import net.sourceforge.waters.analysis.options.BooleanOption;
import net.sourceforge.waters.analysis.options.DoubleOption;
import net.sourceforge.waters.analysis.options.EnumOption;
import net.sourceforge.waters.analysis.options.EventSetOption;
import net.sourceforge.waters.analysis.options.Option;
import net.sourceforge.waters.analysis.options.OptionMap;
import net.sourceforge.waters.analysis.options.PositiveIntOption;
import net.sourceforge.waters.analysis.options.PropositionOption;
import net.sourceforge.waters.model.analysis.AnalysisConfigurationException;
import net.sourceforge.waters.model.analysis.des.AutomatonBuilder;
import net.sourceforge.waters.model.base.WatersRuntimeException;
import net.sourceforge.waters.model.des.ProductDESProxyFactory;


/**
 * A factory class to provide access to all transition relation simplifiers
 * ({@link TransitionRelationSimplifier}) and their options. Used for
 * creating GUI.
 *
 * @author Benjamin Wheeler
 */

public class ChainSimplifierFactory extends AutomatonSimplifierFactory
{

  //#########################################################################
  //# Constructor
  private ChainSimplifierFactory()
  {
    super();
  }

  @Override
  public String toString()
  {
    return "Chain Relation Simplifiers";
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


    db.add(new BooleanOption(OPTION_ChainSimplifierFactory_ConfiguredPreconditionMarking,
              "Configured Precondition Marking",
              "",
              "",
              false));
  }


  //#########################################################################
  //# Auxiliary Methods
  @Override
  protected void registerSimplifierCreators()
  {
    final List<AutomatonSimplifierCreator> creators =  getSimplifierCreators();
    creators.add(new ChainSimplifierCreator("OEQ", "") {
      @Override
      public ChainTRSimplifier create()
      {
        return ChainBuilder.createObservationEquivalenceChain
          (ObservationEquivalenceTRSimplifier.
           Equivalence.OBSERVATION_EQUIVALENCE);
      }
    });
    creators.add(new ChainSimplifierCreator("WOEQ", "") {
      @Override
      public ChainTRSimplifier create()
      {
        return ChainBuilder.createObservationEquivalenceChain
          (ObservationEquivalenceTRSimplifier.
           Equivalence.WEAK_OBSERVATION_EQUIVALENCE);
      }
    });
    creators.add(new ConflictEquivalenceSimplifierCreator
                 ("NB0",
                  "",
                  Equivalence.OBSERVATION_EQUIVALENCE,
                  true, false, false, false));
    creators.add(new ConflictEquivalenceSimplifierCreator
                 ("NB0w",
                  "",
                  Equivalence.WEAK_OBSERVATION_EQUIVALENCE,
                  true, false, false, false));
    creators.add(new ConflictEquivalenceSimplifierCreator
                 ("NB1",
                  "",
                  Equivalence.OBSERVATION_EQUIVALENCE,
                  true, true, false, false));
    creators.add(new ConflictEquivalenceSimplifierCreator
                 ("NB1w",
                  "",
                  Equivalence.WEAK_OBSERVATION_EQUIVALENCE,
                  true, true, false, false));
    creators.add(new ConflictEquivalenceSimplifierCreator
                 ("NBx",
                  "",
                  Equivalence.OBSERVATION_EQUIVALENCE,
                  false, true, false, false));
    creators.add(new ConflictEquivalenceSimplifierCreator
                 ("NB2",
                  "",
                  Equivalence.OBSERVATION_EQUIVALENCE,
                  true, true, false, true));
    creators.add(new ConflictEquivalenceSimplifierCreator
                 ("NB2w",
                  "",
                  Equivalence.WEAK_OBSERVATION_EQUIVALENCE,
                  true, true, false, true));
    creators.add(new ConflictEquivalenceSimplifierCreator
                 ("NB3",
                  "",
                  Equivalence.OBSERVATION_EQUIVALENCE,
                  true, true, true, true));
    creators.add(new ConflictEquivalenceSimplifierCreator
                 ("NB3w",
                  "",
                  Equivalence.WEAK_OBSERVATION_EQUIVALENCE,
                  true, true, true, true));
    creators.add(new GNBSimplifierCreator
                 ("GNB", "", Equivalence.OBSERVATION_EQUIVALENCE, true));
    creators.add(new GNBSimplifierCreator
                 ("GNBw", "", Equivalence.WEAK_OBSERVATION_EQUIVALENCE, true));
  }

  public static ChainSimplifierFactory getInstance()
  {
    if (mInstance == null) {
      mInstance = new ChainSimplifierFactory();
    }
    return mInstance;
  }

  private class GNBSimplifierCreator extends AutomatonSimplifierCreator {

    protected GNBSimplifierCreator(final String name, final String description,
                                   final Equivalence equivalence,
                                   final boolean earlyTransitionRemoval)
    {
      super(name, description);
      mEquivalence = equivalence;
      mEarlyTransitionRemoval = earlyTransitionRemoval;
    }

    @Override
    public List<Option<?>> getOptions(final OptionMap db)
    {
      final List<Option<?>> options = super.getOptions(db);
      db.append(options, ChainSimplifierFactory.
                     OPTION_ChainSimplifierFactory_ConfiguredPreconditionMarking);
      return options;
    }

    @Override
    public void setOption(final Option<?> option)
    {
      if (option.hasID(OPTION_ChainSimplifierFactory_ConfiguredPreconditionMarking)) {
        final BooleanOption propOption = (BooleanOption) option;
        mConfiguredPreconditionMarking = propOption.getValue();
      } else {
        super.setOption(option);
      }
    }

    @Override
    public AutomatonBuilder createBuilder(final ProductDESProxyFactory factory) {
      final ChainTRSimplifier chain = ChainBuilder.createGeneralisedNonblockingChain
      (mEquivalence, mEarlyTransitionRemoval, mConfiguredPreconditionMarking);
      return new ChainAutomatonBuilder(factory, chain);

    }

    private final Equivalence mEquivalence;
    private final boolean mEarlyTransitionRemoval;
    private boolean mConfiguredPreconditionMarking = false;

  }

  private abstract class ChainSimplifierCreator extends AutomatonSimplifierCreator {

    protected ChainSimplifierCreator(final String name, final String description)
    {
      super(name, description);
    }

    @Override
    public AutomatonBuilder createBuilder(final ProductDESProxyFactory factory)
    {
      return new ChainAutomatonBuilder(factory, create());
    }

    public abstract ChainTRSimplifier create();

  }

  private class ConflictEquivalenceSimplifierCreator extends AutomatonSimplifierCreator {

    protected ConflictEquivalenceSimplifierCreator(final String name, final String description,
                                   final Equivalence equivalence,
                                   final boolean certainConflicts,
                                   final boolean earlyTransitionRemoval,
                                   final boolean selfloopSubsumption,
                                   final boolean nonAlphaDeterminisation)
    {
      super(name, description);
      mEquivalence = equivalence;
      mCertainConflicts = certainConflicts;
      mEarlyTransitionRemoval = earlyTransitionRemoval;
      mSelfloopSubsumption = selfloopSubsumption;
      mNonAlphaDeterminisation = nonAlphaDeterminisation;
    }

    @Override
    public AutomatonBuilder createBuilder(final ProductDESProxyFactory factory)
    {
      try {
        final ChainTRSimplifier simp = ChainBuilder.createConflictEquivalenceChain
          (mEquivalence,
            mCertainConflicts,
            mEarlyTransitionRemoval,
            mSelfloopSubsumption,
            mNonAlphaDeterminisation);
        return new ChainAutomatonBuilder(factory, simp);
      } catch(final AnalysisConfigurationException e) {
        //TODO
        throw new WatersRuntimeException(e);
      }

    }

    final Equivalence mEquivalence;
    final boolean mCertainConflicts;
    final boolean mEarlyTransitionRemoval;
    final boolean mSelfloopSubsumption;
    final boolean mNonAlphaDeterminisation;

  }




  //#########################################################################
  //# Data Members
  private static ChainSimplifierFactory mInstance = null;


  //#########################################################################
  //# Class Constants
  public static final String OPTION_ChainSimplifierFactory_ConfiguredPreconditionMarking =
    "ChainSimplifierFactory.ConfiguredPreconditionMarking";

}
