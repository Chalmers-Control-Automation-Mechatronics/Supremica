//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.model.marshaller
//# CLASS:   ModuleConstantAliasListHandler
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.model.marshaller;

import java.util.List;

import net.sourceforge.waters.model.module.ModuleProxy;
import net.sourceforge.waters.xsd.module.Module;
import net.sourceforge.waters.xsd.module.ModuleSequence;


class ModuleSequenceListHandler
  extends JAXBListHandler<ModuleSequence,ModuleSequence,ModuleProxy>
{


  //#########################################################################
  //# Constructors
  ModuleSequenceListHandler()
  {
  }


  //#########################################################################
  //# Overrides for Abstract Base Class JAXBListHandler
  ModuleSequence createListElement(final ModuleSequence container)
  {
    return container;
  }

  ModuleSequence getListElement(final ModuleSequence container)
  {
    return container;
  }

  List<Module> getList(final ModuleSequence listelem)
  {
    return listelem.getList();
  }

}
