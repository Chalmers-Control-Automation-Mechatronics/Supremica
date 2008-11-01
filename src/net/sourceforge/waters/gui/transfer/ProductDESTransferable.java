//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters GUI
//# PACKAGE: net.sourceforge.waters.gui.transfer
//# CLASS:   ProductDESTransferable
//###########################################################################
//# $Id$
//###########################################################################


package net.sourceforge.waters.gui.transfer;

import net.sourceforge.waters.model.des.ProductDESProxy;


/**
 * <P>A transferable that can hold a WATERS product DES ({@link
 * ProductDESProxy}) object.</P>
 *
 * <P>All the functionality of this transferable is included in the base
 * class {@link ProxyTransferable}. The extension is only done to have
 * a separate class for the data flavour.</P>
 *
 * @author Robi Malik
 */

public class ProductDESTransferable
  extends ProxyTransferable<ProductDESProxy>
{

  //#########################################################################
  //# Constructor
  /**
   * Creates a transferable that holds a single product DES.
   */
  public ProductDESTransferable(final ProductDESProxy data)
  {
    super(WatersDataFlavor.PRODUCT_DES, data);
  }

}