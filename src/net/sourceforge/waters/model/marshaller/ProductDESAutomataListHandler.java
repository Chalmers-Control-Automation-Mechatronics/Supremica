//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.model.marshaller
//# CLASS:   ProductDESAutomataListHandler
//###########################################################################
//# $Id: ProductDESAutomataListHandler.java,v 1.2 2005-11-03 01:24:16 robi Exp $
//###########################################################################

package net.sourceforge.waters.model.marshaller;

import java.util.List;
import javax.xml.bind.JAXBException;

import net.sourceforge.waters.model.des.AutomatonProxy;

import net.sourceforge.waters.xsd.des.AutomataListType;
import net.sourceforge.waters.xsd.des.ObjectFactory;
import net.sourceforge.waters.xsd.des.ProductDESType;


class ProductDESAutomataListHandler
  extends JAXBCheckedListHandler
            <ProductDESType,AutomataListType,AutomatonProxy>
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
  AutomataListType createListElement(ProductDESType container)
    throws JAXBException
  {
    final AutomataListType listelem = mFactory.createAutomataList();
    container.setAutomataList(listelem);
    return listelem;
  }

  AutomataListType getListElement(ProductDESType container)
  {
    return container.getAutomataList();
  }

  List getList(AutomataListType listelem)
  {
    return listelem.getList();
  }


  //#########################################################################
  //# Data Members
  private final ObjectFactory mFactory;

}
