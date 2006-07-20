//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.model.base
//# CLASS:   ProxyAccessorWithGeometry
//###########################################################################
//# $Id: ProxyAccessorWithGeometry.java,v 1.2 2006-07-20 02:28:37 robi Exp $
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
      return
        mProxy == null ? accessor.mProxy == null :
        mProxy.equalsWithGeometry(accessor.mProxy);
    } else {
      return false;
    }
  }

  public int hashCode()
  {
    if (mProxy == null) {
      return 0;
    } else {
      return mProxy.hashCodeWithGeometry();
    }
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
