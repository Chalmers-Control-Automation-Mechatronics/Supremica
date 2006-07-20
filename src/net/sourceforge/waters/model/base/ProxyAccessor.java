//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.model.base
//# CLASS:   ProxyAccessor
//###########################################################################
//# $Id: ProxyAccessor.java,v 1.2 2006-07-20 02:28:37 robi Exp $
//###########################################################################

package net.sourceforge.waters.model.base;


/**
 * @author Robi Malik
 */

public interface ProxyAccessor<P extends Proxy>
{

  //#########################################################################
  //# Simple Access Methods
  public P getProxy();

}
