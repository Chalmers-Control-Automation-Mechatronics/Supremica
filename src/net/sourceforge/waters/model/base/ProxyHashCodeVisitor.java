//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters/Supremica GUI
//# PACKAGE: net.sourceforge.waters.model.base
//# CLASS:   EqualityDiagnoser
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.model.base;


/**
 * @author Robi Malik
 */

public abstract class ProxyHashCodeVisitor
  implements ProxyVisitor
{

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


  //#########################################################################
  //# Class Constants
  /**
   * A cryptic constant returned as hash code for <CODE>null</CODE>
   * references.
   */
  private static final int HASH_NULL = 0xabababab;

}
