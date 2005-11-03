//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.model.marshaller
//# CLASS:   ModuleParameterListHandler
//###########################################################################
//# $Id: ModuleParameterListHandler.java,v 1.2 2005-11-03 01:24:16 robi Exp $
//###########################################################################

package net.sourceforge.waters.model.marshaller;

import java.util.List;
import javax.xml.bind.JAXBException;

import net.sourceforge.waters.model.module.ParameterProxy;

import net.sourceforge.waters.xsd.module.ModuleType;
import net.sourceforge.waters.xsd.module.ObjectFactory;
import net.sourceforge.waters.xsd.module.ParameterListType;


class ModuleParameterListHandler
  extends JAXBListHandler<ModuleType,ParameterListType,ParameterProxy>
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
  ParameterListType createListElement(final ModuleType container)
    throws JAXBException
  {
    final ParameterListType listelem = mFactory.createParameterList();
    container.setParameterList(listelem);
    return listelem;
  }

  ParameterListType getListElement(final ModuleType container)
  {
    return container.getParameterList();
  }

  List getList(final ParameterListType listelem)
  {
    return listelem.getList();
  }


  //#########################################################################
  //# Data Members
  private final ObjectFactory mFactory;

}
