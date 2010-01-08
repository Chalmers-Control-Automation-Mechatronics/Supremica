//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.model.marshaller
//# CLASS:   CheckedExportSet
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.model.marshaller;

import java.util.Collection;

import net.sourceforge.waters.model.base.DuplicateNameException;
import net.sourceforge.waters.model.base.IndexedTreeSet;
import net.sourceforge.waters.model.base.NamedProxy;
import net.sourceforge.waters.model.base.Proxy;
import net.sourceforge.waters.model.base.ProxyTools;
import net.sourceforge.waters.model.unchecked.Casting;


class CheckedExportSet<P extends NamedProxy>
  extends IndexedTreeSet<P>
{

  //#########################################################################
  //# Constructors
  public CheckedExportSet(final Proxy container,
                          final String itemkindname)
    throws DuplicateNameException
  {
    mContainer = container;
    mItemKindName = itemkindname;
  }

  public CheckedExportSet(final Collection<? extends P> collection,
                          final Proxy container,
                          final String itemkindname)
    throws DuplicateNameException
  {
    super(collection);
    mContainer = container;
    mItemKindName = itemkindname;
  }


  //#########################################################################
  //# Cloning
  public CheckedExportSet<P> clone()
  {
    final Class<CheckedExportSet<P>> clazz = Casting.toClass(getClass());
    return clazz.cast(super.clone());
  }


  //#########################################################################
  //# Error Messages
  protected void appendContainerName(final StringBuffer buffer)
  {
    final Class<? extends Proxy> clazz = mContainer.getProxyInterface();
    final String clazzname = ProxyTools.getShortClassName(clazz);
    buffer.append(clazzname);
    if (mContainer instanceof NamedProxy) {
      final NamedProxy named = (NamedProxy) mContainer;
      buffer.append(" '");
      buffer.append(named.getName());
      buffer.append('\'');
    }
  }

  protected void appendItemKindName(final StringBuffer buffer)
  {
    buffer.append(mItemKindName);
  }


  //#########################################################################
  //# Data Members
  private final Proxy mContainer;
  private final String mItemKindName;


  //#########################################################################
  //# Class Constants
  private static final long serialVersionUID = 1L;

}
