//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.gui.renderer
//# CLASS:   SubjectShapeProducer
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.gui.renderer;

import net.sourceforge.waters.model.base.Proxy;
import net.sourceforge.waters.model.base.VisitorException;
import net.sourceforge.waters.model.compiler.context.BindingContext;
import net.sourceforge.waters.model.compiler.context.SimpleExpressionCompiler;
import net.sourceforge.waters.model.module.AbstractModuleProxyVisitor;
import net.sourceforge.waters.model.module.BinaryExpressionProxy;
import net.sourceforge.waters.model.module.EdgeProxy;
import net.sourceforge.waters.model.module.EventDeclProxy;
import net.sourceforge.waters.model.module.EventListExpressionProxy;
import net.sourceforge.waters.model.module.GraphProxy;
import net.sourceforge.waters.model.module.GuardActionBlockProxy;
import net.sourceforge.waters.model.module.LabelBlockProxy;
import net.sourceforge.waters.model.module.LabelGeometryProxy;
import net.sourceforge.waters.model.module.NodeProxy;
import net.sourceforge.waters.model.module.SimpleExpressionProxy;
import net.sourceforge.waters.model.module.SimpleNodeProxy;
import net.sourceforge.waters.subject.base.ModelChangeEvent;
import net.sourceforge.waters.subject.base.ModelObserver;
import net.sourceforge.waters.subject.base.Subject;
import net.sourceforge.waters.subject.module.EdgeSubject;
import net.sourceforge.waters.subject.module.GraphSubject;
import net.sourceforge.waters.subject.module.GuardActionBlockSubject;
import net.sourceforge.waters.subject.module.LabelBlockSubject;
import net.sourceforge.waters.subject.module.LabelGeometrySubject;
import net.sourceforge.waters.subject.module.ModuleSubject;
import net.sourceforge.waters.subject.module.SimpleNodeSubject;


public class SubjectShapeProducer
  extends ProxyShapeProducer
  implements ModelObserver
{

  //##########################################################################
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


  //##########################################################################
  //# Clean up
  public void close()
  {
    if (mModule != null) {
      mModule.getEventDeclListModifiable().removeModelObserver(this);
    }
    mSubject.removeModelObserver(this);
    super.close();
  }


  //##########################################################################
  //# Cleaning the Cache
  private void removeMapping(final Subject subject)
  {
    final Proxy proxy = (Proxy) subject;
    removeMapping(proxy);
  }

  private void removeMapping(final Proxy proxy)
  {
    mRemoveMappingVisitor.removeMapping(proxy);
  }


  //##########################################################################
  //# Interface net.sourceforge.waters.subject.base.ModelObserver
  public void modelChanged(final ModelChangeEvent event)
  {
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
      if (esource instanceof EdgeProxy || esource instanceof SimpleNodeProxy) {
        removeMapping(esource);
      } else if (esource == getGraph() &&
                 getGraph().getBlockedEvents() == null &&
                 mOldBlockedEventsList != null) {
        removeMapping(mOldBlockedEventsList);
        mOldBlockedEventsList = null;
      }
      break;
    case ModelChangeEvent.GEOMETRY_CHANGED:
      if (esource instanceof EventDeclProxy) {
        for (final NodeProxy node : getGraph().getNodes()) {
          final EventListExpressionProxy props = node.getPropositions();
          if (!props.getEventList().isEmpty()) {
            removeMapping(node);
          }
        }
      } else if (esource instanceof NodeProxy) {
        for (final EdgeProxy edge : getGraph().getEdges()) {
          if (edge.getSource() == esource || edge.getTarget() == esource) {
            removeMapping(edge);
          }
        }
        removeMapping(esource);
      } else {
        removeMapping(esource);
      }
      break;
    default:
      removeMapping(esource);
    }
  }


  //##########################################################################
  //# Smarter Lookup Using Parents
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

  public LabelProxyShape visitLabelGeometryProxy(final LabelGeometryProxy geo)
  {
    final LabelProxyShape shape = (LabelProxyShape) lookup(geo);
    if (shape != null) {
      return shape;
    } else {
      final LabelGeometrySubject subject = (LabelGeometrySubject) geo;
      final SimpleNodeSubject node = (SimpleNodeSubject) subject.getParent();
      return createNodeLabelProxyShape(geo, node);
    }
  }


  //##########################################################################
  //# Inner Class RemoveMappingVisitor
  private class RemoveMappingVisitor
    extends AbstractModuleProxyVisitor
  {

    //########################################################################
    //# Invocation
    private void removeMapping(final Proxy proxy)
    {
      try {
        proxy.acceptVisitor(this);
      } catch (final VisitorException exception) {
        throw exception.getRuntimeException();
      }
    }

    //########################################################################
    //# Interface net.sourceforge.waters.model.base.ProxyVisitor
    public Object visitProxy(final Proxy proxy)
    {
      unmap(proxy);
      return null;
    }

    //########################################################################
    //# Interface net.sourceforge.waters.model.module.ModuleProxyVisitor
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

    public Object visitLabelBlockProxy(final LabelBlockProxy block)
      throws VisitorException
    {
      visitProxy(block);
      for (final Proxy proxy : block.getEventList()) {
        proxy.acceptVisitor(this);
      }
      return null;
    }

    public Object visitSimpleNodeProxy(final SimpleNodeProxy node)
      throws VisitorException
    {
      visitNodeProxy(node);
      visitLabelGeometryProxy(node.getLabelGeometry());
      return null;
    }

  }

  //##########################################################################
  //# Data Members
  private final Subject mSubject;
  private final ModuleSubject mModule;
  private final RemoveMappingVisitor mRemoveMappingVisitor;

  private LabelBlockProxy mOldBlockedEventsList = null;

}
