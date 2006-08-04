//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.valid
//# CLASS:   ValidToWaters
//###########################################################################
//# $Id: ValidToWaters.java,v 1.5 2006-08-04 06:45:23 robi Exp $
//###########################################################################

package net.sourceforge.waters.valid;


import java.io.File;
import java.io.IOException;
import javax.xml.bind.JAXBException;

import net.sourceforge.waters.model.base.DocumentProxy;
import net.sourceforge.waters.model.compiler.CompilerOperatorTable;
import net.sourceforge.waters.model.compiler.ModuleCompiler;
import net.sourceforge.waters.model.des.ProductDESProxy;
import net.sourceforge.waters.model.des.ProductDESProxyFactory;
import net.sourceforge.waters.model.expr.EvalException;
import net.sourceforge.waters.model.expr.OperatorTable;
import net.sourceforge.waters.model.marshaller.DocumentManager;
import net.sourceforge.waters.model.marshaller.JAXBModuleMarshaller;
import net.sourceforge.waters.model.marshaller.JAXBProductDESMarshaller;
import net.sourceforge.waters.model.marshaller.ProxyMarshaller;
import net.sourceforge.waters.model.module.ModuleProxy;
import net.sourceforge.waters.model.module.ModuleProxyFactory;
import net.sourceforge.waters.plain.des.ProductDESElementFactory;
import net.sourceforge.waters.plain.module.ModuleElementFactory;


/**
 * A simple command line tool to convert VALID projects to Waters modules.
 *
 * <P>This class provides a simple application that accepts a list
 * of VALID file names on the command line, which it then tries to
 * convert to Waters format.</P>
 *
 * <P><CODE>java ValidToWaters [-compile] [-q]
 * &lt;<I>file1</I>&gt; &lt;<I>file2</I>&gt; ...</CODE></P>
 *
 * <P>The following file formats and extensions are supported.</P>
 *
 * <DL>
 * <DT><STRONG>VALID Projects (<CODE>.vprj</CODE>)</STRONG></DT>
 * <DD>When a file <CODE>&lt;<I>project</I>&gt;.vprj</CODE> is requested,
 *     the program will really attempt to open a file called
 *     <CODE>&lt;<I>project</I>&gt;_main.vmod</CODE> in the same
 *     directory. This is exactly the way how VALID works.
 *     The result of the conversion is saved as 
 *     <CODE>&lt;<I>project</I>&gt;.wmod</CODE>.</DD>
 * <DT><STRONG>VALID Modules (<CODE>.vmod</CODE>)</STRONG></DT>
 * <DD>Only <I>main modules</I> are supported, i.e., files named
 *     <CODE>&lt;<I>project</I>&gt;_main.vmod</CODE>.
 *     The result of the conversion is saved as 
 *     <CODE>&lt;<I>project</I>&gt;.wmod</CODE>.</DD>
 * <DD>Other names taken literally and assumed to contain VALID main
 *     modules. If present, their extension is replaced by <CODE>.wmod</CODE>
 *     for the output file, otherwise <CODE>.wmod</CODE> is appended to the
 *     file name.</DD>
 * <DD>If the <CODE>-compile</CODE> option is specified, then all modules
 *     are compiled into a product DES, and that product DES is saved in
 *     a <CODE>.wdes</CODE> file with the same name prefix. In this case,
 *     no <CODE>.wmod</CODE> files are written.
 * </DL>
 *
 * @author Robi Malik
 */

public class ValidToWaters
{

  //#########################################################################
  //# Main
  /**
   * Main routine.
   */
  public static void main(final String[] args)
  {
    boolean compile = false;
    boolean save = true;
    boolean quiet = false;
    int start = 0;
    while (start < args.length) {
      final String arg = args[start];
      if (arg.charAt(0) == '-') {
        start++;
        if (arg.equals("-c") || arg.equals("--compile")) {
          compile = true;
        } else if (arg.equals("-C")) {
          compile = true;
          save = false;
        } else if (arg.equals("-q") || arg.equals("--quiet")) {
          quiet = true;
        } else if (arg.equals("--")) {
          break;
        } else {
          System.err.println
            ("USAGE: vw [-c|--compile] [-q|--quiet] <file> ...");
          System.exit(1);
        }
      } else {
        break;
      }
    }

    try {
      final ModuleProxyFactory modfactory = ModuleElementFactory.getInstance();
      final OperatorTable optable = CompilerOperatorTable.getInstance();
      final ValidUnmarshaller unmarshaller =
        new ValidUnmarshaller(modfactory, optable);
      final DocumentManager manager = new DocumentManager();
      manager.registerUnmarshaller(unmarshaller);
      String saveext = null;
      if (save) {
        final ProxyMarshaller<ModuleProxy> marshaller =
          new JAXBModuleMarshaller(modfactory, optable);
        manager.registerMarshaller(marshaller);
        saveext = marshaller.getDefaultExtension();
      }
      ProductDESProxyFactory desfactory = null;
      String compileext = null;
      if (compile) {
        desfactory = ProductDESElementFactory.getInstance();
        final ProxyMarshaller<ProductDESProxy> marshaller =
          new JAXBProductDESMarshaller(desfactory);
        manager.registerMarshaller(marshaller);
        compileext = marshaller.getDefaultExtension();
      }

      for (int i = start; i < args.length; i++) {
	final String validName = args[i];
        if (!quiet) {
          System.out.print(validName + " ... ");
          System.out.flush();
        }
	final File validFile = new File(validName);
	final ModuleProxy module = (ModuleProxy) manager.load(validFile);
	String watersPrefix;
	if (validName.endsWith("_main.vmod")) {
	  final int len = validName.length() - 10;
	  watersPrefix = validName.substring(0, len);
	} else if (validName.contains(".")) {
	  final int pos = validName.lastIndexOf('.');
	  watersPrefix = validName.substring(0, pos);
	} else {
	  watersPrefix = validName;
	}
        if (save) {
          final File watersFile = new File(watersPrefix + saveext);
          manager.saveAs(module, watersFile);
        }
        if (compile) {
          final ModuleCompiler compiler =
            new ModuleCompiler(manager, desfactory, module);
          final ProductDESProxy des = compiler.compile();
          final File watersFile = new File(watersPrefix + compileext);
          manager.saveAs(des, watersFile);
          }
        if (!quiet) {
          System.out.println();
        }
      }
    } catch (final IOException exception) {
      System.err.println("IO ERROR!");
      System.err.println(exception.getMessage());
    } catch (final EvalException exception) {
      System.err.println("COMPILER ERROR!");
      System.err.println(exception.getMessage());
    } catch (final Throwable exception) {
      System.err.println("FATAL ERROR!");
      exception.printStackTrace(System.err);
    }
  }

}
