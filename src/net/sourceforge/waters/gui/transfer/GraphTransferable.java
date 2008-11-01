//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters GUI
//# PACKAGE: net.sourceforge.waters.gui.transfer
//# CLASS:   GraphTransferable
//###########################################################################
//# $Id$
//###########################################################################


package net.sourceforge.waters.gui.transfer;

import net.sourceforge.waters.model.module.GraphProxy;


/**
 * <P>A transferable that can hold a WATERS graph ({@link GraphProxy})
 * object.</P>
 *
 * <P>All the functionality of this transferable is included in the base
 * class {@link ProxyTransferable}. The extension is only done to have
 * a separate class for the data flavour.</P>
 *
 * @author Robi Malik
 */

public class GraphTransferable extends ProxyTransferable
{

  //#########################################################################
  //# Constructor
  /**
   * Creates a transferable that holds a single graph.
   */
  public GraphTransferable(final GraphProxy data)
  {
    super(WatersDataFlavor.GRAPH, data);
  }

}