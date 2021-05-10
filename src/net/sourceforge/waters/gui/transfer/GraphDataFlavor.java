//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# Copyright (C) 2004-2021 Robi Malik
//###########################################################################
//# This file is part of Waters.
//# Waters is free software: you can redistribute it and/or modify it under
//# the terms of the GNU General Public License as published by the Free
//# Software Foundation, either version 2 of the License, or (at your option)
//# any later version.
//# Waters is distributed in the hope that it will be useful, but WITHOUT ANY
//# WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
//# FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
//# details.
//# You should have received a copy of the GNU General Public License along
//# with Waters. If not, see <http://www.gnu.org/licenses/>.
//#
//# Linking Waters statically or dynamically with other modules is making a
//# combined work based on Waters. Thus, the terms and conditions of the GNU
//# General Public License cover the whole combination.
//# In addition, as a special exception, the copyright holders of Waters give
//# you permission to combine Waters with code included in the standard
//# release of Supremica under the Supremica Software License Agreement (or
//# modified versions of such code, with unchanged license). You may copy and
//# distribute such a system following the terms of the GNU GPL for Waters and
//# the licenses of the other code concerned.
//# Note that people who make modified versions of Waters are not obligated to
//# grant this special exception for their modified versions; it is their
//# choice whether to do so. The GNU General Public License gives permission
//# to release a modified version without this exception; this exception also
//# makes it possible to release a modified version which carries forward this
//# exception.
//###########################################################################

package net.sourceforge.waters.gui.transfer;

import gnu.trove.set.hash.THashSet;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import net.sourceforge.waters.gui.ModuleContext;
import net.sourceforge.waters.model.base.Proxy;
import net.sourceforge.waters.model.base.VisitorException;
import net.sourceforge.waters.model.module.DefaultModuleProxyVisitor;
import net.sourceforge.waters.model.module.EdgeProxy;
import net.sourceforge.waters.model.module.GraphProxy;
import net.sourceforge.waters.model.module.LabelBlockProxy;
import net.sourceforge.waters.model.module.ModuleProxyCloner;
import net.sourceforge.waters.model.module.ModuleProxyFactory;
import net.sourceforge.waters.model.module.NodeProxy;
import net.sourceforge.waters.plain.module.ModuleElementFactory;
import net.sourceforge.waters.subject.module.LabelBlockSubject;
import net.sourceforge.waters.subject.module.ModuleSubjectFactory;


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
  List<Proxy> createExportData(final Collection<? extends Proxy> data,
                               final ModuleContext context)
  {
    final Proxy graph = VISITOR.createGraph(data);
    return Collections.singletonList(graph);
  }

  @Override
  List<Proxy> createImportData(final Collection<? extends Proxy> data)
  {
    final ModuleProxyFactory factory = ModuleSubjectFactory.getInstance();
    final ModuleProxyCloner cloner = factory.getCloner();
    final ModuleProxyFactory rootFactory = ModuleElementFactory.getInstance();
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
    extends DefaultModuleProxyVisitor
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
      final List<Proxy> list = block.getEventIdentifierList();
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
