//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.model.marshaller
//# CLASS:   EventListExpressionEventListHandler
//###########################################################################
//# $Id: EventListExpressionEventListHandler.java,v 1.2 2005-11-03 01:24:16 robi Exp $
//###########################################################################

package net.sourceforge.waters.model.marshaller;

import java.util.List;
import javax.xml.bind.JAXBException;

import net.sourceforge.waters.model.base.Proxy;

import net.sourceforge.waters.xsd.module.EventListExpressionType;
import net.sourceforge.waters.xsd.module.EventListType;
import net.sourceforge.waters.xsd.module.ObjectFactory;


class EventListExpressionEventListHandler
  extends JAXBListHandler<EventListExpressionType,EventListType,Proxy>
{


  //#########################################################################
  //# Constructors
  EventListExpressionEventListHandler()
  {
    this(null);
  }

  EventListExpressionEventListHandler(final ObjectFactory factory)
  {
    mFactory = factory;
  }


  //#########################################################################
  //# Overrides for Abstract Base Class JAXBListHandler
  EventListType createListElement(final EventListExpressionType container)
    throws JAXBException
  {
    final EventListType listelem = mFactory.createEventList();
    container.setEventList(listelem);
    return listelem;
  }

  EventListType getListElement(final EventListExpressionType container)
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
