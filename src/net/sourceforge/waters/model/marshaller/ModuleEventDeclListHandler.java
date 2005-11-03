//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.model.marshaller
//# CLASS:   ModuleEventDeclListHandler
//###########################################################################
//# $Id: ModuleEventDeclListHandler.java,v 1.2 2005-11-03 01:24:16 robi Exp $
//###########################################################################

package net.sourceforge.waters.model.marshaller;

import java.util.List;
import javax.xml.bind.JAXBException;

import net.sourceforge.waters.model.module.EventDeclProxy;

import net.sourceforge.waters.xsd.module.EventDeclListType;
import net.sourceforge.waters.xsd.module.ModuleType;
import net.sourceforge.waters.xsd.module.ObjectFactory;


class ModuleEventDeclListHandler
  extends JAXBListHandler<ModuleType,EventDeclListType,EventDeclProxy>
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
  EventDeclListType createListElement(final ModuleType container)
    throws JAXBException
  {
    final EventDeclListType listelem = mFactory.createEventDeclList();
    container.setEventDeclList(listelem);
    return listelem;
  }

  EventDeclListType getListElement(final ModuleType container)
  {
    return container.getEventDeclList();
  }

  List getList(final EventDeclListType listelem)
  {
    return listelem.getList();
  }


  //#########################################################################
  //# Data Members
  private final ObjectFactory mFactory;

}
