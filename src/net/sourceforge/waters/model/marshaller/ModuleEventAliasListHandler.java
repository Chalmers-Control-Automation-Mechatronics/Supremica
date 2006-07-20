//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.model.marshaller
//# CLASS:   ModuleEventAliasListHandler
//###########################################################################
//# $Id: ModuleEventAliasListHandler.java,v 1.3 2006-07-20 02:28:37 robi Exp $
//###########################################################################

package net.sourceforge.waters.model.marshaller;

import java.util.List;

import net.sourceforge.waters.model.base.Proxy;

import net.sourceforge.waters.xsd.module.EventAliasList;
import net.sourceforge.waters.xsd.module.Module;
import net.sourceforge.waters.xsd.module.ObjectFactory;


class ModuleEventAliasListHandler
  extends JAXBListHandler<Module,EventAliasList,Proxy>
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
  EventAliasList createListElement(final Module container)
  {
    final EventAliasList listelem = mFactory.createEventAliasList();
    container.setEventAliasList(listelem);
    return listelem;
  }

  EventAliasList getListElement(final Module container)
  {
    return container.getEventAliasList();
  }

  List getList(final EventAliasList listelem)
  {
    return listelem.getList();
  }


  //#########################################################################
  //# Data Members
  private final ObjectFactory mFactory;

}
