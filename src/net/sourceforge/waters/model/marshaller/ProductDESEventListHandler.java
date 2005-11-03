//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.model.marshaller
//# CLASS:   ProductDESEventListHandler
//###########################################################################
//# $Id: ProductDESEventListHandler.java,v 1.2 2005-11-03 01:24:16 robi Exp $
//###########################################################################

package net.sourceforge.waters.model.marshaller;

import java.util.List;
import javax.xml.bind.JAXBException;

import net.sourceforge.waters.model.des.EventProxy;

import net.sourceforge.waters.xsd.des.EventListType;
import net.sourceforge.waters.xsd.des.ObjectFactory;
import net.sourceforge.waters.xsd.des.ProductDESType;


class ProductDESEventListHandler
  extends JAXBCheckedListHandler<ProductDESType,EventListType,EventProxy>
{
  
  //#########################################################################
  //# Constructors
  ProductDESEventListHandler()
  {
    this(null);
  }

  ProductDESEventListHandler(final ObjectFactory factory)
  {
    mFactory = factory;
  }

  
  //#########################################################################
  //# Overrides for Abstract Base Class JAXBListHandler
  EventListType createListElement(ProductDESType container)
    throws JAXBException
  {
    final EventListType listelem = mFactory.createEventList();
    container.setEventList(listelem);
    return listelem;
  }

  EventListType getListElement(ProductDESType container)
  {
    return container.getEventList();
  }

  List getList(EventListType listelem)
  {
    return listelem.getList();
  }


  //#########################################################################
  //# Data Members
  private final ObjectFactory mFactory;

}
