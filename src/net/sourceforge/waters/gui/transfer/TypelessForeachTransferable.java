//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters GUI
//# PACKAGE: net.sourceforge.waters.gui.transfer
//# CLASS:   ComponentTransferable
//###########################################################################
//# $Id$
//###########################################################################


package net.sourceforge.waters.gui.transfer;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;
import java.util.List;

import net.sourceforge.waters.model.base.Proxy;


/**
 * <P>A transferable that can hold a list of WATERS module components.</P>
 *
 * <P>This transferable can hold all types of objects that can occur
 * in a module's component list, i.e,</P>
 * <UL>
 * <LI> {@link net.sourceforge.waters.model.module.SimpleComponentProxy}
 * <LI> {@link net.sourceforge.waters.model.module.VariableComponentProxy}
 * <LI> {@link net.sourceforge.waters.model.module.InstanceProxy}
 * <LI> {@link net.sourceforge.waters.model.module.ForeachComponentProxy}
 * </UL>
 *
 * <P>All the functionality of this transferable is included in the base
 * class {@link ProxyTransferable}. The extension is only done to have
 * a separate class for the data flavour.</P>
 *
 * @author Robi Malik
 */

public class TypelessForeachTransferable extends ProxyTransferable<Proxy>
{

  //#########################################################################
  //# Constructor
  /**
   * Creates a transferable that holds a single foreach block.
   */
  public TypelessForeachTransferable(final Proxy data)
  {
    super(FLAVORS, data);
  }

  /**
   * Creates a transferable that holds a whole list of foreach blocks.
   */
  public TypelessForeachTransferable(final List<? extends Proxy> data)
  {
    super(FLAVORS, data);
  }


  //#########################################################################
  //# Interface java.awt.datatransfer.Transferable
  @Override
  public Object getTransferData(final DataFlavor flavor)
    throws IOException, UnsupportedFlavorException
  {
    if (WatersDataFlavor.EVENT_ALIAS_LIST.equals(flavor) ||
      WatersDataFlavor.MODULE_COMPONENT_LIST.equals(flavor) ||
      WatersDataFlavor.IDENTIFIER_LIST.equals(flavor)) {
      return getRawData();
    } else {
      return super.getTransferData(flavor);
    }
  }

  //#########################################################################
  //# Class Constants
  private static final DataFlavor[] FLAVORS = {
    WatersDataFlavor.TYPELESS_FOREACH,
    WatersDataFlavor.EVENT_ALIAS_LIST,
    WatersDataFlavor.IDENTIFIER_LIST,
    WatersDataFlavor.MODULE_COMPONENT_LIST,
    DataFlavor.stringFlavor
  };

}