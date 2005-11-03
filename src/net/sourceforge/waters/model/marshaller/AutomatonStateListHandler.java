//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.model.marshaller
//# CLASS:   AutomatonStateListHandler
//###########################################################################
//# $Id: AutomatonStateListHandler.java,v 1.2 2005-11-03 01:24:16 robi Exp $
//###########################################################################

package net.sourceforge.waters.model.marshaller;

import java.util.List;
import javax.xml.bind.JAXBException;

import net.sourceforge.waters.model.des.StateProxy;

import net.sourceforge.waters.xsd.des.AutomatonType;
import net.sourceforge.waters.xsd.des.ObjectFactory;
import net.sourceforge.waters.xsd.des.StateListType;


class AutomatonStateListHandler
  extends JAXBCheckedListHandler<AutomatonType,StateListType,StateProxy>
{
  
  //#########################################################################
  //# Constructors
  AutomatonStateListHandler()
  {
    this(null);
  }

  AutomatonStateListHandler(final ObjectFactory factory)
  {
    mFactory = factory;
  }

  
  //#########################################################################
  //# Overrides for Abstract Base Class JAXBListHandler
  StateListType createListElement(final AutomatonType container)
    throws JAXBException
  {
    final StateListType listelem = mFactory.createStateList();
    container.setStateList(listelem);
    return listelem;
  }

  StateListType getListElement(final AutomatonType container)
  {
    return container.getStateList();
  }

  List getList(final StateListType listelem)
  {
    return listelem.getList();
  }


  //#########################################################################
  //# Data Members
  private final ObjectFactory mFactory;

}
