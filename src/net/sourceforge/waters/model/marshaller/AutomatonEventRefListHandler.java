//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.model.marshaller
//# CLASS:   AutomatonEventRefListHandler
//###########################################################################
//# $Id: AutomatonEventRefListHandler.java,v 1.2 2005-11-03 01:24:16 robi Exp $
//###########################################################################

package net.sourceforge.waters.model.marshaller;

import java.util.List;
import javax.xml.bind.JAXBException;

import net.sourceforge.waters.model.des.EventProxy;

import net.sourceforge.waters.xsd.des.AutomatonType;
import net.sourceforge.waters.xsd.des.EventRefListType;
import net.sourceforge.waters.xsd.des.ObjectFactory;


class AutomatonEventRefListHandler
  extends JAXBCheckedListHandler<AutomatonType,EventRefListType,EventProxy>
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
  EventRefListType createListElement(AutomatonType container)
    throws JAXBException
  {
    final EventRefListType listelem = mFactory.createEventRefList();
    container.setEventRefList(listelem);
    return listelem;
  }

  EventRefListType getListElement(AutomatonType container)
  {
    return container.getEventRefList();
  }

  List getList(EventRefListType listelem)
  {
    return listelem.getList();
  }


  //#########################################################################
  //# Data Members
  private final ObjectFactory mFactory;

}
