//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.model.marshaller
//# CLASS:   ModuleComponentListHandler
//###########################################################################
//# $Id: ModuleComponentListHandler.java,v 1.2 2005-11-03 01:24:16 robi Exp $
//###########################################################################

package net.sourceforge.waters.model.marshaller;

import java.util.List;
import javax.xml.bind.JAXBException;

import net.sourceforge.waters.model.base.Proxy;

import net.sourceforge.waters.xsd.module.ComponentListType;
import net.sourceforge.waters.xsd.module.ModuleType;
import net.sourceforge.waters.xsd.module.ObjectFactory;


class ModuleComponentListHandler
  extends JAXBListHandler<ModuleType,ComponentListType,Proxy>
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
  ComponentListType createListElement(final ModuleType container)
    throws JAXBException
  {
    final ComponentListType listelem = mFactory.createComponentList();
    container.setComponentList(listelem);
    return listelem;
  }

  ComponentListType getListElement(final ModuleType container)
  {
    return container.getComponentList();
  }

  List getList(final ComponentListType listelem)
  {
    return listelem.getList();
  }


  //#########################################################################
  //# Data Members
  private final ObjectFactory mFactory;

}
