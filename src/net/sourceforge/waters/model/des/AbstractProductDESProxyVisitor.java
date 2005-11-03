//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.model.des
//# CLASS:   AbstractProductDESProxyVisitor
//###########################################################################
//# $Id: AbstractProductDESProxyVisitor.java,v 1.2 2005-11-03 01:24:15 robi Exp $
//###########################################################################

package net.sourceforge.waters.model.des;

import net.sourceforge.waters.model.base.AbstractProxyVisitor;
import net.sourceforge.waters.model.base.VisitorException;


public class AbstractProductDESProxyVisitor
  extends AbstractProxyVisitor
  implements ProductDESProxyVisitor
{

  public Object visitAutomatonProxy(final AutomatonProxy proxy)
    throws VisitorException
  {
    return visitNamedProxy(proxy);
  }

  public Object visitEventProxy(final EventProxy proxy)
    throws VisitorException
  {
    return visitNamedProxy(proxy);
  }

  public Object visitProductDESProxy(final ProductDESProxy proxy)
    throws VisitorException
  {
    return visitDocumentProxy(proxy);
  }

  public Object visitStateProxy(final StateProxy proxy)
    throws VisitorException
  {
    return visitNamedProxy(proxy);
  }

  public Object visitTransitionProxy(final TransitionProxy proxy)
    throws VisitorException
  {
    return visitProxy(proxy);
  }

}
