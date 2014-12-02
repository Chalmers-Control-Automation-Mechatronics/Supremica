//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//##########################################################################
//# PROJECT: Waters Analysis
//# PACKAGE: net.sourceforge.waters.model.analysis
//# CLASS:   CommandLineTool
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
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Collections;
import java.util.Formatter;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;

import net.sourceforge.waters.external.valid.ValidUnmarshaller;
import net.sourceforge.waters.model.analysis.des.ConflictChecker;
import net.sourceforge.waters.model.analysis.des.ModelAnalyzer;
import net.sourceforge.waters.model.analysis.des.ModelAnalyzerFactory;
import net.sourceforge.waters.model.analysis.des.ModelAnalyzerFactoryLoader;
import net.sourceforge.waters.model.analysis.des.ModelVerifier;
import net.sourceforge.waters.model.analysis.des.SupervisorSynthesizer;
import net.sourceforge.waters.model.base.DocumentProxy;
import net.sourceforge.waters.model.base.Proxy;
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

public class CommandLineTool
{

  //#########################################################################
  //# Constructors
  /**
   * Dummy constructor to prevent instantiation of this class.
   */
  private CommandLineTool()
  {
  }


  //#########################################################################
  //# Main Method for Testing
  /**
   * Main method.
   * This is a main method to check a set of files for control loop free.
   * Please refer to the class documentation ({@link CommandLineTool})
   * for more detailed information.
   * @param  args    Array of file names from the command line.
   */
  public static void main(final String[] args)
  {
    boolean verbose = true;
    boolean stats = false;
    boolean noargs = false;
    int timeout = -1;
    PrintWriter csv = null;
    final Formatter formatter = new Formatter(System.out);

    try {
      if (args.length < 2) {
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
      String wrapperName = null;

      final String factoryName = args[0];
      final List<String> argList = new LinkedList<String>();
      for (int i = 1; i < args.length; i++) {
        final String arg = args[i];
        if (noargs) {
          argList.add(arg);
        } else if (arg.equals("-q") || arg.equals("-quiet")) {
          verbose = false;
        } else if (arg.equals("-wrapper") && i + 1 < args.length) {
          wrapperName = args[++i];
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

      final ClassLoader loader = CommandLineTool.class.getClassLoader();
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

      final Iterator<String> iter = argList.iterator();
      final String checkName = iter.next();
      iter.remove();

      final ModelAnalyzerFactoryLoader factoryLoader =
        CommandLineArgumentEnum.parse(ModelAnalyzerFactoryLoader.class,
                                      "model analyser factory", factoryName);
      final ModelAnalyzerFactory factory =
        factoryLoader.getModelAnalyzerFactory();
      final Class<? extends ModelAnalyzerFactory> fclazz = factory.getClass();
      final String createname = "create" + checkName;
      final Method getcheck =
        fclazz.getMethod(createname, ProductDESProxyFactory.class);
      final ModelAnalyzer analyzer =
        (ModelAnalyzer) getcheck.invoke(factory, desFactory);
      final ModelAnalyzer wrapper;
      final boolean keepPropositions;
      if (wrapperName == null) {
        wrapper = analyzer;
        keepPropositions = analyzer instanceof ConflictChecker ||
                           analyzer instanceof SupervisorSynthesizer;
      } else {
        @SuppressWarnings("unchecked")
        final Class<ModelVerifier> clazz =
          (Class<ModelVerifier>) loader.loadClass(wrapperName);
        final Package pack = CommandLineTool.class.getPackage();
        final String ifaceName = pack.getName() + ".des." + checkName;
        final Class<?> iface = loader.loadClass(ifaceName);
        final Constructor<ModelVerifier> constructor =
          clazz.getConstructor(iface, ProductDESProxyFactory.class);
        wrapper = constructor.newInstance(analyzer, desFactory);
        keepPropositions = true;
      }
      final Collection<String> empty = Collections.emptyList();
      final ListIterator<String> argIter = argList.listIterator();
      factory.parse(argIter);
      factory.configure(analyzer);
      final Watchdog watchdog = new Watchdog(wrapper, timeout);
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
          if (!keepPropositions) {
            compiler.setEnabledPropositionNames(empty);
          }
          factory.configure(compiler);
          watchdog.addAbortable(compiler);
          des = compiler.compile(bindings);
          watchdog.removeAbortable(compiler);
        }
        System.out.print(des.getName() + " ... ");
        System.out.flush();

        final long start = System.currentTimeMillis();
        wrapper.setModel(des);
        factory.postConfigure(analyzer);
        boolean additions = false;
        try {
          wrapper.run();
          final AnalysisResult result = wrapper.getAnalysisResult();
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
          final AnalysisResult result = wrapper.getAnalysisResult();
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
        final AnalysisResult result = wrapper.getAnalysisResult();
        if (result != null) {
          if (stats) {
            System.out.println(SEPARATOR);
            System.out.println("Statistics:");
            System.out.println("Automata in model: " +
                               des.getAutomata().size());
            System.out.println("Events in model: " +
                               des.getEvents().size());
            result.print(System.out);
            additions = true;
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
    } catch (final InvocationTargetException exception) {
      final Throwable cause = exception.getCause();
      final String msg = cause.getMessage();
      if (msg != null) {
        System.err.println(msg);
      } else {
        System.err.println("FATAL ERROR !!!");
        System.err.print(ProxyTools.getShortClassName(cause));
        System.err.println(" caught in main()!");
        exception.printStackTrace(System.err);
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
    System.err.println
      ("USAGE: java CommandLineTool <factory>  [options] <checker> <file> ...");
    System.exit(1);
  }


  //#########################################################################
  //# Class Constants
  private static final String LOGGERFACTORY =
    "org.supremica.log.LoggerFactory";
  private static final String SEPARATOR =
    "------------------------------------------------------------";

}
