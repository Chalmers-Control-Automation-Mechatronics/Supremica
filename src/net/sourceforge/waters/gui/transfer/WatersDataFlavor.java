//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters GUI
//# PACKAGE: net.sourceforge.waters.gui.transfer
//# CLASS:   WatersDataFlavor
//###########################################################################
//# $Id: WatersDataFlavor.java,v 1.3 2007-12-12 23:57:49 robi Exp $
//###########################################################################


package net.sourceforge.waters.gui.transfer;

import java.awt.datatransfer.DataFlavor;


/**
 * A collection of constants representing the various data flavours
 * that can be copied, pasted, dragged and dropped in the IDE.
 *
 * @author Robi Malik
 */

public class WatersDataFlavor
{

  public static final DataFlavor EVENTDECL_LIST =
    new DataFlavor(EventDeclTransferable.class, "List<EventDeclProxy>");

  /**
   * The data flavour for a graph. It is implemented as a {@link
   * ProxyTransferable} that cointains a single object of type {@link
   * net.sourceforge.waters.model.GraphProxy}.
   */
  public static final DataFlavor GRAPH =
    new DataFlavor(GraphTransferable.class, "GraphProxy");

  /**
   * The data flavour for a guard/action block. It is implemented as a
   * {@link ProxyTransferable} that cointains a single object of type
   * {@link net.sourceforge.waters.model.GuardActionBlockProxy}.
   */
  public static final DataFlavor GUARD_ACTION_BLOCK =
    new DataFlavor(GuardActionBlockTransferable.class,
                   "GuardActionBlockProxy");

  /**
   * The data flavour for a list of event labels, as found on an edge of a
   * graph. It is implemented as a {@link ProxyTransferable} and contains a
   * list of objects of type {@link
   * net.sourceforge.waters.model.IdentifierProxy} or {@link
   * net.sourceforge.waters.model.ForeachEventProxy}
   */
  public static final DataFlavor IDENTIFIER_LIST =
    new DataFlavor(IdentifierTransferable.class, "List<IdentifierProxy*>");

  /**
   * The data flavour for a list of module components, as contained in the
   * components list tree-view. It is implemented as a {@link
   * ProxyTransferable} and contains a list of objects of type {@link
   * net.sourceforge.waters.model.SimpleComponentProxy}, {@link
   * net.sourceforge.waters.model.VariableComponentProxy}, {@link
   * net.sourceforge.waters.model.InstanceProxy}, or {@link
   * net.sourceforge.waters.model.ForeachComponentProxy}.
   */
  public static final DataFlavor MODULE_COMPONENT_LIST =
    new DataFlavor(ComponentTransferable.class, "List<ComponentProxy*>");

  /**
   * The data flavour for a list of parameter bindings, as contained in the
   * components list tree-view. It is implemented as a {@link
   * ProxyTransferable} and contains a list of objects of type {@link
   * net.sourceforge.waters.model.ParameterBindingProxy}.
   */
  public static final DataFlavor PARAMETER_BINDING_LIST =
    new DataFlavor(ParameterBindingTransferable.class,
                   "List<ParameterBindingProxy>");

  public static final DataFlavor PRODUCT_DES =
    new DataFlavor(ProductDESTransferable.class, "ProductDESProxy");

}