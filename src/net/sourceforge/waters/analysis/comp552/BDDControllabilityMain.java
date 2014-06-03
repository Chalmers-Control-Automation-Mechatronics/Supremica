//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//##########################################################################
//# PROJECT: COMP452/552-14A Assignment 3
//# PACKAGE: net.sourceforge.waters.analysis.comp552
//# CLASS:   BDDControllabilityMain
//##########################################################################
//# This file contains the work of:
//# Family name:
//# First name:
//# Student ID:
//###########################################################################
//# You are welcome to edit this file,
//# but please DO NOT CHANGE the way how the main class communicates
//# with the BDDControllabilityChecker class.
//###########################################################################


package net.sourceforge.waters.analysis.comp552;

import java.io.File;
import java.util.Collection;
import java.util.Collections;

import net.sourceforge.waters.model.base.DocumentProxy;
import net.sourceforge.waters.model.base.ProxyTools;
import net.sourceforge.waters.model.compiler.CompilerOperatorTable;
import net.sourceforge.waters.model.compiler.ModuleCompiler;
import net.sourceforge.waters.model.des.ProductDESProxy;
import net.sourceforge.waters.model.des.ProductDESProxyFactory;
import net.sourceforge.waters.model.des.SafetyTraceProxy;
import net.sourceforge.waters.model.expr.OperatorTable;
import net.sourceforge.waters.model.marshaller.DocumentManager;
import net.sourceforge.waters.model.marshaller.JAXBModuleMarshaller;
import net.sourceforge.waters.model.marshaller.JAXBProductDESMarshaller;
import net.sourceforge.waters.model.module.ModuleProxy;
import net.sourceforge.waters.model.module.ModuleProxyFactory;
import net.sourceforge.waters.plain.des.ProductDESElementFactory;
import net.sourceforge.waters.plain.module.ModuleElementFactory;


/**
 * <P>A sample main class for testing the {@link BDDControllabilityChecker}
 * class.</P>
 *
 * <P>This class provides a simple application that accepts a list
 * of file names on the command line, loads a model from each file,
 * passes it to a controllability checker, and prints the result.
 * More precisely, this class can be run as follows.</P>
 *
 * <P><CODE>java BDDControllabilityMain
 * &lt;<I>file1</I>&gt; &lt;<I>file2</I>&gt; ...</CODE></P>
 *
 * <P>Each file should contain a Waters module (<CODE>.wmod</CODE> extension)
 * or a Waters product DES (<CODE>.wdes</CODE> extension).
 *
 * @author Robi Malik
 */

public class BDDControllabilityMain
{

  //#########################################################################
  //# Constructors
  /**
   * Dummy constructor to prevent instantiation of this class.
   */
  private BDDControllabilityMain()
  {
  }


  //#########################################################################
  //# Main Method for Testing
  /**
   * Main method.
   * This is a main method to check a set of files for conflicts.
   * Please refer to the class documentation ({@link BDDControllabilityMain})
   * for more detailed information.
   * @param  args    Array of file names from the command line.
   */
  public static void main(final String[] args)
  {
    try {
      final ModuleProxyFactory moduleFactory =
        ModuleElementFactory.getInstance();
      final ProductDESProxyFactory desFactory =
        ProductDESElementFactory.getInstance();
      final OperatorTable optable = CompilerOperatorTable.getInstance();
      final JAXBModuleMarshaller moduleMarshaller =
        new JAXBModuleMarshaller(moduleFactory, optable, false);
      final JAXBProductDESMarshaller desMarshaller =
        new JAXBProductDESMarshaller(desFactory);
      final DocumentManager docManager = new DocumentManager();
      docManager.registerUnmarshaller(desMarshaller);
      docManager.registerUnmarshaller(moduleMarshaller);
      final Collection<String> empty = Collections.emptyList();

      for (int i = 0; i < args.length; i++) {
        final String name = args[i];
        final File filename = new File(name);
        final DocumentProxy doc = docManager.load(filename);
        final ProductDESProxy des;
        if (doc instanceof ProductDESProxy) {
          des = (ProductDESProxy) doc;
        } else {
          final ModuleProxy module = (ModuleProxy) doc;
          final ModuleCompiler compiler =
            new ModuleCompiler(docManager, desFactory, module);
          compiler.setEnabledPropertyNames(empty);
          des = compiler.compile();
        }
        final BDDControllabilityChecker checker =
          new BDDControllabilityChecker(des, desFactory);
        System.out.print(des.getName() + " ... ");
        System.out.flush();

        final boolean result = checker.run();
        if (result) {
          System.out.println("controllable");
        } else {
          System.out.println("NOT CONTROLLABLE");
          System.out.println("Counterexample:");
          final SafetyTraceProxy counterex = checker.getCounterExample();
          System.out.println(counterex.toString());
        }
        System.gc();  // Garbage collect all BDDs so init() can be called again.
      }

    } catch (final Throwable exception) {
      System.err.println("FATAL ERROR !!!");
      final String ename = ProxyTools.getShortClassName(exception);
      System.err.println(ename + " caught in main()!");
      exception.printStackTrace(System.err);
    }
  }

}
