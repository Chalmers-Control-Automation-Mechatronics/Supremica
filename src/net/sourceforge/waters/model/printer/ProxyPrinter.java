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
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import net.sourceforge.waters.model.base.AbstractProxyVisitor;
import net.sourceforge.waters.model.base.ComparableProxy;
import net.sourceforge.waters.model.base.NamedProxy;
import net.sourceforge.waters.model.base.Proxy;
import net.sourceforge.waters.model.base.WatersRuntimeException;
import net.sourceforge.waters.model.base.VisitorException;


public class ProxyPrinter
  extends AbstractProxyVisitor
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
    final Class clazz = proxy.getClass();
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
  public ProxyPrinter()
  {
    this(new StringWriter());
  }

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

  public String toString(final Proxy proxy)
  {
    if (mWriter instanceof StringWriter) {
      try {
        final StringWriter swriter = (StringWriter) mWriter;
        final StringBuffer buffer = swriter.getBuffer();
        final String old = buffer.toString();
        final int oldlen = old.length();
        buffer.delete(0, oldlen);
        pprint(proxy);
        return buffer.toString();
      } catch (final IOException exception) {
        throw new WatersRuntimeException(exception);
      }      
    } else {
      throw new IllegalStateException
        ("ProxyPrinter must be initialised with a StringWriter " +
         "to obtain strings!");
    }
  }


  //#########################################################################
  //# Proxy Printing
  public void printProxy(final Proxy proxy)
    throws VisitorException
  {
    proxy.acceptVisitor(this);
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

  public <P extends ComparableProxy<? super P>>
  void printSortedCollection(final String label,
                             final Collection<? extends P> collection)
    throws VisitorException
  {
    final List<P> list = new ArrayList<P>(collection);
    Collections.sort(list);
    printCollection(label, list);
  }

  public <P extends ComparableProxy<? super P>>
  void printSortedCollection(final Collection<? extends P> collection)
    throws VisitorException
  {
    final List<P> list = new ArrayList<P>(collection);
    Collections.sort(list);
    printCollection(list);
  }

  public <P extends ComparableProxy<? super P>>
  void printSortedEmptyCollection(final Collection<? extends P> collection)
    throws VisitorException
  {
    final List<P> list = new ArrayList<P>(collection);
    Collections.sort(list);
    printEmptyCollection(list);
  }

  public void printSortedRefCollection
    (final String label,
     final Collection<? extends NamedProxy> collection)
    throws VisitorException
  {
    final List<NamedProxy> list = new ArrayList<NamedProxy>(collection);
    Collections.sort(list);
    printRefCollection(label, list);
  }


  //#########################################################################
  //# String Printing
  public void print(final String msg)
    throws VisitorException
  {
    if (msg.length() > 0) {
      try {
        indent();
        mWriter.write(msg);
      } catch (final IOException exception) {
        throw wrap(exception);
      }
    }
  }

  public void print(final char msg)
    throws VisitorException
  {
    try {
      indent();
      mWriter.write(msg);
    } catch (final IOException exception) {
      throw wrap(exception);
    }
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
    try {
      if (mWriter instanceof PrintWriter) {
        final PrintWriter pwriter = (PrintWriter) mWriter;
        pwriter.println();
      } else {
        mWriter.write('\n');
      }
      mAtLineStart = true;
    } catch (final IOException exception) {
      throw wrap(exception);
    }
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
  int setIndent(int indent)
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
      try {
        for (int i = mIndent * mIndentWidth; i > 0; i--) {
          mWriter.write(' ');
        }
        mAtLineStart = false;
      } catch (final IOException exception) {
        throw wrap(exception);
      }
    }
  }


  //#########################################################################
  //# Data Members
  private final Writer mWriter;
  private final int mIndentWidth;

  private int mIndent = 0;
  private boolean mAtLineStart = true;

}
