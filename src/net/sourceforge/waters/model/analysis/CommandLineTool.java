//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//##########################################################################
//# PROJECT: Waters Analysis
//# PACKAGE: net.sourceforge.waters.model.analysis
//# CLASS:   CommandLineTool
//##########################################################################
//# $Id$
//##########################################################################

package net.sourceforge.waters.model.analysis;

import java.lang.reflect.Method;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.util.Collection;
import java.util.Collections;
import java.util.Formatter;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import net.sourceforge.waters.analysis.hisc.AbstractSICConflictChecker;
import net.sourceforge.waters.analysis.hisc.SICProperty5Verifier;
import net.sourceforge.waters.analysis.hisc.SICProperty6Verifier;
import net.sourceforge.waters.external.valid.ValidUnmarshaller;
import net.sourceforge.waters.model.base.DocumentProxy;
import net.sourceforge.waters.model.base.ProxyTools;
import net.sourceforge.waters.model.compiler.CompilerOperatorTable;
import net.sourceforge.waters.model.compiler.ModuleCompiler;
import net.sourceforge.waters.model.des.ProductDESProxy;
import net.sourceforge.waters.model.des.ProductDESProxyFactory;
import net.sourceforge.waters.model.des.TraceProxy;
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
    boolean optimise = true;
    boolean noargs = false;
    int timeout = -1;
    PrintWriter csv = null;

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
      ModelVerifier wrapper = null;

      final String factoryname = args[0];
      final List<String> arglist = new LinkedList<String>();
      for (int i = 1; i < args.length; i++) {
        final String arg = args[i];
        if (noargs) {
          arglist.add(arg);
        } else if (arg.equals("-q") || arg.equals("-quiet")) {
          verbose = false;
        } else if (arg.equals("-sic5")) {
          wrapper = new SICProperty5Verifier(desFactory);
        } else if (arg.equals("-sic6")) {
          wrapper = new SICProperty6Verifier(desFactory);
        } else if (arg.equals("-stats")) {
          stats = true;
        } else if (arg.equals("-noopt")) {
          optimise = false;
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
            arglist.add(arg);
          }
        } else if (arg.equals("--")) {
          noargs = true;
          arglist.add(arg);
        } else {
          arglist.add(arg);
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

      final Iterator<String> iter = arglist.iterator();
      final String checkname = iter.next();
      iter.remove();

      final Class<?> fclazz = loader.loadClass(factoryname);
      final Method getinst = fclazz.getMethod("getInstance", List.class);
      final ModelVerifierFactory factory =
        (ModelVerifierFactory) getinst.invoke(null, arglist);
      final String createname = "create" + checkname + "Checker";
      final Method getcheck =
        fclazz.getMethod(createname, ProductDESProxyFactory.class);
      final ModelVerifier checker =
        (ModelVerifier) getcheck.invoke(factory, desFactory);
      final boolean noProperties =
        !(checker instanceof LanguageInclusionChecker);
      final boolean noPropositions = !(checker instanceof ConflictChecker);
      if (wrapper == null) {
        wrapper = checker;
      } else if (checker instanceof ConflictChecker) {
        final AbstractSICConflictChecker wwrapper =
          (AbstractSICConflictChecker) wrapper;
        final ConflictChecker cchecker = (ConflictChecker) checker;
        wwrapper.setConflictChecker(cchecker);
      } else {
        CommandLineArgument.fail
          ("SIC property check requires a conflict checker, " +
           "but none was configured.");
      }
      final Watchdog watchdog = new Watchdog(wrapper, timeout);
      final Collection<String> empty = Collections.emptyList();
      final List<String> filenames = factory.configure(checker);

      final Formatter formatter = new Formatter(System.out);
      boolean first = true;
      for (final String name : filenames) {
        final File filename = new File(name);
        final DocumentProxy doc = docManager.load(filename);
        ProductDESProxy des = null;
        if (doc instanceof ProductDESProxy) {
          des = (ProductDESProxy) doc;
        } else {
          final ModuleProxy module = (ModuleProxy) doc;
          final ModuleCompiler compiler =
            new ModuleCompiler(docManager, desFactory, module);
          compiler.setOptimizationEnabled(optimise);
          if (noProperties) {
            compiler.setEnabledPropertyNames(empty);
          }
          if (noPropositions) {
            compiler.setEnabledPropositionNames(empty);
          }
          factory.configure(compiler);
          des = compiler.compile(bindings);
        }
        System.out.print(des.getName() + " ... ");
        System.out.flush();

        final long start = System.currentTimeMillis();
        wrapper.setModel(des);
        factory.postConfigure(checker);
        boolean additions = false;
        try {
          watchdog.launch();
          final VerificationResult result = wrapper.getAnalysisResult();
          final long stop = System.currentTimeMillis();
          final boolean satisfied = result.isSatisfied();
          final double numstates = result.getTotalNumberOfStates();
          final float difftime = 0.001f * (stop - start);
          final int numnodes = result.getPeakNumberOfNodes();
          if (numstates < 0 && numnodes < 0) {
            formatter.format("%b (%.3f s)\n", satisfied, difftime);
          } else if (numnodes < 0 || (int) numnodes == (int) numstates) {
            formatter.format("%b (%.0f states, %.3f s)\n",
                             satisfied, numstates, difftime);
          } else if (numstates < 0) {
            formatter.format("%b (%d nodes, %.3f s)\n",
                             satisfied, numnodes, difftime);
          } else {
            formatter.format("%b (%.0f states, %d nodes, %.3f s)\n",
                             satisfied, numstates, numnodes, difftime);
          }
          if (verbose && !satisfied) {
            final TraceProxy counterex = result.getCounterExample();
            if (counterex != null) {
              System.out.println(SEPARATOR);
              System.out.println("Counterexample:");
              System.out.print(counterex.toString());
              additions = true;
            }
          }
        } catch (final OutOfMemoryError error) {
          final long stop = System.currentTimeMillis();
          final float difftime = 0.001f * (stop - start);
          formatter.format("OUT OF MEMORY (%.3f s)\n", difftime);
          final VerificationResult result = wrapper.getAnalysisResult();
          if (result != null) {
            final OverflowException overflow = new OverflowException(error);
            result.setException(overflow);
          }
        } catch (final OverflowException overflow) {
          final long stop = System.currentTimeMillis();
          final float difftime = 0.001f * (stop - start);
          formatter.format("OVERFLOW (%.3f s)\n", difftime);
        } catch (final AbortException abort) {
          final long stop = System.currentTimeMillis();
          final float difftime = 0.001f * (stop - start);
          formatter.format("TIMEOUT (%.3f s)\n", difftime);
        }
        final VerificationResult result = wrapper.getAnalysisResult();
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

    } catch (final WatersUnmarshalException exception) {
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
  //# Inner Class Watchdog
  private static class Watchdog implements Runnable {

    //#######################################################################
    //# Constructor
    private Watchdog(final ModelVerifier checker, final int timeout)
    {
      mChecker = checker;
      mTimeoutMillis = timeout >= 0 ? 1000L * timeout : -1;
    }

    //#######################################################################
    //# Invocation
    private boolean launch()
    throws AnalysisException
    {
      if (mTimeoutMillis >= 0) {
        final Thread thread = new Thread(this);
        try {
          thread.start();
          return mChecker.run();
        } finally {
          thread.interrupt();
        }
      } else {
        return mChecker.run();
      }
    }

    //#######################################################################
    //# Interface java.lang.Runnable
    public void run()
    {
      try {
        Thread.sleep(mTimeoutMillis);
        mChecker.requestAbort();
      } catch (final InterruptedException exception) {
        // No problem ...
      }
    }

    //#######################################################################
    //# Data Members
    private final ModelVerifier mChecker;
    private final long mTimeoutMillis;

  }


  //#########################################################################
  //# Class Constants
  private static final String LOGGERFACTORY =
    "org.supremica.log.LoggerFactory";
  private static final String SEPARATOR =
    "------------------------------------------------------------";

}
