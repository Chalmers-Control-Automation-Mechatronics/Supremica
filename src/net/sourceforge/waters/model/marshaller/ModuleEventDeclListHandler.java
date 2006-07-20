//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.model.marshaller
//# CLASS:   ModuleEventDeclListHandler
//###########################################################################
//# $Id: ModuleEventDeclListHandler.java,v 1.3 2006-07-20 02:28:37 robi Exp $
//###########################################################################

package net.sourceforge.waters.model.marshaller;

import java.util.List;

import net.sourceforge.waters.model.module.EventDeclProxy;

import net.sourceforge.waters.xsd.module.EventDeclList;
import net.sourceforge.waters.xsd.module.Module;
import net.sourceforge.waters.xsd.module.ObjectFactory;


class ModuleEventDeclListHandler
  extends JAXBListHandler<Module,EventDeclList,EventDeclProxy>
{


  //#########################################################################
  //# Constructors
  ModuleEventDeclListHandler()
  {
    this(null);
  }

  ModuleEventDeclListHandler(final ObjectFactory factory)
  {
    mFactory = factory;
  }


  //#########################################################################
  //# Overrides for Abstract Base Class JAXBListHandler
  EventDeclList createListElement(final Module container)
  {
    final EventDeclList listelem = mFactory.createEventDeclList();
    container.setEventDeclList(listelem);
    return listelem;
  }

  EventDeclList getListElement(final Module container)
  {
    return container.getEventDeclList();
  }

  List getList(final EventDeclList listelem)
  {
    return listelem.getList();
  }


  //#########################################################################
  //# Data Members
  private final ObjectFactory mFactory;

}
