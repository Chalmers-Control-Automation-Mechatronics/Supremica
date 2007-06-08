//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.gui
//# CLASS:   HTMLPrinter
//###########################################################################
//# $Id: HTMLPrinter.java,v 1.4 2007-06-08 10:45:20 robi Exp $
//###########################################################################

package net.sourceforge.waters.gui;

import java.io.IOException;
import java.io.Writer;
import java.io.StringWriter;
import java.util.List;

import net.sourceforge.waters.model.base.Proxy;
import net.sourceforge.waters.model.base.VisitorException;
import net.sourceforge.waters.model.base.WatersRuntimeException;
import net.sourceforge.waters.model.module.EventDeclProxy;
import net.sourceforge.waters.model.module.ExpressionProxy;
import net.sourceforge.waters.model.module.ForeachProxy;
import net.sourceforge.waters.model.module.IdentifierProxy;
import net.sourceforge.waters.model.module.InstanceProxy;
import net.sourceforge.waters.model.module.ParameterBindingProxy;
import net.sourceforge.waters.model.module.SimpleComponentProxy;
import net.sourceforge.waters.model.module.SimpleExpressionProxy;
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
  public void pprint(final Proxy proxy)
    throws IOException
  {
    try {
      print("<HTML>");
      printProxy(proxy);
      print("</HTML>");
    } catch (final VisitorException exception) {
      unwrap(exception);
    }
  }


  //#########################################################################
  //# Overrides for Base Class
  //# net.sourceforge.waters.model.printer.ModuleProxyVisitor
  public Object visitEventDeclProxy
      (final EventDeclProxy proxy)
    throws VisitorException
  {
    if (!proxy.isObservable()) {
      print("unobservable ");
    }
    print(proxy.getName());
    final List<SimpleExpressionProxy> ranges = proxy.getRanges();
    for (final SimpleExpressionProxy expr : ranges) {
      print('[');
      expr.acceptVisitor(this);
      print(']');
    }
    return null;
  }

  public Object visitForeachProxy
      (final ForeachProxy proxy)
    throws VisitorException
  {
    print("<B>FOR</B> ");
    print(proxy.getName());
    print(" <B>IN</B> ");
    final SimpleExpressionProxy range = proxy.getRange();
    range.acceptVisitor(this);
    final SimpleExpressionProxy guard = proxy.getGuard();
    if (guard != null) {
      print(" <B>WHERE</B> ");
      guard.acceptVisitor(this);
    }
    return null;
  }

  public Object visitInstanceProxy
      (final InstanceProxy proxy)
    throws VisitorException
  {
    print("<I>");
    final IdentifierProxy identifier = proxy.getIdentifier();
    identifier.acceptVisitor(this);
    print("</I> = ");
    print(proxy.getModuleName());
    return null;
  }

  public Object visitParameterBindingProxy
      (final ParameterBindingProxy proxy)
    throws VisitorException
  {
    print("<I>");
    print(proxy.getName());
    print("</I> = ");
    final ExpressionProxy expression = proxy.getExpression();
    expression.acceptVisitor(this);
    return null;
  }

  public Object visitSimpleComponentProxy
      (final SimpleComponentProxy proxy)
    throws VisitorException
  {
    final IdentifierProxy identifier = proxy.getIdentifier();
    identifier.acceptVisitor(this);
    return null;
  }

}
