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

package net.sourceforge.waters.analysis.compositional;

import java.io.File;
import java.io.PrintStream;

import net.sourceforge.waters.analysis.abstraction.ProjectingSupervisorReductionFactory;
import net.sourceforge.waters.analysis.abstraction.SupervisorReductionMainMethod;
import net.sourceforge.waters.analysis.abstraction.SupervisorReductionProjectionMethod;
import net.sourceforge.waters.analysis.compositional.AbstractCompositionalModelAnalyzer.PreselectingMethod;
import net.sourceforge.waters.analysis.options.BooleanOption;
import net.sourceforge.waters.analysis.options.EnumOption;
import net.sourceforge.waters.analysis.options.FileOption;
import net.sourceforge.waters.analysis.options.OptionPage;
import net.sourceforge.waters.model.analysis.AnalysisConfigurationException;
import net.sourceforge.waters.model.analysis.CommandLineArgumentBoolean;
import net.sourceforge.waters.model.analysis.CommandLineArgumentChain;
import net.sourceforge.waters.model.analysis.CommandLineArgumentEnum;
import net.sourceforge.waters.model.analysis.CommandLineArgumentFlag;
import net.sourceforge.waters.model.analysis.CommandLineArgumentInteger;
import net.sourceforge.waters.model.analysis.CommandLineArgumentString;
import net.sourceforge.waters.model.analysis.EnumFactory;
import net.sourceforge.waters.model.analysis.des.AbstractModelAnalyzerFactory;
import net.sourceforge.waters.model.analysis.des.ModelAnalyzer;
import net.sourceforge.waters.model.analysis.des.SupervisorSynthesizer;
import net.sourceforge.waters.model.des.ProductDESProxyFactory;


/**
 * A factory that produces compositional model verifiers.
 *
 * @author Robi Malik
 */

public class CompositionalModelAnalyzerFactory
  extends AbstractModelAnalyzerFactory
{

  //#########################################################################
  //# Singleton Pattern
  public static CompositionalModelAnalyzerFactory getInstance()
  {
    return SingletonHolder.INSTANCE;
  }

  private static class SingletonHolder {
    private static final CompositionalModelAnalyzerFactory INSTANCE =
      new CompositionalModelAnalyzerFactory();
  }


  //#########################################################################
  //# Constructors
  CompositionalModelAnalyzerFactory()
  {
  }


  //#########################################################################
  //# Overrides for
  //# net.sourceforge.waters.model.analysis.AbstractModelAnalyzerFactory
  @Override
  protected void addArguments()
  {
    super.addArguments();
    addArgument(new MonolithicStateLimitArgument());
    addArgument(new InternalStateLimitArgument());
    addArgument(new LowerInternalStateLimitArgument());
    addArgument(new UpperInternalStateLimitArgument());
    addArgument(new MonolithicTransitionLimitArgument());
    addArgument(new InternalTransitionLimitArgument());
    addArgument(new AbstractionMethodArgument());
    addArgument(new PreselectingMethodArgument());
    addArgument(new SelectingMethodArgument());
    addArgument(new SupervisorReductionArgument());
    addArgument(new SupervisorReductionProjectionArgument());
    addArgument(new SupervisorLocalisationArgument());
    addArgument(new SubsumptionArgument());
    addArgument(new SpecialEventsArgument());
    addArgument(new BlockedEventsArgument());
    addArgument(new FailingEventsArgument());
    addArgument(new SelfloopOnlyEventsArgument());
    addArgument(new DeadlockPruningArgument());
    addArgument(new SecondaryFactoryArgument());
    addArgument(new DumpFileArgument());
  }


  //#########################################################################
  //# Interface net.sourceforge.waters.model.analysis.ModelAnalyzerFactory
  @Override
  public CompositionalConflictChecker createConflictChecker
    (final ProductDESProxyFactory factory)
  {
    return new CompositionalConflictChecker(factory);
  }

  @Override
  public CompositionalLanguageInclusionChecker createLanguageInclusionChecker
    (final ProductDESProxyFactory factory)
  {
    return new CompositionalLanguageInclusionChecker(factory);
  }

  @Override
  public CompositionalAutomataSynthesizer createSupervisorSynthesizer
    (final ProductDESProxyFactory factory)
  {
    return new CompositionalAutomataSynthesizer(factory);
  }


  @Override
  public void registerOptions(final OptionPage db)
  {
    super.registerOptions(db);
    db.add(new BooleanOption
             (OPTION_AbstractCompositionalModelAnalyzer_BlockedEventsEnabled,
              "Use blocked events",
              "Detect and remove events known to be globablly disabled.",
              "-be",
              true));
    db.add(new BooleanOption
             (OPTION_AbstractCompositionalModelAnalyzer_FailingEventsEnabled,
              "Use failing events",
              "Detect events that only lead to blocking states and " +
              "simplify automata based on this information.",
              "-fe",
              true));
    db.add(new FileOption
             (OPTION_AbstractCompositionalModelAnalyzer_MonolithicDumpFile,
              "Dump file name",
              "If set, any abstracted model will be written to this file " +
              "before being sent for monolithic analysis.",
              "-dump"));
    db.add(new EnumOption<PreselectingMethod>
             (OPTION_AbstractCompositionalModelAnalyzer_PreselectingMethod,
              "Preselection method",
              "Preselection heuristic to generate groups of automata to consider " +
              "for composition.",
              "-presel",
              AbstractCompositionalModelAnalyzer.getPreselectingMethodFactoryStatic()));
    db.add(new BooleanOption
             (OPTION_AbstractCompositionalModelAnalyzer_SelfloopOnlyEventsEnabled,
              "Use selfloop-only events",
              "Detect events that are appear only as selfloop outside of the " +
              "subsystem being abstracted, and use this information to help " +
              "with minimisation.",
              "-se",
              true));
    db.add(new EnumOption<SelectionHeuristicCreator>
             (OPTION_AbstractCompositionalModelAnalyzer_SelectingMethod,
              "Selection method",
              "Heuristic to choose the group of automata to compose and simplify " +
              "from the options produced by the preselection method.",
              "-sel",
              CompositionalSelectionHeuristicFactory.getInstance()));
    db.add(new BooleanOption
             (OPTION_AbstractCompositionalModelAnalyzer_SubumptionEnabled,
              "Use subumption test",
              "Suppress candidate groups of automata that are supersets of " +
              "other candidates.",
              "-sub",
              true));

    db.add(new BooleanOption
             (OPTION_AbstractCompositionalModelVerifier_TraceCheckingEnabled,
              "Counterexample debugging",
              "When computing counterexamples, perform debug checks to ensure " +
              "that the counterexample is accepted after every abstraction step.",
              "-tc",
              false));

    db.add(new EnumOption<AbstractionProcedureCreator>
             (OPTION_CompositionalAutomataSynthesizer_AbstractionProcedureCreator,
              "Abstraction procedure",
              "Abstraction procedure to simplify automata during compositional " +
              "minimisation.",
              "-method",
              AutomataSynthesisAbstractionProcedureFactory.getInstance()));

    db.add(new EnumOption<AbstractionProcedureCreator>
             (OPTION_CompositionalConflictChecker_AbstractionProcedureCreator,
              "Abstraction procedure",
              "Abstraction procedure to simplify automata during compositional " +
              "minimisation.",
              "-method",
              ConflictAbstractionProcedureFactory.getInstance()));
    db.add(new EnumOption<PreselectingMethod>
             (OPTION_CompositionalConflictChecker_PreselectingMethod,
              "Preselection method",
              "Preselection heuristic to generate groups of automata to consider " +
              "for composition.",
              "-presel",
              CompositionalConflictChecker.getPreselectingMethodFactoryStatic()));
    db.add(new EnumOption<SelectionHeuristicCreator>
             (OPTION_CompositionalConflictChecker_SelectingMethod,
              "Selection method",
              "Heuristic to choose the group of automata to compose and simplify " +
              "from the options produced by the preselection method.",
              "-sel",
              ConflictSelectionHeuristicFactory.getInstance()));
  }


  //#########################################################################
  //# Inner Class FinalStateLimitArgument
  private static class MonolithicStateLimitArgument extends
      CommandLineArgumentInteger
  {

    //#######################################################################
    //# Constructors
    private MonolithicStateLimitArgument()
    {
      super("-mslimit",
            "Maximum number of states constructed in final\n" +
            "monolithic composition");
    }

    //#######################################################################
    //# Overrides for Abstract Base Class
    //# net.sourceforge.waters.model.analysis.CommandLineArgument
    @Override
    public void configureAnalyzer(final Object analyzer)
    {
      final int limit = getValue();
      final AbstractCompositionalModelAnalyzer composer =
        (AbstractCompositionalModelAnalyzer) analyzer;
      composer.setMonolithicStateLimit(limit);
    }

  }


  //#########################################################################
  //# Inner Class InternalStateLimitArgument
  private static class InternalStateLimitArgument extends
      CommandLineArgumentInteger
  {

    //#######################################################################
    //# Constructors
    private InternalStateLimitArgument()
    {
      super("-islimit",
            "Maximum number of states constructed in abstraction attempts");
    }

    //#######################################################################
    //# Overrides for Abstract Base Class
    //# net.sourceforge.waters.model.analysis.CommandLineArgument
    @Override
    public void configureAnalyzer(final Object analyzer)
    {
      final int limit = getValue();
      final AbstractCompositionalModelAnalyzer composer =
        (AbstractCompositionalModelAnalyzer) analyzer;
      composer.setInternalStateLimit(limit);
    }

  }


  //#########################################################################
  //# Inner Class LowerInternalStateLimitArgument
  private static class LowerInternalStateLimitArgument extends
      CommandLineArgumentInteger
  {

    //#######################################################################
    //# Constructors
    private LowerInternalStateLimitArgument()
    {
      super("-lslimit",
            "Initial maximum number of states for abstraction attempts");
    }

    //#######################################################################
    //# Overrides for Abstract Base Class
    //# net.sourceforge.waters.model.analysis.CommandLineArgument
    @Override
    public void configureAnalyzer(final Object analyzer)
    {
      final int limit = getValue();
      final AbstractCompositionalModelAnalyzer composer =
        (AbstractCompositionalModelAnalyzer) analyzer;
      composer.setLowerInternalStateLimit(limit);
    }

  }


  //#########################################################################
  //# Inner Class UpperInternalStateLimitArgument
  private static class UpperInternalStateLimitArgument extends
      CommandLineArgumentInteger
  {

    //#######################################################################
    //# Constructors
    private UpperInternalStateLimitArgument()
    {
      super("-uslimit",
            "Final maximum number of states for abstraction attempts");
    }

    //#######################################################################
    //# Overrides for Abstract Base Class
    //# net.sourceforge.waters.model.analysis.CommandLineArgument
    @Override
    public void configureAnalyzer(final Object analyzer)
    {
      final int limit = getValue();
      final AbstractCompositionalModelAnalyzer composer =
        (AbstractCompositionalModelAnalyzer) analyzer;
      composer.setUpperInternalStateLimit(limit);
    }

  }


  //#########################################################################
  //# Inner Class FinalTransitionLimitArgument
  private static class MonolithicTransitionLimitArgument extends
      CommandLineArgumentInteger
  {

    //#######################################################################
    //# Constructors
    private MonolithicTransitionLimitArgument()
    {
      super("-mtlimit",
            "Maximum number of transitions constructed in final\n" +
            "monolithic composition");
    }

    //#######################################################################
    //# Overrides for Abstract Base Class
    //# net.sourceforge.waters.model.analysis.CommandLineArgument
    @Override
    public void configureAnalyzer(final Object analyzer)
    {
      final int limit = getValue();
      final AbstractCompositionalModelAnalyzer composer =
        (AbstractCompositionalModelAnalyzer) analyzer;
      composer.setMonolithicTransitionLimit(limit);
    }

  }


  //#########################################################################
  //# Inner Class InternalTransitionLimitArgument
  private static class InternalTransitionLimitArgument extends
      CommandLineArgumentInteger
  {

    //#######################################################################
    //# Constructors
    private InternalTransitionLimitArgument()
    {
      super("-itlimit",
            "Maximum number of transitions constructed in abstraction\n" +
            "attempts");
    }

    //#######################################################################
    //# Overrides for Abstract Base Class
    //# net.sourceforge.waters.model.analysis.CommandLineArgument
    @Override
    public void configureAnalyzer(final Object analyzer)
    {
      final int limit = getValue();
      final AbstractCompositionalModelAnalyzer composer =
        (AbstractCompositionalModelAnalyzer) analyzer;
      composer.setInternalTransitionLimit(limit);
    }

  }


  //#########################################################################
  //# Inner Class AbstractionMethodArgument
  private static class AbstractionMethodArgument
    extends CommandLineArgumentString
  {
    //#######################################################################
    //# Constructors
    private AbstractionMethodArgument()
    {
      super("-method", "Abstraction sequence for compositional analysis");
    }

    //#######################################################################
    //# Overrides for Abstract Base Class
    //# net.sourceforge.waters.model.analysis.CommandLineArgument
    @Override
    public void configureAnalyzer(final Object analyzer)
    {
      final String name = getValue();
      final AbstractCompositionalModelAnalyzer composer =
        (AbstractCompositionalModelAnalyzer) analyzer;
      final EnumFactory<AbstractionProcedureCreator>
        factory = composer.getAbstractionProcedureFactory();
      final AbstractionProcedureCreator creator = factory.getEnumValue(name);
      if (creator == null) {
        System.err.println("Bad value for " + getName() + " option!");
        factory.dumpEnumeration(System.err, 0);
        System.exit(1);
      }
      composer.setAbstractionProcedureCreator(creator);
    }

    //#######################################################################
    //# Printing
    @Override
    public void dump(final PrintStream stream, final Object analyzer)
    {
      super.dump(stream, analyzer);
      final AbstractCompositionalModelAnalyzer composer =
        (AbstractCompositionalModelAnalyzer) analyzer;
      final EnumFactory<AbstractionProcedureCreator>
        factory = composer.getAbstractionProcedureFactory();
      factory.dumpEnumeration(stream, INDENT);
    }
  }


  //#########################################################################
  //# Inner Class PreselectingMethodArgument
  private static class PreselectingMethodArgument
    extends CommandLineArgumentString
  {

    //#######################################################################
    //# Constructors
    private PreselectingMethodArgument()
    {
      super("-presel", "Preselecting heuristic for candidate selection");
    }

    //#######################################################################
    //# Overrides for Abstract Base Class
    //# net.sourceforge.waters.model.analysis.CommandLineArgument
    @Override
    public void configureAnalyzer(final Object analyzer)
    {
      final String name = getValue();
      final AbstractCompositionalModelAnalyzer composer =
        (AbstractCompositionalModelAnalyzer) analyzer;
      final EnumFactory<AbstractCompositionalModelAnalyzer.PreselectingMethod>
        factory = composer.getPreselectingMethodFactory();
      final AbstractCompositionalModelAnalyzer.PreselectingMethod method =
        factory.getEnumValue(name);
      if (method == null) {
        System.err.println("Bad value for " + getName() + " option!");
        factory.dumpEnumeration(System.err, 0);
        System.exit(1);
      }
      composer.setPreselectingMethod(method);
    }

    //#######################################################################
    //# Printing
    @Override
    public void dump(final PrintStream stream, final Object analyzer)
    {
      super.dump(stream, analyzer);
      final AbstractCompositionalModelAnalyzer composer =
        (AbstractCompositionalModelAnalyzer) analyzer;
      final EnumFactory<AbstractCompositionalModelAnalyzer.PreselectingMethod>
        factory = composer.getPreselectingMethodFactory();
      factory.dumpEnumeration(stream, INDENT);
    }

  }


  //#########################################################################
  //# Inner Class SelectingMethodArgument
  private static class SelectingMethodArgument
    extends CommandLineArgumentString
  {

    //#######################################################################
    //# Constructors
    private SelectingMethodArgument()
    {
      super("-sel", "Selecting heuristic for candidate selection");
    }

    //#######################################################################
    //# Overrides for Abstract Base Class
    //# net.sourceforge.waters.model.analysis.CommandLineArgument
    @Override
    public void configureAnalyzer(final Object analyzer)
    {
      final AbstractCompositionalModelAnalyzer composer =
        (AbstractCompositionalModelAnalyzer) analyzer;
      final EnumFactory<SelectionHeuristicCreator> factory =
        composer.getSelectionHeuristicFactory();
      final String name = getValue();
      final String[] parts = name.split(",");
      final SelectionHeuristic<Candidate> heuristic;
      if (parts.length == 1) {
        final SelectionHeuristicCreator creator =
          factory.getEnumValue(name);
        if (creator == null) {
          System.err.println("Bad value for " + getName() + " option!");
          factory.dumpEnumeration(System.err, 0);
          System.exit(1);
        }
        heuristic = creator.createChainHeuristic();
      } else {
        @SuppressWarnings("unchecked")
        final SelectionHeuristic<Candidate>[] heuristics =
          new SelectionHeuristic[parts.length];
        for (int i = 0; i < parts.length; i++) {
          final SelectionHeuristicCreator creator =
            factory.getEnumValue(parts[i]);
          if (creator == null) {
            System.err.println("Bad value for " + getName() + " option!");
            factory.dumpEnumeration(System.err, 0);
            System.exit(1);
          }
          heuristics[i] = creator.createBaseHeuristic();
        }
        heuristic = new ChainSelectionHeuristic<Candidate>(heuristics);
      }
      composer.setSelectionHeuristic(heuristic);
    }

    //#######################################################################
    //# Printing
    @Override
    public void dump(final PrintStream stream, final Object analyzer)
    {
      if (analyzer instanceof CompositionalConflictChecker) {
        super.dump(stream, analyzer);
        final CompositionalConflictChecker composer =
          (CompositionalConflictChecker) analyzer;
        final EnumFactory<SelectionHeuristicCreator> factory =
          composer.getSelectionHeuristicFactory();
        factory.dumpEnumeration(stream, INDENT);
      }
    }
  }


  //#########################################################################
  //# Inner Class SupervisorReductionArgument
  private static class SupervisorReductionArgument
    extends CommandLineArgumentEnum<SupervisorReductionMainMethod>
  {
    //#######################################################################
    //# Constructors
    private SupervisorReductionArgument()
    {
      super("-red", "Core method for supervisor reduction",
            SupervisorReductionMainMethod.class);
    }

    //#######################################################################
    //# Overrides for Abstract Base Class
    //# net.sourceforge.waters.model.analysis.CommandLineArgument
    @Override
    public void configureAnalyzer(final Object analyzer)
    {
      if (analyzer instanceof SupervisorSynthesizer) {
        final SupervisorSynthesizer synthesizer =
          (SupervisorSynthesizer) analyzer;
        final SupervisorReductionMainMethod method = getValue();
        ProjectingSupervisorReductionFactory.
          configureSynthesizer(synthesizer, method);
      } else {
        fail("Command line option " + getName() +
             " is only supported for synthesis!");
      }
    }
  }


  private static class SupervisorReductionProjectionArgument
    extends CommandLineArgumentEnum<SupervisorReductionProjectionMethod>
  {
    //#######################################################################
    //# Constructors
    private SupervisorReductionProjectionArgument()
    {
      super("-redproj", "Projection method for supervisor reduction",
            SupervisorReductionProjectionMethod.class);
    }

    //#######################################################################
    //# Overrides for Abstract Base Class
    //# net.sourceforge.waters.model.analysis.CommandLineArgument
    @Override
    public void configureAnalyzer(final Object analyzer)
    {
      if (analyzer instanceof SupervisorSynthesizer) {
        final SupervisorSynthesizer synthesizer =
          (SupervisorSynthesizer) analyzer;
        final SupervisorReductionProjectionMethod method = getValue();
        ProjectingSupervisorReductionFactory.
          configureSynthesizer(synthesizer, method);
      } else {
        fail("Command line option " + getName() +
             " is only supported for synthesis!");
      }
    }
  }


  //#########################################################################
  //# Inner Class SupervisorLocalisationArgument
  private static class SupervisorLocalisationArgument
    extends CommandLineArgumentFlag
  {
    //#######################################################################
    //# Constructors
    private SupervisorLocalisationArgument()
    {
      super("-loc", "Separate supervisors for each controllable event");
    }

    //#######################################################################
    //# Overrides for Abstract Base Class
    //# net.sourceforge.waters.model.analysis.CommandLineArgument
    @Override
    public void configureAnalyzer(final Object analyzer)
    {
      if (analyzer instanceof SupervisorSynthesizer) {
        final SupervisorSynthesizer synthesizer =
          (SupervisorSynthesizer) analyzer;
        synthesizer.setSupervisorLocalizationEnabled(true);
      } else {
        fail("Command line option " + getName() +
             " is only supported for synthesis!");
      }
    }
  }


  //#########################################################################
  //# Inner Class SubsumptionArgument
  private static class SubsumptionArgument extends CommandLineArgumentFlag
  {
    //#######################################################################
    //# Constructors
    private SubsumptionArgument()
    {
      super("-sub", "Check for subsumption in selecting heuristics");
    }

    //#######################################################################
    //# Overrides for Abstract Base Class
    //# net.sourceforge.waters.model.analysis.CommandLineArgument
    @Override
    public void configureAnalyzer(final Object analyzer)
    {
      final AbstractCompositionalModelAnalyzer composer =
        (AbstractCompositionalModelAnalyzer) analyzer;
      composer.setSubumptionEnabled(true);
    }
  }


  //#########################################################################
  //# Inner Class SpecialEventsArgument
  private static class SpecialEventsArgument extends CommandLineArgumentBoolean
  {

    //#######################################################################
    //# Constructors
    private SpecialEventsArgument()
    {
      super("-se",
            "Enable or disable blocked, failing, and selfloop-only events");
    }

    //#######################################################################
    //# Overrides for Abstract Base Class
    //# net.sourceforge.waters.model.analysis.CommandLineArgument
    @Override
    public void configureAnalyzer(final Object analyzer)
    {
      final AbstractCompositionalModelAnalyzer composer =
        (AbstractCompositionalModelAnalyzer) analyzer;
      final boolean enable = getValue();
      composer.setUsingSpecialEvents(enable);
    }

  }


  //#########################################################################
  //# Inner Class BlockedEventsArgument
  private static class BlockedEventsArgument
    extends CommandLineArgumentBoolean
  {

    //#######################################################################
    //# Constructors
    private BlockedEventsArgument()
    {
      super("-be", "Enable or disable blocked events removal");
    }

    //#######################################################################
    //# Overrides for Abstract Base Class
    //# net.sourceforge.waters.model.analysis.CommandLineArgument
    @Override
    public void configureAnalyzer(final Object analyzer)
    {
      final AbstractCompositionalModelAnalyzer composer =
        (AbstractCompositionalModelAnalyzer) analyzer;
      final boolean enable = getValue();
      composer.setBlockedEventsEnabled(enable);
    }

  }


  //#########################################################################
  //# Inner Class FailingEventsArgument
  private static class FailingEventsArgument
    extends CommandLineArgumentBoolean
  {

    //#######################################################################
    //# Constructors
    private FailingEventsArgument()
    {
      super("-fe", "Enable or disable failing events redirection");
    }

    //#######################################################################
    //# Overrides for Abstract Base Class
    //# net.sourceforge.waters.model.analysis.CommandLineArgument
    @Override
    public void configureAnalyzer(final Object analyzer)
    {
      final AbstractCompositionalModelAnalyzer composer =
        (AbstractCompositionalModelAnalyzer) analyzer;
      final boolean enable = getValue();
      composer.setFailingEventsEnabled(enable);
    }

  }


  //#########################################################################
  //# Inner Class SelfloopOnlyEventsArgument
  private static class SelfloopOnlyEventsArgument
    extends CommandLineArgumentBoolean
  {

    //#######################################################################
    //# Constructors
    private SelfloopOnlyEventsArgument()
    {
      super("-sl",
            "Enable or disable selfloop-only events and selfloop removal");
    }

    //#######################################################################
    //# Overrides for Abstract Base Class
    //# net.sourceforge.waters.model.analysis.CommandLineArgument
    @Override
    public void configureAnalyzer(final Object analyzer)
    {
      final AbstractCompositionalModelAnalyzer composer =
        (AbstractCompositionalModelAnalyzer) analyzer;
      final boolean enable = getValue();
      composer.setSelfloopOnlyEventsEnabled(enable);
    }

  }


  //#########################################################################
  //# Inner Class DeadlockPruningArgument
  private static class DeadlockPruningArgument
    extends CommandLineArgumentBoolean
  {
    //#######################################################################
    //# Constructors
    private DeadlockPruningArgument()
    {
      super("-dp",
            "Enable or disable deadlock pruning in synchronous product");
    }

    //#######################################################################
    //# Overrides for Abstract Base Class
    //# net.sourceforge.waters.model.analysis.CommandLineArgument
    @Override
    public void configureAnalyzer(final Object analyzer)
    {
      final AbstractCompositionalModelAnalyzer composer =
        (AbstractCompositionalModelAnalyzer) analyzer;
      final boolean enable = getValue();
      composer.setPruningDeadlocks(enable);
    }
  }


  //#########################################################################
  //# Inner Class SecondaryFactoryArgyment
  private static class SecondaryFactoryArgument
    extends CommandLineArgumentChain
  {
    //#######################################################################
    //# Constructors
    private SecondaryFactoryArgument()
    {
    }

    //#######################################################################
    //# Overrides for Abstract Base Class
    //# net.sourceforge.waters.model.analysis.CommandLineArgument
    @Override
    public void configureAnalyzer(final Object analyzer)
      throws AnalysisConfigurationException
    {
      final ModelAnalyzer modelAnalyzer = (ModelAnalyzer) analyzer;
      final ModelAnalyzer secondaryAnalyzer =
        createSecondaryAnalyzer(modelAnalyzer);
      final AbstractCompositionalModelAnalyzer composer =
        (AbstractCompositionalModelAnalyzer) analyzer;
      composer.setMonolithicAnalyzer(secondaryAnalyzer);
    }
  }


  //#########################################################################
  //# Inner Class DumpFileArgument
  private static class DumpFileArgument
    extends CommandLineArgumentString
  {
    //#######################################################################
    //# Constructors
    protected DumpFileArgument()
    {
      super("-dump",
            "Save abstracted model in given file before monolithic verification");
    }

    //#######################################################################
    //# Overrides for Abstract Base Class
    //# net.sourceforge.waters.model.analysis.CommandLineArgument
    @Override
    public void configureAnalyzer(final Object analyzer)
      throws AnalysisConfigurationException
    {
      final AbstractCompositionalModelAnalyzer composer =
        (AbstractCompositionalModelAnalyzer) analyzer;
      final String fileName = getValue();
      final File file = new File(fileName);
      composer.setMonolithicDumpFile(file);
    }
  }


  //#########################################################################
  //# Class Constants
  public static final String
    OPTION_AbstractCompositionalModelAnalyzer_BlockedEventsEnabled =
    "AbstractCompositionalModelAnalyzer.BlockedEventsEnabled";
  public static final String
    OPTION_AbstractCompositionalModelAnalyzer_FailingEventsEnabled =
    "AbstractCompositionalModelAnalyzer.FailingEventsEnabled";
  public static final String
    OPTION_AbstractCompositionalModelAnalyzer_MonolithicDumpFile =
    "AbstractCompositionalModelAnalyzer.MonolithicDumpFile";
  public static final String
    OPTION_AbstractCompositionalModelAnalyzer_PreselectingMethod =
    "AbstractCompositionalModelAnalyzer.PreselectingMethod";
  public static final String
    OPTION_AbstractCompositionalModelAnalyzer_SelfloopOnlyEventsEnabled =
    "AbstractCompositionalModelAnalyzer.SelfloopOnlyEventsEnabled";
  public static final String
    OPTION_AbstractCompositionalModelAnalyzer_SelectingMethod =
    "AbstractCompositionalModelAnalyzer.SelectingMethod";
  public static final String
    OPTION_AbstractCompositionalModelAnalyzer_SubumptionEnabled =
    "AbstractCompositionalModelAnalyzer.SubumptionEnabled";

  public static final String
    OPTION_AbstractCompositionalModelVerifier_TraceCheckingEnabled =
    "AbstractCompositionalModelVerifier.TraceCheckingEnabled";

  public static final String
    OPTION_CompositionalAutomataSynthesizer_AbstractionProcedureCreator =
    "CompositionalAutomataSynthesizer.AbstractionProcedureCreator";

  public static final String
    OPTION_CompositionalConflictChecker_AbstractionProcedureCreator =
    "CompositionalConflictChecker.AbstractionProcedureCreator";
  public static final String
    OPTION_CompositionalConflictChecker_PreselectingMethod =
    "CompositionalConflictChecker.PreselectingMethod";
  public static final String
    OPTION_CompositionalConflictChecker_SelectingMethod =
    "CompositionalConflictChecker.SelectionHeuristic";

}
