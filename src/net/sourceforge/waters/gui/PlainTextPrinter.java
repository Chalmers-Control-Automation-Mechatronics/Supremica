//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.gui
//# CLASS:   HTMLPrinter
//###########################################################################
//# $Id: PlainTextPrinter.java,v 1.3 2007-06-08 10:45:20 robi Exp $
//###########################################################################

package net.sourceforge.waters.gui;

import java.io.IOException;
import java.io.Writer;
import java.util.List;

import net.sourceforge.waters.model.base.Proxy;
import net.sourceforge.waters.model.base.VisitorException;
import net.sourceforge.waters.model.module.EventDeclProxy;
import net.sourceforge.waters.model.module.ExpressionProxy;
import net.sourceforge.waters.model.module.ForeachProxy;
import net.sourceforge.waters.model.module.IdentifierProxy;
import net.sourceforge.waters.model.module.InstanceProxy;
import net.sourceforge.waters.model.module.ParameterBindingProxy;
import net.sourceforge.waters.model.module.SimpleComponentProxy;
import net.sourceforge.waters.model.module.SimpleExpressionProxy;
import net.sourceforge.waters.model.printer.ModuleProxyPrinter;


public class PlainTextPrinter
  extends ModuleProxyPrinter
{

  //#########################################################################
  //# Constructors
  public PlainTextPrinter()
  {
    super();
  }

  public PlainTextPrinter(final Writer writer)
  {
    super(writer);
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
    print("FOR ");
    print(proxy.getName());
    print(" IN ");
    final SimpleExpressionProxy range = proxy.getRange();
    range.acceptVisitor(this);
    final SimpleExpressionProxy guard = proxy.getGuard();
    if (guard != null) {
      print(" WHERE ");
      guard.acceptVisitor(this);
    }
    return null;
  }

  public Object visitInstanceProxy
      (final InstanceProxy proxy)
    throws VisitorException
  {
    final IdentifierProxy identifier = proxy.getIdentifier();
    identifier.acceptVisitor(this);
    print(" = ");
    print(proxy.getModuleName());
    return null;
  }

  public Object visitParameterBindingProxy
      (final ParameterBindingProxy proxy)
    throws VisitorException
  {
    print(proxy.getName());
    print(" = ");
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
