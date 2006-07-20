//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.model.marshaller
//# CLASS:   GraphNodeListHandler
//###########################################################################
//# $Id: GraphNodeListHandler.java,v 1.3 2006-07-20 02:28:37 robi Exp $
//###########################################################################

package net.sourceforge.waters.model.marshaller;

import java.util.List;

import net.sourceforge.waters.model.module.NodeProxy;

import net.sourceforge.waters.xsd.module.Graph;
import net.sourceforge.waters.xsd.module.NodeList;
import net.sourceforge.waters.xsd.module.ObjectFactory;


class GraphNodeListHandler
  extends JAXBCheckedListHandler<Graph,NodeList,NodeProxy>
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
  NodeList createListElement(final Graph container)
  {
    final NodeList listelem = mFactory.createNodeList();
    container.setNodeList(listelem);
    return listelem;
  }

  NodeList getListElement(final Graph container)
  {
    return container.getNodeList();
  }

  List getList(final NodeList listelem)
  {
    return listelem.getList();
  }


  //#########################################################################
  //# Data Members
  private final ObjectFactory mFactory;

}
