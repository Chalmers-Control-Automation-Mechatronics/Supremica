//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.model.marshaller
//# CLASS:   GraphEdgeListHandler
//###########################################################################
//# $Id: GraphEdgeListHandler.java,v 1.2 2005-11-03 01:24:16 robi Exp $
//###########################################################################

package net.sourceforge.waters.model.marshaller;

import java.util.List;
import javax.xml.bind.JAXBException;

import net.sourceforge.waters.model.module.EdgeProxy;

import net.sourceforge.waters.xsd.module.EdgeListType;
import net.sourceforge.waters.xsd.module.GraphType;
import net.sourceforge.waters.xsd.module.ObjectFactory;


class GraphEdgeListHandler
  extends JAXBListHandler<GraphType,EdgeListType,EdgeProxy>
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
  EdgeListType createListElement(final GraphType container)
    throws JAXBException
  {
    final EdgeListType listelem = mFactory.createEdgeList();
    container.setEdgeList(listelem);
    return listelem;
  }

  EdgeListType getListElement(final GraphType container)
  {
    return container.getEdgeList();
  }

  List getList(final EdgeListType listelem)
  {
    return listelem.getList();
  }


  //#########################################################################
  //# Data Members
  private final ObjectFactory mFactory;

}
