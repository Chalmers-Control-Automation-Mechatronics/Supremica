//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters GUI
//# PACKAGE: net.sourceforge.waters.gui.transfer
//# CLASS:   ComponentTransferable
//###########################################################################
//# $Id$
//###########################################################################


package net.sourceforge.waters.gui.transfer;

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

public class ComponentTransferable extends ProxyTransferable<Proxy>
{

  //#########################################################################
  //# Constructor
  /**
   * Creates a transferable that holds a single event declaration.
   */
  public ComponentTransferable(final Proxy data)
  {
    super(WatersDataFlavor.MODULE_COMPONENT_LIST, data);
  }

  /**
   * Creates a transferable that holds a whole list of event declarations.
   */
  public ComponentTransferable(final List<? extends Proxy> data)
  {
    super(WatersDataFlavor.MODULE_COMPONENT_LIST, data);
  }

}