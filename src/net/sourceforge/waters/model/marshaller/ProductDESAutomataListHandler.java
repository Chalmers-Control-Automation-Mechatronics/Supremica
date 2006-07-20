//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.model.marshaller
//# CLASS:   ProductDESAutomataListHandler
//###########################################################################
//# $Id: ProductDESAutomataListHandler.java,v 1.3 2006-07-20 02:28:37 robi Exp $
//###########################################################################

package net.sourceforge.waters.model.marshaller;

import java.util.List;

import net.sourceforge.waters.model.des.AutomatonProxy;

import net.sourceforge.waters.xsd.des.AutomataList;
import net.sourceforge.waters.xsd.des.ObjectFactory;
import net.sourceforge.waters.xsd.des.ProductDES;


class ProductDESAutomataListHandler
  extends JAXBCheckedListHandler
            <ProductDES,AutomataList,AutomatonProxy>
{
  
  //#########################################################################
  //# Constructors
  ProductDESAutomataListHandler()
  {
    this(null);
  }

  ProductDESAutomataListHandler(final ObjectFactory factory)
  {
    mFactory = factory;
  }

  
  //#########################################################################
  //# Overrides for Abstract Base Class JAXBListHandler
  AutomataList createListElement(ProductDES container)
  {
    final AutomataList listelem = mFactory.createAutomataList();
    container.setAutomataList(listelem);
    return listelem;
  }

  AutomataList getListElement(ProductDES container)
  {
    return container.getAutomataList();
  }

  List getList(AutomataList listelem)
  {
    return listelem.getList();
  }


  //#########################################################################
  //# Data Members
  private final ObjectFactory mFactory;

}
