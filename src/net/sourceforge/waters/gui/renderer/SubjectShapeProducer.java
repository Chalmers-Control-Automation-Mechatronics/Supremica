//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# Copyright (C) 2004-2020 Robi Malik
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

package net.sourceforge.waters.gui.renderer;

import net.sourceforge.waters.model.base.Proxy;
import net.sourceforge.waters.model.base.VisitorException;
import net.sourceforge.waters.model.compiler.context.BindingContext;
import net.sourceforge.waters.model.compiler.context.SimpleExpressionCompiler;
import net.sourceforge.waters.model.module.BinaryExpressionProxy;
import net.sourceforge.waters.model.module.DefaultModuleProxyVisitor;
import net.sourceforge.waters.model.module.EdgeProxy;
import net.sourceforge.waters.model.module.EventDeclProxy;
import net.sourceforge.waters.model.module.EventListExpressionProxy;
import net.sourceforge.waters.model.module.GraphProxy;
import net.sourceforge.waters.model.module.GuardActionBlockProxy;
import net.sourceforge.waters.model.module.LabelBlockProxy;
import net.sourceforge.waters.model.module.LabelGeometryProxy;
import net.sourceforge.waters.model.module.NestedBlockProxy;
import net.sourceforge.waters.model.module.NodeProxy;
import net.sourceforge.waters.model.module.SimpleExpressionProxy;
import net.sourceforge.waters.model.module.SimpleNodeProxy;
import net.sourceforge.waters.subject.base.ModelChangeEvent;
import net.sourceforge.waters.subject.base.ModelObserver;
import net.sourceforge.waters.subject.base.Subject;
import net.sourceforge.waters.subject.base.SubjectTools;
import net.sourceforge.waters.subject.module.EdgeSubject;
import net.sourceforge.waters.subject.module.EventDeclSubject;
import net.sourceforge.waters.subject.module.GraphSubject;
import net.sourceforge.waters.subject.module.GuardActionBlockSubject;
import net.sourceforge.waters.subject.module.LabelBlockSubject;
import net.sourceforge.waters.subject.module.LabelGeometrySubject;
import net.sourceforge.waters.subject.module.ModuleSubject;
import net.sourceforge.waters.subject.module.SimpleExpressionSubject;
import net.sourceforge.waters.subject.module.SimpleNodeSubject;


public class SubjectShapeProducer
  extends ProxyShapeProducer
  implements ModelObserver
{

  //#########################################################################
  //# Constructors
  public SubjectShapeProducer(final GraphSubject graph,
                              final ModuleSubject module,
                              final RenderingContext context)
  {
    this(graph, graph, module, context);
  }

  public SubjectShapeProducer(final GraphProxy graph,
                              final Subject subject,
                              final ModuleSubject module,
                              final RenderingContext context)
  {
    this(graph, subject, module, context, null, null);
  }

  public SubjectShapeProducer(final GraphSubject graph,
                              final ModuleSubject module,
                              final RenderingContext context,
                              final SimpleExpressionCompiler compiler,
                              final BindingContext bindings)
  {
    this(graph, graph, module, context, compiler, bindings);
  }

  public SubjectShapeProducer(final GraphProxy graph,
                              final Subject subject,
                              final ModuleSubject module,
                              final RenderingContext context,
                              final SimpleExpressionCompiler compiler,
                              final BindingContext bindings)
  {
    super(graph, context, compiler, bindings);
    mSubject = subject;
    subject.addModelObserver(this);
    mModule = module;
    if (module != null) {
      module.getEventDeclListModifiable().addModelObserver(this);
    }
    mRemoveMappingVisitor = new RemoveMappingVisitor();
  }


  //#########################################################################
  //# Clean up
  @Override
  public void close()
  {
    if (mModule != null) {
      mModule.getEventDeclListModifiable().removeModelObserver(this);
    }
    mSubject.removeModelObserver(this);
    super.close();
  }


  //#########################################################################
  //# Interface net.sourceforge.waters.subject.base.ModelObserver
  @Override
  public void modelChanged(final ModelChangeEvent event)
  {
    final RenderingContext context = getRenderingContext();
    final GraphProxy graph = getGraph();
    if (context.causesPropositionStatusChange(event, graph)) {
      for (final NodeProxy node : graph.getNodes()) {
        if (node instanceof SimpleNodeProxy) {
          removeMapping(node);
        }
      }
    }

    final Subject esource = event.getSource();
    switch (event.getKind()) {
    case ModelChangeEvent.ITEM_REMOVED:
    case ModelChangeEvent.ITEM_ADDED:
      final Subject parent = esource.getParent();
      if (parent instanceof EventListExpressionProxy) {
        final Subject grandparent = parent.getParent();
        if (grandparent instanceof SimpleNodeProxy) {
          removeMapping((SimpleNodeProxy) grandparent);
        } else if (parent instanceof LabelBlockProxy) {
          removeMapping((LabelBlockProxy) parent);
        }
      } else if (parent instanceof GuardActionBlockProxy) {
        removeMapping((GuardActionBlockProxy) parent);
      } else if (parent instanceof GraphProxy) {
        removeMapping((Subject) event.getValue());
      } else if (parent instanceof NestedBlockProxy) {
        removeMapping((LabelBlockProxy) SubjectTools.
                      getAncestor(parent, LabelBlockSubject.class));
      }
      break;
    case ModelChangeEvent.NAME_CHANGED:
      if (esource instanceof SimpleNodeProxy) {
        final SimpleNodeProxy node = (SimpleNodeProxy) esource;
        removeMapping(node.getLabelGeometry());
      } else {
        removeMapping(esource);
      }
      break;
    case ModelChangeEvent.STATE_CHANGED:
      if (esource instanceof EdgeProxy ||
          esource instanceof SimpleNodeProxy){
        removeMapping(esource);
      } else if (esource instanceof SimpleExpressionProxy) {
        if (isEventNameChange(esource)) {
          removeLabelBlockMappings();
          removeMarkedNodeMappings(); // if renaming from/to :forbidden
        } else {
          final SimpleExpressionSubject expr =
            (SimpleExpressionSubject) esource;
          final Subject ancestor =
            SubjectTools.getAncestor(expr, SimpleNodeSubject.class,
                                     LabelBlockSubject.class);
          if (ancestor != null) {
            removeMapping(ancestor);
          }
        }
      } else if (esource instanceof EventDeclProxy) {
        removeLabelBlockMappings();
        removeMarkedNodeMappings(); // if renaming from/to :forbidden
      } else if (esource == graph &&
                 graph.getBlockedEvents() == null &&
                 mOldBlockedEventsList != null) {
        removeMapping(mOldBlockedEventsList);
        mOldBlockedEventsList = null;
      }
      break;
    case ModelChangeEvent.GEOMETRY_CHANGED:
      if (esource instanceof EventDeclProxy) {
        removeMarkedNodeMappings();
      } else if (esource instanceof NodeProxy) {
        for (final EdgeProxy edge : graph.getEdges()) {
          if (edge.getSource() == esource || edge.getTarget() == esource) {
            removeMapping(edge);
          }
        }
        removeMapping(esource);
      } else {
        removeMapping(esource);
      }
      break;
    case ModelChangeEvent.GENERAL_NOTIFICATION:
      break;
    default:
      removeMapping(esource);
    }
  }

  @Override
  public int getModelObserverPriority()
  {
    return ModelObserver.CLEANUP_PRIORITY_0;
  }


  //#########################################################################
  //# Smarter Lookup Using Parents
  @Override
  public GuardActionBlockProxyShape visitGuardActionBlockProxy
    (final GuardActionBlockProxy block)
  {
    final GuardActionBlockProxyShape shape =
      (GuardActionBlockProxyShape) lookup(block);
    if (shape != null) {
      return shape;
    } else {
      final GuardActionBlockSubject subject = (GuardActionBlockSubject) block;
      final EdgeSubject edge = (EdgeSubject) subject.getParent();
      final EdgeProxyShape eshape = visitEdgeProxy(edge);
      return createGuardActionBlockShape(block, eshape);
    }
  }

  @Override
  public LabelBlockProxyShape visitLabelBlockProxy(final LabelBlockProxy block)
  {
    final LabelBlockProxyShape shape = (LabelBlockProxyShape) lookup(block);
    if (shape != null) {
      return shape;
    } else {
      final LabelBlockSubject subject = (LabelBlockSubject) block;
      final Subject parent = subject.getParent();
      if (parent instanceof EdgeProxy) {
        final EdgeProxy edge = (EdgeProxy) parent;
        final EdgeProxyShape eshape = visitEdgeProxy(edge);
        return createLabelBlockShape(block, eshape);
      } else {
        mOldBlockedEventsList = subject;
        return createLabelBlockShape(block, null);
      }
    }
  }

  @Override
  public LabelShape visitLabelGeometryProxy(final LabelGeometryProxy geo)
  {
    final LabelShape shape = (LabelShape) lookup(geo);
    if (shape != null) {
      return shape;
    } else {
      final LabelGeometrySubject subject = (LabelGeometrySubject) geo;
      final SimpleNodeSubject node = (SimpleNodeSubject) subject.getParent();
      return createNodeLabelShape(geo, node);
    }
  }


  //#########################################################################
  //# Cleaning the Cache
  private void removeLabelBlockMappings()
  {
    final GraphProxy graph = getGraph();
    for (final EdgeProxy edge : graph.getEdges()) {
      final LabelBlockProxy block = edge.getLabelBlock();
      removeMapping(block);
    }
    final LabelBlockProxy block = graph.getBlockedEvents();
    if (block != null) {
      removeMapping(block);
    }
  }

  private void removeMarkedNodeMappings()
  {
    for (final NodeProxy node : getGraph().getNodes()) {
      final EventListExpressionProxy props = node.getPropositions();
      if (!props.getEventIdentifierList().isEmpty()) {
        removeMapping(node);
      }
    }
  }

  private void removeMapping(final Subject subject)
  {
    final Proxy proxy = (Proxy) subject;
    removeMapping(proxy);
  }

  private void removeMapping(final Proxy proxy)
  {
    mRemoveMappingVisitor.removeMapping(proxy);
  }


  //#########################################################################
  //# Auxiliary Methods
  private boolean isEventNameChange(final Subject esource)
  {
    final EventDeclSubject decl =
      SubjectTools.getAncestor(esource, EventDeclSubject.class);
    if (decl != null) {
      return SubjectTools.isAncestor(decl.getIdentifier(), esource);
    } else {
      return false;
    }
  }


  //#########################################################################
  //# Inner Class RemoveMappingVisitor
  private class RemoveMappingVisitor
    extends DefaultModuleProxyVisitor
  {

    //#######################################################################
    //# Invocation
    private void removeMapping(final Proxy proxy)
    {
      try {
        proxy.acceptVisitor(this);
      } catch (final VisitorException exception) {
        throw exception.getRuntimeException();
      }
    }

    //#######################################################################
    //# Interface net.sourceforge.waters.model.base.ProxyVisitor
    @Override
    public Object visitProxy(final Proxy proxy)
    {
      unmap(proxy);
      return null;
    }

    //#######################################################################
    //# Interface net.sourceforge.waters.model.module.ModuleProxyVisitor
    @Override
    public Object visitEdgeProxy(final EdgeProxy edge)
      throws VisitorException
    {
      visitProxy(edge);
      visitLabelBlockProxy(edge.getLabelBlock());
      final GuardActionBlockProxy block = edge.getGuardActionBlock();
      if (block != null) {
        visitGuardActionBlockProxy(block);
      }
      return null;
    }

    @Override
    public Object visitGuardActionBlockProxy(final GuardActionBlockProxy block)
      throws VisitorException
    {
      visitProxy(block);
      for (final SimpleExpressionProxy guard : block.getGuards()) {
        guard.acceptVisitor(this);
      }
      for (final BinaryExpressionProxy action : block.getActions()) {
        visitBinaryExpressionProxy(action);
      }
      return null;
    }

    @Override
    public Object visitLabelBlockProxy(final LabelBlockProxy block)
      throws VisitorException
    {
      visitProxy(block);
      for (final Proxy proxy : block.getEventIdentifierList()) {
        proxy.acceptVisitor(this);
      }
      return null;
    }

    @Override
    public Object visitSimpleNodeProxy(final SimpleNodeProxy node)
      throws VisitorException
    {
      visitNodeProxy(node);
      visitLabelGeometryProxy(node.getLabelGeometry());
      return null;
    }

  }

  //#########################################################################
  //# Data Members
  private final Subject mSubject;
  private final ModuleSubject mModule;
  private final RemoveMappingVisitor mRemoveMappingVisitor;

  private LabelBlockProxy mOldBlockedEventsList = null;

}
