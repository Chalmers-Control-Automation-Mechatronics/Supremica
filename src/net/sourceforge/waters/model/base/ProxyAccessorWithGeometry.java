//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.model.base
//# CLASS:   ProxyAccessorWithGeometry
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.model.base;


/**
 * @author Robi Malik
 */

public class ProxyAccessorWithGeometry<P extends Proxy>
  implements ProxyAccessor<P>
{

  //#########################################################################
  //# Constructors
  public ProxyAccessorWithGeometry(final P proxy)
  {
    mProxy = proxy;
  }


  //#########################################################################
  //# Equality and HashCode
  public boolean equals(final Object partner)
  {
    if (partner instanceof ProxyAccessorWithGeometry<?>) {
      final ProxyAccessorWithGeometry<?> accessor =
        (ProxyAccessorWithGeometry<?>) partner;
      return ProxyTools.equalsWithGeometry(mProxy, accessor.mProxy);
    } else {
      return false;
    }
  }

  public int hashCode()
  {
    return ProxyTools.hashCodeWithGeometry(mProxy);
  }


  //#########################################################################
  //# Simple Access Methods
  public P getProxy()
  {
    return mProxy;
  }


  //#########################################################################
  //# Data Members
  private final P mProxy;

}
