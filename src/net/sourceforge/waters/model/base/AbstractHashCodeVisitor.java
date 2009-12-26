//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters/Supremica GUI
//# PACKAGE: net.sourceforge.waters.model.base
//# CLASS:   EqualityDiagnoser
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.model.base;

import java.util.Collection;
import java.util.List;


/**
 * @author Robi Malik
 */

public abstract class AbstractHashCodeVisitor
  implements ProxyVisitor
{

  //#########################################################################
  //# Invocation
  public AbstractHashCodeVisitor()
  {
    this(false);
  }

  public AbstractHashCodeVisitor(final boolean geo)
  {
    mIsRespectingGeometry = geo;
  }


  //#########################################################################
  //# Simple Access
  public boolean isRespectingGeometry()
  {
    return mIsRespectingGeometry;
  }


  //#########################################################################
  //# Invocation
  public int hashCode(final Proxy proxy)
  {
    try {
      return getProxyHashCode(proxy);
    } catch (final VisitorException exception) {
      throw exception.getRuntimeException();
    }
  }


  //#########################################################################
  //# Interface net.sourceforge.waters.base.ProxyVisitor
  public Integer visitProxy(final Proxy proxy)
  {
    final Class<? extends Proxy> iface = proxy.getProxyInterface();
    return iface.hashCode();
  }

  public Integer visitGeometryProxy(final GeometryProxy proxy)
  {
    return visitProxy(proxy);
  }

  public Integer visitNamedProxy(final NamedProxy proxy)
  {
    int result = visitProxy(proxy);
    result *= 5;
    final String name = proxy.getName();
    result += name.hashCode();
    return result;
  }

  public Integer visitDocumentProxy(final DocumentProxy proxy)
  {
    int result = visitNamedProxy(proxy);
    result *= 5;
    final String comment = proxy.getComment();
    result += getOptionalHashCode(comment);
    return result;
  }


  //#########################################################################
  //# Auxiliary Methods
  protected int getOptionalHashCode(final Object item)
  {
    if (item == null) {
      return HASH_NULL;
    } else {
      return item.hashCode();
    }
  }

  protected int getProxyHashCode(final Proxy proxy)
    throws VisitorException
  {
    if (proxy == null) {
      return HASH_NULL;
    } else {
      return (Integer) proxy.acceptVisitor(this);
    }
  }

  protected int getSetHashCode(final Collection<? extends Proxy> set)
    throws VisitorException
  {
    int result = 0;
    for (final Proxy proxy : set) {
      result += (Integer) proxy.acceptVisitor(this);
    }
    return result;
  }

  protected int getListHashCode(final List<? extends Proxy> list)
    throws VisitorException
  {
    int result = 0;
    for (final Proxy proxy : list) {
      result *= 5;
      result += (Integer) proxy.acceptVisitor(this);
    }
    return result;
  }


  //#########################################################################
  //# Data Members
  private final boolean mIsRespectingGeometry;


  //#########################################################################
  //# Class Constants
  /**
   * A cryptic constant returned as hash code for <CODE>null</CODE>
   * references.
   */
  private static final int HASH_NULL = 0xabababab;

}
