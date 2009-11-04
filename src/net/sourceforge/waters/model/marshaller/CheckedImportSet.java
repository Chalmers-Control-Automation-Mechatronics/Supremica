//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.model.marshaller
//# CLASS:   CheckedImportSet
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.model.marshaller;

import net.sourceforge.waters.model.base.IndexedHashSet;
import net.sourceforge.waters.model.base.NamedProxy;
import net.sourceforge.waters.model.unchecked.Casting;


class CheckedImportSet<P extends NamedProxy>
  extends IndexedHashSet<P>
{

  //#########################################################################
  //# Constructors
  public CheckedImportSet(final Class<?> containerclazz,
                          final String itemkindname)
  {
    this(containerclazz, null, itemkindname);
  }

  public CheckedImportSet(final Class<?> containerclazz,
                          final String containername,
                          final String itemkindname)
  {
    mContainerClazz = containerclazz;
    mContainerName = containername;
    mItemKindName = itemkindname;
  }


  //#########################################################################
  //# Cloning
  public CheckedImportSet<P> clone()
  {
    final Class<CheckedImportSet<P>> clazz = Casting.toClass(getClass());
    return clazz.cast(super.clone());
  }


  //#########################################################################
  //# Error Messages
  protected void appendContainerName(final StringBuffer buffer)
  {
    final String clazzname = getShortClassName(mContainerClazz);
    buffer.append(clazzname);
    if (mContainerName != null) {
      buffer.append(" '");
      buffer.append(mContainerName);
      buffer.append('\'');
    }
  }

  protected void appendItemKindName(final StringBuffer buffer)
  {
    buffer.append(mItemKindName);
  }



  //#########################################################################
  //# Data Members
  private final Class<?> mContainerClazz;
  private final String mContainerName;
  private final String mItemKindName;


  //#########################################################################
  //# Class Constants
  private static final long serialVersionUID = 1L;

}
