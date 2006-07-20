//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.model.marshaller
//# CLASS:   ForeachEventAliasListHandler
//###########################################################################
//# $Id: ForeachEventAliasListHandler.java,v 1.3 2006-07-20 02:28:37 robi Exp $
//###########################################################################

package net.sourceforge.waters.model.marshaller;

import java.util.List;

import net.sourceforge.waters.model.base.Proxy;

import net.sourceforge.waters.xsd.module.EventAliasList;
import net.sourceforge.waters.xsd.module.ForeachEventAlias;
import net.sourceforge.waters.xsd.module.ObjectFactory;


class ForeachEventAliasListHandler
  extends JAXBListHandler<ForeachEventAlias,EventAliasList,Proxy>
{


  //#########################################################################
  //# Constructors
  ForeachEventAliasListHandler()
  {
    this(null);
  }

  ForeachEventAliasListHandler(final ObjectFactory factory)
  {
    mFactory = factory;
  }


  //#########################################################################
  //# Overrides for Abstract Base Class JAXBListHandler
  EventAliasList createListElement(final ForeachEventAlias container)
  {
    final EventAliasList listelem = mFactory.createEventAliasList();
    container.setEventAliasList(listelem);
    return listelem;
  }

  EventAliasList getListElement(final ForeachEventAlias container)
  {
    return container.getEventAliasList();
  }

  List getList(final EventAliasList listelem)
  {
    return listelem.getList();
  }


  //#########################################################################
  //# Data Members
  private final ObjectFactory mFactory;

}
