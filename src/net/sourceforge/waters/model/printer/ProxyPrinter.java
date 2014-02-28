//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.model.printer
//# CLASS:   ProxyPrinter
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.model.printer;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.util.Collection;

import net.sourceforge.waters.model.base.DefaultProxyVisitor;
import net.sourceforge.waters.model.base.DocumentProxy;
import net.sourceforge.waters.model.base.NamedProxy;
import net.sourceforge.waters.model.base.Proxy;
import net.sourceforge.waters.model.base.VisitorException;
import net.sourceforge.waters.model.base.WatersRuntimeException;


public class ProxyPrinter
  extends DefaultProxyVisitor
{

  //#########################################################################
  //# Static Class Methods
  public static String getPrintString(final Proxy proxy)
  {
    try {
      final StringWriter writer = new StringWriter();
      printProxy(writer, proxy);
      return writer.toString();
    } catch (final IOException exception) {
      throw new WatersRuntimeException(exception);
    }
  }

  public static void printProxy(final Writer writer, final Proxy proxy)
    throws IOException
  {
    final Class<?> clazz = proxy.getProxyInterface();
    final Package pack = clazz.getPackage();
    final String packname = pack.getName();
    final int dotpos = packname.lastIndexOf('.');
    final String lastpart = packname.substring(dotpos + 1);
    if (lastpart.equals("des")) {
      final ProductDESProxyPrinter desPrinter =
        new ProductDESProxyPrinter(writer);
      desPrinter.pprint(proxy);
    } else if (lastpart.equals("module")) {
      final ModuleProxyPrinter modulePrinter =
        new ModuleProxyPrinter(writer);
      modulePrinter.pprint(proxy);
    } else {
      throw new UnsupportedOperationException
        ("Printing for class " + clazz.getName() + " not yet implemented!");
    }
  }


  //#########################################################################
  //# Constructors
  public ProxyPrinter(final Writer writer)
  {
    this(writer, 2);
  }

  public ProxyPrinter(final Writer writer, final int indentwidth)
  {
    mWriter = writer;
    mIndentWidth = indentwidth;
  }


  //#########################################################################
  //# Invocation
  public void pprint(final Proxy proxy)
    throws IOException
  {
    try {
      printProxy(proxy);
    } catch (final VisitorException exception) {
      unwrap(exception);
    }
  }

  public void pprint(final String msg)
    throws IOException
  {
    try {
      print(msg);
    } catch (final VisitorException exception) {
      unwrap(exception);
    }
  }

  public void pprint(final Collection<? extends Proxy> list,
                     final String opening,
                     final String separator,
                     final String closing)
    throws IOException
  {
    try {
      boolean first = true;
      print(opening);
      for (final Proxy proxy : list) {
	if (first) {
	  first = false;
	} else {
	  print(separator);
        }
        printProxy(proxy);
      }
      print(closing);
    } catch (final VisitorException exception) {
      unwrap(exception);
    }
  }


  //#########################################################################
  //# Proxy Printing
  public void printProxy(final Proxy proxy)
    throws VisitorException
  {
    proxy.acceptVisitor(this);
  }

  public void printComment(final DocumentProxy doc)
    throws VisitorException
  {
    final String comment = doc.getComment();
    if (comment != null) {
      print('"');
      print(comment);
      println('"');
    }
  }

  public void printEmptyCollection
    (final Collection<? extends Proxy> collection)
    throws VisitorException
  {
    if (collection.isEmpty()) {
      print("{}");
    } else {
      println('{');
      indentIn();
      for (final Proxy proxy : collection) {
        printProxy(proxy);
        println();
      }
      indentOut();
      print('}');
    }
  }

  public void printCollection
    (final Collection<? extends Proxy> collection)
    throws VisitorException
  {
    if (!collection.isEmpty()) {
      printEmptyCollection(collection);
    }
  }

  public void printCollection(final String label,
                              final Collection<? extends Proxy> collection)
    throws VisitorException
  {
    if (!collection.isEmpty()) {
      print(label);
      print(' ');
      printEmptyCollection(collection);
      println();
    }
  }

  public void printEmptyRefCollection
    (final Collection<? extends NamedProxy> collection)
    throws VisitorException
  {
    if (collection.isEmpty()) {
      print("{}");
    } else {
      println('{');
      indentIn();
      for (final NamedProxy proxy : collection) {
        println(proxy.getName());
      }
      indentOut();
      print('}');
    }
  }

  public void printRefCollection
    (final String label,
     final Collection<? extends NamedProxy> collection)
    throws VisitorException
  {
    if (!collection.isEmpty()) {
      print(label);
      print(' ');
      printEmptyRefCollection(collection);
      println();
    }
  }


  //#########################################################################
  //# String Printing
  public void print(final String msg)
    throws VisitorException
  {
    if (msg.length() > 0) {
      indent();
      rawPrint(msg);
    }
  }

  public void print(final char msg)
    throws VisitorException
  {
    indent();
    rawPrint(msg);
  }

  public void print(final double msg)
    throws VisitorException
  {
    print(Double.toString(msg));
  }

  public void print(final int msg)
    throws VisitorException
  {
    print(Integer.toString(msg));
  }

  public void println()
    throws VisitorException
  {
    if (mWriter instanceof PrintWriter) {
      final PrintWriter pwriter = (PrintWriter) mWriter;
      pwriter.println();
    } else {
      rawPrint('\n');
    }
    mAtLineStart = true;
  }

  public void println(final String msg)
    throws VisitorException
  {
    print(msg);
    println();
  }

  public void println(final char msg)
    throws VisitorException
  {
    print(msg);
    println();
  }

  public void println(final int msg)
    throws VisitorException
  {
    print(msg);
    println();
  }

  public void flush()
    throws IOException
  {
    mWriter.flush();
  }


  //#########################################################################
  //# Exception Handling
  protected void unwrap(final VisitorException exception)
    throws IOException
  {
    final Throwable cause = exception.getCause();
    if (cause instanceof IOException) {
      throw (IOException) cause;
    } else {
      throw new WatersRuntimeException(cause);
    }
  }


  //#########################################################################
  //# Indentation
  int setIndent(final int indent)
  {
    final int oldindent = mIndent;
    mIndent = indent;
    return oldindent;
  }

  int indentIn()
  {
    return mIndent++;
  }

  int indentOut()
  {
    return mIndent--;
  }

  void indent()
    throws VisitorException
  {
    if (mAtLineStart) {
      for (int i = mIndent * mIndentWidth; i > 0; i--) {
        indentOneCharacter();
      }
      mAtLineStart = false;
    }
  }

  protected void indentOneCharacter()
    throws VisitorException
  {
    rawPrint(' ');
  }


  //#########################################################################
  //# Raw Printing
  protected void rawPrint(final String msg)
    throws VisitorException
  {
    try {
      mWriter.write(msg);
    } catch (final IOException exception) {
      throw wrap(exception);
    }
  }

  protected void rawPrint(final char msg)
    throws VisitorException
  {
    try {
      mWriter.write(msg);
    } catch (final IOException exception) {
      throw wrap(exception);
    }
  }


  //#########################################################################
  //# Data Members
  private final Writer mWriter;
  private final int mIndentWidth;

  private int mIndent = 0;
  private boolean mAtLineStart = true;

}
