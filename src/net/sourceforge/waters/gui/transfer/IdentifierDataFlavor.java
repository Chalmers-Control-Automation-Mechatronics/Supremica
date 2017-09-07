//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# Copyright (C) 2004-2017 Robi Malik
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

package net.sourceforge.waters.gui.transfer;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import net.sourceforge.waters.model.base.Proxy;
import net.sourceforge.waters.model.base.VisitorException;
import net.sourceforge.waters.model.module.DefaultModuleProxyVisitor;
import net.sourceforge.waters.model.module.EdgeProxy;
import net.sourceforge.waters.model.module.EventAliasProxy;
import net.sourceforge.waters.model.module.EventDeclProxy;
import net.sourceforge.waters.model.module.EventListExpressionProxy;
import net.sourceforge.waters.model.module.ExpressionProxy;
import net.sourceforge.waters.model.module.ForeachProxy;
import net.sourceforge.waters.model.module.IdentifiedProxy;
import net.sourceforge.waters.model.module.IdentifierProxy;
import net.sourceforge.waters.model.module.LabelBlockProxy;
import net.sourceforge.waters.model.module.ModuleProxyCloner;
import net.sourceforge.waters.model.module.ModuleProxyFactory;
import net.sourceforge.waters.model.module.ParameterBindingProxy;
import net.sourceforge.waters.model.module.SimpleExpressionProxy;


/**
 * <P>A data flavour representing a list of WATERS identifiers ({@link
 * IdentifierProxy}). This data flavour is supported by several types,
 * allowing conversion of event declarations ({@link EventDeclProxy})
 * and event aliases ({@link EventAliasProxy}) to identifiers.
 *
 * @author Robi Malik
 */

class IdentifierDataFlavor extends ModuleDataFlavor
{

  //#########################################################################
  //# Constructor
  IdentifierDataFlavor()
  {
    super(IdentifierProxy.class);
  }


  //#########################################################################
  //# Importing and Exporting Data
  @Override
  List<Proxy> createImportData(final Collection<? extends Proxy> data,
                               final ModuleProxyFactory factory)
  {
    return mVisitor.convert(data, factory);
  }


  //#########################################################################
  //# Inner Class ConversionVisitor
  private static class ConversionVisitor extends DefaultModuleProxyVisitor
  {

    //#######################################################################
    //# Invocation
    private List<Proxy> convert(final Collection<? extends Proxy> data,
                                final ModuleProxyFactory factory)
    {
      try {
        mFactory = factory;
        visitCollection(data);
        return mExportList;
      } catch (final VisitorException exception) {
        throw exception.getRuntimeException();
      } finally {
        mExportList = null;
      }
    }

    //#######################################################################
    //# Interface net.sourceforge.waters.model.printer.ModuleProxyVisitor
    @Override
    public Object visitEdgeProxy(final EdgeProxy edge)
    throws VisitorException
    {
      final LabelBlockProxy block = edge.getLabelBlock();
      if (block == null) {
        return null;
      } else {
        return block.acceptVisitor(this);
      }
    }

    @Override
    public Object visitEventListExpressionProxy
      (final EventListExpressionProxy elist)
    throws VisitorException
    {
      final List<Proxy> list = elist.getEventIdentifierList();
      return visitCollection(list);
    }

    @Override
    public Object visitForeachProxy(final ForeachProxy foreach)
    throws VisitorException
    {
      final String name = foreach.getName();
      final ModuleProxyCloner cloner = mFactory.getCloner();
      final SimpleExpressionProxy range = foreach.getRange();
      final SimpleExpressionProxy newRange =
        (SimpleExpressionProxy) cloner.getClone(range);
      final SimpleExpressionProxy guard = foreach.getGuard();
      final SimpleExpressionProxy newGuard =
        (SimpleExpressionProxy) cloner.getClone(guard);
      final List<Proxy> backup = mExportList;
      final List<Proxy> body = foreach.getBody();
      visitCollection(body);
      final ForeachProxy newForeach =
        mFactory.createForeachProxy(name, newRange, newGuard, mExportList);
      mExportList = backup;
      mExportList.add(newForeach);
      return null;
    }

    @Override
    public Object visitIdentifiedProxy(final IdentifiedProxy proxy)
    throws VisitorException
    {
      final IdentifierProxy ident = proxy.getIdentifier();
      return ident.acceptVisitor(this);
    }

    @Override
    public Object visitIdentifierProxy(final IdentifierProxy ident)
    {
      final ModuleProxyCloner cloner = mFactory.getCloner();
      final Proxy cloned = cloner.getClone(ident);
      mExportList.add(cloned);
      return null;
    }

    @Override
    public Object visitParameterBindingProxy(final ParameterBindingProxy param)
      throws VisitorException
    {
      final ExpressionProxy expr = param.getExpression();
      return expr.acceptVisitor(this);
    }

    @Override
    public Object visitSimpleExpressionProxy(final SimpleExpressionProxy expr)
    {
      // Quietly ignore expressions that are not identifiers
      return null;
    }

    //#######################################################################
    //# Overrides for
    //# net.sourceforge.waters.model.base.AbstractModuleProxyVisitor
    @Override
    public Object visitCollection(final Collection<? extends Proxy> collection)
    throws VisitorException
    {
      final int size = collection.size();
      mExportList = new ArrayList<Proxy>(size);
      return super.visitCollection(collection);
    }

    //#######################################################################
    //# Data Members
    private ModuleProxyFactory mFactory;
    private List<Proxy> mExportList;
  }


  //#########################################################################
  //# Class Constants
  private final ConversionVisitor mVisitor = new ConversionVisitor();

}
