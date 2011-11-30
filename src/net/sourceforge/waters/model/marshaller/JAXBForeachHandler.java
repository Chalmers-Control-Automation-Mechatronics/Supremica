//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.model.marshaller
//# CLASS:   JAXBForeachHandler
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.model.marshaller;

import java.util.Collection;

import net.sourceforge.waters.model.base.Proxy;
import net.sourceforge.waters.model.base.ProxyVisitor;
import net.sourceforge.waters.model.base.VisitorException;
import net.sourceforge.waters.xsd.base.ElementType;
import net.sourceforge.waters.xsd.module.ForeachType;


abstract class JAXBForeachHandler<C extends ForeachType, L extends ElementType>
  extends JAXBListHandler<C,L,Proxy>
{

  //#########################################################################
  //# Provided by Subclasses
  abstract ForeachType createForeachElement();

  //#########################################################################
  //# Provided by Subclasses
  @SuppressWarnings("unchecked")
  void toJAXBUnsafe(final ProxyVisitor exporter,
                    final Collection<? extends Proxy> body,
                    final ForeachType container)
    throws VisitorException
  {
    super.toJAXB(exporter, body, (C) container);
  }

}
