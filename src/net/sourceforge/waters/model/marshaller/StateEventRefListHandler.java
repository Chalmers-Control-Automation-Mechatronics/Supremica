//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.model.marshaller
//# CLASS:   StateEventRefListHandler
//###########################################################################
//# $Id: StateEventRefListHandler.java,v 1.2 2005-11-03 01:24:16 robi Exp $
//###########################################################################

package net.sourceforge.waters.model.marshaller;

import java.util.List;
import javax.xml.bind.JAXBException;

import net.sourceforge.waters.model.des.EventProxy;

import net.sourceforge.waters.xsd.des.EventRefListType;
import net.sourceforge.waters.xsd.des.ObjectFactory;
import net.sourceforge.waters.xsd.des.StateType;


class StateEventRefListHandler
  extends JAXBCheckedListHandler<StateType,EventRefListType,EventProxy>
{
  
  //#########################################################################
  //# Constructors
  StateEventRefListHandler()
  {
    this(null);
  }

  StateEventRefListHandler(final ObjectFactory factory)
  {
    mFactory = factory;
  }

  
  //#########################################################################
  //# Overrides for Abstract Base Class JAXBListHandler
  EventRefListType createListElement(StateType container)
    throws JAXBException
  {
    final EventRefListType listelem = mFactory.createEventRefList();
    container.setPropositions(listelem);
    return listelem;
  }

  EventRefListType getListElement(StateType container)
  {
    return container.getPropositions();
  }

  List getList(EventRefListType listelem)
  {
    return listelem.getList();
  }


  //#########################################################################
  //# Data Members
  private final ObjectFactory mFactory;

}
