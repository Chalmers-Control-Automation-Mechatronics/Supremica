//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters GUI
//# PACKAGE: net.sourceforge.waters.gui.transfer
//# CLASS:   GraphDataFlavor
//###########################################################################
//# $Id$
//###########################################################################


package net.sourceforge.waters.gui.transfer;

import gnu.trove.THashSet;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import net.sourceforge.waters.model.base.Proxy;
import net.sourceforge.waters.model.base.VisitorException;
import net.sourceforge.waters.model.module.AbstractModuleProxyVisitor;
import net.sourceforge.waters.model.module.EdgeProxy;
import net.sourceforge.waters.model.module.GraphProxy;
import net.sourceforge.waters.model.module.LabelBlockProxy;
import net.sourceforge.waters.model.module.ModuleProxyCloner;
import net.sourceforge.waters.model.module.ModuleProxyFactory;
import net.sourceforge.waters.model.module.NodeProxy;
import net.sourceforge.waters.plain.module.ModuleElementFactory;
import net.sourceforge.waters.subject.module.LabelBlockSubject;


/**
 * A general data flavour for graphs ({@link GraphProxy}) or parts of graphs.
 * The data in a transferable supporting this flavour consists of a single
 * graph containing the nodes and edges to be transferred.
 *
 * @author Robi Malik
 */

public class GraphDataFlavor extends ModuleDataFlavor
{

  //#########################################################################
  //# Constructor
  GraphDataFlavor()
  {
    super(GraphProxy.class);
  }


  //#########################################################################
  //# Importing and Exporting Data
  @Override
  List<Proxy> createExportData(final Collection<? extends Proxy> data)
  {
    final Proxy graph = VISITOR.createGraph(data);
    return Collections.singletonList(graph);
  }

  @Override
  List<Proxy> createImportData(final Collection<? extends Proxy> data,
                               final ModuleProxyFactory factory)
  {
    final ModuleProxyFactory rootFactory = ModuleElementFactory.getInstance();
    final ModuleProxyCloner cloner = factory.getCloner();
    final int size = data.size();
    final List<Proxy> result = new ArrayList<Proxy>(size);
    for (final Proxy proxy : data) {
      if (proxy instanceof GraphProxy) {
        final GraphProxy graph = (GraphProxy) proxy;
        final GraphProxy cloned = cloner.getClonedGraph(graph, rootFactory);
        result.add(cloned);
      } else {
        final Proxy cloned = cloner.getClone(proxy);
        result.add(cloned);
      }
    }
    return result;
  }


  //#########################################################################
  //# Inner Class GraphExportVisitor
  private static class GraphExportVisitor
    extends AbstractModuleProxyVisitor
  {

    //#######################################################################
    //# Invocation
    private Proxy createGraph(final Collection<? extends Proxy> data)
    {
      try {
        mTransferredBlock = null;
        mTransferredNodes = new LinkedList<NodeProxy>();
        mTransferredEdges = new LinkedList<EdgeProxy>();
        visitCollection(data);
        final Set<NodeProxy> nodes = new THashSet<NodeProxy>(mTransferredNodes);
        final Iterator<EdgeProxy> iter = mTransferredEdges.iterator();
        while (iter.hasNext()) {
          final EdgeProxy edge = iter.next();
          final NodeProxy source = edge.getSource();
          final NodeProxy target = edge.getTarget();
          if (!nodes.contains(source) || !nodes.contains(target)) {
            iter.remove();
          }
        }
        final ModuleProxyFactory factory = ModuleElementFactory.getInstance();
        final GraphProxy graph = factory.createGraphProxy
          (false, mTransferredBlock, mTransferredNodes, mTransferredEdges);
        final ModuleProxyCloner cloner = factory.getCloner();
        return cloner.getClone(graph);
      } catch (final VisitorException exception) {
        throw exception.getRuntimeException();
      } finally {
        mTransferredBlock = null;
        mTransferredNodes = null;
        mTransferredEdges = null;
      }
    }

    //#######################################################################
    //# Interface net.sourceforge.waters.model.module.ModuleProxyVisitor
    @Override
    public Object visitEdgeProxy(final EdgeProxy edge)
    {
      mTransferredEdges.add(edge);
      return null;
    }

    @Override
    public Object visitLabelBlockProxy(final LabelBlockProxy block)
    {
      final List<Proxy> list = block.getEventList();
      final LabelBlockSubject subject = (LabelBlockSubject) block;
      if (list.isEmpty() || subject.getParent() instanceof GraphProxy) {
        mTransferredBlock = block;
      }
      return null;
    }

    @Override
    public Object visitNodeProxy(final NodeProxy node)
    {
      mTransferredNodes.add(node);
      return null;
    }

    //#######################################################################
    //# Data Members
    private LabelBlockProxy mTransferredBlock;
    private List<NodeProxy> mTransferredNodes;
    private List<EdgeProxy> mTransferredEdges;
  }


  //#########################################################################
  //# Data Members
  private final GraphExportVisitor VISITOR = new GraphExportVisitor();

}