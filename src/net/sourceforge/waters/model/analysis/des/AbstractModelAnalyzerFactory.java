//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# Copyright (C) 2004-2020 Robi Malik
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

package net.sourceforge.waters.model.analysis.des;

import java.util.Collection;
import java.util.ListIterator;

import net.sourceforge.waters.analysis.abstraction.SupervisorReductionMainMethod;
import net.sourceforge.waters.analysis.abstraction.SupervisorReductionProjectionMethod;
import net.sourceforge.waters.analysis.distributed.DistributedModelVerifierFactory;
import net.sourceforge.waters.analysis.options.BooleanOption;
import net.sourceforge.waters.analysis.options.ComponentKindOption;
import net.sourceforge.waters.analysis.options.Configurable;
import net.sourceforge.waters.analysis.options.EnumOption;
import net.sourceforge.waters.analysis.options.EventSetOption;
import net.sourceforge.waters.analysis.options.FlagOption;
import net.sourceforge.waters.analysis.options.LeafOptionPage;
import net.sourceforge.waters.analysis.options.Option;
import net.sourceforge.waters.analysis.options.OptionPage;
import net.sourceforge.waters.analysis.options.PositiveIntOption;
import net.sourceforge.waters.analysis.options.PropositionOption;
import net.sourceforge.waters.analysis.options.StringListOption;
import net.sourceforge.waters.analysis.options.StringOption;
import net.sourceforge.waters.model.analysis.AnalysisConfigurationException;
import net.sourceforge.waters.model.analysis.AnalysisException;
import net.sourceforge.waters.model.analysis.cli.CommandLineArgument;
import net.sourceforge.waters.model.analysis.cli.CommandLineOptionContext;
import net.sourceforge.waters.model.analysis.cli.CustomStringCommandLineArgument;
import net.sourceforge.waters.model.analysis.cli.FlagCommandLineArgument;
import net.sourceforge.waters.model.compiler.ModuleCompiler;
import net.sourceforge.waters.model.des.EventProxy;
import net.sourceforge.waters.model.des.ProductDESProxy;
import net.sourceforge.waters.model.des.ProductDESProxyFactory;


/**
 * A default implementation of the {@link ModelAnalyzerFactory} interface.
 * This class is extended for different flavours of model checking
 * algorithms.
 *
 * @author Robi Malik
 */

public abstract class AbstractModelAnalyzerFactory
  implements ModelAnalyzerFactory
{

  //#########################################################################
  //# Constructors
  protected AbstractModelAnalyzerFactory()
  {
  }


  //#########################################################################
  //# Interface net.sourceforge.waters.model.analysis.ModelVerifierFactory
  @Override
  public ConflictChecker createConflictChecker
    (final ProductDESProxyFactory factory)
    throws AnalysisConfigurationException
  {
    throw createUnsupportedOperationException("conflict check");
  }

  @Override
  public ControllabilityChecker createControllabilityChecker
    (final ProductDESProxyFactory factory)
    throws AnalysisConfigurationException
  {
    throw createUnsupportedOperationException("controllability check");
  }

  @Override
  public ControlLoopChecker createControlLoopChecker
    (final ProductDESProxyFactory factory)
    throws AnalysisConfigurationException
  {
    throw createUnsupportedOperationException("control-loop check");
  }

  @Override
  public DeadlockChecker createDeadlockChecker
    (final ProductDESProxyFactory factory)
    throws AnalysisConfigurationException
  {
    throw createUnsupportedOperationException("deadlock check");
  }

  @Override
  public DiagnosabilityChecker createDiagnosabilityChecker
    (final ProductDESProxyFactory factory)
    throws AnalysisConfigurationException
  {
    throw createUnsupportedOperationException("diagnosability check");
  }

  @Override
  public LanguageInclusionChecker createLanguageInclusionChecker
    (final ProductDESProxyFactory factory)
    throws AnalysisConfigurationException
  {
    throw createUnsupportedOperationException("language inclusion check");
  }

  @Override
  public SynchronousProductBuilder createSynchronousProductBuilder
    (final ProductDESProxyFactory factory)
    throws AnalysisConfigurationException
  {
    throw createUnsupportedOperationException("synchronous product");
  }

  @Override
  public SupervisorSynthesizer createSupervisorSynthesizer
    (final ProductDESProxyFactory factory)
    throws AnalysisConfigurationException
  {
    throw createUnsupportedOperationException("synthesis");
  }

  @Override
  public StateCounter createStateCounter
    (final ProductDESProxyFactory factory)
    throws AnalysisConfigurationException
  {
    throw createUnsupportedOperationException("state counting");
  }


  //#########################################################################
  //# Command Line Arguments
  // TODO Option context should be responsible, not factory?
  @Override
  public void configure(final ModelAnalyzer analyzer)
    throws AnalysisConfigurationException
  {
    if (mCommandLineOptionContext != null) {
      for (final CommandLineArgument<?> arg : mCommandLineOptionContext.getArgumentMap().values()) {
        if (arg.isUsed()) {
          mCommandLineOptionContext.configure(analyzer);
        }
      }
    }
  }

  @Override
  public void configure(final ModuleCompiler compiler)
  {
    mCommandLineOptionContext.configure(compiler);
  }

  @Override
  public void postConfigure(final ModelAnalyzer analyzer)
  throws AnalysisException
  {
    for (final CommandLineArgument<?> arg : mCommandLineOptionContext.getArgumentMap().values()) {
      if (arg.isUsed()) {
        arg.postConfigure(analyzer);
      }
    }
  }

  //#########################################################################
  //# Configuration
  @Override
  public void addArguments(final CommandLineOptionContext context,
                              final Configurable configurable,
                              final LeafOptionPage page)
  {
    if (configurable instanceof ModelAnalyzer) {
      mCommandLineOptionContext = context;
      registerOptions(page);
      context.generateArgumentsFromOptions(page, configurable,
                                   DistributedModelVerifierFactory.
                                     OPTION_DistributedModelVerifierFactory_Host);

      if (configurable instanceof ConflictChecker) {
        @SuppressWarnings("unchecked")
        final Option<EventProxy> markingOption = (Option<EventProxy>)
          page.get(AbstractModelAnalyzerFactory.OPTION_ConflictChecker_ConfiguredDefaultMarking);
        context.addArgument(new MarkingArgument(context, markingOption, AbstractModelAnalyzerFactory.
                                                OPTION_ConflictChecker_ConfiguredDefaultMarkingString));
        @SuppressWarnings("unchecked")
        final Option<EventProxy> premarkingOption = (Option<EventProxy>)
          page.get(AbstractModelAnalyzerFactory.OPTION_ConflictChecker_ConfiguredPreconditionMarking);
        context.addArgument(new PreMarkingArgument(context, premarkingOption, AbstractModelAnalyzerFactory.
                                                   OPTION_ConflictChecker_ConfiguredPreconditionMarkingString));
      }


      context.addArgument(new EndArgument(context, new FlagOption
                                          (null, null,
                                           "Treat remaining arguments as file names", "--")));
    }
  }


  @Override
  public void registerOptions(final OptionPage db)
  {
    db.add(new PositiveIntOption
             (OPTION_ModelAnalyzer_FinalStateLimit,
              "Monolithic state limit",
              "Maximum number of states in final synchronous product before aborting.",
              "-fslimit"));
    db.add(new PositiveIntOption
             (OPTION_ModelAnalyzer_FinalTransitionLimit,
              "Monolithic transition limit",
              "Maximum number of transitions in final synchronous product before aborting.",
              "-ftlimit"));
    db.add(new PositiveIntOption
             (OPTION_ModelAnalyzer_InternalStateLimit,
              "Internal state limit",
              "Maximum number of states in intermediate abstraction steps.",
              "-islimit"));
    db.add(new PositiveIntOption
             (OPTION_ModelAnalyzer_InternalTransitionLimit,
              "Internal transition limit",
              "Maximum number of transitions in intermediate abstraction steps.",
              "-itlimit"));

    db.add(new BooleanOption
             (OPTION_ModelVerifier_DetailedOutputEnabled,
              "Compute counterexample",
              "Computate a counterexample if model checking gives a failed result.",
              "-out",
              true));
    db.add(new BooleanOption
             (OPTION_ModelVerifier_ShortCounterExampleRequested,
              "Short counterexample",
              "Try to compute a counterexample that is as short as possible.",
              "-mince",
              false));

    db.add(new PropositionOption
             (OPTION_ConflictChecker_ConfiguredDefaultMarking,
              "Marking proposition",
              "The model is considered nonblocking, if it possible to reach a state " +
              "marked by this proposition from every reachable state.",
              "-marking",
              PropositionOption.DefaultKind.PREVENT_NULL));
    db.add(new PropositionOption
             (OPTION_ConflictChecker_ConfiguredPreconditionMarking,
              "Precondition marking",
              "Precondition marking used for generalised conflict check.",
              "-premarking",
              PropositionOption.DefaultKind.DEFAULT_NULL));

    db.add(new EventSetOption
             (OPTION_ControlLoopChecker_LoopEvents,
              "Loop events",
              "Check whether the system permits a loop using the selected events.",
              "-events",
              EventSetOption.DefaultKind.CONTROLLABLE,
              "Loop Events",
              "Non-Loop Events"));

    db.add(new BooleanOption
             (OPTION_SupervisorSynthesizer_DetailedOutputEnabled,
              "Create supervisor automata",
              "Disable this to suppress the creation of supervisor automata, " +
              "and only determine whether a supervisor exists.",
              "-out",
              true));
    db.add(new PropositionOption
             (OPTION_SupervisorSynthesizer_ConfiguredDefaultMarking,
              "Marking proposition",
              "If synthesising a nonblocking supervisor, it will be " +
              "nonblocking with respect to this proposition.",
              "-marking",
              PropositionOption.DefaultKind.ALLOW_NULL));
    db.add(new BooleanOption
             (OPTION_SupervisorSynthesizer_ControllableSynthesis,
              "Controllable supervisor",
              "Synthesise a controllable supervisor.",
              "-cont",
              true));
    db.add(new BooleanOption
             (OPTION_SupervisorSynthesizer_NonblockingSynthesis,
              "Nonblocking supervisor",
              "Synthesise a nonblocking supervisor.",
              "-nbl",
              true));
    db.add(new BooleanOption
             (OPTION_SupervisorSynthesizer_NormalSynthesis,
              "Normal supervisor",
              "Synthesise a normal supervisor.",
              "-norm",
              false));
    db.add(new StringOption
             (OPTION_SupervisorSynthesizer_OutputName,
              "Supervisor name prefix",
              "Name or name prefix for synthesised supervisors.",
              "-name",
              "sup"));
    db.add(new BooleanOption
             (OPTION_SupervisorSynthesizer_SupervisorLocalisationEnabled,
              "Localize supervisors",
              "If using supervisor reduction, create separate supervisors " +
              "for each controllable event that needs to be disabled.",
              "-loc",
              false));
    db.add(new EnumOption<SupervisorReductionMainMethod>
             (OPTION_SupervisorSynthesizer_SupervisorReductionMainMethod,
              "Supervisor reduction method",
              "Core algorithm to reduce the size of computed supervisors.",
              "-red",
              SupervisorReductionMainMethod.values()));
    db.add(new EnumOption<SupervisorReductionProjectionMethod>
             (OPTION_SupervisorSynthesizer_SupervisorReductionProjectionMethod,
              "Supervisor reduction projection",
              "Method to reduce the number of events before supervisor reduction.",
              "-redproj",
              SupervisorReductionProjectionMethod.values()));

    db.add(new BooleanOption
             (OPTION_SynchronousProductBuilder_DetailedOutputEnabled,
              "Build automaton model",
              "Disable this to suppress the creation of a synchronous product " +
              "automaton, and only run for statistics.",
              "-out",
              true));
    db.add(new StringOption
             (OPTION_SynchronousProductBuilder_OutputName,
              "Output name",
              "Name for the generated synchronous product automaton",
              "-name",
              "sync"));
    db.add(new ComponentKindOption
             (OPTION_SynchronousProductBuilder_OutputKind,
              "Output kind",
              "Type of the generated synchronous product automaton.",
              "-kind"));
    db.add(new BooleanOption
             (OPTION_SynchronousProductBuilder_PruningDeadlocks,
              "Prune deadlocks",
              "Stop synchronous product construction when encountering " +
              "states that are a deadlock in one of the components.",
              "-prune",
              false));
    db.add(new BooleanOption
             (OPTION_SynchronousProductBuilder_RemovingSelfloops,
              "Remove Selfloops",
              "Remove events that appear only as selfloop on every state," +
              "as well as propositions that appear on all states, from the result.",
              "-out",
              true));

    db.add(new StringListOption
             (OPTION_LanguageInclusionChecker_Property, null,
              "Name of a property to be checked " +
              "(can be used more than once)",
              "-property"));
  }


  //#########################################################################
  //# Supremica Options
  @Override
  public void configureFromOptions(final ModelAnalyzer analyzer)
  {
  }


  //#########################################################################
  //# Auxiliary Methods
  private AnalysisConfigurationException createUnsupportedOperationException
    (final String opname)
  {
    final String clsname = getClass().getName();
    final int dotpos = clsname.lastIndexOf('.');
    final String msg =
      clsname.substring(dotpos + 1) + " does not support " + opname + "!";
    return new AnalysisConfigurationException(msg);
  }


  //#########################################################################
  //# Inner Class MarkingArgument
  public static class MarkingArgument
    extends CustomStringCommandLineArgument<EventProxy>
  {
    //#######################################################################
    //# Constructors
    public MarkingArgument(final CommandLineOptionContext context,
                            final Option<EventProxy> option,
                            final String substituteID)
    {
      super(context, option, substituteID);
    }

    //#######################################################################
    //# Overrides for Abstract Base Class
    //# net.sourceforge.waters.model.analysis.CommandLineArgument
    @Override
    public void postConfigure(final ModelAnalyzer analyzer)
    throws EventNotFoundException
    {
      final ConflictChecker cchecker = (ConflictChecker) analyzer;
      final ProductDESProxy model = cchecker.getModel();
      final String markingname = getValue();
      if (markingname != null) {
        final EventProxy marking =
          AbstractConflictChecker.findMarkingProposition(model, markingname);
        cchecker.setConfiguredDefaultMarking(marking);
      }
    }
  }


  //#########################################################################
  //# Inner Class PreMarkingArgument
  public static class PreMarkingArgument
    extends CustomStringCommandLineArgument<EventProxy>
  {
    //#######################################################################
    //# Constructors
    public PreMarkingArgument(final CommandLineOptionContext context,
                               final Option<EventProxy> option,
                               final String substituteID)
    {
      super(context, option, substituteID);
    }

    //#######################################################################
    //# Overrides for Abstract Base Class
    //# net.sourceforge.waters.model.analysis.CommandLineArgument
    @Override
    public void postConfigure(final ModelAnalyzer analyzer)
    throws EventNotFoundException
    {
      final ConflictChecker cchecker = (ConflictChecker) analyzer;
      final ProductDESProxy model = cchecker.getModel();
      final String markingname = getValue();
      if (markingname != null) {
        final EventProxy marking =
          AbstractConflictChecker.findMarkingProposition(model, markingname);
        cchecker.setConfiguredPreconditionMarking(marking);
      }
    }
  }


  //#########################################################################
  //# Inner Class EndArgument
  public static class EndArgument extends FlagCommandLineArgument
  {
    //#######################################################################
    //# Constructors
    public EndArgument(final CommandLineOptionContext context,
                        final FlagOption option)
    {
      super(context, option);
    }

    //#######################################################################
    //# Overrides for Abstract Base Class
    //# net.sourceforge.waters.model.analysis.CommandLineArgument
    @Override
    public void parse(final CommandLineOptionContext context,
                      final Collection<Configurable> configurables,
                      final ListIterator<String> iter)
    {
      iter.remove();
      while (iter.hasNext()) {
        iter.next();
      }
    }

  }


  //#########################################################################
  //# Data Members
  private CommandLineOptionContext mCommandLineOptionContext;

  //#########################################################################
  //# Class Constants
  public static final String OPTION_ModelAnalyzer_FinalStateLimit =
    "ModelAnalyzer.FinalStateLimit";
  public static final String OPTION_ModelAnalyzer_FinalTransitionLimit =
    "ModelAnalyzer.FinalTransitionLimit";
  public static final String OPTION_ModelAnalyzer_InternalStateLimit =
    "ModelAnalyzer.InternalStateLimit";
  public static final String OPTION_ModelAnalyzer_InternalTransitionLimit =
    "ModelAnalyzer.InternalTransitionLimit";

  public static final String OPTION_ModelAnalyzer_SecondaryFactory =
    "AbstractModelAnalyzerFactory.SecondaryFactory";

  public static final String OPTION_ModelVerifier_DetailedOutputEnabled =
    "ModelVerifier.DetailedOutputEnabled";
  public static final String OPTION_ModelVerifier_ShortCounterExampleRequested =
    "ModelVerifier.ShortCounterExampleRequested";

  public static final String OPTION_ConflictChecker_ConfiguredDefaultMarking =
    "ConflictChecker.ConfiguredDefaultMarking";
  public static final String OPTION_ConflictChecker_ConfiguredPreconditionMarking =
    "ConflictChecker.ConfiguredPreconditionMarking";

  public static final String OPTION_ControlLoopChecker_LoopEvents =
    "ControlLoopChecker.LoopEvents";

  public static final String OPTION_LanguageInclusionChecker_Property =
    "LanguageInclusionChecker.Property";

  public static final String OPTION_SupervisorSynthesizer_ConfiguredDefaultMarking =
    "SupervisorSynthesizer.ConfiguredDefaultMarking";
  public static final String OPTION_SupervisorSynthesizer_ControllableSynthesis =
    "SupervisorSynthesizer.ControllableSynthesis";
  public static final String OPTION_SupervisorSynthesizer_DetailedOutputEnabled =
    "SupervisorSynthesizer.DetailedOutputEnabled";
  public static final String OPTION_SupervisorSynthesizer_NonblockingSynthesis =
    "SupervisorSynthesizer.NonblockingSynthesis";
  public static final String OPTION_SupervisorSynthesizer_NormalSynthesis =
    "SupervisorSynthesizer.NormalSynthesis";
  public static final String OPTION_SupervisorSynthesizer_OutputName =
    "SupervisorSynthesizer.OutputName";
  public static final String OPTION_SupervisorSynthesizer_SupervisorLocalisationEnabled =
    "SupervisorSynthesizer.SupervisorLocalisationEnabled";
  public static final String OPTION_SupervisorSynthesizer_SupervisorReductionMainMethod =
    "SupervisorSynthesizer.SupervisorReductionMainMethod";
  public static final String OPTION_SupervisorSynthesizer_SupervisorReductionProjectionMethod =
    "SupervisorSynthesizer.SupervisorReductionProjectionMethod";

  public static final String OPTION_SynchronousProductBuilder_DetailedOutputEnabled =
    "SynchronousProductBuilder.DetailedOutputEnabled";
  public static final String OPTION_SynchronousProductBuilder_OutputName =
    "SynchronousProductBuilder.OutputName";
  public static final String OPTION_SynchronousProductBuilder_OutputKind =
    "SynchronousProductBuilder.OutputKind";
  public static final String OPTION_SynchronousProductBuilder_PruningDeadlocks =
    "SynchronousProductBuilder.PruningDeadlocks";
  public static final String OPTION_SynchronousProductBuilder_RemovingSelfloops =
    "SynchronousProductBuilder.RemovingSelfloops";

  public static final String OPTION_AbstractModelAnalyzerFactory_NoOptimisation =
    "AbstractModelAnalyzerFactory.NoOptimisation";
  public static final String OPTION_AbstractModelAnalyzerFactory_HISCModule =
    "AbstractModelAnalyzerFactory.HISCModule";

  public static final String OPTION_ConflictChecker_ConfiguredDefaultMarkingString =
    "ConflictChecker.ConfiguredDefaultMarkingString";
  public static final String OPTION_ConflictChecker_ConfiguredPreconditionMarkingString =
    "ConflictChecker.ConfiguredPreconditionMarkingString";

}
