//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.gui
//# CLASS:   HTMLPrinter
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.gui;

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.util.List;

import net.sourceforge.waters.model.base.Proxy;
import net.sourceforge.waters.model.base.VisitorException;
import net.sourceforge.waters.model.base.WatersRuntimeException;
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
  public static String getHTMLString(final Proxy proxy)
  {
    try {
      final StringWriter writer = new StringWriter();
      final HTMLPrinter printer = new HTMLPrinter(writer);
      printer.pprint(proxy);
      return writer.toString();
    } catch (final IOException exception) {
      throw new WatersRuntimeException(exception);
    }
  }


  //#########################################################################
  //# Constructors
  public HTMLPrinter()
  {
    super();
  }

  public HTMLPrinter(final Writer writer)
  {
    super(writer);
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
  //# Overrides for net.sourceforge.waters.model.printer.ProxyPrinter
  @Override
  public void print(final String msg)
    throws VisitorException
  {
    for (int i = 0; i < msg.length(); i++) {
      print(msg.charAt(i));
    }
  }

  @Override
  public void print(final char msg)
    throws VisitorException
  {
    if (msg == '&' || msg == '<' || msg == '>' || msg == '"' || msg == '\'') {
      printHTML("&#");
      print((int) msg);
      printHTML(";");
    } else if (msg == ' ') {
      printHTML("&nbsp;");
    } else {
      super.print(msg);
    }
  }

  @Override
  public void println()
    throws VisitorException
  {
    printHTML("<BR />");
    super.println();
  }

  @Override
  protected void indentOneCharacter()
    throws VisitorException
  {
    rawPrint("&nbsp;");
  }


  //#########################################################################
  //# Overrides for Base Class
  //# net.sourceforge.waters.model.printer.ModuleProxyVisitor
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
  public Object visitForeachProxy
      (final ForeachProxy proxy)
    throws VisitorException
  {
    printHTML("<B>FOR</B> ");
    print(proxy.getName());
    printHTML(" <B>IN</B> ");
    final SimpleExpressionProxy range = proxy.getRange();
    range.acceptVisitor(this);
    final SimpleExpressionProxy guard = proxy.getGuard();
    if (guard != null) {
      printHTML(" <B>WHERE</B> ");
      guard.acceptVisitor(this);
    }
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
  // Unescaped Printing
  public void printHTML(final String msg)
    throws VisitorException
  {
    super.print(msg);
  }

}
