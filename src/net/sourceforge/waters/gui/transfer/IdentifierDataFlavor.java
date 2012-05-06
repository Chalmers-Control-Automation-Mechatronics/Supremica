//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters GUI
//# PACKAGE: net.sourceforge.waters.gui.transfer
//# CLASS:   IdentifierDataFlavor
//###########################################################################
//# $Id$
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
import net.sourceforge.waters.model.module.EventListExpressionProxy;
import net.sourceforge.waters.model.module.ForeachProxy;
import net.sourceforge.waters.model.module.IdentifiedProxy;
import net.sourceforge.waters.model.module.IdentifierProxy;
import net.sourceforge.waters.model.module.LabelBlockProxy;
import net.sourceforge.waters.model.module.ModuleProxyCloner;
import net.sourceforge.waters.model.module.ModuleProxyFactory;
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
      final List<Proxy> list = elist.getEventList();
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