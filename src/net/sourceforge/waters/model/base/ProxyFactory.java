//###########################################################################
//# PROJECT: Waters
//# PACKAGE: waters.model.base
//# CLASS:   ProxyFactory
//###########################################################################
//# $Id: ProxyFactory.java,v 1.1 2005-02-17 01:43:35 knut Exp $
//###########################################################################

package net.sourceforge.waters.model.base;

import java.util.List;

import net.sourceforge.waters.xsd.base.ElementType;


public interface ProxyFactory {

  public Proxy createProxy(ElementType element)
    throws ModelException;

  public List getList(ElementType parent);

}
