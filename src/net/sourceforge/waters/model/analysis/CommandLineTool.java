//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//##########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.model.analysis
//# CLASS:   ControlLoopChecker
//##########################################################################
//# $Id: CommandLineTool.java,v 1.4 2007-08-21 00:03:16 robi Exp $
//##########################################################################

package net.sourceforge.waters.model.analysis;

import java.lang.reflect.Method;
import java.io.File;
import java.io.PrintStream;
import java.util.Formatter;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import net.sourceforge.waters.model.base.DocumentProxy;
import net.sourceforge.waters.model.compiler.CompilerOperatorTable;
import net.sourceforge.waters.model.compiler.ModuleCompiler;
import net.sourceforge.waters.model.des.ProductDESProxy;
import net.sourceforge.waters.model.des.ProductDESProxyFactory;
import net.sourceforge.waters.model.des.TraceProxy;
import net.sourceforge.waters.model.expr.OperatorTable;
import net.sourceforge.waters.model.marshaller.DocumentManager;
import net.sourceforge.waters.model.marshaller.JAXBModuleMarshaller;
import net.sourceforge.waters.model.marshaller.JAXBProductDESMarshaller;
import net.sourceforge.waters.model.module.ModuleProxy;
import net.sourceforge.waters.model.module.ModuleProxyFactory;
import net.sourceforge.waters.valid.ValidUnmarshaller;
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
  public static void main(String[] args)
  {
    try {
      if (args.length < 2) {
        System.err.println
          ("USAGE: java CommandLineTool <factory> [options] <checker> <file> ...");
        System.exit(1);
      }
      
      boolean verbose = true;
      boolean noargs = false;
      final String factoryname = args[0];
      final List<String> arglist = new LinkedList<String>();
      for (int i = 1; i < args.length; i++) {
        final String arg = args[i];
        if (noargs) {
          arglist.add(arg);
        } else if (arg.equals("-q") || arg.equals("-quiet")) {
          verbose = false;
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
        if (verbose) {
          final Method method =
            lclazz.getMethod("logToStream", PrintStream.class);
          method.invoke(null, System.err);
        } else {
          final Method method = lclazz.getMethod("logToNull");
          method.invoke(null);
        }
      } catch (final ClassNotFoundException exception) {
        // No loggers---no trouble ...
      }

      final Class<?> fclazz = loader.loadClass(factoryname);
      final Method getinst = fclazz.getMethod("getInstance", List.class);
      final ModelVerifierFactory factory =
        (ModelVerifierFactory) getinst.invoke(null, arglist);

      final ModuleProxyFactory moduleFactory =
        ModuleElementFactory.getInstance();
      final ProductDESProxyFactory desFactory =
        ProductDESElementFactory.getInstance();
      final OperatorTable optable = CompilerOperatorTable.getInstance();
      final ValidUnmarshaller importer =
        new ValidUnmarshaller(moduleFactory, optable);
      final JAXBModuleMarshaller moduleMarshaller =
        new JAXBModuleMarshaller(moduleFactory, optable);
      final JAXBProductDESMarshaller desMarshaller =
        new JAXBProductDESMarshaller(desFactory);
      final DocumentManager docManager = new DocumentManager();
      docManager.registerUnmarshaller(desMarshaller);
      docManager.registerUnmarshaller(moduleMarshaller);
      docManager.registerUnmarshaller(importer);

      final Iterator<String> iter = arglist.iterator();
      final String checkname = iter.next();
      final String createname = "create" + checkname + "Checker";
      final Method getcheck =
        fclazz.getMethod(createname, ProductDESProxyFactory.class);
      final ModelVerifier checker =
        (ModelVerifier) getcheck.invoke(factory, desFactory);
      final Formatter formatter = new Formatter(System.out);
      
      while (iter.hasNext()) {
        final String name = iter.next();
        final File filename = new File(name);
        final DocumentProxy doc = docManager.load(filename);
        ProductDESProxy des = null;
        if (doc instanceof ProductDESProxy) {
          des = (ProductDESProxy) doc;
        } else {
          final ModuleProxy module = (ModuleProxy) doc;
          final ModuleCompiler compiler =
            new ModuleCompiler(docManager, desFactory, module);
          des = compiler.compile();
        }
        System.out.print(des.getName() + " ... ");
        System.out.flush();

        final long start = System.currentTimeMillis();
        checker.setModel(des);
        checker.run();
        final VerificationResult result = checker.getAnalysisResult();
        final long stop = System.currentTimeMillis();
        final boolean satisfied = result.isSatisfied();
        final int numstates = result.getTotalNumberOfStates();
        final float difftime = 0.001f * (stop - start);

        formatter.format("%b (%d states, %.3f s)\n",
                         satisfied, numstates, difftime);
        if (verbose && !satisfied) {
          System.out.println("Counterexample:");
          final TraceProxy counterex = result.getCounterExample();
          System.out.println(counterex.toString());
          System.out.println();
        }
      }

    } catch (final Throwable exception) {
      System.err.println("FATAL ERROR !!!");
      System.err.println(exception.getClass().getName() +
                         " caught in main()!");
      exception.printStackTrace(System.err);
    }
  }


  //#########################################################################
  //# Class Constants
  private static final String LOGGERFACTORY =
    "org.supremica.log.LoggerFactory";

}
