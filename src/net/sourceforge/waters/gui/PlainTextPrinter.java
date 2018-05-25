//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# Copyright (C) 2004-2018 Robi Malik
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
  public PlainTextPrinter(final Writer writer)
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
      printProxy(proxy);
    } catch (final VisitorException exception) {
      unwrap(exception);
    }
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

  @Override
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

  @Override
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

  @Override
  public Object visitSimpleComponentProxy
      (final SimpleComponentProxy proxy)
    throws VisitorException
  {
    final IdentifierProxy identifier = proxy.getIdentifier();
    identifier.acceptVisitor(this);
    return null;
  }

}
