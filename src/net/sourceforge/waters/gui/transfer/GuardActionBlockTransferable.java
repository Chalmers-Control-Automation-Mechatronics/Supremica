//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters GUI
//# PACKAGE: net.sourceforge.waters.gui.transfer
//# CLASS:   GuardActionBlockTransferable
//###########################################################################
//# $Id$
//###########################################################################


package net.sourceforge.waters.gui.transfer;

import net.sourceforge.waters.model.module.GuardActionBlockProxy;


/**
 * <P>A transferable that can hold a WATERS guard/action block ({@link
 * GuardActionBlockProxy}) object.</P>
 *
 * <P>All the functionality of this transferable is included in the base
 * class {@link ProxyTransferable}. The extension is only done to have
 * a separate class for the data flavour.</P>
 *
 * @author Robi Malik
 */

public class GuardActionBlockTransferable
  extends ProxyTransferable<GuardActionBlockProxy>
{

  //#########################################################################
  //# Constructor
  /**
   * Creates a transferable that holds a single guard/action block.
   */
  public GuardActionBlockTransferable(final GuardActionBlockProxy data)
  {
    super(WatersDataFlavor.GUARD_ACTION_BLOCK, data);
  }

}