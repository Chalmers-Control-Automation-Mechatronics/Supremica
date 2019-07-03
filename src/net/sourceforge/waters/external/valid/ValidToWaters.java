//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# Copyright (C) 2004-2019 Robi Malik
//###########################################################################
//# This file is part of Waters.
//# Waters is free software: you can redistribute it and/or modify it under
//# the terms of the GNU General Public License as published by the Free
//# Software Foundation, either version 2 of the License, or (at your option)
//# any later version.
//# Waters is distributed in the hope that it will be useful, but WITHOUT ANY
//# WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
//# FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
//# details.
//# You should have received a copy of the GNU General Public License along
//# with Waters. If not, see <http://www.gnu.org/licenses/>.
//#
//# Linking Waters statically or dynamically with other modules is making a
//# combined work based on Waters. Thus, the terms and conditions of the GNU
//# General Public License cover the whole combination.
//# In addition, as a special exception, the copyright holders of Waters give
//# you permission to combine Waters with code included in the standard
//# release of Supremica under the Supremica Software License Agreement (or
//# modified versions of such code, with unchanged license). You may copy and
//# distribute such a system following the terms of the GNU GPL for Waters and
//# the licenses of the other code concerned.
//# Note that people who make modified versions of Waters are not obligated to
//# grant this special exception for their modified versions; it is their
//# choice whether to do so. The GNU General Public License gives permission
//# to release a modified version without this exception; this exception also
//# makes it possible to release a modified version which carries forward this
//# exception.
//###########################################################################

package net.sourceforge.waters.external.valid;


import java.io.File;
import java.io.IOException;

import net.sourceforge.waters.model.compiler.CompilerOperatorTable;
import net.sourceforge.waters.model.compiler.ModuleCompiler;
import net.sourceforge.waters.model.des.ProductDESProxy;
import net.sourceforge.waters.model.des.ProductDESProxyFactory;
import net.sourceforge.waters.model.expr.EvalException;
import net.sourceforge.waters.model.expr.OperatorTable;
import net.sourceforge.waters.model.marshaller.DocumentManager;
import net.sourceforge.waters.model.marshaller.ProxyMarshaller;
import net.sourceforge.waters.model.marshaller.SAXModuleMarshaller;
import net.sourceforge.waters.model.marshaller.SAXProductDESMarshaller;
import net.sourceforge.waters.model.module.ModuleProxy;
import net.sourceforge.waters.model.module.ModuleProxyFactory;
import net.sourceforge.waters.plain.des.ProductDESElementFactory;
import net.sourceforge.waters.plain.module.ModuleElementFactory;


/**
 * A simple command line tool to convert VALID projects to Waters modules.
 *
 * <P>This class provides a simple application that accepts a list
 * of VALID file names on the command line, which it then tries to
 * convert to Waters format. When Supremica has been installed
 * successfully, the converter can be run using the script called
 * <CODE>vw</CODE> in Supremica's <CODE>dist</CODE> directory as follows.</P>
 *
 * <P><CODE>vw [--compile|-c|--Compile|-C] [--quiet|-q]
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
 * </DL>
 *
 * <P>The converter can be controlled by the following command line
 * options.</P>
 * <DL>
 * <DT><CODE>--compile</CODE>, <CODE>-c</CODE></DT>
 * <DD>If this option is specified, then in addition to converting the
 *     VALID models into Waters modules, they are are compiled into
 *     product DES form, and that product DES is saved in a
 *     <CODE>.wdes</CODE> file with the same name prefix.</DD>
 * <DT><CODE>--Compile</CODE>, <CODE>-C</CODE></DT>
 * <DD>This option behaves like the <CODE>--compile</CODE> option,
 *     but it suppresses the creation of Waters module (<CODE>.wmod</CODE>)
 *     files.</DD>
 * <DT><CODE>--quiet</CODE>, <CODE>-q</CODE></DT>
 * <DD>This option suppresses the progress messages that are normally
 *     written to the console.</DD>
 * <DT><CODE>--</CODE></DT>
 * <DD>This stops option parsing and treats all remaining command
 *     line arguments as file names.</DD>
 * </DL>
 *
 * @author Robi Malik
 */

public class ValidToWaters
{

  //#########################################################################
  //# Constructors
  /**
   * Dummy constructor to prevent instantiation of class.
   */
  private ValidToWaters()
  {
  }


  //#########################################################################
  //# Main
  /**
   * Program entry point.
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
        } else if (arg.equals("-C") || arg.equals("--Compile")) {
          compile = true;
          save = false;
        } else if (arg.equals("-q") || arg.equals("--quiet")) {
          quiet = true;
        } else if (arg.equals("--")) {
          break;
        } else {
          System.err.println
            ("USAGE: vw [-c|--compile|-C|--Compile] [-q|--quiet] <file> ...");
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
          new SAXModuleMarshaller(modfactory, optable);
        manager.registerMarshaller(marshaller);
        saveext = marshaller.getDefaultExtension();
      }
      ProductDESProxyFactory desfactory = null;
      String compileext = null;
      if (compile) {
        desfactory = ProductDESElementFactory.getInstance();
        final ProxyMarshaller<ProductDESProxy> marshaller =
          new SAXProductDESMarshaller(desfactory);
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
