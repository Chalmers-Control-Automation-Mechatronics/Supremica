//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters Analysis
//# PACKAGE: net.sourceforge.waters.analysis.compositional
//# CLASS:   CompositionalModelAnalyzerFactory
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.analysis.compositional;

import java.io.PrintStream;

import net.sourceforge.waters.model.analysis.AnalysisConfigurationException;
import net.sourceforge.waters.model.analysis.CommandLineArgumentBoolean;
import net.sourceforge.waters.model.analysis.CommandLineArgumentChain;
import net.sourceforge.waters.model.analysis.CommandLineArgumentFlag;
import net.sourceforge.waters.model.analysis.CommandLineArgumentInteger;
import net.sourceforge.waters.model.analysis.CommandLineArgumentString;
import net.sourceforge.waters.model.analysis.EnumFactory;
import net.sourceforge.waters.model.analysis.des.AbstractModelAnalyzerFactory;
import net.sourceforge.waters.model.analysis.des.ModelAnalyzer;
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
    addArgument(new SubsumptionArgument());
    addArgument(new SpecialEventsArgument());
    addArgument(new BlockedEventsArgument());
    addArgument(new FailingEventsArgument());
    addArgument(new SelfloopOnlyEventsArgument());
    addArgument(new DeadlockPruningArgument());
    addArgument(new SecondaryFactoryArgument());
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
    public void configure(final ModelAnalyzer analyzer)
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
    public void configure(final ModelAnalyzer analyzer)
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
    public void configure(final ModelAnalyzer analyzer)
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
    public void configure(final ModelAnalyzer analyzer)
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
    public void configure(final ModelAnalyzer analyzer)
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
    public void configure(final ModelAnalyzer analyzer)
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
    public void configure(final ModelAnalyzer analyzer)
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
    public void dump(final PrintStream stream,
                     final ModelAnalyzer analyzer)
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
    public void configure(final ModelAnalyzer analyzer)
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
    public void dump(final PrintStream stream,
                     final ModelAnalyzer analyzer)
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
    public void configure(final ModelAnalyzer analyzer)
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
    public void dump(final PrintStream stream, final ModelAnalyzer analyzer)
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
    public void configure(final ModelAnalyzer analyzer)
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
    public void configure(final ModelAnalyzer analyzer)
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
    public void configure(final ModelAnalyzer analyzer)
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
    public void configure(final ModelAnalyzer analyzer)
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
    public void configure(final ModelAnalyzer analyzer)
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
    public void configure(final ModelAnalyzer analyzer)
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
    public void configure(final ModelAnalyzer analyzer)
      throws AnalysisConfigurationException
    {
      final ModelAnalyzer secondaryAnalyzer =
        createSecondaryAnalyzer(analyzer);
      final AbstractCompositionalModelAnalyzer composer =
        (AbstractCompositionalModelAnalyzer) analyzer;
      composer.setMonolithicAnalyzer(secondaryAnalyzer);
    }
  }

}
