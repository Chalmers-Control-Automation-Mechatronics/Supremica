//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters GUI
//# PACKAGE: net.sourceforge.waters.gui.transfer
//# CLASS:   EdgeDataFlavor
//###########################################################################
//# $Id$
//###########################################################################


package net.sourceforge.waters.gui.transfer;

import java.util.ArrayList;
import java.util.List;
import net.sourceforge.waters.model.module.EdgeProxy;


/**
 * A data flavour representing a collection of edges ({@link EdgeProxy}) of a
 * graph. Edges have a separate data flavour to support the insertion of edges
 * with source and target nodes already in the target graph. In contrast,
 * {@link GraphDataFlavor} supports copy and past of subgraphs with nodes and
 * edges found in the transferable.
 *
 * @author Robi Malik
 */

class EdgeDataFlavor extends ModuleDataFlavor
{

  //#########################################################################
  //# Constructor
  EdgeDataFlavor()
  {
    super(EdgeProxy.class);
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

}