//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.model.marshaller
//# CLASS:   AutomatonEventRefListHandler
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.model.marshaller;

import java.util.List;

import net.sourceforge.waters.model.des.EventProxy;

import net.sourceforge.waters.xsd.des.Automaton;
import net.sourceforge.waters.xsd.des.EventRefList;
import net.sourceforge.waters.xsd.des.ObjectFactory;


class AutomatonEventRefListHandler
  extends JAXBCheckedListHandler<Automaton,EventRefList,EventProxy>
{
  
  //#########################################################################
  //# Constructors
  AutomatonEventRefListHandler()
  {
    this(null);
  }

  AutomatonEventRefListHandler(final ObjectFactory factory)
  {
    mFactory = factory;
  }

  
  //#########################################################################
  //# Overrides for Abstract Base Class JAXBListHandler
  EventRefList createListElement(Automaton container)
  {
    final EventRefList listelem = mFactory.createEventRefList();
    container.setEventRefList(listelem);
    return listelem;
  }

  EventRefList getListElement(Automaton container)
  {
    return container.getEventRefList();
  }

  List<?> getList(EventRefList listelem)
  {
    return listelem.getList();
  }


  //#########################################################################
  //# Data Members
  private final ObjectFactory mFactory;

}
