//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//##########################################################################
//# PROJECT: Waters EFSM Analysis
//# PACKAGE: net.sourceforge.waters.analysis.efa.unified
//# CLASS:   UnifiedEFACommandLineTool
//##########################################################################
//# $Id$
//##########################################################################

package net.sourceforge.waters.analysis.efa.unified;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Formatter;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;

import net.sourceforge.waters.analysis.compositional.ChainSelectionHeuristic;
import net.sourceforge.waters.analysis.compositional.SelectionHeuristic;
import net.sourceforge.waters.external.valid.ValidUnmarshaller;
import net.sourceforge.waters.model.analysis.AnalysisAbortException;
import net.sourceforge.waters.model.analysis.AnalysisException;
import net.sourceforge.waters.model.analysis.AnalysisResult;
import net.sourceforge.waters.model.analysis.CommandLineArgument;
import net.sourceforge.waters.model.analysis.CommandLineArgumentBoolean;
import net.sourceforge.waters.model.analysis.CommandLineArgumentEnum;
import net.sourceforge.waters.model.analysis.CommandLineArgumentFlag;
import net.sourceforge.waters.model.analysis.CommandLineArgumentInteger;
import net.sourceforge.waters.model.analysis.CommandLineArgumentString;
import net.sourceforge.waters.model.analysis.EnumFactory;
import net.sourceforge.waters.model.analysis.ListedEnumFactory;
import net.sourceforge.waters.model.analysis.OverflowException;
import net.sourceforge.waters.model.analysis.ProxyResult;
import net.sourceforge.waters.model.analysis.Watchdog;
import net.sourceforge.waters.model.base.Proxy;
import net.sourceforge.waters.model.base.ProxyTools;
import net.sourceforge.waters.model.compiler.CompilerOperatorTable;
import net.sourceforge.waters.model.expr.EvalException;
import net.sourceforge.waters.model.expr.ExpressionParser;
import net.sourceforge.waters.model.expr.OperatorTable;
import net.sourceforge.waters.model.marshaller.DocumentManager;
import net.sourceforge.waters.model.marshaller.JAXBModuleMarshaller;
import net.sourceforge.waters.model.marshaller.WatersUnmarshalException;
import net.sourceforge.waters.model.module.ModuleProxy;
import net.sourceforge.waters.model.module.ModuleProxyFactory;
import net.sourceforge.waters.model.module.ParameterBindingProxy;
import net.sourceforge.waters.model.module.SimpleExpressionProxy;
import net.sourceforge.waters.plain.module.ModuleElementFactory;


/**
 * Prototype command line tool to invoke UnifiedEFAConflictChecker.
 *
 * @author Robi Malik
 */

public class UnifiedEFACommandLineTool
{

  //#########################################################################
  //# Constructors
  /**
   * Dummy constructor to prevent instantiation of this class.
   */
  private UnifiedEFACommandLineTool()
  {
  }


  //#########################################################################
  //# Main Method for Testing
  /**
   * Main method.
   * This is a main method to check whether one or more modules are
   * nonblocking using {@link UnifiedEFAConflictChecker}.
   */
  public static void main(final String[] args)
  {
    mArgumentMap = new LinkedHashMap<>();
    registerArgument(new CompositionSelectionHeuristicArgument());
    registerArgument(new VariableSelectionHeuristicArgument());
    registerArgument(new PreferLocalArgument());
    registerArgument(new SimplifierFactoryArgument());
    registerArgument(new InternalStateLimitArgument());
    registerArgument(new InternalTransitionLimitArgument());
    registerArgument(new HelpArgument());

    boolean verbose = true;
    boolean stats = false;
    boolean noargs = false;
    int timeout = -1;
    PrintWriter csv = null;
    final Formatter formatter = new Formatter(System.out);

    try {
      if (args.length < 1) {
        usage();
      }

      final ModuleProxyFactory moduleFactory =
        ModuleElementFactory.getInstance();
      final OperatorTable optable = CompilerOperatorTable.getInstance();
      final ExpressionParser parser =
        new ExpressionParser(moduleFactory, optable);
      List<ParameterBindingProxy> bindings = null;

      final List<String> argList = new LinkedList<String>();
      for (int i = 0; i < args.length; i++) {
        final String arg = args[i];
        if (noargs) {
          argList.add(arg);
        } else if (arg.equals("-q") || arg.equals("-quiet")) {
          verbose = false;
        } else if (arg.equals("-stats")) {
          stats = true;
        } else if (arg.equals("-timeout") && i + 1 < args.length) {
          try {
            timeout = Integer.parseInt(args[++i]);
          } catch (final NumberFormatException exception) {
            usage();
          }
        } else if (arg.equals("-csv") && i + 1 < args.length) {
          final String csvname = args[++i];
          final OutputStream csvstream = new FileOutputStream(csvname, true);
          csv = new PrintWriter(csvstream);
        } else if (arg.startsWith("-D")) {
          final int eqpos = arg.indexOf('=', 2);
          if (eqpos > 2) {
            final String name = arg.substring(2, eqpos);
            final String text = arg.substring(eqpos + 1);
            final SimpleExpressionProxy expr = parser.parse(text);
            final ParameterBindingProxy binding =
              moduleFactory.createParameterBindingProxy(name, expr);
            if (bindings == null) {
              bindings = new LinkedList<ParameterBindingProxy>();
            }
            bindings.add(binding);
          } else {
            argList.add(arg);
          }
        } else if (arg.equals("--")) {
          noargs = true;
          argList.add(arg);
        } else {
          argList.add(arg);
        }
      }

      final ClassLoader loader = UnifiedEFACommandLineTool.class.getClassLoader();
      try {
        final Class<?> lclazz = loader.loadClass(LOGGERFACTORY);
        final Method method0 = lclazz.getMethod("getInstance");
        final Object loggerfactory = method0.invoke(null);
        if (verbose) {
          final Method method =
            lclazz.getMethod("logToStream", PrintStream.class);
          method.invoke(loggerfactory, System.err);
        } else {
          final Method method = lclazz.getMethod("logToNull");
          method.invoke(loggerfactory);
        }
      } catch (final ClassNotFoundException exception) {
        // No loggers---no trouble ...
      }

      final ValidUnmarshaller importer =
        new ValidUnmarshaller(moduleFactory, optable);
      final JAXBModuleMarshaller moduleMarshaller =
        new JAXBModuleMarshaller(moduleFactory, optable, false);
      final DocumentManager docManager = new DocumentManager();
      docManager.registerUnmarshaller(moduleMarshaller);
      docManager.registerUnmarshaller(importer);

      final UnifiedEFAConflictChecker checker =
        new UnifiedEFAConflictChecker(moduleFactory);
      checker.setDocumentManager(docManager);
      checker.setBindings(bindings);

      final ListIterator<String> argIter = argList.listIterator();
      while (argIter.hasNext()) {
        final String name = argIter.next();
        final CommandLineArgument arg = mArgumentMap.get(name);
        if (arg != null) {
          arg.parse(argIter);
        } else if (name.startsWith("-")) {
          System.err.println("Unsupported option " + name +
                             ". Try -help to see available options.");
          System.exit(1);
        }
      }
      for (final CommandLineArgument arg : mArgumentMap.values()) {
        if (arg.isUsed()) {
          arg.configureAnalyzer(checker);
        }
      }

      final Watchdog watchdog = new Watchdog(checker, timeout);
      if (timeout > 0) {
        watchdog.start();
      }

      boolean first = true;
      for (final String name : argList) {
        final File filename = new File(name);
        final ModuleProxy module = (ModuleProxy) docManager.load(filename);
        System.out.print(module.getName() + " ... ");
        System.out.flush();

        final long start = System.currentTimeMillis();
        checker.setModel(module);
        boolean additions = false;
        try {
          checker.run();
          final AnalysisResult result = checker.getAnalysisResult();
          final long stop = System.currentTimeMillis();
          final boolean satisfied = result.isSatisfied();
          final double numstates = result.getTotalNumberOfStates();
          final float difftime = 0.001f * (stop - start);
          final int numnodes = result.getPeakNumberOfNodes();
          if (numstates < 0 && numnodes < 0) {
            formatter.format("%b (%.3f s)\n", satisfied, difftime);
          } else if (numnodes < 0 || numnodes == (int) numstates) {
            formatter.format("%b (%.0f states, %.3f s)\n",
                             satisfied, numstates, difftime);
          } else if (numstates < 0) {
            formatter.format("%b (%d nodes, %.3f s)\n",
                             satisfied, numnodes, difftime);
          } else {
            formatter.format("%b (%.0f states, %d nodes, %.3f s)\n",
                             satisfied, numstates, numnodes, difftime);
          }
          if (verbose && result instanceof ProxyResult<?>) {
            final ProxyResult<?> proxyResult = (ProxyResult<?>) result;
            final Proxy proxy = proxyResult.getComputedProxy();
            if (proxy != null) {
              final String description = proxyResult.getResultDescription();
              System.out.println(SEPARATOR);
              System.out.println(description + ":");
              System.out.print(proxy);
              additions = true;
            }
          }
        } catch (final OutOfMemoryError error) {
          final long stop = System.currentTimeMillis();
          final float difftime = 0.001f * (stop - start);
          formatter.format("OUT OF MEMORY (%.3f s)\n", difftime);
          final AnalysisResult result = checker.getAnalysisResult();
          if (result != null) {
            final OverflowException overflow = new OverflowException(error);
            result.setException(overflow);
          }
        } catch (final OverflowException overflow) {
          final long stop = System.currentTimeMillis();
          final float difftime = 0.001f * (stop - start);
          formatter.format("OVERFLOW (%.3f s)\n", difftime);
        } catch (final AnalysisAbortException abort) {
          final long stop = System.currentTimeMillis();
          final float difftime = 0.001f * (stop - start);
          formatter.format("TIMEOUT (%.3f s)\n", difftime);
        }
        final AnalysisResult result = checker.getAnalysisResult();
        if (result != null) {
          if (stats) {
            System.out.println(SEPARATOR);
            System.out.println("Statistics:");
            result.print(System.out);
            additions = true;
          }
          if (csv != null) {
            if (first) {
              csv.print("Model,");
              result.printCSVHorizontalHeadings(csv);
              csv.println();
            }
            csv.print(module.getName() + ',');
            result.printCSVHorizontal(csv);
            csv.println();
          }
        }
        if (additions) {
          System.out.println(SEPARATOR);
        }
        first = false;
      }

    } catch (final EvalException | AnalysisException |
                   WatersUnmarshalException | IOException exception) {
      showSupportedException(exception);
    } catch (final InvocationTargetException exception) {
      final Throwable cause = exception.getCause();
      showSupportedException(cause);
    } catch (final Throwable exception) {
      System.err.println("FATAL ERROR !!!");
      System.err.print(ProxyTools.getShortClassName(exception));
      System.err.println(" caught in main()!");
      exception.printStackTrace(System.err);
    } finally {
      if (csv != null) {
        csv.close();
      }
      formatter.close();
    }
  }


  //#########################################################################
  //# Auxiliary Methods
  private static void registerArgument(final CommandLineArgument argument)
  {
    for (final String name : argument.getNames()) {
      mArgumentMap.put(name, argument);
    }
  }

  private static void usage()
  {
    System.err.println
      ("USAGE: java UnifiedEFACommandLineTool [options] <file> ...");
    System.exit(1);
  }

  private static void showSupportedException(final Throwable exception)
  {
    System.err.print("FATAL ERROR (");
    System.err.print(ProxyTools.getShortClassName(exception));
    System.err.println(")");
    final String msg = exception.getMessage();
    if (msg != null) {
      System.err.println(exception.getMessage());
    }
  }


  //#########################################################################
  //# Inner Class AbstractHeuristicArgument
  private static abstract class AbstractHeuristicArgument<T extends Comparable<? super T>>
    extends CommandLineArgumentString
  {

    //#######################################################################
    //# Constructor
    private AbstractHeuristicArgument(final String name,
                                      final String description,
                                      final EnumFactory<SelectionHeuristic<T>> factory)
    {
      super(name, description);
      mFactory = factory;
    }

    //#######################################################################
    //# Printing
    @Override
    public void dump(final PrintStream stream, final Object analyzer)
    {
      super.dump(stream, analyzer);
      mFactory.dumpEnumeration(stream, INDENT);
    }

    //#######################################################################
    //# Auxiliary Methods
    SelectionHeuristic<T> parse()
    {
      final String name = getValue();
      final String[] parts = name.split(",");
      final SelectionHeuristic<T> chain;
      if (parts.length == 1) {
        final SelectionHeuristic<T> heuristic = mFactory.getEnumValue(name);
        if (heuristic == null) {
          System.err.println("Bad value for " + getName() + " option!");
          mFactory.dumpEnumeration(System.err, 0);
          System.exit(1);
        }
        chain = new ChainSelectionHeuristic<T>(heuristic);
      } else {
        @SuppressWarnings("unchecked")
        final SelectionHeuristic<T>[] heuristics =
          new SelectionHeuristic[parts.length];
        for (int i = 0; i < parts.length; i++) {
          heuristics[i] = mFactory.getEnumValue(parts[i]);
          if (heuristics[i]  == null) {
            System.err.println("Bad value for " + getName() + " option!");
            mFactory.dumpEnumeration(System.err, 0);
            System.exit(1);
          }
        }
        chain = new ChainSelectionHeuristic<T>(heuristics);
      }
      return chain;
    }

    //#######################################################################
    //# Data Members
    private final EnumFactory<SelectionHeuristic<T>> mFactory;
  }


  //#########################################################################
  //# Inner Class CompositionSelectionHeuristicArgument
  private static class CompositionSelectionHeuristicArgument
    extends AbstractHeuristicArgument<UnifiedEFACandidate>
  {

    //#######################################################################
    //# Constructor
    private CompositionSelectionHeuristicArgument()
    {
      super("-compsel", "Composition selection heuristic",
            new CompositionSelectionHeuristicFactory());
    }

    //#######################################################################
    //# Overrides for Abstract Base Class
    //# net.sourceforge.waters.model.analysis.CommandLineArgument
    @Override
    public void configureAnalyzer(final Object analyzer)
    {
      final UnifiedEFAConflictChecker checker =
        (UnifiedEFAConflictChecker) analyzer;
      final SelectionHeuristic<UnifiedEFACandidate> heuristic = parse();
      checker.setCompositionSelectionHeuristic(heuristic);
    }
  }


  //#########################################################################
  //# Inner Class CompositionSelectionHeuristicFactory
  private static class CompositionSelectionHeuristicFactory
    extends ListedEnumFactory<SelectionHeuristic<UnifiedEFACandidate>>
  {
    //#######################################################################
    //# Constructor
    private CompositionSelectionHeuristicFactory()
    {
      register(new CompositionSelectionHeuristicMinS());
      register(new CompositionSelectionHeuristicMinF());
    }
  }


  //#########################################################################
  //# Inner Class VariableSelectionHeuristicArgument
  private static class VariableSelectionHeuristicArgument
    extends AbstractHeuristicArgument<UnifiedEFAVariable>
  {

    //#######################################################################
    //# Constructor
    private VariableSelectionHeuristicArgument()
    {
      super("-varsel", "Variable selection heuristic",
            new VariableSelectionHeuristicFactory());
    }

    //#######################################################################
    //# Overrides for Abstract Base Class
    //# net.sourceforge.waters.model.analysis.CommandLineArgument
    @Override
    public void configureAnalyzer(final Object analyzer)
    {
      final UnifiedEFAConflictChecker checker =
        (UnifiedEFAConflictChecker) analyzer;
      final SelectionHeuristic<UnifiedEFAVariable> heuristic = parse();
      checker.setVariableSelectionHeuristic(heuristic);
    }
  }


  //#########################################################################
  //# Inner Class VariableSelectionHeuristicFactory
  private static class VariableSelectionHeuristicFactory
    extends ListedEnumFactory<SelectionHeuristic<UnifiedEFAVariable>>
  {
    //#######################################################################
    //# Constructor
    private VariableSelectionHeuristicFactory()
    {
      register(new VariableSelectionHeuristicMaxE());
      register(new VariableSelectionHeuristicMaxS());
      register(new VariableSelectionHeuristicMinD());
    }
  }


  //#########################################################################
  //# Inner Class PreferLocalArgument
  private static class PreferLocalArgument
    extends CommandLineArgumentBoolean
  {
    //#######################################################################
    //# Constructor
    private PreferLocalArgument()
    {
      super("-loc", "Enable or disable preference for local variables");
    }

    //#######################################################################
    //# Overrides for Abstract Base Class
    //# net.sourceforge.waters.model.analysis.CommandLineArgument
    @Override
    public void configureAnalyzer(final Object analyzer)
    {
      final boolean prefer = getValue();
      final UnifiedEFAConflictChecker checker =
        (UnifiedEFAConflictChecker) analyzer;
      checker.setUsesLocalVariable(prefer);
    }
  }


  //#########################################################################
  //# Inner Class SimplifierFactoryArgument
  private static class SimplifierFactoryArgument
    extends CommandLineArgumentEnum<UnifiedEFASimplifierFactory>
  {
    //#######################################################################
    //# Constructor
    private SimplifierFactoryArgument()
    {
      super("-method", "Abstraction sequence used for simplification",
            UnifiedEFASimplifierFactory.class);
    }

    //#######################################################################
    //# Overrides for Abstract Base Class
    //# net.sourceforge.waters.model.analysis.CommandLineArgument
    @Override
    public void configureAnalyzer(final Object analyzer)
    {
      final UnifiedEFAConflictChecker checker =
        (UnifiedEFAConflictChecker) analyzer;
      final UnifiedEFASimplifierFactory factory = getValue();
      checker.setSimplifierFactory(factory);
    }
  }


  //#########################################################################
  //# Inner Class InternalStateLimitArgument
  private static class InternalStateLimitArgument
    extends CommandLineArgumentInteger
  {
    //#######################################################################
    //# Constructor
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
      final UnifiedEFAConflictChecker checker =
        (UnifiedEFAConflictChecker) analyzer;
      checker.setInternalStateLimit(limit);
    }
  }


  //#########################################################################
  //# Inner Class InternalTransitionLimitArgument
  private static class InternalTransitionLimitArgument
    extends CommandLineArgumentInteger
  {
    //#######################################################################
    //# Constructors
    private InternalTransitionLimitArgument()
    {
      super("-itlimit",
            "Maximum number of transitions constructed in abstraction\nattempts");
    }

    //#######################################################################
    //# Overrides for Abstract Base Class
    //# net.sourceforge.waters.model.analysis.CommandLineArgument
    @Override
    public void configureAnalyzer(final Object analyzer)
    {
      final int limit = getValue();
      final UnifiedEFAConflictChecker checker =
        (UnifiedEFAConflictChecker) analyzer;
      checker.setInternalTransitionLimit(limit);
    }
  }


  //#########################################################################
  //# Inner Class HelpArgument
  private static class HelpArgument extends CommandLineArgumentFlag
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
        final String name = ProxyTools.getShortClassName(analyzer);
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
  //# Data Members
  private static Map<String,CommandLineArgument> mArgumentMap;


  //#########################################################################
  //# Class Constants
  private static final String LOGGERFACTORY =
    "org.supremica.log.LoggerFactory";
  private static final String SEPARATOR =
    "------------------------------------------------------------";

}
