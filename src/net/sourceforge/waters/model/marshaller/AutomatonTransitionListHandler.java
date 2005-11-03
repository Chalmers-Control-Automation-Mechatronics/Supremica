//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.model.marshaller
//# CLASS:   AutomatonTransitionListHandler
//###########################################################################
//# $Id: AutomatonTransitionListHandler.java,v 1.2 2005-11-03 01:24:16 robi Exp $
//###########################################################################

package net.sourceforge.waters.model.marshaller;

import java.util.List;
import javax.xml.bind.JAXBException;

import net.sourceforge.waters.model.des.TransitionProxy;

import net.sourceforge.waters.xsd.des.AutomatonType;
import net.sourceforge.waters.xsd.des.ObjectFactory;
import net.sourceforge.waters.xsd.des.TransitionListType;


class AutomatonTransitionListHandler
  extends JAXBListHandler<AutomatonType,TransitionListType,TransitionProxy>
{
  
  //#########################################################################
  //# Constructors
  AutomatonTransitionListHandler()
  {
    this(null);
  }

  AutomatonTransitionListHandler(final ObjectFactory factory)
  {
    mFactory = factory;
  }

  
  //#########################################################################
  //# Overrides for Abstract Base Class JAXBListHandler
  TransitionListType createListElement(AutomatonType container)
    throws JAXBException
  {
    final TransitionListType listelem = mFactory.createTransitionList();
    container.setTransitionList(listelem);
    return listelem;
  }

  TransitionListType getListElement(AutomatonType container)
  {
    return container.getTransitionList();
  }

  List getList(TransitionListType listelem)
  {
    return listelem.getList();
  }


  //#########################################################################
  //# Data Members
  private final ObjectFactory mFactory;

}
