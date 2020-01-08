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

package net.sourceforge.waters.model.analysis.des;

import gnu.trove.set.hash.THashSet;

import java.io.PrintStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;

import net.sourceforge.waters.analysis.abstraction.SupervisorReductionMainMethod;
import net.sourceforge.waters.analysis.abstraction.SupervisorReductionProjectionMethod;
import net.sourceforge.waters.analysis.hisc.HISCCompileMode;
import net.sourceforge.waters.analysis.options.BooleanOption;
import net.sourceforge.waters.analysis.options.ComponentKindOption;
import net.sourceforge.waters.analysis.options.EnumOption;
import net.sourceforge.waters.analysis.options.EventSetOption;
import net.sourceforge.waters.analysis.options.OptionPage;
import net.sourceforge.waters.analysis.options.PositiveIntOption;
import net.sourceforge.waters.analysis.options.PropositionOption;
import net.sourceforge.waters.analysis.options.StringOption;
import net.sourceforge.waters.analysis.trcomp.TRCompositionalModelAnalyzerFactory;
import net.sourceforge.waters.model.analysis.AnalysisConfigurationException;
import net.sourceforge.waters.model.analysis.AnalysisException;
import net.sourceforge.waters.model.analysis.CommandLineArgument;
import net.sourceforge.waters.model.analysis.CommandLineArgumentBoolean;
import net.sourceforge.waters.model.analysis.CommandLineArgumentFlag;
import net.sourceforge.waters.model.analysis.CommandLineArgumentInteger;
import net.sourceforge.waters.model.analysis.CommandLineArgumentString;
import net.sourceforge.waters.model.analysis.CommandLineArgumentStringList;
import net.sourceforge.waters.model.analysis.kindtranslator.KindTranslator;
import net.sourceforge.waters.model.base.ComponentKind;
import net.sourceforge.waters.model.base.EventKind;
import net.sourceforge.waters.model.base.ProxyTools;
import net.sourceforge.waters.model.compiler.ModuleCompiler;
import net.sourceforge.waters.model.des.AutomatonProxy;
import net.sourceforge.waters.model.des.EventProxy;
import net.sourceforge.waters.model.des.ProductDESProxy;
import net.sourceforge.waters.model.des.ProductDESProxyFactory;
import net.sourceforge.waters.model.module.EventDeclProxy;


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
    mArgumentMap = new LinkedHashMap<String,CommandLineArgument>(64);
  }


  //#########################################################################
  //# Configuration
  protected void addArguments()
  {
    addArgument(new EndArgument());
    addArgument(new HelpArgument());
    addArgument(new HISCArgument());
    addArgument(new LimitArgument());
    addArgument(new MarkingArgument());
    addArgument(new NoOptimisationArgument());
    addArgument(new NoOutputArgument());
    addArgument(new PreMarkingArgument());
    addArgument(new PropertyArgument());
    addArgument(new ShortCounterExampleArgument());
    addArgument(new TransitionLimitArgument());
  }

  protected void addArgument(final CommandLineArgument argument)
  {
    for (final String name : argument.getNames()) {
      mArgumentMap.put(name, argument);
    }
  }

  protected void removeArgument(final String name)
  {
    mArgumentMap.remove(name);
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
    db.add(new BooleanOption
             (TRCompositionalModelAnalyzerFactory.OPTION_AbstractTRCompositionalModelAnalyzer_WeakObservationEquivalence,
              "Use weak observation equivalence",
              "",//TODO
              "",
            false));
  }


  @Override
  public void parse(final ListIterator<String> iter)
  {
    mArgumentMap.clear();
    addArguments();
    while (iter.hasNext()) {
      final String name = iter.next();
      final CommandLineArgument arg = mArgumentMap.get(name);
      if (arg != null) {
        arg.parse(iter);
      } else if (name.startsWith("-")) {
        System.err.println("Unsupported option " + name +
                           ". Try -help to see available options.");
        System.exit(1);
      }
    }
    checkRequiredArguments();
  }

  @Override
  public void configure(final ModelAnalyzer analyzer)
    throws AnalysisConfigurationException
  {
    for (final CommandLineArgument arg : mArgumentMap.values()) {
      if (arg.isUsed()) {
        arg.configureAnalyzer(analyzer);
      }
    }
  }

  @Override
  public void configure(final ModuleCompiler compiler)
  {
    for (final CommandLineArgument arg : mArgumentMap.values()) {
      if (arg.isUsed()) {
        arg.configureCompiler(compiler);
      }
    }
  }

  @Override
  public void postConfigure(final ModelAnalyzer analyzer)
  throws AnalysisException
  {
    for (final CommandLineArgument arg : mArgumentMap.values()) {
      if (arg.isUsed()) {
        arg.postConfigure(analyzer);
      }
    }
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

  private void checkRequiredArguments()
  {
    for (final CommandLineArgument arg : mArgumentMap.values()) {
      if (arg.isRequired() && !arg.isUsed()) {
        final String clsname = getClass().getName();
        final int dotpos = clsname.lastIndexOf('.');
        final String msg =
          "Required argument " + arg.getName() + " for " +
          clsname.substring(dotpos + 1) + " not specified!";
        CommandLineArgument.fail(msg);
      }
    }
  }


  //#########################################################################
  //# Inner Class EndArgument
  private class EndArgument extends CommandLineArgument
  {
    //#######################################################################
    //# Constructors
    private EndArgument()
    {
      super("--", "Treat remaining arguments as file names");
    }

    //#######################################################################
    //# Overrides for Abstract Base Class
    //# net.sourceforge.waters.model.analysis.CommandLineArgument
    @Override
    public void parse(final ListIterator<String> iter)
    {
      iter.remove();
      while (iter.hasNext()) {
        iter.next();
      }
    }
  }


  //#########################################################################
  //# Inner Class HelpArgument
  private class HelpArgument extends CommandLineArgumentFlag
  {
    //#######################################################################
    //# Constructors
    private HelpArgument()
    {
      super("-help", "Print this message");
    }

    //#######################################################################
    //# Overrides for Abstract Base Class
    //# net.sourceforge.waters.model.analysis.CommandLineArgument
    @Override
    public void configureAnalyzer(final Object analyzer)
    {
      if (getValue()) {
        final String name =
          ProxyTools.getShortClassName(AbstractModelAnalyzerFactory.this);
        System.err.println
          (name + " supports the following command line options:");
        final List<String> keys = new ArrayList<>(mArgumentMap.keySet());
        Collections.sort(keys);
        for (final String key : keys) {
          final CommandLineArgument arg = mArgumentMap.get(key);
          if (arg.getName().startsWith(key)) {
            arg.dump(System.err, analyzer);
          }
        }
        System.exit(0);
      }
    }
  }


  //#########################################################################
  //# Inner Class HISCArgument
  private static class HISCArgument
    extends CommandLineArgumentFlag
  {
    //#######################################################################
    //# Constructors
    private HISCArgument()
    {
      super("-hisc",
            "Compile as HISC module, only including interfaces\nof low levels");
    }

    //#######################################################################
    //# Overrides for Abstract Base Class
    //# net.sourceforge.waters.model.analysis.CommandLineArgument
    @Override
    public void configureCompiler(final ModuleCompiler compiler)
    {
      if (getValue()) {
        compiler.setHISCCompileMode(HISCCompileMode.HISC_HIGH);
        compiler.setEnabledPropertyNames(null);
      }
    }
  }


  //#########################################################################
  //# Inner Class LimitArgument
  private static class LimitArgument extends CommandLineArgumentInteger
  {
    //#######################################################################
    //# Constructors
    private LimitArgument()
    {
      super("-limit",
            "Maximum number of states/nodes explored");
    }

    //#######################################################################
    //# Overrides for Abstract Base Class
    //# net.sourceforge.waters.model.analysis.CommandLineArgument
    @Override
    public void configureAnalyzer(final Object analyzer)
    {
      final ModelAnalyzer modelAnalyzer = (ModelAnalyzer) analyzer;
      final int limit = getValue();
      modelAnalyzer.setNodeLimit(limit);
    }

  }


  //#########################################################################
  //# Inner Class MarkingArgument
  private static class MarkingArgument
    extends CommandLineArgumentString
  {
    //#######################################################################
    //# Constructors
    private MarkingArgument()
    {
      super("-marking",
            "Name of marking proposition for conflict check");
    }

    //#######################################################################
    //# Overrides for Abstract Base Class
    //# net.sourceforge.waters.model.analysis.CommandLineArgument
    @Override
    public void configureAnalyzer(final Object verifier)
    {
      if (!(verifier instanceof ConflictChecker)) {
        fail("Command line option " + getName() +
             " is only supported for conflict check!");
      }
    }

    @Override
    public void configureCompiler(final ModuleCompiler compiler)
    {
      final String name = getValue();
      final Collection<String> current = compiler.getEnabledPropertyNames();
      final Collection<String> props;
      if (current == null || current.isEmpty()) {
        props = Collections.singletonList(name);
      } else if (current.contains(EventDeclProxy.DEFAULT_MARKING_NAME)) {
        final int size = current.size();
        if (size == 1) {
          props = Collections.singletonList(name);
        } else {
          props = new ArrayList<String>(size);
          for (final String prop : current) {
            if (!prop.equals(EventDeclProxy.DEFAULT_MARKING_NAME)) {
              props.add(prop);
            }
          }
          props.add(name);
        }
      } else {
        final int size = current.size() + 1;
        props = new ArrayList<String>(size);
        props.addAll(current);
        props.add(name);
      }
      compiler.setEnabledPropositionNames(props);
    }

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
  //# Inner Class NoOptimisationArgument
  private static class NoOptimisationArgument
    extends CommandLineArgumentFlag
  {
    //#######################################################################
    //# Constructors
    private NoOptimisationArgument()
    {
      super("-noopt", "Disable compiler optimisation");
    }

    //#######################################################################
    //# Overrides for Abstract Base Class
    //# net.sourceforge.waters.model.analysis.CommandLineArgument
    @Override
    public void configureCompiler(final ModuleCompiler compiler)
    {
      final boolean opt = !getValue();
      compiler.setOptimizationEnabled(opt);
    }
  }


  //#########################################################################
  //# Inner Class NoOutputArgument
  private static class NoOutputArgument
    extends CommandLineArgumentFlag
  {
    //#######################################################################
    //# Constructors
    private NoOutputArgument()
    {
      super("-noout", "Disable counter example/supervisor computation");
    }

    //#######################################################################
    //# Overrides for Abstract Base Class
    //# net.sourceforge.waters.model.analysis.CommandLineArgument
    @Override
    public void configureAnalyzer(final Object analyzer)
    {
      final ModelAnalyzer modelAnalyzer = (ModelAnalyzer) analyzer;
      final boolean enable = !getValue();
      modelAnalyzer.setDetailedOutputEnabled(enable);
    }
  }


  //#########################################################################
  //# Inner Class PreMarkingArgument
  private static class PreMarkingArgument
    extends CommandLineArgumentString
  {
    //#######################################################################
    //# Constructors
    private PreMarkingArgument()
    {
      super("-premarking",
            "Name of precondition marking proposition\n" +
            "for generalised conflict check");
    }

    //#######################################################################
    //# Overrides for Abstract Base Class
    //# net.sourceforge.waters.model.analysis.CommandLineArgument
    @Override
    public void configureAnalyzer(final Object verifier)
    {
      if (!(verifier instanceof ConflictChecker)) {
        fail("Command line option " + getName() +
             " is only supported for conflict check!");
      }
    }

    @Override
    public void configureCompiler(final ModuleCompiler compiler)
    {
      final String name = getValue();
      final Collection<String> current = compiler.getEnabledPropertyNames();
      final Collection<String> props;
      if (current == null || current.isEmpty()) {
        props = new ArrayList<String>(2);
        props.add(EventDeclProxy.DEFAULT_MARKING_NAME);
      } else {
        final int size = current.size() + 1;
        props = new ArrayList<String>(size);
        props.addAll(current);
      }
      props.add(name);
      compiler.setEnabledPropositionNames(props);
    }

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
  //# Inner Class PropertyArgument
  private class PropertyArgument
    extends CommandLineArgumentStringList
  {
    //#######################################################################
    //# Constructors
    private PropertyArgument()
    {
      super("-property",
            "Property for language inclusion check\n" +
            "(can be used more than once)");
      setUsed(true);
    }

    //#######################################################################
    //# Overrides for Abstract Base Class
    //# net.sourceforge.waters.model.analysis.CommandLineArgument
    @Override
    public void configureAnalyzer(final Object verifier)
    {
      final Collection<String> props = getValues();
      if (verifier instanceof LanguageInclusionChecker) {
        if (props.isEmpty()) {
          setUsed(false);
        } else {
          final LanguageInclusionChecker lchecker =
            (LanguageInclusionChecker) verifier;
          final Collection<String> names = getValues();
          final PropertyKindTranslator translator =
            new PropertyKindTranslator(names);
          lchecker.setKindTranslator(translator);
        }
      } else {
        if (!props.isEmpty()) {
          fail("Command line option " + getName() +
               " is only supported for language inclusion!");
        }
      }
    }

    @Override
    public void configureCompiler(final ModuleCompiler compiler)
    {
      final Collection<String> props = getValues();
      compiler.setEnabledPropertyNames(props);
    }


    //#######################################################################
    //# Printing
    @Override
    public void dump(final PrintStream stream, final Object analyzer)
    {
      if (analyzer instanceof LanguageInclusionChecker) {
        super.dump(stream, analyzer);
      }
    }
  }


  //#########################################################################
  //# Inner Class ShortCounterExampleArgument
  private static class ShortCounterExampleArgument
    extends CommandLineArgumentBoolean
  {
    //#######################################################################
    //# Constructor
    private ShortCounterExampleArgument()
    {
      super("-mince", "Request short counterexample (or not)");
    }

    //#######################################################################
    //# Overrides for
    //# net.sourceforge.waters.model.analysis.CommandLineArgument
    @Override
    public void configureAnalyzer(final Object analyzer)
    {
      if (analyzer instanceof ModelVerifier) {
        final ModelVerifier verifier = (ModelVerifier) analyzer;
        final boolean req = getValue();
        verifier.setShortCounterExampleRequested(req);
      } else {
        fail("Command line option " + getName() +
             " is only supported for verification!");
      }
    }
  }


  //#########################################################################
  //# Inner Class TransitionLimitArgument
  private static class TransitionLimitArgument
    extends CommandLineArgumentInteger
  {
    //#######################################################################
    //# Constructors
    private TransitionLimitArgument()
    {
      super("-tlimit",
            "Maximum number of transitions stored");
    }

    //#######################################################################
    //# Overrides for Abstract Base Class
    //# net.sourceforge.waters.model.analysis.CommandLineArgument
    @Override
    public void configureAnalyzer(final Object analyzer)
    {
      final ModelAnalyzer modelAnalyzer = (ModelAnalyzer) analyzer;
      final int limit = getValue();
      modelAnalyzer.setTransitionLimit(limit);
    }
  }


  //#########################################################################
  //# Inner Class PropertyKindTranslator
  private static class PropertyKindTranslator
    implements KindTranslator, Serializable
  {
    //#######################################################################
    //# Constructor
    PropertyKindTranslator(final Collection<String> names)
    {
      mUsedPropertyNames = new THashSet<String>(names);
    }

    //#######################################################################
    //# Interface net.sourceforge.waters.model.analysis.KindTranslator
    @Override
    public ComponentKind getComponentKind(final AutomatonProxy aut)
    {
      final ComponentKind kind = aut.getKind();
      switch (kind) {
      case PLANT:
      case SPEC:
        return ComponentKind.PLANT;
      case PROPERTY:
        final String name = aut.getName();
        if (mUsedPropertyNames.contains(name)) {
          return ComponentKind.SPEC;
        } else {
          return kind;
        }
      default:
        return kind;
      }
    }

    @Override
    public EventKind getEventKind(final EventProxy event)
    {
      final EventKind kind = event.getKind();
      switch (kind) {
      case CONTROLLABLE:
      case UNCONTROLLABLE:
        return EventKind.UNCONTROLLABLE;
      default:
        return kind;
      }
    }

    //#######################################################################
    //# Data Members
    private final Collection<String> mUsedPropertyNames;

    //#######################################################################
    //# Class Constants
    private static final long serialVersionUID = 1L;
  }


  //#########################################################################
  //# Data Members
  private final Map<String,CommandLineArgument> mArgumentMap;


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

}
