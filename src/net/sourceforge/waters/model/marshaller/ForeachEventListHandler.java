//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.model.marshaller
//# CLASS:   ForeachEventListHandler
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.model.marshaller;

import java.util.List;

import net.sourceforge.waters.model.base.Proxy;

import net.sourceforge.waters.xsd.module.EventListType;
import net.sourceforge.waters.xsd.module.ForeachEvent;
import net.sourceforge.waters.xsd.module.ObjectFactory;


class ForeachEventListHandler
  extends JAXBListHandler<ForeachEvent,EventListType,Proxy>
{


  //#########################################################################
  //# Constructors
  ForeachEventListHandler()
  {
    this(null);
  }

  ForeachEventListHandler(final ObjectFactory factory)
  {
    mFactory = factory;
  }


  //#########################################################################
  //# Overrides for Abstract Base Class JAXBListHandler
  EventListType createListElement(final ForeachEvent container)
  {
    final EventListType listelem = mFactory.createEventListType();
    container.setEventList(listelem);
    return listelem;
  }

  EventListType getListElement(final ForeachEvent container)
  {
    return container.getEventList();
  }

  List getList(final EventListType listelem)
  {
    return listelem.getList();
  }


  //#########################################################################
  //# Data Members
  private final ObjectFactory mFactory;

}
