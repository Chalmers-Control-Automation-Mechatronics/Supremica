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

package net.sourceforge.waters.gui;

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.util.List;

import javax.swing.JLabel;

import net.sourceforge.waters.gui.util.IconAndFontLoader;
import net.sourceforge.waters.model.base.Proxy;
import net.sourceforge.waters.model.base.VisitorException;
import net.sourceforge.waters.model.base.WatersRuntimeException;
import net.sourceforge.waters.model.module.ConditionalProxy;
import net.sourceforge.waters.model.module.EventDeclProxy;
import net.sourceforge.waters.model.module.ExpressionProxy;
import net.sourceforge.waters.model.module.ForeachProxy;
import net.sourceforge.waters.model.module.IdentifierProxy;
import net.sourceforge.waters.model.module.InstanceProxy;
import net.sourceforge.waters.model.module.ModuleProxy;
import net.sourceforge.waters.model.module.ParameterBindingProxy;
import net.sourceforge.waters.model.module.SimpleComponentProxy;
import net.sourceforge.waters.model.module.SimpleExpressionProxy;
import net.sourceforge.waters.model.module.VariableComponentProxy;
import net.sourceforge.waters.model.printer.ModuleProxyPrinter;


public class HTMLPrinter
  extends ModuleProxyPrinter
{

  //#########################################################################
  //# Static Class Methods
  public static String getHTMLString(final Proxy proxy,
                                     final ModuleContext moduleContext)
  {
    try {
      final StringWriter writer = new StringWriter();
      final HTMLPrinter printer = new HTMLPrinter(writer, moduleContext);
      printer.pprint(proxy);
      return writer.toString();
    } catch (final IOException exception) {
      throw new WatersRuntimeException(exception);
    }
  }

  public static String encodeInHTML(final String text)
  {
    try {
      final StringWriter writer = new StringWriter();
      @SuppressWarnings("resource")
      final HTMLWriter wrapper = new HTMLWriter(writer);
      wrapper.write(text);
      return writer.toString();
    } catch (final IOException exception) {
      throw new WatersRuntimeException(exception);
    }
  }

  public static void setLabelText(final JLabel label,
                                  final String text,
                                  final int width)
  {
    final StringBuilder builder = new StringBuilder();
    builder.append("<HTML><P STYLE=\"text-align: center; font-size: ");
    builder.append(IconAndFontLoader.HTML_FONT_SIZE);
    builder.append("px; width: ");
    final int scaledWidth =
      (int) Math.ceil(width * IconAndFontLoader.GLOBAL_SCALE_FACTOR);
    builder.append(scaledWidth);
    builder.append("px;\">");
    builder.append(HTMLPrinter.encodeInHTML(text));
    builder.append("</P></HTML>");
    label.setText(builder.toString());
  }


  //#########################################################################
  //# Constructors
  public HTMLPrinter(final Writer writer, final ModuleContext moduleContext)
  {
    super(new HTMLWriter(writer));
    mRaw = writer;
    mModuleContext = moduleContext;
  }


  //#########################################################################
  //# Invocation
  @Override
  public void pprint(final Proxy proxy)
    throws IOException
  {
    try {
      printHTML("<HTML>");
      printProxy(proxy);
      printHTML("</HTML>");
    } catch (final VisitorException exception) {
      unwrap(exception);
    }
  }


  //#########################################################################
  //# Proxy Printing
  @Override
  public void printProxy(final Proxy proxy)
    throws VisitorException
  {
    final ModuleCompilationErrors errors =
      mModuleContext.getCompilationErrors();
    if (errors.isUnderlined(proxy)) {
      printHTML("<DIV " + UNDERLINE_STYLE + ">");
      super.printProxy(proxy);
      printHTML("</DIV>");
    } else {
      super.printProxy(proxy);
    }
  }


  //#########################################################################
  //# Overrides for Base Class
  //# net.sourceforge.waters.model.printer.ModuleProxyVisitor
  @Override
  public Object visitConditionalProxy(final ConditionalProxy proxy)
    throws VisitorException
  {
    final ModuleCompilationErrors errors =
      mModuleContext.getCompilationErrors();
    printHTML("<TABLE cellspacing=0 cellpadding=0><TR>");
    printHTML("<TD><B>IF</B>&nbsp;</TD>");
    final SimpleExpressionProxy guard = proxy.getGuard();
    if (errors.isUnderlined(guard)) {
      printHTML("<TD " + UNDERLINE_STYLE + ">");
    } else {
      printHTML("<TD>");
    }
    guard.acceptVisitor(this);
    printHTML("</TD></TR></TABLE>");
    return null;
  }

  @Override
  public Object visitEventDeclProxy
      (final EventDeclProxy proxy)
    throws VisitorException
  {
    print(proxy.getName());
    final List<SimpleExpressionProxy> ranges = proxy.getRanges();
    for (final SimpleExpressionProxy expr : ranges) {
      print('[');
      expr.acceptVisitor(this);
      print(']');
    }
    return null;
  }

  @Override
  public Object visitForeachProxy(final ForeachProxy proxy)
    throws VisitorException
  {
    final ModuleCompilationErrors errors =
      mModuleContext.getCompilationErrors();
    printHTML("<TABLE cellspacing=0 cellpadding=0><TR>");
    printHTML("<TD><B>FOR</B> ");
    print(proxy.getName());
    printHTML(" <B>IN</B>&nbsp;</TD>");
    final SimpleExpressionProxy range = proxy.getRange();
    if (errors.isUnderlined(range)) {
      printHTML("<TD " + UNDERLINE_STYLE + ">");
    } else {
      printHTML("<TD>");
    }
    range.acceptVisitor(this);
    printHTML("</TD>");
    final SimpleExpressionProxy guard = proxy.getGuard();
    if (guard != null) {
      printHTML("<TD>&nbsp;<B>WHERE</B>&nbsp;</TD>");
      if (errors.isUnderlined(guard)) {
        printHTML("<TD " + UNDERLINE_STYLE + ">");
      } else {
        printHTML("<TD>");
      }
      guard.acceptVisitor(this);
      printHTML("</TD>");
    }
    printHTML("</TR></TABLE>");
    return null;
  }

  @Override
  public Object visitInstanceProxy
      (final InstanceProxy proxy)
    throws VisitorException
  {
    printHTML("<I>");
    final IdentifierProxy identifier = proxy.getIdentifier();
    identifier.acceptVisitor(this);
    printHTML("</I> = ");
    print(proxy.getModuleName());
    return null;
  }

  @Override
  public Object visitModuleProxy(final ModuleProxy module)
    throws VisitorException
  {
    printHTML("<B>MODULE</B> ");
    print(module.getName());
    return null;
  }

  @Override
  public Object visitParameterBindingProxy
      (final ParameterBindingProxy proxy)
    throws VisitorException
  {
    printHTML("<I>");
    print(proxy.getName());
    printHTML("</I> = ");
    final ExpressionProxy expression = proxy.getExpression();
    expression.acceptVisitor(this);
    return null;
  }

  @Override
  public Object visitSimpleComponentProxy(final SimpleComponentProxy comp)
    throws VisitorException
  {
    final IdentifierProxy identifier = comp.getIdentifier();
    identifier.acceptVisitor(this);
    return null;
  }

  @Override
  public Object visitVariableComponentProxy(final VariableComponentProxy var)
    throws VisitorException
  {
    final IdentifierProxy identifier = var.getIdentifier();
    identifier.acceptVisitor(this);
    print(" : ");
    final SimpleExpressionProxy type = var.getType();
    type.acceptVisitor(this);
    return null;
  }


  //#########################################################################
  //# Unescaped Printing
  public void printHTML(final String msg)
    throws VisitorException
  {
    try {
      mRaw.write(msg);
    } catch (final IOException e) {
      throw wrap(e);
    }
  }


  //#########################################################################
  //# Inner Class HTMLWriter
  public static class HTMLWriter extends Writer
  {

    public HTMLWriter(final Writer out)
    {
      super(out);
      mOut = out;
    }

    //#######################################################################
    //# Overrides for base class java.io.Writer
    @Override
    public void write(final char[] cbuf, final int off, final int len)
      throws IOException
    {
      synchronized (lock) {
        for (int i = off; i < len; i++) {
          switch (cbuf[i]) {
          case '<':  mOut.write("&lt;");     break;
          case '>':  mOut.write("&gt;");     break;
          case '&':  mOut.write("&amp;");    break;
          case '"':  mOut.write("&quot;");   break;
          case '\'': mOut.write("&#39;");    break;
          case '\n': mOut.write("<BR />\n"); break;
          default:   mOut.write(cbuf[i]);    break;
          }
        }
      }
    }

    @Override
    public void flush()
      throws IOException
    {
      mOut.flush();
    }

    @Override
    public void close()
      throws IOException
    {
      mOut.close();
    }

    //########################################################################
    //# Data Members
    private final Writer mOut;

  }


  //########################################################################
  //# Data Members
  private final Writer mRaw;
  private final ModuleContext mModuleContext;


  //#########################################################################
  //# Class Constants
  private static final String UNDERLINE_STYLE =
    "style='border-bottom: 1px red dashed'";

}
