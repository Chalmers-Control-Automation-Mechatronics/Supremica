//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.model.marshaller
//# CLASS:   ModuleConstantAliasListHandler
//###########################################################################
//# $Id: ModuleConstantAliasListHandler.java,v 1.3 2006-07-20 02:28:37 robi Exp $
//###########################################################################

package net.sourceforge.waters.model.marshaller;

import java.util.List;

import net.sourceforge.waters.model.module.AliasProxy;

import net.sourceforge.waters.xsd.module.ConstantAliasList;
import net.sourceforge.waters.xsd.module.Module;
import net.sourceforge.waters.xsd.module.ObjectFactory;


class ModuleConstantAliasListHandler
  extends JAXBListHandler<Module,ConstantAliasList,AliasProxy>
{


  //#########################################################################
  //# Constructors
  ModuleConstantAliasListHandler()
  {
    this(null);
  }

  ModuleConstantAliasListHandler(final ObjectFactory factory)
  {
    mFactory = factory;
  }


  //#########################################################################
  //# Overrides for Abstract Base Class JAXBListHandler
  ConstantAliasList createListElement(final Module container)
  {
    final ConstantAliasList listelem = mFactory.createConstantAliasList();
    container.setConstantAliasList(listelem);
    return listelem;
  }

  ConstantAliasList getListElement(final Module container)
  {
    return container.getConstantAliasList();
  }

  List getList(final ConstantAliasList listelem)
  {
    return listelem.getList();
  }


  //#########################################################################
  //# Data Members
  private final ObjectFactory mFactory;

}
