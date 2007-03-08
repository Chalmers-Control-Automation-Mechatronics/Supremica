//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.model.base
//# CLASS:   ProxyAccessorByContents
//###########################################################################
//# $Id: ProxyAccessorByContents.java,v 1.3 2007-03-08 00:57:12 robi Exp $
//###########################################################################

package net.sourceforge.waters.model.base;


/**
 * @author Robi Malik
 */

public class ProxyAccessorByContents<P extends Proxy>
  implements ProxyAccessor<P>
{

  //#########################################################################
  //# Constructors
  public ProxyAccessorByContents(final P proxy)
  {
    mProxy = proxy;
  }


  //#########################################################################
  //# Equality and HashCode
  public boolean equals(final Object partner)
  {
    if (partner instanceof ProxyAccessorByContents<?>) {
      final ProxyAccessorByContents<?> accessor =
        (ProxyAccessorByContents<?>) partner;
      return ProxyTools.equalsByContents(mProxy, accessor.mProxy);
    } else {
      return false;
    }
  }

  public int hashCode()
  {
    return ProxyTools.hashCodeByContents(mProxy);
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
