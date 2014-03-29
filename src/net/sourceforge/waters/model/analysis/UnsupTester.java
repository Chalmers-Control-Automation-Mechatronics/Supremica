//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//##########################################################################
//# PROJECT: Waters Analysis
//# PACKAGE: net.sourceforge.waters.model.analysis
//# CLASS:   UnsupTester
//##########################################################################
//# $Id$
//##########################################################################

package net.sourceforge.waters.model.analysis;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.lang.reflect.Method;
import java.util.Formatter;
import java.util.LinkedList;
import java.util.List;

import net.sourceforge.waters.analysis.abstraction.CertainUnsupervisabilityTRSimplifier;
import net.sourceforge.waters.analysis.abstraction.HalfWaySynthesisTRSimplifier;
import net.sourceforge.waters.analysis.abstraction.TRSimplifierStatistics;
import net.sourceforge.waters.analysis.compositional.AbstractCompositionalModelAnalyzer.PreselectingMethod;
import net.sourceforge.waters.analysis.compositional.AutomataSynthesisAbstractionProcedureFactory;
import net.sourceforge.waters.analysis.compositional.CompositionalAutomataSynthesisResult;
import net.sourceforge.waters.analysis.compositional.CompositionalAutomataSynthesizer;
import net.sourceforge.waters.analysis.compositional.CompositionalModelVerifierFactory;
import net.sourceforge.waters.analysis.compositional.SelectionHeuristicCreator;
import net.sourceforge.waters.external.valid.ValidUnmarshaller;
import net.sourceforge.waters.model.analysis.des.ModelVerifierFactory;
import net.sourceforge.waters.model.base.DocumentProxy;
import net.sourceforge.waters.model.base.ProxyTools;
import net.sourceforge.waters.model.compiler.CompilerOperatorTable;
import net.sourceforge.waters.model.compiler.ModuleCompiler;
import net.sourceforge.waters.model.des.ProductDESProxy;
import net.sourceforge.waters.model.des.ProductDESProxyFactory;
import net.sourceforge.waters.model.expr.EvalException;
import net.sourceforge.waters.model.expr.ExpressionParser;
import net.sourceforge.waters.model.expr.OperatorTable;
import net.sourceforge.waters.model.marshaller.DocumentManager;
import net.sourceforge.waters.model.marshaller.JAXBModuleMarshaller;
import net.sourceforge.waters.model.marshaller.JAXBProductDESMarshaller;
import net.sourceforge.waters.model.marshaller.WatersUnmarshalException;
import net.sourceforge.waters.model.module.ModuleProxy;
import net.sourceforge.waters.model.module.ModuleProxyFactory;
import net.sourceforge.waters.model.module.ParameterBindingProxy;
import net.sourceforge.waters.model.module.SimpleExpressionProxy;
import net.sourceforge.waters.plain.des.ProductDESElementFactory;
import net.sourceforge.waters.plain.module.ModuleElementFactory;


/**
 * @author Robi Malik
 */

public class UnsupTester
{

  //#########################################################################
  //# Constructors
  /**
   * Dummy constructor to prevent instantiation of this class.
   */
  private UnsupTester()
  {
  }


  //#########################################################################
  //# Main Method for Testing
  /**
   * Main method.
   * This is a main method to check a set of files for control loop free.
   * Please refer to the class documentation ({@link UnsupTester})
   * for more detailed information.
   * @param  args    Array of file names from the command line.
   */
  public static void main(final String[] args)
  {
    boolean verbose = true;
    boolean stats = false;
    boolean noargs = false;
    String presel = null;
    String sel = null;
    AutomataSynthesisAbstractionProcedureFactory proc =
      AutomataSynthesisAbstractionProcedureFactory.WSOE;
    int timeout = -1;
    PrintWriter csv = null;
    final Formatter formatter = new Formatter(System.out);

    try {
      if (args.length < 1) {
        usage();
      }

      final ModuleProxyFactory moduleFactory =
        ModuleElementFactory.getInstance();
      final ProductDESProxyFactory desFactory =
        ProductDESElementFactory.getInstance();
      final OperatorTable optable = CompilerOperatorTable.getInstance();
      final ExpressionParser parser =
        new ExpressionParser(moduleFactory, optable);
      List<ParameterBindingProxy> bindings = null;

      final List<String> argList = new LinkedList<>();
      for (int i = 0; i < args.length; i++) {
        final String arg = args[i];
        if (noargs) {
          argList.add(arg);
        } else if (arg.equals("-q") || arg.equals("-quiet")) {
          verbose = false;
        } else if (arg.equals("-unsup")) {
          proc = AutomataSynthesisAbstractionProcedureFactory.WSOE_UNSUP;
        } else if (arg.equals("-presel") && i + 1 < args.length) {
          i++;
          presel = args[i];
        } else if (arg.equals("-sel") && i + 1 < args.length) {
          i++;
          sel = args[i];
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

      final ClassLoader loader = UnsupTester.class.getClassLoader();
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
      final JAXBProductDESMarshaller desMarshaller =
        new JAXBProductDESMarshaller(desFactory);
      final DocumentManager docManager = new DocumentManager();
      docManager.registerUnmarshaller(desMarshaller);
      docManager.registerUnmarshaller(moduleMarshaller);
      docManager.registerUnmarshaller(importer);

      final CompositionalAutomataSynthesizer synthesizer =
        new CompositionalAutomataSynthesizer(desFactory, proc);
      if (presel != null) {
        final ListedEnumFactory<PreselectingMethod> factory =
          synthesizer.getPreselectingMethodFactory();
        final PreselectingMethod method = factory.getEnumValue(presel);
        if (method != null) {
          synthesizer.setPreselectingMethod(method);
        }
      }
      if (sel != null) {
        final ListedEnumFactory<SelectionHeuristicCreator> factory =
          synthesizer.getSelectionHeuristicFactory();
        final SelectionHeuristicCreator creator = factory.getEnumValue(sel);
        if (creator != null) {
          synthesizer.setSelectionHeuristic(creator);
        }
      }
      final Watchdog watchdog = new Watchdog(synthesizer, timeout);
      if (timeout > 0) {
        watchdog.start();
      }

      boolean first = true;
      for (final String name : argList) {
        final File filename = new File(name);
        final DocumentProxy doc = docManager.load(filename);
        ProductDESProxy des = null;
        if (doc instanceof ProductDESProxy) {
          des = (ProductDESProxy) doc;
        } else {
          final ModuleProxy module = (ModuleProxy) doc;
          final ModuleCompiler compiler =
            new ModuleCompiler(docManager, desFactory, module);
          final ModelVerifierFactory factory =
            CompositionalModelVerifierFactory.getInstance();
          factory.configure(compiler);
          watchdog.addAbortable(compiler);
          des = compiler.compile(bindings);
          watchdog.removeAbortable(compiler);
        }
        System.out.print(des.getName() + " ... ");
        System.out.flush();

        final long start = System.currentTimeMillis();
        synthesizer.setModel(des);
        boolean additions = false;
        try {
          synthesizer.run();
          final CompositionalAutomataSynthesisResult result =
            synthesizer.getAnalysisResult();
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
        } catch (final OutOfMemoryError error) {
          final long stop = System.currentTimeMillis();
          final float difftime = 0.001f * (stop - start);
          formatter.format("OUT OF MEMORY (%.3f s)\n", difftime);
          final CompositionalAutomataSynthesisResult result =
            synthesizer.getAnalysisResult();
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
        final CompositionalAutomataSynthesisResult result =
          synthesizer.getAnalysisResult();
        if (result != null) {
          additions = true;
          System.out.println(SEPARATOR);
          if (stats) {
            System.out.println("Statistics:");
            System.out.println("Automata in model: " +
                               des.getAutomata().size());
            System.out.println("Events in model: " +
                               des.getEvents().size());
            result.print(System.out);
          } else {
            for (final TRSimplifierStatistics simpStats :
                 result.getSimplifierStatistics()) {
              final Class<?> clazz = simpStats.getSimplifierClass();
              if (clazz == HalfWaySynthesisTRSimplifier.class ||
                  clazz == CertainUnsupervisabilityTRSimplifier.class) {
                final int states = simpStats.getInputStates() -
                  simpStats.getOutputStates() - simpStats.getUnchangedStates();
                System.out.println("State reduction: " + states);
                final int trans = simpStats.getInputTransitions() -
                  simpStats.getOutputTransitions() -
                  simpStats.getUnchangedTransitions();
                System.out.println("Transition reduction: " + trans);
              }
            }
          }
          if (csv != null) {
            if (first) {
              csv.print("Model,");
              result.printCSVHorizontalHeadings(csv);
              csv.println();
            }
            csv.print(des.getName() + ',');
            result.printCSVHorizontal(csv);
            csv.println();
          }
        }
        if (additions) {
          System.out.println(SEPARATOR);
        }
        first = false;
      }

    } catch (final EvalException | WatersUnmarshalException | IOException
             exception) {
      System.err.print("FATAL ERROR (");
      System.err.print(ProxyTools.getShortClassName(exception));
      System.err.println(")");
      final String msg = exception.getMessage();
      if (msg != null) {
        System.err.println(exception.getMessage());
      }
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
  private static void usage()
  {
    System.err.println("USAGE: java UnsupTester [options] <file> ...");
    System.exit(1);
  }


  //#########################################################################
  //# Class Constants
  private static final String LOGGERFACTORY =
    "org.supremica.log.LoggerFactory";
  private static final String SEPARATOR =
    "------------------------------------------------------------";

}
