//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters GUI
//# PACKAGE: net.sourceforge.waters.gui.transfer
//# CLASS:   NodeLabelDataFlavor
//###########################################################################
//# $Id$
//###########################################################################


package net.sourceforge.waters.gui.transfer;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import net.sourceforge.waters.model.base.Proxy;
import net.sourceforge.waters.model.module.ModuleProxyFactory;
import net.sourceforge.waters.model.module.NodeProxy;
import net.sourceforge.waters.model.module.SimpleIdentifierProxy;
import net.sourceforge.waters.plain.module.ModuleElementFactory;
import net.sourceforge.waters.subject.module.LabelGeometrySubject;


/**
 * <P>The data flavour for a list of node labels.</P>
 *
 * <P>Node labels are represented in a graph editor panel using
 * {@link LabelGeometryProxy} objects, which are converted to
 * {@link net.sourceforge.waters.model.module.SimpleIdentifierProxy
 * SimpleIdentifierProxy} objects in the transferable.</P>
 *
 * <P>This is a dummy data flavour, not directly supported by any panel.
 * Pasting of node labels is done as text, using the default string
 * data flavour provided by {@link ProxyTransferable}.</P>
 *
 * @author Robi Malik
 */

class NodeLabelDataFlavor extends ModuleDataFlavor
{

  //#########################################################################
  //# Constructor
  NodeLabelDataFlavor()
  {
    super(SimpleIdentifierProxy.class);
  }


  //#########################################################################
  //# Importing and Exporting Data
  @Override
  List<WatersDataFlavor> reduceDataFlavorList
    (final List<WatersDataFlavor> flavors)
  {
    final int size = flavors.size();
    final List<WatersDataFlavor> reduced =
      new ArrayList<WatersDataFlavor>(size);
    for (final WatersDataFlavor flavor : flavors) {
      if (flavor != WatersDataFlavor.GRAPH) {
        reduced.add(flavor);
      }
    }
    return reduced;
  }

  @Override
  List<Proxy> createExportData(final Collection<? extends Proxy> data)
  {
    final ModuleProxyFactory factory = ModuleElementFactory.getInstance();
    final int size = data.size();
    final List<Proxy> exportList = new ArrayList<Proxy>(size);
    for (final Proxy proxy : data) {
      final LabelGeometrySubject geo = (LabelGeometrySubject) proxy;
      final NodeProxy node = (NodeProxy) geo.getParent();
      final String name = node.getName();
      final SimpleIdentifierProxy ident =
        factory.createSimpleIdentifierProxy(name);
      exportList.add(ident);
    }
    return exportList;
  }

}