//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters(EFA)
//# PACKAGE: net.sourceforge.waters.model.marshaller
//# CLASS:   SimpleComponentVariableListHandler
//###########################################################################
//# $Id: SimpleComponentVariableListHandler.java,v 1.3 2006-07-20 02:28:37 robi Exp $
//###########################################################################

package net.sourceforge.waters.model.marshaller;

import java.util.List;

import net.sourceforge.waters.model.module.VariableProxy;

import net.sourceforge.waters.xsd.module.VariableList;
import net.sourceforge.waters.xsd.module.SimpleComponent;
import net.sourceforge.waters.xsd.module.ObjectFactory;


class SimpleComponentVariableListHandler
  extends JAXBListHandler<SimpleComponent,VariableList,VariableProxy>
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
  VariableList createListElement(final SimpleComponent container)
  {
    final VariableList listelem = mFactory.createVariableList();
    container.setVariableList(listelem);
    return listelem;
  }
  
  VariableList getListElement(final SimpleComponent container)
  {
    return container.getVariableList();
  }

  List getList(final VariableList listelem)
  {
    return listelem.getList();
  }


  //#########################################################################
  //# Data Members
  private final ObjectFactory mFactory;

}
