//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters(EFA)
//# PACKAGE: net.sourceforge.waters.model.marshaller
//# CLASS:   GuardActionBlockActionListHandler
//###########################################################################
//# $Id: GuardActionBlockActionListHandler.java,v 1.2 2006-03-02 12:12:50 martin Exp $
//###########################################################################

package net.sourceforge.waters.model.marshaller;

import java.util.List;
import javax.xml.bind.JAXBException;


import net.sourceforge.waters.model.module.BinaryExpressionProxy;

import net.sourceforge.waters.xsd.module.ActionListType;
import net.sourceforge.waters.xsd.module.GuardActionBlockType;
import net.sourceforge.waters.xsd.module.ObjectFactory;


class GuardActionBlockActionListHandler
  extends JAXBListHandler<GuardActionBlockType,ActionListType, BinaryExpressionProxy>
{


  //#########################################################################
  //# Constructors
	GuardActionBlockActionListHandler()
  {
    this(null);
  }

  GuardActionBlockActionListHandler(final ObjectFactory factory)
  {
    mFactory = factory;
  }


  //#########################################################################
  //# Overrides for Abstract Base Class JAXBCheckedListHandler
  ActionListType createListElement(final GuardActionBlockType container)
    throws JAXBException
  {
    final ActionListType listelem = mFactory.createActionList();
    container.setActionList(listelem);
    return listelem;
  }
  
  ActionListType getListElement(final GuardActionBlockType container)
  {
    return container.getActionList();
  }

  List getList(final ActionListType listelem)
  {
    return listelem.getList();
  }


  //#########################################################################
  //# Data Members
  private final ObjectFactory mFactory;

}
