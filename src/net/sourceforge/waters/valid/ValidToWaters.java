//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.valid
//# CLASS:   ValidToWaters
//###########################################################################
//# $Id: ValidToWaters.java,v 1.4 2006-02-20 22:20:22 robi Exp $
//###########################################################################

package net.sourceforge.waters.valid;


import java.io.File;
import java.io.IOException;
import java.net.URI;
import javax.xml.bind.JAXBException;

import net.sourceforge.waters.model.compiler.CompilerOperatorTable;
import net.sourceforge.waters.model.expr.OperatorTable;
import net.sourceforge.waters.model.marshaller.DocumentManager;
import net.sourceforge.waters.model.marshaller.JAXBModuleMarshaller;
import net.sourceforge.waters.model.module.ModuleProxy;
import net.sourceforge.waters.model.module.ModuleProxyFactory;
import net.sourceforge.waters.plain.module.ModuleElementFactory;


/**
 * A simple command line tool to convert VALID projects to Waters modules.
 *
 * <P>This class provides a simple application that accepts a list
 * of VALID file names on the command line, which it then tries to
 * convert to Waters format.</P>
 *
 * <P><CODE>java ValidToWaters
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
    try {
      final ModuleProxyFactory factory = ModuleElementFactory.getInstance();
      final OperatorTable optable = CompilerOperatorTable.getInstance();
      final ValidUnmarshaller unmarshaller =
        new ValidUnmarshaller(factory, optable);
      final JAXBModuleMarshaller marshaller =
        new JAXBModuleMarshaller(factory, optable);
      final String ext = marshaller.getDefaultExtension();
      for (int i = 0; i < args.length; i++) {
	final String validName = args[i];
	System.out.print(validName + " ...");
	System.out.flush();
	final File validFile = new File(validName);
        final URI validURI = validFile.toURI();
	final ModuleProxy module = unmarshaller.unmarshal(validURI);
	String watersName;
	if (validName.endsWith("_main.vmod")) {
	  final int len = validName.length() - 10;
	  watersName = validName.substring(0, len) + ext;
	} else if (validName.contains(".")) {
	  final int pos = validName.lastIndexOf('.');
	  watersName = validName.substring(0, pos) + ext;
	} else {
	  watersName = validName + ext;
	}
	final File watersFile = new File(watersName);
	marshaller.marshal(module, watersFile);
	System.out.println();
      }
    } catch (final IOException exception) {
      System.err.println("IO ERROR!");
      System.err.println(exception.getMessage());
    } catch (final Throwable exception) {
      System.err.println("FATAL ERROR!");
      exception.printStackTrace(System.err);
    }
  }

}
