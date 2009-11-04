//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.model.marshaller
//# CLASS:   NodeEventListHandler
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.model.marshaller;

import java.util.List;

import net.sourceforge.waters.model.base.Proxy;

import net.sourceforge.waters.xsd.base.ElementType;
import net.sourceforge.waters.xsd.module.NodeType;
import net.sourceforge.waters.xsd.module.EventListType;
import net.sourceforge.waters.xsd.module.ObjectFactory;


class NodeEventListHandler
  extends JAXBListHandler<NodeType,EventListType,Proxy>
{


  //#########################################################################
  //# Constructors
  NodeEventListHandler()
  {
    this(null);
  }

  NodeEventListHandler(final ObjectFactory factory)
  {
    mFactory = factory;
  }


  //#########################################################################
  //# Overrides for Abstract Base Class JAXBCheckedListHandler
  EventListType createListElement(final NodeType container)
  {
    final EventListType listelem = mFactory.createEventListType();
    container.setPropositions(listelem);
    return listelem;
  }

  EventListType getListElement(final NodeType container)
  {
    return container.getPropositions();
  }

  List<ElementType> getList(final EventListType listelem)
  {
    return listelem.getList();
  }


  //#########################################################################
  //# Data Members
  private final ObjectFactory mFactory;

}
