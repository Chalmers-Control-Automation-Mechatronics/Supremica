//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# Copyright (C) 2004-2020 Robi Malik
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

package net.sourceforge.waters.model.printer;

import java.io.IOException;
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
      mWriter.write('\n');
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
