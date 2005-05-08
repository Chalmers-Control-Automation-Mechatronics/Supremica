//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.analysis
//# CLASS:   ControllabilityChecker
//###########################################################################
//# $Id: ControllabilityMain.java,v 1.1 2005-05-08 00:24:31 robi Exp $
//###########################################################################

package net.sourceforge.waters.analysis;

import java.io.File;
import java.util.Iterator;
import java.util.List;

import net.sourceforge.waters.model.base.DocumentManager;
import net.sourceforge.waters.model.base.DocumentProxy;
import net.sourceforge.waters.model.base.ProxyMarshaller;
import net.sourceforge.waters.model.compiler.ModuleCompiler;
import net.sourceforge.waters.model.des.EventProxy;
import net.sourceforge.waters.model.des.ProductDESMarshaller;
import net.sourceforge.waters.model.des.ProductDESProxy;
import net.sourceforge.waters.model.module.ModuleMarshaller;
import net.sourceforge.waters.model.module.ModuleProxy;
import net.sourceforge.waters.valid.ValidUnmarshaller;


/**
 * <P>A sample main class for testing the {@link ControllabilityChecker}
 * class.</P>
 *
 * <P>This class provides a simple application that accepts a list
 * of file names on the command line, loads a model from each file,
 * passes it to a controllability checker, and prints the result.
 * More precisely, this class can be run as follows.</P>
 *
 * <P><CODE>java ControllabilityMain
 * &lt;<I>file1</I>&gt; &lt;<I>file2</I>&gt; ...</CODE></P>
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

public class ControllabilityMain
{

  //#########################################################################
  //# Constructors
  /**
   * Dummy constructor to prevent instantiation of this class.
   */
  private ControllabilityMain()
  {
  }


  //#########################################################################
  //# Main Method for Testing
  /**
   * Main method.
   * This is a main method to check a set of files for controllability.
   * Please refer to the class documentation ({@link ControllabilityMain})
   * for more detailed information.
   * @param  args    Array of file names from the command line.
   */
  public static void main(String[] args)
  {
    try {

      final ValidUnmarshaller validMarshaller = new ValidUnmarshaller();
      final ProxyMarshaller desMarshaller = new ProductDESMarshaller();
      final ProxyMarshaller moduleMarshaller = new ModuleMarshaller();
      final DocumentManager docManager = new DocumentManager();
      docManager.register(desMarshaller);
      docManager.register(moduleMarshaller);
      docManager.register(validMarshaller);
      
      for (int i = 0; i < args.length; i++) {
	final String name = args[i];
	final File filename = new File(name);
	final DocumentProxy doc = docManager.load(filename);
	ProductDESProxy des = null;
	if (doc instanceof ProductDESProxy) {
	  des = (ProductDESProxy) doc;
	} else {
	  final ModuleProxy module = (ModuleProxy) doc;
	  final ModuleCompiler compiler =
	    new ModuleCompiler(module, docManager);
	  des = compiler.compile();
	}
	final ControllabilityChecker checker =
	  new ControllabilityChecker(des);
	System.out.print(des.getName() + " ... ");
	System.out.flush();
	final boolean result = checker.run();
	if (result) {
	  System.out.println("controllable");
	} else {
	  System.out.println("NOT controllable");
	  System.out.println("Counterexample:");
	  final List counterex = checker.getCounterExample();
	  final Iterator iter = counterex.iterator();
	  while (iter.hasNext()) {
	    final EventProxy event = (EventProxy) iter.next();
	    System.out.println("  " + event.getName());
	  }
	}
      }

    } catch (final Throwable exception) {
      System.err.println("FATAL ERROR !!!");
      System.err.println(exception.getClass().getName() +
			 " caught in main()!");
      exception.printStackTrace(System.err);
    }
  }

}
