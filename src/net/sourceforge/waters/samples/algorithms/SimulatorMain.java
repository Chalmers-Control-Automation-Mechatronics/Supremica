//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.samples.algorithms
//# CLASS:   SimulatorMain
//###########################################################################
//# $Id: SimulatorMain.java,v 1.5 2007-04-03 03:53:33 robi Exp $
//###########################################################################

package net.sourceforge.waters.samples.algorithms;

import java.io.File;

import net.sourceforge.waters.model.base.DocumentProxy;
import net.sourceforge.waters.model.compiler.CompilerOperatorTable;
import net.sourceforge.waters.model.compiler.ModuleCompiler;
import net.sourceforge.waters.model.des.ProductDESProxy;
import net.sourceforge.waters.model.des.ProductDESProxyFactory;
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
 * <P>A sample main class to demonstrate how to initialise a
 * a Waters {@link Simulator}.</P>
 *
 * <P>It implements a simple application that accepts a file name on the
 * command line, loads a model from the file, and initialises a simulator
 * for that model.  More precisely, this class can be run as follows.</P>
 *
 * <P><CODE>java SimulatorMain &lt;<I>file</I>&gt;</CODE></P>
 *
 * <P>The following file formats and extensions are supported.</P>
 *
 * <DL>
 * <DT><STRONG>VALID Projects (<CODE>.vprj</CODE>)</STRONG></DT>
 * <DD>When a file <CODE>&lt;<I>project</I>&gt;.vprj</CODE> is requested,
 *     the program will really attempt to open a file called
 *     <CODE>&lt;<I>project</I>&gt;_main.vmod</CODE> in the same
 *     directory. This is exactly the way how VALID works.</DD>
 * <DT><STRONG>VALID Modules (<CODE>.vmod</CODE>)</STRONG></DT>
 * <DD>Only <I>main modules</I> are supported, i.e., files named
 *     <CODE>&lt;<I>project</I>&gt;_main.vmod</CODE>.</DD>
 * <DT><STRONG>Waters Modules (<CODE>.wmod</CODE>)</STRONG></DT>
 * <DT><STRONG>Waters Automata Models (<CODE>.wdes</CODE>)</STRONG></DT>
 * </DL>
 *
 * @author Robi Malik
 */

public class SimulatorMain
{

  //#########################################################################
  //# Constructors
  /**
   * Dummy constructor to prevent instantiation of this class.
   */
  private SimulatorMain()
  {
  }


  //#########################################################################
  //# Main Method for Testing
  /**
   * Main method.
   * This is a main method to demonstrate the use of a simulator.
   * Please refer to the class documentation ({@link SimulatorMain})
   * for more detailed information.
   * @param  args    Array of file names from the command line.
   */
  public static void main(String[] args)
  {
    if (args.length != 1) {
      System.err.println("USAGE: java SimulatorMain <file>");
      System.exit(1);
    }
    final String arg = args[0];

    try {
      // Create and register factories
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
      
      // Load and compile DES
      final File filename = new File(arg);
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

      // Initialise simulator
      new Simulator(des);
      // final Simulator simulator = new Simulator(des);
      System.out.print("Simulator initialised.");

      // ====================================================================
      // Do your simulation here ...
      // ====================================================================

    } catch (final Throwable exception) {
      System.err.println("FATAL ERROR !!!");
      System.err.println(exception.getClass().getName() +
                         " caught in main()!");
      exception.printStackTrace(System.err);
    }
  }

}
