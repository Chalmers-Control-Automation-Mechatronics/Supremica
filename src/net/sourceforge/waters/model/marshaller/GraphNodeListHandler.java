//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.model.marshaller
//# CLASS:   GraphNodeListHandler
//###########################################################################
//# $Id: GraphNodeListHandler.java,v 1.2 2005-11-03 01:24:16 robi Exp $
//###########################################################################

package net.sourceforge.waters.model.marshaller;

import java.util.List;
import javax.xml.bind.JAXBException;

import net.sourceforge.waters.model.module.NodeProxy;

import net.sourceforge.waters.xsd.module.GraphType;
import net.sourceforge.waters.xsd.module.NodeListType;
import net.sourceforge.waters.xsd.module.ObjectFactory;


class GraphNodeListHandler
  extends JAXBCheckedListHandler<GraphType,NodeListType,NodeProxy>
{


  //#########################################################################
  //# Constructors
  GraphNodeListHandler()
  {
    this(null);
  }

  GraphNodeListHandler(final ObjectFactory factory)
  {
    mFactory = factory;
  }


  //#########################################################################
  //# Overrides for Abstract Base Class JAXBCheckedListHandler
  NodeListType createListElement(final GraphType container)
    throws JAXBException
  {
    final NodeListType listelem = mFactory.createNodeList();
    container.setNodeList(listelem);
    return listelem;
  }

  NodeListType getListElement(final GraphType container)
  {
    return container.getNodeList();
  }

  List getList(final NodeListType listelem)
  {
    return listelem.getList();
  }


  //#########################################################################
  //# Data Members
  private final ObjectFactory mFactory;

}
