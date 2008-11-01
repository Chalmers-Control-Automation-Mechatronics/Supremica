//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.model.marshaller
//# CLASS:   ForeachComponentListHandler
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.model.marshaller;

import java.util.List;

import net.sourceforge.waters.model.base.Proxy;

import net.sourceforge.waters.xsd.module.ComponentList;
import net.sourceforge.waters.xsd.module.ForeachComponent;
import net.sourceforge.waters.xsd.module.ObjectFactory;


class ForeachComponentListHandler
  extends JAXBListHandler<ForeachComponent,ComponentList,Proxy>
{


  //#########################################################################
  //# Constructors
  ForeachComponentListHandler()
  {
    this(null);
  }

  ForeachComponentListHandler(final ObjectFactory factory)
  {
    mFactory = factory;
  }


  //#########################################################################
  //# Overrides for Abstract Base Class JAXBListHandler
  ComponentList createListElement(final ForeachComponent container)
  {
    final ComponentList listelem = mFactory.createComponentList();
    container.setComponentList(listelem);
    return listelem;
  }

  ComponentList getListElement(final ForeachComponent container)
  {
    return container.getComponentList();
  }

  List getList(final ComponentList listelem)
  {
    return listelem.getList();
  }


  //#########################################################################
  //# Data Members
  private final ObjectFactory mFactory;

}
