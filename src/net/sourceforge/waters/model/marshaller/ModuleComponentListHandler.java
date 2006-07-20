//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.model.marshaller
//# CLASS:   ModuleComponentListHandler
//###########################################################################
//# $Id: ModuleComponentListHandler.java,v 1.3 2006-07-20 02:28:37 robi Exp $
//###########################################################################

package net.sourceforge.waters.model.marshaller;

import java.util.List;

import net.sourceforge.waters.model.base.Proxy;

import net.sourceforge.waters.xsd.module.ComponentList;
import net.sourceforge.waters.xsd.module.Module;
import net.sourceforge.waters.xsd.module.ObjectFactory;


class ModuleComponentListHandler
  extends JAXBListHandler<Module,ComponentList,Proxy>
{


  //#########################################################################
  //# Constructors
  ModuleComponentListHandler()
  {
    this(null);
  }

  ModuleComponentListHandler(final ObjectFactory factory)
  {
    mFactory = factory;
  }


  //#########################################################################
  //# Overrides for Abstract Base Class JAXBListHandler
  ComponentList createListElement(final Module container)
  {
    final ComponentList listelem = mFactory.createComponentList();
    container.setComponentList(listelem);
    return listelem;
  }

  ComponentList getListElement(final Module container)
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
