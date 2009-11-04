//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.model.marshaller
//# CLASS:   TraceAutomatonRefListHandler
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.model.marshaller;

import java.util.List;

import net.sourceforge.waters.model.des.AutomatonProxy;

import net.sourceforge.waters.xsd.des.AutomatonRef;
import net.sourceforge.waters.xsd.des.TraceType;
import net.sourceforge.waters.xsd.des.AutomatonRefList;
import net.sourceforge.waters.xsd.des.ObjectFactory;


class TraceAutomatonRefListHandler
  extends JAXBCheckedListHandler<TraceType,AutomatonRefList,AutomatonProxy>
{
  
  //#########################################################################
  //# Constructors
  TraceAutomatonRefListHandler()
  {
    this(null);
  }

  TraceAutomatonRefListHandler(final ObjectFactory factory)
  {
    mFactory = factory;
  }

  
  //#########################################################################
  //# Overrides for Abstract Base Class JAXBListHandler
  AutomatonRefList createListElement(TraceType container)
  {
    final AutomatonRefList listelem = mFactory.createAutomatonRefList();
    container.setAutomatonRefList(listelem);
    return listelem;
  }

  AutomatonRefList getListElement(TraceType container)
  {
    return container.getAutomatonRefList();
  }

  List<AutomatonRef> getList(AutomatonRefList listelem)
  {
    return listelem.getList();
  }


  //#########################################################################
  //# Data Members
  private final ObjectFactory mFactory;

}
