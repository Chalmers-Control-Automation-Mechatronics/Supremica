//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.model.marshaller
//# CLASS:   GraphEdgeListHandler
//###########################################################################
//# $Id: GraphEdgeListHandler.java,v 1.3 2006-07-20 02:28:37 robi Exp $
//###########################################################################

package net.sourceforge.waters.model.marshaller;

import java.util.List;

import net.sourceforge.waters.model.module.EdgeProxy;

import net.sourceforge.waters.xsd.module.EdgeList;
import net.sourceforge.waters.xsd.module.Graph;
import net.sourceforge.waters.xsd.module.ObjectFactory;


class GraphEdgeListHandler
  extends JAXBListHandler<Graph,EdgeList,EdgeProxy>
{


  //#########################################################################
  //# Constructors
  GraphEdgeListHandler()
  {
    this(null);
  }

  GraphEdgeListHandler(final ObjectFactory factory)
  {
    mFactory = factory;
  }


  //#########################################################################
  //# Overrides for Abstract Base Class JAXBListHandler
  EdgeList createListElement(final Graph container)
  {
    final EdgeList listelem = mFactory.createEdgeList();
    container.setEdgeList(listelem);
    return listelem;
  }

  EdgeList getListElement(final Graph container)
  {
    return container.getEdgeList();
  }

  List getList(final EdgeList listelem)
  {
    return listelem.getList();
  }


  //#########################################################################
  //# Data Members
  private final ObjectFactory mFactory;

}
