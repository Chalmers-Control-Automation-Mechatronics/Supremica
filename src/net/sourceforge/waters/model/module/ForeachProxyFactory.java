//###########################################################################
//# PROJECT: Waters
//# PACKAGE: waters.model.module
//# CLASS:   ForeachProxyFactory
//###########################################################################
//# $Id: ForeachProxyFactory.java,v 1.1 2005-02-17 01:43:35 knut Exp $
//###########################################################################

package net.sourceforge.waters.model.module;

import net.sourceforge.waters.model.base.ProxyFactory;
import net.sourceforge.waters.xsd.base.ElementType;
import net.sourceforge.waters.xsd.module.ForeachType;


public interface ForeachProxyFactory extends ProxyFactory {

  public ElementType getForeachBody(final ForeachType foreach);

}
