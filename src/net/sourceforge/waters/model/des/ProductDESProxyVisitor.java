//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.model.des
//# CLASS:   ProductDESProxyVisitor
//###########################################################################
//# $Id: ProductDESProxyVisitor.java,v 1.2 2005-11-03 01:24:16 robi Exp $
//###########################################################################

package net.sourceforge.waters.model.des;

import net.sourceforge.waters.model.base.ProxyVisitor;
import net.sourceforge.waters.model.base.VisitorException;


public interface ProductDESProxyVisitor
  extends ProxyVisitor
{

  public Object visitAutomatonProxy(AutomatonProxy proxy)
    throws VisitorException;

  public Object visitEventProxy(EventProxy proxy)
    throws VisitorException;

  public Object visitProductDESProxy(ProductDESProxy proxy)
    throws VisitorException;

  public Object visitStateProxy(StateProxy proxy)
    throws VisitorException;

  public Object visitTransitionProxy(TransitionProxy proxy)
    throws VisitorException;

}
