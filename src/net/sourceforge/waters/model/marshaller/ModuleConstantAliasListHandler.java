//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.model.marshaller
//# CLASS:   ModuleConstantAliasListHandler
//###########################################################################
//# $Id: ModuleConstantAliasListHandler.java,v 1.2 2005-11-03 01:24:16 robi Exp $
//###########################################################################

package net.sourceforge.waters.model.marshaller;

import java.util.List;
import javax.xml.bind.JAXBException;

import net.sourceforge.waters.model.module.AliasProxy;

import net.sourceforge.waters.xsd.module.ConstantAliasListType;
import net.sourceforge.waters.xsd.module.ModuleType;
import net.sourceforge.waters.xsd.module.ObjectFactory;


class ModuleConstantAliasListHandler
  extends JAXBListHandler<ModuleType,ConstantAliasListType,AliasProxy>
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
  ConstantAliasListType createListElement(final ModuleType container)
    throws JAXBException
  {
    final ConstantAliasListType listelem = mFactory.createConstantAliasList();
    container.setConstantAliasList(listelem);
    return listelem;
  }

  ConstantAliasListType getListElement(final ModuleType container)
  {
    return container.getConstantAliasList();
  }

  List getList(final ConstantAliasListType listelem)
  {
    return listelem.getList();
  }


  //#########################################################################
  //# Data Members
  private final ObjectFactory mFactory;

}
