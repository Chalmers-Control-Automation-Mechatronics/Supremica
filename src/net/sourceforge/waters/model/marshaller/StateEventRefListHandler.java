//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.model.marshaller
//# CLASS:   StateEventRefListHandler
//###########################################################################
//# $Id: StateEventRefListHandler.java,v 1.3 2006-07-20 02:28:37 robi Exp $
//###########################################################################

package net.sourceforge.waters.model.marshaller;

import java.util.List;

import net.sourceforge.waters.model.des.EventProxy;

import net.sourceforge.waters.xsd.des.EventRefList;
import net.sourceforge.waters.xsd.des.ObjectFactory;
import net.sourceforge.waters.xsd.des.State;


class StateEventRefListHandler
  extends JAXBCheckedListHandler<State,EventRefList,EventProxy>
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
  EventRefList createListElement(State container)
  {
    final EventRefList listelem = mFactory.createEventRefList();
    container.setPropositions(listelem);
    return listelem;
  }

  EventRefList getListElement(State container)
  {
    return container.getPropositions();
  }

  List getList(EventRefList listelem)
  {
    return listelem.getList();
  }


  //#########################################################################
  //# Data Members
  private final ObjectFactory mFactory;

}
