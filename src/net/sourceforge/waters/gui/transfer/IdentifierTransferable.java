//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters GUI
//# PACKAGE: net.sourceforge.waters.gui.transfer
//# CLASS:   IdentifierTransferable
//###########################################################################
//# $Id$
//###########################################################################


package net.sourceforge.waters.gui.transfer;

import java.util.List;

import net.sourceforge.waters.model.base.Proxy;
import net.sourceforge.waters.model.module.IdentifierProxy;


/**
 * <P>A transferable that can hold a list of WATERS identifier ({@link
 * IdentifierProxy}) objects.</P>
 *
 * <P>All the functionality of this transferable is included in the base
 * class {@link ProxyTransferable}. The extension is only done to have
 * a separate class for the data flavour.</P>
 *
 * @author Robi Malik
 */

public class IdentifierTransferable extends ProxyTransferable<Proxy>
{

  //#########################################################################
  //# Constructor
  /**
   * Creates a transferable that holds a single identifier.
   */
  public IdentifierTransferable(final IdentifierProxy data)
  {
    super(WatersDataFlavor.IDENTIFIER_LIST, data);
  }

  /**
   * Creates a transferable that holds a whole list of identifiers.
   */
  public IdentifierTransferable(final List<? extends Proxy> data)
  {
    super(WatersDataFlavor.IDENTIFIER_LIST, data);
  }

}