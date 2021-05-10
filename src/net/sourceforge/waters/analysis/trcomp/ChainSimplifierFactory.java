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

package net.sourceforge.waters.analysis.trcomp;

import java.util.List;

import net.sourceforge.waters.analysis.abstraction.AutomatonSimplifierCreator;
import net.sourceforge.waters.analysis.abstraction.ChainTRSimplifier;
import net.sourceforge.waters.analysis.abstraction.OPSearchAutomatonSimplifier;
import net.sourceforge.waters.analysis.abstraction.ObservationEquivalenceTRSimplifier.Equivalence;
import net.sourceforge.waters.analysis.abstraction.TRSimplifierFactory;
import net.sourceforge.waters.analysis.abstraction.TransitionRelationSimplifier;
import net.sourceforge.waters.analysis.options.BooleanOption;
import net.sourceforge.waters.analysis.options.EnumOption;
import net.sourceforge.waters.analysis.options.EventSetOption;
import net.sourceforge.waters.analysis.options.EventSetOption.DefaultKind;
import net.sourceforge.waters.analysis.options.FileOption;
import net.sourceforge.waters.analysis.options.Option;
import net.sourceforge.waters.analysis.options.OptionPage;
import net.sourceforge.waters.analysis.options.PropositionOption;
import net.sourceforge.waters.analysis.tr.TRAutomatonBuilder;
import net.sourceforge.waters.model.analysis.AnalysisConfigurationException;
import net.sourceforge.waters.model.analysis.des.AutomatonBuilder;
import net.sourceforge.waters.model.analysis.kindtranslator.IdenticalKindTranslator;
import net.sourceforge.waters.model.base.WatersRuntimeException;
import net.sourceforge.waters.model.des.ProductDESProxyFactory;


/**
 * A factory class to provide access to all transition relation simplifiers
 * ({@link TransitionRelationSimplifier}) and their options. Used for
 * creating GUI.
 *
 * @author Benjamin Wheeler
 */

public class ChainSimplifierFactory extends TRSimplifierFactory
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
    return "Waters Simplifier Chains";
  }


  //#########################################################################
  //# Options
  @Override
  public void registerOptions(final OptionPage db)
  {
    super.registerOptions(db);
    db.add(new BooleanOption
             (OPTION_ChainSimplifierFactory_WeakObservationEquivalence,
              "Use Weak Observation Equivalence",
              "Use weak observation equivalence rather than ordinary " +
              "observation equivalence.",
              "-woeq",
              false));
    db.add(new BooleanOption
             (OPTION_ChainSimplifierFactory_LimitedCertainConflicts,
              "Use Limited Certain Conflicts",
              "Include the Limited Certain Conflict Rule " +
              "in the abstraction sequence.",
              "-lcc",
              true));


    //TODO Descriptions and command line options
    db.add(new EnumOption<OPSearchAutomatonSimplifier.Mode>
           (OPTION_OPSearchAutomatonSimplifier_OperationMode,
            "Operation Mode",
            "",
            "",
            OPSearchAutomatonSimplifier.Mode.values(),
            OPSearchAutomatonSimplifier.Mode.MINIMIZE));
    db.add(new EventSetOption
           (OPTION_OPSearchAutomatonSimplifier_HiddenEvents,
            "Hidden Events",
            "",
            "",
            DefaultKind.PROPER_EVENT,
            "Selected Events",
            "Unselected Events"));
    db.add(new EventSetOption
           (OPTION_OPSearchAutomatonSimplifier_Propositions,
            "Propositions",
            "",
            "",
            DefaultKind.PROPOSITION,
            "Selected Propositions",
            "Unselected Propositions"));
    //TODO Needs single event option (generalise proposition option?)
//    db.add(new ...Option
//           (OPTION_OPSearchAutomatonSimplifier_OutputHiddenEvent,
//            "Output Hidden Event",
//            "",
//            "",
//            ...));
    db.add(new FileOption
           (OPTION_OPSearchAutomatonSimplifier_LogFile,
            "Log File",
            "",
            ""));
  }


  //#########################################################################
  //# Auxiliary Methods
  @Override
  protected void registerSimplifierCreators()
  {
    final List<AutomatonSimplifierCreator> creators =  getSimplifierCreators();
    creators.add(new ChainSimplifierCreator
                   ("OEQ",
                    "An abstraction sequence for efficient simplification by " +
                    "observation equivalence or weak observation equivalence, " +
                    "consisting of special events removal, and &tau;-loop removal " +
                    "before invoking observation equivalence or weak observation " +
                    "equivalence.") {
      @Override
      public ChainTRSimplifier create()
      {
        return ChainBuilder.createObservationEquivalenceChain
          (getEquivalence());
      }
    });
    creators.add(new ConflictEquivalenceSimplifierCreator
                   ("NB0",
                    "An abstraction sequence for standard nonblocking " +
                    "verification, consisting of special events removal, " +
                    "&tau;-loop removal, marking removal, Silent Incoming Rule, " +
                    "Only Silent Outgoing Rule, Incoming equivalence, combined " +
                    "Silent Continuation and Active Events Rules, optionally " +
                    "Limited Certain Conflicts Rule, observation equivalence " +
                    "or weak observation equivalence, and marking saturation.",
                    false, false, false));
    creators.add(new ConflictEquivalenceSimplifierCreator
                   ("NB1",
                    "An abstraction sequence for standard nonblocking " +
                    "verification, consisting of special events removal, " +
                    "&tau;-loop removal, observation equivalent transition " +
                    "removal, marking removal, Silent Incoming Rule, " +
                    "Only Silent Outgoing Rule, Incoming equivalence, combined " +
                    "Silent Continuation and Active Events Rules, optionally " +
                    "Limited Certain Conflicts Rule, observation equivalence " +
                    "or weak observation equivalence, and marking saturation.",
                    true, false, false));
    creators.add(new ConflictEquivalenceSimplifierCreator
                   ("NB2",
                    "An abstraction sequence for standard nonblocking " +
                    "verification, consisting of special events removal, " +
                    "&tau;-loop removal, observation equivalent transition " +
                    "removal, marking removal, Silent Incoming Rule, " +
                    "Only Silent Outgoing Rule, Incoming equivalence, combined " +
                    "Silent Continuation and Active Events Rules, optionally " +
                    "Limited Certain Conflicts Rule, observation equivalence " +
                    "or weak observation equivalence, non-&alpha;-determinisation, " +
                    "and marking saturation.",
                    true, false, true));
    creators.add(new ConflictEquivalenceSimplifierCreator
                   ("NB3",
                    "An abstraction sequence for standard nonblocking " +
                    "verification, consisting of special events removal, " +
                    "&tau;-loop removal, observation equivalent transition " +
                    "removal, selfloop subsumption, marking removal, " +
                    "Silent Incoming Rule, Only Silent Outgoing Rule, " +
                    "Incoming equivalence, combined Silent Continuation and " +
                    "Active Events Rules, optionally Limited Certain Conflicts " +
                    "Rule, observation equivalence or weak observation " +
                    "equivalence, non-&alpha;-determinisation, and " +
                    "marking saturation.",
                    true, true, true));

    creators.add(new GNBSimplifierCreator
                   ("GNB",
                    "An abstraction sequence for generalised nonblocking " +
                    "verification, consisting of special events removal, " +
                    "&tau;-loop removal, marking removal, &omega;-removal, " +
                    "Silent Incoming Rule, Only Silent Outgoing Rule, " +
                    "observation equivalence or weak observation equivalence, " +
                    "non-&alpha; determinisation, &alpha;-determinisation, " +
                    "and marking saturation.",
                    true));
    creators.add(new ChainSimplifierCreator
                   ("Language Equivalence",
                    "Compute the minimal deterministic automaton representing " +
                    "the same language as the automaton being simplified. " +
                    "It is computed by a sequence of &tau;-loop removal, " +
                    "subset construction, and Hopcroft's minimisation " +
                    "algorithm.") {
      @Override
      public ChainTRSimplifier create()
      {
        return ChainBuilder.createProjectionChain();
      }
    });
    creators.add(new AutomatonSimplifierCreator
                   ("OP-Search",
                    "Use the OP-search algorithm to compute a subset of the " +
                    "local events of the automaton that satisfies the " +
                    "observer property, and simplify the automaton by " +
                    "projecting out these event.") {
      @Override
      public AutomatonBuilder createBuilder(final ProductDESProxyFactory factory)
      {
        return new OPSearchAutomatonSimplifier(factory, IdenticalKindTranslator.getInstance());
      }
    });
  }

  public static ChainSimplifierFactory getInstance()
  {
    if (mInstance == null) {
      mInstance = new ChainSimplifierFactory();
    }
    return mInstance;
  }



  private class GNBSimplifierCreator extends ChainSimplifierCreator {

    protected GNBSimplifierCreator(final String name, final String description,
                                   final boolean earlyTransitionRemoval)
    {
      super(name, description);
      mEarlyTransitionRemoval = earlyTransitionRemoval;
    }

    @Override
    public void setOption(final Option<?> option)
    {
      if (option.hasID(OPTION_AbstractMarking_PreconditionMarkingID)) {
        final PropositionOption propOption = (PropositionOption) option;
        mSupportsGeneralisedNonBlocking = propOption.getValue() != null;
      }
      super.setOption(option);

    }

    @Override
    public ChainTRSimplifier create()
    {
      final ChainTRSimplifier chain = ChainBuilder.createGeneralisedNonblockingChain
        (getEquivalence(), mEarlyTransitionRemoval, mSupportsGeneralisedNonBlocking);
      return chain;
    }

    private final boolean mEarlyTransitionRemoval;
    private boolean mSupportsGeneralisedNonBlocking;

  }



  private abstract class ChainSimplifierCreator extends AutomatonSimplifierCreator {

    protected ChainSimplifierCreator(final String name, final String description)
    {
      super(name, description);
    }

    public Equivalence getEquivalence()
    {
      return mEquivalence;
    }

    @Override
    public List<Option<?>> getOptions(final OptionPage db)
    {
      final List<Option<?>> options = super.getOptions(db);
      db.append(options, OPTION_ChainSimplifierFactory_WeakObservationEquivalence);
      return options;
    }

    @Override
    public void setOption(final Option<?> option)
    {
      if (option.hasID(OPTION_ChainSimplifierFactory_WeakObservationEquivalence)) {
        final BooleanOption propOption = (BooleanOption) option;
        mEquivalence = propOption.getValue() ? Equivalence.WEAK_OBSERVATION_EQUIVALENCE
          : Equivalence.OBSERVATION_EQUIVALENCE;
      } else {
        super.setOption(option);
      }
    }

    @Override
    public AutomatonBuilder createBuilder(final ProductDESProxyFactory factory)
    {
      return new TRAutomatonBuilder(factory, create());
    }

    public abstract ChainTRSimplifier create();


    private Equivalence mEquivalence;

  }



  private class ConflictEquivalenceSimplifierCreator extends ChainSimplifierCreator {

    protected ConflictEquivalenceSimplifierCreator(final String name, final String description,
                                   final boolean earlyTransitionRemoval,
                                   final boolean selfloopSubsumption,
                                   final boolean nonAlphaDeterminisation)
    {
      super(name, description);
      mEarlyTransitionRemoval = earlyTransitionRemoval;
      mSelfloopSubsumption = selfloopSubsumption;
      mNonAlphaDeterminisation = nonAlphaDeterminisation;
    }

    @Override
    public List<Option<?>> getOptions(final OptionPage db)
    {
      final List<Option<?>> options = super.getOptions(db);
      db.append(options, OPTION_ChainSimplifierFactory_LimitedCertainConflicts);
      return options;
    }

    @Override
    public void setOption(final Option<?> option)
    {
      if (option.hasID(OPTION_ChainSimplifierFactory_LimitedCertainConflicts)) {
        final BooleanOption propOption = (BooleanOption) option;
        mCertainConflicts = propOption.getValue();
      } else {
        super.setOption(option);
      }
    }

    @Override
    public ChainTRSimplifier create()
    {
      try {
        final ChainTRSimplifier simp = ChainBuilder.createConflictEquivalenceChain
          (getEquivalence(),
            mCertainConflicts,
            mEarlyTransitionRemoval,
            mSelfloopSubsumption,
            mNonAlphaDeterminisation);
        return simp;
      } catch (final AnalysisConfigurationException exception) {
        //TODO Declare to throw AnalysisConfigurationException, catch in GUI
        throw new WatersRuntimeException(exception);
      }
    }

    private boolean mCertainConflicts;
    private final boolean mEarlyTransitionRemoval;
    private final boolean mSelfloopSubsumption;
    private final boolean mNonAlphaDeterminisation;

  }


  //#########################################################################
  //# Data Members
  private static ChainSimplifierFactory mInstance = null;


  //#########################################################################
  //# Class Constants
  public static final String OPTION_ChainSimplifierFactory_LimitedCertainConflicts =
    "ChainSimplifierFactory.LimitedCertainConflicts";
  public static final String OPTION_ChainSimplifierFactory_WeakObservationEquivalence =
    "ChainSimplifierFactory.WeakObservationEquivalence";

  public static final String OPTION_OPSearchAutomatonSimplifier_OperationMode =
    "ChainSimplifierFactory.OperationMode";
  public static final String OPTION_OPSearchAutomatonSimplifier_HiddenEvents =
    "ChainSimplifierFactory.HiddenEvents";
  public static final String OPTION_OPSearchAutomatonSimplifier_Propositions =
    "ChainSimplifierFactory.Propositions";
  public static final String OPTION_OPSearchAutomatonSimplifier_OutputHiddenEvent =
    "ChainSimplifierFactory.OutputHiddenEvent";
  public static final String OPTION_OPSearchAutomatonSimplifier_LogFile =
    "ChainSimplifierFactory.LogFile";

}
