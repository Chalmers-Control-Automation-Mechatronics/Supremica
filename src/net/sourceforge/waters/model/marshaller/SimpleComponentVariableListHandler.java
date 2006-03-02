//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters(EFA)
//# PACKAGE: net.sourceforge.waters.model.marshaller
//# CLASS:   SimpleComponentVariableListHandler
//###########################################################################
//# $Id: SimpleComponentVariableListHandler.java,v 1.2 2006-03-02 12:12:50 martin Exp $
//###########################################################################

package net.sourceforge.waters.model.marshaller;

import java.util.List;
import javax.xml.bind.JAXBException;


import net.sourceforge.waters.model.module.VariableProxy;

import net.sourceforge.waters.xsd.module.VariableListType;
import net.sourceforge.waters.xsd.module.SimpleComponentType;
import net.sourceforge.waters.xsd.module.ObjectFactory;


class SimpleComponentVariableListHandler
  extends JAXBListHandler<SimpleComponentType,VariableListType,VariableProxy>
{


  //#########################################################################
  //# Constructors
	SimpleComponentVariableListHandler()
  {
    this(null);
  }

  SimpleComponentVariableListHandler(final ObjectFactory factory)
  {
    mFactory = factory;
  }


  //#########################################################################
  //# Overrides for Abstract Base Class JAXBCheckedListHandler
  VariableListType createListElement(final SimpleComponentType container)
    throws JAXBException
  {
    final VariableListType listelem = mFactory.createVariableList();
    container.setVariableList(listelem);
    return listelem;
  }
  
  VariableListType getListElement(final SimpleComponentType container)
  {
    return container.getVariableList();
  }

  List getList(final VariableListType listelem)
  {
    return listelem.getList();
  }


  //#########################################################################
  //# Data Members
  private final ObjectFactory mFactory;

}
