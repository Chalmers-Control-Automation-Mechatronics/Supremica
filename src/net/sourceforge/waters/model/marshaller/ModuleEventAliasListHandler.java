//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.model.marshaller
//# CLASS:   ModuleEventAliasListHandler
//###########################################################################
//# $Id: ModuleEventAliasListHandler.java,v 1.2 2005-11-03 01:24:16 robi Exp $
//###########################################################################

package net.sourceforge.waters.model.marshaller;

import java.util.List;
import javax.xml.bind.JAXBException;

import net.sourceforge.waters.model.base.Proxy;

import net.sourceforge.waters.xsd.module.EventAliasListType;
import net.sourceforge.waters.xsd.module.ModuleType;
import net.sourceforge.waters.xsd.module.ObjectFactory;


class ModuleEventAliasListHandler
  extends JAXBListHandler<ModuleType,EventAliasListType,Proxy>
{


  //#########################################################################
  //# Constructors
  ModuleEventAliasListHandler()
  {
    this(null);
  }

  ModuleEventAliasListHandler(final ObjectFactory factory)
  {
    mFactory = factory;
  }


  //#########################################################################
  //# Overrides for Abstract Base Class JAXBListHandler
  EventAliasListType createListElement(final ModuleType container)
    throws JAXBException
  {
    final EventAliasListType listelem = mFactory.createEventAliasList();
    container.setEventAliasList(listelem);
    return listelem;
  }

  EventAliasListType getListElement(final ModuleType container)
  {
    return container.getEventAliasList();
  }

  List getList(final EventAliasListType listelem)
  {
    return listelem.getList();
  }


  //#########################################################################
  //# Data Members
  private final ObjectFactory mFactory;

}
