//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters GUI
//# PACKAGE: net.sourceforge.waters.gui.transfer
//# CLASS:   WatersDataFlavor
//###########################################################################
//# $Id$
//###########################################################################


package net.sourceforge.waters.gui.transfer;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import net.sourceforge.waters.model.base.Proxy;
import net.sourceforge.waters.model.base.VisitorException;
import net.sourceforge.waters.model.module.AbstractModuleProxyVisitor;
import net.sourceforge.waters.model.module.ComponentProxy;
import net.sourceforge.waters.model.module.ConstantAliasProxy;
import net.sourceforge.waters.model.module.EdgeProxy;
import net.sourceforge.waters.model.module.EventAliasProxy;
import net.sourceforge.waters.model.module.EventDeclProxy;
import net.sourceforge.waters.model.module.ExpressionProxy;
import net.sourceforge.waters.model.module.ForeachProxy;
import net.sourceforge.waters.model.module.GraphProxy;
import net.sourceforge.waters.model.module.IdentifierProxy;
import net.sourceforge.waters.model.module.LabelBlockProxy;
import net.sourceforge.waters.model.module.NodeProxy;
import net.sourceforge.waters.model.module.ParameterBindingProxy;
import net.sourceforge.waters.plain.module.GraphElement;
import net.sourceforge.waters.subject.module.LabelBlockSubject;


/**
 * A collection of constants representing the various data flavours
 * that can be copied, pasted, dragged and dropped in the IDE.
 *
 * @author Robi Malik
 */

public class WatersDataFlavor
{
  /**
   * The data flavour for a constant alias
   */
  public static final DataFlavor CONSTANT_ALIAS_LIST =
    new DataFlavor(ConstantAliasTransferable.class, "List<ConstantAliasProxy>");

  public static final DataFlavor EVENT_ALIAS_LIST =
    new DataFlavor(EventAliasTransferable.class, "List<EventAliasProxy>");

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

  public static final DataFlavor TYPELESS_FOREACH =
    new DataFlavor(TypelessForeachTransferable.class, "List<Proxy>");

  public static Transferable createTransferable(final Proxy proxy){
    final List<? extends Proxy> items = Collections.singletonList(proxy);
    return createTransferable(items);
  }

  @SuppressWarnings("unchecked")
  public static Transferable createTransferable(final List<? extends Proxy> items){
     final DataFlavor dataFlavor = getDataFlavor(items);
    if (dataFlavor == WatersDataFlavor.CONSTANT_ALIAS_LIST) {
      return new ConstantAliasTransferable(items);
    } else if (dataFlavor == WatersDataFlavor.MODULE_COMPONENT_LIST) {
      return new ComponentTransferable(items);
    } else if (dataFlavor == WatersDataFlavor.IDENTIFIER_LIST) {
      return new IdentifierTransferable(items);
    } else if (dataFlavor == WatersDataFlavor.EVENT_ALIAS_LIST) {
      return new EventAliasTransferable(items);
    } else if (dataFlavor == WatersDataFlavor.EVENTDECL_LIST) {
      return new EventDeclTransferable((List<EventDeclProxy>) items);
    } else if (dataFlavor == WatersDataFlavor.TYPELESS_FOREACH) {
      return new TypelessForeachTransferable(items);
    } else if (dataFlavor == WatersDataFlavor.PARAMETER_BINDING_LIST) {
      return new ParameterBindingTransferable(items);
    } else if (dataFlavor == WatersDataFlavor.GRAPH) {
      return mGraphTransferableVisitor.createTransferable(items);
    } else
      return null;
  }

  public static DataFlavor getDataFlavor(final List<? extends Proxy> items)
  {
    DataFlavor common = null;
    for (final Proxy proxy : items) {
      final DataFlavor flavor =
        mExportedDataFlavorVisitor.getDataFlavor(proxy);
      if (common == null) {
        common = flavor;
      } else if (common != flavor) {
        return null;
      }
    }
    return common;
  }

  //#########################################################################
  //# Inner Class ExportedDataFlavorVisitor
  private static class ExportedDataFlavorVisitor extends AbstractModuleProxyVisitor
  {
    //#######################################################################
    //# Invocation
    private DataFlavor getDataFlavor(final Proxy proxy)
    {
      try {
        return (DataFlavor) proxy.acceptVisitor(this);
      } catch (final VisitorException exception) {
        throw exception.getRuntimeException();
      }
    }

    //#######################################################################
    //# Interface net.sourceforge.waters.model.module.ModuleProxyVisitor
    @Override
    public DataFlavor visitComponentProxy(final ComponentProxy comp)
    {
      return WatersDataFlavor.MODULE_COMPONENT_LIST;
    }

    @Override
    public DataFlavor visitConstantAliasProxy(final ConstantAliasProxy alias)
    {
      return WatersDataFlavor.CONSTANT_ALIAS_LIST;
    }

    @Override
    public DataFlavor visitEdgeProxy(final EdgeProxy alias)
    {
      return WatersDataFlavor.GRAPH;
    }

    @Override
    public DataFlavor visitEventAliasProxy(final EventAliasProxy alias)
    {
      return WatersDataFlavor.EVENT_ALIAS_LIST;
    }

    @Override
    public DataFlavor visitEventDeclProxy(final EventDeclProxy alias)
    {
      return WatersDataFlavor.EVENTDECL_LIST;
    }

    @Override
    public Object visitExpressionProxy(final ExpressionProxy proxy)
      throws VisitorException
    {
      return WatersDataFlavor.IDENTIFIER_LIST;
    }

    @Override
    public Object visitForeachProxy(final ForeachProxy foreach)
    {
      if (foreach.getBody().isEmpty()) {
        return WatersDataFlavor.TYPELESS_FOREACH;
      } else {
        final List<Proxy> list = foreach.getBody();
        for (int i = 0; i < list.size(); i++) {
          final DataFlavor flavor = getDataFlavor(list.get(i));
          if (flavor != WatersDataFlavor.TYPELESS_FOREACH) {
            return flavor;
          }
        }
      }
      return WatersDataFlavor.TYPELESS_FOREACH;
    }

    @Override
    public Object visitIdentifierProxy(final IdentifierProxy proxy)
      throws VisitorException
    {
      return WatersDataFlavor.IDENTIFIER_LIST;
    }

    @Override
    public DataFlavor visitLabelBlockProxy(final LabelBlockProxy alias)
    {
      return WatersDataFlavor.GRAPH;
    }

    @Override
    public DataFlavor visitNodeProxy(final NodeProxy alias)
    {
      return WatersDataFlavor.GRAPH;
    }

    @Override
    public DataFlavor visitParameterBindingProxy(final ParameterBindingProxy binding)
    {
      return WatersDataFlavor.PARAMETER_BINDING_LIST;
    }
  }

  //#########################################################################
  //# Inner Class GraphTransferableVisitor
  private static class GraphTransferableVisitor
    extends AbstractModuleProxyVisitor
  {

    //#######################################################################
    //# Invocation
    private ProxyTransferable<?> createTransferable
      (final List<? extends Proxy> list)
    {
      mTransferredBlock = null;
      mTransferredNodes = new HashSet<NodeProxy>();
      mTransferredEdges = new LinkedList<EdgeProxy>();
      for (final Proxy proxy : list) {
        try {
          proxy.acceptVisitor(this);
        } catch (final VisitorException exception) {
          throw exception.getRuntimeException();
        }
      }
      final Iterator<EdgeProxy> iter = mTransferredEdges.iterator();
      while (iter.hasNext()) {
        final EdgeProxy edge = iter.next();
        final NodeProxy source = edge.getSource();
        final NodeProxy target = edge.getTarget();
        if (!mTransferredNodes.contains(source) ||
            !mTransferredNodes.contains(target)) {
          iter.remove();
        }
      }
      final GraphProxy graph = new GraphElement
        (false, mTransferredBlock, mTransferredNodes, mTransferredEdges);
      final ProxyTransferable<?> transferable = new GraphTransferable(graph);
      mTransferredBlock = null;
      mTransferredNodes = null;
      mTransferredEdges = null;
      return transferable;
    }

    //#######################################################################
    //# Interface net.sourceforge.waters.model.base.ProxyVisitor
    public Object visitProxy(final Proxy proxy)
    {
      return null;
    }

    //#######################################################################
    //# Interface net.sourceforge.waters.model.module.ModuleProxyVisitor
    public Object visitEdgeProxy(final EdgeProxy edge)
    {
      mTransferredEdges.add(edge);
      return null;
    }

    public Object visitLabelBlockProxy(final LabelBlockProxy block)
    {
      final LabelBlockSubject subject = (LabelBlockSubject) block;
      if (subject.getParent() instanceof GraphProxy) {
        mTransferredBlock = block;
      }
      return null;
    }

    public Object visitNodeProxy(final NodeProxy node)
    {
      mTransferredNodes.add(node);
      return null;
    }

    //#######################################################################
    //# Data Members
    private LabelBlockProxy mTransferredBlock;
    private Collection<NodeProxy> mTransferredNodes;
    private Collection<EdgeProxy> mTransferredEdges;
  }


  private static GraphTransferableVisitor mGraphTransferableVisitor = new GraphTransferableVisitor();
  private static ExportedDataFlavorVisitor mExportedDataFlavorVisitor = new ExportedDataFlavorVisitor();

}