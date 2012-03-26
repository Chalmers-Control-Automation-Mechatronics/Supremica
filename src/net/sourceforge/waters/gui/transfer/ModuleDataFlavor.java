//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters GUI
//# PACKAGE: net.sourceforge.waters.gui.transfer
//# CLASS:   ModuleDataFlavor
//###########################################################################
//# $Id$
//###########################################################################


package net.sourceforge.waters.gui.transfer;

import java.util.Collection;
import java.util.List;

import net.sourceforge.waters.model.base.Proxy;
import net.sourceforge.waters.model.base.ProxyCloner;
import net.sourceforge.waters.model.module.ModuleProxyFactory;
import net.sourceforge.waters.plain.module.ModuleElementFactory;


/**
 * A general data flavour for items contained in a module.
 *
 * @author Robi Malik
 */

public class ModuleDataFlavor extends WatersDataFlavor
{

  //#########################################################################
  //# Constructor
  ModuleDataFlavor(final Class<? extends Proxy> clazz)
  {
    super(clazz);
  }


  //#########################################################################
  //# Importing and Exporting Data
  @Override
  List<Proxy> createExportData(final Collection<? extends Proxy> data)
  {
    final ProxyCloner cloner = ModuleElementFactory.getCloningInstance();
    return cloner.getClonedList(data);
  }

  @Override
  List<Proxy> createImportData(final Collection<? extends Proxy> data,
                               final ModuleProxyFactory factory)
  {
    final ProxyCloner cloner = factory.getCloner();
    return cloner.getClonedList(data);
  }

}