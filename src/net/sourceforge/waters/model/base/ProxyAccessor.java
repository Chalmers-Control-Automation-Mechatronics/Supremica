//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.model.base
//# CLASS:   ProxyAccessor
//###########################################################################
//# $Id$
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
