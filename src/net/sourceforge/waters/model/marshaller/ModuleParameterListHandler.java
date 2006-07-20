//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.model.marshaller
//# CLASS:   ModuleParameterListHandler
//###########################################################################
//# $Id: ModuleParameterListHandler.java,v 1.3 2006-07-20 02:28:37 robi Exp $
//###########################################################################

package net.sourceforge.waters.model.marshaller;

import java.util.List;

import net.sourceforge.waters.model.module.ParameterProxy;

import net.sourceforge.waters.xsd.module.Module;
import net.sourceforge.waters.xsd.module.ObjectFactory;
import net.sourceforge.waters.xsd.module.ParameterList;


class ModuleParameterListHandler
  extends JAXBListHandler<Module,ParameterList,ParameterProxy>
{


  //#########################################################################
  //# Constructors
  ModuleParameterListHandler()
  {
    this(null);
  }

  ModuleParameterListHandler(final ObjectFactory factory)
  {
    mFactory = factory;
  }


  //#########################################################################
  //# Overrides for Abstract Base Class JAXBListHandler
  ParameterList createListElement(final Module container)
  {
    final ParameterList listelem = mFactory.createParameterList();
    container.setParameterList(listelem);
    return listelem;
  }

  ParameterList getListElement(final Module container)
  {
    return container.getParameterList();
  }

  List getList(final ParameterList listelem)
  {
    return listelem.getList();
  }


  //#########################################################################
  //# Data Members
  private final ObjectFactory mFactory;

}
