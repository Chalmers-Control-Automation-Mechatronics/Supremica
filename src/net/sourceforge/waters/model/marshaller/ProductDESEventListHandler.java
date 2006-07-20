//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.model.marshaller
//# CLASS:   ProductDESEventListHandler
//###########################################################################
//# $Id: ProductDESEventListHandler.java,v 1.3 2006-07-20 02:28:37 robi Exp $
//###########################################################################

package net.sourceforge.waters.model.marshaller;

import java.util.List;

import net.sourceforge.waters.model.des.EventProxy;

import net.sourceforge.waters.xsd.des.EventList;
import net.sourceforge.waters.xsd.des.ObjectFactory;
import net.sourceforge.waters.xsd.des.ProductDES;


class ProductDESEventListHandler
  extends JAXBCheckedListHandler<ProductDES,EventList,EventProxy>
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
  EventList createListElement(ProductDES container)
  {
    final EventList listelem = mFactory.createEventList();
    container.setEventList(listelem);
    return listelem;
  }

  EventList getListElement(ProductDES container)
  {
    return container.getEventList();
  }

  List getList(EventList listelem)
  {
    return listelem.getList();
  }


  //#########################################################################
  //# Data Members
  private final ObjectFactory mFactory;

}
